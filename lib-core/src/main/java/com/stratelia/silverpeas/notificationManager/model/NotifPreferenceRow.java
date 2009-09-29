package com.stratelia.silverpeas.notificationManager.model;

public class NotifPreferenceRow {
  private int id;
  private int notifAddressId;
  private int componentInstanceId;
  private int userId;
  private int messageType;

  public int getId() {
    return id;
  }

  public void setId(int aId) {
    id = aId;
  }

  public int getNotifAddressId() {
    return notifAddressId;
  }

  public void setNotifAddressId(int aNotifAddressId) {
    notifAddressId = aNotifAddressId;
  }

  public int getComponentInstanceId() {
    return componentInstanceId;
  }

  public void setComponentInstanceId(int aComponentInstanceId) {
    componentInstanceId = aComponentInstanceId;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int aUserId) {
    userId = aUserId;
  }

  public int getMessageType() {
    return messageType;
  }

  public void setMessageType(int aMessageType) {
    messageType = aMessageType;
  }

  public NotifPreferenceRow(int aId, int aNotifAddressId,
      int aComponentInstanceId, int aUserId, int aMessageType) {
    id = aId;
    notifAddressId = aNotifAddressId;
    componentInstanceId = aComponentInstanceId;
    userId = aUserId;
    messageType = aMessageType;
  }
}
