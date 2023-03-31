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
package org.silverpeas.core.contribution.attachment;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.model.UnlockContext;
import org.silverpeas.core.contribution.attachment.util.SimpleDocumentList;
import org.silverpeas.core.index.indexing.model.DocumentIndexing;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ServiceProvider;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Service to manage the attachments of resources in Silverpeas.
 * @author ehugonnet
 */
public interface AttachmentService extends DocumentIndexing {

  String VERSION_MODE = "versionControl";

  static AttachmentService get() {
    return ServiceProvider.getSingleton(AttachmentService.class);
  }

  /**
   * Deletes all the documents related to the component instance identified by the specified
   * identifier.
   * @param componentInstanceId the component instance identifier.
   * @throws AttachmentException if the deletion failed.
   */
  void deleteAllAttachments(String componentInstanceId) throws AttachmentException;

  /**
   * Writes the binary content into the specified File.
   * @param file the file where the content is to be written.
   * @param pk the id of the document.
   * @param lang the language of the content.
   */
  void getBinaryContent(File file, SimpleDocumentPK pk, String lang);

  /**
   * Writes the binary content into the specified OutputStream.
   * @param output the stream where the content is to be written.
   * @param pk the id of the document.
   * @param lang the language of the content.
   */
  void getBinaryContent(OutputStream output, SimpleDocumentPK pk, String lang);

  /**
   * Writes the binary content contained between begin and end indexes into the specified
   * OutputStream.
   * @param output the stream where the content is to be written.
   * @param pk the id of the document.
   * @param lang the language of the content.
   * @param contentOffset number of bytes to skip from input content before copying into output.
   * @param contentLength number of bytes to copy.
   */
  void getBinaryContent(OutputStream output, SimpleDocumentPK pk, String lang, long contentOffset,
      long contentLength);

  void addXmlForm(SimpleDocumentPK pk, String language, String xmlFormName);

  /**
   * Clones the given document and attached it to the specified resource. The document is attached
   * to a resource that has been cloned; hence this method is to clone the document in order to
   * attach it to the cloned resource. The difference between a clone and a copy is the clone
   * maintains a link with its original counterpart: any changes performed in one is synchronized
   * with the other.
   * @param original the document to clone.
   * @param foreignCloneId the identifier of the resource to which the document is attached.
   * @return the identifier of the cloned document.
   */
  SimpleDocumentPK cloneDocument(SimpleDocument original, String foreignCloneId);

  /**
   * Merges the documents of the cloned resource with their original counterparts.
   * @param originalResource the original resource.
   * @param clonedResource the cloned resource.
   * @param type the type of the documents to merge.
   * @return a map with the cloned document identifier as key and the original document identifier
   * as value.
   */
  Map<String, String> mergeDocuments(ResourceReference originalResource,
      ResourceReference clonedResource,
      DocumentType type);

  /**
   * Copies the specified document to another existing resource. The difference between a copy with
   * a clone is the copy is detached to the original document from which it has been copied. So any
   * changes in one isn't reported to the other one.
   * @param original the document to copy.
   * @param targetResource the resource to which the copy of the document has to be attached.
   * @return the identifier of the copy.
   */
  SimpleDocumentPK copyDocument(SimpleDocument original, ResourceReference targetResource);

  /**
   * Copies all the attachments linked to the given source resource to the specified destination
   * one.
   * @param sourceResource the reference of the resource (identifier and component instance) from
   * which the attachments must be copied.
   * @param destinationResource the reference of the resource (identifier and component instance) to
   * which attachments will be copied.
   * @return le list of copied attachments, empty if nothing is copied.
   */
  List<Pair<SimpleDocumentPK, SimpleDocumentPK>> copyAllDocuments(
      ResourceReference sourceResource,
      ResourceReference destinationResource);

  /**
   * Moves the specified document to the given resource.
   * @param document the document to be moved.
   * @param destination the resource to which the document has to be attached.
   * @return the new document identifier once moved.
   */
  SimpleDocumentPK moveDocument(SimpleDocument document, ResourceReference destination);

  /**
   * Moves all the attachments linked to the specified resource to the another one.
   * @param sourceResource the reference of the resource (identifier and component instance) from
   * which linked attachments must be moved.
   * @param destinationResource the reference of the resource (identifier and component instance)
   * that will get the moved attachments.
   * @return le list of moved attachments, empty if nothing is moved.
   */
  List<SimpleDocumentPK> moveAllDocuments(ResourceReference sourceResource,
      ResourceReference destinationResource);


