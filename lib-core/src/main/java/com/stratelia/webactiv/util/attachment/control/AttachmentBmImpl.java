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

package com.stratelia.webactiv.util.attachment.control;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentException;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDao;
import com.stratelia.webactiv.util.attachment.model.SimpleAttachmentDao;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetailI18N;
import com.stratelia.webactiv.util.attachment.model.AttachmentI18NDAO;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;

public class AttachmentBmImpl implements AttachmentBm {

  private AttachmentDao dao;

  public AttachmentBmImpl() {
    dao = new SimpleAttachmentDao();
  }

  public AttachmentBmImpl(AttachmentDao dao) {
    this.dao = dao;
  }

  @Override
  public AttachmentDetail createAttachment(AttachmentDetail attachDetail)
      throws AttachmentException {
    Connection con = getConnection();
    try {
      return dao.insertRow(con, attachDetail);
    } catch (SQLException se) {
      throw new AttachmentException("AttachmentBmImpl.createAttachment()",
          SilverpeasException.ERROR, "attachment.EX_RECORD_NOT_CREATE_ATTACHMENT", se);
    } catch (UtilException ue) {
      throw new AttachmentException("AttachmentBmImpl.createAttachment()",
          SilverpeasException.ERROR, "attachment.EX_RECORD_NOT_CREATE_ATTACHMENT", ue);
    } finally {
      closeConnection(con);
    }
  }

  @Override
  public void updateAttachment(AttachmentDetail attachDetail) throws AttachmentException {
    Connection con = getConnection();
    try {
      boolean updateDefault = false;
      AttachmentDetail oldAttachment = dao.findByPrimaryKey(con, attachDetail.getPK());
      String oldLang = oldAttachment.getLanguage();

      if (attachDetail.isRemoveTranslation()) {
        if ("-1".equals(attachDetail.getTranslationId())) {
          List<AttachmentDetailI18N> translations = AttachmentI18NDAO.getTranslations(con,
              attachDetail.getPK());

          if (translations != null && translations.size() > 0) {
            AttachmentDetailI18N translation = translations.get(0);
            attachDetail.setPhysicalName(translation.getPhysicalName());
            attachDetail.setLogicalName(translation.getLogicalName());
            attachDetail.setType(translation.getType());
            attachDetail.setCreationDate(translation.getCreationDate());
            attachDetail.setSize(translation.getSize());
            attachDetail.setAuthor(translation.getAuthor());
            attachDetail.setTitle(translation.getTitle());
            attachDetail.setInfo(translation.getInfo());
            attachDetail.setLanguage(translation.getLanguage());
            AttachmentI18NDAO.removeTranslation(con, translation.getId());
            updateDefault = true;
          }
        } else {
          AttachmentI18NDAO.removeTranslation(con, attachDetail.getTranslationId());
        }
      } else {
        // Add or update a translation
        if (attachDetail.getLanguage() != null) {
          if (oldLang == null) {
            // translation for the first time
            oldLang = I18NHelper.defaultLanguage;
          }

          if (!oldLang.equalsIgnoreCase(attachDetail.getLanguage())) {
            AttachmentDetailI18N translation = new AttachmentDetailI18N(attachDetail);
            String translationId = attachDetail.getTranslationId();

            if (translationId != null && !"-1".equals(translationId)) {
              AttachmentI18NDAO.updateTranslation(con, translation);
            } else {
              AttachmentI18NDAO.addTranslation(con, translation);
            }
            attachDetail.setPhysicalName(oldAttachment.getPhysicalName());
            attachDetail.setLogicalName(oldAttachment.getLogicalName());
            attachDetail.setType(oldAttachment.getType());
            attachDetail.setCreationDate(oldAttachment.getCreationDate());
            attachDetail.setSize(oldAttachment.getSize());
            attachDetail.setAuthor(oldAttachment.getAuthor());
            attachDetail.setTitle(oldAttachment.getTitle());
            attachDetail.setInfo(oldAttachment.getInfo());
            attachDetail.setLanguage(oldLang);
          } else {
            updateDefault = true;
          }
        } else {
          updateDefault = true;
        }
      }
      if (updateDefault) {
        dao.updateRow(con, attachDetail);
      }
    } catch (SQLException se) {
      throw new AttachmentException("AttachmentBmImpl.updateAttachment()",
          SilverpeasException.ERROR, "attachment.EX_RECORD_NOT_UPDATE_ATTACHMENT", se);
    } finally {
      closeConnection(con);
    }
  }

