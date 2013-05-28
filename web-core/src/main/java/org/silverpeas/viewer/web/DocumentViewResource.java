package org.silverpeas.viewer.web;

import com.silverpeas.annotation.Authorized;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.web.RESTWebService;
import org.silverpeas.attachment.AttachmentService;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.viewer.DocumentView;
import org.silverpeas.viewer.ViewService;
import org.silverpeas.viewer.exception.PreviewException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.Response.Status;
import java.io.File;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
/**
 * @author Yohann Chastagnier
 */
@Service
@RequestScoped
@Path("view/{componentId}")
@Authorized
public class DocumentViewResource extends RESTWebService {

  @Inject
  @Named("simpleDocumentService")
  private AttachmentService attachmentService;

  @Inject
  private ViewService viewService;
  @PathParam("componentId")
  private String componentId;

  /**
   * Gets the JSON representation of document view information. If it doesn't exist, a 404 HTTP code
   * is returned. If the user isn't authentified, a 401 HTTP code is returned. If a problem occurs
   * when processing the request, a 503 HTTP code is returned.
   *
   * @return the response to the HTTP GET request with the JSON representation of preview
   * information.
   */
  @GET
  @Path("attachment/{id}")
  @Produces(APPLICATION_JSON)
  public DocumentViewEntity getAttachmentView(@PathParam("id") final String id) {
    try {

      // Retrieve attachment data
      final SimpleDocument attachment = attachmentService.searchDocumentById(new SimpleDocumentPK(
          id, getComponentId()), getUserPreferences().getLanguage());

      // Checking availability
      if (attachment == null) {
        throw new PreviewException("ATTACHMENT DOESN'T EXIST");
      }

      // Computing the document view entity
      return asWebEntity(viewService.getDocumentView(attachment.getFilename(), new File(attachment.
          getAttachmentPath())));

    } catch (final PreviewException pe) {
      throw new WebApplicationException(pe, Status.NOT_FOUND);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the JSON representation of document view information. If it doesn't exist, a 404 HTTP code
   * is returned. If the user isn't authentified, a 401 HTTP code is returned. If a problem occurs
   * when processing the request, a 503 HTTP code is returned.
   *
   * @return the response to the HTTP GET request with the JSON representation of document view
   * information.
   */
  @GET
  @Path("version/{id}")
  @Produces(APPLICATION_JSON)
  public DocumentViewEntity getVersionView(@PathParam("id") final String id) {
    try {

      // Retrieve attachment data
      final SimpleDocument attachment = attachmentService.searchDocumentById(new SimpleDocumentPK(
          id, getComponentId()), getUserPreferences().getLanguage());

      // Checking availability
      if (attachment == null) {
        throw new PreviewException("ATTACHMENT DOESN'T EXIST");
      }

      // Computing the document view entity
      return asWebEntity(viewService.getDocumentView(attachment.getFilename(),new File(attachment.
          getAttachmentPath())));

    } catch (final PreviewException pe) {
      throw new WebApplicationException(pe, Status.NOT_FOUND);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Converts the document view into its corresponding web entity.
   *
   * @param documentView the view to convert.
   * @return the corresponding view entity.
   */
  protected DocumentViewEntity asWebEntity(final DocumentView documentView) {
    return DocumentViewEntity.createFrom(documentView, getUserPreferences().getLanguage()).withURI(
        getUriInfo().getRequestUri());
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.web.RESTWebService#getComponentId()
   */
  @Override
  public String getComponentId() {
    return componentId;
  }
}
