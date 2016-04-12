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
package org.silverpeas.core.web.index;

import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;

import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;

import org.silverpeas.core.admin.service.AdminController;

/**
 * @author ehugonnet
 */
public abstract class AbstractIndexer {

  protected final AdminController admin = ServiceProvider.getService(AdminController.class);
  private final static String silvertraceModule = "applicationIndexer";

  public void indexAllSpaces() throws Exception {
    index(null, null);
  }

  public void index(String currentSpaceId, String componentId) throws Exception {
    if (currentSpaceId == null) {
      // index whole application
      String[] spaceIds = OrganizationControllerProvider.getOrganisationController().getAllSpaceIds();

      for (String spaceId : spaceIds) {
        indexSpace(spaceId);
      }
    } else {
      if (!StringUtil.isDefined(componentId)) {
        // index whole space
        indexSpace(currentSpaceId);
      } else {
        // index only one component
        indexComponent(currentSpaceId, componentId);
      }
    }

  }

  /**
   * Indexes one space
   *
   * @param spaceId space identifier
   * @throws Exception whether an exception occurred
   */
  public void indexSpace(String spaceId) throws Exception {
    String currentSpaceId = spaceId;


    if (currentSpaceId.startsWith(SpaceInst.SPACE_KEY_PREFIX)) {
      currentSpaceId = currentSpaceId.substring(2);
    }

    // index space info
    admin.indexSpace(Integer.parseInt(currentSpaceId));

    // index components
    String[] componentIds = OrganizationControllerProvider.getOrganisationController()
        .getAllComponentIds(currentSpaceId);
    for (String componentId : componentIds) {
      indexComponent(currentSpaceId, componentId);
    }

    // index sub spaces
    String[] subSpaceIds = OrganizationControllerProvider.getOrganisationController()
        .getAllSubSpaceIds(currentSpaceId);
    for (String subSpaceId : subSpaceIds) {
      indexSpace(subSpaceId);
    }


  }

  public void indexPersonalComponents() {
    indexPersonalComponent("Agenda");
    indexPersonalComponent("Todo");
  }

  public abstract void indexComponent(String spaceId, String componentId) throws Exception;

  public abstract void indexPersonalComponent(String personalComponent);
}
