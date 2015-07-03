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
package org.silverpeas.file;

import com.silverpeas.util.FileUtil;
import com.stratelia.silverpeas.peasCore.URLManager;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.silverpeas.attachment.AttachmentService;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.silverpeas.file.ImageResizingProcessor.IMAGE_CACHE_PATH;
import static org.silverpeas.file.SilverpeasFileProcessor.ProcessingContext.GETTING;
import static org.silverpeas.file.SilverpeasFileProcessor.ProcessingContext.WRITING;

public class AttachmentUrlLinkProcessorTest {

  private static final String IMAGE_NAME = "image-test.jpg";

  private ConfigurableApplicationContext ctx;
  private File originalImage;
  private File originalImageWithResizeDirective;
  private AttachmentUrlLinkProcessor processor;

  private AttachmentService oldAttachmentService;
  private AttachmentService mockAttachmentService;

  private String originalImageNotAnAttachmentUrlLink =
      "dummy_begin" + URLManager.getApplicationURL() +
          "uriPart/notAnAttachmentId/09-ab-89/lang/en/whaou";

  private String originalImageAttachmentUrlLink = "dummy_begin" + URLManager.getApplicationURL() +
      "uriPart/attachmentId/09-ab-89/lang/en/whaou";

  private String originalImageAttachmentUrlLinkWithoutLang =
      "dummy_begin" + URLManager.getApplicationURL() +
          "uriPart/attachmentId/09-ab-89/whaou";

  private String originalImageAttachmentUrlLinkWithResizeDirective =
      "dummy_begin" + URLManager.getApplicationURL() +
          "uriPart/attachmentId/09-ab-89/lang/en/size/250x150/whaou";

  @Before
  public void setUp() throws Exception {
    // get the original path
    originalImage = new File(getClass().getResource("/" + IMAGE_NAME).getPath());
    assertThat(originalImage.exists(), is(true));
    originalImageWithResizeDirective =
        new File(originalImage.getParentFile(), "250x150/" + IMAGE_NAME);

    // bootstrap the Spring context
    ctx = new ClassPathXmlApplicationContext("classpath:/spring-image.xml");
    ctx.start();
    processor = ctx.getBean(AttachmentUrlLinkProcessor.class);
  }

  @After
  public void tearDown() throws Exception {
    ctx.close();
    File cache = new File(IMAGE_CACHE_PATH);
    if (cache.exists()) {
      FileUtil.forceDeletion(new File(IMAGE_CACHE_PATH));
    }
  }

  @Before
  public void setupAttachmentService() throws Exception {
    // Injecting by reflection the mock instance
    AttachmentServiceFactory attachmentServiceFactory = (AttachmentServiceFactory) FieldUtils
        .readDeclaredStaticField(AttachmentServiceFactory.class, "factory", true);
    oldAttachmentService =
        (AttachmentService) FieldUtils.readDeclaredField(attachmentServiceFactory, "service", true);
    mockAttachmentService = mock(AttachmentService.class);
    FieldUtils.writeDeclaredField(attachmentServiceFactory, "service", mockAttachmentService, true);

    /*
    Mocking methods of attachment service instance
     */

    // searchDocumentById returns always a simple document which the PK is the one specified from
    // method parameters.
    when(mockAttachmentService.searchDocumentById(any(SimpleDocumentPK.class), anyString()))
        .then(new Answer<SimpleDocument>() {
          @Override
          public SimpleDocument answer(final InvocationOnMock invocation) throws Throwable {
            SimpleDocumentPK pk = (SimpleDocumentPK) invocation.getArguments()[0];
            SimpleDocument simpleDocument = mock(SimpleDocument.class);
            when(simpleDocument.getPk()).thenReturn(pk);
            when(simpleDocument.getAttachmentPath()).thenReturn(originalImage.getPath());
            return simpleDocument;
          }
        });
  }

  @After
  public void destroyAttachmentService() throws Exception {
    // Replacing by reflection the mock instances by the previous extracted one.
    AttachmentServiceFactory attachmentServiceFactory = (AttachmentServiceFactory) FieldUtils
        .readDeclaredStaticField(AttachmentServiceFactory.class, "factory", true);
    FieldUtils.writeDeclaredField(attachmentServiceFactory, "service", oldAttachmentService, true);
  }

  @Test
  public void notALink() throws Exception {
    String actualPath = processor.processBefore(originalImage.getCanonicalPath(), GETTING);
    assertThat(actualPath, is(originalImage.getCanonicalPath()));
  }

  @Test
  public void notAnAttachmentUrlLink() throws Exception {
    String actualPath = processor.processBefore(originalImageNotAnAttachmentUrlLink, GETTING);
    assertThat(actualPath, is(originalImageNotAnAttachmentUrlLink));
  }

  @Test
  public void anAttachmentUrlLinkWithoutResizeDirective() throws Exception {
    String actualPath = processor.processBefore(originalImageAttachmentUrlLink, GETTING);
    assertThat(actualPath, is(originalImage.getCanonicalPath()));
  }

  @Test
  public void anAttachmentUrlLinkOnWritingOperation() throws Exception {
    String actualPath = processor.processBefore(originalImageAttachmentUrlLink, WRITING);
    assertThat(actualPath, is(originalImageAttachmentUrlLink));
  }

  @Test
  public void anAttachmentUrlLinkOnAttachmentThatDoesNotExist() throws Exception {
    reset(mockAttachmentService);
    String actualPath = processor.processBefore(originalImageAttachmentUrlLink, GETTING);
    assertThat(actualPath, isEmptyString());
  }


  @Test
  public void anAttachmentUrlLinkWithoutResizeDirectiveNoLanguage() throws Exception {
    String actualPath = processor.processBefore(originalImageAttachmentUrlLinkWithoutLang, GETTING);
    assertThat(actualPath, is(originalImage.getCanonicalPath()));
  }

  @Test
  public void anAttachmentUrlLinkWithResizeDirective() throws Exception {
    String actualPath =
        processor.processBefore(originalImageAttachmentUrlLinkWithResizeDirective, GETTING);
    assertThat(actualPath, is(originalImageWithResizeDirective.getCanonicalPath()));
  }
}