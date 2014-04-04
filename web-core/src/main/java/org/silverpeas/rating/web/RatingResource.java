/**
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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.rating.web;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.silverpeas.rating.Rating;
import org.silverpeas.rating.RatingPK;

import com.silverpeas.annotation.Authorized;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.notation.ejb.NotationBm;
import com.silverpeas.util.StringUtil;
import com.silverpeas.web.RESTWebService;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;

/**
 * A REST Web resource representing a given rating. 
 * It is a web service that provides an access to a rating referenced by its URL.
 */
@Service
@RequestScoped
@Path("rating/{componentId}/{contentType}/{contentId}")
@Authorized
public class RatingResource extends RESTWebService {
  
  @PathParam("componentId")
  private String componentId;
  @PathParam("contentType")
  private String contentType;
  @PathParam("contentId")
  private String contentId;

  @Override
  public String getComponentId() {
    return componentId;
  }
  
  /**
   * Gets the JSON representation of the rating associated to defined content.
   * @return the response to the HTTP GET request with the JSON representation of the rating.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public RatingEntity getRating() {
    try {
      Rating rating = getNotationBm().getRating(getRatingPK());
      URI ratingURI = getUriInfo().getRequestUri();
      return asWebEntity(rating, identifiedBy(ratingURI));
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }
  
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public Response saveRating(String note) {
    if (!StringUtil.isDefined(note)) {
      getNotationBm().deleteUserRating(getRatingPK());
    } else {
      getNotationBm().updateRating(getRatingPK(), Integer.parseInt(note));
    }
    RatingEntity newRating = getRating();
    return Response.ok(newRating).build();
  }
   
  private RatingPK getRatingPK() {
    return new RatingPK(contentId, componentId, contentType, getUserDetail().getId());
  }
  
  /**
   * Converts the comment into its corresponding web entity.
   * @param comment the comment to convert.
   * @param commentURI the URI of the comment.
   * @return the corresponding comment entity.
   */
  protected RatingEntity asWebEntity(final Rating rating, URI notationURI) {
    return RatingEntity.fromRating(rating).withURI(notationURI);
  }
  
  protected URI identifiedBy(URI uri) {
    return uri;
  }
  
  private NotationBm getNotationBm() {
    return EJBUtilitaire.getEJBObjectRef(JNDINames.NOTATIONBM_EJBHOME, NotationBm.class);
  }
}