/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.domain.model.DomainProperties;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
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
import org.silverpeas.core.notification.NotificationException;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.notification.user.client.constant.BuiltInNotifAddress;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.security.session.SessionManagementProvider;
import org.silverpeas.core.socialnetwork.invitation.Invitation;
import org.silverpeas.core.socialnetwork.relationship.RelationShipService;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SilverpeasList;
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
import org.silverpeas.web.directory.model.DirectorySource;
import org.silverpeas.web.directory.model.UserFragmentVO;
import org.silverpeas.web.directory.model.UserItem;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;

import static org.silverpeas.core.util.WebEncodeHelper.javaStringToHtmlParagraphe;
import static org.silverpeas.core.util.WebEncodeHelper.javaStringToHtmlString;

/**
 * @author Nabil Bensalem
 */
public class DirectorySessionController extends AbstractComponentSessionController {

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

  // Display only contacts of a 'yellowPages' component from a global directory
  public static final int DIRECTORY_CONTACTS = 7;

  // Display contacts from a 'yellowPages' component
  public static final int DIRECTORY_COMPONENT = 8;

  private static final int DEFAULT_ELEMENTS_PER_PAGE = 10;
  private static final String CONTEXT_ATTR = "context";
  private int currentDirectory = DIRECTORY_DEFAULT;
  private String currentView = VIEW_ALL;
  private DirectoryItemList lastAllListUsersCalled;
  // cache for pagination
  private DirectoryItemList lastListUsersCalled;
  private UserDetail commonUserDetail;
  private UserDetail otherUserDetail;
  private List<Group> currentGroups;
  private List<Domain> currentDomains;
  private SpaceInstLight currentSpace;
  private SilverpeasComponentInstance currentComponent;
  private RelationShipService relationShipService;
  private String currentQuery;
  private String initSort = SORT_ALPHA;
  private String currentSort = SORT_ALPHA;
  private String previousSort = SORT_ALPHA;
  // Extra form session objects
  private PublicationTemplate xmlTemplate = null;
  private DataRecord xmlData = null;
  private boolean xmlTemplateLoaded = false;
  PagesContext extraFormContext =
      new PagesContext("useless", "useless", getLanguage(), getUserId());

  private SilverpeasTemplate template;
  private PaginationPage memberPage;

  private List<DirectorySource> directorySources = new ArrayList<>();
  private boolean doNotUseContactsComponents = false;

