/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.webapi.attachment;

import org.silverpeas.core.web.attachment.SimpleDocumentUploadData;
import org.silverpeas.core.web.attachment.WebDavTokenProducer;
import org.silverpeas.core.webapi.base.annotation.Authorized;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.notification.user.UserSubscriptionNotificationSendingHandler;
import org.silverpeas.core.webapi.base.UserPrivilegeValidation;
import org.apache.commons.io.FileUtils;
import org.silverpeas.core.contribution.attachment.ActifyDocumentProcessor;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.WebdavServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.model.UnlockContext;
import org.silverpeas.core.contribution.attachment.model.UnlockOption;
import org.silverpeas.core.importexport.versioning.DocumentVersion;
import org.silverpeas.core.web.http.RequestParameterDecoder;
import org.silverpeas.core.webapi.base.UserPrivilegeValidator;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.i18n.I18NHelper;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.silverpeas.core.i18n.I18NHelper.defaultLanguage;
import static org.silverpeas.core.web.util.IFrameAjaxTransportUtil.*;

@Service
@RequestScoped
@Path("documents/{componentId}/document/{id}")
@Authorized
public class SimpleDocumentResource extends AbstractSimpleDocumentResource {

  @PathParam("id")
  private String simpleDocumentId;

  public String getSimpleDocumentId() {
    return simpleDocumentId;
  }

  /**
   * Returns the specified document in the specified lang.
   *
   * @param lang the wanted language.
   * @return the specified document in the specified lang.
   */
  @GET
  @Path("{lang}")
  @Produces(MediaType.APPLICATION_JSON)
  public SimpleDocumentEntity getDocument(final @PathParam("lang") String lang) {
    SimpleDocument attachment = getSimpleDocument(lang);
    URI attachmentUri = getUriInfo().getRequestUriBuilder().path("document").path(attachment.
        getLanguage()).build();
    return SimpleDocumentEntity.fromAttachment(attachment).withURI(attachmentUri);
  }

  /**
   * Deletes the the specified document.
   */
  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public void deleteDocument() {
    UserSubscriptionNotificationSendingHandler.verifyRequest(getHttpRequest());
    SimpleDocument document = getSimpleDocument(null);
    AttachmentServiceProvider.getAttachmentService().deleteAttachment(document);
  }

  /**
   * Deletes the the specified document.
   *
   * @param lang the lang of the content to be deleted.
   */
  @DELETE
  @Path("content/{lang}")
  @Produces(MediaType.APPLICATION_JSON)
  public void deleteContent(final @PathParam("lang") String lang) {
    UserSubscriptionNotificationSendingHandler.verifyRequest(getHttpRequest());
    SimpleDocument document = getSimpleDocument(lang);
    AttachmentServiceProvider.getAttachmentService().removeContent(document, lang, false);
  }

  /**
   * Updates the document identified by the requested URI.
   *
   * A {@link SimpleDocumentUploadData} is extracted from request parameters.
   *
   * @return an HTTP response embodied an entity in a format expected by the client (that is
   * identified by the <code>xRequestedWith</code> parameter).
   * @throws IOException if an error occurs while updating the document.
   */
  @POST
  @Path("{filename}")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response updateDocument(final @PathParam("filename") String filename) throws IOException {
    SimpleDocumentUploadData uploadData =
        RequestParameterDecoder.decode(getHttpRequest(), SimpleDocumentUploadData.class);

    try {

      // Update the attachment
      SimpleDocumentEntity entity = updateSimpleDocument(uploadData, filename);

      if (AJAX_IFRAME_TRANSPORT.equals(uploadData.getXRequestedWith())) {

        // In case of file upload performed by Ajax IFrame transport way,
        // the expected response type is text/html
        // (when FormData API doesn't exist on client side)
        return Response.ok().type(MediaType.TEXT_HTML_TYPE)
            .entity(packObjectToJSonDataWithHtmlContainer(entity)).build();
      } else {

        // Otherwise JSON response type is expected
        return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(entity).build();
      }

    } catch (WebApplicationException wae) {
      if (AJAX_IFRAME_TRANSPORT.equals(uploadData.getXRequestedWith()) &&
          wae.getResponse().getStatus() == Response.Status.PRECONDITION_FAILED.getStatusCode()) {

        // In case of file upload performed by Ajax IFrame transport way,
        // the exception must also be returned into a text/html response.
        wae = createWebApplicationExceptionWithJSonErrorInHtmlContainer(wae);
      }
      throw wae;
    }
  }

