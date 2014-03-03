/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.publication.web;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlElement;

import org.silverpeas.node.web.NodeEntity;

import com.silverpeas.attachment.web.AttachmentEntity;
import com.silverpeas.profile.web.UserProfileEntity;
import com.silverpeas.web.Exposable;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import org.apache.commons.lang3.CharEncoding;
import org.owasp.encoder.Encode;
import org.silverpeas.attachment.model.SimpleDocument;

public class PublicationEntity implements Exposable {

  private static final long serialVersionUID = 7746081841765736096L;
  @XmlElement(defaultValue = "")
  private URI uri;
  @XmlElement(defaultValue = "")
  private String name;
  @XmlElement(defaultValue = "")
  private String description;
  @XmlElement
  private Date updateDate;
  @XmlElement
  private AttachmentEntity[] attachments;
  @XmlElement
  private UserProfileEntity creator;
  @XmlElement
  private UserProfileEntity lastUpdater;

  @Override
  public URI getURI() {
    return uri;
  }

  /**
   * Creates a new publication entity from the specified publication.
   *
   * @param publication the publication to entitify.
   * @return the entity representing the specified node.
   */
  public static PublicationEntity fromPublicationDetail(final PublicationDetail publication, URI uri) {
    return new PublicationEntity(publication, uri);
  }

  private PublicationEntity(final PublicationDetail publication, URI uri) {
    this.setName(Encode.forHtml(publication.getName()));
    this.setDescription(Encode.forHtml(publication.getDescription()));
    this.setUri(uri);
    this.setUpdateDate(publication.getUpdateDate());
    this.creator = UserProfileEntity.fromUser(publication.getCreator());
    this.lastUpdater = UserProfileEntity.fromUser(UserDetail.getById(publication.getUpdaterId()));
  }

  public PublicationEntity withAttachments(final Collection<SimpleDocument> attachmentDetails,
      String baseURI, String token) {
    if (attachmentDetails != null && !attachmentDetails.isEmpty()) {
      List<AttachmentEntity> entities = new ArrayList<AttachmentEntity>(attachmentDetails.size());
      for (SimpleDocument attachment : attachmentDetails) {
        SimpleDocument document = attachment.getLastPublicVersion();
        if (document != null) {
          AttachmentEntity entity = AttachmentEntity.fromAttachment(document);
          URI sharedUri = getAttachmentSharedURI(attachment, baseURI, token);
          entity.setSharedUri(sharedUri);
          entities.add(entity);
        }

      }
      this.attachments = entities.toArray(new AttachmentEntity[entities.size()]);
    }
    return this;
  }

  private URI getAttachmentSharedURI(SimpleDocument attachment, String baseURI, String token) {
    URI sharedUri;
    try {
      sharedUri = new URI(baseURI + "attachments/" + attachment.getInstanceId() + "/" + token + "/"
          + attachment.getId() + "/" + URLEncoder.encode(attachment.getFilename(),
              CharEncoding.UTF_8));
    } catch (UnsupportedEncodingException ex) {
      Logger.getLogger(NodeEntity.class.getName()).log(Level.SEVERE, null, ex);
      throw new RuntimeException(ex.getMessage(), ex);
    } catch (URISyntaxException ex) {
      Logger.getLogger(NodeEntity.class.getName()).log(Level.SEVERE, null, ex);
      throw new RuntimeException(ex.getMessage(), ex);
    }

    return sharedUri;
  }

  public void setUri(URI uri) {
    this.uri = uri;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public void setUpdateDate(Date updateDate) {
    this.updateDate = updateDate;
  }

  public Date getUpdateDate() {
    return updateDate;
  }
}
