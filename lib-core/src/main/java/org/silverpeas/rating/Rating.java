package org.silverpeas.rating;

import java.io.Serializable;

public class Rating implements Serializable {

  private static final long serialVersionUID = 5611801738147739307L;
  
  private RatingPK pk;
  private int numberOfReviews;
  private float globalNote;
  private int userNote;
  
  public Rating(RatingPK pk) {
    this.pk = pk;
  }
  
  public String getInstanceId() {
    return pk.getInstanceId();
  }

  public String getResourceId() {
    return pk.getResourceId();
  }

  public String getResourceType() {
    return pk.getResourceType();
  }
  
  public int getNumberOfReviews() {
    return numberOfReviews;
  }

  public void setNumberOfReviews(int nb) {
    this.numberOfReviews = nb;
  }

  public float getOverallRating() {
    return globalNote;
  }

  public void setOverallRating(float rating) {
    this.globalNote = rating;
  }

  public int getUserRating() {
    return userNote;
  }

  public void setUserRating(int rating) {
    this.userNote = rating;
  }

  public int getRoundedOverallRating() {
    return Math.round(globalNote);
  }

}