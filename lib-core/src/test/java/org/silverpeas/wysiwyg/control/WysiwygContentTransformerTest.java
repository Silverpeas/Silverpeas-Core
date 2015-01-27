/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
package org.silverpeas.wysiwyg.control;

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
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WysiwygContentTransformerTest {

  private static final String IMAGE_ATTACHMENT_LINK =
      "/silverpeas/attached_file/componentId/infoLetter175/attachmentId/d07411cc-19af-49f8-af57" +
          "-16fc9fabf318/lang/fr/name/Aikido16.jpg";
  private static final String IMAGE_ATTACHMENT_LINK_BIS =
      "http://www.toto.fr/silverpeas/File/d07411cc-19af-49f8-af57-16fc9fabf318-bis";
  private static final String LINK_ATTACHMENT_LINK =
      "/silverpeas/attached_file/componentId/infoLetter175/attachmentId/72f56ba9-b089-40c4-b16c" +
          "-255e93658259/lang/fr/name/2-2_Formation_Developpeur-Partie-2.pdf";
  private static final String LINK_ATTACHMENT_LINK_BIS =
      "https://www.toto.fr/silverpeas/attached_file/componentId/infoLetter175/attachmentId" +
          "/72f56ba9-b089-40c4-b16c-255e93658259-bis/lang/fr/name/2-2_Formation_Developpeur" +
          "-Partie-2.pdf";

  private static final String IMAGE_ATTACHMENT_ID = "d07411cc-19af-49f8-af57-16fc9fabf318";
  private static final String IMAGE_ATTACHMENT_ID_BIS = "d07411cc-19af-49f8-af57-16fc9fabf318-bis";
  private static final String LINK_ATTACHMENT_ID = "72f56ba9-b089-40c4-b16c-255e93658259";
  private static final String LINK_ATTACHMENT_ID_BIS = "72f56ba9-b089-40c4-b16c-255e93658259-bis";

  private AttachmentService oldAttachmentService;

  @Before
  public void setup() throws Exception {
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
            SimpleDocument simpleDocument = new SimpleDocument();
            simpleDocument.setPK(pk);
            SimpleAttachment simpleAttachment = new SimpleAttachment();
            simpleDocument.setAttachment(simpleAttachment);
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

  @After
  public void destroy() throws Exception {
    // Replacing by reflection the mock instances by the previous extracted one.
    AttachmentServiceFactory attachmentServiceFactory = (AttachmentServiceFactory) FieldUtils
        .readDeclaredStaticField(AttachmentServiceFactory.class, "factory", true);
    FieldUtils.writeDeclaredField(attachmentServiceFactory, "service", oldAttachmentService, true);
  }

  @Test
  public void toMailContent() throws Exception {
    WysiwygContentTransformer transformer = WysiwygContentTransformer
        .on(getContentOfDocumentNamed("wysiwygWithSeveralTypesOfLink.txt"));

    WysiwygContentTransformer.MailResult mailResult = transformer.toMailContent();

    assertThat(mailResult.getBodyParts().size(), is(6));
    assertThat(mailResult.getWysiwygContent(), is(FileUtils.readFileToString(
        getDocumentNamed("wysiwygWithSeveralTypesOfLinkTransformedForMailSendingResult.txt"))));
  }

  @Test
  public void extractAttachmentsByLinks() {
    WysiwygContentTransformer transformer = WysiwygContentTransformer
        .on(getContentOfDocumentNamed("wysiwygWithSeveralTypesOfLink.txt"));

    Map<String, SimpleDocument> attachmentsByLinks = transformer.extractAttachmentsByLinks();
    Map<String, String> attachmentIdsByLinks = new HashMap<String, String>();
    for (Map.Entry<String, SimpleDocument> attachmentByLink : attachmentsByLinks.entrySet()) {
      attachmentIdsByLinks.put(attachmentByLink.getKey(), attachmentByLink.getValue().getId());
    }

    assertThat(attachmentIdsByLinks.size(), is(4));
    assertThat(attachmentIdsByLinks, hasEntry(IMAGE_ATTACHMENT_LINK, IMAGE_ATTACHMENT_ID));
    assertThat(attachmentIdsByLinks, hasEntry(IMAGE_ATTACHMENT_LINK_BIS, IMAGE_ATTACHMENT_ID_BIS));
    assertThat(attachmentIdsByLinks, hasEntry(LINK_ATTACHMENT_LINK, LINK_ATTACHMENT_ID));
    assertThat(attachmentIdsByLinks, hasEntry(LINK_ATTACHMENT_LINK_BIS, LINK_ATTACHMENT_ID_BIS));
  }

  @Test
  public void manageImageResizing() throws Exception {
    WysiwygContentTransformer transformer = WysiwygContentTransformer
        .on(getContentOfDocumentNamed("wysiwygWithSeveralImages.txt"));

    String result = transformer.updateURLOfImagesAccordingToSizes();

    assertThat(result, is(FileUtils.readFileToString(
        getDocumentNamed("wysiwygWithSeveralImagesTransformedForImageResizingResult.txt"))));
  }

  /*
  TOOL METHODS
   */

  private static String getContentOfDocumentNamed(final String name) {
    try {
      return FileUtil.readFileToString(getDocumentNamed(name));
    } catch (IOException e) {
      return null;
    }
  }

  private static File getDocumentNamed(final String name) {
    final URL documentLocation = WysiwygContentTransformerTest.class.getResource(name);
    try {
      return new File(documentLocation.toURI());
    } catch (URISyntaxException e) {
      return null;
    }
  }
}