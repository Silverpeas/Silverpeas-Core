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
package org.silverpeas.attachment.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;

import org.silverpeas.attachment.AttachmentException;
import org.silverpeas.attachment.AttachmentService;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.attachment.model.UnlockContext;
import org.silverpeas.search.indexEngine.model.FullIndexEntry;

import com.silverpeas.util.MimeTypes;

import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 *
 * @author ehugonnet
 */
public class MockBinaryAttachmentService implements AttachmentService {

  @Override
  public void addContent(SimpleDocument document, InputStream content, boolean indexIt,
      boolean invokeCallback) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void addContent(SimpleDocument document, File content, boolean indexIt,
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
  public void addXmlForm(SimpleDocumentPK pk, String language, String xmlFormName) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public SimpleDocumentPK cloneDocument(SimpleDocument original, String foreignCloneId) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public SimpleDocument createAttachment(SimpleDocument document, InputStream content) throws AttachmentException {
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
  public SimpleDocument createAttachment(SimpleDocument document, File content) throws AttachmentException {
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
  public SimpleDocument searchAttachmentById(SimpleDocumentPK primaryKey, String lang) {
    SimpleDocument doc =  new SimpleDocument();
    doc.setFile(new SimpleAttachment());
    doc.setFilename("Test.pdf");
    doc.setSize("Ceci est un test et ca marche".length());
    doc.setContentType(MimeTypes.PDF_MIME_TYPE);
    return doc;
  }

  @Override
  public List<SimpleDocument> searchAttachmentsByExternalObject(WAPrimaryKey foreignKey, String lang) {
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
  public void updateIndexEntryWithAttachments(FullIndexEntry indexEntry) {
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
  public void changeVersionState(SimpleDocumentPK pk) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
}
