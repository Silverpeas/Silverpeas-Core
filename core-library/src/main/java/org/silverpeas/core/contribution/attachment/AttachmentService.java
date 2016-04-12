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
package org.silverpeas.core.contribution.attachment;

import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.index.indexing.model.DocumentIndexing;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.model.UnlockContext;
import org.silverpeas.core.contribution.attachment.util.SimpleDocumentList;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ehugonnet
 */
public interface AttachmentService extends DocumentIndexing {

  String VERSION_MODE = "versionControl";

  static AttachmentService get() {
    return ServiceProvider.getService(AttachmentService.class);
  }

  /**
   * Deletes all the documents related to the component instance identified by the specified
   * identifier.
   * @param componentInstanceId the component instance identifier.
   * @throws AttachmentException
   */
  void deleteAllAttachments(String componentInstanceId) throws AttachmentException;

  /**
   * Writes the binary content into the specified File.
   *
   * @param file the file where the content is to be written.
   * @param pk the id of the document.
   * @param lang the language of the content.
   */
  void getBinaryContent(File file, SimpleDocumentPK pk, String lang);

  /**
   * Writes the binary content into the specified OutputStream.
   *
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
   * Clone the document to a cloned container.
   *
   * @param original
   * @param foreignCloneId
   * @return
   */
  SimpleDocumentPK cloneDocument(SimpleDocument original, String foreignCloneId);

  /**
   * Merges the documents of cloned container with the original documents.
   *
   * @param originalForeignKey
   * @param cloneForeignKey
   * @param type
   * @return a map with the cloned document id as key and the original document id as value.
   */
  Map<String, String> mergeDocuments(ForeignPK originalForeignKey, ForeignPK cloneForeignKey,
      DocumentType type);

  /**
   * Copies the attachment.
   * @param original
   * @param targetPk
   * @return
   */
  SimpleDocumentPK copyDocument(SimpleDocument original, ForeignPK targetPk);

  /**
   * Copies all the attachment linked to a resource to another one.
   * @param resourceSourcePk the identifier of the resource and its location (component instance)
   * which linked attachments must be copied.
   * @param targetDestinationPk the identifier of the resource and its location (component
   * instance)
   * that will get the copied attachments.
   * @return le list of copied attachments, empty if nothing is copied.
   */
  List<SimpleDocumentPK> copyAllDocuments(WAPrimaryKey resourceSourcePk,
      WAPrimaryKey targetDestinationPk);

  /**
   * Moves the attachment.
   * @param document to be moved.
   * @param destination the foreign id to be moved to.
   * @return the new document id.
   */
  SimpleDocumentPK moveDocument(SimpleDocument document, ForeignPK destination);

  /*
   * Moves all the attachment linked to a resource to another one.
   * @param resourceSourcePk the identifier of the resource and its location (component instance)
   * which linked attachments must be moved.
   * @param targetDestinationPk the identifier of the resource and its location (component instance)
   * that will get the moved attachments.
   * @return le list of moved attachments, empty if nothing is moved.
   */
  List<SimpleDocumentPK> moveAllDocuments(WAPrimaryKey resourceSourcePk,
      WAPrimaryKey targetDestinationPk);


  /**
   * Create file attached to an object who is identified by the foreignId.
   *
   * @param document the document to be created.
   * @param content the binary content of the document.
   * @return the stored document.
   * @throws AttachmentException
   */
  SimpleDocument createAttachment(SimpleDocument document, InputStream content) throws
      AttachmentException;

  /**
   * Create file attached to an object who is identified by the foreignId.
   *
   * @param document the document to be created.
   * @param content the binary content of the document.
   * @param indexIt true if the document is to be indexed - false otherwhise.
   * @return the stored document.
   */
  SimpleDocument createAttachment(SimpleDocument document, InputStream content, boolean indexIt);

  /**
   * Create file attached to an object who is identified by the foreignId.
   *
   * @param document the document to be created.
   * @param content the binary content of the document.
   * @param indexIt true if the document is to be indexed - false otherwise.
   * @param invokeCallback true if the callback methods of the components must be called, false for
   * ignoring thoose callbacks.
   * @return the stored document.
   */
  SimpleDocument createAttachment(SimpleDocument document, InputStream content, boolean indexIt,
      boolean invokeCallback);

