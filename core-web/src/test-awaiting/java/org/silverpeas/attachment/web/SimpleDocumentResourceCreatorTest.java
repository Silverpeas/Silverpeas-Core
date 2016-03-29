/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.contribution.attachment.web;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import org.apache.commons.io.FileUtils;
import org.apache.jackrabbit.api.JackrabbitRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.importExport.versioning.DocumentVersion;
import org.silverpeas.util.Charsets;

import com.silverpeas.jcrutil.RandomGenerator;
import com.silverpeas.jndi.SimpleMemoryContextFactory;
import org.silverpeas.util.ForeignPK;
import org.silverpeas.util.MimeTypes;
import com.silverpeas.util.PathTestUtil;
import com.silverpeas.web.ResourceGettingTest;

import com.stratelia.webactiv.beans.admin.UserDetail;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.silverpeas.core.contribution.attachment.web.SimpleDocumentTestResource.*;
import org.silverpeas.util.URLUtils;

/**
 *
 * @author ehugonnet
 */
public class SimpleDocumentResourceCreatorTest extends ResourceGettingTest<SimpleDocumentTestResource> {

  private UserDetail user;
  private String sessionKey;
  private Date creationDate = RandomGenerator.getOutdatedCalendar().getTime();

  public SimpleDocumentResourceCreatorTest() {
    super("org.silverpeas.core.contribution.attachment.web", "spring-jcr-webservice.xml");
  }

  @Before
  public void prepareTestResources() {
    user = aUser();
    sessionKey = authenticate(user);
  }

  @After
  public void testCleanUp() throws Exception {
    JackrabbitRepository repository = getTestResources().getApplicationContext().
        getBean("repository", JackrabbitRepository.class);
    repository.shutdown();
    SimpleMemoryContextFactory.tearDownAsInitialContext();
    FileUtils.deleteQuietly(new File(PathTestUtil.TARGET_DIR + "tmp" + File.separatorChar
        + "temp_jackrabbit"));
  }

  @Ignore
  @Override
  public void gettingAnUnexistingResource() {
  }

  /**
   * Test of createDocument method, of class SimpleDocumentResourceCreator.
   */
  @Test
  public void testCreateDocument() throws ParseException {
    SimpleDocument document = new SimpleDocument(new SimpleDocumentPK(DOCUMENT_ID, INSTANCE_ID),
        "18", 10, false, new SimpleAttachment("test.pdf", "fr", "Test", "Ceci est un test.", 500L,
        MimeTypes.PDF_MIME_TYPE, USER_ID_IN_TEST, creationDate, null));
    AttachmentService service = mock(AttachmentService.class);
    when(service.findExistingDocument(any(SimpleDocumentPK.class), eq("test.pdf"), eq(new ForeignPK(
        "18")), eq("fr"))).thenReturn(document);
    when(service.createAttachment(any(SimpleDocument.class), any(InputStream.class), any(
        Boolean.class), any(Boolean.class))).thenReturn(document);
    getTestResources().setAttachmentService(service);
    FormDataMultiPart form = new FormDataMultiPart();
    form.field("filename", "test.pdf");
    form.field("fileLang", "fr");
    form.field("fileTitle", "Test");
    form.field("fileDescription", "Ceci est un test.");
    String content = "This is a binary content";
    FormDataBodyPart fdp = new FormDataBodyPart(FormDataContentDisposition.name("file_upload").
        fileName("test.pdf").size(content.getBytes(Charsets.UTF_8).length).build(),
        new ByteArrayInputStream(content.getBytes(Charsets.UTF_8)),
        MediaType.APPLICATION_OCTET_STREAM_TYPE);
    form.bodyPart(fdp);
    SimpleDocumentEntity result = resource().path(aResourceURI() + URLUtils.encodePathSegment(
        "test.pdf")).header(HTTP_SESSIONKEY, getSessionKey()).type(MULTIPART_FORM_DATA).accept(
        APPLICATION_JSON_TYPE).post(SimpleDocumentEntity.class, form);
    assertThat(result, is(notNullValue()));
    assertThat(result.getTitle(), is("Test"));
    assertThat(result.getDescription(), is("Ceci est un test."));
    assertThat(result.getLang(), is("fr"));
  }

