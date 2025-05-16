/*
 * Copyright (C) 2000 - 2025 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.node.service;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.model.ProfileInst;
import org.silverpeas.core.admin.user.notification.ProfileInstEvent;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.notification.system.CDIResourceEventListener;
import org.silverpeas.kernel.SilverpeasRuntimeException;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Listener of events about changes in a given right profile instance of a component instance.
 * For all the user groups removed from the profile instance related by the event, an invocation
 * to the {@link NodeProfileInstUpdater} is performed.
 *
 * @author mmoquillon
 */
@Service
public class NodeProfileInstEventListener extends CDIResourceEventListener<ProfileInstEvent> {

  @Inject
  private Administration admin;
  @Inject
  private NodeProfileInstUpdater updater;

  @Transactional
  @Override
  public void onUpdate(ProfileInstEvent event) {
    ProfileInst before = event.getTransition().getBefore();
    if (before.isOnComponentInstance()) {
      ProfileInst after = event.getTransition().getAfter();
      int instanceId = before.getComponentFatherId();
      ComponentInst instance = getComponentInstanceId(instanceId);
      List<String> groupsAfter = after.getAllGroups();
      Set<String> removedGroups = before.getAllGroups().stream()
          .filter(group -> !groupsAfter.contains(group))
          .collect(Collectors.toSet());
      updater.getRemoverFor(instance.getId())
          .ofGroups(removedGroups)
          .apply();
    }
  }

  private ComponentInst getComponentInstanceId(int localComponentId) {
    try {
      return admin.getComponentInst(String.valueOf(localComponentId));
    } catch (AdminException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

}
  