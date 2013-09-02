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

package com.silverpeas.thumbnail.service;

import java.sql.Connection;
import java.sql.SQLException;

import com.silverpeas.thumbnail.ThumbnailException;
import com.silverpeas.thumbnail.model.ThumbnailDAO;
import com.silverpeas.thumbnail.model.ThumbnailDetail;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;

public class ThumbnailServiceImpl implements ThumbnailService {

  private ThumbnailDAO dao;

  public ThumbnailServiceImpl() {
    dao = new ThumbnailDAO();
  }

  @Override
  public ThumbnailDetail createThumbnail(ThumbnailDetail thumbDetail) throws ThumbnailException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.THUMBNAIL_DATASOURCE);
      return dao.insertThumbnail(con, thumbDetail);
    } catch (SQLException se) {
      throw new ThumbnailException("ThumbnailBmImpl.createThumbnail()",
          SilverpeasException.ERROR,
          "thumbnail.EX_INSERT_ROW", se);
    } catch (UtilException e) {
      throw new ThumbnailException("ThumbnailBmImpl.createThumbnail()",
          SilverpeasException.ERROR,
          "thumbnail.EX_MSG_RECORD_NOT_INSERT", e);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void updateThumbnail(ThumbnailDetail thumbDetail) throws ThumbnailException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.THUMBNAIL_DATASOURCE);
      dao.updateThumbnail(con, thumbDetail);
    } catch (SQLException se) {
      throw new ThumbnailException("ThumbnailBmImpl.updateAttachment()",
          SilverpeasException.ERROR,
          "thumbnail.EX_MSG_RECORD_NOT_UPDATE", se);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void deleteThumbnail(ThumbnailDetail thumbDetail) throws ThumbnailException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.THUMBNAIL_DATASOURCE);
      dao.deleteThumbnail(con, thumbDetail.getObjectId(), thumbDetail.getObjectType(),
          thumbDetail.getInstanceId());
    } catch (SQLException se) {
      throw new ThumbnailException("ThumbnailBmImpl.deleteThumbnail()",
          SilverpeasException.ERROR, "thumbnail.EX_MSG_RECORD_NOT_DELETE", se);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public ThumbnailDetail getCompleteThumbnail(ThumbnailDetail thumbDetail)
      throws ThumbnailException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.THUMBNAIL_DATASOURCE);
      return dao.selectByKey(con, thumbDetail.getInstanceId(), thumbDetail.getObjectId(),
          thumbDetail.getObjectType());
    } catch (SQLException se) {
      throw new ThumbnailException("ThumbnailBmImpl.getCompleteThumbnail()",
          SilverpeasException.ERROR, "thumbnail.EX_MSG_NOT_FOUND", se);
    } finally {
      DBUtil.close(con);
    }
  }

  @Override
  public void deleteAllThumbnail(String componentId) throws ThumbnailException {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.THUMBNAIL_DATASOURCE);
      // delete all thumbnails
      dao.deleteAllThumbnails(con, componentId);
    } catch (SQLException se) {
      throw new ThumbnailException("ThumbnailBmImpl.deleteAllThumbnail()",
          SilverpeasException.ERROR, "thumbnail_MSG_DELETE_ALL_FAILED", se);
    } finally {
      DBUtil.close(con);
    }
  }
}