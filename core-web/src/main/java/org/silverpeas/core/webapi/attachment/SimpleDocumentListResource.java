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
package org.silverpeas.core.webapi.attachment;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.io.media.image.ImageTool;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.viewer.service.PreviewService;
import org.silverpeas.core.viewer.service.ViewService;
import org.silverpeas.core.web.rs.UserPrivilegeValidation;
import org.silverpeas.core.web.rs.annotation.Authorized;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.silverpeas.core.admin.user.model.SilverpeasRole.READER;
import static org.silverpeas.core.admin.user.model.SilverpeasRole.USER;
import static org.silverpeas.core.io.media.image.ImageInfoType.HEIGHT_IN_PIXEL;
import static org.silverpeas.core.io.media.image.ImageInfoType.WIDTH_IN_PIXEL;

@WebService
@Path(AbstractSimpleDocumentResource.PATH + "/{componentId}/resource/{id}")
@Authorized
public class SimpleDocumentListResource extends AbstractSimpleDocumentResource {

  @Inject
  private UserPrivilegeValidation validation;

  @PathParam("id")
  private String resourceId;

  @QueryParam("viewIndicators")
  private boolean viewIndicators = false;

  @QueryParam("viewWidthAndHeight")
  private boolean viewWidthAndHeight = false;

  @QueryParam("highestUserRole")
  private SilverpeasRole highestUserRole = null;

  public String getResourceId() {
    return resourceId;
  }

  /**
   * Returns documents of specified resource in the specified language
   *
   * @param lang the wanted language.
   * @return documents of specified resource
   */
  @GET
  @Path("{lang}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<SimpleDocumentEntity> getDocuments(@PathParam("lang") final String lang) {
    List<SimpleDocument> docs =
        AttachmentServiceProvider.getAttachmentService().listDocumentsByForeignKeyAndType(
            getResourceReference(), DocumentType.attachment, lang);
    return asWebEntities(docs);
  }

  /**
   * Returns documents of specified resource in the specified language
   *
   * @param lang the wanted language.
   * @return documents of specified resource
   */
  @GET
  @Path("types/{type}/{lang}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<SimpleDocumentEntity> getDocumentsByType(@PathParam("type") final String type,
      @PathParam("lang") final String lang) {
    DocumentType documentType = DocumentType.decode(type);
    if (documentType == null) {
      return Collections.emptyList();
    }
    List<SimpleDocument> docs =
        AttachmentServiceProvider.getAttachmentService().listDocumentsByForeignKeyAndType(
            getResourceReference(), documentType, lang);
    return asWebEntities(docs);
  }

  private ResourceReference getResourceReference() {
    return new ResourceReference(getResourceId(), getComponentId());
  }

  private List<SimpleDocumentEntity> asWebEntities(List<SimpleDocument> docs) {
    final SilverpeasRole userRole = highestUserRole != null ? highestUserRole : getHighestUserRole();
    return docs.stream()
        .map(d -> {
            if (d.isVersioned() && !d.isPublic() && (userRole == USER || userRole == READER)) {
              return d.getLastPublicVersion();
            }
            return d;
          })
        .filter(d -> {
          try {
            validation.validateUserAuthorizationOnAttachment(getHttpServletRequest(), getUser(), d);
          } catch (WebApplicationException e) {
            if (e.getResponse().getStatus() != Response.Status.FORBIDDEN.getStatusCode()) {
              throw e;
            }
            return false;
          }
          return true;
        })
        .map(d -> {
          final SimpleDocumentEntity entity = SimpleDocumentEntity.fromAttachment(d);
          performViewIndicator(d, entity);
          performWidthAndHeight(d, entity);
          return entity;
        })
        .collect(Collectors.toList());
  }

  private void performViewIndicator(final SimpleDocument d, final SimpleDocumentEntity entity) {
    if (viewIndicators) {
      final File file = new File(d.getAttachmentPath());
      entity.prewiewable(PreviewService.get().isPreviewable(file))
          .viewable(ViewService.get().isViewable(file))
          .displayAsContent(d.isDisplayableAsContent());
    }
  }

  private void performWidthAndHeight(final SimpleDocument d, final SimpleDocumentEntity entity) {
    if (viewWidthAndHeight && d.isContentImage()) {
      try {
        final File file = new File(d.getAttachmentPath());
        final String[] widthAndHeight = ImageTool.get()
            .getImageInfo(file, WIDTH_IN_PIXEL, HEIGHT_IN_PIXEL);
        if (widthAndHeight.length == 2) {
          entity.widthInPixelOf(Integer.parseInt(widthAndHeight[0]))
                .heightInPixelOf(Integer.parseInt(widthAndHeight[1]));
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }
    }
  }

  @Override
  public void validateUserAuthorization(final UserPrivilegeValidation validation) {
    // authorization verifications MUST be done directly into each signatures
  }
}