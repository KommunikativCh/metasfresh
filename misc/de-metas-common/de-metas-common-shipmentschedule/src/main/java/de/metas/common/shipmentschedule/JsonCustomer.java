/*
 * #%L
 * de-metas-common-shipmentschedule
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

package de.metas.common.shipmentschedule;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import javax.annotation.Nullable;

@Value
public class JsonCustomer
{
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	String companyName;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	String contactName;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	String contactEmail;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	String contactPhone;

	String street;

	String streetNo;

	String postal;

	String city;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	String country;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	String deliveryInfo;

	@JsonCreator
	@Builder
	public JsonCustomer(
			@JsonProperty("companyName") @Nullable final String companyName,
			@JsonProperty("contactName") @Nullable final String contactName,
			@JsonProperty("contactEmail") @Nullable final String contactEmail,
			@JsonProperty("contactPhone") @Nullable final String contactPhone,
			@JsonProperty("street") @NonNull final String street,
			@JsonProperty("streetNo") @NonNull final String streetNo,
			@JsonProperty("postal") @NonNull final String postal,
			@JsonProperty("city") @NonNull final String city,
			@JsonProperty("country") @Nullable final String country,
			@JsonProperty("deliveryInfo") @Nullable final String deliveryInfo)
	{
		this.companyName = companyName;
		this.contactName = contactName;
		this.contactEmail = contactEmail;
		this.contactPhone = contactPhone;
		this.street = street;
		this.streetNo = streetNo;
		this.postal = postal;
		this.city = city;
		this.country = country;
		this.deliveryInfo = deliveryInfo;
	}
}
