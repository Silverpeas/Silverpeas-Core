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
package org.silverpeas.core.contribution.attachment.web;

import org.silverpeas.util.ForeignPK;
import org.silverpeas.util.MimeTypes;
import org.silverpeas.util.WAPrimaryKey;
import org.apache.commons.io.IOUtils;
import org.silverpeas.core.contribution.attachment.AttachmentException;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.model.UnlockContext;
import org.silverpeas.core.contribution.attachment.util.SimpleDocumentList;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ehugonnet
 */
public class MockBinaryAttachmentService implements AttachmentService {

  @Override
  public void deleteAllAttachments(final String componentInstanceId) throws AttachmentException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateAttachment(SimpleDocument document, InputStream content, boolean indexIt,
      boolean invokeCallback) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateAttachment(SimpleDocument document, File content, boolean indexIt,
      boolean invokeCallback) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void getBinaryContent(File file, SimpleDocumentPK pk, String lang) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void getBinaryContent(OutputStream output, SimpleDocumentPK pk, String lang) {
    try {
      IOUtils.write("Ceci est un test et ca marche", output);
    } catch (IOException ex) {
      Logger.getLogger(MockBinaryAttachmentService.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public void getBinaryContent(final OutputStream output, final SimpleDocumentPK pk,
      final String lang, final long contentOffset, final long contentLength) {
    try {
      IOUtils.write("Ceci est un test et ca ne marche pas", output);
    } catch (IOException ex) {
      Logger.getLogger(MockBinaryAttachmentService.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public void addXmlForm(SimpleDocumentPK pk, String language, String xmlFormName) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public SimpleDocumentPK cloneDocument(SimpleDocument original, String foreignCloneId) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public SimpleDocument createAttachment(SimpleDocument document, InputStream content) throws
      AttachmentException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public SimpleDocument createAttachment(SimpleDocument document, InputStream content,
      boolean indexIt) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public SimpleDocument createAttachment(SimpleDocument document, InputStream content,
      boolean indexIt, boolean invokeCallback) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public SimpleDocument createAttachment(SimpleDocument document, File content) throws
      AttachmentException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public SimpleDocument createAttachment(SimpleDocument document, File content, boolean indexIt) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public SimpleDocument createAttachment(SimpleDocument document, File content, boolean indexIt,
      boolean invokeCallback) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void createIndex(SimpleDocument document) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void createIndex(SimpleDocument document, Date startOfVisibilityPeriod,
      Date endOfVisibilityPeriod) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void deleteAttachment(SimpleDocument document) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void deleteAllAttachments(final String resourceId, final String componentInstanceId) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void deleteAttachment(SimpleDocument document, boolean invokeCallback) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void removeContent(SimpleDocument document, String lang, boolean invokeCallback) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void reorderAttachments(List<SimpleDocumentPK> pks) throws AttachmentException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void reorderDocuments(List<SimpleDocument> documents) throws AttachmentException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public SimpleDocument searchDocumentById(SimpleDocumentPK primaryKey, String lang) {
    SimpleDocument doc = new SimpleDocument();
    doc.setAttachment(new SimpleAttachment());
    doc.setFilename("Test.pdf");
    doc.setSize("Ceci est un test et ca marche".length());
    doc.setContentType(MimeTypes.PDF_MIME_TYPE);
    return doc;
  }

  @Override
  public SimpleDocumentList<SimpleDocument> listDocumentsByForeignKey(WAPrimaryKey foreignKey,
      String lang) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void unindexAttachmentsOfExternalObject(WAPrimaryKey foreignKey) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateAttachment(SimpleDocument document, boolean indexIt, boolean invokeCallback) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void updateIndexEntryWithDocuments(FullIndexEntry indexEntry) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<SimpleDocument> listDocumentsRequiringWarning(Date alertDate, String language) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<SimpleDocument> listExpiringDocuments(Date alertDate, String language) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<SimpleDocument> listDocumentsToUnlock(Date expiryDate, String language) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean lock(String attachmentId, String userId, String language) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean unlock(UnlockContext context) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public SimpleDocumentPK changeVersionState(SimpleDocumentPK pk, String comment) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public SimpleDocument findExistingDocument(SimpleDocumentPK pk, String fileName, ForeignPK foreign,
      String lang) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public SimpleDocumentList<SimpleDocument> listDocumentsByForeignKeyAndType(
      WAPrimaryKey foreignKey, DocumentType type, String lang) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public SimpleDocumentPK copyDocument(SimpleDocument original, ForeignPK targetPk) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<SimpleDocumentPK> copyAllDocuments(final WAPrimaryKey resourceSourcePk,
      final WAPrimaryKey targetDestinationPk) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<SimpleDocument> listDocumentsLockedByUser(String usedId, String language) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public List<SimpleDocumentPK> moveAllDocuments(final WAPrimaryKey resourceSourcePk,
      final WAPrimaryKey targetDestinationPk) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public SimpleDocumentPK moveDocument(SimpleDocument document, ForeignPK destination) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public SimpleDocumentList<SimpleDocument> listAllDocumentsByForeignKey(WAPrimaryKey foreignKey,
      String lang) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void indexAllDocuments(WAPrimaryKey fk, Date startOfVisibilityPeriod,
      Date endOfVisibilityPeriod) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void deleteIndex(SimpleDocument document) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Map<String, String> mergeDocuments(ForeignPK originalForeignKey, ForeignPK cloneForeignKey,
      DocumentType type) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void switchComponentBehaviour(String componentId, boolean toVersionning) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void switchAllowingDownloadForReaders(final SimpleDocumentPK pk, final boolean allowing) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
