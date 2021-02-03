/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

package org.silverpeas.core.admin.service;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.backgroundprocess.AbstractBackgroundProcessRequest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author silveryocha
 */
class SyncOfUsersContext {
  private final String domainId;
  private final boolean threaded;
  private final boolean addUserIntoSilverpeas;
  private final boolean delUsersOnDiffSync;
  private final StringBuilder report = new StringBuilder(5000);
  private final Map<String, UserDetail> addedUsers = new LinkedHashMap<>();
  private final Map<String, UserDetail> updatedUsers = new LinkedHashMap<>();
  private final Map<String, UserDetail> removedUsers = new LinkedHashMap<>();
  private final Map<String, UserDetail> restoredUsers = new LinkedHashMap<>();
  private final Map<String, UserDetail> deletedUsers = new LinkedHashMap<>();
  private AbstractBackgroundProcessRequest indexationBackgroundProcess;

  SyncOfUsersContext(final String domainId, final boolean threaded,
      final boolean addUserIntoSilverpeas, final boolean delUsersOnDiffSync) {
    this.domainId = domainId;
    this.threaded = threaded;
    this.addUserIntoSilverpeas = addUserIntoSilverpeas;
    this.delUsersOnDiffSync = delUsersOnDiffSync;
  }

  boolean isRemoveOperationToPerform() {
    return !threaded || delUsersOnDiffSync;
  }

  boolean isAddOperationToPerform() {
    return addUserIntoSilverpeas;
  }

  SyncOfUsersContext appendToReport(final String reportPart) {
    report.append(reportPart);
    return this;
  }

  String getDomainId() {
    return domainId;
  }

  String getReport() {
    return report.toString();
  }

  Map<String, UserDetail> getAddedUsers() {
    return addedUsers;
  }

  Map<String, UserDetail> getUpdatedUsers() {
    return updatedUsers;
  }

  Map<String, UserDetail> getRemovedUsers() {
    return removedUsers;
  }

  Map<String, UserDetail> getRestoredUsers() {
    return restoredUsers;
  }

  Map<String, UserDetail> getDeletedUsers() {
    return deletedUsers;
  }

  AbstractBackgroundProcessRequest getIndexationBackgroundProcess() {
    return indexationBackgroundProcess;
  }

  void setIndexationBackgroundProcess(
      final AbstractBackgroundProcessRequest indexationBackgroundProcess) {
    this.indexationBackgroundProcess = indexationBackgroundProcess;
  }
}
