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

package com.silverpeas.attachment.web;

import com.silverpeas.web.Exposable;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentRuntimeException;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author ehugonnet
 */
@XmlRootElement
public class AttachmentEntity implements Exposable {

  private static final long serialVersionUID = 6578990825699318566L;
  @XmlElement(required = true)
  private String id;
  @XmlElement(required = true)
  private String instanceId;
  @XmlElement(required = true)
  private String logicalName;
  @XmlElement(defaultValue = "")
  private String description;
  @XmlElement(defaultValue = "")
  private String type;
  @XmlElement(defaultValue = "0")
  private long creationDate;
  @XmlElement(defaultValue = "0")
  private long size;
  @XmlElement(defaultValue = "")
  private String author;
  @XmlElement(defaultValue = "")
  private String title;
  @XmlElement(defaultValue = "")
  private String info;
  @XmlElement(required = true)
  private URI uri;
  @XmlElement(required = true)
  private String icon;
  @XmlElement(required = true)
  private String permalink;
  @XmlElement(required = false)
  private URI sharedUri;

  public static AttachmentEntity fromAttachment(AttachmentDetail detail) {
    AttachmentEntity entity = new AttachmentEntity();
    try {
      entity.uri = new URI(URLManager.getSimpleURL(URLManager.URL_FILE, detail.getPK().getId()));
    } catch (URISyntaxException e) {
      throw new AttachmentRuntimeException("AttachmentEntity.fromAttachment(",
          AttachmentRuntimeException.ERROR, "Couldn't build the URI to the attachment", e);
    }
    entity.id = detail.getPK().getId();
    entity.instanceId = detail.getPK().getComponentName();
    entity.logicalName = detail.getLogicalName();
    entity.description = detail.getDescription();
    entity.size = detail.getSize();
    entity.creationDate = detail.getCreationDate().getTime();
    entity.info = detail.getInfo();
    entity.author = detail.getAuthor();
    entity.title = detail.getTitle();
    entity.type = detail.getType();
    entity.icon = detail.getAttachmentIcon();
    entity.permalink = URLManager.getSimpleURL(URLManager.URL_FILE, detail.getPK().getId());
    return entity;
  }

  public static AttachmentEntity fromAttachment(AttachmentDetail detail, String lang) {
    AttachmentEntity entity = new AttachmentEntity();
    try {
      entity.uri = new URI(URLManager.getSimpleURL(URLManager.URL_FILE, detail.getPK().getId()));
    } catch (URISyntaxException e) {
      throw new AttachmentRuntimeException("AttachmentEntity.fromAttachment(",
          AttachmentRuntimeException.ERROR, "Couldn't build the URI to the attachment", e);
    }
    entity.id = detail.getPK().getId();
    entity.instanceId = detail.getPK().getComponentName();
    entity.logicalName = detail.getLogicalName(lang);
    entity.description = detail.getDescription();
    entity.size = detail.getSize(lang);
    entity.creationDate = detail.getCreationDate(lang).getTime();
    entity.info = detail.getInfo(lang);
    entity.author = detail.getAuthor(lang);
    entity.title = detail.getTitle(lang);
    entity.type = detail.getType(lang);
    entity.icon = detail.getAttachmentIcon(lang);
    entity.permalink = URLManager.getSimpleURL(URLManager.URL_FILE, detail.getPK().getId());
    return entity;
  }

  /**
   * Sets a URI to this entity. With this URI, it can then be accessed through the Web.
   * @param uri the web entity URI.
   * @return itself.
   */
  public AttachmentEntity withURI(final URI uri) {
    this.uri = uri;
    return this;
  }

  @Override
  public URI getURI() {
    return this.uri;
  }

  public void setSharedUri(URI sharedUri) {
    this.sharedUri = sharedUri;
  }
}
