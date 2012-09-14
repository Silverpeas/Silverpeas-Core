/**
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
 * FLOSS exception.  You should have received a copy of the text describing
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

package org.silverpeas.attachment.notification;

import com.silverpeas.notification.NotificationSource;
import com.silverpeas.notification.SilverpeasNotification;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;

/**
 * Notification about the deletion of an attachment in a given component instance.
 */
public class AttachmentDeletionNotification extends SilverpeasNotification {

  private static final long serialVersionUID = 3354035649186264026L;

  protected AttachmentDeletionNotification(final AttachmentPK attachment) {
    super(new NotificationSource().withComponentInstanceId(attachment.getInstanceId()), attachment);
  }

  public AttachmentPK getAttachmentPK() {
    return (AttachmentPK) getObject();
  }
}
