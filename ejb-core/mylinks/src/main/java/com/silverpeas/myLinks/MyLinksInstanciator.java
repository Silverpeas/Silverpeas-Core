package com.silverpeas.myLinks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.util.DBUtil;

public class MyLinksInstanciator implements ComponentsInstanciatorIntf {

  public MyLinksInstanciator() {
  }
  
  public void create(Connection con, String spaceId, String componentId, String userId) throws InstanciationException {
	SilverTrace.info("myLinks", "MyLinksInstanciator.create()", "root.MSG_GEN_ENTER_METHOD", "space = "+spaceId+", componentId = "+componentId+", userId ="+userId);

	
	
	SilverTrace.info("myLinks","MyLinksInstanciator.create()","root.MSG_GEN_EXIT_METHOD");
  }

  public void delete(Connection con, String spaceId, String componentId, String userId) throws InstanciationException 
  {
	SilverTrace.info("myLinks","MyLinksInstanciator.delete()","root.MSG_GEN_ENTER_METHOD","space = "+spaceId+", componentId = "+componentId+", userId ="+userId);

	try {
		PreparedStatement prepStmt = null;
		try
		{
			String query = "delete from SB_MyLinks_Link where instanceId = ? ";
			prepStmt = con.prepareStatement(query);
			prepStmt.setString(1, componentId);
			prepStmt.executeUpdate();
		}
		finally
		{
			// fermeture
			DBUtil.close(prepStmt);
		}
	} catch (SQLException e) {
		throw new InstanciationException("Can't delete links for component '"+componentId+"'");
	}

	SilverTrace.info("myLinks","MyLinksInstanciator.delete()","root.MSG_GEN_EXIT_METHOD");
  }
}
