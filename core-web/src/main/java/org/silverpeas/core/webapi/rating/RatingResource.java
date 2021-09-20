/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.webapi.rating;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.contribution.rating.model.ContributionRating;
import org.silverpeas.core.contribution.rating.model.ContributionRatingPK;
import org.silverpeas.core.contribution.rating.model.RaterRating;
import org.silverpeas.core.contribution.rating.model.RaterRatingPK;
import org.silverpeas.core.contribution.rating.service.RatingService;
import org.silverpeas.core.notification.message.MessageNotifier;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.annotation.Authorized;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * A REST Web resource representing a given rating.
 * It is a web service that provides an access to a rating referenced by its URL.
 */
@WebService
@Path(RatingResource.PATH + "/{componentId}/{contributionType}/{contributionId}")
@Authorized
public class RatingResource extends RESTWebService {

  static final String PATH = "rating";

  @PathParam("componentId")
  private String componentId;
  @PathParam("contributionType")
  private String contributionType;
  @PathParam("contributionId")
  private String contributionId;

  @Override
  protected String getBundleLocation() {
    return "org.silverpeas.notation.multilang.notation";
  }

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

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
  public RaterRatingEntity getRaterRating() {
    try {
      ContributionRating contributionRating = RatingService.get().getRating(getRatingPK());
      return asWebEntity(contributionRating.getRaterRating(UserDetail.from(getUser())));
    } catch (Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  @POST
  @Produces(MediaType.APPLICATION_JSON)
  public Response saveRating(String note) {
    RatingService ratingService = RatingService.get();
    if (!StringUtil.isDefined(note)) {
      ratingService.deleteRaterRating(getRaterRatingPK());
      MessageNotifier.addSuccess(getBundle().getString("notation.vote.delete.ok"));
    } else {
      ratingService.updateRating(getRaterRatingPK(), Integer.parseInt(note));
      MessageNotifier.addSuccess(getBundle().getString("notation.vote.ok"));
    }
    RaterRatingEntity newRating = getRaterRating();
    return Response.ok(newRating).build();
  }

  private ContributionRatingPK getRatingPK() {
    return new ContributionRatingPK(contributionId, componentId, contributionType);
  }

  private RaterRatingPK getRaterRatingPK() {
    return new RaterRatingPK(contributionId, componentId, contributionType,
        UserDetail.from(getUser()));
  }

  /**
   * Converts the rating into its corresponding web entity.
   * @param raterRating the rater rating to convert.
   * @return the corresponding rating entity.
   */
  protected RaterRatingEntity asWebEntity(final RaterRating raterRating) {
    return RaterRatingEntity.fromRaterRating(raterRating);
  }
}