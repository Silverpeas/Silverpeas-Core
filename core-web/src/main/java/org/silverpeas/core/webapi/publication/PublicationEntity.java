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
package org.silverpeas.core.webapi.publication;

import org.silverpeas.core.webapi.attachment.AttachmentEntity;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.webapi.profile.UserProfileEntity;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateImpl;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.webapi.base.WebEntity;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.owasp.encoder.Encode;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.util.SharingContext;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Web entity representing a publication that can be serialized into a given media type (JSON, XML).
 */
public class PublicationEntity implements WebEntity {

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

  public PublicationEntity withSharedContent(SharingContext context) {
    String lang = null;
    if (WysiwygController
        .haveGotWysiwygToDisplay(pubDetail.getInstanceId(), pubDetail.getId(), lang)) {
      content = WysiwygController.load(pubDetail.getInstanceId(), pubDetail.getId(), lang);
      content = context.applyOn(content);
    } else if (!StringUtil.isInteger(pubDetail.getInfoId())) {
      PublicationTemplateImpl pubTemplate;
      try {
        pubTemplate = (PublicationTemplateImpl) PublicationTemplateManager.getInstance()
            .getPublicationTemplate(
                pubDetail.getPK().getInstanceId() + ":" + pubDetail.getInfoId());

        Form formView = pubTemplate.getViewForm();
        // get displayed language
        RecordSet recordSet = pubTemplate.getRecordSet();
        DataRecord data = recordSet.getRecord(pubDetail.getId(), lang);
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
        SilverTrace.error("kmelia", "PublicationEntity.withContent", "root.EX_IGNORED",
            "pk = " + pubDetail.getPK().toString(), e);
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