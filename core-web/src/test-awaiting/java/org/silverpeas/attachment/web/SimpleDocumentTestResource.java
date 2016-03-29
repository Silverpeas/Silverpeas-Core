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

import javax.inject.Inject;
import javax.inject.Named;

import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.mock.SimpleDocumentServiceWrapper;

import com.silverpeas.web.TestResources;

/**
 *
 * @author ehugonnet
 */
@Named(TestResources.TEST_RESOURCES_NAME)
public class SimpleDocumentTestResource extends TestResources {

  public static final String INSTANCE_ID = "kmelia36";
  public static final String DOCUMENT_ID = "deadbeef-face-babe-cafe-babecafebabe";
  public static final String RESOURCE_PATH = "documents/" + INSTANCE_ID + "/document/";

  @Inject
  AttachmentService attachmentService;

  public void setAttachmentService(AttachmentService service) {
    ((SimpleDocumentServiceWrapper)attachmentService).setRealService(service);
  }
}
