/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.subscription;

import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.subscribe.SubscriptionResource;
import com.silverpeas.util.CollectionUtil;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.node.model.NodeDetail;

import java.util.ArrayList;
import java.util.Collection;

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
   * @param resource
   */
  public void initialize(SubscriptionResource resource) {
    internalContext = new InternalContext();
    internalContext.resource = resource;
  }

  /**
   * Initializing all context data excepted ones of the user.
   * @param resource
   * @param resourcePath
   */
  public void initialize(SubscriptionResource resource,
      Collection<SubscriptionResourcePath> resourcePath) {
    initialize(resource);
    if (CollectionUtil.isNotEmpty(resourcePath)) {
      internalContext.path.addAll(resourcePath);
    }
  }

  /**
   * Initializing all context data excepted ones of the user.
   * @param resource
   * @param nodePath
   */
  public void initializeFromNode(SubscriptionResource resource, Collection<NodeDetail> nodePath) {
    Collection<SubscriptionResourcePath> resourcePath =
        new ArrayList<SubscriptionResourcePath>(nodePath.size());
    for (NodeDetail node : nodePath) {
      resourcePath.add(new SubscriptionResourcePath(node.getName(userPreferences.getLanguage()),
          node.getLink()));
    }
    initialize(resource, resourcePath);
  }

  /**
   * Gets the current user.
   * @return
   */
  public UserDetail getUser() {
    return user;
  }

  /**
   * Gets the resource aimed by the subscription.
   * @return
   */
  public SubscriptionResource getResource() {
    return internalContext.resource;
  }

  /**
   * Gets the path aimed by the subscription. (Path for browse bar)
   * @return
   */
  public Collection<SubscriptionResourcePath> getPath() {
    return internalContext.path;
  }

  /**
   * Gets the destination URL.
   * @return
   */
  public String getDestinationUrl() {
    return internalContext.destinationUrl;
  }

  /**
   * Internal context
   */
  private class InternalContext {
    public String destinationUrl = "/RSubscription/jsp/Main";
    public SubscriptionResource resource = null;
    public Collection<SubscriptionResourcePath> path = new ArrayList<SubscriptionResourcePath>();
  }
}
