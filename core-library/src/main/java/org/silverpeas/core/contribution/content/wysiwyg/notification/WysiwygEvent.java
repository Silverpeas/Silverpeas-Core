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

package org.silverpeas.core.contribution.content.wysiwyg.notification;

import org.silverpeas.core.notification.system.ResourceEvent;
import org.silverpeas.core.notification.system.AbstractResourceEvent;
import org.silverpeas.core.contribution.content.wysiwyg.WysiwygContent;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * An event about a change in the life-cycle of the specified WYSIWYG content. A change can be
 * its creation, an update or a deletion.
 * @author mmoquillon
 */
@XmlRootElement
public class WysiwygEvent extends AbstractResourceEvent<WysiwygContent> {

  protected WysiwygEvent() {
    super();
  }

  /**
   * @see AbstractResourceEvent ( ResourceEvent.Type, Object[])
   */
  public WysiwygEvent(final Type type, final WysiwygContent... wysiwyg) {
    super(type, wysiwyg);
  }

}
