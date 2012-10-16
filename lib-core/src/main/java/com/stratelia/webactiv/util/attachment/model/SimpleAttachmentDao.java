/**
* Copyright (C) 2000 - 2012 Silverpeas
*
* This program is free software: you can redistribute it and/or modify it under the terms of the
* GNU Affero General Public License as published by the Free Software Foundation, either version 3
* of the License, or (at your option) any later version.
*
* As a special exception to the terms and conditions of version 3.0 of the GPL, you may
* redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
* applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
* text describing the FLOSS exception, and it is also available here:
* "http://repository.silverpeas.com/legal/licensing"
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
* even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License along with this program.
* If not, see <http://www.gnu.org/licenses/>.
*/
package com.stratelia.webactiv.util.attachment.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.exception.UtilException;

public class SimpleAttachmentDao implements AttachmentDao {

  private static String attachmentTableColumns =
    " attachmentId, attachmentPhysicalName, attachmentLogicalName, attachmentDescription, attachmentType, "
    + "attachmentSize, attachmentContext, attachmentForeignkey, instanceId, attachmentCreationDate, attachmentAuthor, "
    + "attachmentTitle, attachmentInfo, attachmentOrderNum, workerId, cloneId, lang, reservationDate, alertDate, expiryDate, xmlForm ";
  private final static int nameMaxLength = 100;
  private static final String SELECT_BY_PRIMARY_KEY = "SELECT attachmentId, attachmentPhysicalName,"
    + " attachmentLogicalName, attachmentDescription, attachmentType, attachmentSize, "
    + "attachmentContext, attachmentForeignkey, instanceId, attachmentCreationDate, "
    + "attachmentAuthor, attachmentTitle, attachmentInfo, attachmentOrderNum, workerId, cloneId, "
    + "lang, reservationDate, alertDate, expiryDate, xmlForm FROM sb_attachment_attachment WHERE "
    + "attachmentId = ? ";

  public SimpleAttachmentDao() {
  }

  private AttachmentDetail result2AttachmentDetail(ResultSet rs) throws SQLException {
    String instanceId = rs.getString("instanceId");
    String id = Integer.toString(rs.getInt("attachmentId"));
    AttachmentPK pk = new AttachmentPK(id, instanceId);

    AttachmentDetail attachDetail = new AttachmentDetail(pk);

    attachDetail.setPhysicalName(rs.getString("attachmentPhysicalName"));
    attachDetail.setLogicalName(rs.getString("attachmentLogicalName"));
    attachDetail.setDescription(rs.getString("attachmentDescription"));
    attachDetail.setSize(Long.parseLong(rs.getString("attachmentSize")));
    attachDetail.setType(rs.getString("attachmentType"));
    attachDetail.setContext(rs.getString("attachmentContext"));
    String u = rs.getString("attachmentCreationDate");
    if (u != null) {
      try {
        attachDetail.setCreationDate(DateUtil.parse(rs.getString("attachmentCreationDate")));
      } catch (java.text.ParseException e) {
        throw new SQLException("AttachmentDAO.result2AttachmentDetail() - internal error: "
          + "creationDate format unknown for attachment.pk = " + pk + " : " + e.toString(), e);
      }
    } else {
      attachDetail.setCreationDate(new Date());
    }
    attachDetail.setAuthor(rs.getString("attachmentAuthor"));
    attachDetail.setTitle(rs.getString("attachmentTitle"));
    attachDetail.setInfo(rs.getString("attachmentInfo"));
    attachDetail.setOrderNum(rs.getInt("attachmentOrderNum"));
    attachDetail.setWorkerId(rs.getString("workerId"));
    attachDetail.setInstanceId(instanceId);

    AttachmentPK fk = new AttachmentPK(rs.getString("attachmentForeignKey"),
      instanceId);
    attachDetail.setForeignKey(fk);

    attachDetail.setCloneId(rs.getString("cloneId"));

    attachDetail.setLanguage(rs.getString("lang"));

    String rd = rs.getString("reservationDate");
    if (rd != null) {
      try {
        attachDetail.setReservationDate(DateUtil.parse(rs.getString("reservationDate")));
      } catch (java.text.ParseException e) {
        throw new SQLException(
          "AttachmentDAO.result2AttachmentDetail() : internal error : reservationDate format unknown for attachment.pk = "
          + pk + " : " + e.toString());
      }
    } else {
      attachDetail.setReservationDate(null);
    }

    // recuperation de la date d'alerte
    String ad = rs.getString("alertDate");
    if (ad != null) {
      try {
        attachDetail.setAlertDate(DateUtil.parse(rs.getString("alertDate")));
      } catch (java.text.ParseException e) {
        throw new SQLException("AttachmentDAO.result2AttachmentDetail() - internal error: "
          + "alertDate format unknown for attachment.pk = " + pk + " : " + e.toString(), e);
      }
    } else {
      attachDetail.setAlertDate(null);
    }
    // recuperation de la date d'expiration
    String ed = rs.getString("expiryDate");
    if (ed != null) {
      try {
        attachDetail.setExpiryDate(DateUtil.parse(rs.getString("expiryDate")));
      } catch (java.text.ParseException e) {
        throw new SQLException("AttachmentDAO.result2AttachmentDetail() - internal error: "
          + "expiryDate format unknown for attachment.pk = " + pk + " : " + e.toString(), e);
      }
    } else {
      attachDetail.setExpiryDate(null);
    }
    attachDetail.setXmlForm(rs.getString("xmlForm"));
    return attachDetail;
  }

