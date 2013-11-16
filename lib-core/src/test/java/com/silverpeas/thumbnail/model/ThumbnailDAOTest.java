/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.silverpeas.thumbnail.model;

import java.sql.Connection;

import com.silverpeas.components.model.AbstractTestDao;

/**
 *
 * @author srochet
 */
public class ThumbnailDAOTest extends AbstractTestDao {

  private static ThumbnailDAO dao = new ThumbnailDAO();
  public ThumbnailDAOTest() {
  }

  /**
   * Test of insertRow method, of class ThumbnailDAO.
   * insert, test by select, delete row
   */
  @org.junit.Test
  public void testInsertThumbnail() throws Exception {
    Connection con = getConnection().getConnection();
    String instanceId = "kmelia57";
    int objectId = 999999;
    int objectType = ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE;
    String mimeType = "image/jpeg";
    String originalFileName = "55555555.jpg";
    String cropFileName = "7777777.jpg";
    int x_start = 25;
    int y_start = 27;
    int x_length = 99;
    int y_length = 111;

    ThumbnailDetail detail = new ThumbnailDetail(instanceId, objectId, objectType);
    detail.setOriginalFileName(originalFileName);
    detail.setMimeType(mimeType);
    detail.setCropFileName(cropFileName);
    detail.setXStart(x_start);
    detail.setYStart(y_start);
    detail.setXLength(x_length);
    detail.setYLength(y_length);

    dao.insertThumbnail(con, detail);
    ThumbnailDetail result = dao.selectByKey(con, instanceId, objectId, objectType);
    assertNotNull(result);
    assertEquals(detail.getInstanceId(), result.getInstanceId());
    assertEquals(detail.getObjectId(), result.getObjectId());
    assertEquals(detail.getObjectType(), result.getObjectType());
    assertEquals(detail.getOriginalFileName(), result.getOriginalFileName());
    assertEquals(detail.getMimeType(), result.getMimeType());
    assertEquals(detail.getCropFileName(), result.getCropFileName());
    assertEquals(detail.getXStart(), result.getXStart());
    assertEquals(detail.getXLength(), result.getXLength());
    assertEquals(detail.getYStart(), result.getYStart());
    assertEquals(detail.getYLength(), result.getYLength());

    dao.deleteThumbnail(con, objectId, objectType, instanceId);
  }

  /**
   * Test of selectByKey method, of class ThumbnailDAO.
   */
  @org.junit.Test
  public void testSelectByKey() throws Exception {
    Connection con = getConnection().getConnection();
    ThumbnailDetail result = dao.selectByKey(con, "kmelia57", 1, 0);
    assertNotNull(result);
    assertEquals("kmelia57", result.getInstanceId());
    assertEquals(1, result.getObjectId());
    assertEquals(0, result.getObjectType());
    assertEquals("123456789.jpg", result.getOriginalFileName());
    assertEquals("image/jpeg", result.getMimeType());
    assertEquals("987654321.jpg", result.getCropFileName());
    assertEquals(10, result.getXStart());
    assertEquals(123, result.getXLength());
    assertEquals(11, result.getYStart());
    assertEquals(321, result.getYLength());
  }

  /**
   * Test of delete method, of class ThumbnailDAO.
   * insert row, delete row, test by select
   */
  @org.junit.Test
  public void testDeleteThumbnail() throws Exception {
    Connection con = getConnection().getConnection();

    String instanceId = "kmelia57";
    int objectId = 999999;
    int objectType = ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE;
    String mimeType = "image/jpeg";
    String originalFileName = "55555555.jpg";
    String cropFileName = "7777777.jpg";
    int x_start = 25;
    int y_start = 27;
    int x_length = 99;
    int y_length = 111;

    ThumbnailDetail detail = new ThumbnailDetail(instanceId, objectId, objectType);
    detail.setOriginalFileName(originalFileName);
    detail.setMimeType(mimeType);
    detail.setCropFileName(cropFileName);
    detail.setXStart(x_start);
    detail.setYStart(y_start);
    detail.setXLength(x_length);
    detail.setYLength(y_length);

    dao.insertThumbnail(con, detail);

    dao.deleteThumbnail(con, objectId, objectType, instanceId);

    ThumbnailDetail result = dao.selectByKey(con, instanceId, objectId, objectType);
    assertNull(result);

  }

