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

import com.silverpeas.directory.DirectoryException;
import com.silverpeas.directory.model.ContactItem;
import com.silverpeas.directory.model.DirectoryItem;
import com.silverpeas.directory.model.DirectoryItemList;
import com.silverpeas.directory.model.DirectoryUserItem;
import com.silverpeas.directory.model.UserFragmentVO;
import com.silverpeas.directory.model.UserItem;
import com.silverpeas.session.SessionInfo;
import com.silverpeas.session.SessionManagement;
import com.silverpeas.session.SessionManagementFactory;
import com.silverpeas.socialnetwork.relationShip.RelationShipService;
import com.silverpeas.util.EncodeHelper;
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
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.contact.control.ContactBm;
import com.stratelia.webactiv.util.contact.model.CompleteContact;
import com.stratelia.webactiv.util.contact.model.ContactPK;
import com.stratelia.webactiv.util.viewGenerator.html.ImageTag;
import org.silverpeas.search.SearchEngineFactory;
import org.silverpeas.search.searchEngine.model.MatchingIndexEntry;
import org.silverpeas.search.searchEngine.model.QueryDescription;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.silverpeas.util.EncodeHelper.javaStringToHtmlString;

/**
 * @author Nabil Bensalem
 */
public class DirectorySessionController extends AbstractComponentSessionController {

  private DirectoryItemList lastAllListUsersCalled;
  private DirectoryItemList lastListUsersCalled; // cache for pagination
  private int elementsByPage = 10;
  public static final String VIEW_ALL = "tous";
  public static final String VIEW_CONNECTED = "connected";
  public static final String VIEW_QUERY = "query";
  private String currentView = VIEW_ALL;
  public static final int DIRECTORY_DEFAULT = 0; // all users
  public static final int DIRECTORY_MINE = 1; // contacts of online user
  public static final int DIRECTORY_COMMON = 2; // common contacts between online user and another user
  public static final int DIRECTORY_OTHER = 3; // contact of another user
  public static final int DIRECTORY_GROUP = 4; // all users of group
  public static final int DIRECTORY_DOMAIN = 5; // all users of domain
  public static final int DIRECTORY_SPACE = 6; // all users of space
  private int currentDirectory = DIRECTORY_DEFAULT;
  private UserDetail commonUserDetail;
  private UserDetail otherUserDetail;
  private List<Group> currentGroups;
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
  public DirectoryItemList getAllUsers() {
    setCurrentView(VIEW_ALL);
    setCurrentDirectory(DIRECTORY_DEFAULT);
    setCurrentQuery(null);
    return getUsers();
  }

  private DirectoryItemList getUsers() {
    switch (GeneralPropertiesManager.getDomainVisibility()) {
      case GeneralPropertiesManager.DVIS_ALL:
        // all users are visible
        lastAllListUsersCalled = getUsersSorted();
        break;
      case GeneralPropertiesManager.DVIS_EACH:
        // only users of user's domain are visible
        lastAllListUsersCalled = getUsersOfCurrentUserDomain();
        break;
      case GeneralPropertiesManager.DVIS_ONE:
        // default domain users can see all users
        // users of other domains can see only users of their domain
        String currentUserDomainId = getUserDetail().getDomainId();
        if ("0".equals(currentUserDomainId)) {
          lastAllListUsersCalled = getUsersSorted();
        } else {
          lastAllListUsersCalled = getUsersOfCurrentUserDomain();
        }
    }
    
    //add contacts
    DirectoryItemList contacts = getContacts();
    if (!contacts.isEmpty()) {
      lastAllListUsersCalled.addAll(contacts);
      lastListUsersCalled = lastAllListUsersCalled;
      sort(getCurrentSort());
    }
    
    setInitialSort(getCurrentSort());
    lastListUsersCalled = lastAllListUsersCalled;
    return lastAllListUsersCalled;
  }

  private DirectoryItemList getUsersSorted() {
    if (getCurrentDirectory() == DIRECTORY_DOMAIN) {
      return getUsersOfDomainsSorted();
    } else {
      return getAllUsersSorted();
    }
  }

  private DirectoryItemList getAllUsersSorted() {
    if (SORT_NEWEST.equals(getCurrentSort())) {
      return new DirectoryItemList(getOrganisationController().getAllUsersFromNewestToOldest());
    } else {
      return new DirectoryItemList(getOrganisationController().getAllUsers());
    }
  }

