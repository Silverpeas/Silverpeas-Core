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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.webapi.subscribe;

import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionFactory;
import org.silverpeas.core.subscription.SubscriptionResource;
import org.silverpeas.core.subscription.SubscriptionResourceType;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.web.rs.RESTWebService;

import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.COMPONENT;
import static org.silverpeas.core.util.StringUtil.isNotDefined;

/**
 * @author silveryocha
 */
public abstract class AbstractSubscriptionResource extends RESTWebService {

  @PathParam("componentId")
  private String componentId;

  @Override
  public String getComponentId() {
    return componentId;
  }

  /**
   * Decodes from the given type as string the right {@link SubscriptionResourceType} instance.
   * @param type type as string.
   * @return the corresponding {@link SubscriptionResourceType} instance.
   */
  protected SubscriptionResourceType decodeSubscriptionResourceType(final String type) {
    return SubscriptionResourceType.from(type.toUpperCase());
  }

  /**
   * Gets the {@link SubscriptionResource} instance from given {@link SubscriptionResourceType}
   * and ressource identifier.
   * @param type the subscription resource type.
   * @param resourceId the identifier of a resource.
   * @return the right {@link SubscriptionResource} instance.
   */
  protected SubscriptionResource getSubscriptionResource(
      final SubscriptionResourceType type, final String resourceId) {
    if (type == null || !type.isValid()) {
      throw new WebApplicationException("type not found", BAD_REQUEST);
    }
    if (type != COMPONENT) {
      checkResourceId(resourceId);
    }
    return SubscriptionFactory.get()
        .createSubscriptionResourceInstance(type, resourceId, null, componentId);
  }

  /**
   * Gets the {@link Subscription} instance from given parameters.
   * @param subscriber the subscription subscriber.
   * @param type the subscription resource type.
   * @param resourceId the identifier of a resource.
   * @return the right {@link SubscriptionResource} instance.
   */
  protected Subscription getSubscription(final SubscriptionSubscriber subscriber,
      final SubscriptionResourceType type, final String resourceId) {
    if (type == null || !type.isValid()) {
      throw new WebApplicationException("type not found", BAD_REQUEST);
    }
    if (type != COMPONENT) {
      checkResourceId(resourceId);
    }
    final SubscriptionResource resource = getSubscriptionResource(type, resourceId);
    return SubscriptionFactory.get()
        .createSubscriptionInstance(subscriber, resource, getUser().getId());
  }

  private void checkResourceId(final String resourceId) {
    if (isNotDefined(resourceId)) {
      throw new WebApplicationException("resource identifier not found", BAD_REQUEST);
    }
  }
}
