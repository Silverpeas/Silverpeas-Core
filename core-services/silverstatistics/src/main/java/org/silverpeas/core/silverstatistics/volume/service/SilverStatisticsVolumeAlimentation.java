/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.silverstatistics.volume.service;

import org.silverpeas.core.silverstatistics.volume.model.UserIdCountVolumeCouple;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * This is the alimentation for the statistics on volume. It gets the number of elements from each
 * components from each space. All components must implements the ComponentStatisticsInterface.
 * @author sleroux
 */
public class SilverStatisticsVolumeAlimentation {

  private static final SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.silverstatistics.SilverStatistics");

  /**
   * Method declaration
   * @see
   */
  public static void makeVolumeAlimentationForAllComponents() {
    java.util.Date now = new java.util.Date();
    // get all spaces
    List<String> listAllSpacesId = getAllSpacesAndAllSubSpacesId();

    if (listAllSpacesId != null && !listAllSpacesId.isEmpty()) {

      for (String currentSpaceId : listAllSpacesId) {
        // get all components from a space
        List<ComponentInst> listAllComponentsInst = getAllComponentsInst(currentSpaceId);

        for (ComponentInst ci : listAllComponentsInst) {
          String currentComponentsId = ci.getId();
          // get all elements from a component
          Collection<UserIdCountVolumeCouple> collectionUserIdCountVolume =
              getCollectionUserIdCountVolume(currentSpaceId, ci);

          if (collectionUserIdCountVolume != null) {
            for (UserIdCountVolumeCouple currentUserIdCountVolume : collectionUserIdCountVolume) {
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
    String[] spaceIds = getAdminController().getAllSpaceIds();
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
    SpaceInst mySpaceInst = getAdminController().getSpaceInstById(spaceId);
    return mySpaceInst.getAllComponentsInst();
  }

  /**
   * Method declaration
   * @param spaceId
   * @param ci
   * @return
   * @see
   */
  private static Collection<UserIdCountVolumeCouple> getCollectionUserIdCountVolume(String spaceId,
      ComponentInst ci) {
    Collection<UserIdCountVolumeCouple> c = null;
    try {
      String qualifier = getComponentStatisticsQualifier(ci.getName());
      if (StringUtil.isDefined(qualifier)) {
        ComponentStatisticsProvider statistics = ServiceProvider.getService(qualifier);
        Collection<UserIdCountVolumeCouple> v = statistics.getVolume(spaceId, ci.getId());
        c = agregateUser(v);
      }
    } catch (Exception e) {
      SilverTrace.error("silverstatistics",
          "SilverStatisticsVolumeAlimentation.getCollectionUserIdCountVolume()",
          "silverstatistics.EX_SUPPLY_VOLUME_COMPONENT_FAILED",
          "component = " + ci.getName(), e);
    }
    return c;
  }

  /**
   * Gets the component statistics qualifier defined into SilverStatistics.properties file
   * associated to the component identified by the given name.<br/>
   * If no qualifier is defined for the component, that is because it does not exist statistics
   * treatment for the component.
   * @param componentName the name of the component for which the qualifier is searched.
   * @return a string that represents the qualifier name of the implementation of the statistic
   * treatment associated to the aimed component, empty if no qualifier.
   */
  private static String getComponentStatisticsQualifier(String componentName) {
    return settings.getString(componentName, "");
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
      if (eltOut == null) {
        myArrayList.add(eltIn);
      } else {
        eltOut.setCountVolume(eltIn.getCountVolume() + eltOut.getCountVolume());
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

  private static AdminController getAdminController() {
    return ServiceProvider.getService(AdminController.class);
  }

  private SilverStatisticsVolumeAlimentation() {
  }
}