  /**
   * Test of delete method, of class ThumbnailDAO.
   * insert row, delete row, test by select
   */
  @org.junit.Test
  public void testDeleteAllThumbnails() throws Exception {
    Connection con = getConnection().getConnection();

    String instanceId = "kmelia58";
    int objectId = 999999;
    int objectType = ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE;
    String mimeType = "image/jpeg";
    String originalFileName = "55555555.jpg";
    String cropFileName = "7777777.jpg";
    int x_start = 25;
    int y_start = 27;
    int x_length = 99;
    int y_length = 111;

    ThumbnailDetail detail = new ThumbnailDetail(instanceId, objectId, objectType);
    detail.setOriginalFileName(originalFileName);
    detail.setMimeType(mimeType);
    detail.setCropFileName(cropFileName);
    detail.setXStart(x_start);
    detail.setYStart(y_start);
    detail.setXLength(x_length);
    detail.setYLength(y_length);

    dao.insertThumbnail(con, detail);

    int objectId2 = 777777;
    detail.setObjectId(objectId2);

    dao.insertThumbnail(con, detail);

    dao.deleteAllThumbnails(con, instanceId);

    ThumbnailDetail result = dao.selectByKey(con, instanceId, objectId, objectType);
    assertNull(result);
    ThumbnailDetail result2 = dao.selectByKey(con, instanceId, objectId2, objectType);
    assertNull(result2);

  }

  /**
   * Test of update method, of class ThumbnailDAO.
   * insert row, test by select, update row, test by select, delete row
   */
  @org.junit.Test
  public void testUpdateRow() throws Exception {
    Connection con = getConnection().getConnection();

    String instanceId = "kmelia57";
    int objectId = 999999;
    int objectType = ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE;
    String mimeType = "image/jpeg";
    String originalFileName = "55555555.jpg";
    String cropFileName = "7777777.jpg";
    int x_start = 25;
    int y_start = 27;
    int x_length = 99;
    int y_length = 111;

    ThumbnailDetail detail = new ThumbnailDetail(instanceId, objectId, objectType);
    detail.setOriginalFileName(originalFileName);
    detail.setMimeType(mimeType);
    detail.setCropFileName("");
    detail.setXStart(0);
    detail.setYStart(0);
    detail.setXLength(0);
    detail.setYLength(0);

    dao.insertThumbnail(con, detail);

    ThumbnailDetail result = dao.selectByKey(con, instanceId, objectId, objectType);
    assertNotNull(result);
    assertEquals(detail.getCropFileName(), "");
    assertEquals(detail.getXStart(), 0);
    assertEquals(detail.getXLength(), 0);
    assertEquals(detail.getYStart(), 0);
    assertEquals(detail.getYLength(), 0);

    detail.setCropFileName(cropFileName);
    detail.setXStart(x_start);
    detail.setYStart(y_start);
    detail.setXLength(x_length);
    detail.setYLength(y_length);

    dao.updateThumbnail(con, detail);

    result = dao.selectByKey(con, instanceId, objectId, objectType);
    assertNotNull(result);
    assertEquals(detail.getInstanceId(), result.getInstanceId());
    assertEquals(detail.getObjectId(), result.getObjectId());
    assertEquals(detail.getObjectType(), result.getObjectType());
    assertEquals(detail.getOriginalFileName(), result.getOriginalFileName());
    assertEquals(detail.getMimeType(), result.getMimeType());
    assertEquals(detail.getCropFileName(), result.getCropFileName());
    assertEquals(detail.getXStart(), result.getXStart());
    assertEquals(detail.getXLength(), result.getXLength());
    assertEquals(detail.getYStart(), result.getYStart());
    assertEquals(detail.getYLength(), result.getYLength());
    dao.deleteThumbnail(con, objectId, objectType, instanceId);
  }

  @Override
  protected String getDatasetFileName() {
    return "test-thumbnail-dao-dataset.xml";
  }
  
  
  @Override
  protected String getTableCreationFileName() {
    return "create-database.sql";
  }
}
