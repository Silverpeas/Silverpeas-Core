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
package org.silverpeas.core.importexport.attachment;

import org.silverpeas.core.importexport.form.XMLModelContentType;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.MimeTypes;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.i18n.AbstractBean;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.WAPrimaryKey;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

/**
 * Class declaration
 *
 * @author
 */
public final class AttachmentDetail extends AbstractBean
    implements Serializable, MimeTypes, Cloneable {

  private static final long serialVersionUID = 5441809463555598057L;
  public static final String ATTACHMENTS_FOLDER = "attachments";
  transient public static final int GROUP_FILE = 0;
  transient public static final int GROUP_FILE_LINK = 1;
  transient public static final int GROUP_HTML_LINK = 2;
  transient public static final int GROUP_DIR = 3;
  transient public static final int GROUP_DUMMY = 4;
  private AttachmentPK pk = null;
  private String physicalName = null;
  private String type = null;
  private Date creationDate;
  private long size;
  private String context = null;
  private WAPrimaryKey foreignKey = null;
  private String author = null;
  private String title = null;
  private String info = null;
  private int orderNum;
  private String instanceId = null;
  private String workerId = null;
  private String cloneId = null;
  public static final String IMPORT_UPDATE_RULE_ADD = "add";
  public static final String IMPORT_UPDATE_RULE_REPLACE = "replace";
  private String importUpdateRule = IMPORT_UPDATE_RULE_ADD; // used by the import engine
  private boolean removeAfterImport = false;
  private String originalPath;
  private Date reservationDate = null; // date de réservation
  private Date alertDate = null; // date d'alerte pour la notification intermédiaire
  private Date expiryDate = null; // date d'expiration
  private String xmlForm = null;
  private XMLModelContentType xmlModelContentType = null;

  public String getCloneId() {
    return cloneId;
  }

  public void setCloneId(String cloneId) {
    this.cloneId = cloneId;
  }

  /**
   * Constructor
   *
   * @author Jean-Claude Groccia
   * @version
   */
  public AttachmentDetail() {
  }

  /**
   * Constructor
   *
   * @param foreignKey : type WAPrimaryKey: the key of custumer object
   * @author Jean-Claude Groccia
   * @version
   */
  public AttachmentDetail(WAPrimaryKey foreignKey) {
    this.foreignKey = foreignKey;
  }

  /**
   * Constructor
   *
   * @param attachPK : type AttachmentPK: the primary key of AttachmentDetail
   * @param logicalName :type String: the name file
   * @param physicalName : type String: the name file stored in the server
   * @param description : type String: the description of file, size=4000 character
   * @param type : type String: the mime type of file
   * @param size the size of the file.
   * @param context : type String: the context or the file is recorded
   * @param creationDate : type Date: the date where the file was added
   * @param foreignKey : type WAPrimaryKey: the key of custumer object
   * @param author the creator of the attechment.
   * @author Jean-Claude Groccia
   * @version
   */
  public AttachmentDetail(AttachmentPK attachPK, String physicalName,
      String logicalName, String description, String type, long size,
      String context, Date creationDate, WAPrimaryKey foreignKey, String author) {
    pk = attachPK;
    if (pk == null) {

    } else {

    }
    this.physicalName = physicalName;
    setLogicalName(logicalName);
    setDescription(description);
    this.type = type;
    checkMimeType();
    this.size = size;
    // this.context = context;
    setContext(context);
    this.creationDate = creationDate;
    this.foreignKey = foreignKey;
    this.author = author;
  }

  public AttachmentDetail(AttachmentPK attachPK, String physicalName,
      String logicalName, String description, String type, long size,
      String context, Date creationDate, WAPrimaryKey foreignKey) {
    pk = attachPK;
    this.physicalName = physicalName;
    setLogicalName(logicalName);
    setDescription(description);
    this.type = type;
    checkMimeType();
    this.size = size;
    setContext(context);
    this.creationDate = creationDate;
    this.foreignKey = foreignKey;
  }

  public AttachmentDetail(AttachmentPK attachPK, String physicalName,
      String logicalName, String description, String type, long size,
      String context, Date creationDate, WAPrimaryKey foreignKey, String title,
      String info, int orderNum) {
    pk = attachPK;

    this.physicalName = physicalName;
    setLogicalName(logicalName);
    setDescription(description);
    this.type = type;
    checkMimeType();
    this.size = size;
    setContext(context);
    this.creationDate = creationDate;
    this.foreignKey = foreignKey;
    this.title = title;
    this.info = info;
    this.orderNum = orderNum;
  }

  /**
   * @param attachPK : type AttachmentPK: the primary key of AttachmentDetail
   * @param logicalName :type String: the name file
   * @param physicalName : type String: the name file stored in the server
   * @param description : type String: the description of file, size=4000 character
   * @param type : type String: the mime type of file
   * @param context : type String: the context or the file is recorded
   * @param creationDate : type Date: the date where the file was added
   * @param foreignKey : type WAPrimaryKey: the key of custumer object
   * @param author
   * @param title
   */
  public AttachmentDetail(AttachmentPK attachPK, String physicalName,
      String logicalName, String description, String type, long size,
      String context, Date creationDate, WAPrimaryKey foreignKey,
      String author, String title) {
    pk = attachPK;
    if (pk == null) {

    } else {

    }
    this.physicalName = physicalName;
    setLogicalName(logicalName);
    setDescription(description);
    this.type = type;
    checkMimeType();
    this.size = size;
    setContext(context);
    this.creationDate = creationDate;
    this.foreignKey = foreignKey;
    this.author = author;
    this.title = title;
  }

  /**
   * Constructors
   *
   * @param attachPK , the primary key of AttachmentDetail
   * @see AttachmentPK
   * @author Jean-Claude Groccia
   * @version
   */
  public AttachmentDetail(AttachmentPK attachPK) {
    pk = attachPK;
  }

  /**
   * the getters and setters
   */
  public AttachmentPK getPK() {
    return pk;
  }

  /**
   * Method declaration
   *
   * @param pk
   * @see
   */
  public void setPK(AttachmentPK pk) {
    this.pk = pk;
    if (pk == null) {

    } else {
      this.instanceId = pk.getInstanceId();

    }

  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public String getPhysicalName() {
    return physicalName;
  }


  /**
   * Method declaration
   *
   * @param physicalName
   * @see
   */
  public void setPhysicalName(String physicalName) {
    this.physicalName = physicalName;
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public String getLogicalName() {
    return getName();
  }

  /**
   * Method declaration
   *
   * @param logicalName
   * @see
   */
  public void setLogicalName(String logicalName) {

    setName(logicalName);
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public String getType() {
    return type;
  }

  /**
   * Method declaration
   *
   * @param type
   * @see
   */
  /**
   * @param type
   */
  public void setType(String type) {
    this.type = type;
    checkMimeType();
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public long getSize() {
    return size;
  }

  /**
   * Method declaration
   *
   * @param size
   * @see
   */
  public void setSize(long size) {
    this.size = size;
  }

  /**
   * Methode declaration
   *
   * @see
   */
  public Date getCreationDate() {
    return creationDate;
  }

  /**
   * Methode declaration
   *
   * @param creationDate
   * @see
   */
  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  /**
   * Method declaration
   *
   * @param context
   * @see
   */
  public void setContext(String context) {
    if (context != null && !context.equals("null") && context.length() > 0) {
      this.context = context;
    } else {
      this.context = null;
    }
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public String getContext() {
    return context;
  }

  /**
   * Method declaration
   *
   * @param foreignKey
   * @see
   */
  public void setForeignKey(WAPrimaryKey foreignKey) {
    this.foreignKey = foreignKey;
    this.instanceId = foreignKey.getInstanceId();
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public WAPrimaryKey getForeignKey() {
    return foreignKey;
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

  public void setOrderNum(int orderNum) {
    this.orderNum = orderNum;
  }

  public int getOrderNum() {
    return orderNum;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getInstanceId() {
    return instanceId;
  }

  /**
   * Set the use who's working on the document
   *
   * @param workerId
   */
  public void setWorkerId(String workerId) {
    this.workerId = workerId;
  }

  /**
   * Get the current user who's working on the document
   *
   * @return
   */
  public String getWorkerId() {
    return workerId;
  }

  /**
   * Retourne le group auquel appartient l'attachment. Ce groupe est deduit du champ description
   *
   * @see
   */
  public int getAttachmentGroup() {
    int valret = GROUP_FILE;

    if (getDescription() != null) {
      if (getDescription().startsWith("link")) {
        valret = GROUP_FILE_LINK;
      } else if (getDescription().startsWith("html")) {
        valret = GROUP_HTML_LINK;
      } else if (getDescription().startsWith("dir")) {
        valret = GROUP_DIR;
      } else if (getDescription().startsWith("dummy")) {
        valret = GROUP_DUMMY;
      }
    }
    return valret;
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

  /**
   * Check if the attachment is compatible with OpenOffice using the mime type .
   *
   * @return true if the attachment is compatible with OpenOffice false otherwise.
   */
  public boolean isOpenOfficeCompatible() {
    return FileUtil.isOpenOfficeCompatible(getLogicalName());
  }


  /**
   * Use isOpenOfficeCompatible instead as Ms Office is no longer a special case.
   *
   * @return
   * @deprecated Use isOpenOfficeCompatible instead as Ms Office is no longer a special case.
   */
  @Deprecated
  public boolean isOfficeDocument() {
    return isOpenOfficeCompatible();
  }

  public boolean isSpinfireDocument() {
    boolean isSpinfireDocument = false;
    if (getType() != null) {
      isSpinfireDocument = getType().equals(SPINFIRE_MIME_TYPE);
    }
    return isSpinfireDocument;
  }

  public String getExtension() {
    return FileRepositoryManager.getFileExtension(getLogicalName());
  }

  @Override
  public Object clone() {
    AttachmentDetail clone = new AttachmentDetail();
    clone.setAuthor(author);
    clone.setCloneId(cloneId);
    clone.setContext(context);
    clone.setCreationDate(creationDate);
    clone.setDescription(getDescription());
    clone.setForeignKey(foreignKey);
    clone.setInfo(info);
    clone.setInstanceId(instanceId);
    clone.setLogicalName(getLogicalName());
    clone.setOrderNum(orderNum);
    clone.setPhysicalName(physicalName);
    clone.setPK(pk);
    clone.setSize(size);
    clone.setTitle(title);
    clone.setType(type);
    clone.setWorkerId(workerId);
    return clone;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 89 * hash + (this.pk != null ? this.pk.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object arg0) {
    if (arg0 == null) {
      return false;
    }
    if (arg0 instanceof AttachmentDetail) {
      AttachmentDetail a = (AttachmentDetail) arg0;
      return a.getPK().getId().equals(this.getPK().getId());
    } else {
      return false;
    }
  }

  public String getImportUpdateRule() {
    return importUpdateRule;
  }

  public void setImportUpdateRule(String updateRule) {
    this.importUpdateRule = updateRule;
  }


  /**
   * if type is known as application/octet-stream, try to find right mimeType
   */
  public void checkMimeType() {
    if (getLogicalName() != null && (!StringUtil.isDefined(type) || DEFAULT_MIME_TYPE.equalsIgnoreCase(type))) {
      type = FileUtil.getMimeType(getLogicalName());
    }
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
}