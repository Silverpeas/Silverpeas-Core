/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.importexport.control;

import org.silverpeas.core.contribution.content.form.XMLField;
import org.silverpeas.core.importexport.model.ImportExportException;
import org.silverpeas.core.importexport.model.RepositoriesType;
import org.silverpeas.core.importexport.model.RepositoryType;
import org.silverpeas.core.importexport.report.ImportReportManager;
import org.silverpeas.core.importexport.report.MassiveReport;
import org.silverpeas.core.importexport.report.UnitReport;
import org.silverpeas.core.pdc.pdc.importexport.PdcImportExport;
import org.silverpeas.core.importexport.publication.PublicationContentType;
import org.silverpeas.core.importexport.publication.XMLModelContentType;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.EntityArrays;
import org.apache.commons.lang3.text.translate.LookupTranslator;
import org.silverpeas.core.contribution.attachment.ActifyDocumentProcessor;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.HistorisedDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.model.UnlockContext;
import org.silverpeas.core.contribution.attachment.model.UnlockOption;
import org.silverpeas.core.contribution.attachment.util.AttachmentSettings;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.importexport.attachment.AttachmentDetail;
import org.silverpeas.core.importexport.attachment.AttachmentImportExport;
import org.silverpeas.core.importexport.attachment.AttachmentPK;
import org.silverpeas.core.importexport.versioning.DocumentVersion;
import org.silverpeas.core.importexport.versioning.VersioningImportExport;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.io.media.MetaData;
import org.silverpeas.core.io.media.MetadataExtractor;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.error.SilverpeasTransverseErrorUtil;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.mail.extractor.Extractor;
import org.silverpeas.core.mail.extractor.Mail;
import org.silverpeas.core.mail.extractor.MailAttachment;
import org.silverpeas.core.mail.extractor.MailExtractor;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.silverpeas.core.contribution.attachment.AttachmentServiceProvider.getAttachmentService;
import static org.silverpeas.core.contribution.attachment.model.DocumentType.attachment;

/**
 * Classe manager des importations massives du moteur d'importExport de silverPeas
 *
 * @author sdevolder
 */
@Singleton
public class RepositoriesTypeManager {

  public static final CharSequenceTranslator ESCAPE_ISO8859_1 = new LookupTranslator(
      EntityArrays.ISO8859_1_ESCAPE());

  @Inject
  private PdcImportExport pdcImportExport;

  protected RepositoriesTypeManager() {

  }

