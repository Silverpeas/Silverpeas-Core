/*
 * Copyright (C) 2000 - 2026 Silverpeas
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.comment.CommentRuntimeException;
import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionResourceType;
import org.silverpeas.core.subscription.SubscriptionServiceProvider;
import org.silverpeas.core.subscription.service.UserSubscriptionSubscriber;
import org.silverpeas.core.web.mvc.webcomponent.WebMessager;
import org.silverpeas.core.web.rs.annotation.Authorized;
import org.silverpeas.core.web.subscription.bean.SubscriptionBeanProvider;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import java.net.URI;

import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.COMPONENT;
import static org.silverpeas.core.util.JSONCodec.encodeArray;

/**
* A REST Web resource representing a given subscription.
* It is a web service that provides an access to a subscription referenced by its URL.
*/
@Tag(name = "Subscriptions",
    description = "The subscriptions to the changes occurring on the resources of Silverpeas: an " +
    "application as a whole, or one of the contributions it contains. The type of the targeted " +
    "resource is given by the subscription type: a publication, a calendar, a blog post, and so " +
    "on.")
@WebService
@Path(SubscribeResource.PATH + "/{componentId}")
@Authorized
public class SubscribeResource extends AbstractSubscriptionResource {

  static final String PATH = "subscribe";

  @Operation(summary = "Subscribes the authenticated user to the changes in the given application.")
  @ApiResponse(responseCode = "200", description = "The subscription has been done.",
      content = @Content(array = @ArraySchema(schema = @Schema(type = "string"))))
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public String subscribeToComponent() {
    return subscribeToResource(COMPONENT, null);
  }

  @Operation(summary = "Subscribes the authenticated user to the changes on the given resource " +
      "of the application.")
  @ApiResponse(responseCode = "200", description = "The subscription has been done.",
      content = @Content(array = @ArraySchema(schema = @Schema(type = "string"))))
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
              getBundle().getString("GML.subscribe.success"), b.getPath()));
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
