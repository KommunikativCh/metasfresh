package de.metas.material.planning.event;

import static org.adempiere.model.InterfaceWrapperHelper.loadOutOfTrx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.mm.attributes.AttributeSetInstanceId;
import org.adempiere.warehouse.WarehouseId;
import org.compiere.model.I_AD_Org;
import org.compiere.model.I_M_Product;
import org.compiere.model.I_M_Warehouse;
import org.compiere.model.I_S_Resource;
import org.compiere.util.Env;
import org.compiere.util.TimeUtil;
import org.eevolution.model.I_PP_Product_Planning;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;

import de.metas.Profiles;
import de.metas.material.event.MaterialEvent;
import de.metas.material.event.MaterialEventHandler;
import de.metas.material.event.PostMaterialEventService;
import de.metas.material.event.commons.EventDescriptor;
import de.metas.material.event.commons.MaterialDescriptor;
import de.metas.material.event.commons.SupplyRequiredDescriptor;
import de.metas.material.event.ddorder.DDOrder;
import de.metas.material.event.supplyrequired.SupplyRequiredEvent;
import de.metas.material.planning.IMRPContextFactory;
import de.metas.material.planning.IMutableMRPContext;
import de.metas.material.planning.IProductPlanningDAO;
import de.metas.material.planning.IProductPlanningDAO.ProductPlanningQuery;
import de.metas.material.planning.ddorder.DDOrderAdvisedEventCreator;
import de.metas.material.planning.ddorder.DDOrderPojoSupplier;
import de.metas.material.planning.pporder.PPOrderAdvisedEventCreator;
import de.metas.organization.OrgId;
import de.metas.product.ProductId;
import de.metas.product.ResourceId;
import de.metas.util.Loggables;
import de.metas.util.Services;
import lombok.NonNull;

/*
 * #%L
 * metasfresh-material-planning
 * %%
 * Copyright (C) 2017 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

@Service
@Profile(Profiles.PROFILE_App) // we want only one component to bother itself with SupplyRequiredEvent
public class SupplyRequiredHandler implements MaterialEventHandler<SupplyRequiredEvent>
{
	private final DDOrderAdvisedEventCreator dDOrderAdvisedEventCreator;
	private final PPOrderAdvisedEventCreator ppOrderAdvisedEventCreator;

	private final PostMaterialEventService postMaterialEventService;

	public SupplyRequiredHandler(
			@NonNull final DDOrderAdvisedEventCreator dDOrderAdvisedEventCreator,
			@NonNull final PPOrderAdvisedEventCreator ppOrderAdvisedEventCreator,
			@NonNull final PostMaterialEventService fireMaterialEventService)
	{
		this.dDOrderAdvisedEventCreator = dDOrderAdvisedEventCreator;
		this.ppOrderAdvisedEventCreator = ppOrderAdvisedEventCreator;
		this.postMaterialEventService = fireMaterialEventService;
	}

	@Override
	public Collection<Class<? extends SupplyRequiredEvent>> getHandeledEventType()
	{
		return ImmutableList.of(SupplyRequiredEvent.class);
	}

	@Override
	public void handleEvent(@NonNull final SupplyRequiredEvent event)
	{
		handleSupplyRequiredEvent(event.getSupplyRequiredDescriptor());
	}

	/**
	 * Invokes our {@link DDOrderPojoSupplier} and returns the resulting {@link DDOrder} pojo as {@link DistributionAdvisedOrCreatedEvent}
	 *
	 * @param materialDemandEvent
	 */
	public void handleSupplyRequiredEvent(@NonNull final SupplyRequiredDescriptor descriptor)
	{
		final IMutableMRPContext mrpContext = createMRPContextOrNull(descriptor);
		if (mrpContext == null)
		{
			return; // nothing to do
		}

		final List<MaterialEvent> events = new ArrayList<>();

		events.addAll(dDOrderAdvisedEventCreator.createDDOrderAdvisedEvents(descriptor, mrpContext));
		events.addAll(ppOrderAdvisedEventCreator.createPPOrderAdvisedEvents(descriptor, mrpContext));

		events.forEach(postMaterialEventService::postEventNow);
	}

	private IMutableMRPContext createMRPContextOrNull(@NonNull final SupplyRequiredDescriptor materialDemandEvent)
	{
		final EventDescriptor eventDescr = materialDemandEvent.getEventDescriptor();

		final MaterialDescriptor materialDescr = materialDemandEvent.getMaterialDescriptor();

		final I_M_Warehouse warehouse = loadOutOfTrx(materialDescr.getWarehouseId(), I_M_Warehouse.class);

		final IProductPlanningDAO productPlanningDAO = Services.get(IProductPlanningDAO.class);

		final I_S_Resource plant = productPlanningDAO.findPlant(
				eventDescr.getOrgId(),
				warehouse,
				materialDescr.getProductId(),
				materialDescr.getAttributeSetInstanceId());

		final ProductPlanningQuery productPlanningQuery = ProductPlanningQuery.builder()
				.orgId(OrgId.ofRepoId(eventDescr.getOrgId()))
				.warehouseId(WarehouseId.ofRepoId(materialDescr.getWarehouseId()))
				.plantId(ResourceId.ofRepoId(plant.getS_Resource_ID()))
				.productId(ProductId.ofRepoId(materialDescr.getProductId()))
				.attributeSetInstanceId(AttributeSetInstanceId.ofRepoId(materialDescr.getAttributeSetInstanceId()))
				.build();

		final I_PP_Product_Planning productPlanning = productPlanningDAO.find(productPlanningQuery);
		if (productPlanning == null)
		{
			Loggables.addLog("No PP_Product_Planning record found; query={}", productPlanningQuery);
			return null;
		}

		final IMRPContextFactory mrpContextFactory = Services.get(IMRPContextFactory.class);
		final IMutableMRPContext mrpContext = mrpContextFactory.createInitialMRPContext();

		final I_M_Product product = loadOutOfTrx(materialDescr.getProductId(), I_M_Product.class);
		mrpContext.setM_Product(product);
		mrpContext.setM_AttributeSetInstance_ID(materialDescr.getAttributeSetInstanceId());
		mrpContext.setM_Warehouse(warehouse);
		mrpContext.setDate(TimeUtil.asDate(materialDescr.getDate()));
		mrpContext.setCtx(Env.getCtx());
		mrpContext.setTrxName(ITrx.TRXNAME_ThreadInherited);

		mrpContext.setProductPlanning(productPlanning);
		mrpContext.setPlant(plant);

		final I_AD_Org org = loadOutOfTrx(eventDescr.getOrgId(), I_AD_Org.class);
		mrpContext.setAD_Client_ID(org.getAD_Client_ID());
		mrpContext.setAD_Org(org);
		return mrpContext;
	}
}
