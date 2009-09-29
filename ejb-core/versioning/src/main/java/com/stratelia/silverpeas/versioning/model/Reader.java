/*
 * Aliaksei_Budnikau
 * Date: Oct 16, 2002
 */
package com.stratelia.silverpeas.versioning.model;

public class Reader implements java.io.Serializable, Cloneable {

  /** This constant indicates that pk reference in class was not initilized */
  public static final int NULLID = -1;

  private int userId = NULLID;
  private int documentId = NULLID;
  private String instanceId;
  private int saved;

  public Reader() {
  }

  public Reader(int userId, int documentId, String instanceId, int saved) {
    this.userId = userId;
    this.documentId = documentId;
    this.instanceId = instanceId;
    this.saved = saved;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public int getDocumentId() {
    return documentId;
  }

  public void setDocumentId(int documentId) {
    this.documentId = documentId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public int getSaved() {
    return saved;
  }

  public void setSaved(int saved) {
    this.saved = saved;
  }

  /**
   * Overriden toString method for debug/trace purposes
   */
  public String toString() {
    return "Reader object : [ userId = " + userId + ", documentId = "
        + documentId + ", instanceId = " + instanceId + ", saved = " + saved
        + " ];";
  }

  /**
   * Support Cloneable Interface
   */
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      return null; // this should never happened
    }
  }

}
