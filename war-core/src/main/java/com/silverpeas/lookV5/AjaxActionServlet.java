/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.lookV5;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import com.silverpeas.look.LookHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.UserFavoriteSpaceManager;
import com.stratelia.webactiv.organization.DAOFactory;
import com.stratelia.webactiv.organization.UserFavoriteSpaceDAO;
import com.stratelia.webactiv.organization.UserFavoriteSpaceVO;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
import org.silverpeas.core.admin.OrganisationController;

public class AjaxActionServlet extends HttpServlet {

  private static final long serialVersionUID = -2317530610033349156L;

  // Servlet action controller
  private static final String ACTION_ADD_SPACE = "addSpace";
  private static final String ACTION_REMOVE_SPACE = "removeSpace";
  private static final String ACTION_GET_FRAME = "getFrame";
  private static final String DEFAULT_JSP_FRAM = "MainFrameSilverpeasV5.jsp";

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    SilverTrace.debug("lookSilverpeasV5", "AjaxActionServlet.doPost", "root.MSG_GEN_ENTER_METHOD");

    String action = getAction(req);

    String result = null;
    if (ACTION_ADD_SPACE.equals(action)) {
      result = addSpace(req);
    } else if (ACTION_REMOVE_SPACE.equals(action)) {
      result = removeSpace(req);
    } else if (ACTION_GET_FRAME.equals(action)) {
      result = getFrame(req);
    } else {
      result = "{success:false, message:'Unknown action servlet'}";
    }