  private DirectoryItemList getUsersOfDomainsSorted() {
    List<String> domainIds = getCurrentDomainIds();
    if (SORT_NEWEST.equals(getCurrentSort())) {
      return new DirectoryItemList(
          getOrganisationController().getUsersOfDomainsFromNewestToOldest(domainIds));
    } else {
      return new DirectoryItemList(getOrganisationController().getUsersOfDomains(domainIds));
    }
  }

  private List<String> getCurrentDomainIds() {
    List<String> ids = new ArrayList<String>();
    for (Domain domain : getCurrentDomains()) {
      ids.add(domain.getId());
    }
    return ids;
  }

  public void setCurrentDomains(List<String> domainIds) {
    currentDomains = new ArrayList<Domain>();
    for (String domainId : domainIds) {
      currentDomains.add(getOrganisationController().getDomain(domainId));
    }
    setCurrentDirectory(DIRECTORY_DOMAIN);
  }

  private DirectoryItemList getUsersOfCurrentUserDomain() {
    String currentUserDomainId = getUserDetail().getDomainId();
    DirectoryItemList allItems = getAllUsersSorted();
    DirectoryItemList userItems = new DirectoryItemList();
    for (DirectoryItem item : allItems) {
      if (item instanceof DirectoryUserItem) {
        DirectoryUserItem userItem = (DirectoryUserItem) item;
        if (currentUserDomainId.equals(userItem.getDomainId())) {
          userItems.add(userItem);
        }
      }
    }
    return userItems;
  }

  /**
   * get all Users that their Last Name begin with 'Index'
   * @param index:Alphabetical Index like A,B,C,E......
   * @see
   */
  public DirectoryItemList getUsersByIndex(String index) {
    setCurrentView(index);
    setCurrentQuery(null);
    if (getCurrentSort().equals(SORT_PERTINENCE)) {
      setCurrentSort(getPreviousSort());
    }
    lastListUsersCalled = new DirectoryItemList();
    for (DirectoryItem varUd : lastAllListUsersCalled) {
      if (varUd.getLastName().toUpperCase().startsWith(index)) {
        lastListUsersCalled.add(varUd);
      }
    }
    if (!getCurrentSort().equals(getInitialSort())) {
      // force results to be sorted cause original list is used
      sort(getCurrentSort());
    }
    return lastListUsersCalled;
  }

  /**
   * get all User that their lastname or first name like "Key"
   * @param query the search query
   * @param globalSearch true if it's a search outside directory (direct from URL)
   * @throws DirectoryException
   * @see
   */
  public DirectoryItemList getUsersByQuery(String query, boolean globalSearch)
      throws DirectoryException {
    setCurrentView(VIEW_QUERY);
    setCurrentQuery(query);
    if (globalSearch) {
      setCurrentDirectory(DIRECTORY_DEFAULT);
    }
    if (!getCurrentSort().equals(SORT_PERTINENCE)) {
      setPreviousSort(getCurrentSort());
    }
    setCurrentSort(SORT_PERTINENCE);
    DirectoryItemList results = new DirectoryItemList();

    QueryDescription queryDescription = new QueryDescription(query);
    queryDescription.addSpaceComponentPair(null, "users");
    for (String appId : getContactComponentIds()) {
      queryDescription.addComponent(appId);
    }
    
    try {
      List<MatchingIndexEntry> plainSearchResults = SearchEngineFactory.getSearchEngine().search(
          queryDescription).getEntries();
      
      if (plainSearchResults != null && !plainSearchResults.isEmpty()) {
        DirectoryItemList allUsers = lastAllListUsersCalled;
        if (globalSearch || currentDirectory == DIRECTORY_DOMAIN) {
          // forcing to get all users to re-init list of visible users
          allUsers = getUsers();
        }
        for (MatchingIndexEntry result : plainSearchResults) {
          String objectId = result.getObjectId();
          String itemId = DirectoryItem.ITEM_TYPE.User.toString()+objectId;
          if ("Contact".equals(result.getObjectType())) {
            itemId = DirectoryItem.ITEM_TYPE.Contact.toString()+objectId;
          }
          DirectoryItem item = allUsers.getItemByUniqueId(itemId);
          if (item != null) {
            results.add(item);
          }
        }
      }
    } catch (Exception e) {
      throw new DirectoryException(this.getClass().getSimpleName(), "directory.EX_CANT_SEARCH", e);
    }
    lastListUsersCalled = results;
    return lastListUsersCalled;

  }

