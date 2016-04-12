/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.web.look;

import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.space.UserFavoriteSpaceService;
import org.silverpeas.core.admin.space.model.UserFavoriteSpaceVO;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class AjaxActionServlet extends HttpServlet {

  private static final long serialVersionUID = -2317530610033349156L;

  @Inject
  private OrganizationController organisationController;
  @Inject
  private UserFavoriteSpaceService userFavoriteSpaceService;

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
    String action = getAction(req);

    String result;
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
    // Get current session
    HttpSession session = req.getSession(true);
    // Retrieve user identifier from session
    String userId = UserDetail.getCurrentRequester().getId();

    // Retrieve space identifier parameter
    String spaceId = req.getParameter("SpaceId");

    String json = "";
    if (StringUtil.isDefined(spaceId) && StringUtil.isDefined(userId)) {
      // Retrieve all sub space identifier
      ArrayList<String> addedSubSpaceIds = getSubSpaceIdentifiers(userId, spaceId);
      if (!addedSubSpaceIds.isEmpty()) {
        json = JSONCodec.encodeObject(jsonObject -> {
          jsonObject.put("success", true).put("spaceids", getJSONSpaces(addedSubSpaceIds));
          LookHelper helper = LookHelper.getLookHelper(session);
          if (helper.isEnableUFSContainsState()) {
            // Retrieve all current space path (parent and root space identifier)
            ArrayList<String> parentSpaceIds = getParentSpaceIds(spaceId);
            jsonObject.put("parentids", getJSONSpaces(parentSpaceIds));
          }
          return jsonObject;
        });
      } else {
        // TODO bundle this message error
        json = JSONCodec.encodeObject(jsonObject -> jsonObject.put("success", false)
            .put("message", "Technical problem in DAO"));
      }
    } else {
      json = JSONCodec.encodeObject(
          jsonObject -> jsonObject.put("success", false).put("message", "Invalid parameter"));
    }
    return json;
  }

  /**
   * @param listSpaces
   * @return JSONArray of list of spaces
   */
  private Function<JSONCodec.JSONArray, JSONCodec.JSONArray> getJSONSpaces(
      List<String> listSpaces) {
    return (jsonSpaces -> {
      for (String curSpaceId : listSpaces) {
        jsonSpaces.addJSONObject(jsonObject -> jsonObject.put("spaceid", curSpaceId));
      }
      return jsonSpaces;
    });
  }

  /**
   * @param spaceId
   * @return
   */
  private ArrayList<String> getParentSpaceIds(String spaceId) {
    ArrayList<String> parentSpaceIds = new ArrayList<String>();
    parentSpaceIds = getParentSpaceOfFavoriteSpace(spaceId, parentSpaceIds);
    return parentSpaceIds;
  }

  /**
   * @param userId
   * @param spaceId
   * @return sub space identifiers of current space id given in parameter
   */
  private ArrayList<String> getSubSpaceIdentifiers(String userId, String spaceId) {
    ArrayList<String> addedSubSpaceIds = new ArrayList<String>();
    addSubSpace(spaceId, userId, addedSubSpaceIds);
    return addedSubSpaceIds;
  }

  /**
   * addSubSpace add all sub space into user favorite spaces
   * @param spaceId
   * @param userId
   * @param addedSpaceIds
   * @return
   */
  private List<String> addSubSpace(String spaceId, String userId, ArrayList<String> addedSpaceIds) {
    SpaceInstLight space = organisationController.getSpaceInstLightById(spaceId);
    if (userFavoriteSpaceService.addUserFavoriteSpace(
        new UserFavoriteSpaceVO(Integer.parseInt(userId), space.getLocalId()))) {
      addedSpaceIds.add(spaceId);
    }
    // Retrieve user sub space identifier
    String[] subSpaceIds = organisationController.getAllSubSpaceIds(spaceId, userId);
    // addedSpaceIds.addAll(Arrays.asList(subSpaceIds));
    for (String subSpaceId : subSpaceIds) {
      // addedSpaceIds.addAll(Arrays.asList(subSpaceIds));
      addSubSpace(subSpaceId, userId, addedSpaceIds);
    }
    return addedSpaceIds;
  }

  /**
   * @param req
   * @return JSON action result
   */
  private String removeSpace(HttpServletRequest req) {
    // Get current session
    HttpSession session = req.getSession(true);
    MainSessionController m_MainSessionCtrl = (MainSessionController) session
        .getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);

    // Retrieve user identifier from session
    String userId = m_MainSessionCtrl.getUserId();

    // Retrieve space identifier parameter
    String spaceId = req.getParameter("SpaceId");
    String json = "";
    if (StringUtil.isDefined(spaceId) && StringUtil.isDefined(userId)) {
      SpaceInstLight spaceInst = organisationController.getSpaceInstLightById(spaceId);
      // Retrieve all the subspace identifier and status in order to display the right status
      if (userFavoriteSpaceService.removeUserFavoriteSpace(
          new UserFavoriteSpaceVO(Integer.parseInt(userId), spaceInst.getLocalId()))) {
        // Remove has been done successfully
        json = JSONCodec.encodeObject(jsonRslt -> {
          jsonRslt.put("success", true).put("spaceid", spaceId);
          List<UserFavoriteSpaceVO> listUFS =
              userFavoriteSpaceService.getListUserFavoriteSpace(userId);
          // Check user favorite space state (enable contains state)
          LookHelper helper = LookHelper.getLookHelper(session);
          String spaceState = "empty";
          if (helper.isEnableUFSContainsState()) {
            if (userFavoriteSpaceService.containsFavoriteSubSpace(spaceInst, listUFS, userId)) {
              spaceState = "contains";
              jsonRslt.put("spacestate", spaceState);
            }
            // Retrieve father space identifiers
            ArrayList<String> parentSpaceIds = getParentSpaceIds(spaceId);
            jsonRslt.put("spacestate", spaceState)
                .put("parentids", buildParentJA(userId, listUFS, parentSpaceIds));
          } else {
            jsonRslt.put("spacestate", spaceState);
          }
          return jsonRslt;
        });
      } else {
        json = JSONCodec.encodeObject(
            jsonRslt -> jsonRslt.put("success", false).put("message", "Technical problem in DAO"));
      }
    } else {
      json = JSONCodec.encodeObject(jsonRslt -> jsonRslt.put("success", false)
          .put("message", "Invalid parameter call in Ajax method"));
    }
    return json;
  }

  /**
   * @param userId
   * @param listUFS
   * @param parentSpaceIds
   * @return
   */
  private Function<JSONCodec.JSONArray, JSONCodec.JSONArray> buildParentJA(String userId,
      List<UserFavoriteSpaceVO> listUFS, ArrayList<String> parentSpaceIds) {
    return (resultJA -> {
      for (String curSpaceId : parentSpaceIds) {
        resultJA.addJSONObject(curParentState -> {
          curParentState.put("spaceid", curSpaceId);
          SpaceInstLight spaceInst = organisationController.getSpaceInstLightById(curSpaceId);

          // Retrieve current space state identifier
          if (userFavoriteSpaceService.isUserFavoriteSpace(listUFS, spaceInst)) {
            curParentState.put("spacestate", "favorite");
          } else {
            if (userFavoriteSpaceService.containsFavoriteSubSpace(spaceInst, listUFS, userId)) {
              curParentState.put("spacestate", "contains");
            } else {
              curParentState.put("spacestate", "empty");
            }
          }
          return curParentState;
        });
      }
      return resultJA;
    });
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
   * @return true if current space is a sub space from a user favorite space, false else if
   */
  private ArrayList<String> getParentSpaceOfFavoriteSpace(String spaceId,
      ArrayList<String> parentSpaceIds) {
    SpaceInst curSpaceInst = organisationController.getSpaceInstById(spaceId);
    if (curSpaceInst.getLevel() > 0) {
      // Retrieve father space identifier
      String fatherSpaceId = curSpaceInst.getDomainFatherId();
      if (!fatherSpaceId.startsWith(SpaceInst.SPACE_KEY_PREFIX)) {
        fatherSpaceId = SpaceInst.SPACE_KEY_PREFIX + fatherSpaceId;
      }

      parentSpaceIds.add(fatherSpaceId);
      // It's not a root node, we need to check
      getParentSpaceOfFavoriteSpace(fatherSpaceId, parentSpaceIds);
    }
    return parentSpaceIds;
  }

  /**
   * @param req the current HttpServletRequest
   * @return JSON action result
   */
  private String getFrame(HttpServletRequest req) {
    HttpSession session = req.getSession(true);
    GraphicElementFactory gef =
        (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);

    // Retrieve FrameJSP from look name parameter
    String lookName = req.getParameter("LookName");
    String resource = gef.getLookSettings().getString(lookName);
    SettingBundle specificSettings = ResourceLocator.getSettingBundle(resource);
    final String mainFrame = specificSettings.getString("FrameJSP", DEFAULT_JSP_FRAM);

    // Declare JSon result object
    return JSONCodec
        .encodeObject(jsonRslt -> jsonRslt.put("frame", mainFrame).put("success", true));
  }
}