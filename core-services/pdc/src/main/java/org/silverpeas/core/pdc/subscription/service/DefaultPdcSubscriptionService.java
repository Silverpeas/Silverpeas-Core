/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.pdc.subscription.service;

import org.silverpeas.core.pdc.subscription.model.PdcSubscription;
import org.silverpeas.core.pdc.subscription.model.PdcSubscriptionRuntimeException;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.pdc.classification.Criteria;
import org.silverpeas.core.pdc.classification.Value;
import org.silverpeas.core.contribution.contentcontainer.content.ContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentPeas;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentVisibility;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Transactional
public class DefaultPdcSubscriptionService implements PdcSubscriptionService {

  /**
   * Remote interface method
   *
   * @param userId
   */
  @Override
  public List<PdcSubscription> getPDCSubscriptionByUserId(int userId) {
    try(Connection conn = DBUtil.openConnection()) {
      return PdcSubscriptionDAO.getPDCSubscriptionByUserId(conn, userId);
    } catch (SQLException re) {
      throw new PdcSubscriptionRuntimeException("PdcSubscriptionBmEJB.getPDCSubscriptionByUserId",
          PdcSubscriptionRuntimeException.ERROR,
          "PdcSubscription.CANNOT_FIND_SUBSCRIPTION_BYUSID", String.valueOf(userId), re);
    }
  }

