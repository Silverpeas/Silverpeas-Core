package org.silverpeas.rating;

import java.io.Serializable;

import com.silverpeas.util.ForeignPK;

public class RatingPK extends ForeignPK implements Serializable {
  
  private static final long serialVersionUID = -4144961919465268637L;
  private String resourceType;
  private String userId;
  
  public RatingPK(String id, String componentId, String type) {
    super(id, componentId);
    this.resourceType = type;
  }
  
  public RatingPK(String id, String componentId, String type, String userId) {
    this(id, componentId, type);
    this.userId = userId;
  }
  
  public String getResourceId() {
    return getId();
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }
  
  @Override
  public int hashCode() {
    return this.toString().hashCode();
  }

  /**
   * Comparison between two notation primary key. Since various attributes of the both elements can
   * be null, using toString() method to compare the elements avoids to check null cases for each
   * attribute.
   * @param other
   */
  @Override
  public boolean equals(Object other) {
    return ((other instanceof RatingPK) && (toString().equals(((RatingPK) other).toString())));
  }

  @Override
  public String toString() {
    return new StringBuffer().append("(id = ").append(getResourceId()).append(", componentId = ")
        .append(getComponentName()).append(", type = ").append(getResourceType()).append(
            ", userId = ").append(getUserId()).append(")").toString();
  }
}