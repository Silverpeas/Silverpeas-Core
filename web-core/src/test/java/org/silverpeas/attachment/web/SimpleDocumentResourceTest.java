/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
package org.silverpeas.attachment.web;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Date;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import org.apache.commons.io.FileUtils;
import org.apache.jackrabbit.api.JackrabbitRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.silverpeas.attachment.AttachmentService;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.attachment.model.UnlockContext;
import org.silverpeas.attachment.model.UnlockOption;
import org.silverpeas.util.Charsets;

import com.silverpeas.jcrutil.RandomGenerator;
import com.silverpeas.jndi.SimpleMemoryContextFactory;
import com.silverpeas.util.MimeTypes;
import com.silverpeas.util.PathTestUtil;
import com.silverpeas.web.ResourceGettingTest;

import com.stratelia.webactiv.beans.admin.UserDetail;

import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.silverpeas.attachment.web.SimpleDocumentTestResource.*;

/**
 *
 * @author ehugonnet
 */
public class SimpleDocumentResourceTest extends ResourceGettingTest<SimpleDocumentTestResource> {

  private UserDetail user;
  private String sessionKey;
  private Date creationDate = RandomGenerator.getOutdatedCalendar().getTime();

  public SimpleDocumentResourceTest() {
    super("org.silverpeas.attachment.web", "spring-jcr-webservice.xml");
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

  @Before
  public void prepareTestResources() {
    user = aUser();
    sessionKey = authenticate(user);
  }

  private String getDocumentUri(String id, String lang) {
    return RESOURCE_PATH + id + '/' + lang;
  }

  private String getTranslationsUri(String id) {
    return RESOURCE_PATH + id + '/' + "translations";
  }

  /**
   * Test of getDocument method, of class SimpleDocumentRessource.
   */
  @Test
  public void testGetDocument() {
    SimpleDocument document = new SimpleDocument(new SimpleDocumentPK(DOCUMENT_ID, INSTANCE_ID),
        "18", 10, false,
        new SimpleAttachment("test.pdf", "fr", "Test", "Ceci est un test.", 500L,
        MimeTypes.PDF_MIME_TYPE, USER_ID_IN_TEST, creationDate, null));
    AttachmentService service = mock(AttachmentService.class);
    when(service.searchAttachmentById(eq(new SimpleDocumentPK(DOCUMENT_ID)), anyString())).
        thenReturn(document);
    getTestResources().setAttachmentService(service);
    SimpleDocumentEntity result = getAt(getDocumentUri(DOCUMENT_ID, null),
        SimpleDocumentEntity.class);
    assertThat(result, SimpleDocumentEntityMatcher.matches(document));
  }

  /**
   * Test of getDocumentTanslations method, of class SimpleDocumentRessource.
   */
  @Test
  public void testGetDocumentTanslations() {
    SimpleDocument document_fr = new SimpleDocument(new SimpleDocumentPK(DOCUMENT_ID, INSTANCE_ID),
        "18", 10, false, new SimpleAttachment("Mon Test.pdf", "fr", "Test français",
        "Ceci est un test.", 500L, MimeTypes.PDF_MIME_TYPE, USER_ID_IN_TEST, creationDate, null));
    SimpleDocument document_en = new SimpleDocument(new SimpleDocumentPK(DOCUMENT_ID, INSTANCE_ID),
        "18", 5, false, new SimpleAttachment("My Test.pdf", "en", "English Test", "This is a test.",
        600L, MimeTypes.PDF_MIME_TYPE, USER_ID_IN_TEST, creationDate, null));
    AttachmentService service = mock(AttachmentService.class);
    when(service.searchAttachmentById(eq(new SimpleDocumentPK(DOCUMENT_ID)), eq("fr"))).thenReturn(
        document_fr);
    when(service.searchAttachmentById(eq(new SimpleDocumentPK(DOCUMENT_ID)), eq("de"))).thenReturn(
        document_fr);
    when(service.searchAttachmentById(eq(new SimpleDocumentPK(DOCUMENT_ID)), eq("en"))).thenReturn(
        document_en);
    getTestResources().setAttachmentService(service);
    SimpleDocumentEntity[] result = getAt(getTranslationsUri(DOCUMENT_ID),
        SimpleDocumentEntity[].class);
    assertThat(result, is(notNullValue()));
    assertThat(result.length, is(2));
    assertThat(result[0], SimpleDocumentEntityMatcher.matches(document_fr));
    assertThat(result[1], SimpleDocumentEntityMatcher.matches(document_en));
  }

  /**
   * Test of getFileContent method, of class SimpleDocumentRessource.
   */
  @Test
  public void testGetFileContent() {
    AttachmentService service = new MockBinaryAttachmentService();
    getTestResources().setAttachmentService(service);
    String language = "fr";
    SimpleDocumentResource instance = new SimpleDocumentResource();
    Response response = instance.getFileContent(language);
    assertThat(response, is(notNullValue()));
    assertThat(response.getStatus(), is(Status.OK.getStatusCode()));
    assertThat(response.getMetadata(), is(notNullValue()));
    assertThat((MediaType) response.getMetadata().getFirst(HttpHeaders.CONTENT_TYPE), is(MediaType.
        valueOf(MimeTypes.PDF_MIME_TYPE)));
    assertThat((Long) response.getMetadata().getFirst(HttpHeaders.CONTENT_LENGTH), is(29L));
    assertThat((String) response.getMetadata().getFirst("content-disposition"), is(
        "attachment;filename=Test.pdf"));
  }

  @Test
  public void testUpdateDocumentWithContent() {
    SimpleDocument document = new SimpleDocument(new SimpleDocumentPK(DOCUMENT_ID, INSTANCE_ID),
        "18", 10, false,
        new SimpleAttachment("test.pdf", "fr", "Test", "Ceci est un test.", 500L,
        MimeTypes.PDF_MIME_TYPE, USER_ID_IN_TEST, creationDate, null));
    AttachmentService service = mock(AttachmentService.class);
    when(service.searchAttachmentById(eq(new SimpleDocumentPK(DOCUMENT_ID)), anyString())).
        thenReturn(document);
    getTestResources().setAttachmentService(service);
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
    WebResource webResource = resource();
    SimpleDocumentEntity result = webResource.path(RESOURCE_PATH + DOCUMENT_ID).header(
        HTTP_SESSIONKEY,
        getSessionKey()).type(MULTIPART_FORM_DATA).post(SimpleDocumentEntity.class, form);
    assertThat(result, is(notNullValue()));
    assertThat(result.getTitle(), is("Upload test"));
    assertThat(result.getDescription(),
        is("This test is trying to simulate the update of a content"));
    assertThat(result.getLang(), is("en"));
  }

  @Test
  public void testUpdateDocumentWithoutContent() {
    SimpleDocument document = new SimpleDocument(new SimpleDocumentPK(DOCUMENT_ID, INSTANCE_ID),
        "18", 10, false, new SimpleAttachment("test.pdf", "fr", "Test", "Ceci est un test.", 500L,
        MimeTypes.PDF_MIME_TYPE, USER_ID_IN_TEST, creationDate, null));
    AttachmentService service = mock(AttachmentService.class);
    when(service.searchAttachmentById(eq(new SimpleDocumentPK(DOCUMENT_ID)), anyString())).
        thenReturn(document);
    getTestResources().setAttachmentService(service);
    FormDataMultiPart form = new FormDataMultiPart();
    form.field("fileName", "/Shared/marketing/my_test_document.txt");
    form.field("fileLang", "en");
    form.field("fileTitle", "Upload test");
    form.field("fileDescription", "This test is trying to simulate the update of a content");
    WebResource webResource = resource();
    SimpleDocumentEntity result = webResource.path(RESOURCE_PATH + DOCUMENT_ID).header(
        HTTP_SESSIONKEY,
        getSessionKey()).type(MULTIPART_FORM_DATA).post(SimpleDocumentEntity.class, form);
    assertThat(result, is(notNullValue()));
    assertThat(result.getTitle(), is("Upload test"));
    assertThat(result.getDescription(),
        is("This test is trying to simulate the update of a content"));
    assertThat(result.getLang(), is("en"));
  }

  @Test
  public void testLockDocument() {
    String lang = "fr";
    SimpleDocument document = new SimpleDocument(new SimpleDocumentPK(DOCUMENT_ID, INSTANCE_ID),
        "18", 10, false, new SimpleAttachment("test.pdf", lang, "Test", "Ceci est un test.", 500L,
        MimeTypes.PDF_MIME_TYPE, USER_ID_IN_TEST, creationDate, null));
    AttachmentService service = mock(AttachmentService.class);
    when(service.searchAttachmentById(eq(new SimpleDocumentPK(DOCUMENT_ID)), eq(lang))).
        thenReturn(document);
    when(service.lock(eq(DOCUMENT_ID), anyString(), eq(lang))).thenReturn(true);
    getTestResources().setAttachmentService(service);
    WebResource webResource = resource();
    String result = webResource.path(RESOURCE_PATH + DOCUMENT_ID + "/lock").header(
        HTTP_SESSIONKEY, getSessionKey()).put(String.class);
    assertThat(result, is(notNullValue()));
    assertThat(result, is("{\"status\":true}"));
  }

  @Test
  @Ignore
  public void testUnlockDocument() {
    String lang = "fr";
    String comment = "Déverrouillage";
    SimpleDocument document = new SimpleDocument(new SimpleDocumentPK(DOCUMENT_ID, INSTANCE_ID),
        "18", 10, false, new SimpleAttachment("test.pdf", lang, "Test", "Ceci est un test.", 500L,
        MimeTypes.PDF_MIME_TYPE, USER_ID_IN_TEST, creationDate, null));
    AttachmentService service = mock(AttachmentService.class);
    when(service.searchAttachmentById(eq(new SimpleDocumentPK(DOCUMENT_ID)), eq(lang))).
        thenReturn(document);
    UnlockContext unlockContext = new UnlockContext(DOCUMENT_ID, user.getId(), lang, comment);
    unlockContext.addOption(UnlockOption.FORCE);
    when(service.unlock(eq(unlockContext))).thenReturn(true);
    getTestResources().setAttachmentService(service);
    Form form = new Form();
    form.putSingle("comment", comment);
    form.putSingle("force", true);
    WebResource webResource = resource();
    String result = webResource.path(RESOURCE_PATH + DOCUMENT_ID + "/unlock").header(
        HTTP_SESSIONKEY, getSessionKey()).post(String.class, form);
    assertThat(result, is(notNullValue()));
    assertThat(result, is("{\"status\":true}"));
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{INSTANCE_ID};
  }

  @Override
  public String aResourceURI() {
    return getDocumentUri(DOCUMENT_ID, "fr");
  }

  @Override
  public String anUnexistingResourceURI() {
    AttachmentService service = mock(AttachmentService.class);
    getTestResources().setAttachmentService(service);
    return getDocumentUri("d3adb33f-fac3-bab3-caf3-bab3caf3bab3", "fr");
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
}
