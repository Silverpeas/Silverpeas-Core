/**
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.thumbnail.service;

import java.sql.Connection;
import java.sql.SQLException;

import com.silverpeas.thumbnail.ThumbnailException;
import com.silverpeas.thumbnail.model.ThumbnailDAO;
import com.silverpeas.thumbnail.model.ThumbnailDetail;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;

public class ThumbnailServiceImpl implements ThumbnailService {

  public ThumbnailServiceImpl() {
  }

  public ThumbnailDetail createThumbnail(ThumbnailDetail thumbDetail)
      throws ThumbnailException {
    Connection con = getConnection();
    try {
      return ThumbnailDAO.insertThumbnail(con, thumbDetail);
    } catch (SQLException se) {
      throw new ThumbnailException("ThumbnailBmImpl.createThumbnail()",
            SilverpeasException.ERROR,
            "thumbnail.EX_INSERT_ROW", se);
    } catch (UtilException e) {
      throw new ThumbnailException("ThumbnailBmImpl.createThumbnail()",
                SilverpeasException.ERROR,
                "thumbnail.EX_MSG_RECORD_NOT_INSERT", e);
    } finally {
      closeConnection(con);
    }
  }

  public void updateThumbnail(ThumbnailDetail thumbDetail)
      throws ThumbnailException {
    Connection con = getConnection();
    try {
      ThumbnailDAO.updateThumbnail(con, thumbDetail);
    } catch (SQLException se) {
      throw new ThumbnailException("ThumbnailBmImpl.updateAttachment()",
          SilverpeasException.ERROR,
          "thumbnail.EX_MSG_RECORD_NOT_UPDATE", se);
    } finally {
      closeConnection(con);
    }
  }

  public void deleteThumbnail(ThumbnailDetail thumbDetail)
      throws ThumbnailException {
    Connection con = getConnection();
    try {
      // delete thumbnail
      ThumbnailDAO.deleteThumbnail(con, thumbDetail.getObjectId(), thumbDetail.getObjectType(),
          thumbDetail.getInstanceId());
    } catch (SQLException se) {
      throw new ThumbnailException("ThumbnailBmImpl.deleteThumbnail()",
          SilverpeasException.ERROR, "thumbnail.EX_MSG_RECORD_NOT_DELETE", se);
    } finally {
      closeConnection(con);
    }
  }

  public ThumbnailDetail getCompleteThumbnail(ThumbnailDetail thumbDetail)
      throws ThumbnailException {
    Connection con = getConnection();
    try {
      // select thumbnail
      return ThumbnailDAO.selectByKey(con, thumbDetail.getInstanceId(), thumbDetail.getObjectId(),
          thumbDetail.getObjectType());
    } catch (SQLException se) {
      throw new ThumbnailException("ThumbnailBmImpl.getCompleteThumbnail()",
          SilverpeasException.ERROR, "thumbnail.EX_MSG_NOT_FOUND", se);
    } finally {
      closeConnection(con);
    }
  }

  private Connection getConnection() throws ThumbnailException {
    SilverTrace.info("thumbnail", "ThumbnailBmImpl.getConnection()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      Connection con = DBUtil.makeConnection(JNDINames.THUMBNAIL_DATASOURCE);
      return con;
    } catch (Exception e) {
      throw new ThumbnailException("ThumbnailBmImpl.getConnection()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  private void closeConnection(Connection con) {
    try {
      if (con != null)
        con.close();
    } catch (Exception e) {
      SilverTrace.error("thumbnail", "ThumbnailBmImpl.closeConnection()",
          "root.EX_CONNECTION_CLOSE_FAILED", "", e);
    }
  }

  public void deleteAllThumbnail(String componentId)
      throws ThumbnailException {
    Connection con = getConnection();
    try {
      // delete all thumbnails
      ThumbnailDAO.deleteAllThumbnails(con, componentId);
    } catch (SQLException se) {
      throw new ThumbnailException("ThumbnailBmImpl.deleteAllThumbnail()",
          SilverpeasException.ERROR, "thumbnail_MSG_DELETE_ALL_FAILED", se);
    } finally {
      closeConnection(con);
    }
  }
}