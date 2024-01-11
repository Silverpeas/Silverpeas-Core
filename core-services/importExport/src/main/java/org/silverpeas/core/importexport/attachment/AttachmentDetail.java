/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.importexport.attachment;

import org.apache.commons.io.FilenameUtils;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.importexport.form.XMLModelContentType;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.io.Serializable;
import java.util.Date;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public final class AttachmentDetail implements Serializable {

  private static final long serialVersionUID = 5441809463555598057L;

  private AttachmentPK pk = null;
  private String filename = null;
  @XmlAttribute(name = "path")
  private String physicalName = null;
  private String type = null;
  private Date creationDate;
  private long size;
  private String context = null;
  private String author = null;
  @XmlElement(name = "name")
  private String title = null;
  @XmlElement(name = "description")
  private String description = null;
  private String instanceId = null;
  public static final String IMPORT_UPDATE_RULE_ADD = "add";
  public static final String IMPORT_UPDATE_RULE_REPLACE = "replace";
  // used by the import engine
  @XmlAttribute(name = "updateRule")
  private String importUpdateRule = IMPORT_UPDATE_RULE_ADD;
  @XmlAttribute
  private boolean removeAfterImport = false;
  private String originalPath;
  private String xmlForm = null;
  @XmlElement(name = "xmlModel")
  private XMLModelContentType xmlModelContentType = null;
  private String mailContentID = null;

  public AttachmentDetail() {
  }

  public AttachmentDetail(SimpleDocument document, String physicalName) {
    pk = new AttachmentPK(document.getId(), document.getInstanceId());
    this.physicalName = physicalName;
    this.title = document.getTitle();
    this.description = document.getDescription();
    this.type = document.getContentType();
    this.size = document.getSize();
    setContext(document.getDocumentType().toString());
    this.creationDate = document.getCreationDate();
  }

  public AttachmentPK getPK() {
    return pk;
  }

  public void setPK(AttachmentPK pk) {
    this.pk = pk;
    if (pk != null) {
      this.instanceId = pk.getInstanceId();
    }
  }

  public String getPhysicalName() {
    return physicalName;
  }

  public void setPhysicalName(String physicalName) {
    this.physicalName = physicalName;
  }

  public String getLogicalName() {
    if (filename != null) {
      return filename;
    }
    return FileUtil.getFilename(getPhysicalName());
  }

  public void setLogicalName(String logicalName) {
    filename = logicalName;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public void setContext(String context) {
    if (StringUtil.isDefined(context)) {
      this.context = context;
    } else {
      this.context = null;
    }
  }

  public String getContext() {
    return context;
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

  public void setDescription(String info) {
    description = info;
  }

  public String getDescription() {
    return description;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  protected String getLanguage(String language) {
    if (language != null && ("fr".equalsIgnoreCase(language) || "".equals(language.trim()))) {
      return null;
    }
    return language;
  }

  /**
   * Return the path to the attachment file.
   *
   * @return the path to the attachment file.
   */
  public String getAttachmentPath() {
    if (isPhysicalPathAbsolute()) {
      return physicalName.replace('/', File.separatorChar);
    }
    String directory = FileRepositoryManager.getAbsolutePath(getInstanceId(),
        FileRepositoryManager.getAttachmentContext(getContext()));
    if (!directory.endsWith(File.separator)) {
      directory = directory + File.separatorChar;
    }
    directory = directory.replace('/', File.separatorChar);
    return directory + getPhysicalName();
  }

  private boolean isPhysicalPathAbsolute() {
    String filePath = FilenameUtils.separatorsToSystem(physicalName);
    File file = new File(filePath);
    return file.exists() && file.isFile();
  }

  public String getImportUpdateRule() {
    return importUpdateRule;
  }

  public String getXmlForm() {
    return xmlForm;
  }

  public void setXmlForm(String xmlForm) {
    this.xmlForm = xmlForm;
  }

  public XMLModelContentType getXMLModelContentType() {
    return xmlModelContentType;
  }

  public boolean isRemoveAfterImport() {
    return removeAfterImport;
  }

  public void setRemoveAfterImport(boolean removeAfterImport) {
    this.removeAfterImport = removeAfterImport;
  }

  public void setOriginalPath(String originalPath) {
    this.originalPath = originalPath;
  }

  public String getOriginalPath() {
    return originalPath;
  }

  public String getMailContentID() {
    return mailContentID;
  }

  public void setMailContentID(final String mailContentID) {
    this.mailContentID = mailContentID;
  }
}