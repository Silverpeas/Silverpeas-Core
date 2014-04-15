package org.silverpeas.rating;

public interface Rateable {

  /**
   * Returns rating of this rateable resource
   * @param userId user identifier 
   * @return If userId is defined, returned rating will contain rating of this user
   */
  public Rating getRating(String userId);
  
}
