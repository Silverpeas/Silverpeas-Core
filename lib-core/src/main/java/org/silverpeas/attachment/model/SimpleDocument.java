/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.attachment.model;

import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import java.util.Date;

/**
 *
 * @author ehugonnet
 */
public class SimpleDocument {

  public final static String ATTACHMENT_PREFIX = "attach_";
  public final static String VERSION_PREFIX = "version_";
  public final static String FILE_PREFIX = "file_";
  private SimpleDocumentPK pk;
  private String foreignId;
  private int order;
  private boolean versioned;
  private String editedBy;
  private Date reservation;
  private Date alert;
  private Date expiry;
  private String status;
  private SimpleAttachment file;

  public SimpleDocument(SimpleDocumentPK pk, String foreignId, int order, boolean versioned,
      SimpleAttachment file) {
    this(pk, foreignId, order, versioned, null, file);
  }

  public SimpleDocument(SimpleDocumentPK pk, String foreignId, int order, boolean versioned,
      String editedBy, SimpleAttachment file) {
    this(pk, foreignId, order, versioned, editedBy, null, null, null, null, file);
  }

  public SimpleDocument(SimpleDocumentPK pk, String foreignId, int order, boolean versioned,
      String editedBy, Date reservation, Date alert, Date expiry, String status,
      SimpleAttachment file) {
    this.pk = pk;
    this.foreignId = foreignId;
    this.order = order;
    this.versioned = versioned;
    this.editedBy = editedBy;
    this.reservation = reservation;
    this.alert = DateUtil.getBeginOfDay(alert);
    this.expiry = DateUtil.getBeginOfDay(expiry);
    this.status = status;
    this.file = file;
  }

  public SimpleDocument() {
  }

  public String getFilename() {
    return file.getFilename();
  }

  public void setFilename(String filename) {
    file.setFilename(filename);
  }

  public String getLanguage() {
    return file.getLanguage();
  }

  public void setLanguage(String language) {
    file.setLanguage(language);
  }

  public String getTitle() {
    return file.getTitle();
  }

  public void setTitle(String title) {
    file.setTitle(title);
  }

  public String getDescription() {
    return file.getDescription();
  }

  public void setDescription(String description) {
    file.setDescription(description);
  }

  public long getSize() {
    return file.getSize();
  }

  public void setSize(long size) {
    file.setSize(size);
  }

  public String getContentType() {
    return file.getContentType();
  }

  public void setContentType(String contentType) {
    file.setContentType(contentType);
  }

  public String getCreatedBy() {
    return file.getCreatedBy();
  }

  public void setCreatedBy(String createdBy) {
    file.setCreatedBy(createdBy);
  }

  public Date getCreated() {
    return file.getCreated();
  }

  public void setCreated(Date created) {
    file.setCreated(created);
  }

  public String getUpdatedBy() {
    return file.getUpdatedBy();
  }

  public void setUpdatedBy(String updatedBy) {
    file.setUpdatedBy(updatedBy);
  }

  public Date getUpdated() {
    return file.getUpdated();
  }

  public void setUpdated(Date updated) {
    file.setUpdated(updated);
  }

  public Date getReservation() {
    return reservation;
  }

  public void setReservation(Date reservation) {
    this.reservation = reservation;
  }

  public Date getAlert() {
    return alert;
  }

  public void setAlert(Date alert) {
    this.alert = DateUtil.getBeginOfDay(alert);
  }

  public Date getExpiry() {
    return expiry;
  }

  public void setExpiry(Date expiry) {
    this.expiry = DateUtil.getBeginOfDay(expiry);
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public int getMinorVersion() {
    return file.getMinorVersion();
  }

  public void setMinorVersion(int minorVersion) {
    file.setMinorVersion(minorVersion);
  }

  public int getMajorVersion() {
    return file.getMajorVersion();
  }

  public void setMajorVersion(int majorVersion) {
    file.setMajorVersion(majorVersion);
  }

  public String getEditedBy() {
    return editedBy;
  }

  public void edit(String currentEditor) {
    this.editedBy = currentEditor;
  }

  public String getCloneId() {
    return file.getCloneId();
  }

  public void setCloneId(String cloneId) {
    file.setCloneId(cloneId);
  }

  public String getXmlFormId() {
    return file.getXmlFormId();
  }

  public void setXmlFormId(String xmlFormId) {
    file.setXmlFormId(xmlFormId);
  }

  public String getId() {
    if (pk != null) {
      return pk.getId();
    }
    return null;
  }

  public void setId(String id) {
    if (pk != null) {
      this.pk.setId(id);
    } else {
      this.pk = new SimpleDocumentPK(id);
    }
  }

  public void setPK(SimpleDocumentPK pk) {
    this.pk = pk;
  }

  public String getInstanceId() {
    return this.pk.getInstanceId();
  }

  public long getOldSilverpeasId() {
    return this.pk.getOldSilverpeasId();
  }

  public void setOldSilverpeasId(long oldSilverpeasId) {
    this.pk.setOldSilverpeasId(oldSilverpeasId);
  }

  public String getForeignId() {
    return foreignId;
  }

  public void setForeignId(String foreignId) {
    this.foreignId = foreignId;
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public boolean isVersioned() {
    return versioned;
  }

  public void setVersioned(boolean versioned) {
    this.versioned = versioned;
  }

  public SimpleAttachment getFile() {
    return file;
  }

  public SimpleDocumentPK getPk() {
    return this.pk;
  }

  public void setFile(SimpleAttachment file) {
    this.file = file;
  }

  public String getNodeName() {
    if (getOldSilverpeasId() <= 0L) {
      if (isVersioned()) {
        setOldSilverpeasId(DBUtil.getNextId("sb_document_version", null));
      } else {
        setOldSilverpeasId(DBUtil.getNextId("sb_attachment_attachment", null));
      }
    }
    if (isVersioned()) {
      return VERSION_PREFIX + getOldSilverpeasId();
    }
    return ATTACHMENT_PREFIX + getOldSilverpeasId();
  }

  @Override
  public String toString() {
    return "SimpleDocument{" + "pk=" + pk + ", foreignId=" + foreignId + ", order=" + order
        + ", versioned=" + versioned + ", editedBy=" + editedBy + ", reservation=" + reservation
        + ", alert=" + alert + ", expiry=" + expiry + ", status=" + status + ", file=" + file + '}';
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 31 * hash + (this.pk != null ? this.pk.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SimpleDocument other = (SimpleDocument) obj;
    if (this.pk != other.pk && (this.pk == null || !this.pk.equals(other.pk))) {
      return false;
    }
    return true;
  }
}
