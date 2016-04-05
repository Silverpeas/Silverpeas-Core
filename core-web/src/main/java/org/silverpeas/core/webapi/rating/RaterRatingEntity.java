/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.webapi.rating;

import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.webapi.base.WebEntity;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.apache.ecs.xhtml.script;
import org.silverpeas.core.contribution.rating.model.Rateable;
import org.silverpeas.core.contribution.rating.model.RaterRating;
import org.silverpeas.core.util.JSONCodec;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RaterRatingEntity implements WebEntity {

  private static final long serialVersionUID = 1528651545753548217L;

  @XmlElement(defaultValue = "")
  private URI uri;

  @XmlElement(required = true)
  @NotNull
  @Size(min = 2)
  private String componentId;

  @XmlElement(required = true)
  @NotNull
  @Size(min = 1)
  private String contributionType;

  @XmlElement(required = true)
  @NotNull
  @Size(min = 1)
  private String contributionId;

  @XmlElement(required = true)
  @NotNull
  private int numberOfRaterRatings;

  @XmlElement(required = true)
  @NotNull
  private float ratingAverage;

  @XmlElement
  private int raterRatingValue;

  @XmlElement(name = "isRatingDone")
  private boolean isRatingDone;

  /**
   * Creates a new rater rating entity from the specified parameter.
   * The rater of the returned entity is the user that requests the current treatment.
   * @param rateableContribution a contribution that implements the {@link Rateable} behaviour.
   * @return the entity representing the rater rating from the specified parameter.
   * @see UserDetail#getCurrentRequester()
   */
  public static RaterRatingEntity fromRateable(final Rateable rateableContribution) {
    RaterRating raterRating =
        rateableContribution.getRating().getRaterRating(UserDetail.getCurrentRequester());
    return fromRaterRating(raterRating);
  }

  /**
   * Creates a new rater rating entity from the specified parameter.
   * @param raterRating the rater rating to entitify.
   * @return the entity representing the specified rater rating.
   */
  protected static RaterRatingEntity fromRaterRating(final RaterRating raterRating) {
    return new RaterRatingEntity(raterRating).withURI(buildRaterRatingURI(raterRating));
  }

  /**
   * Sets a URI to this entity. With this URI, it can then be accessed through the Web.
   * @param uri the web entity URI.
   * @return itself.
   */
  private RaterRatingEntity withURI(final URI uri) {
    this.uri = uri;
    return this;
  }

  @Override
  public URI getURI() {
    return uri;
  }

  /**
   * Gets the identifier of the Silverpeas component instance to which the rated content
   * belongs.
   * @return the silverpeas component instance identifier.
   */
  public String getComponentId() {
    return componentId;
  }

  /**
   * Gets the type of the resource that is rated by this.
   * @return the rated resource type.
   */
  public String getContributionType() {
    return contributionType;
  }

  /**
   * Gets the identifier of the resource that is rated by this.
   * @return the rated resource identifier.
   */
  public String getContributionId() {
    return contributionId;
  }

  /**
   * Gets the number of rater ratings on the contribution.
   * @return a positive integer between 0 and 5 (included).
   */
  public int getNumberOfRaterRatings() {
    return numberOfRaterRatings;
  }

  /**
   * Gets the average rating on the contribution.
   * @return a float value between 0 and 5 (included)
   */
  public float getRatingAverage() {
    return ratingAverage;
  }

  /**
   * Gets the rating of the current user.
   * @return a positive integer between 0 and 5 (included)
   */
  public int getRaterRatingValue() {
    return raterRatingValue;
  }

  /**
   * Indicates if the current user made a rating on the contribution.
   * @return true if the user made a rating, false otherwise.
   */
  public boolean isRatingDone() {
    return isRatingDone;
  }

  /**
   * Hidden constructor.
   * @param raterRating the rater rating to wrap.
   */
  private RaterRatingEntity(RaterRating raterRating) {
    this.componentId = raterRating.getRating().getInstanceId();
    this.ratingAverage = raterRating.getRating().getRatingAverage();
    this.numberOfRaterRatings = raterRating.getRating().getRaterRatings().size();
    this.contributionId = raterRating.getRating().getContributionId();
    this.contributionType = raterRating.getRating().getContributionType();
    this.raterRatingValue = raterRating.getValue();
    this.isRatingDone = raterRating.isRatingDone();
  }

  public String toJSonScript(String jsVariableName) {
    return new script().setType("text/javascript")
        .addElement("var " + jsVariableName + " = " + getAsJSonString() + ";").toString();
  }

  public String getAsJSonString() {
    return JSONCodec.encode(this);
  }

  /**
   * Centralized the build of a rater rating URI.
   * @param raterRating the aimed rater rating.
   * @return the URI of specified rater rating.
   */
  private static URI buildRaterRatingURI(RaterRating raterRating) {
    if (raterRating == null) {
      return null;
    }
    return UriBuilder.fromUri(URLUtil.getApplicationURL())
        .path(RESTWebService.REST_WEB_SERVICES_URI_BASE).path(
            RatingResourceURIs.RATER_RATING_BASE_URI)
        .path(raterRating.getRating().getInstanceId())
        .path(raterRating.getRating().getContributionType())
        .path(raterRating.getRating().getContributionId()).build();
  }

  protected RaterRatingEntity() {

  }

  protected void setUri(final URI uri) {
    withURI(uri);
  }

  protected void setComponentId(final String componentId) {
    this.componentId = componentId;
  }

  protected void setContributionType(final String contributionType) {
    this.contributionType = contributionType;
  }

  protected void setContributionId(final String contributionId) {
    this.contributionId = contributionId;
  }

  protected void setNumberOfRaterRatings(final int numberOfRaterRatings) {
    this.numberOfRaterRatings = numberOfRaterRatings;
  }

  protected void setRatingAverage(final float ratingAverage) {
    this.ratingAverage = ratingAverage;
  }

  protected void setRaterRatingValue(final int raterRatingValue) {
    this.raterRatingValue = raterRatingValue;
  }

  protected void setRatingDone(final boolean isRatingDone) {
    this.isRatingDone = isRatingDone;
  }
}