  @Override
  public Vector<AttachmentDetail> getAttachmentsByForeignKey(AttachmentPK foreignKey)
      throws AttachmentException {
    Connection con = getConnection();
    try {
      return dao.findByForeignKey(con, foreignKey);
    } catch (SQLException se) {
      throw new AttachmentException(
          "AttachmentBmImpl.getAttachmentsByForeignKey()",
          SilverpeasException.ERROR, "attachment.EX_RECORD_NOT_LOAD", se);
    } finally {
      closeConnection(con);
    }
  }

  public Vector<AttachmentDetail> getAttachmentsByWorkerId(String workerId)
      throws AttachmentException {
    Connection con = getConnection();
    try {
      return dao.findByWorkerId(con, workerId);
    } catch (SQLException se) {
      throw new AttachmentException(
          "AttachmentBmImpl.getAttachmentsByWorkerId()",
          SilverpeasException.ERROR, "attachment.EX_RECORD_NOT_LOAD", se);
    } finally {
      closeConnection(con);
    }
  }

  public AttachmentDetail findPrevious(AttachmentDetail ad)
      throws AttachmentException {
    Connection con = getConnection();
    try {
      return dao.findPrevious(con, ad);
    } catch (SQLException se) {
      throw new AttachmentException("AttachmentBmImpl.findPrevious()",
          SilverpeasException.ERROR, "attachment.EX_RECORD_NOT_LOAD", se);
    } finally {
      closeConnection(con);
    }
  }

  public AttachmentDetail findNext(AttachmentDetail ad)
      throws AttachmentException {
    Connection con = getConnection();
    try {
      return dao.findNext(con, ad);
    } catch (SQLException se) {
      throw new AttachmentException("AttachmentBmImpl.findNext()",
          SilverpeasException.ERROR, "attachment.EX_RECORD_NOT_LOAD", se);
    } finally {
      closeConnection(con);
    }
  }

  public AttachmentDetail getAttachmentByPrimaryKey(AttachmentPK primaryKey)
      throws AttachmentException {
    Connection con = getConnection();
    try {
      return dao.findByPrimaryKey(con, primaryKey);
    } catch (SQLException se) {
      throw new AttachmentException("AttachmentBmImpl.createAttachment()",
          SilverpeasException.ERROR, "attachment.EX_RECORD_NOT_LOAD", se);
    } finally {
      closeConnection(con);
    }
  }

  public Vector<AttachmentDetail> getAttachmentsByPKAndParam(AttachmentPK foreignKey,
      String nameAttribut, String valueAttribut) throws AttachmentException {
    Connection con = getConnection();
    try {
      return dao.findByPKAndParam(con, foreignKey, nameAttribut,
          valueAttribut);
    } catch (SQLException se) {
      throw new AttachmentException("AttachmentBmImpl.createAttachment()",
          SilverpeasException.ERROR, "attachment.EX_RECORD_NOT_LOAD", se);
    } finally {
      closeConnection(con);
    }
  }

  public Vector<AttachmentDetail> getAttachmentsByPKAndContext(AttachmentPK foreignKey,
      String context, Connection con) throws AttachmentException {
    if (con == null) {
      SilverTrace.info("attachment",
          "AttachmentBmImpl.getAttachmentsByPKAndContext()",
          "root.MSG_GEN_PARAM_VALUE",
          "parameter con is null, new connection is created !");
      con = getConnection();
    } else {
      SilverTrace.info("attachment",
          "AttachmentBmImpl.getAttachmentsByPKAndContext()",
          "root.MSG_GEN_PARAM_VALUE",
          "parameter con is not null, this connection is used !");
    }
    try {
      return dao.findByPKAndContext(con, foreignKey, context);
    } catch (SQLException se) {
      throw new AttachmentException("AttachmentBmImpl.createAttachment()",
          SilverpeasException.ERROR, "attachment.EX_RECORD_NOT_LOAD", se);
    } finally {
      closeConnection(con);
    }
  }

  public void deleteAttachment(AttachmentPK primaryKey)
      throws AttachmentException {
    Connection con = getConnection();
    try {
      AttachmentI18NDAO.removeTranslations(con, primaryKey);

      dao.deleteAttachment(con, primaryKey);
    } catch (SQLException se) {
      throw new AttachmentException("AttachmentBmImpl.createAttachment()",
          SilverpeasException.ERROR, "attachment_MSG_NOT_DELETE_FILE", se);
    } finally {
      closeConnection(con);
    }
  }

