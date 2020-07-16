/*
 * #%L
 * de.metas.business.rest-api-impl
 * %%
 * Copyright (C) 2020 metas GmbH
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

package de.metas.rest_api.shipment;

import de.metas.bpartner.composite.repository.BPartnerCompositeRepository;
import de.metas.business.BusinessTestHelper;
import de.metas.common.shipmentschedule.JsonResponseShipmentCandidates;
import de.metas.inoutcandidate.ShipmentScheduleRepository;
import de.metas.inoutcandidate.exportaudit.ShipmentScheduleAuditRepository;
import de.metas.inoutcandidate.exportaudit.ShipmentScheduleExportStatus;
import de.metas.inoutcandidate.model.I_M_ShipmentSchedule;
import de.metas.location.CountryId;
import de.metas.product.ProductRepository;
import de.metas.util.time.SystemTime;
import org.adempiere.ad.table.MockLogEntriesRepository;
import org.adempiere.service.ClientId;
import org.adempiere.test.AdempiereTestHelper;
import org.assertj.core.api.Assertions;
import org.compiere.model.I_C_BP_Group;
import org.compiere.model.I_C_BPartner;
import org.compiere.model.I_C_BPartner_Location;
import org.compiere.model.I_C_Location;
import org.compiere.model.I_C_UOM;
import org.compiere.model.I_M_Product;
import org.compiere.util.Env;
import org.compiere.util.TimeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.adempiere.model.InterfaceWrapperHelper.newInstance;
import static org.adempiere.model.InterfaceWrapperHelper.saveRecord;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ShipmentCandidateExporterTest
{

	private ShipmentCandidateExporter shipmentCandidateExporter;
	private I_C_BPartner bPartner;
	private I_C_BPartner_Location bPartnerLocation;
	private I_C_BPartner bpartnerOverride;
	private I_C_BPartner_Location bPartnerLocationOverride;
	private I_C_UOM uom;
	private I_M_Product product;

	@BeforeEach
	void beforeEach()
	{
		AdempiereTestHelper.get().init();
		Env.setContext(Env.getCtx(), Env.CTXNAME_AD_Client_ID, ClientId.METASFRESH.getRepoId());

		bPartner = BusinessTestHelper.createBPartner("bpartner");
		bPartnerLocation = BusinessTestHelper.createBPartnerLocation(bPartner);

		bpartnerOverride = BusinessTestHelper.createBPartner("bpartnerOverride");
		bPartnerLocationOverride = BusinessTestHelper.createBPartnerLocation(bpartnerOverride);

		final CountryId countryId = BusinessTestHelper.createCountry("DE");

		final I_C_Location location = newInstance(I_C_Location.class);
		location.setC_Country_ID(countryId.getRepoId());
		saveRecord(location);
		bPartnerLocationOverride.setC_Location_ID(location.getC_Location_ID());
		saveRecord(bPartnerLocationOverride);

		final I_C_BP_Group bpGroup = newInstance(I_C_BP_Group.class);
		saveRecord(bpGroup);

		bpartnerOverride.setC_BP_Group_ID(bpGroup.getC_BP_Group_ID());
		saveRecord(bpartnerOverride);

		uom = BusinessTestHelper.createUOM("stockUOM");
		product = BusinessTestHelper.createProduct("product", uom);

		shipmentCandidateExporter = new ShipmentCandidateExporter(
				new ShipmentScheduleAuditRepository(),
				new ShipmentScheduleRepository(),
				new BPartnerCompositeRepository(new MockLogEntriesRepository()),
				new ProductRepository());
	}

	@Test
	void exportShipmentCandidates()
	{
		// given
		createShipmentScheduleRecord();

		// when
		final JsonResponseShipmentCandidates result = shipmentCandidateExporter.exportShipmentCandidates();

		// then
		assertThat(result.isHasMoreItems()).isFalse();
		assertThat(result.getItems()).hasSize(1);
		assertThat(result.getItems().get(0).getCustomer().getCompanyName()).isEqualTo("bpartnerOverride"); // expecting C_BPartner.Name because companyName is not set
	}

	private I_M_ShipmentSchedule createShipmentScheduleRecord()
	{
		final I_M_ShipmentSchedule record = newInstance(I_M_ShipmentSchedule.class);

		record.setC_BPartner_ID(bPartner.getC_BPartner_ID());
		record.setC_BPartner_Location_ID(bPartnerLocation.getC_BPartner_Location_ID());
		record.setC_BPartner_Override_ID(bpartnerOverride.getC_BPartner_ID());
		record.setC_BP_Location_Override_ID(bPartnerLocationOverride.getC_BPartner_Location_ID());
		record.setM_Product_ID(product.getM_Product_ID());
		record.setCanBeExportedFrom(TimeUtil.asTimestamp(SystemTime.asInstant().minusMillis(1000)));
		record.setExportStatus(ShipmentScheduleExportStatus.Pending.getCode());
		saveRecord(record);

		return record;
	}
}