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
package org.silverpeas.core.webapi.contribution;

import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.webapi.base.annotation.Authorized;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * User: Yohann Chastagnier
 * Date: 21/05/13
 */
@RequestScoped
@Path("contribution/{componentInstanceId}/{contributionId}/content")
@Authorized
public class ContributionContentResource extends AbstractContributionResource {

  private static final String CONTRIBUTION_BASE_URI = "contribution";
  private static final String CONTRIBUTION_CONTENT_URI_PART = "content";
  private static final String CONTRIBUTION_CONTENT_FORM_URI_PART = "form";

  @PathParam("componentInstanceId")
  private String componentInstanceId;

  @PathParam("contributionId")
  private String contributionId;

  @Override
  protected String getResourceBasePath() {
    return CONTRIBUTION_BASE_URI;
  }

  @Override
  public String getComponentId() {
    return componentInstanceId;
  }

  @Override
  public String getContributionId() {
    return contributionId;
  }

  /**
   * Gets the JSON representation of contribution template.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @return the response to the HTTP GET request with the JSON representation of form
   *         informations.
   */
  @GET
  @Path("form")
  @Produces(APPLICATION_JSON)
  public FormEntity getFormContent(@QueryParam("lang") final String lang) {
    return getFormContent(getDefaultFormId(), lang, false);
  }

  /**
   * Gets the JSON representation of contribution template.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If the user isn't authentified, a 401 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @param formId the form identifier
   * @param lang the language
   * @return the response to the HTTP GET request with the JSON representation of form
   *         informations.
   */
  @GET
  @Path("form/{formId}")
  @Produces(APPLICATION_JSON)
  public FormEntity getFormContent(@PathParam("formId") final String formId,
      @QueryParam("lang") final String lang, @QueryParam("renderView") final boolean renderView) {
    try {
      final String language =
          (StringUtil.isDefined(lang) ? lang : getDefaultPublicationTemplateLanguage());
      final FormData formData = loadFormData(formId, language, renderView);
      final DataRecord data = formData.getData();
      // Creating the form entity
      final FormEntity form = FormEntity.createFrom(formId);
      // Adding form content
      for (FieldTemplate fieldTemplate : formData.getRecordTemplate().getFieldTemplates()) {
        final Map<String, String> keyValuePairs =
            ((GenericFieldTemplate) fieldTemplate).getKeyValuePairs(lang);
        if (fieldTemplate.isRepeatable() || !keyValuePairs.isEmpty()) {
          // Field is repeatable or multi-valuable (like checkbox)
          final List<FormFieldValueEntity>
              fieldValueEntities = getFormFieldValues(fieldTemplate, data, language);
          // Field entity
          final FormFieldEntity fieldEntity = FormFieldEntity
              .createFrom(fieldTemplate.getTypeName(), fieldTemplate.getFieldName(),
                  fieldTemplate.getLabel(language), fieldValueEntities);
          // Adding field to the form entity
          form.addFormField(fieldEntity);
        } else {
          // Field value
          final FormFieldValueEntity fieldValueEntity = getFormFieldValue(fieldTemplate, data, language);
          // Field entity
          final FormFieldEntity fieldEntity = FormFieldEntity
              .createFrom(fieldTemplate.getTypeName(), fieldTemplate.getFieldName(),
                  fieldTemplate.getLabel(language), fieldValueEntity);
          // Adding field to the form entity
          form.addFormField(fieldEntity);
        }
      }
      // Render view if any
      formData.getFormView().ifPresent(v -> {
        final PagesContext context =
            new PagesContext("myForm", "0", getUserPreferences().getLanguage(), false,
                getComponentId(), getUser().getId());
        context.setObjectId(contributionId);
        form.withRenderedView(v.toString(context, data));
      });
      // Returning the contribution content entity
      final URI formUri = getUri().getBaseUriBuilder()
          .segment(CONTRIBUTION_BASE_URI, getComponentId(), getContributionId(),
              CONTRIBUTION_CONTENT_URI_PART, CONTRIBUTION_CONTENT_FORM_URI_PART, formId)
          .build();
      return form.withURI(formUri);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Response.Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Loading the data of a form registration from its identifier and a content language.
   * @param formId the identifier of the form registration.
   * @param language the aimed content language.
   * @param renderView true tu also get the render view form, false to avoid to load it.
   * @return a {@link FormData} instance containing all the aimed data.
   * @throws PublicationTemplateException in case of technical problem with services.
   * @throws FormException in case of technical problem with services.
   * @throws WebApplicationException thrown if no data have been found from given identifier and
   * given language.
   */
  private FormData loadFormData(final String formId, final String language,
      final boolean renderView) throws PublicationTemplateException, FormException {
    final FormData formData = new FormData();
    if (StringUtil.isDefined(formId)) {
      final PublicationTemplate publicationTemplate = getPublicationTemplate(formId);
      if (publicationTemplate != null) {
        if (renderView) {
          formData.setFormView(publicationTemplate.getViewForm());
        }
        formData.setRecordTemplate(publicationTemplate.getRecordTemplate());
        formData.setData(publicationTemplate.getRecordSet().getRecord(contributionId, language));
      }
    }
    // If no data exists, http not found error is returned
    if (formData.getRecordTemplate() == null || formData.getData() == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    return formData;
  }

  private static class FormData {
    private RecordTemplate recordTemplate = null;
    private DataRecord data = null;
    private Form formView = null;

    public RecordTemplate getRecordTemplate() {
      return recordTemplate;
    }

    public void setRecordTemplate(final RecordTemplate recordTemplate) {
      this.recordTemplate = recordTemplate;
    }

    public DataRecord getData() {
      return data;
    }

    public void setData(final DataRecord data) {
      this.data = data;
    }

    public Optional<Form> getFormView() {
      return Optional.ofNullable(formView);
    }

    public void setFormView(final Form formView) {
      this.formView = formView;
    }
  }
}
