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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.jobStartPagePeas.control;

import com.silverpeas.admin.components.WAComponent;
import com.silverpeas.admin.localized.LocalizedComponent;
import com.silverpeas.admin.spaces.SpaceTemplate;
import com.silverpeas.jobStartPagePeas.DisplaySorted;
import com.silverpeas.jobStartPagePeas.JobStartPagePeasException;
import com.silverpeas.jobStartPagePeas.JobStartPagePeasSettings;
import com.silverpeas.jobStartPagePeas.NavBarManager;
import com.silverpeas.ui.DisplayI18NHelper;
import com.silverpeas.util.ArrayUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.clipboard.ClipboardSelection;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.ComponentSelection;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.ProfileInst;
import com.stratelia.webactiv.beans.admin.Recover;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.SpaceProfileInst;
import com.stratelia.webactiv.beans.admin.SpaceSelection;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Class declaration
 * @author
 */
public class JobStartPagePeasSessionController extends AbstractComponentSessionController {

  private final AdminController adminController;
  NavBarManager m_NavBarMgr = new NavBarManager();
  String m_ManagedSpaceId = null;
  boolean m_isManagedSpaceRoot = true;
  Selection selection = null;
  String m_ManagedInstanceId = null;
  ProfileInst m_ManagedProfile = null;
  ProfileInst m_ManagedInheritedProfile = null;
  // Space creation parameters
  String m_ssEspace = "";
  String m_name = "";
  String m_desc = "";
  String currentLanguage = "";
  String m_look = null;
  String m_spaceTemplate = "";
  String[][] currentSpaceTemplateProfilesGroups = new String[0][0];
  String[][] m_TemplateProfilesUsers = new String[0][0];
  // Order space / component b
  boolean m_spaceFirst = true;
  // Space sort buffers
  SpaceInst[] m_BrothersSpaces = new SpaceInst[0];
  ComponentInst[] m_BrothersComponents = ArrayUtil.EMPTY_COMPONENT_INSTANCE_ARRAY;
  public static final int SCOPE_BACKOFFICE = 0;
  public static final int SCOPE_FRONTOFFICE = 1;
  private int scope = SCOPE_BACKOFFICE;
  
  public static final int MAINTENANCE_OFF = 0;
  public static final int MAINTENANCE_PLATFORM = 1;
  public static final int MAINTENANCE_ONEPARENT = 2;
  public static final int MAINTENANCE_THISSPACE = 3;

  private static final Properties templateConfiguration = new Properties();

