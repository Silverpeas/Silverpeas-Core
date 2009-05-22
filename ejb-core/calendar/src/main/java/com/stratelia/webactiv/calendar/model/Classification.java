package com.stratelia.webactiv.calendar.model;

import java.io.Serializable;

public class Classification implements Serializable {
  
  final public static String PRIVATE      = "private";
  final public static String PUBLIC       = "public";
  final public static String CONFIDENTIAL = "confidential";
  
  static public String[] getAllClassifications() {
    String[] all = {PUBLIC, PRIVATE, CONFIDENTIAL};
    return all;
  }

    static public String[] getAllClassificationsWithoutConfidential() {
    String[] all = {PUBLIC, PRIVATE};
    return all;
  }
  
  private String classification = PRIVATE;
  
  public Classification() {
  }
  
  public Classification(String classification) {
    setString(classification);
  }
  
  public void setString(String classification) {
    if (classification == null) return;
    if (classification.equals(PRIVATE))
      this.classification = PRIVATE;
    if (classification.equals(PUBLIC))
      this.classification = PUBLIC;
    if (classification.equals(CONFIDENTIAL))
      this.classification = CONFIDENTIAL;
  }
  
  public String getString() {
    return classification;
  }
  
  public boolean isPublic() {
    return (classification.equals(PUBLIC)); // has the object is Serializable, this has to be an equals() method
  }
  
  public boolean isPrivate() {
    return (classification.equals(PRIVATE)); 
  }
  
  public boolean isConfidential() {
    return (classification.equals(CONFIDENTIAL)); 
  }
  
}