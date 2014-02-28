/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.attachment.web;

import com.silverpeas.annotation.Authorized;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.web.RESTWebService;
import com.silverpeas.web.UserPriviledgeValidation;
import com.stratelia.webactiv.util.ResourceLocator;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
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
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import org.apache.commons.io.FileUtils;
import org.silverpeas.attachment.ActifyDocumentProcessor;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.WebdavServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.attachment.model.UnlockContext;
import org.silverpeas.attachment.model.UnlockOption;
import org.silverpeas.importExport.versioning.DocumentVersion;

import static org.silverpeas.web.util.IFrameAjaxTransportUtil.*;

import static com.silverpeas.util.i18n.I18NHelper.defaultLanguage;

@Service
@RequestScoped
@Path("documents/{componentId}/document/{id}")
@Authorized
public class SimpleDocumentResource extends RESTWebService {

  @PathParam("componentId")
  private String componentId;
  @PathParam("id")
  private String simpleDocumentId;

  @Override
  public String getComponentId() {
    return componentId;
  }

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
    if (attachment == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
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
    SimpleDocument document = getSimpleDocument(null);
    AttachmentServiceFactory.getAttachmentService().deleteAttachment(document);
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
    SimpleDocument document = getSimpleDocument(lang);
    AttachmentServiceFactory.getAttachmentService().removeContent(document, lang, false);
  }

  /**
   * Updates the document identified by the requested URI.
   *
   * @param uploadedInputStream the input stream from which the content of the file can be read.
   * @param fileDetail detail about the uploaded file like the filename for example.
   * @param xRequestedWith a parameter indicating from which the upload was performed. It is valued
   * with the identifier of the HTML or javascript component at the origin of the uploading.
   * According to his value, the expected response can be different.
   * @param lang the two-characters code of the language in which the document's content is written.
   * @param title the title of the document as indicated by the user.
   * @param description a short description of the document's content as indicated by the user.
   * @param versionType the scope of the version (public or private) in the case of a versioned
   * document.
   * @param comment a comment about the upload.
   * @return an HTTP response embodied an entity in a format expected by the client (that is
   * identified by the <code>xRequestedWith</code> parameter).
   * @throws IOException if an error occurs while updating the document.
   */
  @POST
  @Path("{filename}")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response updateDocument(
      final @FormDataParam("file_upload") InputStream uploadedInputStream,
      final @FormDataParam("file_upload") FormDataContentDisposition fileDetail,
      final @FormDataParam(X_REQUESTED_WITH) String xRequestedWith,
      final @FormDataParam("fileLang") String lang, final @FormDataParam("fileTitle") String title,
      final @FormDataParam("fileDescription") String description,
      final @FormDataParam("versionType") String versionType,
      final @FormDataParam("commentMessage") String comment,
      final @PathParam("filename") String filename) throws IOException {

    // Update the attachment
    SimpleDocumentEntity entity = updateSimpleDocument(uploadedInputStream, fileDetail, filename,
        lang, title, description, versionType, comment);

    if (AJAX_IFRAME_TRANSPORT.equals(xRequestedWith)) {

      // In case of file upload performed by Ajax IFrame transport way,
      // the expected response type is text/html
      // (when FormData API doesn't exist on client side)
      return Response.ok().type(MediaType.TEXT_HTML_TYPE)
          .entity(packObjectToJSonDataWithHtmlContainer(entity)).build();
    } else {

      // Otherwise JSON response type is expected
      return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(entity).build();
    }
  }

