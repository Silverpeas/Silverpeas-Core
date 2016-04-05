/*
 * Copyright (C) 2000-2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
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
package org.silverpeas.core.contribution.attachment.mock;

import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.WAPrimaryKey;
import org.mockito.Mockito;
import org.silverpeas.core.contribution.attachment.AttachmentException;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.model.UnlockContext;
import org.silverpeas.core.contribution.attachment.util.SimpleDocumentList;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static javax.interceptor.Interceptor.Priority.APPLICATION;

/**
 * A wrapper of a mock of an {@code AttachmentService} instance dedicated to the tests. This wrapper
 * decorates the mock and it is used to be managed by an IoC container as an
 * {@code AttachmentService} instance.
 *
 * @author mmoquillon
 */
@Singleton
@Alternative
@Priority(APPLICATION + 10)
public class AttachmentServiceMockWrapper implements AttachmentService {

  private final AttachmentService mock = Mockito.mock(AttachmentService.class);

  public AttachmentService getAttachmentServiceMock() {
    return mock;
  }

  @Override
  public void deleteAllAttachments(final String componentInstanceId) throws AttachmentException {
    mock.deleteAllAttachments(componentInstanceId);
  }

  @Override
  public void getBinaryContent(File file, SimpleDocumentPK pk, String lang) {
    mock.getBinaryContent(file, pk, lang);
  }

  @Override
  public void getBinaryContent(OutputStream output, SimpleDocumentPK pk, String lang) {
    mock.getBinaryContent(output, pk, lang);
  }

  @Override
  public void getBinaryContent(final OutputStream output, final SimpleDocumentPK pk,
      final String lang, final long contentOffset, final long contentLength) {
    mock.getBinaryContent(output, pk, lang, contentOffset, contentLength);
  }

  @Override
  public void addXmlForm(SimpleDocumentPK pk, String language, String xmlFormName) {
    mock.addXmlForm(pk, language, xmlFormName);
  }

  @Override
  public SimpleDocumentPK cloneDocument(SimpleDocument original, String foreignCloneId) {
    return mock.cloneDocument(original, foreignCloneId);
  }

  @Override
  public Map<String, String> mergeDocuments(ForeignPK originalForeignKey, ForeignPK cloneForeignKey,
      DocumentType type) {
    return mock.mergeDocuments(originalForeignKey, cloneForeignKey, type);
  }

  @Override
  public SimpleDocumentPK copyDocument(SimpleDocument original, ForeignPK targetPk) {
    return mock.copyDocument(original, targetPk);
  }

  @Override
  public List<SimpleDocumentPK> copyAllDocuments(final WAPrimaryKey resourceSourcePk,
      final WAPrimaryKey targetDestinationPk) {
    return mock.copyAllDocuments(resourceSourcePk, targetDestinationPk);
  }

  @Override
  public SimpleDocumentPK moveDocument(SimpleDocument document, ForeignPK destination) {
    return mock.moveDocument(document, destination);
  }

  @Override
  public List<SimpleDocumentPK> moveAllDocuments(final WAPrimaryKey resourceSourcePk,
      final WAPrimaryKey targetDestinationPk) {
    return mock.moveAllDocuments(resourceSourcePk, targetDestinationPk);
  }

  @Override
  public SimpleDocument createAttachment(SimpleDocument document, InputStream content) throws
      AttachmentException {
    return mock.createAttachment(document, content);
  }

  @Override
  public SimpleDocument createAttachment(SimpleDocument document, InputStream content,
      boolean indexIt) {
    return mock.createAttachment(document, content, indexIt);
  }

  @Override
  public SimpleDocument createAttachment(SimpleDocument document, InputStream content,
      boolean indexIt, boolean invokeCallback) {
    return mock.createAttachment(document, content, indexIt, invokeCallback);
  }

  @Override
  public SimpleDocument createAttachment(SimpleDocument document, File content) throws
      AttachmentException {
    return mock.createAttachment(document, content);
  }

  @Override
  public SimpleDocument createAttachment(SimpleDocument document, File content, boolean indexIt) {
    return mock.createAttachment(document, content, indexIt);
  }

  @Override
  public SimpleDocument createAttachment(SimpleDocument document, File content, boolean indexIt,
      boolean invokeCallback) {
    return mock.createAttachment(document, content, indexIt, invokeCallback);
  }

  @Override
  public void createIndex(SimpleDocument document) {
    mock.createIndex(document);
  }

  @Override
  public void deleteIndex(SimpleDocument document) {
    mock.deleteIndex(document);
  }

  @Override
  public void createIndex(SimpleDocument document, Date startOfVisibilityPeriod,
      Date endOfVisibilityPeriod) {
    mock.createIndex(document, startOfVisibilityPeriod, endOfVisibilityPeriod);
  }

