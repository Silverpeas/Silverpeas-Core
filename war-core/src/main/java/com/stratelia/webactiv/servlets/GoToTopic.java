package com.stratelia.webactiv.servlets;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.peasUtil.GoTo;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class GoToTopic extends GoTo
{
	
    public String getDestination(String objectId, HttpServletRequest req, HttpServletResponse res) throws Exception
	{
    	String componentId	= req.getParameter("ComponentId");
    	 
    	NodePK 		pk 		= new NodePK(objectId, componentId);
        NodeDetail 	node 	= getNodeBm().getHeader(pk);
        
        SilverTrace.info("peasUtil", "GoToTopic.doPost", "root.MSG_GEN_PARAM_VALUE", "componentId = "+componentId);
        
        String gotoURL = URLManager.getURL(null, componentId)+node.getURL();
		
        return "goto="+URLEncoder.encode(gotoURL);
	}
    
    public NodeBm getNodeBm() {
    	NodeBm nodeBm = null;
        try {
            NodeBmHome nodeBmHome = (NodeBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
            nodeBm = nodeBmHome.create();
        } catch (Exception e) {
        	displayError(null);
		}
        return nodeBm;
    }
}