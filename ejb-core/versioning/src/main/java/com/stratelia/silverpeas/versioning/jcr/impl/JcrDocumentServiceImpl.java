package com.stratelia.silverpeas.versioning.jcr.impl;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import com.silverpeas.jcrutil.BasicDaoFactory;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.ejb.VersioningRuntimeException;
import com.stratelia.silverpeas.versioning.jcr.JcrDocumentDao;
import com.stratelia.silverpeas.versioning.jcr.JcrDocumentService;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class JcrDocumentServiceImpl implements JcrDocumentService {

  private JcrDocumentDao jcrDocumentDao;

  public void setJcrDocumentDao(JcrDocumentDao jcrDocumentDao) {
    this.jcrDocumentDao = jcrDocumentDao;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.stratelia.webactiv.util.document.model.jcr.impl.JcrDocumentService#
   * createDocument(com.stratelia.webactiv.util.document.model.DocumentVersion,
   * java.lang.String)
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
   * 
   * @see
   * com.stratelia.webactiv.util.document.model.jcr.impl.JcrDocumentService#
   * getUpdatedDocument
   * (com.stratelia.webactiv.util.document.model.DocumentVersion,
   * java.lang.String)
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
   * 
   * @see
   * com.stratelia.webactiv.util.document.model.jcr.impl.JcrDocumentService#
   * deleteDocument(com.stratelia.webactiv.util.document.model.DocumentVersion,
   * java.lang.String)
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
   * Indicate if the node for the specified attachment is currently locked (for
   * example by Office in the case of a webdav online edition).
   * 
   * @param attachment
   *          the attachment.
   * @param language
   *          the language to obtain the file.
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