  @Override
  public AttachmentDetail insertRow(Connection con, AttachmentDetail attach) throws SQLException,
    UtilException {
    int id = DBUtil.getNextId("sb_attachment_attachment", "attachmentId");

    attach.getPK().setId(String.valueOf(id));

    // First get the max orderNum
    AttachmentDetail ad = findLast(con, attach);
    if (ad != null) {
      attach.setOrderNum(ad.getOrderNum() + 1);
    }

    String insertQuery = "INSERT INTO sb_attachment_attachment (attachmentId, "
      + "attachmentPhysicalName, attachmentLogicalName, attachmentDescription, attachmentType, "
      + "attachmentSize, attachmentContext, attachmentForeignkey, instanceId, "
      + "attachmentCreationDate, attachmentAuthor, attachmentTitle, attachmentInfo, "
      + "attachmentOrderNum, workerId, cloneId, lang, reservationDate, alertDate, expiryDate, "
      + "xmlForm) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(insertQuery);
      prepStmt.setInt(1, id);
      prepStmt.setString(2, attach.getPhysicalName());
      prepStmt.setString(3, attach.getLogicalName());
      prepStmt.setString(4, attach.getDescription());
      prepStmt.setString(5, attach.getType());
      prepStmt.setString(6, String.valueOf(attach.getSize()));
      prepStmt.setString(7, attach.getContext());
      prepStmt.setString(8, attach.getForeignKey().getId());
      prepStmt.setString(9, attach.getPK().getComponentName());
      prepStmt.setString(10, DateUtil.date2SQLDate(attach.getCreationDate()));

      prepStmt.setString(11, attach.getAuthor());
      prepStmt.setString(12, StringUtil.truncate(attach.getTitle(), nameMaxLength));
      prepStmt.setString(13, attach.getInfo());
      prepStmt.setInt(14, attach.getOrderNum());
      prepStmt.setString(15, attach.getWorkerId());
      prepStmt.setString(16, attach.getCloneId());
      prepStmt.setString(17, attach.getLanguage());
      if (attach.getReservationDate() != null) {
        prepStmt.setString(18, DateUtil.date2SQLDate(attach.getReservationDate()));
      } else {
        prepStmt.setString(18, null);
      }
      if (attach.getAlertDate() != null) {
        prepStmt.setString(19, DateUtil.date2SQLDate(attach.getAlertDate()));
      } else {
        prepStmt.setString(19, null);
      }
      if (attach.getExpiryDate() != null) {
        prepStmt.setString(20, DateUtil.date2SQLDate(attach.getExpiryDate()));
      } else {
        prepStmt.setString(20, null);
      }
      prepStmt.setString(21, attach.getXmlForm());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }

    // set translations
    setTranslations(con, attach);

