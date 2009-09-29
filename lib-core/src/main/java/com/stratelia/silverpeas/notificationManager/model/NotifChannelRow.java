package com.stratelia.silverpeas.notificationManager.model;

public class NotifChannelRow {
  private int id;
  private String name;
  private String description;
  private String couldBeAdded;
  private String fromAvailable;
  private String subjectAvailable;

  public int getId() {
    return id;
  }

  public void setId(int aId) {
    id = aId;
  }

  public String getName() {
    return name;
  }

  public void setName(String aName) {
    name = aName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String aDescription) {
    description = aDescription;
  }

  public String getCouldBeAdded() {
    return couldBeAdded;
  }

  public void setCouldBeAdded(String aCouldBeAdded) {
    couldBeAdded = aCouldBeAdded;
  }

  public String getFromAvailable() {
    return fromAvailable;
  }

  public void setFromAvailable(String aFromAvailable) {
    fromAvailable = aFromAvailable;
  }

  public String getSubjectAvailable() {
    return subjectAvailable;
  }

  public void setSubjectAvailable(String aSubjectAvailable) {
    subjectAvailable = aSubjectAvailable;
  }

  public NotifChannelRow(int aId, String aName, String aDescription,
      String aCouldBeAdded, String aFromAvailable, String aSubjectAvailable) {
    id = aId;
    name = aName;
    description = aDescription;
    couldBeAdded = aCouldBeAdded;
    fromAvailable = aFromAvailable;
    subjectAvailable = aSubjectAvailable;
  }
}
