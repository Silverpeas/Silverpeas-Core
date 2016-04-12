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

/*
 * Aliaksei_Budnikau
 * Date: Oct 16, 2002
 */
package org.silverpeas.core.importexport.versioning;

import org.silverpeas.core.importexport.form.XMLModelContentType;
import org.apache.commons.io.FilenameUtils;
import org.silverpeas.core.importexport.attachment.AttachmentDetail;
import org.silverpeas.core.persistence.jcr.JcrDataConverter;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.MimeTypes;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

import java.io.File;
import java.util.Date;

public class DocumentVersion implements java.io.Serializable, Cloneable, MimeTypes {

  private static final long serialVersionUID = 1L;
  public final static int STATUS_VALIDATION_NOT_REQ = 0;
  public final static int STATUS_VALIDATION_REQUIRED = 1;
  public final static int STATUS_VERSION_VALIDATED = 2;
  public final static int STATUS_VERSION_REFUSED = 3;
  public final static int TYPE_DEFAULT_VERSION = 1;
  public final static int TYPE_PUBLIC_VERSION = 0;
  public final static String CONTEXT = "Versioning";
  public final static String CONTEXT_VERSIONING = CONTEXT + File.separator;
  private DocumentVersionPK pk;
  private DocumentPK documentPK;
  private int majorNumber;
  private int minorNumber;
  private int authorId;
  private Date creationDate;
  private String comments;
  private int type = TYPE_DEFAULT_VERSION;
  private int status = STATUS_VALIDATION_NOT_REQ;
  private String physicalName;
  private String logicalName;
  private String mimeType;
  private long size;
  private String instanceId;
  private String xmlForm = null;
  // following attributes are used by import/export XML
  private String creatorName;
  private XMLModelContentType xmlModelContentType = null;
  private boolean removeAfterImport = false;
  private String originalPath;

  public DocumentVersion() {
  }

  public DocumentVersion(DocumentVersionPK pk, DocumentPK documentPK,
      int majorNumber, int minorNumber, int authorId, Date creationDate,
      String comments, int type, int status, String physicalName,
      String logicalName, String mimeType, long size, String instanceId) {
    this.pk = pk;
    this.documentPK = documentPK;
    this.majorNumber = majorNumber;
    this.minorNumber = minorNumber;
    this.authorId = authorId;
    this.creationDate = creationDate;
    this.comments = comments;
    this.type = type;
    this.status = status;
    this.physicalName = physicalName;
    this.logicalName = logicalName;
    this.mimeType = mimeType;
    this.size = size;
    this.instanceId = instanceId;
  }

  public DocumentVersion(AttachmentDetail attachment) {
    this.creationDate = attachment.getCreationDate();
    this.physicalName = attachment.getPhysicalName();
    this.logicalName = attachment.getLogicalName();
    this.mimeType = attachment.getType();
    this.size = attachment.getSize();
    this.instanceId = attachment.getPK().getInstanceId();
  }

  public DocumentVersionPK getPk() {
    return pk;
  }

  public void setPk(DocumentVersionPK pk) {
    this.pk = pk;
  }

  public DocumentPK getDocumentPK() {
    return documentPK;
  }

  public void setDocumentPK(DocumentPK documentPK) {
    this.documentPK = documentPK;
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

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
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

  public String getDisplaySize() {
    return FileRepositoryManager.formatFileSize(getSize());
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
   * Return if a document is an Office file
   *
   * @return true or false
   * @deprecated Use isOpenOfficeCompatibleDocument instead as Ms office is no longer a special
   * case.
   */
  @Deprecated
  public boolean isOfficeDocument() {
    return isOpenOfficeCompatibleDocument();
  }

  public String getJcrPath() {
    StringBuilder jcrPath = new StringBuilder(500);
    jcrPath.append(getInstanceId()).append('/');
    jcrPath.append(CONTEXT).append('/');
    if (getDocumentPK().getId() != null) {
      jcrPath.append(getDocumentPK().getId()).append('/');
    }
    jcrPath.append(majorNumber).append(".").append(minorNumber).append('/');
    jcrPath.append(JcrDataConverter.escapeIllegalJcrChars(getLogicalName()));
    return jcrPath.toString();
  }

  public String getWebdavUrl() {
    StringBuilder url = new StringBuilder(500);
    SettingBundle messages = ResourceLocator.getGeneralSettingBundle();
    String webAppContext = messages.getString("ApplicationURL");
    if (!webAppContext.endsWith("/")) {
      webAppContext += '/';
    }
    url.append(webAppContext).append(messages.getString("webdav.respository")).append('/').append(
        messages.
        getString("webdav.workspace")).append('/').append(getJcrPath());
    return url.toString();
  }

  /**
   * If 3d document
   *
   * @return true or false
   */
  public boolean isSpinfireDocument() {
    return SPINFIRE_MIME_TYPE.equals(getMimeType());
  }

  /**
   * If 3d document
   *
   * @return true or false
   */
  public boolean isOpenOfficeCompatibleDocument() {
    boolean isOpenOfficeCompatibleDocument = false;
    if (getLogicalName() != null) {
      isOpenOfficeCompatibleDocument = FileUtil.isOpenOfficeCompatible(getLogicalName());
    }
    return isOpenOfficeCompatibleDocument;
  }

  /**
   * Overriden toString method for debug/trace purposes
   *
   * @return the String representation of this document.
   */
  @Override
  public String toString() {
    return "DocumentVersion object : [  pk = " + pk + ", documentPK = "
        + documentPK + ", majorNumber = " + majorNumber + ", minorNumber = "
        + minorNumber + ", authorId = " + authorId + ", creationDate = "
        + creationDate + ", comments = " + comments + ", type = " + type
        + ", status = " + status + ", physicalName = " + physicalName
        + ", logicalName = " + logicalName + ", mimeType = " + mimeType
        + ", size = " + size + " ];";
  }

  /**
   * Support Cloneable Interface
   *
   * @return the clone
   */
  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      return null; // this should never happened
    }
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

  public String getDocumentIcon() {
    String icon = "";
    if (getPhysicalName().lastIndexOf('.') >= 0) {
      String fileType = FileRepositoryManager.getFileExtension(getPhysicalName());
      icon = FileRepositoryManager.getFileIcon(fileType);
    } else {
      icon = FileRepositoryManager.getFileIcon("");
    }
    return icon;
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