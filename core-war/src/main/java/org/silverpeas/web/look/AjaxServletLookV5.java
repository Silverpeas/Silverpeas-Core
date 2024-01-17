/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.look;

import org.owasp.encoder.Encode;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.space.PersonalSpaceManager;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.space.UserFavoriteSpaceService;
import org.silverpeas.core.admin.space.model.UserFavoriteSpaceVO;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.SearchAxis;
import org.silverpeas.core.pdc.pdc.model.SearchContext;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.personalization.UserMenuDisplay;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.external.webconnections.model.WebConnectionsInterface;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.web.look.SilverpeasLook;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.webcomponent.SilverpeasAuthenticatedHttpServlet;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.web.jobstartpage.JobStartPagePeasSettings;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;

import static org.silverpeas.core.sharing.services.SharingServiceProvider.getSharingTicketService;

public class AjaxServletLookV5 extends SilverpeasAuthenticatedHttpServlet {

  private static final long serialVersionUID = 1L;
  private static final String PDC_START_TAG = "<pdc>";
  private static final String PDC_END_TAG = "</pdc>";
  private static final String NAME = "\" name=\"";

  @Inject
  private OrganizationController organisationController;
  @Inject
  private PersonalSpaceManager personalSpaceManager;
  @Inject
  private UserFavoriteSpaceService userFavoriteSpaceService;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    HttpSession session = request.getSession(true);
    MainSessionController mainSessionController = (MainSessionController) session
        .getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    GraphicElementFactory gef =
        (GraphicElementFactory) session.getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
    LookHelper helper = LookHelper.getLookHelper(session);

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
    List<UserFavoriteSpaceVO> listUserFS = new ArrayList<>();
    if (helper.isMenuPersonalisationEnabled()) {
      listUserFS = userFavoriteSpaceService.getListUserFavoriteSpace(userId);
    }

