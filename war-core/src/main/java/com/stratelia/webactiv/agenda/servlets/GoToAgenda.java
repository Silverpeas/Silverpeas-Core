package com.stratelia.webactiv.agenda.servlets;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.peasUtil.GoTo;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

public class GoToAgenda extends GoTo
{
    public String getDestination(String objectId, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
    	String url = "ViewOtherAgenda?Id=" + objectId;
    	
        SilverTrace.info("agenda", "GoToAgenda.getDestination", "root.MSG_GEN_PARAM_VALUE", "Url = "+url);
            
        String gotoURL = URLManager.getURL(URLManager.CMP_AGENDA)+url;
         
        SilverTrace.info("agenda", "GoToAgenda.getDestination", "root.MSG_GEN_PARAM_VALUE", "gotoURL = "+gotoURL);
        
        return "goto="+URLEncoder.encode(gotoURL);
	}
    
}