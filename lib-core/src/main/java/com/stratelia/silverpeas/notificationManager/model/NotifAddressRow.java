package com.stratelia.silverpeas.notificationManager.model;


public class NotifAddressRow
{
  private int id;
  private int userId;
  private String notifName;
  private int notifChannelId;
  private String address;
  private String usage;
  private int priority;

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

  public String getNotifName() {return notifName;}
  public void setNotifName(String aNotifName)
  {
    notifName= aNotifName;
  }

  public int getNotifChannelId() {return notifChannelId;}
  public void setNotifChannelId(int aNotifChannelId)
  {
    notifChannelId= aNotifChannelId;
  }

  public String getAddress() {return address;}
  public void setAddress(String aAddress)
  {
    address= aAddress;
  }

  public String getUsage() {return usage;}
  public void setUsage(String aUsage)
  {
    usage= aUsage;
  }

  public int getPriority() {return priority;}
  public void setPriority(int aPriority)
  {
    priority= aPriority;
  }


  public NotifAddressRow(int aId,
              int aUserId,
              String aNotifName,
              int aNotifChannelId,
              String aAddress,
              String aUsage,
              int aPriority)
  {
    id = aId ;
    userId = aUserId ;
    notifName = aNotifName ;
    notifChannelId = aNotifChannelId ;
    address = aAddress ;
    usage = aUsage ;
    priority = aPriority ;
  }
}
