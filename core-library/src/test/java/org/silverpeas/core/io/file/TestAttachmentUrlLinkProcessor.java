/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import org.silverpeas.core.util.URLUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.test.TestBeanContainer;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.util.file.FileUtil;

import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.silverpeas.core.io.file.ImageResizingProcessor.IMAGE_CACHE_PATH;

public class TestAttachmentUrlLinkProcessor {

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  private static final String IMAGE_NAME = "image-test.jpg";

  private File originalImage;
  private File originalImageWithResizeDirective;
  private AttachmentUrlLinkProcessor processor;

  private String originalImageNotAnAttachmentUrlLink;
  private String originalImageAttachmentUrlLink;
  private String originalImageAttachmentUrlLinkWithoutLang;
  private String originalImageAttachmentUrlLinkWithResizeDirective;

  @Before
  public void variableInit() throws Exception {
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

  @Before
  public void setUp() throws Exception {
    // get the original path
    originalImage = new File(getClass().getResource("/" + IMAGE_NAME).getPath());
    assertThat(originalImage.exists(), is(true));
    originalImageWithResizeDirective =
        new File(originalImage.getParentFile(), "250x150/" + IMAGE_NAME);

    // bootstrap the Spring context
    processor = new AttachmentUrlLinkProcessor();
  }

  @After
  public void tearDown() throws Exception {
    File cache = new File(IMAGE_CACHE_PATH);
    if (cache.exists()) {
      FileUtil.forceDeletion(new File(IMAGE_CACHE_PATH));
    }
  }

  @Before
  public void setupAttachmentService() throws Exception {

    // The mock instance
    AttachmentService mockAttachmentService = mock(AttachmentService.class);
    when(TestBeanContainer.getMockedBeanContainer().getBeanByType(AttachmentService.class))
        .thenReturn(mockAttachmentService);

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
  public void notALink() throws Exception {
    String actualPath = processor.processBefore(originalImage.getCanonicalPath(), SilverpeasFileProcessor.ProcessingContext.GETTING);
    assertThat(actualPath, is(originalImage.getCanonicalPath()));
  }

  @Test
  public void notAnAttachmentUrlLink() throws Exception {
    String actualPath = processor.processBefore(originalImageNotAnAttachmentUrlLink, SilverpeasFileProcessor.ProcessingContext.GETTING);
    assertThat(actualPath, is(originalImageNotAnAttachmentUrlLink));
  }

  @Test
  public void anAttachmentUrlLinkWithoutResizeDirective() throws Exception {
    String actualPath = processor.processBefore(originalImageAttachmentUrlLink, SilverpeasFileProcessor.ProcessingContext.GETTING);
    assertThat(actualPath, is(originalImage.getCanonicalPath()));
  }

  @Test
  public void anAttachmentUrlLinkOnWritingOperation() throws Exception {
    String actualPath = processor.processBefore(originalImageAttachmentUrlLink, SilverpeasFileProcessor.ProcessingContext.WRITING);
    assertThat(actualPath, is(originalImageAttachmentUrlLink));
  }

  @Test
  public void anAttachmentUrlLinkOnAttachmentThatDoesNotExist() throws Exception {
    reset(AttachmentServiceProvider.getAttachmentService());
    String actualPath = processor.processBefore(originalImageAttachmentUrlLink, SilverpeasFileProcessor.ProcessingContext.GETTING);
    assertThat(actualPath, isEmptyString());
  }


  @Test
  public void anAttachmentUrlLinkWithoutResizeDirectiveNoLanguage() throws Exception {
    String actualPath = processor.processBefore(originalImageAttachmentUrlLinkWithoutLang, SilverpeasFileProcessor.ProcessingContext.GETTING);
    assertThat(actualPath, is(originalImage.getCanonicalPath()));
  }

  @Test
  public void anAttachmentUrlLinkWithResizeDirective() throws Exception {
    String actualPath =
        processor.processBefore(originalImageAttachmentUrlLinkWithResizeDirective, SilverpeasFileProcessor.ProcessingContext.GETTING);
    assertThat(actualPath, is(originalImageWithResizeDirective.getCanonicalPath()));
  }
}