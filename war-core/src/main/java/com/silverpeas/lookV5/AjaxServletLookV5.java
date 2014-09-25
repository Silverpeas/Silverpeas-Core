/**
* Copyright (C) 2000 - 2013 Silverpeas
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* As a special exception to the terms and conditions of version 3.0 of
* the GPL, you may redistribute this Program in connection with Free/Libre
* Open Source Software ("FLOSS") applications as described in Silverpeas's
* FLOSS exception. You should have received a copy of the text describing
* the FLOSS exception, and it is also available here:
* "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.silverpeas.lookV5;

import com.silverpeas.external.webConnections.dao.WebConnectionService;
import com.silverpeas.external.webConnections.model.WebConnectionsInterface;
import com.silverpeas.jobStartPagePeas.JobStartPagePeasSettings;
import com.silverpeas.look.LookHelper;
import com.silverpeas.look.SilverpeasLook;
import com.silverpeas.personalization.UserMenuDisplay;
import com.silverpeas.personalization.UserPreferences;
import com.silverpeas.sharing.services.SharingServiceFactory;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.SearchAxis;
import com.stratelia.silverpeas.pdc.model.SearchContext;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.PersonalSpaceController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.UserFavoriteSpaceManager;
import com.stratelia.webactiv.organization.DAOFactory;
import com.stratelia.webactiv.organization.UserFavoriteSpaceDAO;
import com.stratelia.webactiv.organization.UserFavoriteSpaceVO;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
import org.silverpeas.core.admin.OrganisationController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AjaxServletLookV5 extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
          throws ServletException, IOException {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    HttpSession session = request.getSession(true);
    MainSessionController mainSessionController = (MainSessionController) session.getAttribute(
            MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute(
            GraphicElementFactory.GE_FACTORY_SESSION_ATT);
    LookHelper helper = (LookHelper) session.getAttribute(LookHelper.SESSION_ATT);
    OrganisationController orgaController = mainSessionController.getOrganisationController();

    String userId = mainSessionController.getUserId();
    UserPreferences preferences = mainSessionController.getPersonalization();

    // Get ajax action
    String responseId = request.getParameter("ResponseId");
    String init = request.getParameter("Init");
    String spaceId = request.getParameter("SpaceId");
    String componentId = request.getParameter("ComponentId");
    String axisId = request.getParameter("AxisId");
    String valuePath = request.getParameter("ValuePath");
    String pdc = request.getParameter("Pdc");
    boolean displayContextualPDC = helper.displayContextualPDC();
    boolean displayPDC = "true".equalsIgnoreCase(request.getParameter("GetPDC"));
    boolean restrictedPath = helper.getSettings("restrictedPathForSpaceTransverse", false);

    // User favorite space DAO
    List<UserFavoriteSpaceVO> listUserFS = new ArrayList<UserFavoriteSpaceVO>();
    if (helper.isMenuPersonalisationEnabled()) {
      UserFavoriteSpaceDAO ufsDAO = DAOFactory.getUserFavoriteSpaceDAO();
      listUserFS = ufsDAO.getListUserFavoriteSpace(userId);
    }

    // Set current space and component identifier (helper and gef)
    if (StringUtil.isDefined(componentId)) {
      helper.setComponentIdAndSpaceIds(null, null, componentId);
      String helperSpaceId = helper.getSubSpaceId();
      if (!StringUtil.isDefined(helperSpaceId)) {
        helperSpaceId = helper.getSpaceId();
      }
      gef.setSpaceIdForCurrentRequest(helperSpaceId);
    } else if (StringUtil.isDefined(spaceId) && !isPersonalSpace(spaceId)) {
      helper.setSpaceIdAndSubSpaceId(spaceId);
      gef.setSpaceIdForCurrentRequest(spaceId);
    }

    // New request parameter to manage Bookmarks view or classical view
    UserMenuDisplay displayMode = helper.getDisplayUserMenu();
    if (helper.isMenuPersonalisationEnabled()) {
      if (StringUtil.isDefined(request.getParameter("UserMenuDisplayMode"))) {
        displayMode = UserMenuDisplay.valueOf(request.getParameter("UserMenuDisplayMode"));
      } else if (preferences.getDisplay().isNotDefault() &&
          !UserMenuDisplay.ALL.equals(displayMode) &&
          !UserMenuDisplay.BOOKMARKS.equals(displayMode)) {
        // Initialize displayMode from user preferences
        displayMode = preferences.getDisplay();
      }
    }
    helper.setDisplayUserMenu(displayMode);

    // Retrieve current look
    String defaultLook = gef.getDefaultLookName();
    response.setContentType("text/xml");
    response.setHeader("charset", "UTF-8");

    Writer writer = response.getWriter();
    writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    writer.write("<ajax-response>");
    writer.write("<response type=\"object\" id=\"" + responseId + "\">");

    if ("1".equals(init)) {
      if (!StringUtil.isDefined(spaceId) && !StringUtil.isDefined(componentId)) {
        displayFirstLevelSpaces(userId, preferences.getLanguage(), defaultLook, orgaController,
                helper, writer, listUserFS, displayMode);
      } else {
        // First get space's path cause it can be a subspace
        List<String> spaceIdsPath = getSpaceIdsPath(spaceId, componentId, orgaController);

        // space transverse
        displaySpace(spaceId, componentId, spaceIdsPath, userId, preferences.getLanguage(),
                defaultLook, displayPDC, true, orgaController, helper, writer, listUserFS,
                displayMode, restrictedPath);

        // other spaces
        displayTree(userId, componentId, spaceIdsPath, preferences.getLanguage(),
                defaultLook, orgaController, helper, writer, listUserFS, displayMode);

        displayPDC(displayPDC, spaceId, componentId, userId, mainSessionController, writer);
      }
    } else if (StringUtil.isDefined(axisId) && StringUtil.isDefined(valuePath)) {
      try {
        writer.write("<pdc>");
        getPertinentValues(spaceId, componentId, userId, axisId, valuePath,
                displayContextualPDC, mainSessionController, writer);
        writer.write("</pdc>");
      } catch (PdcException e) {
        SilverTrace.error("lookSilverpeasV5", "Ajax", "root.ERROR");
      }
    } else if (StringUtil.isDefined(spaceId)) {
      if (isPersonalSpace(spaceId)) {
        // Affichage de l'espace perso
        ResourceLocator settings = gef.getFavoriteLookSettings();
        ResourceLocator message = new ResourceLocator(
                "com.stratelia.webactiv.homePage.multilang.homePageBundle",
                preferences.getLanguage());
        serializePersonalSpace(writer, userId, preferences.getLanguage(), orgaController, helper,
                settings, message);
      } else {
        // First get space's path cause it can be a subspace
        List<String> spaceIdsPath = getSpaceIdsPath(spaceId, componentId, orgaController);
        displaySpace(spaceId, componentId, spaceIdsPath, userId, preferences.getLanguage(),
                defaultLook, displayPDC, false, orgaController, helper, writer, listUserFS,
                displayMode,restrictedPath);
        displayPDC(displayPDC, spaceId, componentId, userId, mainSessionController, writer);
      }
    } else if (StringUtil.isDefined(componentId)) {
      displayPDC(displayPDC, spaceId, componentId, userId, mainSessionController, writer);
    } else if (StringUtil.isDefined(pdc)) {
      displayNotContextualPDC(userId, mainSessionController, writer);
    }
    writer.write("</response>");
    writer.write("</ajax-response>");

  }

  private void displayNotContextualPDC(String userId,
          MainSessionController mainSC, Writer writer) throws IOException {
    try {
      writer.write("<pdc>");
      getPertinentAxis(null, null, userId, mainSC, writer);
      writer.write("</pdc>");
    } catch (PdcException e) {
      SilverTrace.error("lookSilverpeasV5", "Ajax", "root.ERROR");
    }
  }

  private void displayPDC(boolean displayPDC, String spaceId, String componentId,
          String userId, MainSessionController mainSC, Writer writer)
          throws IOException {
    try {
      writer.write("<pdc>");
      if (displayPDC) {
        getPertinentAxis(spaceId, componentId, userId, mainSC, writer);
      }
      writer.write("</pdc>");
    } catch (PdcException e) {
      SilverTrace.error("lookSilverpeasV5", "Ajax", "root.ERROR");
    }
  }

  private List<String> getSpaceIdsPath(String spaceId, String componentId,
          OrganisationController orgaController) {
    List<SpaceInst> spacePath = new ArrayList<SpaceInst>();
    if (StringUtil.isDefined(spaceId)) {
      spacePath = orgaController.getSpacePath(spaceId);
    } else if (StringUtil.isDefined(componentId)) {
      spacePath = orgaController.getSpacePathToComponent(componentId);
    }
    List<String> spaceIdsPath = new ArrayList<String>();
    for (SpaceInst space : spacePath) {
      if (spaceIdsPath == null) {
        spaceIdsPath = new ArrayList<String>();
      }
      if (!space.getId().startsWith(Admin.SPACE_KEY_PREFIX)) {
        spaceIdsPath.add(Admin.SPACE_KEY_PREFIX + space.getId());
      } else {
        spaceIdsPath.add(space.getId());
      }
    }
    return spaceIdsPath;
  }

  /**
* @param spaceId : space identifier
* @param listUFS : the list of user favorite space
* @param orgaController : the OrganizationController object
* @return true if the current space contains user favorites sub space, false else if
*/
  private boolean containsFavoriteSubSpace(String spaceId, List<UserFavoriteSpaceVO> listUFS,
          OrganisationController orgaController, String userId) {
    return UserFavoriteSpaceManager.containsFavoriteSubSpace(spaceId, listUFS, orgaController,
            userId);
  }

  /**
* @param listUFS : the list of user favorite space
* @param spaceId : space identifier
* @return true if list of user favorites space contains spaceId identifier, false else if
*/
  private boolean isUserFavoriteSpace(List<UserFavoriteSpaceVO> listUFS, String spaceId) {
    return UserFavoriteSpaceManager.isUserFavoriteSpace(listUFS, spaceId);
  }

  /**
* displaySpace build XML response tree of current spaceId
* @param spaceId
* @param componentId
* @param spacePath
* @param userId
* @param language
* @param defaultLook
* @param displayPDC
* @param displayTransverse
* @param orgaController
* @param helper
* @param writer
* @param listUFS
* @param userMenuDisplayMode
* @throws IOException
*/
  private void displaySpace(String spaceId, String componentId, List<String> spacePath,
          String userId, String language, String defaultLook,
          boolean displayPDC, boolean displayTransverse,
          OrganisationController orgaController, LookHelper helper, Writer writer,
          List<UserFavoriteSpaceVO> listUFS, UserMenuDisplay userMenuDisplayMode, boolean restrictedPath)
      throws IOException {
    boolean isTransverse = false;
    int i = 0;
    while (!isTransverse && i < spacePath.size()) {
      String spaceIdInPath = spacePath.get(i);
      isTransverse = helper.getTopSpaceIds().contains(spaceIdInPath);
      i++;
    }

    if (displayTransverse && !isTransverse) {
      return;
    }

    boolean open = (spacePath != null && spacePath.contains(spaceId));
    if ((open) && (!restrictedPath)) {
      spaceId = spacePath.remove(0);
    }

    // Affichage de l'espace collaboratif
    SpaceInstLight space = orgaController.getSpaceInstLightById(spaceId);
    if (space != null && isSpaceVisible(userId, spaceId, orgaController, helper)) {
      StringBuilder itemSB = new StringBuilder(200);
      itemSB.append("<item open=\"").append(open).append("\" ");
      itemSB.append(getSpaceAttributes(space, language, defaultLook, helper, orgaController));
      itemSB.append(getFavoriteSpaceAttribute(userId, orgaController, listUFS, space, helper));
      itemSB.append(">");

      writer.write(itemSB.toString());

      if (open) {
        // Default display configuration
        boolean spaceBeforeComponent = isSpaceBeforeComponentNeeded(space);
        if (spaceBeforeComponent) {
          getSubSpaces(spaceId, userId, spacePath, componentId, language,
                  defaultLook, orgaController, helper, writer, listUFS, userMenuDisplayMode);
          getComponents(spaceId, componentId, userId, language, orgaController,
                  writer, userMenuDisplayMode, listUFS);
        } else {
          getComponents(spaceId, componentId, userId, language, orgaController,
                  writer, userMenuDisplayMode, listUFS);
          getSubSpaces(spaceId, userId, spacePath, componentId, language,
                  defaultLook, orgaController, helper, writer, listUFS, userMenuDisplayMode);
        }
      }
    }
    writer.write("</item>");
  }

  /**
* @param userId
* @param orgaController
* @param listUFS
* @param space
* @param helper
* @return an XML user favorite space attribute only if User Favorite Space is enable
*/
  private String getFavoriteSpaceAttribute(String userId, OrganisationController orgaController,
          List<UserFavoriteSpaceVO> listUFS, SpaceInstLight space, LookHelper helper) {
    StringBuilder favSpace = new StringBuilder(20);
    if (UserMenuDisplay.DISABLE != helper.getDisplayUserMenu()) {
      favSpace.append(" favspace=\"");
      if (isUserFavoriteSpace(listUFS, space.getShortId())) {
        favSpace.append("true");
      } else {
        if (helper.isEnableUFSContainsState()) {
          if (containsFavoriteSubSpace(space.getShortId(), listUFS, orgaController, userId)) {
            favSpace.append("contains");
          } else {
            favSpace.append("false");
          }
        } else {
          favSpace.append("false");
        }
      }
      favSpace.append("\"");
    }
    return favSpace.toString();
  }

  private void displayTree(String userId, String targetComponentId,
          List<String> spacePath, String language, String defaultLook,
          OrganisationController orgaController, LookHelper helper, Writer out,
          List<UserFavoriteSpaceVO> listUFS, UserMenuDisplay userMenuDisplayMode)
      throws IOException {
    // Then get all first level spaces
    String[] availableSpaceIds = getRootSpaceIds(userId, orgaController, helper);

    out.write("<spaces menu=\"" + helper.getDisplayUserMenu() + "\">");
    String spaceId = null;

    for (int nI = 0; nI < availableSpaceIds.length; nI++) {
      spaceId = availableSpaceIds[nI];
      boolean loadCurSpace = isLoadingContentNeeded(userMenuDisplayMode, userId, spaceId, listUFS,
              orgaController);
      if (loadCurSpace && isSpaceVisible(userId, spaceId, orgaController, helper)) {
        displaySpace(spaceId, targetComponentId, spacePath, userId, language,
                defaultLook, false, false, orgaController, helper, out, listUFS,
            userMenuDisplayMode, false);
      }
      loadCurSpace = false;
    }
    out.write("</spaces>");
  }

  private String getSpaceAttributes(SpaceInstLight space, String language,
          String defaultLook, LookHelper helper, OrganisationController orga) {
    String spaceLook = getSpaceLookAttribute(space, defaultLook, orga);
    String spaceWallpaper = getWallPaper(space.getFullId());
    String spaceCSS = SilverpeasLook.getSilverpeasLook().getSpaceWithCSS(space.getFullId());

    boolean isTransverse = helper.getTopSpaceIds().contains(space.getFullId());

    String attributeType = "space";
    if (isTransverse) {
      attributeType = "spaceTransverse";
    }

    return "id=\"" + space.getFullId() + "\" name=\""
            + EncodeHelper.escapeXml(space.getName(language)) + "\" description=\""
            + EncodeHelper.escapeXml(space.getDescription()) + "\" type=\""
            + attributeType + "\" kind=\"space\" level=\"" + space.getLevel()
            + "\" look=\"" + spaceLook + "\" wallpaper=\"" + spaceWallpaper + "\""
            + " css=\""+spaceCSS+"\"";
  }

  /**
* Recursive method to get the right look.
* @param space
* @param defaultLook : current default look name
* @param orga : the organization controller
* @return the space style according to the space hierarchy
*/
  private String getSpaceLookAttribute(SpaceInstLight space, String defaultLook,
          OrganisationController orga) {
    String spaceLook = space.getLook();
    if (!StringUtil.isDefined(spaceLook)) {
      if (!space.isRoot()) {
        SpaceInstLight fatherSpace = orga.getSpaceInstLightById(space.getFatherId());
        spaceLook = getSpaceLookAttribute(fatherSpace, defaultLook, orga);
      } else {
        spaceLook = defaultLook;
      }
    }
    return spaceLook;
  }

  private void displayFirstLevelSpaces(String userId, String language,
          String defaultLook, OrganisationController orgaController, LookHelper helper,
          Writer out, List<UserFavoriteSpaceVO> listUFS, UserMenuDisplay userMenuDisplayMode)
          throws IOException {
    String[] availableSpaceIds = getRootSpaceIds(userId, orgaController, helper);

    // Loop variable declaration
    SpaceInstLight space = null;
    String spaceId = null;
    // Start writing XML spaces node
    out.write("<spaces menu=\"" + helper.getDisplayUserMenu() + "\">");
    for (int nI = 0; nI < availableSpaceIds.length; nI++) {
      spaceId = availableSpaceIds[nI];
      boolean loadCurSpace = isLoadingContentNeeded(userMenuDisplayMode, userId, spaceId, listUFS,
              orgaController);
      if (loadCurSpace && isSpaceVisible(userId, spaceId, orgaController, helper)) {
        space = orgaController.getSpaceInstLightById(spaceId);
        if (space != null) {
          StringBuilder itemSB = new StringBuilder(200);
          itemSB.append("<item ");
          itemSB.append(getSpaceAttributes(space, language, defaultLook, helper, orgaController));
          itemSB.append(getFavoriteSpaceAttribute(userId, orgaController, listUFS, space, helper));
          itemSB.append("/>");
          out.write(itemSB.toString());
        }
      }
      loadCurSpace = false;
    }
    out.write("</spaces>");
  }

  private void getSubSpaces(String spaceId, String userId, List<String> spacePath,
          String targetComponentId, String language, String defaultLook,
          OrganisationController orgaController, LookHelper helper, Writer out,
          List<UserFavoriteSpaceVO> listUFS, UserMenuDisplay userMenuDisplayMode)
          throws IOException {
    String[] spaceIds = orgaController.getAllSubSpaceIds(spaceId, userId);

    String subSpaceId = null;
    boolean open = false;
    boolean loadCurSpace = false;
    for (int nI = 0; nI < spaceIds.length; nI++) {
      subSpaceId = spaceIds[nI];
      SpaceInstLight space = orgaController.getSpaceInstLightById(subSpaceId);
      if (space != null) {
        open = (spacePath != null && spacePath.contains(subSpaceId));
        // Check user favorite space
        loadCurSpace = isLoadingContentNeeded(userMenuDisplayMode, userId, subSpaceId, listUFS,
                orgaController);
        if (loadCurSpace && isSpaceVisible(userId, subSpaceId, orgaController, helper)) {
          StringBuilder itemSB = new StringBuilder(200);
          itemSB.append("<item ");
          itemSB.append(getSpaceAttributes(space, language, defaultLook, helper, orgaController));
          itemSB.append(" open=\"").append(open).append("\"");
          itemSB.append(getFavoriteSpaceAttribute(userId, orgaController, listUFS, space, helper));
          itemSB.append(">");

          out.write(itemSB.toString());

          if (open) {
            // Default display configuration
            boolean spaceBeforeComponent = isSpaceBeforeComponentNeeded(space);
            // the subtree must be displayed
            // components of expanded space must be displayed too
            if (spaceBeforeComponent) {
              getSubSpaces(subSpaceId, userId, spacePath, targetComponentId,
                      language, defaultLook, orgaController, helper, out, listUFS,
                      userMenuDisplayMode);
              getComponents(subSpaceId, targetComponentId, userId, language,
                      orgaController, out, userMenuDisplayMode, listUFS);
            } else {
              getComponents(subSpaceId, targetComponentId, userId, language,
                      orgaController, out, userMenuDisplayMode, listUFS);
              getSubSpaces(subSpaceId, userId, spacePath, targetComponentId,
                      language, defaultLook, orgaController, helper, out, listUFS,
                      userMenuDisplayMode);
            }

          }

          out.write("</item>");
        }
        loadCurSpace = false;
      }
    }
  }

  private void getComponents(String spaceId, String targetComponentId,
          String userId, String language, OrganisationController orgaController,
          Writer out, UserMenuDisplay userMenuDisplayMode, List<UserFavoriteSpaceVO> listUFS)
          throws IOException {
    boolean loadCurComponent =
        isLoadingContentNeeded(userMenuDisplayMode, userId, spaceId, listUFS,
            orgaController);
    if (loadCurComponent) {
      String[] componentIds = orgaController.getAvailCompoIdsAtRoot(spaceId, userId);
      SpaceInstLight space = orgaController.getSpaceInstLightById(spaceId);
      int level = space.getLevel() + 1;
      ComponentInst component = null;
      boolean open = false;
      String url = null;
      String kind = null;
      for (int c = 0; componentIds != null && c < componentIds.length; c++) {
        component = orgaController.getComponentInst(componentIds[c]);
        if (component != null && !component.isHidden()) {
          open = (targetComponentId != null && component.getId().equals(
                  targetComponentId));
          url = URLManager.getURL(component.getName(), null, component.getId()) + "Main";

          kind = component.getName();
          if (component.isWorkflow()) {
            kind = "processManager";
          }

          out.write("<item id=\"" + component.getId() + "\" name=\""
                  + EncodeHelper.escapeXml(component.getLabel(language))
                  + "\" description=\""
                  + EncodeHelper.escapeXml(component.getDescription(language))
                  + "\" type=\"component\" kind=\"" + EncodeHelper.escapeXml(kind)
                  + "\" level=\"" + level + "\" open=\"" + open + "\" url=\"" + url
                  + "\"/>");
        }
      }
    }
  }

  private void getPertinentAxis(String spaceId, String componentId,
          String userId, MainSessionController mainSC, Writer out)
          throws PdcException, IOException {
    List<SearchAxis> primaryAxis = null;
    SearchContext searchContext = new SearchContext();

    PdcBm pdc = new PdcBmImpl();

    if (StringUtil.isDefined(componentId)) {
      // L'item courant est un composant
      primaryAxis = pdc.getPertinentAxisByInstanceId(searchContext, "P",
              componentId);
    } else {
      List<String> cmps = null;
      if (StringUtil.isDefined(spaceId)) {
        // L'item courant est un espace
        cmps = getAvailableComponents(spaceId, userId, mainSC.getOrganisationController());
      } else {
        cmps = Arrays.asList(mainSC.getUserAvailComponentIds());
      }

      if (cmps != null && cmps.size() > 0) {
        primaryAxis = pdc.getPertinentAxisByInstanceIds(searchContext, "P",
                cmps);
      }
    }
    SearchAxis axis = null;
    if (primaryAxis != null) {
      for (int a = 0; a < primaryAxis.size(); a++) {
        axis = primaryAxis.get(a);
        if (axis != null && axis.getNbObjects() > 0) {
          out.write("<axis id=\"" + axis.getAxisId() + "\" name=\""
                  + EncodeHelper.escapeXml(axis.getAxisName())
                  + "\" description=\"\" level=\"0\" open=\"false\" nbObjects=\""
                  + axis.getNbObjects() + "\"/>");
        }
      }
    }
    pdc = null;
    primaryAxis = null;
  }

  private List<String> getAvailableComponents(String spaceId, String userId,
          OrganisationController orgaController) {
    String a[] = orgaController.getAvailCompoIds(spaceId, userId);
    return Arrays.asList(a);
  }

  private String getValueId(String valuePath) {
    // cherche l'id de la valeur
    // valuePath est de la forme /0/1/2/

    String valueId = valuePath;
    int len = valuePath.length();
    valueId = valuePath.substring(0, len - 1); // on retire le slash

    if ("/".equals(valuePath)) {
      valueId = valueId.substring(1);// on retire le slash
    } else {
      int lastIdx = valueId.lastIndexOf('/');
      valueId = valueId.substring(lastIdx + 1);
    }
    return valueId;
  }

  private List<Value> getPertinentValues(String spaceId, String componentId,
          String userId, String axisId, String valuePath,
          boolean displayContextualPDC, MainSessionController mainSC, Writer out)
      throws IOException,
          PdcException {
    List<Value> daughters = null;
    SearchContext searchContext = new SearchContext();
    searchContext.setUserId(userId);

    if (StringUtil.isDefined(axisId)) {
      PdcBm pdc = new PdcBmImpl();

      // TODO : some improvements can be made here !
      // daughters contains all pertinent values of axis instead of pertinent
      // daughters only
      if (displayContextualPDC) {
        if (StringUtil.isDefined(componentId)) {
          daughters = pdc.getPertinentDaughterValuesByInstanceId(searchContext,
                  axisId, valuePath, componentId);
        } else {
          List<String> cmps = getAvailableComponents(spaceId, userId, mainSC.
              getOrganisationController());
          daughters = pdc.getPertinentDaughterValuesByInstanceIds(
                  searchContext, axisId, valuePath, cmps);
        }
      } else {
        List<String> cmps = Arrays.asList(mainSC.getUserAvailComponentIds());
        daughters = pdc.getPertinentDaughterValuesByInstanceIds(searchContext,
                axisId, valuePath, cmps);
      }

      String valueId = getValueId(valuePath);

      Value value = null;
      for (int v = 0; v < daughters.size(); v++) {
        value = daughters.get(v);
        if (value != null && value.getMotherId().equals(valueId)) {
          out.write("<value id=\"" + value.getFullPath() + "\" name=\""
                  + EncodeHelper.escapeXml(value.getName())
                  + "\" description=\"\" level=\"" + value.getLevelNumber()
                  + "\" open=\"false\" nbObjects=\"" + value.getNbObjects()
                  + "\"/>");
        }
      }

      pdc = null;
    }

    return daughters;
  }

  private String getWallPaper(String spaceId) {
    if (SilverpeasLook.getSilverpeasLook().hasSpaceWallpaper(spaceId)) {
      return "1";
    }
    return "0";
  }

  private String[] getRootSpaceIds(String userId, OrganisationController orgaController,
          LookHelper helper) {
    List<String> rootSpaceIds = new ArrayList<String>();
    List<String> topSpaceIds = helper.getTopSpaceIds();
    String[] availableSpaceIds = orgaController.getAllRootSpaceIds(userId);
    for (int i = 0; i < availableSpaceIds.length; i++) {
      if (!topSpaceIds.contains(availableSpaceIds[i])) {
        rootSpaceIds.add(availableSpaceIds[i]);
      }
    }
    return rootSpaceIds.toArray(new String[rootSpaceIds.size()]);
  }

  protected boolean isPersonalSpace(String spaceId) {
    return SpaceInst.PERSONAL_SPACE_ID.equalsIgnoreCase(spaceId);
  }

  protected void serializePersonalSpace(Writer writer, String userId, String language,
          OrganisationController orgaController, LookHelper helper, ResourceLocator settings,
          ResourceLocator message) throws IOException {
    // Affichage de l'espace perso
    writer.write("<spacePerso id=\"spacePerso\" type=\"space\" level=\"0\">");
    boolean isAnonymousAccess = helper.isAnonymousAccess();

    if (!isAnonymousAccess && settings.getBoolean("personnalSpaceVisible", true)) {
      if (settings.getBoolean("agendaVisible", true)) {
        writer.write("<item id=\"agenda\" name=\""
                + EncodeHelper.escapeXml(message.getString("Diary"))
                +
            "\" description=\"\" type=\"component\" kind=\"\" level=\"1\" open=\"false\" url=\""
                + URLManager.getURL(URLManager.CMP_AGENDA, null, null) + "Main\"/>");
      }
      if (settings.getBoolean("todoVisible", true)) {
        writer.write("<item id=\"todo\" name=\""
                + EncodeHelper.escapeXml(message.getString("ToDo"))
                +
            "\" description=\"\" type=\"component\" kind=\"\" level=\"1\" open=\"false\" url=\""
                + URLManager.getURL(URLManager.CMP_TODO, null, null) + "todo.jsp\"/>");
      }
      if (settings.getBoolean("notificationVisible", true)) {
        writer.write("<item id=\"notification\" name=\""
                + EncodeHelper.escapeXml(message.getString("Mail"))
                +
            "\" description=\"\" type=\"component\" kind=\"\" level=\"1\" open=\"false\" url=\""
                + URLManager.getURL(URLManager.CMP_SILVERMAIL, null, null) + "Main\"/>");
      }
      if (settings.getBoolean("interestVisible", true)) {
        writer.write("<item id=\"subscriptions\" name=\""
                + EncodeHelper.escapeXml(message.getString("MyInterestCenters"))
                +
            "\" description=\"\" type=\"component\" kind=\"\" level=\"1\" open=\"false\" url=\""
                + URLManager.getURL(URLManager.CMP_PDCSUBSCRIPTION, null, null)
                + "subscriptionList.jsp\"/>");
      }
      if (settings.getBoolean("favRequestVisible", true)) {
        writer.write("<item id=\"requests\" name=\""
                + EncodeHelper.escapeXml(message.getString("FavRequests"))
                +
            "\" description=\"\" type=\"component\" kind=\"\" level=\"1\" open=\"false\" url=\""
                + URLManager.getURL(URLManager.CMP_INTERESTCENTERPEAS, null, null)
                + "iCenterList.jsp\"/>");
      }
      if (settings.getBoolean("linksVisible", true)) {
        writer.write("<item id=\"links\" name=\""
                + EncodeHelper.escapeXml(message.getString("FavLinks"))
                +
            "\" description=\"\" type=\"component\" kind=\"\" level=\"1\" open=\"false\" url=\""
                + URLManager.getURL(URLManager.CMP_MYLINKSPEAS, null, null)
                + "Main\"/>");
      }
      if (settings.getBoolean("fileSharingVisible", true)) {
        if (!SharingServiceFactory.getSharingTicketService().getTicketsByUser(userId).isEmpty()) {
          writer.write("<item id=\"sharingTicket\" name=\""
                  + EncodeHelper.escapeXml(message.getString("FileSharing"))
                  +
              "\" description=\"\" type=\"component\" kind=\"\" level=\"1\" open=\"false\" url=\""
                  + URLManager.getURL(URLManager.CMP_FILESHARING, null, null)
                  + "Main\"/>");
        }
      }
      // mes connexions
      if (settings.getBoolean("webconnectionsVisible", true)) {
        WebConnectionsInterface webConnections = new WebConnectionService();
        if (webConnections.listWebConnectionsOfUser(userId).size() > 0) {
          writer.write("<item id=\"webConnections\" name=\""
                  + EncodeHelper.escapeXml(message.getString("WebConnections"))
                  +
              "\" description=\"\" type=\"component\" kind=\"\" level=\"1\" open=\"false\" url=\""
                  + URLManager.getURL(URLManager.CMP_WEBCONNECTIONS, null, null)
                  + "Main\"/>");
        }
      }

      // fonctionnalité "Trouver une date"
      if (settings.getBoolean("scheduleEventVisible", false)) {
        writer.write("<item id=\"scheduleevent\" name=\""
                + EncodeHelper.escapeXml(message.getString("ScheduleEvent"))
                +
            "\" description=\"\" type=\"component\" kind=\"\" level=\"1\" open=\"false\" url=\""
                + URLManager.getURL(URLManager.CMP_SCHEDULE_EVENT, null, null) + "Main\"/>");
      }

      if (settings.getBoolean("customVisible", true)) {
        writer.write("<item id=\"personalize\" name=\""
                + EncodeHelper.escapeXml(message.getString("Personalization"))
                +
            "\" description=\"\" type=\"component\" kind=\"\" level=\"1\" open=\"false\" url=\""
                + URLManager.getURL(URLManager.CMP_MYPROFILE, null, null)
                + "Main\"/>");
      }
      if (settings.getBoolean("mailVisible", true)) {
        writer
            .write(
                "<item id=\"notifAdmins\" name=\""
                    +
                    EncodeHelper.escapeXml(message.getString("Feedback"))
                    +
                    "\" description=\"\" type=\"component\" kind=\"\" level=\"1\" open=\"false\" url=\"javascript:notifyAdministrators()\"/>");
      }
      if (settings.getBoolean("clipboardVisible", true)) {
        writer
            .write(
                "<item id=\"clipboard\" name=\""
                    +
                    EncodeHelper.escapeXml(message.getString("Clipboard"))
                    +
                    "\" description=\"\" type=\"component\" kind=\"\" level=\"1\" open=\"false\" url=\"javascript:openClipboard()\"/>");
      }

      if (settings.getBoolean("PersonalSpaceAddingsEnabled", true)) {
        PersonalSpaceController psc = new PersonalSpaceController();
        SpaceInst personalSpace = psc.getPersonalSpace(userId);
        if (personalSpace != null) {
          for (ComponentInst component : personalSpace.getAllComponentsInst()) {
            String label =
                    helper.getString("lookSilverpeasV5.personalSpace." + component.getName());
            if (!StringUtil.isDefined(label)) {
              label = component.getName();
            }
            String url = URLManager.getURL(component.getName(), null, component.getName()
                    + component.getId()) + "Main";
            writer
                .write("<item id=\""
                    +
                    component.getName()
                    +
                    component.getId()
                    +
                    "\" name=\""
                    +
                    EncodeHelper.escapeXml(label)
                    +
                    "\" description=\"\" type=\"component\" kind=\"personalComponent\" level=\"1\" open=\"false\" url=\""
                    + url + "\"/>");
          }
        }
        int nbComponentAvailables = psc.getVisibleComponents(orgaController).size();
        if (nbComponentAvailables > 0) {
          if (personalSpace == null ||
              personalSpace.getAllComponentsInst().size() < nbComponentAvailables) {
            writer
                .write(
                    "<item id=\"addComponent\" name=\""
                        +
                        EncodeHelper.escapeXml(helper
                            .getString("lookSilverpeasV5.personalSpace.add"))
                        +
                        "\" description=\"\" type=\"component\" kind=\"\" level=\"1\" open=\"false\" url=\"javascript:listComponents()\"/>");
          }
        }
      }
    }
    writer.write("</spacePerso>");
  }

  protected boolean isLoadingContentNeeded(UserMenuDisplay userMenuDisplayMode, String userId,
          String spaceId, List<UserFavoriteSpaceVO> listUFS, OrganisationController orgaController) {
    switch (userMenuDisplayMode) {
      case DISABLE:
      case ALL:
        return true;
      case BOOKMARKS:
        return isUserFavoriteSpace(listUFS, spaceId) || containsFavoriteSubSpace(spaceId, listUFS,
                orgaController, userId);
    }
    return false;
  }

  /**
* a Space is visible if at least one of its items is visible for the currentUser
* @param userId
* @param spaceId
* @param orgaController
* @param helper
* @return true or false
*/
  protected boolean isSpaceVisible(String userId, String spaceId,
          OrganisationController orgaController, LookHelper helper) {
    if (helper.getSettings("displaySpaceContainingOnlyHiddenComponents", true)) {
      return true;
    }
    String compoIds[] = orgaController.getAvailCompoIds(spaceId, userId);
    for (String id : compoIds) {
      ComponentInst compInst = orgaController.getComponentInst(id);
      if (!compInst.isHidden()) {
        return true;
      }
    }
    return false;
  }

  protected boolean isSpaceBeforeComponentNeeded(SpaceInstLight space) {
    // Display computing : First look at global configuration
    if (JobStartPagePeasSettings.SPACEDISPLAYPOSITION_CONFIG.equalsIgnoreCase(
            JobStartPagePeasSettings.SPACEDISPLAYPOSITION_BEFORE)) {
      return true;
    } else if (JobStartPagePeasSettings.SPACEDISPLAYPOSITION_CONFIG.equalsIgnoreCase(
            JobStartPagePeasSettings.SPACEDISPLAYPOSITION_AFTER)) {
      return false;
    } else if (JobStartPagePeasSettings.SPACEDISPLAYPOSITION_CONFIG.equalsIgnoreCase(
            JobStartPagePeasSettings.SPACEDISPLAYPOSITION_TODEFINE)) {
      return space.isDisplaySpaceFirst();
    }
    return true;
  }
}