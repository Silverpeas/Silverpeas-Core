package com.silverpeas.notation.model;

import java.io.Serializable;

import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * Primary key of a notation.
 */
public class NotationPK extends WAPrimaryKey implements Serializable {

  private int type;
  private String userId;

  public NotationPK(String id) {
    super(id);
    this.type = Notation.TYPE_UNDEFINED;
  }

  public NotationPK(String id, String componentId, int type) {
    super(id, componentId);
    this.type = type;
  }

  public NotationPK(String id, String componentId, int type, String userId) {
    this(id, componentId, type);
    this.userId = userId;
  }

  public int getType() {
    return type;
  }

  public String getUserId() {
    return userId;
  }

  /**
   * Comparison between two notation primary key. Since various attributes of
   * the both elements can be null, using toString() method to compare the
   * elements avoids to check null cases for each attribute.
   */
  public boolean equals(Object other) {
    return ((other instanceof NotationPK) && (toString()
        .equals(((NotationPK) other).toString())));
  }

  public String toString() {
    return new StringBuffer().append("(id = ").append(getId()).append(
        ", space = ").append(getSpace()).append(", componentName = ").append(
        getComponentName()).append(", type = ").append(getType()).append(
        ", userId = ").append(getUserId()).append(")").toString();
  }

}