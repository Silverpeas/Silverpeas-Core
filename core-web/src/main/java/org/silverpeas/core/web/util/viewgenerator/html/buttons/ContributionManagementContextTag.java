/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.web.util.viewgenerator.html.buttons;

import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.subscription.SubscriptionResourceType;

import java.util.Collections;
import java.util.List;

import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.COMPONENT;

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
public class ContributionManagementContextTag extends AbstractContributionManagementContextTag {
  private static final long serialVersionUID = 7960431491482255658L;

  private ContributionIdentifier contributionId;
  private SubscriptionResourceType subscriptionResourceType;
  private String subscriptionResourceId;
  private String jsValidationCallbackMethodName;
  private Boolean contributionIndexable;
  private Location location;

  public ContributionManagementContextTag() {
    super();
  }

  public SubscriptionResourceType getSubscriptionResourceType() {
    return subscriptionResourceType;
  }

  public ContributionIdentifier getContributionId() {
    return contributionId;
  }

  public void setContributionId(final ContributionIdentifier contributionId) {
    this.contributionId = contributionId;
  }

  public void setSubscriptionResourceType(final SubscriptionResourceType subscriptionResourceType) {
    this.subscriptionResourceType = subscriptionResourceType;
  }

  public String getSubscriptionResourceId() {
    return subscriptionResourceId;
  }

  public void setSubscriptionResourceId(final String subscriptionResourceId) {
    this.subscriptionResourceId = subscriptionResourceId;
  }

  @Override
  public String getJsValidationCallbackMethodName() {
    return jsValidationCallbackMethodName;
  }

  public void setJsValidationCallbackMethodName(final String jsValidationCallbackMethodName) {
    this.jsValidationCallbackMethodName = jsValidationCallbackMethodName;
  }

  public Boolean getContributionIndexable() {
    return contributionIndexable;
  }

  public void setContributionIndexable(final Boolean contributionIndexable) {
    this.contributionIndexable = contributionIndexable;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(final Location location) {
    this.location = location;
  }

  @Override
  void init() {
    contributionId = null;
    subscriptionResourceType = COMPONENT;
    subscriptionResourceId = null;
    jsValidationCallbackMethodName = null;
    contributionIndexable = true;
    location = null;
  }

  @Override
  List<Item> getItems() {
    final Item item = new Item();
    item.contributionId = getContributionId();
    item.contributionIndexable = getContributionIndexable();
    item.location = getLocation();
    item.subscriptionResourceId = getSubscriptionResourceId();
    item.subscriptionResourceType = getSubscriptionResourceType();
    return Collections.singletonList(item);
  }
}
