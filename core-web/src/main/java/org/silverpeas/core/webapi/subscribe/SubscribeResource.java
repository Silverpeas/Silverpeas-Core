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
package org.silverpeas.core.webapi.subscribe;

import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.comment.CommentRuntimeException;
import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionResourceType;
import org.silverpeas.core.subscription.SubscriptionServiceProvider;
import org.silverpeas.core.subscription.service.UserSubscriptionSubscriber;
import org.silverpeas.core.web.mvc.webcomponent.WebMessager;
import org.silverpeas.core.web.subscription.bean.SubscriptionBeanProvider;
import org.silverpeas.core.webapi.base.annotation.Authorized;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.text.MessageFormat;

import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.COMPONENT;
import static org.silverpeas.core.util.JSONCodec.encodeArray;

/**
* A REST Web resource representing a given subscription.
* It is a web service that provides an access to a subscription referenced by its URL.
*/
@WebService
@Path(SubscribeResource.PATH + "/{componentId}")
@Authorized
public class SubscribeResource extends AbstractSubscriptionResource {

  static final String PATH = "subscribe";

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public String subscribeToComponent() {
    return subscribeToResource(COMPONENT, null);
  }

  @POST
  @Path("{subscriptionType}/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public String subscribeToResource(@PathParam("subscriptionType") String subscriptionType, @PathParam("id") String id) {
    return subscribeToResource(decodeSubscriptionResourceType(subscriptionType), id);
  }

  private String subscribeToResource(final SubscriptionResourceType subscriptionResourceType, final String resourceId) {
    try {
      final Subscription subscription = getSubscription(
          UserSubscriptionSubscriber.from(getUser().getId()), subscriptionResourceType, resourceId);
      SubscriptionServiceProvider.getSubscribeService().subscribe(subscription);
      final String userLanguage = getUserPreferences().getLanguage();
      SubscriptionBeanProvider.getBySubscription(subscription, userLanguage).ifPresent(
          b -> WebMessager.getInstance().addSuccess(
              MessageFormat.format(getBundle().getString("GML.subscribe.success"), b.getPath())));
      return encodeArray(j -> j.add("OK"));
    } catch (CommentRuntimeException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  protected URI identifiedBy(URI uri) {
    return uri;
  }

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }
}