  /**
   * Creates the attachment and saves the content of the specified document. The created document is
   * indexed and a notification about the creation is sent.
   * <p>
   * The filename returned by {@link SimpleDocument#getFilename()} is normalized in order to get
   * only single character encoding and no more combined characters. The normalized filename is set
   * to the given document.
   * </p>
   * @param document the document to be created.
   * @param content the binary content of the document.
   * @return the stored document.
   * @throws AttachmentException if the creation of the specified document fails.
   */
  SimpleDocument createAttachment(SimpleDocument document, InputStream content)
      throws AttachmentException;

  /**
   * Creates the attachment, saves the content of the specified document and indexes it if asked.
   * Once the attachment created, a notification about this creation is then sent.
   * <p>
   * The filename returned by {@link SimpleDocument#getFilename()} is normalized in order to get
   * only single character encoding and no more combined characters. The normalized filename is set
   * to the given document.
   * </p>
   * @param document the document to be created.
   * @param content the binary content of the document.
   * @param indexIt true if the document has to be indexed, false otherwise.
   * @return the stored document.
   */
  SimpleDocument createAttachment(SimpleDocument document, InputStream content, boolean indexIt);

  /**
   * Creates the attachment, saves the content of the specified document, indexes it if asked, and
   * notifies about the creation.
   * <p>
   * The filename returned by {@link SimpleDocument#getFilename()} is normalized in order to get
   * only single character encoding and no more combined characters. The normalized filename is set
   * to the given document.
   * </p>
   * @param document the document to be created.
   * @param content the binary content of the document.
   * @param indexIt true if the document has to be indexed, false otherwise.
   * @param notify true if a notification about the creation has to be sent, false otherwise.
   * @return the stored document.
   */
  SimpleDocument createAttachment(SimpleDocument document, InputStream content, boolean indexIt,
      boolean notify);

  /**
   * Creates the attachment and saves the content of the specified document. The created document is
   * indexed and a notification about the creation is sent.
   * <p>
   * The filename returned by {@link SimpleDocument#getFilename()} is normalized in order to get
   * only single character encoding and no more combined characters. The normalized filename is set
   * to the given document.
   * </p>
   * @param document the document to be created.
   * @param content the binary content of the document.
   * @return the stored document.
   * @throws AttachmentException if an error occurs while creating the attachment and saving the
   * content.
   */
  SimpleDocument createAttachment(SimpleDocument document, File content) throws AttachmentException;

  /**
   * Creates the attachment, saves the content of the specified document and indexes it if asked.
   * Once the attachment created, a notification about this creation is then sent.
   * <p>
   * The filename returned by {@link SimpleDocument#getFilename()} is normalized in order to get
   * only single character encoding and no more combined characters. The normalized filename is set
   * to the given document.
   * </p>
   * @param document the document to be created.
   * @param content the binary content of the document.
   * @param indexIt true if the document has to be indexed, false otherwise.
   * @return the stored document.
   */
  SimpleDocument createAttachment(SimpleDocument document, File content, boolean indexIt);

  /**
   * Creates the attachment, saves the content of the specified document, indexes it if asked, and
   * notifies about the creation.
   * <p>
   * The filename returned by {@link SimpleDocument#getFilename()} is normalized in order to get
   * only single character encoding and no more combined characters. The normalized filename is set
   * to the given document.
   * </p>
   * @param document the document to be created.
   * @param content the binary content of the document.
   * @param indexIt true if the document has to be indexed, false otherwise.
   * @param notify true if a notification about the creation has to be sent, false otherwise.
   * @return the stored document.
   */
  SimpleDocument createAttachment(SimpleDocument document, File content, boolean indexIt,
      boolean notify);

  /**
   * Indexes the specified document.
   * @param document the document
   */
  void createIndex(SimpleDocument document);

  /**
   * Deletes any index on the specified document.
   * @param document the document.
   */
  void deleteIndex(SimpleDocument document);

  /**
   * Creates an index on the specified document with a period of time of visibility.
   * @param document the document to index.
   * @param startOfVisibilityPeriod the date at which the document should be visible.
   * @param endOfVisibilityPeriod the date at which the document shouldn't be anymore visible.
   */
  void createIndex(SimpleDocument document, Date startOfVisibilityPeriod,
      Date endOfVisibilityPeriod);

  /**
   * Deletes all the documents attached to a resource.
   * @param resourceId the identifier of the resource.
   * @param componentInstanceId the identifier of the component instance into which the resource is
   * located.
   */
  void deleteAllAttachments(String resourceId, String componentInstanceId);

  /**
   * Deletes the given document and notifies about this deletion.
   * @param document the document to delete.
   */
  void deleteAttachment(SimpleDocument document);