  public JobStartPagePeasSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.silverpeas.jobStartPagePeas.multilang.jobStartPagePeasBundle",
        "com.silverpeas.jobStartPagePeas.settings.jobStartPagePeasIcons");
    setComponentRootName(URLManager.CMP_JOBSTARTPAGEPEAS);
    selection = getSelection();
    adminController = new AdminController(getUserId());
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR,
        JobStartPagePeasSettings.TEMPLATE_PATH);
    templateConfiguration.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR,
        JobStartPagePeasSettings.CUSTOMERS_TEMPLATE_PATH);
  }

  // Init at first entry
  public void init() {
    m_NavBarMgr.initWithUser(this, getUserDetail());
  }

  public boolean isInheritanceEnable() {
    return JobStartPagePeasSettings.isInheritanceEnable;
  }

  public boolean isJSR168Used() {
    return JobStartPagePeasSettings.useJSR168Portlets;
  }

  public boolean isUserAdmin() {
    return getUserDetail().isAccessAdmin();
  }

  // method du spaceInst
  public SpaceInst getSpaceInstById() {
    if (!StringUtil.isDefined(getManagedSpaceId())) {
      return null;
    }
    SpaceInst space = adminController.getSpaceInstById("WA" + getManagedSpaceId());
    space.setCreator(getUserDetail(space.getCreatorUserId()));
    space.setUpdater(getUserDetail(space.getUpdaterUserId()));
    space.setRemover(getUserDetail(space.getRemoverUserId()));
    return space;
  }

  public void setManagedSpaceId(String sId, boolean isManagedSpaceRoot) {
    String spaceId = getShortSpaceId(sId);
    m_ManagedSpaceId = spaceId;
    m_isManagedSpaceRoot = isManagedSpaceRoot;
    SilverTrace.info("jobStartPagePeas", "JobStartPagePeasSessionController.setManagedSpaceId()",
        "root.MSG_GEN_PARAM_VALUE", "Current Space=" + m_ManagedSpaceId);
  }

  public String getManagedSpaceId() {
    return m_ManagedSpaceId;
  }

  public DisplaySorted getManagedSpace() {
    return m_NavBarMgr.getSpace(getManagedSpaceId());
  }

  public boolean isManagedSpaceRoot() {
    return m_isManagedSpaceRoot;
  }

  public DisplaySorted[] getManagedSpaceComponents() {
    if (isManagedSpaceRoot()) {
      return getSpaceComponents();
    }
    return getSubSpaceComponents();
  }

  // methods set
  public void setSubSpaceId(String subSpaceId) {
    if (m_NavBarMgr.setCurrentSubSpace(subSpaceId)) {
      setManagedSpaceId(subSpaceId, false);
    } else {
      setManagedSpaceId(getSpaceId(), true);
    }
  }

  public void setSpaceId(String spaceUserId) {
    if (m_NavBarMgr.setCurrentSpace(spaceUserId)) {
      setManagedSpaceId(spaceUserId, true);
    } else {
      setManagedSpaceId(null, true);
    }
  }

  private String getShortSpaceId(String spaceId) {
    SilverTrace.info("jobStartPagePeas", "JobStartPagePeasSessionController.getShortSpaceId()",
        "root.MSG_GEN_PARAM_VALUE", "spaceId=" + spaceId);
    if ((spaceId != null) && (spaceId.startsWith("WA"))) {
      return spaceId.substring(2);
    }
    return (spaceId == null) ? "" : spaceId;
  }

  // method get
  public SpaceInst getSpaceInstFromTemplate(String templateName) {
    return adminController.getSpaceInstFromTemplate(templateName);
  }

  public Map<String, SpaceTemplate> getAllSpaceTemplates() {
    return adminController.getAllSpaceTemplates();
  }

  @Override
  public String getSpaceId() {
    return m_NavBarMgr.getCurrentSpaceId();
  }

  public DisplaySorted[] getSpaces() {
    return m_NavBarMgr.getAvailableSpaces();
  }

  public DisplaySorted[] getSpaceComponents() {
    return m_NavBarMgr.getAvailableSpaceComponents();
  }

  public String getSubSpaceId() {
    return m_NavBarMgr.getCurrentSubSpaceId();
  }

  public DisplaySorted[] getSubSpaces() {
    return m_NavBarMgr.getAvailableSubSpaces();
  }

  public DisplaySorted[] getSubSpaceComponents() {
    return m_NavBarMgr.getAvailableSubSpaceComponents();
  }

  public void setSpaceMaintenance(String spaceId, boolean mode) {
    setSpaceModeMaintenance(spaceId, mode);
  }

  public void refreshCurrentSpaceCache() {
    m_NavBarMgr.resetSpaceCache(getManagedSpaceId());
  }

  public void setManagedInstanceId(String sId) {
    m_ManagedInstanceId = sId;
    setScope(SCOPE_BACKOFFICE);
  }

  public void setManagedInstanceId(String sId, int scope) {
    setManagedInstanceId(sId);
    setScope(scope);
  }

  public String getManagedInstanceId() {
    return m_ManagedInstanceId;
  }

  public boolean isComponentManageable(String componentId) {
    return getOrganizationController().isComponentManageable(componentId, getUserId());
  }

  public void setManagedProfile(ProfileInst sProfile) {
    m_ManagedProfile = sProfile;

    if (sProfile != null) {
      m_ManagedInheritedProfile = adminController.getComponentInst(
          getManagedInstanceId()).getInheritedProfileInst(sProfile.getName());
    } else {
      m_ManagedInheritedProfile = null;
    }
  }

  public ProfileInst getManagedProfile() {
    return m_ManagedProfile;
  }

  public String getManagedProfileHelp(String componentName) {
    return getComponentByName(componentName).getProfile(getManagedProfile().getName()).getHelp(
        getLanguage());
  }

  public ProfileInst getManagedInheritedProfile() {
    return m_ManagedInheritedProfile;
  }

  public Boolean isProfileEditable() {
    return JobStartPagePeasSettings.m_IsProfileEditable;
  }

  public Boolean isBackupEnable() {
    return JobStartPagePeasSettings.isBackupEnable;
  }

  public String getConfigSpacePosition() {
    return JobStartPagePeasSettings.SPACEDISPLAYPOSITION_CONFIG;
  }

  /*********************** Gestion des espaces *****************************************/

  /**
   * @param isNew
   * @return
   */
  public SpaceInst[] getBrotherSpaces(boolean isNew) {
    String[] sids;
    SpaceInst spaceint1 = getSpaceInstById();
    String fatherId;
    String currentSpaceId;
    int j;

    if (isNew) {
      if (spaceint1 == null) {
        fatherId = null;
      } else {
        fatherId = "WA" + getManagedSpaceId();
      }
      currentSpaceId = "";
    } else {
      fatherId = spaceint1.getDomainFatherId();
      currentSpaceId = "WA" + getManagedSpaceId();
    }

    if (fatherId != null && !fatherId.equals("0")) {
      sids = adminController.getAllSubSpaceIds(fatherId);
    } else {
      sids = adminController.getAllRootSpaceIds();
    }

    if (sids == null || sids.length <= 0) {
      return new SpaceInst[0];
    }
    if (isNew) {
      m_BrothersSpaces = new SpaceInst[sids.length];
    } else {
      m_BrothersSpaces = new SpaceInst[sids.length - 1];
    }
    j = 0;
    for (String sid : sids) {
      if (isNew || !sid.equals(currentSpaceId)) {
        m_BrothersSpaces[j++] = adminController.getSpaceInstById(sid);
      }
    }
    Arrays.sort(m_BrothersSpaces);
    return m_BrothersSpaces;
  }

  // Get spaces "manageable" by the current user (ie spaces in maintenance or current space)
  public SpaceInst[] getUserManageableSpacesIds() {
    List<SpaceInst> vManageableSpaces = new ArrayList<SpaceInst>();
    String[] sids = getUserManageableSpaceIds();
    SpaceInst currentSpace = getSpaceInstById();
    String currentSpaceId = (currentSpace == null) ? "-1" : currentSpace.getId();

    for (String sid : sids) {
      if (isSpaceInMaintenance(sid.substring(2)) || sid.equals(currentSpaceId)) {
        vManageableSpaces.add(adminController.getSpaceInstById(sid));
      }
    }

    SpaceInst[] aManageableSpaces = vManageableSpaces.toArray(new SpaceInst[vManageableSpaces.size()]);
    Arrays.sort(aManageableSpaces);
    return aManageableSpaces;
  }

  public void setSpacePlace(String idSpaceBefore) {
    int orderNum = 0;
    int i = 0;
    SpaceInst theSpace = getSpaceInstById();

    for (i = 0; i < m_BrothersSpaces.length; i++) {
      if (idSpaceBefore.equals(m_BrothersSpaces[i].getId())) {
        theSpace.setOrderNum(orderNum);
        adminController.updateSpaceOrderNum(theSpace.getId(), orderNum);
        orderNum++;
      }
      if (m_BrothersSpaces[i].getOrderNum() != orderNum) {
        m_BrothersSpaces[i].setOrderNum(orderNum);
        adminController.updateSpaceOrderNum(m_BrothersSpaces[i].getId(), orderNum);
      }
      orderNum++;
    }
    if (orderNum == i) {
      theSpace.setOrderNum(orderNum);
      adminController.updateSpaceOrderNum(theSpace.getId(), orderNum);
      orderNum++;
    }
    m_NavBarMgr.resetAllCache();
  }

  public SpaceInst getSpaceInstById(String idSpace) {
    if (idSpace == null || idSpace.length() <= 0) {
      return null;
    }
    if (idSpace.length() > 2 && idSpace.substring(0, 2).equals("WA")) {
      idSpace = idSpace.substring(2);
    }
    return adminController.getSpaceInstById("WA" + idSpace);
  }

  public void setCreateSpaceParameters(String name, String desc, String ssEspace,
      String spaceTemplate, String language, String look) {
    m_ssEspace = ssEspace;
    m_name = name;
    m_desc = desc;
    currentLanguage = language;
    m_spaceTemplate = spaceTemplate;
    m_look = look;
    // Only use global variable to set spacePosition
    m_spaceFirst = !JobStartPagePeasSettings.SPACEDISPLAYPOSITION_AFTER.equalsIgnoreCase(
        JobStartPagePeasSettings.SPACEDISPLAYPOSITION_CONFIG);
  }

  public String createSpace() {
    SpaceInst spaceInst;

    if (m_desc == null) {
      m_desc = "";
    }

    // Create the space
    if (m_spaceTemplate != null && m_spaceTemplate.length() > 0) {
      spaceInst = getSpaceInstFromTemplate(m_spaceTemplate);
    } else {
      spaceInst = new SpaceInst();
    }

    SpaceInst spaceint1 = getSpaceInstById();
    String fatherId = null;

    if (m_ssEspace != null && m_ssEspace.equals("SousEspace")) { // on est en creation de
      // sous-espace
      String idSpace = spaceint1.getId();
      if (idSpace != null) {
        spaceInst.setDomainFatherId(idSpace);
      }
    } else {// on est en creation d'espace
      if (spaceint1 != null) {
        fatherId = spaceint1.getDomainFatherId();
      }
      if (fatherId != null && !fatherId.equals("0")) {// dans un espace
        SilverTrace.info("jobStartPagePeas",
            "JobStartPagePeasRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "setDomainFatherId !!");
        spaceInst.setDomainFatherId("WA" + fatherId);
      }
    }

    spaceInst.setName(m_name);
    spaceInst.setDescription(m_desc);
    spaceInst.setLanguage(currentLanguage);
    spaceInst.setCreatorUserId(getUserId());
    String sSpaceInstId = addSpaceInst(spaceInst, m_spaceTemplate);
    if (sSpaceInstId != null && sSpaceInstId.length() > 0) {
      SilverTrace.spy("jobStartPagePeas",
          "JobStartPagePeasSessionController.createSpace()",
          sSpaceInstId, "SP", spaceInst.getName(),
          getUserDetail().getId(), SilverTrace.SPY_ACTION_CREATE);

      if (m_ssEspace != null && m_ssEspace.equals("SousEspace")) { // on est en creation de
        // sous-espace
        setSubSpaceId(sSpaceInstId);
      } else {// on est en creation d'espace
        if (fatherId != null && !fatherId.equals("0")) {// dans un espace
          setSubSpaceId(sSpaceInstId);
        } else {
          setSpaceId(sSpaceInstId);
        }
      }
    }
    spaceInst.setDisplaySpaceFirst(m_spaceFirst);
    return sSpaceInstId;
  }

  public String addSpaceInst(SpaceInst spaceInst, String templateName) {
    String res = adminController.addSpaceInst(spaceInst);
    if (res == null || res.length() == 0) {
      return res;
    }

    SilverTrace.info("jobStartPagePeas",
        "JobStartPagePeasRequestRouter.addSpaceInst()",
        "root.MSG_GEN_PARAM_VALUE", "SpaceAdded");
    if (templateName != null && templateName.length() > 0) {
      SpaceInst si = adminController.getSpaceInstById(res);

      // Apply the Template profiles
      ArrayList<ComponentInst> acl = si.getAllComponentsInst();
      if (acl != null) {
        for (ComponentInst ci : acl) {
          Map<String, ProfileInst> componentProfilesToCreate = new HashMap<String, ProfileInst>();
          SilverTrace.info("jobStartPagePeas", "JobStartPagePeasRequestRouter.addSpaceInst()",
              "root.MSG_GEN_PARAM_VALUE", "Looking for component " + ci.getLabel() + " - " + ci.
              getName());
          // Add profiles
          for (ProfileInst profileInst : componentProfilesToCreate.values()) {
            adminController.addProfileInst(profileInst);
          }
        }
      }
    }
    // Finally refresh the cache
    m_NavBarMgr.addSpaceInCache(res);
    return res;
  }

  public String updateSpaceInst(SpaceInst spaceInst) {
    SilverTrace.spy("jobStartPagePeas",
        "JobStartPagePeasSessionController.updateSpaceInst()",
        spaceInst.getId(), "SP", spaceInst.getName(),
        getUserId(), SilverTrace.SPY_ACTION_UPDATE);

    spaceInst.setUpdaterUserId(getUserId());
    String res = adminController.updateSpaceInst(spaceInst);
    return res;
  }

  private boolean isRemovingSpaceAllowed(String spaceId) {
    if (isUserAdmin()) {
      // admin can always remove space
      return true;
    } else {
      List<String> spaceIds = Arrays.asList(getUserManageableSpaceIds());
      if (spaceIds == null || spaceIds.isEmpty()) {
        // user is not a space manager
        return false;
      } else {
        // Check if user manages this space or one of its parent
        List<SpaceInst> spaces = getOrganizationController().getSpacePath(spaceId);
        for (SpaceInst spaceInPath : spaces) {
          if (spaceIds.contains(spaceInPath.getId())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public String deleteSpace(String spaceId) {

    if (!isRemovingSpaceAllowed(spaceId)) {
      SilverTrace.error("jobStartPagePeas",
          "JobStartPagePeasSessionController.deleteSpace()",
          "root.MSG_GEN_PARAM_VALUE", "user #" + getUserId() + " is not allowed to delete space #"
          + spaceId);
      return "";
    } else {
      SpaceInst spaceint1 = adminController.getSpaceInstById(spaceId);
      SilverTrace.spy("jobStartPagePeas",
          "JobStartPagePeasSessionController.deleteSpace()",
          spaceint1.getId(), "SP", spaceint1.getName(),
          getUserDetail().getId(), SilverTrace.SPY_ACTION_DELETE);

      boolean definitiveDelete = !JobStartPagePeasSettings.isBasketEnable;
      if (JobStartPagePeasSettings.isBasketEnable && isUserAdmin()) {
        definitiveDelete = !JobStartPagePeasSettings.useBasketWhenAdmin;
      }

      String res = adminController.deleteSpaceInstById(spaceint1.getId(),
          definitiveDelete);

      m_NavBarMgr.removeSpaceInCache(res);
      if (isManagedSpaceRoot()) {
        setManagedSpaceId(null, true);
      } else {
        setManagedSpaceId(getSpaceId(), true);
      }
      return res;
    }
  }

  public void recoverSpaceRights(String spaceId) throws AdminException {
    Recover recover = new Recover();
    if (spaceId == null) {
      recover.recoverRights();
    } else if (StringUtil.isDefined(spaceId)) {
      recover.recoverSpaceRights(spaceId);
    }
  }

  /*********************** Gestion des managers d'espaces *****************************************/
  public String getSpaceProfileName(SpaceInst spaceint1) {
    ArrayList<SpaceProfileInst> m_Profile = spaceint1.getAllSpaceProfilesInst();
    int i = 0;
    SpaceProfileInst m_SpaceProfileInst = null;
    String name = "";
    if (i < m_Profile.size()) {// seulement le premier profil (manager)
      m_SpaceProfileInst = m_Profile.get(i);
      name = m_SpaceProfileInst.getLabel();
    }
    SilverTrace.info("jobStartPagePeas",
        "JobStartPagePeasSessionController.getSpaceProfileName()",
        "root.MSG_GEN_PARAM_VALUE", "name avant= " + name);

    if (name != null) {
      if (name.equals("")) {
        name = getMultilang().getString("Manager");
      }
    } else {
      name = getMultilang().getString("Manager");
    }

    SilverTrace.info("jobStartPagePeas",
        "JobStartPagePeasSessionController.getSpaceProfileName()",
        "root.MSG_GEN_PARAM_VALUE", "name après = " + name);
    return name;
  }

  // arrayList de String
  private List<String> getAllCurrentGroupIdSpace(String role) {
    SpaceProfileInst m_SpaceProfileInst = getSpaceInstById().getSpaceProfileInst(
        role);
    if (m_SpaceProfileInst != null) {
      return m_SpaceProfileInst.getAllGroups();
    }

    return new ArrayList<String>();
  }

  // arrayList de Group
  public List<Group> getAllCurrentGroupSpace(String role) {
    List<Group> res = new ArrayList<Group>();
    SpaceProfileInst m_SpaceProfileInst = getSpaceInstById().getSpaceProfileInst(
        role);

    if (m_SpaceProfileInst != null) {
      List<String> alGroupIds = m_SpaceProfileInst.getAllGroups();

      Group theGroup = null;
      for (String alGroupId : alGroupIds) {
        theGroup = adminController.getGroupById(alGroupId);
        res.add(theGroup);
      }
    }
    return res;
  }

  // arrayList de String
  private List<String> getAllCurrentUserIdSpace(String role) {
    SpaceProfileInst m_SpaceProfileInst = getSpaceInstById().getSpaceProfileInst(
        role);
    if (m_SpaceProfileInst != null) {
      return m_SpaceProfileInst.getAllUsers();
    }

    return new ArrayList<String>();
  }

  // List de userDetail
  public List<UserDetail> getAllCurrentUserSpace(String role) {
    List<UserDetail> res = new ArrayList<UserDetail>();
    SpaceProfileInst m_SpaceProfileInst = getSpaceInstById().getSpaceProfileInst(
        role);

    if (m_SpaceProfileInst != null) {
      List<String> alUserIds = m_SpaceProfileInst.getAllUsers();

      UserDetail userDetail = null;
      for (String alUserId : alUserIds) {
        userDetail = adminController.getUserDetail(alUserId);
        res.add(userDetail);
      }
    }
    return res;
  }

  // user panel de selection de n groupes et n users
  public void initUserPanelSpaceForGroupsUsers(String compoURL, String role)
      throws SelectionException {
    SpaceInst spaceint1 = getSpaceInstById();
    SpaceProfileInst profile = spaceint1.getSpaceProfileInst(role);

    selection.resetAll();

    String hostSpaceName = getMultilang().getString("JSPP.manageHomePage");
    selection.setHostSpaceName(hostSpaceName);

    PairObject hostComponentName = null;
    String idFather = getSpaceInstById().getDomainFatherId();
    if (idFather != null && !idFather.equals("0")) {// je suis sur un ss-espace
      SpaceInst spaceFather = getSpaceInstById(idFather);
      hostComponentName = new PairObject(spaceFather.getName() + " > " + getSpaceInstById().
          getName(), null);
    } else {
      hostComponentName = new PairObject(getSpaceInstById().getName(), null);
    }
    selection.setHostComponentName(hostComponentName);

    String nameProfile = null;
    if (profile == null) {
      nameProfile = getMultilang().getString("JSPP." + role);
    } else {
      nameProfile = profile.getLabel();
      if (!StringUtil.isDefined(nameProfile)) {
        nameProfile = getMultilang().getString("JSPP." + role);
      }
    }
    ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(getLanguage());
    PairObject[] hostPath = { new PairObject(nameProfile + " > " + generalMessage.getString(
        "GML.selection"), null) };
    selection.setHostPath(hostPath);

    String hostUrl = compoURL + "EffectiveUpdateSpaceProfile?Role=" + role;
    if (profile == null) // creation
    {
      hostUrl = compoURL + "EffectiveCreateSpaceProfile?Role=" + role;
    }
    SilverTrace.info("jobStartPagePeas",
        "JobStartPagePeasSessionController.initUserPanelSpaceForGroupsUsers()",
        "root.MSG_GEN_PARAM_VALUE", "compoURL = " + compoURL + " hostSpaceName=" + hostSpaceName
        + " hostComponentName=" + getSpaceInstById().
        getName() + " hostUrlTest=" + hostUrl);
    selection.setGoBackURL(hostUrl);
    selection.setCancelURL(compoURL + "CancelCreateOrUpdateSpaceProfile?Role=" + role);

    List<String> users = getAllCurrentUserIdSpace(role);
    List<String> groups = getAllCurrentGroupIdSpace(role);
    selection.setSelectedElements(users.toArray(new String[users.size()]));
    selection.setSelectedSets(groups.toArray(new String[groups.size()]));
  }

  public void createSpaceRole(String role) {
    // Create the profile
    SpaceProfileInst spaceProfileInst = new SpaceProfileInst();
    spaceProfileInst.setName(role);
    if (role.equals("Manager")) {
      spaceProfileInst.setLabel("Manager d'espace");
    }
    spaceProfileInst.setSpaceFatherId(getSpaceInstById().getId());

    setGroupsAndUsers(spaceProfileInst, selection.getSelectedSets(), selection
        .getSelectedElements());

    SilverTrace.spy("jobStartPagePeas", "JobStartPagePeasSC.createSpaceRole", spaceProfileInst.
        getSpaceFatherId(), "N/A", spaceProfileInst.getName(), getUserId(),
        SilverTrace.SPY_ACTION_CREATE);

    // Add the profile
    adminController.addSpaceProfileInst(spaceProfileInst, getUserId());
  }

  public void updateSpaceRole(String role) {
    // Update the profile
    SpaceInst spaceint1 = getSpaceInstById();
    SpaceProfileInst m_SpaceProfileInst = spaceint1.getSpaceProfileInst(role);

    SpaceProfileInst spaceProfileInst = new SpaceProfileInst();
    spaceProfileInst.setId(m_SpaceProfileInst.getId());
    spaceProfileInst.setSpaceFatherId(spaceint1.getId());
    spaceProfileInst.setName(m_SpaceProfileInst.getName());

    setGroupsAndUsers(spaceProfileInst, selection.getSelectedSets(), selection
        .getSelectedElements());

    SilverTrace.spy("jobStartPagePeas", "JobStartPagePeasSC.updateSpaceRole", spaceProfileInst.
        getSpaceFatherId(), "N/A", spaceProfileInst.getName(), getUserId(),
        SilverTrace.SPY_ACTION_UPDATE);

    // Update the profile
    adminController.updateSpaceProfileInst(spaceProfileInst, getUserId());
  }

  public void deleteSpaceRole(String role) {
    // Delete the space profile
    SpaceProfileInst m_SpaceProfileInst = getSpaceInstById().getSpaceProfileInst(
        role);
    if (m_SpaceProfileInst != null) {
      SilverTrace.spy("jobStartPagePeas", "JobStartPagePeasSC.deleteSpaceRole", m_SpaceProfileInst.
          getSpaceFatherId(), "N/A", m_SpaceProfileInst.getName(), getUserId(),
          SilverTrace.SPY_ACTION_DELETE);
      adminController.deleteSpaceProfileInst(m_SpaceProfileInst.getId(), getUserId());
    }
  }

  public void updateSpaceManagersDescription(SpaceProfileInst spaceProfileInst) {
    SilverTrace.spy("jobStartPagePeas",
        "JobStartPagePeasSC.updateSpaceManagersDescription", spaceProfileInst.getSpaceFatherId(),
        "N/A", spaceProfileInst.getName(), getUserId(),
        SilverTrace.SPY_ACTION_UPDATE);

    // Update the profile description
    adminController.updateSpaceProfileInst(spaceProfileInst, getUserId());
  }

  /*********************** Gestion de la corbeille *****************************************/
  public List<SpaceInstLight> getRemovedSpaces() {
    List<SpaceInstLight> removedSpaces = adminController.getRemovedSpaces();
    SpaceInstLight space = null;
    String name = null;
    for (int s = 0; removedSpaces != null && s < removedSpaces.size(); s++) {
      space = removedSpaces.get(s);
      space.setRemoverName(getOrganizationController().getUserDetail(String.valueOf(space.
          getRemovedBy())).getDisplayedName());
      space.setPath(adminController.getPathToSpace(space.getFullId(), false));

      // Remove suffix
      name = space.getName();
      name = name.substring(0, name.indexOf(Admin.basketSuffix));
      space.setName(name);
    }
    return removedSpaces;
  }

  public List<ComponentInstLight> getRemovedComponents() {
    List<ComponentInstLight> removedComponents = adminController.getRemovedComponents();
    ComponentInstLight component = null;
    String name = null;
    for (int s = 0; removedComponents != null && s < removedComponents.size(); s++) {
      component = removedComponents.get(s);
      component.setRemoverName(getOrganizationController().getUserDetail(String.valueOf(component.
          getRemovedBy())).getDisplayedName());
      component.setPath(adminController.getPathToComponent(component.getId()));

      // Remove suffix
      name = component.getLabel();
      name = name.substring(0, name.indexOf(Admin.basketSuffix));
      component.setLabel(name);
    }
    return removedComponents;
  }

  public void restoreSpaceFromBin(String spaceId) {
    adminController.restoreSpaceFromBasket(spaceId);

    // Display restored space in navBar
    m_NavBarMgr.resetAllCache();
  }

  public void deleteSpaceInBin(String spaceId) {
    adminController.deleteSpaceInstById(spaceId, true);
  }

  public void restoreComponentFromBin(String componentId) {
    adminController.restoreComponentFromBasket(componentId);
  }

  public void deleteComponentInBin(String componentId) {
    adminController.deleteComponentInst(componentId, true);
  }

  /*********************** Gestion des composants *****************************************/
  public ComponentInst[] getBrotherComponents(boolean isNew) {
    ArrayList<ComponentInst> arc = getSpaceInstById().getAllComponentsInst();
    if (arc == null || arc.isEmpty()) {
      return ArrayUtil.EMPTY_COMPONENT_INSTANCE_ARRAY;
    }
    if (isNew) {
      m_BrothersComponents = new ComponentInst[arc.size()];
    } else {
      m_BrothersComponents = new ComponentInst[arc.size() - 1];
    }
    int j = 0;
    for (ComponentInst theComponent : arc) {
      SilverTrace.info("jobStartPagePeas",
          "JobStartPagePeasSesionController.getBrotherComponents()",
          "root.MSG_GEN_PARAM_VALUE", "Current = '" + getManagedInstanceId() + "' Loop = '"
          + theComponent.getId() + "'");
      if (isNew || !theComponent.getId().equals(getManagedInstanceId())) {
        m_BrothersComponents[j++] = theComponent;
      }
    }
    Arrays.sort(m_BrothersComponents);
    return m_BrothersComponents;
  }

  // get all components in the space
  public ComponentInst[] getComponentsOfSpace(String spaceId) {
    ArrayList<ComponentInst> arc = getSpaceInstById(spaceId).getAllComponentsInst();
    if (arc == null || arc.isEmpty()) {
      return ArrayUtil.EMPTY_COMPONENT_INSTANCE_ARRAY;
    }
    ComponentInst[] m_Components = new ComponentInst[arc.size()];
    int j = 0;
    for (ComponentInst theComponent : arc) {
      SilverTrace.info("jobStartPagePeas",
          "JobStartPagePeasSesionController.getComponentsOfSpace()",
          "root.MSG_GEN_PARAM_VALUE", "Current = '" + getManagedInstanceId() + "' Loop = '"
          + theComponent.getId() + "'");
      m_Components[j++] = theComponent;
    }
    Arrays.sort(m_BrothersComponents);
    return m_Components;
  }

  public void setComponentPlace(String idComponentBefore) {
    int orderNum = 0;
    int i = 0;
    ComponentInst theComponent = getComponentInst(getManagedInstanceId());

    for (i = 0; i < m_BrothersComponents.length; i++) {
      if (idComponentBefore.equals(m_BrothersComponents[i].getId())) {
        theComponent.setOrderNum(orderNum);
        adminController.updateComponentOrderNum(theComponent.getId(), orderNum);
        orderNum++;
      }
      if (m_BrothersComponents[i].getOrderNum() != orderNum) {
        m_BrothersComponents[i].setOrderNum(orderNum);
        adminController.updateComponentOrderNum(m_BrothersComponents[i].getId(),
            orderNum);
      }
      orderNum++;
    }
    if (orderNum == i) {
      theComponent.setOrderNum(orderNum);
      adminController.updateComponentOrderNum(theComponent.getId(), orderNum);
      orderNum++;
    }
    m_NavBarMgr.resetSpaceCache(getManagedSpaceId());
  }

  public void setMoveComponentToSpace(ComponentInst component,
      String destinationSpaceId, String idComponentBefore) throws AdminException {
    SilverTrace.info("jobStartPagePeas",
        "JobStartPagePeasSessionController.setMoveComponentToSpace()",
        "root.MSG_GEN_PARAM_VALUE",
        "component = " + component.getId() + " espace dest:" + destinationSpaceId
        + " idComponentBefore=" + idComponentBefore);
    String originSpace = component.getDomainFatherId();
    ComponentInst[] m_destBrothersComponents = getDestBrotherComponents(
        destinationSpaceId, true, component.getId());
    adminController.moveComponentInst(destinationSpaceId, component.getId(),
        idComponentBefore, m_destBrothersComponents);
    // The destination Space becomes the managed space
    setManagedSpaceId(originSpace, false);
    m_NavBarMgr.resetAllCache();
  }

  public ComponentInst[] getDestBrotherComponents(String spaceId, boolean isNew, String componentId) {
    ComponentInst[] m_DestBrothersComponents;

    ArrayList<ComponentInst> arc = getSpaceInstById(spaceId).getAllComponentsInst();

    if (arc == null || arc.isEmpty()) {
      return ArrayUtil.EMPTY_COMPONENT_INSTANCE_ARRAY;
    }
    if (isNew) {
      m_DestBrothersComponents = new ComponentInst[arc.size()];
    } else {
      m_DestBrothersComponents = new ComponentInst[arc.size() - 1];
    }
    int j = 0;
    for (ComponentInst theComponent : arc) {
      SilverTrace.info("jobStartPagePeas",
          "JobStartPagePeasSesionController.getDestBrotherComponents()",
          "root.MSG_GEN_PARAM_VALUE", "Current = '" + componentId + "' Loop = '" + theComponent.
          getId() + "'");
      if (isNew || !theComponent.getId().equals(componentId)) {
        m_DestBrothersComponents[j++] = theComponent;
      }
    }
    Arrays.sort(m_BrothersComponents);
    return m_DestBrothersComponents;
  }

  public WAComponent[] getAllComponents() {
    // liste des composants triés ordre alphabétique
    Map<String, WAComponent> resTable = adminController.getAllComponents();
    WAComponent[] componentsModels = resTable.values().toArray(new WAComponent[resTable.size()]);
    Arrays.sort(componentsModels, new Comparator<WAComponent>() {

      @Override
      public int compare(WAComponent o1, WAComponent o2) {
        String valcomp1 = o1.getSuite() + o1.getLabel().get(I18NHelper.defaultLanguage);
        String valcomp2 = o2.getSuite() + o2.getLabel().get(I18NHelper.defaultLanguage);
        return valcomp1.toUpperCase().compareTo(valcomp2.toUpperCase());
        }
            });
    return componentsModels;
  }

  public List<LocalizedComponent> getAllLocalizedComponents() {
    // liste des composants triés ordre alphabétique
    Map<String, WAComponent> resTable = adminController.getAllComponents();
    List<LocalizedComponent> result = new ArrayList<LocalizedComponent>(resTable.size());
    for (WAComponent component : resTable.values()) {
      result.add(new LocalizedComponent(component, getLanguage()));
    }
    Collections.sort(result, new Comparator<LocalizedComponent>() {

      @Override
      public int compare(LocalizedComponent o1, LocalizedComponent o2) {
        String valcomp1 = o1.getSuite() + o1.getLabel();
        String valcomp2 = o2.getSuite() + o2.getLabel();
        return valcomp1.toUpperCase().compareTo(valcomp2.toUpperCase());
        }
            });
    return result;
  }

  public WAComponent getComponentByName(String name) {
    WAComponent[] compos = getAllComponents();
    if (compos != null) {
      for (WAComponent compo : compos) {
        if (compo.getName().equals(name)) {
          return compo;
        }
      }
    }
    return null;
  }

  public String addComponentInst(ComponentInst componentInst) {
    SilverTrace.spy("jobStartPagePeas",
        "JobStartPagePeasSessionController.addComponentInst()",
        componentInst.getDomainFatherId(), "CMP", componentInst.getLabel(),
        getUserDetail().getId(), SilverTrace.SPY_ACTION_CREATE);

    componentInst.setCreatorUserId(getUserId());
    return adminController.addComponentInst(componentInst);
  }

  public ComponentInst getComponentInst(String sInstanceId) {
    ComponentInst component = adminController.getComponentInst(sInstanceId);

    component.setCreator(getUserDetail(component.getCreatorUserId()));
    component.setUpdater(getUserDetail(component.getUpdaterUserId()));
    component.setRemover(getUserDetail(component.getRemoverUserId()));

    return component;
  }

  public String updateComponentInst(ComponentInst componentInst) {
    SilverTrace.spy("jobStartPagePeas",
        "JobStartPagePeasSessionController.updateComponentInst()",
        componentInst.getDomainFatherId(), componentInst.getId(), componentInst.getLabel(),
        getUserDetail().getId(), SilverTrace.SPY_ACTION_UPDATE);

    componentInst.setUpdaterUserId(getUserId());
    return adminController.updateComponentInst(componentInst);
  }

  public String deleteComponentInst(String sInstanceId) {
    SilverTrace.spy("jobStartPagePeas",
        "JobStartPagePeasSessionController.deleteComponentInst()",
        "CMP", sInstanceId, "",
        getUserDetail().getId(), SilverTrace.SPY_ACTION_DELETE);

    boolean definitiveDelete = !JobStartPagePeasSettings.isBasketEnable;
    if (JobStartPagePeasSettings.isBasketEnable && isUserAdmin()) {
      definitiveDelete = !JobStartPagePeasSettings.useBasketWhenAdmin;
    }

    return adminController.deleteComponentInst(sInstanceId, definitiveDelete);
  }

  // ArrayList de ProfileInst dont l'id est vide ou pas
  // role non cree : id vide - name - label (identique à name)
  // role cree : id non vide - name - label
  public List<ProfileInst> getAllProfiles(ComponentInst m_FatherComponentInst) {
    ArrayList<ProfileInst> alShowProfile = new ArrayList<ProfileInst>();
    String sComponentName = m_FatherComponentInst.getName();
    // profils dispo
    String[] asAvailProfileNames = adminController.getAllProfilesNames(sComponentName);
    for (String profileName : asAvailProfileNames) {
      SilverTrace.info("jobStartPagePeas",
          "JobStartPagePeasSessionController.getAllProfilesNames()",
          "root.MSG_GEN_PARAM_VALUE",
          "asAvailProfileNames = " + profileName);
      boolean bFound = false;

      ProfileInst profile = m_FatherComponentInst.getProfileInst(profileName);
      if (profile != null) {
        bFound = true;
        setProfileLabel(sComponentName, profileName, profile);
        alShowProfile.add(profile);
      }

      if (!bFound) {
        profile = new ProfileInst();
        profile.setName(profileName);
        setProfileLabel(sComponentName, profileName, profile);
        alShowProfile.add(profile);
      }
    }
    return alShowProfile;

  }

  private void setProfileLabel(String sComponentName, String profileName, ProfileInst profile) {
    String label = adminController.getProfileLabelfromName(sComponentName, profileName,
        getLanguage());
    if (!StringUtil.isDefined(label)) {
      label = adminController.getProfileLabelfromName(sComponentName, profileName,
          DisplayI18NHelper.getDefaultLanguage());
    }
    if (StringUtil.isDefined(label)) {
      profile.setLabel(label);
    }
  }

  public ProfileInst getProfile(String sProfileId, String sProfileName,
      String sProfileLabel) {
    if (StringUtil.isDefined(sProfileId)) {
      return adminController.getProfileInst(sProfileId);
    }
    ProfileInst res = new ProfileInst();
    res.setName(sProfileName);
    res.setLabel(sProfileLabel);
    return res;
  }

  public List<UserDetail> getAllCurrentUserInstance() {
    return userIds2users(getManagedProfile().getAllUsers());
  }

  public List<Group> getAllCurrentGroupInstance() {
    List<String> alGroupIds = getManagedProfile().getAllGroups();

    return groupIds2groups(alGroupIds);
  }

  public List<Group> groupIds2groups(List<String> groupIds) {
    List<Group> res = new ArrayList<Group>();
    Group theGroup = null;

    for (int nI = 0; groupIds != null && nI < groupIds.size(); nI++) {
      theGroup = adminController.getGroupById(groupIds.get(nI));
      if (theGroup != null) {
        res.add(theGroup);
      }
    }

    return res;
  }

  public List<UserDetail> userIds2users(List<String> userIds) {
    List<UserDetail> res = new ArrayList<UserDetail>();
    UserDetail user = null;

    for (int nI = 0; userIds != null && nI < userIds.size(); nI++) {
      user = getUserDetail(userIds.get(nI));
      if (user != null) {
        res.add(user);
      }
    }

    return res;
  }

  // user panel de selection de n groupes et n users
  public void initUserPanelInstanceForGroupsUsers(String compoURL) throws
      SelectionException {
    String profileId = getManagedProfile().getId();
    String profile = getManagedProfile().getLabel();
    SilverTrace.info("jobStartPagePeas",
        "JobStartPagePeasSessionController.initUserPanelInstanceForGroupsUsers()",
        "root.MSG_GEN_PARAM_VALUE", "profile = " + profile);
    String labelProfile = getMultilang().getString(profile.replace(' ', '_'));
    SilverTrace.info("jobStartPagePeas",
        "JobStartPagePeasSessionController.initUserPanelInstanceForGroupsUsers()",
        "root.MSG_GEN_PARAM_VALUE", "labelProfile = " + labelProfile);
    if (!StringUtil.isDefined(labelProfile)) {
      labelProfile = profile;
    }

    selection.resetAll();

    String hostSpaceName = getMultilang().getString("JSPP.manageHomePage");
    selection.setHostSpaceName(hostSpaceName);

    PairObject hostComponentName = null;
    SpaceInst space = getSpaceInstById();
    if (space != null) {
      String idFather = space.getDomainFatherId();
      if (idFather != null && !idFather.equals("0")) {// je suis sur un ss-espace
        SpaceInst spaceFather = getSpaceInstById(idFather);
        hostComponentName = new PairObject(spaceFather.getName() + " > " + getSpaceInstById().
            getName(), null);
      } else {
        hostComponentName = new PairObject(getSpaceInstById().getName(), null);
      }
      selection.setHostComponentName(hostComponentName);
    }

    ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(getLanguage());
    String compoName = getComponentInst(getManagedInstanceId()).getLabel();
    PairObject[] hostPath =
        { new PairObject(compoName + " > " + labelProfile + " > " + generalMessage.
        getString("GML.selection"), null) };
    selection.setHostPath(hostPath);

    String hostUrl = compoURL + "EffectiveUpdateInstanceProfile";
    if (!StringUtil.isDefined(profileId)) { // creation
      hostUrl = compoURL + "EffectiveCreateInstanceProfile";
    }
    SilverTrace.info("jobStartPagePeas",
        "JobStartPagePeasSessionController.initUserPanelInstanceForGroupsUsers()",
        "root.MSG_GEN_PARAM_VALUE", "hostUrl=" + hostUrl);
    selection.setGoBackURL(hostUrl);
    selection.setCancelURL(compoURL + "CancelCreateOrUpdateInstanceProfile");
    List<String> users = getManagedProfile().getAllUsers();
    List<String> groups = getManagedProfile().getAllGroups();
    selection.setSelectedElements(users.toArray(new String[users.size()]));
    selection.setSelectedSets(groups.toArray(new String[groups.size()]));
  }

  public void createInstanceProfile() {
    // Create the profile
    ProfileInst profileInst = new ProfileInst();
    SilverTrace.info("jobStartPagePeas",
        "JobStartPagePeasSessionController.createInstanceProfile()",
        "root.MSG_GEN_PARAM_VALUE",
        "name='" + getManagedProfile().getName() + "'");
    profileInst.setName(getManagedProfile().getName());
    profileInst.setLabel(getManagedProfile().getLabel());
    profileInst.setComponentFatherId(getManagedInstanceId());

    // set groupIds and userIds
    setGroupsAndUsers(profileInst, selection.getSelectedSets(), selection.getSelectedElements());

    SilverTrace.spy("jobStartPagePeas",
        "JobStartPagePeasSC.createInstanceProfile", "unknown", profileInst.getComponentFatherId(),
        profileInst.getName(), getUserId(),
        SilverTrace.SPY_ACTION_CREATE);

    // Add the profile
    adminController.addProfileInst(profileInst, getUserId());

    // mise à jour
    setManagedProfile(profileInst);
  }

  public String updateInstanceProfile() {
    // Update the profile
    ProfileInst profile = new ProfileInst();
    profile.setId(getManagedProfile().getId());
    profile.setName(getManagedProfile().getName());
    profile.setLabel(getManagedProfile().getLabel());
    profile.setDescription(getManagedProfile().getDescription());
    profile.setComponentFatherId(getManagedProfile().getComponentFatherId());

    // set groupIds and userIds
    setGroupsAndUsers(profile, selection.getSelectedSets(), selection.getSelectedElements());

    SilverTrace.spy("jobStartPagePeas", "JobStartPagePeasSC.updateInstanceProfile", "unknown",
        profile.getComponentFatherId(), profile.getName(), getUserId(),
        SilverTrace.SPY_ACTION_UPDATE);

    // mise à jour
    setManagedProfile(profile);

    // Update the profile
    return adminController.updateProfileInst(profile, getUserId());
  }

  private void setGroupsAndUsers(ProfileInst profile, String[] groupIds,
      String[] userIds) {
    // groups
    for (int i = 0; groupIds != null && i < groupIds.length; i++) {
      if (groupIds[i] != null && groupIds[i].length() > 0) {
        profile.addGroup(groupIds[i]);
      }
    }

    // users
    for (int i = 0; userIds != null && i < userIds.length; i++) {
      if (userIds[i] != null && userIds[i].length() > 0) {
        profile.addUser(userIds[i]);
      }
    }
  }

  private void setGroupsAndUsers(SpaceProfileInst profile, String[] groupIds,
      String[] userIds) {
    // groups
    for (int i = 0; groupIds != null && i < groupIds.length; i++) {
      if (groupIds[i] != null && groupIds[i].length() > 0) {
        profile.addGroup(groupIds[i]);
      }
    }

    // users
    for (int i = 0; userIds != null && i < userIds.length; i++) {
      if (userIds[i] != null && userIds[i].length() > 0) {
        profile.addUser(userIds[i]);
      }
    }
  }

  public String deleteInstanceProfile() {
    // Update the profile
    ProfileInst profile = new ProfileInst();
    profile.setId(getManagedProfile().getId());
    profile.setName(getManagedProfile().getName());
    profile.setLabel(getManagedProfile().getLabel());
    profile.setDescription(getManagedProfile().getDescription());
    profile.setComponentFatherId(getManagedProfile().getComponentFatherId());

    SilverTrace.spy("jobStartPagePeas", "JobStartPagePeasSC.deleteInstanceProfile", "unknown",
        profile.getComponentFatherId(), profile.getName(), getUserId(),
        SilverTrace.SPY_ACTION_DELETE);

    // mise à jour
    setManagedProfile(profile);

    // Update the profile
    return adminController.updateProfileInst(profile, getUserId());
  }

  public void updateProfileInstanceDescription(String name, String desc) {
    // Update the profile
    ProfileInst profile = new ProfileInst();
    profile.setId(getManagedProfile().getId());
    profile.setName(getManagedProfile().getName());
    profile.setLabel(name);
    profile.setDescription(desc);
    profile.setComponentFatherId(getManagedProfile().getComponentFatherId());
    // groupes
    List<String> groups = getManagedProfile().getAllGroups();
    if (groups != null) {
      for (String group : groups) {
        profile.addGroup(group);
      }
    }
    // users
    List<String> users = getManagedProfile().getAllUsers();
    if (users != null) {
      for (String user : users) {
        profile.addUser(user);
      }
    }
    // mise à jour
    setManagedProfile(profile);
    // Update the profile
    adminController.updateProfileInst(profile);
  }

  /**
   * Copy component
   * @param id
   * @throws RemoteException
   */
  public void copyComponent(String id) throws RemoteException {
    copyOrCutComponent(id, false);
  }
  
  public void cutComponent(String id) throws RemoteException {
    copyOrCutComponent(id, true);
  }
  
  private void copyOrCutComponent(String id, boolean cut) throws RemoteException {
    ComponentInst componentInst = getComponentInst(id);
    ComponentSelection compoSelect = new ComponentSelection(componentInst);
    compoSelect.setCutted(cut);
    SilverTrace.info("jobStartPagePeas", "JobStartPagePeasSessionController.copyComponent()",
        "root.MSG_GEN_PARAM_VALUE", "clipboard = " + getClipboardName() + "' count="
        + getClipboardCount());
    addClipboardSelection(compoSelect);
  }
  
  public void copySpace(String id) throws RemoteException {
    copyOrCutSpace(id, false);
  }
  
  public void cutSpace(String id) throws RemoteException {
    copyOrCutSpace(id, true);
  }
  
  private void copyOrCutSpace(String id, boolean cut) throws RemoteException {
    SpaceInst space = getSpaceInstById(id);
    SpaceSelection spaceSelect = new SpaceSelection(space);
    spaceSelect.setCutted(cut);
    SilverTrace.info("jobStartPagePeas", "JobStartPagePeasSessionController.copyOrCutSpace()",
        "root.MSG_GEN_PARAM_VALUE", "clipboard = " + getClipboardName() + "' count="
        + getClipboardCount());
    addClipboardSelection(spaceSelect);
  }

  /**
   * Paste component(s) copied
   * @throws RemoteException
   * @throws JobStartPagePeasException
   */
  public void paste() throws RemoteException, JobStartPagePeasException {
    try {
      SilverTrace.info("jobStartPagePeas",
          "JobStartPagePeasSessionController.pasteComponent()",
          "root.MSG_GEN_PARAM_VALUE", "clipboard = " + getClipboardName() + " count="
          + getClipboardCount());
      Collection<ClipboardSelection> clipObjects = getClipboardSelectedObjects();
      for (ClipboardSelection clipObject : clipObjects) {
        if (clipObject != null) {
          if (clipObject.isDataFlavorSupported(ComponentSelection.ComponentDetailFlavor)) {
            ComponentInst compo = (ComponentInst) clipObject.getTransferData(
                ComponentSelection.ComponentDetailFlavor);
            if (clipObject.isCutted()) {
              moveComponent(compo.getId());
            } else {
              pasteComponent(compo.getId());
            }
          } else if (clipObject.isDataFlavorSupported(SpaceSelection.SpaceFlavor)) {
            SpaceInst space = (SpaceInst) clipObject.getTransferData(SpaceSelection.SpaceFlavor);
            if (clipObject.isCutted()) {
              moveSpace(space.getId());
            } else {
              pasteSpace(space.getId());
            }
          }
          if (clipObject.isCutted()) {
            m_NavBarMgr.resetAllCache();
          }
        }
      }
    } catch (Exception e) {
      throw new JobStartPagePeasException("JobStartPagePeasSessionController.paste()",
          SilverpeasRuntimeException.ERROR, "jobStartPagePeas.EX_PASTE_ERROR", e);
    }
    clipboardPasteDone();
  }

  /**
   * Paste component with profiles
   * @param componentId
   * @throws JobStartPagePeasException
   */
  public void pasteComponent(String componentId) throws JobStartPagePeasException {
    try {
      String sComponentId = adminController.copyAndPasteComponent(componentId, getManagedSpaceId(),
          getUserId());
      // Adding ok
      if (StringUtil.isDefined(sComponentId)) {
        setManagedInstanceId(sComponentId);
        refreshCurrentSpaceCache();
      }
    } catch (AdminException e) {
      throw new JobStartPagePeasException("JobStartPagePeasSessionController.pasteComponent()",
          SilverpeasRuntimeException.ERROR, "jobStartPagePeas.EX_PASTE_ERROR",
          "componentId = " + componentId + " in space " + getManagedSpaceId(), e);
    }
  }
  
  private void moveComponent(String componentId) throws AdminException {
    adminController.moveComponentInst(getManagedSpaceId(), componentId, null, null);
  }

  private void pasteSpace(String spaceId) throws JobStartPagePeasException {
    try {
      String newSpaceId = adminController.copyAndPasteSpace(spaceId, getManagedSpaceId(),
          getUserId());
      if (StringUtil.isDefined(newSpaceId)) {
        if (StringUtil.isDefined(getManagedSpaceId())) {
          refreshCurrentSpaceCache();
        } else {
          m_NavBarMgr.addSpaceInCache(newSpaceId);
        }
      }
    } catch (AdminException e) {
      throw new JobStartPagePeasException(
          "JobStartPagePeasSessionController.pasteSpace()",
          SilverpeasRuntimeException.ERROR, "jobStartPagePeas.EX_PASTE_ERROR",
          "spaceId = " + spaceId + " in space " + getManagedSpaceId(), e);
    }
  }
  
  private void moveSpace(String spaceId) throws AdminException {
    moveSpace(spaceId, getManagedSpaceId());
  }
  
  public void moveSpace(String spaceId, String targetSpaceId) throws AdminException {
    adminController.moveSpace(spaceId, targetSpaceId);
  }
  
  public int getCurrentSpaceMaintenanceState() {
    if (isAppInMaintenance()) {
      return JobStartPagePeasSessionController.MAINTENANCE_PLATFORM;
    }
    if (isSpaceInMaintenance(getManagedSpaceId())) {
      return JobStartPagePeasSessionController.MAINTENANCE_THISSPACE;
    }
    // check if a parent is is maintenance
    List<SpaceInst> spaces = getOrganizationController().getSpacePath(getManagedSpaceId());
    for (SpaceInst space : spaces) {
      if (isSpaceInMaintenance(space.getId())) {
        return JobStartPagePeasSessionController.MAINTENANCE_ONEPARENT;
      }
    }
    return JobStartPagePeasSessionController.MAINTENANCE_OFF;
  }

  public void setScope(int scope) {
    this.scope = scope;
  }

  public int getScope() {
    return scope;
  }

  /**
   * Return the silverpeas template linked to JobStartPage module
   * @return a SilverpeasTemplate
   */
  public SilverpeasTemplate getSilverpeasTemplate() {
    Properties configuration = new Properties(templateConfiguration);
    SilverpeasTemplate template = SilverpeasTemplateFactory.createSilverpeasTemplate(configuration);
    return template;
  }

}