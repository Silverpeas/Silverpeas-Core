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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.ecs.xhtml.script;
import org.json.JSONObject;
import org.silverpeas.rating.Rating;
import org.silverpeas.rating.RatingPK;

import com.silverpeas.web.Exposable;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RatingEntity implements Exposable {
  
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
  private String resourceType;
  
  @XmlElement(required = true)
  @NotNull
  @Size(min = 1)
  private String resourceId;
  
  @XmlElement(required = true)
  @NotNull
  private int notesCount;
  
  @XmlElement(required = true)
  @NotNull
  private float globalRating;
  
  @XmlElement
  private int userRating;
  
  @XmlElement
  private String userId;
  
  /**
   * Creates a new rating entity from the specified notation.
   *
   * @param notation the notation to entitify.
   * @return the entity representing the specified notation.
   */
  public static RatingEntity fromRating(final Rating rating) {
    return new RatingEntity(rating);
  }
  
  /**
   * Sets a URI to this entity. With this URI, it can then be accessed through the Web.
   *
   * @param uri the web entity URI.
   * @return itself.
   */
  public RatingEntity withURI(final URI uri) {
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
   *
   * @return the silverpeas component instance identifier.
   */
  public String getComponentId() {
    return componentId;
  }

  /**
   * Gets the type of the resource that is rated by this.
   *
   * @return the rated resource type.
   */
  public String getResourceType() {
    return resourceType;
  }

  /**
   * Gets the identifier of the resource that is rated by this.
   *
   * @return the rated resource identifier.
   */
  public String getResourceId() {
    return resourceId;
  }
  
  private RatingEntity(Rating rating) {
    this.componentId = rating.getInstanceId();
    this.globalRating = rating.getOverallRating();
    this.notesCount = rating.getNumberOfReviews();
    this.resourceId = rating.getResourceId();
    this.resourceType = rating.getResourceType();
    this.userRating = rating.getUserRating();
  }
  
  /**
   * Gets the comment business objet this entity represent.
   *
   * @return a comment instance.
   */
  public Rating toRating() {
    Rating rating =
        new Rating(new RatingPK(resourceId, componentId, resourceType, userId));
    rating.setUserRating(userRating);
    rating.setOverallRating(globalRating);
    rating.setNumberOfReviews(notesCount);
    return rating;
  }
  
  public String toJSonScript(String jsVariableName) {
    return new script().setType("text/javascript").addElement("var " + jsVariableName + " = " + toJSon() + ";").toString();
  }
  
  public String toJSon() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("componentId", componentId);
    jsonObject.put("globalRating",globalRating);
    jsonObject.put("notesCount",notesCount);
    jsonObject.put("resourceId",resourceId);
    jsonObject.put("resourceType",resourceType);
    jsonObject.put("userRating", userRating);
    return jsonObject.toString();
  }

}