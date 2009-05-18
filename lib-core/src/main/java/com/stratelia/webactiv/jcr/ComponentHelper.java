package com.stratelia.webactiv.jcr;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import com.silverpeas.jcrutil.BasicDaoFactory;
import com.silverpeas.jcrutil.JcrConstants;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;

public class ComponentHelper {

  public static void deleteComponent(String componentId)
      throws InstanciationException {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      if (session.getRootNode().hasNode(componentId)) {
        Node componentNode = session.getRootNode().getNode(componentId);
        componentNode.remove();
      }
      session.save();
    } catch (RepositoryException rex) {
      throw new InstanciationException("ComponentHelper.deleteComponent()",
          InstanciationException.ERROR, "root.EX_RECORD_DELETION_FAILED",
          "Node name = " + componentId, rex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  public static void createForumComponent(String componentId)
      throws InstanciationException {
    createComponentNode(componentId, JcrConstants.SLV_FORUM);
  }

  public static void createKmeliaComponent(String componentId)
      throws InstanciationException {
    createComponentNode(componentId, JcrConstants.SLV_KMELIA);
  }

  public static void createBlogComponent(String componentId)
      throws InstanciationException {
    createComponentNode(componentId, JcrConstants.SLV_BLOG);
  }

  public static void createQuickInfoComponent(String componentId)
      throws InstanciationException {
    createComponentNode(componentId, JcrConstants.SLV_QUICK_INFO);
  }

  public static final void createAttachmentFolder(String componentId)
      throws InstanciationException {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      if (!session.getRootNode().hasNode(componentId)) {
        session.getRootNode().addNode(componentId, JcrConstants.NT_FOLDER);
      }
      session.save();
    } catch (RepositoryException rex) {
      throw new InstanciationException("ComponentHelper.createComponentNode()",
          InstanciationException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
          "Node name = " + componentId + "/" + JcrConstants.NT_FOLDER, rex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  protected static final void createComponentNode(String componentId,
      String componentType) throws InstanciationException {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      if (session.getRootNode().hasNode(componentId)) {
        throw new InstanciationException(
            "ComponentHelper.createComponentNode()",
            InstanciationException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
            "Node name = " + componentId + "/" + componentType
                + " already exists");
      }
      session.getRootNode().addNode(componentId, componentType);
      session.save();
    } catch (RepositoryException rex) {
      throw new InstanciationException("ComponentHelper.createComponentNode()",
          InstanciationException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
          "Node name = " + componentId + "/" + componentType, rex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  public static void createNewsEditoComponent(String componentId)
      throws InstanciationException {
    createComponentNode(componentId, JcrConstants.SLV_NEWS_EDITO);
  }

  public static void createGalleryComponent(String componentId)
      throws InstanciationException {
    createComponentNode(componentId, JcrConstants.SLV_GALLERY);
  }

  public static void createQuestionReplyComponent(String componentId)
      throws InstanciationException {
    createComponentNode(componentId, JcrConstants.SLV_QUESTION_REPLY);
  }

  public static void createWebSitesComponent(String componentId)
      throws InstanciationException {
    createComponentNode(componentId, JcrConstants.SLV_WEB_SITES);
  }

}
