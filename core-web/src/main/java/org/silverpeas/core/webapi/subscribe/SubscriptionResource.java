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
import org.silverpeas.core.subscription.constant.SubscriptionResourceType;
import org.silverpeas.core.subscription.service.ComponentSubscriptionResource;
import org.silverpeas.core.subscription.service.NodeSubscriptionResource;
import org.silverpeas.core.subscription.service.ResourceSubscriptionProvider;
import org.silverpeas.core.subscription.service.SubscribeRuntimeException;
import org.silverpeas.core.subscription.util.SubscriptionList;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.StringUtil;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A REST Web resource representing a given subscription.
 * It is a web service that provides an access to a subscription referenced by its URL.
 */
@Service
@RequestScoped
@Path(SubscriptionResourceURIs.SUBSCRIPTION_BASE_URI + "/{componentId}")
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
   * Gets the JSON representation of component/node subscriptions of a resource.
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
      final SubscriptionList subscriptions;
      if (StringUtil.isDefined(resourceId)) {
        subscriptions = SubscriptionServiceProvider.getSubscribeService()
            .getByResource(NodeSubscriptionResource.from(new NodePK(resourceId, componentId)));
      } else {
        subscriptions = SubscriptionServiceProvider.
            getSubscribeService().getByResource(ComponentSubscriptionResource.from(componentId));
      }
      return asWebEntities(subscriptions.filterOnDomainVisibilityFrom(getUserDetail()));
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
  @Path(SubscriptionResourceURIs.SUBSCRIPTION_SUBSCRIBER_URI_PART)
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<SubscriberEntity> getComponentSubscribers() {
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
  @Path(SubscriptionResourceURIs.SUBSCRIPTION_SUBSCRIBER_URI_PART + "/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<SubscriberEntity> getSubscribers(@PathParam("id") String resourceId) {
    try {
      final org.silverpeas.core.subscription.SubscriptionResource subscriptionResource;
      if (StringUtil.isDefined(resourceId)) {
        subscriptionResource = NodeSubscriptionResource.from(new NodePK(resourceId, componentId));
      } else {
        subscriptionResource = ComponentSubscriptionResource.from(componentId);
      }
      return asSubscriberWebEntities(SubscriptionServiceProvider.
          getSubscribeService().getSubscribers(subscriptionResource)
          .filterOnDomainVisibilityFrom(getUserDetail()));
    } catch (SubscribeRuntimeException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the JSON representation of resource subscription subscribers with inheritance.
   * For example, it returns subscribers af a node and those of its parents too.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param subscriptionType the type of subscription.
   * @param existenceIndicatorOnly indicates if the return must only be true (if it exists at
   * least one subscriber) or false (no subscribers).
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * component subscriptions.
   */
  @GET
  @Path(
      SubscriptionResourceURIs.SUBSCRIPTION_SUBSCRIBER_URI_PART + "/{subscriptionType}/inheritance")
  public Response getComponentSubscribersWithInheritance(
      @PathParam("subscriptionType") String subscriptionType,
      @QueryParam("existenceIndicatorOnly") boolean existenceIndicatorOnly) {
    return getSubscribersWithInheritance(subscriptionType, null, existenceIndicatorOnly);
  }

  /**
   * Gets the JSON representation of resource subscription subscribers with inheritance.
   * For example, it returns subscribers af a node and those of its parents too.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param subscriptionType the type of subscription.
   * @param resourceId identifier of the aimed resource (NODE for now). When a new type of resource
   * will be managed, the resource type will have to be passed into URI
   * @param existenceIndicatorOnly indicates if the return must only be true (if it exists at
   * least one subscriber) or false (no subscribers).
   * @return the response to the HTTP GET request with the JSON representation of the asked
   *         component subscriptions.
   */
  @GET
  @Path(SubscriptionResourceURIs.SUBSCRIPTION_SUBSCRIBER_URI_PART + "/{subscriptionType}/inheritance/{id}")
  public Response getSubscribersWithInheritance(
      @PathParam("subscriptionType") String subscriptionType, @PathParam("id") String resourceId,
      @QueryParam("existenceIndicatorOnly") boolean existenceIndicatorOnly) {
    try {
      SubscriptionResourceType parsedSubscriptionResourceType =
          SubscriptionResourceType.from(subscriptionType);
      if (parsedSubscriptionResourceType == SubscriptionResourceType.UNKNOWN) {
        throw new WebApplicationException(Status.NOT_FOUND);
      }
      if (parsedSubscriptionResourceType != SubscriptionResourceType.COMPONENT &&
          StringUtil.isNotDefined(resourceId)) {
        throw new WebApplicationException(Status.NOT_FOUND);
      }
      SubscriptionSubscriberList subscribers = ResourceSubscriptionProvider
          .getSubscribersOfComponentAndTypedResource(getComponentId(),
              parsedSubscriptionResourceType, resourceId);
      if (existenceIndicatorOnly) {
        return Response
            .ok(String.valueOf(!subscribers.getAllUserIds().isEmpty()), MediaType.APPLICATION_JSON)
            .build();
      } else {
        return Response.ok(asSubscriberWebEntities(subscribers), MediaType.APPLICATION_JSON)
            .build();
      }
    } catch (SubscribeRuntimeException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      if (ex instanceof WebApplicationException) {
        throw (WebApplicationException) ex;
      }
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
      entities.add(asWebEntity(subscription));
    }
    return entities;
  }

  /**
   * Gets the WEB entity representing the given subscription.
   * @param subscription
   * @return
   */
  protected SubscriptionEntity asWebEntity(final Subscription subscription) {
    return SubscriptionEntity.from(subscription);
  }

  /**
   * Gets WEB entity collection representing the given subscriber collection.
   * @param subscribers
   * @return
   */
  protected Collection<SubscriberEntity> asSubscriberWebEntities(
      Collection<SubscriptionSubscriber> subscribers) {
    Collection<SubscriberEntity> entities = new ArrayList<SubscriberEntity>(subscribers.size());
    for (SubscriptionSubscriber subscriber : subscribers) {
      entities.add(asSubscriberWebEntity(subscriber));
    }
    return entities;
  }

  /**
   * Gets the WEB entity representing the given subscriber.
   * @param subscriber
   * @return
   */
  protected SubscriberEntity asSubscriberWebEntity(final SubscriptionSubscriber subscriber) {
    return SubscriberEntity.from(subscriber);
  }

  @Override
  public String getComponentId() {
    return this.componentId;
  }
}
