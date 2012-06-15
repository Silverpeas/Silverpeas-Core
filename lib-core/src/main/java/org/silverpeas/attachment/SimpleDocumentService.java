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

import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordSet;
import com.silverpeas.jcrutil.BasicDaoFactory;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.attachment.control.RepositoryHelper;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import javax.inject.Named;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.attachment.repository.DocumentRepository;

import static javax.jcr.Property.JCR_CONTENT;
import static javax.jcr.Property.JCR_DATA;

/**
 *
 * @author ehugonnet
 */
@Named("simpleDocumentService")
public class SimpleDocumentService implements AttachmentService {

  private final ResourceLocator resources = new ResourceLocator(
      "com.stratelia.webactiv.util.attachment.Attachment", "");
  private final DocumentRepository repository = new DocumentRepository();

  @Override
  public void createIndex(SimpleDocument document) {
    createIndex(document, null, null);
  }

  @Override
  public void createIndex(SimpleDocument document, Date startOfVisibility, Date endOfVisibility) {
    String language = document.getLanguage();
    if (!StringUtil.isDefined(language)) {
      language = I18NHelper.defaultLanguage;
    }
    String objectType = "Attachment" + document.getId();
    if (I18NHelper.isI18N) {
      objectType += "_" + language;
    }
    FullIndexEntry indexEntry = new FullIndexEntry(document.getInstanceId(), objectType, document.
        getForeignId());
    indexEntry.setLang(language);
    indexEntry.setCreationDate(document.getCreated());
    indexEntry.setCreationUser(document.getCreatedBy());
    if (startOfVisibility != null) {
      indexEntry.setStartDate(DateUtil.date2SQLDate(startOfVisibility));
    }
    if (endOfVisibility != null) {
      indexEntry.setEndDate(DateUtil.date2SQLDate(endOfVisibility));
    }
    indexEntry.setTitle(document.getTitle(), language);
    String title = document.getTitle();
    if (StringUtil.isDefined(title)) {
      indexEntry.setKeywords(title, language);
    }
    indexEntry.addFileContent(document.getAttachmentPath(), CharEncoding.UTF_8, document.
        getContentType(), language);

    if (StringUtil.isDefined(document.getXmlFormId())) {
      updateIndexEntryWithXMLFormContent(document.getPk(), document.getXmlFormId(), indexEntry);
    }
    IndexEngineProxy.addIndexEntry(indexEntry);
  }

  private void updateIndexEntryWithXMLFormContent(SimpleDocumentPK pk, String xmlFormName,
      FullIndexEntry indexEntry) {
    SilverTrace.info("attachment", "AttachmentController.updateIndexEntryWithXMLFormContent()",
        "root.MSG_GEN_ENTER_METHOD", "indexEntry = " + indexEntry.toString());
    try {
      String objectType = "Attachment";
      PublicationTemplate pub = PublicationTemplateManager.getInstance().
          getPublicationTemplate(indexEntry.getComponent() + ":" + objectType + ":" + xmlFormName);
      RecordSet set = pub.getRecordSet();
      set.indexRecord(pk.getId(), xmlFormName, indexEntry);
    } catch (PublicationTemplateException e) {
      SilverTrace.error("attachment",
          "AttachmentController.updateIndexEntryWithXMLFormContent()", "", e);
    } catch (FormException e) {
      SilverTrace.error("attachment",
          "AttachmentController.updateIndexEntryWithXMLFormContent()", "", e);
    }
  }

