/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.look;

import com.silverpeas.personalization.UserMenuDisplay;
import com.silverpeas.personalization.service.PersonalizationService;
import com.silverpeas.session.SessionManagement;
import com.silverpeas.session.SessionManagementFactory;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.SpaceProfile;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.core.admin.OrganisationControllerFactory;

import javax.ejb.EJBException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

public class LookSilverpeasV5Helper implements LookHelper {

  private OrganisationController orga = null;
  private ResourceLocator resources = null;
  private ResourceLocator messages = null;
  private ResourceLocator defaultMessages = null;
  private MainSessionController mainSC = null;
  private boolean displayPDCInNav = false;
  private boolean shouldDisplayPDCFrame = false;
  private boolean shouldDisplayContextualPDC = true;
  private boolean shouldDisplaySpaceIcons = true;
  private boolean shouldDisplayConnectedUsers = true;
  private boolean displayPDCInHomePage = true;
  private List<TopItem> topItems = null;
  private List<String> topSpaceIds = null; // sublist of topItems
  private String mainFrame = "MainFrameSilverpeasV5.jsp";
  private String spaceId = null;
  private String subSpaceId = null;
  private String componentId = null;
  private SimpleDateFormat formatter = null;
  private PublicationHelper kmeliaTransversal = null;
  private PublicationBm publicationBm = null;
  // Attribute used to manage user favorite space look
  private UserMenuDisplay displayUserMenu = UserMenuDisplay.DISABLE;
  private boolean enableUFSContainsState = false;
  private HttpSession session = null;
  private String currentLookName = null;

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getSpaceId()
   */
  @Override
  public String getSpaceId() {
    return spaceId;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#setSpaceId(java.lang.String)
   */
  @Override
  public void setSpaceId(String spaceId) {
    this.spaceId = spaceId;
    if (StringUtil.isDefined(spaceId)) {
      if (!spaceId.startsWith(Admin.SPACE_KEY_PREFIX)) {
        this.spaceId = Admin.SPACE_KEY_PREFIX + spaceId;
      }
      reloadProperties(spaceId);
    }
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getSubSpaceId()
   */
  @Override
  public String getSubSpaceId() {
    return subSpaceId;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#setSubSpaceId(java.lang.String)
   */
  @Override
  public void setSubSpaceId(String subSpaceId) {
    this.subSpaceId = subSpaceId;
    if (StringUtil.isDefined(subSpaceId)) {
      if (!subSpaceId.startsWith(Admin.SPACE_KEY_PREFIX)) {
        this.subSpaceId = Admin.SPACE_KEY_PREFIX + subSpaceId;
      }
      reloadProperties(subSpaceId);
    }
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getComponentId()
   */
  @Override
  public String getComponentId() {
    return componentId;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#setComponentId(java.lang.String)
   */
  @Override
  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#setSpaceIdAndSubSpaceId(java.lang.String)
   */
  @Override
  public void setSpaceIdAndSubSpaceId(String spaceId) {
    if (StringUtil.isDefined(spaceId)) {
      List<SpaceInstLight> spacePath = orga.getPathToSpace(spaceId);
      if (!spacePath.isEmpty()) {
        SpaceInstLight space = spacePath.get(0);
        SpaceInstLight subSpace = spacePath.get(spacePath.size() - 1);
        setSpaceId(space.getFullId());
        setSubSpaceId(subSpace.getFullId());
      }
      setComponentId(null);
    }
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#setComponentIdAndSpaceIds(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  @Override
  public void setComponentIdAndSpaceIds(String spaceId, String subSpaceId, String componentId) {
    setComponentId(componentId);

    if (!StringUtil.isDefined(spaceId)) {
      List<SpaceInstLight> spacePath = orga.getPathToComponent(componentId);
      if (!spacePath.isEmpty()) {
        SpaceInstLight space = spacePath.get(0);
        SpaceInstLight subSpace = spacePath.get(spacePath.size() - 1);
        setSpaceId(space.getFullId());
        setSubSpaceId(subSpace.getFullId());
      }
    } else {
      setSpaceId(spaceId);
      setSubSpaceId(subSpaceId);
    }
  }

  /*
   * Use LookSilverpeasV5Helper(HttpSession session)
   * As HttpSession can be reused in a same Silverpeas session (anonymous case), so MainSessionController can not be stored.
   * It must be retrieved from HttpSession only.
   */
  @Deprecated
  public LookSilverpeasV5Helper(MainSessionController mainSessionController,
      ResourceLocator resources) {
    init(mainSessionController, resources);
  }
  
  public LookSilverpeasV5Helper(HttpSession session) {
    this.session = session;
    GraphicElementFactory gef = getGraphicElementFactory();
    init(gef.getFavoriteLookSettings());
  }

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.look.LookHelper#init(com.stratelia.silverpeas.peasCore.MainSessionController,
   * com.stratelia.webactiv.util.ResourceLocator, com.stratelia.webactiv.util.ResourceLocator)
   */
  @Override
  public final void init(MainSessionController mainSessionController, ResourceLocator resources) {
    this.mainSC = mainSessionController;
    init(resources);
  }
  
  private final void init(ResourceLocator resources) {
    this.orga = OrganisationControllerFactory.getOrganisationController();
    this.resources = resources;
    this.defaultMessages = new ResourceLocator(
        "org.silverpeas.lookSilverpeasV5.multilang.lookBundle",
        getMainSessionController().getFavoriteLanguage());
    if (StringUtil.isDefined(resources.getString("MessageBundle"))) {
      this.messages = new ResourceLocator(resources.getString("MessageBundle"),
          getMainSessionController().getFavoriteLanguage());
    }
    initProperties();
    getTopItems();
  }

  private void initProperties() {
    displayPDCInNav = resources.getBoolean("displayPDCInNav", false);
    shouldDisplayPDCFrame = resources.getBoolean("displayPDCFrame", false);
    shouldDisplayContextualPDC = resources.getBoolean("displayContextualPDC", true);
    shouldDisplaySpaceIcons = resources.getBoolean("displaySpaceIcons", true);
    shouldDisplayConnectedUsers = resources.getBoolean("displayConnectedUsers", true);
    displayPDCInHomePage = resources.getBoolean("displayPDCInHomePage", true);
    if (isAnonymousUser()) {
      displayUserMenu = UserMenuDisplay.DISABLE;
    } else {
      displayUserMenu = UserMenuDisplay.valueOf(resources.getString("displayUserFavoriteSpace",
          PersonalizationService.DEFAULT_MENU_DISPLAY_MODE.name()).toUpperCase());
      if (isMenuPersonalisationEnabled() && getMainSessionController().getPersonalization().getDisplay().isNotDefault()) {
        this.displayUserMenu = getMainSessionController().getPersonalization().getDisplay();
      }
      enableUFSContainsState = resources.getBoolean("enableUFSContainsState", false);
    }
  }

  private void reloadProperties(String spaceId) {
    String spaceLook = SilverpeasLook.getSilverpeasLook().getSpaceLook(spaceId);
    if (!StringUtil.isDefined(spaceLook)) {
      // no look defined for this space (or its parent),
      // use user's favorite look or look by default
      spaceLook = getMainSessionController().getFavoriteLook();
    }
    if (spaceLook != null && !spaceLook.equals(currentLookName)) {
      getGraphicElementFactory().setLook(spaceLook);
      init(getGraphicElementFactory().getFavoriteLookSettings());
      currentLookName = spaceLook;
    }
  }

  @Override
  public boolean isMenuPersonalisationEnabled() {
    return UserMenuDisplay.DISABLE != UserMenuDisplay.valueOf(resources.getString(
        "displayUserFavoriteSpace", PersonalizationService.DEFAULT_MENU_DISPLAY_MODE.name()).
        toUpperCase());
  }

  protected MainSessionController getMainSessionController() {
    if (session != null) {
      return (MainSessionController) session
          .getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    }
    return mainSC;
  }

  protected OrganisationController getOrganisationController() {
    return orga;
  }

  protected GraphicElementFactory getGraphicElementFactory() {
    if (session != null) {
      GraphicElementFactory gef = (GraphicElementFactory) session
          .getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
      return gef;
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getUserFullName(java.lang.String)
   */
  @Override
  public String getUserFullName(String userId) {
    return orga.getUserDetail(userId).getDisplayedName();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getUserFullName()
   */
  @Override
  public String getUserFullName() {
    return getUserDetail().getDisplayedName();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getUserId()
   */
  @Override
  public String getUserId() {
    return getMainSessionController().getUserId();
  }

  public UserDetail getUserDetail() {
    return UserDetail.getById(getUserId());
  }

  public UserFull getUserFull() {
    return orga.getUserFull(getUserId());
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getLanguage()
   */
  @Override
  public String getLanguage() {
    return getMainSessionController().getFavoriteLanguage();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#isAnonymousUser()
   */
  @Override
  public boolean isAnonymousUser() {
    return UserDetail.isAnonymousUser(getUserId());
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#displayPDCInNavigationFrame()
   */
  @Override
  public boolean displayPDCInNavigationFrame() {
    return displayPDCInNav;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#displayPDCFrame()
   */
  @Override
  public boolean displayPDCFrame() {
    return shouldDisplayPDCFrame;
  }

  @Override
  public boolean displayContextualPDC() {
    return shouldDisplayContextualPDC;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#shouldDisplaySpaceIcons()
   */
  @Override
  public boolean displaySpaceIcons() {
    return shouldDisplaySpaceIcons;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getSpaceId(java.lang.String)
   */
  @Override
  public String getSpaceId(String componentId) {
    ComponentInstLight component = orga.getComponentInstLight(componentId);
    if (component != null) {
      return component.getDomainFatherId();
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getWallPaper(java.lang.String)
   */
  @Override
  public String getWallPaper(String spaceId) {
    String hasWallpaper = "0";
    if (StringUtil.isDefined(spaceId) && SilverpeasLook.getSilverpeasLook().hasSpaceWallpaper(
        spaceId)) {
      hasWallpaper = "1";
    }
    return hasWallpaper;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getNBConnectedUsers()
   */
  @Override
  public int getNBConnectedUsers() {
    int nbConnectedUsers = 0;
    if (shouldDisplayConnectedUsers) {
      // Remove the current user
      SessionManagementFactory factory = SessionManagementFactory.getFactory();
      SessionManagement sessionManagement = factory.getSessionManagement();
      nbConnectedUsers = sessionManagement.getNbConnectedUsersList(getMainSessionController().
          getCurrentUserDetail()) - 1;
    }
    return nbConnectedUsers;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#isAnonymousAccess()
   */
  @Override
  public boolean isAnonymousAccess() {
    return isAnonymousUser();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getSettings(java.lang.String)
   */
  @Override
  public boolean getSettings(String key) {
    return resources.getBoolean(key, false);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getSettings(java.lang.String, boolean)
   */
  @Override
  public boolean getSettings(String key, boolean defaultValue) {
    return resources.getBoolean(key, defaultValue);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getSettings(java.lang.String, java.lang.String)
   */
  @Override
  public String getSettings(String key, String defaultValue) {
    return resources.getString(key, defaultValue);
  }
  
  @Override
  public int getSettings(String key, int defaultValue) {
    return resources.getInteger(key, defaultValue);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getString(java.lang.String)
   */
  @Override
  public String getString(String key) {
    if (key.startsWith("lookSilverpeasV5")) {
      return defaultMessages.getString(key, "");
    }
    return messages.getString(key, "");
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#isBackOfficeVisible()
   */
  @Override
  public boolean isBackOfficeVisible() {
    return getMainSessionController().isBackOfficeVisible();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getTopItems()
   */
  @Override
  public List<TopItem> getTopItems() {
    topItems = new ArrayList<TopItem>();
    topSpaceIds = new ArrayList<String>();
    StringTokenizer tokenizer = new StringTokenizer(resources.getString("componentsTop", ""), ",");
    while (tokenizer.hasMoreTokens()) {
      String itemId = tokenizer.nextToken();

      if (itemId.startsWith(Admin.SPACE_KEY_PREFIX)) {
        if (orga.isSpaceAvailable(itemId, getUserId())) {
          SpaceInstLight space = orga.getSpaceInstLightById(itemId);
          SpaceInstLight rootSpace = orga.getRootSpace(itemId);
          TopItem item = new TopItem();
          item.setLabel(space.getName(getLanguage()));
          item.setSpaceId(rootSpace.getFullId());
          item.setSubSpaceId(itemId);
          topItems.add(item);
          topSpaceIds.add(item.getSpaceId());
        }
      } else {
        if (orga.isComponentAvailable(itemId, getUserId())) {
          ComponentInstLight component = orga.getComponentInstLight(itemId);
          String currentSpaceId = component.getDomainFatherId();
          SpaceInstLight rootSpace = orga.getRootSpace(currentSpaceId);
          TopItem item = new TopItem();
          item.setLabel(component.getLabel(getLanguage()));
          item.setComponentId(itemId);
          item.setSpaceId(rootSpace.getFullId());
          item.setSubSpaceId(currentSpaceId);

          topItems.add(item);
        }
      }
    }
    return topItems;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getTopSpaceIds()
   */
  @Override
  public List<String> getTopSpaceIds() {
    return topSpaceIds;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getMainFrame()
   */
  @Override
  public String getMainFrame() {
    return mainFrame;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#setMainFrame(java.lang.String)
   */
  @Override
  public void setMainFrame(String newMainFrame) {
    if (StringUtil.isDefined(newMainFrame)) {
      this.mainFrame = newMainFrame;
    }
  }

  /*
   * (non-Javadoc) @see com.silverpeas.look.LookHelper#getSpaceWallPaper()
   */
  @Override
  public String getSpaceWallPaper() {
    String theSpaceId = getCurrentDeepestSpaceId();
    if (StringUtil.isDefined(theSpaceId)) {
      return SilverpeasLook.getSilverpeasLook().getWallpaperOfSpace(theSpaceId);
    }
    return null;
  }

  @Override
  public String getSpaceWithCSSToApply() {
    String spaceId = getCurrentDeepestSpaceId();
    if (StringUtil.isDefined(spaceId)) {
      return SilverpeasLook.getSilverpeasLook().getSpaceWithCSS(spaceId);
    }
    return null;
  }

  private String getCurrentDeepestSpaceId() {
    String theSpaceId = getSpaceId();
    if (StringUtil.isDefined(theSpaceId)) {
      if (StringUtil.isDefined(getSubSpaceId())) {
        theSpaceId = getSubSpaceId();
      }
    }
    return theSpaceId;
  }

  public String getComponentURL(String key, String function) {
    String currentFunction = function;
    String currentComponentId = resources.getString(key);
    if (!StringUtil.isDefined(function)) {
      currentFunction = "Main";
    }
    return URLManager.getApplicationURL() + URLManager.getURL("useless", currentComponentId)
        + currentFunction;
  }

  @Override
  public String getComponentURL(String key) {
    return getComponentURL(key, "Main");
  }

  @Override
  public String getDate() {
    if (formatter == null) {
      formatter = new SimpleDateFormat(resources.getString("DateFormat", "dd/MM/yyyy"),
          new Locale(getMainSessionController().getFavoriteLanguage()));
    }
    return formatter.format(new Date());
  }

  @Override
  public String getDefaultSpaceId() {
    String defaultSpaceId = resources.getString("DefaultSpaceId");
    if (!StringUtil.isDefined(defaultSpaceId)) {
      defaultSpaceId = getMainSessionController().getFavoriteSpace();
    }
    return defaultSpaceId;
  }

  private PublicationHelper getPublicationHelper() throws ClassNotFoundException,
      InstantiationException, IllegalAccessException {
    if (kmeliaTransversal == null) {
      String helperClassName = resources.getString("publicationHelper",
          "com.stratelia.webactiv.kmelia.KmeliaTransversal");
      Class<?> helperClass = Class.forName(helperClassName);
      kmeliaTransversal = (PublicationHelper) helperClass.newInstance();
      kmeliaTransversal.setMainSessionController(getMainSessionController());
    }
    return kmeliaTransversal;
  }

  @Override
  public List<PublicationDetail> getLatestPublications(String spaceId, int nbPublis) {
    try {
      return getPublicationHelper().getPublications(spaceId, nbPublis);
    } catch (ClassNotFoundException ex) {
      return new ArrayList<PublicationDetail>();
    } catch (InstantiationException ex) {
      return new ArrayList<PublicationDetail>();
    } catch (IllegalAccessException ex) {
      return new ArrayList<PublicationDetail>();
    }
  }

  @Override
  public List<PublicationDetail> getValidPublications(NodePK nodePK) {
    List<PublicationDetail> publis = (List<PublicationDetail>) getPublicationBm().
        getDetailsByFatherPK(nodePK, null, true);
    List<PublicationDetail> filteredPublis = new ArrayList<PublicationDetail>();
    PublicationDetail publi;
    for (int i = 0; publis != null && i < publis.size(); i++) {
      publi = publis.get(i);
      if (PublicationDetail.VALID.equalsIgnoreCase(publi.getStatus())) {
        filteredPublis.add(publi);
      }
    }
    return filteredPublis;
  }

  public PublicationBm getPublicationBm() {
    if (publicationBm == null) {
      try {
        publicationBm = EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
            PublicationBm.class);
      } catch (Exception e) {
        throw new EJBException(e);
      }
    }
    return publicationBm;
  }

  public String getSpaceHomePage(String spaceId, HttpServletRequest request)
      throws UnsupportedEncodingException {
    SpaceInst spaceStruct = getOrganisationController().getSpaceInstById(spaceId);
    // Page d'accueil de l'espace = Composant
    if (spaceStruct != null
        && (spaceStruct.getFirstPageType() == SpaceInst.FP_TYPE_COMPONENT_INST)
        && spaceStruct.getFirstPageExtraParam() != null
        && spaceStruct.getFirstPageExtraParam().length() > 0) {
      if (getOrganisationController().isComponentAvailable(
          spaceStruct.getFirstPageExtraParam(), getUserId())) {
        return URLManager.getSimpleURL(URLManager.URL_COMPONENT,
            spaceStruct.getFirstPageExtraParam());
      }
    }

    // Page d'accueil de l'espace = URL
    if (spaceStruct != null
        && (spaceStruct.getFirstPageType() == SpaceInst.FP_TYPE_HTML_PAGE)
        && (spaceStruct.getFirstPageExtraParam() != null)
        && (spaceStruct.getFirstPageExtraParam().length() > 0)) {
      String destination = spaceStruct.getFirstPageExtraParam();
      destination = getParsedDestination(destination, "%ST_USER_LOGIN%",
          getMainSessionController().getCurrentUserDetail().getLogin());
      destination = getParsedDestination(destination, "%ST_USER_FULLNAME%",
          URLEncoder.encode(getMainSessionController().getCurrentUserDetail().getDisplayedName(),
              "UTF-8"));
      destination = getParsedDestination(destination, "%ST_USER_ID%",
          URLEncoder.encode(getMainSessionController().getUserId(), "UTF-8"));
      destination = getParsedDestination(destination, "%ST_SESSION_ID%",
          URLEncoder.encode(request.getSession().getId(), "UTF-8"));

      // !!!! Add the password : this is an uggly patch that use a session
      // variable set in the "AuthenticationServlet" servlet
      HttpSession session = request.getSession();
      return getParsedDestination(destination, "%ST_USER_PASSWORD%",
          (String) session.getAttribute("Silverpeas_pwdForHyperlink"));
    }
    return null;
  }

  private String getParsedDestination(String sDestination, String sKeyword, String sValue) {
    String parsedDestination = sDestination;
    int nLoginIndex = sDestination.indexOf(sKeyword);
    if (nLoginIndex != -1) {
      // Replace the keyword with the actual value
      String sParsed = sDestination.substring(0, nLoginIndex);
      sParsed = sParsed + sValue;
      if (sDestination.length() > nLoginIndex + sKeyword.length()) {
        sParsed += sDestination.substring(nLoginIndex + sKeyword.length(), sDestination.length());
      }
      parsedDestination = sParsed;
    }
    return parsedDestination;
  }

  /**
   * @return user favorite space menu display mode
   */
  @Override
  public UserMenuDisplay getDisplayUserMenu() {
    return displayUserMenu;
  }

  /**
   * @param displayUserMenu
   */
  @Override
  public void setDisplayUserMenu(UserMenuDisplay displayUserMenu) {
    this.displayUserMenu = displayUserMenu;
  }

  /**
   * @return true if displaying three states, false if displaying two states
   */
  @Override
  public boolean isEnableUFSContainsState() {
    return enableUFSContainsState;
  }

  /**
   * Returns a list of shortcuts to display on a page (home page, heading page...)
   *
   * @param id identify the area of shorcuts
   * @param nb the number of shortcuts to retrieve
   * @return a List of Shorcut
   */
  public List<Shortcut> getShortcuts(String id, int nb) {
    List<Shortcut> shortcuts = new ArrayList<Shortcut>();
    for (int i = 1; i <= nb; i++) {
      String prefix = "Shortcut." + id + "." + i;
      String url = getSettings(prefix + ".Url", "toBeDefined");
      String target = getSettings(prefix + ".Target", "toBeDefined");
      String altText = getSettings(prefix + ".AltText", "toBeDefined");
      String iconUrl = getSettings(prefix + ".IconUrl", "toBeDefined");
      Shortcut shortcut = new Shortcut(iconUrl, target, url, altText);
      shortcuts.add(shortcut);
    }
    return shortcuts;
  }

  /**
   * @return the displayPDCInHomePage
   */
  public boolean isDisplayPDCInHomePage() {
    return displayPDCInHomePage;
  }
  
  public DefaultSpaceHomePage getSpaceHomePage(String spaceId) {
    setSpaceIdAndSubSpaceId(spaceId);
    String currentSpaceId = getSubSpaceId();
    DefaultSpaceHomePage homepage = new DefaultSpaceHomePage();
    
    // get main information of space
    SpaceInstLight space = orga.getSpaceInstLightById(currentSpaceId);
    homepage.setSpace(space);

    // get latest publications
    if (resources.getBoolean("space.homepage.latestpublications", true)) {
      try {
        homepage.setPublications(getPublicationHelper().getUpdatedPublications(currentSpaceId, 0,
            resources.getInteger("space.homepage.latestpublications.nb", 5)));
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    if (resources.getBoolean("space.homepage.news", true)) {
      // get visible news from 'quickinfo' apps
      homepage.setNews(getNews(currentSpaceId));
    }

    if (resources.getBoolean("space.homepage.subspaces", true)) {
      // get allowed subspaces
      String[] subspaceIds =
          getOrganisationController().getAllowedSubSpaceIds(getUserId(), currentSpaceId);
      List<SpaceInstLight> subspaces = new ArrayList<SpaceInstLight>();
      for (String subspaceId : subspaceIds) {
        subspaces.add(getOrganisationController().getSpaceInstLightById(subspaceId));
      }
      homepage.setSubSpaces(subspaces);
    }

    boolean displayApps = resources.getBoolean("space.homepage.apps", true);
    boolean displayEvents = resources.getBoolean("space.homepage.events", true);
    if (displayApps || displayEvents) {
      // get allowed apps
      String[] appIds = getOrganisationController().getAvailCompoIdsAtRoot(currentSpaceId, getUserId());
      List<ComponentInstLight> apps = new ArrayList<ComponentInstLight>();
      for (String appId : appIds) {
        ComponentInstLight app = getOrganisationController().getComponentInstLight(appId);
        if (displayApps && !app.isHidden()) {
          apps.add(app);
        }
        if (displayEvents && app.getName().equals("almanach") &&
            !StringUtil.isDefined(homepage.getNextEventsURL())) {
          homepage.setNextEventsURL(URLManager.getApplicationURL()+URLManager.getURL(null, appId)+"portlet");
        }
      }
      homepage.setApps(apps);
    }
    
    if (resources.getBoolean("space.homepage.admins", true)) {
      // get space admins (not global admins)
      homepage.setAdmins(getSpaceAdmins(currentSpaceId));
    }

    return homepage;
  }

  public List<UserDetail> getSpaceAdmins(String spaceId) {
    List<UserDetail> admins = new ArrayList<UserDetail>();
    try {
      SpaceProfile spaceProfile =
          getOrganisationController().getSpaceProfile(spaceId, SilverpeasRole.Manager);
      Set<String> userIds = spaceProfile.getAllUserIdsIncludingAllGroups();
      for (String userId : userIds) {
        admins.add(UserDetail.getById(userId));
      }
    } catch (AdminException e) {
      e.printStackTrace();
    }
    return admins;
  }

  public List<PublicationDetail> getNews(String spaceId) {
    List<String> appIds = new ArrayList<String>();
    String[] cIds = getOrganisationController().getAvailCompoIds(spaceId, getUserId());
    for (String id : cIds) {
      if (StringUtil.startsWithIgnoreCase(id, "quickinfo")) {
        appIds.add(id);
      }
    }

    List<PublicationDetail> news = new ArrayList<PublicationDetail>();
    for (String appId : appIds) {
      Collection<PublicationDetail> someNews =
          getPublicationBm().getOrphanPublications(new PublicationPK("", appId));
      for (PublicationDetail aNews : someNews) {
        if (isVisibleNews(aNews)) {
          news.add(aNews);
        }
      }
    }

    Collections.sort(news, PublicationUpdateDateComparator.comparator);
    
    return news;
  }

  private boolean isVisibleNews(PublicationDetail news) {
    return news.isValid() && news.getVisibilityPeriod().contains(new Date());
  }

  public TickerSettings getTickerSettings() {
    TickerSettings tickerSettings = new TickerSettings(resources);
    String labelParam = getSettings("ticker.label", "");
    if (labelParam.equalsIgnoreCase("default")) {
      tickerSettings.setLabel(getString("lookSilverpeasV5.ticker.label"));
    }
    return tickerSettings;
  }
  
  public String getURLOfLastVisitedCollaborativeSpace() {
    String spaceId = getSpaceId();
    if (StringUtil.isDefined(getSubSpaceId())) {
      spaceId = getSubSpaceId();
    }
    if (StringUtil.isDefined(spaceId)) {
      return URLManager.getSimpleURL(URLManager.URL_SPACE, spaceId)+"?Fallback=true";
    }
    return null;
  }
}