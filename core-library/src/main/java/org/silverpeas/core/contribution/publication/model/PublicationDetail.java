/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.contribution.publication.model;

import org.owasp.encoder.Encode;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.ContributionModificationContextHandler;
import org.silverpeas.core.contribution.ContributionStatus;
import org.silverpeas.core.contribution.ContributionWithVisibility;
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
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagementEngine;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagementEngineProvider;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentInterface;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.ContributionModel;
import org.silverpeas.core.contribution.model.I18nContribution;
import org.silverpeas.core.contribution.model.LocalizedContribution;
import org.silverpeas.core.contribution.model.Thumbnail;
import org.silverpeas.core.contribution.model.WithAttachment;
import org.silverpeas.core.contribution.model.WithPermanentLink;
import org.silverpeas.core.contribution.model.WithThumbnail;
import org.silverpeas.core.contribution.model.WysiwygContent;
import org.silverpeas.core.contribution.rating.model.ContributionRating;
import org.silverpeas.core.contribution.rating.model.ContributionRatingPK;
import org.silverpeas.core.contribution.rating.model.Rateable;
import org.silverpeas.core.contribution.rating.service.RatingService;
import org.silverpeas.core.contribution.template.form.service.FormTemplateService;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.contribution.tracking.ModificationTracked;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.i18n.AbstractI18NBean;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.i18n.I18n;
import org.silverpeas.core.index.indexing.model.IndexManager;
import org.silverpeas.core.io.media.image.thumbnail.control.ThumbnailController;
import org.silverpeas.core.io.media.image.thumbnail.model.ThumbnailDetail;
import org.silverpeas.core.reminder.WithReminder;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.AccessControlOperation;
import org.silverpeas.core.security.authorization.PublicationAccessControl;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
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
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.silverpeas.core.SilverpeasExceptionMessages.failureOnGetting;
import static org.silverpeas.core.contribution.indicator.NewContributionIndicator.isNewContribution;
import static org.silverpeas.core.date.TemporalConverter.asDate;
import static org.silverpeas.core.date.TemporalConverter.asOffsetDateTime;
import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.util.StringUtil.split;

/**
 * This object contains the description of a publication
 */
