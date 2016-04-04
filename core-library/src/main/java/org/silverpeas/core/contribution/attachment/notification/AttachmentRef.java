/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.contribution.attachment.notification;

import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.util.StringUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * It represents a reference to an attachment that was either created, updated or deleted. The
 * attachment is represented by a {@code org.silverpeas.core.contribution.attachment.model.SimpleDocument} instance.
 * Instead of a such instance to be transmitted within a notification, it is a reference to it,
 * more simple and independent of other specific properties.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AttachmentRef implements Serializable {

  private static final long serialVersionUID = -3675788384425272201L;

  @XmlElement
  private String id;
  @XmlElement
  private String instanceId;
  @XmlElement
  private String foreignId;
  @XmlElement
  private long oldSilverpeasId;
  @XmlElement
  private String name;
  @XmlElement
  private boolean versioned;
  @XmlElement
  private String userId;

  protected AttachmentRef() {

  }

  /**
   * Constructs a new attachment reference referring the specified document.
   * @param document a document.
   */
  public AttachmentRef(SimpleDocument document) {
    this.id = document.getId();
    this.instanceId = document.getInstanceId();
    this.foreignId = document.getForeignId();
    this.oldSilverpeasId = document.getOldSilverpeasId();
    this.name = document.getFilename();
    this.versioned = document.isVersioned();
    this.userId = document.getEditedBy();
    if (!StringUtil.isDefined(userId)) {
      this.userId = document.getUpdatedBy();
      if (!StringUtil.isDefined(userId)) {
        userId = document.getCreatedBy();
      }
    }
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

  public String getUserId() {
    return userId;
  }
}
