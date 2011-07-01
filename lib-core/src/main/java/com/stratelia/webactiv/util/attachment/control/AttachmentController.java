/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.util.attachment.control;

import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.i18n.Translation;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentException;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentRuntimeException;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetailI18N;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

public class AttachmentController {

  private static AttachmentBm attachmentBm = new AttachmentBmImpl();
  public final static String CONTEXT_ATTACHMENTS = "Attachment" + File.separatorChar + "Images"
      + File.separatorChar;
  // For Office files direct update
  public final static String NO_UPDATE_MODE = "0";
  public final static String UPDATE_DIRECT_MODE = "1";
  public final static String UPDATE_SHORTCUT_MODE = "2";
  private static ResourceLocator resources = new ResourceLocator(
      "com.stratelia.webactiv.util.attachment.Attachment", "");
  private static final MimetypesFileTypeMap MIME_TYPES = new MimetypesFileTypeMap();

  /**
   * the constructor.
   */
  public AttachmentController() {
  }
  
  /**
   * For tests only
   * @param attachmentManager 
   */
  AttachmentController(AttachmentBm attachmentManager) {
    AttachmentController.attachmentBm = attachmentManager;
  }

  /**
   * Create file attached to an object who is identified by "PK" AttachmentDetail object contains an
   * attribute who identifie the link by a foreign key.
   *
   * @param attachDetail the data of an attachement to be created.
   * @param indexIt      tells if the attachment must be indexed or not.
   * @return the AttachmentDetail just created
   * @throws AttachmentRuntimeException if the attachment cannot be created.
   * @author Nicolas EYSSERIC
   * @version 1.0
   */
  public static AttachmentDetail createAttachment(AttachmentDetail attachDetail,
      boolean indexIt) {
    return createAttachment(attachDetail, indexIt, true);
  }

  /**
   * Create file attached to an object who is identified by "PK" AttachmentDetail object contains an
   * attribute who identifie the link by a foreign key.
   *
   * @param attachDetail   the data of an attachement to be created.
   * @param indexIt        tells if the attachment must be indexed or not.
   * @param invokeCallback <code>true</code> if the callback methods of the components must be
   *                       called, <code>false</code> for ignoring thoose callbacks.
   * @return the AttachmentDetail just created
   * @throws AttachmentRuntimeException if the attachment cannot be created.
   */
  public static AttachmentDetail createAttachment(AttachmentDetail attachDetail,
      boolean indexIt, boolean invokeCallback) {

    try {
      AttachmentDetail attachmentDetail = attachmentBm.createAttachment(
          attachDetail);

      if (invokeCallback && (attachmentDetail.getAuthor() != null)
          && (attachmentDetail.getAuthor().length() > 0)) {
        CallBackManager callBackManager = CallBackManager.get();
        callBackManager.invoke(CallBackManager.ACTION_ATTACHMENT_ADD, Integer
            .parseInt(attachmentDetail.getAuthor()), attachmentDetail.getForeignKey()
            .getInstanceId(), attachmentDetail.getForeignKey().
            getId());
      }

      if (indexIt) {
        createIndex(attachDetail);
      }

      return attachmentDetail;

    } catch (Exception e) {
      throw new AttachmentRuntimeException(
          "AttachmentController.createAttachment()",
          SilverpeasRuntimeException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
          e);
    }
  }

  public static AttachmentDetail createAttachment(AttachmentDetail attachDetail) {
    return createAttachment(attachDetail, true);
  }

  /**
   * to update file information (title and description) AttachmentDetail object to update.
   *
   * @param attachDetail : AttachmentDetail.
   * @param indexIt      - indicates if attachment must be indexed or not
   */
  public static void updateAttachment(AttachmentDetail attachDetail, boolean indexIt) {
    updateAttachment(attachDetail, indexIt, true);
  }

