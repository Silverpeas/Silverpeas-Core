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
package com.silverpeas.directory.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.silverpeas.directory.DirectoryException;
import com.silverpeas.directory.model.Member;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.SessionInfo;
import com.stratelia.silverpeas.peasCore.SessionManager;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.ProfileInst;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBm;
import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBmHome;
import com.stratelia.webactiv.searchEngine.model.MatchingIndexEntry;
import com.stratelia.webactiv.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;

/**
 * @author Nabil Bensalem
 */
public class DirectorySessionController extends AbstractComponentSessionController {

  private List<UserDetail> lastAlllistUsersCalled;
  private List<UserDetail> lastListUsersCalled; // cache for pagination

  private int elementsByPage = 10;

  private String currentView = "tous";
  private Properties stConfig;

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public DirectorySessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, "com.silverpeas.directory.multilang.DirectoryBundle",
        "com.silverpeas.directory.settings.DirectoryIcons",
        "com.silverpeas.directory.settings.DirectorySettings");

    elementsByPage = Integer.parseInt(getSettings().getString("ELEMENTS_PER_PAGE", "10"));

    stConfig = new Properties();
    stConfig.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR, getSettings().getString(
        "templatePath"));
    stConfig.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR, getSettings().getString(
        "customersTemplatePath"));
  }

  public int getElementsByPage() {
    return elementsByPage;
  }

  /**
   * get All Users
   * @see
   */
  public List<UserDetail> getAllUsers() {
    setCurrentView("tous");
    lastAlllistUsersCalled = Arrays.asList(getOrganizationController().getAllUsers());
    lastListUsersCalled = lastAlllistUsersCalled;
    return lastAlllistUsersCalled;

  }

  /**
   *get all Users that their Last Name begin with 'Index'
   * @param index:Alphabetical Index like A,B,C,E......
   * @see
   */
  public List<UserDetail> getUsersByIndex(String index) {
    setCurrentView(index);
    lastListUsersCalled = new ArrayList<UserDetail>();
    for (UserDetail varUd : lastAlllistUsersCalled) {
      if (varUd.getLastName().toUpperCase().startsWith(index)) {
        lastListUsersCalled.add(varUd);
      }
    }
    return lastListUsersCalled;
  }

  /**
   *get all User that heir lastname or first name Last Name like "Key"
   * @param Key:the key of search
   * @throws DirectoryException
   * @see
   */
  public List<UserDetail> getUsersByQuery(String query) throws DirectoryException {
    setCurrentView("query");
    lastListUsersCalled = new ArrayList<UserDetail>();

    QueryDescription queryDescription = new QueryDescription(query);
    queryDescription.addSpaceComponentPair(null, "users");

    SearchEngineBm searchEngine = getSearchEngineBm();
    try {
      searchEngine.search(queryDescription);

      MatchingIndexEntry[] plainSearchResults =
          searchEngine.getRange(0, searchEngine.getResultLength());

      for (MatchingIndexEntry result : plainSearchResults) {
        String userId = result.getObjectId();
        for (UserDetail varUd : lastAlllistUsersCalled) {
          if (varUd.getId().equals(userId)) {
            lastListUsersCalled.add(varUd);
          }
        }
      }
    } catch (Exception e) {
      throw new DirectoryException(this.getClass().getSimpleName(), "directory.EX_CANT_SEARCH",
          e);
    }

    return lastListUsersCalled;

  }

  /**
   *get all User of the Group who has Id="groupId"
   * @param groupId:the ID of group
   * @see
   */
  public List<UserDetail> getAllUsersByGroup(String groupId) {
    setCurrentView("tous");
    lastAlllistUsersCalled = Arrays.asList(getOrganizationController().getAllUsersOfGroup(groupId));
    lastListUsersCalled = lastAlllistUsersCalled;
    return lastAlllistUsersCalled;
  }

  /**
   *get all User "we keep the last list of All users"
   * @see
   */
  public List<UserDetail> getLastListOfAllUsers() {
    setCurrentView("tous");
    lastListUsersCalled = lastAlllistUsersCalled;
    return lastAlllistUsersCalled;
  }

  /**
   *get the last list of users colled " keep the session"
   * @see
   */
  public List<UserDetail> getLastListOfUsersCallded() {
    return lastListUsersCalled;
  }

  /**
   *return All users of Space who has Id="spaceId"
   * @param spaceId:the ID of Space
   * @see
   */
  public List<UserDetail> getAllUsersBySpace(String spaceId) {
    setCurrentView("tous");
    List<String> lus = new ArrayList<String>();
    lus = getAllUsersBySpace(lus, spaceId);
    lastAlllistUsersCalled =
        Arrays.asList(getOrganizationController().getUserDetails(lus.toArray(new String[lus.
            size()])));
    lastListUsersCalled = lastAlllistUsersCalled;
    return lastAlllistUsersCalled;

  }

  private List<String> getAllUsersBySpace(List<String> lus, String spaceId) {
    SpaceInst si = getOrganizationController().getSpaceInstById(spaceId);
    for (String ChildSpaceVar : si.getSubSpaceIds()) {
      getAllUsersBySpace(lus, ChildSpaceVar);
    }
    for (ComponentInst ciVar : si.getAllComponentsInst()) {
      for (ProfileInst piVar : ciVar.getAllProfilesInst()) {
        lus = fillList(lus, piVar.getAllUsers());

      }
    }
    return lus;
  }

  public List<String> fillList(List<String> ol, List<String> nl) {

    for (String var : nl) {
      if (!ol.contains(var)) {
        ol.add(var);
      }
    }
    return ol;
  }

  /**
   *return All user of Domaine who has Id="domainId"
   * @param domainId:the ID of Domaine
   * @see
   */
  public List<UserDetail> getAllUsersByDomain(String domainId) {
    getAllUsers();// recuperer tous les users
    lastListUsersCalled = new ArrayList<UserDetail>();
    for (UserDetail var : lastAlllistUsersCalled) {

      if (var.getDomainId() == null ? domainId == null : var.getDomainId().equals(domainId)) {
        lastListUsersCalled.add(var);
      }
    }
    lastAlllistUsersCalled = lastListUsersCalled;
    return lastAlllistUsersCalled;

  }

  public UserFull getUserFul(String userId) {
    return this.getOrganizationController().getUserFull(userId);
  }

  /**
   * Method declaration
   * @param notificationId
   * @param priorityId
   * @param txtTitle
   * @param txtMessage
   * @param selectedUsers
   * @param selectedGroups
   * @throws NotificationManagerException
   * @see
   */
  public void sendMessage(String compoId,
      String txtTitle, String txtMessage,
      String[] selectedUsers)
      throws NotificationManagerException {
    NotificationSender notifSender = new NotificationSender(compoId);
    int notifTypeId = NotificationParameters.ADDRESS_DEFAULT;
    int priorityId = 0;
    SilverTrace.debug("notificationUser",
        "NotificationUsersessionController.sendMessage()",
        "root.MSG_GEN_PARAM_VALUE", "  AVANT CONTROLE priorityId="
            + priorityId);
    NotificationMetaData notifMetaData = new NotificationMetaData(
        priorityId, txtTitle, txtMessage);
    notifMetaData.setSender(getUserId());
    notifMetaData.setSource(getString("manualNotification"));
    notifMetaData.addUserRecipients(selectedUsers);
    notifMetaData.addGroupRecipients(null);
    notifSender.notifyUser(notifTypeId, notifMetaData);
  }

  public String getPhoto(String filename) {
    return getUserDetail().getAvatarFileName();
  }

  public void setCurrentView(String currentView) {
    this.currentView = currentView;
  }

  public String getCurrentView() {
    return currentView;
  }

  public List<UserDetail> getConnectedUsers() {
    setCurrentView("connected");
    List<UserDetail> connectedUsers = new ArrayList<UserDetail>();

    Collection<SessionInfo> sessions = SessionManager.getInstance().getDistinctConnectedUsersList();
    for (SessionInfo session : sessions) {
      connectedUsers.add(session.m_User);
    }

    ArrayList<UserDetail> users = new ArrayList<UserDetail>(lastAlllistUsersCalled);
    users.retainAll(connectedUsers);
    lastListUsersCalled = users;
    return lastListUsersCalled;
  }

  public List<String> getFragments(List<Member> membersToDisplay) {
    // using StringTemplate to personalize display of members
    List<String> fragments = new ArrayList<String>();
    SilverpeasTemplate template = SilverpeasTemplateFactory.createSilverpeasTemplate(stConfig);
    for (Member member : membersToDisplay) {
      template.setAttribute("user", member);
      template.setAttribute("type", getString("GML.user.type." + member.getAccessLevel()));
      template.setAttribute("avatar", getAvatarFragment(member));
      template.setAttribute("context", URLManager.getApplicationURL());
      template.setAttribute("notMyself", !member.getId().equals(getUserId()));
      template.setAttribute("notAContact", !member.isRelationOrInvitation(getUserId()));

      UserFull userFull = getUserFul(member.getId());
      HashMap<String, String> extra = new HashMap<String, String>();
      if (userFull != null) {
        Set<String> keys = userFull.getSpecificDetails().keySet();
        // put only defined values
        for (String key : keys) {
          String value = userFull.getValue(key);
          if (StringUtil.isDefined(value)) {
            extra.put(key, value);
          }
        }
      }
      template.setAttribute("extra", extra);
      fragments.add(template.applyFileTemplate("user_" + getLanguage()));
    }
    return fragments;

  }

  private String getAvatarFragment(Member member) {
    StringBuilder sb = new StringBuilder();
    String context = URLManager.getApplicationURL();
    sb.append("<a href=\"").append(context).append("/Rprofil/jsp/Main?userId=").append(
        member.getId()).append("\">");
    String avatarClass = "avatar";
    if (!member.haveAvatar()) {
      avatarClass = "defaultAvatar";
    }
    sb.append("<img src=\"").append(context).append(member.getProfilPhoto()).append(
        "\" alt=\"viewUser\"");
    sb.append("class=\"").append(avatarClass).append("\"/></a>");
    return sb.toString();
  }

  private SearchEngineBm getSearchEngineBm() throws DirectoryException {
    try {
      SearchEngineBmHome home = (SearchEngineBmHome) EJBUtilitaire.getEJBObjectRef(
              JNDINames.SEARCHBM_EJBHOME, SearchEngineBmHome.class);
      return home.create();
    } catch (Exception e) {
      throw new DirectoryException(this.getClass().getSimpleName(), "root.EX_SEARCH_ENGINE_FAILED",
          e);
    }
  }

}