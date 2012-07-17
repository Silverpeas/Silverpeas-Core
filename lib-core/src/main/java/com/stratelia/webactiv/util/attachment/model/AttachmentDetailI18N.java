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

package com.stratelia.webactiv.util.attachment.model;

import java.io.Serializable;
import java.util.Date;

import com.silverpeas.util.i18n.Translation;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;

/**
 * Class declaration
 * @author
 */
public class AttachmentDetailI18N extends Translation implements Serializable {

  private static final long serialVersionUID = -9079969283559100288L;
  private String physicalName = null;
  private String logicalName = null;
  private String type = null;
  private Date creationDate;
  private long size;
  private String author = null;
  private String title = null;
  private String info = null;
  private String instanceId = null;
  private String xmlForm = null;

  public AttachmentDetailI18N() {
  }

  public AttachmentDetailI18N(AttachmentDetail attachmentDetail) {
    super.setLanguage(attachmentDetail.getLanguage());
    super.setObjectId(attachmentDetail.getPK().getId());
    if (attachmentDetail.getTranslationId() != null) {
      super.setId(Integer.parseInt(attachmentDetail.getTranslationId()));
    }

    this.physicalName = attachmentDetail.getPhysicalName();
    this.logicalName = attachmentDetail.getLogicalName();
    this.type = attachmentDetail.getType();
    this.creationDate = attachmentDetail.getCreationDate();
    this.size = attachmentDetail.getSize();
    this.author = attachmentDetail.getAuthor();
    this.title = attachmentDetail.getTitle();
    this.info = attachmentDetail.getInfo();
    this.instanceId = attachmentDetail.getPK().getInstanceId();
    this.xmlForm = attachmentDetail.getXmlForm();
  }

  public AttachmentDetailI18N(String lang, String physicalName, String logicalName, String type,
      Date creationDate, long size, String author, String title, String info, String instanceId) {
    if (lang != null) {
      super.setLanguage(lang);
    }

    this.physicalName = physicalName;
    this.logicalName = logicalName;
    this.type = type;
    this.creationDate = creationDate;
    this.size = size;
    this.author = author;
    this.title = title;
    this.info = info;
    this.instanceId = instanceId;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getPhysicalName() {
    return physicalName;
  }

  /**
   * Method declaration
   * @param physicalName
   * @see
   */
  public void setPhysicalName(String physicalName) {
    this.physicalName = physicalName;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getLogicalName() {
    return logicalName;
  }

  /**
   * Method declaration
   * @param logicalName
   * @see
   */
  public void setLogicalName(String logicalName) {
    SilverTrace.info("attachment", "AttachmentDetail.setLogicalName()", "root.MSG_GEN_PARAM_VALUE",
        "logicalName = " + logicalName);
    this.logicalName = logicalName;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getType() {
    return type;
  }

  /**
   * Method declaration
   * @param type
   * @see
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public long getSize() {
    return size;
  }

  /**
   * Method declaration
   * @param size
   * @see
   */
  public void setSize(long size) {
    this.size = size;
  }

  /**
   * Methode declaration
   * @see
   */
  public Date getCreationDate() {
    return creationDate;
  }

  /**
   * Methode declaration
   * @param fileDate
   * @see
   */
  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getAuthor() {
    return this.author;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return this.title;
  }

  public void setInfo(String info) {
    this.info = info;
  }

  public String getInfo() {
    return this.info;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public String getExtension() {
    return FileRepositoryManager.getFileExtension(logicalName);
  }

  public String getXmlForm() {
    return xmlForm;
  }

  public void setXmlForm(String xmlForm) {
    this.xmlForm = xmlForm;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 67 * hash + this.getId();
    return hash;
  }

  @Override
  public boolean equals(Object arg0) {
    if (arg0 == null || !(arg0 instanceof AttachmentDetailI18N)) {
      return false;
    }
    AttachmentDetailI18N a = (AttachmentDetailI18N) arg0;
    return a.getId() == getId();

  }
}