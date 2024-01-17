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
package org.silverpeas.core.web.util.viewgenerator.html.buttons;

import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.util.ContributionBatchManagementContext;
import org.silverpeas.core.subscription.SubscriptionResource;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This TAG can be called into a {@link ButtonTag}.<br>
 * It permits to display a popups in order to ask to the user some validation and confirmations.
 * <p>
 *   For now, it asks:
 *   <ul>
 *     <li>for minor/major modifications</li>
 *     <li>for user notification sending</li>
 *   </ul>
 * </p>
 */
public class ContributionBatchManagementContextTag extends AbstractContributionManagementContextTag {
  private static final long serialVersionUID = -3652425520811693463L;

  private transient ContributionBatchManagementContext context;
  private String jsValidationCallbackMethodName;

  public ContributionBatchManagementContext getContext() {
    return context;
  }

  public ContributionBatchManagementContextTag() {
    super();
  }

  public void setContext(final ContributionBatchManagementContext context) {
    this.context = context;
  }

  @Override
  public String getJsValidationCallbackMethodName() {
    return jsValidationCallbackMethodName;
  }

  @Override
  void init() {
    context = null;
    jsValidationCallbackMethodName = null;
  }

  @Override
  List<Item> getItems() {
    return getContext().getContributionContexts().stream().map(c -> {
      final Item item = new Item();
      final Contribution contribution = c.getContribution();
      item.contributionId = contribution.getIdentifier();
      item.contributionIndexable = contribution.isIndexable();
      item.contributionStatus = c.getContributionStatus();
      item.location = c.getLocation();
      final SubscriptionResource subscriptionResource = c.getLinkedSubscriptionResource();
      item.subscriptionResourceId = subscriptionResource.getId();
      item.subscriptionResourceType = subscriptionResource.getType();
      return item;
    }).collect(Collectors.toList());
  }

  public void setJsValidationCallbackMethodName(final String jsValidationCallbackMethodName) {
    this.jsValidationCallbackMethodName = jsValidationCallbackMethodName;
  }
}
