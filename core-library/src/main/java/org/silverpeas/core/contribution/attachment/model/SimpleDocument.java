/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.attachment.model;

import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.AccessControlOperation;
import org.silverpeas.core.security.authorization.AccessController;
import org.silverpeas.core.security.authorization.AccessControllerProvider;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.URLEncoder;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.security.authorization.SimpleDocumentAccessControl;
import org.silverpeas.core.contribution.attachment.WebdavServiceProvider;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.i18n.I18NHelper;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

import static java.io.File.separatorChar;
import static org.silverpeas.core.i18n.I18NHelper.defaultLanguage;

/**
 *
 * @author ehugonnet
 */
public class SimpleDocument implements Serializable {

  private static final long serialVersionUID = 8778738762037114180L;
  private final static SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.util.attachment.Attachment");
  public static final String WEBDAV_FOLDER = "webdav";
  public final static String ATTACHMENT_PREFIX = "attach_";
  public final static String VERSION_PREFIX = "version_";
  public final static String FILE_PREFIX = "file_";
  public final static String DOCUMENT_PREFIX = "simpledoc_";
  private String repositoryPath;
  private SimpleDocument versionMaster = this;
  private int versionIndex = 0;
  private SimpleDocumentPK pk;
  private String foreignId;
  private int order;
  private boolean versioned;
  private String webdavContentEditionLanguage;
  private long webdavContentEditionSize = -1;
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
  private Set<SilverpeasRole> forbiddenDownloadForRoles = null;

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
  private SimpleAttachment attachment;

  public SimpleDocument(SimpleDocumentPK pk, String foreignId, int order, boolean versioned,
      SimpleAttachment attachment) {
    this(pk, foreignId, order, versioned, null, attachment);
  }

  public SimpleDocument(SimpleDocumentPK pk, String foreignId, int order, boolean versioned,
      String editedBy, SimpleAttachment attachment) {
    this(pk, foreignId, order, versioned, editedBy, null, null, null, null, attachment);
  }

