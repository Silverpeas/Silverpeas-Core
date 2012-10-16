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
package org.silverpeas.attachment.webdav.impl;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.webdav.WebdavRepository;
import org.silverpeas.attachment.webdav.WebdavService;

import com.silverpeas.jcrutil.BasicDaoFactory;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentRuntimeException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

@Named("webdavService")
public class WebDavDocumentService implements WebdavService {

  @Inject
  @Named("webdavRepository")
  private WebdavRepository webdavAttachmentDao;

  @Override
  public void createAttachment(SimpleDocument attachment) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      webdavAttachmentDao.createAttachmentNode(session, attachment);
      session.save();
    } catch (IOException ex) {
      SilverTrace.error("attachment", "JcrAttachmentServiceImpl",
          "attachment.jcr.create.exception", ex);
      throw new AttachmentRuntimeException("JcrAttachmentServiceImpl",
          SilverpeasRuntimeException.ERROR, "attachment.jcr.create.exception", ex);
    } catch (RepositoryException ex) {
      SilverTrace.error("attachment", "JcrAttachmentServiceImpl",
          "attachment.jcr.create.exception", ex);
      throw new AttachmentRuntimeException("JcrAttachmentServiceImpl",
          SilverpeasRuntimeException.ERROR, "attachment.jcr.create.exception", ex);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  @Override
  public void getUpdatedDocument(SimpleDocument attachment) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      webdavAttachmentDao.updateAttachment(session, attachment);
      session.save();
    } catch (RepositoryException ex) {
      SilverTrace.error("attachment", "JcrAttachmentServiceImpl",
          "attachment.jcr.create.exception", ex);
      throw new AttachmentRuntimeException("JcrAttachmentServiceImpl",
          SilverpeasRuntimeException.ERROR, "attachment.jcr.create.exception", ex);
    } catch (IOException ex) {
      SilverTrace.error("attachment", "JcrAttachmentServiceImpl",
          "attachment.jcr.create.exception", ex);
      throw new AttachmentRuntimeException("JcrAttachmentServiceImpl",
          SilverpeasRuntimeException.ERROR, "attachment.jcr.create.exception", ex);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  @Override
  public void deleteAttachment(SimpleDocument attachment) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      webdavAttachmentDao.deleteAttachmentNode(session, attachment);
      session.save();
    } catch (RepositoryException ex) {
      SilverTrace.error("attachment", "JcrAttachmentServiceImpl",
          "attachment.jcr.delete.exception", ex);
      throw new AttachmentRuntimeException("JcrAttachmentServiceImpl",
          SilverpeasRuntimeException.ERROR, "attachment.jcr.delete.exception", ex);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  @Override
  public void updateNodeAttachment(SimpleDocument attachment) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      webdavAttachmentDao.updateNodeAttachment(session, attachment);
      session.save();
    } catch (RepositoryException ex) {
      SilverTrace.error("attachment", "JcrAttachmentServiceImpl",
          "attachment.jcr.create.exception", ex);
      throw new AttachmentRuntimeException("JcrAttachmentServiceImpl",
          SilverpeasRuntimeException.ERROR, "attachment.jcr.delete.exception", ex);
    } catch (IOException ex) {
      SilverTrace.error("attachment", "JcrAttachmentServiceImpl",
          "attachment.jcr.create.exception", ex);
      throw new AttachmentRuntimeException("JcrAttachmentServiceImpl",
          SilverpeasRuntimeException.ERROR, "attachment.jcr.delete.exception", ex);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  @Override
  public boolean isNodeLocked(SimpleDocument attachment) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      return webdavAttachmentDao.isNodeLocked(session, attachment);
    } catch (RepositoryException ex) {
      SilverTrace.error("attachment", "JcrAttachmentServiceImpl",
          "attachment.jcr.isLocked.exception", ex);
      throw new AttachmentRuntimeException("JcrAttachmentServiceImpl",
          SilverpeasRuntimeException.ERROR, "attachment.jcr.delete.exception", ex);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }
}