  @Override
  public void deleteAttachment(SimpleDocument document) {
    mock.deleteAttachment(document);
  }

  @Override
  public void deleteAllAttachments(final String resourceId, final String componentInstanceId) {
    mock.deleteAllAttachments(resourceId, componentInstanceId);
  }

  @Override
  public void deleteAttachment(SimpleDocument document, boolean invokeCallback) {
    mock.deleteAttachment(document, invokeCallback);
  }

  @Override
  public void removeContent(SimpleDocument document, String lang, boolean invokeCallback) {
    mock.removeContent(document, lang, invokeCallback);
  }

  @Override
  public void reorderAttachments(List<SimpleDocumentPK> pks) throws AttachmentException {
    mock.reorderAttachments(pks);
  }

  @Override
  public void reorderDocuments(List<SimpleDocument> documents) throws AttachmentException {
    mock.reorderDocuments(documents);
  }

  @Override
  public SimpleDocument searchDocumentById(SimpleDocumentPK primaryKey, String lang) {
    return mock.searchDocumentById(primaryKey, lang);
  }

  @Override
  public SimpleDocumentList<SimpleDocument> listDocumentsByForeignKey(WAPrimaryKey foreignKey,
      String lang) {
    return mock.listDocumentsByForeignKey(foreignKey, lang);
  }

  @Override
  public SimpleDocumentList<SimpleDocument> listAllDocumentsByForeignKey(WAPrimaryKey foreignKey,
      String lang) {
    return mock.listAllDocumentsByForeignKey(foreignKey, lang);
  }

  @Override
  public SimpleDocumentList<SimpleDocument> listDocumentsByForeignKeyAndType(
      WAPrimaryKey foreignKey, DocumentType type, String lang) {
    return mock.listDocumentsByForeignKeyAndType(foreignKey, type, lang);
  }

  @Override
  public void unindexAttachmentsOfExternalObject(WAPrimaryKey foreignKey) {
    mock.unindexAttachmentsOfExternalObject(foreignKey);
  }

  @Override
  public void updateAttachment(SimpleDocument document, boolean indexIt, boolean invokeCallback) {
    mock.updateAttachment(document, indexIt, invokeCallback);
  }

  @Override
  public void updateAttachment(SimpleDocument document, File content, boolean indexIt,
      boolean invokeCallback) {
    mock.updateAttachment(document, content, indexIt, invokeCallback);
  }

  @Override
  public void updateAttachment(SimpleDocument document, InputStream content, boolean indexIt,
      boolean invokeCallback) {
    mock.updateAttachment(document, content, indexIt, invokeCallback);
  }

  @Override
  public List<SimpleDocument> listDocumentsRequiringWarning(Date alertDate, String language) {
    return mock.listDocumentsRequiringWarning(alertDate, language);
  }

  @Override
  public List<SimpleDocument> listExpiringDocuments(Date alertDate, String language) {
    return mock.listExpiringDocuments(alertDate, language);
  }

  @Override
  public List<SimpleDocument> listDocumentsToUnlock(Date expiryDate, String language) {
    return mock.listDocumentsToUnlock(expiryDate, language);
  }

  @Override
  public boolean lock(String attachmentId, String userId, String language) {
    return mock.lock(attachmentId, userId, language);
  }

  @Override
  public boolean unlock(UnlockContext context) {
    return mock.unlock(context);
  }

  @Override
  public SimpleDocumentPK changeVersionState(SimpleDocumentPK pk, String comment) {
    return mock.changeVersionState(pk, comment);
  }

  @Override
  public SimpleDocument findExistingDocument(SimpleDocumentPK pk, String fileName, ForeignPK foreign,
      String lang) {
    return mock.findExistingDocument(pk, fileName, foreign, lang);
  }

  @Override
  public List<SimpleDocument> listDocumentsLockedByUser(String usedId, String language) {
    return mock.listDocumentsLockedByUser(usedId, language);
  }

  @Override
  public void updateIndexEntryWithDocuments(FullIndexEntry indexEntry) {
    mock.updateIndexEntryWithDocuments(indexEntry);
  }

  @Override
  public void indexAllDocuments(WAPrimaryKey fk, Date startOfVisibilityPeriod,
      Date endOfVisibilityPeriod) {
    mock.indexAllDocuments(fk, startOfVisibilityPeriod, endOfVisibilityPeriod);
  }

  @Override
  public void switchComponentBehaviour(String componentId, boolean toVersionning) {
    mock.switchComponentBehaviour(componentId, toVersionning);
  }

  @Override
  public void switchAllowingDownloadForReaders(SimpleDocumentPK pk, boolean allowing) {
    mock.switchAllowingDownloadForReaders(pk, allowing);
  }

}