  /**
   * get all User of the Group who has Id="groupId"
   * @param groupId:the ID of group
   * @see
   */
  public DirectoryItemList getAllUsersByGroup(String groupId) {
    setCurrentView(VIEW_ALL);
    setCurrentDirectory(DIRECTORY_GROUP);
    setCurrentQuery(null);
    currentGroups = new ArrayList<Group>();
    currentGroups.add(getOrganisationController().getGroup(groupId));
    lastAllListUsersCalled = new DirectoryItemList(getOrganisationController().getAllUsersOfGroup(groupId));
    lastListUsersCalled = lastAllListUsersCalled;
    return lastAllListUsersCalled;
  }

  /**
   * get all Users of the Groups which Id is in "groupIds"
   * @param groupIds:a list of groups' ids
   * @see
   */
  public DirectoryItemList getAllUsersByGroups(List<String> groupIds) {
    setCurrentView(VIEW_ALL);
    setCurrentDirectory(DIRECTORY_GROUP);
    setCurrentQuery(null);

    DirectoryItemList tmpList = new DirectoryItemList();

    currentGroups = new ArrayList<Group>();
    for (String groupId : groupIds) {
      mergeUsersIntoDirectoryItemList(getOrganisationController().getAllUsersOfGroup(groupId),
          tmpList);
      currentGroups.add(getOrganisationController().getGroup(groupId));
    }

    lastAllListUsersCalled = tmpList;

    lastListUsersCalled = lastAllListUsersCalled;
    return lastAllListUsersCalled;

  }

  /**
   * get all User "we keep the last list of All users"
   * @see
   */
  public DirectoryItemList getLastListOfAllUsers() {
    setCurrentView(VIEW_ALL);
    setCurrentQuery(null);
    if (getCurrentSort().equals(SORT_PERTINENCE)) {
      setCurrentSort(getPreviousSort());
    }
    
    lastListUsersCalled = lastAllListUsersCalled;
    return lastListUsersCalled;
  }

  /**
   * get the last list of users called "keep the session"
   * @see
   */
  public DirectoryItemList getLastListOfUsersCalled() {
    return lastListUsersCalled;
  }

  /**
   * return All users of Space who has Id="spaceId"
   * @param spaceId:the ID of Space
   * @see
   */
  public DirectoryItemList getAllUsersBySpace(String spaceId) {
    setCurrentView(VIEW_ALL);
    setCurrentDirectory(DIRECTORY_SPACE);
    setCurrentQuery(null);
    currentSpace = getOrganisationController().getSpaceInstLightById(spaceId);
    DirectoryItemList lus = new DirectoryItemList();
    String[] componentIds = getOrganisationController().getAllComponentIdsRecur(spaceId);
    for (String componentId : componentIds) {
      mergeUsersIntoDirectoryItemList(getOrganisationController().getAllUsers(componentId), lus);
    }

    lastAllListUsersCalled = lus;
    lastListUsersCalled = lastAllListUsersCalled;

    // Sorting list before returning it
    sort(getCurrentSort());
    return lastAllListUsersCalled;

  }

  /**
   * Merges given user list into the specified directory list of items.
   * For each given user, if no associated user item exists into directoryItems it is added to the
   * directoryItems. If it does already exist, nothing is done.
   * @param users the users to add into directoryItems.
   * @param directoryItems the list of directory items that will be filled.
   */
  public void mergeUsersIntoDirectoryItemList(UserDetail[] users,
      DirectoryItemList directoryItems) {
    for (UserDetail var : users) {
      if (!directoryItems.contains(var)) {
        directoryItems.add(var);
      }
    }
  }

  /**
   * return All users of current domains
   */
  public DirectoryItemList getAllUsersByDomains() {
    setCurrentQuery(null);
    return getUsers();
  }

