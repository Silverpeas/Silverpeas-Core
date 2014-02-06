/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;

import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.util.URLUtils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import static com.silverpeas.util.i18n.I18NHelper.defaultLanguage;
import static java.io.File.separatorChar;

/**
 *
 * @author ehugonnet
 */
public class SimpleDocument implements Serializable {

  private static final long serialVersionUID = 8778738762037114180L;
  private final static ResourceLocator resources = new ResourceLocator(
      "org.silverpeas.util.attachment.Attachment", "");
  public static final String WEBDAV_FOLDER = "webdav";
  public final static String ATTACHMENT_PREFIX = "attach_";
  public final static String VERSION_PREFIX = "version_";
  public final static String FILE_PREFIX = "file_";
  public final static String DOCUMENT_PREFIX = "simpledoc_";
  private SimpleDocumentPK pk;
  private String foreignId;
  private int order;
  private boolean versioned;
  private String editedBy;
  private Date reservation;
  private Date alert;
  private Date expiry;
  private String status;
  private String cloneId;
  private int minorVersion = 0;
  private int majorVersion = 0;
  private boolean publicDocument = true;
  private String nodeName;
  private String comment;
  private DocumentType documentType = DocumentType.attachment;

  public void setDocumentType(DocumentType documentType) {
    this.documentType = documentType;
  }

  public DocumentType getDocumentType() {
    return documentType;
  }

  /**
   * Get the value of cloneId
   *
   * @return the value of cloneId
   */
  public String getCloneId() {
    return cloneId;
  }

