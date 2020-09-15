/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.webapi.subscribe;

import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.comment.CommentRuntimeException;
import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionResourceType;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.service.GroupSubscriptionSubscriber;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.text.MessageFormat;
import java.util.Collections;

import static org.silverpeas.core.subscription.SubscriptionServiceProvider.getSubscribeService;
import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.COMPONENT;

/**
 * A REST Web resource representing a given subscription.
 * It is a web service that provides an access to a subscription referenced by its URL.
 */
@WebService
@Path(UnsubscribeResource.PATH + "/{componentId}")
@Authorized
public class UnsubscribeResource extends AbstractSubscriptionResource {

  static final String PATH = "unsubscribe";

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public Response unsubscribeUserFromComponent() {
    return unsubscribeUserFromComponent(getUser().getId());
  }

  @POST
  @Path("user/{userId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response unsubscribeUserFromComponent(@PathParam("userId") String userId) {
    return unsubscribeSubscriberFromResource(COMPONENT, UserSubscriptionSubscriber.from(userId), null);
  }

  @POST
  @Path("group/{groupId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response unsubscribeGroupFromComponent(@PathParam("groupId") String groupId) {
    return unsubscribeSubscriberFromResource(COMPONENT, GroupSubscriptionSubscriber.from(groupId), null);
  }

  @POST
  @Path("{subscriptionType}/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response unsubscribeUserFromResource(@PathParam("subscriptionType") String subscriptionType, @PathParam("id") String id) {
    return unsubscribeUserFromResource(subscriptionType, id, getUser().getId());
  }

  @POST
  @Path("{subscriptionType}/{id}/user/{userId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response unsubscribeUserFromResource(
      @PathParam("subscriptionType") String subscriptionType, @PathParam("id") String id,
      @PathParam("userId") String userId) {
    return unsubscribeSubscriberFromResource(decodeSubscriptionResourceType(subscriptionType),
        UserSubscriptionSubscriber.from(userId), id);
  }

  @POST
  @Path("{subscriptionType}/{id}/group/{groupId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response unsubscribeGroupFromResource(
      @PathParam("subscriptionType") String subscriptionType, @PathParam("id") String id,
      @PathParam("groupId") String groupId) {
    return unsubscribeSubscriberFromResource(decodeSubscriptionResourceType(subscriptionType),
        GroupSubscriptionSubscriber.from(groupId), id);
  }

  /**
   * Centralizing resource unsubscribe
   * @param subscriptionResourceType the aimed subscription resource type
   * @param subscriber a subscription subscriber
   * @param resourceId the identifier of a resource
   * @return the response
   */
  private Response unsubscribeSubscriberFromResource(final SubscriptionResourceType subscriptionResourceType,
      final SubscriptionSubscriber subscriber, final String resourceId) {
    try {
      final Subscription subscription = getSubscription(subscriber, subscriptionResourceType, resourceId);
      getSubscribeService().unsubscribe(subscription);
      final String userLanguage = getUserPreferences().getLanguage();
      SubscriptionBeanProvider.getBySubscription(subscription, userLanguage).ifPresent(
          b -> WebMessager.getInstance().addSuccess(
              MessageFormat.format(getBundle().getString("GML.unsubscribe.success"), b.getPath())));
      return Response.ok(Collections.singletonList("OK")).build();
    } catch (CommentRuntimeException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }
}
