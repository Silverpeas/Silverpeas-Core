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
package com.silverpeas.directory.control;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.silverpeas.search.SearchEngineFactory;
import org.silverpeas.search.searchEngine.model.MatchingIndexEntry;
import org.silverpeas.search.searchEngine.model.QueryDescription;

import com.silverpeas.directory.DirectoryException;
import com.silverpeas.directory.model.Member;
import com.silverpeas.directory.model.UserFragmentVO;
import com.silverpeas.session.SessionInfo;
import com.silverpeas.session.SessionManagement;
import com.silverpeas.session.SessionManagementFactory;
import com.silverpeas.socialnetwork.relationShip.RelationShipService;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.template.SilverpeasTemplate;
import com.silverpeas.util.template.SilverpeasTemplateFactory;

import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

/**
 * @author Nabil Bensalem
 */
public class DirectorySessionController extends AbstractComponentSessionController {

  private List<UserDetail> lastAlllistUsersCalled;
  private List<UserDetail> lastListUsersCalled; // cache for pagination
  private int elementsByPage = 10;
  private String currentView = "tous";
  public static final int DIRECTORY_DEFAULT = 0; // all users
  public static final int DIRECTORY_MINE = 1; // contacts of online user
  public static final int DIRECTORY_COMMON = 2; // common contacts between online user and another
                                                // user
  public static final int DIRECTORY_OTHER = 3; // contact of another user
  public static final int DIRECTORY_GROUP = 4; // all users of group
  public static final int DIRECTORY_DOMAIN = 5; // all users of domain
  public static final int DIRECTORY_SPACE = 6; // all users of space
  private int currentDirectory = DIRECTORY_DEFAULT;
  private UserDetail commonUserDetail;
  private UserDetail otherUserDetail;
  private Group currentGroup;
  private List<Domain> currentDomains;
  private SpaceInstLight currentSpace;
  private RelationShipService relationShipService;
  private String currentQuery;

  private String initSort = SORT_ALPHA;
  private String currentSort = SORT_ALPHA;
  private String previousSort = SORT_ALPHA;
  public static final String SORT_ALPHA = "ALPHA";
  public static final String SORT_NEWEST = "NEWEST";
  public static final String SORT_PERTINENCE = "PERTINENCE";

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public DirectorySessionController(MainSessionController mainSessionCtrl,
          ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, "org.silverpeas.directory.multilang.DirectoryBundle",
            "org.silverpeas.directory.settings.DirectoryIcons",
            "org.silverpeas.directory.settings.DirectorySettings");

    elementsByPage = getSettings().getInteger("ELEMENTS_PER_PAGE", 10);