  /**
   *
   * @param document
   * @param lang
   */
  private void deleteIndex(SimpleDocument document, String lang) {
    SilverTrace.debug("attachment", "DocumentService.deleteIndex", "root.MSG_GEN_ENTER_METHOD",
        document.getId());
    String language = lang;
    if (language == null) {
      language = I18NHelper.defaultLanguage;
    }
    String objectType = "Attachment" + document.getId() + '_' + language;
    IndexEntryPK indexEntry = new IndexEntryPK(document.getInstanceId(), objectType, document.
        getForeignId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  @Override
  public void unindexAttachmentsOfExternalObject(WAPrimaryKey foreignKey) {
    Session session = null;
    try {
      List<SimpleDocument> docs = repository.listDocumentsByForeignId(session, foreignKey.
          getInstanceId(), foreignKey.getId(), null);
      for (SimpleDocument doc : docs) {
        deleteIndex(doc, null);
      }
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public void updateIndexEntryWithAttachments(FullIndexEntry indexEntry) {
    if (resources.getBoolean("attachment.index.incorporated", true)) {
      if (!indexEntry.getObjectType().startsWith("Attachment")) {
        WAPrimaryKey pk = new ForeignPK(indexEntry.getObjectId(), indexEntry.getComponent());
        List<SimpleDocument> attachments = searchAttachmentsByExternalObject(pk, indexEntry.
            getLang());
        for (SimpleDocument attachment : attachments) {
          indexEntry.addFileContent(attachment.getAttachmentPath(), CharEncoding.UTF_8,
              attachment.getContentType(), indexEntry.getLang());
        }
      }
    }
  }

  @Override
  public void addXmlForm(SimpleDocumentPK pk, String language, String xmlFormName) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      SimpleDocument doc = repository.findDocumentById(session, pk, language);
      doc.setXmlFormId(xmlFormName);
      repository.updateDocument(session, doc);
      session.save();
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

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
  @Override
  public SimpleDocument createAttachment(SimpleDocument document, InputStream content) throws
      AttachmentException {
    return createAttachment(document, content, true);
  }

  /**
   * Create file attached to an object who is identified by the foreignId.
   *
   * @param document the document to be created.
   * @param content the binary content of the document.
   * @param indexIt<code>true</code> if the document is to be indexed,  <code>false</code>
   * otherwhise.
   * @return the stored document.
   */
  @Override
  public SimpleDocument createAttachment(SimpleDocument document, InputStream content,
      boolean indexIt) {
    return createAttachment(document, content, indexIt, true);
  }

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
  @Override
  public SimpleDocument createAttachment(SimpleDocument document, InputStream content,
      boolean indexIt, boolean invokeCallback) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      SimpleDocumentPK docPk = repository.createDocument(session, document, content);
      SimpleDocument currentDocument = repository.findDocumentById(session, docPk, document.
          getLanguage());
      storeContent(session, currentDocument);
      if (invokeCallback && StringUtil.isDefined(currentDocument.getCreatedBy())) {
        CallBackManager callBackManager = CallBackManager.get();
        callBackManager.invoke(CallBackManager.ACTION_ATTACHMENT_ADD, Integer.
            parseInt(currentDocument.getCreatedBy()), currentDocument.getInstanceId(),
            currentDocument.getForeignId());
      }
      session.save();
      if (indexIt) {
        createIndex(currentDocument);
      }
      return currentDocument;
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } catch (IOException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  private void storeContent(Session session, SimpleDocument document) throws RepositoryException,
      IOException {
    File file = new File(document.getAttachmentPath());
    Node docNode = session.getNodeByIdentifier(document.getId());
    String fileNodeName = SimpleDocument.FILE_PREFIX + document.getLanguage();
    if (docNode.hasNode(fileNodeName)) {
      Node fileNode = docNode.getNode(fileNodeName);
      if (fileNode.hasNode(JCR_CONTENT)) {
        Node contentNode = fileNode.getNode(JCR_CONTENT);
        Binary binary = contentNode.getProperty(JCR_DATA).getBinary();
        FileUtils.copyInputStreamToFile(binary.getStream(), file);
        binary.dispose();
      }
    }
  }
  


  /**
   * Delete a given attachment.
   *
   * @param document the document to deleted.
   */
  @Override
  public void deleteAttachment(SimpleDocument document) {
    deleteAttachment(document, true);
  }

  /**
   * Delete a given attachment.
   *
   * @param attachmentDetail the attachmentDetail object to deleted.
   * @param invokeCallback   <code>true</code> if the callback methods of the components must be
   * called, <code>false</code> for ignoring thoose callbacks.
   * @throws AttachmentRuntimeException if the attachement cannot be deleted.
   */
  @Override
  public void deleteAttachment(SimpleDocument document, boolean invokeCallback) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      repository.deleteDocument(session, document.getPk());
      FileUtils.deleteQuietly(new File(document.getAttachmentPath()));
      for (String lang : I18NHelper.getAllSupportedLanguages()) {
        deleteIndex(document, lang);
      }
      if (document.isOpenOfficeCompatible() && !document.isReadOnly()) {
        RepositoryHelper.getJcrAttachmentService().
            deleteAttachment(document, document.getLanguage());
      }
      if (invokeCallback) {
        int authorId = -1;
        if (StringUtil.isDefined(document.getCreatedBy())) {
          authorId = Integer.parseInt(document.getCreatedBy());
        }
        CallBackManager callBackManager = CallBackManager.get();
        callBackManager.invoke(CallBackManager.ACTION_ATTACHMENT_REMOVE, authorId, document.
            getInstanceId(), document);
      }
      session.save();
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public SimpleDocument searchAttachmentById(SimpleDocumentPK primaryKey, String lang) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      if (StringUtil.isDefined(primaryKey.getId())) {
        return repository.findDocumentById(session, primaryKey, lang);
      }
      return repository.findDocumentByOldSilverpeasId(session, primaryKey.getComponentName(),
          primaryKey.getOldSilverpeasId(), false, lang);
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public List<SimpleDocument> searchAttachmentsByExternalObject(WAPrimaryKey foreignKey, String lang) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      return repository.listDocumentsByForeignId(session, foreignKey.getInstanceId(), foreignKey.
          getId(), lang);
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public void updateAttachment(SimpleDocument document, boolean indexIt, boolean invokeCallback) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      SimpleDocument oldAttachment = repository.findDocumentById(session, document.getPk(),
          document.getLanguage());

      String language = document.getLanguage();
      repository.updateDocument(session, document);
      if (document.isOpenOfficeCompatible() && document.isReadOnly()) {
        // le fichier est renommé
        if (!oldAttachment.getFilename().equals(document.getFilename())) {
          RepositoryHelper.getJcrAttachmentService().deleteAttachment(oldAttachment, language);
          RepositoryHelper.getJcrAttachmentService().createAttachment(document, language);
        } else {
          RepositoryHelper.getJcrAttachmentService().updateNodeAttachment(document,
              document.getLanguage());
        }
      }
      String userId = document.getCreatedBy();
      if ((userId != null) && (userId.length() > 0) && invokeCallback) {
        CallBackManager callBackManager = CallBackManager.get();
        callBackManager.invoke(CallBackManager.ACTION_ATTACHMENT_UPDATE, Integer.parseInt(userId),
            document.getInstanceId(), document.getForeignId());
      }
      if (indexIt) {
        createIndex(document);
      }
      session.save();
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public void addContent(SimpleDocument document, InputStream in, boolean indexIt,
      boolean invokeCallback) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      repository.addContent(session, document.getPk(), document.getFile(), in);
      storeContent(session, document);
      if (document.isOpenOfficeCompatible() && document.isReadOnly()) {
        RepositoryHelper.getJcrAttachmentService().updateNodeAttachment(document,
            document.getLanguage());
      }
      String userId = document.getCreatedBy();
      if ((userId != null) && (userId.length() > 0) && invokeCallback) {
        CallBackManager callBackManager = CallBackManager.get();
        callBackManager.invoke(CallBackManager.ACTION_ATTACHMENT_UPDATE, Integer.parseInt(userId),
            document.getInstanceId(), document.getForeignId());
      }
      if (indexIt) {
        createIndex(document);
      }
      session.save();
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } catch (IOException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public void removeContent(SimpleDocument document, String lang, boolean invokeCallback) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      repository.removeContent(session, document.getPk(), lang);
      FileUtils.deleteQuietly(new File(document.getDirectoryPath(lang)));
      if (document.isOpenOfficeCompatible() && document.isReadOnly()) {
        RepositoryHelper.getJcrAttachmentService().
            deleteAttachment(document, document.getLanguage());
      }
      String userId = document.getCreatedBy();
      if ((userId != null) && (userId.length() > 0) && invokeCallback) {
        CallBackManager callBackManager = CallBackManager.get();
        callBackManager.invoke(CallBackManager.ACTION_ATTACHMENT_UPDATE, Integer.parseInt(userId),
            document.getInstanceId(), document.getForeignId());
      }
      deleteIndex(document, document.getLanguage());
      session.save();
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }
  /* public void moveDownAttachment(AttachmentDetail attachDetail) {

   try {
   AttachmentDetail next = attachmentBm.findNext(attachDetail);

   if (next != null) {
   int stockNum = next.getOrderNum();
   next.setOrderNum(attachDetail.getOrderNum());
   attachDetail.setOrderNum(stockNum);
   attachmentBm.updateAttachment(next);
   attachmentBm.updateAttachment(attachDetail);
   }
   } catch (Exception e) {
   throw new AttachmentRuntimeException(
   "AttachmentController.moveDownAttachment()",
   SilverpeasRuntimeException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
   e);
   }
   }

   public void moveUpAttachment(AttachmentDetail attachDetail) {

   try {
   AttachmentDetail prev = attachmentBm.findPrevious(attachDetail);

   if (prev != null) {
   int stockNum = prev.getOrderNum();
   prev.setOrderNum(attachDetail.getOrderNum());
   attachDetail.setOrderNum(stockNum);
   attachmentBm.updateAttachment(prev);
   attachmentBm.updateAttachment(attachDetail);
   }
   } catch (Exception e) {
   throw new AttachmentRuntimeException(
   "AttachmentController.moveUpAttachment()",
   SilverpeasRuntimeException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
   e);
   }
   }

   public void moveAttachments(ForeignPK fromPK, ForeignPK toPK,
   boolean indexIt) throws AttachmentException {
   SilverTrace.debug("attachment", "AttachmentController.moveAttachments",
   "root.MSG_GEN_ENTER_METHOD", "fromPK = " + fromPK.toString()
   + ", toPK = " + toPK.toString() + ", indexIt = " + indexIt);

   String toAbsolutePath = FileRepositoryManager.getAbsolutePath(toPK.getInstanceId());
   String fromAbsolutePath = FileRepositoryManager.getAbsolutePath(fromPK.getInstanceId());

   // First, remove existing index
   unindexAttachmentsByForeignKey(fromPK);

   AttachmentPK pk = new AttachmentPK(fromPK.getId(), fromPK.getInstanceId());
   Vector<AttachmentDetail> attachments = attachmentBm.getAttachmentsByForeignKey(pk);

   if (attachments != null) {
   SilverTrace.debug("attachment", "AttachmentController.moveAttachments",
   "root.MSG_GEN_PARAM_VALUE", "# of attachments to move = " + attachments.size());
   }
   for (int a = 0; (attachments != null) && (a < attachments.size()); a++) {
   AttachmentDetail attachment = attachments.get(a);

   // move file on disk
   File fromFile = new File(fromAbsolutePath + "Attachment" + File.separator
   + attachment.getContext() + File.separator + attachment.getPhysicalName());
   File toFile = new File(toAbsolutePath + "Attachment" + File.separator
   + attachment.getContext() + File.separator + attachment.getPhysicalName());

   SilverTrace.debug("attachment", "AttachmentController.moveAttachments",
   "root.MSG_GEN_PARAM_VALUE", "fromFile = " + fromFile.getPath()
   + ", toFile = " + toFile.getPath());

   // ensure directory exists
   String testPath = createPath(toPK.getInstanceId(), attachment.getContext());
   SilverTrace.debug("attachment", "AttachmentController.moveAttachments",
   "root.MSG_GEN_PARAM_VALUE", "path '" + testPath + "' exists !");

   if (fromFile != null) {
   SilverTrace.debug("attachment", "AttachmentController.moveAttachments",
   "root.MSG_GEN_PARAM_VALUE", "fromFile exists ? " + fromFile.exists());
   }

   boolean fileMoved = false;

   if (fromFile.exists()) {
   fileMoved = fromFile.renameTo(toFile);
   }

   if (fileMoved) {
   SilverTrace.debug("attachment", "AttachmentController.moveAttachments",
   "root.MSG_GEN_PARAM_VALUE", "file successfully moved");
   } else {
   SilverTrace.error("attachment", "AttachmentController.moveAttachments",
   "root.MSG_GEN_PARAM_VALUE", "file unsuccessfully moved ! from "
   + fromFile.getPath() + " to " + toFile.getPath());
   }

   // change foreignKey
   attachment.setForeignKey(toPK);
   attachmentBm.updateAttachment(attachment);

   SilverTrace.debug("attachment", "AttachmentController.moveAttachments",
   "root.MSG_GEN_PARAM_VALUE", "attachment updated in DB");
   if (attachment.getTranslations() != null) {
   Collection<Translation> translations = attachment.getTranslations().values();

   for (Translation translation : translations) {

   if (translation != null) {

   // move file on disk
   fromFile = new File(fromAbsolutePath + "Attachment"
   + File.separator + attachment.getContext() + File.separator
   + ((AttachmentDetailI18N) translation).getPhysicalName());
   toFile = new File(toAbsolutePath + "Attachment" + File.separator
   + attachment.getContext() + File.separator
   + ((AttachmentDetailI18N) translation).getPhysicalName());

   SilverTrace.debug("attachment",
   "AttachmentController.moveAttachments",
   "root.MSG_GEN_PARAM_VALUE", "move translation fromFile = "
   + fromFile.getPath() + ", toFile = " + toFile.getPath());

   if ((fromFile != null) && fromFile.exists()) {
   fromFile.renameTo(toFile);
   }
   }
   }
   }
   }

   if (indexIt) {
   // create index for attachments and translations
   attachmentIndexer(toPK);
   }
   }

   public Vector<AttachmentDetail> searchAttachmentByPKAndContext(WAPrimaryKey foreignKey,
   String context) {
   return searchAttachmentByPKAndContext(foreignKey, context, null);
   }
   */

  /**
   * to search all file attached by primary key of customer object and context of file attached
   *
   * @param pk : com.stratelia.webactiv.util.WAPrimaryKey: the primary key of customer object but
   * this key must be transformed to AttachmentPK
   * @param context : String: the context attribute of file attached
   * @return java.util.Vector, a vector of AttachmentDetail
   * @throws AttachmentRuntimeException when is impossible to search
   * @author Jean-Claude Groccia
   * @version 1.0
   * @see com.stratelia.webactiv.util.attachment.model.AttachmentDetail.
   */
// méthode pour wysiwig pb de gestion d'exception
 /* public Vector<AttachmentDetail> searchAttachmentByPKAndContext(WAPrimaryKey foreignKey,
   String context, Connection con) {
   AttachmentPK fk =
   new AttachmentPK(foreignKey.getId(), foreignKey.getSpace(), foreignKey.getComponentName());

   try {
   return attachmentBm.getAttachmentsByPKAndContext(fk, context, con);
   } catch (Exception fe) {
   throw new AttachmentRuntimeException(
   "AttachmentController.searchAttachmentByPKAndContext(WAPrimaryKey foreignKey, String context)",
   SilverpeasRuntimeException.ERROR, "root.EX_RECORD_NOT_FOUND", fe);
   }
   }
   */
  /**
   * to provide applicationIndexer service
   *
   * @param fk : com.stratelia.webactiv.util.WAPrimaryKey: the primary key of customer object
   * @return void
   * @author Jean-Claude Groccia
   * @version 1.0
   * @see com.stratelia.webactiv.util.attachment.model.AttachmentDetail.
   */
  /*public static void attachmentIndexer(WAPrimaryKey fk) {
   try {
   for (AttachmentDetail detail : searchAttachmentByCustomerPK(fk)) {
   createIndex(detail);
   }
   } catch (Exception fe) {
   throw new AttachmentRuntimeException(
   "AttachmentController.attachmentIndexer(WAPrimaryKey foreignKey, String context)",
   SilverpeasRuntimeException.ERROR, "root.EX_RECORD_NOT_FOUND", fe);
   }
   }

   public void attachmentIndexer(WAPrimaryKey foreignKey, Date startOfVisibilityPeriod,
   Date endOfVisibilityPeriod) {
   try {
   for (SimpleDocument doc : searchAttachmentByFor(fk)) {
   createIndex(doc, startOfVisibilityPeriod, endOfVisibilityPeriod);
   }
   } catch (Exception fe) {
   throw new AttachmentRuntimeException(
   "AttachmentController.attachmentIndexer(WAPrimaryKey foreignKey, String context)",
   SilverpeasRuntimeException.ERROR, "root.EX_RECORD_NOT_FOUND", fe);
   }
   }
   */
  /**
   * to delete all file attached to an customer object
   *
   * @param foreignKey : the primary key of customer object.
   * @return void
   * @throws AttachmentRuntimeException when is impossible to delete
   * @author Jean-Claude Groccia
   * @version 1.0
   * @see com.stratelia.webactiv.util.attachment.model.AttachmentDetail.
   */
  /*public void deleteAttachmentByCustomerPK(WAPrimaryKey foreignKey) {
   AttachmentPK fk = new AttachmentPK(foreignKey.getId(), foreignKey.getComponentName());
   Vector<AttachmentDetail> attachmentDetails = searchAttachmentByCustomerPK(fk);
   deleteAttachment(attachmentDetails);
   }

   public void deleteAttachmentsByCustomerPKAndContext(
   WAPrimaryKey foreignKey, String context) {
   Vector<AttachmentDetail> attachmentDetails = searchAttachmentByPKAndContext(foreignKey,
   context);
   deleteAttachment(attachmentDetails);
   }

   public void deleteWysiwygAttachmentByCustomerPK(WAPrimaryKey foreignKey) {
   AttachmentPK fk =
   new AttachmentPK(foreignKey.getId(), foreignKey.getSpace(), foreignKey.getComponentName());
   Vector<AttachmentDetail> attachmentDetails = searchAttachmentByCustomerPK(fk);

   // Astuce pour que seuls les attachements wysiwyg soit effacés
   int i = 0;

   while (i < attachmentDetails.size()) {
   AttachmentDetail attDetail = attachmentDetails.get(i);

   if (!((attDetail.getContext().charAt(0) >= '0') && (attDetail.getContext().charAt(
   0) <= '9'))) {
   attachmentDetails.remove(i);
   } else {
   i++;
   }
   }

   deleteAttachment(attachmentDetails);
   }
   */
  /*

  

   public static Hashtable<String, String> copyAttachmentByCustomerPKAndContext(
   WAPrimaryKey foreignKeyFrom, WAPrimaryKey foreignKeyTo, String context)
   throws AttachmentRuntimeException {
   SilverTrace.debug("attachment",
   "AttachmentController.copyAttachmentByCustomerPK",
   "root.MSG_GEN_ENTER_METHOD", "foreignKeyFrom = " + foreignKeyFrom
   + ", foreignKeyTo=" + foreignKeyTo);

   Vector<AttachmentDetail> attsToCopy = searchAttachmentByPKAndContext(foreignKeyFrom, context);

   return copyAttachments(attsToCopy, foreignKeyFrom, foreignKeyTo);
   }

   public Hashtable<String, String> copyAttachment(AttachmentDetail attToCopy,
   WAPrimaryKey foreignKeyFrom, WAPrimaryKey foreignKeyTo)
   throws AttachmentRuntimeException {
   Vector<AttachmentDetail> attsToCopy = new Vector<AttachmentDetail>();
   attsToCopy.add(attToCopy);
   return copyAttachments(attsToCopy, foreignKeyFrom, foreignKeyTo);
   }

   private Hashtable<String, String> copyAttachments(Vector<AttachmentDetail> attsToCopy,
   WAPrimaryKey foreignKeyFrom, WAPrimaryKey foreignKeyTo)
   throws AttachmentRuntimeException {
   SilverTrace.debug("attachment", "AttachmentController.copyAttachments",
   "root.MSG_GEN_ENTER_METHOD", "foreignKeyFrom = " + foreignKeyFrom
   + ", foreignKeyTo=" + foreignKeyTo);

   Hashtable<String, String> ids = new Hashtable<String, String>();

   if (attsToCopy != null) {
   AttachmentPK atPK = new AttachmentPK(null, foreignKeyTo.getSpace(),
   foreignKeyTo.getComponentName());
   String type = null;
   String physicalName = null;
   AttachmentDetail copy = null;

   for (AttachmentDetail attToCopy : attsToCopy) {

   copy =
   new AttachmentDetail(atPK, attToCopy.getPhysicalName(),
   attToCopy.getLogicalName(), attToCopy.getDescription(), attToCopy.getType(),
   attToCopy.getSize(), attToCopy.getContext(),
   attToCopy.getCreationDate(), foreignKeyTo, attToCopy.getTitle(),
   attToCopy.getInfo(), attToCopy.getOrderNum());

   if (!"link".equalsIgnoreCase(attToCopy.getDescription())) {

   // The file must be copied only if it's not a linked file
   // type =
   // attToCopy.getLogicalName().substring(attToCopy.getLogicalName().indexOf(".")+1,
   // attToCopy.getLogicalName().length());
   type = FileRepositoryManager.getFileExtension(attToCopy.getLogicalName());
   physicalName = Long.toString(System.currentTimeMillis()) + "." + type;
   copy.setPhysicalName(physicalName);

   copyFileOnServer(attToCopy, copy);
   }

   copy = createAttachment(copy);
   ids.put(attToCopy.getPK().getId(), copy.getPK().getId());

   // Copy translations
   Iterator translations = attToCopy.getTranslations().values().iterator();
   AttachmentDetailI18N translation = (AttachmentDetailI18N) translations.next(); // skip
   // default
   // attachment.
   // It has
   // been
   // copied
   // earlier.
   AttachmentDetail translationCopy = null;

   while (translations.hasNext()) {
   translation = (AttachmentDetailI18N) translations.next();

   translationCopy = new AttachmentDetail(copy.getPK(), "toDefine",
   translation.getLogicalName(), "", translation.getType(),
   translation.getSize(), copy.getContext(), copy.getCreationDate(),
   foreignKeyTo, translation.getTitle(), translation.getInfo(),
   attToCopy.getOrderNum());
   translationCopy.setLanguage(translation.getLanguage());

   type = FileRepositoryManager.getFileExtension(translation.getLogicalName());
   physicalName = java.lang.Long.toString(System.currentTimeMillis()) + "." + type;
   translationCopy.setPhysicalName(physicalName);

   attToCopy.setPhysicalName(translation.getPhysicalName());

   copyFileOnServer(attToCopy, translationCopy);

   updateAttachment(translationCopy);
   }
   }
   }

   return ids;
   }*/
  /**
   * to copy one file to another on server param attDetailFrom: type AttachmentDetail: the object
   * AttachmentDetail to copy param attDetailTo: type AttachmentDetail: the object AttachmentDetail
   * to create
   *
   * @author SCO
   * @version 1.0
   * @see com.stratelia.webactiv.util.attachment.model.AttachmentDetail
   */
  /*private void copyFileOnServer(AttachmentDetail attDetailFrom,
   AttachmentDetail attDetailTo) {
   String filePathFrom =
   FileRepositoryManager.getAbsolutePath(attDetailFrom.getPK().getComponentName(),
   FileRepositoryManager.getAttachmentContext(attDetailFrom.getContext()));
   String filePathTo =
   FileRepositoryManager.getAbsolutePath(attDetailTo.getPK().getComponentName(),
   FileRepositoryManager.getAttachmentContext(attDetailTo.getContext()));
   String fileNameFrom = attDetailFrom.getPhysicalName();
   String fileNameTo = attDetailTo.getPhysicalName();

   try {
   SilverTrace.debug("attachment", "AttachmentController.copyFileOnServer",
   "root.MSG_GEN_ENTER_METHOD", "From " + filePathFrom + fileNameFrom
   + " To " + filePathTo + fileNameTo);

   File directoryToTest = new File(filePathTo);

   if (!directoryToTest.exists()) {
   directoryToTest.mkdir();
   }

   FileRepositoryManager.copyFile(filePathFrom + fileNameFrom, filePathTo
   + fileNameTo);
   } catch (Exception e) {
   throw new AttachmentRuntimeException(
   "AttachmentController.copyFileOnServer()",
   SilverpeasRuntimeException.ERROR, "attachment_EX_NOT_COPY_FILE", e);
   }
   }*/
  /**
   * Checkin a file
   *
   * @param attachmentId
   * @param userId
   * @param upload : indicates if the file has been uploaded throught a form.
   * @param force if the user is an Admin he can force the release.
   * @param language the language for the attachment.
   * @return false if the file is locked - true if the checkin succeeded.
   * @throws AttachmentException
   */
  /* public boolean checkinFile(String attachmentId, String userId, boolean upload,
   boolean update, boolean force, String language)
   throws AttachmentException {
   try {
   SilverTrace.debug("attachment",
   "AttachmentController.checkinOfficeFile()",
   "root.MSG_GEN_ENTER_METHOD", "attachmentId = " + attachmentId);

   AttachmentDetail attachmentDetail = searchAttachmentByPK(new AttachmentPK(
   attachmentId));

   if (attachmentDetail.isOpenOfficeCompatible()
   && !force
   && RepositoryHelper.getJcrAttachmentService().isNodeLocked(
   attachmentDetail, language)) {
   SilverTrace.warn("attachment", "AttachmentController.checkinOfficeFile()",
   "attachment.NODE_LOCKED");
   return false;
   }
   if (!force && attachmentDetail.isReadOnly() && !attachmentDetail.getWorkerId().equals(
   userId)) {
   SilverTrace.warn("attachment", "AttachmentController.checkinOfficeFile()",
   "attachment.INCORRECT_USER");
   return false;
   }

   String componentId = attachmentDetail.getInstanceId();
   boolean invokeCallback = false;

   if (update || upload) {
   String workerId = attachmentDetail.getWorkerId();
   attachmentDetail.setCreationDate(null);
   attachmentDetail.setAuthor(workerId);
   invokeCallback = true;
   }

   if (upload) {
   String uploadedFile = FileRepositoryManager.getAbsolutePath(componentId)
   + CONTEXT_ATTACHMENTS + attachmentDetail.getPhysicalName(language);
   long newSize = FileRepositoryManager.getFileSize(uploadedFile);
   attachmentDetail.setSize(newSize);
   }

   if (attachmentDetail.isOpenOfficeCompatible() && !upload && update) {
   RepositoryHelper.getJcrAttachmentService().getUpdatedDocument(
   attachmentDetail, language);
   } else if (attachmentDetail.isOpenOfficeCompatible()
   && (upload || !update)) {
   RepositoryHelper.getJcrAttachmentService().deleteAttachment(
   attachmentDetail, language);
   }
   // Remove workerId from this attachment
   attachmentDetail.setWorkerId(null);
   attachmentDetail.setReservationDate(null);
   attachmentDetail.setAlertDate(null);
   attachmentDetail.setExpiryDate(null);
   updateAttachment(attachmentDetail, false, invokeCallback);
   } catch (Exception e) {
   SilverTrace.error("attachment", "AttachmentController.checkinOfficeFile()",
   "attachment.CHECKIN_FAILED", e);
   throw new AttachmentException("AttachmentController.checkinOfficeFile()",
   SilverpeasRuntimeException.ERROR, "attachment.CHECKIN_FAILED", e);
   }
   return true;
   }

   private static void addDays(Calendar calendar, int nbDay) {
   SilverTrace.debug("versioning", "addDays", "root.MSG_GEN_PARAM_VALUE",
   "nbDay = " + nbDay);

   int nb = 0;

   while (nb < nbDay) {
   SilverTrace.debug("versioning", "addDays", "root.MSG_GEN_PARAM_VALUE",
   "time = " + calendar.getTime());
   SilverTrace.debug("versioning", "addDays", "root.MSG_GEN_PARAM_VALUE",
   "nbDay = " + nbDay + " nb = " + nb);
   calendar.add(Calendar.DATE, 1);

   if ((calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY)
   && (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)) {
   nb += 1;
   }

   SilverTrace.debug("versioning", "addDays", "root.MSG_GEN_PARAM_VALUE",
   "time = " + calendar.getTime());
   }
   }*/
  /**
   * Checkout a file for update by user
   *
   * @param attachmentId
   * @param userId
   * @return false if the attahcment is already checkout - true if the attachment was successfully
   * checked out.
   * @throws AttachmentException
   */
  /* public boolean checkoutFile(String attachmentId, String userId)
   throws AttachmentException {
   return checkoutFile(attachmentId, userId, null);
   }*/
  /**
   * Checkout a file to be updated by user
   *
   * @param attachmentId
   * @param userId
   * @param language
   * @return false if the attachment is already checkout - true if the attachment was successfully
   * checked out.
   * @throws AttachmentException
   */
  /*  public boolean checkoutFile(String attachmentId, String userId,
   String language) throws AttachmentException {
   SilverTrace.debug("attachment", "AttachmentController.checkoutFile()",
   "root.MSG_GEN_ENTER_METHOD", "attachmentId = " + attachmentId + ", userId = " + userId);

   try {
   AttachmentDetail attachmentDetail = attachmentBm.getAttachmentByPrimaryKey(
   new AttachmentPK(attachmentId));
   if (attachmentDetail.isReadOnly()) {
   return attachmentDetail.getWorkerId().equals(userId);
   }
   attachmentDetail.setWorkerId(userId);
   if (attachmentDetail.isOpenOfficeCompatible()) {
   RepositoryHelper.getJcrAttachmentService().createAttachment(attachmentDetail, language);
   }
   // mise à jour de la date d'expiration
   Calendar cal = Calendar.getInstance(Locale.FRENCH);
   attachmentDetail.setReservationDate(cal.getTime());

   // 1. rechercher le nombre de jours avant expiration dans le composant
   OrganizationController orga = new OrganizationController();
   SilverTrace.info("attachment", "getExpiryDate", "root.MSG_GEN_PARAM_VALUE", "instanceId = "
   + attachmentDetail.getInstanceId());

   String day =
   orga.getComponentParameterValue(attachmentDetail.getInstanceId(), "nbDayForReservation");

   if (StringUtil.isDefined(day)) {
   int nbDay = Integer.parseInt(day);
   SilverTrace.info("attachment", "getExpiryDate", "root.MSG_GEN_PARAM_VALUE",
   "nbDay = " + nbDay);

   // 2. calcul la date d'expiration en fonction de la date d'aujourd'hui
   // et de la durée de réservation
   Calendar calendar = Calendar.getInstance(Locale.FRENCH);
   // calendar.add(Calendar.DATE, nbDay);
   addDays(calendar, nbDay);
   attachmentDetail.setExpiryDate(calendar.getTime());
   }
   // mise à jour de la date d'alerte
   // 1. rechercher le % dans le properties
   int delayReservedFile = Integer.parseInt(resources.getString("DelayReservedFile"));
   if ((delayReservedFile >= 0) && (delayReservedFile <= 100)) {
   // calculer le nombre de jours
   if (StringUtil.isDefined(day)) {
   int nbDay = Integer.parseInt(day);
   int result = (nbDay * delayReservedFile) / 100;
   SilverTrace.info("attachment", "getExpiryDate", "root.MSG_GEN_PARAM_VALUE",
   "delayReservedFile = " + delayReservedFile);
   SilverTrace.info("attachment", "getExpiryDate", "root.MSG_GEN_PARAM_VALUE",
   "result = " + result);

   if (result > 2) {
   Calendar calendar = Calendar.getInstance(Locale.FRENCH);
   addDays(calendar, result);
   attachmentDetail.setAlertDate(calendar.getTime());
   }
   }
   }

   updateAttachment(attachmentDetail, false, false);
   } catch (Exception e) {
   throw new AttachmentRuntimeException(
   "AttachmentController.checkoutFile()",
   SilverpeasRuntimeException.ERROR, "attachment.CHECKOUT_FAILED", e);
   }
   return true;
   }

   public void cloneAttachments(SimpleDocumentPK fromForeignKey,
   SimpleDocumentPK toForeignKey)  {
   Vector<AttachmentDetail> attachments = attachmentBm.getAttachmentsByPKAndParam(
   fromForeignKey, "Context", "Images");
   for (AttachmentDetail a : attachments) {
   AttachmentDetail clone = (AttachmentDetail) a.clone();
   // The file must be copied
   String physicalName = java.lang.Long.toString(System.currentTimeMillis()) + "." + a.
   getExtension();
   clone.setPhysicalName(physicalName);
   copyFileOnServer(a, clone);
   clone.setForeignKey(toForeignKey);
   clone.setCloneId(a.getPK().getId());
   clone = attachmentBm.createAttachment(clone);
   }
   }

   public void mergeAttachments(AttachmentPK fromForeignKey,
   AttachmentPK toForeignKey) throws AttachmentException {

   // On part des fichiers d'origine
   Vector<AttachmentDetail> attachments = attachmentBm.getAttachmentsByPKAndParam(
   fromForeignKey, "Context", "Images");
   Iterator<AttachmentDetail> iAttachments = attachments.iterator();

   Vector<AttachmentDetail> clones = attachmentBm.getAttachmentsByPKAndParam(toForeignKey,
   "Context", "Images");

   // recherche suppressions et modifications
   AttachmentDetail attachmentDetail = null;
   AttachmentDetail clone = null;

   while (iAttachments.hasNext()) {
   attachmentDetail = iAttachments.next();

   // Ce fichier existe-il toujours ?
   clone = searchClone(attachmentDetail, clones);

   if (clone != null) {

   // le fichier existe toujours !
   // Merge du clone sur le fichier d'origine
   mergeAttachment(attachmentDetail, clone);

   // Suppression de la liste des clones
   clones.remove(clone);
   } else {

   // le fichier a été supprimé
   // Suppression du fichier d'origine
   deleteAttachment(attachmentDetail);
   }
   }

   if (clones.size() > 0) {

   // Il s'agit d'ajouts
   Iterator<AttachmentDetail> iClones = clones.iterator();

   clone = null;

   while (iClones.hasNext()) {
   clone = iClones.next();

   clone.setForeignKey(fromForeignKey);
   clone.setCloneId(null);

   attachmentBm.updateAttachment(clone);
   attachmentBm.updateForeignKey(clone.getPK(),
   fromForeignKey.getId());
   }
   }
   }

   private SimpleDocument searchClone(AttachmentDetail attachmentDetail, Vector<AttachmentDetail> clones) {
   Iterator<AttachmentDetail> iClones = clones.iterator();

   AttachmentDetail clone = null;

   while (iClones.hasNext()) {
   clone = iClones.next();

   if ((clone.getCloneId() != null)
   && clone.getCloneId().equals(attachmentDetail.getPK().getId())) {
   return clone;
   }
   }

   return null;
   }

   private void mergeAttachment(AttachmentDetail attachmentDetail,
   AttachmentDetail clone) throws AttachmentException {

   // Màj du fichier d'origine
   attachmentDetail.setAuthor(clone.getAuthor());
   attachmentDetail.setInfo(clone.getInfo());
   attachmentDetail.setLogicalName(clone.getLogicalName());
   attachmentDetail.setOrderNum(clone.getOrderNum());
   attachmentDetail.setPhysicalName(clone.getPhysicalName());
   attachmentDetail.setSize(clone.getSize());
   attachmentDetail.setTitle(clone.getTitle());
   attachmentDetail.setType(clone.getType());
   attachmentDetail.setWorkerId(null);

   attachmentBm.updateAttachment(attachmentDetail);

   // Suppression du clone
   attachmentBm.deleteAttachment(clone.getPK());
   }*/
  /**
   * Clone the attchments
   *
   * @param original
   * @param foreignCloneId
   * @return
   */
  @Override
  public SimpleDocumentPK cloneDocument(SimpleDocument original, String foreignCloneId) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      SimpleDocumentPK clonePk = repository.copyDocument(session, original, new ForeignPK(
          foreignCloneId, original.getInstanceId()));
      SimpleDocument clone = repository.findDocumentById(session, clonePk, null);
      clone.setCloneId(foreignCloneId);
      repository.updateDocument(session, clone);
      session.save();
      return clonePk;
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Reorder the attachments according to the order in the list.
   *
   * @param pks
   * @throws AttachmentException
   */
  @Override
  public void reorderAttachments(List<SimpleDocumentPK> pks) throws AttachmentException {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      int i = 5;
      for (SimpleDocumentPK pk : pks) {
        SimpleDocument doc = repository.findDocumentById(session, pk, null);
        doc.setOrder(i);
        repository.updateDocument(session, doc);
        i = i + 5;
      }
      session.save();
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  //@Override
  public InputStream getBinaryContent(SimpleDocumentPK pk, String lang) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      return repository.getContent(session, pk, lang);
    } catch (IOException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public void getBinaryContent(OutputStream output, SimpleDocumentPK pk, String lang) {
    Session session = null;
    InputStream in = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      in = repository.getContent(session, pk, lang);
      IOUtils.copy(in, output);
    } catch (IOException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      IOUtils.closeQuietly(in);
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public List<SimpleDocument> listDocumentsRequiringWarning(Date alertDate, String language) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      return repository.listDocumentsRequiringWarning(session, alertDate, language);
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public List<SimpleDocument> listExpiringDocuments(Date expiryDate, String language) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      return repository.listExpiringDocuments(session, expiryDate, language);
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public List<SimpleDocument> listDocumentsToUnlock(Date expiryDate, String language) {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      return repository.listDocumentsToUnlock(session, expiryDate, language);
    } catch (RepositoryException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public void addContent(SimpleDocument document, File content, boolean indexIt,
      boolean invokeCallback) {
    InputStream in = null;
    try {
      in = new BufferedInputStream(new FileInputStream(content));
      addContent(document, in, indexIt, invokeCallback);
    } catch (FileNotFoundException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  @Override
  public void getBinaryContent(File file, SimpleDocumentPK pk, String lang) {
    OutputStream out = null;
    try {
      out = new BufferedOutputStream(new FileOutputStream(file));
      getBinaryContent(out, pk, lang);
    } catch (FileNotFoundException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      IOUtils.closeQuietly(out);
    }
  }

  @Override
  public SimpleDocument createAttachment(SimpleDocument document, File content) throws
      AttachmentException {
    return createAttachment(document, content, true);
  }

  @Override
  public SimpleDocument createAttachment(SimpleDocument document, File content, boolean indexIt) {
    return createAttachment(document, content, indexIt, true);
  }

  @Override
  public SimpleDocument createAttachment(SimpleDocument document, File content, boolean indexIt,
      boolean invokeCallback) {
    InputStream in = null;
    try {
      in = new BufferedInputStream(new FileInputStream(content));
      return createAttachment(document, in, indexIt, invokeCallback);
    } catch (FileNotFoundException ex) {
      throw new AttachmentException(this.getClass().getName(), SilverpeasException.ERROR, "", ex);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }
}
