/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.io.file;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedMock;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.file.FileUtil;

import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.silverpeas.core.io.file.ImageResizingProcessor.IMAGE_CACHE_PATH;

@EnableSilverTestEnv
class AttachmentUrlLinkProcessorTest {

  private static final String IMAGE_NAME = "image-test.jpg";

  private File originalImage;
  private File originalImageWithResizeDirective;
  private AttachmentUrlLinkProcessor processor;

  private String originalImageNotAnAttachmentUrlLink;
  private String originalImageAttachmentUrlLink;
  private String originalImageAttachmentUrlLinkWithoutLang;
  private String originalImageAttachmentUrlLinkWithResizeDirective;

  @BeforeEach
  void variableInit() throws Exception {
    originalImageNotAnAttachmentUrlLink = "dummy_begin" + URLUtil.getApplicationURL() +
        "uriPart/notAnAttachmentId/09-ab-89/lang/en/whaou";
    originalImageAttachmentUrlLink = "dummy_begin" + URLUtil.getApplicationURL() +
        "uriPart/attachmentId/09-ab-89/lang/en/whaou";
    originalImageAttachmentUrlLinkWithoutLang = "dummy_begin" + URLUtil.getApplicationURL() +
        "uriPart/attachmentId/09-ab-89/whaou";
    originalImageAttachmentUrlLinkWithResizeDirective =
        "dummy_begin" + URLUtil.getApplicationURL() +
            "uriPart/attachmentId/09-ab-89/lang/en/size/250x150/whaou";
  }

  @BeforeEach
  void setUp() {
    // get the original path
    originalImage = new File(getClass().getResource("/" + IMAGE_NAME).getPath());
    assertThat(originalImage.exists(), is(true));
    originalImageWithResizeDirective =
        new File(originalImage.getParentFile(), "250x150/" + IMAGE_NAME);

    // bootstrap the Spring context
    processor = new AttachmentUrlLinkProcessor();
  }

  @AfterEach
  void tearDown() throws Exception {
    File cache = new File(IMAGE_CACHE_PATH);
    if (cache.exists()) {
      FileUtil.forceDeletion(new File(IMAGE_CACHE_PATH));
    }
  }

  @BeforeEach
  void setupAttachmentService(@TestManagedMock AttachmentService mockAttachmentService) {
    /*
    Mocking methods of attachment service instance
     */

    // searchDocumentById returns always a simple document which the PK is the one specified from
    // method parameters.
    when(mockAttachmentService.searchDocumentById(any(SimpleDocumentPK.class), anyString()))
        .then(invocation -> {
          SimpleDocumentPK pk = (SimpleDocumentPK) invocation.getArguments()[0];
          SimpleDocument simpleDocument = mock(SimpleDocument.class);
          when(simpleDocument.getPk()).thenReturn(pk);
          when(simpleDocument.getAttachmentPath()).thenReturn(originalImage.getPath());
          return simpleDocument;
        });
  }

  @Test
  void notALink() throws Exception {
    String actualPath = processor.processBefore(originalImage.getCanonicalPath(), SilverpeasFileProcessor.ProcessingContext.GETTING);
    assertThat(actualPath, is(originalImage.getCanonicalPath()));
  }

  @Test
  void notAnAttachmentUrlLink() throws Exception {
    String actualPath = processor.processBefore(originalImageNotAnAttachmentUrlLink, SilverpeasFileProcessor.ProcessingContext.GETTING);
    assertThat(actualPath, is(originalImageNotAnAttachmentUrlLink));
  }

  @Test
  void anAttachmentUrlLinkWithoutResizeDirective() throws Exception {
    String actualPath = processor.processBefore(originalImageAttachmentUrlLink, SilverpeasFileProcessor.ProcessingContext.GETTING);
    assertThat(actualPath, is(originalImage.getCanonicalPath()));
  }

  @Test
  void anAttachmentUrlLinkOnWritingOperation() throws Exception {
    String actualPath = processor.processBefore(originalImageAttachmentUrlLink, SilverpeasFileProcessor.ProcessingContext.WRITING);
    assertThat(actualPath, is(originalImageAttachmentUrlLink));
  }

  @Test
  void anAttachmentUrlLinkOnAttachmentThatDoesNotExist() throws Exception {
    reset(AttachmentServiceProvider.getAttachmentService());
    String actualPath = processor.processBefore(originalImageAttachmentUrlLink, SilverpeasFileProcessor.ProcessingContext.GETTING);
    assertThat(actualPath, isEmptyString());
  }


  @Test
  void anAttachmentUrlLinkWithoutResizeDirectiveNoLanguage() throws Exception {
    String actualPath = processor.processBefore(originalImageAttachmentUrlLinkWithoutLang, SilverpeasFileProcessor.ProcessingContext.GETTING);
    assertThat(actualPath, is(originalImage.getCanonicalPath()));
  }

  @Test
  void anAttachmentUrlLinkWithResizeDirective() throws Exception {
    String actualPath =
        processor.processBefore(originalImageAttachmentUrlLinkWithResizeDirective, SilverpeasFileProcessor.ProcessingContext.GETTING);
    assertThat(actualPath, is(originalImageWithResizeDirective.getCanonicalPath()));
  }
}