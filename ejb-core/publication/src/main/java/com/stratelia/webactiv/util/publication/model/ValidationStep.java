package com.stratelia.webactiv.util.publication.model;

import java.io.Serializable;
import java.util.Date;

public class ValidationStep implements Serializable {

  private int id = -1;
  private PublicationPK pubPK = null;
  private String userId = null;
  private Date validationDate = null;
  private String decision = null;

  private String userFullName = null;

  public ValidationStep() {
  }

  public ValidationStep(PublicationPK pubPK, String userId, String decision) {
    this.pubPK = pubPK;
    this.userId = userId;
    this.decision = decision;
  }

  public PublicationPK getPubPK() {
    return pubPK;
  }

  public void setPubPK(PublicationPK pubPK) {
    this.pubPK = pubPK;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public Date getValidationDate() {
    return validationDate;
  }

  public void setValidationDate(Date validationDate) {
    this.validationDate = validationDate;
  }

  public String getUserFullName() {
    return userFullName;
  }

  public void setUserFullName(String userFullName) {
    this.userFullName = userFullName;
  }

  public String getDecision() {
    return decision;
  }

  public void setDecision(String decision) {
    this.decision = decision;
  }

  public int getId() {
    return id;
  }

  public void setId(int stepId) {
    this.id = stepId;
  }

}
