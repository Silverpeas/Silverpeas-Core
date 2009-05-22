package com.stratelia.silverpeas.portlet.model;

import java.sql.Connection;

import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.Schema;
import com.stratelia.webactiv.util.exception.UtilException;

public class PortletSchema extends Schema
{
  public PortletSchema(int cl, Connection co) throws UtilException
  {
    super(cl,co);
    init();
  } 

  public PortletSchema(int cl) throws UtilException
  {
    super(cl);
    init();
  } 
  protected String getJNDIName() 
  {
      return JNDINames.ADMIN_DATASOURCE;
  }

  private void init()
  {
    portletColumn = new PortletColumnTable(this);
    portletRow = new PortletRowTable(this);
    portletState = new PortletStateTable(this);
  }

  public PortletColumnTable portletColumn = null;
  public PortletRowTable portletRow = null;
  public PortletStateTable portletState = null;
}
