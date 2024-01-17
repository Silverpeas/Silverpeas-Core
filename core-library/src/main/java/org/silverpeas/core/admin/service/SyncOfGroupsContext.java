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

package org.silverpeas.core.admin.service;

import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.backgroundprocess.AbstractBackgroundProcessRequest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author silveryocha
 */
class SyncOfGroupsContext {
  private final String domainId;
  private final Map<String, String> userIdsMapping;
  private final StringBuilder report = new StringBuilder(5000);
  private final Map<String, GroupDetail> addedGroups = new LinkedHashMap<>();
  private final Map<String, GroupDetail> updatedGroups = new LinkedHashMap<>();
  private final Map<String, GroupDetail> removedGroups = new LinkedHashMap<>();
  private final Map<String, GroupDetail> restoredGroups = new LinkedHashMap<>();
  private AbstractBackgroundProcessRequest indexationBackgroundProcess;

  SyncOfGroupsContext(final String domainId, final Map<String, String> userIdsMapping) {
    this.domainId = domainId;
    this.userIdsMapping = userIdsMapping;
  }

  SyncOfGroupsContext appendToReport(final String reportPart) {
    report.append(reportPart);
    return this;
  }

  String getDomainId() {
    return domainId;
  }

  Map<String, String> getUserIdsMapping() {
    return userIdsMapping;
  }

  String getReport() {
    return report.toString();
  }

  Map<String, GroupDetail> getAddedGroups() {
    return addedGroups;
  }

  Map<String, GroupDetail> getUpdatedGroups() {
    return updatedGroups;
  }

  Map<String, GroupDetail> getRemovedGroups() {
    return removedGroups;
  }

  Map<String, GroupDetail> getRestoredGroups() {
    return restoredGroups;
  }

  AbstractBackgroundProcessRequest getIndexationBackgroundProcess() {
    return indexationBackgroundProcess;
  }

  void setIndexationBackgroundProcess(
      final AbstractBackgroundProcessRequest indexationBackgroundProcess) {
    this.indexationBackgroundProcess = indexationBackgroundProcess;
  }
}
