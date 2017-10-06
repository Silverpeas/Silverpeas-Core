/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.directory.control;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.domain.model.DomainProperties;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.chat.ChatUser;
import org.silverpeas.core.chat.servers.ChatServer;
import org.silverpeas.core.contact.model.CompleteContact;
import org.silverpeas.core.contact.model.ContactPK;
import org.silverpeas.core.contact.service.ContactService;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.form.XmlSearchForm;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateImpl;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.index.indexing.model.FieldDescription;
import org.silverpeas.core.index.search.SearchEngineProvider;
import org.silverpeas.core.index.search.model.MatchingIndexEntry;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.notification.user.client.NotificationManagerException;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationParameters;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.security.session.SessionManagementProvider;
import org.silverpeas.core.socialnetwork.invitation.Invitation;
import org.silverpeas.core.socialnetwork.relationship.RelationShipService;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.util.viewgenerator.html.ImageTag;
import org.silverpeas.web.directory.DirectoryException;
import org.silverpeas.web.directory.model.ContactItem;
import org.silverpeas.web.directory.model.DirectoryItem;
import org.silverpeas.web.directory.model.DirectoryItemList;
import org.silverpeas.web.directory.model.DirectoryUserItem;
import org.silverpeas.web.directory.model.UserFragmentVO;
import org.silverpeas.web.directory.model.UserItem;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

import static org.silverpeas.core.util.WebEncodeHelper.javaStringToHtmlParagraphe;
import static org.silverpeas.core.util.WebEncodeHelper.javaStringToHtmlString;

/**
 * @author Nabil Bensalem
 */
public class DirectorySessionController extends AbstractComponentSessionController {

  private static final int DEFAULT_ELEMENTS_PER_PAGE = 10;
  private static final String CONTEXT_ATTR = "context";
  public static final String VIEW_QUERY = "query";
  public static final String VIEW_ALL = "tous";
  public static final String VIEW_CONNECTED = "connected";
  public static final String SORT_ALPHA = "ALPHA";
  public static final String SORT_NEWEST = "NEWEST";
  public static final String SORT_PERTINENCE = "PERTINENCE";
  /**
   * By default, display all users.
   */
  public static final int DIRECTORY_DEFAULT = 0;
  /**
   * Display only the contacts of the current user.
   */
  public static final int DIRECTORY_MINE = 1;
  /**
   * Display only the contacts that are common to the current user and to another user.
   */
  public static final int DIRECTORY_COMMON = 2;
  /**
   * Display only the contacts of another user.
   */
  public static final int DIRECTORY_OTHER = 3;
  /**
   * Display all the users of a given group.
   */
  public static final int DIRECTORY_GROUP = 4;
  /**
   * Display all the users of a given domain.
   */
  public static final int DIRECTORY_DOMAIN = 5;
  /**
   * Display all the users that can access a given space.
   */
  public static final int DIRECTORY_SPACE = 6;
  private int currentDirectory = DIRECTORY_DEFAULT;
  private String currentView = VIEW_ALL;
  private DirectoryItemList lastAllListUsersCalled;
  // cache for pagination
  private DirectoryItemList lastListUsersCalled;
  private int elementsByPage = DEFAULT_ELEMENTS_PER_PAGE;
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

  // Extra form session objects
  private PublicationTemplate xmlTemplate = null;
  private DataRecord xmlData = null;
  private boolean xmlTemplateLoaded = false;

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

    elementsByPage = getSettings().getInteger("ELEMENTS_PER_PAGE", DEFAULT_ELEMENTS_PER_PAGE);

