/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.importExport.control;

import com.silverpeas.form.importExport.XMLField;
import com.silverpeas.importExport.model.ImportExportException;
import com.silverpeas.importExport.model.RepositoriesType;
import com.silverpeas.importExport.model.RepositoryType;
import com.silverpeas.importExport.report.ImportReportManager;
import com.silverpeas.importExport.report.MassiveReport;
import com.silverpeas.importExport.report.UnitReport;
import com.silverpeas.pdc.importExport.PdcImportExport;
import com.silverpeas.publication.importExport.PublicationContentType;
import com.silverpeas.publication.importExport.XMLModelContentType;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.EntityArrays;
import org.apache.commons.lang3.text.translate.LookupTranslator;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.HistorisedDocument;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.importExport.attachment.AttachmentDetail;
import org.silverpeas.importExport.attachment.AttachmentImportExport;
import org.silverpeas.importExport.attachment.AttachmentPK;
import org.silverpeas.importExport.versioning.DocumentVersion;
import org.silverpeas.importExport.versioning.VersioningImportExport;
import org.silverpeas.util.mail.Extractor;
import org.silverpeas.util.mail.Mail;
import org.silverpeas.util.mail.MailAttachment;
import org.silverpeas.util.mail.MailExtractor;

/**
 * Classe manager des importations massives du moteur d'importExport de silverPeas
 *
 * @author sdevolder
 */
public class RepositoriesTypeManager {

  public static final CharSequenceTranslator ESCAPE_ISO8859_1 = new LookupTranslator(
      EntityArrays.ISO8859_1_ESCAPE());

  /**
   * Méthode métier du moteur d'importExport créant toutes les publications massives définies au
   * niveau du fichier d'import xml passé en paramètre au moteur d'importExport.
   *
   * @param userDetail - contient les informations sur l'utilisateur du moteur d'importExport
   * @param repositoriesType - objet mappé par castor contenant toutes les informations de création
   * des publications du path défini
   * @return un objet ComponentReport contenant les informations de création des publications
   * unitaires et nécéssaire au rapport détaillé
   */
  public void processImport(RepositoriesType repositoriesType, ImportSettings settings,
      ImportReportManager reportManager) {
    List<RepositoryType> listRep_Type = repositoriesType.getListRepositoryType();
    Iterator<RepositoryType> itListRep_Type = listRep_Type.iterator();
    AttachmentImportExport attachmentIE = new AttachmentImportExport();
    VersioningImportExport versioningIE = new VersioningImportExport(settings.getUser());
    PdcImportExport pdcIE = new PdcImportExport();

    while (itListRep_Type.hasNext()) {
      RepositoryType rep_Type = itListRep_Type.next();

      String componentId = rep_Type.getComponentId();
      int topicId = rep_Type.getTopicId();
      String sPath = rep_Type.getPath();

      // Création du rapport de repository
      MassiveReport massiveReport = new MassiveReport();
      reportManager.addMassiveReport(massiveReport, componentId);
      massiveReport.setRepositoryPath(sPath);

      ComponentInst componentInst = OrganisationControllerFactory.getOrganisationController()
          .getComponentInst(componentId);
      if (componentInst == null) {
        // le composant n'existe pas
        massiveReport.setError(UnitReport.ERROR_NOT_EXISTS_COMPONENT);
      } else {
        reportManager.setComponentName(componentId, componentInst.getLabel());

        File path = new File(sPath);
        if (!path.isDirectory()) {
          // La variable path ne peut contenir qu'un dossier
          massiveReport.setError(UnitReport.ERROR_NOT_EXISTS_OR_INACCESSIBLE_DIRECTORY);
        } else {
          GEDImportExport gedIE =
              ImportExportFactory.createGEDImportExport(settings.getUser(), componentId);

          Iterator<File> itListcontenuPath = getPathContent(path);
          while (itListcontenuPath.hasNext()) {
            File file = itListcontenuPath.next();
            if (file.isFile()) {
              settings.setFolderId(String.valueOf(topicId));
              importFile(file, reportManager, massiveReport, gedIE, pdcIE, settings);
            } else if (file.isDirectory()) {
              switch (rep_Type.getMassiveTypeInt()) {
                case RepositoryType.NO_RECURSIVE:
                  // on ne fait rien
                  break;
                case RepositoryType.RECURSIVE_NOREPLICATE:
                  // traitement récursif spécifique
                  settings.setPathToImport(file.getAbsolutePath());
                  processImportRecursiveNoReplicate(reportManager, massiveReport, gedIE,
                      attachmentIE, versioningIE, pdcIE, settings);
                  break;
                case RepositoryType.RECURSIVE_REPLICATE:
                  try {
                    NodeDetail nodeDetail = gedIE.addSubTopicToTopic(file, topicId, massiveReport);
                    // Traitement récursif spécifique
                    settings.setPathToImport(file.getAbsolutePath());
                    settings.setFolderId(nodeDetail.getNodePK().getId());
                    processImportRecursiveReplicate(reportManager, massiveReport, gedIE, pdcIE,
                        settings);
                  } catch (ImportExportException ex) {
                    massiveReport.setError(UnitReport.ERROR_NOT_EXISTS_OR_INACCESSIBLE_DIRECTORY);
                  }
                  break;
              }
            }
          }
        }
      }
    }
  }