    relationShipService = new RelationShipService();
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
    setCurrentDirectory(DIRECTORY_DEFAULT);
    setCurrentQuery(null);
    return getUsers();
  }

  private List<UserDetail> getUsers() {
    switch (GeneralPropertiesManager.getDomainVisibility()) {
      case GeneralPropertiesManager.DVIS_ALL:
        // all users are visible
        lastAlllistUsersCalled = getUsersSorted();
        break;
      case GeneralPropertiesManager.DVIS_EACH:
        // only users of user's domain are visible
        lastAlllistUsersCalled = getUsersOfCurrentUserDomain();
        break;
      case GeneralPropertiesManager.DVIS_ONE:
        // default domain users can see all users
        // users of other domains can see only users of their domain
        String currentUserDomainId = getUserDetail().getDomainId();
        if ("0".equals(currentUserDomainId)) {
          lastAlllistUsersCalled = getUsersSorted();
        } else {
          lastAlllistUsersCalled = getUsersOfCurrentUserDomain();
        }
    }
    setInitialSort(getCurrentSort());
    lastListUsersCalled = lastAlllistUsersCalled;
    return lastAlllistUsersCalled;
  }

  private List<UserDetail> getUsersSorted() {
    if (getCurrentDirectory() == DIRECTORY_DOMAIN) {
      return getUsersOfDomainsSorted();
    } else {
      return getAllUsersSorted();
    }
  }

  private List<UserDetail> getAllUsersSorted() {
    if (SORT_NEWEST.equals(getCurrentSort())) {
      return getOrganisationController().getAllUsersFromNewestToOldest();
    } else {
      return Arrays.asList(getOrganisationController().getAllUsers());
    }
  }

  private List<UserDetail> getUsersOfDomainsSorted() {
    List<String> domainIds = getCurrentDomainIds();
    if (SORT_NEWEST.equals(getCurrentSort())) {
      return getOrganisationController().getUsersOfDomainsFromNewestToOldest(domainIds);
    } else {
      return getOrganisationController().getUsersOfDomains(domainIds);
    }
  }

  private List<String> getCurrentDomainIds() {
    List<String> ids = new ArrayList<String>();
    for (Domain domain : getCurrentDomains()) {
      ids.add(domain.getId());
    }
    return ids;
  }

  private List<UserDetail> getUsersOfCurrentUserDomain() {
    String currentUserDomainId = getUserDetail().getDomainId();
    List<UserDetail> allUsers = getAllUsersSorted();
    List<UserDetail> users = new ArrayList<UserDetail>();
    for (UserDetail var : allUsers) {
      if (currentUserDomainId.equals(var.getDomainId())) {
        users.add(var);
      }
    }
    return users;
  }

  /**
   * get all Users that their Last Name begin with 'Index'
   * @param index:Alphabetical Index like A,B,C,E......
   * @see
   */
  public List<UserDetail> getUsersByIndex(String index) {
    setCurrentView(index);
    setCurrentQuery(null);
    if (getCurrentSort().equals(SORT_PERTINENCE)) {
      setCurrentSort(getPreviousSort());
    }
    lastListUsersCalled = new ArrayList<UserDetail>();
    for (UserDetail varUd : lastAlllistUsersCalled) {
      if (varUd.getLastName().toUpperCase().startsWith(index)) {
        lastListUsersCalled.add(varUd);
      }
    }
    if (getCurrentSort().equals(getInitialSort())) {
      return lastListUsersCalled;
    } else {
      // force results to be sorted cause original list is used
      return sort(getCurrentSort());
    }
  }

  /**
   * get all User that heir lastname or first name Last Name like "Key"
   * @param query the search query
   * @throws DirectoryException
   * @see
   */
  public List<UserDetail> getUsersByQuery(String query) throws DirectoryException {
    setCurrentView("query");
    setCurrentQuery(query);
    if (!getCurrentSort().equals(SORT_PERTINENCE)) {
      setPreviousSort(getCurrentSort());
    }
    setCurrentSort(SORT_PERTINENCE);
    lastListUsersCalled = new ArrayList<UserDetail>();

    QueryDescription queryDescription = new QueryDescription(query);
    queryDescription.addSpaceComponentPair(null, "users");
    try {
      List<MatchingIndexEntry> plainSearchResults = SearchEngineFactory.getSearchEngine().search(
          queryDescription).getEntries();

      for (MatchingIndexEntry result : plainSearchResults) {
        String userId = result.getObjectId();
        for (UserDetail varUd : lastAlllistUsersCalled) {
          if (varUd.getId().equals(userId)) {
            lastListUsersCalled.add(varUd);
          }
        }
      }
    } catch (Exception e) {
      throw new DirectoryException(this.getClass().getSimpleName(), "directory.EX_CANT_SEARCH", e);
    }
    return lastListUsersCalled;

  }

  /**
   * get all User of the Group who has Id="groupId"
   * @param groupId:the ID of group
   * @see
   */
  public List<UserDetail> getAllUsersByGroup(String groupId) {
    setCurrentView("tous");
    setCurrentDirectory(DIRECTORY_GROUP);
    setCurrentQuery(null);
    currentGroup = getOrganisationController().getGroup(groupId);
    lastAlllistUsersCalled = Arrays.asList(getOrganisationController().getAllUsersOfGroup(groupId));
    lastListUsersCalled = lastAlllistUsersCalled;
    return lastAlllistUsersCalled;
  }

  /**
   * get all User "we keep the last list of All users"
   * @see
   */
  public List<UserDetail> getLastListOfAllUsers() {
    setCurrentView("tous");
    setCurrentQuery(null);
    if (getCurrentSort().equals(SORT_PERTINENCE)) {
      setCurrentSort(getPreviousSort());
    }
    if (getCurrentSort().equals(getInitialSort())) {
      lastListUsersCalled = lastAlllistUsersCalled;
    } else {
      // force to sort all users according to current sort
      getUsers();
    }
    return lastListUsersCalled;
  }

  /**
   * get the last list of users called "keep the session"
   * @see
   */
  public List<UserDetail> getLastListOfUsersCalled() {
    return lastListUsersCalled;
  }

  /**
   * return All users of Space who has Id="spaceId"
   * @param spaceId:the ID of Space
   * @see
   */
  public List<UserDetail> getAllUsersBySpace(String spaceId) {
    setCurrentView("tous");
    setCurrentDirectory(DIRECTORY_SPACE);
    setCurrentQuery(null);
    currentSpace = getOrganisationController().getSpaceInstLightById(spaceId);
    List<UserDetail> lus = new ArrayList<UserDetail>();
    String[] componentIds = getOrganisationController().getAllComponentIdsRecur(spaceId);
    for (String componentId : componentIds) {
      fillList(lus, getOrganisationController().getAllUsers(componentId));
    }
    lastAlllistUsersCalled = lus;

    lastListUsersCalled = lastAlllistUsersCalled;
    return lastAlllistUsersCalled;

  }

  public void fillList(List<UserDetail> ol, UserDetail[] nl) {
    for (UserDetail var : nl) {
      if (!ol.contains(var)) {
        ol.add(var);
      }
    }
  }

  /**
   * return All user of Domaine who has Id="domainId"
   * @param domainId:the ID of Domaine
   * @see
   */
  public List<UserDetail> getAllUsersByDomain(String domainId) {
    List<String> domainIds = new ArrayList<String>();
    domainIds.add(domainId);
    return getAllUsersByDomains(domainIds);
  }

  public List<UserDetail> getAllUsersByDomains(List<String> domainIds) {
    setCurrentDirectory(DIRECTORY_DOMAIN);
    setCurrentQuery(null);
    currentDomains = new ArrayList<Domain>();
    for (String domainId : domainIds) {
      currentDomains.add(getOrganisationController().getDomain(domainId));
    }
    return getUsers();
  }

  public List<UserDetail> getAllContactsOfUser(String userId) {
    setCurrentView("tous");
    setCurrentQuery(null);
    if (getUserId().equals(userId)) {
      setCurrentDirectory(DIRECTORY_MINE);
    } else {
      setCurrentDirectory(DIRECTORY_OTHER);
      otherUserDetail = getUserDetail(userId);
    }
    lastAlllistUsersCalled = new ArrayList<UserDetail>();
    try {
      List<String> contactsIds = relationShipService.getMyContactsIds(Integer.parseInt(userId));
      for (String contactId : contactsIds) {
        lastAlllistUsersCalled.add(getOrganisationController().getUserDetail(contactId));
      }
    } catch (SQLException ex) {
      SilverTrace.error("newsFeedService", "NewsFeedService.getMyContactsIds", "", ex);
    }
    lastListUsersCalled = lastAlllistUsersCalled;
    return lastAlllistUsersCalled;
  }

  public List<UserDetail> getCommonContacts(String userId) {
    setCurrentView("tous");
    setCurrentDirectory(DIRECTORY_COMMON);
    commonUserDetail = getUserDetail(userId);
    lastAlllistUsersCalled = new ArrayList<UserDetail>();
    try {
      List<String> contactsIds =
              relationShipService.getAllCommonContactsIds(Integer.parseInt(getUserId()), Integer.
                  parseInt(userId));
      for (String contactId : contactsIds) {
        lastAlllistUsersCalled.add(getOrganisationController().getUserDetail(contactId));
      }
    } catch (SQLException ex) {
      SilverTrace.error("newsFeedService", "NewsFeedService.getMyContactsIds", "", ex);
    }
    lastListUsersCalled = lastAlllistUsersCalled;
    return lastAlllistUsersCalled;
  }

  public UserFull getUserFul(String userId) {
    return getOrganisationController().getUserFull(userId);
  }

  /**
   * @param compoId
   * @param txtTitle
   * @param txtMessage
   * @param selectedUsers
   * @throws NotificationManagerException
   */
  public void sendMessage(String compoId, String txtTitle, String txtMessage,
          UserRecipient[] selectedUsers) throws NotificationManagerException {
    NotificationSender notifSender = new NotificationSender(compoId);
    int notifTypeId = NotificationParameters.ADDRESS_DEFAULT;
    int priorityId = 0;
    SilverTrace.debug("notificationUser", "NotificationUsersessionController.sendMessage()",
            "root.MSG_GEN_PARAM_VALUE", " AVANT CONTROLE priorityId=" + priorityId);
    NotificationMetaData notifMetaData = new NotificationMetaData(priorityId, txtTitle, txtMessage);
    notifMetaData.setSender(getUserId());
    notifMetaData.setSource(getString("manualNotification"));
    notifMetaData.addUserRecipients(selectedUsers);
    notifMetaData.addGroupRecipients(null);
    notifSender.notifyUser(notifTypeId, notifMetaData);
  }

  public void setCurrentView(String currentView) {
    this.currentView = currentView;
  }

  public String getCurrentView() {
    return currentView;
  }

  public List<UserDetail> getConnectedUsers() {
    setCurrentView("connected");
    setCurrentQuery(null);
    if (getCurrentSort().equals(SORT_PERTINENCE)) {
      setCurrentSort(getPreviousSort());
    }
    List<UserDetail> connectedUsers = new ArrayList<UserDetail>();

    SessionManagement sessionManagement =
        SessionManagementFactory.getFactory().getSessionManagement();
    Collection<SessionInfo> sessions =
        sessionManagement.getDistinctConnectedUsersList(getUserDetail());
    for (SessionInfo session : sessions) {
      connectedUsers.add(session.getUserDetail());
    }
    
    if (getCurrentDirectory() != DIRECTORY_DEFAULT) {
      // all connected users must be filtered according to directory scope
      lastListUsersCalled = new ArrayList<UserDetail>();
      for (UserDetail connectedUser : connectedUsers) {
        if (lastAlllistUsersCalled.contains(connectedUser)) {
          lastListUsersCalled.add(connectedUser);
        }
      }
    } else {
      lastListUsersCalled = connectedUsers;
      lastAlllistUsersCalled = connectedUsers;
    }

    return lastListUsersCalled;
  }

  public List<UserFragmentVO> getFragments(List<Member> membersToDisplay) {
    // using StringTemplate to personalize display of members
    List<UserFragmentVO> fragments = new ArrayList<UserFragmentVO>();
    SilverpeasTemplate template = SilverpeasTemplateFactory.createSilverpeasTemplateOnCore("directory");
    for (Member member : membersToDisplay) {
      template.setAttribute("user", member);
      template.setAttribute("type", getString("GML.user.type." + member.getAccessLevel().code()));
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

      fragments.add(new UserFragmentVO(member.getId(), template.applyFileTemplate("user_"
              + getLanguage())));
    }
    return fragments;

  }

  private String getAvatarFragment(Member member) {
    StringBuilder sb = new StringBuilder();
    String webcontext = URLManager.getApplicationURL();
    sb.append("<a href=\"").append(webcontext).append("/Rprofil/jsp/Main?userId=").append(
            member.getId()).append("\">");
    sb.append("<img src=\"").append(webcontext).append(member.getUserDetail().getAvatar()).append(
            "\" alt=\"viewUser\"").append(" class=\"avatar\"/></a>");
    return sb.toString();
  }

  private void setCurrentDirectory(int currentDirectory) {
    this.currentDirectory = currentDirectory;
  }

  public int getCurrentDirectory() {
    return currentDirectory;
  }

  public UserDetail getCommonUserDetail() {
    return commonUserDetail;
  }

  public UserDetail getOtherUserDetail() {
    return otherUserDetail;
  }

  public Group getCurrentGroup() {
    return currentGroup;
  }

  public List<Domain> getCurrentDomains() {
    return currentDomains;
  }

  public SpaceInstLight getCurrentSpace() {
    return currentSpace;
  }

  public void setCurrentQuery(String currentQuery) {
    this.currentQuery = currentQuery;
  }

  public String getCurrentQuery() {
    return currentQuery;
  }

  public String getCurrentSort() {
    return currentSort;
  }

  public void setCurrentSort(String sort) {
    currentSort = sort;
  }

  private String getPreviousSort() {
    return previousSort;
  }

  private void setPreviousSort(String sort) {
    previousSort = sort;
  }

  private String getInitialSort() {
    return initSort;
  }

  private void setInitialSort(String sort) {
    initSort = sort;
  }

  public List<UserDetail> sort(String sort) {
    setCurrentSort(sort);
    if ("tous".equalsIgnoreCase(getCurrentView())) {
      // get users from DB instead of sorting it
      getUsers();
    } else {
      // sort lastListUsersCalled
      if (getCurrentSort().equals(SORT_ALPHA)) {
        Collections.sort(lastListUsersCalled, new UserNameComparator());
      } else {
        Collections.sort(lastListUsersCalled, new UserIdComparator());
      }
    }
    return lastListUsersCalled;
  }

  private class UserNameComparator implements Comparator<UserDetail> {

    @Override
    public int compare(UserDetail o1, UserDetail o2) {
      int compare = o1.getLastName().compareToIgnoreCase(o2.getLastName());
      if (compare != 0) {
        return compare;
      }
      return o1.getFirstName().compareToIgnoreCase(o2.getFirstName());
    }

  }

  /**
   * Used to sort user id from highest to lowest
   */
  private class UserIdComparator implements Comparator<UserDetail> {

    @Override
    public int compare(UserDetail o1, UserDetail o2) {
      return 0 - (Integer.parseInt(o1.getId()) - Integer.parseInt(o2.getId()));
    }

  }

}