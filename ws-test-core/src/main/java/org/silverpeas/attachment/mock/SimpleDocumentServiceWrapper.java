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
package org.silverpeas.attachment.mock;

import com.silverpeas.annotation.Service;
import com.silverpeas.util.Default;
import com.silverpeas.util.ForeignPK;
import com.stratelia.webactiv.util.WAPrimaryKey;
import org.mockito.Mockito;
import org.silverpeas.attachment.AttachmentException;
import org.silverpeas.attachment.AttachmentService;
import org.silverpeas.attachment.model.DocumentType;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.attachment.model.UnlockContext;
import org.silverpeas.attachment.util.SimpleDocumentList;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;

import javax.inject.Named;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Default
@Service
@Named("simpleDocumentService")
public class SimpleDocumentServiceWrapper implements AttachmentService {

  private AttachmentService realService;

  public SimpleDocumentServiceWrapper() {
    this.realService = Mockito.mock(AttachmentService.class);
  }

  public SimpleDocumentServiceWrapper(AttachmentService realService) {
    this.realService = realService;
  }

  public AttachmentService getRealService() {
    return realService;
  }

  public void setRealService(AttachmentService realService) {
    this.realService = realService;
  }

  @Override
  public void deleteAllAttachments(final String componentInstanceId) throws AttachmentException {
    realService.deleteAllAttachments(componentInstanceId);
  }

  @Override
  public void updateAttachment(SimpleDocument document, InputStream in, boolean indexIt,
      boolean invokeCallback) {
    realService.updateAttachment(document, in, indexIt, invokeCallback);
  }

  @Override
  public void getBinaryContent(OutputStream output, SimpleDocumentPK pk, String lang) {
    realService.getBinaryContent(output, pk, lang);
  }

  @Override
  public void getBinaryContent(final OutputStream output, final SimpleDocumentPK pk,
      final String lang, final long contentOffset, final long contentLength) {
    realService.getBinaryContent(output, pk, lang, contentOffset, contentLength);
  }

  @Override
  public void addXmlForm(SimpleDocumentPK pk, String language, String xmlFormName) {
    realService.addXmlForm(pk, language, xmlFormName);
  }

  @Override
  public SimpleDocumentPK cloneDocument(SimpleDocument original, String foreignCloneId) {
    return realService.cloneDocument(original, foreignCloneId);
  }

  @Override
  public Map<String, String> mergeDocuments(ForeignPK originalForeignKey, ForeignPK cloneForeignKey,
      DocumentType type) {
    return realService.mergeDocuments(originalForeignKey, cloneForeignKey, type);
  }

  @Override
  public SimpleDocument createAttachment(SimpleDocument document, InputStream content) throws
      AttachmentException {
    return realService.createAttachment(document, content);
  }

  @Override
  public SimpleDocument createAttachment(SimpleDocument document, InputStream content,
      boolean indexIt) {
    return realService.createAttachment(document, content, indexIt);
  }

  @Override
  public SimpleDocument createAttachment(SimpleDocument document, InputStream content,
      boolean indexIt, boolean invokeCallback) {
    return realService.createAttachment(document, content, indexIt, invokeCallback);
  }

  @Override
  public void createIndex(SimpleDocument document) {
    realService.createIndex(document);
  }

  @Override
  public void createIndex(SimpleDocument document, Date startOfVisibilityPeriod,
      Date endOfVisibilityPeriod) {
    realService.createIndex(document, startOfVisibilityPeriod, endOfVisibilityPeriod);
  }

  @Override
  public void deleteAttachment(SimpleDocument document) {
    realService.deleteAttachment(document);
  }

  @Override
  public void deleteAttachment(SimpleDocument document, boolean invokeCallback) {
    realService.deleteAttachment(document, invokeCallback);
  }

  @Override
  public void removeContent(SimpleDocument document, String lang, boolean invokeCallback) {
    realService.removeContent(document, lang, invokeCallback);
  }

  @Override
  public void reorderAttachments(List<SimpleDocumentPK> pks) throws AttachmentException {
    realService.reorderAttachments(pks);
  }

  @Override
  public SimpleDocument searchDocumentById(SimpleDocumentPK primaryKey, String lang) {
    return realService.searchDocumentById(primaryKey, lang);
  }

  @Override
  public SimpleDocumentList<SimpleDocument> listDocumentsByForeignKey(WAPrimaryKey foreignKey,
      String lang) {
    return realService.listDocumentsByForeignKey(foreignKey, lang);
  }

