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
package org.silverpeas.core.web.subscription;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.subscription.SubscriptionResource;
import org.silverpeas.core.util.CollectionUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * User: Yohann Chastagnier
 * Date: 04/03/13
 */
public class SubscriptionContext {

  private UserDetail user;
  private UserPreferences userPreferences;
  private InternalContext internalContext;

  /**
   * Default constructor
   */
  public SubscriptionContext(final UserDetail user, final UserPreferences userPreferences) {
    this.user = user;
    this.userPreferences = userPreferences;
  }

  /**
   * Initializing all context data excepted ones of the user.
   * @param resource a subscription resource
   * @return itself.
   */
  public SubscriptionContext initialize(SubscriptionResource resource) {
    internalContext = new InternalContext();
    internalContext.resource = resource;
    return this;
  }

  /**
   * Indicates the location from which the context is initialized.
   * @param location a {@link Location} instance.
   * @return itself.
   */
  public SubscriptionContext atLocation(final Location location) {
    internalContext.location = location;
    return this;
  }

  /**
   * Indicates the node path of the resource as subscription side.
   * @param nodePath a collection of {@link NodeDetail} representing a path.
   * @return itself.
   */
  public SubscriptionContext withNodePath(Collection<NodeDetail> nodePath) {
    Collection<SubscriptionResourcePath> resourcePath = new ArrayList<>(nodePath.size());
    for (NodeDetail node : nodePath) {
      resourcePath.add(new SubscriptionResourcePath(node.getName(userPreferences.getLanguage()),
          node.getLink()));
    }
    return withPath(resourcePath);
  }

  /**
   * Indicates the path of the resource as subscription side.
   * @param path a collection of {@link SubscriptionResourcePath}.
   * @return itself.
   */
  public SubscriptionContext withPath(final Collection<SubscriptionResourcePath> path) {
    if (CollectionUtil.isNotEmpty(path)) {
      internalContext.path.addAll(path);
    }
    return this;
  }

  /**
   * @return the current user.
   */
  public UserDetail getUser() {
    return user;
  }

  /**
   * @return the resource aimed by the subscription.
   */
  public SubscriptionResource getResource() {
    return internalContext.resource;
  }

  /**
   * @return the location from which the context is initialized.
   */
  public Optional<Location> getLocation() {
    return Optional.ofNullable(internalContext.location);
  }

  /**
   * @return the path aimed by the subscription. (Path for browse bar)
   */
  public Collection<SubscriptionResourcePath> getPath() {
    return internalContext.path;
  }

  /**
   * @return the destination URL.
   */
  public String getDestinationUrl() {
    return internalContext.destinationUrl;
  }

  /**
   * Internal context
   */
  private static class InternalContext {
    String destinationUrl = "/RSubscription/jsp/Main";
    SubscriptionResource resource = null;
    Location location = null;
    Collection<SubscriptionResourcePath> path = new ArrayList<>();
  }
}
