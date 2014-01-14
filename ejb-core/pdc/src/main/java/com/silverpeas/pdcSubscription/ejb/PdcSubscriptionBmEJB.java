/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Aliaksei_Budnikau
 * Date: Oct 24, 2002
 */
package com.silverpeas.pdcSubscription.ejb;

import com.silverpeas.notification.builder.helper.UserNotificationHelper;
import com.silverpeas.pdcSubscription.PdcResourceClassificationUserNotification;
import com.silverpeas.pdcSubscription.PdcSubscriptionDeletionUserNotification;
import com.silverpeas.pdcSubscription.PdcSubscriptionRuntimeException;
import com.silverpeas.pdcSubscription.model.PDCSubscription;
import com.stratelia.silverpeas.classifyEngine.Criteria;
import com.stratelia.silverpeas.classifyEngine.Value;
import com.stratelia.silverpeas.contentManager.ContentInterface;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentPeas;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.silverpeas.contentManager.SilverContentVisibility;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import org.silverpeas.core.admin.OrganisationController;
import org.silverpeas.core.admin.OrganisationControllerFactory;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Stateless(name = "PdcSubscription", description = "Stateless bean to manage pdc subscription.")
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class PdcSubscriptionBmEJB implements PdcSubscriptionBm {

  /**
   * Remote interface method
   *
   * @param userId
   */
  @Override
  public List<PDCSubscription> getPDCSubscriptionByUserId(int userId) {
    Connection conn = DBUtil.makeConnection(JNDINames.PDC_SUBSCRIPTION_DATASOURCE);
    try {
      return PdcSubscriptionDAO.getPDCSubscriptionByUserId(conn, userId);
    } catch (SQLException re) {
      throw new PdcSubscriptionRuntimeException("PdcSubscriptionBmEJB.getPDCSubscriptionByUserId",
          PdcSubscriptionRuntimeException.ERROR,
          "PdcSubscription.CANNOT_FIND_SUBSCRIPTION_BYUSID", String.valueOf(userId), re);
    } finally {
      DBUtil.close(conn);
    }
  }

  /**
   * Remote interface method
   *
   * @param id
   * @return
   */
  @Override
  public PDCSubscription getPDCSubsriptionById(int id) {
    Connection conn = DBUtil.makeConnection(JNDINames.PDC_SUBSCRIPTION_DATASOURCE);
    PDCSubscription result = null;
    try {
      result = PdcSubscriptionDAO.getPDCSubsriptionById(conn, id);
    } catch (SQLException re) {
      throw new PdcSubscriptionRuntimeException("PdcSubscriptionBmEJB.getPDCSubsriptionById",
          PdcSubscriptionRuntimeException.ERROR, "PdcSubscription.CANNOT_FIND_SUBSCRIPTION_BYUSID",
          String.valueOf(id), re);
    } finally {
      DBUtil.close(conn);
    }
    if (result == null) {
      throw new PdcSubscriptionRuntimeException("PdcSubscriptionBmEJB.getPDCSubsriptionById",
          PdcSubscriptionRuntimeException.ERROR, "PdcSubscription.NO_SUBSCRIPTION_WITH_ID", String.
          valueOf(id));
    }
    return result;
  }

  /**
   * Remote interface method
   *
   * @param subscription
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public int createPDCSubscription(PDCSubscription subscription) {
    Connection conn = DBUtil.makeConnection(JNDINames.PDC_SUBSCRIPTION_DATASOURCE);
    try {
      return PdcSubscriptionDAO.createPDCSubscription(conn, subscription);
    } catch (Exception re) {
      throw new PdcSubscriptionRuntimeException("PdcSubscriptionBmEJB.createPDCSubscription",
          PdcSubscriptionRuntimeException.ERROR, "PdcSubscription.CANNOT_CREATE_SUBSCRIPTION",
          subscription, re);
    } finally {
      DBUtil.close(conn);
    }
  }

  /**
   * Remote interface method
   *
   * @param subscription
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void updatePDCSubscription(PDCSubscription subscription) {
    SilverTrace.info("PdcSubscription", "PdcSubscriptionBmEJB.updatePDCSubscription()",
        "root.MSG_GEN_ENTER_METHOD", "subscription = " + subscription);
    Connection conn = DBUtil.makeConnection(JNDINames.PDC_SUBSCRIPTION_DATASOURCE);
    try {
      PdcSubscriptionDAO.updatePDCSubscription(conn, subscription);
    } catch (SQLException re) {
      throw new PdcSubscriptionRuntimeException("PdcSubscriptionBmEJB.updatePDCSubscription",
          PdcSubscriptionRuntimeException.ERROR, "PdcSubscription.CANNOT_UPDATE_SUBSCRIPTION",
          subscription, re);
    } finally {
      DBUtil.close(conn);
    }
  }

  /**
   * Remote interface method
   *
   * @param id
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void removePDCSubscriptionById(int id) {
    Connection conn = DBUtil.makeConnection(JNDINames.PDC_SUBSCRIPTION_DATASOURCE);
    try {
      PdcSubscriptionDAO.removePDCSubscriptionById(conn, id);
    } catch (SQLException re) {
      throw new PdcSubscriptionRuntimeException("PdcSubscriptionBmEJB.removePDCSubscriptionById",
          PdcSubscriptionRuntimeException.ERROR, "PdcSubscription.CANNOT_DELETE_SUBSCRIPTION",
          String.valueOf(id), re);
    } finally {
      DBUtil.close(conn);
    }
  }

  /**
   * Remote interface method
   *
   * @param ids
   */
  @Override
  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  public void removePDCSubscriptionById(int[] ids) {
    Connection conn = DBUtil.makeConnection(JNDINames.PDC_SUBSCRIPTION_DATASOURCE);
    try {
      PdcSubscriptionDAO.removePDCSubscriptionById(conn, ids);
    } catch (SQLException re) {
      throw new PdcSubscriptionRuntimeException("PdcSubscriptionBmEJB.removePDCSubscriptionById",
          PdcSubscriptionRuntimeException.ERROR, "PdcSubscription.CANNOT_DELETE_SUBSCRIPTION",
          Arrays.toString(ids), re);
    } finally {
      DBUtil.close(conn);
    }
  }

  /**
   * Remote inteface method Implements PDCSubscription check for axis deletion. It deletes all
   * references to this axis from PDCSubscription module DB
   *
   * @param axisId the axis to be checked
   * @param axisName the name of the axis
   */
  @Override
  public void checkAxisOnDelete(int axisId, String axisName) {
    Connection conn = DBUtil.makeConnection(JNDINames.PDC_SUBSCRIPTION_DATASOURCE);
    try {
      // found all subscription uses axis provided
      List<PDCSubscription> subscriptions = PdcSubscriptionDAO.getPDCSubscriptionByUsedAxis(conn,
          axisId);
      if (subscriptions == null || subscriptions.isEmpty()) {
        return;
      }
      int[] pdcIds = new int[subscriptions.size()];
      for (int i = 0; i < subscriptions.size(); i++) {
        PDCSubscription subscription = subscriptions.get(i);
        pdcIds[i] = subscription.getId();
        UserNotificationHelper.buildAndSend(
            new PdcSubscriptionDeletionUserNotification(subscription, axisName, false));
      }
      // remove all found subscriptions
      PdcSubscriptionDAO.removePDCSubscriptionById(conn, pdcIds);
    } catch (SQLException e) {
      throw new PdcSubscriptionRuntimeException("PdcSubscriptionBmEJB.checkAxisOnDelete",
          PdcSubscriptionRuntimeException.ERROR, "PdcSubscription.EX_CHECK_AXIS_FALIED", e);
    } finally {
      DBUtil.close(conn);
    }
  }

  /**
   * Implements PDCSubscription check for value deletion. It deletes all references to the path
   * containing this value from PDCSubscription module DB
   *
   * @param axisId the axis to be checked
   * @param axisName the name of the axis
   * @param oldPath old path that would be removed soon
   * @param newPath new path. That will be places instead of old for this axis
   * @param pathInfo should contains PdcBm.getFullPath data structure
   */
  @Override
  public void checkValueOnDelete(int axisId, String axisName, List<String> oldPath,
      List<String> newPath, List<com.stratelia.silverpeas.pdc.model.Value> pathInfo) {
    Connection conn = DBUtil.makeConnection(JNDINames.PDC_SUBSCRIPTION_DATASOURCE);
    try {
      List<PDCSubscription> subscriptions =
          PdcSubscriptionDAO.getPDCSubscriptionByUsedAxis(conn, axisId);
      if (subscriptions == null || subscriptions.isEmpty()) {
        return;
      }

      int[] removeIds = new int[subscriptions.size()];
      int removeLength = 0;
      for (PDCSubscription subscription : subscriptions) {
        // for each subscription containing axis affected by value deletion
        // check if any criteria value has been deleted
        if (checkSubscriptionRemove(subscription, axisId, oldPath, newPath)) {
          UserNotificationHelper.buildAndSend(
              new PdcSubscriptionDeletionUserNotification(subscription, axisName, true));
          removeIds[removeLength] = subscription.getId();
          removeLength++;
        }
      }
      int[] ids = new int[removeLength];
      System.arraycopy(removeIds, 0, ids, 0, removeLength);
      PdcSubscriptionDAO.removePDCSubscriptionById(conn, ids);
    } catch (SQLException e) {
      throw new PdcSubscriptionRuntimeException("PdcSubscriptionBmEJB.checkValueOnDelete",
          PdcSubscriptionRuntimeException.ERROR, "PdcSubscription.EX_CHECK_AXIS_FALIED", e);
    } finally {
      DBUtil.close(conn);
    }
  }

  /**
   * This method check is any subscription that match criterias provided and sends notification if
   * succeed
   *
   * @param classifyValues Linst of ClassifyValues to be checked
   * @param componentId component where classify event occures
   * @param silverObjectid object that was classified
   */
  @Override
  public void checkSubscriptions(List<? extends Value> classifyValues, String componentId,
      int silverObjectid) {
    SilverTrace.info("PdcSubscription", "PdcSubscriptionBmEJB.checkSubscriptions()",
        "root.MSG_GEN_ENTER_METHOD", "classifyValues = " + classifyValues + ", componentId = "
        + componentId + ", silverObjectid = " + silverObjectid);

    Connection conn = DBUtil.makeConnection(JNDINames.PDC_SUBSCRIPTION_DATASOURCE);
    SilverContentInterface silverContent = null;
    OrganisationController organizationController = OrganisationControllerFactory
        .getOrganisationController();

    try {
      ContentManager contentManager = new ContentManager();
      SilverContentVisibility scv = contentManager.getSilverContentVisibility(silverObjectid);
      boolean contentObjectIsVisible = (scv.isVisible() == 1);

      if(contentObjectIsVisible) {
        // load all PDCSubscritions into the memory to perform future check of them
        List<PDCSubscription> subscriptions = PdcSubscriptionDAO.getAllPDCSubscriptions(conn);
        // loop through all subscription
        for (PDCSubscription subscription : subscriptions) {
          // check if current subscription corresponds a list of classify values
          // provided into the method
          if (isCorrespondingSubscription(subscription, classifyValues)) {
            if (silverContent == null) {
              silverContent = getSilverContent(componentId, silverObjectid);
            }
            // The current subscription matches the new classification.
            // Now, we have to test if subscription's owner is allowed to access
            // the classified item.
            String userId = String.valueOf(subscription.getOwnerId());
            String[] roles = organizationController.getUserProfiles(userId, componentId);
            if (roles.length > 0) {
              // if user have got at least one role, sends a notification to the
              // user specified in pdcSubscription
              UserNotificationHelper.buildAndSend(
                  new PdcResourceClassificationUserNotification(subscription,
                      silverContent));
            }
          }
        }
      }

    } catch (Exception e) {
      throw new PdcSubscriptionRuntimeException("PdcSubscriptionBmEJB.checkSubscriptions",
          PdcSubscriptionRuntimeException.ERROR, "PdcSubscription.EX_CHECK_SUBSCR_FALIED",
          classifyValues, e);
    } finally {
      DBUtil.close(conn);
    }
  }

  /**
   * get the silverContent object according to the given silverObjectid
   *
   * @param componentId - the component where is classified the silverContent
   * @param silverObjectId - the unique identifier of the silverContent
   * @return SilverContentInterface the object which has been classified
   */
  private SilverContentInterface getSilverContent(String componentId, int silverObjectId) {
    ArrayList<Integer> silverobjectIds = new ArrayList<Integer>();
    silverobjectIds.add(silverObjectId);

    List<SilverContentInterface> silverContents;
    SilverContentInterface silverContent = null;
    try {
      ContentManager contentManager = new ContentManager();
      ContentPeas contentPeas = contentManager.getContentPeas(componentId);
      ContentInterface contentInterface = contentPeas.getContentInterface();
      ArrayList<String> userRoles = new ArrayList<String>();
      userRoles.add("admin");
      silverContents = contentInterface.getSilverContentById(silverobjectIds,
          componentId, "unknown", userRoles);
    } catch (Exception e) {
      throw new PdcSubscriptionRuntimeException(
          "PdcSubscriptionBmEJB.sendSubscriptionNotif",
          PdcSubscriptionRuntimeException.ERROR,
          "PdcSubscription.EX_CHECK_SUBSCR_FALIED", e);
    }
    if (silverContents != null && !silverContents.isEmpty()) {
      silverContent = silverContents.get(0);
    }

    return silverContent;
  }

  /**
   * @param subscription PDCSubscription to check
   * @param axisId id of the axis value of which should be removed
   * @param oldPath list of original axis paths (before deletion)
   * @param newPath list new axis path to be places instead of old path
   * @return true if subscription should be removed
   */
  protected boolean checkSubscriptionRemove(PDCSubscription subscription,
      int axisId, List<String> oldPath, List<String> newPath) {
    List<? extends Criteria> subscriptionCtx = subscription.getPdcContext();
    for (Criteria criteria : subscriptionCtx) {
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
   * @param originalPath
   * @param oldPath
   * @param newPath
   * @return true if path provided was removed should be removed
   */
  protected boolean checkValuesRemove(String originalPath, List<String> oldPath,
      List<String> newPath) {
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
  protected boolean checkValueInPath(String value, List<String> pathList) {
    for (String path : pathList) {
      String currentPath = path;
      if (currentPath == null) {
        // this means that first level has been removed, so use root path
        currentPath = "/0/";
      }
      if (currentPath.contains(value)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param subscription
   * @param classifyValues
   * @return true if subscription provided match the list of classify values
   */
  protected boolean isCorrespondingSubscription(PDCSubscription subscription,
      List<? extends Value> classifyValues) {
    SilverTrace.info("PdcSubscription", "PdcSubscriptionBmEJB.isCorrespondingSubscription()",
        "root.MSG_GEN_ENTER_METHOD", "subscription = " + subscription + ", classifyValues = "
        + classifyValues);
    List<? extends Criteria> searchCriterias = subscription.getPdcContext();
    if (searchCriterias == null || classifyValues == null || searchCriterias.isEmpty()
        || classifyValues.isEmpty() || searchCriterias.size() > classifyValues.size()) {
      return false;
    }

    /*
     * The following algorithm implemented Loop every SearchCriteria and for axis of SearchCriteria
     * found ClassifyValue with such axis if true check getValue() of ClassifyValue and getValue()
     * of SearchCriteria. The start of the value String of ClassifyValue should match the whole
     * value String of SearchCriteria.
     */
    for (Criteria criteria : searchCriterias) {
      if (criteria == null) {
        continue;
      }
      boolean result = false;
      for (Value value : classifyValues) {
        if (checkValues(criteria, value)) {
          result = true;
          break;
        }
      }
      if (!result) {
        return false;
      }
    }
    return true;
  }

  /**
   * @param criteria
   * @param searchValue
   * @return true if criteria provided match the searchValue provided
   */
  protected boolean checkValues(Criteria criteria, Value searchValue) {
    return searchValue.getAxisId() == criteria.getAxisId() &&
        searchValue.getValue().startsWith(criteria.getValue(), 0);
  }
}