/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.attachment.notification;

import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.notification.AbstractResourceEvent;
import org.silverpeas.notification.ResourceEvent;
import org.silverpeas.util.JSONCodec;
import org.silverpeas.util.exception.DecodingException;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/** An event about the life-cycle change of an attachment in Silverpeas.
 * It represents an event occurring when an attachment, represented by a simple document, is either
 * created, or updated or deleted.
 * @author mmoquillon
 */
@XmlRootElement
public class AttachmentEvent extends AbstractResourceEvent<AttachmentRef> {

  private static final long serialVersionUID = 7484300357092142528L;

  protected AttachmentEvent() {
    super();
  }

  /**
   * @see org.silverpeas.notification.AbstractResourceEvent#AbstractResourceEvent(org.silverpeas.notification.ResourceEvent.Type, Object[])
   */
  public AttachmentEvent(final Type type, final SimpleDocument... document) {
    super(type, toAttachmentRefs(document));
  }

  private static AttachmentRef[] toAttachmentRefs(SimpleDocument... documents) {
    AttachmentRef[] refs = new AttachmentRef[documents.length];
    for (int i = 0; i < documents.length; i++) {
      refs[i] = new AttachmentRef(documents[i]);
    }
    return refs;
  }
}
