/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.stratelia.webactiv.util.attachment.model;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

import com.silverpeas.form.importExport.XMLModelContentType;
import com.silverpeas.jcrutil.converter.ConverterUtil;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.MimeTypes;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.AbstractI18NBean;
import com.silverpeas.util.i18n.I18NHelper;

import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentException;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;

/**
 * Class declaration
 *
 * @author
 */
public final class AttachmentDetail extends AbstractI18NBean implements Serializable, MimeTypes,
    Cloneable {

  private static final long serialVersionUID = 5441809463555598057L;
  public static final String ATTACHMENTS_FOLDER = "attachments";
  transient public static final int GROUP_FILE = 0;
  transient public static final int GROUP_FILE_LINK = 1;
  transient public static final int GROUP_HTML_LINK = 2;
  transient public static final int GROUP_DIR = 3;
  transient public static final int GROUP_DUMMY = 4;
  private AttachmentPK pk = null;
  private String physicalName = null;
  private String logicalName = null;
  private String description = null;
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
   * @see com.stratelia.util.WAPrimaryKey
   * @author Jean-Claude Groccia
   * @version
   */
  public AttachmentDetail(WAPrimaryKey foreignKey) {
    SilverTrace.info("attachment",
        "Contructor AttachmentDetail(WAPrimaryKey foreignKey)",
        "root.MSG_GEN_PARAM_VALUE", "foreignKey = " + foreignKey.toString());
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
   * @see com.stratelia.util.WAPrimaryKey
   * @author Jean-Claude Groccia
   * @version
   */
  public AttachmentDetail(AttachmentPK attachPK, String physicalName,
      String logicalName, String description, String type, long size,
      String context, Date creationDate, WAPrimaryKey foreignKey, String author) {
    pk = attachPK;
    if (pk == null) {
      SilverTrace.info("attachment", "Contructor with author",
          "root.MSG_GEN_PARAM_VALUE", "pk = null !");
    } else {
      SilverTrace.info("attachment", "Contructor with author",
          "root.MSG_GEN_PARAM_VALUE", "pk = " + pk.toString());
    }
    this.physicalName = physicalName;
    this.logicalName = logicalName;
    this.description = description;
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
    SilverTrace.info("attachment",
        "Contructor without author and without title",
        "root.MSG_GEN_PARAM_VALUE", "pk = " + pk.toString());
    this.physicalName = physicalName;
    this.logicalName = logicalName;
    this.description = description;
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
    SilverTrace.info("attachment", "Contructor without author but with title",
        "root.MSG_GEN_PARAM_VALUE", "pk = " + pk.toString());
    this.physicalName = physicalName;
    this.logicalName = logicalName;
    this.description = description;
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
   * @author dlesimple
   * @see com.stratelia.util.WAPrimaryKey
   */
  public AttachmentDetail(AttachmentPK attachPK, String physicalName,
      String logicalName, String description, String type, long size,
      String context, Date creationDate, WAPrimaryKey foreignKey,
      String author, String title) {
    pk = attachPK;
    if (pk == null) {
      SilverTrace.info("attachment", "Contructor with author and title",
          "root.MSG_GEN_PARAM_VALUE", "pk = null !");
    } else {
      SilverTrace.info("attachment", "Contructor with author an title",
          "root.MSG_GEN_PARAM_VALUE", "pk = " + pk.toString());
    }
    this.physicalName = physicalName;
    this.logicalName = logicalName;
    this.description = description;
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
   * @param AttachmentPK , the primary key of AttachmentDetail
   * @see com.stratelia.webactiv.util.attachment.ejb.AttachmentPK
   * @author Jean-Claude Groccia
   * @version
   */
  public AttachmentDetail(AttachmentPK attachPK) {
    pk = attachPK;
    SilverTrace.info("attachment",
        "Contructor AttachmentDetail(AttachmentPK attachPK)",
        "root.MSG_GEN_PARAM_VALUE", "pk = " + pk.toString());
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
      SilverTrace.info("attachment", "AttachmentDetail.setPK()",
          "root.MSG_GEN_PARAM_VALUE", "pk is null !");
    } else {
      this.instanceId = pk.getInstanceId();
      SilverTrace.info("attachment", "AttachmentDetail.setPK()",
          "root.MSG_GEN_PARAM_VALUE", "pk is not null = " + pk.toString());
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

  public String getPhysicalName(String language) {
    if (language == null || !I18NHelper.isI18N) {
      return getPhysicalName();
    }
    AttachmentDetailI18N detail = getAttachment(language);
    if (detail == null) {
      return getPhysicalName();
    }
    return detail.getPhysicalName();
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
    return logicalName;
  }

  public String getLogicalName(String language) {
    if (language == null || !I18NHelper.isI18N) {
      return getLogicalName();
    }
    AttachmentDetailI18N detail = getAttachment(language);
    if (detail == null) {
      return getLogicalName();
    }
    return detail.getLogicalName();
  }

  /**
   * Method declaration
   *
   * @param logicalName
   * @see
   */
  public void setLogicalName(String logicalName) {
    SilverTrace.info("attachment", "AttachmentDetail.setLogicalName()",
        "root.MSG_GEN_PARAM_VALUE", "logicalName = " + logicalName);
    this.logicalName = logicalName;
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  public String getDescription() {
    return description;
  }

  /**
   * Method declaration
   *
   * @param desc
   * @see
   */
  public void setDescription(String desc) {
    this.description = desc;
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

  public String getType(String language) {
    if (language == null || !I18NHelper.isI18N) {
      return getType();
    }
    AttachmentDetailI18N detail = getAttachment(language);
    if (detail == null) {
      return getType();
    }
    if (!StringUtil.isDefined(detail.getType())
        || detail.getType().equalsIgnoreCase(DEFAULT_MIME_TYPE)) {
      return FileUtil.getMimeType(detail.getLogicalName());
    }
    return detail.getType();
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

  public long getSize(String language) {
    if (language == null || !I18NHelper.isI18N) {
      return getSize();
    }
    AttachmentDetailI18N detail = getAttachment(language);
    if (detail == null) {
      return getSize();
    }
    return detail.getSize();
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

  public Date getCreationDate(String language) {
    if (language == null || !I18NHelper.isI18N) {
      return getCreationDate();
    }
    AttachmentDetailI18N detail = getAttachment(language);
    if (detail == null) {
      return getCreationDate();
    }
    return detail.getCreationDate();
  }

  /**
   * Methode declaration
   *
   * @param fileDate
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

  public String getAuthor(String language) {
    if (language == null || !I18NHelper.isI18N) {
      return getAuthor();
    }
    AttachmentDetailI18N detail = getAttachment(language);
    if (detail == null) {
      return getAuthor();
    }
    return detail.getAuthor();
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return this.title;
  }

  public String getTitle(String language) {
    if (language == null || !I18NHelper.isI18N) {
      return getTitle();
    }
    AttachmentDetailI18N detail = getAttachment(language);
    if (detail == null) {
      return getTitle();
    }
    return getAttachment(language).getTitle();
  }

  public void setInfo(String info) {
    this.info = info;
  }

  public String getInfo() {
    return this.info;
  }

  public String getInfo(String language) {
    if (language == null || !I18NHelper.isI18N) {
      return getInfo();
    }
    AttachmentDetailI18N detail = getAttachment(language);
    if (detail == null) {
      return getInfo();
    }
    return detail.getInfo();
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

    if (description != null) {
      if (description.startsWith("link")) {
        valret = GROUP_FILE_LINK;
      } else if (description.startsWith("html")) {
        valret = GROUP_HTML_LINK;
      } else if (description.startsWith("dir")) {
        valret = GROUP_DIR;
      } else if (description.startsWith("dummy")) {
        valret = GROUP_DUMMY;
      }
    }
    return valret;
  }

  /**
   * Retourne l'URL de l'attachment. Cette URL est construite a partir des autres informations
   *
   * @see
   */
  public String getAttachmentURL(String language) {
    return FileServerUtils.getAttachmentURL(pk.getInstanceId(), getLogicalName(language), pk
        .getId(),
        language);
  }

  public String getAttachmentURL() {
    return getAttachmentURL(null);
  }

  public String getOnlineURL(String language) {
    String theContext =
        FileRepositoryManager.getRelativePath(FileRepositoryManager.getAttachmentContext(context));
    String thePhysicalName = getPhysicalName(language);
    String theLogicalName = getLogicalName(language);
    String theType = getType(language);

    String valret = FileServerUtils.getOnlineURL(pk.getComponentName(),
        theLogicalName, thePhysicalName, theType, theContext);
    if (thePhysicalName != null) {
      String extension = FileRepositoryManager.getFileExtension(thePhysicalName);
      if ("exe".equalsIgnoreCase(extension) || "pdf".equalsIgnoreCase(extension)) {
        valret += "&logicalName=" + FileServerUtils.replaceSpecialChars(theLogicalName);
      }
    }
    return valret;
  }

  public String getOnlineURL() {
    return getOnlineURL(null);
  }

  public String getAliasURL(String language) {
    String localizedPhysicalName = getPhysicalName(language);
    String localizedLogicalName = getLogicalName(language);

    String valret =
        FileServerUtils.getAliasURL(pk.getInstanceId(), localizedLogicalName, pk.getId());
    if (I18NHelper.isI18N && !I18NHelper.isDefaultLanguage(language)) {
      valret += "&lang=" + language;
    }
    if (localizedPhysicalName != null) {
      String extension = FileRepositoryManager.getFileExtension(localizedPhysicalName);
      if ("exe".equalsIgnoreCase(extension) || "pdf".equalsIgnoreCase(extension)) {
        valret += "&logicalName=" + FileServerUtils.replaceSpecialChars(localizedLogicalName);
      }
    }
    return valret;
  }

  public String getAliasURL() {
    return getOnlineURL(null);
  }

  protected String getLanguage(String language) {
    if (language != null && ("fr".equalsIgnoreCase(language) || "".equals(language.trim()))) {
      return null;
    }
    return language;
  }

  public String getWebdavUrl(String language) {
    StringBuilder url = new StringBuilder(500);
    String webAppContext = URLManager.getApplicationURL();
    if (!webAppContext.endsWith("/")) {
      webAppContext = webAppContext + '/';
    }
    url.append(webAppContext).append(GeneralPropertiesManager.getString("webdav.respository")).
        append('/').append(GeneralPropertiesManager.getString("webdav.workspace")).append('/').
        append(getJcrPath(getLanguage(language)));
    return url.toString();
  }

  public String getJcrPath(String language) {
    StringBuilder jcrPath = new StringBuilder(500);
    jcrPath.append(ATTACHMENTS_FOLDER).append('/').append(getInstanceId()).append('/');
    if (StringUtil.isDefined(this.context)) {
      String[] elements = FileRepositoryManager.getAttachmentContext(this.context);
      for (String element : elements) {
        jcrPath.append(element).append('/');
      }
    }
    if (getPK().getId() != null) {
      jcrPath.append(getPK().getId()).append('/');
    }
    if (getLanguage(language) != null) {
      jcrPath.append(getLanguage(language)).append('/');
    }
    jcrPath.append(ConverterUtil.escapeIllegalJcrChars(getLogicalName(getLanguage(language))));
    return jcrPath.toString();
  }

  /**
   * Retourne l'URL de l'attachment pour l'utilisation externe (web). Cette URL est construite a
   * partir des autres informations
   *
   * @see
   */
  public String getWebURL() {
    SilverTrace.info("attachment", "Contructor getWebURL()",
        "root.MSG_GEN_PARAM_VALUE", "pk is not null !");
    String valret = FileServerUtils.getAttachmentURL(pk.getInstanceId(),
        logicalName, pk.getId(), getLanguage());
    SilverTrace.info("attachment", "Contructor getWebURL()",
        "root.MSG_GEN_PARAM_VALUE", valret);
    return valret;
  }

  /**
   * Retourne l'URL de l'attachment afin que le téléchargement soit enregistré. Cette URL est
   * construite a partir des autres informations
   *
   * @see
   */
  public String getAttachmentURLToMemorize(String userId, String fatherId) {
    String valret = "";
    int attGr = getAttachmentGroup();

    if (attGr == GROUP_HTML_LINK) {
      valret = physicalName;
    } else {
      if ((attGr == GROUP_FILE) || (attGr == GROUP_DIR)) {
        String theContext =
            FileRepositoryManager.getRelativePath(
            FileRepositoryManager.getAttachmentContext(context));
        int nodeId = 0;
        if (fatherId == null) {
          nodeId = Integer.parseInt(fatherId);
        }
        valret = FileServerUtils.getUrl(pk.getSpace(), pk.getComponentName(),
            userId, logicalName, physicalName, type, true, Integer.parseInt(
            foreignKey.getId()), nodeId, theContext);
        SilverTrace.info("attachment",
            "AttachmentDetail.getAttachmentURLToMemorize",
            "root.MSG_GEN_PARAM_VALUE",
            "(attGr == GROUP_FILE) || (attGr == GROUP_DIR) : url = " + valret);
      } else {
        valret = FileServerUtils.getUrl(logicalName, physicalName, type);
        SilverTrace.info("attachment",
            "AttachmentDetail.getAttachmentURLToMemorize",
            "root.MSG_GEN_PARAM_VALUE",
            "(attGr != GROUP_FILE) && (attGr != GROUP_DIR) : url = " + valret);
      }
    }
    SilverTrace.info("attachment",
        "AttachmentDetail.getAttachmentURLToMemorize",
        "root.MSG_GEN_PARAM_VALUE", "physicalName = " + physicalName);

    valret += "&attachmentId=" + pk.getId();

    if (physicalName != null) {
      String extension = FileRepositoryManager.getFileExtension(physicalName);
      if ("exe".equalsIgnoreCase(extension)
          || "pdf".equalsIgnoreCase(extension)) {
        valret += "&logicalName="
            + FileServerUtils.replaceSpecialChars(logicalName);
      }
    }

    SilverTrace.info("attachment",
        "AttachmentDetail.getAttachmentURLToMemorize",
        "root.MSG_GEN_PARAM_VALUE", "valret = " + valret);
    return valret;
  }

  /**
   * Retourne l'extension de l'icone de l'attachment.
   *
   * @see
   */
  public String getAttachmentIcon(String language) {
    String valret = "";
    if (getPhysicalName(language).lastIndexOf('.') >= 0) {
      String fileType = FileRepositoryManager.getFileExtension(getPhysicalName(language));
      valret = FileRepositoryManager.getFileIcon(fileType);
    } else {
      if (getAttachmentGroup() == GROUP_HTML_LINK || getAttachmentGroup() == GROUP_DUMMY) {
        valret = FileRepositoryManager.getFileIcon("html");
      }
    }
    return valret;
  }

  public String getAttachmentIcon() {
    return getAttachmentIcon(null);
  }

  /**
   * Retourne la taille du fichier lie sour forme de string.
   *
   * @see
   */
  public String getAttachmentFileSize(String language) {
    String valret = "";

    int attGr = getAttachmentGroup();
    if (attGr == GROUP_FILE || attGr == GROUP_FILE_LINK || attGr == GROUP_DIR) {
      valret = FileRepositoryManager.formatFileSize(getSize(language));
    } else {
      valret = FileRepositoryManager.formatFileSize(0);
    }
    return valret;
  }

  public String getAttachmentFileSize() {
    return getAttachmentFileSize(null);
  }

  /**
   * Retourne la taille du fichier lie sour forme de string.
   *
   * @see
   */
  public String getAttachmentDownloadEstimation(String language) {
    int attGr = getAttachmentGroup();

    if (attGr == GROUP_FILE || attGr == GROUP_FILE_LINK || attGr == GROUP_DIR) {
      return FileRepositoryManager.getFileDownloadTime(getSize(language));
    } else {
      return FileRepositoryManager.getFileDownloadTime(0);
    }
  }

  public String getAttachmentDownloadEstimation() {
    return getAttachmentDownloadEstimation(null);
  }

  public boolean isAttachmentOffset(String lastDirContext) {
    if (lastDirContext == null || lastDirContext.length() <= 0) {
      return false;
    } else {
      return lastDirContext.startsWith(context);
    }
  }

  public boolean isAttachmentLinked() {
    return (getAttachmentGroup() == GROUP_FILE_LINK);
  }

  public boolean isOfficeDocument(String language) {
    return isOpenOfficeCompatible(language);
  }

  /**
   * Return the path to the attachment file.
   *
   * @param language the language for the file.
   * @return the path to the attachment file.
   */
  public String getAttachmentPath(String language) {
    String directory = FileRepositoryManager.getAbsolutePath(getInstanceId(),
        FileRepositoryManager.getAttachmentContext(getContext()));
    if (!directory.endsWith(File.separator)) {
      directory = directory + File.separatorChar;
    }
    directory = directory.replace('/', File.separatorChar);
    return directory + getPhysicalName(language);
  }

  /**
   * Check if the attachment is compatible with OpenOffice using the mime type .
   *
   * @return true if the attachment is compatible with OpenOffice false otherwise.
   */
  public boolean isOpenOfficeCompatible() {
    return isOpenOfficeCompatible(null);
  }

  /**
   * Check if the attachment for the specified language is compatible with OpenOffice using the mime
   * type .
   *
   * @param language the language of the attachment.
   * @return true if the attachment is compatible with OpenOffice false otherwise.
   */
  public boolean isOpenOfficeCompatible(String language) {
    return FileUtil.isOpenOfficeCompatible(getLogicalName(language));
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

  /**
   * If 3d document
   *
   * @param language
   * @return true or false
   */
  public boolean isSpinfireDocument(String language) {
    boolean isSpinfireDocument = false;
    if (getType(language) != null) {
      isSpinfireDocument = getType(language).equals(SPINFIRE_MIME_TYPE);
    }
    SilverTrace.info("attachment", "AttachmentDetail.isSpinfireDocument()",
        "root.MSG_GEN_PARAM_VALUE", "isSpinfireDocument = "
        + isSpinfireDocument);
    return isSpinfireDocument;
  }

  public boolean isSpinfireDocument() {
    boolean isSpinfireDocument = false;
    if (getType() != null) {
      SilverTrace.info("attachment", "AttachmentDetail.isSpinfireDocument()",
          "root.MSG_GEN_PARAM_VALUE", "isSpinfireDocument = "
          + getType().equals(SPINFIRE_MIME_TYPE));
      isSpinfireDocument = getType().equals(SPINFIRE_MIME_TYPE);
    }
    return isSpinfireDocument;
  }

  /**
   * Is the Office file in read-only mode ?
   *
   * @return
   * @throws AttachmentException
   */
  public boolean isReadOnly() {
    boolean isReadOnly = false;
    isReadOnly = (getWorkerId() != null);
    SilverTrace.info("attachment", "AttachmentDetail.isReadOnly()",
        "root.MSG_GEN_PARAM_VALUE", "isReadOnly = " + isReadOnly);

    return isReadOnly;
  }

  public String getExtension() {
    return FileRepositoryManager.getFileExtension(logicalName);
  }

  @Override
  public Object clone() {
    AttachmentDetail clone = new AttachmentDetail();
    clone.setAuthor(author);
    clone.setCloneId(cloneId);
    clone.setContext(context);
    clone.setCreationDate(creationDate);
    clone.setDescription(description);
    clone.setForeignKey(foreignKey);
    clone.setInfo(info);
    clone.setInstanceId(instanceId);
    clone.setLogicalName(logicalName);
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

  public void setImportUpdateRule(String importUpdateRule) {
    this.importUpdateRule = importUpdateRule;
  }

  private AttachmentDetailI18N getAttachment(String language) {
    AttachmentDetailI18N p = (AttachmentDetailI18N) getTranslations().get(
        language);
    if (p == null) {
      p = (AttachmentDetailI18N) getNextTranslation();
    }
    return p;
  }

  public Date getReservationDate() {
    return reservationDate;
  }

  public void setReservationDate(Date reservationDate) {
    this.reservationDate = reservationDate;
  }

  public Date getAlertDate() {
    return alertDate;
  }

  public void setAlertDate(Date alertDate) {
    this.alertDate = alertDate;
  }

  public Date getExpiryDate() {
    return expiryDate;
  }

  public void setExpiryDate(Date expiryDate) {
    this.expiryDate = expiryDate;
  }

  /**
   * if type is known as application/octet-stream, try to find right mimeType
   */
  public void checkMimeType() {
    if (logicalName != null
        && (!StringUtil.isDefined(type) || DEFAULT_MIME_TYPE.equalsIgnoreCase(type))) {
      type = FileUtil.getMimeType(logicalName);
    }
  }

  public String getXmlForm() {
    return xmlForm;
  }

  public String getXmlForm(String language) {
    if (language == null || !I18NHelper.isI18N) {
      return getXmlForm();
    }
    AttachmentDetailI18N detail = getAttachment(language);
    if (detail == null) {
      return getXmlForm();
    }
    return detail.getXmlForm();
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