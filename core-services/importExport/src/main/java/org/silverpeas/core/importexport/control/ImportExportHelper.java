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
package org.silverpeas.core.importexport.control;

import org.silverpeas.core.admin.service.OrganizationControllerProvider;

import org.silverpeas.core.util.StringUtil;

import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;

import static org.silverpeas.core.contribution.attachment.AttachmentService.VERSION_MODE;

/**
 * @author neysseri
 */
public class ImportExportHelper {

  public static boolean isVersioningUsed(final String componentId) {
    return isVersioningUsed(OrganizationControllerProvider.getOrganisationController()
        .getComponentInst(componentId));
  }

  public static boolean isVersioningUsed(final ComponentInst component) {
    return StringUtil.getBooleanValue(component.getParameterValue(VERSION_MODE));
  }

  public static boolean isDraftUsed(ComponentInst componentInst) {
    return StringUtil.getBooleanValue(componentInst.getParameterValue("draft"));
  }

  public static boolean isIndexable(PublicationDetail pubDetail) {
    return pubDetail.isIndexable();
  }

  public static String checkUserId(String userId, UserDetail importer) {
    if (OrganizationControllerProvider.getOrganisationController().getUserDetail(userId) != null) {
      return userId;
    }
    return importer.getId();
  }
}
