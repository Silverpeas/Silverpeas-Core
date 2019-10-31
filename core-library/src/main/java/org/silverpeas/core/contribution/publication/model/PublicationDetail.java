/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.contribution.publication.model;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.TypeManager;
import org.silverpeas.core.contribution.content.form.XMLField;
import org.silverpeas.core.contribution.content.form.displayers.WysiwygFCKFieldDisplayer;
import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerProvider;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentInterface;
import org.silverpeas.core.contribution.model.ContributionContent;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.ContributionModel;
import org.silverpeas.core.contribution.model.I18nContribution;
import org.silverpeas.core.contribution.model.LocalizedContribution;
import org.silverpeas.core.contribution.model.WithAttachment;
import org.silverpeas.core.contribution.model.WithThumbnail;
import org.silverpeas.core.contribution.model.WysiwygContent;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.contribution.rating.model.ContributionRating;
import org.silverpeas.core.contribution.rating.model.ContributionRatingPK;
import org.silverpeas.core.contribution.rating.model.Rateable;
import org.silverpeas.core.contribution.rating.service.RatingService;
import org.silverpeas.core.contribution.template.form.service.FormTemplateService;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.i18n.AbstractI18NBean;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.index.indexing.model.IndexManager;
import org.silverpeas.core.io.media.image.thumbnail.control.ThumbnailController;
import org.silverpeas.core.io.media.image.thumbnail.model.ThumbnailDetail;
import org.silverpeas.core.reminder.WithReminder;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.AccessControlOperation;
import org.silverpeas.core.security.authorization.AccessController;
import org.silverpeas.core.security.authorization.AccessControllerProvider;
import org.silverpeas.core.security.authorization.PublicationAccessControl;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.xml.DateAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.silverpeas.core.SilverpeasExceptionMessages.failureOnGetting;
import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.util.StringUtil.split;

/**
 * This object contains the description of a publication
 */
