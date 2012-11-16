/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.importExport.control;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationControllerFactory;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

/**
 * @author neysseri
 */
public class ImportExportHelper {


  public static boolean isVersioningUsed(ComponentInst componentInst) {
    return StringUtil.getBooleanValue(componentInst.getParameterValue("versionControl"));
  }

  public static boolean isDraftUsed(ComponentInst componentInst) {
    return StringUtil.getBooleanValue(componentInst.getParameterValue("draft"));
  }

  public static boolean isIndexable(PublicationDetail pubDetail) {
    return pubDetail.isIndexable();
  }

  public static String checkUserId(String userId, UserDetail importer) {
    if (OrganizationControllerFactory.getOrganizationController().getUserDetail(userId) != null) {
      return userId;
    }
    return importer.getId();
  }
}
