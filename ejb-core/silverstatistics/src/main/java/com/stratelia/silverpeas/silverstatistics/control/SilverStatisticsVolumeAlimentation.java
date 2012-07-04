/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.silverstatistics.control;

import com.silverpeas.silverstatistics.ComponentStatisticsInterface;
import com.silverpeas.silverstatistics.UserIdCountVolumeCouple;
import com.silverpeas.util.FileUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * This is the alimentation for the statistics on volume. It gets the number of elements from each
 * components from each space. All components must implements the ComponentStatisticsInterface.
 * @author sleroux
 */
public class SilverStatisticsVolumeAlimentation {

  private static ResourceBundle resources = null;
  private static final AdminController myAdminController = new AdminController("");

  static {
    try {
      resources = FileUtil.loadBundle("com.stratelia.silverpeas.silverstatistics.SilverStatistics",
          Locale.getDefault());
    } catch (Exception ex) {
      SilverTrace.error("silverstatistics", "SilverStatisticsVolumeAlimentation",
          "root.EX_CLASS_NOT_INITIALIZED", ex);
    }
  }

  /**
   * Method declaration
   * @see
   */
  public static void makeVolumeAlimentationForAllComponents() {
    java.util.Date now = new java.util.Date();
    // get all spaces
    List<String> listAllSpacesId = getAllSpacesAndAllSubSpacesId();

    if (listAllSpacesId != null && !listAllSpacesId.isEmpty()) {

      for (String aListAllSpacesId : listAllSpacesId) {
        // get all components from a space
        String currentSpaceId = aListAllSpacesId;
        List<ComponentInst> listAllComponentsInst = getAllComponentsInst(currentSpaceId);

        for (ComponentInst ci : listAllComponentsInst) {
          String currentComponentsId = ci.getId();
          // get all elements from a component
          Collection<UserIdCountVolumeCouple> collectionUserIdCountVolume =
              getCollectionUserIdCountVolume(
              currentSpaceId, ci);

          if (collectionUserIdCountVolume != null) {
            for (UserIdCountVolumeCouple currentUserIdCountVolume : collectionUserIdCountVolume) {
              SilverTrace.debug("silverstatistics",
                  "SilverStatisticsVolumeAlimentation.makeVolumeAlimentationForAllComponents",
                  "userId= " + currentUserIdCountVolume.getUserId() + " countVolume=  "
                  + currentUserIdCountVolume.getCountVolume() + " name= " + ci.getName()
                  + " spaceId= " + currentSpaceId + " compoId= " + currentComponentsId);

              // notify statistics
              SilverStatisticsManager.getInstance().addStatVolume(
                  currentUserIdCountVolume.getUserId(),
                  currentUserIdCountVolume.getCountVolume(), now, ci.getName(), currentSpaceId,
                  currentComponentsId);
            }
          }
        }
      }
    }
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  private static List<String> getAllSpacesAndAllSubSpacesId() {
    List<String> resultList = new ArrayList<String>();
    String[] spaceIds = myAdminController.getAllSpaceIds();
    if (spaceIds != null) {
      resultList.addAll(Arrays.asList(spaceIds));
    }
    return resultList;
  }

  /**
   * Method declaration
   * @param spaceId
   * @return
   * @see
   */
  private static List<ComponentInst> getAllComponentsInst(String spaceId) {
    SpaceInst mySpaceInst = myAdminController.getSpaceInstById(spaceId);
    return mySpaceInst.getAllComponentsInst();
  }

  /**
   * Method declaration
   * @param spaceId
   * @param componentId
   * @return
   * @see
   */
  private static Collection<UserIdCountVolumeCouple> getCollectionUserIdCountVolume(String spaceId,
      ComponentInst ci) {
    Collection<UserIdCountVolumeCouple> c = null;
    try {
      SilverTrace.info("silverstatistics",
          "SilverStatisticsVolumeAlimentation.getCollectionUserIdCountVolume()",
          "root.MSG_GEN_PARAM_VALUE", "spaceId=" + spaceId);
      SilverTrace.info(
          "silverstatistics",
          "SilverStatisticsVolumeAlimentation.getCollectionUserIdCountVolume()",
          "root.MSG_GEN_PARAM_VALUE", "componentId=" + ci.getId());
      String className = getComponentStatisticsClassName(ci.getName());
      if (className != null) {
        ComponentStatisticsInterface myCompo = (ComponentStatisticsInterface) Class.forName(
            className).newInstance();
        Collection<UserIdCountVolumeCouple> v = (Collection<UserIdCountVolumeCouple>) myCompo.
            getVolume(spaceId, ci.getId());
        c = agregateUser(v);
      }
    } catch (ClassNotFoundException ce) {
      SilverTrace.info("silverstatistics",
          "SilverStatisticsVolumeAlimentation.getCollectionUserIdCountVolume()",
          "silverstatistics.EX_SUPPLY_VOLUME_COMPONENT_NOT_FOUND",
          "component = " + ci.getName(), ce);
    } catch (Exception e) {
      SilverTrace.error("silverstatistics",
          "SilverStatisticsVolumeAlimentation.getCollectionUserIdCountVolume()",
          "silverstatistics.EX_SUPPLY_VOLUME_COMPONENT_FAILED",
          "component = " + ci.getName(), e);
    }
    return c;
  }

  private static String getComponentStatisticsClassName(String componentName) {
    String componentStatisticsClassName;

    try {
      componentStatisticsClassName = resources.getString(componentName);
    } catch (MissingResourceException e) {
      componentStatisticsClassName = null;
      SilverTrace.error("silverstatistics",
          "SilverStatisticsVolumeAlimentation.getCollectionUserIdCountVolume()",
          "silverstatistics.EX_SUPPLY_VOLUME_COMPONENT_FAILED",
          "No statistic implementation class for component '" + componentName + "'");
    }

    return componentStatisticsClassName;
  }

  private static Collection<UserIdCountVolumeCouple> agregateUser(
      Collection<UserIdCountVolumeCouple> in) {

    if (in == null) {
      return null;
    }
    List<UserIdCountVolumeCouple> myArrayList = new ArrayList<UserIdCountVolumeCouple>(in.size());

    // parcours collection initiale
    for (UserIdCountVolumeCouple eltIn : in) {
      // lecture d'un userId
      // s'il n'existe pas dans la collection finale alors on l'ajoute
      // sinon on modifie le countVolume et on passe au suivant
      UserIdCountVolumeCouple eltOut = getCouple(myArrayList, eltIn);
      SilverTrace.debug("silverstatistics", "SilverStatisticsVolumeAlimentation.agregateUser)",
          "eltIn.getUserId() = " + eltIn.getUserId() + "eltIn.getCountVolume() = " + eltIn.
          getCountVolume());
      if (eltOut == null) {
        myArrayList.add(eltIn);
        SilverTrace.debug("silverstatistics", "SilverStatisticsVolumeAlimentation.agregateUser)",
            "add eltIn");
      } else {
        eltOut.setCountVolume(eltIn.getCountVolume() + eltOut.getCountVolume());
        SilverTrace.debug("silverstatistics", "SilverStatisticsVolumeAlimentation.agregateUser)",
            "eltOut.getUserId() = " + eltOut.getUserId() + "eltOut.getCountVolume() = " + eltOut.
            getCountVolume());
      }
    }
    return myArrayList;
  }

  private static UserIdCountVolumeCouple getCouple(Collection<UserIdCountVolumeCouple> in,
      UserIdCountVolumeCouple eltIn) {
    for (UserIdCountVolumeCouple elt : in) {
      if (elt.getUserId().equals(eltIn.getUserId())) {
        return elt;
      }
    }
    return null;
  }

  private SilverStatisticsVolumeAlimentation() {
  }
}
