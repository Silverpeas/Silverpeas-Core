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
package org.silverpeas.core.webapi.media;

import org.jboss.resteasy.plugins.providers.html.View;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.cache.model.Cache;
import org.silverpeas.core.contribution.attachment.AttachmentException;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.io.file.SilverpeasFile;
import org.silverpeas.core.io.file.SilverpeasFileProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.viewer.model.DocumentView;
import org.silverpeas.core.viewer.service.ViewService;
import org.silverpeas.core.viewer.service.ViewerContext;
import org.silverpeas.core.web.http.FileResponse;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.annotation.Authenticated;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.nio.file.Paths;

import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.silverpeas.core.cache.service.CacheServiceProvider.getApplicationCacheService;
import static org.silverpeas.core.util.StringUtil.isDefined;
import static org.silverpeas.core.util.StringUtil.isNotDefined;

/**
 * A common service to view document with an embed viewer.
 * @author Yohann Chastagnier
 */
@WebService
@Path(EmbedMediaViewerResource.PATH)
@Authenticated
public class EmbedMediaViewerResource extends RESTWebService {

  static final String PATH = "media/viewer/embed";
  private static final String PDF_VIEWER_CACHE_PREFIX = "PdfEmbedMediaViewer_";

  @Inject
  private ViewService viewService;

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }

  /**
   * Gets a view on the content with the embed pdf viewer.
   * @return a descriptor of the renderer to use to view the document.
   */
  @GET
  @Path("pdf")
  public View getPdfEmbedViewer(@QueryParam("documentId") final String documentId,
      @QueryParam("language") final String language) {
    try {
      final SimpleDocument document = getDocument(documentId, language);

      getHttpServletRequest().setAttribute("contentUrl", getUri().getRequestUriBuilder().path("content").build());
      setCommonRequestViewerAttributes(document);

      final String cacheKey = PDF_VIEWER_CACHE_PREFIX + documentId + "@" + language;
      ((Cache) getApplicationCacheService().getCache()).put(cacheKey, true, 10, 0);
      return new View("/media/jsp/pdf/viewer.jsp");
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final AttachmentException ex) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Response.Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the content of the pdf. The player URI MUST have be accessed before accessing this one.
   */
  @GET
  @Path("pdf/content")
  public Response getPdfContent(@QueryParam("documentId") final String documentId,
      @QueryParam("language") final String language) {
    try {
      final String cacheKey = PDF_VIEWER_CACHE_PREFIX + documentId + "@" + language;
      final boolean playerAccessed = getApplicationCacheService().getCache().remove(cacheKey) != null;
      if (!playerAccessed) {
        return Response.seeOther(getUri().getAbsoluteWebResourcePathBuilder()
                                         .path("pdf")
                                         .queryParam("documentId", documentId)
                                         .queryParam("language", language).build())
                                 .build();
      }

      final SimpleDocument document = getDocument(documentId, language);
      final DocumentView documentView = viewService.getDocumentView(ViewerContext.from(document));
      return sendDocument(document, documentView);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final AttachmentException ex) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Response.Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets a view on the content with the embed flowpaper viewer.
   * @return a descriptor of the renderer to use to view the document.
   */
  @GET
  @Path("fp")
  public View getFlowPaperEmbedViewer(@QueryParam("documentId") final String documentId,
      @QueryParam("language") final String language) {
    try {
      final SimpleDocument document = getDocument(documentId, language);

      final DocumentView documentView = viewService.getDocumentView(ViewerContext.from(document));
      final String displayLicenseKey = documentView.getDisplayLicenseKey();
      final String displayViewerPath;
      if (StringUtil.isDefined(displayLicenseKey)) {
        displayViewerPath = UriBuilder.fromPath("/weblib").path("flexpaper").path("flash").build().toString();
      } else {
        displayViewerPath = URLUtil.getApplicationURL() + "/media/jsp/fp/core/flash";
      }

      getHttpServletRequest().setAttribute("contentUrl", getUri().getBaseUriBuilder()
                                                                 .path(PATH)
                                                                 .path("fp/content")
                                                                 .path(documentId)
                                                                 .path(language)
                                                                 .path(documentView.getPhysicalFile().getName())
                                                                 .build());
      setCommonRequestViewerAttributes(document);
      getHttpServletRequest().setAttribute("documentView", documentView);
      getHttpServletRequest().setAttribute("displayViewerPath", displayViewerPath);

      return new View("/media/jsp/fp/viewer.jsp");
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final AttachmentException ex) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Response.Status.SERVICE_UNAVAILABLE);
    }
  }

  /**
   * Gets the content of the swf. The player URI MUST have be accessed before accessing this one.
   */
  @GET
  @Path("fp/content/{documentId}/{language}/{page}")
  public Response getFlowPaperContent(@PathParam("documentId") final String documentId,
      @PathParam("language") final String language,
      @PathParam("page") final String page) {
    try {
      final SimpleDocument document = getDocument(documentId, language);
      final DocumentView documentView = viewService.getDocumentView(ViewerContext.from(document));

      if (documentView.getServerFilePath().toString().endsWith("file.pdf")) {
        return Response.seeOther(getUri().getAbsoluteWebResourcePathBuilder()
                                         .path("pdf")
                                         .queryParam("documentId", documentId)
                                         .queryParam("language", language).build())
                                 .build();
      }
      return sendDocument(document, documentView, page);
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final AttachmentException ex) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Response.Status.SERVICE_UNAVAILABLE);
    }
  }

  @Override
  public String getComponentId() {
    return null;
  }

  private SimpleDocument getDocument(final String documentId, String language) {
    if (isNotDefined(documentId)) {
      throw new WebApplicationException("documentId is missing", Response.Status.BAD_REQUEST);
    }

    final SimpleDocument document = AttachmentService.get()
        .searchDocumentById(new SimpleDocumentPK(documentId), language);
    if (document == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    } else if (!document.canBeAccessedBy(getUser())) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
    return document;
  }

  private void setCommonRequestViewerAttributes(final SimpleDocument document) {
    getHttpServletRequest().setAttribute("downloadEnabled", document.isDownloadAllowedForRolesFrom(getUser()));
    getHttpServletRequest().setAttribute("userLanguage", getUserPreferences().getLanguage());
  }

  private Response sendDocument(final SimpleDocument document, final DocumentView documentView) {
    return sendDocument(document, documentView, null);
  }

  private Response sendDocument(final SimpleDocument document, final DocumentView documentView, final String page) {
    final java.nio.file.Path path = isDefined(page) ?
        Paths.get(documentView.getServerFilePath().getParent().toString(), page) :
        documentView.getServerFilePath();
    final SilverpeasFile file = SilverpeasFileProvider.getFile(path.toString());
    final String filename = isDefined(page) ?
        documentView.getOriginalFileName() :
        getBaseName(documentView.getOriginalFileName()) + ".pdf";
    return FileResponse.fromRest(getHttpRequest(), getHttpServletResponse())
        .forceMimeType(document.getContentType())
        .forceFileName(filename)
        .silverpeasFile(file)
        .build();
  }
}