  protected SimpleDocumentEntity updateSimpleDocument(SimpleDocumentUploadData uploadData,
      String filename) throws IOException {
    try {

      UserSubscriptionNotificationSendingHandler.verifyRequest(getHttpRequest());

      SimpleDocument document = getSimpleDocument(uploadData.getLanguage());
      boolean isPublic = false;
      if (uploadData.getVersionType() != null) {
        isPublic = uploadData.getVersionType() == DocumentVersion.TYPE_PUBLIC_VERSION;
        document.setPublicDocument(isPublic);
      }
      document.setUpdatedBy(getUserDetail().getId());
      document.setLanguage(uploadData.getLanguage());
      document.setTitle(uploadData.getTitle());
      document.setDescription(uploadData.getDescription());
      document.setComment(uploadData.getComment());
      String uploadedFilename = filename;
      if (StringUtil.isNotDefined(filename)) {
        uploadedFilename = uploadData.getRequestFile().getName();
      }
      boolean isWebdav = false;
      InputStream uploadedInputStream;
      if (uploadData.getRequestFile() != null &&
          (uploadedInputStream = uploadData.getRequestFile().getInputStream()) != null &&
          StringUtil.isDefined(uploadedFilename) && !"no_file".equalsIgnoreCase(uploadedFilename)) {
        File tempFile = File.createTempFile("silverpeas_", uploadedFilename);
        FileUtils.copyInputStreamToFile(uploadedInputStream, tempFile);
        document.setFilename(uploadedFilename);
        document.setContentType(FileUtil.getMimeType(tempFile.getPath()));

        //check the file
        checkUploadedFile(tempFile);

        document.setSize(tempFile.length());
        InputStream content = new BufferedInputStream(new FileInputStream(tempFile));
        if (!StringUtil.isDefined(document.getEditedBy())) {
          document.edit(getUserDetail().getId());
        }

        AttachmentServiceProvider.getAttachmentService().updateAttachment(document, content, true,
            true);
        content.close();
        FileUtils.deleteQuietly(tempFile);
        // in the case the document is a CAD one, process it for Actify
        ActifyDocumentProcessor.getProcessor().process(document);
      } else {
        isWebdav = document.isOpenOfficeCompatible() && document.isReadOnly();
        if (document.isVersioned()) {
          isWebdav = document.isOpenOfficeCompatible() && document.isReadOnly();
          File content = new File(document.getAttachmentPath());
          AttachmentServiceProvider.getAttachmentService()
              .lock(document.getId(), getUserDetail().getId(), document.getLanguage());
          AttachmentServiceProvider.getAttachmentService()
              .updateAttachment(document, content, true, true);
        } else {
          if (isWebdav) {
            WebdavServiceProvider.getWebdavService().updateDocumentContent(document);
          }
          AttachmentServiceProvider.getAttachmentService().updateAttachment(document, true, true);
        }
      }
      UnlockContext unlockContext =
          new UnlockContext(document.getId(), getUserDetail().getId(), uploadData.getLanguage(),
              uploadData.getComment());
      if (isWebdav) {
        unlockContext.addOption(UnlockOption.WEBDAV);
      } else {
        unlockContext.addOption(UnlockOption.UPLOAD);
      }
      if (!isPublic) {
        unlockContext.addOption(UnlockOption.PRIVATE_VERSION);
      }
      AttachmentServiceProvider.getAttachmentService().unlock(unlockContext);
      if (isWebdav) {
        WebDavTokenProducer.deleteToken(getUserDetail(), document.getId());
      }
      document = getSimpleDocument(uploadData.getLanguage());
      URI attachmentUri = getUriInfo().getRequestUriBuilder().path("document").path(document.
          getLanguage()).build();
      return SimpleDocumentEntity.fromAttachment(document).withURI(attachmentUri);
    } catch (RuntimeException re) {
      performRuntimeException(re);
    }
    throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
  }

  /**
   * Returns all the existing translation of a SimpleDocument.
   *
   * @return all the existing translation of a SimpleDocument.
   */
  @GET
  @Path("translations")
  @Produces(MediaType.APPLICATION_JSON)
  public SimpleDocumentEntity[] getDocumentTanslations() {
    List<SimpleDocumentEntity> result = new ArrayList<>(I18NHelper.getNumberOfLanguages());
    for (String lang : I18NHelper.getAllSupportedLanguages()) {
      SimpleDocument attachment = getSimpleDocument(lang);
      if (lang.equals(attachment.getLanguage())) {
        URI attachmentUri = getUriInfo().getRequestUriBuilder().path("document").path(lang).build();
        result.add(SimpleDocumentEntity.fromAttachment(attachment).withURI(attachmentUri));
      }
    }
    return result.toArray(new SimpleDocumentEntity[result.size()]);
  }

