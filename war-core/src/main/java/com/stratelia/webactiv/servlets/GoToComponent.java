package com.stratelia.webactiv.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.peasUtil.GoTo;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;

public class GoToComponent extends GoTo
{
    public String getDestination(String objectId, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
		OrganizationController organization = new OrganizationController();
        ComponentInstLight component = organization.getComponentInstLight(objectId);
        
        if (component != null)
        	return "ComponentId="+objectId;
        
        return null;
	}
}