  /**
   * Méthode métier du moteur d'importExport créant toutes les publications massives définies au
   * niveau du fichier d'import xml passé en paramètre au moteur d'importExport.
   *
   * @param repositoriesType - objet mappé par castor contenant toutes les informations de création
   * des publications du path défini
   * @return un objet ComponentReport contenant les informations de création des publications
   * unitaires et nécéssaire au rapport détaillé
   */
  public void processImport(RepositoriesType repositoriesType, ImportSettings settings,
      ImportReportManager reportManager) {
    List<RepositoryType> listRep_Type = repositoriesType.getListRepositoryType();
    Iterator<RepositoryType> itListRep_Type = listRep_Type.iterator();

    while (itListRep_Type.hasNext()) {
      RepositoryType rep_Type = itListRep_Type.next();

      String componentId = rep_Type.getComponentId();
      int topicId = rep_Type.getTopicId();
      String sPath = rep_Type.getPath();

      // Création du rapport de repository
      MassiveReport massiveReport = new MassiveReport();
      reportManager.addMassiveReport(massiveReport, componentId);
      massiveReport.setRepositoryPath(sPath);

      ComponentInst componentInst = OrganizationControllerProvider.getOrganisationController()
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
              importFile(null, file, reportManager, massiveReport, gedIE, pdcImportExport, settings);
            } else if (file.isDirectory()) {
              switch (rep_Type.getMassiveTypeInt()) {
                case RepositoryType.NO_RECURSIVE:
                  // on ne fait rien
                  break;
                case RepositoryType.RECURSIVE_NOREPLICATE:
                  // traitement récursif spécifique
                  settings.setPathToImport(file.getAbsolutePath());
                  processImportRecursiveNoReplicate(reportManager, massiveReport, gedIE, pdcImportExport,
                      settings);
                  break;
                case RepositoryType.RECURSIVE_REPLICATE:
                  try {
                    NodeDetail nodeDetail = gedIE.addSubTopicToTopic(file, topicId, massiveReport);
                    // Traitement récursif spécifique
                    settings.setPathToImport(file.getAbsolutePath());
                    settings.setFolderId(nodeDetail.getNodePK().getId());
                    processImportRecursiveReplicate(reportManager, massiveReport, gedIE,
                        pdcImportExport, settings);
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

  private PublicationDetail importFile(final PublicationDetail previousSavedPublication, File file,
      ImportReportManager reportManager, MassiveReport massiveReport, GEDImportExport gedIE,
      PdcImportExport pdcIE, ImportSettings settings) {
    String componentId = gedIE.getCurrentComponentId();
    UserDetail userDetail = gedIE.getCurrentUserDetail();
    PublicationDetail pubDetailToSave = null;
    try {
      // Création du rapport unitaire
      UnitReport unitReport = new UnitReport();
      massiveReport.addUnitReport(unitReport);

      // Check the file size
      long maximumFileSize = FileRepositoryManager.getUploadMaximumFileSize();
      long fileSize = file.length();
      if (fileSize <= 0L) {
        unitReport.setError(UnitReport.ERROR_NOT_EXISTS_OR_INACCESSIBLE_FILE);
        reportManager.addNumberOfFilesNotImported(1);
        return null;
      } else if (fileSize > maximumFileSize) {
        unitReport.setError(UnitReport.ERROR_FILE_SIZE_EXCEEDS_LIMIT);
        reportManager.addNumberOfFilesNotImported(1);
        return null;
      }

      if (!settings.mustCreateOnePublicationForAllFiles() || previousSavedPublication == null) {

        // On récupére les infos nécéssaires à la création de la publication
        pubDetailToSave =
            PublicationImportExport.convertFileInfoToPublicationDetail(file, settings);
        pubDetailToSave.setPk(new PublicationPK("unknown", "useless", componentId));
        if ((settings.isDraftUsed() && pdcIE.isClassifyingMandatory(componentId)) || settings.
            isDraftUsed()) {
          pubDetailToSave.setStatus(PublicationDetail.DRAFT);
          pubDetailToSave.setStatusMustBeChecked(false);
        }
        // Création de la publication
        pubDetailToSave =
            gedIE.createPublicationForMassiveImport(unitReport, pubDetailToSave, settings);
        unitReport.setLabel(pubDetailToSave.getPK().getId());
      } else {
        pubDetailToSave = previousSavedPublication;
      }

      if (!settings.mustCreateOnePublicationForAllFiles() && FileUtil.isMail(file.getName())) {
        // if imported file is an e-mail, its textual content is saved in a dedicated form
        // and attached files are attached to newly created publication
        processMailContent(pubDetailToSave, file, reportManager, unitReport, gedIE, settings.
            isVersioningUsed());
      }

      // add attachment
      Date creationDate = new Date();
      if (settings.useFileDates() && !settings.mustCreateOnePublicationForAllFiles()) {
        if (pubDetailToSave.getUpdateDate() != null) {
          creationDate = pubDetailToSave.getUpdateDate();
        } else {
          creationDate = pubDetailToSave.getCreationDate();
        }
      }

      final SimpleDocument document =
          handleFileToAttach(userDetail, componentId, pubDetailToSave.getPK().getId(), null,
              attachment, file, settings.getContentLanguage(), creationDate,
              pubDetailToSave.isIndexable(), settings.isVersioningUsed(),
              settings.getVersionType() == DocumentVersion.TYPE_PUBLIC_VERSION);

      reportManager.addNumberOfFilesProcessed(1);
      reportManager.addImportedFileSize(document.getSize(), componentId);

    } catch (Exception ex) {
      massiveReport.setError(UnitReport.ERROR_ERROR);
      SilverTrace
          .error("importExport", "RepositoriesTypeManager.importFile", "root.EX_NO_MESSAGE", ex);
      SilverpeasTransverseErrorUtil.throwTransverseErrorIfAny(ex, I18NHelper.defaultLanguage);
    }
    return pubDetailToSave;
  }

  /**
   * Handles the creation or modification of an attached file on the aimed resource.
   */
  public static SimpleDocument handleFileToAttach(UserDetail currentUser, String componentId,
      String resourceId, String oldSilverpeasId, final DocumentType documentType, File file,
      String contentLanguage, final Date creationDate, boolean hasToBeIndexed,
      boolean isComponentVersionActivated, boolean publicVersionRequired) throws IOException {
    final String fileName = file.getName();
    final long fileSize = file.length();
    boolean publicVersion = isComponentVersionActivated && publicVersionRequired;

    final String mimeType = FileUtil.getMimeType(fileName);
    final SimpleDocumentPK documentPK = new SimpleDocumentPK(null, componentId);
    if (StringUtil.isDefined(oldSilverpeasId)) {
      if (StringUtil.isInteger(oldSilverpeasId)) {
        documentPK.setOldSilverpeasId(Long.parseLong(oldSilverpeasId));
      } else {
        documentPK.setId(oldSilverpeasId);
      }
    }

    SimpleDocument document = getAttachmentService().
        findExistingDocument(documentPK, fileName, new ForeignPK(resourceId, componentId),
            contentLanguage);

    final boolean needCreation = (document == null || !document.isVersioned());
    if (needCreation) {
      if (isComponentVersionActivated) {
        document = new HistorisedDocument(documentPK, resourceId, 0, currentUser.getId(),
            new SimpleAttachment(fileName, contentLanguage, fileName, "", fileSize, mimeType,
                currentUser.getId(), creationDate, null));
        document.setPublicDocument(publicVersion);
      } else {
        document = new SimpleDocument(new SimpleDocumentPK(null, componentId), resourceId, 0, false,
            new SimpleAttachment(fileName, contentLanguage, null, null, fileSize, mimeType,
                currentUser.getId(), creationDate, null));
      }
      document.setDocumentType(documentType);
      setMetadata(document, file);
    }

    if (needCreation) {
      boolean notifying = !document.isVersioned() || publicVersion;
      document =
          getAttachmentService().createAttachment(document, file, hasToBeIndexed, notifying);
    } else {
      document.setLanguage(contentLanguage);
      document.setPublicDocument(publicVersion);
      document.edit(currentUser.getId());
      getAttachmentService().updateAttachment(document, file, hasToBeIndexed, publicVersion);
      UnlockContext unlockContext =
          new UnlockContext(document.getId(), currentUser.getId(), contentLanguage, "");
      unlockContext.addOption(UnlockOption.UPLOAD);
      if (!publicVersion) {
        unlockContext.addOption(UnlockOption.PRIVATE_VERSION);
      }
      getAttachmentService().unlock(unlockContext);
    }

    // Specific case: 3d file to convert by Actify Publisher
    ActifyDocumentProcessor.getProcessor().process(document);

    return document;
  }

  /**
   * Sets the metadata from the physical file.
   * @param document the attachment.
   * @param file the physical file.
   */
  private static void setMetadata(SimpleDocument document, File file) {
    if (AttachmentSettings.isUseFileMetadataForAttachmentDataEnabled()) {
      final MetadataExtractor extractor = MetadataExtractor.get();
      final MetaData metadata = extractor.extractMetadata(file);
      document.setSize(file.length());
      document.setTitle(metadata.getTitle());
      document.setDescription(metadata.getSubject());
    }
  }

  private void processMailContent(PublicationDetail pubDetail, File file,
      ImportReportManager reportManager,
      UnitReport unitReport, GEDImportExport gedIE, boolean isVersioningUsed) throws
      ImportExportException {

    String componentId = gedIE.getCurrentComponentId();
    UserDetail userDetail = gedIE.getCurrentUserDetail();
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
      List<XMLField> fields = new ArrayList<>();
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
        List<AttachmentDetail> documents = new ArrayList<>();

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
              documents, pubDetail.isIndexable());
        } else {
          // classic mode
          AttachmentImportExport attachmentIE =
              new AttachmentImportExport(gedIE.getCurrentUserDetail());
          attachmentIE.importAttachments(pubDetail.getPK().getId(), componentId, documents,
              pubDetail.isIndexable());
        }
      } catch (Exception e) {
        SilverTrace.error("importExport", "RepositoriesTypeManager.processMailContent",
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
   */
  public void processImportRecursiveNoReplicate(ImportReportManager reportManager,
      MassiveReport massiveReport, GEDImportExport gedIE, PdcImportExport pdcIE,
      ImportSettings settings) {
    Iterator<File> itListcontenuPath = getPathContent(new File(settings.getPathToImport()));
    while (itListcontenuPath.hasNext()) {
      File file = itListcontenuPath.next();
      if (file.isFile()) {
        importFile(null, file, reportManager, massiveReport, gedIE, pdcIE, settings);
      } else if (file.isDirectory()) {
        // traitement récursif spécifique
        settings.setPathToImport(file.getAbsolutePath());
        processImportRecursiveNoReplicate(reportManager, massiveReport, gedIE, pdcIE, settings);
      }
    }
  }

  /**
   * Méthode récursive appelée dans le cas de l'importation massive récursive avec création de
   * nouveau topic: chaque sous dossier entrainera la création d'un topic de même nom.
   *
   * @param massiveReport - référence sur l'objet de rapport détaillé du cas import massif
   * permettant de le compléter quelque soit le niveau de récursivité.
   * @return the list of publications created by the import.
   * @throws ImportExportException
   */
  public List<PublicationDetail> processImportRecursiveReplicate(ImportReportManager reportManager,
      MassiveReport massiveReport, GEDImportExport gedIE, PdcImportExport pdcIE,
      ImportSettings settings)
      throws ImportExportException {
    List<PublicationDetail> publications = new ArrayList<>();
    File path = new File(settings.getPathToImport());
    Iterator<File> itListcontenuPath = getPathContent(path);
    PublicationDetail publication = null;
    while (itListcontenuPath.hasNext()) {
      File file = itListcontenuPath.next();
      if (file.isFile()) {
        publication =
            importFile(publication, file, reportManager, massiveReport, gedIE, pdcIE, settings);
        if (publication != null &&
            (!settings.mustCreateOnePublicationForAllFiles() || publications.isEmpty())) {
          publications.add(publication);
        }
      } else if (file.isDirectory()) {
        NodeDetail nodeDetail =
            gedIE.addSubTopicToTopic(file, Integer.valueOf(settings.getFolderId()), massiveReport);
        // massiveReport.addOneTopicCreated();
        // Traitement récursif spécifique
        ImportSettings recursiveSettings = settings.clone();
        recursiveSettings.setPathToImport(file.getAbsolutePath());
        recursiveSettings.setFolderId(nodeDetail.getNodePK().getId());
        publications.addAll(processImportRecursiveReplicate(reportManager, massiveReport, gedIE,
            pdcIE, recursiveSettings));
      }
    }
    return publications;
  }

  private Iterator<File> getPathContent(File path) {
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
    List<File> listFile = new ArrayList<>();
    if (listFileName == null) {
      return null;
    }
    for (String aListFileName : listFileName) {
      listFile.add(new File(path + File.separator + aListFileName));
    }
    return listFile;
  }
}
