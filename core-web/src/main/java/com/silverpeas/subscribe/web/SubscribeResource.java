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
package com.silverpeas.subscribe.web;

import com.silverpeas.annotation.Authorized;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import org.silverpeas.core.comment.CommentRuntimeException;
import com.silverpeas.subscribe.Subscription;
import com.silverpeas.subscribe.SubscriptionServiceProvider;
import com.silverpeas.subscribe.service.ComponentSubscription;
import com.silverpeas.subscribe.service.NodeSubscription;
import com.silverpeas.web.RESTWebService;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.node.model.NodePK;
import org.silverpeas.util.NotifierUtil;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.text.MessageFormat;

/**
* A REST Web resource representing a given subscription.
* It is a web service that provides an access to a subscription referenced by its URL.
*/
@Service
@RequestScoped
@Path("subscribe/{componentId}")
@Authorized
public class SubscribeResource extends RESTWebService {

  @PathParam("componentId")
  private String componentId;

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public String subscribeToComponent() {
    try {
      Subscription subscription = new ComponentSubscription(getUserDetail().getId(), componentId);
      SubscriptionServiceProvider.getSubscribeService().subscribe(subscription);
      ComponentInstLight component = getOrganisationController().getComponentInstLight(componentId);
      NotifierUtil.addSuccess(MessageFormat
          .format(getBundle().getString("GML.subscribe.success"),
              component.getLabel(getUserDetail().getUserPreferences().getLanguage())));
      return "OK";
    } catch (CommentRuntimeException ex) {
      throw new WebApplicationException(ex, Status.NOT_FOUND);
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  @POST
  @Path("/topic/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public String subscribeToTopic(@PathParam("id") String topicId) {
    try {
      Subscription subscription = new NodeSubscription(getUserDetail().getId(), new NodePK(topicId,
              componentId));
      SubscriptionServiceProvider.getSubscribeService().subscribe(subscription);
      return "OK";
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
  public String getComponentId() {
    return this.componentId;
  }
}
