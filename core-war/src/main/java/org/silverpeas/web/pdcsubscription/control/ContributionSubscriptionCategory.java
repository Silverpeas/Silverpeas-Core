/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.web.pdcsubscription.control;

import org.silverpeas.core.subscription.SubscriptionContributionType;
import org.silverpeas.core.subscription.SubscriptionResourceType;
import org.silverpeas.core.subscription.SubscriptionResourceTypeRegistry;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the Category providing all {@link SubscriptionContributionType} instances.
 */
public class ContributionSubscriptionCategory extends SubscriptionCategory {

  private final List<SubscriptionResourceType> handledTypes;

  public ContributionSubscriptionCategory(final PdcSubscriptionSessionController ctrl) {
    super(ctrl);
    handledTypes = SubscriptionResourceTypeRegistry.get()
        .streamAll()
        .filter(SubscriptionResourceType::isValid)
        .filter(SubscriptionContributionType.class::isInstance)
        .collect(Collectors.toList());
  }

  @Override
  public String getId() {
    return "CONTRIBUTION_CATEGORY";
  }

  @Override
  public int priority() {
    return 10;
  }

  @Override
  public String getLabel() {
    return getCtrl().getString("contribution");
  }

  @Override
  public String getResourceTypeLabel() {
    return getCtrl().getString("ContributionType");
  }

  @Override
  public List<SubscriptionResourceType> getHandledTypes() {
    return handledTypes;
  }
}
