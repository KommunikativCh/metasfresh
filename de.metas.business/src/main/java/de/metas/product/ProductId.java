package de.metas.product;

import java.util.Collection;
import java.util.Set;

import org.adempiere.util.Check;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;

import de.metas.lang.RepoIdAware;
import lombok.Value;

/*
 * #%L
 * de.metas.business
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

@Value
public class ProductId implements RepoIdAware
{
	int repoId;

	public static ProductId ofRepoId(final int repoId)
	{
		return new ProductId(repoId);
	}

	public static ProductId ofRepoIdOrNull(final int repoId)
	{
		return repoId > 0 ? new ProductId(repoId) : null;
	}

	public static Set<ProductId> ofRepoIds(final Collection<Integer> repoIds)
	{
		return repoIds.stream()
				.filter(repoId -> repoId != null && repoId > 0)
				.map(ProductId::ofRepoId)
				.collect(ImmutableSet.toImmutableSet());
	}

	public static int toRepoId(final ProductId productId)
	{
		return productId != null ? productId.getRepoId() : -1;
	}

	public static Set<Integer> toRepoIds(final Collection<ProductId> productIds)
	{
		return productIds.stream()
				.filter(Predicates.notNull())
				.map(ProductId::toRepoId)
				.collect(ImmutableSet.toImmutableSet());
	}

	private ProductId(final int repoId)
	{
		this.repoId = Check.assumeGreaterThanZero(repoId, "repoId");
	}
}