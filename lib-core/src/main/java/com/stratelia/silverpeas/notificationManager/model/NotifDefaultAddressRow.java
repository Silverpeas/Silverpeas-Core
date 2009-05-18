package com.stratelia.silverpeas.notificationManager.model;


public class NotifDefaultAddressRow
{
  private int id;
  private int userId;
  private int notifAddressId;

  public int getId() {return id;}
  public void setId(int aId)
  {
    id= aId;
  }

  public int getUserId() {return userId;}
  public void setUserId(int aUserId)
  {
    userId= aUserId;
  }

  public int getNotifAddressId() {return notifAddressId;}
  public void setNotifAddressId(int aNotifAddressId)
  {
    notifAddressId= aNotifAddressId;
  }


  public NotifDefaultAddressRow(int aId,
              int aUserId,
              int aNotifAddressId)
  {
    id = aId ;
    userId = aUserId ;
    notifAddressId = aNotifAddressId ;
  }
}
