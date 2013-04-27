/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
import org.codehaus.jackson.map.ObjectMapper;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.attachment.model.UnlockContext;
import org.silverpeas.attachment.model.UnlockOption;
import org.silverpeas.importExport.versioning.DocumentVersion;

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
   * Return the specified document in the specified lang.
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
   * Delete the the specified document.
   */
  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public void deleteDocument() {
    SimpleDocument document = getSimpleDocument(null);
    AttachmentServiceFactory.getAttachmentService().deleteAttachment(document);
  }

  /**
   * Delete the the specified document.
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
   * Update the the specified document.
   *
   * @param uploadedInputStream
   * @param fileDetail
   * @param lang
   * @param title
   * @param description
   * @param versionType
   * @param comment
   * @return
   * @throws IOException
   */
  @POST
  @Path("{filename}")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_XHTML_XML)
  public String updateDocumentForInternetExplorer(
      final @FormDataParam("file_upload") InputStream uploadedInputStream,
      final @FormDataParam("file_upload") FormDataContentDisposition fileDetail,
      final @FormDataParam("fileLang") String lang, final @FormDataParam("fileTitle") String title,
      final @FormDataParam("fileDescription") String description,
      final @FormDataParam("versionType") String versionType,
      final @FormDataParam("commentMessage") String comment,
      final @PathParam("filename") String filename) throws IOException {
    SimpleDocumentEntity entity = updateSimpleDocument(uploadedInputStream, fileDetail, filename,
        lang, title, description, versionType, comment);
    String result = null;
    if (entity != null) {
      ObjectMapper mapper = new ObjectMapper();
      result = mapper.writeValueAsString(entity);
    }
    return result;
  }

  /**
   * Update the the specified document.
   *
   * @param uploadedInputStream
   * @param fileDetail
   * @param lang
   * @param title
   * @param description
   * @param versionType
   * @param comment
   * @return
   * @throws IOException
   */
  @POST
  @Path("{filename}")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces({MediaType.APPLICATION_JSON})
  public SimpleDocumentEntity updateDocument(
      final @FormDataParam("file_upload") InputStream uploadedInputStream,
      final @FormDataParam("file_upload") FormDataContentDisposition fileDetail,
      final @FormDataParam("fileLang") String lang, final @FormDataParam("fileTitle") String title,
      final @FormDataParam("fileDescription") String description,
      final @FormDataParam("versionType") String versionType,
      final @FormDataParam("commentMessage") String comment,
      final @PathParam("filename") String filename) throws IOException {
    return updateSimpleDocument(uploadedInputStream, fileDetail, filename, lang, title, description,
        versionType, comment);
  }

  /**
   * Update the the specified document.
   *
   * @param uploadedInputStream
   * @param fileDetail
   * @param lang
   * @param title
   * @param description
   * @param versionType
   * @param comment
   * @return
   * @throws IOException
   */
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
    if (uploadedInputStream != null && fileDetail != null && StringUtil.isDefined(uploadedFilename)
        && !"no_file".equalsIgnoreCase(uploadedFilename)) {
      document.setFilename(uploadedFilename);
      document.setContentType(FileUtil.getMimeType(uploadedFilename));
      File tempFile = File.createTempFile("silverpeas_", uploadedFilename);
      FileUtils.copyInputStreamToFile(uploadedInputStream, tempFile);
      document.setSize(tempFile.length());
      InputStream content = new BufferedInputStream(new FileInputStream(tempFile));
      if (!StringUtil.isDefined(document.getEditedBy())) {
        document.edit(getUserDetail().getId());
      }
      AttachmentServiceFactory.getAttachmentService().
          updateAttachment(document, content, true, true);
      content.close();
      FileUtils.deleteQuietly(tempFile);
    } else {
      if (document.isVersioned()) {
        File content = new File(document.getAttachmentPath());
        AttachmentServiceFactory.getAttachmentService().lock(document.getId(), getUserDetail()
            .getId(), document.getLanguage());
        AttachmentServiceFactory.getAttachmentService().updateAttachment(document, content, true,
            true);
      } else {
        AttachmentServiceFactory.getAttachmentService().updateAttachment(document, true, true);
      }
    }
    UnlockContext unlockContext = new UnlockContext(document.getId(), getUserDetail().getId(),
        lang, comment);
    unlockContext.addOption(UnlockOption.UPLOAD);
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
   * Return the content of the specified document in the specified language.
   *
   * @param language
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
      public void write(OutputStream output) throws IOException, WebApplicationException {
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
   * Lock the specified document for exclusive edition.
   *
   * @return JSON status to true if the document was locked successfully - JSON status to false
   * otherwise..
   */
  @PUT
  @Path("lock")
  @Produces(MediaType.APPLICATION_JSON)
  public String lock() {
    SimpleDocument document = AttachmentServiceFactory.getAttachmentService().
        searchDocumentById(new SimpleDocumentPK(getSimpleDocumentId()), I18NHelper.defaultLanguage);
    if (document == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    boolean result = AttachmentServiceFactory.getAttachmentService().lock(getSimpleDocumentId(),
        getUserDetail().getId(), I18NHelper.defaultLanguage);
    return MessageFormat.format("'{'\"status\":{0}}", result);
  }

  /**
   * Move the specified document up in the list.
   *
   * @param language
   * @return JSON status to true if the document was locked successfully - JSON status to false
   * otherwise..
   */
  @PUT
  @Path("moveUp")
  @Produces(MediaType.APPLICATION_JSON)
  public String moveSimpleDocumentUp() {
    SimpleDocument document = AttachmentServiceFactory.getAttachmentService().
        searchDocumentById(new SimpleDocumentPK(getSimpleDocumentId()), I18NHelper.defaultLanguage);
    if (document == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    List<SimpleDocument> docs = AttachmentServiceFactory.getAttachmentService()
        .listDocumentsByForeignKey(
        new ForeignPK(document.getForeignId(), componentId), I18NHelper.defaultLanguage);
    int position = docs.indexOf(document);
    Collections.swap(docs, position, position - 1);
    AttachmentServiceFactory.getAttachmentService().reorderDocuments(docs);
    return MessageFormat.format("'{'\"status\":{0}}", true);
  }

  /**
   * Move the specified document down in the list.
   *
   * @param language
   * @return JSON status to true if the document was locked successfully - JSON status to false
   * otherwise..
   */
  @PUT
  @Path("moveDown")
  @Produces(MediaType.APPLICATION_JSON)
  public String moveSimpleDocumentDown() {
    SimpleDocument document = AttachmentServiceFactory.getAttachmentService().
        searchDocumentById(new SimpleDocumentPK(getSimpleDocumentId()), I18NHelper.defaultLanguage);
    if (document == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    List<SimpleDocument> docs = AttachmentServiceFactory.getAttachmentService()
        .listDocumentsByForeignKey(
        new ForeignPK(document.getForeignId(), componentId), I18NHelper.defaultLanguage);
    int position = docs.indexOf(document);
    Collections.swap(docs, position, position + 1);
    AttachmentServiceFactory.getAttachmentService().reorderDocuments(docs);
    return MessageFormat.format("'{'\"status\":{0}}", true);
  }

  /**
   * Unlock the specified document for exclusive edition.
   *
   * @param force
   * @param webdav
   * @param privateVersion
   * @param comment
   * @return XML status to true if the document was locked successfully - XML status to false
   * otherwise..
   */
  @POST
  @Path("unlock")
  @Produces(MediaType.APPLICATION_XHTML_XML)
  public String unlockDocumentForInternetExplorer(@FormParam("force") final boolean force,
      @FormParam("webdav") final boolean webdav, @FormParam("private") final boolean privateVersion,
      @FormParam("comment") final String comment) {
    SimpleDocument document = AttachmentServiceFactory.getAttachmentService().
        searchDocumentById(new SimpleDocumentPK(getSimpleDocumentId()), I18NHelper.defaultLanguage);
    boolean result = unlock(document, force, webdav, privateVersion, comment);
    return MessageFormat.format("<result><status>{0}</status><id>{1}</id>"
        + "<attachmentId>{2}</attachmentId></result>", result, document.getOldSilverpeasId(),
        document.getId());
  }

  protected boolean unlock(final SimpleDocument document, final boolean force, final boolean webdav,
      final boolean privateVersion, final String comment) {
    if (document == null) {
      throw new WebApplicationException(Status.NOT_FOUND);
    }
    UnlockContext unlockContext = new UnlockContext(getSimpleDocumentId(), getUserDetail().getId(),
        I18NHelper.defaultLanguage, comment);
    if (force) {
      unlockContext.addOption(UnlockOption.FORCE);
    }
    if (webdav) {
      unlockContext.addOption(UnlockOption.WEBDAV);
    }
    if (privateVersion) {
      unlockContext.addOption(UnlockOption.PRIVATE_VERSION);
    }
    return AttachmentServiceFactory.getAttachmentService().unlock(unlockContext);
  }

  /**
   * Unlock the specified document for exclusive edition.
   *
   * @param force
   * @param webdav
   * @param privateVersion
   * @param comment
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
        searchDocumentById(new SimpleDocumentPK(getSimpleDocumentId()), I18NHelper.defaultLanguage);
    boolean result = unlock(document, force, webdav, privateVersion, comment);
    return MessageFormat.format("'{'\"status\":{0}, \"id\":{1}, \"attachmentId\":\"{2}\"}", result,
        document.getOldSilverpeasId(), document.getId());
  }

  SimpleDocument getSimpleDocument(String lang) {
    return AttachmentServiceFactory.getAttachmentService().
        searchDocumentById(new SimpleDocumentPK(getSimpleDocumentId()), lang);
  }
}