  /**
   * Deletes the given document and notifies about this deletion is asked.
   * @param document the document to delete.
   * @param notify true if a notification about the deletion has to be sent. False otherwise.
   * @throws AttachmentException if the document cannot be deleted.
   */
  void deleteAttachment(SimpleDocument document, boolean notify);

  /**
   * Removes the content of the specified document in the given language.
   * @param document the document.
   * @param lang the ISO-631 code of a language.
   * @param notify true if a notification about the content deletion has to be sent. False
   * otherwise. If the document has again content (in others languages), an document update event is
   * sent. Otherwise a deletion event is sent.
   */
  void removeContent(SimpleDocument document, String lang, boolean notify);

  /**
   * Reorders the documents referenced in the given list according their order in the list.
   * @param pks a list of document identifiers.
   * @throws AttachmentException if an error occurs while reordering the documents.
   */
  void reorderAttachments(List<SimpleDocumentPK> pks) throws AttachmentException;

  /**
   * Reorders the documents in the given list according their order in the list.
   * @param documents the documents to reorder.
   * @throws AttachmentException if an error occurs while reordering the documents.
   */
  void reorderDocuments(List<SimpleDocument> documents) throws AttachmentException;

  /**
   * Search the document with the given identifier and in the specified language.
   * @param docPk the identifier of the document to search.
   * @param lang the ISO-631 code of a language.
   * @return the document or null if no such document exists.
   * @throws AttachmentException if an error occurs while searching the document.
   */
  SimpleDocument searchDocumentById(SimpleDocumentPK docPk, String lang);

  /**
   * Search all the documents of type files attached to the specified resource and in the given
   * language.
   * @param foreignKey a reference to the resource.
   * @param lang the ISO-631 code of a language.
   * @return the list of documents of type files attached to the specified resource.
   * @throws AttachmentException if an error occurs while searching the documents.
   */
  SimpleDocumentList<SimpleDocument> listDocumentsByForeignKey(ResourceReference foreignKey,
      String lang);

  /**
   * Search all the documents (whatever their type: files, xmlform content, wysiwyg, ...) attached
   * to the specified resource and in the given language.
   * @param foreignKey a reference to the resource.
   * @param lang the ISO-631 code of a language.
   * @return the list of documents attached to the specified resource.
   * @throws AttachmentException if an error occurs while searching the documents.
   */
  SimpleDocumentList<SimpleDocument> listAllDocumentsByForeignKey(ResourceReference foreignKey,
      String lang);

  /**
   * Search all the documents attached to the specified resource, for a given type of document, and
   * in the specified language.
   * @param foreignKey a reference to the resource.
   * @param type the type of the documents to search.
   * @param lang the ISO-621 code of a language.
   * @return the list of documents attached to the specified resource.
   * @throws AttachmentException if an error occurs while searching the documents.
   */
  SimpleDocumentList<SimpleDocument> listDocumentsByForeignKeyAndType(ResourceReference foreignKey,
      DocumentType type, String lang);

  /**
   * Remove indexes on all the attachments of the specified resource.
   * @param externalResource a reference to the resource.
   */
  void unindexAttachmentsOfExternalObject(ResourceReference externalResource);

  /**
   * Updates the specified whole document: status, metadata but not its content.
   * <p>
   * The filename returned by {@link SimpleDocument#getFilename()} is normalized in order to get
   * only single character encoding and no more combined characters. The normalized filename is set
   * to given document.
   * </p>
   * @param document the document to update.
   * @param indexIt true if the document change has to be indexed. False otherwise.
   * @param notify true if a notification about the update has to be sent. False otherwise.
   */
  void updateAttachment(SimpleDocument document, boolean indexIt, boolean notify);

  /**
   * Updates the specified whole document with its content either by updating the given content or
   * by adding it among its others contents.
   * <p>
   * The filename returned by {@link SimpleDocument#getFilename()} is normalized in order to get
   * only single character encoding and no more combined characters. The normalized filename is set
   * to given document.
   * </p>
   * @param document the document to update.
   * @param content the content to update or to add (in the case of multi-languages support).
   * @param indexIt true if the change has to be indexed. False otherwise.
   * @param notify true if a notification about the update has to be sent. False otherwise.
   */
  void updateAttachment(SimpleDocument document, File content, boolean indexIt, boolean notify);

