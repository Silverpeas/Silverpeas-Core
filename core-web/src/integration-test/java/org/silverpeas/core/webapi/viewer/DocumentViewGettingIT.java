/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.webapi.viewer;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.viewer.model.DocumentView;
import org.silverpeas.core.viewer.service.DefaultViewService;
import org.silverpeas.core.viewer.service.ViewerContext;
import org.silverpeas.core.web.test.WarBuilder4WebCore;
import org.silverpeas.core.webapi.attachment.SimpleDocumentEmbedMediaViewProvider;
import org.silverpeas.web.ResourceGettingTest;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Paths;

import static javax.interceptor.Interceptor.Priority.APPLICATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests on the comment getting by the CommentResource web service.
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class DocumentViewGettingIT extends ResourceGettingTest {

  private String apiToken;
  private DocumentView expected;
  private ComponentInst component;

  static String ATTACHMENT_ID = "7";

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4WebCore.onWarForTestClass(DocumentViewGettingIT.class)
        .addRESTWebServiceEnvironment().testFocusedOn(warBuilder -> {
          warBuilder.addClasses(SimpleDocumentEmbedMediaViewProvider.class);
          warBuilder.addPackages(true, "org.silverpeas.core.webapi.viewer");
          warBuilder.addAsResource("org/silverpeas/viewer/viewer.properties");
        }).build();
  }

  @Before
  public void setup() throws Exception {
    ServiceProvider.getSingleton(SimpleDocumentEmbedMediaViewProvider.class).init();
  }

  @Singleton
  @Alternative
  @Priority(APPLICATION + 10)
  public static class StubbedDocumentViewService extends DefaultViewService {
    @Override
    public DocumentView getDocumentView(final ViewerContext viewerContext) {
      final String originalFileName = viewerContext.getOriginalFileName();
      final File physicalFile = viewerContext.getOriginalSourceFile();
      final DocumentView documentView = mock(DocumentView.class);
      when(documentView.getDocumentId()).thenReturn(ATTACHMENT_ID);
      when(documentView.getOriginalFileName()).thenReturn(originalFileName);
      when(documentView.getURLAsString()).thenReturn("/URL/" + physicalFile.getName());
      when(documentView.getServerFilePath()).thenReturn(Paths.get(originalFileName));
      when(documentView.getLanguage()).thenReturn("fr");
      return documentView;
    }

    @Override
    public void removeDocumentView(final ViewerContext viewerContext) {

    }
  }

  @Before
  public void prepareTestResources() {
    component = getSilverpeasEnvironmentTest().getDummyPublicComponent();
    apiToken = getTokenKeyOf(getSilverpeasEnvironmentTest().createDefaultUser());
    expected = DocumentViewBuilder.getDocumentViewBuilder()
        .buildFileName("documentViewId", "originalFileName7");
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
    return "view/attachment/" + attachmentId;
  }

  @Override
  public String anUnexistingResourceURI() {
    return aResourceURI(StubbedAttachmentService.ATTACHMENT_ID_DOESNT_EXISTS);
  }

  @Override
  public DocumentView aResource() {
    return expected;
  }

  @Override
  public String getAPITokenValue() {
    return apiToken;
  }

  @Override
  public Class<?> getWebEntityClass() {
    return DocumentViewEntity.class;
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{component.getId()};
  }
}
