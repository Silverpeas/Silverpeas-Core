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
package org.silverpeas.core.silverstatistics.volume.service;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.silverstatistics.volume.model.UserIdCountVolumeCouple;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * This is the alimentation for the statistics on volume. It gets the number of elements from each
 * components from each space. All components must implements the ComponentStatisticsInterface.
 * @author sleroux
 */
class SilverStatisticsVolumeAlimentation {

  static void makeVolumeAlimentationForAllComponents() {
    java.util.Date now = new java.util.Date();
    // get all spaces
    final List<String> listAllSpacesId = getAllSpacesAndAllSubSpacesId();

    if (!listAllSpacesId.isEmpty()) {

      for (String currentSpaceId : listAllSpacesId) {
        // get all components from a space
        List<ComponentInst> listAllComponentsInst = getAllComponentsInst(currentSpaceId);

        for (ComponentInst ci : listAllComponentsInst) {
          String currentComponentsId = ci.getId();
          // get all elements from a component
          getCollectionUserIdCountVolume(currentSpaceId, ci).forEach(v ->
              // notify statistics
              SilverStatisticsManager.getInstance()
                  .addStatVolume(v.getUserId(), v.getCountVolume(), now, ci.getName(),
                      currentSpaceId, currentComponentsId));
        }
      }
    }
  }

  private static List<String> getAllSpacesAndAllSubSpacesId() {
    List<String> resultList = new ArrayList<>();
    String[] spaceIds = getAdminController().getAllSpaceIds();
    if (spaceIds != null) {
      resultList.addAll(Arrays.asList(spaceIds));
    }
    return resultList;
  }

  private static List<ComponentInst> getAllComponentsInst(String spaceId) {
    SpaceInst mySpaceInst = getAdminController().getSpaceInstById(spaceId);
    return mySpaceInst.getAllComponentsInst();
  }

  private static Collection<UserIdCountVolumeCouple> getCollectionUserIdCountVolume(String spaceId,
      ComponentInst ci) {
    Collection<UserIdCountVolumeCouple> result = Collections.emptyList();
    try {
      final Optional<ComponentStatisticsProvider> statistics = ComponentStatisticsProvider
          .getByComponentName(ci.getName());
      if (statistics.isPresent()) {
        Collection<UserIdCountVolumeCouple> v = statistics.get().getVolume(spaceId, ci.getId());
        result = aggregateUser(v);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(SilverStatisticsVolumeAlimentation.class).warn(e.getMessage(), e);
    }
    return result;
  }

  private static Collection<UserIdCountVolumeCouple> aggregateUser(
      Collection<UserIdCountVolumeCouple> in) {

    if (CollectionUtil.isEmpty(in)) {
      return Collections.emptyList();
    }
    List<UserIdCountVolumeCouple> myArrayList = new ArrayList<>(in.size());

    // loop on initial collection
    for (UserIdCountVolumeCouple eltIn : in) {
      UserIdCountVolumeCouple eltOut = getCouple(myArrayList, eltIn);
      if (eltOut == null) {
        // no user matching
        myArrayList.add(eltIn);
      } else {
        // user matching
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
