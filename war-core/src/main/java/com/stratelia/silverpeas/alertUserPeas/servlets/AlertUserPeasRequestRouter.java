/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.alertUserPeas.servlets;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.alertUserPeas.control.AlertUserPeasSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;


/**
 * Class declaration
 *
 *
 * @author
 */
public class AlertUserPeasRequestRouter extends ComponentRequestRouter
{

    /**
     * Method declaration
     * 
     * 
     * @param mainSessionCtrl
     * @param componentContext
     * 
     * @return
     * 
     * @see
     */
    public ComponentSessionController createComponentSessionController(MainSessionController mainSessionCtrl, ComponentContext componentContext)
    {
        return new AlertUserPeasSessionController(mainSessionCtrl, componentContext);
    }

    /**
     * This method has to be implemented in the component request rooter class.
     * returns the session control bean name to be put in the request object
     * ex : for almanach, returns "almanach"
     */
    public String getSessionControlBeanName()
    {
        return "alertUserPeas";
    }

    /**
     * This method has to be implemented by the component request rooter
     * it has to compute a destination page
     * @param function The entering request function (ex : "Main.jsp")
     * @param componentSC The component Session Control, build and initialised.
     * @return The complete destination URL for a forward (ex : "/almanach/jsp/almanach.jsp?flag=user")
     */
    public String getDestination(String function, ComponentSessionController componentSC, HttpServletRequest request)
    {
        String                             destination = "";
        AlertUserPeasSessionController  scc = (AlertUserPeasSessionController)componentSC;
        SilverTrace.info("alertUserPeas", "getDestination()", "root.MSG_GEN_PARAM_VALUE", "Function=" + function);

        try
        {
            if (function.equals("Main"))
            {   
				scc.init();
				destination = getDestination("ToSelection", scc, request);					
            }
			else if(function.startsWith("ToSelection")) // nav vers selectionPeas pour choix users et groupes
			{  
				destination = scc.initSelection();			
			}
			else if(function.startsWith("FromSelection")) // récupère les users et groupes selectionnés au travers de selectionPeas et les place en session
			{  
				scc.computeSelection(); 
				UserDetail[] userDetails = scc.getUserRecipients();
				Group[] groups = scc.getGroupRecipients();
				if ((userDetails.length > 0)||(groups.length > 0))
				{
					request.setAttribute("UserR", userDetails);
					request.setAttribute("GroupR", groups);
					request.setAttribute("HostComponentName", scc.getHostComponentName());
					request.setAttribute("HostSpaceName", scc.getHostSpaceName());
					destination = "/alertUserPeas/jsp/writeMessage.jsp";
				}
				else destination = getDestination("Close", scc, request); //pas de users ou groupes selectionnes => fermeture fenêtre
			}
			else if(function.startsWith("Close")) // fermeture de la fenêtre 
			{  
				destination = "/alertUserPeas/jsp/close.jsp";
			}
			else if(function.startsWith("ToAlert")) // nav vers message attente notification 
			{  
				request.setAttribute("HostComponentName", scc.getHostComponentName());
				request.setAttribute("HostSpaceName", scc.getHostSpaceName());
				scc.prepareNotification(request.getParameter("messageAux"));
				destination = "/alertUserPeas/jsp/sendMessage.jsp";
			}
			else if(function.startsWith("Notify")) // Notification 
			{  
				request.setAttribute("HostComponentName", scc.getHostComponentName());
				request.setAttribute("HostSpaceName", scc.getHostSpaceName());
				scc.sendNotification();
				destination = "/alertUserPeas/jsp/messageOk.jsp";
			}
        }
        catch (Exception e)
        {
            request.setAttribute("javax.servlet.jsp.jspException", e);
            destination = "/admin/jsp/errorpageMain.jsp";
        }

        SilverTrace.info("alertUserPeas", "getDestination()", "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
        return destination;
    }
    
}
