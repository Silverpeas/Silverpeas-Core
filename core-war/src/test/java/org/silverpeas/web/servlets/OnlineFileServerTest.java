/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.servlets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.AccessControlOperation;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.security.authorization.SimpleDocumentAccessControl;
import org.silverpeas.core.security.authorization.SpaceAccessControl;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests on the access control performed by the {@link OnlineFileServer} servlet before
 * sending a file.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class OnlineFileServerTest {

  private static final String USER_ID = "42";
  private static final String COMPONENT_ID = "kmelia7";
  private static final String ATTACHMENT_ID = "3f2c8b9e-5b8a-4d2b-9d6e-0c7a1b2e4f5a";
  private static final String VERSION_ID = "9a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d";

  @TestManagedMock
  private ComponentAccessControl componentAccessControl;
  @TestManagedMock
  private SpaceAccessControl spaceAccessControl;
  @TestManagedMock
  private SimpleDocumentAccessControl documentAccessControl;
  @TestManagedMock
  private AttachmentService attachmentService;
  @TestedBean
  private OnlineFileServer servlet;

  private HttpServletRequest request;
  private HttpServletResponse response;
  private HttpSession session;

  @BeforeEach
  void setUp() throws Exception {
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
    session = mock(HttpSession.class);
    MainSessionController controller = mock(MainSessionController.class);
    when(controller.getUserId()).thenReturn(USER_ID);
    when(session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT)).thenReturn(
        controller);
    when(request.getSession(anyBoolean())).thenReturn(session);
    when(request.getSession()).thenReturn(session);
  }

  @Test
  void noFileIsSentWithoutAnAuthenticatedSession() throws Exception {
    when(request.getSession(anyBoolean())).thenReturn(null);
    when(request.getSession()).thenReturn(null);
    askForComponentFile(COMPONENT_ID);
    when(componentAccessControl.isUserAuthorized(any(), eq(COMPONENT_ID))).thenReturn(true);

    servlet.doGet(request, response);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(response, never()).setStatus(HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  void noFileIsSentWhenTheSessionHasNoUser() throws Exception {
    when(session.getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT)).thenReturn(null);
    askForComponentFile(COMPONENT_ID);
    when(componentAccessControl.isUserAuthorized(any(), eq(COMPONENT_ID))).thenReturn(true);

    servlet.doGet(request, response);

    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(response, never()).setStatus(HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  void aComponentFileIsNotSentToAUserWithoutAccessToTheComponent() throws Exception {
    askForComponentFile(COMPONENT_ID);
    when(componentAccessControl.isUserAuthorized(USER_ID, COMPONENT_ID)).thenReturn(false);

    servlet.doPost(request, response);

    verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
    verify(response, never()).setStatus(HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  void aComponentFileIsSentToAUserWithAccessToTheComponent() throws Exception {
    askForComponentFile(COMPONENT_ID);
    when(componentAccessControl.isUserAuthorized(USER_ID, COMPONENT_ID)).thenReturn(true);

    servlet.doPost(request, response);

    assertTheFileIsLookedFor();
  }

  @Test
  void noFileIsSentWhenNoComponentIsSpecified() throws Exception {
    askForComponentFile(null);

    servlet.doPost(request, response);

    verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
    verify(response, never()).setStatus(HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  void aSpaceLookFileIsNotSentToAUserWithoutAccessToTheSpace() throws Exception {
    askForComponentFile("Space12");
    when(spaceAccessControl.isUserAuthorized(USER_ID, "WA12")).thenReturn(false);
    when(spaceAccessControl.hasUserSpaceManagementAuthorization(USER_ID, "WA12")).thenReturn(false);

    servlet.doPost(request, response);

    verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
    verify(response, never()).setStatus(HttpServletResponse.SC_NOT_FOUND);
    verify(componentAccessControl, never()).isUserAuthorized(any(), any(String.class));
  }

  @Test
  void aSpaceLookFileIsSentToAUserWithAccessToTheSpace() throws Exception {
    askForComponentFile("Space12");
    when(spaceAccessControl.isUserAuthorized(USER_ID, "WA12")).thenReturn(true);

    servlet.doPost(request, response);

    assertTheFileIsLookedFor();
  }

  @Test
  void aSpaceLookFileIsSentToAManagerOfTheSpace() throws Exception {
    askForComponentFile("SpaceWA12");
    when(spaceAccessControl.isUserAuthorized(USER_ID, "WA12")).thenReturn(false);
    when(spaceAccessControl.hasUserSpaceManagementAuthorization(USER_ID, "WA12")).thenReturn(true);

    servlet.doPost(request, response);

    assertTheFileIsLookedFor();
  }

  @Test
  void anAttachmentIsNotSentToAUserNotAllowedToDownloadIt() throws Exception {
    SimpleDocument attachment = anAttachmentIn("kmelia99", "file.pdf");
    when(request.getParameter("attachmentId")).thenReturn(ATTACHMENT_ID);
    when(request.getParameter("ComponentId")).thenReturn("kmelia99");
    when(attachmentService.searchDocumentById(any(SimpleDocumentPK.class), any())).thenReturn(
        attachment);
    when(documentAccessControl.isUserAuthorized(eq(USER_ID), eq(attachment),
        any(AccessControlContext.class))).thenReturn(false);

    servlet.doPost(request, response);

    verify(attachmentService).searchDocumentById(
        argThat(pk -> pk.getId().equals(ATTACHMENT_ID) && "kmelia99".equals(pk.getInstanceId())),
        any());
    verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
    verify(response, never()).setStatus(HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  void anAttachmentOfAnotherComponentIsNotSentEvenIfTheRequestedOneIsAccessible()
      throws Exception {
    // the attachment is in another component than the one given in the request: only the access
    // rights on the attachment itself matter
    SimpleDocument attachment = anAttachmentIn("kmelia99", "file.pdf");
    when(request.getParameter("attachmentId")).thenReturn(ATTACHMENT_ID);
    when(request.getParameter("ComponentId")).thenReturn(COMPONENT_ID);
    when(attachmentService.searchDocumentById(any(SimpleDocumentPK.class), any())).thenReturn(
        attachment);
    when(componentAccessControl.isUserAuthorized(USER_ID, COMPONENT_ID)).thenReturn(true);
    when(documentAccessControl.isUserAuthorized(eq(USER_ID), eq(attachment),
        any(AccessControlContext.class))).thenReturn(false);

    servlet.doPost(request, response);

    verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
    verify(response, never()).setStatus(HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  void anAttachmentIsSentToAUserAllowedToDownloadIt() throws Exception {
    SimpleDocument attachment = anAttachmentIn("kmelia99", "file.pdf");
    when(request.getParameter("attachmentId")).thenReturn(ATTACHMENT_ID);
    when(request.getParameter("ComponentId")).thenReturn("kmelia99");
    when(attachmentService.searchDocumentById(any(SimpleDocumentPK.class), any())).thenReturn(
        attachment);
    when(documentAccessControl.isUserAuthorized(eq(USER_ID), eq(attachment),
        argThat(c -> c.getOperations().contains(AccessControlOperation.DOWNLOAD)))).thenReturn(
        true);

    servlet.doPost(request, response);

    assertTheFileIsLookedFor();
  }

  @Test
  void aVersionIsNotSentToAUserNotAllowedToDownloadIt() throws Exception {
    SimpleDocument version = anAttachmentIn("kmelia99", "version.odt");
    when(request.getParameter("DocumentId")).thenReturn("8");
    when(request.getParameter("VersionId")).thenReturn(VERSION_ID);
    when(request.getParameter("ComponentId")).thenReturn("kmelia99");
    when(attachmentService.searchDocumentById(any(SimpleDocumentPK.class), any())).thenReturn(
        version);
    when(documentAccessControl.isUserAuthorized(eq(USER_ID), eq(version),
        any(AccessControlContext.class))).thenReturn(false);

    servlet.doPost(request, response);

    verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
    verify(response, never()).setStatus(HttpServletResponse.SC_NOT_FOUND);
  }

  @Test
  void aVersionIsSentToAUserAllowedToDownloadIt() throws Exception {
    SimpleDocument version = anAttachmentIn("kmelia99", "version.odt");
    when(request.getParameter("DocumentId")).thenReturn("8");
    when(request.getParameter("VersionId")).thenReturn(VERSION_ID);
    when(request.getParameter("ComponentId")).thenReturn("kmelia99");
    when(attachmentService.searchDocumentById(any(SimpleDocumentPK.class), any())).thenReturn(
        version);
    when(documentAccessControl.isUserAuthorized(eq(USER_ID), eq(version),
        any(AccessControlContext.class))).thenReturn(true);

    servlet.doPost(request, response);

    assertTheFileIsLookedFor();
  }

  private void askForComponentFile(final String componentId) {
    when(request.getParameter("ComponentId")).thenReturn(componentId);
    when(request.getParameter("SourceFile")).thenReturn("wallpaper.jpg");
    when(request.getParameter("MimeType")).thenReturn("image/jpeg");
    when(request.getParameter("Directory")).thenReturn("look");
  }

  private SimpleDocument anAttachmentIn(final String instanceId, final String fileName) {
    SimpleDocument document = mock(SimpleDocument.class);
    when(document.getInstanceId()).thenReturn(instanceId);
    when(document.getContentType()).thenReturn("application/octet-stream");
    when(document.getAttachmentPath()).thenReturn("/tmp/" + instanceId + "/" + fileName);
    return document;
  }

  /**
   * The access is granted: the file is then looked for, and as it doesn't exist, a not found
   * status is sent back.
   */
  private void assertTheFileIsLookedFor() {
    verify(response, never()).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    verify(response, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
    verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND);
  }
}