  /**
   * Create file attached to an object who is identified by the foreignId.
   *
   * @param document the document to be created.
   * @param content the binary content of the document.
   * @return the stored document.
   * @throws AttachmentException
   */
  SimpleDocument createAttachment(SimpleDocument document, File content) throws
      AttachmentException;

  /**
   * Create file attached to an object who is identified by the foreignId.
   *
   * @param document the document to be created.
   * @param content the binary content of the document.
   * @param indexIt true if the document is to be indexed, false otherwhise.
   * @return the stored document.
   */
  SimpleDocument createAttachment(SimpleDocument document, File content, boolean indexIt);

  /**
   * Create file attached to an object who is identified by the foreignId.
   *
   * @param document the document to be created.
   * @param content the binary content of the document.
   * @param indexIt true if the document is to be indexed, false otherwhise.
   * @param invokeCallback true if the callback methods of the components must be called, false for
   * ignoring thoose callbacks.
   * @return the stored document.
   */
  SimpleDocument createAttachment(SimpleDocument document, File content, boolean indexIt,
      boolean invokeCallback);

  /**
   *
   * @param document
   */
  void createIndex(SimpleDocument document);

  /**
   *
   * @param document
   */
  void deleteIndex(SimpleDocument document);

  /**
   *
   * @param document
   * @param startOfVisibilityPeriod
   * @param endOfVisibilityPeriod
   */
  void createIndex(SimpleDocument document, Date startOfVisibilityPeriod, Date endOfVisibilityPeriod);

  /**
   * Deletes all the document attached to a component resource.
   * @param resourceId the identifier of the resource.
   * @param componentInstanceId the identifier of the component instance into which the resource is
   * referenced.
   */
  void deleteAllAttachments(String resourceId, String componentInstanceId);

  /**
   * Delete a given attachment.
   *
   * @param document the document to deleted.
   */
  void deleteAttachment(SimpleDocument document);

  /**
   * Delete a given attachment.
   *
   * @param document the document to deleted.
   * @param invokeCallback true if the callback methods of the components must be called, false for
   * ignoring thoose callbacks.
   * @throws AttachmentException if the attachement cannot be deleted.
   */
  void deleteAttachment(SimpleDocument document, boolean invokeCallback);

  /**
   * To remove the content of the document in the specified language.
   *
   * @param document
   * @param lang
   * @param invokeCallback
   */
  void removeContent(SimpleDocument document, String lang, boolean invokeCallback);

  /**
   * Reorder the attachments according to the order in the list.
   *
   * @param pks
   * @throws AttachmentException
   */
  void reorderAttachments(List<SimpleDocumentPK> pks) throws AttachmentException;

  /**
   * Reorder the attachments according to the order in the list.
   *
   *
   * @param documents
   * @throws AttachmentException
   */
  void reorderDocuments(List<SimpleDocument> documents) throws AttachmentException;

  /**
   * Search the document.
   *
   * @param primaryKey the primary key of document.
   * @param lang the lang of the document.
   * @return java.util.Vector: a collection of AttachmentDetail
   * @throws AttachmentException when is impossible to search
   */
  SimpleDocument searchDocumentById(SimpleDocumentPK primaryKey, String lang);

  /**
   * Search all files attached to a foreign object.
   *
   * @param foreignKey : the primary key of foreign object.
   * @param lang the language of the documents.
   * @return the list of attached documents.
   * @throws AttachmentException when is impossible to search
   */
  SimpleDocumentList<SimpleDocument> listDocumentsByForeignKey(WAPrimaryKey foreignKey,
      String lang);

  /**
   * Search all documents (files, xmlform content, wysiwyg) attached to a foreign object.
   *
   * @param foreignKey : the primary key of foreign object.
   * @param lang the language of the documents.
   * @return the list of attached documents.
   * @throws AttachmentException when is impossible to search
   */
  SimpleDocumentList<SimpleDocument> listAllDocumentsByForeignKey(WAPrimaryKey foreignKey,
      String lang);

  /**
   * Search all file attached to a foreign object.
   *
   * @param foreignKey : the primary key of foreign object.
   * @param type : the type of document
   * @param lang the lang for the documents.
   * @return the list of attached documents.
   * @throws AttachmentException when is impossible to search
   */
  SimpleDocumentList<SimpleDocument> listDocumentsByForeignKeyAndType(WAPrimaryKey foreignKey,
      DocumentType type, String lang);

  void unindexAttachmentsOfExternalObject(WAPrimaryKey foreignKey);