    relationShipService = RelationShipService.get();
  }

  public int getElementsByPage() {
    return elementsByPage;
  }

  public void setElementsByPage(int nb) {
    elementsByPage = nb;
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
    if (DomainProperties.areDomainsVisibleToAll()) {
      lastAllListUsersCalled = getUsersSorted();
    } else if (DomainProperties.areDomainsNonVisibleToOthers()) {
      lastAllListUsersCalled = getUsersOfCurrentUserDomain();
    } else if (DomainProperties.areDomainsVisibleOnlyToDefaultOne()) {
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
   * get all users corresponding to search request
   * @param queryDescription the search request
   * @param globalSearch true if it's a search outside directory (direct from URL)
   * @throws DirectoryException
   * @see
   */
  public DirectoryItemList getUsersByQuery(QueryDescription queryDescription,
      boolean globalSearch) throws DirectoryException {
    setCurrentView(VIEW_QUERY);
    if (globalSearch) {
      setCurrentDirectory(DIRECTORY_DEFAULT);
    }
    if (!getCurrentSort().equals(SORT_PERTINENCE)) {
      setPreviousSort(getCurrentSort());
    }
    setCurrentSort(SORT_PERTINENCE);
    DirectoryItemList results = new DirectoryItemList();

    try {
      List<MatchingIndexEntry> plainSearchResults = SearchEngineProvider.getSearchEngine().search(
          queryDescription).getEntries();

      if (plainSearchResults != null && !plainSearchResults.isEmpty()) {
        DirectoryItemList allUsers = lastAllListUsersCalled;
        if (globalSearch || currentDirectory == DIRECTORY_DOMAIN) {
          // forcing to get all users to re-init list of visible users
          allUsers = getUsers();
        }
        for (MatchingIndexEntry result : plainSearchResults) {
          DirectoryItem item = getDirectoryItem(allUsers, result);
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

  public void removeUserFromLists(User userToRemove) {
    if (userToRemove != null) {
      List<DirectoryItemList> directoryUserItemLists =
          Arrays.asList(lastAllListUsersCalled, lastListUsersCalled);
      directoryUserItemLists.forEach(userList -> userList.removeIf(
          directoryItem -> !(directoryItem instanceof UserItem &&
              !((UserItem) directoryItem).getUserDetail().getId().equals(userToRemove.getId()))));
    }
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
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
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
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
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
    NotificationMetaData notifMetaData = new NotificationMetaData(priorityId, txtTitle, txtMessage);
    notifMetaData.setSender(getUserId());
    notifMetaData.setSource(ResourceLocator
        .getLocalizationBundle("org.silverpeas.notificationUser.multilang.notificationUserBundle")
        .getString("manualNotification"));
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

    SessionManagement sessionManagement = SessionManagementProvider.getSessionManagement();
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
      template.setAttribute(CONTEXT_ATTR, URLUtil.getApplicationURL());
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
    ChatUser userDetail = ChatUser.fromUser(user.getUserDetail());
    template.setAttribute("user", userDetail);
    if (StringUtil.isDefined(userDetail.getStatus())) {
      template.setAttribute("status", javaStringToHtmlParagraphe(userDetail.getStatus()));
    } else {
      template.setAttribute("status", null);
    }
    template.setAttribute("type", getString("GML.user.type." + user.getAccessLevel()));
    template.setAttribute(CONTEXT_ATTR, URLUtil.getApplicationURL());
    template.setAttribute("notMyself", !user.getOriginalId().equals(getUserId()));
    template.setAttribute("chatEnabled", ChatServer.isEnabled());
    template.setAttribute("aContact", userDetail.isInRelationWith(getUserId()));
    Invitation invitationSent = getUserDetail().getInvitationSentTo(userDetail.getId());
    if (invitationSent != null) {
      template.setAttribute("invitationSent", invitationSent.getId());
    } else {
      template.setAttribute("invitationSent", null);
    }
    Invitation invitationReceived = getUserDetail().getInvitationReceivedFrom(userDetail.getId());
    if (invitationReceived != null) {
      template.setAttribute("invitationReceived",invitationReceived.getId());
    } else {
      template.setAttribute("invitationReceived", null);
    }

    UserFull userFull = getUserFul(user.getOriginalId());
    Map<String, String> extra = new HashMap<String, String>();
    if (userFull != null) {
      extra = userFull.getAllDefinedValues(getLanguage());
    }

    template.setAttribute("extra", extra);

    return new UserFragmentVO(user.getOriginalId(), template.applyFileTemplate("user_" +
        getLanguage()), user.getType());
  }

  private UserFragmentVO getContactFragment(ContactItem contact, SilverpeasTemplate template) {
    ContactPK contactPK = contact.getContact().getPK();
    template.setAttribute("contact", contact.getContact());
    template.setAttribute(CONTEXT_ATTR, URLUtil.getApplicationURL());
    template.setAttribute("url",
        URLUtil.getComponentInstanceURL(contactPK.getInstanceId()) + "ContactExternalView?Id=" +
            contactPK.getId());

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
      imageTag.setSrc(User.DEFAULT_AVATAR_PATH);
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

  private ContactService getContactBm() {
    return ContactService.get();
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

  private DirectoryItem getDirectoryItem(final DirectoryItemList allUsers,
      final MatchingIndexEntry result) {
    String objectId = result.getObjectId();
    String itemId = DirectoryItem.ITEM_TYPE.User.toString() + objectId;
    if ("Contact".equals(result.getObjectType())) {
      itemId = DirectoryItem.ITEM_TYPE.Contact.toString() + objectId;
    }
    return allUsers.getItemByUniqueId(itemId);
  }

  public Form getExtraForm() {
    PublicationTemplate template = getExtraTemplate();
    if (template != null) {
      try {
        Form searchForm = template.getSearchForm();
        if (xmlData != null) {
          searchForm.setData(xmlData);
        } else {
          searchForm.setData(template.getRecordSet().getEmptyRecord());
        }
        return searchForm;
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
    return null;
  }

  private PublicationTemplate getExtraTemplate() {
    if (xmlTemplate == null && !xmlTemplateLoaded) {
      PublicationTemplateManager templateManager = PublicationTemplateManager.getInstance();
      xmlTemplate = templateManager.getDirectoryTemplate();
      xmlTemplateLoaded = true;
    }
    return xmlTemplate;
  }

  private void saveExtraRequest(List<FileItem> items) {
    PublicationTemplateImpl template = (PublicationTemplateImpl) getExtraTemplate();
    if (template != null) {
      // build a dataRecord object storing user's entries
      try {
        RecordTemplate searchTemplate = template.getSearchTemplate();
        DataRecord data = searchTemplate.getEmptyRecord();

        PagesContext context = new PagesContext("useless", "useless", getLanguage(), getUserId());

        XmlSearchForm searchForm = (XmlSearchForm) template.getSearchForm();
        searchForm.update(items, data, context);

        // xmlQuery is in the data object, store it into session
        xmlData = data;
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
  }

  public QueryDescription buildQuery(List<FileItem> items) {
    String query = FileUploadUtil.getParameter(items, "key");
    QueryDescription queryDescription = new QueryDescription(query);
    queryDescription.addComponent("users");
    for (String appId : getContactComponentIds()) {
      queryDescription.addComponent(appId);
    }

    setCurrentQuery(query);
    saveExtraRequest(items);

    buildExtraQuery(queryDescription);
    return queryDescription;
  }

  private void buildExtraQuery(QueryDescription query) {
    PublicationTemplateImpl template = (PublicationTemplateImpl) getExtraTemplate();
    if (template == null) {
      return;
    }
    // build the xmlSubQuery according to the dataRecord object
    String templateFileName = template.getFileName();
    String templateName = templateFileName.substring(0, templateFileName.lastIndexOf("."));
    if (xmlData != null) {
      for (String fieldName : xmlData.getFieldNames()) {
        try {
          Field field = xmlData.getField(fieldName);
          String fieldValue = field.getStringValue();
          if (fieldValue != null && fieldValue.trim().length() > 0) {
            String fieldQuery = fieldValue.trim().replaceAll("##", " AND ");
            query.addFieldQuery(new FieldDescription(templateName + "$$" + fieldName, fieldQuery, getLanguage()));
          }
        } catch (Exception e) {
          SilverLogger.getLogger(this).error(e);
        }
      }
    }
  }

  public void clear() {
    xmlData = null;
  }

}