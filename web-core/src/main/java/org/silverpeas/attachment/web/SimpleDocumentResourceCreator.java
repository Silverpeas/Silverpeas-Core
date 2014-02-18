/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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
package org.silverpeas.attachment.web;

import com.silverpeas.annotation.Authorized;
import com.silverpeas.annotation.RequestScoped;
import com.silverpeas.annotation.Service;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.MetaData;
import com.silverpeas.util.MetadataExtractor;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.web.RESTWebService;
import com.stratelia.webactiv.util.ResourceLocator;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;
import org.silverpeas.attachment.ActifyDocumentProcessor;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.DocumentType;
import org.silverpeas.attachment.model.HistorisedDocument;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.attachment.model.UnlockContext;
import org.silverpeas.attachment.model.UnlockOption;
import org.silverpeas.importExport.versioning.DocumentVersion;

import static org.silverpeas.web.util.IFrameAjaxTransportUtil.*;

/**
 *
 * @author ehugonnet
 */
@Service
@RequestScoped
@Path("documents/{componentId}/document/create")
@Authorized
public class SimpleDocumentResourceCreator extends RESTWebService {

  @PathParam("componentId")
  private String componentId;

  @Override
  public String getComponentId() {
    return componentId;
  }

  /**
   * Create the document identified by the requested URI and from the content and some additional
   * parameters passed within the request.
   *
   * @param uploadedInputStream the input stream from which the content of the file can be read.
   * @param fileDetail detail about the uploaded file like the filename for example.
   * @param xRequestedWith a parameter indicating from which the upload was performed. It is valued
   * with the identifier of the HTML or javascript component at the origin of the uploading.
   * According to his value, the expected response can be different.
   * @param language the two-characters code of the language in which the document's content is
   * written.
   * @param fileTitle the title of the document as indicated by the user.
   * @param description a short description of the document's content as indicated by the user.
   * @param foreignId the identifier of the resource to which the document belong (a publication for
   * example).
   * @param indexIt flag meaning the indexation or not of the document's content.
   * @param type the scope of the version (public or private) in the case of a versioned document.
   * @param comment a comment about the upload.
   * @param context the context in which the document is created: in the case of a file attachment,
   * in the case of a WYSIWYG edition, ...
   * @return an HTTP response embodied an entity in a format expected by the client (that is
   * identified by the <code>xRequestedWith</code> parameter).
   * @throws IOException if an error occurs while updating the document.
   */
  @POST
  @Path("{filename}")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response createDocument(final @FormDataParam("file_upload") InputStream uploadedInputStream,
      final @FormDataParam("file_upload") FormDataContentDisposition fileDetail,
      final @FormDataParam(X_REQUESTED_WITH) String xRequestedWith,
      final @FormDataParam("fileLang") String language,
      final @FormDataParam("fileTitle") String fileTitle,
      final @FormDataParam("fileDescription") String description,
      final @FormDataParam("foreignId") String foreignId,
      final @FormDataParam("indexIt") String indexIt,
      final @FormDataParam("versionType") String type,
      final @FormDataParam("commentMessage") String comment,
      final @FormDataParam("context") String context,
      final @PathParam("filename") String filename) throws IOException {

    // Create the attachment
    SimpleDocumentEntity entity = createSimpleDocument(uploadedInputStream, fileDetail, filename,
        language, fileTitle, description, foreignId, indexIt, type, comment, context);

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

  protected SimpleDocumentEntity createSimpleDocument(InputStream uploadedInputStream,
      FormDataContentDisposition fileDetail, String filename, String language, String fileTitle,
      String fileDescription,
      String foreignId, String indexIt, String type, String comment, String context) throws
      IOException {
    String uploadedFilename = filename;
    if (StringUtil.isNotDefined(filename)) {
      uploadedFilename = fileDetail.getFileName();
    }
    if (uploadedInputStream != null && fileDetail != null && StringUtil.isDefined(uploadedFilename)) {
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

      String lang = I18NHelper.checkLanguage(language);
      String title = fileTitle;
      String description = fileDescription;
      if (!StringUtil.isDefined(fileTitle)) {
        MetadataExtractor extractor = new MetadataExtractor();
        MetaData metadata = extractor.extractMetadata(tempFile);
        if (StringUtil.isDefined(metadata.getTitle())) {
          title = metadata.getTitle();
        } else {
          title = "";
        }
        if (!StringUtil.isDefined(description) && StringUtil.isDefined(metadata.getSubject())) {
          description = metadata.getSubject();
        }
      }
      if (!StringUtil.isDefined(description)) {
        description = "";
      }

      DocumentType attachmentContext;
      if (!StringUtil.isDefined(context)) {
        attachmentContext = DocumentType.attachment;
      } else {
        attachmentContext = DocumentType.valueOf(context);
      }
      SimpleDocumentPK pk = new SimpleDocumentPK(null, componentId);
      String userId = getUserDetail().getId();
      SimpleDocument document;
      boolean needCreation = true;
      boolean publicDocument = true;
      if (StringUtil.isDefined(type) && StringUtil.isInteger(type)) {
        document = AttachmentServiceFactory.getAttachmentService().findExistingDocument(pk,
            uploadedFilename, new ForeignPK(foreignId, componentId), lang);
        publicDocument = Integer.parseInt(type) == DocumentVersion.TYPE_PUBLIC_VERSION;
        needCreation = document == null;
        if (document == null) {
          document = new HistorisedDocument(pk, foreignId, 0, userId,
              new SimpleAttachment(uploadedFilename, lang, title, description, fileDetail.getSize(),
                  FileUtil.getMimeType(uploadedFilename), userId, new Date(), null));
          document.setDocumentType(attachmentContext);
        }
        document.setPublicDocument(publicDocument);
        document.setComment(comment);
      } else {
        document = new SimpleDocument(pk, foreignId, 0, false, null,
            new SimpleAttachment(uploadedFilename, lang, title, description, fileDetail.getSize(),
                FileUtil.getMimeType(uploadedFilename), userId, new Date(), null));
        document.setDocumentType(attachmentContext);
      }
      document.setLanguage(lang);
      document.setTitle(title);
      document.setDescription(description);
      document.setSize(fileSize);
      InputStream content = new BufferedInputStream(new FileInputStream(tempFile));
      if (needCreation) {
        document = AttachmentServiceFactory.getAttachmentService().createAttachment(document,
            content, StringUtil.getBooleanValue(indexIt), publicDocument);
      } else {
        document.edit(userId);
        AttachmentServiceFactory.getAttachmentService().lock(document.getId(), userId, lang);
        AttachmentServiceFactory.getAttachmentService().updateAttachment(document, content,
            StringUtil.getBooleanValue(indexIt), true);
        UnlockContext unlockContext = new UnlockContext(document.getId(), userId, lang);
        unlockContext.addOption(UnlockOption.UPLOAD);
        if (!publicDocument) {
          unlockContext.addOption(UnlockOption.PRIVATE_VERSION);
        }
        AttachmentServiceFactory.getAttachmentService().unlock(unlockContext);
      }
      content.close();
      FileUtils.deleteQuietly(tempFile);

      // in the case the document is a CAD one, process it for Actify
      ActifyDocumentProcessor.getProcessor().process(document);

      URI attachmentUri = getUriInfo().getRequestUriBuilder().path("document").path(document.
          getLanguage()).build();
      return SimpleDocumentEntity.fromAttachment(document).withURI(attachmentUri);
    }
    return null;
  }
}
