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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.wysiwyg.control;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import javax.jcr.RepositoryException;

import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.util.Charsets;

import com.silverpeas.jcrutil.BasicDaoFactory;
import com.silverpeas.jcrutil.model.SilverpeasRegister;
import com.silverpeas.util.ForeignPK;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.commons.cnd.ParseException;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class WysiwygControllerTest {

  public WysiwygControllerTest() {
  }

  /**
   * Test of finNode method, of class WysiwygController.
   */
  @Test
  public void testFinNode() {
    String path = "c:\\\\silverpeas_data\\\\webSite17\\\\id\\\\rep1\\\\rep2\\\\rep3";
    String componentId = "webSite17";
    String result = WysiwygController.finNode(path, componentId);
    assertThat(result, is("id\\rep1\\rep2\\rep3"));

    path = "c:\\silverpeas_data\\webSite17\\id\\rep1\\rep2\\rep3";
    componentId = "webSite17";
    result = WysiwygController.finNode(path, componentId);
    assertThat(result, is("id\\rep1\\rep2\\rep3"));

    path = "/var/silverpeas_data/webSite17/id/rep1/rep2/rep3";
    componentId = "webSite17";
    result = WysiwygController.finNode(path, componentId);
    assertThat(result, is("id/rep1/rep2/rep3"));
  }

  /**
   * Test of finNode2 method, of class WysiwygController.
   */
  @Test
  public void testFinNode2() {
    String path = "c:\\\\silverpeas_data\\\\webSite17\\\\id\\\\rep1\\\\rep2\\\\rep3";
    String componentId = "webSite17";
    String result = WysiwygController.finNode2(path, componentId);
    assertThat(result, is("rep1\\rep2\\rep3"));

    path = "/var/silverpeas_data/webSite17/id/rep1/rep2/rep3";
    componentId = "webSite17";
    result = WysiwygController.finNode2(path, componentId);
    assertThat(result, is("rep1/rep2/rep3"));
  }

  /**
   * Test of getNodePath method, of class WysiwygController.
   */
  @Test
  public void testGetNodePath() {
    String currentPath = "c:\\j2sdk\\public_html\\WAwebSiteUploads\\webSite17\\3\\rep1\\rep11\\";
    String componentId = "webSite17";
    String result = WysiwygController.getNodePath(currentPath, componentId);
    assertThat(result, is("c:\\j2sdk\\public_html\\WAwebSiteUploads\\webSite17\\3"));
    currentPath = "c:\\j2sdk\\public_html\\WAwebSiteUploads\\webSite17\\3";
    result = WysiwygController.getNodePath(currentPath, componentId);
    assertThat(result, is("c:\\j2sdk\\public_html\\WAwebSiteUploads\\webSite17\\3"));
  }

  /**
   * Test of getNodePath method, of class WysiwygController.
   */
  @Test
  public void testGetNodePathOnLinux() {
    String currentPath = "/home/ehugonnet/programs/silverpeas/data/web/website.war/webSites45/1";
    String componentId = "webSites45";
    String result = WysiwygController.getNodePath(currentPath, componentId);
    assertThat(result, is("/home/ehugonnet/programs/silverpeas/data/web/website.war/webSites45/1"));
    currentPath
        = "/home/ehugonnet/programs/silverpeas/data/web/website.war/webSites45/1/repertoire1/repertoire2/";
    result = WysiwygController.getNodePath(currentPath, componentId);
    assertThat(result, is("/home/ehugonnet/programs/silverpeas/data/web/website.war/webSites45/1"));
  }

  /**
   * Test of ignoreAntiSlash method, of class WysiwygController.
   */
  @Test
  public void testSuppressLeadingSlashesOrAntislashes() {
    String chemin = "\\\\rep1\\rep2\\rep3";
    String result = WysiwygController.suppressLeadingSlashesOrAntislashes(chemin);
    assertThat(result, is("rep1\\rep2\\rep3"));

    chemin = "\\rep1\\rep2\\rep3";
    result = WysiwygController.suppressLeadingSlashesOrAntislashes(chemin);
    assertThat(result, is("rep1\\rep2\\rep3"));

    chemin = "/rep1/rep2/rep3";
    result = WysiwygController.suppressLeadingSlashesOrAntislashes(chemin);
    assertThat(result, is("rep1/rep2/rep3"));
  }

  /**
   * Test of ignoreAntiSlash method, of class WysiwygController.
   */
  @Test
  public void testIgnoreLeadingSlash() {
    String chemin = "\\\\rep1\\rep2\\rep3";
    String result = WysiwygController.ignoreLeadingSlash(chemin);
    assertThat(result, is("\\\\rep1\\rep2\\rep3"));

    chemin = "//rep1/rep2/rep3";
    result = WysiwygController.ignoreLeadingSlash(chemin);
    assertThat(result, is("rep1/rep2/rep3"));

    chemin = "/rep1/rep2/rep3";
    result = WysiwygController.ignoreLeadingSlash(chemin);
    assertThat(result, is("rep1/rep2/rep3"));
    chemin = "";
    result = WysiwygController.ignoreLeadingSlash(chemin);
    assertThat(result, is(""));
  }

  /**
   * Test of ignoreAntiSlash method, of class WysiwygController.
   */
  @Test
  public void testSupprDoubleAntiSlash() {
    String chemin = "\\\\rep1\\rep2\\\\rep3";
    String result = WysiwygController.supprDoubleAntiSlash(chemin);
    assertThat(result, is("\\rep1\\rep2\\rep3"));

    chemin = "\\rep1\\rep2\\rep3";
    result = WysiwygController.supprDoubleAntiSlash(chemin);
    assertThat(result, is("\\rep1\\rep2\\rep3"));

    chemin = "/rep1/rep2/rep3";
    result = WysiwygController.supprDoubleAntiSlash(chemin);
    assertThat(result, is("/rep1/rep2/rep3"));
  }

  @Test
  public void testSuppressFinalSlash() {
    String result = WysiwygController.suppressFinalSlash("\\\\id\\\\rep1\\\\rep2\\\\rep3/");
    assertThat(result, is("\\\\id\\\\rep1\\\\rep2\\\\rep3"));

    result = WysiwygController.suppressFinalSlash("\\\\id\\\\rep1\\\\rep2\\\\rep3////");
    assertThat(result, is("\\\\id\\\\rep1\\\\rep2\\\\rep3"));

    result = WysiwygController.suppressFinalSlash("");
    assertThat(result, is(""));
  }

  /**
   * Test of ignoreAntiSlash method, of class WysiwygController.
   */
  @Test
  public void testCreateWysiwyg() throws RepositoryException, IOException, ParseException {

    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
        "/spring-pure-memory-jcr.xml");
    Reader reader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(
        "silverpeas-jcr.txt"), Charsets.UTF_8);
    try {
      SilverpeasRegister.registerNodeTypes(reader);
    } finally {
      IOUtils.closeQuietly(reader);
    }
    try {
      String componentId = "blog974";
      String messageId = "18";
      String expectedContent = "Hello World";
      String userId = "7";
      String language = "en";
      WysiwygController.createFileAndAttachment(expectedContent, new ForeignPK(messageId,
          componentId), userId, language);
      String content = WysiwygController.load(componentId, messageId, language);
      assertThat(content, is(expectedContent));
      List<SimpleDocument> lockedFiles = AttachmentServiceFactory.getAttachmentService()
          .listDocumentsLockedByUser(userId, null);
      assertThat(lockedFiles, is(notNullValue()));
      assertThat(lockedFiles, hasSize(0));
    } finally {
      ((JackrabbitRepository) context.getBean(BasicDaoFactory.JRC_REPOSITORY)).shutdown();
      context.close();
    }
  }

  @Test
  public void testReplaceInternalImageId() throws IOException {
    InputStream in = WysiwygController.class.getResourceAsStream("24wysiwyg_fr.txt");
    InputStream resultIn = WysiwygController.class.getResourceAsStream("move_result.txt");
    try {
      String content = IOUtils.toString(in);
      String result = IOUtils.toString(resultIn);
      SimpleDocumentPK oldPk = new SimpleDocumentPK("dd99f10b-0640-40d3-9ef4-8b84d29a7c85",
          "kmelia1");
      oldPk.setOldSilverpeasId(34L);
      SimpleDocumentPK newPk = new SimpleDocumentPK("f2eb803f-cb46-4988-b89d-045c4e846da4",
          "kmelia1");
      newPk.setOldSilverpeasId(41L);
      String move = WysiwygController.replaceInternalImageId(content, oldPk, newPk);
      assertThat(move, is(result));
    } finally {
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(resultIn);
    }
  }

  @Test
  public void testReplaceInternalImageIdInOtherInstance() throws IOException {
    InputStream in = WysiwygController.class.getResourceAsStream("24wysiwyg_fr.txt");
    InputStream resultIn = WysiwygController.class.getResourceAsStream("move_out_result.txt");
    try {
      String content = IOUtils.toString(in);
      String result = IOUtils.toString(resultIn);
      SimpleDocumentPK oldPk = new SimpleDocumentPK("359d2924-b6c6-461c-a459-2eef38f12c3c",
          "kmelia1");
      oldPk.setOldSilverpeasId(34L);
      SimpleDocumentPK newPk = new SimpleDocumentPK("f2eb803f-cb46-4988-b89d-045c4e846da4",
          "kmelia18");
      newPk.setOldSilverpeasId(41L);
      String move = WysiwygController.replaceInternalImageId(content, oldPk, newPk);
      assertThat(move, is(result));
    } finally {
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(resultIn);
    }
  }
}