  public DirectoryItemList getAllContactsOfUser(String userId) {
    setCurrentView(VIEW_ALL);
    setCurrentQuery(null);
    if (getUserId().equals(userId)) {
      setCurrentDirectory(DIRECTORY_MINE);
    } else {
      setCurrentDirectory(DIRECTORY_OTHER);
      otherUserDetail = getUserDetail(userId);
    }
    lastAllListUsersCalled = new DirectoryItemList();
    try {
      List<String> contactsIds = relationShipService.getMyContactsIds(Integer.parseInt(userId));
      for (String contactId : contactsIds) {
        lastAllListUsersCalled.add(new UserItem(getOrganisationController().getUserDetail(contactId)));
      }
    } catch (SQLException ex) {
      SilverTrace.error("directory", "DirectorySessionController.getAllContactsOfUser", "", ex);
    }
    lastListUsersCalled = lastAllListUsersCalled;
    return lastAllListUsersCalled;
  }

  public DirectoryItemList getCommonContacts(String userId) {
    setCurrentView(VIEW_ALL);
    setCurrentDirectory(DIRECTORY_COMMON);
    commonUserDetail = getUserDetail(userId);
    lastAllListUsersCalled = new DirectoryItemList();
    try {
      List<String> contactsIds = relationShipService.getAllCommonContactsIds(Integer.parseInt(
          getUserId()), Integer.parseInt(userId));
      for (String contactId : contactsIds) {
        lastAllListUsersCalled.add(new UserItem(getOrganisationController().getUserDetail(contactId)));
      }
    } catch (SQLException ex) {
      SilverTrace.error("directory", "DirectorySessionController.getCommonContacts", "", ex);
    }
    lastListUsersCalled = lastAllListUsersCalled;
    return lastAllListUsersCalled;
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
    SilverTrace.debug("directory", "DirectorySessionController.sendMessage()",
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

  public DirectoryItemList getConnectedUsers() {
    setCurrentView(VIEW_CONNECTED);
    setCurrentQuery(null);
    if (getCurrentSort().equals(SORT_PERTINENCE)) {
      setCurrentSort(getPreviousSort());
    }
    DirectoryItemList connectedUsers = new DirectoryItemList();

    SessionManagement sessionManagement = SessionManagementFactory.getFactory().
        getSessionManagement();
    Collection<SessionInfo> sessions = sessionManagement.getDistinctConnectedUsersList(
        getUserDetail());
    for (SessionInfo session : sessions) {
      connectedUsers.add(session.getUserDetail());
    }
    
    sort(connectedUsers);
    
    if (getCurrentDirectory() != DIRECTORY_DEFAULT) {
      // all connected users must be filtered according to directory scope
      lastListUsersCalled = new DirectoryItemList();
      for (DirectoryItem connectedUser : connectedUsers) {
        if (lastAllListUsersCalled.contains(connectedUser)) {
          lastListUsersCalled.add(connectedUser);
        }
      }
    } else {
      lastListUsersCalled = connectedUsers;
    }

    return lastListUsersCalled;
  }

  public List<UserFragmentVO> getFragments(DirectoryItemList itemsToDisplay) {
    // using StringTemplate to personalize display of members
    List<UserFragmentVO> fragments = new ArrayList<UserFragmentVO>();
    SilverpeasTemplate template = SilverpeasTemplateFactory.createSilverpeasTemplateOnCore(
        "directory");
    for (DirectoryItem item : itemsToDisplay) {
      template.setAttribute("mail",
          StringUtil.isDefined(item.getMail()) ? javaStringToHtmlString(item.getMail()) : null);
      template.setAttribute("phone", javaStringToHtmlString(item.getPhone()));
      template.setAttribute("fax", javaStringToHtmlString(item.getFax()));
      template.setAttribute("avatar", getAvatarFragment(item));
      template.setAttribute("context", URLManager.getApplicationURL());
      if (item instanceof UserItem) {
        UserItem user = (UserItem) item;  
        fragments.add(getUserFragment(user, template));
      } else if (item instanceof ContactItem) {
        ContactItem contact = (ContactItem) item;
        fragments.add(getContactFragment(contact, template));
      }
    }
    return fragments;
  }
  
  private UserFragmentVO getUserFragment(UserItem user, SilverpeasTemplate template) {
    template.setAttribute("user", user.getUserDetail());
    if (StringUtil.isDefined(user.getUserDetail().getStatus())) {
      template.setAttribute("status", javaStringToHtmlString(user.getUserDetail().getStatus()));
    } else {
      template.setAttribute("status", null);
    }
    template.setAttribute("type", getString("GML.user.type." + user.getAccessLevel()));
    template.setAttribute("context", URLManager.getApplicationURL());
    template.setAttribute("notMyself", !user.getOriginalId().equals(getUserId()));
    template.setAttribute("notAContact", !user.getUserDetail().isInRelationWithOrInvitedBy(
        getUserId()));

    UserFull userFull = getUserFul(user.getOriginalId());
    HashMap<String, String> extra = new HashMap<String, String>();
    if (userFull != null) {
      Set<String> keys = userFull.getSpecificDetails().keySet();
      // put only defined values
      for (String key : keys) {
        String value = userFull.getValue(key);
        if (StringUtil.isDefined(value)) {
          extra.put(key, javaStringToHtmlString(value));
        }
      }
    }
    template.setAttribute("extra", extra);
    
    return new UserFragmentVO(user.getOriginalId(), template.applyFileTemplate("user_" +
        getLanguage()), user.getType());
  }
  
  private UserFragmentVO getContactFragment(ContactItem contact, SilverpeasTemplate template) {
    ContactPK pk = contact.getContact().getPK();
    template.setAttribute("contact", contact.getContact());
    template.setAttribute("context", URLManager.getApplicationURL());
    template.setAttribute("url", URLManager.getComponentInstanceURL(pk.getInstanceId())+"ContactExternalView?Id="+pk.getId());
    
    CompleteContact completeContact = (CompleteContact) contact.getContact();
    Map<String, String> extra = completeContact.getFormValues(getLanguage(), true);
    
    template.setAttribute("extra", extra);
    
    return new UserFragmentVO(contact.getOriginalId(), template.applyFileTemplate("contact_" +
        getLanguage()), contact.getType());
  }

  private String getAvatarFragment(DirectoryItem item) {
    ImageTag imageTag = new ImageTag();
    imageTag.setType("avatar.profil");
    imageTag.setAlt("viewUser");
    imageTag.setCss("avatar");
    
    if (item instanceof UserItem) {
      UserItem user = (UserItem) item;
      imageTag.setSrc(user.getAvatar());
    } else {
      imageTag.setSrc("/directory/jsp/icons/avatar.png");
    }
    
    return imageTag.generateHtml();
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

  public List<Group> getCurrentGroups() {
    return currentGroups;
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

  public void sort(String sort) {
    setCurrentSort(sort);
    sort(lastAllListUsersCalled);
    sort(lastListUsersCalled);
  }
  
  private void sort(DirectoryItemList list) {
    if (getCurrentSort().equals(SORT_ALPHA)) {
      Collections.sort(list);
    } else if (getCurrentSort().equals(SORT_NEWEST)) {
      Collections.sort(list, new CreationDateComparator());
    }
  }

  /**
   * Used to sort user id from highest to lowest
   */
  private class CreationDateComparator implements Comparator<DirectoryItem> {

    @Override
    public int compare(DirectoryItem o1, DirectoryItem o2) {
      return getSureCreationDate(o2).compareTo(getSureCreationDate(o1));
    }
    
    private Date getSureCreationDate(DirectoryItem item) {
      if (item.getCreationDate() != null) {
        return item.getCreationDate();
      }
      try {
        return DateUtil.parse("1970/01/01");
      } catch (ParseException e) {
        return new Date();
      }
    }

  }
  
  private ContactBm getContactBm() {
    return EJBUtilitaire.getEJBObjectRef(JNDINames.CONTACTBM_EJBHOME, ContactBm.class);
  }

  private List<String> getContactComponentIds() {
    String[] appIds =
        getOrganisationController().getComponentIdsForUser(getUserId(), "yellowpages");
    List<String> result = new ArrayList<String>();
    for (String appId : appIds) {
      String param =
          getOrganisationController().getComponentParameterValue(appId, "displayedInDirectory");
      if (StringUtil.getBooleanValue(param)) {
        result.add(appId);
      }
    }
    return result;
  }

  /**
   * Gets contacts which does not correspond to an explicit user account.
   * @return the list of complete contact which are not of USER type.
   */
  private DirectoryItemList getContacts() {
    DirectoryItemList items = new DirectoryItemList();
    for (String componentId : getContactComponentIds()) {
      List<CompleteContact> componentContacts = getContactBm().getVisibleContacts(componentId);
      for (CompleteContact completeContact : componentContacts) {
        if (StringUtil.isNotDefined(completeContact.getUserId())) {
          items.add(completeContact);
        }
      }
    }
    return items;
  }
}