/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.MimeTypes;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.webactiv.util.FileRepositoryManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.DocumentType;
import org.silverpeas.attachment.model.HistorisedDocument;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.attachment.util.JcrTest;
import org.silverpeas.attachment.util.SimpleDocumentList;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class WysiwygControllerTest {

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
    currentPath = "/home/ehugonnet/programs/silverpeas/data/web/website" +
        ".war/webSites45/1/repertoire1/repertoire2/";
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
   * Test of copy method, of class WysiwygController.
   */
  @Test
  public void testCopyWysiwygThatExistsOnlyInEN() throws Exception {
    new JcrTest() {
      @Override
      public void run() {
        String componentId = "blog974";
        String messageId = "18";
        String expectedContent = "<mark>EN_Content_FileServer_ComponentId=blog974";
        String userId = "7";
        String language = "en";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);

        ForeignPK resourceSrcTestPK = new ForeignPK(messageId, componentId);
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "de"), hasSize(0));

        String destComponentId = "kmelia26";
        String destMessageId = "38";
        ForeignPK resourceDestTestPK = new ForeignPK(destMessageId, destComponentId);
        assertThat(listWysiwygs(resourceDestTestPK), hasSize(0));

        assertThat(WysiwygController.load(componentId, messageId, "fr"),
            is("<mark>EN_Content_FileServer_ComponentId=blog974"));
        assertThat(WysiwygController.load(componentId, messageId, "en"),
            is("<mark>EN_Content_FileServer_ComponentId=blog974"));
        assertThat(WysiwygController.load(destComponentId, destMessageId, "fr"), is(""));
        assertThat(WysiwygController.load(destComponentId, destMessageId, "en"), is(""));

        WysiwygController.copy(componentId, messageId, destComponentId, destMessageId, "26");

        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "de"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "de"), hasSize(0));

        assertThat(WysiwygController.load(componentId, messageId, "fr"),
            is("<mark>EN_Content_FileServer_ComponentId=blog974"));
        assertThat(WysiwygController.load(componentId, messageId, "en"),
            is("<mark>EN_Content_FileServer_ComponentId=blog974"));
        assertThat(WysiwygController.load(destComponentId, destMessageId, "fr"),
            is("<mark>EN_Content_FileServer_ComponentId=kmelia26"));
        assertThat(WysiwygController.load(destComponentId, destMessageId, "en"),
            is("<mark>EN_Content_FileServer_ComponentId=kmelia26"));
      }
    }.execute();
  }

  /**
   * Test of copy method, of class WysiwygController.
   */
  @Test
  public void testCopyWysiwygFRENNoImageInJcr() throws Exception {
    new JcrTest() {
      @Override
      public void run() throws Exception {
        String componentId = "blog974";
        String messageId = "18";
        String expectedContent =
            "<mark>EN_Content_FileServer_ComponentId=blog974_/componentId/blog974/attachmentId/18/";
        String userId = "7";
        String language = "en";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        language = "fr";
        expectedContent =
            "<mark>FR_Content_FileServer_ComponentId=blog974_/componentId/blog974/attachmentId/18/";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);


        ForeignPK resourceSrcTestPK = new ForeignPK(messageId, componentId);
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "fr"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "de"), hasSize(0));
        String destComponentId = "kmelia26";
        String destMessageId = "38";
        ForeignPK resourceDestTestPK = new ForeignPK(destMessageId, destComponentId);
        assertThat(listWysiwygs(resourceDestTestPK), hasSize(0));

        // No image
        assertThat(listImages(resourceSrcTestPK), hasSize(0));

        assertThat(WysiwygController.load(componentId, messageId, "fr"),
            is("<mark>FR_Content_FileServer_ComponentId=blog974_" +
                "/componentId/blog974/attachmentId/18/")
        );
        assertThat(WysiwygController.load(componentId, messageId, "en"),
            is("<mark>EN_Content_FileServer_ComponentId=blog974_" +
                "/componentId/blog974/attachmentId/18/")
        );
        assertThat(WysiwygController.load(destComponentId, destMessageId, "fr"), is(""));
        assertThat(WysiwygController.load(destComponentId, destMessageId, "en"), is(""));

        WysiwygController.copy(componentId, messageId, destComponentId, destMessageId, "26");

        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "fr"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "de"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "fr"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "de"), hasSize(0));

        // No image
        assertThat(listImages(resourceSrcTestPK), hasSize(0));
        assertThat(listImages(resourceDestTestPK), hasSize(0));

        assertThat(WysiwygController.load(componentId, messageId, "fr"),
            is("<mark>FR_Content_FileServer_ComponentId=blog974_" +
                "/componentId/blog974/attachmentId/18/")
        );
        assertThat(WysiwygController.load(componentId, messageId, "en"),
            is("<mark>EN_Content_FileServer_ComponentId=blog974_" +
                "/componentId/blog974/attachmentId/18/")
        );
        assertThat(WysiwygController.load(destComponentId, destMessageId, "fr"),
            is("<mark>FR_Content_FileServer_ComponentId=kmelia26_" +
                "/componentId/blog974/attachmentId/18/")
        );
        assertThat(WysiwygController.load(destComponentId, destMessageId, "en"),
            is("<mark>EN_Content_FileServer_ComponentId=kmelia26_" +
                "/componentId/blog974/attachmentId/18/")
        );
      }
    }.execute();
  }

  /**
   * Test of copy method, of class WysiwygController.
   */
  @Test
  public void testCopyWysiwygFRENWithImageInJcr() throws Exception {
    new JcrTest() {
      @Override
      public void run() throws Exception {
        String componentId = "blog974";
        String messageId = "18";
        SimpleDocument image = createImageContent(componentId, messageId);
        String expectedContent =
            "<mark>EN_Content_FileServer_ComponentId=blog974_/componentId/blog974/attachmentId/" +
                image.getId() + "/";
        String userId = "7";
        String language = "en";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        language = "fr";
        expectedContent =
            "<mark>FR_Content_FileServer_ComponentId=blog974_/componentId/blog974/attachmentId/" +
                image.getId() + "/";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);

        ForeignPK resourceSrcTestPK = new ForeignPK(messageId, componentId);
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "fr"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "de"), hasSize(0));

        String destComponentId = "kmelia26";
        String destMessageId = "38";
        ForeignPK resourceDestTestPK = new ForeignPK(destMessageId, destComponentId);
        assertThat(listWysiwygs(resourceDestTestPK), hasSize(0));

        // One image
        for (String lang : I18NHelper.getAllSupportedLanguages()) {
          assertThat(listImagesWithNoLanguageFallback(resourceSrcTestPK, lang),
              hasSize(I18NHelper.defaultLanguage.equals(lang) ? 1 : 0));
          assertThat(listImagesWithNoLanguageFallback(resourceDestTestPK, lang), hasSize(0));
        }

        assertThat(WysiwygController.load(componentId, messageId, "fr"),
            is("<mark>FR_Content_FileServer_ComponentId=blog974_" +
                "/componentId/blog974/attachmentId/" + image.getId() + "/")
        );
        assertThat(WysiwygController.load(componentId, messageId, "en"),
            is("<mark>EN_Content_FileServer_ComponentId=blog974_" +
                "/componentId/blog974/attachmentId/" + image.getId() + "/")
        );
        assertThat(WysiwygController.load(destComponentId, destMessageId, "fr"), is(""));
        assertThat(WysiwygController.load(destComponentId, destMessageId, "en"), is(""));

        WysiwygController.copy(componentId, messageId, destComponentId, destMessageId, "26");

        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "fr"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "de"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "fr"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "de"), hasSize(0));

        // One image
        assertThat(listImages(resourceSrcTestPK), hasSize(1));
        SimpleDocumentList<SimpleDocument> copiedImages = listImages(resourceDestTestPK);
        assertThat(copiedImages, hasSize(1));
        for (String lang : I18NHelper.getAllSupportedLanguages()) {
          assertThat(listImagesWithNoLanguageFallback(resourceSrcTestPK, lang),
              hasSize(I18NHelper.defaultLanguage.equals(lang) ? 1 : 0));
          assertThat(listImagesWithNoLanguageFallback(resourceDestTestPK, lang),
              hasSize(I18NHelper.defaultLanguage.equals(lang) ? 1 : 0));
        }

        assertThat(WysiwygController.load(componentId, messageId, "fr"),
            is("<mark>FR_Content_FileServer_ComponentId=blog974_" +
                "/componentId/blog974/attachmentId/" + image.getId() + "/")
        );
        assertThat(WysiwygController.load(componentId, messageId, "en"),
            is("<mark>EN_Content_FileServer_ComponentId=blog974_" +
                "/componentId/blog974/attachmentId/" + image.getId() + "/")
        );
        assertThat(WysiwygController.load(destComponentId, destMessageId, "fr"),
            is("<mark>FR_Content_FileServer_ComponentId=kmelia26_" +
                "/componentId/kmelia26/attachmentId/" + copiedImages.get(0).getId() + "/")
        );
        assertThat(WysiwygController.load(destComponentId, destMessageId, "en"),
            is("<mark>EN_Content_FileServer_ComponentId=kmelia26_" +
                "/componentId/kmelia26/attachmentId/" + copiedImages.get(0).getId() + "/")
        );
      }
    }.execute();
  }

  /**
   * Test of copy method, of class WysiwygController.
   */
  @Test
  public void testCopyWysiwygFRThatIsEmptyENWithImageInJcr() throws Exception {
    new JcrTest() {
      @Override
      public void run() throws Exception {
        String componentId = "blog974";
        String messageId = "18";
        SimpleDocument image = createImageContent(componentId, messageId);
        String expectedContent =
            "EN_Content_FileServer_ComponentId=blog974_/componentId/blog974/attachmentId/" +
                image.getId() + "/";
        String userId = "7";
        String language = "en";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        language = "fr";
        expectedContent = "empty";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        // Empty FR wysiwyg ...
        ForeignPK resourceSrcTestPK = new ForeignPK(messageId, componentId);
        File frWysiwygFile = new File(
            listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "fr").get(0).getAttachmentPath());
        assertThat(frWysiwygFile.length(), greaterThan(0L));
        FileUtils.write(frWysiwygFile, "");
        assertThat(frWysiwygFile.length(), is(0L));

        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "fr"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "de"), hasSize(0));

        String destComponentId = "kmelia26";
        String destMessageId = "38";
        ForeignPK resourceDestTestPK = new ForeignPK(destMessageId, destComponentId);
        SimpleDocumentList<SimpleDocument> wysiwygs = listWysiwygs(resourceDestTestPK);
        assertThat(wysiwygs, hasSize(0));

        // One image
        for (String lang : I18NHelper.getAllSupportedLanguages()) {
          assertThat(listImagesWithNoLanguageFallback(resourceSrcTestPK, lang),
              hasSize(I18NHelper.defaultLanguage.equals(lang) ? 1 : 0));
          assertThat(listImagesWithNoLanguageFallback(resourceDestTestPK, lang), hasSize(0));
        }

        assertThat(WysiwygController.load(componentId, messageId, "fr"),
            is("EN_Content_FileServer_ComponentId=blog974_" +
                "/componentId/blog974/attachmentId/" + image.getId() + "/")
        );
        assertThat(WysiwygController.load(componentId, messageId, "en"),
            is("EN_Content_FileServer_ComponentId=blog974_" +
                "/componentId/blog974/attachmentId/" + image.getId() + "/")
        );
        assertThat(WysiwygController.load(destComponentId, destMessageId, "fr"), is(""));
        assertThat(WysiwygController.load(destComponentId, destMessageId, "en"), is(""));

        WysiwygController.copy(componentId, messageId, destComponentId, destMessageId, "26");

        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "fr"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "de"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "de"), hasSize(0));

        // One image
        assertThat(listImages(resourceSrcTestPK), hasSize(1));
        SimpleDocumentList<SimpleDocument> copiedImages = listImages(resourceDestTestPK);
        assertThat(copiedImages, hasSize(1));
        for (String lang : I18NHelper.getAllSupportedLanguages()) {
          assertThat(listImagesWithNoLanguageFallback(resourceSrcTestPK, lang),
              hasSize(I18NHelper.defaultLanguage.equals(lang) ? 1 : 0));
          assertThat(listImagesWithNoLanguageFallback(resourceDestTestPK, lang),
              hasSize(I18NHelper.defaultLanguage.equals(lang) ? 1 : 0));
        }

        assertThat(WysiwygController.load(componentId, messageId, "fr"),
            is("EN_Content_FileServer_ComponentId=blog974_" +
                "/componentId/blog974/attachmentId/" + image.getId() + "/")
        );
        assertThat(WysiwygController.load(componentId, messageId, "en"),
            is("EN_Content_FileServer_ComponentId=blog974_" +
                "/componentId/blog974/attachmentId/" + image.getId() + "/")
        );
        assertThat(WysiwygController.load(destComponentId, destMessageId, "fr"),
            is("EN_Content_FileServer_ComponentId=kmelia26_" +
                "/componentId/kmelia26/attachmentId/" + copiedImages.get(0).getId() + "/")
        );
        assertThat(WysiwygController.load(destComponentId, destMessageId, "en"),
            is("EN_Content_FileServer_ComponentId=kmelia26_" +
                "/componentId/kmelia26/attachmentId/" + copiedImages.get(0).getId() + "/")
        );
      }
    }.execute();
  }

  /**
   * Test of copy method, of class WysiwygController.
   */
  @Test
  public void testCopyWysiwygFRThatIsEmptyENThatIsEmptyWithImageInJcr() throws Exception {
    new JcrTest() {
      @Override
      public void run() throws Exception {
        String componentId = "blog974";
        String messageId = "18";
        SimpleDocument image = createImageContent(componentId, messageId);
        String expectedContent =
            "<mark>EN_Content_FileServer_ComponentId=blog974_/componentId/blog974/attachmentId/" +
                image.getId() + "/";
        String userId = "7";
        String language = "en";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        language = "fr";
        expectedContent =
            "<mark>FR_Content_FileServer_ComponentId=blog974_/componentId/blog974/attachmentId/" +
                image.getId() + "/";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        // Empty FR wysiwyg ...
        ForeignPK resourceSrcTestPK = new ForeignPK(messageId, componentId);
        File frWysiwygFile = new File(
            listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "fr").get(0).getAttachmentPath());
        assertThat(frWysiwygFile.length(), greaterThan(0L));
        FileUtils.write(frWysiwygFile, "");
        assertThat(frWysiwygFile.length(), is(0L));
        // Empty EN wysiwyg ...
        File enWysiwygFile = new File(
            listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "en").get(0).getAttachmentPath());
        assertThat(enWysiwygFile.length(), greaterThan(0L));
        FileUtils.write(enWysiwygFile, "");
        assertThat(enWysiwygFile.length(), is(0L));

        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "fr"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "de"), hasSize(0));

        String destComponentId = "kmelia26";
        String destMessageId = "38";
        ForeignPK resourceDestTestPK = new ForeignPK(destMessageId, destComponentId);
        SimpleDocumentList<SimpleDocument> wysiwygs = listWysiwygs(resourceDestTestPK);
        assertThat(wysiwygs, hasSize(0));

        // One image
        for (String lang : I18NHelper.getAllSupportedLanguages()) {
          assertThat(listImagesWithNoLanguageFallback(resourceSrcTestPK, lang),
              hasSize(I18NHelper.defaultLanguage.equals(lang) ? 1 : 0));
          assertThat(listImagesWithNoLanguageFallback(resourceDestTestPK, lang), hasSize(0));
        }

        assertThat(WysiwygController.load(componentId, messageId, "fr"), is(""));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is(""));
        assertThat(WysiwygController.load(destComponentId, destMessageId, "fr"), is(""));
        assertThat(WysiwygController.load(destComponentId, destMessageId, "en"), is(""));

        WysiwygController.copy(componentId, messageId, destComponentId, destMessageId, "26");

        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "fr"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "de"), hasSize(0));
        // Finally no copy done
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "en"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "de"), hasSize(0));

        // No image copied
        for (String lang : I18NHelper.getAllSupportedLanguages()) {
          assertThat(listImagesWithNoLanguageFallback(resourceSrcTestPK, lang),
              hasSize(I18NHelper.defaultLanguage.equals(lang) ? 1 : 0));
          assertThat(listImagesWithNoLanguageFallback(resourceDestTestPK, lang), hasSize(0));
        }

        assertThat(WysiwygController.load(componentId, messageId, "fr"), is(""));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is(""));
        assertThat(WysiwygController.load(destComponentId, destMessageId, "fr"), is(""));
        assertThat(WysiwygController.load(destComponentId, destMessageId, "en"), is(""));
      }
    }.execute();
  }

  /**
   * Test of move method, of class WysiwygController.
   */
  @Test
  public void testMoveWysiwygThatExistsOnlyInEN() throws Exception {
    new JcrTest() {
      @Override
      public void run() {
        String componentId = "blog974";
        String messageId = "18";
        String expectedContent = "<mark>EN_Content_FileServer_ComponentId=blog974";
        String userId = "7";
        String language = "en";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);

        ForeignPK resourceSrcTestPK = new ForeignPK(messageId, componentId);
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "de"), hasSize(0));

        String destComponentId = "kmelia26";
        String destMessageId = "38";
        ForeignPK resourceDestTestPK = new ForeignPK(destMessageId, destComponentId);
        assertThat(listWysiwygs(resourceDestTestPK), hasSize(0));

        assertThat(WysiwygController.load(componentId, messageId, "fr"),
            is("<mark>EN_Content_FileServer_ComponentId=blog974"));
        assertThat(WysiwygController.load(componentId, messageId, "en"),
            is("<mark>EN_Content_FileServer_ComponentId=blog974"));
        assertThat(WysiwygController.load(destComponentId, destMessageId, "fr"), is(""));
        assertThat(WysiwygController.load(destComponentId, destMessageId, "en"), is(""));

        WysiwygController.move(componentId, messageId, destComponentId, destMessageId);

        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "en"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "de"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "de"), hasSize(0));

        assertThat(WysiwygController.load(componentId, messageId, "fr"), is(""));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is(""));
        assertThat(WysiwygController.load(destComponentId, destMessageId, "fr"),
            is("<mark>EN_Content_FileServer_ComponentId=kmelia26"));
        assertThat(WysiwygController.load(destComponentId, destMessageId, "en"),
            is("<mark>EN_Content_FileServer_ComponentId=kmelia26"));
      }
    }.execute();
  }

  /**
   * Test of move method, of class WysiwygController.
   */
  @Test
  public void testMoveWysiwygFRENNoImageInJcr() throws Exception {
    new JcrTest() {
      @Override
      public void run() throws Exception {
        String componentId = "blog974";
        String messageId = "18";
        String expectedContent =
            "<mark>EN_Content_FileServer_ComponentId=blog974_/componentId/blog974/attachmentId/18/";
        String userId = "7";
        String language = "en";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        language = "fr";
        expectedContent =
            "<mark>FR_Content_FileServer_ComponentId=blog974_/componentId/blog974/attachmentId/18/";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);


        ForeignPK resourceSrcTestPK = new ForeignPK(messageId, componentId);
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "fr"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "de"), hasSize(0));
        String destComponentId = "kmelia26";
        String destMessageId = "38";
        ForeignPK resourceDestTestPK = new ForeignPK(destMessageId, destComponentId);
        assertThat(listWysiwygs(resourceDestTestPK), hasSize(0));

        // No image
        assertThat(listImages(resourceSrcTestPK), hasSize(0));

        assertThat(WysiwygController.load(componentId, messageId, "fr"),
            is("<mark>FR_Content_FileServer_ComponentId=blog974_" +
                "/componentId/blog974/attachmentId/18/")
        );
        assertThat(WysiwygController.load(componentId, messageId, "en"),
            is("<mark>EN_Content_FileServer_ComponentId=blog974_" +
                "/componentId/blog974/attachmentId/18/")
        );
        assertThat(WysiwygController.load(destComponentId, destMessageId, "fr"), is(""));
        assertThat(WysiwygController.load(destComponentId, destMessageId, "en"), is(""));

        WysiwygController.move(componentId, messageId, destComponentId, destMessageId);

        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "en"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "de"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "fr"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "de"), hasSize(0));

        // No image
        assertThat(listImages(resourceSrcTestPK), hasSize(0));
        assertThat(listImages(resourceDestTestPK), hasSize(0));

        assertThat(WysiwygController.load(componentId, messageId, "fr"), is(""));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is(""));
        assertThat(WysiwygController.load(destComponentId, destMessageId, "fr"),
            is("<mark>FR_Content_FileServer_ComponentId=kmelia26_" +
                "/componentId/blog974/attachmentId/18/")
        );
        assertThat(WysiwygController.load(destComponentId, destMessageId, "en"),
            is("<mark>EN_Content_FileServer_ComponentId=kmelia26_" +
                "/componentId/blog974/attachmentId/18/")
        );
      }
    }.execute();
  }

  /**
   * Test of move method, of class WysiwygController.
   */
  @Test
  public void testMoveWysiwygFRENWithImageInJcr() throws Exception {
    new JcrTest() {
      @Override
      public void run() throws Exception {
        String componentId = "blog974";
        String messageId = "18";
        SimpleDocument image = createImageContent(componentId, messageId);
        String expectedContent =
            "<mark>EN_Content_FileServer_ComponentId=blog974_/componentId/blog974/attachmentId/" +
                image.getId() + "/";
        String userId = "7";
        String language = "en";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        language = "fr";
        expectedContent =
            "<mark>FR_Content_FileServer_ComponentId=blog974_/componentId/blog974/attachmentId/" +
                image.getId() + "/";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);

        ForeignPK resourceSrcTestPK = new ForeignPK(messageId, componentId);
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "fr"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "de"), hasSize(0));

        String destComponentId = "kmelia26";
        String destMessageId = "38";
        ForeignPK resourceDestTestPK = new ForeignPK(destMessageId, destComponentId);
        assertThat(listWysiwygs(resourceDestTestPK), hasSize(0));

        // One image
        for (String lang : I18NHelper.getAllSupportedLanguages()) {
          assertThat(listImagesWithNoLanguageFallback(resourceSrcTestPK, lang),
              hasSize(I18NHelper.defaultLanguage.equals(lang) ? 1 : 0));
          assertThat(listImagesWithNoLanguageFallback(resourceDestTestPK, lang), hasSize(0));
        }

        assertThat(WysiwygController.load(componentId, messageId, "fr"),
            is("<mark>FR_Content_FileServer_ComponentId=blog974_" +
                "/componentId/blog974/attachmentId/" + image.getId() + "/")
        );
        assertThat(WysiwygController.load(componentId, messageId, "en"),
            is("<mark>EN_Content_FileServer_ComponentId=blog974_" +
                "/componentId/blog974/attachmentId/" + image.getId() + "/")
        );
        assertThat(WysiwygController.load(destComponentId, destMessageId, "fr"), is(""));
        assertThat(WysiwygController.load(destComponentId, destMessageId, "en"), is(""));

        WysiwygController.move(componentId, messageId, destComponentId, destMessageId);

        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "en"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "de"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "fr"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "de"), hasSize(0));

        // One image
        for (String lang : I18NHelper.getAllSupportedLanguages()) {
          assertThat(listImagesWithNoLanguageFallback(resourceSrcTestPK, lang), hasSize(0));
          assertThat(listImagesWithNoLanguageFallback(resourceDestTestPK, lang),
              hasSize(I18NHelper.defaultLanguage.equals(lang) ? 1 : 0));
        }

        assertThat(WysiwygController.load(componentId, messageId, "fr"), is(""));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is(""));
        assertThat(WysiwygController.load(destComponentId, destMessageId, "fr"),
            is("<mark>FR_Content_FileServer_ComponentId=kmelia26_" +
                "/componentId/kmelia26/attachmentId/" + image.getId() + "/")
        );
        assertThat(WysiwygController.load(destComponentId, destMessageId, "en"),
            is("<mark>EN_Content_FileServer_ComponentId=kmelia26_" +
                "/componentId/kmelia26/attachmentId/" + image.getId() + "/")
        );
      }
    }.execute();
  }

  /**
   * Test of move method, of class WysiwygController.
   */
  @Test
  public void testMoveWysiwygFRThatIsEmptyENWithImageInJcr() throws Exception {
    new JcrTest() {
      @Override
      public void run() throws Exception {
        String componentId = "blog974";
        String messageId = "18";
        SimpleDocument image = createImageContent(componentId, messageId);
        String expectedContent =
            "EN_Content_FileServer_ComponentId=blog974_/componentId/blog974/attachmentId/" +
                image.getId() + "/";
        String userId = "7";
        String language = "en";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        language = "fr";
        expectedContent =
            "<mark>FR_Content_FileServer_ComponentId=blog974_/componentId/blog974/attachmentId/" +
                image.getId() + "/";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        // Empty FR wysiwyg ...
        ForeignPK resourceSrcTestPK = new ForeignPK(messageId, componentId);
        File frWysiwygFile = new File(
            listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "fr").get(0).getAttachmentPath());
        assertThat(frWysiwygFile.length(), greaterThan(0L));
        FileUtils.write(frWysiwygFile, "");
        assertThat(frWysiwygFile.length(), is(0L));

        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "fr"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "de"), hasSize(0));

        String destComponentId = "kmelia26";
        String destMessageId = "38";
        ForeignPK resourceDestTestPK = new ForeignPK(destMessageId, destComponentId);
        SimpleDocumentList<SimpleDocument> wysiwygs = listWysiwygs(resourceDestTestPK);
        assertThat(wysiwygs, hasSize(0));

        // One image
        for (String lang : I18NHelper.getAllSupportedLanguages()) {
          assertThat(listImagesWithNoLanguageFallback(resourceSrcTestPK, lang),
              hasSize(I18NHelper.defaultLanguage.equals(lang) ? 1 : 0));
          assertThat(listImagesWithNoLanguageFallback(resourceDestTestPK, lang), hasSize(0));
        }

        assertThat(WysiwygController.load(componentId, messageId, "fr"),
            is("EN_Content_FileServer_ComponentId=blog974_" +
                "/componentId/blog974/attachmentId/" + image.getId() + "/")
        );
        assertThat(WysiwygController.load(componentId, messageId, "en"),
            is("EN_Content_FileServer_ComponentId=blog974_" +
                "/componentId/blog974/attachmentId/" + image.getId() + "/")
        );
        assertThat(WysiwygController.load(destComponentId, destMessageId, "fr"), is(""));
        assertThat(WysiwygController.load(destComponentId, destMessageId, "en"), is(""));

        WysiwygController.move(componentId, messageId, destComponentId, destMessageId);

        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "en"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "de"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "fr"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "de"), hasSize(0));

        // One image
        for (String lang : I18NHelper.getAllSupportedLanguages()) {
          assertThat(listImagesWithNoLanguageFallback(resourceSrcTestPK, lang), hasSize(0));
          assertThat(listImagesWithNoLanguageFallback(resourceDestTestPK, lang),
              hasSize(I18NHelper.defaultLanguage.equals(lang) ? 1 : 0));
        }

        assertThat(WysiwygController.load(componentId, messageId, "fr"), is(""));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is(""));
        assertThat(WysiwygController.load(destComponentId, destMessageId, "fr"),
            is("EN_Content_FileServer_ComponentId=kmelia26_" +
                "/componentId/kmelia26/attachmentId/" + image.getId() + "/")
        );
        assertThat(WysiwygController.load(destComponentId, destMessageId, "en"),
            is("EN_Content_FileServer_ComponentId=kmelia26_" +
                "/componentId/kmelia26/attachmentId/" + image.getId() + "/")
        );
      }
    }.execute();
  }

  /**
   * Test of move method, of class WysiwygController.
   */
  @Test
  public void testMoveWysiwygFRThatIsEmptyENThatIsEmptyWithImageInJcr() throws Exception {
    new JcrTest() {
      @Override
      public void run() throws Exception {
        String componentId = "blog974";
        String messageId = "18";
        SimpleDocument image = createImageContent(componentId, messageId);
        String expectedContent =
            "<mark>EN_Content_FileServer_ComponentId=blog974_/componentId/blog974/attachmentId/" +
                image.getId() + "/";
        String userId = "7";
        String language = "en";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        language = "fr";
        expectedContent =
            "<mark>FR_Content_FileServer_ComponentId=blog974_/componentId/blog974/attachmentId/" +
                image.getId() + "/";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        // Empty FR wysiwyg ...
        ForeignPK resourceSrcTestPK = new ForeignPK(messageId, componentId);
        File frWysiwygFile = new File(
            listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "fr").get(0).getAttachmentPath());
        assertThat(frWysiwygFile.length(), greaterThan(0L));
        FileUtils.write(frWysiwygFile, "");
        assertThat(frWysiwygFile.length(), is(0L));
        // Empty EN wysiwyg ...
        File enWysiwygFile = new File(
            listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "en").get(0).getAttachmentPath());
        assertThat(enWysiwygFile.length(), greaterThan(0L));
        FileUtils.write(enWysiwygFile, "");
        assertThat(enWysiwygFile.length(), is(0L));

        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "fr"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "de"), hasSize(0));

        String destComponentId = "kmelia26";
        String destMessageId = "38";
        ForeignPK resourceDestTestPK = new ForeignPK(destMessageId, destComponentId);
        SimpleDocumentList<SimpleDocument> wysiwygs = listWysiwygs(resourceDestTestPK);
        assertThat(wysiwygs, hasSize(0));

        // One image
        for (String lang : I18NHelper.getAllSupportedLanguages()) {
          assertThat(listImagesWithNoLanguageFallback(resourceSrcTestPK, lang),
              hasSize(I18NHelper.defaultLanguage.equals(lang) ? 1 : 0));
          assertThat(listImagesWithNoLanguageFallback(resourceDestTestPK, lang), hasSize(0));
        }

        assertThat(WysiwygController.load(componentId, messageId, "fr"), is(""));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is(""));
        assertThat(WysiwygController.load(destComponentId, destMessageId, "fr"), is(""));
        assertThat(WysiwygController.load(destComponentId, destMessageId, "en"), is(""));

        WysiwygController.move(componentId, messageId, destComponentId, destMessageId);

        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "en"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceSrcTestPK, "de"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "fr"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceDestTestPK, "de"), hasSize(0));

        // Images moved
        for (String lang : I18NHelper.getAllSupportedLanguages()) {
          assertThat(listImagesWithNoLanguageFallback(resourceSrcTestPK, lang), hasSize(0));
          assertThat(listImagesWithNoLanguageFallback(resourceDestTestPK, lang),
              hasSize(I18NHelper.defaultLanguage.equals(lang) ? 1 : 0));
        }

        assertThat(WysiwygController.load(componentId, messageId, "fr"), is(""));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is(""));
        assertThat(WysiwygController.load(destComponentId, destMessageId, "fr"), is(""));
        assertThat(WysiwygController.load(destComponentId, destMessageId, "en"), is(""));
      }
    }.execute();
  }

  /**
   * Test of load method, of class WysiwygController.
   */
  @Test
  public void testLoadWysiwygThatExistsOnlyInEN() throws Exception {
    new JcrTest() {
      @Override
      public void run() {
        String componentId = "blog974";
        String messageId = "18";
        String expectedContent = "<mark>EN_Content";
        String userId = "7";
        String language = "en";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        // Jcr State
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        assertThat(WysiwygController.load(componentId, messageId, "fr"), is("<mark>EN_Content"));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is("<mark>EN_Content"));
      }
    }.execute();
  }

  /**
   * Test of load method, of class WysiwygController.
   */
  @Test
  public void testLoadWysiwygThatDoesNotExist() throws Exception {
    new JcrTest() {
      @Override
      public void run() {
        String componentId = "blog974";
        String messageId = "18";
        // Jcr State
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        assertThat(WysiwygController.load(componentId, messageId, "fr"), is(""));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is(""));
      }
    }.execute();
  }

  /**
   * Test of load method, of class WysiwygController.
   */
  @Test
  public void testLoadLegacyFRWysiwyg() throws Exception {
    new JcrTest() {
      @Override
      public void run() throws Exception {
        String componentId = "blog974";
        String messageId = "18";
        createLegacyWysiwygContent(componentId, messageId, "fr", "<mark>LegacyContent");
        // Jcr State
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        assertThat(WysiwygController.load(componentId, messageId, "fr"), is("<mark>LegacyContent"));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is("<mark>LegacyContent"));
      }
    }.execute();
  }


  /**
   * Test of load method, of class WysiwygController.
   */
  @Test
  public void testLoadLegacyEmptyFRWysiwyg() throws Exception {
    new JcrTest() {
      @Override
      public void run() throws Exception {
        String componentId = "blog974";
        String messageId = "18";
        createLegacyWysiwygContent(componentId, messageId, "fr", "");
        // Jcr State
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        assertThat(WysiwygController.load(componentId, messageId, "fr"), is(""));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is(""));
      }
    }.execute();
  }

  /**
   * Test of load method, of class WysiwygController.
   */
  @Test
  public void testLoadLegacyEmptyFRWysiwygAndJcrENwysiwyg() throws Exception {
    new JcrTest() {
      @Override
      public void run() throws Exception {
        String componentId = "blog974";
        String messageId = "18";
        WysiwygController.save("ENContent", componentId, messageId, "26", "en", false);
        createLegacyWysiwygContent(componentId, messageId, "fr", "");
        // Jcr State
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        /*
        This test shows that if a not empty document exists into the JCR,
        then legacy content is never loaded.
         */
        assertThat(WysiwygController.load(componentId, messageId, "fr"), is("ENContent"));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is("ENContent"));
      }
    }.execute();
  }

  /**
   * Test of load method, of class WysiwygController.
   */
  @Test
  public void testLoadLegacyFRWysiwygAndJcrEmptyENwysiwyg() throws Exception {
    new JcrTest() {
      @Override
      public void run() throws Exception {
        String componentId = "blog974";
        String messageId = "18";
        WysiwygController.save("<mark>ENContent", componentId, messageId, "26", "en", false);
        createLegacyWysiwygContent(componentId, messageId, "fr", "LegacyContent");
        // Empty EN wysiwyg ...
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        File enWysiwygFile = new File(
            listWysiwygsWithNoLanguageFallback(resourceTestPK, "en").get(0).getAttachmentPath());
        assertThat(enWysiwygFile.length(), greaterThan(0L));
        FileUtils.write(enWysiwygFile, "");
        assertThat(enWysiwygFile.length(), is(0L));
        // Jcr State
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        assertThat(WysiwygController.load(componentId, messageId, "fr"), is("LegacyContent"));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is("LegacyContent"));
      }
    }.execute();
  }

  /**
   * Test of load method, of class WysiwygController.
   */
  @Test
  public void testLoadLegacyEmptyFRWysiwygAndJcrEmptyENwysiwyg() throws Exception {
    new JcrTest() {
      @Override
      public void run() throws Exception {
        String componentId = "blog974";
        String messageId = "18";
        WysiwygController.save("<mark>ENContent", componentId, messageId, "26", "en", false);
        createLegacyWysiwygContent(componentId, messageId, "fr", "");
        // Empty EN wysiwyg ...
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        File enWysiwygFile = new File(
            listWysiwygsWithNoLanguageFallback(resourceTestPK, "en").get(0).getAttachmentPath());
        assertThat(enWysiwygFile.length(), greaterThan(0L));
        FileUtils.write(enWysiwygFile, "");
        assertThat(enWysiwygFile.length(), is(0L));
        // Jcr State
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        assertThat(WysiwygController.load(componentId, messageId, "fr"), is(""));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is(""));
      }
    }.execute();
  }

  /**
   * Test of load method, of class WysiwygController.
   */
  @Test
  public void testLoadWysiwyg() throws Exception {
    new JcrTest() {
      @Override
      public void run() {
        String componentId = "blog974";
        String messageId = "18";
        String expectedContent = "EN_Content";
        String userId = "7";
        String language = "en";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        expectedContent = "<mark>FR_Content";
        language = "fr";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        // Jcr State
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        assertThat(WysiwygController.load(componentId, messageId, "fr"), is("<mark>FR_Content"));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is("EN_Content"));
      }
    }.execute();
  }

  /**
   * Test of load method, of class WysiwygController.
   */
  @Test
  public void testLoadEmptyWysiwygFRAndEmptyENWysiwyg() throws Exception {
    new JcrTest() {
      @Override
      public void run() throws IOException {
        String componentId = "blog974";
        String messageId = "18";
        String expectedContent = "EN_Content";
        String userId = "7";
        String language = "en";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        expectedContent = "<mark>FR_Content";
        language = "fr";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        // Empty FR wysiwyg ...
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        File frWysiwygFile = new File(
            listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr").get(0).getAttachmentPath());
        assertThat(frWysiwygFile.length(), greaterThan(0L));
        FileUtils.write(frWysiwygFile, "");
        assertThat(frWysiwygFile.length(), is(0L));
        // Empty EN wysiwyg ...
        File enWysiwygFile = new File(
            listWysiwygsWithNoLanguageFallback(resourceTestPK, "en").get(0).getAttachmentPath());
        assertThat(enWysiwygFile.length(), greaterThan(0L));
        FileUtils.write(enWysiwygFile, "");
        assertThat(enWysiwygFile.length(), is(0L));
        // Jcr State
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        assertThat(WysiwygController.load(componentId, messageId, "fr"), is(""));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is(""));
      }
    }.execute();
  }

  /**
   * Test of haveGotWysiwygToDisplay method, of class WysiwygController.
   */
  @Test
  public void testHavegotwysiwygtodisplayWysiwygThatExistsOnlyInEN() throws Exception {
    new JcrTest() {
      @Override
      public void run() {
        String componentId = "blog974";
        String messageId = "18";
        String expectedContent = "<mark>EN_Content";
        String userId = "7";
        String language = "en";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        // Jcr State
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        assertThat(WysiwygController.haveGotWysiwygToDisplay(componentId, messageId, "fr"), is(true));
        assertThat(WysiwygController.haveGotWysiwygToDisplay(componentId, messageId, "en"), is(true));
      }
    }.execute();
  }

  /**
   * Test of haveGotWysiwygToDisplay method, of class WysiwygController.
   */
  @Test
  public void testHavegotwysiwygtodisplayWysiwygThatDoesNotExist() throws Exception {
    new JcrTest() {
      @Override
      public void run() {
        String componentId = "blog974";
        String messageId = "18";
        // Jcr State
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        assertThat(WysiwygController.haveGotWysiwygToDisplay(componentId, messageId, "fr"), is(false));
        assertThat(WysiwygController.haveGotWysiwygToDisplay(componentId, messageId, "en"), is(false));
      }
    }.execute();
  }

  /**
   * Test of haveGotWysiwygToDisplay method, of class WysiwygController.
   */
  @Test
  public void testHavegotwysiwygtodisplayLegacyFRWysiwyg() throws Exception {
    new JcrTest() {
      @Override
      public void run() throws Exception {
        String componentId = "blog974";
        String messageId = "18";
        createLegacyWysiwygContent(componentId, messageId, "fr", "<mark>LegacyContent");
        // Jcr State
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        assertThat(WysiwygController.haveGotWysiwygToDisplay(componentId, messageId, "fr"), is(true));
        assertThat(WysiwygController.haveGotWysiwygToDisplay(componentId, messageId, "en"), is(true));
      }
    }.execute();
  }


  /**
   * Test of haveGotWysiwygToDisplay method, of class WysiwygController.
   */
  @Test
  public void testHavegotwysiwygtodisplayLegacyEmptyFRWysiwyg() throws Exception {
    new JcrTest() {
      @Override
      public void run() throws Exception {
        String componentId = "blog974";
        String messageId = "18";
        createLegacyWysiwygContent(componentId, messageId, "fr", "");
        // Jcr State
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        assertThat(WysiwygController.haveGotWysiwygToDisplay(componentId, messageId, "fr"), is(false));
        assertThat(WysiwygController.haveGotWysiwygToDisplay(componentId, messageId, "en"), is(false));
      }
    }.execute();
  }

  /**
   * Test of haveGotWysiwygToDisplay method, of class WysiwygController.
   */
  @Test
  public void testHavegotwysiwygtodisplayLegacyEmptyFRWysiwygAndJcrENwysiwyg() throws Exception {
    new JcrTest() {
      @Override
      public void run() throws Exception {
        String componentId = "blog974";
        String messageId = "18";
        WysiwygController.save("ENContent", componentId, messageId, "26", "en", false);
        createLegacyWysiwygContent(componentId, messageId, "fr", "");
        // Jcr State
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        /*
        This test shows that if a not empty document exists into the JCR,
        then legacy content is never haveGotWysiwygToDisplayed.
         */
        assertThat(WysiwygController.haveGotWysiwygToDisplay(componentId, messageId, "fr"), is(true));
        assertThat(WysiwygController.haveGotWysiwygToDisplay(componentId, messageId, "en"), is(true));
      }
    }.execute();
  }

  /**
   * Test of haveGotWysiwygToDisplay method, of class WysiwygController.
   */
  @Test
  public void testHavegotwysiwygtodisplayLegacyFRWysiwygAndJcrEmptyENwysiwyg() throws Exception {
    new JcrTest() {
      @Override
      public void run() throws Exception {
        String componentId = "blog974";
        String messageId = "18";
        WysiwygController.save("<mark>ENContent", componentId, messageId, "26", "en", false);
        createLegacyWysiwygContent(componentId, messageId, "fr", "LegacyContent");
        // Empty EN wysiwyg ...
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        File enWysiwygFile = new File(
            listWysiwygsWithNoLanguageFallback(resourceTestPK, "en").get(0).getAttachmentPath());
        assertThat(enWysiwygFile.length(), greaterThan(0L));
        FileUtils.write(enWysiwygFile, "");
        assertThat(enWysiwygFile.length(), is(0L));
        // Jcr State
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        assertThat(WysiwygController.haveGotWysiwygToDisplay(componentId, messageId, "fr"), is(true));
        assertThat(WysiwygController.haveGotWysiwygToDisplay(componentId, messageId, "en"), is(true));
      }
    }.execute();
  }

  /**
   * Test of haveGotWysiwygToDisplay method, of class WysiwygController.
   */
  @Test
  public void testHavegotwysiwygtodisplayLegacyEmptyFRWysiwygAndJcrEmptyENwysiwyg() throws Exception {
    new JcrTest() {
      @Override
      public void run() throws Exception {
        String componentId = "blog974";
        String messageId = "18";
        WysiwygController.save("<mark>ENContent", componentId, messageId, "26", "en", false);
        createLegacyWysiwygContent(componentId, messageId, "fr", "");
        // Empty EN wysiwyg ...
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        File enWysiwygFile = new File(
            listWysiwygsWithNoLanguageFallback(resourceTestPK, "en").get(0).getAttachmentPath());
        assertThat(enWysiwygFile.length(), greaterThan(0L));
        FileUtils.write(enWysiwygFile, "");
        assertThat(enWysiwygFile.length(), is(0L));
        // Jcr State
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        assertThat(WysiwygController.haveGotWysiwygToDisplay(componentId, messageId, "fr"), is(false));
        assertThat(WysiwygController.haveGotWysiwygToDisplay(componentId, messageId, "en"), is(false));
      }
    }.execute();
  }

  /**
   * Test of haveGotWysiwygToDisplay method, of class WysiwygController.
   */
  @Test
  public void testHavegotwysiwygtodisplayWysiwyg() throws Exception {
    new JcrTest() {
      @Override
      public void run() {
        String componentId = "blog974";
        String messageId = "18";
        String expectedContent = "EN_Content";
        String userId = "7";
        String language = "en";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        expectedContent = "<mark>FR_Content";
        language = "fr";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        // Jcr State
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        assertThat(WysiwygController.haveGotWysiwygToDisplay(componentId, messageId, "fr"), is(true));
        assertThat(WysiwygController.haveGotWysiwygToDisplay(componentId, messageId, "en"), is(true));
      }
    }.execute();
  }

  /**
   * Test of haveGotWysiwygToDisplay method, of class WysiwygController.
   */
  @Test
  public void testHavegotwysiwygtodisplayEmptyWysiwygFRAndEmptyENWysiwyg() throws Exception {
    new JcrTest() {
      @Override
      public void run() throws IOException {
        String componentId = "blog974";
        String messageId = "18";
        String expectedContent = "EN_Content";
        String userId = "7";
        String language = "en";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        expectedContent = "<mark>FR_Content";
        language = "fr";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        // Empty FR wysiwyg ...
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        File frWysiwygFile = new File(
            listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr").get(0).getAttachmentPath());
        assertThat(frWysiwygFile.length(), greaterThan(0L));
        FileUtils.write(frWysiwygFile, "");
        assertThat(frWysiwygFile.length(), is(0L));
        // Empty EN wysiwyg ...
        File enWysiwygFile = new File(
            listWysiwygsWithNoLanguageFallback(resourceTestPK, "en").get(0).getAttachmentPath());
        assertThat(enWysiwygFile.length(), greaterThan(0L));
        FileUtils.write(enWysiwygFile, "");
        assertThat(enWysiwygFile.length(), is(0L));
        // Jcr State
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        assertThat(WysiwygController.haveGotWysiwygToDisplay(componentId, messageId, "fr"), is(false));
        assertThat(WysiwygController.haveGotWysiwygToDisplay(componentId, messageId, "en"), is(false));
      }
    }.execute();
  }

  /**
   * Test of haveGotWysiwyg method, of class WysiwygController.
   */
  @Test
  public void testHavegotwysiwygWysiwygThatExistsOnlyInEN() throws Exception {
    new JcrTest() {
      @Override
      public void run() {
        String componentId = "blog974";
        String messageId = "18";
        String expectedContent = "<mark>EN_Content";
        String userId = "7";
        String language = "en";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        // Jcr State
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        assertThat(WysiwygController.haveGotWysiwyg(componentId, messageId, "fr"), is(true));
        assertThat(WysiwygController.haveGotWysiwyg(componentId, messageId, "en"), is(true));
      }
    }.execute();
  }

  /**
   * Test of haveGotWysiwyg method, of class WysiwygController.
   */
  @Test
  public void testHavegotwysiwygWysiwygThatDoesNotExist() throws Exception {
    new JcrTest() {
      @Override
      public void run() {
        String componentId = "blog974";
        String messageId = "18";
        // Jcr State
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        assertThat(WysiwygController.haveGotWysiwyg(componentId, messageId, "fr"), is(false));
        assertThat(WysiwygController.haveGotWysiwyg(componentId, messageId, "en"), is(false));
      }
    }.execute();
  }

  /**
   * Test of haveGotWysiwyg method, of class WysiwygController.
   */
  @Test
  public void testHavegotwysiwygLegacyFRWysiwyg() throws Exception {
    new JcrTest() {
      @Override
      public void run() throws Exception {
        String componentId = "blog974";
        String messageId = "18";
        createLegacyWysiwygContent(componentId, messageId, "fr", "<mark>LegacyContent");
        // Jcr State
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        assertThat(WysiwygController.haveGotWysiwyg(componentId, messageId, "fr"), is(true));
        assertThat(WysiwygController.haveGotWysiwyg(componentId, messageId, "en"), is(true));
      }
    }.execute();
  }


  /**
   * Test of haveGotWysiwyg method, of class WysiwygController.
   */
  @Test
  public void testHavegotwysiwygLegacyEmptyFRWysiwyg() throws Exception {
    new JcrTest() {
      @Override
      public void run() throws Exception {
        String componentId = "blog974";
        String messageId = "18";
        createLegacyWysiwygContent(componentId, messageId, "fr", "");
        // Jcr State
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        assertThat(WysiwygController.haveGotWysiwyg(componentId, messageId, "fr"), is(false));
        assertThat(WysiwygController.haveGotWysiwyg(componentId, messageId, "en"), is(false));
      }
    }.execute();
  }

  /**
   * Test of haveGotWysiwyg method, of class WysiwygController.
   */
  @Test
  public void testHavegotwysiwygLegacyEmptyFRWysiwygAndJcrENwysiwyg() throws Exception {
    new JcrTest() {
      @Override
      public void run() throws Exception {
        String componentId = "blog974";
        String messageId = "18";
        WysiwygController.save("ENContent", componentId, messageId, "26", "en", false);
        createLegacyWysiwygContent(componentId, messageId, "fr", "");
        // Jcr State
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        /*
        This test shows that if a not empty document exists into the JCR,
        then legacy content is never haveGotWysiwyged.
         */
        assertThat(WysiwygController.haveGotWysiwyg(componentId, messageId, "fr"), is(true));
        assertThat(WysiwygController.haveGotWysiwyg(componentId, messageId, "en"), is(true));
      }
    }.execute();
  }

  /**
   * Test of haveGotWysiwyg method, of class WysiwygController.
   */
  @Test
  public void testHavegotwysiwygLegacyFRWysiwygAndJcrEmptyENwysiwyg() throws Exception {
    new JcrTest() {
      @Override
      public void run() throws Exception {
        String componentId = "blog974";
        String messageId = "18";
        WysiwygController.save("<mark>ENContent", componentId, messageId, "26", "en", false);
        createLegacyWysiwygContent(componentId, messageId, "fr", "LegacyContent");
        // Empty EN wysiwyg ...
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        File enWysiwygFile = new File(
            listWysiwygsWithNoLanguageFallback(resourceTestPK, "en").get(0).getAttachmentPath());
        assertThat(enWysiwygFile.length(), greaterThan(0L));
        FileUtils.write(enWysiwygFile, "");
        assertThat(enWysiwygFile.length(), is(0L));
        // Jcr State
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        assertThat(WysiwygController.haveGotWysiwyg(componentId, messageId, "fr"), is(true));
        assertThat(WysiwygController.haveGotWysiwyg(componentId, messageId, "en"), is(true));
      }
    }.execute();
  }

  /**
   * Test of haveGotWysiwyg method, of class WysiwygController.
   */
  @Test
  public void testHavegotwysiwygLegacyEmptyFRWysiwygAndJcrEmptyENwysiwyg() throws Exception {
    new JcrTest() {
      @Override
      public void run() throws Exception {
        String componentId = "blog974";
        String messageId = "18";
        WysiwygController.save("<mark>ENContent", componentId, messageId, "26", "en", false);
        createLegacyWysiwygContent(componentId, messageId, "fr", "");
        // Empty EN wysiwyg ...
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        File enWysiwygFile = new File(
            listWysiwygsWithNoLanguageFallback(resourceTestPK, "en").get(0).getAttachmentPath());
        assertThat(enWysiwygFile.length(), greaterThan(0L));
        FileUtils.write(enWysiwygFile, "");
        assertThat(enWysiwygFile.length(), is(0L));
        // Jcr State
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(0));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        assertThat(WysiwygController.haveGotWysiwyg(componentId, messageId, "fr"), is(false));
        assertThat(WysiwygController.haveGotWysiwyg(componentId, messageId, "en"), is(false));
      }
    }.execute();
  }

  /**
   * Test of haveGotWysiwyg method, of class WysiwygController.
   */
  @Test
  public void testHavegotwysiwygWysiwyg() throws Exception {
    new JcrTest() {
      @Override
      public void run() {
        String componentId = "blog974";
        String messageId = "18";
        String expectedContent = "EN_Content";
        String userId = "7";
        String language = "en";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        expectedContent = "<mark>FR_Content";
        language = "fr";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        // Jcr State
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        assertThat(WysiwygController.haveGotWysiwyg(componentId, messageId, "fr"), is(true));
        assertThat(WysiwygController.haveGotWysiwyg(componentId, messageId, "en"), is(true));
      }
    }.execute();
  }

  /**
   * Test of haveGotWysiwyg method, of class WysiwygController.
   */
  @Test
  public void testHavegotwysiwygEmptyWysiwygFRAndEmptyENWysiwyg() throws Exception {
    new JcrTest() {
      @Override
      public void run() throws IOException {
        String componentId = "blog974";
        String messageId = "18";
        String expectedContent = "EN_Content";
        String userId = "7";
        String language = "en";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        expectedContent = "<mark>FR_Content";
        language = "fr";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        // Empty FR wysiwyg ...
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        File frWysiwygFile = new File(
            listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr").get(0).getAttachmentPath());
        assertThat(frWysiwygFile.length(), greaterThan(0L));
        FileUtils.write(frWysiwygFile, "");
        assertThat(frWysiwygFile.length(), is(0L));
        // Empty EN wysiwyg ...
        File enWysiwygFile = new File(
            listWysiwygsWithNoLanguageFallback(resourceTestPK, "en").get(0).getAttachmentPath());
        assertThat(enWysiwygFile.length(), greaterThan(0L));
        FileUtils.write(enWysiwygFile, "");
        assertThat(enWysiwygFile.length(), is(0L));
        // Jcr State
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "fr"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "en"), hasSize(1));
        assertThat(listWysiwygsWithNoLanguageFallback(resourceTestPK, "de"), hasSize(0));
        // Tests
        assertThat(WysiwygController.haveGotWysiwyg(componentId, messageId, "fr"), is(false));
        assertThat(WysiwygController.haveGotWysiwyg(componentId, messageId, "en"), is(false));
      }
    }.execute();
  }

  private void createLegacyWysiwygContent(String componentId, String resourceId, String language,
      String content) throws Exception {
    File legacyWysiwyg = FileUtils
        .getFile(FileRepositoryManager.getAbsolutePath(componentId), "Attachment", "wysiwyg",
            resourceId + "wysiwyg_" + language + ".txt");
    FileUtils.write(legacyWysiwyg, content);
  }

  private SimpleDocument createImageContent(String componentId, String resourceId)
      throws Exception {
    SimpleDocument image =
        new SimpleDocument(new SimpleDocumentPK("-1", componentId), resourceId, 0, false,
            new SimpleAttachment("imageFileName", I18NHelper.defaultLanguage, "imageTitle",
                "imageDescription", 0, MimeTypes.PLAIN_TEXT_MIME_TYPE, "1", new Date(), null)
        );
    image.setDocumentType(DocumentType.image);
    return AttachmentServiceFactory.getAttachmentService()
        .createAttachment(image, new ByteArrayInputStream("ImageContent".getBytes()));
  }

  /**
   * Test of createFileAndAttachment method, of class WysiwygController.
   */
  @Test
  public void testCreateWysiwyg() throws Exception {
    new JcrTest() {
      @Override
      public void run() {
        String componentId = "blog974";
        String messageId = "18";
        String expectedContent = "EN_Content";
        String userId = "7";
        String language = "en";
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        List<SimpleDocument> wysiwygs = listWysiwygs(resourceTestPK);
        assertThat(wysiwygs, hasSize(0));
        assertThat(WysiwygController.load(componentId, messageId, language), isEmptyString());
        WysiwygController
            .createFileAndAttachment(expectedContent, resourceTestPK, userId, language);
        String content = WysiwygController.load(componentId, messageId, language);
        assertThat(content, is(expectedContent));
        List<SimpleDocument> lockedFiles =
            AttachmentServiceFactory.getAttachmentService().listDocumentsLockedByUser(userId, null);
        assertThat(lockedFiles, is(notNullValue()));
        assertThat(lockedFiles, hasSize(0));

        wysiwygs = listWysiwygs(resourceTestPK);
        assertThat(wysiwygs, hasSize(1));

        expectedContent = "FR_Content";
        language = "fr";
        WysiwygController
            .createFileAndAttachment(expectedContent, resourceTestPK, userId, language);

        wysiwygs = listWysiwygs(resourceTestPK);
        assertThat(wysiwygs, hasSize(2));
        assertThat(WysiwygController.load(componentId, messageId, "fr"), is("FR_Content"));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is("EN_Content"));
      }
    }.execute();
  }

  private SimpleDocumentList<SimpleDocument> listWysiwygs(ForeignPK foreignPK) {
    return AttachmentServiceFactory.getAttachmentService()
        .listDocumentsByForeignKeyAndType(foreignPK, DocumentType.wysiwyg, null);
  }

  private SimpleDocumentList<SimpleDocument> listImages(ForeignPK foreignPK) {
    return AttachmentServiceFactory.getAttachmentService()
        .listDocumentsByForeignKeyAndType(foreignPK, DocumentType.image, null);
  }

  private SimpleDocumentList<SimpleDocument> listWysiwygsWithNoLanguageFallback(ForeignPK foreignPK,
      String language) {
    return AttachmentServiceFactory.getAttachmentService()
        .listDocumentsByForeignKeyAndType(foreignPK, DocumentType.wysiwyg, language)
        .removeLanguageFallbacks();
  }

  private SimpleDocumentList<SimpleDocument> listImagesWithNoLanguageFallback(ForeignPK foreignPK,
      String language) {
    return AttachmentServiceFactory.getAttachmentService()
        .listDocumentsByForeignKeyAndType(foreignPK, DocumentType.image, language)
        .removeLanguageFallbacks();
  }

  /**
   * Test of save method, of class WysiwygController.
   */
  @Test
  public void testSaveWysiwyg() throws Exception {
    new JcrTest() {
      @Override
      public void run() {
        String componentId = "blog974";
        String messageId = "18";
        String expectedContent = "EN_Content";
        String userId = "7";
        String language = "en";
        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        List<SimpleDocument> wysiwygs = listWysiwygs(resourceTestPK);
        assertThat(wysiwygs, hasSize(0));
        assertThat(WysiwygController.load(componentId, messageId, language), isEmptyString());
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);
        String content = WysiwygController.load(componentId, messageId, language);
        assertThat(content, is(expectedContent));
        List<SimpleDocument> lockedFiles =
            AttachmentServiceFactory.getAttachmentService().listDocumentsLockedByUser(userId, null);
        assertThat(lockedFiles, is(notNullValue()));
        assertThat(lockedFiles, hasSize(0));

        wysiwygs = listWysiwygs(resourceTestPK);
        assertThat(wysiwygs, hasSize(1));

        expectedContent = "FR_Content";
        language = "fr";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);

        wysiwygs = listWysiwygs(resourceTestPK);
        assertThat(wysiwygs, hasSize(1));
        assertThat(WysiwygController.load(componentId, messageId, "fr"), is("FR_Content"));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is("EN_Content"));
      }
    }.execute();
  }

  /**
   * Test of save method, of class WysiwygController.
   */
  @Test
  public void testSaveEmptyWysiwyg() throws Exception {
    new JcrTest() {
      @Override
      public void run() {
        String componentId = "blog974";
        String messageId = "18";
        String expectedContent = "";
        String userId = "7";
        String language = "en";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);

        expectedContent = "";
        language = "fr";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);

        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        List<SimpleDocument> wysiwygs = listWysiwygs(resourceTestPK);
        assertThat(wysiwygs, hasSize(0));
        assertThat(WysiwygController.load(componentId, messageId, "fr"), is(""));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is(""));
      }
    }.execute();
  }

  /**
   * Test of save method, of class WysiwygController.
   */
  @Test
  public void testSaveWysiwygThenUpdateIt() throws Exception {
    new JcrTest() {
      @Override
      public void run() {
        String componentId = "blog974";
        String messageId = "18";
        String expectedContent = "EN_Content";
        String userId = "7";
        String language = "en";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);

        expectedContent = "FR_Content";
        language = "fr";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);

        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        List<SimpleDocument> wysiwygs = listWysiwygs(resourceTestPK);
        assertThat(wysiwygs, hasSize(1));
        assertThat(WysiwygController.load(componentId, messageId, "fr"), is("FR_Content"));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is("EN_Content"));

        WysiwygController
            .save(expectedContent + "_updated", componentId, messageId, userId, language, false);
        wysiwygs = listWysiwygs(resourceTestPK);
        assertThat(wysiwygs, hasSize(1));
        assertThat(WysiwygController.load(componentId, messageId, "fr"), is("FR_Content_updated"));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is("EN_Content"));
      }
    }.execute();
  }

  /**
   * Test of save method, of class WysiwygController.
   */
  @Test
  public void testSaveWysiwygThenUpdateItWithEmptyOne() throws Exception {
    new JcrTest() {
      @Override
      public void run() {
        String componentId = "blog974";
        String messageId = "18";
        String expectedContent = "EN_Content";
        String userId = "7";
        String language = "en";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);

        expectedContent = "FR_Content";
        language = "fr";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);

        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        List<SimpleDocument> wysiwygs = listWysiwygs(resourceTestPK);
        assertThat(wysiwygs, hasSize(1));
        assertThat(WysiwygController.load(componentId, messageId, "fr"), is("FR_Content"));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is("EN_Content"));

        WysiwygController.save("", componentId, messageId, userId, language, false);
        wysiwygs = listWysiwygs(resourceTestPK);
        assertThat(wysiwygs, hasSize(1));
        assertThat(WysiwygController.load(componentId, messageId, "fr"), is("EN_Content"));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is("EN_Content"));

        language = "en";
        WysiwygController.save("", componentId, messageId, userId, language, false);
        wysiwygs = listWysiwygs(resourceTestPK);
        assertThat(wysiwygs, hasSize(0));
        assertThat(WysiwygController.load(componentId, messageId, "fr"), is(""));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is(""));
      }
    }.execute();
  }

  /**
   * Test of save method, of class WysiwygController.
   */
  @Test
  public void testSaveVersionedWysiwygThenUpdateItWithEmptyOne() throws Exception {
    new JcrTest() {
      @Override
      public void run() {
        String componentId = "blog974";
        String messageId = "18";
        String expectedContent = "EN_Content";
        String userId = "7";
        String language = "en";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);

        expectedContent = "FR_Content";
        language = "fr";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);

        ForeignPK resourceTestPK = new ForeignPK(messageId, componentId);
        List<SimpleDocument> wysiwygs = listWysiwygs(resourceTestPK);
        assertThat(wysiwygs, hasSize(1));
        assertThat(WysiwygController.load(componentId, messageId, "fr"), is("FR_Content"));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is("EN_Content"));

        AttachmentServiceFactory.getAttachmentService()
            .changeVersionState(wysiwygs.get(0).getPk(), "Versioned test");

        expectedContent = "FR_Content_updated";
        language = "fr";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);

        expectedContent = "EN_Content_updated";
        language = "en";
        WysiwygController.save(expectedContent, componentId, messageId, userId, language, false);

        wysiwygs = listWysiwygs(resourceTestPK);
        assertThat(wysiwygs, hasSize(1));
        assertThat(wysiwygs.get(0), instanceOf(HistorisedDocument.class));
        assertThat(WysiwygController.load(componentId, messageId, "fr"), is("FR_Content_updated"));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is("EN_Content_updated"));

        language = "fr";
        WysiwygController.save("", componentId, messageId, userId, language, false);
        wysiwygs = listWysiwygs(resourceTestPK);
        assertThat(wysiwygs, hasSize(1));
        assertThat(wysiwygs.get(0), instanceOf(HistorisedDocument.class));
        assertThat(WysiwygController.load(componentId, messageId, "fr"), is("EN_Content_updated"));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is("EN_Content_updated"));

        language = "en";
        WysiwygController.save("", componentId, messageId, userId, language, false);
        wysiwygs = listWysiwygs(resourceTestPK);
        assertThat(wysiwygs, hasSize(0));
        assertThat(WysiwygController.load(componentId, messageId, "fr"), is(""));
        assertThat(WysiwygController.load(componentId, messageId, "en"), is(""));
      }
    }.execute();
  }

  @Test
  public void testReplaceInternalImageId() throws IOException {
    InputStream in = WysiwygController.class.getResourceAsStream("24wysiwyg_fr.txt");
    InputStream resultIn = WysiwygController.class.getResourceAsStream("move_result.txt");
    try {
      String content = IOUtils.toString(in);
      String result = IOUtils.toString(resultIn);
      SimpleDocumentPK oldPk =
          new SimpleDocumentPK("dd99f10b-0640-40d3-9ef4-8b84d29a7c85", "kmelia1");
      oldPk.setOldSilverpeasId(34L);
      SimpleDocumentPK newPk =
          new SimpleDocumentPK("f2eb803f-cb46-4988-b89d-045c4e846da4", "kmelia1");
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
      SimpleDocumentPK oldPk =
          new SimpleDocumentPK("359d2924-b6c6-461c-a459-2eef38f12c3c", "kmelia1");
      oldPk.setOldSilverpeasId(34L);
      SimpleDocumentPK newPk =
          new SimpleDocumentPK("f2eb803f-cb46-4988-b89d-045c4e846da4", "kmelia18");
      newPk.setOldSilverpeasId(41L);
      String move = WysiwygController.replaceInternalImageId(content, oldPk, newPk);
      assertThat(move, is(result));
    } finally {
      IOUtils.closeQuietly(in);
      IOUtils.closeQuietly(resultIn);
    }
  }
}
