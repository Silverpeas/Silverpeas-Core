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
* FLOSS exception. You should have recieved a copy of the text describing
* the FLOSS exception, and it is also available here:
* "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.silverpeas.core.webapi.subscribe;

import org.silverpeas.core.webapi.base.annotation.Authorized;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.comment.CommentRuntimeException;
import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionServiceProvider;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.service.ComponentSubscription;
import org.silverpeas.core.subscription.service.GroupSubscriptionSubscriber;
import org.silverpeas.core.subscription.service.NodeSubscription;
import org.silverpeas.core.subscription.service.UserSubscriptionSubscriber;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.notification.message.MessageNotifier;

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

/**
 * A REST Web resource representing a given subscription.
 * It is a web service that provides an access to a subscription referenced by its URL.
 */
@Service
@RequestScoped
@Path("unsubscribe/{componentId}")
@Authorized
public class UnsubscribeResource extends RESTWebService {

  @PathParam("componentId")
  private String componentId;

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public Response unsubscribeUserFromComponent() {
    return unsubscribeUserFromComponent(getUserDetail().getId());
  }

  @POST
  @Path("/user/{userId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response unsubscribeUserFromComponent(@PathParam("userId") String userId) {
    return unsubscribeSubscriberFromComponent(UserSubscriptionSubscriber.from(userId));
  }

  @POST
  @Path("/group/{groupId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response unsubscribeGroupFromComponent(@PathParam("groupId") String groupId) {
    return unsubscribeSubscriberFromComponent(GroupSubscriptionSubscriber.from(groupId));
  }

  /**
   * Centralizing component unsubscribe
   * @param subscriber a subscription subscriber
   * @return
   */
  private Response unsubscribeSubscriberFromComponent(SubscriptionSubscriber subscriber) {
    try {
      Subscription subscription =
          new ComponentSubscription(subscriber, componentId, getUserDetail().getId());
      SubscriptionServiceProvider.getSubscribeService().unsubscribe(subscription);
      ComponentInstLight component = getOrganisationController().getComponentInstLight(componentId);
      MessageNotifier.addSuccess(MessageFormat
          .format(getBundle().getString("GML.unsubscribe.success"),
              component.getLabel(getUserDetail().getUserPreferences().getLanguage())));
      return Response.ok(Collections.singletonList("OK")).build();
    } catch (CommentRuntimeException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  @POST
  @Path("/topic/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response unsubscribeUserFromTopic(@PathParam("id") String topicId) {
    return unsubscribeUserFromTopic(topicId, getUserDetail().getId());
  }

  @POST
  @Path("/topic/{topicId}/user/{userId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response unsubscribeUserFromTopic(@PathParam("topicId") String topicId,
      @PathParam("userId") String userId) {
    return unsubscribeSubscriberFromTopic(topicId, UserSubscriptionSubscriber.from(userId));
  }

  @POST
  @Path("/topic/{topicId}/group/{groupId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response unsubscribeGroupFromTopic(@PathParam("topicId") String topicId,
      @PathParam("groupId") String groupId) {
    return unsubscribeSubscriberFromTopic(topicId, GroupSubscriptionSubscriber.from(groupId));
  }

  /**
   * Centralizing topic unsubscribe
   * @param topicId the topic identifier
   * @param subscriber a subscription subscriber
   * @return
   */
  private Response unsubscribeSubscriberFromTopic(@PathParam("topicId") String topicId,
      SubscriptionSubscriber subscriber) {
    try {
      Subscription subscription = new NodeSubscription(subscriber, new NodePK(topicId, componentId),
          getUserDetail().getId());
      SubscriptionServiceProvider.getSubscribeService().unsubscribe(subscription);
      return Response.ok(Collections.singletonList("OK")).build();
    } catch (CommentRuntimeException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  @Override
  public String getComponentId() {
    return this.componentId;
  }
}