  /**
   * Validates the authorization of the user to request this web service. For doing, the user must
   * have the rights to access the component instance that manages this web resource. The validation
   * is actually delegated to the validation service by passing it the required information.
   *
   * This method should be invoked for web service requiring an authorized access. For doing, the
   * authentication of the user must be first valdiated. Otherwise, the annotation Authorized can be
   * also used instead at class level for both authentication and authorization.
   *
   * @see UserPrivilegeValidator
   * @param validation the validation instance to use.
   * @throws WebApplicationException if the rights of the user are not enough to access this web
   * resource.
   */
  @Override
  public void validateUserAuthorization(final UserPrivilegeValidation validation) throws
      WebApplicationException {
    validation.validateUserAuthorizationOnAttachment(getHttpServletRequest(), getUserDetail(),
        getSimpleDocument(null));
  }

  /**
   * Returns the content of the specified document in the specified language.
   *
   * @param language the language of the document's content to get.
   * @return the content of the specified document in the specified language.
   */
  @GET
  @Path("content/{lang}")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response getFileContent(@PathParam("lang") final String language) {
    SimpleDocument document = getSimpleDocument(language);
    StreamingOutput stream = new StreamingOutput() {
      @Override
      public void write(OutputStream output) throws WebApplicationException {
        try {
          AttachmentServiceProvider.getAttachmentService().getBinaryContent(output,
              new SimpleDocumentPK(getSimpleDocumentId()), language);
        } catch (Exception e) {
          throw new WebApplicationException(e);
        }
      }
    };
    return Response.ok(stream).type(document.getContentType()).header(HttpHeaders.CONTENT_LENGTH,
        document.getSize()).header("content-disposition", "attachment;filename=" + document.
            getFilename()).build();
  }

  /**
   * Locks the specified document for exclusive edition.
   *
   * @return JSON status to true if the document was locked successfully - JSON status to false
   * otherwise..
   */
  @PUT
  @Path("lock/{lang}")
  @Produces(MediaType.APPLICATION_JSON)
  public String lock(@PathParam("lang") final String language) {
    boolean result = AttachmentServiceProvider.getAttachmentService().lock(getSimpleDocumentId(),
        getUserDetail().getId(), language);
    return MessageFormat.format("'{'\"status\":{0}}", result);
  }

  /**
   * @param document
   * @return
   */
  private List<SimpleDocument> getListDocuments(SimpleDocument document) {
    List<SimpleDocument> docs =
        AttachmentServiceProvider.getAttachmentService().listDocumentsByForeignKeyAndType(
            new ForeignPK(document.getForeignId(), getComponentId()), document.getDocumentType(), defaultLanguage);
    return docs;
  }

  /**
   * @param docs
   * @return
   */
  private String reorderDocuments(List<SimpleDocument> docs) {
    AttachmentServiceProvider.getAttachmentService().reorderDocuments(docs);
    return MessageFormat.format("'{'\"status\":{0}}", true);
  }

  /**
   * Moves the specified document up in the list.
   *
   * @return JSON status to true if the document was locked successfully - JSON status to false
   * otherwise..
   */
  @PUT
  @Path("moveUp")
  @Produces(MediaType.APPLICATION_JSON)
  public String moveSimpleDocumentUp() {
    SimpleDocument document = getSimpleDocument(defaultLanguage);
    List<SimpleDocument> docs = getListDocuments(document);
    int position = docs.indexOf(document);
    Collections.swap(docs, position, position - 1);
    String message = reorderDocuments(docs);
    return message;
  }

  /**
   * Moves the specified document down in the list.
   *
   * @return JSON status to true if the document was locked successfully - JSON status to false
   * otherwise..
   */
  @PUT
  @Path("moveDown")
  @Produces(MediaType.APPLICATION_JSON)
  public String moveSimpleDocumentDown() {
    SimpleDocument document = getSimpleDocument(defaultLanguage);
    List<SimpleDocument> docs = getListDocuments(document);
    int position = docs.indexOf(document);
    Collections.swap(docs, position, position + 1);
    String message = reorderDocuments(docs);
    return message;
  }

