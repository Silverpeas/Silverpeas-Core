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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.contribution.ContributionOperationContextPropertyHandler;
import org.silverpeas.core.contribution.attachment.ActifyDocumentProcessor;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.HistorisedDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.model.UnlockContext;
import org.silverpeas.core.contribution.attachment.model.UnlockOption;
import org.silverpeas.core.contribution.attachment.util.AttachmentSettings;
import org.silverpeas.core.documenttemplate.DocumentTemplate;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.importexport.versioning.DocumentVersion;
import org.silverpeas.core.io.media.MetaData;
import org.silverpeas.core.io.media.MetadataExtractor;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.web.attachment.SimpleDocumentUploadData;
import org.silverpeas.core.web.http.RequestFile;
import org.silverpeas.core.web.rs.annotation.Authorized;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.Objects;
import java.util.function.Consumer;

import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;
import static org.silverpeas.core.contribution.attachment.AttachmentServiceProvider.getAttachmentService;
import static org.silverpeas.kernel.util.StringUtil.defaultStringIfNotDefined;
import static org.silverpeas.kernel.util.StringUtil.isDefined;
import static org.silverpeas.core.web.attachment.SimpleDocumentUploadData.decode;
import static org.silverpeas.core.web.util.IFrameAjaxTransportUtil.*;

/**
 * @author ehugonnet
 */
@WebService
@Path(AbstractSimpleDocumentResource.PATH + "/{componentId}/document/create")
@Authorized
public class SimpleDocumentResourceCreator extends AbstractSimpleDocumentResource {