  /**
   * Remote interface method
   *
   * @param id
   * @return
   */
  @Override
  public PdcSubscription getPDCSubsriptionById(int id) {
    PdcSubscription result = null;
    try(Connection conn = DBUtil.openConnection()) {
      result = PdcSubscriptionDAO.getPDCSubsriptionById(conn, id);
    } catch (SQLException re) {
      throw new PdcSubscriptionRuntimeException("PdcSubscriptionBmEJB.getPDCSubsriptionById",
          PdcSubscriptionRuntimeException.ERROR, "PdcSubscription.CANNOT_FIND_SUBSCRIPTION_BYUSID",
          String.valueOf(id), re);
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
  @Transactional(Transactional.TxType.REQUIRED)
  public int createPDCSubscription(PdcSubscription subscription) {
    try(Connection conn = DBUtil.openConnection()) {
      return PdcSubscriptionDAO.createPDCSubscription(conn, subscription);
    } catch (Exception re) {
      throw new PdcSubscriptionRuntimeException("PdcSubscriptionBmEJB.createPDCSubscription",
          PdcSubscriptionRuntimeException.ERROR, "PdcSubscription.CANNOT_CREATE_SUBSCRIPTION",
          subscription, re);
    }
  }

  /**
   * Remote interface method
   *
   * @param subscription
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void updatePDCSubscription(PdcSubscription subscription) {
    try(Connection conn = DBUtil.openConnection()) {
      PdcSubscriptionDAO.updatePDCSubscription(conn, subscription);
    } catch (SQLException re) {
      throw new PdcSubscriptionRuntimeException("PdcSubscriptionBmEJB.updatePDCSubscription",
          PdcSubscriptionRuntimeException.ERROR, "PdcSubscription.CANNOT_UPDATE_SUBSCRIPTION",
          subscription, re);
    }
  }

  /**
   * Remote interface method
   *
   * @param id
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void removePDCSubscriptionById(int id) {
    try(Connection conn = DBUtil.openConnection()) {
      PdcSubscriptionDAO.removePDCSubscriptionById(conn, id);
    } catch (SQLException re) {
      throw new PdcSubscriptionRuntimeException("PdcSubscriptionBmEJB.removePDCSubscriptionById",
          PdcSubscriptionRuntimeException.ERROR, "PdcSubscription.CANNOT_DELETE_SUBSCRIPTION",
          String.valueOf(id), re);
    }
  }

  /**
   * Remote interface method
   *
   * @param ids
   */
  @Override
  @Transactional(Transactional.TxType.REQUIRED)
  public void removePDCSubscriptionById(int[] ids) {
    try(Connection conn = DBUtil.openConnection()) {
      PdcSubscriptionDAO.removePDCSubscriptionById(conn, ids);
    } catch (SQLException re) {
      throw new PdcSubscriptionRuntimeException("PdcSubscriptionBmEJB.removePDCSubscriptionById",
          PdcSubscriptionRuntimeException.ERROR, "PdcSubscription.CANNOT_DELETE_SUBSCRIPTION",
          Arrays.toString(ids), re);
    }
  }

  /**
   * Remote inteface method Implements PdcSubscription check for axis deletion. It deletes all
   * references to this axis from PdcSubscription module DB
   *
   * @param axisId the axis to be checked
   * @param axisName the name of the axis
   */
  @Override
  public void checkAxisOnDelete(int axisId, String axisName) {
    try(Connection conn = DBUtil.openConnection()) {
      // found all subscription uses axis provided
      List<PdcSubscription> subscriptions = PdcSubscriptionDAO.getPDCSubscriptionByUsedAxis(conn,
          axisId);
      if (subscriptions == null || subscriptions.isEmpty()) {
        return;
      }
      int[] pdcIds = new int[subscriptions.size()];
      for (int i = 0; i < subscriptions.size(); i++) {
        PdcSubscription subscription = subscriptions.get(i);
        pdcIds[i] = subscription.getId();
        UserNotificationHelper.buildAndSend(
            new PdcSubscriptionDeletionUserNotification(subscription, axisName, false));
      }
      // remove all found subscriptions
      PdcSubscriptionDAO.removePDCSubscriptionById(conn, pdcIds);
    } catch (SQLException e) {
      throw new PdcSubscriptionRuntimeException("PdcSubscriptionBmEJB.checkAxisOnDelete",
          PdcSubscriptionRuntimeException.ERROR, "PdcSubscription.EX_CHECK_AXIS_FALIED", e);
    }
  }

  /**
   * Implements PdcSubscription check for value deletion. It deletes all references to the path
   * containing this value from PdcSubscription module DB
   *
   * @param axisId the axis to be checked
   * @param axisName the name of the axis
   * @param oldPath old path that would be removed soon
   * @param newPath new path. That will be places instead of old for this axis
   * @param pathInfo should contains PdcBm.getFullPath data structure
   */
  @Override
  public void checkValueOnDelete(int axisId, String axisName, List<String> oldPath,
      List<String> newPath, List<org.silverpeas.core.pdc.pdc.model.Value> pathInfo) {
    try(Connection conn = DBUtil.openConnection()) {
      List<PdcSubscription> subscriptions =
          PdcSubscriptionDAO.getPDCSubscriptionByUsedAxis(conn, axisId);
      if (subscriptions == null || subscriptions.isEmpty()) {
        return;
      }

      int[] removeIds = new int[subscriptions.size()];
      int removeLength = 0;
      for (PdcSubscription subscription : subscriptions) {
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
    OrganizationController organizationController = OrganizationControllerProvider
        .getOrganisationController();

    try(Connection conn = DBUtil.openConnection()) {
      ContentManager contentManager = new ContentManager();
      SilverContentVisibility scv = contentManager.getSilverContentVisibility(silverObjectid);
      boolean contentObjectIsVisible = (scv.isVisible() == 1);

      if(contentObjectIsVisible) {
        // load all PDCSubscritions into the memory to perform future check of them
        List<PdcSubscription> subscriptions = PdcSubscriptionDAO.getAllPDCSubscriptions(conn);
        // loop through all subscription
        for (PdcSubscription subscription : subscriptions) {
          // check if current subscription corresponds a list of classify values
          // provided into the method
          if (isCorrespondingSubscription(subscription, classifyValues)) {
            // The current subscription matches the new classification.
            // Now, we have to test if subscription's owner is allowed to access
            // the classified item.
            String userId = String.valueOf(subscription.getOwnerId());
            if (organizationController.isComponentAvailable(componentId, userId)) {
              // if user is able to see component which contains content
              SilverContentInterface silverContent =
                  getSilverContent(componentId, silverObjectid, userId);
              if (silverContent != null) {
                // if user is able to see this content, sends a notification to the
                // user specified in the pdcSubscription
                UserNotificationHelper.buildAndSend(
                    new PdcResourceClassificationUserNotification(subscription,
                        silverContent));
              } else {
                SilverLogger.getLogger(this).warn("User {0} now alloawed to see the content {1}",
                    userId, silverObjectid);
              }
            }
          }
        }
      }
    } catch (Exception e) {
      throw new PdcSubscriptionRuntimeException("PdcSubscriptionBmEJB.checkSubscriptions",
          PdcSubscriptionRuntimeException.ERROR, "PdcSubscription.EX_CHECK_SUBSCR_FALIED",
          classifyValues, e);
    }
  }

  /**
   * get the silverContent object according to the given silverObjectid
   *
   * @param componentId - the component where is classified the silverContent
   * @param silverObjectId - the unique identifier of the silverContent
   * @return SilverContentInterface the object which has been classified
   */
  private SilverContentInterface getSilverContent(String componentId, int silverObjectId, String userId) {
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
          componentId, userId, userRoles);
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
   * @param subscription PdcSubscription to check
   * @param axisId id of the axis value of which should be removed
   * @param oldPath list of original axis paths (before deletion)
   * @param newPath list new axis path to be places instead of old path
   * @return true if subscription should be removed
   */
  protected boolean checkSubscriptionRemove(PdcSubscription subscription,
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
  protected boolean isCorrespondingSubscription(PdcSubscription subscription,
      List<? extends Value> classifyValues) {
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