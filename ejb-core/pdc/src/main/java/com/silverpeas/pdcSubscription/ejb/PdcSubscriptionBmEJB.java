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

/*
 * Aliaksei_Budnikau
 * Date: Oct 24, 2002
 */
package com.silverpeas.pdcSubscription.ejb;

import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.silverpeas.pdcSubscription.PdcSubscriptionRuntimeException;
import com.silverpeas.pdcSubscription.model.PDCSubscription;
import com.stratelia.silverpeas.classifyEngine.Criteria;
import com.stratelia.silverpeas.classifyEngine.Value;
import com.stratelia.silverpeas.contentManager.ContentInterface;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentPeas;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.personalization.control.ejb.PersonalizationBm;
import com.stratelia.webactiv.personalization.control.ejb.PersonalizationBmHome;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;

//import com.stratelia.silverpeas.peasCore.URLManager;

public class PdcSubscriptionBmEJB implements SessionBean {

  public final static String DATE_FORMAT = "yyyy/MM/dd";
  public final static String MESSAGE_TITLE = "notification.title";
  public final static String MESSAGE_DELETE_TITLE = "notification.delete.title";
  public final static String SOURCE_CLASSIFICATION = "pdcClassification";

  /**
   * Remote interface method
   */
  public ArrayList getPDCSubscriptionByUserId(int userId)
      throws RemoteException {
    Connection conn = null;
    ArrayList result = null;

    try {
      conn = openConnection();
      result = PdcSubscriptionDAO.getPDCSubscriptionByUserId(conn, userId);
    } catch (Exception re) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionBmEJB.getPDCSubscriptionByUserId",
          PdcSubscriptionRuntimeException.ERROR,
          "PdcSubscription.CANNOT_FIND_SUBSCRIPTION_BYUSID", String
          .valueOf(userId), re);
    } finally {
      closeConnection(conn);
    }

    return result;
  }

  /**
   * Remote interface method
   */
  public PDCSubscription getPDCSubsriptionById(int id) throws RemoteException {
    Connection conn = null;
    PDCSubscription result = null;

    try {
      conn = openConnection();
      result = PdcSubscriptionDAO.getPDCSubsriptionById(conn, id);
    } catch (Exception re) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionBmEJB.getPDCSubsriptionById",
          PdcSubscriptionRuntimeException.ERROR,
          "PdcSubscription.CANNOT_FIND_SUBSCRIPTION_BYUSID",
          String.valueOf(id), re);
    } finally {
      closeConnection(conn);
    }

    if (result == null) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionBmEJB.getPDCSubsriptionById",
          PdcSubscriptionRuntimeException.ERROR,
          "PdcSubscription.NO_SUBSCRIPTION_WITH_ID", String.valueOf(id));
    }

    return result;
  }

  /**
   * Remote interface method
   */
  public int createPDCSubscription(PDCSubscription subscription)
      throws RemoteException {
    Connection conn = null;
    int result = -1;

    try {
      conn = openConnection();
      result = PdcSubscriptionDAO.createPDCSubscription(conn, subscription);
    } catch (Exception re) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionBmEJB.createPDCSubscription",
          PdcSubscriptionRuntimeException.ERROR,
          "PdcSubscription.CANNOT_CREATE_SUBSCRIPTION", subscription, re);
    } finally {
      closeConnection(conn);
    }

    return result;
  }

  /**
   * Remote interface method
   */
  public void updatePDCSubscription(PDCSubscription subscription)
      throws RemoteException {
    SilverTrace.info("PdcSubscription",
        "PdcSubscriptionBmEJB.updatePDCSubscription()",
        "root.MSG_GEN_ENTER_METHOD", "subscription = "
        + subscription.toString());
    Connection conn = null;

    try {
      conn = openConnection();
      PdcSubscriptionDAO.updatePDCSubscription(conn, subscription);
    } catch (Exception re) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionBmEJB.updatePDCSubscription",
          PdcSubscriptionRuntimeException.ERROR,
          "PdcSubscription.CANNOT_UPDATE_SUBSCRIPTION", subscription, re);
    } finally {
      closeConnection(conn);
    }
  }

  /**
   * Remote interface method
   */
  public void removePDCSubscriptionById(int id) throws RemoteException {
    Connection conn = null;

    try {
      conn = openConnection();
      PdcSubscriptionDAO.removePDCSubscriptionById(conn, id);
    } catch (Exception re) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionBmEJB.removePDCSubscriptionById",
          PdcSubscriptionRuntimeException.ERROR,
          "PdcSubscription.CANNOT_DELETE_SUBSCRIPTION", String.valueOf(id), re);
    } finally {
      closeConnection(conn);
    }
  }

  /**
   * Remote interface method
   */
  public void removePDCSubscriptionById(int[] ids) throws RemoteException {
    Connection conn = null;

    try {
      conn = openConnection();
      PdcSubscriptionDAO.removePDCSubscriptionById(conn, ids);
    } catch (Exception re) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionBmEJB.removePDCSubscriptionById",
          PdcSubscriptionRuntimeException.ERROR,
          "PdcSubscription.CANNOT_DELETE_SUBSCRIPTION", String.valueOf(ids), re);
    } finally {
      closeConnection(conn);
    }
  }

  /**
   * Remote inteface method Implements PDCSubscription check for axis deletion. It deletes all
   * references to this axis from PDCSubscription module DB
   * @param axisId the axis to be checked
   * @param axisName the name of the axis
   */
  public void checkAxisOnDelete(int axisId, String axisName)
      throws RemoteException {
    Connection conn = null;
    List subscriptions = null;

    try {
      conn = openConnection();
      // found all subscription uses axis provided
      subscriptions = PdcSubscriptionDAO.getPDCSubscriptionByUsedAxis(conn,
          axisId);
      if (subscriptions == null
          || (subscriptions != null && subscriptions.size() <= 0)) {
        return;
      }
      int[] pdcIds = new int[subscriptions.size()];
      OrganizationController ocontroller = new OrganizationController();
      int adminId;

      for (int i = 0; i < subscriptions.size(); i++) {
        PDCSubscription subscription = (PDCSubscription) subscriptions.get(i);
        pdcIds[i] = subscription.getId();
        adminId = getFirstAdministrator(ocontroller, subscription.getOwnerId());
        sendDeleteNotif(subscription, axisName, false, adminId, null);
      }
      // remove all found subscriptions
      PdcSubscriptionDAO.removePDCSubscriptionById(conn, pdcIds);

    } catch (Exception e) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionBmEJB.checkAxisOnDelete",
          PdcSubscriptionRuntimeException.ERROR,
          "PdcSubscription.EX_CHECK_AXIS_FALIED", subscriptions, e);

    } finally {
      closeConnection(conn);
    }
  }

  /**
   * Implements PDCSubscription check for value deletion. It deletes all references to the path
   * containing this value from PDCSubscription module DB
   * @param axisId the axis to be checked
   * @param axisName the name of the axis
   * @param oldPath old path that would be removed soon
   * @param newPath new path. That will be places instead of old for this axis
   * @param pathInfo should contains PdcBm.getFullPath data structure
   */
  public void checkValueOnDelete(int axisId, String axisName, List oldPath,
      List newPath, List pathInfo) throws RemoteException {
    Connection conn = null;
    List subscriptions = null;

    try {
      conn = openConnection();
      subscriptions = PdcSubscriptionDAO.getPDCSubscriptionByUsedAxis(conn,
          axisId);
      if (subscriptions == null
          || (subscriptions != null && subscriptions.size() <= 0)) {
        return;
      }

      int[] removeIds = new int[subscriptions.size()];
      int removeLength = 0;
      OrganizationController ocontroller = new OrganizationController();
      int adminId;

      for (int i = 0; i < subscriptions.size(); i++) {
        PDCSubscription subscription = (PDCSubscription) subscriptions.get(i);
        // for each subscription containing axis affected by value deletion
        // check if any criteria value has been
        // deleted
        if (checkSubscriptionRemove(subscription, axisId, oldPath, newPath)) {
          adminId = getFirstAdministrator(ocontroller, subscription
              .getOwnerId());
          sendDeleteNotif(subscription, axisName, true, adminId, pathInfo);
          removeIds[removeLength] = subscription.getId();
          removeLength++;
        }
      }
      int[] ids = new int[removeLength];
      System.arraycopy(removeIds, 0, ids, 0, removeLength);

      PdcSubscriptionDAO.removePDCSubscriptionById(conn, ids);

    } catch (Exception e) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionBmEJB.checkValueOnDelete",
          PdcSubscriptionRuntimeException.ERROR,
          "PdcSubscription.EX_CHECK_AXIS_FALIED", subscriptions, e);

    } finally {
      closeConnection(conn);
    }
  }

  /**
   * This method check is any subscription that match criterias provided and sends notification if
   * succeed
   * @param classifyValues Linst of ClassifyValues to be checked
   * @param componentId component where classify event occures
   * @param silverObjectid object that was classified
   */
  public void checkSubscriptions(List classifyValues, String componentId,
      int silverObjectid) throws RemoteException {
    SilverTrace.info("PdcSubscription",
        "PdcSubscriptionBmEJB.checkSubscriptions()",
        "root.MSG_GEN_ENTER_METHOD", "classifyValues = " + classifyValues
        + ", componentId = " + componentId + ", silverObjectid = "
        + silverObjectid);

    Connection conn = null;
    SilverContentInterface silverContent = null;
    ArrayList spaceAndInstanceNames = null;
    int firstAdminId = -1;
    OrganizationController organizationController = new OrganizationController();

    try {
      conn = openConnection();
      // load all PDCSubscritions into the memory to perform future check of
      // them
      ArrayList subscriptions = PdcSubscriptionDAO.getAllPDCSubscriptions(conn);
      // loop through all subscription
      for (int i = 0; i < subscriptions.size(); i++) {
        PDCSubscription subscription = (PDCSubscription) subscriptions.get(i);
        // check if current subscription corresponds a list of classify values
        // provided into the method
        if (isCorrespondingSubscription(subscription, classifyValues)) {
          if (silverContent == null) {
            silverContent = getSilverContent(componentId, silverObjectid);
            spaceAndInstanceNames = getSpaceAndInstanceNames(componentId,
                organizationController);
            firstAdminId = getFirstAdministrator(organizationController,
                subscription.getOwnerId());
          }
          // The current subscription matches the new classification.
          // Now, we have to test if subscription's owner is allowed to access
          // the classified item.
          String userId = new Integer(subscription.getOwnerId()).toString();
          String[] roles = organizationController.getUserProfiles(userId,
              componentId);
          if (roles.length > 0) {
            // if user have got at least one role, sends a notification to the
            // user specified in pdcSubscription
            sendSubscriptionNotif(subscription, spaceAndInstanceNames,
                componentId, silverContent, firstAdminId);
          }
        }
      }

    } catch (Exception e) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionBmEJB.checkSubscriptions",
          PdcSubscriptionRuntimeException.ERROR,
          "PdcSubscription.EX_CHECK_SUBSCR_FALIED", classifyValues, e);

    } finally {
      closeConnection(conn);
    }
  }

  /**
   * get the silverContent object according to the given silverObjectid
   * @param componentId - the component where is classified the silverContent
   * @param silverObjectid - the unique identifier of the silverContent
   * @param organizationController - the OrganizationController
   * @return SilverContentInterface the object which has been classified
   */
  private SilverContentInterface getSilverContent(String componentId,
      int silverObjectId) {
    ArrayList silverobjectIds = new ArrayList();
    silverobjectIds.add(new Integer(silverObjectId));

    List silverContents = null;
    SilverContentInterface silverContent = null;
    try {
      ContentManager contentManager = new ContentManager();
      ContentPeas contentPeas = contentManager.getContentPeas(componentId);
      ContentInterface contentInterface = (ContentInterface) contentPeas
          .getContentInterface();
      ArrayList userRoles = new ArrayList();
      userRoles.add("admin");
      silverContents = contentInterface.getSilverContentById(silverobjectIds,
          componentId, "unknown", userRoles);
    } catch (Exception e) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionBmEJB.sendSubscriptionNotif",
          PdcSubscriptionRuntimeException.ERROR,
          "PdcSubscription.EX_CHECK_SUBSCR_FALIED", e);
    }
    if (silverContents != null && silverContents.size() != 0)
      silverContent = (SilverContentInterface) silverContents.get(0);

    return silverContent;
  }

  /**
   * get the names of space and instance where the object have been classified
   * @param componentId - the component where is classified the silverContent
   * @param organizationController - the OrganizationController
   * @return ArrayList 1 - spaceName 2 - instanceName 3 - spaceId
   */
  private ArrayList getSpaceAndInstanceNames(String componentId,
      OrganizationController organizationController) {
    ComponentInst componentInstance = organizationController
        .getComponentInst(componentId);
    String instanceName = componentInstance.getLabel();
    String workSpaceId = componentInstance.getDomainFatherId();
    SpaceInst spaceInst = organizationController.getSpaceInstById(workSpaceId);
    String spaceName = spaceInst.getName();

    ArrayList spaceAndInstanceNames = new ArrayList();
    spaceAndInstanceNames.add(spaceName);
    spaceAndInstanceNames.add(instanceName);
    spaceAndInstanceNames.add(workSpaceId);

    return spaceAndInstanceNames;
  }

  /**
   * @return first administrator id
   */
  private int getFirstAdministrator(
      OrganizationController organizationController, int userId) {
    int fromUserID = -1;
    String[] admins = organizationController.getAdministratorUserIds(Integer
        .toString(userId));
    if (admins != null && admins.length > 0) {
      fromUserID = Integer.parseInt(admins[0]);
    }
    return fromUserID;
  }

  /**
   * @return true if subscription should be removed
   * @param subscription PDCSubscription to check
   * @param axisId id of the axis value of which should be removed
   * @param oldPath list of original axis paths (before deletion)
   * @param newPath list new axis path to be places instead of old path
   */
  protected boolean checkSubscriptionRemove(PDCSubscription subscription,
      int axisId, List oldPath, List newPath) {
    List subscriptionCtx = subscription.getPdcContext();
    for (int i = 0; i < subscriptionCtx.size(); i++) {
      Criteria criteria = (Criteria) subscriptionCtx.get(i);
      if (criteria.getAxisId() == axisId) {
        // check if criterias value has been removed from axis
        if (checkValuesRemove(criteria.getValue(), oldPath, newPath)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * @return true if path provided was removed should be removed
   */
  protected boolean checkValuesRemove(String originalPath, List oldPath,
      List newPath) {
    if (!originalPath.endsWith("/")) {
      originalPath += "/";
    }
    // substring a value from original path. Ex: /2/3/9/ value will be /9/
    int idx = originalPath.lastIndexOf("/", originalPath.length() - 2);
    String value = originalPath.substring(idx, originalPath.length());
    boolean result = false;

    // check if extracted value presented in old path but not presented in
    // newPath
    if (checkValueInPath(value, oldPath) && !checkValueInPath(value, newPath)) {
      result = true;
    }

    return result;
  }

  /**
   * Checks if values provided presented in provided path. <br>
   * Ex1: path /2/3/4/5/ value: /5/ result:true <br>
   * Ex2: path /2/3/4/5/ value: /3/ result true <br>
   * Ex2: path /2/3/4/5/ value: /8/ result false
   */
  protected boolean checkValueInPath(String value, List pathList) {
    for (int i = 0; i < pathList.size(); i++) {
      String path = (String) pathList.get(i);
      if (path.indexOf(value) != -1) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return true if subscription provided match the list of classify values
   */
  protected boolean isCorrespondingSubscription(PDCSubscription subscription,
      List classifyValues) {
    SilverTrace.info("PdcSubscription",
        "PdcSubscriptionBmEJB.isCorrespondingSubscription()",
        "root.MSG_GEN_ENTER_METHOD", "subscription = "
        + subscription.toString() + ", classifyValues = "
        + classifyValues.toString());
    List searchCriterias = subscription.getPdcContext();

    if (searchCriterias == null || classifyValues == null) {
      return false;
    }
    if (searchCriterias.size() == 0 || classifyValues.size() == 0
        || searchCriterias.size() > classifyValues.size()) {
      return false;
    }

    /*
     * The following algorithm implemented Loop every SearchCriteria and for axis of SearchCriteria
     * found ClassifyValue with such axis if true check getValue() of ClassifyValue and getValue()
     * of SearchCriteria. The start of the value String of ClassifyValue should match the whole
     * value String of SearchCriteria.
     */
    for (int i = 0; i < searchCriterias.size(); i++) {
      Criteria criteria = (Criteria) searchCriterias.get(i);
      if (criteria == null)
        continue;

      boolean result = false;

      for (int j = 0; j < classifyValues.size(); j++) {
        Value value = (Value) classifyValues.get(j);
        if (checkValues(criteria, value)) {
          result = true;
          break;
        }
      }

      if (result == false) {
        return false;
      }
    }
    return true;
  }

  /**
   * @return true if criteria provided match the searchValue provided
   */
  protected boolean checkValues(Criteria criteria, Value searchValue) {
    if (searchValue.getAxisId() != criteria.getAxisId()) {
      return false;
    }
    if (searchValue.getValue().startsWith(criteria.getValue(), 0)) {
      return true;
    }
    return false;
  }

  /**
   * Sends delete notifications
   */
  protected void sendDeleteNotif(PDCSubscription subscription, String axisName,
      boolean isValueDeleted, int fromUser, List path)
      throws NotificationManagerException {
    SilverTrace.info("PdcSubscription",
        "PdcSubscriptionBmEJB.sendDeleteNotif()", "root.MSG_GEN_ENTER_METHOD");

    final String language = getDefaultUserLanguage(subscription.getOwnerId());
    final ResourceLocator resources = new ResourceLocator(
        "com.silverpeas.pdcSubscription.multilang.pdcsubscription", language);
    final StringBuffer message = new StringBuffer(150);

    if (isValueDeleted) {
      message.append(resources.getString("deleteOnValueMessage"));
    } else {
      message.append(resources.getString("deleteOnAxisMessage"));
    }
    message.append("\n");

    message.append(resources.getString("Subscription"));
    message.append(subscription.getName());
    message.append("\n");

    message.append(resources.getString("Axis"));
    message.append(axisName);
    message.append("\n");

    if (path != null) {
      message.append(resources.getString("Path"));
      // message.append(formatPath(path));
      message.append("\n");
    }

    String[] notifUserList = new String[1];
    notifUserList[0] = Integer.toString(subscription.getOwnerId());

    NotificationSender notifSender = new NotificationSender("");
    NotificationMetaData notifMetaData = new NotificationMetaData(
        NotificationParameters.NORMAL, resources
        .getString(MESSAGE_DELETE_TITLE), message.toString());
    notifMetaData.setSender(String.valueOf(fromUser));
    notifMetaData.addUserRecipients(notifUserList);
    notifMetaData.setSource(resources.getString(SOURCE_CLASSIFICATION));
    notifSender.notifyUser(notifMetaData);
  }

  /**
   * Sends a notification when subscription criterias math a new content classified
   */
  protected void sendSubscriptionNotif(PDCSubscription subscription,
      ArrayList spaceAndInstanceNames, String componentId,
      SilverContentInterface silverContent, int fromUserId)
      throws NotificationManagerException {
    final int userID = subscription.getOwnerId();
    final String subscriptionName = subscription.getName();
    final java.util.Date classifiedDate = new java.util.Date();

    String documentName = "";
    String documentUrl = "";
    if (silverContent != null) {
      String contentUrl = silverContent.getURL();
      String contentName = silverContent.getName();
      documentUrl = "";
      if (contentUrl != null) {
        documentUrl = "/RpdcSearch/jsp/GlobalContentForward?contentURL="
            + URLEncoder.encode(contentUrl) + "&componentId=" + componentId;
      }
      documentName = (contentName != null) ? contentName : "";
    }

    String spaceName = (String) spaceAndInstanceNames.get(0);
    String instanceName = (String) spaceAndInstanceNames.get(1);

    sendNotification(userID, fromUserId, subscriptionName, instanceName,
        componentId, spaceName, classifiedDate, "standartMessage", documentUrl,
        documentName);
  }

  /**
   * Utility method
   * @return Connection to use in all ejb db operations
   */
  protected Connection openConnection() {
    try {
      Connection con = DBUtil
          .makeConnection(JNDINames.PDC_SUBSCRIPTION_DATASOURCE);
      return con;
    } catch (Exception e) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscription.getConnection()",
          PdcSubscriptionRuntimeException.ERROR,
          "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  /**
   * Utility method. Closes a connection opened by @link #openConnection() method
   */
  protected void closeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("PdcSubscription",
            "PdcSubscriptionBmEJB.closeConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  protected void sendNotification(int userID, int fromUserID,
      String subscription, String component, String componentID,
      String workSpace, java.util.Date classifiedDate,
      String notificationMessageKey, String documentUrl, String documentName)
      throws NotificationManagerException {
    SilverTrace.info("PdcSubscription",
        "PdcSubscriptionBmEJB.sendNotification()", "root.MSG_GEN_ENTER_METHOD",
        "userID = " + userID + ", fromUserID = " + fromUserID
        + ", subscription = " + subscription + ", componentId = "
        + component + ", workSpaceId = " + workSpace
        + ", notificationMessageKey = " + notificationMessageKey
        + ", documentUrl = " + documentUrl);

    final String language = getDefaultUserLanguage(userID);
    final ResourceLocator resources = new ResourceLocator(
        "com.silverpeas.pdcSubscription.multilang.pdcsubscription", language);
    final StringBuffer message = new StringBuffer(150);

    message.append(resources.getString("Subscription"));
    message.append(subscription);
    message.append("\n");

    message.append(resources.getString("DocumentName"));
    message.append(documentName);
    message.append("\n");

    String[] notifUserList = new String[1];
    notifUserList[0] = Integer.toString(userID);

    NotificationSender notifSender = new NotificationSender(componentID);
    NotificationMetaData notifMetaData = new NotificationMetaData(
        NotificationParameters.NORMAL, resources
        .getString(notificationMessageKey), message.toString());
    notifMetaData.setSender(String.valueOf(fromUserID));
    notifMetaData.addUserRecipients(notifUserList);
    notifMetaData.setSource(workSpace + " - " + component);
    notifMetaData.setLink(documentUrl);
    notifSender.notifyUser(notifMetaData);
  }

  /**
   * @return user preferred language by userid provided
   */
  protected String getDefaultUserLanguage(int userID) {
    String lang = "fr";
    try {
      PersonalizationBmHome personalizationBmHome = (PersonalizationBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.PERSONALIZATIONBM_EJBHOME,
          PersonalizationBmHome.class);
      PersonalizationBm personalizationBm = personalizationBmHome.create();
      personalizationBm.setActor(String.valueOf(userID));
      lang = personalizationBm.getFavoriteLanguage();
    } catch (Exception e) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionBmEJB.getDefaultUserLanguage()",
          PdcSubscriptionRuntimeException.ERROR,
          "root.EX_CANT_GET_PREFERRED_USER_LANG", e);
    }
    return lang;
  }

  /**
   * Formats a path (of values) to be showed to users
   */
  protected String formatPath(List pathInfos) {
    final StringBuffer res = new StringBuffer();
    for (int i = 0; i < pathInfos.size(); i++) {
      if (i != 0) {
        res.append("/");
      }
      String value = (String) ((List) pathInfos.get(i)).get(0);
      res.append(value);
    }
    return res.toString();
  }

  /**
   * EJB Required method
   */
  public void ejbCreate() throws CreateException {
  }

  /**
   * EJB Required method
   */
  public void ejbRemove() {
  }

  /**
   * EJB Required method
   */
  public void ejbActivate() {
  }

  /**
   * EJB Required method
   */
  public void ejbPassivate() {
  }

  /**
   * EJB Required method
   */
  public void setSessionContext(SessionContext sc) {
  }

}
