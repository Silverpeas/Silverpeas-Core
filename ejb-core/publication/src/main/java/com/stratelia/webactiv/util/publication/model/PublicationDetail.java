/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.stratelia.webactiv.util.publication.model;

import com.silverpeas.SilverpeasContent;
import com.silverpeas.accesscontrol.AccessController;
import com.silverpeas.accesscontrol.AccessControllerProvider;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.TypeManager;
import com.silverpeas.form.displayers.WysiwygFCKFieldDisplayer;
import com.silverpeas.form.importExport.XMLField;
import com.silverpeas.form.record.GenericFieldTemplate;
import com.silverpeas.formTemplate.ejb.FormTemplateBm;
import com.silverpeas.notation.ejb.RatingServiceFactory;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.thumbnail.control.ThumbnailController;
import com.silverpeas.thumbnail.model.ThumbnailDetail;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.AbstractI18NBean;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.contentManager.ContentManagerFactory;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.info.model.InfoDetail;
import com.stratelia.webactiv.util.publication.info.model.InfoTextDetail;
import org.apache.commons.lang3.ObjectUtils;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.DocumentType;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.importExport.attachment.AttachmentPK;
import org.silverpeas.rating.Rateable;
import org.silverpeas.rating.ContributionRating;
import org.silverpeas.rating.ContributionRatingPK;
import org.silverpeas.search.indexEngine.model.IndexManager;
import org.silverpeas.wysiwyg.control.WysiwygController;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * This object contains the description of a publication
 */
