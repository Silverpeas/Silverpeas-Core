package com.silverpeas.util.i18n;

import java.io.Serializable;

public class Translation implements Serializable {

  private int id = -1;
  private String objectId = null;
  private String language = "fr";

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getObjectId() {
    return objectId;
  }

  public void setObjectId(String objectId) {
    this.objectId = objectId;
  }

}