  @Override
  public void unindexAttachmentsOfExternalObject(WAPrimaryKey foreignKey) {
    realService.unindexAttachmentsOfExternalObject(foreignKey);
  }

  @Override
  public void updateAttachment(SimpleDocument document, boolean indexIt, boolean invokeCallback) {
    realService.updateAttachment(document, indexIt, invokeCallback);
  }

  @Override
  public void updateIndexEntryWithDocuments(FullIndexEntry indexEntry) {
    realService.updateIndexEntryWithDocuments(indexEntry);
  }

  @Override
  public List<SimpleDocument> listDocumentsRequiringWarning(Date alertDate, String language) {
    return realService.listDocumentsRequiringWarning(alertDate, language);
  }

  @Override
  public List<SimpleDocument> listExpiringDocuments(Date alertDate, String language) {
    return realService.listExpiringDocuments(alertDate, language);
  }

  @Override
  public List<SimpleDocument> listDocumentsToUnlock(Date expiryDate, String language) {
    return realService.listDocumentsToUnlock(expiryDate, language);
  }

  @Override
  public void updateAttachment(SimpleDocument document, File content, boolean indexIt,
      boolean invokeCallback) {
    realService.updateAttachment(document, content, indexIt, invokeCallback);
  }

  @Override
  public void getBinaryContent(File file, SimpleDocumentPK pk, String lang) {
    realService.getBinaryContent(file, pk, lang);
  }

  @Override
  public SimpleDocument createAttachment(SimpleDocument document, File content) throws
      AttachmentException {
    return realService.createAttachment(document, content);
  }

  @Override
  public SimpleDocument createAttachment(SimpleDocument document, File content, boolean indexIt) {
    return realService.createAttachment(document, content, indexIt);
  }

  @Override
  public SimpleDocument createAttachment(SimpleDocument document, File content, boolean indexIt,
      boolean invokeCallback) {
    return realService.createAttachment(document, content, indexIt, invokeCallback);
  }

  @Override
  public void reorderDocuments(List<SimpleDocument> documents) throws AttachmentException {
    realService.reorderDocuments(documents);
  }

  @Override
  public boolean lock(String attachmentId, String userId, String language) {
    return realService.lock(attachmentId, userId, language);
  }

  @Override
  public boolean unlock(UnlockContext context) {
    return realService.unlock(context);
  }

  @Override
  public SimpleDocumentPK changeVersionState(SimpleDocumentPK pk, String comment) {
    return realService.changeVersionState(pk, comment);
  }

  @Override
  public SimpleDocument findExistingDocument(SimpleDocumentPK pk, String fileName, ForeignPK foreign,
      String lang) {
    return realService.findExistingDocument(pk, fileName, foreign, lang);
  }

  @Override
  public SimpleDocumentList<SimpleDocument> listDocumentsByForeignKeyAndType(
      WAPrimaryKey foreignKey, DocumentType type, String lang) {
    return realService.listDocumentsByForeignKeyAndType(foreignKey, type, lang);
  }

  @Override
  public SimpleDocumentPK copyDocument(SimpleDocument original, ForeignPK targetPk) {
    return realService.copyDocument(original, targetPk);
  }

  @Override
  public List<SimpleDocument> listDocumentsLockedByUser(String usedId, String language) {
    return realService.listDocumentsLockedByUser(usedId, language);
  }

  @Override
  public SimpleDocumentPK moveDocument(SimpleDocument document, ForeignPK destination) {
    return realService.moveDocument(document, destination);
  }

  @Override
  public SimpleDocumentList<SimpleDocument> listAllDocumentsByForeignKey(WAPrimaryKey foreignKey,
      String lang) {
    return realService.listAllDocumentsByForeignKey(foreignKey, lang);
  }

  @Override
  public void indexAllDocuments(WAPrimaryKey fk, Date startOfVisibilityPeriod,
      Date endOfVisibilityPeriod) {
    realService.indexAllDocuments(fk, startOfVisibilityPeriod, endOfVisibilityPeriod);
  }

  @Override
  public void deleteIndex(SimpleDocument document) {
    realService.deleteIndex(document);
  }

  @Override
  public void switchComponentBehaviour(String componentId, boolean toVersionning) {
    realService.switchComponentBehaviour(componentId, toVersionning);
  }

  @Override
  public void switchAllowingDownloadForReaders(final SimpleDocumentPK pk, final boolean allowing) {
    realService.switchAllowingDownloadForReaders(pk, allowing);
  }
}
