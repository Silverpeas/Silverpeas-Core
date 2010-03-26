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
package com.stratelia.webactiv.util.attachment.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.exception.UtilException;

public class AttachmentI18NDAO {
  private static String attachmentTableName = "SB_Attachment_AttachmentI18N";
  private static String attachmentTableColumns =
      " id, attachmentId, lang, attachmentPhysicalName, attachmentLogicalName, attachmentType, attachmentSize, instanceId, attachmentCreationDate, attachmentAuthor, attachmentTitle, attachmentInfo, xmlForm ";

  public AttachmentI18NDAO() {
  }

  private static AttachmentDetailI18N result2AttachmentDetailI18N(ResultSet rs) throws SQLException {
    AttachmentDetailI18N attachDetail = new AttachmentDetailI18N();

    attachDetail.setId(rs.getInt("id"));
    attachDetail.setObjectId(Integer.toString(rs.getInt("attachmentId")));
    attachDetail.setLanguage(rs.getString("lang"));
    attachDetail.setPhysicalName(rs.getString("attachmentPhysicalName"));
    attachDetail.setLogicalName(rs.getString("attachmentLogicalName"));
    attachDetail.setSize(Long.parseLong(rs.getString("attachmentSize")));
    attachDetail.setType(rs.getString("attachmentType"));
    String u = rs.getString("attachmentCreationDate");
    if (u != null) {
      try {
        attachDetail.setCreationDate(DateUtil.parse(u));
      } catch (ParseException e) {
        throw new SQLException(
            "AttachmentDAO.result2AttachmentDetail() : internal error : creationDate format unknown for attachment translation = " +
                attachDetail.getId() + " : " + e.toString());
      }
    } else
      attachDetail.setCreationDate(new Date());
    attachDetail.setAuthor(rs.getString("attachmentAuthor"));
    attachDetail.setTitle(rs.getString("attachmentTitle"));
    attachDetail.setInfo(rs.getString("attachmentInfo"));
    attachDetail.setInstanceId(rs.getString("instanceId"));
    attachDetail.setXmlForm(rs.getString("xmlForm"));

    return attachDetail;
  }

  public static List<AttachmentDetailI18N> getTranslations(Connection con, WAPrimaryKey foreignKey)
      throws SQLException {
    StringBuffer selectStatement = new StringBuffer();
    selectStatement.append("select ").append(attachmentTableColumns);
    selectStatement.append(" from ").append(attachmentTableName);
    selectStatement.append(" where attachmentId= ? and instanceId= ? ");

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    List<AttachmentDetailI18N> attachments = new ArrayList<AttachmentDetailI18N>();
    try {
      prepStmt = con.prepareStatement(selectStatement.toString());

      prepStmt.setInt(1, Integer.parseInt(foreignKey.getId()));
      prepStmt.setString(2, foreignKey.getComponentName());

      rs = prepStmt.executeQuery();

      while (rs.next()) {
        AttachmentDetailI18N attachmentDetail = result2AttachmentDetailI18N(rs);
        attachments.add(attachmentDetail);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return attachments;
  }

  public static void addTranslation(Connection con, AttachmentDetailI18N attach)
      throws SQLException {
    int id = -1;
    try {
      id = DBUtil.getNextId(attachmentTableName, "id");
    } catch (UtilException e) {
      throw new SQLException(e.toString());
    }

    String insertQuery =
        "insert into " + attachmentTableName +
        " values ( ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? )";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(insertQuery);
      prepStmt.setInt(1, id);
      prepStmt.setInt(2, Integer.parseInt(attach.getObjectId()));
      prepStmt.setString(3, attach.getLanguage());
      prepStmt.setString(4, attach.getPhysicalName());
      prepStmt.setString(5, attach.getLogicalName());
      prepStmt.setString(6, attach.getType());
      prepStmt.setString(7, String.valueOf(attach.getSize()));
      prepStmt.setString(8, attach.getInstanceId());
      prepStmt.setString(9, DateUtil.date2SQLDate(new Date()));
      prepStmt.setString(10, attach.getAuthor());
      prepStmt.setString(11, attach.getTitle());
      prepStmt.setString(12, attach.getInfo());
      prepStmt.setString(13, attach.getXmlForm());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void updateTranslation(Connection con, AttachmentDetailI18N attach)
      throws SQLException {
    String updateQuery =
        "update " +
            attachmentTableName +
            " set attachmentTitle = ?, attachmentInfo = ?, attachmentPhysicalName = ?, attachmentLogicalName = ?, attachmentSize = ?, attachmentType = ?, attachmentCreationDate = ?, attachmentAuthor = ? where id = ? ";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(updateQuery);
      prepStmt.setString(1, attach.getTitle());
      prepStmt.setString(2, attach.getInfo());
      prepStmt.setString(3, attach.getPhysicalName());
      prepStmt.setString(4, attach.getLogicalName());
      prepStmt.setString(5, Long.toString(attach.getSize()));
      prepStmt.setString(6, attach.getType());
      if (attach.getCreationDate() == null) {
        prepStmt.setString(7, DateUtil.today2SQLDate());
      } else {
        prepStmt.setString(7, DateUtil.date2SQLDate(attach.getCreationDate()));
      }
      prepStmt.setString(8, attach.getAuthor());
      prepStmt.setInt(9, attach.getId());

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void removeTranslation(Connection con, String translationId) throws SQLException {
    removeTranslation(con, Integer.parseInt(translationId));
  }

  public static void removeTranslation(Connection con, int translationId) throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      String deleteQuery = "delete from " + attachmentTableName + " where id = ? ";

      prepStmt = con.prepareStatement(deleteQuery);
      prepStmt.setInt(1, translationId);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void removeTranslations(Connection con, WAPrimaryKey primaryKey)
      throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      String deleteQuery =
          "delete from " + attachmentTableName + " where attachmentId = ? and instanceId = ? ";

      prepStmt = con.prepareStatement(deleteQuery);
      prepStmt.setInt(1, Integer.parseInt(primaryKey.getId()));
      prepStmt.setString(2, primaryKey.getInstanceId());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void updateXmlForm(Connection con, AttachmentPK pk, String language,
      String xmlFormName) throws SQLException {
    String updateQuery =
        "update " + attachmentTableName + " set xmlForm = ? where attachmentid = ? and lang = ? ";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(updateQuery);
      prepStmt.setString(1, xmlFormName);
      prepStmt.setInt(2, Integer.parseInt(pk.getId()));
      prepStmt.setString(3, language);

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

}