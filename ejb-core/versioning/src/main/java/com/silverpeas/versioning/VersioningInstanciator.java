/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.versioning;

import java.rmi.RemoteException;
import java.sql.Connection;

import javax.ejb.CreateException;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.silverpeas.admin.components.InstanciationException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.ejb.VersioningBm;
import com.stratelia.silverpeas.versioning.ejb.VersioningBmHome;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

public class VersioningInstanciator extends SQLRequest implements ComponentsInstanciatorIntf {

  private VersioningBm versioningBm = null;

  /** Creates new KmeliaInstanciator */
  public VersioningInstanciator() {
    super("com.silverpeas.versioning");
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
  }

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("versioning", "VersioningInstanciator.delete()",
        "root.MSG_GEN_ENTER_METHOD", "componentId = " + componentId);

    // 1 - delete data in database
    try {
      getVersioningBm().deleteDocumentsByInstanceId(componentId);
    } catch (Exception e) {
      throw new InstanciationException("VersioningInstanciator.delete()",
          InstanciationException.ERROR, "root.EX_RECORD_DELETE_FAILED", e);
    }

    // 2 - delete directory where files are stored
    String[] ctx = { "Versioning" };
    String path = FileRepositoryManager.getAbsolutePath(componentId, ctx);
    try {
      FileFolderManager.deleteFolder(path);
    } catch (Exception e) {
      throw new InstanciationException("VersioningInstanciator.delete()",
          InstanciationException.ERROR, "root.DELETING_DATA_DIRECTORY_FAILED",
          e);
    }
  }

  private VersioningBm getVersioningBm() throws UtilException, RemoteException,
      CreateException {
    if (versioningBm == null) {
      VersioningBmHome versioningBmHome = (VersioningBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.VERSIONING_EJBHOME, VersioningBmHome.class);
      versioningBm = versioningBmHome.create();

    }
    return versioningBm;
  }

}