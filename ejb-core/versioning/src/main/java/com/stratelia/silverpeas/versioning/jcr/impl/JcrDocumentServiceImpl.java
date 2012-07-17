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

package com.stratelia.silverpeas.versioning.jcr.impl;

import com.silverpeas.jcrutil.BasicDaoFactory;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.ejb.VersioningRuntimeException;
import com.stratelia.silverpeas.versioning.jcr.JcrDocumentDao;
import com.stratelia.silverpeas.versioning.jcr.JcrDocumentService;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;

public class JcrDocumentServiceImpl implements JcrDocumentService {

  private JcrDocumentDao jcrDocumentDao;

  public void setJcrDocumentDao(JcrDocumentDao jcrDocumentDao) {
    this.jcrDocumentDao = jcrDocumentDao;
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.webactiv.util.document.model.jcr.impl.JcrDocumentService#
   * createDocument(com.stratelia.webactiv.util.document.model.DocumentVersion, java.lang.String)
   */
  public void createDocument(DocumentVersion document) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      jcrDocumentDao.createDocumentNode(session, document);
      session.save();
    } catch (RepositoryException ex) {
      SilverTrace.error("document", "JcrDocumentServiceImpl",
          "document.jcr.create.exception", ex);
      throw new VersioningRuntimeException("JcrDocumentServiceImpl",
          SilverpeasRuntimeException.ERROR, "document.jcr.create.exception", ex);
    } catch (IOException ex) {
      SilverTrace.error("document", "JcrDocumentServiceImpl",
          "document.jcr.create.exception", ex);
      throw new VersioningRuntimeException("JcrDocumentServiceImpl",
          SilverpeasRuntimeException.ERROR, "document.jcr.create.exception", ex);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.webactiv.util.document.model.jcr.impl.JcrDocumentService# getUpdatedDocument
   * (com.stratelia.webactiv.util.document.model.DocumentVersion, java.lang.String)
   */
  public void getUpdatedDocument(DocumentVersion document) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      jcrDocumentDao.updateDocument(session, document);
      session.save();
    } catch (RepositoryException ex) {
      SilverTrace.error("document", "JcrDocumentServiceImpl",
          "document.jcr.create.exception", ex);
      throw new VersioningRuntimeException("JcrDocumentServiceImpl",
          SilverpeasRuntimeException.ERROR, "document.jcr.create.exception", ex);
    } catch (IOException ex) {
      SilverTrace.error("document", "JcrDocumentServiceImpl",
          "document.jcr.create.exception", ex);
      throw new VersioningRuntimeException("JcrDocumentServiceImpl",
          SilverpeasRuntimeException.ERROR, "document.jcr.create.exception", ex);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.webactiv.util.document.model.jcr.impl.JcrDocumentService#
   * deleteDocument(com.stratelia.webactiv.util.document.model.DocumentVersion, java.lang.String)
   */
  public void deleteDocument(DocumentVersion document) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      jcrDocumentDao.deleteDocumentNode(session, document);
      session.save();
    } catch (RepositoryException ex) {
      SilverTrace.error("document", "JcrDocumentServiceImpl",
          "document.jcr.delete.exception", ex);
      throw new VersioningRuntimeException("JcrDocumentServiceImpl",
          SilverpeasRuntimeException.ERROR, "document.jcr.delete.exception", ex);
    } catch (IOException ex) {
      SilverTrace.error("document", "JcrDocumentServiceImpl",
          "document.jcr.delete.exception", ex);
      throw new VersioningRuntimeException("JcrDocumentServiceImpl",
          SilverpeasRuntimeException.ERROR, "document.jcr.delete.exception", ex);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  public void updateDocument(DocumentVersion document) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      jcrDocumentDao.updateNodeDocument(session, document);
      session.save();
    } catch (RepositoryException ex) {
      SilverTrace.error("document", "JcrDocumentServiceImpl",
          "document.jcr.create.exception", ex);
      throw new VersioningRuntimeException("JcrDocumentServiceImpl",
          SilverpeasRuntimeException.ERROR, "document.jcr.delete.exception", ex);
    } catch (IOException ex) {
      SilverTrace.error("document", "JcrDocumentServiceImpl",
          "document.jcr.create.exception", ex);
      throw new VersioningRuntimeException("JcrDocumentServiceImpl",
          SilverpeasRuntimeException.ERROR, "document.jcr.delete.exception", ex);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  /**
   * Indicate if the node for the specified attachment is currently locked (for example by Office in
   * the case of a webdav online edition).
   * @param document the attachment.
   * @return true if the node is locked - false otherwise.
   * @throws RepositoryException
   */
  public boolean isNodeLocked(DocumentVersion document) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      return jcrDocumentDao.isNodeLocked(session, document);
    } catch (RepositoryException ex) {
      SilverTrace.error("document", "JcrDocumentServiceImpl",
          "document.jcr.isLocked.exception", ex);
      throw new VersioningRuntimeException("JcrDocumentServiceImpl",
          SilverpeasRuntimeException.ERROR, "document.jcr.isLocked.exception",
          ex);
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }
}
