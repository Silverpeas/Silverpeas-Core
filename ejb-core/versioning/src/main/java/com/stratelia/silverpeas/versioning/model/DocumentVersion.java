/*
 * Aliaksei_Budnikau
 * Date: Oct 16, 2002
 */
package com.stratelia.silverpeas.versioning.model;

import java.io.File;
import java.util.Date;

import com.silverpeas.form.importExport.XMLModelContentType;
import com.silverpeas.util.MimeTypes;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;

public class DocumentVersion implements java.io.Serializable, Cloneable,
    MimeTypes {

  public final static int STATUS_VALIDATION_NOT_REQ = 0;
  public final static int STATUS_VALIDATION_REQUIRED = 1;
  public final static int STATUS_VERSION_VALIDATED = 2;
  public final static int STATUS_VERSION_REFUSED = 3;

  public final static int TYPE_DEFAULT_VERSION = 1;
  public final static int TYPE_PUBLIC_VERSION = 0;

  final static String SPINFIRE_MIME_TYPE = "application/xview3d-3d";

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
  private int size;
  private String instanceId;
  private String xmlForm = null;
  private String creatorName; // used by import/export XML
  private XMLModelContentType xmlModelContentType = null;

  public DocumentVersion() {
  }

  public DocumentVersion(DocumentVersionPK pk, DocumentPK documentPK,
      int majorNumber, int minorNumber, int authorId, Date creationDate,
      String comments, int type, int status, String physicalName,
      String logicalName, String mimeType, int size, String instanceId) {
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
    this.size = new Long(attachment.getSize()).intValue();
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
    return creationDate;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
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

  public int getSize() {
    return size;
  }

  public String getDisplaySize() {
    return FileRepositoryManager.formatFileSize(getSize());
  }

  public void setSize(int size) {
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
   */
  public boolean isOfficeDocument() {
    boolean isOfficeDocument = false;
    if (getMimeType() != null) {
      isOfficeDocument = MS_OFFICE_MIME_TYPES.contains(getMimeType());
      SilverTrace.info("versioning", "DocumentVersion.isOfficeDocument()",
          "root.MSG_GEN_PARAM_VALUE", "is Office Document = "
              + isOfficeDocument);
    }
    return isOfficeDocument;
  }

  public String getJcrPath() {
    StringBuffer jcrPath = new StringBuffer(500);
    jcrPath.append(getInstanceId()).append('/');
    jcrPath.append(CONTEXT).append('/');
    if (getDocumentPK().getId() != null) {
      jcrPath.append(getDocumentPK().getId()).append('/');
    }
    jcrPath.append(majorNumber + "." + minorNumber).append('/');
    jcrPath.append(StringUtil.escapeQuote(getLogicalName()));
    return jcrPath.toString();
  }

  public String getWebdavUrl() {
    StringBuffer url = new StringBuffer(500);
    ResourceLocator messages = GeneralPropertiesManager
        .getGeneralResourceLocator();
    String webAppContext = messages.getString("ApplicationURL");
    if (!webAppContext.endsWith("/")) {
      webAppContext = webAppContext + '/';
    }
    url.append(webAppContext).append(messages.getString("webdav.respository"))
        .append('/').append(messages.getString("webdav.workspace")).append('/')
        .append(getJcrPath());
    return url.toString();
  }

  /**
   * If 3d document
   * 
   * @return true or false
   */
  public boolean isSpinfireDocument() {
    boolean isSpinfireDocument = false;
    if (getMimeType() != null) {
      SilverTrace.info("versioning", "DocumentVersion.isSpinfireDocument()",
          "root.MSG_GEN_PARAM_VALUE", "isSpinfireDocument = "
              + (getMimeType().equals(SPINFIRE_MIME_TYPE)));
      isSpinfireDocument = getMimeType().equals(SPINFIRE_MIME_TYPE);
    }
    return isSpinfireDocument;
  }

  /**
   * If 3d document
   * 
   * @return true or false
   */
  public boolean isOpenOfficeCompatibleDocument() {
    boolean isOpenOfficeCompatibleDocument = false;
    if (getMimeType() != null) {
      isOpenOfficeCompatibleDocument = OPEN_OFFICE_MIME_TYPES
          .contains(getMimeType());
      SilverTrace.info("versioning", "DocumentVersion.isSpinfireDocument()",
          "root.MSG_GEN_PARAM_VALUE", "isOpenOfficeCompatibleDocument = "
              + isOpenOfficeCompatibleDocument);
    }
    return isOpenOfficeCompatibleDocument;
  }

  /**
   * Overriden toString method for debug/trace purposes
   */
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
   */
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
    String directory = FileRepositoryManager.getAbsolutePath(getInstanceId(),
        new String[] { CONTEXT });
    if (!directory.endsWith(File.separator)) {
      directory = directory + File.separator;
    }
    return directory + getPhysicalName();
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
}