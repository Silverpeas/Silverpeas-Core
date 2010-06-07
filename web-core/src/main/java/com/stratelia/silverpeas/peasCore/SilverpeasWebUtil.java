/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.silverpeas.peasCore;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author ehugonnet
 */
public class SilverpeasWebUtil {

  public static final String MAIN_SESSION_CONTROLLER = "SilverSessionController";  
  private OrganizationController organizationController = new OrganizationController();

   public SilverpeasWebUtil() {
     
   }

  public SilverpeasWebUtil(OrganizationController controller) {
    organizationController = controller;
  }

  public OrganizationController getOrganizationController() {
    return organizationController;
  }

  /**
   * Accessing the MainSessionController
   * @param request the HttpServletRequest
   * @return the current MainSessionController.
   */
  public MainSessionController getMainSessionController(HttpServletRequest request) {
    return (MainSessionController) request.getSession().getAttribute(MAIN_SESSION_CONTROLLER);
  }

  /**
   * Extract the space id and the component id.
   */
  public String[] getComponentId(HttpServletRequest request) {
    String spaceId;
    String componentId;
    String function;
    String pathInfo = request.getPathInfo();
    SilverTrace.info("peasCore", "ComponentRequestRouter.getComponentId",
        "root.MSG_GEN_PARAM_VALUE", "pathInfo=" + pathInfo);
    if (pathInfo != null) {
      spaceId = null;
      pathInfo = pathInfo.substring(1); // remove first '/'
      function = pathInfo.substring(pathInfo.indexOf('/') + 1, pathInfo.length());
      if (pathInfo.startsWith("jsp")) {
        // Pour les feuilles de styles, icones, ... + Pour les composants de
        // l'espace personnel (non instanciables)
        componentId = null;
      } else {
        // Get the space and component Ids
        // componentId extracted from the URL
        // Old url (with WA..)
        if (pathInfo.indexOf("WA") != -1) {
          String sAndCId = pathInfo.substring(0, pathInfo.indexOf('/'));
          // spaceId looks like WA17
          spaceId = sAndCId.substring(0, sAndCId.indexOf('_'));
          // componentId looks like kmelia123
          componentId = sAndCId.substring(spaceId.length() + 1, sAndCId.length());
        } else {
          componentId = pathInfo.substring(0, pathInfo.indexOf("/"));
        }

        if (function.startsWith("Main") || function.startsWith("searchResult")
            || function.equalsIgnoreCase("searchresult")
            || function.startsWith("portlet")
            || function.equals("GoToFilesTab")) {
          ComponentInstLight component = organizationController.getComponentInstLight(componentId);
          spaceId = component.getDomainFatherId();
        }
        SilverTrace.info("peasCore", "ComponentRequestRouter.getComponentId",
            "root.MSG_GEN_PARAM_VALUE", "componentId=" + componentId
            + "spaceId=" + spaceId + " pathInfo=" + pathInfo);
      }
    } else {
      spaceId = "-1";
      componentId = "-1";
      function = "Error";
    }
    String[] context = new String[]{spaceId, componentId, function};
    SilverTrace.info("peasCore", "ComponentRequestRouter.getComponentId",
        "root.MSG_GEN_PARAM_VALUE", "spaceId=" + spaceId + " | componentId="
        + componentId + " | function=" + function);
    return context;
  }

  public String[] getRoles(HttpServletRequest request) {
    MainSessionController controller = getMainSessionController(request);
    if (controller != null) {
      return organizationController.getUserProfiles(controller.getUserId(), getComponentId(request)[1]);
    }
    return new String[0];
  }
}
