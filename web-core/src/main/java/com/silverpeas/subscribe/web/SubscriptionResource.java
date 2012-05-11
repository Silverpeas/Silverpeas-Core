/*
* Copyright (C) 2000 - 2011 Silverpeas
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
* "http://www.silverpeas.com/legal/licensing"
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
import com.silverpeas.comment.CommentRuntimeException;
import com.silverpeas.subscribe.Subscription;
import com.silverpeas.subscribe.SubscriptionServiceFactory;
import com.silverpeas.subscribe.service.SubscribeRuntimeException;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.web.RESTWebService;
import java.net.URI;
import java.util.Collection;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
* A REST Web resource representing a given subscription.
* It is a web service that provides an access to a subscription referenced by its URL.
*/
@Service
@Scope("request")
@Path("subscriptions/{componentId}")
@Authorized
public class SubscriptionResource extends RESTWebService {

  @PathParam("componentId")
  private String componentId;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public SubscriptionEntity[] getComponentSubscriptions() {
    try {
      Collection<? extends Subscription> subscriptions = SubscriptionServiceFactory.getFactory().
              getSubscribeService().getUserSubscriptionsByComponent(getUserDetail().getId(),
              componentId);
      return asWebEntities(subscriptions);
    } catch (CommentRuntimeException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  @GET
  @Path("/subscribers/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public String[] getSubscribers(@PathParam("id") String topicId) {
    try {
      Collection<String> subscribers = SubscriptionServiceFactory.getFactory().
              getSubscribeService().getSubscribers(new ForeignPK(topicId, componentId));
      return subscribers.toArray(new String[subscribers.size()]);
    } catch (SubscribeRuntimeException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  protected SubscriptionEntity[] asWebEntities(Collection<? extends Subscription> subscriptions) {
    SubscriptionEntity[] entities = new SubscriptionEntity[subscriptions.size()];
    int i = 0;
    for (Subscription subscription : subscriptions) {
      URI subscriptionURI = getUriInfo().getRequestUriBuilder().path(subscription.getTopic().getId()).
              build();
      entities[i] = asWebEntity(subscription, identifiedBy(subscriptionURI));
      i++;
    }
    return entities;
  }

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