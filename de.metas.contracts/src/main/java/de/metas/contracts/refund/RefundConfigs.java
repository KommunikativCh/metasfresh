package de.metas.contracts.refund;

import static de.metas.util.collections.CollectionUtils.extractSingleElement;
import static de.metas.util.collections.CollectionUtils.hasDifferentValues;

import java.util.Comparator;
import java.util.List;

import org.adempiere.exceptions.AdempiereException;

import com.google.common.collect.ImmutableList;

import de.metas.contracts.refund.RefundConfig.RefundMode;
import de.metas.i18n.IMsgBL;
import de.metas.i18n.ITranslatableString;
import de.metas.product.ProductId;
import de.metas.util.Check;
import de.metas.util.Loggables;
import de.metas.util.Services;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/*
 * #%L
 * de.metas.contracts
 * %%
 * Copyright (C) 2018 metas GmbH
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

@UtilityClass
public class RefundConfigs
{
	private static final String MSG_REFUND_CONFIG_SAME_REFUND_INVOICE_TYPE = "de.metas.constracts.refund.C_Flatrate_RefundConfig_SameRefundInvoiceType";
	private static final String MSG_REFUND_CONFIG_SAME_INVOICE_SCHEDULE = "de.metas.constracts.refund.C_Flatrate_RefundConfig_SameInvoiceSchedule";
	private static final String MSG_REFUND_CONFIG_SAME_REFUND_MODE = "de.metas.constracts.refund.C_Flatrate_RefundConfig_SameRefundMode";
	private static final String MSG_REFUND_CONFIG_SAME_REFUND_BASE = "de.metas.constracts.refund.C_Flatrate_RefundConfig_SameRefundBase";

	public ImmutableList<RefundConfig> sortByMinQtyAsc(@NonNull final List<RefundConfig> refundConfigs)
	{
		// we need to look at the lowest minQty first, in order to "fill" it; only the "biggest" config is does not have the next config's minQty as ceiling
		final ImmutableList<RefundConfig> sortedConfigs = refundConfigs
				.stream()
				.sorted(Comparator.comparing(RefundConfig::getMinQty))
				.collect(ImmutableList.toImmutableList());
		return sortedConfigs;
	}

	public ImmutableList<RefundConfig> sortByMinQtyDesc(@NonNull final List<RefundConfig> refundConfigs)
	{
		final ImmutableList<RefundConfig> sortedConfigs = refundConfigs
				.stream()
				.sorted(Comparator.comparing(RefundConfig::getMinQty).reversed())
				.collect(ImmutableList.toImmutableList());
		return sortedConfigs;
	}

	public RefundConfig largestMinQty(@NonNull final List<RefundConfig> refundConfigs)
	{
		Check.assumeNotEmpty(refundConfigs, "The given refundConfigs may not be empty");

		return refundConfigs
				.stream()
				.max(Comparator.comparing(RefundConfig::getMinQty))
				.get();
	}

	public RefundConfig smallestMinQty(@NonNull final List<RefundConfig> refundConfigs)
	{
		Check.assumeNotEmpty(refundConfigs, "The given refundConfigs may not be empty");

		return refundConfigs
				.stream()
				.min(Comparator.comparing(RefundConfig::getMinQty))
				.get();
	}

	public RefundMode extractRefundMode(@NonNull final List<RefundConfig> refundConfigs)
	{
		final RefundMode refundMode = extractSingleElement(
				refundConfigs,
				RefundConfig::getRefundMode);
		return refundMode;
	}

	public ProductId extractProductId(@NonNull final List<RefundConfig> refundConfigs)
	{
		final ProductId productId = extractSingleElement(
				refundConfigs,
				RefundConfig::getProductId);
		return productId;
	}

	public void assertValid(@NonNull final List<RefundConfig> refundConfigs)
	{
		Check.assumeNotEmpty(refundConfigs, "refundConfigs");

		final IMsgBL msgBL = Services.get(IMsgBL.class);

		if (hasDifferentValues(refundConfigs, RefundConfig::getRefundBase))
		{
			Loggables.addLog("The given refundConfigs need to all have the same RefundBase; refundConfigs={}", refundConfigs);

			final ITranslatableString msg = msgBL.getTranslatableMsgText(MSG_REFUND_CONFIG_SAME_REFUND_BASE);
			throw new AdempiereException(msg).markAsUserValidationError();
		}
		if (hasDifferentValues(refundConfigs, RefundConfig::getRefundMode))
		{
			Loggables.addLog("The given refundConfigs need to all have the same RefundMode; refundConfigs={}", refundConfigs);

			final ITranslatableString msg = msgBL.getTranslatableMsgText(MSG_REFUND_CONFIG_SAME_REFUND_MODE);
			throw new AdempiereException(msg).markAsUserValidationError();
		}

		if (RefundMode.APPLY_TO_ALL_QTIES.equals(extractRefundMode(refundConfigs)))
		{
			// we have one IC with different configs, so those configs need to have the consistent settings
			if (hasDifferentValues(refundConfigs, RefundConfig::getInvoiceSchedule))
			{
				Loggables.addLog(
						"Because refundMode={}, all the given refundConfigs need to all have the same invoiceSchedule; refundConfigs={}",
						RefundMode.APPLY_TO_ALL_QTIES, refundConfigs);

				final ITranslatableString msg = msgBL.getTranslatableMsgText(MSG_REFUND_CONFIG_SAME_INVOICE_SCHEDULE);
				throw new AdempiereException(msg).markAsUserValidationError();
			}
			if (hasDifferentValues(refundConfigs, RefundConfig::getRefundInvoiceType))
			{
				Loggables.addLog(
						"Because refundMode={}, all the given refundConfigs need to all have the same refundInvoiceType; refundConfigs={}",
						RefundMode.APPLY_TO_ALL_QTIES, refundConfigs);

				final ITranslatableString msg = msgBL.getTranslatableMsgText(MSG_REFUND_CONFIG_SAME_REFUND_INVOICE_TYPE);
				throw new AdempiereException(msg).markAsUserValidationError();
			}
		}
	}
}
