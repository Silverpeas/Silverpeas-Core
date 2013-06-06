/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.contribution.web;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.displayers.WysiwygFCKFieldDisplayer;
import com.silverpeas.form.fieldType.UserField;
import com.silverpeas.form.record.GenericFieldTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.StringUtil;
import com.silverpeas.web.RESTWebService;
import com.stratelia.silverpeas.peasCore.URLManager;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Map;

/**
 * User: Yohann Chastagnier
 * Date: 21/05/13
 */
public abstract class AbstractContributionResource extends RESTWebService {

  public abstract String getContributionId();

  /**
   * Gets the default form id
   * @return
   */
  protected String getDefaultFormId() {
    String xmlFormName =
        getOrganisationController().getComponentParameterValue(getComponentId(), "XMLFormName");
    if (StringUtil.isDefined(xmlFormName)) {
      return xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
    }
    return null;
  }

  /**
   * Gets the default publication template language
   * @return
   */
  protected String getDefaultPublicationTemplateLanguage() {
    return getUserPreferences().getLanguage();
  }

  /**
   * Creating the external id from the specified form id
   * @param formId
   * @return
   */
  private String createExternalId(String formId) {
    return getComponentId() + ":" + formId;
  }

  /**
   * Gets the value of a field.
   * @param fieldTemplate
   * @param data
   * @param lang
   * @return
   * @throws Exception
   */
  protected FormFieldValueEntity getFormFieldValue(FieldTemplate fieldTemplate, DataRecord data,
      String lang) throws Exception {

    // Field data
    Field field = data.getField(fieldTemplate.getFieldName());

    final FormFieldValueEntity entity;
    if (Field.TYPE_FILE.equals(field.getTypeName())) {

      // File Field case
      String fieldValue = field.getValue(lang);
      String attachmentId = null;
      String attachmentUrl = null;
      URI attachmentUri = null;
      if (StringUtil.isDefined(fieldValue)) {
        String serverApplicationURL =
            URLManager.getServerURL(getHttpServletRequest()) + URLManager.getApplicationURL();
        if (fieldValue.startsWith("/")) {
          // case of an image provided by a gallery
          attachmentUrl = fieldValue;
          fieldValue = null;
        } else {
          SimpleDocument attachment = AttachmentServiceFactory.getAttachmentService()
              .searchDocumentById(new SimpleDocumentPK(fieldValue, getComponentId()), lang);
          if (attachment != null) {
            attachmentId = fieldValue;
            fieldValue = attachment.getTitle();
            attachmentUrl = serverApplicationURL + attachment.getAttachmentURL();
            attachmentUri = new URI(URLManager.getServerURL(getHttpServletRequest()) +
                URLManager.getSimpleURL(URLManager.URL_FILE, attachmentId));
          }
        }
      }
      if (fieldValue == null) {
        fieldValue = "";
      }
      entity = FormFieldValueEntity.createFrom(attachmentId, fieldValue).withLink(attachmentUrl)
          .withAttachmentURI(attachmentUri);

    } else if (UserField.TYPE.equals(field.getTypeName())) {

      // User field case
      UserField userField = (UserField) field;
      entity = FormFieldValueEntity.createFrom(userField.getUserId(), userField.getValue());

    } else {

      // Text field case
      String fieldValue = field.getValue(lang);

      if (StringUtil.isDefined(fieldValue)) {
        if (fieldValue.startsWith(WysiwygFCKFieldDisplayer.dbKey)) {

          // Rich text
          entity = FormFieldValueEntity.createFrom(null, WysiwygFCKFieldDisplayer
              .getContentFromFile(getComponentId(), getContributionId(),
                  fieldTemplate.getFieldName(), lang));

        } else {

          // Simple text
          Map<String, String> keyValuePairs =
              ((GenericFieldTemplate) fieldTemplate).getKeyValuePairs(lang);

          // Field value entity
          if (keyValuePairs.isEmpty()) {
            // Simple value
            entity = FormFieldValueEntity.createFrom(null, fieldValue);
          } else {
            // Value like checkbox for example.
            // Value is empty if "##" string is detected.
            entity = FormFieldValueEntity
                .createFrom((!fieldValue.contains("##") ? fieldValue : null),
                    keyValuePairs.get(fieldValue));
          }
        }
      } else {

        // No value
        entity = FormFieldValueEntity.createFrom(null, "");
      }
    }
    return entity;
  }

  /**
   * Gets the publication template of the specified form id
   * @param formId
   * @return
   */
  protected PublicationTemplate getPublicationTemplate(String formId)
      throws PublicationTemplateException {
    try {
      return PublicationTemplateManager.getInstance()
          .getPublicationTemplate(createExternalId(formId));
    } catch (PublicationTemplateException e) {
      throw new WebApplicationException(e, Response.Status.NOT_FOUND);
    }
  }
}
