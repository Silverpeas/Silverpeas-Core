/*
* Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.subscribe.web;

import com.silverpeas.annotation.Authorized;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.comment.CommentRuntimeException;
import com.silverpeas.subscribe.Subscription;
import com.silverpeas.subscribe.SubscriptionServiceFactory;
import com.silverpeas.subscribe.SubscriptionSubscriber;
import com.silverpeas.subscribe.service.ComponentSubscriptionResource;
import com.silverpeas.subscribe.service.NodeSubscriptionResource;
import com.silverpeas.subscribe.service.SubscribeRuntimeException;
import com.silverpeas.subscribe.service.UserSubscriptionSubscriber;
import com.silverpeas.util.StringUtil;
import com.silverpeas.web.RESTWebService;
import com.stratelia.webactiv.util.node.model.NodePK;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A REST Web resource representing a given subscription.
 * It is a web service that provides an access to a subscription referenced by its URL.
 */
@Service
@RequestScoped
@Path("subscriptions/{componentId}")
@Authorized
public class SubscriptionResource extends RESTWebService {

  @PathParam("componentId")
  private String componentId;

  /**
   * Gets the JSON representation of component subscriptions in relation with the user.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   *         component subscriptions.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<SubscriptionEntity> getComponentSubscriptions() {
    return getSubscriptions(null);
  }

  /**
   * Gets the JSON representation of component/node subscriptions in relation with the user.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param resourceId identifier of the aimed resource (NODE for now). When a new type of resource
   * will be managed, the resource type will have to be passed into URI
   * @return the response to the HTTP GET request with the JSON representation of the asked
   *         component subscriptions.
   */
  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<SubscriptionEntity> getSubscriptions(@PathParam("id") String resourceId) {
    try {
      final SubscriptionSubscriber userSubscriber =
          UserSubscriptionSubscriber.from(getUserDetail().getId());
      final Collection<Subscription> subscriptions;
      if (StringUtil.isDefined(resourceId)) {
        subscriptions = SubscriptionServiceFactory.getFactory().getSubscribeService()
            .getBySubscriberAndResource(userSubscriber,
                NodeSubscriptionResource.from(new NodePK(resourceId, componentId)));
      } else {
        subscriptions = SubscriptionServiceFactory.getFactory().
            getSubscribeService().getBySubscriberAndComponent(userSubscriber, componentId);
      }
      return asWebEntities(subscriptions);
    } catch (CommentRuntimeException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the JSON representation of component subscription subscribers.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   *         component subscriptions.
   */
  @GET
  @Path("/subscribers")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<String> getComponentSubscribers() {
    return getSubscribers(null);
  }

  /**
   * Gets the JSON representation of component/node subscription subscribers.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param resourceId identifier of the aimed resource (NODE for now). When a new type of resource
   * will be managed, the resource type will have to be passed into URI
   * @return the response to the HTTP GET request with the JSON representation of the asked
   *         component subscriptions.
   */
  @GET
  @Path("/subscribers/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<String> getSubscribers(@PathParam("id") String resourceId) {
    try {
      final com.silverpeas.subscribe.SubscriptionResource subscriptionResource;
      if (StringUtil.isDefined(resourceId)) {
        subscriptionResource = NodeSubscriptionResource.from(new NodePK(resourceId, componentId));
      } else {
        subscriptionResource = ComponentSubscriptionResource.from(componentId);
      }
      return SubscriptionServiceFactory.getFactory().
          getSubscribeService().getUserSubscribers(subscriptionResource);
    } catch (SubscribeRuntimeException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets WEB entity collection representing the given subscription collection.
   * @param subscriptions
   * @return
   */
  protected Collection<SubscriptionEntity> asWebEntities(Collection<Subscription> subscriptions) {
    Collection<SubscriptionEntity> entities =
        new ArrayList<SubscriptionEntity>(subscriptions.size());
    for (Subscription subscription : subscriptions) {
      URI subscriptionURI =
          getUriInfo().getRequestUriBuilder().path(subscription.getResource().getId()).
              build();
      entities.add(asWebEntity(subscription, identifiedBy(subscriptionURI)));
    }
    return entities;
  }

  /**
   * Gets the WEB entity representing the given subscription.
   * @param subscription
   * @param subscriptionURI
   * @return
   */
  protected SubscriptionEntity asWebEntity(final Subscription subscription, URI subscriptionURI) {
    return SubscriptionEntity.fromSubscription(subscription).withURI(subscriptionURI);
  }

  protected URI identifiedBy(URI uri) {
    return uri;
  }

  @Override
  public String getComponentId() {
    return this.componentId;
  }
}
