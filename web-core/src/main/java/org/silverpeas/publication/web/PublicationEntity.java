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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;

import org.owasp.encoder.Encode;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.sharing.SharingContext;
import org.silverpeas.wysiwyg.control.WysiwygController;

import com.silverpeas.attachment.web.AttachmentEntity;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.profile.web.UserProfileEntity;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.StringUtil;
import com.silverpeas.web.Exposable;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

/**
 * Web entity representing a publication that can be serialized into a given media type (JSON, XML).
 */
public class PublicationEntity implements Exposable {

  private static final long serialVersionUID = 7746081841765736096L;
  
  @XmlElement(defaultValue = "")
  private URI uri;
  @XmlElement(defaultValue = "")
  private String id;
  @XmlElement(required = true)
  @NotNull
  @Size(min = 2)
  private String componentId;
  @XmlElement(defaultValue = "")
  private String name;
  @XmlElement(defaultValue = "")
  private String description;
  @XmlElement(defaultValue = "")
  private String keywords;
  @XmlElement(defaultValue = "0")
  private int importance = 0;
  @XmlElement
  private Date updateDate;
  @XmlElement
  private List<AttachmentEntity> attachments;
  @XmlElement
  private UserProfileEntity creator;
  @XmlElement
  private UserProfileEntity lastUpdater;
  private PublicationDetail pubDetail;
  @XmlElement
  private String content;

  @Override
  public URI getURI() {
    return uri;
  }
  
  /**
   * Gets the unique identifier of the publication.
   *
   * @return the publication identifier.
   */
  public String getId() {
    return id;
  }

  /**
   * Gets the identifier of the Silverpeas component instance which the publication belongs to.
   *
   * @return the silverpeas component instance identifier.
   */
  public String getComponentId() {
    return componentId;
  }

  private PublicationEntity() {
    
  }
  
  /**
    * Creates a new publication entity from the specified publication.
    *
    * @param publication the publication to entitify.
    */
  public static PublicationEntity fromPublicationDetail(final PublicationDetail publication, URI uri) {
    return new PublicationEntity(publication, uri);
  }
  
  private PublicationEntity(final PublicationDetail publication, URI uri) {
    this.componentId = publication.getPK().getInstanceId();
    this.id = publication.getPK().getId();
    this.setName(Encode.forHtml(publication.getName()));
    this.setDescription(Encode.forHtml(publication.getDescription()));
    this.setKeywords(Encode.forHtml(publication.getKeywords()));
    this.importance = publication.getImportance();
    this.pubDetail = publication;
    this.setUri(uri);
    this.setUpdateDate(publication.getUpdateDate());
    this.creator = UserProfileEntity.fromUser(publication.getCreator());
    this.lastUpdater = UserProfileEntity.fromUser(UserDetail.getById(publication.getUpdaterId()));
  }
  
  public PublicationEntity withAttachments(Collection<SimpleDocument> attachmentDetails) {
    if (attachmentDetails != null && !attachmentDetails.isEmpty()) {
      List<AttachmentEntity> entities = new ArrayList<AttachmentEntity>(attachmentDetails.size());
      for (SimpleDocument attachment : attachmentDetails) {
        SimpleDocument document = attachment.getLastPublicVersion();
        if (document != null) {
          AttachmentEntity entity = AttachmentEntity.fromAttachment(document);
          entities.add(entity);
        }
      }
      setAttachments(entities);
    }
    return this;
  }
  
  public PublicationEntity withContent(SharingContext context) {
    String lang = null;
    if (WysiwygController.haveGotWysiwygToDisplay(pubDetail.getInstanceId(), pubDetail.getId(),
        lang)) {
      content = WysiwygController.load(pubDetail.getInstanceId(), pubDetail.getId(), lang);
    } else if (!StringUtil.isInteger(pubDetail.getInfoId())) {
      PublicationTemplateImpl pubTemplate;
      try {
        pubTemplate =
            (PublicationTemplateImpl) PublicationTemplateManager.getInstance()
                .getPublicationTemplate(
                    pubDetail.getPK().getInstanceId() + ":" + pubDetail.getInfoId());

        Form formView = pubTemplate.getViewForm();
        // get displayed language
        String language = null;
        RecordSet recordSet = pubTemplate.getRecordSet();
        DataRecord data = recordSet.getRecord(pubDetail.getId(), language);
        if (data != null) {
          PagesContext formContext = new PagesContext();
          formContext.setComponentId(pubDetail.getInstanceId());
          formContext.setObjectId(pubDetail.getId());
          formContext.setBorderPrinted(false);
          formContext.setSharingContext(context);
          content = formView.toString(formContext, data);
        }
      } catch (Exception e) {
        content = "Error while getting content !";
        SilverTrace.error("kmelia", "PublicationEntity.withContent", "root.EX_IGNORED", "pk = " +
            pubDetail.getPK().toString(), e);
      }
    }
    return this;
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
  
  public void setKeywords(String keywords) {
    this.keywords = keywords;
  }
  
  public String getKeywords() {
    return keywords;
  }

  public int getImportance() {
    return importance;
  }

  public void setImportance(int importance) {
    this.importance = importance;
  }

  public void setUpdateDate(Date updateDate) {
    this.updateDate = updateDate;
  }

  public Date getUpdateDate() {
    return updateDate;
  }

  protected void setAttachments(List<AttachmentEntity> attachments) {
    this.attachments = attachments;
  }
  
  protected List<AttachmentEntity> getAttachments() {
    return attachments;
  }
  
  public PublicationDetail toPublicationDetail() {
    PublicationDetail publication = new PublicationDetail();
    publication.setPk(new PublicationPK(id, componentId));
    publication.setName(name);
    publication.setDescription(description);
    publication.setKeywords(keywords);
    publication.setImportance(importance);
    publication.setCreatorId(creator.getId());
    publication.setUpdaterId(lastUpdater.getId());
    publication.setUpdateDate(updateDate);
    return publication;
  }
  
}