  private PublicationDetail importFile(File file, ImportReportManager reportManager,
      MassiveReport massiveReport, GEDImportExport gedIE, PdcImportExport pdcIE,
      ImportSettings settings) {
    SilverTrace.debug("importExport", "RepositoriesTypeManager.importFile",
        "root.MSG_GEN_ENTER_METHOD", "file = " + file.getName());
    String componentId = gedIE.getCurrentComponentId();
    UserDetail userDetail = gedIE.getCurentUserDetail();
    PublicationDetail pubDetailToCreate = null;
    try {
      // Création du rapport unitaire
      UnitReport unitReport = new UnitReport();
      massiveReport.addUnitReport(unitReport);

      // On récupére les infos nécéssaires à la création de la publication
      pubDetailToCreate = PublicationImportExport.convertFileInfoToPublicationDetail(file, settings);
      pubDetailToCreate.setPk(new PublicationPK("unknown", "useless", componentId));
      if ((settings.isDraftUsed() && pdcIE.isClassifyingMandatory(componentId)) || settings.
          isDraftUsed()) {
        pubDetailToCreate.setStatus(PublicationDetail.DRAFT);
        pubDetailToCreate.setStatusMustBeChecked(false);
      }
      SilverTrace.debug("importExport", "RepositoriesTypeManager.importFile",
          "root.MSG_GEN_PARAM_VALUE", "pubDetailToCreate.status = " + pubDetailToCreate.getStatus());

      // Création de la publication
      pubDetailToCreate = gedIE.createPublicationForMassiveImport(unitReport, pubDetailToCreate,
          settings);

      SilverTrace.debug("importExport", "RepositoriesTypeManager.importFile",
          "root.MSG_GEN_PARAM_VALUE", "pubDetailToCreate created");

      if (FileUtil.isMail(file.getName())) {
        // if imported file is an e-mail, its textual content is saved in a dedicated form
        // and attached files are attached to newly created publication
        processMailContent(pubDetailToCreate, file, reportManager, unitReport, gedIE, settings.
            isVersioningUsed());
      }

      // add attachment
      SimpleDocument document;
      SimpleDocumentPK pk = new SimpleDocumentPK(null, componentId);
      if (settings.isVersioningUsed()) {
        document = new HistorisedDocument();
        document.setPublicDocument(settings.getVersionType() == DocumentVersion.TYPE_PUBLIC_VERSION);
      } else {
        document = new SimpleDocument();
      }
      document.setPK(pk);
      document.setFile(new SimpleAttachment());
      document.setFilename(file.getName());
      document.setSize(file.length());
      document.getFile().setCreatedBy(userDetail.getId());
      document.setCreated(new Date());
      document.setForeignId(pubDetailToCreate.getPK().getId());
      document.setContentType(FileUtil.getMimeType(file.getName()));
      if (document.getSize() > 0L) {
        AttachmentServiceFactory.getAttachmentService()
            .createAttachment(document, file, pubDetailToCreate.isIndexable(), false);
        reportManager.addNumberOfFilesProcessed(1);
        reportManager.addImportedFileSize(document.getSize(), componentId);
      } else {
        unitReport.setError(UnitReport.ERROR_NOT_EXISTS_OR_INACCESSIBLE_FILE);
        reportManager.addNumberOfFilesNotImported(1);
      }
    } catch (Exception ex) {
      massiveReport.setError(UnitReport.ERROR_ERROR);
      SilverTrace
          .error("importExport", "RepositoriesTypeManager.importFile()", "root.EX_NO_MESSAGE", ex);
    }
    return pubDetailToCreate;
  }