@XmlRootElement(namespace = "http://www.silverpeas.org/exchange")
@XmlAccessorType(XmlAccessType.NONE)
@ModificationTracked
public class PublicationDetail extends AbstractI18NBean<PublicationI18N>
    implements I18nContribution, ContributionWithVisibility, SilverContentInterface, Rateable,
    Serializable, WithAttachment, WithThumbnail, WithReminder, WithPermanentLink {
  private static final long serialVersionUID = 9199848912262605680L;

  private static final String EXCHANGE_NAMESPACE = "http://www.silverpeas.org/exchange";
  public static final String DELAYED_VISIBILITY_AT_MODEL_PROPERTY = "DELAYED_VISIBILITY_AT";

  private PublicationPK pk;
  private String infoId;
  @XmlElement(name = "creationDate", namespace = EXCHANGE_NAMESPACE)
  @XmlJavaTypeAdapter(DateAdapter.class)
  private Date creationDate;
  @XmlElement(name = "beginDate", namespace = EXCHANGE_NAMESPACE)
  @XmlJavaTypeAdapter(DateAdapter.class)
  private Date beginDate;
  @XmlElement(name = "endDate", namespace = EXCHANGE_NAMESPACE)
  @XmlJavaTypeAdapter(DateAdapter.class)
  private Date endDate;
  @XmlElement(name = "creatorId", namespace = EXCHANGE_NAMESPACE)
  private String creatorId;
  @XmlElement(name = "creatorName", namespace = EXCHANGE_NAMESPACE)
  private String creatorName;
  @XmlElement(name = "importance", namespace = EXCHANGE_NAMESPACE)
  private int importance;
  @XmlElement(name = "version", namespace = EXCHANGE_NAMESPACE)
  private String version;
  @XmlElement(name = "keywords", namespace = EXCHANGE_NAMESPACE)
  private String keywords;
  private String content;
  @XmlElement(name = "status", namespace = EXCHANGE_NAMESPACE)
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
  private Location authorizedLocation = null;
  private transient ThumbnailDetail thumbnail = null;

  private ContributionRating contributionRating;

  /**
   * Gets a builder of {@link PublicationDetail} instances for the default language as defined in
   * {@link I18n#getDefaultLanguage()}. All the textual properties (name, description and keywords)
   * will be related to this language.
   * @return a {@link Builder} instance
   */
  public static Builder builder() {
    return new Builder(I18n.get().getDefaultLanguage());
  }

  /**
   * Gets a builder of {@link PublicationDetail} instances for the specified language. All the
   * textual properties (name, description and keywords) will be related to this language.
   * @param language a ISO 639-1 code of a supported language.
   * @return a {@link Builder} instance
   */
  public static Builder builder(final String language) {
    return new Builder(language);
  }

  @Override
  protected Class<PublicationI18N> getTranslationType() {
    return PublicationI18N.class;
  }

  /**
   * Default contructor, required for JAXB mapping in importExport.
   */
  protected PublicationDetail() {
    // Nothing to do
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
    if (period == null || period.startsAtMinDate()) {
      setBeginDate(null);
      setBeginHour(null);
    } else {
      final Date periodStart =
          asDate(asOffsetDateTime(period.getStartDate()).atZoneSameInstant(ZoneId.systemDefault()));
      setBeginDate(periodStart);
      setBeginHour(DateUtil.formatTime(periodStart));
    }
    if (period == null || period.endsAtMaxDate()) {
      setEndDate(null);
      setEndHour(null);
    } else {
      final Date periodEnd =
          asDate(asOffsetDateTime(period.getEndDate()).atZoneSameInstant(ZoneId.systemDefault()));
      setEndDate(periodEnd);
      setEndHour(DateUtil.formatTime(periodEnd));
    }
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
  public ContributionIdentifier getIdentifier() {
    return new PublicationIdentifier(getInstanceId(), getId(), getContributionType());
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

  public ContributionStatus getContributionStatus() {
    final ContributionStatus contributionStatus;
    if (DRAFT_STATUS.equalsIgnoreCase(status)) {
      contributionStatus = ContributionStatus.DRAFT;
    } else if (VALID_STATUS.equalsIgnoreCase(status)) {
      contributionStatus = ContributionStatus.VALIDATED;
    } else if (TO_VALIDATE_STATUS.equalsIgnoreCase(status)) {
      contributionStatus = ContributionStatus.PENDING_VALIDATION;
    } else if (REFUSED_STATUS.equalsIgnoreCase(status)) {
      contributionStatus = ContributionStatus.REFUSED;
    } else {
      contributionStatus = ContributionStatus.UNKNOWN;
    }
    return contributionStatus;
  }

  public String getImage() {
    final Thumbnail thumbDetail = getThumbnail();
    if (thumbDetail != null) {
      return thumbDetail.getImageFileName();
    }
    return null;

  }

  public String getImageMimeType() {
    final Thumbnail thumbDetail = getThumbnail();
    if (thumbDetail != null) {
      return thumbDetail.getMimeType();
    }
    return null;
  }

  /**
   * Gets the thumbnail linked to the publication.
   * <p>
   * The corresponding {@link ThumbnailDetail} is loaded once and cached into publication instance.
   * </p>
   * @return the {@link ThumbnailDetail} instance if any.
   */
  @Override
  public Thumbnail getThumbnail() {
    if (thumbnail == null && getPK() != null && getPK().getInstanceId() != null &&
        getPK().getId() != null) {
      ThumbnailDetail thumbnailReference =
          new ThumbnailDetail(getPK().getInstanceId(), Integer.parseInt(getPK().getId()),
              ThumbnailDetail.THUMBNAIL_OBJECTTYPE_PUBLICATION_VIGNETTE);
      thumbnail = ThumbnailController.getCompleteThumbnail(thumbnailReference);
    }
    return thumbnail;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Optional<PublicationPath> getResourcePath() {
    return Optional.of(this).map(PublicationPath::getPath);
  }

  public void setThumbnail(final ThumbnailDetail thumbnail) {
    this.thumbnail = thumbnail;
  }

  public Date getLastUpdateDate() {
    return updateDate;
  }

  public String getUpdaterId() {
    return updaterId;
  }

  @Override
  public User getLastUpdater() {
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
    result.append(" getUpdateDate() = ").append(getLastUpdateDate()).append("\n");
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
      ContentManagementEngine contentMgtEngine =
          ContentManagementEngineProvider.getContentManagementEngine();
      try {
        int objectId = contentMgtEngine.getSilverContentId(getId(), getInstanceId());
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
    if (getLastUpdateDate() != null) {
      return DateUtil.date2SQLDate(getLastUpdateDate());
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


  /*
   * FormTemplate exposition for taglibs
   */

  @SuppressWarnings("unused")
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
        xmlFields =
            getFormTemplateBm().getXMLFieldsForExport(getPK().getInstanceId() + ":" + getInfoId(),
                getPK().getId(), language);
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
      SilverLogger.getLogger(this)
          .warn(failureOnGetting("form record with",
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
        FieldDisplayer<Field> fieldDisplayer =
            TypeManager.getInstance().getDisplayer(fieldTemplate.getTypeName(), "simpletext");
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        fieldDisplayer.display(out, field, fieldTemplate, pageContext);
        formValues.put(fieldName, sw.toString());
      } catch (Exception e) {
        SilverLogger.getLogger(this)
            .warn(failureOnGetting("field value with",
                MessageFormat.format("pubid {0} and fieldName {1}", getPK().getId(), fieldName)),
                e);
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
        String attachmentId = fieldValue.substring(fieldValue.indexOf('_') + 1);
        fieldValue = getFieldValueFromAttachment(attachmentId, language, fieldValue);
      } else if (fieldValue.startsWith(WysiwygFCKFieldDisplayer.DB_KEY)) {
        fieldValue =
            WysiwygFCKFieldDisplayer.getContentFromFile(getPK().getInstanceId(), getPK().getId(),
                xmlField.getName(), language);
      } else {
        fieldValue = Encode.forHtml(fieldValue);
      }
    }
    return fieldValue;
  }

  private String getFieldValueFromAttachment(final String attachmentId, final String language,
      final String defaultValue) {
    final String fieldValue;
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
        } else {
          fieldValue = defaultValue;
        }
      }
    } else {
      fieldValue = "";
    }
    return fieldValue;
  }

  private FormTemplateService getFormTemplateBm() {
    try {
      return ServiceProvider.getSingleton(FormTemplateService.class);
    } catch (Exception e) {
      throw new PublicationRuntimeException(e);
    }
  }

  public WysiwygContent getContent() {
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
    String lang = getLanguage();
    PublicationI18N translation = getTranslation(lang);
    translation.setKeywords(keywords);
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
    clone.setLanguage(getLanguage());
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
    clone.setPk(new PublicationPK(pk.getId(), pk));
    clone.setStatus(status);
    clone.setTargetValidatorId(targetValidatorId);
    clone.setCloneId(cloneId);
    clone.setUpdateDate(updateDate);
    clone.setUpdaterId(updaterId);
    clone.setValidateDate(validateDate);
    clone.setValidatorId(validatorId);
    clone.setVersion(version);
    clone.alias = alias;
    clone.authorizedLocation = authorizedLocation;
    return clone;
  }

  public String getCloneStatus() {
    return cloneStatus;
  }

  public void setCloneStatus(String cloneStatus) {
    this.cloneStatus = cloneStatus;
  }

  /**
   * Indicates if the update data MUST be set.
   * <p>
   *   The update data are:
   *   <ul>
   *     <li>the last update date</li>
   *     <li>the last updater</li>
   *   </ul>
   * </p>
   * @return true if update data MUST be set, false otherwise.
   */
  public boolean isUpdateDataMustBeSet() {
    return !ContributionModificationContextHandler.get().isMinorModification().orElse(false) &&
        updateDateMustBeSet;
  }

  public void setUpdateDataMustBeSet(boolean updateDataMustBeSet) {
    this.updateDateMustBeSet = updateDataMustBeSet;
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

  @Override
  public Visibility getVisibility() {
    if (visibility == null) {
      visibility = Visibility.from(this, beginDate, beginHour, endDate, endHour);
    }
    return visibility;
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
   * A user can access a publication if he has enough rights to access both the application instance
   * in which is managed this publication and one of the nodes to which this publication belongs
   * to.
   * @param user a user in Silverpeas.
   * @return true if the user can access this publication, false otherwise.
   */
  @Override
  public boolean canBeAccessedBy(final User user) {
    return PublicationAccessControl.get().isUserAuthorized(user.getId(), this);
  }

  /**
   * Is the specified user can modify this publication?
   * <p>
   * A user can access a publication on persist context if he has enough rights to access both the
   * application instance in which is managed this publication and one of the nodes to which this
   * publication belongs to.
   * @param user a user in Silverpeas.
   * @return true if the user can access this publication, false otherwise.
   */
  @Override
  public boolean canBeModifiedBy(final User user) {
    return PublicationAccessControl.get()
        .isUserAuthorized(user.getId(), this,
            AccessControlContext.init().onOperationsOf(AccessControlOperation.MODIFICATION));
  }

  /**
   * Is the specified user can file in this publication attachments?
   * @param user a user in Silverpeas.
   * @return true if the user has modification rights on this publication. In this case, he can
   * attach documents to this publication. False otherwise.
   */
  @Override
  public boolean canBeFiledInBy(final User user) {
    return canBeModifiedBy(user);
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

  /**
   * Sets an authorized location to the current instance.
   * <p>
   * A publication is linked or not to a node, depending the application functional context. This
   * method should be used into the case of a publication linked to a node, located at a location in
   * other words. Sometimes, a publication can be located at several locations (so linked to several
   * nodes), in that case there is a main location and the others which are called "aliases". The
   * caller of this method has verified which locations of a publication is authorized according to
   * a context, that the publication detail instance does not know about, and set one of these
   * authorized ones to the current instance. If the authorized location set defines an alias then
   * some methods will take into account this information, like {@link #getPermalink()} for
   * example.
   * </p>
   * <p>
   * Giving a null location means that alias data MUST be cleared.
   * </p>
   * @param location a {@link Location} instance.
   */
  public void setAuthorizedLocation(final Location location) {
    if (location == null) {
      alias = false;
      authorizedLocation = null;
    } else {
      this.alias = location.isAlias();
      this.authorizedLocation = alias ? location : null;
    }
  }

  public void setAlias(boolean alias) {
    this.alias = alias;
  }

  public boolean isAlias() {
    return alias;
  }

  @Override
  public String getPermalink() {
    return URLUtil.getSimpleURL(URLUtil.URL_PUBLI, getId(),
        authorizedLocation != null && !authorizedLocation.getInstanceId().equals(getInstanceId()) ?
            authorizedLocation.getInstanceId() :
            null);
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
    return PublicationAccessControl.get()
        .isUserAuthorized(user.getId(), this,
            AccessControlContext.init().onOperationsOf(AccessControlOperation.SHARING));
  }

  @Override
  public ContributionModel getModel() {
    return new PublicationDetailModel(this);
  }

  public String getMostRecentUpdater() {
    String userId = getUpdaterId();
    if (!isDefined(userId)) {
      userId = getCreatorId();
    }
    return userId;
  }

  /**
   * Is this publication a new one? A publication is considered as a new one when it was created or
   * updated before a given amount of day. This amount is a parameter that is set in the
   * <code>publicationSettings.properties</code> properties file.
   * @return true of this publication was created or updated recently. False otherwise
   */
  public boolean isNew() {
    return isNewContribution(this);
  }

  /**
   * A builder of a {@link PublicationDetail} instance by setting some of its properties.
   */
  public static class Builder {

    private final PublicationDetail publication = new PublicationDetail();

    Builder(final String language) {
      publication.setLanguage(language);
    }

    /**
     * Builds a {@link PublicationDetail} instance from the properties that were previously set
     * with this builder.
     * @return a {@link PublicationDetail} instance.
     */
    public PublicationDetail build() {
      if (publication.getPK() == null) {
        publication.setPk(new PublicationPK(ResourceReference.UNKNOWN_ID));
      }
      return publication;
    }

    /**
     * Sets the unique identifier of the {@link PublicationDetail} instance to build.
     * @param pk a unique identifier of a publication.
     * @return itself.
     */
    public Builder setPk(final PublicationPK pk) {
      publication.setPk(pk);
      return this;
    }

    /**
     * Sets the creation properties of the {@link PublicationDetail} instance to build.
     * @param creationDate the date at which the publication was created.
     * @param creatorId the identifier of the user that created the publication.
     * @return itself.
     */
    public Builder created(final Date creationDate, final String creatorId) {
      publication.creationDate = creationDate;
      publication.creatorId = creatorId;
      return this;
    }

    /**
     * Sets the update properties of the {@link PublicationDetail} instance to build.
     * @param updateDate the date at which the publication was lastly updated.
     * @param updaterId the identifier of the user that lastly updated the publication.
     * @return itself.
     */
    public Builder updated(final Date updateDate, final String updaterId) {
      publication.updateDate = updateDate;
      publication.updaterId = updaterId;
      return this;
    }

    /**
     * Sets the validation properties of the {@link PublicationDetail} instance to build.
     * @param validateDate the date at which the publication was validated.
     * @param validatorId the identifier of the user that validated the publication.
     * @return itself.
     */
    public Builder validated(final Date validateDate, final String validatorId) {
      publication.validateDate = validateDate;
      publication.validatorId = validatorId;
      return this;
    }

    /**
     * Sets the visibility begin date properties of the {@link PublicationDetail} instance to build.
     * @param date the day at which the publication begins to be visible.
     * @param hour the hour at which the publication begins to be visible.
     * @return itself.
     */
    public Builder setBeginDateTime(final Date date, final String hour) {
      publication.beginDate = date;
      publication.beginHour = hour;
      return this;
    }

    /**
     * Sets the visibility end date properties of the {@link PublicationDetail} instance to build.
     * @param date the day at which the publication ends to be visible.
     * @param hour the hour at which the publication ends to be visible.
     * @return itself.
     */
    public Builder setEndDateTime(final Date date, final String hour) {
      publication.endDate = date;
      publication.endHour = hour;
      return this;
    }

    /**
     * Sets the importance of the {@link PublicationDetail} instance to build.
     * @param importance the importance of the publication. Lower value means more importance.
     * @return itself.
     */
    public Builder setImportance(final int importance) {
      publication.importance = importance;
      return this;
    }

    /**
     * Sets the keywords of the {@link PublicationDetail} instance to build.
     * @param keywords the keywords of the publication.
     * @return itself.
     */
    public Builder setKeywords(final String keywords) {
      publication.keywords = keywords;
      return this;
    }

    /**
     * Sets the URL path where is located the content of the {@link PublicationDetail} instance
     * to build.
     * @param contentPagePath the path of the content of the publication.
     * @return itself.
     */
    public Builder setContentPagePath(final String contentPagePath) {
      publication.content = contentPagePath;
      return this;
    }

    /**
     * Sets the version of the {@link PublicationDetail} instance to build.
     * @param version the version of the publication.
     * @return itself.
     */
    public Builder setVersion(final String version) {
      publication.version = version;
      return this;
    }

    /**
     * Sets in the default language the given name and description of the publication to build.
     * @param name the name of the publication.
     * @param description the description of the publication.
     * @return itself.
     */
    public Builder setNameAndDescription(final String name, final String description) {
      publication.setName(name);
      publication.setDescription(description);
      return this;
    }
  }
}
