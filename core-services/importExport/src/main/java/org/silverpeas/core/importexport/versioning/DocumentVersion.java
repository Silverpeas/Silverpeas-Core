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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * Aliaksei_Budnikau
 * Date: Oct 16, 2002
 */
package org.silverpeas.core.importexport.versioning;

import org.apache.commons.io.FilenameUtils;
import org.silverpeas.core.importexport.form.XMLModelContentType;
import org.silverpeas.core.xml.DateAdapter;
import org.silverpeas.core.util.file.FileRepositoryManager;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.File;
import java.util.Date;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class DocumentVersion implements java.io.Serializable, Cloneable {

  private static final long serialVersionUID = 1L;
  public static final int STATUS_VALIDATION_NOT_REQ = 0;
  public static final int TYPE_DEFAULT_VERSION = 1;
  public static final int TYPE_PUBLIC_VERSION = 0;
  private static final String CONTEXT = "Versioning";
  private DocumentVersionPK pk;
  @XmlElement(name = "majorNumber")
  private int majorNumber;
  @XmlElement(name = "minorNumber")
  private int minorNumber;
  @XmlElement(name = "creatorId", defaultValue = "-1")
  private int authorId = -1;
  @XmlElement(name = "creationDate")
  @XmlJavaTypeAdapter(DateAdapter.class)
  private Date creationDate;
  @XmlElement(name = "description")
  private String comments;
  @XmlElement(name = "versionType")
  private int type = TYPE_DEFAULT_VERSION;
  @XmlAttribute(name = "path")
  private String physicalName;
  private String logicalName;
  private String mimeType;
  private long size;
  private String instanceId;
  private String xmlForm = null;
  // following attributes are used by import/export XML
  @XmlElement(name = "creatorName")
  private String creatorName;
  @XmlElement(name = "xmlModel")
  private XMLModelContentType xmlModelContentType = null;
  @XmlAttribute
  private boolean removeAfterImport = false;
  private String originalPath;

  public DocumentVersion() {
  }

  public DocumentVersionPK getPk() {
    return pk;
  }

  public void setPk(DocumentVersionPK pk) {
    this.pk = pk;
  }

  public int getMajorNumber() {
    return majorNumber;
  }

  public void setMajorNumber(int majorNumber) {
    this.majorNumber = majorNumber;
  }

  public int getMinorNumber() {
    return minorNumber;
  }

  public void setMinorNumber(int minorNumber) {
    this.minorNumber = minorNumber;
  }

  public int getAuthorId() {
    return authorId;
  }

  public void setAuthorId(int authorId) {
    this.authorId = authorId;
  }

  public Date getCreationDate() {
    if (creationDate != null) {
      return new Date(creationDate.getTime());
    }
    return null;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = new Date(creationDate.getTime());
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getPhysicalName() {
    return physicalName;
  }

  public void setPhysicalName(String physicalName) {
    this.physicalName = physicalName;
  }

  public String getLogicalName() {
    return logicalName;
  }

  public void setLogicalName(String logicalName) {
    this.logicalName = logicalName;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  /**
   * Overriden toString method for debug/trace purposes
   *
   * @return the String representation of this document.
   */
  @Override
  public String toString() {
    return "DocumentVersion object : [  pk = " + pk + ", majorNumber = " + majorNumber
        + ", minorNumber = " + minorNumber + ", authorId = " + authorId + ", creationDate = "
        + creationDate + ", comments = " + comments + ", type = " + type
        + ", physicalName = " + physicalName + ", logicalName = " + logicalName
        + ", mimeType = " + mimeType + ", size = " + size + " ];";
  }

  /**
   * Return the path to the document file.
   *
   * @return the path to the document file.
   */
  public String getDocumentPath() {
    if (isPhysicalPathAbsolute()) {
      return FilenameUtils.separatorsToSystem(physicalName);
    }
    String directory = FileRepositoryManager.getAbsolutePath(getInstanceId(), new String[]{CONTEXT});
    directory = FilenameUtils.separatorsToSystem(directory);
    if (!directory.endsWith(File.separator)) {
      directory += File.separator;
    }
    return directory + getPhysicalName();
  }

  private boolean isPhysicalPathAbsolute() {
    String filePath = FilenameUtils.separatorsToSystem(physicalName);
    File file = new File(filePath);
    return file.exists() && file.isFile();
  }

  public String getCreatorName() {
    return creatorName;
  }

  public void setCreatorName(String creatorName) {
    this.creatorName = creatorName;
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

  public void setXMLModelContentType(XMLModelContentType xmlModelContentType) {
    this.xmlModelContentType = xmlModelContentType;
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
}