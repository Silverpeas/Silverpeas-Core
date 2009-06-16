package com.silverpeas.jcrutil.security.jaas;

import java.io.ByteArrayInputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;

import com.silverpeas.jcrutil.BasicDaoFactory;
import com.silverpeas.jcrutil.JcrConstants;
import com.silverpeas.jcrutil.model.impl.AbstractJcrRegisteringTestCase;
import com.silverpeas.util.MimeTypes;

public class TestAccessAuthentified extends AbstractJcrRegisteringTestCase {

  static{
    registred = false;
  }

  private static final String FOLDER_NAME = "SimpleTest";

  private static final String FILE_NAME = "MyTest";

  private static final String BART_ID = "7";

  private static final String BART_LOGIN = "bsimpson";

  private static final String BART_PASSWORD = "bart";

  protected String[] getConfigLocations() {
    return new String[] { "spring-jaas.xml" };
  }

  protected void onSetUp() {
    super.onSetUp();
    Session session = null;
    try {
      registerSilverpeasNodeTypes();
      session = BasicDaoFactory.getSystemSession();
      Node rootNode = session.getRootNode();
      rootNode.addNode(FOLDER_NAME, JcrConstants.NT_FOLDER);
      session.save();
    } catch (Exception ex) {
      fail(ex.getMessage());
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  public void testAccessFileOwnable() throws Exception {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      Node folder = session.getRootNode().getNode(FOLDER_NAME);
      Node fileNode = folder.addNode(FILE_NAME, JcrConstants.NT_FILE);
      fileNode.addMixin(JcrConstants.SLV_OWNABLE_MIXIN);
      fileNode.setProperty(JcrConstants.SLV_PROPERTY_OWNER, BART_ID);
      Node contentNode = fileNode.addNode(JcrConstants.JCR_CONTENT,
          JcrConstants.NT_RESOURCE);
      contentNode.setProperty(JcrConstants.JCR_MIMETYPE,
          MimeTypes.PLAIN_TEXT_MIME_TYPE);
      contentNode.setProperty(JcrConstants.JCR_ENCODING, "");
      contentNode.setProperty(JcrConstants.JCR_DATA, new ByteArrayInputStream(
          "Bonjour le monde".getBytes()));
      Calendar lastModified = Calendar.getInstance();
      contentNode.setProperty(JcrConstants.JCR_LASTMODIFIED, lastModified);
      session.save();
    } catch (Exception ex) {
      fail(ex.getMessage());
    } finally {
      BasicDaoFactory.logout(session);
    }
    try {
      session = BasicDaoFactory.getSystemSession();
      validateFile(session, true, true);
    } catch (Exception ex) {
      fail(ex.getMessage());
    } finally {
      BasicDaoFactory.logout(session);
    }
    try {
      session = BasicDaoFactory.getAuthentifiedSession(BART_LOGIN,
          BART_PASSWORD);
      validateFile(session, true, true);
    } catch (Exception ex) {
      ex.printStackTrace();
      fail(ex.getMessage());
    } finally {
      BasicDaoFactory.logout(session);
    }
    try {
      session = BasicDaoFactory.getAuthentifiedSession("lsimpson", "lisa");
      validateFile(session, false, true);
    } catch (Exception ex) {
      fail(ex.getMessage());
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  public void testAccessFileNotOwnable() throws Exception {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      Node folder = session.getRootNode().getNode(FOLDER_NAME);
      Node fileNode = folder.addNode(FILE_NAME, JcrConstants.NT_FILE);
      Node contentNode = fileNode.addNode(JcrConstants.JCR_CONTENT,
          JcrConstants.NT_RESOURCE);
      contentNode.setProperty(JcrConstants.JCR_MIMETYPE,
          MimeTypes.PLAIN_TEXT_MIME_TYPE);
      contentNode.setProperty(JcrConstants.JCR_ENCODING, "");
      contentNode.setProperty(JcrConstants.JCR_DATA, new ByteArrayInputStream(
          "Bonjour le monde".getBytes()));
      Calendar lastModified = Calendar.getInstance();
      contentNode.setProperty(JcrConstants.JCR_LASTMODIFIED, lastModified);
      session.save();
    } catch (Exception ex) {
      fail(ex.getMessage());
    } finally {
      BasicDaoFactory.logout(session);
    }
    try {
      session = BasicDaoFactory.getSystemSession();
      validateFile(session, true, false);
    } catch (Exception ex) {
      fail(ex.getMessage());
    } finally {
      BasicDaoFactory.logout(session);
    }
    try {
      session = BasicDaoFactory.getAuthentifiedSession(BART_LOGIN,
          BART_PASSWORD);
      validateFile(session, true, false);
    } catch (Exception ex) {
      ex.printStackTrace();
      fail(ex.getMessage());
    } finally {
      BasicDaoFactory.logout(session);
    }

    try {
      session = BasicDaoFactory.getAuthentifiedSession("lsimpson", "lisa");
      validateFile(session, true, false);
    } catch (Exception ex) {
      fail(ex.getMessage());
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  protected void validateFile(Session session, boolean isAccessible,
      boolean hasMixin) throws RepositoryException {
    Node folderNode = session.getRootNode().getNode(FOLDER_NAME);
    assertNotNull("Folder not found", folderNode);
    assertEquals("Folder not of correct type", JcrConstants.NT_FOLDER,
        folderNode.getPrimaryNodeType().getName());
    if (isAccessible) {
      Node fileNode = session.getRootNode().getNode(FOLDER_NAME).getNode(
          FILE_NAME);
      assertNotNull("File not found", fileNode);
      assertEquals("File not of correct type", JcrConstants.NT_FILE, fileNode
          .getPrimaryNodeType().getName());
      assertEquals("File has not the correct mixin", hasMixin, hasMixin(
          JcrConstants.SLV_OWNABLE_MIXIN, fileNode));
    } else {
      assertFalse("File should not be accessible", folderNode
          .hasNode(FILE_NAME));
    }
  }

  protected void validateFolder(Session session, boolean hasMixin)
      throws RepositoryException {
    Node parentFolderNode = session.getRootNode().getNode(FOLDER_NAME);
    assertNotNull("Folder not found", parentFolderNode);
    assertEquals("Folder not of correct type", JcrConstants.NT_FOLDER,
        parentFolderNode.getPrimaryNodeType().getName());
    Node folderNode = parentFolderNode.getNode(FILE_NAME);
    assertNotNull("Folder not found", folderNode);
    assertEquals("Folder not of correct type", JcrConstants.NT_FOLDER,
        folderNode.getPrimaryNodeType().getName());
    assertEquals(JcrConstants.NT_FOLDER, folderNode.getPrimaryNodeType()
        .getName());
    assertEquals("Folder has not the correct mixin", hasMixin, hasMixin(
        JcrConstants.SLV_OWNABLE_MIXIN, folderNode));
  }

  public void testAccessFolderOwnable() throws Exception {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      Node folder = session.getRootNode().getNode(FOLDER_NAME);
      Node fileNode = folder.addNode(FILE_NAME, JcrConstants.NT_FOLDER);
      fileNode.addMixin(JcrConstants.SLV_OWNABLE_MIXIN);
      fileNode.setProperty(JcrConstants.SLV_PROPERTY_OWNER, BART_ID);
      session.save();
    } catch (Exception ex) {
      fail(ex.getMessage());
    } finally {
      BasicDaoFactory.logout(session);
    }
    try {
      session = BasicDaoFactory.getSystemSession();
      validateFolder(session, true);
    } catch (Exception ex) {
      fail(ex.getMessage());
    } finally {
      BasicDaoFactory.logout(session);
    }
    try {
      session = BasicDaoFactory.getAuthentifiedSession(BART_LOGIN,
          BART_PASSWORD);
      validateFolder(session, true);
    } catch (Exception ex) {
      ex.printStackTrace();
      fail(ex.getMessage());
    } finally {
      BasicDaoFactory.logout(session);
    }

    try {
      session = BasicDaoFactory.getAuthentifiedSession("lsimpson", "lisa");
      validateFolder(session, true);
    } catch (Exception ex) {
      fail(ex.getMessage());
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  public void testAccessFolderNotOwnable() throws Exception {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      Node folder = session.getRootNode().getNode(FOLDER_NAME);
      folder.addNode(FILE_NAME, JcrConstants.NT_FOLDER);
      session.save();
    } catch (Exception ex) {
      fail(ex.getMessage());
    } finally {
      BasicDaoFactory.logout(session);
    }
    try {
      session = BasicDaoFactory.getSystemSession();
      validateFolder(session, false);
    } catch (Exception ex) {
      fail(ex.getMessage());
    } finally {
      BasicDaoFactory.logout(session);
    }
    try {
      session = BasicDaoFactory.getAuthentifiedSession(BART_LOGIN,
          BART_PASSWORD);
      validateFolder(session, false);
    } catch (Exception ex) {
      ex.printStackTrace();
      fail(ex.getMessage());
    } finally {
      BasicDaoFactory.logout(session);
    }

    try {
      session = BasicDaoFactory.getAuthentifiedSession("lsimpson", "lisa");
      validateFolder(session, false);
    } catch (Exception ex) {
      fail(ex.getMessage());
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  protected IDataSet getDataSet() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(this
        .getClass().getResourceAsStream("test-jcrutil-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    return dataSet;
  }

  @Override
  protected void clearRepository() throws Exception {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      Node rootNode = session.getRootNode();
      rootNode.getNode(FOLDER_NAME).remove();
      session.save();
    } catch (Exception ex) {
      fail(ex.getMessage());
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  protected boolean hasMixin(String mixinName, Node node)
      throws RepositoryException {
    NodeType[] types = node.getMixinNodeTypes();
    for (NodeType type : types) {
      if (mixinName.equals(type.getName())) {
        return true;
      }
    }
    return false;
  }
}
