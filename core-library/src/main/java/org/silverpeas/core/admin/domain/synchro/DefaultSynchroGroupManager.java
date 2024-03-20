/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.admin.domain.synchro;

import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.synchronizedSet;
import static org.silverpeas.kernel.util.StringUtil.isNotDefined;

@Service
public class DefaultSynchroGroupManager implements SynchroGroupManager {

  private final Set<String> synchronizedGroupIds = synchronizedSet(new HashSet<>());

  @Override
  public void resetContext() {
    try {
      synchronizedGroupIds.clear();
      Administration.get().getSynchronizedGroups().forEach(this::updateContextWith);
    } catch (AdminException e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  @Transactional
  @Override
  public void synchronize() {
    synchronized (synchronizedGroupIds) {
      SynchroGroupReport.startSynchro();
      synchronizedGroupIds.forEach(i -> {
        try {
          Administration.get().synchronizeGroupByRule(i, true);
        } catch (AdminException e) {
          SilverLogger.getLogger(this).error(e.getMessage(), e);
        }
      });
      SynchroGroupReport.stopSynchro();
    }
  }

  @Override
  public void updateContextWith(final Group group) {
    final String groupId = group.getId();
    if (isNotDefined(groupId)) {
      throw new IllegalArgumentException("Missing group identifier");
    }
    if (group.isSynchronized()) {
      synchronizedGroupIds.add(groupId);
    } else {
      synchronizedGroupIds.remove(groupId);
    }
  }

  @Override
  public void removeFromContext(final Group group) {
    final String groupId = group.getId();
    if (isNotDefined(groupId)) {
      throw new IllegalArgumentException("Missing group identifier");
    }
    synchronizedGroupIds.remove(groupId);
  }
}
