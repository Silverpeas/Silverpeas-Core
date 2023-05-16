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
import org.silverpeas.core.viewer.model.Preview;
import org.silverpeas.core.viewer.service.DefaultPreviewService;
import org.silverpeas.core.viewer.service.ViewerContext;
import org.silverpeas.core.web.test.WarBuilder4WebCore;
import org.silverpeas.core.webapi.attachment.SimpleDocumentEmbedMediaViewProvider;
import org.silverpeas.web.ResourceGettingTest;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;
import java.io.File;

import static javax.interceptor.Interceptor.Priority.APPLICATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * Tests on the comment getting by the CommentResource web service.
 *
 * @author Yohann Chastagnier
 */
@RunWith(Arquillian.class)
public class PreviewGettingIT extends ResourceGettingTest {

    private String tokenKey;
    private Preview expected;
    private ComponentInst component;

    private static final String ATTACHMENT_ID = "7";

    @Deployment
    public static Archive<?> createTestArchive() {
        return WarBuilder4WebCore.onWarForTestClass(PreviewGettingIT.class)
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
    public static class StubbedPreviewService extends DefaultPreviewService {
        @Override
        public Preview getPreview(final ViewerContext viewerContext) {
            final String originalFileName = viewerContext.getOriginalFileName();
            final File physicalFile = viewerContext.getOriginalSourceFile();
            return PreviewBuilder.getPreviewBuilder()
                    .buildFileName(physicalFile.getName(), originalFileName);
        }

        @Override
        public void removePreview(final ViewerContext viewerContext) {

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
        return "preview/attachment/" + attachmentId;
    }

    @Override
    public String anUnexistingResourceURI() {
        return aResourceURI(StubbedAttachmentService.ATTACHMENT_ID_DOESNT_EXISTS);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Preview aResource() {
        return expected;
    }

    @Override
    public String getAPITokenValue() {
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