  private final Function<DirectoryItem, UserFragmentVO> asFragment = item -> {
    SilverpeasTemplate fragmentTemplate = getFragmentTemplate();
    fragmentTemplate.setAttribute("mail",
        StringUtil.isDefined(item.getMail()) ? javaStringToHtmlString(item.getMail()) : null);
    fragmentTemplate.setAttribute("phone", javaStringToHtmlString(item.getPhone()));
    fragmentTemplate.setAttribute("fax", javaStringToHtmlString(item.getFax()));
    fragmentTemplate.setAttribute("avatar", getAvatarFragment(item));
    fragmentTemplate.setAttribute(CONTEXT_ATTR, URLUtil.getApplicationURL());
    if (item instanceof UserItem) {
      UserItem user = (UserItem) item;
      return getUserFragment(user, fragmentTemplate);
    } else if (item instanceof ContactItem) {
      ContactItem contact = (ContactItem) item;
      return getContactFragment(contact, fragmentTemplate);
    }
    return null;
  };

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   *
   */
  public DirectorySessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, "org.silverpeas.directory.multilang.DirectoryBundle",
        "org.silverpeas.directory.settings.DirectoryIcons",
        "org.silverpeas.directory.settings.DirectorySettings");

    memberPage = new PaginationPage(1,
        getSettings().getInteger("ELEMENTS_PER_PAGE", DEFAULT_ELEMENTS_PER_PAGE));

    relationShipService = RelationShipService.get();
  }

  public PaginationPage getMemberPage() {
    return memberPage;
  }

  public void setMemberPage(final PaginationPage memberPage) {
    this.memberPage = memberPage;
  }

  /**
   * get All Users
   *
   */
  public DirectoryItemList getAllUsers() {
    setCurrentView(VIEW_ALL);
    setCurrentDirectory(DIRECTORY_DEFAULT);
    setCurrentQuery(null);
    setCurrentComponent(null);
    return getUsers();
  }

  private DirectoryItemList getUsers() {
    //getting users according to restricted domains
    lastAllListUsersCalled = getUsersOfDomainsSorted(getDomainSources());

    final DirectorySource selectedSource = getSelectedSource();
    if (!isDoNotUseContacts() && (selectedSource == null || selectedSource.isContactsComponent())) {
      //add contacts only in a global view or a component view
      DirectoryItemList contacts = getContacts();
      if (!contacts.isEmpty()) {
        lastAllListUsersCalled.addAll(contacts);
        lastListUsersCalled = lastAllListUsersCalled;
        sort(getCurrentSort());
      }
    }

    setInitialSort(getCurrentSort());
    lastListUsersCalled = lastAllListUsersCalled;
    return lastAllListUsersCalled;
  }

  private DirectoryItemList getUsersOfDomainsSorted(List<String> domainIds) {
    final DirectoryItemList result;
    if (domainIds.isEmpty()) {
      result = new DirectoryItemList();
    } else if (SORT_NEWEST.equals(getCurrentSort())) {
      result = new DirectoryItemList(
          getOrganisationController().getUsersOfDomainsFromNewestToOldest(domainIds));
    } else {
      result = new DirectoryItemList(getOrganisationController().getUsersOfDomains(domainIds));
    }
    return result;
  }

  /**
   * get all Users that their Last Name begin with 'Index'
   * @param index:Alphabetical Index like A,B,C,E......
   *
   */
  public DirectoryItemList getUsersByIndex(String index) {
    setCurrentView(index);
    setCurrentQuery(null);
    if (getCurrentSort().equals(SORT_PERTINENCE)) {
      setCurrentSort(getPreviousSort());
    }
    DirectoryItemList usersByIndex = new DirectoryItemList();
    if (lastAllListUsersCalled == null) {
      // forcing to get all users to re-init list of visible users
      lastAllListUsersCalled = getUsers();
    }
    for (DirectoryItem varUd : lastAllListUsersCalled) {
      if (varUd.getLastName().toUpperCase().startsWith(index)) {
        usersByIndex.add(varUd);
      }
    }
    lastListUsersCalled = usersByIndex;
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
   *
   */
  public DirectoryItemList getUsersByQuery(QueryDescription queryDescription,
      boolean globalSearch) throws DirectoryException {
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
        if (allUsers == null || globalSearch || currentDirectory == DIRECTORY_DOMAIN) {
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
   *
   */
  public DirectoryItemList getAllUsersByGroup(String groupId) {
    resetDirectorySession();
    setCurrentDirectory(DIRECTORY_GROUP);
    currentGroups = new ArrayList<>();
    currentGroups.add(getOrganisationController().getGroup(groupId));
    lastAllListUsersCalled = new DirectoryItemList(getOrganisationController().getAllUsersOfGroup(groupId));
    lastListUsersCalled = lastAllListUsersCalled;
    return lastAllListUsersCalled;
  }

  /**
   * get all Users of the Groups which Id is in "groupIds"
   * @param groupIds:a list of groups' ids
   *
   */
  public DirectoryItemList getAllUsersByGroups(List<String> groupIds) {
    resetDirectorySession();
    setCurrentDirectory(DIRECTORY_GROUP);

    DirectoryItemList tmpList = new DirectoryItemList();

    currentGroups = new ArrayList<>();
    for (String groupId : groupIds) {
      mergeUsersIntoDirectoryItemList(getOrganisationController().getAllUsersOfGroup(groupId),
          tmpList);
      Group group = getOrganisationController().getGroup(groupId);
      if (group != null) {
        currentGroups.add(group);
      }
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
   *
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
   *
   */
  public DirectoryItemList getLastListOfUsersCalled() {
    return lastListUsersCalled;
  }

  /**
   * return All users of Space who has Id="spaceId"
   * @param spaceId:the ID of Space
   *
   */
  public DirectoryItemList getAllUsersBySpace(String spaceId) {
    resetDirectorySession();
    setCurrentDirectory(DIRECTORY_SPACE);
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
    setCurrentComponent(null);
    return getUsers();
  }

  public DirectoryItemList getAllContactsOfUser(String userId) {
    resetDirectorySession();
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
    resetDirectorySession();
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
   * @throws NotificationException
   */
  public void sendMessage(String compoId, String txtTitle, String txtMessage,
      UserRecipient[] selectedUsers) throws NotificationException {
    NotificationSender notifSender = new NotificationSender(compoId);
    int notifTypeId = BuiltInNotifAddress.DEFAULT.getId();
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

  public String getCurrentView() {
    return currentView;
  }

  public void setCurrentView(String currentView) {
    this.currentView = currentView;
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

  public SilverpeasList<UserFragmentVO> getFragments(SilverpeasList<DirectoryItem> items) {
    return items.stream().map(asFragment).filter(Objects::nonNull)
        .collect(SilverpeasList.collector(items));
  }

  private UserFragmentVO getUserFragment(UserItem user, SilverpeasTemplate template) {
    UserDetail otherUser = user.getUserDetail();
    UserDetail currentUser = getUserDetail();
    template.setAttribute("user", otherUser);
    if (StringUtil.isDefined(otherUser.getStatus())) {
      template.setAttribute("status", javaStringToHtmlParagraphe(otherUser.getStatus()));
    } else {
      template.setAttribute("status", null);
    }
    template.setAttribute("type", getString("GML.user.type." + user.getAccessLevel()));
    template.setAttribute(CONTEXT_ATTR, URLUtil.getApplicationURL());
    template.setAttribute("notMyself", !user.getOriginalId().equals(getUserId()));
    template.setAttribute("chatEnabled", ChatServer.isEnabled());
    template.setAttribute("aContact", otherUser.isInRelationWith(getUserId()));
    Invitation invitationSent = currentUser.getInvitationSentTo(otherUser.getId());
    if (invitationSent != null) {
      template.setAttribute("invitationSent", invitationSent.getId());
    } else {
      template.setAttribute("invitationSent", null);
    }
    Invitation invitationReceived = currentUser.getInvitationReceivedFrom(otherUser.getId());
    if (invitationReceived != null) {
      template.setAttribute("invitationReceived",invitationReceived.getId());
    } else {
      template.setAttribute("invitationReceived", null);
    }

    UserFull userFull = getUserFul(user.getOriginalId());
    Map<String, String> extra = new HashMap<>();
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

  public int getCurrentDirectory() {
    return currentDirectory;
  }

  public void setCurrentDirectory(int currentDirectory) {
    this.currentDirectory = currentDirectory;
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

  public void setCurrentDomains(List<String> domainIds) {
    currentDomains = new ArrayList<>();
    for (String domainId : domainIds) {
      Domain domain = getOrganisationController().getDomain(domainId);
      if (domain != null) {
        currentDomains.add(domain);
      }
    }
    setCurrentDirectory(DIRECTORY_DOMAIN);
  }

  public SpaceInstLight getCurrentSpace() {
    return currentSpace;
  }

  public String getCurrentQuery() {
    return currentQuery;
  }

  public void setCurrentQuery(String currentQuery) {
    this.currentQuery = currentQuery;
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
    if (list != null) {
      if (getCurrentSort().equals(SORT_ALPHA)) {
        Collections.sort(list);
      } else if (getCurrentSort().equals(SORT_NEWEST)) {
        Collections.sort(list, new CreationDateComparator());
      }
    }
  }

  private ContactService getContactBm() {
    return ContactService.get();
  }

  private List<String> getContactComponentIds() {
    String[] appIds =
        getOrganisationController().getComponentIdsForUser(getUserId(), "yellowpages");
    List<String> result = new ArrayList<>();
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
      items.addContactItems(getContacts(componentId, false));
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
    PublicationTemplate extraTemplate = getExtraTemplate();
    if (extraTemplate != null) {
      try {
        Form searchForm = extraTemplate.getSearchForm();
        if (searchForm != null) {
          if (xmlData != null) {
            searchForm.setData(xmlData);
          } else {
            searchForm.setData(extraTemplate.getRecordSet().getEmptyRecord());
          }
          return searchForm;
        }
        SilverLogger.getLogger(this)
            .warn("searchForm should exists, please verify form configuration of {0}",
                extraTemplate.getName());
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
    return null;
  }

  public PagesContext getExtraFormContext() {
    return extraFormContext;
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
    PublicationTemplateImpl extraTemplate = (PublicationTemplateImpl) getExtraTemplate();
    if (extraTemplate != null) {
      // build a dataRecord object storing user's entries
      try {
        RecordTemplate searchTemplate = extraTemplate.getSearchTemplate();
        DataRecord data = searchTemplate.getEmptyRecord();

        XmlSearchForm searchForm = (XmlSearchForm) extraTemplate.getSearchForm();
        searchForm.update(items, data, extraFormContext);

        // xmlQuery is in the data object, store it into session
        xmlData = data;
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
  }

  public QueryDescription buildSimpleQuery(String query) {
    QueryDescription queryDescription = new QueryDescription(query);
    queryDescription.addComponent("users");
    for (String appId : getContactComponentIds()) {
      queryDescription.addComponent(appId);
    }
    setCurrentQuery(query);
    return queryDescription;
  }

  public QueryDescription buildQuery(List<FileItem> items) {
    String query = FileUploadUtil.getParameter(items, "key");
    QueryDescription queryDescription = buildSimpleQuery(query);
    saveExtraRequest(items);
    buildExtraQuery(queryDescription, items);
    return queryDescription;
  }

  private void buildExtraQuery(QueryDescription query, List<FileItem> items) {
    PublicationTemplateImpl extraTemplate = (PublicationTemplateImpl) getExtraTemplate();
    if (extraTemplate == null) {
      return;
    }
    // build the xmlSubQuery according to the dataRecord object
    String templateFileName = extraTemplate.getFileName();
    String templateName = templateFileName.substring(0, templateFileName.lastIndexOf('.'));
    if (xmlData != null) {
      for (String fieldName : xmlData.getFieldNames()) {
        FieldDescription fieldQuery = buildFieldDescription(fieldName, templateName, items);
        query.addFieldQuery(fieldQuery);
      }
    }
  }

  private FieldDescription buildFieldDescription(String fieldName, String templateName,
      List<FileItem> items) {
    try {
      Field field = xmlData.getField(fieldName);
      String fieldValue = field.getStringValue();
      if (fieldValue != null && fieldValue.trim().length() > 0) {
        String fieldQuery = fieldValue.trim();
        if (fieldValue.contains("##")) {
          String operator = FileUploadUtil.getParameter(items,fieldName+"Operator");
          getExtraFormContext().setSearchOperator(fieldName, operator);
          fieldQuery = fieldQuery.replaceAll("##", " "+operator+" ");
        }
        return new FieldDescription(templateName + "$$" + fieldName, fieldQuery, getLanguage());
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
    return null;
  }

  public void clear() {
    xmlData = null;
  }

  public boolean isQuickUserSelectionEnabled() {
    return getCurrentDirectory() == DIRECTORY_DEFAULT ||
        getCurrentDirectory() == DIRECTORY_DOMAIN || getCurrentDirectory() == DIRECTORY_GROUP;
  }

  private SilverpeasTemplate getFragmentTemplate() {
    if (template == null) {
      template = SilverpeasTemplateFactory.createSilverpeasTemplateOnCore("directory");
    }
    return template;
  }

  private List<SilverpeasComponentInstance> getContactComponents() {
    List<SilverpeasComponentInstance> components = new ArrayList<>();
    List<String> componentIds = getContactComponentIds();
    for (String componentId : componentIds) {
      components.add(getOrganisationController().getComponentInstLight(componentId));
    }
    return components;
  }

  /**
   * Gets contacts of a given 'yellowPages' component.
   * @return the list of complete contact of component.
   */
  public DirectoryItemList getContacts(String componentId, boolean componentScope) {
    DirectoryItemList items = new DirectoryItemList();
    List<CompleteContact> componentContacts = getContactBm().getVisibleContacts(componentId);
    for (CompleteContact completeContact : componentContacts) {
      if (StringUtil.isNotDefined(completeContact.getUserId())) {
        items.add(completeContact);
      }
    }
    SilverpeasComponentInstance component =
        getOrganisationController().getComponentInstLight(componentId);
    if (componentScope) {
      setCurrentComponent(component);
      lastAllListUsersCalled = items;
      lastListUsersCalled = lastAllListUsersCalled;
    }
    return items;
  }

  public SilverpeasComponentInstance getCurrentComponent() {
    return currentComponent;
  }

  private void setCurrentComponent(final SilverpeasComponentInstance currentComponent) {
    this.currentComponent = currentComponent;
  }

  private void processDomainsAsSources(boolean domainsRestriction, Domain... domains) {
    for (Domain domain : domains) {
      if (!domainsRestriction || isDomainOneOfAsked(domain)) {
        addSource(domain);
      }
    }
  }

  public void initSources(boolean domainsRestriction) {
    directorySources = new ArrayList<>();
    Domain userDomain = getUserDetail().getDomain();
    Domain[] domains = getOrganisationController().getAllDomains();
    if (DomainProperties.areDomainsVisibleToAll()) {
      processDomainsAsSources(domainsRestriction, domains);
    } else if (DomainProperties.areDomainsNonVisibleToOthers()) {
      processDomainsAsSources(domainsRestriction, userDomain);
    } else if (DomainProperties.areDomainsVisibleOnlyToDefaultOne()) {
      if ("0".equals(userDomain.getId())) {
        processDomainsAsSources(domainsRestriction, domains);
      } else {
        processDomainsAsSources(domainsRestriction, userDomain);
      }
    }

    if (!isDoNotUseContacts()) {
      List<SilverpeasComponentInstance> components = getContactComponents();
      for (SilverpeasComponentInstance component : components) {
        addSource(component);
      }
    }
  }

  private boolean isDomainOneOfAsked(Domain domain) {
    for (Domain aDomain : getCurrentDomains()) {
      if (aDomain.getId().equals(domain.getId())) {
        return true;
      }
    }
    return false;
  }

  private void addSource(final SilverpeasComponentInstance component) {
    directorySources.add(new DirectorySource(component.getId(), component.getLabel(getLanguage()),
        component.getDescription(getLanguage())));
  }

  private void addSource(final Domain domain) {
    directorySources.add(new DirectorySource(domain.getId(), domain.getName(), domain.getDescription()));
  }

  public void setSelectedSource(String id) {
    for (DirectorySource source : directorySources) {
      source.setSelected(source.getId().equals(id));
    }
  }

  private DirectorySource getSelectedSource() {
    for (DirectorySource source : directorySources) {
      if (source.isSelected()) {
        return source;
      }
    }
    return null;
  }

  public List<DirectorySource> getDirectorySources() {
    return directorySources;
  }

  private List<String> getDomainSources() {
    List<String> ids = new ArrayList<>();
    for (DirectorySource source : directorySources) {
      if (!source.isContactsComponent()) {
        if (source.isSelected()) {
          return Arrays.asList(source.getId());
        }
        ids.add(source.getId());
      }
    }
    return ids;
  }

  private void resetDirectorySession() {
    setCurrentView(VIEW_ALL);
    setCurrentQuery(null);
    setCurrentComponent(null);
    directorySources = new ArrayList<>();
    setDoNotUseContacts(false);
  }

  public void setDoNotUseContacts(boolean doNotUse) {
    this.doNotUseContactsComponents = doNotUse;
  }

  private boolean isDoNotUseContacts() {
    return doNotUseContactsComponents;
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

}