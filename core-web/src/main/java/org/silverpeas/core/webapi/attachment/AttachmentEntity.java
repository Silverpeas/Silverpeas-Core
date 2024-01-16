/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.attachment;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.contribution.attachment.AttachmentException;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.webapi.base.WebEntity;

import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

/**
 * @author ehugonnet
 */
@XmlRootElement
public class AttachmentEntity implements WebEntity {

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
  @XmlElement(required = true)
  private URI uri;
  @XmlElement(required = true)
  private String icon;
  @XmlElement(required = true)
  private String permalink;
  @XmlElement(required = false)
  private URI sharedUri;

  public static AttachmentEntity fromAttachment(SimpleDocument detail) {
    AttachmentEntity entity = new AttachmentEntity();
    try {
      entity.uri = new URI(URLUtil.getSimpleURL(URLUtil.URL_FILE, detail.getId()));
    } catch (URISyntaxException e) {
      throw new AttachmentException("Couldn't build the URI to the attachment", e);
    }
    entity.id = detail.getId();
    entity.instanceId = detail.getInstanceId();
    entity.logicalName = detail.getFilename();
    entity.description = detail.getDescription();
    entity.size = detail.getSize();
    entity.creationDate = detail.getCreated().getTime();
    entity.author = detail.getCreatedBy();
    entity.title = detail.getTitle();
    entity.type = detail.getContentType();
    entity.icon = detail.getDisplayIcon();
    entity.permalink = URLUtil.getSimpleURL(URLUtil.URL_FILE, detail.getId());

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

  public void withSharedUri(String baseURI, String token) {
    URI theUri;
    try {
      theUri = UriBuilder.fromUri(baseURI)
          .path("sharing/attachments")
          .path(instanceId)
          .path(token)
          .path(id)
          .path(URLEncoder.encode(logicalName, Charsets.UTF_8.name()))
          .build();
    } catch (UnsupportedEncodingException ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
      throw new SilverpeasRuntimeException(ex.getMessage(), ex);
    }
    this.sharedUri = theUri;
  }

  public void withBaseUri(String baseURI) {
    URI privateUri;
    try {
      privateUri = UriBuilder.fromUri(baseURI)
          .path("private/attachments")
          .path(instanceId)
          .path(id)
          .path(URLEncoder.encode(logicalName, Charsets.UTF_8.name()))
          .build();
    } catch (UnsupportedEncodingException ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
      throw new SilverpeasRuntimeException(ex.getMessage(), ex);
    }
    this.uri = privateUri;
  }

}
