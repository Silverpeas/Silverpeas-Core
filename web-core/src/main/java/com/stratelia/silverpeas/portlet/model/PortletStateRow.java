package com.stratelia.silverpeas.portlet.model;


public class PortletStateRow
{
  private int id;
  private int state;
  private int userId;
  private int portletRowId;

  public int getId() {return id;}
  public void setId(int aId)
  {
    id= aId;
  }

  public int getState() {return state;}
  public void setState(int aState)
  {
    state= aState;
  }

  public int getUserId() {return userId;}
  public void setUserId(int aUserId)
  {
    userId= aUserId;
  }

  public int getPortletRowId() {return portletRowId;}
  public void setPortletRowId(int aPortletRowId)
  {
    portletRowId= aPortletRowId;
  }


  public PortletStateRow(int aId,
              int aState,
              int aUserId,
              int aPortletRowId)
  {
    id = aId ;
    state = aState ;
    userId = aUserId ;
    portletRowId = aPortletRowId ;
  }
}
