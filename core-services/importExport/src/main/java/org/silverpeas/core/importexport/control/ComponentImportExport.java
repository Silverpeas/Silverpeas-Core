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

package org.silverpeas.core.importexport.control;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

public class ComponentImportExport {

  final SettingBundle resources =
      ResourceLocator.getSettingBundle("org.silverpeas.importExport.settings.importSettings");

  private UserDetail currentUserDetail = null;
  private String currentComponentId = null;

  public ComponentImportExport() {
  }

  public ComponentImportExport(UserDetail currentUserDetail, String currentComponentId) {
    this.currentUserDetail = currentUserDetail;
    this.currentComponentId = currentComponentId;
  }

  public UserDetail getCurrentUserDetail() {
    return currentUserDetail;
  }

  public String getCurrentComponentId() {
    return currentComponentId;
  }

  public void setCurrentComponentId(String currentComponentId) {
    this.currentComponentId = currentComponentId;
  }

}
