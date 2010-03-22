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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.jobStartPagePeas.control;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.silverpeas.jobStartPagePeas.DisplaySorted;
import com.silverpeas.jobStartPagePeas.JobStartPagePeasException;
import com.silverpeas.jobStartPagePeas.JobStartPagePeasSettings;
import com.silverpeas.jobStartPagePeas.NavBarManager;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.clipboard.ClipboardSelection;
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
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.SpaceProfileInst;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentPasteInterface;
import com.stratelia.webactiv.beans.admin.instance.control.PasteDetail;
import com.stratelia.webactiv.beans.admin.instance.control.WAComponent;
import com.stratelia.webactiv.beans.admin.spaceTemplates.SpaceTemplate;
import com.stratelia.webactiv.beans.admin.spaceTemplates.SpaceTemplateProfile;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/**
 * Class declaration
 * @author
 */
public class JobStartPagePeasSessionController extends AbstractComponentSessionController {

  AdminController m_AdminCtrl = null;
  NavBarManager m_NavBarMgr = new NavBarManager();
  String m_ManagedSpaceId = null;
  boolean m_isManagedSpaceRoot = true;
  Selection sel = null;
  String m_ManagedInstanceId = null;
  ProfileInst m_ManagedProfile = null;
  ProfileInst m_ManagedInheritedProfile = null;
  // Space creation parameters
  String m_ssEspace = "";
  String m_name = "";
  String m_desc = "";
  String m_language = "";
  String m_look = null;
  String m_spaceTemplate = "";
  SpaceTemplateProfile[] m_TemplateProfiles = new SpaceTemplateProfile[0];
  String[][] m_TemplateProfilesGroups = new String[0][0];
  String[][] m_TemplateProfilesUsers = new String[0][0];
  // Order space / component b
  boolean m_spaceFirst = true;

  // Space sort buffers
  SpaceInst[] m_BrothersSpaces = new SpaceInst[0];
  ComponentInst[] m_BrothersComponents = new ComponentInst[0];