  /**
   * Create the document identified by the requested URI and from the content and some additional
   * parameters passed within the request.
   * <p>
   * A {@link SimpleDocumentUploadData} is extracted from request parameters.
   * @return an HTTP response embodied an entity in a format expected by the client (that is
   * identified by the <code>xRequestedWith</code> parameter).
   * @throws IOException if an error occurs while updating the document.
   */
  @POST
  @Path("{filename}")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  public Response createDocument(final @PathParam("filename") String filename) throws IOException {
    SimpleDocumentUploadData uploadData = decode(getHttpRequest());

    try {

      // Create the attachment
      final String normalizedFileName = StringUtil.normalize(filename);
      final SimpleDocumentEntity entity = createSimpleDocument(uploadData, normalizedFileName);

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
        throw createWebApplicationExceptionWithJSonErrorInHtmlContainer(wae);
      }
      throw wae;
    }
  }

  protected SimpleDocumentEntity createSimpleDocument(SimpleDocumentUploadData uploadData,
      String filename) throws IOException {
    final Context context = initializeContext(uploadData, filename);
    if (!context.contentDataAvailable()) {
      throw new WebApplicationException("Missing content data (file content or physical filename)", BAD_REQUEST);
    }
    try {
      ContributionOperationContextPropertyHandler.parseRequest(getHttpRequest());
      String lang = I18NHelper.checkLanguage(uploadData.getLanguage());
      String title = defaultStringIfNotDefined(uploadData.getTitle());
      String description = defaultStringIfNotDefined(uploadData.getDescription());
      if (!context.isDocumentTemplateContent() &&
          AttachmentSettings.isUseFileMetadataForAttachmentDataEnabled() &&
          !StringUtil.isDefined(title)) {
        MetadataExtractor extractor = MetadataExtractor.get();
        MetaData metadata = extractor.extractMetadata(context.getContentFile());
        title = defaultStringIfNotDefined(metadata.getTitle(), title);
        if (!StringUtil.isDefined(description)) {
          description = defaultStringIfNotDefined(metadata.getSubject(), description);
        }
      }
      final DocumentType attachmentContext = ofNullable(uploadData.getContext())
          .filter(StringUtil::isDefined)
          .map(DocumentType::valueOf)
          .orElse(DocumentType.attachment);
      final SimpleDocumentPK pk = new SimpleDocumentPK(null, getComponentId());
      final String userId = getUser().getId();
      SimpleDocument document;
      boolean needCreation = true;
      boolean publicDocument = true;
      if (uploadData.getVersionType() != null) {
        document = getAttachmentService()
            .findExistingDocument(pk, context.getFilename(),
                new ResourceReference(uploadData.getForeignId(), getComponentId()), lang);
        publicDocument = uploadData.getVersionType() == DocumentVersion.TYPE_PUBLIC_VERSION;
        needCreation = document == null;
        if (needCreation) {
          final SimpleAttachment attachment = SimpleAttachment.builder(lang)
              .setFilename(context.getFilename())
              .setTitle(title)
              .setDescription(description)
              .setSize(context.getContentSize())
              .setContentType(context.getContentType())
              .setCreationData(userId, new Date())
              .build();
          document = new HistorisedDocument(pk, uploadData.getForeignId(), 0, userId, attachment);
          document.setDocumentType(attachmentContext);
        }
        document.setPublicDocument(publicDocument);
        document.setComment(uploadData.getComment());
      } else {
        final SimpleAttachment attachment = SimpleAttachment.builder(lang)
            .setFilename(context.getFilename())
            .setTitle(title)
            .setDescription(description)
            .setSize(context.getContentSize())
            .setContentType(context.getContentType())
            .setCreationData(userId, new Date())
            .build();
        document = new SimpleDocument(pk, uploadData.getForeignId(), 0, false, null, attachment);
        document.setDocumentType(attachmentContext);
      }
      document.setLanguage(lang);
      document.setTitle(title);
      document.setDescription(description);
      document.setSize(context.getContentFile().length());
      document = saveContent(context, document, needCreation, publicDocument, lang);
      // in the case the document is a CAD one, process it for Actify
      ActifyDocumentProcessor.getProcessor().process(document);
      final URI attachmentUri = getUri().getRequestUriBuilder().path("document").path(document.
          getLanguage()).build();
      return SimpleDocumentEntity.fromAttachment(document).withURI(attachmentUri);
    } catch (RuntimeException re) {
      performRuntimeException(re);
    } finally {
      context.ifContentDataAvailable(c -> FileUtils.deleteQuietly(c.getContentFile()));
    }
    throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
  }

  private SimpleDocument saveContent(final Context context,
      final SimpleDocument document,final boolean needCreation,
      final boolean publicDocument, final String lang)
      throws IOException {
    final String userId = getUser().getId();
    final SimpleDocument result;
    try (final InputStream content = new BufferedInputStream(new FileInputStream(context.getContentFile()))) {
      if (needCreation) {
        result = getAttachmentService().createAttachment(document, content, context.mustIndex(), publicDocument);
      } else {
        document.edit(userId);
        getAttachmentService().lock(document.getId(), userId, lang);
        getAttachmentService().updateAttachment(document, content, context.mustIndex(), true);
        UnlockContext unlockContext = new UnlockContext(document.getId(), userId, lang);
        unlockContext.addOption(UnlockOption.UPLOAD);
        if (!publicDocument) {
          unlockContext.addOption(UnlockOption.PRIVATE_VERSION);
        }
        getAttachmentService().unlock(unlockContext);
        result = document;
      }
    }
    return result;
  }

  private Context initializeContext(final SimpleDocumentUploadData uploadData, final String filename) {
    final Context context;
    try {
      context = new Context(this, uploadData, filename);
    } catch (RuntimeException re) {
      performRuntimeException(re);
      throw re;
    }
    return context;
  }

  public static class Context {

    private final AbstractSimpleDocumentResource resource;
    private final SimpleDocumentUploadData uploadedData;
    private final DocumentTemplate documentTemplate;
    private final RequestFile requestFile;
    private final String filename;
    private final File tempFile;

    public Context(final AbstractSimpleDocumentResource resource,
        final SimpleDocumentUploadData uploadedData, final String filename) {
      this.resource = resource;
      this.uploadedData = uploadedData;
      this.documentTemplate = uploadedData.getDocumentTemplate().orElse(null);
      this.requestFile =
          this.documentTemplate == null && isDefined(uploadedData.getRequestFile().getName()) ?
              uploadedData.getRequestFile() :
              null;
      this.filename = computeFileName(filename);
      this.tempFile = createTemporaryFile();
    }

    boolean isDocumentTemplateContent() {
      return uploadedData.getDocumentTemplate().isPresent();
    }

    boolean contentDataAvailable() {
      return tempFile != null;
    }

    void ifContentDataAvailable(final Consumer<Context> consumer) {
      if (contentDataAvailable()) {
        consumer.accept(this);
      }
    }

    public String getFilename() {
      Objects.requireNonNull(filename);
      return filename;
    }

    File getContentFile() {
      Objects.requireNonNull(tempFile);
      return tempFile;
    }

    long getContentSize() {
      Objects.requireNonNull(tempFile);
      return documentTemplate != null ? tempFile.length() : requestFile.getSize();
    }

    String getContentType() {
      Objects.requireNonNull(tempFile);
      return FileUtil.getMimeType(tempFile.getPath());
    }

    boolean mustIndex() {
      return uploadedData.getIndexIt();
    }

    private File createTemporaryFile() {
      if (isDefined(filename) && (documentTemplate != null || requestFile != null)) {
        try (final InputStream uploadedInputStream = documentTemplate != null ?
            documentTemplate.openInputStream() :
            requestFile.getInputStream()) {
          final File result = File.createTempFile("silverpeas_", filename);
          FileUtils.copyToFile(uploadedInputStream, result);
          //check the file
          resource.checkUploadedFile(result);
          return result;
        } catch (IOException e) {
          throw new WebApplicationException(e, PRECONDITION_FAILED);
        }
      }
      return null;
    }

    private String computeFileName(final String filename) {
      final String result;
      if (documentTemplate != null) {
        result = ofNullable(FilenameUtils.getBaseName(filename))
            .filter(StringUtil::isDefined)
            .map(b -> b + "." + documentTemplate.getExtension())
            .orElse(null);
      } else if (StringUtil.isNotDefined(filename) && requestFile != null) {
        result = requestFile.getName();
      } else {
        result = filename;
      }
      return result;
    }
  }
}