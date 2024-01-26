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
package org.silverpeas.core.webapi.contribution;

import org.apache.commons.io.FilenameUtils;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.displayers.WysiwygFCKFieldDisplayer;
import org.silverpeas.core.contribution.content.form.field.UserField;
import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.core.web.rs.RESTWebService;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: Yohann Chastagnier
 * Date: 21/05/13
 */
public abstract class AbstractContributionResource extends RESTWebService {

  public abstract String getContributionId();

  /**
   * @return the default form id
   */
  protected String getDefaultFormId() {
    final String xmlFormName =
        getOrganisationController().getComponentParameterValue(getComponentId(), "XMLFormName");
    if (StringUtil.isDefined(xmlFormName)) {
      return xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName.indexOf('.'));
    }
    return null;
  }

  /**
   * @return the default publication template language
   */
  protected String getDefaultPublicationTemplateLanguage() {
    return getUserPreferences().getLanguage();
  }

  /**
   * Creating the external id from the specified form id
   * @param formId the form identifier
   * @return a string representing the external identifier of given form identifier.
   */
  private String createExternalId(String formId) {
    return getComponentId() + ":" + FilenameUtils.getBaseName(formId);
  }

  protected List<FormFieldValueEntity> getFormFieldValues(FieldTemplate fieldTemplate,
      DataRecord data, String lang) throws FormException {
    final int maxOccurrences = fieldTemplate.getMaximumNumberOfOccurrences();
    final List<FormFieldValueEntity> values = new ArrayList<>();
    final Map<String, String> keyValuePairs =
        ((GenericFieldTemplate) fieldTemplate).getKeyValuePairs(lang);
    if (!keyValuePairs.isEmpty()) {
      // it's a multi-value field (checkbox)
      final Field field = data.getField(fieldTemplate.getFieldName());
      final String fieldValue = field.getValue(lang);
      if (StringUtil.isDefined(fieldValue)) {
        final String[] fieldValues = fieldValue.split("##");
        for (String value : fieldValues) {
          final FormFieldValueEntity valueEntity =
              FormFieldValueEntity.createFrom(value, keyValuePairs.getOrDefault(value, ""));
          values.add(valueEntity);
        }
      }
    } else {
      for (int occ = 0; occ < maxOccurrences; occ++) {
        final Field field = data.getField(fieldTemplate.getFieldName(), occ);
        if (field != null && !field.isNull()) {
          final FormFieldValueEntity value = getFormFieldValue(fieldTemplate, field, lang);
          values.add(value);
        }
      }
    }
    return values;
  }

  /**
   * Gets the value of a field.
   * @param fieldTemplate a field template.
   * @param data the data of a form registration.
   * @param lang the content language of data.
   * @return a {@link FormFieldValueEntity} instance initialized with given form data.
   * @throws FormException which could be thrown by form services.
   */
  protected FormFieldValueEntity getFormFieldValue(FieldTemplate fieldTemplate, DataRecord data,
      String lang) throws FormException {
    // Field data
    final Field field = data.getField(fieldTemplate.getFieldName());
    return getFormFieldValue(fieldTemplate, field, lang);
  }

  private FormFieldValueEntity getFormFieldValue(FieldTemplate fieldTemplate, Field field,
      String lang) {
    final FormFieldValueEntity entity;
    if (Field.TYPE_FILE.equals(field.getTypeName())) {
      entity = createFileValueEntity(field, lang);
    } else if (UserField.TYPE.equals(field.getTypeName())) {
      entity = createUserValueEntity((UserField) field);
    } else {
      entity = createTextValueEntity(fieldTemplate, field, lang);
    }
    return entity;
  }

  private FormFieldValueEntity createTextValueEntity(final FieldTemplate fieldTemplate,
      final Field field, final String lang) {
    final FormFieldValueEntity entity;
    final String fieldValue = field.getValue(lang);
    if (StringUtil.isDefined(fieldValue)) {
      if (fieldValue.startsWith(WysiwygFCKFieldDisplayer.DB_KEY)) {
        // Rich text
        entity = FormFieldValueEntity.createFrom(null, WysiwygFCKFieldDisplayer
            .getContentFromFile(getComponentId(), getContributionId(),
                fieldTemplate.getFieldName(), lang));
      } else {
        // Simple value
        entity = FormFieldValueEntity.createFrom(null, fieldValue);
      }
    } else {
      // No value
      entity = FormFieldValueEntity.createFrom(null, "");
    }
    return entity;
  }

  private FormFieldValueEntity createUserValueEntity(final UserField userField) {
    return FormFieldValueEntity.createFrom(userField.getUserId(), userField.getValue());
  }

  private FormFieldValueEntity createFileValueEntity(final Field field, final String lang) {
    final FormFieldValueEntity entity;
    String fieldValue = field.getValue(lang);
    String attachmentId = null;
    String attachmentUrl = null;
    URI attachmentUri = null;
    if (StringUtil.isDefined(fieldValue)) {
      String serverApplicationURL =
          URLUtil.getServerURL(getHttpServletRequest()) + URLUtil.getApplicationURL();
      if (fieldValue.startsWith("/")) {
        // case of an image provided by a gallery
        attachmentUrl = fieldValue;
        fieldValue = null;
      } else {
        SimpleDocument attachment = AttachmentServiceProvider.getAttachmentService()
            .searchDocumentById(new SimpleDocumentPK(fieldValue, getComponentId()), lang);
        if (attachment != null) {
          attachmentId = fieldValue;
          fieldValue = attachment.getTitle();
          attachmentUrl = serverApplicationURL + attachment.getAttachmentURL();
          try {
            attachmentUri = new URI(URLUtil.getServerURL(getHttpServletRequest()) +
                URLUtil.getSimpleURL(URLUtil.URL_FILE, attachmentId));
          } catch (URISyntaxException e) {
            SilverLogger.getLogger(this).warn(e);
          }
        }
      }
    }
    if (fieldValue == null) {
      fieldValue = "";
    }
    entity = FormFieldValueEntity.createFrom(attachmentId, fieldValue).withLink(attachmentUrl)
        .withAttachmentURI(attachmentUri);
    return entity;
  }

  /**
   * Gets the publication template of the specified form id
   * @param formId an identifier of a form.
   * @return the corresponding {@link PublicationTemplate} instance if any, null otherwise.
   */
  protected PublicationTemplate getPublicationTemplate(String formId) {
    try {
      return PublicationTemplateManager.getInstance()
          .getPublicationTemplate(createExternalId(formId));
    } catch (PublicationTemplateException e) {
      throw new WebApplicationException(e, Response.Status.NOT_FOUND);
    }
  }
}