  public JobStartPagePeasSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.silverpeas.jobStartPagePeas.multilang.jobStartPagePeasBundle",
        "com.silverpeas.jobStartPagePeas.settings.jobStartPagePeasIcons");
    setComponentRootName(URLManager.CMP_JOBSTARTPAGEPEAS);
    sel = getSelection();
    m_AdminCtrl = new AdminController(getUserId());
  }

  // Init at first entry
  public void init() {
    m_NavBarMgr.initWithUser(this, getUserDetail(), true);
  }

  public boolean isInheritanceEnable() {
    return JobStartPagePeasSettings.isInheritanceEnable;
  }

  public boolean isJSR168Used() {
    return JobStartPagePeasSettings.useJSR168Portlets;
  }

  public boolean isUserAdmin() {
    return getUserDetail().getAccessLevel().equalsIgnoreCase("A");
  }

  // method du spaceInst
  public SpaceInst getSpaceInstById() {
    if (getManagedSpaceId() == null || getManagedSpaceId().length() <= 0) {
      return null;
    }
    SpaceInst space = m_AdminCtrl.getSpaceInstById("WA" + getManagedSpaceId());

    space.setCreator(getUserDetail(space.getCreatorUserId()));
    space.setUpdater(getUserDetail(space.getUpdaterUserId()));
    space.setRemover(getUserDetail(space.getRemoverUserId()));

    return space;
  }

  public void setManagedSpaceId(String sId, boolean isManagedSpaceRoot) {
    String spaceId = getShortSpaceId(sId);
    m_ManagedSpaceId = spaceId;
    m_isManagedSpaceRoot = isManagedSpaceRoot;
    SilverTrace.info("jobStartPagePeas",
        "JobStartPagePeasSessionController.setManagedSpaceId()",
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
    } else {
      return getSubSpaceComponents();
    }
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
    SilverTrace.info("jobStartPagePeas",
        "JobStartPagePeasSessionController.getShortSpaceId()",
        "root.MSG_GEN_PARAM_VALUE", "spaceId=" + spaceId);
    if ((spaceId != null) && (spaceId.startsWith("WA"))) {
      return spaceId.substring(2);
    } else {
      return (spaceId == null) ? "" : spaceId;
    }
  }

  // method get
  public SpaceInst getSpaceInstFromTemplate(String templateName) {
    return m_AdminCtrl.getSpaceInstFromTemplate(templateName);
  }

  public Hashtable<String, SpaceTemplate> getAllSpaceTemplates() {
    return m_AdminCtrl.getAllSpaceTemplates();
  }

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
  }

  public String getManagedInstanceId() {
    return m_ManagedInstanceId;
  }

  public void setManagedProfile(ProfileInst sProfile) {
    m_ManagedProfile = sProfile;

    if (sProfile != null) {
      m_ManagedInheritedProfile = m_AdminCtrl.getComponentInst(
          getManagedInstanceId()).getInheritedProfileInst(sProfile.getName());
    } else {
      m_ManagedInheritedProfile = null;
    }
  }

  public ProfileInst getManagedProfile() {
    return m_ManagedProfile;
  }

  public ProfileInst getManagedInheritedProfile() {
    return m_ManagedInheritedProfile;
  }

  public Boolean isProfileEditable() {
    return new Boolean(JobStartPagePeasSettings.m_IsProfileEditable);
  }

  public Boolean isBackupEnable() {
    return new Boolean(JobStartPagePeasSettings.isBackupEnable);
  }

  public String getConfigSpacePosition() {
    return JobStartPagePeasSettings.SPACEDISPLAYPOSITION_CONFIG;
  }

  /*********************** Gestion des espaces *****************************************/
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
      sids = m_AdminCtrl.getAllSubSpaceIds(fatherId);
    } else {
      sids = m_AdminCtrl.getAllRootSpaceIds();
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
    for (int i = 0; i < sids.length; i++) {
      if (isNew || !sids[i].equals(currentSpaceId)) {
        m_BrothersSpaces[j++] = m_AdminCtrl.getSpaceInstById(sids[i]);
      }
    }
    Arrays.sort(m_BrothersSpaces);
    return m_BrothersSpaces;
  }

  // Get spaces "manageable" by the current user (ie spaces in maintenance or current space)
  public SpaceInst[] getUserManageableSpacesIds() {
    Vector<SpaceInst> vManageableSpaces = new Vector<SpaceInst>();
    SpaceInst[] aManageableSpaces = null;
    String[] sids = getUserManageableSpaceIds();
    SpaceInst currentSpace = getSpaceInstById();
    String currentSpaceId = (currentSpace == null) ? "-1"
        : currentSpace.getId();

    for (int i = 0; i < sids.length; i++) {
      if ((isSpaceInMaintenance(sids[i].substring(2)))
          || (sids[i].equals(currentSpaceId))) {
        vManageableSpaces.add(
            m_AdminCtrl.getSpaceInstById(sids[i]));
      }
    }

    aManageableSpaces = vManageableSpaces.toArray(new SpaceInst[0]);
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
        m_AdminCtrl.updateSpaceOrderNum(theSpace.getId(), orderNum);
        orderNum++;
      }
      if (m_BrothersSpaces[i].getOrderNum() != orderNum) {
        m_BrothersSpaces[i].setOrderNum(orderNum);
        m_AdminCtrl.updateSpaceOrderNum(m_BrothersSpaces[i].getId(), orderNum);
      }
      orderNum++;
    }
    if (orderNum == i) {
      theSpace.setOrderNum(orderNum);
      m_AdminCtrl.updateSpaceOrderNum(theSpace.getId(), orderNum);
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
    return m_AdminCtrl.getSpaceInstById("WA" + idSpace);
  }

  public SpaceTemplateProfile[] getCurrentSpaceTemplateProfiles() {
    return m_TemplateProfiles;
  }

  public String[][] getCurrentSpaceTemplateProfilesGroups() {
    return m_TemplateProfilesGroups;
  }

  public String[][] getCurrentSpaceTemplateProfilesUsers() {
    return m_TemplateProfilesUsers;
  }

  public void setCreateSpaceParameters(String name, String desc, String ssEspace,
      String spaceTemplate, String language, String look) {
    m_ssEspace = ssEspace;
    m_name = name;
    m_desc = desc;
    m_language = language;
    m_spaceTemplate = spaceTemplate;
    m_TemplateProfiles = m_AdminCtrl.getTemplateProfiles(m_spaceTemplate);
    if (m_TemplateProfiles == null) {
      m_TemplateProfiles = new SpaceTemplateProfile[0];
    }
    m_TemplateProfilesGroups = new String[m_TemplateProfiles.length][0];
    m_TemplateProfilesUsers = new String[m_TemplateProfiles.length][0];
    m_look = look;
    // Only use global variable to set spacePosition
    if (JobStartPagePeasSettings.SPACEDISPLAYPOSITION_AFTER.equalsIgnoreCase(
        JobStartPagePeasSettings.SPACEDISPLAYPOSITION_CONFIG)) {
      m_spaceFirst = false;
    } else {
      m_spaceFirst = true;
    }
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
    spaceInst.setLanguage(m_language);
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
    String res = m_AdminCtrl.addSpaceInst(spaceInst);
    if (res == null || res.length() == 0) {
      return res;
    }

    SilverTrace.info("jobStartPagePeas",
        "JobStartPagePeasRequestRouter.addSpaceInst()",
        "root.MSG_GEN_PARAM_VALUE", "SpaceAdded");
    if (templateName != null && templateName.length() > 0) {
      SpaceInst si = m_AdminCtrl.getSpaceInstById(res);

      // Apply the Template profiles
      ArrayList<ComponentInst> acl = si.getAllComponentsInst();
      if (acl != null) {
        Iterator<ComponentInst> it = acl.iterator();
        while (it.hasNext()) {
          ComponentInst ci = it.next();
          Map<String, ProfileInst> componentProfilesToCreate = new HashMap<String, ProfileInst>();
          SilverTrace.info("jobStartPagePeas",
              "JobStartPagePeasRequestRouter.addSpaceInst()",
              "root.MSG_GEN_PARAM_VALUE", "Looking for component " + ci.getLabel() + " - " + ci.
              getName());
          for (int i = 0; i < m_TemplateProfiles.length; i++) {
            Vector<String> componentProfiles = m_TemplateProfiles[i].
                getMappedComponentProfileName(ci.getLabel());

            if (componentProfiles != null) {
              for (Enumeration<String> e = componentProfiles.elements(); e.
                  hasMoreElements();) {
                String componentProfile = e.nextElement();
                if (componentProfile != null && componentProfile.length() > 0) {
                  WAComponent modeleCompo = m_AdminCtrl.getAllComponents().get(ci.getName());
                  String[] pl = modeleCompo.getProfilList();
                  String refProfileLabel = null;

                  SilverTrace.info("jobStartPagePeas",
                      "JobStartPagePeasRequestRouter.addSpaceInst()",
                      "root.MSG_GEN_PARAM_VALUE", "Scan Template Profile " + m_TemplateProfiles[i].
                      getName() + " Profile Founded : " + componentProfile);
                  for (int j = 0; j < pl.length && refProfileLabel == null; j++) {
                    if (componentProfile.equalsIgnoreCase(pl[j])) {
                      refProfileLabel = modeleCompo.getProfilLabelList()[j];
                    }
                  }

                  if (refProfileLabel != null) {
                    ProfileInst profileInst = null;
                    profileInst = componentProfilesToCreate.get(componentProfile);
                    if (profileInst == null) {
                      profileInst = new ProfileInst();
                      profileInst.setName(componentProfile);
                      profileInst.setLabel(refProfileLabel);
                      profileInst.setComponentFatherId(ci.getId());
                    }

                    // groupes
                    String[] groups = m_TemplateProfilesGroups[i];
                    if (groups != null) {
                      for (int j = 0; j < groups.length; j++) {
                        if ((groups[j] != null) && (groups[j].length() > 0)) {
                          profileInst.addGroup(groups[j]);
                        }
                      }
                    }
                    // users
                    String[] users = m_TemplateProfilesUsers[i];
                    if (users != null) {
                      for (int j = 0; j < users.length; j++) {
                        if ((users[j] != null) && (users[j].length() > 0)) {
                          profileInst.addUser(users[j]);
                        }
                      }
                    }

                    // Add the todo list
                    componentProfilesToCreate.put(componentProfile, profileInst);
                  }
                }
              }
            }
          }

          // Add profiles
          Iterator<ProfileInst> profiles = componentProfilesToCreate.values().iterator();
          while (profiles.hasNext()) {
            ProfileInst profileInst = profiles.next();
            m_AdminCtrl.addProfileInst(profileInst);
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
    String res = m_AdminCtrl.updateSpaceInst(spaceInst);
    return res;
  }

  private boolean isRemovingSpaceAllowed(String spaceId) {
    if (isUserAdmin()) {
      // admin can always remove space
      return true;
    } else {
      List<String> spaceIds = Arrays.asList(getUserManageableSpaceIds());
      if (spaceIds == null || spaceIds.size() == 0) {
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
          "root.MSG_GEN_PARAM_VALUE", "user #" + getUserId() + " is not allowed to delete space #" +
          spaceId);
      return "";
    } else {
      SpaceInst spaceint1 = m_AdminCtrl.getSpaceInstById(spaceId);
      SilverTrace.spy("jobStartPagePeas",
          "JobStartPagePeasSessionController.deleteSpace()",
          spaceint1.getId(), "SP", spaceint1.getName(),
          getUserDetail().getId(), SilverTrace.SPY_ACTION_DELETE);

      boolean definitiveDelete = !JobStartPagePeasSettings.isBasketEnable;
      if (JobStartPagePeasSettings.isBasketEnable && isUserAdmin()) {
        definitiveDelete = !JobStartPagePeasSettings.useBasketWhenAdmin;
      }

      String res = m_AdminCtrl.deleteSpaceInstById(spaceint1.getId(),
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
      for (int nI = 0; nI < alGroupIds.size(); nI++) {
        theGroup = m_AdminCtrl.getGroupById(alGroupIds.get(nI));
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
      for (int nI = 0; nI < alUserIds.size(); nI++) {
        userDetail = m_AdminCtrl.getUserDetail(alUserIds.get(nI));
        res.add(userDetail);
      }
    }
    return res;
  }

  // user panel de selection de n groupes et n users
  public String initUserPanelForTemplateProfile(String compoURL,
      String profileIndex) {
    try {
      SpaceTemplateProfile stp = getCurrentSpaceTemplateProfiles()[Integer.
          parseInt(profileIndex)];
      sel.resetAll();

      String hostSpaceName = getMultilang().getString("JSPP.TemplateProfile");
      sel.setHostSpaceName(hostSpaceName);

      PairObject hostComponentName = new PairObject(stp.getLabel(), null);
      sel.setHostComponentName(hostComponentName);

      ResourceLocator generalMessage = GeneralPropertiesManager.
          getGeneralMultilang(getLanguage());
      PairObject[] hostPath = { new PairObject(generalMessage.getString(
          "GML.selection"), null) };
      sel.setHostPath(hostPath);

      sel.setGoBackURL(
          compoURL + "ReturnUserPanelForTemplateProfile?profileIndex=" + profileIndex +
          "&SpaceTemplate=" + m_spaceTemplate);
      sel.setCancelURL(
          compoURL + "SetSpaceTemplateProfile?SpaceTemplate=" + m_spaceTemplate);
      sel.setPopupMode(true);
      sel.setSelectedElements(m_TemplateProfilesUsers[Integer.parseInt(
          profileIndex)]);
      sel.setSelectedSets(
          m_TemplateProfilesGroups[Integer.parseInt(profileIndex)]);
    } catch (Exception e) {
      SilverTrace.error("jobStartPagePeas",
          "JobStartPageSessionController.initUserPanelForTemplateProfile()",
          "root.EX_USERPANEL_FAILED", "profileIndex = " + profileIndex, e);
    }
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  public void returnUserPanelForTemplateProfile(String profileIndex) {
    m_TemplateProfilesUsers[Integer.parseInt(profileIndex)] = sel.
        getSelectedElements();
    m_TemplateProfilesGroups[Integer.parseInt(profileIndex)] = sel.
        getSelectedSets();
  }

  // user panel de selection de n groupes et n users
  public void initUserPanelSpaceForGroupsUsers(String compoURL, String role)
      throws SelectionException {
    SpaceInst spaceint1 = getSpaceInstById();
    SpaceProfileInst profile = spaceint1.getSpaceProfileInst(role);

    sel.resetAll();

    String hostSpaceName = getMultilang().getString("JSPP.manageHomePage");
    sel.setHostSpaceName(hostSpaceName);

    PairObject hostComponentName = null;
    String idFather = getSpaceInstById().getDomainFatherId();
    if (idFather != null && !idFather.equals("0")) {// je suis sur un ss-espace
      SpaceInst spaceFather = getSpaceInstById(idFather);
      hostComponentName = new PairObject(spaceFather.getName() + " > " + getSpaceInstById().
          getName(), null);
    } else {
      hostComponentName = new PairObject(getSpaceInstById().getName(), null);
    }
    sel.setHostComponentName(hostComponentName);

    String nameProfile = null;
    if (profile == null) {
      nameProfile = getMultilang().getString("JSPP." + role);
    } else {
      nameProfile = profile.getLabel();
      if (!StringUtil.isDefined(nameProfile)) {
        nameProfile = getMultilang().getString("JSPP." + role);
      }
    }
    ResourceLocator generalMessage = GeneralPropertiesManager.
        getGeneralMultilang(getLanguage());
    PairObject[] hostPath = { new PairObject(nameProfile + " > " + generalMessage.
        getString("GML.selection"), null) };
    sel.setHostPath(hostPath);

    String hostUrl = compoURL + "EffectiveUpdateSpaceProfile?Role=" + role;
    if (profile == null) // creation
    {
      hostUrl = compoURL + "EffectiveCreateSpaceProfile?Role=" + role;
    }
    SilverTrace.info("jobStartPagePeas",
        "JobStartPagePeasSessionController.initUserPanelSpaceForGroupsUsers()",
        "root.MSG_GEN_PARAM_VALUE", "compoURL = " + compoURL + " hostSpaceName=" + hostSpaceName +
        " hostComponentName=" + getSpaceInstById().
        getName() + " hostUrlTest=" + hostUrl);
    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(compoURL + "CancelCreateOrUpdateSpaceProfile?Role=" + role);

    List<String> users = getAllCurrentUserIdSpace(role);
    List<String> groups = getAllCurrentGroupIdSpace(role);
    sel.setSelectedElements(users.toArray(new String[0]));
    sel.setSelectedSets(groups.toArray(new String[0]));
  }

  public void createSpaceRole(String role) {
    // Create the profile
    SpaceProfileInst spaceProfileInst = new SpaceProfileInst();
    spaceProfileInst.setName(role);
    if (role.equals("Manager")) {
      spaceProfileInst.setLabel("Manager d'espace");
    }
    spaceProfileInst.setSpaceFatherId(getSpaceInstById().getId());

    setGroupsAndUsers(spaceProfileInst, sel.getSelectedSets(), sel.
        getSelectedElements());

    SilverTrace.spy("jobStartPagePeas", "JobStartPagePeasSC.createSpaceRole", spaceProfileInst.
        getSpaceFatherId(), "N/A", spaceProfileInst.getName(), getUserId(),
        SilverTrace.SPY_ACTION_CREATE);

    // Add the profile
    m_AdminCtrl.addSpaceProfileInst(spaceProfileInst, getUserId());
  }

  public void updateSpaceRole(String role) {
    // Update the profile
    SpaceInst spaceint1 = getSpaceInstById();
    SpaceProfileInst m_SpaceProfileInst = spaceint1.getSpaceProfileInst(role);

    SpaceProfileInst spaceProfileInst = new SpaceProfileInst();
    spaceProfileInst.setId(m_SpaceProfileInst.getId());
    spaceProfileInst.setSpaceFatherId(spaceint1.getId());
    spaceProfileInst.setName(m_SpaceProfileInst.getName());

    setGroupsAndUsers(spaceProfileInst, sel.getSelectedSets(), sel.
        getSelectedElements());

    SilverTrace.spy("jobStartPagePeas", "JobStartPagePeasSC.updateSpaceRole", spaceProfileInst.
        getSpaceFatherId(), "N/A", spaceProfileInst.getName(), getUserId(),
        SilverTrace.SPY_ACTION_UPDATE);

    // Update the profile
    m_AdminCtrl.updateSpaceProfileInst(spaceProfileInst, getUserId());
  }

  public void deleteSpaceRole(String role) {
    // Delete the space profile
    SpaceProfileInst m_SpaceProfileInst = getSpaceInstById().getSpaceProfileInst(
        role);
    if (m_SpaceProfileInst != null) {
      SilverTrace.spy("jobStartPagePeas", "JobStartPagePeasSC.deleteSpaceRole", m_SpaceProfileInst.
          getSpaceFatherId(), "N/A", m_SpaceProfileInst.getName(), getUserId(),
          SilverTrace.SPY_ACTION_DELETE);
      m_AdminCtrl.deleteSpaceProfileInst(m_SpaceProfileInst.getId(), getUserId());
    }
  }

  public void updateSpaceManagersDescription(SpaceProfileInst spaceProfileInst) {
    SilverTrace.spy("jobStartPagePeas",
        "JobStartPagePeasSC.updateSpaceManagersDescription", spaceProfileInst.
        getSpaceFatherId(), "N/A", spaceProfileInst.getName(), getUserId(),
        SilverTrace.SPY_ACTION_UPDATE);

    // Update the profile description
    m_AdminCtrl.updateSpaceProfileInst(spaceProfileInst, getUserId());
  }

  /*********************** Gestion de la corbeille *****************************************/
  public List<SpaceInstLight> getRemovedSpaces() {
    List<SpaceInstLight> removedSpaces = m_AdminCtrl.getRemovedSpaces();
    SpaceInstLight space = null;
    String name = null;
    for (int s = 0; removedSpaces != null && s < removedSpaces.size(); s++) {
      space = removedSpaces.get(s);
      space.setRemoverName(getOrganizationController().getUserDetail(String.
          valueOf(space.getRemovedBy())).getDisplayedName());
      space.setPath(m_AdminCtrl.getPathToSpace(space.getFullId(), false));

      // Remove suffix
      name = space.getName();
      name = name.substring(0, name.indexOf(Admin.basketSuffix));
      space.setName(name);
    }
    return removedSpaces;
  }

  public List<ComponentInstLight> getRemovedComponents() {
    List<ComponentInstLight> removedComponents = m_AdminCtrl.getRemovedComponents();
    ComponentInstLight component = null;
    String name = null;
    for (int s = 0; removedComponents != null && s < removedComponents.size(); s++) {
      component = removedComponents.get(s);
      component.setRemoverName(getOrganizationController().getUserDetail(String.
          valueOf(component.getRemovedBy())).getDisplayedName());
      component.setPath(m_AdminCtrl.getPathToComponent(component.getId()));

      // Remove suffix
      name = component.getLabel();
      name = name.substring(0, name.indexOf(Admin.basketSuffix));
      component.setLabel(name);
    }
    return removedComponents;
  }

  public void restoreSpaceFromBin(String spaceId) {
    m_AdminCtrl.restoreSpaceFromBasket(spaceId);

    // Display restored space in navBar
    m_NavBarMgr.resetAllCache();
  }

  public void deleteSpaceInBin(String spaceId) {
    m_AdminCtrl.deleteSpaceInstById(spaceId, true);
  }

  public void restoreComponentFromBin(String componentId) {
    m_AdminCtrl.restoreComponentFromBasket(componentId);
  }

  public void deleteComponentInBin(String componentId) {
    m_AdminCtrl.deleteComponentInst(componentId, true);
  }

  /*********************** Gestion des composants *****************************************/
  public ComponentInst[] getBrotherComponents(boolean isNew) {
    ArrayList<ComponentInst> arc;
    int j;
    ComponentInst theComponent;

    arc = getSpaceInstById().getAllComponentsInst();

    if (arc == null || arc.size() <= 0) {
      return new ComponentInst[0];
    }
    if (isNew) {
      m_BrothersComponents = new ComponentInst[arc.size()];
    } else {
      m_BrothersComponents = new ComponentInst[arc.size() - 1];
    }
    j = 0;
    for (int i = 0; i < arc.size(); i++) {
      theComponent = arc.get(i);

      SilverTrace.info("jobStartPagePeas",
          "JobStartPagePeasSesionController.getBrotherComponents()",
          "root.MSG_GEN_PARAM_VALUE", "Current = '" + getManagedInstanceId() + "' Loop = '" +
          theComponent.
          getId() + "'");
      if (isNew || !theComponent.getId().equals(getManagedInstanceId())) {
        m_BrothersComponents[j++] = theComponent;
      }
    }
    Arrays.sort(m_BrothersComponents);
    return m_BrothersComponents;
  }

  // get all components in the space
  public ComponentInst[] getComponentsOfSpace(String spaceId) {
    ArrayList<ComponentInst> arc;
    int j;
    ComponentInst theComponent;
    arc = getSpaceInstById(spaceId).getAllComponentsInst();
    if (arc == null || arc.size() <= 0) {
      return new ComponentInst[0];
    }
    ComponentInst[] m_Components = new ComponentInst[arc.size()];
    j = 0;
    for (int i = 0; i < arc.size(); i++) {
      theComponent = arc.get(i);
      SilverTrace.info("jobStartPagePeas",
          "JobStartPagePeasSesionController.getComponentsOfSpace()",
          "root.MSG_GEN_PARAM_VALUE", "Current = '" + getManagedInstanceId() + "' Loop = '" +
          theComponent.
          getId() + "'");
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
        m_AdminCtrl.updateComponentOrderNum(theComponent.getId(), orderNum);
        orderNum++;
      }
      if (m_BrothersComponents[i].getOrderNum() != orderNum) {
        m_BrothersComponents[i].setOrderNum(orderNum);
        m_AdminCtrl.updateComponentOrderNum(m_BrothersComponents[i].getId(),
            orderNum);
      }
      orderNum++;
    }
    if (orderNum == i) {
      theComponent.setOrderNum(orderNum);
      m_AdminCtrl.updateComponentOrderNum(theComponent.getId(), orderNum);
      orderNum++;
    }
    m_NavBarMgr.resetSpaceCache(getManagedSpaceId());
  }

  public void setMoveComponentToSpace(ComponentInst component,
      String destinationSpaceId, String idComponentBefore) throws AdminException {
    SilverTrace.info("jobStartPagePeas",
        "JobStartPagePeasSessionController.setMoveComponentToSpace()",
        "root.MSG_GEN_PARAM_VALUE",
        "component = " + component.getId() + " espace dest:" + destinationSpaceId +
        " idComponentBefore=" + idComponentBefore);
    String originSpace = component.getDomainFatherId();
    ComponentInst[] m_destBrothersComponents = getDestBrotherComponents(
        destinationSpaceId, true, component.getId());
    m_AdminCtrl.moveComponentInst(destinationSpaceId, component.getId(),
        idComponentBefore, m_destBrothersComponents);
    // The destination Space becomes the managed space
    setManagedSpaceId(originSpace, false);
    m_NavBarMgr.resetAllCache();
  }

  public ComponentInst[] getDestBrotherComponents(String spaceId, boolean isNew,
      String componentId) {
    ArrayList<ComponentInst> arc;
    int j;
    ComponentInst theComponent;
    ComponentInst[] m_DestBrothersComponents;

    arc = getSpaceInstById(spaceId).getAllComponentsInst();

    if (arc == null || arc.size() <= 0) {
      return new ComponentInst[0];
    }
    if (isNew) {
      m_DestBrothersComponents = new ComponentInst[arc.size()];
    } else {
      m_DestBrothersComponents = new ComponentInst[arc.size() - 1];
    }
    j = 0;
    for (int i = 0; i < arc.size(); i++) {
      theComponent = arc.get(i);
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
    Hashtable<String, WAComponent> resTable = m_AdminCtrl.getAllComponents();
    WAComponent[] componentsModels = resTable.values().toArray(new WAComponent[0]);
    Arrays.sort(componentsModels, new Comparator<WAComponent>() {

      public int compare(WAComponent o1, WAComponent o2) {
        String valcomp1 = o1.getSuite() + o1.getLabel();
        String valcomp2 = o2.getSuite() + o2.getLabel();
        return valcomp1.toUpperCase().compareTo(valcomp2.toUpperCase());
        }

      public boolean equals(Object o) {
        return false;
        }
            });
    return componentsModels;
  }

  public WAComponent getComponentByNum(int num) {
    WAComponent[] compos = getAllComponents();
    if (num < compos.length) {
      return compos[num];
    } else {
      return null;
    }
  }

  public WAComponent getComponentByName(String name) {
    WAComponent[] compos = getAllComponents();
    for (int nI = 0; compos != null && nI < compos.length; nI++) {
      if (compos[nI].getName().equals(name)) {
        return compos[nI];
      }
    }
    return null;
  }

  public ComponentInst getComponentInst(ComponentInst[] m_aComponentInst,
      String sComponentName) {
    for (int nI = 0; m_aComponentInst != null && nI < m_aComponentInst.length; nI++) {
      if (m_aComponentInst[nI].getName().equals(sComponentName)) {
        return m_aComponentInst[nI];
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
    String res = m_AdminCtrl.addComponentInst(componentInst);
    return res;
  }

  public ComponentInst getComponentInst(String sComponentName,
      String sInstanceId) {
    String id = sInstanceId.substring(sComponentName.length());
    SilverTrace.info("jobStartPagePeas",
        "JobStartPagePeasSessionController.getComponentInst()",
        "root.MSG_GEN_PARAM_VALUE", "idComponent = " + id);
    return m_AdminCtrl.getComponentInst(id);
  }

  public ComponentInst getComponentInst(String sInstanceId) {
    ComponentInst component = m_AdminCtrl.getComponentInst(sInstanceId);

    component.setCreator(getUserDetail(component.getCreatorUserId()));
    component.setUpdater(getUserDetail(component.getUpdaterUserId()));
    component.setRemover(getUserDetail(component.getRemoverUserId()));

    return component;
  }

  public String updateComponentInst(ComponentInst componentInst) {
    SilverTrace.spy("jobStartPagePeas",
        "JobStartPagePeasSessionController.updateComponentInst()",
        componentInst.getDomainFatherId(), componentInst.getId(), componentInst.
        getLabel(),
        getUserDetail().getId(), SilverTrace.SPY_ACTION_UPDATE);

    componentInst.setUpdaterUserId(getUserId());
    return m_AdminCtrl.updateComponentInst(componentInst);
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

    return m_AdminCtrl.deleteComponentInst(sInstanceId, definitiveDelete);
  }

  // ArrayList de ProfileInst dont l'id est vide ou pas
  // role non cree : id vide - name - label (identique à name)
  // role cree : id non vide - name - label
  public ArrayList<ProfileInst> getAllProfiles(ComponentInst m_FatherComponentInst) {
    ArrayList<ProfileInst> alShowProfile = new ArrayList<ProfileInst>();
    ProfileInst profile = null;

    String sComponentName = m_FatherComponentInst.getName();

    // profils dispo
    String[] asAvailProfileNames = m_AdminCtrl.getAllProfilesNames(
        sComponentName);

    // Replace the profiles already selected
    ProfileInst[] asProfileSelected = new ProfileInst[0]; // tableau profils deja selectionnes
    if (m_FatherComponentInst != null) {
      ArrayList<ProfileInst> alProfileInst = m_FatherComponentInst.getAllProfilesInst();
      asProfileSelected = new ProfileInst[alProfileInst.size()];
      for (int nI = 0; nI < alProfileInst.size(); nI++) {
        asProfileSelected[nI] = alProfileInst.get(nI);
      }
    }

    for (int nI = 0; nI < asAvailProfileNames.length; nI++) {
      SilverTrace.info("jobStartPagePeas",
          "JobStartPagePeasSessionController.getAllProfilesNames()",
          "root.MSG_GEN_PARAM_VALUE",
          "asAvailProfileNames = " + asAvailProfileNames[nI]);
      boolean bFound = false;

      profile = m_FatherComponentInst.getProfileInst(asAvailProfileNames[nI]);
      if (profile != null) {
        bFound = true;
        profile.setLabel(m_AdminCtrl.getProfileLabelfromName(sComponentName,
            asAvailProfileNames[nI]));
        alShowProfile.add(profile);
      }

      if (!bFound) {
        profile = new ProfileInst();
        profile.setName(asAvailProfileNames[nI]);
        profile.setLabel(m_AdminCtrl.getProfileLabelfromName(sComponentName,
            asAvailProfileNames[nI]));
        alShowProfile.add(profile);
      }
    }

    return alShowProfile;

  }

  public ProfileInst getProfile(String sProfileId, String sProfileName,
      String sProfileLabel) {
    if (sProfileId != null && !sProfileId.trim().equals("")) {
      return m_AdminCtrl.getProfileInst(sProfileId);
    } else {
      ProfileInst res = new ProfileInst();
      res.setName(sProfileName);
      res.setLabel(sProfileLabel);
      return res;
    }
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
      theGroup = m_AdminCtrl.getGroupById(groupIds.get(nI));
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
    if (labelProfile == null || labelProfile.equals("")) {
      labelProfile = profile;
    }

    sel.resetAll();

    String hostSpaceName = getMultilang().getString("JSPP.manageHomePage");
    sel.setHostSpaceName(hostSpaceName);

    PairObject hostComponentName = null;
    String idFather = getSpaceInstById().getDomainFatherId();
    if (idFather != null && !idFather.equals("0")) {// je suis sur un ss-espace
      SpaceInst spaceFather = getSpaceInstById(idFather);
      hostComponentName = new PairObject(spaceFather.getName() + " > " + getSpaceInstById().
          getName(), null);
    } else {
      hostComponentName = new PairObject(getSpaceInstById().getName(), null);
    }
    sel.setHostComponentName(hostComponentName);

    ResourceLocator generalMessage = GeneralPropertiesManager.
        getGeneralMultilang(getLanguage());
    String compoName = getComponentInst(getManagedInstanceId()).getLabel();
    PairObject[] hostPath =
        { new PairObject(compoName + " > " + labelProfile + " > " + generalMessage.
        getString("GML.selection"), null) };
    sel.setHostPath(hostPath);

    String hostUrl = compoURL + "EffectiveUpdateInstanceProfile";
    if (profileId == null || profileId.equals("")) // creation
    {
      hostUrl = compoURL + "EffectiveCreateInstanceProfile";
    }
    SilverTrace.info("jobStartPagePeas",
        "JobStartPagePeasSessionController.initUserPanelInstanceForGroupsUsers()",
        "root.MSG_GEN_PARAM_VALUE", "compoURL = " + compoURL + " hostSpaceName=" + hostSpaceName +
        " hostComponentName=" + getSpaceInstById().
        getName() + " hostUrlTest=" + hostUrl);
    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(compoURL + "CancelCreateOrUpdateInstanceProfile");

    // SilverTrace.info("jobStartPagePeas","JobStartPagePeasSessionController.initUserPanelInstanceForGroupsUsers()","root.MSG_GEN_PARAM_VALUE","groupes size= "+getAllCurrentUserSpace().size());
    ArrayList<String> users = getManagedProfile().getAllUsers();
    ArrayList<String> groups = getManagedProfile().getAllGroups();
    sel.setSelectedElements(users.toArray(new String[0]));
    sel.setSelectedSets(groups.toArray(new String[0]));
  }

  public String createInstanceProfile() {
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
    setGroupsAndUsers(profileInst, sel.getSelectedSets(), sel.
        getSelectedElements());

    SilverTrace.spy("jobStartPagePeas",
        "JobStartPagePeasSC.createInstanceProfile", "unknown", profileInst.
        getComponentFatherId(), profileInst.getName(), getUserId(),
        SilverTrace.SPY_ACTION_CREATE);

    // Add the profile
    return m_AdminCtrl.addProfileInst(profileInst, getUserId());
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
    setGroupsAndUsers(profile, sel.getSelectedSets(), sel.getSelectedElements());

    SilverTrace.spy("jobStartPagePeas",
        "JobStartPagePeasSC.updateInstanceProfile", "unknown", profile.
        getComponentFatherId(), profile.getName(), getUserId(),
        SilverTrace.SPY_ACTION_UPDATE);

    // mise à jour
    setManagedProfile(profile);

    // Update the profile
    return m_AdminCtrl.updateProfileInst(profile, getUserId());
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

    SilverTrace.spy("jobStartPagePeas",
        "JobStartPagePeasSC.deleteInstanceProfile", "unknown", profile.
        getComponentFatherId(), profile.getName(), getUserId(),
        SilverTrace.SPY_ACTION_DELETE);

    // mise à jour
    setManagedProfile(profile);

    // Update the profile
    return m_AdminCtrl.updateProfileInst(profile, getUserId());
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
      for (int i = 0; i < groups.size(); i++) {
        profile.addGroup(groups.get(i));
      }
    }

    // users
    List<String> users = getManagedProfile().getAllUsers();
    if (users != null) {
      for (int i = 0; i < users.size(); i++) {
        profile.addUser(users.get(i));
      }
    }

    // mise à jour
    setManagedProfile(profile);

    // Update the profile
    m_AdminCtrl.updateProfileInst(profile);
  }

  /**
   * Copy component
   * @param id
   * @throws RemoteException
   */
  public void copyComponent(String id) throws RemoteException {
    ComponentInst componentInst = getComponentInst(id);
    ComponentSelection compoSelect = new ComponentSelection(componentInst);
    SilverTrace.info("jobStartPagePeas",
        "JobStartPagePeasSessionController.copyComponent()",
        "root.MSG_GEN_PARAM_VALUE", "clipboard = " + getClipboardName() + "' count=" +
        getClipboardCount());
    addClipboardSelection((ClipboardSelection) compoSelect);
  }

  /**
   * Paste component(s) copied
   * @throws RemoteException
   * @throws JobStartPagePeasException
   */
  public void pasteComponent() throws RemoteException, JobStartPagePeasException {
    try {
      SilverTrace.info("jobStartPagePeas",
          "JobStartPagePeasSessionController.pasteComponent()",
          "root.MSG_GEN_PARAM_VALUE", "clipboard = " + getClipboardName() + " count=" +
          getClipboardCount());
      Collection<ClipboardSelection> clipObjects = getClipboardSelectedObjects();
      Iterator<ClipboardSelection> clipObjectIterator = clipObjects.iterator();
      while (clipObjectIterator.hasNext()) {
        ClipboardSelection clipObject = clipObjectIterator.next();
        if (clipObject != null) {
          if (clipObject.isDataFlavorSupported(
              ComponentSelection.ComponentDetailFlavor)) {
            ComponentInst compo = (ComponentInst) clipObject.getTransferData(
                ComponentSelection.ComponentDetailFlavor);
            pasteComponent(compo);
          }
        }
      }
    } catch (Exception e) {
      throw new JobStartPagePeasException(
          "JobStartPagePeasSessionController.pasteComponent()",
          SilverpeasRuntimeException.ERROR, "jobStartPagePeas.EX_PASTE_ERROR", e);
    }
    clipboardPasteDone();
  }

  /**
   * Paste component with profiles
   * @param compoInst
   * @throws JobStartPagePeasException
   */
  public void pasteComponent(ComponentInst compoInst) throws
      JobStartPagePeasException {
    ComponentInst newCompo = (ComponentInst) compoInst.clone();
    SpaceInst destinationSpace = getSpaceInstById();

    // Creation
    newCompo.setId("-1");
    newCompo.setDomainFatherId(destinationSpace.getId());
    newCompo.setOrderNum(destinationSpace.getNumComponentInst());
    newCompo.setCreateDate(new Date());
    newCompo.setLanguage(getLanguage());

    // Rename if componentName already exists in the destination space
    String label = renameComponentName(newCompo.getLabel(getLanguage()), destinationSpace.
        getAllComponentsInst());
    newCompo.setLabel(label);

    // Delete profiles
    newCompo.removeAllProfilesInst();

    // Add the component
    String sComponentId = addComponentInst(newCompo);

    // Adding ok
    if (sComponentId != null && sComponentId.length() > 0) {
      setManagedInstanceId(sComponentId);
      // Get profiles from destination Space
      List<SpaceProfileInst> spaceProfiles = destinationSpace.
          getAllSpaceProfilesInst();
      if (spaceProfiles.size() > 1) {
        m_AdminCtrl.setSpaceProfilesToComponent(newCompo, destinationSpace);
      } else // Get Profiles from Component
      {
        List<ProfileInst> compoProfiles = compoInst.getAllProfilesInst();
        Iterator<ProfileInst> itProfIterator = compoProfiles.iterator();
        newCompo.setInheritanceBlocked(true);
        while (itProfIterator.hasNext()) {
          ProfileInst profileInst = itProfIterator.next();

          // Create the profile
          ProfileInst newProfileInst = new ProfileInst();
          newProfileInst.setLabel(profileInst.getLabel());
          newProfileInst.setName(profileInst.getName());
          newProfileInst.setComponentFatherId(getManagedInstanceId());
          newProfileInst.setInherited(false);
          newProfileInst.setDescription(profileInst.getDescription());

          // Set groupIds and userIds
          String[] groupsIds = null;
          String[] usersIds = null;
          if (profileInst.getAllGroups() != null && profileInst.getAllGroups().
              size() > 0) {
            groupsIds = new String[profileInst.getAllGroups().size()];
            profileInst.getAllGroups().toArray(groupsIds);
          }
          if (profileInst.getAllUsers() != null && profileInst.getAllUsers().
              size() > 0) {
            usersIds = new String[profileInst.getAllUsers().size()];
            profileInst.getAllUsers().toArray(usersIds);
          }

          newProfileInst.setGroupsAndUsers(groupsIds, usersIds);

          // Add the profile
          m_AdminCtrl.addProfileInst(newProfileInst, getUserId());
          newCompo.addProfileInst(newProfileInst);
        }
      }
      updateComponentInst(newCompo);
      refreshCurrentSpaceCache();
    }

    // Execute specific paste by the component
    try {
      PasteDetail pasteDetail = new PasteDetail(compoInst.getId(), sComponentId,
          getUserId());
      String componentRootName = URLManager.getComponentNameFromComponentId(compoInst.
          getId());
      String className =
          "com.silverpeas.component." + componentRootName + "." + componentRootName.
          substring(0, 1).toUpperCase() + componentRootName.substring(1) + "Paste";
      if (Class.forName(className).getClass() != null) {
        ComponentPasteInterface componentPaste = (ComponentPasteInterface) Class.
            forName(className).newInstance();
        componentPaste.paste(pasteDetail);
      }
    } catch (Exception e) {
      SilverTrace.warn("jobStartPagePeas",
          "JobStartPagePeasSessionController.pasteComponent()",
          "root.GEN_EXIT_METHOD", e);
    }
  }

  /**
   * Rename component Label if necessary
   * @param name
   * @param listComponents
   * @return
   */
  private String renameComponentName(String label,
      ArrayList<ComponentInst> listComponents) {
    String newComponentLabel = label;
    for (int i = 0; i < listComponents.size(); i++) {
      ComponentInst componentInst = (ComponentInst) listComponents.get(i);
      if (componentInst.getLabel().equals(newComponentLabel)) {
        newComponentLabel = getMultilang().getString("JSPP.CopyOf") + label;
        return renameComponentName(newComponentLabel, listComponents);
      }
    }
    return newComponentLabel;
  }
}
