/*
 * Created on 5 août 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.stratelia.silverpeas.silverStatisticsPeas.control;

import java.sql.SQLException;
import java.util.Hashtable;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * @author BERTINL
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DocPieChartBuilder extends AbstractPieChartBuilder 
{
	private String currentUserId;
	private String spaceId;
	private ResourceLocator message;

	public DocPieChartBuilder(String currentUserId, String spaceId, ResourceLocator message)
	{
		this.currentUserId = currentUserId;
		this.spaceId = spaceId;
		this.message = message;
	}
	
	/* (non-Javadoc)
	 * @see com.stratelia.silverpeas.silverStatisticsPeas.control.AbstractPieChartBuilder#getChartTitle()
	 */
	public String getChartTitle() 
	{
		String title = message.getString("silverStatisticsPeas.VolumeDocsNumber")+" ";
		
		try {
			if ( (this.spaceId != null) && (this.spaceId.length()>0) && (! this.spaceId.equals("WA0"))) {
				SpaceInstLight space = new Admin().getSpaceInstLightById(this.spaceId);
				title += message.getString("silverStatisticsPeas.FromSpace")+" ["+space.getName()+"]"; 
			}
		} catch(Exception e) {
			SilverTrace.error("silverStatisticsPeas", "DocPieChartBuilder.getChartTitle()", "root.EX_SQL_QUERY_FAILED", e);
		}
		
		return title;
	}

	/* (non-Javadoc)
	 * @see com.stratelia.silverpeas.silverStatisticsPeas.control.AbstractPieChartBuilder#getCmpStats()
	 */
	Hashtable getCmpStats() 
	{
		Hashtable cmpStats = new Hashtable();
		try 
		{
			cmpStats.putAll( SilverStatisticsPeasDAOVolumeServer.getStatsAttachmentsVentil(this.currentUserId));
			cmpStats.putAll( SilverStatisticsPeasDAOVolumeServer.getStatsVersionnedAttachmentsVentil(this.currentUserId));
		} 
		catch (SQLException e) 
		{
			SilverTrace.error("silverStatisticsPeas", "DocPieChartBuilder.getCmpStats()", "root.EX_SQL_QUERY_FAILED", e);
		}
		
		return cmpStats;
	}

}