  public void updateForeignKey(AttachmentPK pk, String foreignKey)
      throws AttachmentException {
    Connection con = getConnection();
    try {
      dao.updateForeignKey(con, pk, foreignKey);
    } catch (SQLException se) {
      throw new AttachmentException("AttachmentBmImpl.updateForeignKey()",
          SilverpeasException.ERROR, "EX_RECORD_NOT_UPDATE_ATTACHMENT", se);
    } finally {
      closeConnection(con);
    }
  }

  public Collection<AttachmentDetail> getAllAttachmentByDate(Date date, boolean alert)
      throws AttachmentException {
    Connection con = getConnection();
    try {
      return dao.getAllAttachmentByDate(con, date, alert);
    } catch (SQLException se) {
      throw new AttachmentException("AttachmentBmImpl.createAttachment()",
          SilverpeasException.ERROR, "attachment.EX_RECORD_NOT_LOAD", se);
    } finally {
      closeConnection(con);
    }
  }

  public Collection<AttachmentDetail> getAllAttachmentToLib(Date date) throws AttachmentException {
    Connection con = getConnection();
    try {
      return dao.getAllAttachmentToLib(con, date);
    } catch (SQLException se) {
      throw new AttachmentException("AttachmentBmImpl.createAttachment()",
          SilverpeasException.ERROR, "attachment.EX_RECORD_NOT_LOAD", se);
    } finally {
      closeConnection(con);
    }
  }

  public void notifyUser(NotificationMetaData notifMetaData, String senderId,
      String componentId) throws AttachmentException {
    SilverTrace.info("attachment", "AttachmentBmImpl.notifyUser()",
        "root.MSG_GEN_EXIT_METHOD");
    try {
      SilverTrace.info("attachment", "AttachmentBmImpl.notifyUser()",
          "root.MSG_GEN_EXIT_METHOD", " senderId = " + senderId
          + " componentId = " + componentId);
      if (notifMetaData.getSender() == null
          || notifMetaData.getSender().length() == 0) {
        notifMetaData.setSender(senderId);
      }
      NotificationSender notifSender = new NotificationSender(componentId);
      notifSender.notifyUser(notifMetaData);
    } catch (NotificationManagerException e) {
      throw new AttachmentException("AttachmentBmImpl.notifyUser()",
          SilverpeasRuntimeException.ERROR,
          "attachment.MSG_ATTACHMENT_NOT_EXIST", e);
    }
  }

  public void updateXmlForm(AttachmentPK pk, String language, String xmlFormName)
      throws AttachmentException {
    Connection con = getConnection();
    try {
      if (!I18NHelper.isI18N || !StringUtil.isDefined(language)) {
        dao.updateXmlForm(con, pk, xmlFormName);
      } else {
        AttachmentDetail attachment = dao.findByPrimaryKey(con, pk);
        if (!StringUtil.isDefined(attachment.getLanguage())
            || attachment.getLanguage().equals(language)) {
          dao.updateXmlForm(con, pk, xmlFormName);
        } else {
          AttachmentI18NDAO.updateXmlForm(con, pk, language, xmlFormName);
        }
      }
    } catch (SQLException se) {
      throw new AttachmentException("AttachmentBmImpl.updateXmlForm()",
          SilverpeasException.ERROR, "EX_RECORD_NOT_UPDATE_ATTACHMENT", se);
    } finally {
      closeConnection(con);
    }
  }

  public void sortAttachments(List<AttachmentPK> attachmentPKs) throws AttachmentException {
    Connection con = getConnection();
    try {
      dao.sortAttachments(con, attachmentPKs);
    } catch (SQLException se) {
      throw new AttachmentException("AttachmentBmImpl.sortAttachments()",
          SilverpeasException.ERROR, "EX_ERROR_WHEN_SORTING_ATTACHMENTS", se);
    } finally {
      closeConnection(con);
    }
  }

  private Connection getConnection() throws AttachmentException {
    SilverTrace.info("attachment", "AttachmentBmImpl.getConnection()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      Connection con = DBUtil.makeConnection(JNDINames.ATTACHMENT_DATASOURCE);
      return con;
    } catch (Exception e) {
      throw new AttachmentException("AttachmentBmImpl.getConnection()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  private void closeConnection(Connection con) {
    try {
      if (con != null) {
        con.close();
      }
    } catch (Exception e) {
      SilverTrace.error("attachment", "AttachmentBmImpl.closeConnection()",
          "root.EX_CONNECTION_CLOSE_FAILED", "", e);
    }
  }
}
