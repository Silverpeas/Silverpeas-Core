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
package com.stratelia.webactiv.util.attachment.model.jcr.impl;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import com.silverpeas.jcrutil.BasicDaoFactory;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentRuntimeException;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.attachment.model.jcr.JcrAttachmentDao;
import com.stratelia.webactiv.util.attachment.model.jcr.JcrAttachmentService;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class JcrAttachmentServiceImpl implements JcrAttachmentService {

  private JcrAttachmentDao jcrAttachmentDao;

  public void setJcrAttachmentDao(JcrAttachmentDao jcrAttachmentDao) {
    this.jcrAttachmentDao = jcrAttachmentDao;
  }

  public String getLanguage(String language) {
    if (language != null
        && ("fr".equalsIgnoreCase(language) || "".equals(language.trim()))) {
      return null;
    }
    return language;
  }

  public void createAttachment(AttachmentDetail attachment, String language) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      jcrAttachmentDao.createAttachmentNode(session, attachment,
          getLanguage(language));
      session.save();
    } catch (RepositoryException ex) {
      SilverTrace.error("attachment", "JcrAttachmentServiceImpl",
          "attachment.jcr.create.exception", ex);
      throw new AttachmentRuntimeException("JcrAttachmentServiceImpl",
          SilverpeasRuntimeException.ERROR, "attachment.jcr.create.exception",
          ex);
    } catch (IOException ex) {
      SilverTrace.error("attachment", "JcrAttachmentServiceImpl",
          "attachment.jcr.create.exception", ex);
      throw new AttachmentRuntimeException("JcrAttachmentServiceImpl",
          SilverpeasRuntimeException.ERROR, "attachment.jcr.create.exception",
          ex);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  public void getUpdatedDocument(AttachmentDetail attachment, String language) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      jcrAttachmentDao.updateAttachment(session, attachment,
          getLanguage(language));
      session.save();
    } catch (RepositoryException ex) {
      SilverTrace.error("attachment", "JcrAttachmentServiceImpl",
          "attachment.jcr.create.exception", ex);
      throw new AttachmentRuntimeException("JcrAttachmentServiceImpl",
          SilverpeasRuntimeException.ERROR, "attachment.jcr.create.exception",
          ex);
    } catch (IOException ex) {
      SilverTrace.error("attachment", "JcrAttachmentServiceImpl",
          "attachment.jcr.create.exception", ex);
      throw new AttachmentRuntimeException("JcrAttachmentServiceImpl",
          SilverpeasRuntimeException.ERROR, "attachment.jcr.create.exception",
          ex);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  public void deleteAttachment(AttachmentDetail attachment, String language) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      jcrAttachmentDao.deleteAttachmentNode(session, attachment,
          getLanguage(language));
      session.save();
    } catch (RepositoryException ex) {
      SilverTrace.error("attachment", "JcrAttachmentServiceImpl",
          "attachment.jcr.delete.exception", ex);
      throw new AttachmentRuntimeException("JcrAttachmentServiceImpl",
          SilverpeasRuntimeException.ERROR, "attachment.jcr.delete.exception",
          ex);
    } catch (IOException ex) {
      SilverTrace.error("attachment", "JcrAttachmentServiceImpl",
          "attachment.jcr.delete.exception", ex);
      throw new AttachmentRuntimeException("JcrAttachmentServiceImpl",
          SilverpeasRuntimeException.ERROR, "attachment.jcr.delete.exception",
          ex);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  public void updateNodeAttachment(AttachmentDetail attachment, String language) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      jcrAttachmentDao.updateNodeAttachment(session, attachment,
          getLanguage(language));
      session.save();
    } catch (RepositoryException ex) {
      SilverTrace.error("attachment", "JcrAttachmentServiceImpl",
          "attachment.jcr.create.exception", ex);
      throw new AttachmentRuntimeException("JcrAttachmentServiceImpl",
          SilverpeasRuntimeException.ERROR, "attachment.jcr.delete.exception",
          ex);
    } catch (IOException ex) {
      SilverTrace.error("attachment", "JcrAttachmentServiceImpl",
          "attachment.jcr.create.exception", ex);
      throw new AttachmentRuntimeException("JcrAttachmentServiceImpl",
          SilverpeasRuntimeException.ERROR, "attachment.jcr.delete.exception",
          ex);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  public boolean isNodeLocked(AttachmentDetail attachment, String language) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      return jcrAttachmentDao.isNodeLocked(session, attachment,
          getLanguage(language));
    } catch (RepositoryException ex) {
      SilverTrace.error("attachment", "JcrAttachmentServiceImpl",
          "attachment.jcr.isLocked.exception", ex);
      throw new AttachmentRuntimeException("JcrAttachmentServiceImpl",
          SilverpeasRuntimeException.ERROR, "attachment.jcr.delete.exception",
          ex);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }
}