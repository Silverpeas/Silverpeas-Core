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

import javax.inject.Inject;
import javax.inject.Named;

import org.silverpeas.viewer.web.mock.AttachmentServiceMockWrapper;
import org.silverpeas.viewer.web.mock.PreviewServiceMockWrapper;

import com.silverpeas.web.TestResources;

/**
 * Resources required by all the unit tests on the comment web resource.
 */
@Named(TestResources.TEST_RESOURCES_NAME)
public class ViewerTestResources extends TestResources {

  @Inject
  @Named("attachmentService")
  private AttachmentServiceMockWrapper attachmentServiceMockWrapper;

  @Inject
  @Named("previewService")
  private PreviewServiceMockWrapper previewServiceMockWrapper;

  public static final String JAVA_PACKAGE = "org.silverpeas.viewer.web";
  public static final String SPRING_CONTEXT = "spring-viewer-webservice.xml";

  public AttachmentServiceMockWrapper getAttachmentServiceMock() {
    return attachmentServiceMockWrapper;
  }

  public PreviewServiceMockWrapper getPreviewServiceMockWrapper() {
    return previewServiceMockWrapper;
  }
}
