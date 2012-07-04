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
package org.silverpeas.attachment.web;

import java.io.File;
import java.util.Date;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import org.silverpeas.attachment.AttachmentService;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import com.silverpeas.jcrutil.RandomGenerator;
import com.silverpeas.jndi.SimpleMemoryContextFactory;
import com.silverpeas.util.MimeTypes;
import com.silverpeas.util.PathTestUtil;
import com.silverpeas.web.ResourceGettingTest;

import com.stratelia.webactiv.beans.admin.UserDetail;

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
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class SimpleDocumentResourceTest extends ResourceGettingTest<SimpleDocumentTestResource> {

  private UserDetail user;
  private String sessionKey;
  private Date creationDate = RandomGenerator.getOutdatedCalendar().getTime();

  public SimpleDocumentResourceTest() {
    super("org.silverpeas.attachment.web", "spring-jcr-webservice.xml");
  }
  
  @After
  public void generalCleanUp() throws Exception {
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
