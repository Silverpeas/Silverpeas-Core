/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.webactiv.applicationIndexer.control;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.OrganizationController;

/**
 * @author ehugonnet
 */
public abstract class AbstractIndexer {

  final OrganizationController organizationController = new OrganizationController();
  final AdminController admin = new AdminController(null);

  void setSilverTraceLevel() {
    SilverTrace.setTraceLevel(this.getClass().getName(), SilverTrace.TRACE_LEVEL_INFO);
  }

  public void indexAllSpaces() throws Exception {
    index(null, null);
  }

  public void index(String currentSpaceId, String componentId) throws Exception {
    setSilverTraceLevel();
    SilverTrace.info("applicationIndexer", "ApplicationIndexer.index()",
        "root.MSG_GEN_ENTER_METHOD");
    if (currentSpaceId == null) {
      // index whole application
      String[] spaceIds = organizationController.getAllSpaceIds();
      SilverTrace.info("applicationIndexer", "ApplicationIndexer.index()",
          "applicationIndexer.MSG_INDEXING_ALL_SPACES");
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
    SilverTrace
        .info("applicationIndexer", "ApplicationIndexer.index()", "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Indexes one space
   * @param spaceId space identifier
   * @throws Exception whether an exception occurred
   */
  public void indexSpace(String spaceId) throws Exception {
    SilverTrace.info(ApplicationDYMIndexer.class.toString(), "ApplicationDYMIndexer.indexSpace()",
        "applicationIndexer.MSG_START_INDEXING_SPACE", "spaceId = " + spaceId);

    if (spaceId.startsWith(Admin.SPACE_KEY_PREFIX)) {
      spaceId = spaceId.substring(2);
    }

    // index space info
    admin.indexSpace(Integer.parseInt(spaceId));

    // index components
    String[] componentIds = organizationController.getAllComponentIds(spaceId);
    for (String componentId : componentIds) {
      indexComponent(spaceId, componentId);
    }

    // index sub spaces
    String[] subSpaceIds = organizationController.getAllSubSpaceIds(spaceId);
    for (String subSpaceId : subSpaceIds) {
      indexSpace(subSpaceId);
    }

    SilverTrace.info(ApplicationDYMIndexer.class.toString(), "ApplicationDYMIndexer.indexSpace()",
        "applicationIndexer.MSG_END_INDEXING_SPACE", "spaceId = " + spaceId);
  }

  public void indexPersonalComponents() {
    indexPersonalComponent("Agenda");
    indexPersonalComponent("Todo");
  }

  public abstract void indexComponent(String spaceId, String componentId) throws Exception;

  public abstract void indexPersonalComponent(String personalComponent);
}