    // Send response
    Writer writer = res.getWriter();
    writer.write(result);
  }

  /**
   * @param req
   * @return JSON action result
   */
  private String addSpace(HttpServletRequest req) {
    SilverTrace
        .debug("lookSilverpeasV5", "AjaxActionServlet.addSpace", "root.MSG_GEN_ENTER_METHOD");

    // Declare JSon result object
    JSONObject jsonRslt = new JSONObject();
    // Get current session
    HttpSession session = req.getSession(true);
    MainSessionController m_MainSessionCtrl = (MainSessionController) session
        .getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    // Retrieve user identifier from session
    String userId = m_MainSessionCtrl.getUserId();

    // Retrieve space identifier parameter
    String spaceId = req.getParameter("SpaceId");
    if (StringUtil.isDefined(spaceId) && StringUtil.isDefined(userId)) {

      OrganisationController orgaController = m_MainSessionCtrl
          .getOrganisationController();
      // Retrieve all sub space identifier
      ArrayList<String> addedSubSpaceIds = getSubSpaceIdentifiers(userId, spaceId, orgaController);

      // TODO refactor this code using object serializer Bean2JSon or Bean2Xml
      if (!addedSubSpaceIds.isEmpty()) {
        // Added has been done successfully
        jsonRslt.put("success", true);
        // Add list of spaces in JSon response
        JSONArray jsonAddedSpaces = getJSONSpaces(addedSubSpaceIds);
        jsonRslt.put("spaceids", jsonAddedSpaces);
        LookHelper helper = (LookHelper) session.getAttribute(LookHelper.SESSION_ATT);

        if (helper.isEnableUFSContainsState()) {
          // Retrieve all current space path (parent and root space identifier)
          ArrayList<String> parentSpaceIds = getParentSpaceIds(spaceId, orgaController);

          // Add this into jsonParentSpaces
          JSONArray jsonParentSpaces = getJSONSpaces(parentSpaceIds);
          jsonRslt.put("parentids", jsonParentSpaces);
        }
      } else {
        // TODO bundle this message error
        jsonRslt.put("success", false);
        jsonRslt.put("message", "Technical problem in DAO");
      }
    } else {
      jsonRslt.put("success", false);
      jsonRslt.put("message", "Invalid parameter");
    }
    return jsonRslt.toString();
  }

  /**
   * @param listSpaces
   * @return JSONArray of list of spaces
   */
  private JSONArray getJSONSpaces(ArrayList<String> listSpaces) {
    JSONArray jsonSpaces = new JSONArray();
    for (String curSpaceId : listSpaces) {
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("spaceid", curSpaceId);
      jsonSpaces.put(jsonObject);
    }
    return jsonSpaces;
  }

  /**
   * @param spaceId
   * @param orgaController
   * @return
   */
  private ArrayList<String> getParentSpaceIds(String spaceId, OrganisationController orgaController) {
    ArrayList<String> parentSpaceIds = new ArrayList<String>();
    parentSpaceIds = getParentSpaceOfFavoriteSpace(spaceId, orgaController, parentSpaceIds);
    return parentSpaceIds;
  }

  /**
   * @param userId
   * @param spaceId
   * @param orgaController
   * @return sub space identifiers of current space id given in parameter
   */
  private ArrayList<String> getSubSpaceIdentifiers(String userId, String spaceId,
      OrganisationController orgaController) {
    ArrayList<String> addedSubSpaceIds = new ArrayList<String>();
    addSubSpace(spaceId, userId, orgaController, addedSubSpaceIds);
    return addedSubSpaceIds;
  }

  /**
   * addSubSpace add all sub space into user favorite spaces
   * @param spaceId
   * @param userId
   * @param orgaController
   * @param addedSpaceIds
   * @return
   */
  private List<String> addSubSpace(String spaceId, String userId,
      OrganisationController orgaController, ArrayList<String> addedSpaceIds) {
    String shortSpaceId = spaceId;
    if (spaceId.startsWith(Admin.SPACE_KEY_PREFIX)) {
      shortSpaceId = spaceId.substring(Admin.SPACE_KEY_PREFIX.length());
    }
    UserFavoriteSpaceDAO ufsDAO = DAOFactory.getUserFavoriteSpaceDAO();
    if (ufsDAO.addUserFavoriteSpace(new UserFavoriteSpaceVO(Integer.parseInt(userId), Integer
        .parseInt(shortSpaceId)))) {
      addedSpaceIds.add(spaceId);
    }
    // Retrieve user sub space identifier
    String[] subSpaceIds = orgaController.getAllSubSpaceIds(spaceId, userId);
    // addedSpaceIds.addAll(Arrays.asList(subSpaceIds));
    for (String subSpaceId : subSpaceIds) {
      // addedSpaceIds.addAll(Arrays.asList(subSpaceIds));
      addSubSpace(subSpaceId, userId, orgaController, addedSpaceIds);
    }
    return addedSpaceIds;
  }

  /**
   * @param req
   * @return JSON action result
   */
  private String removeSpace(HttpServletRequest req) {
    SilverTrace.debug("lookSilverpeasV5", "AjaxActionServlet.removeSpace",
        "root.MSG_GEN_ENTER_METHOD");
    // Get current session
    HttpSession session = req.getSession(true);
    MainSessionController m_MainSessionCtrl = (MainSessionController) session
        .getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);

    // Retrieve user identifier from session
    String userId = m_MainSessionCtrl.getUserId();

    // Declare JSon result object
    JSONObject jsonRslt = new JSONObject();

    // Retrieve space identifier parameter
    String spaceId = req.getParameter("SpaceId");
    if (StringUtil.isDefined(spaceId) && StringUtil.isDefined(userId)) {
      if (spaceId.startsWith("WA")) {
        spaceId = spaceId.substring("WA".length());
      }
      UserFavoriteSpaceDAO ufsDAO = DAOFactory.getUserFavoriteSpaceDAO();
      // Retrieve all the subspace identifier and status in order to display the right status
      if (ufsDAO.removeUserFavoriteSpace(new UserFavoriteSpaceVO(Integer.parseInt(userId), Integer
          .parseInt(spaceId)))) {
        // Remove has been done successfully
        jsonRslt.put("success", true);
        jsonRslt.put("spaceid", spaceId);

        List<UserFavoriteSpaceVO> listUFS = ufsDAO.getListUserFavoriteSpace(userId);
        // Check user favorite space state (enable contains state)
        LookHelper helper = (LookHelper) session.getAttribute(LookHelper.SESSION_ATT);
        String spaceState = "empty";
        if (helper.isEnableUFSContainsState()) {
          if (UserFavoriteSpaceManager.containsFavoriteSubSpace(spaceId, listUFS, m_MainSessionCtrl
              .getOrganisationController(), userId)) {
            spaceState = "contains";
            jsonRslt.put("spacestate", spaceState);
          }
          // Retrieve father space identifiers
          OrganisationController orga = m_MainSessionCtrl.getOrganisationController();
          ArrayList<String> parentSpaceIds = getParentSpaceIds(spaceId, orga);
          JSONArray parentSpaceJA = buildParentJA(userId, listUFS, orga, parentSpaceIds);
          jsonRslt.put("spacestate", spaceState);
          jsonRslt.put("parentids", parentSpaceJA);
        } else {
          jsonRslt.put("spacestate", spaceState);
        }
      } else {
        jsonRslt.put("success", false);
        jsonRslt.put("message", "Technical problem in DAO");
      }
    } else {
      jsonRslt.put("success", false);
      jsonRslt.put("message", "Invalid parameter call in Ajax method");
    }
    return jsonRslt.toString();
  }

  /**
   * @param userId
   * @param listUFS
   * @param orga
   * @param parentSpaceIds
   * @return
   */
  private JSONArray buildParentJA(String userId, List<UserFavoriteSpaceVO> listUFS,
      OrganisationController orga, ArrayList<String> parentSpaceIds) {
    JSONArray resultJA = new JSONArray();
    for (String curSpaceId : parentSpaceIds) {
      JSONObject curParentState = new JSONObject();
      curParentState.put("spaceid", curSpaceId);

      // Retrieve current space state identifier
      if (UserFavoriteSpaceManager.isUserFavoriteSpace(listUFS, curSpaceId)) {
        curParentState.put("spacestate", "favorite");
      } else {
        if (UserFavoriteSpaceManager.containsFavoriteSubSpace(curSpaceId, listUFS, orga,
            userId)) {
          curParentState.put("spacestate", "contains");
        } else {
          curParentState.put("spacestate", "empty");
        }
      }
      resultJA.put(curParentState);
    }
    return resultJA;
  }

  /**
   * @param req the HttpServletRequest request
   * @return "Action" request parameter
   */
  private String getAction(HttpServletRequest req) {
    return req.getParameter("Action");
  }

  /**
   * @param spaceId
   * @param listUFS
   * @return true if current space is a sub space from a user favorite space, false else if
   */
  private ArrayList<String> getParentSpaceOfFavoriteSpace(String spaceId,
      OrganisationController orgaController, ArrayList<String> parentSpaceIds) {
    SpaceInst curSpaceInst = orgaController.getSpaceInstById(spaceId);
    if (curSpaceInst.getLevel() > 0) {
      // Retrieve father space identifier
      String fatherSpaceId = curSpaceInst.getDomainFatherId();
      if (!fatherSpaceId.startsWith(Admin.SPACE_KEY_PREFIX)) {
        fatherSpaceId = Admin.SPACE_KEY_PREFIX + fatherSpaceId;
      }

      parentSpaceIds.add(fatherSpaceId);
      // It's not a root node, we need to check
      getParentSpaceOfFavoriteSpace(fatherSpaceId, orgaController, parentSpaceIds);
    }
    return parentSpaceIds;
  }

  /**
   * @param req the current HttpServletRequest
   * @return JSON action result
   */
  private String getFrame(HttpServletRequest req) {
    SilverTrace.debug("lookSilverpeasV5", AjaxActionServlet.class.getName() + ".getFrame",
        "root.MSG_GEN_ENTER_METHOD");
    HttpSession session = req.getSession(true);
    GraphicElementFactory gef =
        (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);

    // Retrieve FrameJSP from look name parameter
    String lookName = req.getParameter("LookName");
    String resource = gef.getLookSettings().getString(lookName);
    ResourceLocator specificSettings = new ResourceLocator(resource, "");
    String mainFrame = DEFAULT_JSP_FRAM;
    mainFrame = specificSettings.getString("FrameJSP", DEFAULT_JSP_FRAM);

    // Declare JSon result object
    JSONObject jsonRslt = new JSONObject();
    jsonRslt.put("frame", mainFrame);
    jsonRslt.put("success", true);

    return jsonRslt.toString();
  }
}