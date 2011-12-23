/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.silverpeas.directory.control;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.silverpeas.socialnetwork.relationShip.RelationShipService;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.ProfileInst;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;

public class DirectoryService {

 private List<UserDetail> lastAlllistUsersCalled;
  private List<UserDetail> lastListUsersCalled;
  private OrganizationController organizationController=new OrganizationController();
  private String userId;
  private RelationShipService relationShipService = new RelationShipService();






  /**
   * Standard Session Controller Constructeur
   *
   *
   * @param mainSessionCtrl   The user's profile
   * @param componentContext  The component's profile
   *
   * @see
   */


  /**

   *get All Users
   *
   * @see
   */
  public List<UserDetail> getAllUsers() {
    lastAlllistUsersCalled = Arrays.asList(getOrganizationController().getAllUsers());
    lastListUsersCalled = lastAlllistUsersCalled;
    return lastAlllistUsersCalled;

  }

  /**
   *get all Users that their Last Name  begin with 'Index'
   *
   * @param index:Alphabetical Index like A,B,C,E......
   * @see
   */
  public List<UserDetail> getUsersByIndex(String index) {
    lastListUsersCalled = new ArrayList<UserDetail>();

    for (UserDetail varUd : lastAlllistUsersCalled) {
      if (varUd.getLastName().toUpperCase().startsWith(index)) {
        lastListUsersCalled.add(varUd);
      }
    }
    return lastListUsersCalled;

  }

  /**
   *get all User that  heir  lastname or first name  Last Name  like  "Key"
   *
   * @param Key:the key of search
   * @see
   */
  public List<UserDetail> getUsersByLastName(String Key) {
    lastListUsersCalled = new ArrayList<UserDetail>();

    for (UserDetail varUd : lastAlllistUsersCalled) {
      if (varUd.getLastName().toUpperCase().startsWith(Key) || varUd.getFirstName().toUpperCase().
          startsWith(Key)) {
        lastListUsersCalled.add(varUd);
      }
    }
    return lastListUsersCalled;

  }

  /**
   *get all User of the Group who has Id="groupId"
   *
   * @param groupId:the ID of group
   * @see
   */
  public List<UserDetail> getAllUsersByGroup(String groupId) {
    lastAlllistUsersCalled = Arrays.asList(getOrganizationController().getAllUsersOfGroup(groupId));
    lastListUsersCalled = lastAlllistUsersCalled;
    return lastAlllistUsersCalled;
  }

  /**
   *get all User "we keep the last list of All users"
   *
   *
   * @see
   */
  public List<UserDetail> getLastListOfAllUsers() {
    lastListUsersCalled = lastAlllistUsersCalled;
    return lastAlllistUsersCalled;
  }

  /**
   *get the last list of users colled   " keep the session"
   *
   *
   * @see
   */
  public List<UserDetail> getLastListOfUsersCallded() {
    return lastListUsersCalled;
  }

  /**
   *return All users of Space who has Id="spaceId"
   *
   * @param spaceId:the ID of Space
   * @see
   */
  public List<UserDetail> getAllUsersBySpace(String spaceId) {
    List<String> lus = new ArrayList<String>();
    lus = getAllUsersBySpace(lus, spaceId);
    lastAlllistUsersCalled = Arrays.asList(getOrganizationController().getUserDetails(lus.toArray(new String[lus.
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
   *
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
   *return contacts  of user who has Id="domainId"
   *
   * @param domainId:the ID of Domaine
   * @see
   */
  public List<UserDetail> getAllContatcsOfUuser(String userId) {
    lastAlllistUsersCalled = new ArrayList<UserDetail>();
    try {
      List<String> contactsIds = relationShipService.getMyContactsIds(Integer.parseInt(userId));
      for (String contactId : contactsIds) {
        lastAlllistUsersCalled.add(getOrganizationController().getUserDetail(contactId));
      }
    } catch (SQLException ex) {
      SilverTrace.error("newsFeedService",
          "NewsFeedService.getMyContactsIds", "", ex);
    }
    lastListUsersCalled = lastAlllistUsersCalled;
    return lastAlllistUsersCalled;
  }
  /**
   *return contacts  of user who has Id="domainId"
   *
   * @param domainId:the ID of Domaine
   * @see
   */
  public List<UserDetail> getCommonContacts(String userId1,String userId2) {
    lastAlllistUsersCalled = new ArrayList<UserDetail>();
    try {
      List<String> contactsIds = relationShipService.getAllCommonContactsIds(Integer.parseInt(userId1),Integer.parseInt(userId2));
      for (String contactId : contactsIds) {
        lastAlllistUsersCalled.add(getOrganizationController().getUserDetail(contactId));
      }
    } catch (SQLException ex) {
      SilverTrace.error("newsFeedService",
          "NewsFeedService.getMyContactsIds", "", ex);
    }
    lastListUsersCalled = lastAlllistUsersCalled;
    return lastAlllistUsersCalled;
  }

  
 
public OrganizationController getOrganizationController() {
    return organizationController;
  }
public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }
}