public class PublicationDetail extends AbstractI18NBean<PublicationI18N> implements SilverContentInterface,
    SilverpeasContent, Rateable, Serializable, Cloneable {

  private static final long serialVersionUID = 9199848912262605680L;
  private PublicationPK pk;
  private String infoId;
  private Date creationDate;
  private Date beginDate;
  private Date endDate;
  private String creatorId;
  private String creatorName;
  private int importance;
  private String version;
  private String keywords;
  private String content;
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
  private String silverObjectId; // added for the components - PDC integration
  private String iconUrl;
  private int explicitRank = -1;
  // added for the taglib
  private InfoDetail infoDetail = null;
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
  public static final String DRAFT = "Draft";
  public static final String VALID = "Valid";
  public static final String TO_VALIDATE = "ToValidate";
  public static final String REFUSED = "Unvalidate";
  public static final String CLONE = "Clone";
  public static final String TYPE = "Publication";

  private ContributionRating contributionRating;

  /**
   * Default contructor, required for castor mapping in importExport.
   */
  public PublicationDetail() {
  }

  public PublicationDetail(PublicationPK pk, String name, String description,
      Date creationDate, Date beginDate, Date endDate, String creatorId,
      int importance, String version, String keywords, String content) {
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
  public PublicationDetail(String id, String name, String description,
      Date creationDate, Date beginDate, Date endDate, String creatorId,
      String importance, String version, String keywords, String content) {
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

  public PublicationDetail(PublicationPK pk, String name, String description,
      Date creationDate, Date beginDate, Date endDate, String creatorId,
      int importance, String version, String keywords, String content,
      String status) {
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

  public PublicationDetail(PublicationPK pk, String name, String description,
      Date creationDate, Date beginDate, Date endDate, String creatorId,
      int importance, String version, String keywords, String content,
      String status, Date updateDate) {
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
   * @deprecated @param pk
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
   */
  public PublicationDetail(PublicationPK pk, String name, String description,
      Date creationDate, Date beginDate, Date endDate, String creatorId,
      int importance, String version, String keywords, String content,
      String status, Date updateDate,
      String updaterId) {
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

  public PublicationDetail(PublicationPK pk, String name, String description,
      Date creationDate, Date beginDate, Date endDate, String creatorId,
      int importance, String version, String keywords, String content,
      String status, Date updateDate,
      String updaterId, String author) {
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
   * @deprecated @param id
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
   */
  public PublicationDetail(String id, String name, String description,
      Date creationDate, Date beginDate, Date endDate, String creatorId,
      String importance, String version, String keywords, String content,
      String status) {
    this.pk = new PublicationPK(id);
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.beginDate = beginDate;
    this.endDate = endDate;
    this.creatorId = creatorId;
    this.importance = new Integer(importance).intValue();
    this.version = version;
    this.keywords = keywords;
    this.content = content;
    this.status = status;
  }

  public PublicationDetail(String id, String name, String description,
      Date creationDate, Date beginDate, Date endDate, String creatorId,
      String importance, String version, String keywords, String content,
      String status, String updaterId, String author) {
    this.pk = new PublicationPK(id);
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.beginDate = beginDate;
    this.endDate = endDate;
    this.creatorId = creatorId;
    this.importance = new Integer(importance).intValue();
    this.version = version;
    this.keywords = keywords;
    this.content = content;
    this.status = status;
    this.updaterId = updaterId;
    this.author = author;
  }

  public PublicationDetail(String id, String name, String description,
      Date creationDate, Date beginDate, Date endDate, String creatorId,
      String importance, String version, String keywords, String content,
      String status, Date updateDate) {
    this.pk = new PublicationPK(id);
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.beginDate = beginDate;
    this.endDate = endDate;
    this.creatorId = creatorId;
    this.importance = new Integer(importance).intValue();
    this.version = version;
    this.keywords = keywords;
    this.content = content;
    this.status = status;
    this.updateDate = updateDate;
  }

  public PublicationDetail(String id, String name, String description,
      Date creationDate, Date beginDate, Date endDate, String creatorId,
      String importance, String version, String keywords, String content,
      String status, Date updateDate,
      String updaterId) {
    this.pk = new PublicationPK(id);
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.beginDate = beginDate;
    this.endDate = endDate;
    this.creatorId = creatorId;
    this.importance = new Integer(importance).intValue();
    this.version = version;
    this.keywords = keywords;
    this.content = content;
    this.status = status;
    this.updateDate = updateDate;
    this.updaterId = updaterId;
  }

  public PublicationDetail(String id, String name, String description,
      Date creationDate, Date beginDate, Date endDate, String creatorId,
      String importance, String version, String keywords, String content,
      String status, Date updateDate,
      String updaterId, Date validateDate, String validatorId) {
    this.pk = new PublicationPK(id);
    setName(name);
    setDescription(description);
    this.creationDate = creationDate;
    this.beginDate = beginDate;
    this.endDate = endDate;
    this.creatorId = creatorId;
    this.importance = new Integer(importance).intValue();
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
   *
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
   *
   * @deprecated
   */
  public PublicationDetail(PublicationPK pk, String name, String description,
      Date creationDate, Date beginDate, Date endDate, String creatorId,
      int importance, String version, String keywords, String content,
      String status, Date updateDate,
      String updaterId, Date validateDate, String validatorId) {
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

  public PublicationDetail(PublicationPK pk, String name, String description,
      Date creationDate, Date beginDate, Date endDate, String creatorId,
      int importance, String version, String keywords, String content,
      String status, Date updateDate,
      String updaterId, Date validateDate, String validatorId, String author) {
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

  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

  public void setContent(String content) {
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

  @Override
  public String getCreatorId() {
    return creatorId;
  }

  @Override
  public UserDetail getCreator() {
    return UserDetail.getById(getCreatorId());
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
    if (!I18NHelper.isI18N) {
      return getKeywords();
    }
    PublicationI18N p = (PublicationI18N) getTranslations().get(lang);
    if (p == null) {
      p = (PublicationI18N) getNextTranslation();
    }
    if (p != null) {
      return p.getKeywords();
    }
    return getKeywords();
  }

  public String getContent() {
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
    result.append(" getContent() = ").append(getContent()).append("\n");
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
    this.silverObjectId = new Integer(silverObjectId).toString();
  }

  public String getSilverObjectId() {
    if (this.silverObjectId == null) {
      ContentManager contentManager = ContentManagerFactory.getFactory().getContentManager();
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
      return new ArrayList<XMLField>();
    }
    if (xmlFields == null) {
      try {
        xmlFields = getFormTemplateBm().getXMLFieldsForExport(
            getPK().getInstanceId() + ":" + getInfoId(), getPK().getId(), language);
      } catch (Exception e) {
        throw new PublicationRuntimeException(
            "PublicationDetail.getDataRecord()",
            SilverpeasRuntimeException.ERROR,
            "publication.EX_IMPOSSIBLE_DE_FABRIQUER_FORMTEMPLATEBM_HOME", e);
      }
    }
    return xmlFields;
  }

  public HashMap<String, String> getFormValues(String language) {
    HashMap<String, String> formValues = new HashMap<String, String>();
    if ("0".equals(getInfoId())) {
      // this publication does not use a form
      return formValues;
    }

    DataRecord data = null;
    PublicationTemplate pub = null;
    try {
      pub = PublicationTemplateManager.getInstance().getPublicationTemplate(
          getPK().getInstanceId() + ":" + getInfoId());
      data = pub.getRecordSet().getRecord(pk.getId());
    } catch (Exception e) {
      SilverTrace.warn("publication", "PublicationDetail.getFormValues", "CANT_GET_FORM_RECORD",
          "pubId = " + getPK().getId() + "infoId = " + getInfoId());
    }

    if (data == null) {
      return formValues;
    }

    String fieldNames[] = data.getFieldNames();
    PagesContext pageContext = new PagesContext();
    pageContext.setLanguage(language);
    for (String fieldName : fieldNames) {
      try {
        Field field = data.getField(fieldName);
        GenericFieldTemplate fieldTemplate = (GenericFieldTemplate) pub.getRecordTemplate()
            .getFieldTemplate(fieldName);
        FieldDisplayer fieldDisplayer = TypeManager.getInstance().getDisplayer(fieldTemplate
            .getTypeName(), "simpletext");
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        fieldDisplayer.display(out, field, fieldTemplate, pageContext);
        formValues.put(fieldName, sw.toString());
      } catch (Exception e) {
        SilverTrace.warn("publication", "PublicationDetail.getFormValues", "CANT_GET_FIELD_VALUE",
            "pubId = " + getPK().getId() + "fieldName = " + fieldName, e);
      }

    }
    return formValues;
  }

  public String getFieldValue(String fieldNameAndLanguage) {
    SilverTrace.info("publication", "PublicationDetail.getModelContent()",
        "root.MSG_GEN_ENTER_METHOD", "fieldNameAndLanguage = "
        + fieldNameAndLanguage);

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

    SilverTrace.info("publication", "PublicationDetail.getModelContent('" + fieldName + "')",
        "root.MSG_GEN_EXIT_METHOD", "fieldValue = " + fieldValue);
    return fieldValue;
  }

  private String getValueOfField(XMLField xmlField, String language) {
    String fieldValue = xmlField.getValue();
    if (fieldValue == null) {
      fieldValue = "";
    } else {
      if (fieldValue.startsWith("image_") || fieldValue.startsWith("file_")) {
        String attachmentId = fieldValue.substring(fieldValue.indexOf("_") + 1, fieldValue.length());
        if (StringUtil.isDefined(attachmentId)) {
          if (attachmentId.startsWith("/")) {
            // case of an image provided by a gallery
            fieldValue = attachmentId;
          } else {
            SimpleDocument attachment = AttachmentServiceFactory.getAttachmentService()
                .searchDocumentById(new SimpleDocumentPK(attachmentId, getPK().getInstanceId()),
                    language);
            if (attachment != null) {
              fieldValue = attachment.getAttachmentURL();
            }
          }
        } else {
          fieldValue = "";
        }
      } else if (fieldValue.startsWith(WysiwygFCKFieldDisplayer.dbKey)) {
        fieldValue = WysiwygFCKFieldDisplayer.getContentFromFile(getPK().getInstanceId(), getPK().
            getId(), xmlField.getName(), language);
      } else {
        fieldValue = EncodeHelper.javaStringToHtmlParagraphe(fieldValue);
      }
    }
    return fieldValue;
  }

  private FormTemplateBm getFormTemplateBm() {
    try {
      return EJBUtilitaire.getEJBObjectRef(JNDINames.FORMTEMPLATEBM_EJBHOME, FormTemplateBm.class);
    } catch (Exception e) {
      throw new PublicationRuntimeException("PublicationDetail.getFormTemplateBm()",
          SilverpeasRuntimeException.ERROR,
          "publication.EX_IMPOSSIBLE_DE_FABRIQUER_FORMTEMPLATEBM_HOME", e);
    }
  }

  public InfoDetail getInfoDetail() {
    if (infoDetail == null) {
      try {
        infoDetail = getPublicationBm().getInfoDetail(getPK());
      } catch (Exception e) {
        throw new PublicationRuntimeException("PublicationDetail.getInfoDetail()",
            SilverpeasRuntimeException.ERROR, "publication.GETTING_CONTENT_FAILED", e);
      }
    }
    return infoDetail;
  }

  public String getModelContent() {
    StringBuilder buffer = new StringBuilder();
    InfoDetail currentInfoSetail = getInfoDetail();
    if (currentInfoSetail != null) {
      Collection<InfoTextDetail> allInfoText = currentInfoSetail.getInfoTextList();
      if (allInfoText != null) {
        for (InfoTextDetail textDetail : allInfoText) {
          buffer.append(textDetail.getContent());
        }
      }
    }
    return buffer.toString();
  }

  public String getModelContent(int fieldIndex) {
    SilverTrace.info("publication", "PublicationDetail.getModelContent()",
        "root.MSG_GEN_ENTER_METHOD", "fieldIndex = " + fieldIndex);
    String fieldContent = "";
    InfoDetail currentInfoDetail = getInfoDetail();
    ArrayList<InfoTextDetail> allInfoText = null;
    if (currentInfoDetail != null) {
      allInfoText = (ArrayList<InfoTextDetail>) currentInfoDetail.getInfoTextList();
    }

    if (allInfoText != null) {
      if (fieldIndex < allInfoText.size()) {
        fieldContent = (allInfoText.get(fieldIndex)).getContent();
      }
    }
    return fieldContent;
  }

  public PublicationBm getPublicationBm() {
    if (publicationBm == null) {
      try {
        publicationBm = EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
            PublicationBm.class);
      } catch (Exception e) {
        throw new PublicationRuntimeException("PublicationDetail.getPublicationBm()",
            SilverpeasRuntimeException.ERROR,
            "publication.EX_IMPOSSIBLE_DE_FABRIQUER_PUBLICATIONBM_HOME", e);
      }
    }
    return publicationBm;
  }

  public Collection<SimpleDocument> getAttachments() {
    if (getPK() == null) {
      SilverTrace.info("publication", "PublicationDetail.getAttachments()",
          "root.MSG_GEN_ENTER_METHOD", "getPK() is null !");
    } else {
      SilverTrace.info("publication", "PublicationDetail.getAttachments()",
          "root.MSG_GEN_ENTER_METHOD", "getPK() is not null !");
    }
    AttachmentPK foreignKey = new AttachmentPK(getPK().getId(), getPK().getSpace(), getPK().
        getComponentName());
    SilverTrace.
        info("publication", "PublicationDetail.getAttachments()", "root.MSG_GEN_PARAM_VALUE",
            "foreignKey = " + foreignKey.toString());
    Collection<SimpleDocument> attachmentList = AttachmentServiceFactory.getAttachmentService().
        listDocumentsByForeignKeyAndType(foreignKey, DocumentType.attachment, null);
    SilverTrace.
        info("publication", "PublicationDetail.getAttachments()", "root.MSG_GEN_PARAM_VALUE",
            "attachmentList.size() = " + attachmentList.size());
    return attachmentList;
  }

  public String getWysiwyg() {
    String wysiwygContent;
    try {
      wysiwygContent = WysiwygController.load(getPK().getComponentName(), getPK().getId(),
          getLanguage());
    } catch (Exception e) {
      wysiwygContent = "Erreur lors du chargement du wysiwyg !";
    }
    return wysiwygContent;
  }
  private PublicationBm publicationBm = null;

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
    return "/R" + componentName + "/" + getPK().getInstanceId()
        + "/searchResult?Type=Publication&Id=" + getPK().getId();
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

  public String getCloneId() {
    return cloneId;
  }

  public void setCloneId(String tempPubId) {
    this.cloneId = tempPubId;
  }

  public boolean haveGotClone() {
    return (cloneId != null && !"-1".equals(cloneId) && !"null".equals(cloneId) && cloneId.length()
        > 0);
  }

  public boolean isClone() {
    return CLONE.equalsIgnoreCase(getStatus());
  }

  public boolean isValid() {
    return VALID.equalsIgnoreCase(getStatus());
  }

  public boolean isValidationRequired() {
    return TO_VALIDATE.equalsIgnoreCase(getStatus());
  }

  public boolean isRefused() {
    return REFUSED.equalsIgnoreCase(getStatus());
  }

  public boolean isDraft() {
    return DRAFT.equalsIgnoreCase(getStatus());
  }

  public PublicationPK getClonePK() {
    return new PublicationPK(getCloneId(), getPK());
  }

  @Override
  public Object clone() {
    PublicationDetail clone = new PublicationDetail();
    clone.setAuthor(author);
    clone.setBeginDate(beginDate);
    clone.setBeginHour(beginHour);
    clone.setContent(content);
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

  public boolean isIndexable() {
    return VALID.equals(this.status);
  }

  public boolean isPublicationEditor(String userId) {
    return ObjectUtils.equals(creatorId, userId) || ObjectUtils.equals(updaterId, userId);
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
  public String getComponentInstanceId() {
    return getPK().getInstanceId();
  }

  @Override
  public String getContributionType() {
    return TYPE;
  }

  /**
   * Is the specified user can access this publication?
   * <p/>
   * A user can access a publication if he has enough rights to access both the application instance
   * in which is managed this publication and one of the nodes to which this publication belongs to.
   *
   * @param user a user in Silverpeas.
   * @return true if the user can access this publication, false otherwise.
   */
  @Override
  public boolean canBeAccessedBy(final UserDetail user) {
    AccessController<String> accessController = AccessControllerProvider.getAccessController(
        "componentAccessController");
    boolean canBeAccessed = accessController.
        isUserAuthorized(user.getId(), getComponentInstanceId());
    if (canBeAccessed) {
      AccessController<NodePK> nodeAccessController = AccessControllerProvider.getAccessController(
          "nodeAccessController");
      Collection<NodePK> nodes = getPublicationBm().getAllFatherPK(new PublicationPK(getId(),
          getInstanceId()));
      for (NodePK aNode : nodes) {
        canBeAccessed = nodeAccessController.isUserAuthorized(user.getId(), aNode);
        if (canBeAccessed) {
          break;
        }
      }
    }
    return canBeAccessed;
  }

  /**
   * The type of this resource
   *
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
      contributionRating = RatingServiceFactory.getRatingService()
          .getRating(new ContributionRatingPK(getId(), getInstanceId(), "Publication"));
    }
    return contributionRating;
  }
}