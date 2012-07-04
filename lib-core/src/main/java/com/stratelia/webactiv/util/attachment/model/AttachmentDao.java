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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util.attachment.model;

import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.exception.UtilException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Vector;

/**
 * @author ehugonnet
 */
public interface AttachmentDao {

  void deleteAttachment(Connection con, AttachmentPK pk) throws SQLException;

  /**
   * Method declaration
   * @param con
   * @param foreignKey
   * @return
   * @throws SQLException
   * @see
   */
  Vector<AttachmentDetail> findByForeignKey(Connection con, WAPrimaryKey foreignKey)
      throws SQLException;

  /**
   * Method declaration
   * @param con
   * @param foreignKey
   * @param context
   * @return
   * @throws SQLException
   * @see
   */
  Vector<AttachmentDetail> findByPKAndContext(Connection con, WAPrimaryKey foreignKey,
      String context) throws SQLException;

  /**
   * Method declaration
   * @param con
   * @param foreignKey
   * @param nameAttribut
   * @param valueAttribut
   * @return
   * @throws SQLException
   * @see
   */
  Vector<AttachmentDetail> findByPKAndParam(Connection con, WAPrimaryKey foreignKey,
      String nameAttribut, String valueAttribut) throws SQLException;

  AttachmentDetail findByPrimaryKey(Connection con, AttachmentPK pk) throws SQLException;

  Vector<AttachmentDetail> findByWorkerId(Connection con, String workerId) throws SQLException;

  AttachmentDetail findLast(Connection con, AttachmentDetail ad) throws SQLException;

  AttachmentDetail findNext(Connection con, AttachmentDetail ad) throws SQLException;

  AttachmentDetail findPrevious(Connection con, AttachmentDetail ad) throws SQLException;

  Collection<AttachmentDetail> getAllAttachmentByDate(Connection con, Date date, boolean alert)
      throws SQLException;

  Collection<AttachmentDetail> getAllAttachmentToLib(Connection con, Date date) throws SQLException;

  AttachmentDetail insertRow(Connection con, AttachmentDetail attach) throws SQLException,
      UtilException;

  void sortAttachments(Connection con,
      List<AttachmentPK> attachmentPKs) throws SQLException;

  void updateForeignKey(Connection con, AttachmentPK pk, String foreignKey) throws SQLException;

  void updateRow(Connection con, AttachmentDetail attach) throws SQLException;

  void updateXmlForm(Connection con, AttachmentPK pk, String xmlFormName) throws SQLException;

}