  /**
   * Updates the specified whole document with its content either by updating the given content or
   * by adding it among its others contents.
   * <p>
   * The filename returned by {@link SimpleDocument#getFilename()} is normalized in order to get
   * only single character encoding and no more combined characters. The normalized filename is set
   * to given document.
   * </p>
   * @param document the document to update.
   * @param content the content to update or to add (in the case of multi-languages support).
   * @param indexIt true if the change has to be indexed. False otherwise.
   * @param notify true if a notification about the update has to be sent. False otherwise.
   */
  void updateAttachment(SimpleDocument document, InputStream content, boolean indexIt,
      boolean notify);

  /**
   * Search all the documents in an instance which are locked at the alert date.
   * @param alertDate the date when a warning is required.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   */
  List<SimpleDocument> listDocumentsRequiringWarning(Date alertDate, String language);

  /**
   * Search all the documents in an instance which require an alert at the specified date.
   * @param alertDate the date when the document reservation should alter.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   */
  List<SimpleDocument> listExpiringDocuments(Date alertDate, String language);

  /**
   * Search all the documents in an instance requiring to be unlocked at the specified date.
   * @param expiryDate the date when the document reservation should expire.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   */
  List<SimpleDocument> listDocumentsToUnlock(Date expiryDate, String language);

  /**
   * Checkout a file to be updated by user.
   * @param attachmentId the id of the attachment to be locked.
   * @param userId : the user locking and modifying the attachment.
   * @param language the language of the attachment.
   * @return false if the attachment is already checkout - true if the attachment was successfully
   * checked out.
   */
  boolean lock(String attachmentId, String userId, String language);

  /**
   * Release a locked file.
   * @param context : the unlock parameters.
   * @return false if the file is locked - true if the unlock succeeded.
   * @throws AttachmentException if the unlocking fails.
   */
  boolean unlock(UnlockContext context);

  /**
   * Change the management of versions of the document. If the document is currently with version
   * management, then all history is removed and the document becomes a simple document with no more
   * version management. If the document has no version management then a new public version is
   * created and the document becomes a document with a version history management. F
   * @param pk the id of the document.
   * @param comment the comment of the versioned document if we are switching from simple to
   * versioned.
   * @return the pk to the document after is state change.
   */
  SimpleDocumentPK changeVersionState(SimpleDocumentPK pk, String comment);

  /**
   * Find documents with the same name attached to the specified foreign id.
   * @param fileName the name of the file.
   * @param pk the id of the document.
   * @param lang the language of the document.
   * @param foreign the id of the container of the document.
   * @return a document with the same filename - null if none is found.
   */
  SimpleDocument findExistingDocument(SimpleDocumentPK pk, String fileName,
      ResourceReference foreign,
      String lang);

  /**
   * Search all the documents locked by a specific user.
   * @param usedId the id of the user.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   */
  List<SimpleDocument> listDocumentsLockedByUser(String usedId, String language);

  /**
   * Add the documents to the index.
   * @param indexEntry the entry to be updated with the document indexes.
   */
  @Override
  void updateIndexEntryWithDocuments(FullIndexEntry indexEntry);

  /**
   * Indexes all the documents (whatever their type) of the given resource.
   * @param resource the resource.
   * @param startOfVisibilityPeriod the start date of the document visibility. It can be null.
   * @param endOfVisibilityPeriod the end date of the document visibility. It can be null.
   */
  void indexAllDocuments(ResourceReference resource, Date startOfVisibilityPeriod,
      Date endOfVisibilityPeriod);

  /**
   * Change the management of versions of the documents of a whole component (only attachments are
   * taken into account). If the document is currently with version management, then all history is
   * removed and the document becomes a simple document with no more version management. If the
   * document has no version management then a new public version is created and the document
   * becomes a document with a version history management.
   * @param componentId the id of the component switching its behaviour.
   * @param versioned if set to true all simple attachments become versioned, if false all
   * versioned attachments become simple attachments.
   */
  void switchComponentBehaviour(String componentId, boolean versioned);

  /**
   * Allows or forbids the download for readers.
   * @param pk the id of the document.
   * @param allowing: allowing the download for readers if true versioned attachments become simple
   * attachments.
   */
  void switchAllowingDownloadForReaders(SimpleDocumentPK pk, boolean allowing);

  /**
   * Enables or not the display of the content of an attachment.
   * @param pk the id of the document.
   * @param enable enable the display if true
   */
  void switchEnableDisplayAsContent(SimpleDocumentPK pk, boolean enable);

  /**
   * Enables or not the simultaneous edition of the content of an attachment.
   * @param pk the id of the document.
   * @param enable enable edition simultaneously if true
   */
  void switchEnableEditSimultaneously(SimpleDocumentPK pk, boolean enable);
}