  public static void updateAttachment(AttachmentDetail attachDetail, boolean indexIt,
      boolean invokeCallback) {
    try {
      AttachmentDetail oldAttachment =
          attachmentBm.getAttachmentByPrimaryKey(attachDetail.getPK());
      if (I18NHelper.isI18N) {
        if (attachDetail.isRemoveTranslation()) {
          String languageToDelete = attachDetail.getLanguage();
          // suppression du fichier de la traduction
          AttachmentDetailI18N translation = (AttachmentDetailI18N) oldAttachment.getTranslation(
              languageToDelete);
          oldAttachment.setPhysicalName(translation.getPhysicalName());
          deleteFileAndIndex(oldAttachment);
        } else {
          if (attachDetail.getPhysicalName() == null) {
            // the file has not been modified
            String languageToUpdate = attachDetail.getLanguage();
            AttachmentDetailI18N translation =
                (AttachmentDetailI18N) oldAttachment.getTranslation(languageToUpdate);
            attachDetail.setPhysicalName(translation.getPhysicalName());
            attachDetail.setLogicalName(translation.getLogicalName());
            attachDetail.setType(translation.getType());
            attachDetail.setSize(translation.getSize());
          }
        }
      }

      String language = attachDetail.getLanguage();
      attachmentBm.updateAttachment(attachDetail);
      if (attachDetail.isOpenOfficeCompatible() && attachDetail.isReadOnly()) {
        // le fichier est renommé
        if (oldAttachment.getLogicalName(language).equals(attachDetail.getLogicalName(language))) {
          RepositoryHelper.getJcrAttachmentService().deleteAttachment(oldAttachment, language);
          RepositoryHelper.getJcrAttachmentService().createAttachment(attachDetail, language);
        } else {
          RepositoryHelper.getJcrAttachmentService().updateNodeAttachment(attachDetail,
              attachDetail.getLanguage());
        }
      }
      String userId = attachDetail.getAuthor();
      if ((userId != null) && (userId.length() > 0) && invokeCallback) {
        CallBackManager callBackManager = CallBackManager.get();
        callBackManager.invoke(CallBackManager.ACTION_ATTACHMENT_UPDATE,
            Integer.parseInt(attachDetail.getAuthor()), attachDetail.getInstanceId(), attachDetail
            .getForeignKey().getId());
      }
      if (indexIt) {
        createIndex(attachDetail.getPK());
      }
    } catch (Exception e) {
      throw new AttachmentRuntimeException(
          "AttachmentController.updateAttachment()",
          SilverpeasRuntimeException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
          e);
    }
  }

  public static void updateAttachment(AttachmentDetail attachDetail) {
    updateAttachment(attachDetail, true);
  }

  public static void updateAttachmentForeignKey(AttachmentPK pk, String foreignKey) {
    try {
      attachmentBm.updateForeignKey(pk, foreignKey);
    } catch (Exception e) {
      throw new AttachmentRuntimeException("AttachmentController.updateAttachmentForeignKey()",
          SilverpeasRuntimeException.ERROR, "root.EX_RECORD_INSERTION_FAILED", e);
    }

  }