  protected SimpleDocumentEntity updateSimpleDocument(InputStream uploadedInputStream,
      FormDataContentDisposition fileDetail, String filename, String lang, String title,
      String description, String versionType, String comment) throws IOException {
    SimpleDocument document = getSimpleDocument(lang);
    boolean isPublic = false;
    if (StringUtil.isDefined(versionType) && StringUtil.isInteger(versionType)) {
      isPublic = Integer.parseInt(versionType) == DocumentVersion.TYPE_PUBLIC_VERSION;
      document.setPublicDocument(isPublic);
    }
    document.setUpdatedBy(getUserDetail().getId());
    document.setLanguage(lang);
    document.setTitle(title);
    document.setDescription(description);
    document.setComment(comment);
    String uploadedFilename = filename;
    if (StringUtil.isNotDefined(filename)) {
      uploadedFilename = fileDetail.getFileName();
    }
    boolean isWebdav = false;
    if (uploadedInputStream != null && fileDetail != null && StringUtil.isDefined(uploadedFilename)
        && !"no_file".equalsIgnoreCase(uploadedFilename)) {
      document.setFilename(uploadedFilename);
      document.setContentType(FileUtil.getMimeType(uploadedFilename));
      File tempFile = File.createTempFile("silverpeas_", uploadedFilename);
      FileUtils.copyInputStreamToFile(uploadedInputStream, tempFile);

      //check the file size
      ResourceLocator uploadSettings = new ResourceLocator(
          "org.silverpeas.util.uploads.uploadSettings", "");
      long maximumFileSize = uploadSettings.getLong("MaximumFileSize", 10485760);
      long fileSize = tempFile.length();
      if (fileSize > maximumFileSize) {
        FileUtils.deleteQuietly(tempFile);
        throw new WebApplicationException(Response.Status.PRECONDITION_FAILED);
      }

      document.setSize(fileSize);
      InputStream content = new BufferedInputStream(new FileInputStream(tempFile));
      if (!StringUtil.isDefined(document.getEditedBy())) {
        document.edit(getUserDetail().getId());
      }
      AttachmentServiceFactory.getAttachmentService().updateAttachment(document, content, true,
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
        AttachmentServiceFactory.getAttachmentService().lock(document.getId(), getUserDetail()
            .getId(), document.getLanguage());
        if (!StringUtil.isDefined(document.getEditedBy())) {
          document.edit(getUserDetail().getId());
        }
        AttachmentServiceFactory.getAttachmentService().updateAttachment(document, content, true,
            true);
      } else {
        if (isWebdav) {
          WebdavServiceFactory.getWebdavService().getUpdatedDocument(document);
        }
        AttachmentServiceFactory.getAttachmentService().updateAttachment(document, true, true);
      }
    }
    UnlockContext unlockContext = new UnlockContext(document.getId(), getUserDetail().getId(),
        lang, comment);
    if (isWebdav) {
      unlockContext.addOption(UnlockOption.WEBDAV);
    } else {
      unlockContext.addOption(UnlockOption.UPLOAD);
    }
    if (!isPublic) {
      unlockContext.addOption(UnlockOption.PRIVATE_VERSION);
    }
    AttachmentServiceFactory.getAttachmentService().unlock(unlockContext);
    document = getSimpleDocument(lang);
    URI attachmentUri = getUriInfo().getRequestUriBuilder().path("document").path(document.
        getLanguage()).build();
    return SimpleDocumentEntity.fromAttachment(document).withURI(attachmentUri);
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
    List<SimpleDocumentEntity> result = new ArrayList<SimpleDocumentEntity>(I18NHelper.
        getNumberOfLanguages());
    for (String lang : I18NHelper.getAllSupportedLanguages()) {
      SimpleDocument attachment = getSimpleDocument(lang);
      if (attachment == null) {
        throw new WebApplicationException(Status.NOT_FOUND);
      }
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
   * @see UserPriviledgeValidation
   * @param validation the validation instance to use.
   * @throws WebApplicationException if the rights of the user are not enough to access this web
   * resource.
   */
  @Override
  public void validateUserAuthorization(final UserPriviledgeValidation validation) throws
      WebApplicationException {
    super.validateUserAuthorization(validation);
    validation.validateUserAuthorizationOnAttachment(getUserDetail(), getSimpleDocument(null));
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
    SimpleDocument document = AttachmentServiceFactory.getAttachmentService().
        searchDocumentById(new SimpleDocumentPK(getSimpleDocumentId()), language);
    if (document == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    StreamingOutput stream = new StreamingOutput() {
      @Override
      public void write(OutputStream output) throws WebApplicationException {
        try {
          AttachmentServiceFactory.getAttachmentService().getBinaryContent(output,
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
  @Path("lock")
  @Produces(MediaType.APPLICATION_JSON)
  public String lock() {
    SimpleDocument document = AttachmentServiceFactory.getAttachmentService().
        searchDocumentById(new SimpleDocumentPK(getSimpleDocumentId()), defaultLanguage);
    if (document == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    boolean result = AttachmentServiceFactory.getAttachmentService().lock(getSimpleDocumentId(),
        getUserDetail().getId(), I18NHelper.defaultLanguage);
    return MessageFormat.format("'{'\"status\":{0}}", result);
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
    SimpleDocument document = AttachmentServiceFactory.getAttachmentService().searchDocumentById(
        new SimpleDocumentPK(getSimpleDocumentId()), defaultLanguage);
    if (document == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    List<SimpleDocument> docs = AttachmentServiceFactory.getAttachmentService()
        .listDocumentsByForeignKey(new ForeignPK(document.getForeignId(), componentId),
            defaultLanguage);
    int position = docs.indexOf(document);
    Collections.swap(docs, position, position - 1);
    AttachmentServiceFactory.getAttachmentService().reorderDocuments(docs);
    return MessageFormat.format("'{'\"status\":{0}}", true);
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
    SimpleDocument document = AttachmentServiceFactory.getAttachmentService().
        searchDocumentById(new SimpleDocumentPK(getSimpleDocumentId()), defaultLanguage);
    if (document == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    List<SimpleDocument> docs = AttachmentServiceFactory.getAttachmentService()
        .listDocumentsByForeignKey(new ForeignPK(document.getForeignId(), componentId),
            defaultLanguage);
    int position = docs.indexOf(document);
    Collections.swap(docs, position, position + 1);
    AttachmentServiceFactory.getAttachmentService().reorderDocuments(docs);
    return MessageFormat.format("'{'\"status\":{0}}", true);
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
    SimpleDocument document = AttachmentServiceFactory.getAttachmentService().
        searchDocumentById(new SimpleDocumentPK(getSimpleDocumentId()), defaultLanguage);
    if (document == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
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
    boolean result = AttachmentServiceFactory.getAttachmentService().unlock(unlockContext);
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
    SimpleDocument document = AttachmentServiceFactory.getAttachmentService().
        searchDocumentById(new SimpleDocumentPK(getSimpleDocumentId()), defaultLanguage);
    if (document == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    SimpleDocumentPK pk = new SimpleDocumentPK(getSimpleDocumentId());
    if (document.isVersioned() && useMajor) {
      pk = document.getLastPublicVersion().getPk();
    }
    pk = AttachmentServiceFactory.getAttachmentService().changeVersionState(pk, comment);
    document = AttachmentServiceFactory.getAttachmentService().searchDocumentById(pk,
        defaultLanguage);
    return MessageFormat.format("'{'\"status\":{0}, \"id\":{1,number,#}, \"attachmentId\":\"{2}\"}",
        true, document.getOldSilverpeasId(), document.getId());
  }

  SimpleDocument getSimpleDocument(String lang) {
    return AttachmentServiceFactory.getAttachmentService().
        searchDocumentById(new SimpleDocumentPK(getSimpleDocumentId()), lang);
  }
}