    // Set current space and component identifier (helper and gef)
    if (StringUtil.isDefined(componentId)) {
      helper.setComponentIdAndSpaceIds(null, null, componentId);
    } else if (StringUtil.isDefined(spaceId) && !isPersonalSpace(spaceId)) {
      helper.setSpaceIdAndSubSpaceId(spaceId);
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

    try {
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
          displayFirstLevelSpaces(userId, preferences.getLanguage(), defaultLook, helper, writer,
              listUserFS, displayMode);
        } else {
          // First get space's path cause it can be a subspace
          List<String> spaceIdsPath = getSpaceIdsPath(spaceId, componentId, organisationController);

          // space transverse
          displaySpace(spaceId, componentId, spaceIdsPath, userId, preferences.getLanguage(),
              defaultLook, true, helper, writer, listUserFS, displayMode, restrictedPath);

          // other spaces
          displayTree(userId, componentId, spaceIdsPath, preferences.getLanguage(), defaultLook,
              helper, writer, listUserFS, displayMode);

          displayPDC(displayPDC, spaceId, componentId, userId, mainSessionController, writer);
        }
      } else if (StringUtil.isDefined(axisId) && StringUtil.isDefined(valuePath)) {
        writePertinentValues(mainSessionController, userId, spaceId, componentId, axisId, valuePath,
            displayContextualPDC, writer);
      } else if (StringUtil.isDefined(spaceId)) {
        if (isPersonalSpace(spaceId)) {
          // Affichage de l'espace perso
          SettingBundle settings = gef.getFavoriteLookSettings();
          LocalizationBundle message = ResourceLocator.getLocalizationBundle(
              "org.silverpeas.homePage.multilang.homePageBundle", preferences.getLanguage());
          serializePersonalSpace(writer, userId, helper, settings, message);
        } else {
          // First get space's path cause it can be a subspace
          List<String> spaceIdsPath = getSpaceIdsPath(spaceId, componentId, organisationController);
          displaySpace(spaceId, componentId, spaceIdsPath, userId, preferences.getLanguage(),
              defaultLook, false, helper, writer, listUserFS, displayMode, restrictedPath);
          displayPDC(displayPDC, spaceId, componentId, userId, mainSessionController, writer);
        }
      } else if (StringUtil.isDefined(componentId)) {
        displayPDC(displayPDC, spaceId, componentId, userId, mainSessionController, writer);
      } else if (StringUtil.isDefined(pdc)) {
        displayNotContextualPDC(userId, mainSessionController, writer);
      }
      writer.write("</response>");
      writer.write("</ajax-response>");
    } catch (IOException e) {
      SilverLogger.getLogger(this).error(e);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private void writePertinentValues(final MainSessionController mainSessionController,
      final String userId, final String spaceId, final String componentId, final String axisId,
      final String valuePath, final boolean displayContextualPDC, final Writer writer)
      throws IOException {
    try {
      writer.write(PDC_START_TAG);
      getPertinentValues(spaceId, componentId, userId, axisId, valuePath, displayContextualPDC,
          mainSessionController, writer);
      writer.write(PDC_END_TAG);
    } catch (PdcException e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  private void displayNotContextualPDC(String userId, MainSessionController mainSC, Writer writer)
      throws IOException {
    try {
      writer.write(PDC_START_TAG);
      getPertinentAxis(null, null, userId, mainSC, writer);
      writer.write(PDC_END_TAG);
    } catch (PdcException e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  private void displayPDC(boolean displayPDC, String spaceId, String componentId, String userId,
      MainSessionController mainSC, Writer writer) throws IOException {
    try {
      writer.write(PDC_START_TAG);
      if (displayPDC) {
        getPertinentAxis(spaceId, componentId, userId, mainSC, writer);
      }
      writer.write(PDC_END_TAG);
    } catch (PdcException e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  private List<String> getSpaceIdsPath(String spaceId, String componentId,
      OrganizationController orgaController) {
    List<SpaceInstLight> spacePath = new ArrayList<>();
    if (StringUtil.isDefined(spaceId)) {
      spacePath = orgaController.getPathToSpace(spaceId);
    } else if (StringUtil.isDefined(componentId)) {
      spacePath = orgaController.getPathToComponent(componentId);
    }
    List<String> spaceIdsPath = new ArrayList<>();
    for (SpaceInstLight space : spacePath) {
      spaceIdsPath.add(space.getId());
    }
    return spaceIdsPath;
  }

  /**
   * @param space : the space instance
   * @param listUFS : the list of user favorite space
   * @return true if the current space contains user favorites sub space, false else if
   */
  private boolean containsFavoriteSubSpace(SpaceInstLight space, List<UserFavoriteSpaceVO> listUFS,
      String userId) {
    return userFavoriteSpaceService.containsFavoriteSubSpace(space, listUFS, userId);
  }

  /**
   * @param listUFS : the list of user favorite space
   * @param space : the space instance
   * @return true if list of user favorites space contains spaceId identifier, false else if
   */
  private boolean isUserFavoriteSpace(List<UserFavoriteSpaceVO> listUFS, SpaceInstLight space) {
    return userFavoriteSpaceService.isUserFavoriteSpace(listUFS, space);
  }

  /**
   * displaySpace build XML response tree of current spaceId
   * @param spaceId
   * @param componentId
   * @param spacePath
   * @param userId
   * @param language
   * @param defaultLook
   * @param displayTransverse
   * @param helper
   * @param writer
   * @param listUFS
   * @param userMenuDisplayMode
   * @throws IOException
   */
  private void displaySpace(String spaceId, String componentId, List<String> spacePath,
      String userId, String language, String defaultLook, boolean displayTransverse,
      LookHelper helper, Writer writer, List<UserFavoriteSpaceVO> listUFS,
      UserMenuDisplay userMenuDisplayMode, boolean restrictedPath) throws IOException {
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

    boolean open = spacePath.contains(spaceId);
    if (open && !restrictedPath) {
      spaceId = spacePath.remove(0);
    }

    // Affichage de l'espace collaboratif
    SpaceInstLight space = organisationController.getSpaceInstLightById(spaceId);
    if (space != null && isSpaceVisible(userId, space, helper)) {
      StringBuilder itemSB = new StringBuilder(200);
      itemSB.append("<item open=\"").append(open).append("\" ");
      itemSB.append(getSpaceAttributes(space, language, defaultLook, helper));
      itemSB.append(getFavoriteSpaceAttribute(userId, listUFS, space, helper));
      itemSB.append(">");

      writer.write(itemSB.toString());

      if (open) {
        // Default display configuration
        boolean spaceBeforeComponent = isSpaceBeforeComponentNeeded(space);
        if (spaceBeforeComponent) {
          getSubSpaces(spaceId, userId, spacePath, componentId, language, defaultLook, helper,
              writer, listUFS, userMenuDisplayMode);
          getComponents(spaceId, componentId, userId, language, writer, userMenuDisplayMode,
              listUFS);
        } else {
          getComponents(spaceId, componentId, userId, language, writer, userMenuDisplayMode,
              listUFS);
          getSubSpaces(spaceId, userId, spacePath, componentId, language, defaultLook, helper,
              writer, listUFS, userMenuDisplayMode);
        }
      }
      writer.write("</item>");
    }
  }

  /**
   * @param userId
   * @param listUFS
   * @param space
   * @param helper
   * @return an XML user favorite space attribute only if User Favorite Space is enable
   */
  private String getFavoriteSpaceAttribute(String userId, List<UserFavoriteSpaceVO> listUFS,
      SpaceInstLight space, LookHelper helper) {
    StringBuilder favSpace = new StringBuilder(20);
    if (UserMenuDisplay.DISABLE != helper.getDisplayUserMenu()) {
      favSpace.append(" favspace=\"");
      if (isUserFavoriteSpace(listUFS, space)) {
        favSpace.append("true");
      } else {
        if (helper.isEnableUFSContainsState()) {
          if (containsFavoriteSubSpace(space, listUFS, userId)) {
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

  private void displayTree(String userId, String targetComponentId, List<String> spacePath,
      String language, String defaultLook, LookHelper helper, Writer out,
      List<UserFavoriteSpaceVO> listUFS, UserMenuDisplay userMenuDisplayMode) throws IOException {
    // Then get all first level spaces
    String[] availableSpaceIds = getRootSpaceIds(userId, helper);

    out.write("<spaces menu=\"" + helper.getDisplayUserMenu() + "\">");
    String spaceId;

    for (final String availableSpaceId : availableSpaceIds) {
      spaceId = availableSpaceId;
      SpaceInstLight spaceInst = organisationController.getSpaceInstLightById(spaceId);
      boolean loadCurSpace =
          isLoadingContentNeeded(userMenuDisplayMode, userId, spaceInst, listUFS);
      if (loadCurSpace && isSpaceVisible(userId, spaceInst, helper)) {
        displaySpace(spaceId, targetComponentId, spacePath, userId, language, defaultLook, false,
            helper, out, listUFS, userMenuDisplayMode, false);
      }
    }
    out.write("</spaces>");
  }

  private String getSpaceAttributes(SpaceInstLight space, String language, String defaultLook,
      LookHelper helper) {
    String spaceLook = getSpaceLookAttribute(space, defaultLook);
    String spaceWallpaper = getWallPaper(space.getId());
    String spaceCSS = SilverpeasLook.getSilverpeasLook().getSpaceWithCSS(space.getId());

    boolean isTransverse = helper.getTopSpaceIds().contains(space.getId());

    String attributeType = "space";
    if (isTransverse) {
      attributeType = "spaceTransverse";
    }

    return "id=\"" + space.getId() + NAME +
        Encode.forXml(space.getName(language)) + "\" description=\"" +
        Encode.forXml(space.getDescription()) + "\" type=\"" + attributeType +
        "\" kind=\"space\" level=\"" + space.getLevel() + "\" look=\"" + spaceLook +
        "\" wallpaper=\"" + spaceWallpaper + "\"" + " css=\"" + spaceCSS + "\"";
  }

  /**
   * Recursive method to get the right look.
   * @param space
   * @param defaultLook : current default look name
   * @return the space style according to the space hierarchy
   */
  private String getSpaceLookAttribute(SpaceInstLight space, String defaultLook) {
    String spaceLook = space.getLook();
    if (!StringUtil.isDefined(spaceLook)) {
      if (!space.isRoot()) {
        SpaceInstLight fatherSpace =
            organisationController.getSpaceInstLightById(space.getFatherId());
        spaceLook = getSpaceLookAttribute(fatherSpace, defaultLook);
      } else {
        spaceLook = defaultLook;
      }
    }
    return spaceLook;
  }

  private void displayFirstLevelSpaces(String userId, String language, String defaultLook,
      LookHelper helper, Writer out, List<UserFavoriteSpaceVO> listUFS,
      UserMenuDisplay userMenuDisplayMode) throws IOException {
    String[] availableSpaceIds = getRootSpaceIds(userId, helper);

    // Loop variable declaration
    SpaceInstLight space;
    String spaceId;
    // Start writing XML spaces node
    out.write("<spaces menu=\"" + helper.getDisplayUserMenu() + "\">");
    for (final String availableSpaceId : availableSpaceIds) {
      spaceId = availableSpaceId;
      space = organisationController.getSpaceInstLightById(spaceId);
      boolean loadCurSpace = isLoadingContentNeeded(userMenuDisplayMode, userId, space, listUFS);
      if (loadCurSpace && isSpaceVisible(userId, space, helper) && space != null) {
        StringBuilder itemSB = new StringBuilder(200);
        itemSB.append("<item ");
        itemSB.append(getSpaceAttributes(space, language, defaultLook, helper));
        itemSB.append(getFavoriteSpaceAttribute(userId, listUFS, space, helper));
        itemSB.append("/>");
        out.write(itemSB.toString());
      }
    }
    out.write("</spaces>");
  }

  private void getSubSpaces(String spaceId, String userId, List<String> spacePath,
      String targetComponentId, String language, String defaultLook, LookHelper helper, Writer out,
      List<UserFavoriteSpaceVO> listUFS, UserMenuDisplay userMenuDisplayMode) throws IOException {
    String[] spaceIds = organisationController.getAllSubSpaceIds(spaceId, userId);

    String subSpaceId;
    boolean open;
    boolean loadCurSpace;
    for (final String spaceId1 : spaceIds) {
      subSpaceId = spaceId1;
      SpaceInstLight space = organisationController.getSpaceInstLightById(subSpaceId);
      if (space != null) {
        open = (spacePath != null && spacePath.contains(subSpaceId));
        // Check user favorite space
        loadCurSpace = isLoadingContentNeeded(userMenuDisplayMode, userId, space, listUFS);
        if (loadCurSpace && isSpaceVisible(userId, space, helper)) {
          StringBuilder itemSB = new StringBuilder(200);
          itemSB.append("<item ");
          itemSB.append(getSpaceAttributes(space, language, defaultLook, helper));
          itemSB.append(" open=\"").append(open).append("\"");
          itemSB.append(getFavoriteSpaceAttribute(userId, listUFS, space, helper));
          itemSB.append(">");

          out.write(itemSB.toString());

          if (open) {
            // Default display configuration
            boolean spaceBeforeComponent = isSpaceBeforeComponentNeeded(space);
            // the subtree must be displayed
            // components of expanded space must be displayed too
            if (spaceBeforeComponent) {
              getSubSpaces(subSpaceId, userId, spacePath, targetComponentId, language, defaultLook,
                  helper, out, listUFS, userMenuDisplayMode);
              getComponents(subSpaceId, targetComponentId, userId, language, out,
                  userMenuDisplayMode, listUFS);
            } else {
              getComponents(subSpaceId, targetComponentId, userId, language, out,
                  userMenuDisplayMode, listUFS);
              getSubSpaces(subSpaceId, userId, spacePath, targetComponentId, language, defaultLook,
                  helper, out, listUFS, userMenuDisplayMode);
            }

          }

          out.write("</item>");
        }
      }
    }
  }

  private void getComponents(String spaceId, String targetComponentId, String userId,
      String language, Writer out, UserMenuDisplay userMenuDisplayMode,
      List<UserFavoriteSpaceVO> listUFS) throws IOException {
    SpaceInstLight spaceInst = organisationController.getSpaceInstLightById(spaceId);
    boolean loadCurComponent =
        isLoadingContentNeeded(userMenuDisplayMode, userId, spaceInst, listUFS);
    if (loadCurComponent) {
      String[] componentIds = organisationController.getAvailCompoIdsAtRoot(spaceId, userId);
      SpaceInstLight space = organisationController.getSpaceInstLightById(spaceId);
      int level = space.getLevel() + 1;
      for (String componentId : componentIds) {
        ComponentInst component = organisationController.getComponentInst(componentId);
        if (component != null && WAComponent.getByName(component.getName()).isPresent() &&
            !component.isHidden()) {
          boolean open = (targetComponentId != null && component.getId().equals(targetComponentId));

          String kind = component.getName();
          if (component.isWorkflow()) {
            kind = "processManager";
          }

          ComponentItem.of(component).withLabel(component.getLabel(language))
              .withDescription(component.getDescription(language)).ofKind(kind).atLevel(level)
              .andOpen(open).write(out);
        }
      }
    }
  }

  private void getPertinentAxis(String spaceId, String componentId, String userId,
      MainSessionController mainSC, Writer out) throws PdcException, IOException {
    List<SearchAxis> primaryAxis = null;
    SearchContext searchContext = new SearchContext(userId);

    PdcManager pdc = PdcManager.get();

    if (StringUtil.isDefined(componentId)) {
      // L'item courant est un composant
      primaryAxis = pdc.getPertinentAxisByInstanceId(searchContext, "P", componentId);
    } else {
      List<String> cmps;
      if (StringUtil.isDefined(spaceId)) {
        // L'item courant est un espace
        cmps = getAvailableComponents(spaceId, userId);
      } else {
        cmps = Arrays.asList(mainSC.getUserAvailComponentIds());
      }

      if (!cmps.isEmpty()) {
        primaryAxis = pdc.getPertinentAxisByInstanceIds(searchContext, "P", cmps);
      }
    }
    SearchAxis axis;
    if (primaryAxis != null) {
      for (SearchAxis primaryAxi : primaryAxis) {
        axis = primaryAxi;
        if (axis != null && axis.getNbObjects() > 0) {
          out.write("<axis id=\"" + axis.getAxisId() + NAME +
              Encode.forXml(axis.getAxisName()) +
              "\" description=\"\" level=\"0\" open=\"false\" nbObjects=\"" + axis.getNbObjects() +
              "\"/>");
        }
      }
    }
  }

  private List<String> getAvailableComponents(String spaceId, String userId) {
    String[] a = organisationController.getAvailCompoIds(spaceId, userId);
    return Arrays.asList(a);
  }

  private String getValueId(String valuePath) {
    // cherche l'id de la valeur
    // valuePath est de la forme /0/1/2/

    String valueId;
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

  private List<Value> getPertinentValues(String spaceId, String componentId, String userId,
      String axisId, String valuePath, boolean displayContextualPDC, MainSessionController mainSC,
      Writer out) throws IOException, PdcException {
    List<Value> daughters = null;
    SearchContext searchContext = new SearchContext(userId);

    if (StringUtil.isDefined(axisId)) {
      PdcManager pdc = PdcManager.get();

      // TODO : some improvements can be made here !
      // daughters contains all pertinent values of axis instead of pertinent
      // daughters only
      if (displayContextualPDC) {
        if (StringUtil.isDefined(componentId)) {
          daughters = pdc.getPertinentDaughterValuesByInstanceId(searchContext, axisId, valuePath,
              componentId);
        } else {
          List<String> cmps = getAvailableComponents(spaceId, userId);
          daughters =
              pdc.getPertinentDaughterValuesByInstanceIds(searchContext, axisId, valuePath, cmps);
        }
      } else {
        List<String> cmps = Arrays.asList(mainSC.getUserAvailComponentIds());
        daughters =
            pdc.getPertinentDaughterValuesByInstanceIds(searchContext, axisId, valuePath, cmps);
      }

      String valueId = getValueId(valuePath);

      Value value;
      for (Value daughter : daughters) {
        value = daughter;
        if (value != null && value.getMotherId().equals(valueId)) {
          out.write("<value id=\"" + value.getFullPath() + NAME +
              Encode.forXml(value.getName()) + "\" description=\"\" level=\"" +
              value.getLevelNumber() + "\" open=\"false\" nbObjects=\"" + value.getNbObjects() +
              "\"/>");
        }
      }

    }

    return daughters;
  }

  private String getWallPaper(String spaceId) {
    if (SilverpeasLook.getSilverpeasLook().hasSpaceWallpaper(spaceId)) {
      return "1";
    }
    return "0";
  }

  private String[] getRootSpaceIds(String userId, LookHelper helper) {
    List<String> rootSpaceIds = new ArrayList<>();
    List<String> topSpaceIds = helper.getTopSpaceIds();
    String[] availableSpaceIds = organisationController.getAllRootSpaceIds(userId);
    for (final String availableSpaceId : availableSpaceIds) {
      if (!topSpaceIds.contains(availableSpaceId)) {
        rootSpaceIds.add(availableSpaceId);
      }
    }
    return rootSpaceIds.toArray(new String[rootSpaceIds.size()]);
  }

  protected boolean isPersonalSpace(String spaceId) {
    return SpaceInst.PERSONAL_SPACE_ID.equalsIgnoreCase(spaceId);
  }

  protected void serializePersonalSpace(Writer writer, String userId, LookHelper helper,
      SettingBundle settings, LocalizationBundle message) throws IOException {
    User user = User.getById(userId);

    // Affichage de l'espace perso
    List<ComponentItem> componentItems = new ArrayList<>();
    boolean isAnonymousAccess = helper.isAnonymousAccess();
    boolean isAccessGuest = helper.isAccessGuest();

    if (!isAnonymousAccess && !isAccessGuest && settings.getBoolean("personnalSpaceVisible", true)) {
      if (settings.getBoolean("agendaVisible", true)) {
        componentItems
            .add(ComponentItem.of(URLUtil.CMP_AGENDA).withLabel(message.getString("Diary")));
      }
      if (settings.getBoolean("todoVisible", true)) {
        componentItems.add(ComponentItem.of(URLUtil.CMP_TODO).withLabel(message.getString("ToDo"))
            .andComponentUrlSuffix("todo.jsp"));
      }
      if (settings.getBoolean("notificationVisible", true)) {
        componentItems.add(
            ComponentItem.of(URLUtil.CMP_SILVERMAIL).withLabel(message.getString("Mail"))
                .withSpecificId("notification"));
      }
      if (settings.getBoolean("interestVisible", true)) {
        componentItems.add(ComponentItem.of(URLUtil.CMP_PDCSUBSCRIPTION)
            .withLabel(message.getString("MyInterestCenters")).withSpecificId("subscriptions")
            .andComponentUrlSuffix("subscriptionList.jsp"));
      }
      if (settings.getBoolean("favRequestVisible", true)) {
        componentItems.add(ComponentItem.of(URLUtil.CMP_INTERESTCENTERPEAS)
            .withLabel(message.getString("FavRequests")).withSpecificId("requests")
            .andComponentUrlSuffix("iCenterList.jsp"));
      }
      if (settings.getBoolean("linksVisible", true)) {
        componentItems.add(
            ComponentItem.of(URLUtil.CMP_MYLINKSPEAS).withLabel(message.getString("FavLinks"))
                .withSpecificId("links"));
      }
      if (settings.getBoolean("fileSharingVisible", true) &&
          getSharingTicketService().countTicketsByUser(userId) > 0) {
        componentItems.add(
            ComponentItem.of(URLUtil.CMP_FILESHARING).withLabel(message.getString("FileSharing"))
                .withSpecificId("sharingTicket"));
      }
      // mes connexions
      if (settings.getBoolean("webconnectionsVisible", true)) {
        WebConnectionsInterface webConnections = WebConnectionsInterface.get();
        if (!webConnections.listWebConnectionsOfUser(userId).isEmpty()) {
          componentItems.add(ComponentItem.of(URLUtil.CMP_WEBCONNECTIONS)
              .withLabel(message.getString("WebConnections")));
        }
      }

      if (settings.getBoolean("scheduleEventVisible", false)) {
        componentItems.add(ComponentItem.of(URLUtil.CMP_SCHEDULE_EVENT)
            .withLabel(message.getString("ScheduleEvent")).withSpecificId("scheduleevent"));
      }

      if (settings.getBoolean("customVisible", true)) {
        componentItems.add(
            ComponentItem.of(URLUtil.CMP_MYPROFILE).withLabel(message.getString("Personalization"))
                .withSpecificId("personalize"));
      }
      if (settings.getBoolean("mailVisible", true)) {
        componentItems.add(ComponentItem.of("notifAdmins").withLabel(message.getString("Feedback"))
            .andFullComponentUrl("javascript:notifyAdministrators()"));
      }
      if (settings.getBoolean("clipboardVisible", true)) {
        componentItems.add(ComponentItem.of("clipboard").withLabel(message.getString("Clipboard"))
            .andFullComponentUrl("javascript:openClipboard()"));
      }

      List<SilverpeasComponentInstance> instances = new ArrayList<>();
      instances.addAll(personalSpaceManager.getVisiblePersonalComponentInstances(user));

      if (settings.getBoolean("PersonalSpaceAddingsEnabled", true)) {
        SpaceInst personalSpace = personalSpaceManager.getPersonalSpace(userId);
        if (personalSpace != null) {
          instances.addAll(personalSpace.getAllComponentsInst());
        }
        int nbComponentAvailables = personalSpaceManager.getVisibleComponents().size();
        if (nbComponentAvailables > 0 && (personalSpace == null ||
            personalSpace.getAllComponentsInst().size() < nbComponentAvailables)) {
          componentItems.add(ComponentItem.of("addComponent")
              .withLabel(helper.getString("lookSilverpeasV5.personalSpace.add"))
              .andFullComponentUrl("javascript:listComponents()"));
        }
      }

      for (SilverpeasComponentInstance instance : instances) {
        String label;
        try {
          label = helper.getString("lookSilverpeasV5.personalSpace." + instance.getName());
        } catch (MissingResourceException e) {
          SilverLogger.getLogger(this).silent(e);
          label = instance.getLabel(helper.getLanguage());
        }
        ComponentItem componentItem = ComponentItem.of(instance).withLabel(label);
        if (instance.isPersonal()) {
          componentItems.add(0, componentItem);
        } else {
          componentItems.add((componentItems.size() - 1), componentItem);
        }
      }
    }
    writer.write("<spacePerso id=\"spacePerso\" type=\"space\" level=\"0\">");
    for (ComponentItem componentItem : componentItems) {
      componentItem.write(writer);
    }
    writer.write("</spacePerso>");
  }

  protected boolean isLoadingContentNeeded(UserMenuDisplay userMenuDisplayMode, String userId,
      SpaceInstLight space, List<UserFavoriteSpaceVO> listUFS) {
    switch (userMenuDisplayMode) {
      case DISABLE:
      case ALL:
        return true;
      case BOOKMARKS:
        return isUserFavoriteSpace(listUFS, space) ||
            containsFavoriteSubSpace(space, listUFS, userId);
      default:
        return false;
    }
  }

  /**
   * a Space is visible if this space isn't removed and at least one of its items is visible for the
   * current user
   * @param userId the unique identifier of the user
   * @param space the space
   * @param helper a Look helper to access some peculiar settings regarding the accessibility.
   * @return true or false
   */
  protected boolean isSpaceVisible(String userId, SpaceInstLight space, LookHelper helper) {
    if (space.isRemoved()) {
      return false;
    }
    if (helper.getSettings("displaySpaceContainingOnlyHiddenComponents", true)) {
      return true;
    }
    String[] compoIds = organisationController.getAvailCompoIds(space.getId(), userId);
    for (String id : compoIds) {
      ComponentInst compInst = organisationController.getComponentInst(id);
      if (!compInst.isHidden()) {
        return true;
      }
    }
    return false;
  }

  protected boolean isSpaceBeforeComponentNeeded(SpaceInstLight space) {
    // Display computing : First look at global configuration
    if (JobStartPagePeasSettings.SPACEDISPLAYPOSITION_CONFIG
        .equalsIgnoreCase(JobStartPagePeasSettings.SPACEDISPLAYPOSITION_BEFORE)) {
      return true;
    } else if (JobStartPagePeasSettings.SPACEDISPLAYPOSITION_CONFIG
        .equalsIgnoreCase(JobStartPagePeasSettings.SPACEDISPLAYPOSITION_AFTER)) {
      return false;
    } else if (JobStartPagePeasSettings.SPACEDISPLAYPOSITION_CONFIG
        .equalsIgnoreCase(JobStartPagePeasSettings.SPACEDISPLAYPOSITION_TODEFINE)) {
      return space.isDisplaySpaceFirst();
    }
    return true;
  }

  private static class ComponentItem {
    private final String name;
    private String id;
    private String label = "N/A";
    private String description = "";
    private String kind = "";
    private String component = "component";
    private int level = 1;
    private boolean open = false;
    private String componentUrl;

    private ComponentItem(final String name) {
      this.name = name;
      this.id = name;
    }

    static ComponentItem of(final String name) {
      return new ComponentItem(name).andFullComponentUrl(URLUtil.getURL(name, null, null) + "Main");
    }

    static ComponentItem of(final SilverpeasComponentInstance componentInstance) {
      String kind = "personalComponent";
      if (componentInstance.isPersonal()) {
        kind += "NotRemovable";
      }
      return new ComponentItem(componentInstance.getId()).ofKind(kind).andFullComponentUrl(
          URLUtil.getURL(componentInstance.getName(), null, componentInstance.getId()) + "Main");
    }

    void write(Writer writer) throws IOException {
      writer.write(
          "<item id=\"" + id + NAME + Encode.forXml(label) + "\" description=\"" +
              Encode.forXml(description) + "\" type=\"" + component + "\" kind=\"" + kind +
              "\" level=\"" + level + "\" open=\"" + open + "\" " + "url=\"" + componentUrl + "\"/>");
    }

    ComponentItem withSpecificId(final String specificId) {
      this.id = specificId;
      return this;
    }

    ComponentItem withLabel(final String label) {
      this.label = label;
      return this;
    }

    ComponentItem withDescription(final String description) {
      this.description = description;
      return this;
    }

    ComponentItem ofKind(final String kind) {
      this.kind = kind;
      return this;
    }

    ComponentItem atLevel(final int level) {
      this.level = level;
      return this;
    }

    ComponentItem andOpen(final boolean open) {
      this.open = open;
      return this;
    }

    ComponentItem andComponentUrlSuffix(final String componentUrlSuffix) {
      this.componentUrl = URLUtil.getURL(name, null, null) + componentUrlSuffix;
      return this;
    }

    ComponentItem andFullComponentUrl(final String fullComponentUrl) {
      this.componentUrl = fullComponentUrl;
      return this;
    }
  }
}