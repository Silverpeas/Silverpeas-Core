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

package com.silverpeas.thumbnail;

import java.sql.Connection;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.silverpeas.admin.components.InstanciationException;
import com.silverpeas.thumbnail.service.ThumbnailService;
import com.silverpeas.thumbnail.service.ThumbnailServiceImpl;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

public class ThumbnailInstanciator extends SQLRequest implements ComponentsInstanciatorIntf {

  private ThumbnailService thumbnailService = null;

  public ThumbnailInstanciator() {
  }

  public ThumbnailInstanciator(String fullPathName) {
    super("com.silverpeas.thumbnail");
  }

  private ThumbnailService getThumbnailBm() {
    if (thumbnailService == null) {
      thumbnailService = new ThumbnailServiceImpl();
    }
    return thumbnailService;
  }

  @Override
  public void create(Connection connection, String spaceId, String componentId,
      String userId) throws InstanciationException {
    // TODO Auto-generated method stub

  }

  @Override
  public void delete(Connection connection, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("thumbnail", "ThumbnailInstanciator.delete()",
        "root.MSG_GEN_ENTER_METHOD", "componentId = " + componentId);

    // 1 - delete data in database
    try {
      getThumbnailBm().deleteAllThumbnail(componentId);
    } catch (Exception e) {
      throw new InstanciationException("ThumbnailInstanciator.delete()",
          InstanciationException.ERROR, "root.EX_RECORD_DELETE_FAILED", e);
    }

    // 2 - delete directory where files are stored
    String[] ctx = { "thumbnail" };
    String path = FileRepositoryManager.getAbsolutePath(componentId, ctx);
    try {
      FileFolderManager.deleteFolder(path);
    } catch (Exception e) {
      throw new InstanciationException("ThumbnailInstanciator.delete()",
          InstanciationException.ERROR, "root.DELETING_DATA_DIRECTORY_FAILED",
          e);
    }
    SilverTrace.info("thumbnail", "ThumbnailInstanciator.delete()",
        "root.MSG_GEN_EXIT_METHOD");

  }

}