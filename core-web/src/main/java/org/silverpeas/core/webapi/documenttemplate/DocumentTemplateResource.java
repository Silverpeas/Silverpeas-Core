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

package org.silverpeas.core.webapi.documenttemplate;

import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.documenttemplate.DocumentTemplate;
import org.silverpeas.core.documenttemplate.DocumentTemplateRestrictionFilter;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.annotation.Authenticated;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.silverpeas.core.webapi.documenttemplate.DocumentTemplateResourceURIs.DOC_TEMPLATE_BASE_URI;
import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * @author silveryocha
 */
@WebService
@Path(DOC_TEMPLATE_BASE_URI)
@Authenticated
public class DocumentTemplateResource extends RESTWebService {

  @Inject
  private DocumentTemplateWebManager manager;

  @Inject
  private DocumentTemplateResourceURIs uri;

  @QueryParam("instanceIdFilter")
  private String instanceIdFilter;

  @Override
  protected String getResourceBasePath() {
    return DOC_TEMPLATE_BASE_URI;
  }

  /**
   * Gets the JSON representation of a document template.
   * If it doesn't exist, a 404 HTTP code is returned.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * document template.
   */
  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public DocumentTemplateEntity get(@PathParam("id") final String id) {
    return asWebEntity(manager.getDocumentTemplate(id));
  }

  /**
   * Gets the JSON representation of a list of document template.
   * If it doesn't exist, a 404 HTTP code is returned.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   * document templates.
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<DocumentTemplateEntity> listAll() {
    checkUserCanAccessInstanceIdIfAny();
    final DocumentTemplateRestrictionFilter filter = new DocumentTemplateRestrictionFilter()
        .setInstanceId(instanceIdFilter);
    return asWebEntities(manager.getAllDocumentTemplates().stream().filter(filter::applyOn));
  }

  private List<DocumentTemplateEntity> asWebEntities(final Stream<DocumentTemplate> docTemplates) {
    return docTemplates.map(this::asWebEntity).collect(Collectors.toList());
  }

  private DocumentTemplateEntity asWebEntity(final DocumentTemplate docTemplate) {
    return DocumentTemplateEntity.from(docTemplate, uri.ofDocumentTemplate(docTemplate),
        getHttpRequest().getUserLanguage());
  }

  private void checkUserCanAccessInstanceIdIfAny() {
    if (isDefined(instanceIdFilter) &&
        !ComponentAccessControl.get().isUserAuthorized(getUser().getId(), instanceIdFilter)) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  @Override
  public String getComponentId() {
    return null;
  }
}