    return attach;
  }

  @Override
  public void updateRow(Connection con, AttachmentDetail attach)
    throws SQLException {
    StringBuilder updateQuery = new StringBuilder();
    updateQuery.append("UPDATE sb_attachment_attachment SET attachmentTitle = ?, ");
    updateQuery
      .append("attachmentInfo = ?, attachmentPhysicalName = ?, attachmentLogicalName = ?, ");
    updateQuery.append("attachmentDescription = ?, attachmentSize = ?, attachmentType = ?, ");
    updateQuery.append("attachmentContext = ?, attachmentCreationDate = ?, attachmentAuthor = ?, ");
    updateQuery.append("attachmentOrderNum = ?, workerId = ?, instanceId = ?, lang = ?, ");
    updateQuery.append("reservationDate = ? , alertDate = ?, expiryDate = ?, xmlForm = ? ");
    updateQuery.append(" WHERE attachmentId = ? ");
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(updateQuery.toString());
      prepStmt.setString(1, StringUtil.truncate(attach.getTitle(), nameMaxLength));
      prepStmt.setString(2, attach.getInfo());
      prepStmt.setString(3, attach.getPhysicalName());
      prepStmt.setString(4, attach.getLogicalName());
      prepStmt.setString(5, attach.getDescription());
      prepStmt.setString(6, Long.toString(attach.getSize()));
      prepStmt.setString(7, attach.getType());
      prepStmt.setString(8, attach.getContext());
      if (attach.getCreationDate() == null) {
        prepStmt.setString(9, DateUtil.today2SQLDate());
      } else {
        prepStmt.setString(9, DateUtil.date2SQLDate(attach.getCreationDate()));
      }
      prepStmt.setString(10, attach.getAuthor());
      prepStmt.setInt(11, attach.getOrderNum());
      prepStmt.setString(12, attach.getWorkerId());
      prepStmt.setString(13, attach.getInstanceId());

      if (!StringUtil.isDefined(attach.getLanguage())
        || I18NHelper.isDefaultLanguage(attach.getLanguage())) {
        prepStmt.setNull(14, Types.VARCHAR);
      } else {
        prepStmt.setString(14, attach.getLanguage());
      }
      if (attach.getReservationDate() == null) {
        prepStmt.setString(15, null);
      } else {
        prepStmt.setString(15, DateUtil.date2SQLDate(attach.getReservationDate()));
      }
      if (attach.getAlertDate() == null) {
        prepStmt.setString(16, null);
      } else {
        prepStmt.setString(16, DateUtil.date2SQLDate(attach.getAlertDate()));
      }
      if (attach.getExpiryDate() == null) {
        prepStmt.setString(17, null);
      } else {
        prepStmt.setString(17, DateUtil.date2SQLDate(attach.getExpiryDate()));
      }
      prepStmt.setString(18, attach.getXmlForm());
      prepStmt.setInt(19, Integer.parseInt(attach.getPK().getId()));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  @Override
  public void updateXmlForm(Connection con, AttachmentPK pk, String xmlFormName) throws SQLException {
    String updateQuery = "UPDATE sb_attachment_attachment SET xmlForm = ? WHERE attachmentId = ? ";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(updateQuery);
      prepStmt.setString(1, xmlFormName);
      prepStmt.setInt(2, Integer.parseInt(pk.getId()));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  @Override
  public void updateForeignKey(Connection con, AttachmentPK pk, String foreignKey) throws
    SQLException {
    String updateQuery = "UPDATE sb_attachment_attachment SET attachmentForeignkey = ? "
      + "WHERE attachmentId = ? ";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(updateQuery);
      prepStmt.setString(1, foreignKey);
      prepStmt.setInt(2, Integer.parseInt(pk.getId()));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  @Override
  public AttachmentDetail findByPrimaryKey(Connection con, AttachmentPK pk) throws SQLException {
    ResultSet rs = null;
    PreparedStatement prepStmt = null;
    AttachmentDetail attachDetail = null;
    try {
      SilverTrace.info("attachment", "AttachmentDAO.loadRow()","root.MSG_GEN_PARAM_VALUE",
        "selectQuery = " + SELECT_BY_PRIMARY_KEY + " with attachmentId = " + pk.getId());
      prepStmt = con.prepareStatement(SELECT_BY_PRIMARY_KEY);
      prepStmt.setInt(1, Integer.parseInt(pk.getId()));
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        attachDetail = result2AttachmentDetail(rs);
        setTranslations(con, attachDetail);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return attachDetail;
  }

  /**
* Method declaration
*
* @param con
* @param foreignKey
* @return
* @throws SQLException
* @see
*/
  @Override
  public Vector<AttachmentDetail> findByForeignKey(Connection con, WAPrimaryKey foreignKey)
    throws SQLException {
    StringBuilder selectStatement = new StringBuilder();
    selectStatement.append("SELECT ").append(attachmentTableColumns);
    selectStatement.append(" FROM sb_attachment_attachment WHERE attachmentForeignKey= ? AND instanceId= ? ");
    selectStatement.append(" ORDER BY attachmentOrderNum, attachmentId ");

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    Vector<AttachmentDetail> attachments;
    try {
      attachments = new Vector<AttachmentDetail>();
      prepStmt = con.prepareStatement(selectStatement.toString());

      // this "id" is the "id" of customer object.
      // The row id in the table defined by getTableName()
      prepStmt.setString(1, foreignKey.getId());
      prepStmt.setString(2, foreignKey.getComponentName());

      rs = prepStmt.executeQuery();

      while (rs.next()) {
        AttachmentDetail attachmentDetail = result2AttachmentDetail(rs);
        setTranslations(con, attachmentDetail);
        attachments.addElement(attachmentDetail);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return attachments;
  }

  private void setTranslations(Connection con,
    AttachmentDetail attachmentDetail) throws SQLException {
    AttachmentDetailI18N translation = new AttachmentDetailI18N(
      attachmentDetail);
    attachmentDetail.addTranslation(translation);

    if (I18NHelper.isI18N) {
      List translations = AttachmentI18NDAO.getTranslations(con, attachmentDetail.getPK());
      attachmentDetail.setTranslations(translations);
    }
  }

  @Override
  public Vector<AttachmentDetail> findByWorkerId(Connection con, String workerId)
    throws SQLException {
    StringBuilder selectStatement = new StringBuilder();
    selectStatement.append("SELECT ").append(attachmentTableColumns);
    selectStatement.append(" FROM sb_attachment_attachment WHERE workerId = ? ");

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    Vector<AttachmentDetail> attachments;
    try {
      attachments = new Vector<AttachmentDetail>();
      prepStmt = con.prepareStatement(selectStatement.toString());

      prepStmt.setString(1, workerId);

      rs = prepStmt.executeQuery();

      while (rs.next()) {
        AttachmentDetail attachmentDetail = result2AttachmentDetail(rs);

        // set translations
        setTranslations(con, attachmentDetail);

        attachments.addElement(attachmentDetail);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return attachments;
  }

  /**
* Method declaration
*
* @param con
* @param foreignKey
* @param nameAttribut
* @param valueAttribut
* @return
* @throws SQLException
* @see
*/
  @Override
  public Vector<AttachmentDetail> findByPKAndParam(Connection con,
    WAPrimaryKey foreignKey, String nameAttribut, String valueAttribut)
    throws SQLException {
    StringBuilder selectQuery = new StringBuilder();
    selectQuery.append("SELECT ").append(attachmentTableColumns);
    selectQuery.append(" FROM sb_attachment_attachment WHERE attachmentForeignKey = ? ");
    selectQuery.append(" AND attachment").append(nameAttribut).append(" = ? ");
    selectQuery.append(" AND instanceId = ? ");
    selectQuery.append(" ORDER BY attachmentOrderNum, attachmentId ");
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    Vector<AttachmentDetail> attachments = new Vector<AttachmentDetail>();
    try {
      prepStmt = con.prepareStatement(selectQuery.toString());
      prepStmt.setString(1, foreignKey.getId());
      prepStmt.setString(2, valueAttribut);
      prepStmt.setString(3, foreignKey.getComponentName());
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        AttachmentDetail attachmentDetail = result2AttachmentDetail(rs);
        setTranslations(con, attachmentDetail);
        attachments.addElement(attachmentDetail);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return attachments;
  }

  /**
* Method declaration
*
* @param con
* @param foreignKey
* @param context
* @return
* @throws SQLException
* @see
*/
  @Override
  public Vector<AttachmentDetail> findByPKAndContext(Connection con,
    WAPrimaryKey foreignKey, String context) throws SQLException {
    StringBuilder selectQuery = new StringBuilder();
    selectQuery.append("SELECT ").append(attachmentTableColumns);
    selectQuery.append(" FROM sb_attachment_attachment WHERE attachmentForeignKey = ? ");
    if (context != null) {
      selectQuery.append(" AND attachmentContext LIKE '").append(context).append("%'");
    }
    selectQuery.append(" AND instanceId = ? ");
    selectQuery.append(" ORDER BY attachmentOrderNum, attachmentId ");
    SilverTrace.info("attachment", "AttachmentDAO.findByPKAndContext()",
      "root.MSG_GEN_PARAM_VALUE", "selectQuery = " + selectQuery.toString());
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    int i = 0;
    Vector<AttachmentDetail> attachments;
    try {
      attachments = new Vector<AttachmentDetail>();
      prepStmt = con.prepareStatement(selectQuery.toString());
      prepStmt.setString(1, foreignKey.getId());
      prepStmt.setString(2, foreignKey.getComponentName());
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        AttachmentDetail attachmentDetail = result2AttachmentDetail(rs); setTranslations(con, attachmentDetail);

        if (attachmentDetail.getOrderNum() != i) {
          attachmentDetail.setOrderNum(i);
          updateRow(con, attachmentDetail);
        }
        i++;
        attachments.addElement(attachmentDetail);
      }

      SilverTrace.info("attachment", "AttachmentDAO.findByPKAndContext()",
        "root.MSG_GEN_PARAM_VALUE", "attachments.size() = " + attachments.size());
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return attachments;
  }

  @Override
  public AttachmentDetail findPrevious(Connection con, AttachmentDetail ad) throws SQLException {
    WAPrimaryKey foreignKey = ad.getForeignKey();
    StringBuilder selectQuery = new StringBuilder();
    selectQuery.append("SELECT ").append(attachmentTableColumns);
    selectQuery.append(" FROM sb_attachment_attachment WHERE attachmentForeignKey = ? ");
    if (ad.getContext() != null) {
      selectQuery.append(" AND attachmentContext LIKE '").append(ad.getContext()).append("%'");
    }
    selectQuery.append(" AND instanceId = ? ");
    selectQuery.append(" AND attachmentOrderNum < ?");
    selectQuery.append(" ORDER BY attachmentOrderNum DESC, attachmentId DESC");
    SilverTrace.info("attachment", "AttachmentDAO.findPrevious()",
      "root.MSG_GEN_PARAM_VALUE", "selectQuery = " + selectQuery.toString());
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    AttachmentDetail attachmentDetail = null;
    try {
      prepStmt = con.prepareStatement(selectQuery.toString());
      prepStmt.setString(1, foreignKey.getId());
      prepStmt.setString(2, foreignKey.getComponentName());
      prepStmt.setInt(3, ad.getOrderNum());
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        attachmentDetail = result2AttachmentDetail(rs);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return attachmentDetail;
  }

  @Override
  public AttachmentDetail findNext(Connection con, AttachmentDetail ad) throws SQLException {
    WAPrimaryKey foreignKey = ad.getForeignKey();
    StringBuilder selectQuery = new StringBuilder();
    selectQuery.append("SELECT ").append(attachmentTableColumns);
    selectQuery.append(" FROM sb_attachment_attachment WHERE attachmentForeignKey = ? ");
    if (ad.getContext() != null) {
      selectQuery.append(" AND attachmentContext like '").append(
        ad.getContext()).append("%'");
    }
    selectQuery.append(" AND instanceId = ? ");
    selectQuery.append(" AND attachmentOrderNum > ?");
    selectQuery.append(" ORDER BY attachmentOrderNum, attachmentId");

    SilverTrace.info("attachment", "AttachmentDAO.findNext()",
      "root.MSG_GEN_PARAM_VALUE", "selectQuery = " + selectQuery.toString());

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    AttachmentDetail attachmentDetail = null;

    try {
      prepStmt = con.prepareStatement(selectQuery.toString());
      prepStmt.setString(1, foreignKey.getId());
      prepStmt.setString(2, foreignKey.getComponentName());
      prepStmt.setInt(3, ad.getOrderNum());
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        attachmentDetail = result2AttachmentDetail(rs);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return attachmentDetail;
  }

  @Override
  public AttachmentDetail findLast(Connection con, AttachmentDetail ad) throws SQLException {
    WAPrimaryKey foreignKey = ad.getForeignKey();
    StringBuilder selectQuery = new StringBuilder();
    selectQuery.append("SELECT ").append(attachmentTableColumns);
    selectQuery.append(" FROM sb_attachment_attachment WHERE attachmentForeignKey = ? ");
    if (ad.getContext() != null) {
      selectQuery.append(" AND attachmentContext LIKE '").append(
        ad.getContext()).append("%'");
    }
    selectQuery.append(" AND instanceId = ? ORDER BY attachmentOrderNum DESC, attachmentId DESC");

    SilverTrace.info("attachment", "AttachmentDAO.findNext()",
      "root.MSG_GEN_PARAM_VALUE", "selectQuery = " + selectQuery.toString());

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    AttachmentDetail attachmentDetail = null;

    try {
      prepStmt = con.prepareStatement(selectQuery.toString());
      prepStmt.setString(1, foreignKey.getId());
      prepStmt.setString(2, foreignKey.getComponentName());
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        attachmentDetail = result2AttachmentDetail(rs);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return attachmentDetail;
  }

  @Override
  public void deleteAttachment(Connection con, AttachmentPK pk)
    throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement("DELETE FROM sb_attachment_attachment WHERE attachmentId = ? ");
      prepStmt.setInt(1, Integer.parseInt(pk.getId()));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  @Override
  public Collection<AttachmentDetail> getAllAttachmentByDate(Connection con, Date date,
    boolean alert) throws SQLException {
    StringBuilder selectQuery = new StringBuilder();
    selectQuery.append("SELECT ").append(attachmentTableColumns);
    selectQuery.append(" FROM sb_attachment_attachment");
    if (alert) {
      selectQuery.append(" WHERE alertDate = ? ");
    } else {
      selectQuery.append(" WHERE expiryDate = ? ");
    }

    SilverTrace.info("attachment", "AttachmentDAO.findByPKAndContext()",
      "root.MSG_GEN_PARAM_VALUE", "selectQuery = " + selectQuery.toString());

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    Collection<AttachmentDetail> attachments;
    try {
      attachments = new ArrayList<AttachmentDetail>();
      prepStmt = con.prepareStatement(selectQuery.toString());
      prepStmt.setString(1, DateUtil.date2SQLDate(date));
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        AttachmentDetail attachmentDetail = result2AttachmentDetail(rs);
        setTranslations(con, attachmentDetail);
        attachments.add(attachmentDetail);
      }
      SilverTrace.info("attachment", "AttachmentDAO.findByPKAndContext()",
        "root.MSG_GEN_PARAM_VALUE", "attachments.size() = " + attachments.size());
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return attachments;
  }

  @Override
  public Collection<AttachmentDetail> getAllAttachmentToLib(Connection con, Date date)
    throws SQLException {
    StringBuilder selectQuery = new StringBuilder();
    selectQuery.append("SELECT ").append(attachmentTableColumns);
    selectQuery.append(" FROM sb_attachment_attachment WHERE expiryDate < ? ");
    SilverTrace.info("attachment", "AttachmentDAO.findByPKAndContext()",
      "root.MSG_GEN_PARAM_VALUE", "selectQuery = " + selectQuery.toString());
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    Collection<AttachmentDetail> attachments;
    try {
      attachments = new ArrayList<AttachmentDetail>();
      prepStmt = con.prepareStatement(selectQuery.toString());
      prepStmt.setString(1, DateUtil.date2SQLDate(date));
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        AttachmentDetail attachmentDetail = result2AttachmentDetail(rs);
        setTranslations(con, attachmentDetail);

        attachments.add(attachmentDetail);
      }
      SilverTrace.info("attachment", "AttachmentDAO.findByPKAndContext()",
        "root.MSG_GEN_PARAM_VALUE", "attachments.size() = " + attachments.size());
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return attachments;
  }

  @Override
  public void sortAttachments(Connection con, List<AttachmentPK> attachmentPKs) throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(
        "UPDATE sb_attachment_attachment SET attachmentOrderNum = ? WHERE attachmentId = ? ");
      for (int i = 0; i < attachmentPKs.size(); i++) {
       AttachmentPK attachmentPK = attachmentPKs.get(i);
        prepStmt.setInt(1, i);
        prepStmt.setInt(2, Integer.parseInt(attachmentPK.getId()));
        prepStmt.executeUpdate();
      }
    } finally {
      DBUtil.close(prepStmt);
    }
  }
}