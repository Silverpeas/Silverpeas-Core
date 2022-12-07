/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.web.documenttemplate;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.documenttemplate.DocumentTemplate;
import org.silverpeas.core.io.upload.UploadedFile;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.webcomponent.annotation.Homepage;
import org.silverpeas.core.web.mvc.webcomponent.annotation.LowestRoleAccess;
import org.silverpeas.core.web.mvc.webcomponent.annotation.RedirectToInternalJsp;
import org.silverpeas.core.web.mvc.webcomponent.annotation.WebComponentController;
import org.silverpeas.core.webapi.documenttemplate.DocumentTemplateWebManager;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.silverpeas.core.io.upload.FileUploadManager.getUploadedFiles;
import static org.silverpeas.core.util.file.FileUtil.isOpenOfficeCompatible;
import static org.silverpeas.core.webapi.documenttemplate.DocumentTemplateResourceURIs.DOC_TEMPLATE_BASE_URI;

/**
 * @author silveryocha
 */
@WebComponentController(DocumentTemplateWebController.DOC_TEMPLATE_COMPONENT_NAME)
public class DocumentTemplateWebController extends
    org.silverpeas.core.web.mvc.webcomponent.WebComponentController<DocumentTemplateWebRequestContext> {

  public static final String DOC_TEMPLATE_COMPONENT_NAME = DOC_TEMPLATE_BASE_URI;

  public DocumentTemplateWebController(final MainSessionController controller, final ComponentContext context) {
    super(controller, context, "org.silverpeas.documentTemplate.multilang.documentTemplate");
  }

  @Override
  protected void onInstantiation(final DocumentTemplateWebRequestContext context) {
    // nothing to do
  }

  @Override
  public String getComponentName() {
    return DOC_TEMPLATE_COMPONENT_NAME;
  }

  @GET
  @Path("Main")
  @Homepage
  @RedirectToInternalJsp("documentTemplates.jsp")
  @LowestRoleAccess(SilverpeasRole.ADMIN)
  public void home(DocumentTemplateWebRequestContext context) {
    context.getRequest().setAttribute("documentTemplateList", DocumentTemplateWebManager.get().getAllDocumentTemplates());
  }

  @GET
  @Path("new")
  @RedirectToInternalJsp("documentTemplateSaveFragment.jsp")
  @LowestRoleAccess(SilverpeasRole.ADMIN)
  public void newDocumentTemplate(DocumentTemplateWebRequestContext context) {
    context.getRequest().setAttribute("documentTemplate", new DocumentTemplate());
  }

  @POST
  @Path("create")
  @Produces(MediaType.APPLICATION_JSON)
  @LowestRoleAccess(SilverpeasRole.ADMIN)
  public void createDocumentTemplate(DocumentTemplateWebRequestContext context) {
    final DocumentTemplate documentTemplate = new DocumentTemplate();
    final UploadedFile uploadedFile = mergeDocumentTemplateData(context, documentTemplate);
    DocumentTemplateWebManager.get().createDocumentTemplate(documentTemplate, uploadedFile);
  }

  @GET
  @Path("modify/{id}")
  @RedirectToInternalJsp("documentTemplateSaveFragment.jsp")
  @LowestRoleAccess(SilverpeasRole.ADMIN)
  public void modifyDocumentTemplate(DocumentTemplateWebRequestContext context) {
    final String id = context.getPathVariables().get("id");
    final DocumentTemplate documentTemplate = DocumentTemplateWebManager.get().getDocumentTemplate(id);
    context.getRequest().setAttribute("documentTemplate", documentTemplate);
  }

  @POST
  @Path("update/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @LowestRoleAccess(SilverpeasRole.ADMIN)
  public void updateDocumentTemplate(DocumentTemplateWebRequestContext context) {
    final String id = context.getPathVariables().get("id");
    final DocumentTemplate documentTemplate = DocumentTemplateWebManager.get().getDocumentTemplate(id);
    final UploadedFile uploadedFile = mergeDocumentTemplateData(context, documentTemplate);
    DocumentTemplateWebManager.get().updateDocumentTemplate(documentTemplate, uploadedFile);
  }

  @POST
  @Path("deleteSelected")
  @Produces(MediaType.APPLICATION_JSON)
  @LowestRoleAccess(SilverpeasRole.ADMIN)
  public void deleteDocumentTemplates(DocumentTemplateWebRequestContext context) {
    DocumentTemplateWebManager.get().deleteDocumentTemplates(context.getRequest().getParameterAsList("id"));
  }

  @GET
  @Path("refreshList")
  @Produces(MediaType.APPLICATION_JSON)
  @LowestRoleAccess(SilverpeasRole.ADMIN)
  public void refreshDocumentTemplates(DocumentTemplateWebRequestContext context) {
    DocumentTemplateWebManager.get().clearCachedList();
  }

  @SuppressWarnings("unchecked")
  @POST
  @Path("sort")
  @Produces(MediaType.APPLICATION_JSON)
  @LowestRoleAccess(SilverpeasRole.ADMIN)
  public void sortDocumentTemplates(DocumentTemplateWebRequestContext context) {
    final Map<String, List<String>> ids;
    try(final InputStream is = context.getRequest().getInputStream()) {
      ids = JSONCodec.decode(is, Map.class);
    } catch (IOException e) {
      throw new WebApplicationException(BAD_REQUEST);
    }
    DocumentTemplateWebManager.get()
        .sortDocumentTemplates(ids.entrySet()
            .stream()
            .flatMap(e -> e.getValue().stream())
            .collect(Collectors.toList()));
  }

  private UploadedFile mergeDocumentTemplateData(DocumentTemplateWebRequestContext context,
      final DocumentTemplate documentTemplate) {
    final HttpRequest request = context.getRequest();
    ofNullable(context.getRequest().getParameterAsInteger("position"))
        .ifPresent(documentTemplate::setPosition);
    DisplayI18NHelper.getLanguages().forEach(l -> {
      documentTemplate.setName(request.getParameter("name-" + l), l);
      documentTemplate.setDescription(request.getParameter("description-" + l), l);
    });
    final Optional<UploadedFile> uploadedFile = getUploadedFiles(request,
        context.getUser()).stream().findFirst();
    uploadedFile.ifPresent(f -> {
      if (isOpenOfficeCompatible(f.getFile().getPath())) {
        documentTemplate.setExtension(getExtension(f.getFile().getName()));
      } else {
        context.getMessager().addError(getString("docTemplate.save.content.type"));
      }
    });
    return uploadedFile.orElse(null);
  }
}
