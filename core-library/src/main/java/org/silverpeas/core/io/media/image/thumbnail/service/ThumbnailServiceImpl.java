/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.io.media.image.thumbnail.service;

import org.silverpeas.core.io.media.image.thumbnail.ThumbnailException;
import org.silverpeas.core.io.media.image.thumbnail.ThumbnailRuntimeException;
import org.silverpeas.core.io.media.image.thumbnail.model.ThumbnailDAO;
import org.silverpeas.core.io.media.image.thumbnail.model.ThumbnailDetail;
import org.silverpeas.core.io.media.image.thumbnail.model.ThumbnailReference;
import org.silverpeas.core.persistence.jdbc.DBUtil;

import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

@Singleton
@Default
public class ThumbnailServiceImpl implements ThumbnailService {

  @Inject
  private ThumbnailDAO thumbnailDAO;

  protected ThumbnailServiceImpl() {
    // This constructor declaration avoid the direct use of this implementation ...
    // Callers have to use the ThumbnailServiceFactory or the @inject annotation to perform
    // Thumbnail services.
  }

  @Override
  public ThumbnailDetail createThumbnail(ThumbnailDetail thumbDetail) throws ThumbnailException {
    try (final Connection con = DBUtil.openConnection()) {
      return thumbnailDAO.insertThumbnail(con, thumbDetail);
    } catch (SQLException se) {
      throw new ThumbnailException("Thumbnail creation failure", se);
    }
  }

  @Override
  public void updateThumbnail(ThumbnailDetail thumbDetail) throws ThumbnailException {
    try (final Connection con = DBUtil.openConnection()) {
      thumbnailDAO.updateThumbnail(con, thumbDetail);
    } catch (SQLException se) {
      throw new ThumbnailException("Thumbnail update failure", se);
    }
  }

  @Override
  public void deleteThumbnail(ThumbnailDetail thumbDetail) throws ThumbnailException {
    try (final Connection con = DBUtil.openConnection()) {
      thumbnailDAO.deleteThumbnail(con, thumbDetail.getObjectId(), thumbDetail.getObjectType(),
          thumbDetail.getInstanceId());
    } catch (SQLException se) {
      throw new ThumbnailException("Thumbnail deletion failure", se);
    }
  }

  @Override
  public ThumbnailDetail getCompleteThumbnail(ThumbnailDetail thumbDetail)
      throws ThumbnailException {
    try (final Connection con = DBUtil.openConnection()) {
      return thumbnailDAO.selectByKey(con, thumbDetail.getInstanceId(), thumbDetail.getObjectId(),
          thumbDetail.getObjectType());
    } catch (SQLException se) {
      throw new ThumbnailException("Thumbnail not found", se);
    }
  }

  @Override
  public List<ThumbnailDetail> getByReference(final Set<ThumbnailReference> references) {
    try (final Connection con = DBUtil.openConnection()) {
      return thumbnailDAO.selectByReference(con, references);
    } catch (SQLException se) {
      throw new ThumbnailRuntimeException("Thumbnail not found", se);
    }
  }

  @Override
  public void deleteAllThumbnail(String componentId) throws ThumbnailException {
    try (final Connection con = DBUtil.openConnection()) {
      thumbnailDAO.deleteAllThumbnails(con, componentId);
    } catch (SQLException se) {
      throw new ThumbnailException("Thumbnail deletion failure", se);
    }
  }

  @Override
  public void moveThumbnail(ThumbnailDetail thumbDetail, String toInstanceId) throws ThumbnailException {
    try (final Connection con = DBUtil.openConnection()) {
      thumbnailDAO.moveThumbnail(con, thumbDetail, toInstanceId);
    } catch (SQLException se) {
      throw new ThumbnailException("Thumbnail move failure", se);
    }
  }
}