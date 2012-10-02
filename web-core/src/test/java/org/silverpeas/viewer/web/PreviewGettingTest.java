/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.viewer.web;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.silverpeas.viewer.web.PreviewEntityMatcher.matches;
import static org.silverpeas.viewer.web.ViewerTestResources.JAVA_PACKAGE;
import static org.silverpeas.viewer.web.ViewerTestResources.SPRING_CONTEXT;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.viewer.Preview;

import com.silverpeas.web.ResourceGettingTest;
import com.stratelia.webactiv.beans.admin.UserDetail;

/**
 * Tests on the comment getting by the CommentResource web service.
 * @author Yohann Chastagnier
 */
public class PreviewGettingTest extends ResourceGettingTest<ViewerTestResources> {

  private UserDetail user;
  private String sessionKey;
  private Preview expected;

  private static String ATTACHMENT_ID = "7";
  private static String ATTACHMENT_ID_DOESNT_EXISTS = "8";

  public PreviewGettingTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void prepareTestResources() {
    user = aUser();
    sessionKey = authenticate(user);
    expected = PreviewBuilder.getPreviewBuilder().buildFileName("previewId", "originalFileName7");

    when(getTestResources().getAttachmentServiceMock().searchAttachmentById(any(SimpleDocumentPK.class), any(String.class)))
        .thenAnswer(new Answer<SimpleDocument>() {

          /*
           * (non-Javadoc)
           * @see org.mockito.stubbing.Answer#answer(org.mockito.invocation.InvocationOnMock)
           */
          @Override
          public SimpleDocument answer(final InvocationOnMock invocation) throws Throwable {
            final SimpleDocumentPK attachmentPK = (SimpleDocumentPK) invocation.getArguments()[0];
            SimpleDocument attachmentDetail = null;
            if (!ATTACHMENT_ID_DOESNT_EXISTS.equals(attachmentPK.getId())) {
              attachmentDetail = new SimpleDocument();
              attachmentDetail.setPK(attachmentPK);
              attachmentDetail.setOldSilverpeasId(Long.parseLong(attachmentPK.getId()));
              attachmentDetail.setFile(new SimpleAttachment());
              attachmentDetail.setFilename("originalFileName" + attachmentPK.getId());
            }
            return attachmentDetail;
          }
        });

    when(getTestResources().getPreviewServiceMockWrapper().getPreview(anyString(), any(File.class)))
        .thenAnswer(new Answer<Preview>() {

          /*
           * (non-Javadoc)
           * @see org.mockito.stubbing.Answer#answer(org.mockito.invocation.InvocationOnMock)
           */
          @Override
          public Preview answer(final InvocationOnMock invocation) throws Throwable {
            final String originalFileName = (String) invocation.getArguments()[0];
            final File physicalFile = (File) invocation.getArguments()[1];
            final Preview preview = mock(Preview.class);
            when(preview.getOriginalFileName()).thenReturn(originalFileName);
            when(preview.getURLAsString()).thenReturn("/URL/" + physicalFile.getName());
            return preview;
          }
        });
  }

  @Test
  public void getPreview() {
    final PreviewEntity entity = getAt(aResourceURI(), PreviewEntity.class);
    assertNotNull(entity);
    assertThat(entity, PreviewEntityMatcher.matches(expected));
  }

  @Override
  public String aResourceURI() {
    return aResourceURI(ATTACHMENT_ID);
  }

  private String aResourceURI(final String attachmentId) {
    return "preview/" + getExistingComponentInstances()[0] + "/attachment/" + attachmentId;
  }

  @Override
  public String anUnexistingResourceURI() {
    return aResourceURI(ATTACHMENT_ID_DOESNT_EXISTS);
  }

  @Override
  public Preview aResource() {
    return expected;
  }

  @Override
  public String getSessionKey() {
    return sessionKey;
  }

  @Override
  public Class<?> getWebEntityClass() {
    return PreviewEntity.class;
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[] { "componentName5" };
  }
}
