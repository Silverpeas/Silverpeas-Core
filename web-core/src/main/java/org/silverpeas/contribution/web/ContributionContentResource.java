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

import com.silverpeas.annotation.Authorized;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.form.DataRecord;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.Form;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.util.StringUtil;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * User: Yohann Chastagnier
 * Date: 21/05/13
 */
@Service
@RequestScoped
@Path("contribution/{componentInstanceId}/{contributionId}/content")
@Authorized
public class ContributionContentResource extends AbstractContributionResource {

  @PathParam("componentInstanceId")
  private String componentInstanceId;

  @PathParam("contributionId")
  private String contributionId;

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
   * @param formId
   * @param lang
   * @return the response to the HTTP GET request with the JSON representation of form
   *         informations.
   */
  @GET
  @Path("form/{formId}")
  @Produces(APPLICATION_JSON)
  public FormEntity getFormContent(@PathParam("formId") final String formId,
      @QueryParam("lang") final String lang, @QueryParam("renderView") final boolean renderView) {
    try {
      String language =
          (StringUtil.isDefined(lang) ? lang : getDefaultPublicationTemplateLanguage());

      RecordTemplate recordTemplate = null;
      DataRecord data = null;
      Form formView = null;
      if (StringUtil.isDefined(formId)) {
        PublicationTemplate publicationTemplate = getPublicationTemplate(formId);
        if (publicationTemplate != null) {
          if (renderView) {
            formView = publicationTemplate.getViewForm();
          }
          recordTemplate = publicationTemplate.getRecordTemplate();
          data = publicationTemplate.getRecordSet().getRecord(contributionId, language);
        }
      }

      // If no data exists, http not found error is returned
      if (data == null) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      }

      // Creating the form entity
      FormEntity form = FormEntity.createFrom(formId);

      // Adding form content
      for (FieldTemplate fieldTemplate : recordTemplate.getFieldTemplates()) {

        // Field value
        FormFieldValueEntity fieldValueEntity = getFormFieldValue(fieldTemplate, data, language);

        // Field entity
        FormFieldEntity fieldEntity = FormFieldEntity
            .createFrom(fieldTemplate.getTypeName(), fieldTemplate.getFieldName(),
                fieldTemplate.getLabel(language), fieldValueEntity);

        // Adding field to the form entity
        form.addFormField(fieldEntity);
      }

      if (formView != null) {
        PagesContext context =
            new PagesContext("myForm", "0", getUserPreferences().getLanguage(), false,
                getComponentId(), getUserDetail().getId());
        context.setObjectId(contributionId);
        form.withRenderedView(formView.toString(context, data));
      }

      // Returning the contribution content entity
      return form.withURI(ContributionResourceURIs.buildURIOfContributionFormContent(this, formId));

    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Response.Status.SERVICE_UNAVAILABLE);
    }
  }
}
