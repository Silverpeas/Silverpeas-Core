package com.stratelia.webactiv.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.peasUtil.GoTo;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;

public class GoToSpace extends GoTo
{
    public String getDestination(String objectId, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
    	OrganizationController organization = new OrganizationController();
        SpaceInstLight space = organization.getSpaceInstLightById(objectId);
        
        if (space != null && space.getShortId() != null)
        	return "SpaceId="+objectId;
        
        return null;
	}
}