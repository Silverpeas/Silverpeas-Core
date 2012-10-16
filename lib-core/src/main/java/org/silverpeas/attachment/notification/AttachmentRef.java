/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

import java.io.Serializable;
import org.silverpeas.attachment.model.SimpleDocument;

public class AttachmentRef implements Serializable {

  private static final long serialVersionUID = -3675788384425272201L;
  private String id;
  private String instanceId;
  private String foreignId;
  private long oldSilverpeasId;
  private String name;
  private boolean versioned;

  public AttachmentRef(SimpleDocument document) {
    this.id = document.getId();
    this.instanceId = document.getInstanceId();
    this.foreignId = document.getForeignId();
    this.oldSilverpeasId = document.getOldSilverpeasId();
    this.name = document.getFilename();
    this.versioned = document.isVersioned();
  }

  public String getId() {
    return id;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public String getForeignId() {
    return foreignId;
  }

  public long getOldSilverpeasId() {
    return oldSilverpeasId;
  }

  public String getName() {
    return name;
  }

  public boolean isVersioned() {
    return versioned;
  }
}