  public static void moveDownAttachment(AttachmentDetail attachDetail) {

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

  public static void moveUpAttachment(AttachmentDetail attachDetail) {

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

  /**
   * to search all file attached to an object who is identified by "PK"
   *
   * @param foreignKey : com.stratelia.webactiv.util.WAPrimaryKey: the primary key of customer object but
   *           this key must be transformed to AttachmentPK
   * @return java.util.Vector: a collection of AttachmentDetail
   * @throws AttachmentRuntimeException when is impossible to search
   * @author Jean-Claude Groccia
   * @version 1.0
   */
  public static Vector<AttachmentDetail> searchAttachmentByCustomerPK(WAPrimaryKey foreignKey) {
    AttachmentPK fk =
        new AttachmentPK(foreignKey.getId(), foreignKey.getSpace(), foreignKey.getComponentName());

    try {
      return attachmentBm.getAttachmentsByForeignKey(fk);
    } catch (Exception fe) {
      throw new AttachmentRuntimeException(
          "AttachmentController.searchAttachmentByCustomerPK()",
          SilverpeasRuntimeException.ERROR, "root.EX_RECORD_NOT_FOUND", fe);
    }
  }

  public static void moveAttachments(ForeignPK fromPK, ForeignPK toPK,
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

  /**
   * to search all file attached
   *
   * @param primaryKey the primary key of object AttachmentDetail
   * @return java.util.Vector: a collection of AttachmentDetail
   * @throws AttachmentRuntimeException when is impossible to search
   * @author Jean-Claude Groccia
   * @version 1.0
   * @see com.stratelia.webactiv.util.attachment.model.AttachmentDetail.
   */
  public static AttachmentDetail searchAttachmentByPK(AttachmentPK primaryKey) {
    try {
      return attachmentBm.getAttachmentByPrimaryKey(primaryKey);
    } catch (Exception fe) {
      throw new AttachmentRuntimeException(
          "AttachmentController.searchAttachmentByPK(AttachmentPK primaryKey )",
          SilverpeasRuntimeException.ERROR, "root.EX_RECORD_NOT_FOUND", fe);
    }
  }

  /**
   * to search all file attached by primary key of customer object and mime type of file attached
   *
   * @param foreignKey : com.stratelia.webactiv.util.WAPrimaryKey:the primary key of customer object
   *                   but this key must be transformed to AttachmentPK
   * @param mimeType   : the mime type of file attached
   * @return java.util.Vector, a vector of AttachmentDetail
   * @throws AttachmentRuntimeException when is impossible to search
   * @author Jean-Claude Groccia
   * @version 1.0
   * @see com.stratelia.webactiv.util.attachment.model.AttachmentDetail.
   */
  public static Vector<AttachmentDetail> searchAttachmentByPKAndMimeType(WAPrimaryKey foreignKey,
      String mimeType) {
    AttachmentPK fk =
        new AttachmentPK(foreignKey.getId(), foreignKey.getSpace(), foreignKey.getComponentName());

    try {
      return attachmentBm.getAttachmentsByPKAndParam(fk, "type", mimeType);
    } catch (Exception fe) {
      throw new AttachmentRuntimeException(
          "AttachmentController.searchAttachmentByPK(AttachmentPK primaryKey )",
          SilverpeasRuntimeException.ERROR, "root.EX_RECORD_NOT_FOUND", fe);
    }
  }

  public static Vector<AttachmentDetail> searchAttachmentByPKAndContext(WAPrimaryKey foreignKey,
      String context) {
    return searchAttachmentByPKAndContext(foreignKey, context, null);
  }

  /**
   * to search all file attached by primary key of customer object and context of file attached
   *
   * @param pk      : com.stratelia.webactiv.util.WAPrimaryKey: the primary key of customer object
   *                but this key must be transformed to AttachmentPK
   * @param context : String: the context attribute of file attached
   * @return java.util.Vector, a vector of AttachmentDetail
   * @throws AttachmentRuntimeException when is impossible to search
   * @author Jean-Claude Groccia
   * @version 1.0
   * @see com.stratelia.webactiv.util.attachment.model.AttachmentDetail.
   */
  // méthode pour wysiwig pb de gestion d'exception
  public static Vector<AttachmentDetail> searchAttachmentByPKAndContext(WAPrimaryKey foreignKey,
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

  /**
   * to provide applicationIndexer service
   *
   * @param fk : com.stratelia.webactiv.util.WAPrimaryKey: the primary key of customer object
   * @return void
   * @author Jean-Claude Groccia
   * @version 1.0
   * @see com.stratelia.webactiv.util.attachment.model.AttachmentDetail.
   */
  public static void attachmentIndexer(WAPrimaryKey fk) {

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
  public static void deleteAttachmentByCustomerPK(WAPrimaryKey foreignKey) {
    AttachmentPK fk = new AttachmentPK(foreignKey.getId(), foreignKey.getComponentName());
    Vector<AttachmentDetail> attachmentDetails = searchAttachmentByCustomerPK(fk);
    deleteAttachment(attachmentDetails);
  }

  public static void deleteAttachmentsByCustomerPKAndContext(
      WAPrimaryKey foreignKey, String context) {
    Vector<AttachmentDetail> attachmentDetails = searchAttachmentByPKAndContext(foreignKey,
        context);
    deleteAttachment(attachmentDetails);
  }

  public static void unindexAttachmentsByForeignKey(WAPrimaryKey foreignKey) {
    AttachmentPK fk =
        new AttachmentPK(foreignKey.getId(), foreignKey.getSpace(), foreignKey.getComponentName());
    Vector<AttachmentDetail> attachmentDetails = searchAttachmentByCustomerPK(fk);
    Iterator<AttachmentDetail> it = attachmentDetails.iterator();

    while (it.hasNext()) {
      AttachmentDetail aD = it.next();
      deleteIndex(aD);
    }
  }

  public static void deleteWysiwygAttachmentByCustomerPK(WAPrimaryKey foreignKey) {
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

  /**
   * Delete a given attachment.
   *
   * @param attachmentDetail the attachmentDetail object to deleted.
   * @throws AttachmentRuntimeException if the attachement cannot be deleted.
   * @author Jean-Claude Groccia
   * @version 1.0
   * @see com.stratelia.webactiv.util.attachment.model.AttachmentDetail.
   */
  public static void deleteAttachment(AttachmentDetail attachmentDetail) {
    deleteAttachment(attachmentDetail, true);
  }

  /**
   * Delete a given attachment.
   *
   * @param attachmentDetail the attachmentDetail object to deleted.
   * @param invokeCallback   <code>true</code> if the callback methods of the components must be
   *                         called, <code>false</code> for ignoring thoose callbacks.
   * @throws AttachmentRuntimeException if the attachement cannot be deleted.
   */
  public static void deleteAttachment(AttachmentDetail attachmentDetail,
      boolean invokeCallback) {

    try {
      attachmentBm.deleteAttachment(attachmentDetail.getPK());

      if (!I18NHelper.isI18N) {
        deleteFileAndIndex(attachmentDetail);
      } else {
        // delete all translation files
        deleteTranslations(attachmentDetail);
      }

      if (attachmentDetail.isOpenOfficeCompatible()
          && !attachmentDetail.isReadOnly()) {
        RepositoryHelper.getJcrAttachmentService().deleteAttachment(
            attachmentDetail, attachmentDetail.getLanguage());
      }

      if (invokeCallback) {
        int authorId = -1;
        if (StringUtil.isDefined(attachmentDetail.getAuthor())) {
          authorId = Integer.parseInt(attachmentDetail.getAuthor());
        }

        CallBackManager callBackManager = CallBackManager.get();
        callBackManager.invoke(CallBackManager.ACTION_ATTACHMENT_REMOVE,
            authorId, attachmentDetail.getPK().getInstanceId(), attachmentDetail);
      }

    } catch (Exception fe) {
      throw new AttachmentRuntimeException(
          "AttachmentController.deleteAttachment(AttachmentDetail attachDetail)",
          SilverpeasRuntimeException.ERROR, "root.EX_RECORD_DELETED_FAILED", fe);
    }
  }

  public static void deleteAttachment(AttachmentPK pk) {
    deleteAttachment(pk, true);
  }

  public static void deleteAttachment(AttachmentPK pk, boolean invokeCallback) {

    try {
      AttachmentDetail attachDetail = attachmentBm.
          getAttachmentByPrimaryKey(pk);
      deleteAttachment(attachDetail, invokeCallback);

    } catch (Exception fe) {
      throw new AttachmentRuntimeException(
          "AttachmentController.deleteAttachment(AttachmentPK pk)",
          SilverpeasRuntimeException.ERROR, "root.EX_RECORD_DELETED_FAILED",
          "pk = " + pk.toString(), fe);
    }
  }

  private static void deleteTranslations(AttachmentDetail attachDetail) {

    if (attachDetail.getTranslations() != null) {
      Iterator translations = attachDetail.getTranslations().values().iterator();
      AttachmentDetailI18N translation = null;

      while (translations.hasNext()) {
        translation = (AttachmentDetailI18N) translations.next();

        if (translation != null) {
          attachDetail.setPhysicalName(translation.getPhysicalName());
          attachDetail.setLanguage(translation.getLanguage());
          deleteFileAndIndex(attachDetail);
        }
      }
    }
  }

  /**
   * to delete a list of file attached.
   *
   * @param vectorAttachmentDetail: the vector of attachmentDetail object to deleted
   * @return void
   * @throws AttachmentRuntimeException when is impossible to delete
   * @author Jean-Claude Groccia
   * @version 1.0
   * @see com.stratelia.webactiv.util.attachment.model.AttachmentDetail.
   */
  public static void deleteAttachment(Vector<AttachmentDetail> vectorAttachmentDetail) {
    for (AttachmentDetail aD : vectorAttachmentDetail) {
      deleteAttachment(aD);
    }
  }

  /**
   * to delete one file attached.
   *
   * @param attachDetail : the attachmentDetail object to deleted
   * @return void
   * @throws AttachmentRuntimeException when is impossible to delete
   * @author Jean-Claude Groccia
   * @version 1.0
   * @see com.stratelia.webactiv.util.attachment.model.AttachmentDetail.
   */
  public static void deleteFileAndIndex(AttachmentDetail attachDetail) {

    try {
      int attGroup = attachDetail.getAttachmentGroup();

      // Remove Linked file(s) on server
      if ((attGroup == AttachmentDetail.GROUP_FILE)) {
        deleteFileOnServer(attachDetail);
      } else if (attGroup == AttachmentDetail.GROUP_DIR) {
        deleteAttachment(searchAttachmentByPKAndContext(attachDetail.getForeignKey(),
            attachDetail.getContext()));
        // deleteFolderOnServer(attachDetail);
      }

      // Remove Index
      if ((attGroup == AttachmentDetail.GROUP_FILE)
          || (attGroup == AttachmentDetail.GROUP_FILE_LINK)
          || (attGroup == AttachmentDetail.GROUP_DIR)) {
        deleteIndex(attachDetail);
      }
    } catch (Exception fe) {
      throw new AttachmentRuntimeException(
          "AttachmentController.deleteAttachment(Attachment attach, AttachmentDetail attachDetail)",
          SilverpeasRuntimeException.ERROR, "root.EX_RECORD_DELETED_FAILED", fe);
    }
  }

  /**
   * to create file attached to an object who is identified by "PK" AttachmentDetail object contains
   * a attribute who identifie the father by a foreign key.
   *
   * @param vectorAttachmentDetail : java.util.Vector contains a list of AttachmentDetail object.
   * @throws AttachmentRuntimeException when is impossible to create
   * @author Jean-Claude Groccia
   * @version 1.0
   * @see com.stratelia.webactiv.util.attachment.model.AttachmentDetail.
   */
  public static void createAttachment(Vector<AttachmentDetail> vectorAttachmentDetail) {
    for (AttachmentDetail attachmentDetail : vectorAttachmentDetail) {
      createAttachment(attachmentDetail);
    }
  }

  /**
   * Method to build the attachment Context
   *
   * @param str   : String: the string of repertories
   * @param token : String: the token séparating the repertories
   * @return: String : the string separating by token of attachmentDetail.context.
   */
  public static String getDetailContext(String str, String token) {
    return str.replace(token.charAt(0), ',');
  }

  /**
   * to create path
   *
   * @param spaceId     : type String: the name of space
   * @param componentId : type String: the name of component
   * @param context     : type String: string made up of the repertories separated by token ","
   * @deprecated Warning: the token separing the repertories is ","
   */
  public static String createPath(String spaceId, String componentId,
      String context) {
    return createPath(componentId, context);
  }

  /**
   * To create path Warning: the token separing the repertories is ","
   *
   * @param spaceId     : type String: the name of space
   * @param componentId : type String: the name of component
   * @param context     : type String: string made up of the repertories separated by token ","
   */
  public static String createPath(String componentId, String context) {
    String path = null;

    if ((context != null) && !context.equals("null") && (context.length() > 0)) {

      // to create the context
      String strAt = "Attachment,";

      strAt = strAt.concat(context);

      StringTokenizer strToken = new StringTokenizer(strAt, ",");

      // number of elements
      int nElt = strToken.countTokens();

      // to init array
      String[] ctx = new String[nElt];

      int k = 0;

      while (strToken.hasMoreElements()) {
        ctx[k] = (String) strToken.nextElement();
        k++;
      }

      path = FileRepositoryManager.getAbsolutePath(componentId, ctx);
    } else {
      String[] ctx = {"Attachment"};

      path = FileRepositoryManager.getAbsolutePath(componentId, ctx);
    }

    try {
      File d = new File(path);

      if (!d.exists()) {
        FileFolderManager.createFolder(path);
      }

      return path;
    } catch (Exception e) {
      throw new AttachmentRuntimeException(
          "AttachmentController.createPath(String spaceId, String componentId, String context)",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_CREATE_FILE", e);
    }
  }

  /**
   * to get mime type of the file param extensionFile : type String
   *
   * @p
   */
  public static String getMimeType(String fileLogicalName) {
    return FileUtil.getMimeType(fileLogicalName);
  }

  public static List<String> getLanguagesOfAttachments(ForeignPK foreignPK) {
    List<String> languages = new ArrayList<String>();
    Vector<AttachmentDetail> attachments = searchAttachmentByPKAndContext(foreignPK, "Images");
    AttachmentDetail detail = null;
    Iterator<String> itLanguages = null;
    String language = null;

    for (int v = 0; attachments != null && v < attachments.size(); v++) {
      detail = attachments.get(v);
      itLanguages = detail.getLanguages();

      while ((itLanguages != null) && itLanguages.hasNext()) {
        language = itLanguages.next();

        if (!languages.contains(language)) {
          languages.add(language);
        }
      }

    }

    return languages;
  }

  /**
   * to delete file on server param atDetail: type AttachmentDetail: the object AttachmentDetail to
   * deleted
   *
   * @author Jean-Claude Groccia
   * @version 1.0
   * @see com.stratelia.webactiv.util.attachment.model.AttachmentDetail
   */
  private static void deleteFileOnServer(AttachmentDetail attachDetail) {

    // to create the context
    String[] ctx = FileRepositoryManager.getAttachmentContext(attachDetail.getContext());
    String filePath =
        FileRepositoryManager.getAbsolutePath(attachDetail.getPK().getComponentName(), ctx)
            + attachDetail.getPhysicalName();

    try {
      File d = new File(filePath);

      if (d.exists()) {
        FileFolderManager.deleteFile(filePath);
      }

      RepositoryHelper.getJcrAttachmentService().deleteAttachment(attachDetail,
          null);
    } catch (Exception e) {
      SilverTrace.warn(
          "attachment",
          "AttachmentController.deleteFileOnServer((AttachmentDetail attachDetail)",
          "attachment_MSG_NOT_DELETE_FILE", "filePath=" + filePath, e);
    }
  }

  public static void createIndex(AttachmentPK pk) {
    AttachmentDetail attachment = searchAttachmentByPK(pk);
    createIndex(attachment);
  }

  /**
   * Method declaration
   *
   * @param detail
   * @see
   */
  public static void createIndex(AttachmentDetail detail) {

    // index the uploaded or linked file
    int attGroup = detail.getAttachmentGroup();

    if ((attGroup == AttachmentDetail.GROUP_FILE)
        || (attGroup == AttachmentDetail.GROUP_FILE_LINK)
        || (attGroup == AttachmentDetail.GROUP_DIR)) {
      String component = detail.getPK().getComponentName();
      String fk = detail.getForeignKey().getId();

      try {
        Iterator<String> languages = detail.getLanguages();

        while (languages.hasNext()) {
          String language = languages.next();
          AttachmentDetailI18N translation = (AttachmentDetailI18N) detail.getTranslation(language);

          String objectType = "Attachment" + detail.getPK().getId();

          if (I18NHelper.isI18N && !I18NHelper.isDefaultLanguage(language)) {
            objectType += "_" + language;
          }

          FullIndexEntry indexEntry = new FullIndexEntry(component, objectType,
              fk);

          indexEntry.setLang(language);
          indexEntry.setCreationDate(translation.getCreationDate());
          indexEntry.setCreationUser(translation.getAuthor());

          indexEntry.setTitle(translation.getLogicalName(), language);

          String title = translation.getTitle();

          if (StringUtil.isDefined(title)) {
            indexEntry.setKeywords(title, language);
          }

          String info = translation.getInfo();

          if (StringUtil.isDefined(info)) {
            indexEntry.setPreview(info, language);
          }

          /*
           * le champs description est utilisé pour savoir si c'est un lien ou une copie sur le
           * fichier
           */
          String path;

          if (detail.getAttachmentGroup() == AttachmentDetail.GROUP_FILE_LINK) {

            /*
             * c'est un lien, le chemin est contenu dans la colonne physicalName(complet) un lien,
             * le chemin est contenu dans la colonne physicalName(complet)
             */
            path = detail.getPhysicalName();
          } else {
            path = createPath(component, detail.getContext()) + File.separator
                + translation.getPhysicalName();
          }

          String encoding = null;
          String format = translation.getType();
          String lang = translation.getLanguage();

          indexEntry.addFileContent(path, encoding, format, lang);

          if (StringUtil.isDefined(detail.getXmlForm())) {
            updateIndexEntryWithXMLFormContent(detail.getPK(), detail.getXmlForm(), indexEntry);
          }

          IndexEngineProxy.addIndexEntry(indexEntry);
        }
      } catch (Exception e) {
        SilverTrace.warn("attachment",
            "AttachmentController.createIndex((AttachmentDetail attachDetail)",
            "root.EX_INDEX_FAILED");
      }
    }
  }

  private static void updateIndexEntryWithXMLFormContent(AttachmentPK pk,
      String xmlFormName, FullIndexEntry indexEntry) {
    SilverTrace.info("attachment",
        "AttachmentController.updateIndexEntryWithXMLFormContent()",
        "root.MSG_GEN_ENTER_METHOD", "indexEntry = " + indexEntry.toString());
    try {
      String objectType = "Attachment";
      PublicationTemplate pub = PublicationTemplateManager.getInstance()
          .getPublicationTemplate(indexEntry.getComponent() + ":" + objectType + ":" +
              xmlFormName);
      RecordSet set = pub.getRecordSet();
      set.indexRecord(pk.getId(), xmlFormName, indexEntry);
    } catch (Exception e) {
      SilverTrace.error("attachment",
          "AttachmentController.updateIndexEntryWithXMLFormContent()", "", e);
    }
  }

  /**
   * Method declaration
   *
   * @param detail
   * @see
   */
  private static void deleteIndex(AttachmentDetail detail) {
    SilverTrace.debug("attachment", "AttachmentController.deleteIndex",
        "root.MSG_GEN_ENTER_METHOD", detail.getPK().toString());

    try {
      String objectType = "Attachment" + detail.getPK().getId();

      if (I18NHelper.isI18N
          && !I18NHelper.isDefaultLanguage(detail.getLanguage())) {
        objectType += "_" + detail.getLanguage();
      }

      IndexEntryPK indexEntry =
          new IndexEntryPK(detail.getPK().getComponentName(), objectType, detail.getForeignKey()
              .getId());

      IndexEngineProxy.removeIndexEntry(indexEntry);
    } catch (Exception e) {
      SilverTrace.warn("attachment",
          "AttachmentController.deleteIndex((AttachmentDetail attachDetail)",
          "root.EX_INDEX_DELETE_FAILED");
    }
  }

  /**
   * to copy all files attached to an object who is identified by "PK" to an other object
   *
   * @param foreignKeyFrom : com.stratelia.webactiv.util.WAPrimaryKey: the primary key of customer
   *                       object source
   * @param foreignKeyTo   : com.stratelia.webactiv.util.WAPrimaryKey: the primary key of customer
   *                       object destination
   * @throws AttachmentRuntimeException
   * @author SCO
   * @version 1.0
   */
  public static Hashtable<String, String> copyAttachmentByCustomerPK(
      WAPrimaryKey foreignKeyFrom, WAPrimaryKey foreignKeyTo)
      throws AttachmentRuntimeException {
    SilverTrace.debug("attachment",
        "AttachmentController.copyAttachmentByCustomerPK",
        "root.MSG_GEN_ENTER_METHOD", "foreignKeyFrom = " + foreignKeyFrom
            + ", foreignKeyTo=" + foreignKeyTo);

    Vector<AttachmentDetail> attsToCopy = searchAttachmentByCustomerPK(foreignKeyFrom);

    return copyAttachments(attsToCopy, foreignKeyFrom, foreignKeyTo);
  }

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

  public static Hashtable<String, String> copyAttachment(AttachmentDetail attToCopy,
      WAPrimaryKey foreignKeyFrom, WAPrimaryKey foreignKeyTo)
      throws AttachmentRuntimeException {
    Vector<AttachmentDetail> attsToCopy = new Vector<AttachmentDetail>();
    attsToCopy.add(attToCopy);
    return copyAttachments(attsToCopy, foreignKeyFrom, foreignKeyTo);
  }

  private static Hashtable<String, String> copyAttachments(Vector<AttachmentDetail> attsToCopy,
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
          physicalName = new Long(new Date().getTime()).toString() + "." + type;
          copy.setPhysicalName(physicalName);

          copyFileOnServer(attToCopy, copy);
        }

        copy = AttachmentController.createAttachment(copy);
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
          physicalName = Long.toString(System.currentTimeMillis()) + "." + type;
          translationCopy.setPhysicalName(physicalName);

          attToCopy.setPhysicalName(translation.getPhysicalName());

          copyFileOnServer(attToCopy, translationCopy);

          AttachmentController.updateAttachment(translationCopy);
        }
      }
    }

    return ids;
  }

  /**
   * to copy one file to another on server param attDetailFrom: type AttachmentDetail: the object
   * AttachmentDetail to copy param attDetailTo: type AttachmentDetail: the object AttachmentDetail
   * to create
   *
   * @author SCO
   * @version 1.0
   * @see com.stratelia.webactiv.util.attachment.model.AttachmentDetail
   */
  private static void copyFileOnServer(AttachmentDetail attDetailFrom,
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
  }

  /**
   * Checkin a file
   *
   * @param attachmentId
   * @param userId
   * @param upload       : indicates if the file has been uploaded throught a form.
   * @param force        if the user is an Admin he can force the release.
   * @param language     the language for the attachment.
   * @return false if the file is locked - true if the checkin succeeded.
   * @throws AttachmentException
   */
  public static boolean checkinFile(String attachmentId, String userId, boolean upload,
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
      AttachmentController.updateAttachment(attachmentDetail, false, invokeCallback);
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
  }

  /**
   * Checkout a file for update by user
   *
   * @param attachmentId
   * @param userId
   * @return false if the attahcment is already checkout - true if the attachment was successfully
   *         checked out.
   * @throws AttachmentException
   */
  public static boolean checkoutFile(String attachmentId, String userId)
      throws AttachmentException {
    return checkoutFile(attachmentId, userId, null);
  }

  /**
   * Checkout a file to be updated by user
   *
   * @param attachmentId
   * @param userId
   * @param language
   * @return false if the attachment is already checkout - true if the attachment was successfully
   *         checked out.
   * @throws AttachmentException
   */
  public static boolean checkoutFile(String attachmentId, String userId,
      String language) throws AttachmentException {
    SilverTrace.debug("attachment", "AttachmentController.checkoutFile()",
        "root.MSG_GEN_ENTER_METHOD", "attachmentId = " + attachmentId + ", userId = " + userId);

    try {
      AttachmentDetail attachmentDetail = attachmentBm.getAttachmentByPrimaryKey(
          new AttachmentPK(attachmentId));
      if (attachmentDetail.isReadOnly()) {
        return attachmentDetail.getWorkerId().equals(userId);
      }
      // Check if user haven't check out another file with same name to prevent overwriting
      Iterator<AttachmentDetail> checkOutFiles = attachmentBm.getAttachmentsByWorkerId(
          userId).iterator();
      AttachmentDetail checkOutFile = null;
      while (checkOutFiles.hasNext()) {
        checkOutFile = checkOutFiles.next();
        if (checkOutFile.getLogicalName(language).equalsIgnoreCase(
            attachmentDetail.getLogicalName(language))) {
          return false;
        }
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

      AttachmentController.updateAttachment(attachmentDetail, false, false);
    } catch (Exception e) {
      throw new AttachmentRuntimeException(
          "AttachmentController.checkoutFile()",
          SilverpeasRuntimeException.ERROR, "attachment.CHECKOUT_FAILED", e);
    }
    return true;
  }

  /**
   * Get a UserDetail
   *
   * @param userId
   * @return the UserDetail.
   * @throws AttachmentException
   */
  public static UserDetail getUserDetail(String userId)
      throws AttachmentException {
    OrganizationController oc = new OrganizationController();
    return oc.getUserDetail(userId);
  }

  public static void cloneAttachments(AttachmentPK fromForeignKey,
      AttachmentPK toForeignKey) throws AttachmentException {
    Vector<AttachmentDetail> attachments = attachmentBm.getAttachmentsByPKAndParam(
        fromForeignKey, "Context", "Images");
    for (AttachmentDetail a : attachments) {
      AttachmentDetail clone = (AttachmentDetail) a.clone();
      // The file must be copied
      String physicalName = Long.toString(System.currentTimeMillis()) + "." + a.getExtension();
      clone.setPhysicalName(physicalName);
      copyFileOnServer(a, clone);
      clone.setForeignKey(toForeignKey);
      clone.setCloneId(a.getPK().getId());
      clone = attachmentBm.createAttachment(clone);
    }
  }

  public static void mergeAttachments(AttachmentPK fromForeignKey,
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

  private static AttachmentDetail searchClone(
      AttachmentDetail attachmentDetail, Vector<AttachmentDetail> clones) {
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

  private static void mergeAttachment(AttachmentDetail attachmentDetail,
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
  }

  public static void addXmlForm(AttachmentPK pk, String language,
      String xmlFormName) throws AttachmentException {
    attachmentBm.updateXmlForm(pk, language, xmlFormName);
  }

  public static void sortAttachments(List<AttachmentPK> attachmentPKs) throws AttachmentException {
    attachmentBm.sortAttachments(attachmentPKs);
  }
}