  /**
   * To update the document : status, metadata but not its content.
   *
   * @param document
   * @param indexIt
   * @param invokeCallback
   */
  void updateAttachment(SimpleDocument document, boolean indexIt, boolean invokeCallback);

  /**
   * To update a document content by updating or adding some content.
   *
   * @param document
   * @param content
   * @param indexIt
   * @param invokeCallback
   */
  void updateAttachment(SimpleDocument document, File content, boolean indexIt,
      boolean invokeCallback);

  /**
   * To update a document content by updating or adding some content.
   *
   * @param document
   * @param content
   * @param indexIt
   * @param invokeCallback
   */
  void updateAttachment(SimpleDocument document, InputStream content, boolean indexIt,
      boolean invokeCallback);

  /**
   * Search all the documents in an instance which are locked at the alert date.
   *
   * @param alertDate the date when a warning is required.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   */
  List<SimpleDocument> listDocumentsRequiringWarning(Date alertDate, String language);

  /**
   * Search all the documents in an instance which require an alert at the specified date.
   *
   * @param alertDate the date when the document reservation should alter.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   */
  List<SimpleDocument> listExpiringDocuments(Date alertDate, String language);

  /**
   * Search all the documents in an instance requiring to be unlocked at the specified date.
   *
   * @param expiryDate the date when the document reservation should expire.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   */
  List<SimpleDocument> listDocumentsToUnlock(Date expiryDate, String language);

  /**
   * Checkout a file to be updated by user.
   *
   * @param attachmentId the id of the attachemnt to be locked.
   * @param userId : the user locking and modifying the attachment.
   * @param language the language of the attachment.
   * @return false if the attachment is already checkout - true if the attachment was successfully
   * checked out.
   */
  public boolean lock(String attachmentId, String userId, String language);

  /**
   * Release a locked file.
   *
   * @param context : the unlock parameters.
   * @return false if the file is locked - true if the unlock succeeded.
   * @throws AttachmentException
   */
  public boolean unlock(UnlockContext context);

  /**
   * Change the management of versions of the document. If the document is currently with version
   * management, then all history is removed and the document becomes a simple document with no more
   * version management. If the document has no version management then a new public version is
   * created and the document becomes a document with a version history management. F
   *
   * @param pk the id of the document.
   * @param comment the comment of the versioned documetn if we are switching from simple to
   * versioned.
   * @return the pk to the document after is state change.
   */
  public SimpleDocumentPK changeVersionState(SimpleDocumentPK pk, String comment);

  /**
   * Find documents with the same name attached to the specified foreign id.
   *
   * @param fileName the name of the file.
   * @param pk the id of the document.
   * @param lang the language of the document.
   * @param foreign the id of the container of the document.
   * @return a document with the same filename - null if none is found.
   */
  public SimpleDocument findExistingDocument(SimpleDocumentPK pk, String fileName, ForeignPK foreign,
      String lang);

  /**
   * Search all the documents locked by a specific user.
   *
   * @param usedId the id of the user.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   */
  List<SimpleDocument> listDocumentsLockedByUser(String usedId, String language);

  /**
   * Add the documents to the index.
   *
   * @param indexEntry the entry to be updated with the document indexes.
   */
  @Override
  void updateIndexEntryWithDocuments(FullIndexEntry indexEntry);

  /**
   * Indexes all the documents (whatever their type) of a container.
   *
   * @param fk the id of the container of the document.
   * @param startOfVisibilityPeriod can be null.
   * @param endOfVisibilityPeriod can be null.
   */
  void indexAllDocuments(WAPrimaryKey fk, Date startOfVisibilityPeriod, Date endOfVisibilityPeriod);

  /**
   * Change the management of versions of the documents of a whole component (only attachments are
   * taken into account). If the document is currently with version management, then all history is
   * removed and the document becomes a simple document with no more version management. If the
   * document has no version management then a new public version is created and the document
   * becomes a document with a version history management.
   *
   * @param componentId : the id of the component switching its behaviour.
   * @param toVersionning: if set to true all simple attachments become versioned, if false all
   * versioned attachments become simple attachments.
   */
  public void switchComponentBehaviour(String componentId, boolean toVersionning);

  /**
   * Allows or forbids the download for readers.
   *
   * @param pk the id of the document.
   * @param allowing: allowing the download for readers if true
   * versioned attachments become simple attachments.
   */
  public void switchAllowingDownloadForReaders(SimpleDocumentPK pk, boolean allowing);
}
