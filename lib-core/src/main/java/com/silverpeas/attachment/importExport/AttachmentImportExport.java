/**
 * Copyright (C) 2000 - 2009 Silverpeas
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

/*
 * Created on 25 janv. 2005
 *
 */
package com.silverpeas.attachment.importExport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.silverpeas.form.AbstractForm;
import com.silverpeas.form.importExport.FormTemplateImportExport;
import com.silverpeas.form.importExport.XMLModelContentType;
import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentException;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Classe de gestion des attachments dans le moteur d'importExport de silverpeas.
 * @author sdevolder
 */
public class AttachmentImportExport {

  // Variables
  private static int BUFFER_SIZE = 1024;

  // Methodes
  /**
   * Methode utilisee par l'import massive du moteur d'importExport de silverpeaseffectuant la copie
   * de fichier ainsi que sa liaison avec une publication cible.
   * @param pubId - publication dans laquelle creer l'attachement
   * @param componentId - id du composant contenant la publication (necessaire pour determiner le
   * chemin physique du fichier importe)
   * @param attachmentDetail - objet contenant les details necessaires a la creation du fichier
   * importe et a sa liaison avec la publication
   * @throws AttachmentException
   */
  public void importAttachment(String pubId, String componentId,
      AttachmentDetail attachmentDetail, boolean indexIt) {
    importAttachment(pubId, componentId, attachmentDetail, indexIt, true);
  }

  public void importAttachment(String pubId, String componentId,
      AttachmentDetail attachmentDetail, boolean indexIt,
      boolean updateLogicalName) {
    this.copyFile(componentId, attachmentDetail, updateLogicalName);
    if (attachmentDetail.getSize() > 0)
      this.addAttachmentToPublication(pubId, componentId, attachmentDetail,
          AbstractForm.CONTEXT_FORM_FILE, indexIt);
  }

  public AttachmentDetail importWysiwygAttachment(String pubId,
      String componentId, AttachmentDetail attachmentDetail, String context) {
    AttachmentDetail a_detail = null;
    this.copyFileWysiwyg(componentId, attachmentDetail, context);
    if (attachmentDetail.getSize() > 0)
      a_detail = this.addAttachmentToPublication(pubId, componentId,
          attachmentDetail, context, false);
    return a_detail;
  }

  public List<AttachmentDetail> importAttachments(String pubId, String componentId,
      List<AttachmentDetail> attachments, String userId) {
    return importAttachments(pubId, componentId, attachments, userId, false);
  }

  public List<AttachmentDetail> importAttachments(String pubId, String componentId,
      List<AttachmentDetail> attachments, String userId, boolean indexIt) {
    List<AttachmentDetail> copiedAttachments = copyFiles(componentId, attachments);
    FormTemplateImportExport xmlIE = null;
    for (AttachmentDetail attDetail : copiedAttachments) {
      attDetail.setAuthor(userId);
      XMLModelContentType xmlContent = attDetail.getXMLModelContentType();
      if (xmlContent != null)
        attDetail.setXmlForm(xmlContent.getName());

      this.addAttachmentToPublication(pubId, componentId, attDetail,
          AbstractForm.CONTEXT_FORM_FILE, indexIt);

      // Store xml content
      try {
        if (xmlContent != null) {
          if (xmlIE == null)
            xmlIE = new FormTemplateImportExport();

          ForeignPK pk = new ForeignPK(attDetail.getPK().getId(), attDetail
              .getPK().getInstanceId());
          xmlIE.importXMLModelContentType(pk, "Attachment", xmlContent,
              attDetail.getAuthor());
        }
      } catch (Exception e) {
        SilverTrace.error("attachment",
            "AttachmentImportExport.importAttachments()",
            "root.MSG_GEN_PARAM_VALUE", e);
      }
    }
    return copiedAttachments;
  }

  private AttachmentDetail copyFile(String componentId,
      AttachmentDetail a_Detail, boolean updateLogicalName) {
    String path = getPath(componentId);
    return copyFile(componentId, a_Detail, path, updateLogicalName);
  }