  private void processMailContent(PublicationDetail pubDetail, File file,
      ImportReportManager reportManager,
      UnitReport unitReport, GEDImportExport gedIE, boolean isVersioningUsed) throws
      ImportExportException {

    String componentId = gedIE.getCurrentComponentId();
    UserDetail userDetail = gedIE.getCurentUserDetail();
    MailExtractor extractor = null;
    Mail mail = null;
    try {
      extractor = Extractor.getExtractor(file);
      mail = extractor.getMail();
    } catch (Exception e) {
      SilverTrace.error("importExport",
          "RepositoriesTypeManager.processMailContent",
          "importExport.EX_CANT_EXTRACT_MAIL_DATA", e);
    }
    if (mail != null) {
      // save mail data into dedicated form
      String content = mail.getBody();
      PublicationContentType pubContent = new PublicationContentType();
      XMLModelContentType modelContent = new XMLModelContentType("mail");
      pubContent.setXMLModelContentType(modelContent);
      List<XMLField> fields = new ArrayList<XMLField>();
      modelContent.setFields(fields);

      XMLField subject = new XMLField("subject", mail.getSubject());
      fields.add(subject);

      XMLField body = new XMLField("body", ESCAPE_ISO8859_1.translate(content));
      fields.add(body);

      XMLField date = new XMLField("date", DateUtil.getOutputDateAndHour(mail.getDate(), "fr"));
      fields.add(date);

      InternetAddress address = mail.getFrom();
      String from = "";
      if (StringUtil.isDefined(address.getPersonal())) {
        from += address.getPersonal() + " - ";
      }
      from += "<a href=\"mailto:" + address.getAddress() + "\">" + address.getAddress() + "</a>";
      XMLField fieldFROM = new XMLField("from", from);
      fields.add(fieldFROM);

      Address[] recipients = mail.getAllRecipients();
      String to = "";
      for (Address recipient : recipients) {
        InternetAddress ia = (InternetAddress) recipient;
        if (StringUtil.isDefined(ia.getPersonal())) {
          to += ia.getPersonal() + " - ";
        }
        to += "<a href=\"mailto:" + ia.getAddress() + "\">" + ia.getAddress() + "</a></br>";
      }
      XMLField fieldTO = new XMLField("to", to);
      fields.add(fieldTO);

      // save form
      gedIE.createPublicationContent(reportManager, unitReport, Integer.parseInt(pubDetail.getPK().
          getId()),
          pubContent, userDetail.getId(), null);

      // extract each file from mail...
      try {
        List<AttachmentDetail> documents = new ArrayList<AttachmentDetail>();

        List<MailAttachment> attachments = extractor.getAttachments();
        for (MailAttachment attachment : attachments) {
          if (attachment != null) {
            AttachmentDetail attDetail = new AttachmentDetail();
            AttachmentPK pk = new AttachmentPK("unknown", "useless", componentId);
            attDetail.setLogicalName(attachment.getName());
            attDetail.setPhysicalName(attachment.getPath());
            attDetail.setAuthor(userDetail.getId());
            attDetail.setSize(attachment.getSize());
            attDetail.setPK(pk);

            documents.add(attDetail);
          }
        }

        // ... and save it
        if (isVersioningUsed) {
          // versioning mode
          VersioningImportExport versioningIE = new VersioningImportExport(userDetail);
          versioningIE.importDocuments(pubDetail.getPK().getId(), componentId,
              documents, Integer.parseInt(userDetail.getId()),
              pubDetail.isIndexable());
        } else {
          // classic mode
          AttachmentImportExport attachmentIE = new AttachmentImportExport();
          attachmentIE.importAttachments(pubDetail.getPK().getId(), componentId, documents,
              userDetail.getId(), pubDetail.isIndexable());
        }
      } catch (Exception e) {
        SilverTrace.error("importExport", "RepositoriesTypeManager.processMailContent()",
            "root.EX_NO_MESSAGE", e);
      }
    }
  }

