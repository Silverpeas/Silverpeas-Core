package com.silverpeas.tagcloud;

import java.sql.Connection;
import java.sql.PreparedStatement;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.util.DBUtil;

public class TagCloudInstanciator implements ComponentsInstanciatorIntf {

	private static final String TAGCLOUD_TABLENAME = "SB_TagCloud_TagCloud";

	public TagCloudInstanciator()
	{
	}
	
	public void create(Connection con, String spaceId, String componentId, String userId)
		throws InstanciationException 
  	{
		SilverTrace.info("tagCloud", "TagCloudInstanciator.create()", "root.MSG_GEN_PARAM_VALUE",
			"componentId = " + componentId);
	}

	public void delete(Connection con, String spaceId, String componentId, String userId)
		throws InstanciationException
	{
		SilverTrace.info("tagCloud", "TagCloudInstanciator.delete()", "root.MSG_GEN_PARAM_VALUE",
			"componentId = " + componentId);
		
		String query = "DELETE FROM " + TAGCLOUD_TABLENAME + " WHERE instanceId = ? ";
		PreparedStatement prepStmt = null;
		try
		{
			prepStmt = con.prepareStatement(query);
			prepStmt.setString(1, componentId);
			prepStmt.executeUpdate();
		}
		catch (Exception e)
		{
			throw new InstanciationException("TagCloudInstanciator.delete()",
				InstanciationException.ERROR, "root.EX_RECORD_DELETION_FAILED", e);
		}
		finally
		{
			DBUtil.close(prepStmt);
		}
	}
	
}