  private AttachmentDetail copyFileWysiwyg(String componentId,
      AttachmentDetail a_Detail, String context) {
    String path = getPathWysiwyg(componentId, context);
    a_Detail.setContext(context);
    return copyFile(componentId, a_Detail, path);
  }

  public List<AttachmentDetail> copyFiles(String componentId, List<AttachmentDetail> attachments) {
    return copyFiles(componentId, attachments, getPath(componentId));
  }

  public List<AttachmentDetail> copyFiles(String componentId, List<AttachmentDetail> attachments,
      String path) {
    List<AttachmentDetail> copiedAttachments = new ArrayList<AttachmentDetail>();
    for (AttachmentDetail attDetail : attachments) {
      this.copyFile(componentId, attDetail, path);
      if (attDetail.getSize() != 0) {
        copiedAttachments.add(attDetail);
      }
    }
    return copiedAttachments;
  }

  /**
   * M�thode de copie de fichier utilis�e par la m�thode
   * importAttachement(String,String,AttachmentDetail)
   * @param componentId - id du composant contenant la publication � laquelle est destin�
   * l'attachement
   * @param a_Detail - objet contenant les informations sur le fichier � copier
   * @param path - chemin o� doit �tre copi� le fichier
   * @return renvoie l'objet des informations sur le fichier � copier compl�t� par les nouvelles
   * donn�es issues de la copie
   * @throws AttachmentException
   */
  public AttachmentDetail copyFile(String componentId,
      AttachmentDetail a_Detail, String path) {
    return copyFile(componentId, a_Detail, path, true);
  }

  public AttachmentDetail copyFile(String componentId,
      AttachmentDetail a_Detail, String path, boolean updateLogicalName) {

    String fileToUpload = a_Detail.getPhysicalName();

    // Pr�paration des param�tres du fichier � creer
    String logicalName = fileToUpload.substring(fileToUpload
        .lastIndexOf(File.separator) + 1);
    String type = logicalName.substring(logicalName.lastIndexOf(".") + 1,
        logicalName.length());
    String mimeType = AttachmentController.getMimeType(logicalName);
    String physicalName = System.currentTimeMillis() + "." + type;
    File fileToCreate = new File(path + physicalName);
    while (fileToCreate.exists()) {
      SilverTrace.info("attachment", "AttachmentImportExport.copyFile()",
          "root.MSG_GEN_PARAM_VALUE", "fileToCreate already exists="
          + fileToCreate.getAbsolutePath());

      // To prevent overwriting
      physicalName = new Long(new Date().getTime()).toString() + "." + type;
      fileToCreate = new File(path + physicalName);
    }
    SilverTrace.info("attachment", "AttachmentImportExport.copyFile()",
        "root.MSG_GEN_PARAM_VALUE", "fileName=" + logicalName);

    long size = 0;
    try {
      // Copie du fichier dans silverpeas
      size = copyFileToDisk(fileToUpload, fileToCreate);
    } catch (Exception e) {
      SilverTrace.error("attachment", "AttachmentImportExport.copyFile()",
          "attachment.EX_FILE_COPY_ERROR", e);
    }

    // Compl�ments sur les attachmentDetail
    a_Detail.setSize(size);
    a_Detail.setType(mimeType);
    a_Detail.setPhysicalName(physicalName);
    if (updateLogicalName) {
      a_Detail.setLogicalName(logicalName);
    }

    AttachmentPK pk = new AttachmentPK("unknown", "useless", componentId);
    a_Detail.setPK(pk);

    return a_Detail;
  }