  /**
   * Méthode récursive appelée dans le cas de l'importation massive récursive sans création de
   * nouveau topic: toutes les publications crées le seront dans le thème passé en paramètre.
   *
   * @param massiveReport - référence sur l'objet de rapport détaillé du cas import massif
   * permettant de le compléter quelque soit le niveau de récursivité.
   * @param userDetail - contient les informations sur l'utilisateur du moteur d'importExport.
   * @param path - dossier correspondant au niveau de récursivité auquel on se trouve.
   * @param componentId - id du composant dans le lequel l'import massif est effectué.
   * @param topicId - id du thème dans lequel seront crées les éléments, l'id passé est toujours le
   * même dans le cas présent
   * @throws ImportExportException
   */
  public void processImportRecursiveNoReplicate(ImportReportManager reportManager,
      MassiveReport massiveReport, GEDImportExport gedIE, AttachmentImportExport attachmentIE,
      VersioningImportExport versioningIE, PdcImportExport pdcIE, ImportSettings settings) {
    Iterator<File> itListcontenuPath = getPathContent(new File(settings.getPathToImport()));
    while (itListcontenuPath.hasNext()) {
      File file = itListcontenuPath.next();
      if (file.isFile()) {
        importFile(file, reportManager, massiveReport, gedIE, pdcIE, settings);
      } else if (file.isDirectory()) {
        // traitement récursif spécifique
        settings.setPathToImport(file.getAbsolutePath());
        processImportRecursiveNoReplicate(reportManager, massiveReport, gedIE, attachmentIE,
            versioningIE, pdcIE, settings);
      }
    }
  }

  /**
   * Méthode récursive appelée dans le cas de l'importation massive récursive avec création de
   * nouveau topic: chaque sous dossier entrainera la création d'un topic de même nom.
   *
   * @param massiveReport - référence sur l'objet de rapport détaillé du cas import massif
   * permettant de le compléter quelque soit le niveau de récursivité.
   * @param userDetail - contient les informations sur l'utilisateur du moteur d'importExport.
   * @param path - dossier correspondant au niveau de récursivité auquel on se trouve.
   * @param componentId - id du composant dans le lequel l'import massif est effectué.
   * @param topicId - id du thème dans lequel seront crées les éléments du niveau de récursivité
   * auquel on se trouve.
   * @return the list of publications created by the import.
   * @throws ImportExportException
   */
  public List<PublicationDetail> processImportRecursiveReplicate(ImportReportManager reportManager,
      MassiveReport massiveReport, GEDImportExport gedIE, PdcImportExport pdcIE,
      ImportSettings settings)
      throws ImportExportException {
    List<PublicationDetail> publications = new ArrayList<PublicationDetail>();
    File path = new File(settings.getPathToImport());
    Iterator<File> itListcontenuPath = getPathContent(path);
    while (itListcontenuPath.hasNext()) {
      File file = itListcontenuPath.next();
      if (file.isFile()) {
        PublicationDetail publication = importFile(file, reportManager, massiveReport, gedIE, pdcIE,
            settings);
        if (publication != null) {
          publications.add(publication);
        }
      } else if (file.isDirectory()) {
        NodeDetail nodeDetail =
            gedIE.addSubTopicToTopic(file, Integer.valueOf(settings.getFolderId()), massiveReport);
        // massiveReport.addOneTopicCreated();
        // Traitement récursif spécifique
        settings.setPathToImport(file.getAbsolutePath());
        settings.setFolderId(nodeDetail.getNodePK().getId());
        publications.addAll(processImportRecursiveReplicate(reportManager, massiveReport, gedIE,
            pdcIE, settings));
      }
    }
    return publications;
  }

  private Iterator<File> getPathContent(File path) {
    SilverTrace.debug("importExport", "RepositoriesTypeManager.getPathContent",
        "root.MSG_GEN_ENTER_METHOD", "path = " + path.getPath());
    // Récupération du contenu du dossier
    String[] listContenuStringPath = path.list();

    // Tri alphabétique du contenu
    Arrays.sort(listContenuStringPath);

    List<File> listcontenuPath = convertListStringToListFile(listContenuStringPath, path.getPath());

    return listcontenuPath.iterator();
  }

  /**
   * Transforme la table des chaines de caractères de nom de fichier en une liste de fichiers pour
   * le chemin passé en paramètre
   *
   * @param listFileName - table des nom de fichier sous forme de chaine de caractères.
   * @param path - chemin des fichiers contenu dans les chaines de caractères.
   * @return renvoie une liste d'objets File pour les noms de fichiers passés en paramètres
   */
  private List<File> convertListStringToListFile(String[] listFileName, String path) {
    List<File> listFile = new ArrayList<File>();
    if (listFileName == null) {
      return null;
    }
    for (String aListFileName : listFileName) {
      listFile.add(new File(path + File.separator + aListFileName));
    }
    return listFile;
  }
}
