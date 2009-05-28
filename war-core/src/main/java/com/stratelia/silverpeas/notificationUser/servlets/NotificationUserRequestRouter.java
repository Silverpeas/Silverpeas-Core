/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.notificationUser.servlets;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.notificationUser.control.NotificationUserSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;


/**
 * Class declaration
 *
 *
 * @author
 */
public class NotificationUserRequestRouter extends ComponentRequestRouter
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
        return new NotificationUserSessionController(mainSessionCtrl, componentContext);
    }

    /**
     * This method has to be implemented in the component request rooter class.
     * returns the session control bean name to be put in the request object
     * ex : for notificationUser, returns "notificationUser"
     */
    public String getSessionControlBeanName()
    {
        return "notificationUser";
    }

    /**
     * This method has to be implemented by the component request rooter
     * it has to compute a destination page
     * @param function The entering request function (ex : "Main.jsp")
     * @param componentSC The component Session Control, build and initialised.
     * @param request The entering request. The request rooter need it to get parameters
     * @return The complete destination URL for a forward (ex : "/notificationUser/jsp/notificationUser.jsp?flag=user")
     */
    public String getDestination(String function, ComponentSessionController componentSC, HttpServletRequest request)
    {
				//remarques
				//tous les paramètres des la jsp sont transferé par la request.
				//le UserPanel étant unique par session, il est impératif de récupérér les objets selectionnés via userPanel et de transporter
				//les id des ses  de jsp en jsp en soumettant un formulaire.
				//En effet, la notification peut être utilisé "en même temps" qu' le client utiliser userPanelPeas. Cela mélange les objets selectionnée.

        String destination = "";
        NotificationUserSessionController  nuSC = (NotificationUserSessionController)componentSC;
        SilverTrace.info("notificationUser", "NotificationUserRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "function="+function);

		String popupMode = null;
		String editTargets = null;
		String txtTitle=null;
		String txtMessage = null;
		String notificationId= null;
		String priorityId = null;

		try{
			if (function.startsWith("Main"))
	        {
				String[] selectedIdUsers = new String[0];
				String[] selectedIdGroups = new String[0];
				String theTargetsUsers = request.getParameter("theTargetsUsers");
				String theTargetsGroups = request.getParameter("theTargetsGroups");

				nuSC.resetNotification();

				if(theTargetsUsers!=null || theTargetsGroups!=null){//appel pour notifier les targets
					popupMode = request.getParameter("popupMode");
					editTargets = request.getParameter("editTargets");

					selectedIdUsers = nuSC.initTargetsUsers(theTargetsUsers);
					selectedIdGroups = nuSC.initTargetsGroups(theTargetsGroups);

					destination = "/notificationUser/jsp/notificationSender.jsp?popupMode="+popupMode+"&editTargets="+editTargets;
				}
				else{
					//appel standard
					destination = "/notificationUser/jsp/notificationSender.jsp";
				}

				request.setAttribute("SelectedIdUsers", selectedIdUsers);
				request.setAttribute("SelectedIdGroups", selectedIdGroups);
	        }
	        else if(function.equals("SetTarget"))
	        {
				//récupération des is des objet selectionés
				String[] idUsers =  request.getParameterValues("selectedUsers");
				String[] idGroups =  request.getParameterValues("selectedGroups");

				//paramètres jsp
				popupMode = request.getParameter("popupMode");
				editTargets = request.getParameter("editTargets");
				txtTitle = request.getParameter("txtTitle");
				txtMessage = request.getParameter("txtMessage");
				notificationId = request.getParameter("notificationId");
				priorityId = request.getParameter("priorityId");

				nuSC.setTxtTitle(txtTitle);
				nuSC.setTxtMessage(txtMessage);
				nuSC.setNotificationId(notificationId);
				nuSC.setPriorityId(priorityId);

				StringBuffer paramValue= new StringBuffer("?popupMode=").append(popupMode);

				//initialisation des paramètres d'initialisations de UserPanel/UserPanelPeas
				destination = nuSC.initSelectionPeas(idUsers, idGroups, paramValue.toString());
			}
			else if(function.startsWith("GetTarget")){
				//récupération des objets sélélectionnés
				String[] selectedIdUsers = nuSC.getTargetIdUsers();
				String[] selectedIdGroups = nuSC.getTargetIdGroups();

				//paramètres jsp
				popupMode = request.getParameter("popupMode");
				editTargets = request.getParameter("editTargets");

				request.setAttribute("txtTitle", nuSC.getTxtTitle());
				request.setAttribute("txtMessage", nuSC.getTxtMessage());
				request.setAttribute("notificationId", nuSC.getNotificationId());
				request.setAttribute("priorityId", nuSC.getPriorityId());

				StringBuffer paramValue= new StringBuffer("?popupMode=").append(popupMode);

				request.setAttribute("SelectedIdUsers", selectedIdUsers);
				request.setAttribute("SelectedIdGroups", selectedIdGroups);
				destination = "/notificationUser/jsp/notificationSender.jsp"+paramValue.toString();
			}
			else if(function.startsWith("sendNotif")){
				SilverTrace.debug("notificationUser", "NotificationUserRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "Enter sendNotif");

				//récupération des donnés sélélectionnées
				String[] selectedIdUsers = request.getParameterValues("selectedUsers");
				String[] selectedIdGroups = request.getParameterValues("selectedGroups");

				for (Enumeration e=request.getParameterNames();e.hasMoreElements(); )
				{
					String nom =  (String) e.nextElement();
					SilverTrace.debug("notificationUser", "NotificationUserRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "nom="+nom);
					SilverTrace.debug("notificationUser", "NotificationUserRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "value="+request.getParameter(nom));
				}

				//paramètres jsp
				notificationId = request.getParameter("notificationId");
				priorityId = request.getParameter("priorityId");
				txtTitle = request.getParameter("txtTitle");
				txtMessage = request.getParameter("txtMessage");
				popupMode = request.getParameter("popupMode");
				editTargets = request.getParameter("editTargets");

				SilverTrace.debug("notificationUser", "NotificationUserRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "notificationId="+notificationId);
				SilverTrace.debug("notificationUser", "NotificationUserRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "priorityId="+priorityId);
				SilverTrace.debug("notificationUser", "NotificationUserRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "txtMessage="+txtMessage);
				SilverTrace.debug("notificationUser", "NotificationUserRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "txtTitle="+txtTitle);
				SilverTrace.debug("notificationUser", "NotificationUserRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "popupMode="+popupMode);
				SilverTrace.debug("notificationUser", "NotificationUserRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "editTargets="+editTargets);

				nuSC.sendMessage("",notificationId, priorityId, txtTitle, txtMessage, selectedIdUsers, selectedIdGroups);

				request.setAttribute("SelectedIdUsers", new String[0]);
				request.setAttribute("SelectedIdGroups", new String[0]);

				destination = "/notificationUser/jsp/notificationSender.jsp?Action=sendNotif&notificationId=&priorityId=&txtTitle=&txtMessage=&popupMode="+popupMode+"&editTargets="+editTargets+"&compoId=";

			}
			else if(function.startsWith("emptyAll")){
				request.setAttribute("SelectedIdUsers", new String[0]);
				request.setAttribute("SelectedIdGroups", new String[0]);

				editTargets = request.getParameter("editTargets");
				popupMode = request.getParameter("popupMode");
				destination = "/notificationUser/jsp/notificationSender.jsp?Action=emptyAll&notificationId=&priorityId=&txtTitle=&txtMessage=&popupMode="+popupMode+"&editTargets="+editTargets+"&compoId=";
			}
			else
			{
				destination = "/notificationUser/jsp/" + function;
			}
		}
		catch (Exception e)
		{
			request.setAttribute("javax.servlet.jsp.jspException", e);
			destination = "/admin/jsp/errorpageMain.jsp";
		}
		SilverTrace.info("notificationUser", "NotificationUserRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "destination="+destination);
		return destination;
    }
}