  private long copyFileToDisk(String from, File to) throws AttachmentException {
    FileInputStream fl_in = null;
    FileOutputStream fl_out = null;

    long size = 0;
    try {
      fl_in = new FileInputStream(from);
    } catch (FileNotFoundException ex) {
      throw new AttachmentException("AttachmentsType.copyFileToDisk()",
          SilverpeasException.ERROR, "attachment.EX_FILE_TO_UPLOAD_NOTFOUND",
          ex);
    }
    try {
      fl_out = new FileOutputStream(to);

      byte[] data = new byte[BUFFER_SIZE];
      int bytes_readed = fl_in.read(data);
      while (bytes_readed > 0) {
        size += bytes_readed;
        fl_out.write(data, 0, bytes_readed);
        bytes_readed = fl_in.read(data);
      }
      fl_in.close();
      fl_out.close();
    } catch (Exception ex) {
      throw new AttachmentException("AttachmentsType.copyFileToDisk()",
          SilverpeasException.ERROR, "attachment.EX_FILE_COPY_ERROR", ex);
    }
    return size;
  }

  /**
   * M�thode utilis�e par la m�thode importAttachement(String,String,AttachmentDetail) pour creer un
   * attachement sur la publication cr��e dans la m�thode cit�e.
   * @param pubId - id de la publication dans laquelle cr�er l'attachment
   * @param componentId - id du composant contenant la publication
   * @param a_Detail - obejt contenant les informations n�c�ssaire � la cr�ation de l'attachment
   * @return AttachmentDetail cr��
   */
  private AttachmentDetail addAttachmentToPublication(String pubId,
      String componentId, AttachmentDetail a_Detail, String context,
      boolean indexIt) {

    AttachmentDetail ad_toCreate = null;
    int incrementSuffixe = 0;
    AttachmentPK atPK = new AttachmentPK(null, componentId);
    AttachmentPK foreignKey = new AttachmentPK(pubId, componentId);
    Vector<AttachmentDetail> attachments = AttachmentController
        .searchAttachmentByCustomerPK(foreignKey);
    int i = 0;

    String logicalName = a_Detail.getLogicalName();
    String userId = a_Detail.getAuthor();
    String updateRule = a_Detail.getImportUpdateRule();
    if (updateRule == null || updateRule.length() == 0
        || updateRule.equalsIgnoreCase("null"))
      updateRule = AttachmentDetail.IMPORT_UPDATE_RULE_ADD;

    SilverTrace.info("attachment",
        "AttachmentImportExport.addAttachmentToPublication()",
        "root.MSG_GEN_PARAM_VALUE", "updateRule=" + updateRule);

    // V�rification s'il existe un attachment de m�me nom, si oui, ajout
    // d'un
    // suffixe au nouveau fichier
    while (i < attachments.size()) {
      ad_toCreate = attachments.get(i);
      if (ad_toCreate.getLogicalName().equals(logicalName))// si les tailles
      // sont diff�rentes,
      // on
      {
        if ((ad_toCreate.getSize() != a_Detail.getSize())
            && updateRule
            .equalsIgnoreCase(AttachmentDetail.IMPORT_UPDATE_RULE_ADD)) {
          logicalName = a_Detail.getLogicalName();
          int Extposition = logicalName.lastIndexOf(".");
          if (Extposition != -1)
            logicalName = logicalName.substring(0, Extposition) + "_"
                + (++incrementSuffixe)
                + logicalName.substring(Extposition, logicalName.length());
          else
            logicalName += "_" + (++incrementSuffixe);
          // On reprend la boucle au d�but pour v�rifier que le nom
          // g�n�r� n est
          // pas lui meme un autre nom d'attachment de la publication
          i = 0;
        } else {// on efface l'ancien fichier joint et on stoppe la boucle
          AttachmentController.deleteAttachment(ad_toCreate);
          break;
        }
      } else
        i++;
    }
    a_Detail.setLogicalName(logicalName);

    // On instancie l'objet attachment � creer
    ad_toCreate = new AttachmentDetail(atPK, a_Detail.getPhysicalName(),
        a_Detail.getLogicalName(), null, a_Detail.getType(),
        a_Detail.getSize(), context, new Date(), foreignKey, userId);
    ad_toCreate.setTitle(a_Detail.getTitle());
    ad_toCreate.setInfo(a_Detail.getInfo());
    ad_toCreate.setXmlForm(a_Detail.getXmlForm());
    AttachmentController.createAttachment(ad_toCreate, indexIt);

    return ad_toCreate;
  }

