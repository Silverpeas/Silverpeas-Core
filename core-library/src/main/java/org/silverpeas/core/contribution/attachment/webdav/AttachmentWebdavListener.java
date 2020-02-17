/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.contribution.attachment.webdav;

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.contribution.attachment.notification.AttachmentEvent;
import org.silverpeas.core.contribution.attachment.notification.AttachmentRef;
import org.silverpeas.core.notification.system.CDIResourceEventListener;
import org.silverpeas.core.wopi.WopiFileEditionManager;

/**
 * Handles the WOPI releasing on attachment manipulations.
 * @author silveryocha
 */
@Bean
public class AttachmentWebdavListener extends CDIResourceEventListener<AttachmentEvent> {


  @Override
  public void onUpdate(final AttachmentEvent event) {
    final AttachmentRef attachment = event.getTransition().getBefore();
    release(attachment);
  }

  @Override
  public void onUnlock(final AttachmentEvent event) {
    onUpdate(event);
  }

  @Override
  public void onRemoving(final AttachmentEvent event) {
    onUpdate(event);
  }

  @Override
  public void onDeletion(final AttachmentEvent event) {
    onUpdate(event);
  }

  private void release(final AttachmentRef attachment) {
    WopiFileEditionManager.get().revokeFile(new WebdavWopiFile(attachment.getId(), null));
  }
}
