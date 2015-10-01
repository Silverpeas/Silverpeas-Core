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
package org.silverpeas.viewer.web;

import com.silverpeas.web.ResourceGettingTest;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.viewer.DocumentView;
import org.silverpeas.viewer.ViewerContext;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.silverpeas.viewer.web.ViewerTestResources.JAVA_PACKAGE;
import static org.silverpeas.viewer.web.ViewerTestResources.SPRING_CONTEXT;

/**
 * Tests on the comment getting by the CommentResource web service.
 * @author Yohann Chastagnier
 */
public class DocumentViewGettingTest extends ResourceGettingTest<ViewerTestResources> {

  private UserDetail user;
  private String sessionKey;
  private DocumentView expected;

  private static String ATTACHMENT_ID = "7";
  private static String ATTACHMENT_ID_DOESNT_EXISTS = "8";

  public DocumentViewGettingTest() {
    super(JAVA_PACKAGE, SPRING_CONTEXT);
  }

  @Before
  public void prepareTestResources() {
    user = aUser();
    sessionKey = authenticate(user);
    expected = DocumentViewBuilder.getDocumentViewBuilder()
        .buildFileName("documentViewId", "originalFileName7");

    when(getTestResources().getAttachmentServiceMock()
        .searchDocumentById(any(SimpleDocumentPK.class), any(String.class)))
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
              attachmentDetail.setAttachment(new SimpleAttachment());
              attachmentDetail.setFilename("originalFileName" + attachmentPK.getId());
            }
            return attachmentDetail;
          }
        });

    when(getTestResources().getDocumentViewServiceMockWrapper()
        .getDocumentView(any(ViewerContext.class))).thenAnswer(new Answer<DocumentView>() {

      /*
       * (non-Javadoc)
       * @see org.mockito.stubbing.Answer#answer(org.mockito.invocation.InvocationOnMock)
       */
      @Override
      public DocumentView answer(final InvocationOnMock invocation) throws Throwable {
        final ViewerContext viewerContext = (ViewerContext) invocation.getArguments()[0];
        final String originalFileName = viewerContext.getOriginalFileName();
        final File physicalFile = viewerContext.getOriginalSourceFile();
        final DocumentView documentView = mock(DocumentView.class);
        when(documentView.getOriginalFileName()).thenReturn(originalFileName);
        when(documentView.getURLAsString()).thenReturn("/URL/" + physicalFile.getName());
        return documentView;
      }
    });
  }

  @Test
  public void getDocumentView() {
    final DocumentViewEntity entity = getAt(aResourceURI(), DocumentViewEntity.class);
    assertNotNull(entity);
    assertThat(entity, DocumentViewEntityMatcher.matches(expected));
  }

  @Override
  public String aResourceURI() {
    return aResourceURI(ATTACHMENT_ID);
  }

  private String aResourceURI(final String attachmentId) {
    return "view/" + getExistingComponentInstances()[0] + "/attachment/" + attachmentId;
  }

  @Override
  public String anUnexistingResourceURI() {
    return aResourceURI(ATTACHMENT_ID_DOESNT_EXISTS);
  }

  @Override
  public DocumentView aResource() {
    return expected;
  }

  @Override
  public String getSessionKey() {
    return sessionKey;
  }

  @Override
  public Class<?> getWebEntityClass() {
    return DocumentViewEntity.class;
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{"componentName5"};
  }
}