  public SimpleDocument(SimpleDocumentPK pk, String foreignId, int order, boolean versioned,
      Date reservation, Date alert, Date expiry, String comment, SimpleAttachment attachment) {
    this.pk = pk;
    this.foreignId = foreignId;
    this.order = order;
    this.versioned = versioned;
    setReservation(reservation);
    this.alert = DateUtil.getBeginOfDay(alert);
    this.expiry = DateUtil.getBeginOfDay(expiry);
    this.comment = comment;
    this.attachment = attachment;
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
   * @param attachment
   */
  public SimpleDocument(SimpleDocumentPK pk, String foreignId, int order, boolean versioned,
      String editedBy, Date reservation, Date alert, Date expiry, String comment,
      SimpleAttachment attachment) {
    this.pk = pk;
    this.foreignId = foreignId;
    this.order = order;
    this.versioned = versioned;
    this.editedBy = editedBy;
    setReservation(reservation);
    this.alert = DateUtil.getBeginOfDay(alert);
    this.expiry = DateUtil.getBeginOfDay(expiry);
    this.comment = comment;
    this.attachment = attachment;
  }

  public SimpleDocument() {
  }

  public SimpleDocument(SimpleDocument simpleDocument) {
    this.repositoryPath = simpleDocument.getRepositoryPath();
    this.versionMaster = simpleDocument.getVersionMaster();
    this.versionIndex = simpleDocument.getVersionIndex();
    this.pk = simpleDocument.getPk();
    this.foreignId = simpleDocument.getForeignId();
    this.order = simpleDocument.getOrder();
    this.versioned = simpleDocument.isVersioned();
    this.editedBy = simpleDocument.getEditedBy();
    this.reservation = simpleDocument.getReservation();
    this.alert = simpleDocument.getAlert();
    this.expiry = simpleDocument.getExpiry();
    this.status = simpleDocument.getStatus();
    this.cloneId = simpleDocument.getCloneId();
    this.minorVersion = simpleDocument.getMinorVersion();
    this.majorVersion = simpleDocument.getMajorVersion();
    this.publicDocument = simpleDocument.isPublic();
    this.nodeName = simpleDocument.getNodeName();
    this.comment = simpleDocument.getComment();
    this.documentType = simpleDocument.getDocumentType();
    this.forbiddenDownloadForRoles = simpleDocument.forbiddenDownloadForRoles;
    this.attachment = simpleDocument.getAttachment();
  }

  public String getFilename() {
    return getAttachment().getFilename();
  }

  public void setFilename(String filename) {
    getAttachment().setFilename(filename);
  }

  public String getLanguage() {
    return getAttachment().getLanguage();
  }

  public void setLanguage(String language) {
    getAttachment().setLanguage(language);
  }

  public String getTitle() {
    return getAttachment().getTitle();
  }

  public void setTitle(String title) {
    getAttachment().setTitle(title);
  }

  public String getDescription() {
    return getAttachment().getDescription();
  }

  public void setDescription(String description) {
    getAttachment().setDescription(description);
  }

  public long getSize() {
    return getAttachment().getSize();
  }

  public void setSize(long size) {
    getAttachment().setSize(size);
  }

  public String getContentType() {
    return getAttachment().getContentType();
  }

  public void setContentType(String contentType) {
    getAttachment().setContentType(contentType);
  }

  public String getCreatedBy() {
    return getAttachment().getCreatedBy();
  }

  public Date getCreated() {
    return getAttachment().getCreated();
  }

  public void setCreated(Date created) {
    getAttachment().setCreated(created);
  }

  public String getUpdatedBy() {
    return getAttachment().getUpdatedBy();
  }

  public void setUpdatedBy(String updatedBy) {
    getAttachment().setUpdatedBy(updatedBy);
  }

  public Date getUpdated() {
    return getAttachment().getUpdated();
  }

  public void setUpdated(Date updated) {
    getAttachment().setUpdated(updated);
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
    if(alert == null) {
      this.alert = null;
    } else {
      this.alert = DateUtil.getBeginOfDay(alert);
    }
  }

  public Date getExpiry() {
    if (expiry == null) {
      return null;
    }
    return new Date(expiry.getTime());
  }

  public void setExpiry(Date expiry) {
    if (expiry == null) {
      this.expiry = null;
    } else {
      this.expiry = DateUtil.getBeginOfDay(expiry);
    }
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

  /**
   * Gets a version number as String.
   * For now, this is the concatenation of major and minor version data that are separated by a
   * point.
   * @return
   */
  public String getVersion() {
    return getMajorVersion() + "." + getMinorVersion();
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  protected void resetWebdavContentEditionContext() {
    this.webdavContentEditionLanguage = null;
    this.webdavContentEditionSize = -1;
  }

  /**
   * Gets the content language handled into webdav for the document.
   * @return the content language if the document is currently handled into the webdav repository,
   * empty string otherwise.
   */
  public String getWebdavContentEditionLanguage() {
    if (webdavContentEditionLanguage == null) {
      // To be handled into webdav repository, the document must be an open office compatible,
      // and it must also be read only (reservation)
      if (isOpenOfficeCompatible() && isReadOnly()) {
        // The method has not been called yet.
        // Firstly searching through Webdav services the information.
        webdavContentEditionLanguage =
            WebdavServiceProvider.getWebdavService().getContentEditionLanguage(getVersionMaster());
      }
      // If null, it indicates that the document does not exists into webdav repository.
      // The class attribute is initialized to empty value.
      if (webdavContentEditionLanguage == null) {
        webdavContentEditionLanguage = StringUtil.EMPTY;
      }
    }
    return webdavContentEditionLanguage;
  }

  /**
   * Gets the content size handled into webdav for the document.
   * @return the content size if the document is currently handled into the webdav repository,
   * a value less than zero otherwise.
   */
  public long getWebdavContentEditionSize() {
    if (webdavContentEditionSize < 0) {
      // To be handled into webdav repository, the document must be an open office compatible,
      // and it must also be read only (reservation)
      if (isOpenOfficeCompatible() && isReadOnly()) {
        // The method has not been called yet.
        // Firstly searching through Webdav services the information.
        webdavContentEditionSize =
            WebdavServiceProvider.getWebdavService().getContentEditionSize(getVersionMaster());
      }
      // If negative value, it indicates that the document does not exists into webdav repository.
      // The class attribute is initialized to zero.
      if (webdavContentEditionSize < 0) {
        webdavContentEditionSize = 0;
      }
    }
    return webdavContentEditionSize;
  }

  public String getEditedBy() {
    return editedBy;
  }

  public void edit(String currentEditor) {
    resetWebdavContentEditionContext();
    this.editedBy = currentEditor;
    setReservation(new Date());
    String day =
        OrganizationControllerProvider.getOrganisationController()
            .getComponentParameterValue(getInstanceId(), "nbDayForReservation");

    if (StringUtil.isInteger(day)) {
      int nbDay = Integer.parseInt(day);
      Calendar calendar = Calendar.getInstance();
      DateUtil.addDaysExceptWeekEnds(calendar, nbDay);
      setExpiry(calendar.getTime());

      int delayReservedFile = settings.getInteger("DelayReservedFile", -1);
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
    resetWebdavContentEditionContext();
    this.editedBy = null;
    setReservation(null);
    setExpiry(null);
    setAlert(null);
  }

  public String getXmlFormId() {
    return getAttachment().getXmlFormId();
  }

  public void setXmlFormId(String xmlFormId) {
    getAttachment().setXmlFormId(xmlFormId);
  }

  public String getId() {
    if (getPk() != null) {
      return getPk().getId();
    }
    return null;
  }

  public void setId(String id) {
    if (getPk() != null) {
      getPk().setId(id);
    } else {
      setPK(new SimpleDocumentPK(id));
    }
  }

  public void setPK(SimpleDocumentPK pk) {
    this.pk = pk;
  }

  public String getInstanceId() {
    return getPk().getInstanceId();
  }

  public long getOldSilverpeasId() {
    return getPk().getOldSilverpeasId();
  }

  public void setOldSilverpeasId(long oldSilverpeasId) {
    getPk().setOldSilverpeasId(oldSilverpeasId);
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

  public SimpleAttachment getAttachment() {
    return attachment;
  }

  public SimpleDocumentPK getPk() {
    return this.pk;
  }

  public void setAttachment(SimpleAttachment attachment) {
    this.attachment = attachment;
  }

  public boolean isPublic() {
    return publicDocument;
  }

  public void setPublicDocument(boolean publicDocument) {
    this.publicDocument = publicDocument;
  }

  public void unlock() {
    resetWebdavContentEditionContext();
    this.editedBy = null;
    setExpiry(null);
    setAlert(null);
  }

  public String getNodeName() {
    return nodeName;
  }

  public void setNodeName(String nodeName) {
    this.nodeName = nodeName;
  }

  public String computeNodeName() {
    if (!StringUtil.isDefined(getNodeName())) {
      if (getOldSilverpeasId() <= 0L) {
        try {
          setOldSilverpeasId(DBUtil.getNextId("sb_simple_document", "id"));
        } catch (SQLException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
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
    return getFullJcrPath() + '/' + getAttachment().getNodeName();
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
    return "SimpleDocument{" + getNodeName() + " pk=" + getPk() + ", foreignId=" + getForeignId() +
        ", order=" + getOrder() + ", versioned=" + isVersioned() + ", editedBy=" + getEditedBy() +
        ", reservation=" + getReservation() + ", alert=" + getAlert() + ", expiry=" + getExpiry() +
        ", status=" + getStatus() + ", cloneId=" + getCloneId() + ", attachment=" + getAttachment() +
        ", minorVersion=" + getMinorVersion() + ", majorVersion=" + getMajorVersion() +
        ", comment=" + getComment() + '}';
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 31 * hash + (getPk() != null ? getPk().hashCode() : 0);
    hash = 31 * hash + getMinorVersion();
    hash = 31 * hash + getMajorVersion();
    hash = 31 * hash + getVersionIndex();
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
    if (getPk() != other.getPk() &&
        (getPk() == null || !getPk().equals(other.getPk()))) {
      return false;
    }
    if (getMinorVersion() != other.getMinorVersion()) {
      return false;
    }
    if (getMajorVersion() != other.getMajorVersion()) {
      return false;
    }
    if (getVersionIndex() != other.getVersionIndex()) {
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
    return FileServerUtils
        .getAttachmentURL(getPk().getInstanceId(), getFilename(), getPk().getId(), getLanguage());
  }

  public String getUniversalURL() {
    return URLUtil.getSimpleURL(URLUtil.URL_FILE, getId()) + "?ContentLanguage=" +
        getLanguage();
  }

  public String getOnlineURL() {
    String onlineUrl = FileServerUtils
        .getOnlineURL(getPk().getComponentName(), getFilename(), "", getContentType(), "");
    String extension = FileRepositoryManager.getFileExtension(getFilename());
    if ("exe".equalsIgnoreCase(extension) || "pdf".equalsIgnoreCase(extension)) {
      onlineUrl += "&logicalName=" + URLEncoder.encodePathParamValue(getFilename());
    }
    return onlineUrl;
  }

  public String getAliasURL() {
    String aliasUrl =
        FileServerUtils.getAliasURL(getPk().getInstanceId(), getFilename(), getPk().getId());
    if (I18NHelper.isI18nContentActivated && !I18NHelper.isDefaultLanguage(getLanguage())) {
      aliasUrl += "&lang=" + getLanguage();
    }
    String extension = FileRepositoryManager.getFileExtension(getFilename());
    if ("exe".equalsIgnoreCase(extension) || "pdf".equalsIgnoreCase(extension)) {
      aliasUrl += "&logicalName=" + URLEncoder.encodePathParamValue(getFilename());
    }
    return aliasUrl;
  }

  public String getWebdavUrl() {
    StringBuilder url = new StringBuilder(500);
    String webAppContext = URLUtil.getApplicationURL();
    url.append(webAppContext);
    if (!webAppContext.endsWith("/")) {
      url.append('/');
    }
    url.append(ResourceLocator.getGeneralSettingBundle().getString("webdav.respository")).
        append('/').
        append(ResourceLocator.getGeneralSettingBundle().getString("webdav.workspace")).
        append('/').
        append(getWebdavJcrPath());

    return url.toString();
  }

  public String getWebdavJcrPath() {
    StringBuilder jcrPath = new StringBuilder(500);
    jcrPath.append(WEBDAV_FOLDER).append('/').append(DocumentType.attachment.getFolderName()).append(
        '/').
        append(getVersionMaster().getInstanceId()).append('/');
    if (getVersionMaster().getId() != null) {
      jcrPath.append(getVersionMaster().getId()).append('/');
    }
    if (getLanguage() != null) {
      jcrPath.append(getVersionMaster().getLanguage()).append('/');
    }
    jcrPath.append(StringUtil.escapeQuote(getVersionMaster().getFilename()));
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
    return FileServerUtils
        .getAttachmentURL(getPk().getInstanceId(), getFilename(), getPk().getId(), getLanguage());
  }

  /**
   * Returns the master of versioned document.
   * If not versionned, it returns itself.
   * If versioned, it returns the master of versioned document (the last created or updated in
   * other words).
   * @return
   */
  public SimpleDocument getVersionMaster() {
    return versionMaster;
  }

  public void setVersionMaster(final SimpleDocument versionMaster) {
    this.versionMaster = versionMaster;
  }

  /**
   * Indicates if the current instance is a master one.
   * @return true if current instance is master, false otherwise.
   */
  public boolean isVersionMaster() {
    return this == getVersionMaster();
  }

  /**
   * Returns the path into the repository.
   * @return
   */
  public String getRepositoryPath() {
    return repositoryPath;
  }

  public void setRepositoryPath(final String repositoryPath) {
    this.repositoryPath = repositoryPath;
  }

  /**
   * Returns the index of document into the history if any and if the document is a versioned one.
   * In other cases, it returns 0 (the start index).
   * @return
   */
  public int getVersionIndex() {
    return versionIndex;
  }

  public void setVersionIndex(final int versionIndex) {
    this.versionIndex = versionIndex;
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
    return getDocumentType().getFolderName();
  }

  public boolean isSharingAllowedForRolesFrom(final UserDetail user) {
    if (user == null || StringUtil.isNotDefined(user.getId()) || !user.isValidState()) {
      // In that case, from point of security view if no user data exists,
      // then download is forbidden.
      return false;
    }

    // Access is verified for sharing context
    AccessController<SimpleDocument> accessController = AccessControllerProvider
        .getAccessController(SimpleDocumentAccessControl.class);
    return accessController.isUserAuthorized(user.getId(), getVersionMaster(),
        AccessControlContext.init().onOperationsOf(AccessControlOperation.sharing));
  }

  /**
   * Is the specified user can access this document?
   * @param user a user in Silverpeas.
   * @return true if the user can access this document, false otherwise.
   */
  public boolean canBeAccessedBy(final UserDetail user) {
    AccessController<SimpleDocument> accessController =
        AccessControllerProvider.getAccessController(SimpleDocumentAccessControl.class);
    return accessController.isUserAuthorized(user.getId(), this);
  }

  /**
   * Is the specified user can access this document on persist context?
   * @param user a user in Silverpeas.
   * @return true if the user can access this document, false otherwise.
   */
  public boolean canBeModifiedBy(final UserDetail user) {
    AccessController<SimpleDocument> accessController =
        AccessControllerProvider.getAccessController(SimpleDocumentAccessControl.class);
    return accessController.isUserAuthorized(user.getId(), this,
        AccessControlContext.init().onOperationsOf(AccessControlOperation.modification));
  }

  /**
   * Indicates if the download of the document is allowed for the given user in relation to its
   * roles.
   * DON'T USE THIS METHOD IN CASE OF LARGE NUMBER OF ATTACHMENT TO PERFORM.
   * @param user
   * @return true if download is allowed.
   */
  public boolean isDownloadAllowedForRolesFrom(final UserDetail user) {
    if (user == null || StringUtil.isNotDefined(user.getId()) || !user.isValidState()) {
      // In that case, from point of security view if no user data exists,
      // then download is forbidden.
      return false;
    }

    if (CollectionUtil.isEmpty(getVersionMaster().forbiddenDownloadForRoles)) {
      // In that case, there is no reason to verify user role informations because it doesn't
      // exists any restriction for downloading.
      return true;
    }

    // Otherwise access is verified for download context
    AccessController<SimpleDocument> accessController = AccessControllerProvider
        .getAccessController(SimpleDocumentAccessControl.class);
    return accessController.isUserAuthorized(user.getId(), getVersionMaster(),
        AccessControlContext.init().onOperationsOf(AccessControlOperation.download));
  }

  /**
   * Indicates if the download of the document is allowed for the given roles.
   * @param roles
   * @return true if download is allowed.
   */
  public boolean isDownloadAllowedForRoles(final Set<SilverpeasRole> roles) {
    if (CollectionUtil.isEmpty(roles)) {
      return false;
    }

    if (CollectionUtil.isEmpty(getVersionMaster().forbiddenDownloadForRoles)) {
      // In that case, there is no reason to verify user role informations because it doesn't
      // exists any restriction for downloading.
      return true;
    }

    // If the intersection of the allowed roles compared to the given ones is empty,
    // then the download is allowed.
    return SilverpeasRole.getGreaterFrom(roles)
        .isGreaterThan(SilverpeasRole.getGreaterFrom(getVersionMaster().forbiddenDownloadForRoles));
  }

  /**
   * Indicates if the download is allowed for readers.
   */
  public boolean isDownloadAllowedForReaders() {
    return isDownloadAllowedForRoles(SilverpeasRole.READER_ROLES);
  }

  /**
   * Forbids the download for the given roles.
   * PLEASE BE CAREFUL : this method doesn't persist the information. It is used by attachment
   * services during the conversion from JCR data to SimpleDocument data.
   * @param forbiddenRoles
   * @return true if roles were not forbidden before the call of this method.
   */
  public boolean addRolesForWhichDownloadIsForbidden(final SilverpeasRole... forbiddenRoles) {
    return addRolesForWhichDownloadIsForbidden(Arrays.asList(forbiddenRoles));
  }

  /**
   * Forbids the download for the given roles.
   * PLEASE BE CAREFUL : this method doesn't persist the information. It is used by attachment
   * services during the conversion from JCR data to SimpleDocument data.
   * @param forbiddenRoles
   * @return true if roles were not forbidden before the call of this method.
   */
  public boolean addRolesForWhichDownloadIsForbidden(
      final Collection<SilverpeasRole> forbiddenRoles) {
    if (CollectionUtil.isNotEmpty(forbiddenRoles)) {
      if (getVersionMaster().forbiddenDownloadForRoles == null) {
        getVersionMaster().forbiddenDownloadForRoles = EnumSet.noneOf(SilverpeasRole.class);
      }
      return getVersionMaster().forbiddenDownloadForRoles.addAll(forbiddenRoles);
    }
    return false;
  }

  /**
   * Allows the download for the given roles.
   * PLEASE BE CAREFUL : this method doesn't persist the information. It is used by attachment
   * services during the conversion from JCR data to SimpleDocument data.
   * @param allowedRoles
   * @return true if roles were not allowed before the call of this method.
   */
  public boolean addRolesForWhichDownloadIsAllowed(final SilverpeasRole... allowedRoles) {
    return addRolesForWhichDownloadIsAllowed(Arrays.asList(allowedRoles));
  }

  /**
   * Allows the download for the given roles.
   * PLEASE BE CAREFUL : this method doesn't persist the information. It is used by attachment
   * services during the conversion from JCR data to SimpleDocument data.
   * @param allowedRoles
   * @return true if roles were not allowed before the call of this method.
   */
  public boolean addRolesForWhichDownloadIsAllowed(final Collection<SilverpeasRole> allowedRoles) {
    return CollectionUtil.isNotEmpty(allowedRoles) &&
        CollectionUtil.isNotEmpty(getVersionMaster().forbiddenDownloadForRoles) &&
        getVersionMaster().forbiddenDownloadForRoles.removeAll(allowedRoles);
  }

  /**
   * Gets roles for which download is not allowed.
   * @return
   */
  public Set<SilverpeasRole> getForbiddenDownloadForRoles() {
    return (getVersionMaster().forbiddenDownloadForRoles != null) ?
        Collections.unmodifiableSet(getVersionMaster().forbiddenDownloadForRoles) : null;
  }

  /**
   * Indicates if the file described by current {@link SimpleAttachment} is type of image.
   */
  public boolean isContentImage() {
    return FileUtil.isImage(getAttachmentPath());
  }

  /**
   * Indicates if the file described by current {@link SimpleAttachment} is type of 3D.
   */
  public boolean isContentSpinfire() {
    return FileUtil.isSpinfireDocument(getAttachmentPath());
  }

  /**
   * Indicates if the file described by current {@link SimpleAttachment} is type of archive.
   */
  public boolean isContentArchive() {
    return FileUtil.isArchive(getAttachmentPath());
  }

  /**
   * Indicates if the file described by current {@link SimpleAttachment} is type of mail.
   */
  public boolean isContentMail() {
    return FileUtil.isMail(getAttachmentPath());
  }

  /**
   * Indicates if the file described by current {@link SimpleAttachment} is type of pdf.
   */
  public boolean isContentPdf() {
    return FileUtil.isPdf(getAttachmentPath());
  }
}