  /**
   * Methode de recuperation des attachements et de copie des fichiers dans le dossier d'exportation
   * @param pk - PrimaryKey de l'obijet dont on veut les attachments?
   * @param exportPath - Repertoire dans lequel copier les fichiers
   * @param relativeExportPath chemin relatif du fichier copie
   * @return une liste des attachmentDetail trouves
   */
  public Vector<AttachmentDetail> getAttachments(WAPrimaryKey pk, String exportPath,
      String relativeExportPath, String extensionFilter) {

    // Recuperation des attachments
    Vector<AttachmentDetail> listAttachment = AttachmentController
        .searchAttachmentByCustomerPK(pk);
    Vector<AttachmentDetail> listToReturn = new Vector<AttachmentDetail>();
    if (listAttachment != null && listAttachment.isEmpty())// Si on
      // recoit
      // une liste
      // vide, on
      // retourne
      // null
      listAttachment = null;
    if (listAttachment != null) {
      // Pour chaque attachment trouv�, on copie le fichier dans le dossier
      // d'exportation
      for (AttachmentDetail attDetail : listAttachment) {
        if (!attDetail.getContext().equals(AbstractForm.CONTEXT_FORM_FILE)) {
          // ce n est pas un fichier joint mais un fichier appartenant surement
          // au wysiwyg si le context
          // est different de images et ce quelque soit le type du fichier
          continue;// on ne copie pas le fichier
        }

        if (extensionFilter == null) {
          try {
            copyAttachment(attDetail, pk, exportPath);

            // Le nom physique correspond maintenant au fichier copie
            attDetail.setPhysicalName(relativeExportPath
                + File.separator
                + FileServerUtils
                .replaceAccentChars(attDetail.getLogicalName()));

          } catch (IOException ex) {
            // TODO: gerer ou ne pas gerer telle est la question
            ex.printStackTrace();
          }

          listToReturn.add(attDetail);

        } else if (attDetail.getExtension().equalsIgnoreCase(extensionFilter)) {
          try {
            copyAttachment(attDetail, pk, exportPath);
            // Le nom physique correspond maintenant au fichier copi
            attDetail.setLogicalName(FileServerUtils
                .replaceAccentChars(attDetail.getLogicalName()));

          } catch (Exception ex) {
            // TODO: gerer ou ne pas gerer telle est la question
            ex.printStackTrace();
          }

          listToReturn.add(attDetail);
        }
      }
    }

    return listToReturn;
  }

  private void copyAttachment(AttachmentDetail attDetail, WAPrimaryKey pk,
      String exportPath) throws FileNotFoundException, IOException {
    String fichierJoint = AttachmentController.createPath(pk.getInstanceId(),
        attDetail.getContext())
        + File.separator + attDetail.getPhysicalName();

    String fichierJointExport = exportPath + File.separator
        + FileServerUtils.replaceAccentChars(attDetail.getLogicalName());

    FileRepositoryManager.copyFile(fichierJoint, fichierJointExport);
  }

  /**
   * M�thode r�cup�rant le chemin d'acc�s au dossier de stockage des fichiers import�s dans un
   * composant.
   * @param componentId - id du composant dont on veut r�cuperer le chemin de stockage de ses
   * fichiers import�s
   * @return le chemin recherch�
   */
  private String getPath(String componentId) {
    String path = AttachmentController.createPath(componentId,
        AbstractForm.CONTEXT_FORM_FILE);
    SilverTrace.info("attachment", "AttachmentImportExport.getPath()",
        "root.MSG_GEN_PARAM_VALUE", "path=" + path);
    return path;
  }

  private String getPathWysiwyg(String componentId, String context) {
    String path = AttachmentController.createPath(componentId, context);
    SilverTrace.info("attachment", "AttachmentImportExport.getPath()",
        "root.MSG_GEN_PARAM_VALUE", "path=" + path);
    return path;
  }
}