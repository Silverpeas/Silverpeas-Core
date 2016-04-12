/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.webapi.viewer;

import com.silverpeas.web.ResourceGettingTest;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.contribution.attachment.SimpleDocumentService;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.web.test.WarBuilder4WebCore;
import org.silverpeas.core.viewer.service.DefaultPreviewService;
import org.silverpeas.core.viewer.model.Preview;
import org.silverpeas.core.viewer.service.ViewerContext;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;
import java.io.File;

import static javax.interceptor.Interceptor.Priority.APPLICATION;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests on the comment getting by the CommentResource web service.
 *
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class PreviewGettingTest extends ResourceGettingTest {

  private String tokenKey;
  private Preview expected;
  private ComponentInst component;

  private static String ATTACHMENT_ID = "7";
  private static String ATTACHMENT_ID_DOESNT_EXISTS = "8";

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4WebCore.onWarForTestClass(DocumentViewGettingTest.class)
        .addRESTWebServiceEnvironment().testFocusedOn(warBuilder -> {
          warBuilder.addPackages(true, "org.silverpeas.core.webapi.viewer");
          warBuilder.addAsResource("org/silverpeas/viewer/viewer.properties");
        }).build();
  }

  @Singleton
  @Alternative
  @Priority(APPLICATION + 10)
  public static class StubbedAttachmentService extends SimpleDocumentService {
    @Override
    public SimpleDocument searchDocumentById(final SimpleDocumentPK attachmentPK,
        final String lang) {
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
  }

  @Singleton
  @Alternative
  @Priority(APPLICATION + 10)
  public static class StubbedPreviewService extends DefaultPreviewService {
    @Override
    public Preview getPreview(final ViewerContext viewerContext) {
      final String originalFileName = viewerContext.getOriginalFileName();
      final File physicalFile = viewerContext.getOriginalSourceFile();
      final Preview preview = mock(Preview.class);
      when(preview.getOriginalFileName()).thenReturn(originalFileName);
      when(preview.getURLAsString()).thenReturn("/URL/" + physicalFile.getName());
      return preview;
    }
  }

  @Before
  public void prepareTestResources() {
    component = getSilverpeasEnvironmentTest().getDummyPublicComponent();
    tokenKey = getTokenKeyOf(getSilverpeasEnvironmentTest().createDefaultUser());
    expected = PreviewBuilder.getPreviewBuilder().buildFileName("previewId", "originalFileName7");
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
  public String getTokenKey() {
    return tokenKey;
  }

  @Override
  public Class<?> getWebEntityClass() {
    return PreviewEntity.class;
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{component.getId()};
  }
}