@XmlRootElement(namespace = "http://www.silverpeas.org/exchange")
@XmlAccessorType(XmlAccessType.NONE)
public class PublicationDetail extends AbstractI18NBean<PublicationI18N>
    implements I18nContribution, SilverContentInterface, Rateable, Serializable,
    WithAttachment, WithThumbnail, WithReminder {
  private static final long serialVersionUID = 9199848912262605680L;

  public static final String DELAYED_VISIBILITY_AT_MODEL_PROPERTY = "DELAYED_VISIBILITY_AT";

  private PublicationPK pk;
  private String infoId;
  @XmlElement(name = "creationDate", namespace = "http://www.silverpeas.org/exchange")
  @XmlJavaTypeAdapter(DateAdapter.class)
  private Date creationDate;
  @XmlElement(name = "beginDate", namespace = "http://www.silverpeas.org/exchange")
  @XmlJavaTypeAdapter(DateAdapter.class)
  private Date beginDate;
  @XmlElement(name = "endDate", namespace = "http://www.silverpeas.org/exchange")
  @XmlJavaTypeAdapter(DateAdapter.class)
  private Date endDate;
  @XmlElement(name = "creatorId", namespace = "http://www.silverpeas.org/exchange")
  private String creatorId;
  @XmlElement(name = "creatorName", namespace = "http://www.silverpeas.org/exchange")
  private String creatorName;
  @XmlElement(name = "importance", namespace = "http://www.silverpeas.org/exchange")
  private int importance;
  @XmlElement(name = "version", namespace = "http://www.silverpeas.org/exchange")
  private String version;
  @XmlElement(name = "keywords", namespace = "http://www.silverpeas.org/exchange")
  private String keywords;
  private String content;
  @XmlElement(name = "status", namespace = "http://www.silverpeas.org/exchange")
  private String status;
  private Date updateDate;
  private String updaterId;
  private Date validateDate;
  private String validatorId;
  private String beginHour;
  private String endHour;
  private String author;
  private String targetValidatorId;
  private String cloneId;
  private String cloneStatus;
  private Date draftOutDate;
  // added for the components - PDC integration
  private String silverObjectId;
  private String iconUrl;
  private int explicitRank = -1;
  // added for the taglib
  private List<XMLField> xmlFields = null;
  // added for indexation
  private int indexOperation = IndexManager.ADD;
  // added for import/export
  private boolean statusMustBeChecked = true;
  private boolean updateDateMustBeSet = true;
  // ajouté pour les statistiques
  private int nbAccess = 0;
  private Visibility visibility = null;
  // added for export component
  public static final String DRAFT_STATUS = "Draft";
  public static final String VALID_STATUS = "Valid";
  public static final String TO_VALIDATE_STATUS = "ToValidate";
  public static final String REFUSED_STATUS = "Unvalidate";
  public static final String CLONE_STATUS = "Clone";
  public static final String TYPE = "Publication";
  private boolean alias = false;

  private ContributionRating contributionRating;

  /**
   * Default contructor, required for JAXB mapping in importExport.
   */
  public PublicationDetail() {
    // Nothing to do
  }

  public PublicationDetail(String name, String description, Period visibilityPeriod,
      String creatorId, String componentId) {
    this.pk = new PublicationPK("unknown", componentId);
    setName(name);
    setDescription(description);
    setVisibilityPeriod(visibilityPeriod);
    this.creatorId = creatorId;
  }

  public PublicationDetail(PublicationPK pk, String name, String description, Date creationDate,
      Date beginDate, Date endDate, String creatorId, int importance, String version,
      String keywords, String content) {
    this.pk = pk;
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.beginDate = beginDate;
    this.endDate = endDate;
    this.creatorId = creatorId;
    this.importance = importance;
    this.version = version;
    this.keywords = keywords;
    this.content = content;
  }

  /**
   * @param id
   * @param name
   * @param description
   * @param creationDate
   * @param beginDate
   * @param endDate
   * @param creatorId
   * @param importance
   * @param version
   * @param keywords
   * @param content
   */
  public PublicationDetail(String id, String name, String description, Date creationDate,
      Date beginDate, Date endDate, String creatorId, String importance, String version,
      String keywords, String content) {
    this.pk = new PublicationPK(id);
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.beginDate = beginDate;
    this.endDate = endDate;
    this.creatorId = creatorId;
    this.importance = new Integer(importance);
    this.version = version;
    this.keywords = keywords;
    this.content = content;
  }

  public PublicationDetail(PublicationPK pk, String name, String description, Date creationDate,
      Date beginDate, Date endDate, String creatorId, int importance, String version,
      String keywords, String content, String status) {
    this.pk = pk;
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.beginDate = beginDate;
    this.endDate = endDate;
    this.creatorId = creatorId;
    this.importance = importance;
    this.version = version;
    this.keywords = keywords;
    this.content = content;
    this.status = status;
  }

  public PublicationDetail(PublicationPK pk, String name, String description, Date creationDate,
      Date beginDate, Date endDate, String creatorId, int importance, String version,
      String keywords, String content, String status, Date updateDate) {
    this.pk = pk;
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.beginDate = beginDate;
    this.endDate = endDate;
    this.creatorId = creatorId;
    this.importance = importance;
    this.version = version;
    this.keywords = keywords;
    this.content = content;
    this.status = status;
    this.updateDate = updateDate;
  }

  /**
   * @param name
   * @param description
   * @param creationDate
   * @param beginDate
   * @param endDate
   * @param creatorId
   * @param importance
   * @param version
   * @param keywords
   * @param content
   * @param status
   * @param updateDate
   * @param updaterId
   * @deprecated @param pk
   */
  public PublicationDetail(PublicationPK pk, String name, String description, Date creationDate,
      Date beginDate, Date endDate, String creatorId, int importance, String version,
      String keywords, String content, String status, Date updateDate, String updaterId) {
    this.pk = pk;
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.beginDate = beginDate;
    this.endDate = endDate;
    this.creatorId = creatorId;
    this.importance = importance;
    this.version = version;
    this.keywords = keywords;
    this.content = content;
    this.status = status;
    this.updateDate = updateDate;
    this.updaterId = updaterId;
  }

  public PublicationDetail(PublicationPK pk, String name, String description, Date creationDate,
      Date beginDate, Date endDate, String creatorId, int importance, String version,
      String keywords, String content, String status, Date updateDate, String updaterId,
      String author) {
    this.pk = pk;
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.beginDate = beginDate;
    this.endDate = endDate;
    this.creatorId = creatorId;
    this.importance = importance;
    this.version = version;
    this.keywords = keywords;
    this.content = content;
    this.status = status;
    this.updateDate = updateDate;
    this.updaterId = updaterId;
    this.author = author;
  }

  /**
   * @param name
   * @param description
   * @param creationDate
   * @param beginDate
   * @param endDate
   * @param creatorId
   * @param importance
   * @param version
   * @param keywords
   * @param content
   * @param status
   * @deprecated @param id
   */
  public PublicationDetail(String id, String name, String description, Date creationDate,
      Date beginDate, Date endDate, String creatorId, String importance, String version,
      String keywords, String content, String status) {
    this.pk = new PublicationPK(id);
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.beginDate = beginDate;
    this.endDate = endDate;
    this.creatorId = creatorId;
    this.importance = Integer.parseInt(importance);
    this.version = version;
    this.keywords = keywords;
    this.content = content;
    this.status = status;
  }

  public PublicationDetail(String id, String name, String description, Date creationDate,
      Date beginDate, Date endDate, String creatorId, String importance, String version,
      String keywords, String content, String status, String updaterId, String author) {
    this.pk = new PublicationPK(id);
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.beginDate = beginDate;
    this.endDate = endDate;
    this.creatorId = creatorId;
    this.importance = Integer.parseInt(importance);
    this.version = version;
    this.keywords = keywords;
    this.content = content;
    this.status = status;
    this.updaterId = updaterId;
    this.author = author;
  }

  public PublicationDetail(String id, String name, String description, Date creationDate,
      Date beginDate, Date endDate, String creatorId, String importance, String version,
      String keywords, String content, String status, Date updateDate) {
    this.pk = new PublicationPK(id);
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.beginDate = beginDate;
    this.endDate = endDate;
    this.creatorId = creatorId;
    this.importance = Integer.parseInt(importance);
    this.version = version;
    this.keywords = keywords;
    this.content = content;
    this.status = status;
    this.updateDate = updateDate;
  }

  public PublicationDetail(String id, String name, String description, Date creationDate,
      Date beginDate, Date endDate, String creatorId, String importance, String version,
      String keywords, String content, String status, Date updateDate, String updaterId) {
    this.pk = new PublicationPK(id);
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.beginDate = beginDate;
    this.endDate = endDate;
    this.creatorId = creatorId;
    this.importance = Integer.parseInt(importance);
    this.version = version;
    this.keywords = keywords;
    this.content = content;
    this.status = status;
    this.updateDate = updateDate;
    this.updaterId = updaterId;
  }

  public PublicationDetail(String id, String name, String description, Date creationDate,
      Date beginDate, Date endDate, String creatorId, String importance, String version,
      String keywords, String content, String status, Date updateDate, String updaterId,
      Date validateDate, String validatorId) {
    this.pk = new PublicationPK(id);
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.beginDate = beginDate;
    this.endDate = endDate;
    this.creatorId = creatorId;
    this.importance = Integer.parseInt(importance);
    this.version = version;
    this.keywords = keywords;
    this.content = content;
    this.status = status;
    this.updateDate = updateDate;
    this.updaterId = updaterId;
    this.validateDate = validateDate;
    this.validatorId = validatorId;

  }

  /**
   * @param pk
   * @param name
   * @param description
   * @param creationDate
   * @param beginDate
   * @param endDate
   * @param creatorId
   * @param importance
   * @param version
   * @param keywords
   * @param content
   * @param status
   * @param updateDate
   * @param updaterId
   * @param validateDate
   * @param validatorId
   * @deprecated
   */
  public PublicationDetail(PublicationPK pk, String name, String description, Date creationDate,
      Date beginDate, Date endDate, String creatorId, int importance, String version,
      String keywords, String content, String status, Date updateDate, String updaterId,
      Date validateDate, String validatorId) {
    this.pk = pk;
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.beginDate = beginDate;
    this.endDate = endDate;
    this.creatorId = creatorId;
    this.importance = importance;
    this.version = version;
    this.keywords = keywords;
    this.content = content;
    this.status = status;
    this.updateDate = updateDate;
    this.updaterId = updaterId;
    this.validateDate = validateDate;
    this.validatorId = validatorId;

  }

  public PublicationDetail(PublicationPK pk, String name, String description, Date creationDate,
      Date beginDate, Date endDate, String creatorId, int importance, String version,
      String keywords, String content, String status, Date updateDate, String updaterId,
      Date validateDate, String validatorId, String author) {
    this.pk = pk;
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.beginDate = beginDate;
    this.endDate = endDate;
    this.creatorId = creatorId;
    this.importance = importance;
    this.version = version;
    this.keywords = keywords;
    this.content = content;
    this.status = status;
    this.updateDate = updateDate;
    this.updaterId = updaterId;
    this.validateDate = validateDate;
    this.validatorId = validatorId;
    this.author = author;

  }

  @Override
  public PublicationI18N getTranslation(final String language) {
    return super.getTranslation(language);
  }

  public PublicationPK getPK() {
    return pk;
  }

  public void setPk(PublicationPK pk) {
    this.pk = pk;
  }

  public String getInfoId() {
    return infoId;
  }

  public void setInfoId(String infoId) {
    this.infoId = infoId;
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = creationDate;
  }

  public void setBeginDate(Date beginDate) {
    this.beginDate = beginDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public void setVisibilityPeriod(Period period) {
    if (period.isBeginNotDefined()) {
      setBeginDate(null);
      setBeginHour(null);
    } else {
      setBeginDate(period.getBeginDate());
      setBeginHour(DateUtil.formatTime(period.getBeginDate()));
    }
    if (period.isEndNotDefined()) {
      setEndDate(null);
      setEndHour(null);
    } else {
      setEndDate(period.getEndDate());
      setEndHour(DateUtil.formatTime(period.getEndDate()));
    }
  }

  public Period getVisibilityPeriod() {
    Date begin = getBeginDate();
    if (begin == null) {
      begin = DateUtil.MINIMUM_DATE;
    } else {
      begin = DateUtil.getDate(begin, getBeginHour());
    }
    Date end = getEndDate();
    if (end == null) {
      end = DateUtil.MAXIMUM_DATE;
    } else {
      end = DateUtil.getDate(end, getEndHour());
    }
    return Period.from(begin, end);
  }

  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

  public void setContentPagePath(String content) {
    this.content = content;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setUpdateDate(Date updateDate) {
    this.updateDate = updateDate;
  }

  public void setUpdaterId(String updaterId) {
    this.updaterId = updaterId;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  @Override
  public Date getCreationDate() {
    return creationDate;
  }

  public Date getBeginDate() {
    return beginDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public String getCreatorId() {
    return creatorId;
  }

  @Override
  public ContributionIdentifier getContributionId() {
    return ContributionIdentifier.from(getInstanceId(), getId(), getContributionType());
  }

  public int getImportance() {
    return importance;
  }

  public String getVersion() {
    return version;
  }

  public String getKeywords() {
    return keywords;
  }

  public String getKeywords(String lang) {
    if (!I18NHelper.isI18nContentActivated) {
      return getKeywords();
    }
    PublicationI18N p = getTranslations().get(lang);
    if (p == null) {
      p = getNextTranslation();
    }
    if (p != null) {
      return p.getKeywords();
    }
    return getKeywords();
  }

  public String getContentPagePath() {
    return content;
  }

  public String getStatus() {
    return status;
  }

  public String getImage() {
    ThumbnailDetail thumbDetail = getThumbnail();
    if (thumbDetail != null) {
      String[] imageProps = ThumbnailController.getImageAndMimeType(thumbDetail);
      return imageProps[0];
    }
    return null;

  }

  public String getImageMimeType() {
    ThumbnailDetail thumbDetail = getThumbnail();
    if (thumbDetail != null) {
      String[] imageProps = ThumbnailController.getImageAndMimeType(thumbDetail);
      return imageProps[1];
    }
    return null;
  }

  public ThumbnailDetail getThumbnail() {
    if (getPK() != null && getPK().getInstanceId() != null && getPK().getId() != null) {
      ThumbnailDetail thumbDetail = new ThumbnailDetail(getPK().getInstanceId(), Integer.
          valueOf(getPK().getId()), ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE);
      return ThumbnailController.getCompleteThumbnail(thumbDetail);
    }
    return null;
  }

  public Date getUpdateDate() {
    return updateDate;
  }

  public String getUpdaterId() {
    return updaterId;
  }

  @Override
  public User getLastModifier() {
    return updaterId != null ? User.getById(updaterId) : null;
  }

  public String getAuthor() {
    return author;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append("PublicationDetail {").append("\n");
    if (getPK() != null) {
      result.append(" getPK().getId() = ").append(getPK().getId()).append("\n");
      result.append(" getPK().getEd() = ").append(getPK().getSpace()).append("\n");
      result.append(" getPK().getCo() = ").append(getPK().getComponentName()).append("\n");
    }
    result.append(" getName() = ").append(getName()).append("\n");
    result.append(" getDescription() = ").append(getDescription()).append("\n");
    result.append(" getCreationDate() = ").append(getCreationDate()).append("\n");
    result.append(" getBeginDate() = ").append(getBeginDate()).append("\n");
    result.append(" getBeginHour() = ").append(getBeginHour()).append("\n");
    result.append(" getEndDate() = ").append(getEndDate()).append("\n");
    result.append(" getEndHour() = ").append(getEndHour()).append("\n");
    result.append(" getCreatorId() = ").append(getCreatorId()).append("\n");
    result.append(" getImportance() = ").append(getImportance()).append("\n");
    result.append(" getVersion() = ").append(getVersion()).append("\n");
    result.append(" getKeywords() = ").append(getKeywords()).append("\n");
    result.append(" getContent() = ").append(getContentPagePath()).append("\n");
    result.append(" getStatus() = ").append(getStatus()).append("\n");
    result.append(" getUpdateDate() = ").append(getUpdateDate()).append("\n");
    result.append(" getUpdaterId() = ").append(getUpdaterId()).append("\n");
    result.append(" getValidateDate() = ").append(getValidateDate()).append("\n");
    result.append(" getValidatorId() = ").append(getValidatorId()).append("\n");
    result.append(" getSilverObjectId() = ").append(getSilverObjectId()).append("\n");
    result.append(" getAuthor() = ").append(getAuthor()).append("\n");
    result.append("}");
    return result.toString();
  }

  public Date getValidateDate() {
    return validateDate;
  }

  public String getValidatorId() {
    return validatorId;
  }

  public void setValidateDate(Date validateDate) {
    this.validateDate = validateDate;
  }

  public void setValidatorId(String validatorId) {
    this.validatorId = validatorId;
  }

  public void setBeginHour(String hour) {
    this.beginHour = hour;
  }

  public String getBeginHour() {
    return this.beginHour;
  }

  public void setEndHour(String hour) {
    this.endHour = hour;
  }

  public String getEndHour() {
    return this.endHour;
  }

  public void setSilverObjectId(String silverObjectId) {
    this.silverObjectId = silverObjectId;
  }

  public void setSilverObjectId(int silverObjectId) {
    this.silverObjectId = Integer.toString(silverObjectId);
  }

  public String getSilverObjectId() {
    if (this.silverObjectId == null) {
      ContentManager contentManager = ContentManagerProvider.getContentManager();
      try {
        int objectId = contentManager.getSilverContentId(getId(), getInstanceId());
        if (objectId >= 0) {
          this.silverObjectId = String.valueOf(objectId);
        }
      } catch (ContentManagerException ex) {
        this.silverObjectId = null;
      }
    }
    return this.silverObjectId;
  }

  @Override
  public String getURL() {
    return "searchResult?Type=Publication&Id=" + getId();
  }

  @Override
  public String getId() {
    return getPK().getId();
  }

  @Override
  public String getInstanceId() {
    return getPK().getComponentName();
  }

  @Override
  public String getDate() {
    if (getUpdateDate() != null) {
      return DateUtil.date2SQLDate(getUpdateDate());
    }
    return DateUtil.date2SQLDate(getCreationDate());
  }

  @Override
  public String getSilverCreationDate() {
    return DateUtil.date2SQLDate(getCreationDate());
  }

  @Override
  public String getTitle() {
    return getName();
  }

  public void setIconUrl(String iconUrl) {
    this.iconUrl = iconUrl;
  }

  @Override
  public String getIconUrl() {
    return this.iconUrl;
  }

  /**
   * *************************************************************************************
   */
  /**
   * FormTemplate exposition for taglibs
   */
  /**
   * *************************************************************************************
   */
  public List<XMLField> getXmlFields() {
    return getXmlFields(null);
  }

  public List<XMLField> getXmlFields(String language) {
    if ("0".equals(getInfoId())) {
      // this publication does not use a form
      return new ArrayList<>();
    }
    if (xmlFields == null) {
      try {
        xmlFields = getFormTemplateBm()
            .getXMLFieldsForExport(getPK().getInstanceId() + ":" + getInfoId(), getPK().getId(),
                language);
      } catch (Exception e) {
        throw new PublicationRuntimeException(e);
      }
    }
    return xmlFields;
  }

  public Map<String, String> getFormValues(String language) {
    HashMap<String, String> formValues = new HashMap<>();
    if ("0".equals(getInfoId())) {
      // this publication does not use a form
      return formValues;
    }

    DataRecord data = null;
    PublicationTemplate pub = null;
    try {
      pub = PublicationTemplateManager.getInstance()
          .getPublicationTemplate(getPK().getInstanceId() + ":" + getInfoId());
      data = pub.getRecordSet().getRecord(pk.getId());
    } catch (Exception e) {
      SilverLogger.getLogger(this).warn(failureOnGetting("form record with",
          MessageFormat.format("pubid {0} and infoId {1}", getPK().getId(), getInfoId())), e);
    }

    if (data == null) {
      return formValues;
    }

    String[] fieldNames = data.getFieldNames();
    PagesContext pageContext = new PagesContext();
    pageContext.setLanguage(language);
    for (String fieldName : fieldNames) {
      try {
        Field field = data.getField(fieldName);
        GenericFieldTemplate fieldTemplate =
            (GenericFieldTemplate) pub.getRecordTemplate().getFieldTemplate(fieldName);
        FieldDisplayer fieldDisplayer =
            TypeManager.getInstance().getDisplayer(fieldTemplate.getTypeName(), "simpletext");
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        fieldDisplayer.display(out, field, fieldTemplate, pageContext);
        formValues.put(fieldName, sw.toString());
      } catch (Exception e) {
        SilverLogger.getLogger(this).warn(failureOnGetting("field value with",
            MessageFormat.format("pubid {0} and fieldName {1}", getPK().getId(), fieldName)), e);
      }

    }
    return formValues;
  }

  public String getFieldValue(String fieldNameAndLanguage) {
    String[] params = fieldNameAndLanguage.split(",");

    String fieldName = params[0];
    String language = null;
    if (params.length > 1) {
      language = params[1];
    }

    String fieldValue = "";

    List<XMLField> xmlFieldsForLanguage = getXmlFields(language);
    for (XMLField xmlField : xmlFieldsForLanguage) {
      if (fieldName.equals(xmlField.getName())) {
        fieldValue = getValueOfField(xmlField, language);
      }
    }


    return fieldValue;
  }

  private String getValueOfField(XMLField xmlField, String language) {
    String fieldValue = xmlField.getValue();
    if (fieldValue == null) {
      fieldValue = "";
    } else {
      if (fieldValue.startsWith("image_") || fieldValue.startsWith("file_")) {
        String attachmentId =
            fieldValue.substring(fieldValue.indexOf("_") + 1, fieldValue.length());
        if (isDefined(attachmentId)) {
          if (attachmentId.startsWith("/")) {
            // case of an image provided by a gallery
            fieldValue = attachmentId;
          } else {
            SimpleDocument attachment = AttachmentServiceProvider.getAttachmentService()
                .searchDocumentById(new SimpleDocumentPK(attachmentId, getPK().getInstanceId()),
                    language);
            if (attachment != null) {
              fieldValue = attachment.getAttachmentURL();
            }
          }
        } else {
          fieldValue = "";
        }
      } else if (fieldValue.startsWith(WysiwygFCKFieldDisplayer.DB_KEY)) {
        fieldValue = WysiwygFCKFieldDisplayer.getContentFromFile(getPK().getInstanceId(), getPK().
            getId(), xmlField.getName(), language);
      } else {
        fieldValue = WebEncodeHelper.javaStringToHtmlParagraphe(fieldValue);
      }
    }
    return fieldValue;
  }

  private FormTemplateService getFormTemplateBm() {
    try {
      return ServiceProvider.getService(FormTemplateService.class);
    } catch (Exception e) {
      throw new PublicationRuntimeException(e);
    }
  }

  public PublicationService getPublicationService() {
    try {
      return ServiceProvider.getService(PublicationService.class);
    } catch (Exception e) {
      throw new PublicationRuntimeException(e);
    }
  }

  public ContributionContent getContent() {
    try {
      return WysiwygController.get(getPK().getComponentName(), getPK().getId(), getLanguage());
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .warn("can not load wysiwyg of publication {0} into {1} language", getId(), getLanguage(),
              e);
    }
    return new WysiwygContent(LocalizedContribution.from(this, getLanguage()),
        "Erreur lors du chargement du wysiwyg !");
  }

  public void setImportance(int importance) {
    this.importance = importance;
  }

  public void setKeywords(String keywords) {
    this.keywords = keywords;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getCreatorName() {
    return creatorName;
  }

  public void setCreatorName(String creatorName) {
    this.creatorName = creatorName;
  }

  public int getIndexOperation() {
    return indexOperation;
  }

  public void setIndexOperation(int i) {
    indexOperation = i;
  }

  public String getDefaultUrl(String componentName) {
    return "/R" + componentName + "/" + getPK().getInstanceId() +
        "/searchResult?Type=Publication&Id=" + getPK().getId();
  }

  public boolean isStatusMustBeChecked() {
    return statusMustBeChecked;
  }

  public void setStatusMustBeChecked(boolean statusMustBeChecked) {
    this.statusMustBeChecked = statusMustBeChecked;
  }

  public String getTargetValidatorId() {
    return targetValidatorId;
  }

  public void setTargetValidatorId(String targetValidatorId) {
    this.targetValidatorId = targetValidatorId;
  }

  public String getTargetValidatorNames() {
    StringBuilder validatorNames = new StringBuilder();
    String[] validatorIds = getTargetValidatorIds();
    if (validatorIds != null) {
      for (String valId : validatorIds) {
        if (validatorNames.length() > 0) {
          validatorNames.append(", ");
        }
        validatorNames.append(User.getById(valId).getDisplayedName());
      }
    }
    return validatorNames.toString();
  }

  public String[] getTargetValidatorIds() {
    return split(getTargetValidatorId(), ',');
  }

  public String getCloneId() {
    return cloneId;
  }

  public void setCloneId(String tempPubId) {
    this.cloneId = tempPubId;
  }

  public boolean haveGotClone() {
    return cloneId != null && !"-1".equals(cloneId) && !"null".equals(cloneId) &&
        cloneId.length() > 0;
  }

  public boolean isClone() {
    return CLONE_STATUS.equalsIgnoreCase(getStatus()) ||
        (isDefined(getCloneId()) && !"-1".equals(getCloneId()) && !isDefined(getCloneStatus()));
  }

  public boolean isValid() {
    return VALID_STATUS.equalsIgnoreCase(getStatus());
  }

  public boolean isValidationRequired() {
    return TO_VALIDATE_STATUS.equalsIgnoreCase(getStatus());
  }

  public boolean isRefused() {
    return REFUSED_STATUS.equalsIgnoreCase(getStatus());
  }

  public boolean isDraft() {
    return DRAFT_STATUS.equalsIgnoreCase(getStatus());
  }

  public PublicationPK getClonePK() {
    return new PublicationPK(getCloneId(), getPK());
  }

  public PublicationDetail copy() {
    PublicationDetail clone = new PublicationDetail();
    clone.setAuthor(author);
    clone.setBeginDate(beginDate);
    clone.setBeginHour(beginHour);
    clone.setContentPagePath(content);
    clone.setCreationDate(creationDate);
    clone.setCreatorId(creatorId);
    clone.setDescription(getDescription());
    clone.setEndDate(endDate);
    clone.setEndHour(endHour);
    clone.setImportance(importance);
    clone.setInfoId(infoId);
    clone.setKeywords(keywords);
    clone.setName(getName());
    clone.setPk(pk);
    clone.setStatus(status);
    clone.setTargetValidatorId(targetValidatorId);
    clone.setCloneId(cloneId);
    clone.setUpdateDate(updateDate);
    clone.setUpdaterId(updaterId);
    clone.setValidateDate(validateDate);
    clone.setValidatorId(validatorId);
    clone.setVersion(version);

    return clone;
  }

  public String getCloneStatus() {
    return cloneStatus;
  }

  public void setCloneStatus(String cloneStatus) {
    this.cloneStatus = cloneStatus;
  }

  public boolean isUpdateDateMustBeSet() {
    return updateDateMustBeSet;
  }

  public void setUpdateDateMustBeSet(boolean updateDateMustBeSet) {
    this.updateDateMustBeSet = updateDateMustBeSet;
  }

  public int getNbAccess() {
    return nbAccess;
  }

  public void setNbAccess(int nbAccess) {
    this.nbAccess = nbAccess;
  }

  public boolean isVisible() {
    return getVisibility().isVisible();
  }

  public boolean isNoMoreVisible() {
    return getVisibility().isNoMoreVisible();
  }

  public boolean isNotYetVisible() {
    return getVisibility().isNotYetVisible();
  }

  public Date getBeginDateAndHour() {
    return getVisibility().getBeginDateAndHour();
  }

  public Date getEndDateAndHour() {
    return getVisibility().getEndDateAndHour();
  }

  private Visibility getVisibility() {
    if (visibility == null) {
      visibility = new Visibility(beginDate, beginHour, endDate, endHour);
    }
    return visibility;
  }

  public Date getDraftOutDate() {
    if (draftOutDate != null) {
      return (Date) draftOutDate.clone();
    }
    return null;
  }

  public void setDraftOutDate(Date draftOutDate) {
    if (draftOutDate != null) {
      this.draftOutDate = (Date) draftOutDate.clone();
    }
    this.draftOutDate = null;
  }

  @Override
  public boolean isIndexable() {
    return VALID_STATUS.equals(this.status);
  }

  public boolean isPublicationEditor(String userId) {
    return Objects.equals(creatorId, userId) || Objects.equals(updaterId, userId);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof PublicationDetail) {
      PublicationDetail anotherPublication = (PublicationDetail) o;
      return this.pk.equals(anotherPublication.getPK());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 23 * hash + (this.pk != null ? this.pk.hashCode() : 0);
    return hash;
  }

  @Override
  public String getContributionType() {
    return TYPE;
  }

  /**
   * Is the specified user can access this publication?
   * <p>
   * A user can access a publication if he has enough rights to access both the application
   * instance
   * in which is managed this publication and one of the nodes to which this publication belongs
   * to.
   * @param user a user in Silverpeas.
   * @return true if the user can access this publication, false otherwise.
   */
  @Override
  public boolean canBeAccessedBy(final User user) {
    AccessController<PublicationPK> accessController =
        AccessControllerProvider.getAccessController(PublicationAccessControl.class);
    return accessController.isUserAuthorized(user.getId(), getPK());
  }

  /**
   * Is the specified user can access this publication on persist context?
   * <p>
   * A user can access a publication on persist context if he has enough rights to access both the
   * application instance in which is managed this publication and one of the nodes to which this
   * publication belongs to.
   * @param user a user in Silverpeas.
   * @return true if the user can access this publication, false otherwise.
   */
  @Override
  public boolean canBeModifiedBy(final User user) {
    AccessController<PublicationPK> accessController =
        AccessControllerProvider.getAccessController(PublicationAccessControl.class);
    return accessController.isUserAuthorized(user.getId(), getPK(),
        AccessControlContext.init().onOperationsOf(AccessControlOperation.modification));
  }

  /**
   * The type of this resource
   * @return the same value returned by getContributionType()
   */
  public static String getResourceType() {
    return TYPE;
  }

  @Override
  public String getSilverpeasContentId() {
    return getSilverObjectId();
  }

  public void setExplicitRank(int explicitRank) {
    this.explicitRank = explicitRank;
  }

  public int getExplicitRank() {
    return explicitRank;
  }

  /**
   * Gets the rating informations linked with the current publication.
   * @return the rating of the publication.
   */
  @Override
  public ContributionRating getRating() {
    if (contributionRating == null) {
      contributionRating = RatingService.get()
          .getRating(new ContributionRatingPK(getId(), getInstanceId(), PublicationDetail.TYPE));
    }
    return contributionRating;
  }

  public void setAlias(boolean alias) {
    this.alias = alias;
  }

  public boolean isAlias() {
    return alias;
  }

  public String getPermalink() {
    return URLUtil.getSimpleURL(URLUtil.URL_PUBLI, getId());
  }

  public boolean isSharingAllowedForRolesFrom(final UserDetail user) {
    if (!isValid()) {
      // a not valid publication can not be shared
      return false;
    }

    if (user == null || StringUtil.isNotDefined(user.getId()) || !user.isValidState()) {
      // In that case, from point of security view if no user data exists, sharing is forbidden.
      return false;
    }

    // Access is verified for sharing context
    AccessController<PublicationPK> accessController = AccessControllerProvider
        .getAccessController(PublicationAccessControl.class);
    return accessController.isUserAuthorized(user.getId(), getPK(),
        AccessControlContext.init().onOperationsOf(AccessControlOperation.sharing));
  }

  @Override
  public ContributionModel getModel() {
    return new PublicationDetailModel(this);
  }
}