  /**
   * Unlocks the specified document for exclusive edition.
   *
   * @param force if the unlocking has to be forced.
   * @param webdav if the unlock is performed while a WebDAV access.
   * @param privateVersion if the document is a private version.
   * @param comment a comment about the unlock.
   * @return JSON status to true if the document was locked successfully - JSON status to false
   * otherwise..
   */
  @POST
  @Path("unlock")
  @Produces(MediaType.APPLICATION_JSON)
  public String unlockDocument(@FormParam("force") final boolean force,
      @FormParam("webdav") final boolean webdav, @FormParam("private") final boolean privateVersion,
      @FormParam("comment") final String comment) {
    UserSubscriptionNotificationSendingHandler.verifyRequest(getHttpRequest());
    SimpleDocument document = getSimpleDocument(defaultLanguage);
    UnlockContext unlockContext = new UnlockContext(getSimpleDocumentId(), getUserDetail().getId(),
        defaultLanguage, comment);
    if (force) {
      unlockContext.addOption(UnlockOption.FORCE);
    }
    if (webdav) {
      unlockContext.addOption(UnlockOption.WEBDAV);
    }
    if (privateVersion) {
      unlockContext.addOption(UnlockOption.PRIVATE_VERSION);
    }
    boolean result = AttachmentServiceProvider.getAttachmentService().unlock(unlockContext);
    if (result) {
      WebDavTokenProducer.deleteToken(getUserDetail(), document.getId());
    }
    return MessageFormat.format("'{'\"status\":{0}, \"id\":{1,number,#}, \"attachmentId\":\"{2}\"}",
        result, document.getOldSilverpeasId(), document.getId());
  }

  /**
   * Changes the document version state.
   *
   * @param comment comment about the version state switching.
   * @param version the new version state.
   * @return JSON status to true if the document was locked successfully - JSON status to false
   * otherwise..
   */
  @PUT
  @Path("switchState")
  @Produces(MediaType.APPLICATION_JSON)
  public String switchDocumentVersionState(@FormParam("switch-version-comment") final String comment,
      @FormParam("switch-version") final String version) {
    boolean useMajor = "lastMajor".equalsIgnoreCase(version);
    SimpleDocument document = getSimpleDocument(defaultLanguage);
    SimpleDocumentPK pk = new SimpleDocumentPK(getSimpleDocumentId());
    if (document.isVersioned() && useMajor) {
      final SimpleDocument lastPublicVersion = document.getLastPublicVersion();
      if (lastPublicVersion == null) {
        throw new WebApplicationException(Status.NOT_FOUND);
      }
      pk = lastPublicVersion.getPk();
    }
    pk = AttachmentServiceProvider.getAttachmentService().changeVersionState(pk, comment);
    document = AttachmentServiceProvider.getAttachmentService().searchDocumentById(pk,
        defaultLanguage);
    return MessageFormat.format("'{'\"status\":{0}, \"id\":{1,number,#}, \"attachmentId\":\"{2}\"}",
        true, document.getOldSilverpeasId(), document.getId());
  }

  /**
   * Forbid or allow the download of the document for readers.
   * @return JSON download state for readers. allowedDownloadForReaders = true or false.
   */
  @POST
  @Path("switchDownloadAllowedForReaders")
  @Produces(MediaType.APPLICATION_JSON)
  public String switchDownloadAllowedForReaders(@FormParam("allowed") final boolean allowed) {

    // Performing the request
    SimpleDocument document = getSimpleDocument(null);
    AttachmentServiceProvider.getAttachmentService()
        .switchAllowingDownloadForReaders(document.getPk(), allowed);

    // JSON Response.
    return MessageFormat.format(
        "'{'\"allowedDownloadForReaders\":{0}, \"id\":{1,number,#}, \"attachmentId\":\"{2}\"}",
        allowed, document.getOldSilverpeasId(), document.getId());
  }

  /**
   * Return the current document
   * @param lang
   * @return SimpleDocument
   */
  private SimpleDocument getSimpleDocument(String lang) {
    String language = (lang == null ? defaultLanguage : lang);
    SimpleDocument attachment = AttachmentServiceProvider.getAttachmentService().
        searchDocumentById(new SimpleDocumentPK(getSimpleDocumentId()), language);
    if (attachment == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    return attachment;
  }
}
