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
package org.silverpeas.core.web.index;

import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

/**
 * @author ehugonnet
 */
public abstract class AbstractIndexer {

  protected final AdminController admin = ServiceProvider.getService(AdminController.class);
  private boolean indexAllProcess = false;

  public final void indexAll() {
    indexAllProcess = true;
    IndexEngineProxy.removeAllIndexEntries();
    indexAllData();
  }

  protected abstract void indexAllData();

  public final void indexAllSpaces() {
    if (!isIndexAllProcess()) {
      admin.deleteAllSpaceIndexes();
      admin.deleteAllComponentIndexes();
    }
    index(null, null);
  }

  public void index(String currentSpaceId, String componentId) {
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
   */
  private void indexSpace(String spaceId) {
    SilverLogger.getLogger(this).debug("starting space indexation with id ''{0}''", spaceId);

    String currentSpaceId = spaceId;
    try {

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

    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .error("failure while indexing space with id ''{0}''", new Object[]{spaceId}, e);
    }
    SilverLogger.getLogger(this).debug("ending space indexation with id ''{0}''", spaceId);
  }

  protected void indexPersonalComponents() {
    indexPersonalComponent("Todo");
  }

  protected boolean isIndexAllProcess() {
    return indexAllProcess;
  }

  public abstract void indexComponent(String spaceId, String componentId);

  public abstract void indexPersonalComponent(String personalComponent);
}
