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
package org.silverpeas.wysiwyg.control.result;

import com.silverpeas.util.FileUtil;
import com.stratelia.silverpeas.peasCore.URLManager;
import org.apache.commons.io.FileUtils;
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
import org.silverpeas.file.AttachmentUrlLinkProcessor;
import org.silverpeas.file.SilverpeasFileProcessor;
import org.silverpeas.file.SilverpeasFileProvider;
import org.silverpeas.wysiwyg.control.WysiwygContentTransformerTest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MailContentProcessTest {

  private static final String ODT_NAME = "LibreOffice.odt";
  private static final String IMAGE_NAME = "image-test.jpg";

  private File originalOdt;
  private File originalImage;
  private File originalImageWithResize100x;
  private File originalImageWithResize100x100;

  private static final String ODT_ATTACHMENT_ID = "72f56ba9-b089-40c4-b16c-255e93658259";

  private static final String IMAGE_ATTACHMENT_LINK =
      "/silverpeas/attached_file/componentId/infoLetter175/attachmentId/d07411cc-19af-49f8-af57" +
          "-16fc9fabf318/lang/fr/name/Aikido16.jpg";
  private static final String IMAGE_ATTACHMENT_LINK_WITH_SIZE_100x =
      "/silverpeas/attached_file/componentId/infoLetter175/attachmentId/d07411cc-19af-49f8-af57" +
          "-16fc9fabf318/lang/fr/size/100x/name/Aikido16.jpg";
  private static final String IMAGE_ATTACHMENT_LINK_WITH_SIZE_100x100 =
      "/silverpeas/attached_file/componentId/infoLetter175/attachmentId/d07411cc-19af-49f8-af57" +
          "-16fc9fabf318/lang/fr/size/100x100/name/Aikido16.jpg";
  private static final String LINK_ATTACHMENT_LINK =
      "/silverpeas/attached_file/componentId/infoLetter175/attachmentId/72f56ba9-b089-40c4-b16c" +
          "-255e93658259/lang/fr/name/2-2_Formation_Developpeur-Partie-2.pdf";
  private static final String LINK_ATTACHMENT_LINK_BIS =
      "https://www.toto.fr/silverpeas/attached_file/componentId/infoLetter175/attachmentId" +
          "/72f56ba9-b089-40c4-b16c-255e93658259-bis/lang/fr/name/2-2_Formation_Developpeur-Partie-2.pdf";
  private static final String IMAGE_ATTACHMENT_LINK_BIS =
      "http://www.toto.fr/silverpeas/File/d07411cc-19af-49f8-af57-16fc9fabf318-bis";

  private AttachmentService oldAttachmentService;

  @SuppressWarnings("unchecked")
  @Before
  public void setup() throws Exception {
    originalOdt = new File(getClass().getResource("/" + ODT_NAME).getPath());
    assertThat(originalOdt.exists(), is(true));
    originalImage = new File(getClass().getResource("/" + IMAGE_NAME).getPath());
    assertThat(originalImage.exists(), is(true));
    originalImageWithResize100x =
        new File(originalImage.getParentFile(), "100x/" + IMAGE_NAME);
    FileUtils.touch(originalImageWithResize100x);
    assertThat(originalImageWithResize100x.exists(), is(true));
    originalImageWithResize100x100 =
        new File(originalImage.getParentFile(), "100x100/" + IMAGE_NAME);
    FileUtils.touch(originalImageWithResize100x100);
    assertThat(originalImageWithResize100x100.exists(), is(true));

    // SilverpeasFile
    List<SilverpeasFileProcessor> processors = (List<SilverpeasFileProcessor>) FieldUtils
        .readDeclaredField(SilverpeasFileProvider.getInstance(), "processors", true);
    processors.clear();
    SilverpeasFileProvider.getInstance().addProcessor(new AttachmentUrlLinkProcessor());

    // Injecting by reflection the mock instance
    AttachmentServiceFactory attachmentServiceFactory = (AttachmentServiceFactory) FieldUtils
        .readDeclaredStaticField(AttachmentServiceFactory.class, "factory", true);
    oldAttachmentService =
        (AttachmentService) FieldUtils.readDeclaredField(attachmentServiceFactory, "service", true);
    AttachmentService mockAttachmentService = mock(AttachmentService.class);
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
            if (pk.getId().contains(ODT_ATTACHMENT_ID)) {
              when(simpleDocument.getAttachmentPath()).thenReturn(originalOdt.getPath());
            } else {
              when(simpleDocument.getAttachmentPath()).thenReturn(originalImage.getPath());
            }
            return simpleDocument;
          }
        });

    /*
    Setting the server start URL
     */
    HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
    when(mockHttpServletRequest.getScheme()).thenReturn("http");
    when(mockHttpServletRequest.getServerName()).thenReturn("www.unit-test-silverpeas.org");
    when(mockHttpServletRequest.getServerPort()).thenReturn(80);
    URLManager.setCurrentServerUrl(mockHttpServletRequest);
  }

  @SuppressWarnings("unchecked")
  @After
  public void destroy() throws Exception {
    FileUtils.deleteQuietly(originalImageWithResize100x.getParentFile());
    FileUtils.deleteQuietly(originalImageWithResize100x100.getParentFile());

    // SilverpeasFile
    List<SilverpeasFileProcessor> processors = (List<SilverpeasFileProcessor>) FieldUtils
        .readDeclaredField(SilverpeasFileProvider.getInstance(), "processors", true);
    processors.clear();

    // Replacing by reflection the mock instances by the previous extracted one.
    AttachmentServiceFactory attachmentServiceFactory = (AttachmentServiceFactory) FieldUtils
        .readDeclaredStaticField(AttachmentServiceFactory.class, "factory", true);
    FieldUtils.writeDeclaredField(attachmentServiceFactory, "service", oldAttachmentService, true);
  }

  @Test
  public void toMailContent() throws Exception {
    MailContentProcess mailContentProcess = new MailContentProcess();
    String wysiwygContentSource = getContentOfDocumentNamed("wysiwygWithSeveralTypesOfLink.txt");

    MailContentProcess.MailResult mailResult = mailContentProcess.execute(wysiwygContentSource);

    assertThat(mailResult.getBodyParts().size(), is(6));
    assertThat(mailResult.getWysiwygContent(), is(getContentOfDocumentNamed(
        "wysiwygWithSeveralTypesOfLinkTransformedForMailSendingResult.txt")));
  }

  @Test
  public void extractAttachmentsByLinks() {
    MailContentProcess mailContentProcess = new MailContentProcess();
    String wysiwygContentSource = getContentOfDocumentNamed("wysiwygWithSeveralTypesOfLink.txt");

    List<String> attachmentLinks =
        mailContentProcess.extractAllLinksOfReferencedAttachments(wysiwygContentSource);

    assertThat(attachmentLinks.size(), is(6));
    assertThat(attachmentLinks,
        contains(IMAGE_ATTACHMENT_LINK, IMAGE_ATTACHMENT_LINK_WITH_SIZE_100x,
            IMAGE_ATTACHMENT_LINK_WITH_SIZE_100x100, LINK_ATTACHMENT_LINK_BIS, LINK_ATTACHMENT_LINK,
            IMAGE_ATTACHMENT_LINK_BIS));
  }

  /*
  TOOL METHODS
   */

  private synchronized static String getContentOfDocumentNamed(final String name) {
    try {
      return FileUtil.readFileToString(getDocumentNamed(name));
    } catch (IOException e) {
      return null;
    }
  }

  private synchronized static File getDocumentNamed(final String name) {
    final URL documentLocation = WysiwygContentTransformerTest.class.getResource(name);
    try {
      return new File(documentLocation.toURI());
    } catch (URISyntaxException e) {
      return null;
    }
  }
}