  /**
   * Test of createDocument method, of class SimpleDocumentResourceCreator.
   */
  @Test
  public void testCreateExistingDocument() throws ParseException {
    SimpleDocument document = new SimpleDocument(new SimpleDocumentPK(DOCUMENT_ID, INSTANCE_ID),
        "18", 10, false, new SimpleAttachment("test.pdf", "fr", "Test", "Ceci est un test.", 500L,
        MimeTypes.PDF_MIME_TYPE, USER_ID_IN_TEST, creationDate, null));
    AttachmentService service = mock(AttachmentService.class);
    when(service.
        findExistingDocument(any(SimpleDocumentPK.class), anyString(), any(ForeignPK.class),
        anyString())).thenReturn(document);
    when(service.createAttachment(any(SimpleDocument.class), any(InputStream.class), any(
        Boolean.class), any(Boolean.class))).thenReturn(document);
    getTestResources().setAttachmentService(service);
    FormDataMultiPart form = new FormDataMultiPart();
    form.field("filename", "test.pdf");
    form.field("versionType", "" + DocumentVersion.TYPE_DEFAULT_VERSION);
    form.field("fileLang", "fr");
    form.field("fileTitle", "Upload test");
    form.field("fileDescription", "This test is trying to simulate the update of a content");
    String content = "This is a binary content";
    FormDataBodyPart fdp = new FormDataBodyPart(FormDataContentDisposition.name("file_upload").
        fileName("test.pdf").size(content.getBytes(Charsets.UTF_8).length).build(),
        new ByteArrayInputStream(content.getBytes(Charsets.UTF_8)),
        MediaType.APPLICATION_OCTET_STREAM_TYPE);
    form.bodyPart(fdp);
    SimpleDocumentEntity result = resource().path(aResourceURI() + URLUtils.encodePathSegment(
        "test.pdf")).header(HTTP_SESSIONKEY, getSessionKey()).type(MULTIPART_FORM_DATA).accept(
        APPLICATION_JSON_TYPE).post(SimpleDocumentEntity.class, form);
    assertThat(result, is(notNullValue()));
    assertThat(result.getTitle(), is("Upload test"));
    assertThat(result.getDescription(),
        is("This test is trying to simulate the update of a content"));
    assertThat(result.getLang(), is("fr"));
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{INSTANCE_ID};
  }

  @Override
  public String aResourceURI() {
    return RESOURCE_PATH + "create/";
  }

  @Override
  public String anUnexistingResourceURI() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public <T> T aResource() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public String getSessionKey() {
    return sessionKey;
  }

  @Override
  public Class<?> getWebEntityClass() {
    return SimpleDocumentEntity.class;
  }

  @Test
  @Override
  public void gettingAResourceByANonAuthenticatedUser() {
    try {
      FormDataMultiPart form = new FormDataMultiPart();
      form.field("fileName", "/Shared/marketing/my_test_document.txt");
      form.field("fileLang", "en");
      form.field("fileTitle", "Upload test");
      form.field("fileDescription", "This test is trying to simulate the update of a content");
      String content = "This is a binary content";
      FormDataBodyPart fdp = new FormDataBodyPart("content",
          new ByteArrayInputStream(content.getBytes(Charsets.UTF_8)),
          MediaType.APPLICATION_OCTET_STREAM_TYPE);
      form.bodyPart(fdp);
      resource().path(aResourceURI() + URLUtils.encodePathSegment("test.pdf")).type(
          MULTIPART_FORM_DATA).post(form);
      fail("A non authenticated user shouldn't access the resource");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.UNAUTHORIZED.getStatusCode();
      assertThat(receivedStatus, is(unauthorized));
    }
  }

  @Test
  @Override
  public void gettingAResourceWithAnExpiredSession() {
    try {
      FormDataMultiPart form = new FormDataMultiPart();
      form.field("fileName", "/Shared/marketing/my_test_document.txt");
      form.field("fileLang", "en");
      form.field("fileTitle", "Upload test");
      form.field("fileDescription", "This test is trying to simulate the update of a content");
      String content = "This is a binary content";
      FormDataBodyPart fdp = new FormDataBodyPart("content",
          new ByteArrayInputStream(content.getBytes(Charsets.UTF_8)),
          MediaType.APPLICATION_OCTET_STREAM_TYPE);
      form.bodyPart(fdp);
      resource().path(aResourceURI() + URLUtils.encodePathSegment("test.pdf")).type(
          MULTIPART_FORM_DATA).header(HTTP_SESSIONKEY, UUID.randomUUID().toString()).post(form);
      fail("A non authenticated user shouldn't access the resource");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.UNAUTHORIZED.getStatusCode();
      assertThat(receivedStatus, is(unauthorized));
    }
  }

  @Test
  @Override
  public void gettingAResourceByAnUnauthorizedUser() {
    denieAuthorizationToUsers();
    try {
      FormDataMultiPart form = new FormDataMultiPart();
      form.field("fileName", "/Shared/marketing/my_test_document.txt");
      form.field("fileLang", "en");
      form.field("fileTitle", "Upload test");
      form.field("fileDescription", "This test is trying to simulate the update of a content");
      String content = "This is a binary content";
      FormDataBodyPart fdp = new FormDataBodyPart("content",
          new ByteArrayInputStream(content.getBytes(Charsets.UTF_8)),
          MediaType.APPLICATION_OCTET_STREAM_TYPE);
      form.bodyPart(fdp);
      resource().path(aResourceURI() + URLUtils.encodePathSegment("test.pdf")).type(
          MULTIPART_FORM_DATA).header(HTTP_SESSIONKEY, getSessionKey()).post(form);
      fail("An unauthorized user shouldn't access the resource");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int forbidden = Status.FORBIDDEN.getStatusCode();
      assertThat(receivedStatus, is(forbidden));
    }
  }
}