  /**
   * Set the value of cloneId
   *
   * @param cloneId new value of cloneId
   */
  public void setCloneId(String cloneId) {
    this.cloneId = cloneId;
  }
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
      Date reservation, Date alert, Date expiry, String comment, SimpleAttachment file) {
    this.pk = pk;
    this.foreignId = foreignId;
    this.order = order;
    this.versioned = versioned;
    setReservation(reservation);
    this.alert = DateUtil.getBeginOfDay(alert);
    this.expiry = DateUtil.getBeginOfDay(expiry);
    this.comment = comment;
    this.file = file;
  }

  /**
   *
   * @param pk
   * @param foreignId
   * @param order
   * @param versioned
   * @param editedBy
   * @param reservation
   * @param alert
   * @param expiry
   * @param comment
   * @param file
   */
  public SimpleDocument(SimpleDocumentPK pk, String foreignId, int order, boolean versioned,
      String editedBy, Date reservation, Date alert, Date expiry, String comment,
      SimpleAttachment file) {
    this.pk = pk;
    this.foreignId = foreignId;
    this.order = order;
    this.versioned = versioned;
    this.editedBy = editedBy;
    setReservation(reservation);
    this.alert = DateUtil.getBeginOfDay(alert);
    this.expiry = DateUtil.getBeginOfDay(expiry);
    this.comment = comment;
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
    if (reservation == null) {
      return null;
    }
    return new Date(reservation.getTime());
  }

  public final void setReservation(Date reservationDate) {
    if (reservationDate == null) {
      this.reservation = null;
    } else {
      this.reservation = new Date(reservationDate.getTime());
    }
  }

  public Date getAlert() {
    if (alert == null) {
      return null;
    }
    return new Date(alert.getTime());
  }

  public void setAlert(Date alert) {
    this.alert = DateUtil.getBeginOfDay(alert);
  }

  public Date getExpiry() {
    if (expiry == null) {
      return null;
    }
    return new Date(expiry.getTime());
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
    return minorVersion;
  }

  public void setMinorVersion(int minorVersion) {
    this.minorVersion = minorVersion;
  }

  public int getMajorVersion() {
    return majorVersion;
  }

  public void setMajorVersion(int majorVersion) {
    this.majorVersion = majorVersion;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getEditedBy() {
    return editedBy;
  }

  public void edit(String currentEditor) {
    this.editedBy = currentEditor;
    this.reservation = new Date();
    String day =
        OrganisationControllerFactory.getOrganisationController()
            .getComponentParameterValue(getInstanceId(), "nbDayForReservation");

    if (StringUtil.isInteger(day)) {
      int nbDay = Integer.parseInt(day);
      Calendar calendar = Calendar.getInstance();
      DateUtil.addDaysExceptWeekEnds(calendar, nbDay);
      setExpiry(calendar.getTime());

      int delayReservedFile = resources.getInteger("DelayReservedFile", -1);
      if ((delayReservedFile >= 0) && (delayReservedFile <= 100)) {
        int result = (nbDay * delayReservedFile) / 100;
        if (result > 2) {
          calendar = Calendar.getInstance();
          DateUtil.addDaysExceptWeekEnds(calendar, result);
          setAlert(calendar.getTime());
        }
      }
    }
  }

  public void release() {
    this.editedBy = null;
    this.reservation = null;
    setExpiry(null);
    setAlert(null);
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

  public SimpleAttachment getFile() {
    return file;
  }

  public SimpleDocumentPK getPk() {
    return this.pk;
  }

  public void setFile(SimpleAttachment file) {
    this.file = file;
  }

  public boolean isPublic() {
    return publicDocument;
  }

  public void setPublicDocument(boolean publicDocument) {
    this.publicDocument = publicDocument;
  }

  public void unlock() {
    this.editedBy = null;
    this.expiry = null;
    this.alert = null;
  }

  public String getNodeName() {
    return nodeName;
  }

  public void setNodeName(String nodeName) {
    this.nodeName = nodeName;
  }

  public String computeNodeName() {
    if (!StringUtil.isDefined(nodeName)) {
      if (getOldSilverpeasId() <= 0L) {
        setOldSilverpeasId(DBUtil.getNextId("sb_simple_document", "id"));
      }
      setNodeName(DOCUMENT_PREFIX + getOldSilverpeasId());
      return getNodeName();
    }
    return getNodeName();
  }

  /**
   * Full JCR path to the file node.
   *
   * @return the full JCR path to the file node (starting with /).
   */
  public String getFullJcrContentPath() {
    return getFullJcrPath() + '/' + file.getNodeName();
  }

  /**
   * Full JCR path to the document node.
   *
   * @return the full JCR path to the document node (starting with /).
   */
  public String getFullJcrPath() {
    return '/' + getInstanceId() + '/' + getFolder() + '/' + getNodeName();
  }

  /**
   * Return the icon correponding to the file.
   *
   * @return
   */
  public String getDisplayIcon() {
    return FileRepositoryManager.getFileIcon(FileRepositoryManager.getFileExtension(getFilename()));
  }

  /**
   * Check if the document is compatible with OpenOffice using the mime type .
   *
   * @return true if the document is compatible with OpenOffice false otherwise.
   */
  public boolean isOpenOfficeCompatible() {
    return FileUtil.isOpenOfficeCompatible(getFilename());
  }

  public boolean isReadOnly() {
    return StringUtil.isDefined(getEditedBy());
  }

  /**
   * Path to the file stored on the filesystem.
   *
   * @return the path to the file stored on the filesystem.
   */
  public String getAttachmentPath() {
    String lang = getLanguage();
    if (!StringUtil.isDefined(lang)) {
      lang = defaultLanguage;
    }
    return getDirectoryPath(lang) + getFilename();
  }

  /**
   * Path to the directory where the file is to be stored.
   *
   * @param language the language of the document.
   * @return the path to the directory where the file is to be stored.
   */
  public String getDirectoryPath(String language) {
    String directory = FileRepositoryManager.getAbsolutePath(getInstanceId());
    directory = directory.replace('/', separatorChar);
    String versionDir = getMajorVersion() + "_" + getMinorVersion();
    String lang = language;
    if (!StringUtil.isDefined(lang)) {
      lang = defaultLanguage;
    }
    return directory + getNodeName() + separatorChar + versionDir + separatorChar + lang
        + separatorChar;
  }

  @Override
  public String toString() {
    return "SimpleDocument{" + nodeName + " pk=" + pk + ", foreignId=" + foreignId + ", order="
        + order + ", versioned=" + versioned + ", editedBy=" + editedBy + ", reservation="
        + reservation + ", alert=" + alert + ", expiry=" + expiry + ", status=" + status
        + ", cloneId=" + cloneId + ", file=" + file + ", minorVersion=" + minorVersion
        + ", majorVersion=" + majorVersion + ", comment=" + comment + '}';
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 31 * hash + (this.pk != null ? this.pk.hashCode() : 0);
    hash = 31 * hash + this.minorVersion;
    hash = 31 * hash + this.majorVersion;
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof SimpleDocument)) {
      return false;
    }
    final SimpleDocument other = (SimpleDocument) obj;
    if (this.pk != other.pk && (this.pk == null || !this.pk.equals(other.pk))) {
      return false;
    }
    if (this.minorVersion != other.minorVersion) {
      return false;
    }
    if (this.majorVersion != other.majorVersion) {
      return false;
    }
    return true;
  }

  /**
   * Returns the attachment URL.
   *
   * @return the attachment URL.
   */
  public String getAttachmentURL() {
    return FileServerUtils.getAttachmentURL(pk.getInstanceId(), getFilename(), pk.getId(),
        getLanguage());
  }

  public String getUniversalURL() {
    return URLManager.getSimpleURL(URLManager.URL_FILE, getId());
  }

  public String getOnlineURL() {
    String onlineUrl = FileServerUtils.getOnlineURL(pk.getComponentName(), getFilename(), "",
        getContentType(), "");
    String extension = FileRepositoryManager.getFileExtension(getFilename());
    if ("exe".equalsIgnoreCase(extension) || "pdf".equalsIgnoreCase(extension)) {
      onlineUrl += "&logicalName=" + URLUtils.encodePathParamValue(getFilename());
    }
    return onlineUrl;
  }

  public String getAliasURL() {
    String aliasUrl = FileServerUtils.getAliasURL(pk.getInstanceId(), getFilename(), pk.getId());
    if (I18NHelper.isI18N && !I18NHelper.isDefaultLanguage(getLanguage())) {
      aliasUrl += "&lang=" + getLanguage();
    }
    String extension = FileRepositoryManager.getFileExtension(getFilename());
    if ("exe".equalsIgnoreCase(extension) || "pdf".equalsIgnoreCase(extension)) {
      aliasUrl += "&logicalName=" + URLUtils.encodePathParamValue(getFilename());
    }
    return aliasUrl;
  }

  public String getWebdavUrl() {
    StringBuilder url = new StringBuilder(500);
    String webAppContext = URLManager.getApplicationURL();
    url.append(webAppContext);
    if (!webAppContext.endsWith("/")) {
      url.append('/');
    }
    url.append(URLUtils.encodePathSegment(GeneralPropertiesManager.getString("webdav.respository"))).
        append('/').
        append(URLUtils.encodePathSegment(GeneralPropertiesManager.getString("webdav.workspace")));

    String[] pathParts = StringUtil.split(getWebdavJcrPath(), '/');
    for(String pathElement : pathParts) {
      url.append('/');
      url.append((URLUtils.encodePathSegment(pathElement)));
    }
    return url.toString();
  }

  public String getWebdavJcrPath() {
    StringBuilder jcrPath = new StringBuilder(500);
    jcrPath.append(WEBDAV_FOLDER).append('/').append(DocumentType.attachment.getFolderName()).append('/').
        append(getInstanceId()).append('/');
    if (getId() != null) {
      jcrPath.append(getId()).append('/');
    }
    if (getLanguage() != null) {
      jcrPath.append(getLanguage()).append('/');
    }
    jcrPath.append(StringUtil.escapeQuote(getFilename()));
    return jcrPath.toString();
  }

  /**
   * Returns the attachment URL.
   *
   * @return the attachment URL.
   * @deprecated use getAttachmentURL instead.
   */
  @Deprecated
  public String getWebURL() {
    return FileServerUtils.getAttachmentURL(pk.getInstanceId(), getFilename(), pk.getId(),
        getLanguage());
  }

  /**
   * Returns the more recent public version of this document - null if none exists.
   *
   * @return the more recent public version of this document - null if none exists.
   */
  public SimpleDocument getLastPublicVersion() {
    return this;
  }

  public String getFolder() {
    return documentType.getFolderName();
  }
}
