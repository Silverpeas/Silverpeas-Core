/*
 * Copyright (C) 2000 - 2019 Silverpeas
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

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.comment.CommentRuntimeException;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.constant.SubscriptionResourceType;
import org.silverpeas.core.subscription.service.ComponentSubscriptionResource;
import org.silverpeas.core.subscription.service.NodeSubscriptionResource;
import org.silverpeas.core.subscription.service.ResourceSubscriptionProvider;
import org.silverpeas.core.subscription.service.SubscribeRuntimeException;
import org.silverpeas.core.subscription.service.UserSubscriptionSubscriber;
import org.silverpeas.core.subscription.util.SubscriptionList;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.webapi.base.annotation.Authorized;

import javax.annotation.Nonnull;
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

import static org.silverpeas.core.subscription.SubscriptionServiceProvider.getSubscribeService;
import static org.silverpeas.core.util.StringUtil.isDefined;

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
   * @param userId optional filter on a user represented by its id.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   *         component subscriptions.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<SubscriptionEntity> getComponentSubscriptions(
      @QueryParam("userId") final String userId) {
    return getSubscriptions(null, userId);
  }

  /**
   * Gets the JSON representation of component/node subscriptions of a resource.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param resourceId identifier of the aimed resource (NODE for now). When a new type of resource
   * will be managed, the resource type will have to be passed into URI
   * @param userId optional filter on a user represented by its id.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   *         component subscriptions.
   */
  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<SubscriptionEntity> getSubscriptions(@PathParam("id") final String resourceId,
      @QueryParam("userId") final String userId) {
    try {
      final SubscriptionList subscriptions;
      final org.silverpeas.core.subscription.SubscriptionResource
          resource = getSubscriptionResource(resourceId);
      if (isDefined(userId)) {
        final User user = "me".equals(userId) ? getUser() : User.getById(userId);
        final SubscriptionSubscriber subscriber = UserSubscriptionSubscriber.from(user.getId());
        subscriptions = getSubscribeService().getBySubscriberAndResource(subscriber, resource);
      } else {
        subscriptions = getSubscribeService().getByResource(resource);
      }
      return asWebEntities(subscriptions.filterOnDomainVisibilityFrom(UserDetail.from(getUser())));
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
      final org.silverpeas.core.subscription.SubscriptionResource
          subscriptionResource = getSubscriptionResource(resourceId);
      return asSubscriberWebEntities(getSubscribeService().getSubscribers(subscriptionResource)
          .filterOnDomainVisibilityFrom(UserDetail.from(getUser())));
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
    } catch (WebApplicationException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  @Nonnull
  private org.silverpeas.core.subscription.SubscriptionResource getSubscriptionResource(
      final String resourceId) {
    final org.silverpeas.core.subscription.SubscriptionResource subscriptionResource;
    if (isDefined(resourceId)) {
      subscriptionResource = NodeSubscriptionResource.from(new NodePK(resourceId, componentId));
    } else {
      subscriptionResource = ComponentSubscriptionResource.from(componentId);
    }
    return subscriptionResource;
  }

  /**
   * Gets WEB entity collection representing the given subscription collection.
   * @param subscriptions a collection of subscriptions
   * @return a collection of subscription entities
   */
  protected Collection<SubscriptionEntity> asWebEntities(Collection<Subscription> subscriptions) {
    final Collection<SubscriptionEntity> entities = new ArrayList<>(subscriptions.size());
    for (Subscription subscription : subscriptions) {
      entities.add(asWebEntity(subscription));
    }
    return entities;
  }

  /**
   * Gets the WEB entity representing the given subscription.
   * @param subscription a subscription
   * @return a subscription entity
   */
  protected SubscriptionEntity asWebEntity(final Subscription subscription) {
    return SubscriptionEntity.from(subscription);
  }

  /**
   * Gets WEB entity collection representing the given subscriber collection.
   * @param subscribers a collection of subscribers in subscriptions
   * @return a collection of subscriber entities.
   */
  protected Collection<SubscriberEntity> asSubscriberWebEntities(
      Collection<SubscriptionSubscriber> subscribers) {
    final Collection<SubscriberEntity> entities = new ArrayList<>(subscribers.size());
    for (SubscriptionSubscriber subscriber : subscribers) {
      entities.add(asSubscriberWebEntity(subscriber));
    }
    return entities;
  }

  /**
   * Gets the WEB entity representing the given subscriber.
   * @param subscriber a subscriber in a subscription
   * @return a subscriber entity
   */
  protected SubscriberEntity asSubscriberWebEntity(final SubscriptionSubscriber subscriber) {
    return SubscriberEntity.from(subscriber);
  }

  @Override
  protected String getResourceBasePath() {
    return SubscriptionResourceURIs.SUBSCRIPTION_BASE_URI;
  }

  @Override
  public String getComponentId() {
    return this.componentId;
  }
}
