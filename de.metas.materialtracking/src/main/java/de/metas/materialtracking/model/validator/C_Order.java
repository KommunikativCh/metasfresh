package de.metas.materialtracking.model.validator;

/*
 * #%L
 * de.metas.materialtracking
 * %%
 * Copyright (C) 2015 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */


import java.util.List;

import org.adempiere.ad.modelvalidator.annotations.Interceptor;
import org.adempiere.util.Services;
import org.compiere.model.I_C_Order;
import org.compiere.model.I_C_OrderLine;
import org.compiere.model.I_M_AttributeSetInstance;

import de.metas.adempiere.service.IOrderDAO;

@Interceptor(I_C_Order.class)
public class C_Order extends MaterialTrackableDocumentByASIInterceptor<I_C_Order, I_C_OrderLine>
{
	@Override
	protected final boolean isEligibleForMaterialTracking(final I_C_Order order)
	{
		// Sales orders are not eligible
		if (order.isSOTrx())
		{
			return false;
		}
		
		return true;
	}

	@Override
	protected List<I_C_OrderLine> retrieveDocumentLines(final I_C_Order document)
	{
		// note: we don't have C_Order reversals to check for
		final IOrderDAO orderDAO = Services.get(IOrderDAO.class);
		final List<I_C_OrderLine> documentLines = orderDAO.retrieveOrderLines(document, I_C_OrderLine.class);
		return documentLines;
	}

	@Override
	protected I_M_AttributeSetInstance getM_AttributeSetInstance(final I_C_OrderLine documentLine)
	{
		return documentLine.getM_AttributeSetInstance();
	}
}
