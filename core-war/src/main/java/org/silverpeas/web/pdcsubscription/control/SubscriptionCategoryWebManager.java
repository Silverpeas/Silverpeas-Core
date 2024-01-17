/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.web.pdcsubscription.control;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.subscription.SubscriptionContributionType;
import org.silverpeas.core.subscription.SubscriptionResourceType;
import org.silverpeas.core.subscription.SubscriptionFactory;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Singleton;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparingInt;

/**
 * The manager of Subscription Categories.
 * @author silveryocha
 */
@Service
@Singleton
public class SubscriptionCategoryWebManager {

  private static final String CATEGORY_LIST_CACHE_KEY =
      SubscriptionCategoryWebManager.class.getSimpleName() + "#getCategories";

  public static SubscriptionCategoryWebManager get() {
    return ServiceProvider.getSingleton(SubscriptionCategoryWebManager.class);
  }

  /**
   * Gets the list of subscription categories.
   * <p>
   * The result list is cached into REQUEST scope.
   * </p>
   * @param ctrl the {@link PdcSubscriptionSessionController} which is essentially used for labels.
   * @return a list of {@link SubscriptionCategory} instances.
   */
  @SuppressWarnings("unchecked")
  List<SubscriptionCategory> getCategories(final PdcSubscriptionSessionController ctrl) {
    return CacheAccessorProvider.getThreadCacheAccessor()
        .getCache()
        .computeIfAbsent(CATEGORY_LIST_CACHE_KEY, List.class, () -> loadCategories(ctrl));
  }

  private List<SubscriptionCategory> loadCategories(final PdcSubscriptionSessionController ctrl) {
    Stream<SubscriptionCategory> categories = SubscriptionFactory.get()
        .streamAll()
        .filter(SubscriptionResourceType::isValid)
        .filter(Predicate.not(SubscriptionContributionType.class::isInstance))
        .map(t -> new DefaultSubscriptionCategory(ctrl, t));
    categories = Stream.concat(categories, Stream.of(new ContributionSubscriptionCategory(ctrl)));
    return categories.sorted(
        comparingInt(SubscriptionCategory::priority).thenComparing(SubscriptionCategory::getId))
        .collect(Collectors.toList());
  }
}
