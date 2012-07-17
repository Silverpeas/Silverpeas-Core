/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.attachment;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentRuntimeException;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

/**
 *
 * @author ehugonnet
 */
public interface AttachmentService {

  String NO_UPDATE_MODE = "0";
  String UPDATE_DIRECT_MODE = "1";
  String UPDATE_SHORTCUT_MODE = "2";

  /**
   * To update a document content by updating or adding some content.
   *
   * @param document
   * @param content
   * @param indexIt
   * @param invokeCallback
   */
  void addContent(SimpleDocument document, InputStream content, boolean indexIt,
      boolean invokeCallback);

  /**
   * To update a document content by updating or adding some content.
   *
   * @param document
   * @param content
   * @param indexIt
   * @param invokeCallback
   */
  void addContent(SimpleDocument document, File content, boolean indexIt, boolean invokeCallback);

  /**
   *  Writes the binary content into the specified File.
   *
   * @param file the file where the content is to be written.
   * @param pk the id of the document.
   * @param lang the language of the content.
   * @return a stream to the content.
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

  void addXmlForm(SimpleDocumentPK pk, String language, String xmlFormName);

  /**
   * Clone the attchments
   *
   * @param original
   * @param foreignCloneId
   * @return
   */
  SimpleDocumentPK cloneDocument(SimpleDocument original, String foreignCloneId);

  /**
   * Create file attached to an object who is identified by the foreignId.
   *
   * @param document the document to be created.
   * @param content the binary content of the document.
   * @param indexIt<code>true</code> if the document is to be indexed,  <code>false</code>
   * otherwhise.
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
   * @param indexIt<code>true</code> if the document is to be indexed,  <code>false</code>
   * otherwhise.
   * @return the stored document.
   */
  SimpleDocument createAttachment(SimpleDocument document, InputStream content, boolean indexIt);

  /**
   * Create file attached to an object who is identified by the foreignId.
   *
   * @param document the document to be created.
   * @param content the binary content of the document.
   * @param indexIt<code>true</code> if the document is to be indexed,  <code>false</code>
   * otherwhise.
   * @param invokeCallback <code>true</code> if the callback methods of the components must be
   * called, <code>false</code> for ignoring thoose callbacks.
   * @return the stored document.
   */
  SimpleDocument createAttachment(SimpleDocument document, InputStream content, boolean indexIt,
      boolean invokeCallback);

  /**
   * Create file attached to an object who is identified by the foreignId.
   *
   * @param document the document to be created.
   * @param content the binary content of the document.
   * @param indexIt<code>true</code> if the document is to be indexed,  <code>false</code>
   * otherwhise.
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
   * @param indexIt<code>true</code> if the document is to be indexed,  <code>false</code>
   * otherwhise.
   * @return the stored document.
   */
  SimpleDocument createAttachment(SimpleDocument document, File content, boolean indexIt);

  /**
   * Create file attached to an object who is identified by the foreignId.
   *
   * @param document the document to be created.
   * @param content the binary content of the document.
   * @param indexIt<code>true</code> if the document is to be indexed,  <code>false</code>
   * otherwhise.
   * @param invokeCallback <code>true</code> if the callback methods of the components must be
   * called, <code>false</code> for ignoring thoose callbacks.
   * @return the stored document.
   */
  SimpleDocument createAttachment(SimpleDocument document, File content, boolean indexIt,
      boolean invokeCallback);

  /**
   *
   * @param document
   * @param lang
   */
  void createIndex(SimpleDocument document);

  /**
   *
   * @param document
   * @param lang
   * @param startOfVisibilityPeriod
   * @param endOfVisibilityPeriod
   */
  void createIndex(SimpleDocument document, Date startOfVisibilityPeriod, Date endOfVisibilityPeriod);

  /**
   * Delete a given attachment.
   *
   * @param document the document to deleted.
   */
  void deleteAttachment(SimpleDocument document);

  /**
   * Delete a given attachment.
   *
   * @param attachmentDetail the attachmentDetail object to deleted.
   * @param invokeCallback   <code>true</code> if the callback methods of the components must be
   * called, <code>false</code> for ignoring thoose callbacks.
   * @throws AttachmentRuntimeException if the attachement cannot be deleted.
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
   * to search all file attached
   *
   * @param primaryKey the primary key of object AttachmentDetail
   * @return java.util.Vector: a collection of AttachmentDetail
   * @throws AttachmentRuntimeException when is impossible to search
   */
  SimpleDocument searchAttachmentById(SimpleDocumentPK primaryKey, String lang);

  /**
   * to search all file attached to an object who is identified by "PK"
   *
   * @param foreignKey : com.stratelia.webactiv.util.WAPrimaryKey: the primary key of customer
   * object but this key must be transformed to AttachmentPK
   * @return java.util.Vector: a collection of AttachmentDetail
   * @throws AttachmentRuntimeException when is impossible to search
   */
  List<SimpleDocument> searchAttachmentsByExternalObject(WAPrimaryKey foreignKey, String lang);

  void unindexAttachmentsOfExternalObject(WAPrimaryKey foreignKey);

  /**
   * To update the document : status, informations but not its content.
   *
   * @param document
   * @param indexIt
   * @param invokeCallback
   */
  void updateAttachment(SimpleDocument document, boolean indexIt, boolean invokeCallback);

  void updateIndexEntryWithAttachments(FullIndexEntry indexEntry);

  /**
   * Search all the documents in an instance which are locked at the alert date.
   *
   * @param alertDate the date when a warning is required.
   * @param language the language in which the documents are required.
   * @return an ordered list of the documents.
   */
  List<SimpleDocument> listDocumentsRequiringWarning(Date alertDate, String language);

  /**
   * Search all the documents in an instance which are expiring at the specified date.
   *
   * @param expiryDate the date when the document reservation should expire.
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
   * Checkout a file to be updated by user
   *
   * @param attachmentId
   * @param userId
   * @param language
   * @return false if the attachment is already checkout - true if the attachment was successfully
   * checked out.
   */
  public boolean lock(String attachmentId, String userId, String language);
  
    /**
   * Release a locked file.
   *
   * @param attachmentId
   * @param userId
   * @param upload : indicates if the file has been uploaded throught a form.
   * @param force if the user is an Admin he can force the release.
   * @param language the language for the attachment.
   * @return false if the file is locked - true if the checkin succeeded.
   * @throws AttachmentException
   */
  public boolean unlock(String attachmentId, String userId, boolean upload,
      boolean update, boolean force, String language);
}
