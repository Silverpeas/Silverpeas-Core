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
package org.silverpeas.attachment;

import com.stratelia.webactiv.beans.admin.ComponentInst;
import org.silverpeas.admin.component.notification.ComponentInstanceEvent;
import org.silverpeas.notification.CDIResourceEventListener;
import org.silverpeas.notification.ResourceEvent;
import org.silverpeas.util.StringUtil;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.RepositoryException;

/**
 * Clean the JCR repository for all documents concerning the specified component instance
 * identifier.
 *
 * @author ehugonnet
 */
@Singleton
public class ComponentInstanceReferenceInJCRRemover
    extends CDIResourceEventListener<ComponentInstanceEvent> {

  @Inject
  private AttachmentService attachmentService;

  @Override
  public void onDeletion(final ComponentInstanceEvent event) throws Exception {
    ComponentInst component = event.getResource();
    String id = component.getId();
    if (StringUtil.isInteger(id)) {
      id = component.getName() + component.getId();
    }
    attachmentService.deleteAllAttachments(id);
  }
}
