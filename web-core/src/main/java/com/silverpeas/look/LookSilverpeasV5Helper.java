/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.look;

import java.io.File;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.ejb.EJBException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.SessionManager;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.SilverpeasSettings;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

public class LookSilverpeasV5Helper implements LookHelper {

  private OrganizationController orga = null;
  private ResourceLocator resources = null;
  private ResourceLocator messages = null;
  private ResourceLocator defaultMessages = null;
  private MainSessionController mainSC = null;
  private String userId = null;
  private String guestId = null;
  private boolean displayPDCInNav = false;
  private boolean displayPDCFrame = false;
  private boolean displayContextualPDC = true;
  private boolean displaySpaceIcons = true;
  private boolean displayConnectedUsers = true;
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
  private String displayUserFavoriteSpace = null;
  private boolean enableUFSContainsState = false;

  private static final String DEFAULT_USERMENU_DISPLAY_MODE = "DISABLE";

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getSpaceId()
   */
  public String getSpaceId() {
    return spaceId;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#setSpaceId(java.lang.String)
   */
  public void setSpaceId(String spaceId) {
    if (!spaceId.startsWith("WA")) {
      spaceId = "WA" + spaceId;
    }
    this.spaceId = spaceId;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getSubSpaceId()
   */
  public String getSubSpaceId() {
    return subSpaceId;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#setSubSpaceId(java.lang.String)
   */
  public void setSubSpaceId(String subSpaceId) {
    if (!subSpaceId.startsWith("WA")) {
      subSpaceId = "WA" + subSpaceId;
    }
    this.subSpaceId = subSpaceId;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getComponentId()
   */
  public String getComponentId() {
    return componentId;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#setComponentId(java.lang.String)
   */
  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#setSpaceIdAndSubSpaceId(java.lang.String)
   */
  public void setSpaceIdAndSubSpaceId(String spaceId) {
    if (StringUtil.isDefined(spaceId)) {
      List<SpaceInst> spacePath = orga.getSpacePath(spaceId);
      if (!spacePath.isEmpty()) {
        SpaceInst space = spacePath.get(0);
        SpaceInst subSpace = spacePath.get(spacePath.size() - 1);
        setSpaceId(space.getId());
        setSubSpaceId(subSpace.getId());
      }
      setComponentId(null);
    }
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#setComponentIdAndSpaceIds(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  public void setComponentIdAndSpaceIds(String spaceId, String subSpaceId, String componentId) {
    setComponentId(componentId);

    if (!StringUtil.isDefined(spaceId)) {
      List<SpaceInst> spacePath = orga.getSpacePathToComponent(componentId);
      if (!spacePath.isEmpty()) {
        SpaceInst space = spacePath.get(0);
        SpaceInst subSpace = spacePath.get(spacePath.size() - 1);
        setSpaceId(space.getId());
        setSubSpaceId(subSpace.getId());
      }
    } else {
      setSpaceId(spaceId);
      setSubSpaceId(subSpaceId);
    }
  }

  public LookSilverpeasV5Helper(MainSessionController mainSessionController,
      ResourceLocator resources) {
    init(mainSessionController, resources);
  }

  /*
   * (non-Javadoc)
   * @see
   * com.silverpeas.look.LookHelper#init(com.stratelia.silverpeas.peasCore.MainSessionController,
   * com.stratelia.webactiv.util.ResourceLocator, com.stratelia.webactiv.util.ResourceLocator)
   */
  public void init(MainSessionController mainSessionController, ResourceLocator resources) {
    this.mainSC = mainSessionController;
    this.orga = mainSessionController.getOrganizationController();
    this.userId = mainSessionController.getUserId();
    this.resources = resources;
    this.defaultMessages =
        new ResourceLocator("com.silverpeas.lookSilverpeasV5.multilang.lookBundle",
        mainSessionController.getFavoriteLanguage());
    if (StringUtil.isDefined(resources.getString("MessageBundle"))) {
      this.messages =
          new ResourceLocator(resources.getString("MessageBundle"), mainSessionController
          .getFavoriteLanguage());
    }
    initProperties();
    getTopItems();
  }

  private void initProperties() {
    this.guestId = resources.getString("guestId");
    displayPDCInNav = resources.getBoolean("displayPDCInNav", false);
    displayPDCFrame = resources.getBoolean("displayPDCFrame", false);
    displayContextualPDC = resources.getBoolean("displayContextualPDC", true);
    displaySpaceIcons = resources.getBoolean("displaySpaceIcons", true);
    displayConnectedUsers = resources.getBoolean("displayConnectedUsers", true);
    displayUserFavoriteSpace =
        resources.getString("displayUserFavoriteSpace", DEFAULT_USERMENU_DISPLAY_MODE);
    enableUFSContainsState = resources.getBoolean("enableUFSContainsState", false);
  }

  protected MainSessionController getMainSessionController() {
    return mainSC;
  }

  protected OrganizationController getOrganizationController() {
    return orga;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getUserFullName(java.lang.String)
   */
  public String getUserFullName(String userId) {
    return orga.getUserDetail(userId).getDisplayedName();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getUserFullName()
   */
  public String getUserFullName() {
    return orga.getUserDetail(userId).getDisplayedName();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getUserId()
   */
  public String getUserId() {
    return userId;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getAnonymousUserId()
   */
  public String getAnonymousUserId() {
    return guestId;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getLanguage()
   */
  public String getLanguage() {
    return mainSC.getFavoriteLanguage();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#isAnonymousUser()
   */
  public boolean isAnonymousUser() {
    if (StringUtil.isDefined(userId) && StringUtil.isDefined(guestId)) {
      return userId.equals(guestId);
    } else {
      return false;
    }
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
    return displayPDCFrame;
  }

  @Override
  public boolean displayContextualPDC() {
    return displayContextualPDC;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#displaySpaceIcons()
   */
  @Override
  public boolean displaySpaceIcons() {
    return displaySpaceIcons;
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
    if (!StringUtil.isDefined(spaceId)) {
      return "0";
    }

    if (StringUtil.isDefined(getSpaceWallPaper(spaceId))) {
      return "1";
    }
    return "0";
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getNBConnectedUsers()
   */
  @Override
  public int getNBConnectedUsers() {
    int nbConnectedUsers = 0;
    if (displayConnectedUsers) {
      // Remove the current user
      nbConnectedUsers = SessionManager.getInstance().getNbConnectedUsersList() - 1;
    }
    return nbConnectedUsers;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#isAnonymousAccess()
   */
  public boolean isAnonymousAccess() {
    return (StringUtil.isDefined(guestId) && guestId.equals(userId));
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getSettings(java.lang.String)
   */
  public boolean getSettings(String key) {
    return SilverpeasSettings.readBoolean(resources, key, false);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getSettings(java.lang.String, boolean)
   */
  public boolean getSettings(String key, boolean defaultValue) {
    return SilverpeasSettings.readBoolean(resources, key, defaultValue);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getSettings(java.lang.String, java.lang.String)
   */
  public String getSettings(String key, String defaultValue) {
    return SilverpeasSettings.readString(resources, key, defaultValue);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getString(java.lang.String)
   */
  public String getString(String key) {
    if (key.startsWith("lookSilverpeasV5")) {
      return SilverpeasSettings.readString(defaultMessages, key, "");
    } else {
      return SilverpeasSettings.readString(messages, key, "");
    }
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#isBackOfficeVisible()
   */
  public boolean isBackOfficeVisible() {
    return mainSC.isBackOfficeVisible();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getTopItems()
   */
  public List<TopItem> getTopItems() {
    if (topItems == null) {
      topItems = new ArrayList<TopItem>();
      topSpaceIds = new ArrayList<String>();

      StringTokenizer tokenizer = new StringTokenizer(resources.getString("componentsTop"), ",");

      String itemId = null;
      ComponentInstLight component = null;
      SpaceInstLight space = null;
      while (tokenizer.hasMoreTokens()) {
        itemId = tokenizer.nextToken();

        if (itemId.startsWith("WA")) {
          if (orga.isSpaceAvailable(itemId, userId)) {
            space = orga.getSpaceInstLightById(itemId);
            SpaceInstLight rootSpace = orga.getRootSpace(itemId);
            TopItem item = new TopItem();
            item.setLabel(space.getName(getLanguage()));
            item.setSpaceId(rootSpace.getFullId());
            item.setSubSpaceId(itemId);
            topItems.add(item);
            topSpaceIds.add(item.getSpaceId());
          }
        } else {
          if (orga.isComponentAvailable(itemId, userId)) {
            component = orga.getComponentInstLight(itemId);
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
    }
    return topItems;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getTopSpaceIds()
   */
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
  public void setMainFrame(String mainFrame) {
    this.mainFrame = mainFrame;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.look.LookHelper#getSpaceWallPaper()
   */
  @Override
  public String getSpaceWallPaper() {
    if (!StringUtil.isDefined(getSpaceId())) {
      return null;
    }

    if (!StringUtil.isDefined(getSubSpaceId())) {
      return getSpaceWallPaper(getSpaceId());
    } else {
      // get wallpaper of current subspace or first super space
      List<SpaceInst> spaces = getOrganizationController().getSpacePath(getSubSpaceId());
      Collections.reverse(spaces);

      String wallpaper = null;
      for (int i = 0; wallpaper == null && i < spaces.size(); i++) {
        SpaceInst space = spaces.get(i);
        wallpaper = getSpaceWallPaper(space.getId());
      }
      return wallpaper;
    }
  }

  private String getSpaceWallPaper(String id) {
    if (id.startsWith(Admin.SPACE_KEY_PREFIX)) {
      id = id.substring(2);
    }
    String path =
        FileRepositoryManager.getAbsolutePath("Space" + id, new String[] { "look" });

    String filePath = getWallPaper(path, id, "jpg");
    if (!StringUtil.isDefined(filePath)) {
      filePath = getWallPaper(path, id, "gif");
      if (!StringUtil.isDefined(filePath)) {
        filePath = getWallPaper(path, id, "png");
      }
    }
    return filePath;
  }

  private String getWallPaper(String path, String spaceId, String extension) {
    String image = "wallPaper." + extension;
    File file = new File(path + image);
    if (file.isFile()) {
      return FileServerUtils.getOnlineURL("Space" + spaceId, file.getName(), file
          .getName(), FileUtil.getMimeType(image), "look");
    }
    return null;
  }

  public String getComponentURL(String key, String function) {
    String currentComponentId = resources.getString(key);
    if (!StringUtil.isDefined(function)) {
      function = "Main";
    }
    return URLManager.getApplicationURL() + URLManager.getURL("useless", currentComponentId) +
        function;
  }

  public String getComponentURL(String key) {
    return getComponentURL(key, "Main");
  }

  @Override
  public String getDate() {
    if (formatter == null) {
      formatter =
          new SimpleDateFormat(resources.getString("DateFormat", "dd/MM/yyyy"), new Locale(mainSC
          .getFavoriteLanguage()));
    }

    return formatter.format(new Date());
  }

  @Override
  public String getDefaultSpaceId() {
    String defaultSpaceId = resources.getString("DefaultSpaceId");
    if (!StringUtil.isDefined(defaultSpaceId)) {
      defaultSpaceId = mainSC.getFavoriteSpace();
    }

    return defaultSpaceId;
  }

  private PublicationHelper getPublicationHelper() throws ClassNotFoundException,
      InstantiationException, IllegalAccessException {
    if (kmeliaTransversal == null) {
      String helperClassName =
          resources.getString("publicationHelper",
              "com.stratelia.webactiv.kmelia.KmeliaTransversal");
      Class<?> helperClass = Class.forName(helperClassName);
      kmeliaTransversal = (PublicationHelper) helperClass.newInstance();
      kmeliaTransversal.setMainSessionController(mainSC);
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
    List<PublicationDetail> publis = null;
    try {
      publis =
          (List<PublicationDetail>) getPublicationBm().getDetailsByFatherPK(nodePK, null, true);
    } catch (RemoteException e) {
      SilverTrace.error("lookSilverpeasV5", "LookSilverpeasV5Helper.getPublications",
          "root.MSG_GEN_PARAM_VALUE", e);
    }
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
        publicationBm =
            ((PublicationBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
            PublicationBmHome.class)).create();
      } catch (Exception e) {
        throw new EJBException(e);
      }
    }
    return publicationBm;
  }

  public String getSpaceHomePage(String spaceId, HttpServletRequest request) {
    SpaceInst spaceStruct = getOrganizationController().getSpaceInstById(
        spaceId);

    // Page d'accueil de l'espace = Composant
    if (spaceStruct != null
        && (spaceStruct.getFirstPageType() == SpaceInst.FP_TYPE_COMPONENT_INST)
        && spaceStruct.getFirstPageExtraParam() != null
        && spaceStruct.getFirstPageExtraParam().length() > 0) {
      if (getOrganizationController().isComponentAvailable(
          spaceStruct.getFirstPageExtraParam(), getUserId())) {
        return URLManager.getSimpleURL(URLManager.URL_COMPONENT, spaceStruct
            .getFirstPageExtraParam());
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
          URLEncoder.encode(getMainSessionController().getCurrentUserDetail()
          .getDisplayedName()));
      destination = getParsedDestination(destination, "%ST_USER_ID%",
          URLEncoder.encode(getMainSessionController().getUserId()));
      destination = getParsedDestination(destination, "%ST_SESSION_ID%",
          URLEncoder.encode(request.getSession().getId()));

      // !!!! Add the password : this is an uggly patch that use a session
      // variable set in the "AuthenticationServlet" servlet
      HttpSession session = request.getSession();
      return getParsedDestination(destination, "%ST_USER_PASSWORD%",
          (String) session.getAttribute("Silverpeas_pwdForHyperlink"));
    }

    return null;
  }

  private String getParsedDestination(String sDestination, String sKeyword,
      String sValue) {
    int nLoginIndex = sDestination.indexOf(sKeyword);
    if (nLoginIndex != -1) {
      // Replace the keyword with the actual value
      String sParsed = sDestination.substring(0, nLoginIndex);
      sParsed += sValue;
      if (sDestination.length() > nLoginIndex + sKeyword.length())
        sParsed += sDestination.substring(nLoginIndex + sKeyword.length(),
            sDestination.length());
      sDestination = sParsed;
    }
    return sDestination;
  }

  /**
   * @return user favorite space menu display mode
   */
  public String getDisplayUserFavoriteSpace() {
    return displayUserFavoriteSpace;
  }

  /**
   * @return user favorite space menu display mode
   */
  public void setDisplayUserFavoriteSpace(String displayUserFavoriteSpace) {
    this.displayUserFavoriteSpace = displayUserFavoriteSpace;
  }

  /**
   * @return true if displaying three states, false if displaying two states
   */
  public boolean isEnableUFSContainsState() {
    return enableUFSContainsState;
  }

}