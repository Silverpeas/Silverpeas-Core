package org.silverpeas.publication.web;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import com.silverpeas.attachment.web.AttachmentEntity;
import com.silverpeas.rest.Exposable;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

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

  @Override
  public URI getURI() {
    return uri;
  }
  
  /**
   * Creates a new publication entity from the specified publication.
   * @param node the node to entitify.
   * @return the entity representing the specified node.
   */
  public static PublicationEntity fromPublicationDetail(final PublicationDetail publication, URI uri) {
    return new PublicationEntity(publication, uri);
  }
  
  private PublicationEntity(final PublicationDetail publication, URI uri) {
    this.setName(publication.getName());
    this.setDescription(publication.getDescription());
    this.setUri(uri);
    this.setUpdateDate(publication.getUpdateDate());
  }
  
  public PublicationEntity withAttachments(final Collection<AttachmentDetail> attachmentDetails) {
    if(attachmentDetails != null && !attachmentDetails.isEmpty()) {
      List<AttachmentEntity> entities = new ArrayList<AttachmentEntity>(attachmentDetails.size());
      for(AttachmentDetail attachment : attachmentDetails) {
        entities.add(AttachmentEntity.fromAttachment(attachment));
      }
      this.attachments = entities.toArray(new AttachmentEntity[entities.size()]);
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

  public void setUpdateDate(Date updateDate) {
    this.updateDate = updateDate;
  }

  public Date getUpdateDate() {
    return updateDate;
  }

}
