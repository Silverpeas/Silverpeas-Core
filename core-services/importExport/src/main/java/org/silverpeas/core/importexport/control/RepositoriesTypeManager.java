/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.importexport.control;

import org.apache.commons.text.translate.CharSequenceTranslator;
import org.apache.commons.text.translate.EntityArrays;
import org.apache.commons.text.translate.LookupTranslator;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.attachment.ActifyDocumentProcessor;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.HistorisedDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.model.UnlockContext;
import org.silverpeas.core.contribution.attachment.model.UnlockOption;
import org.silverpeas.core.contribution.attachment.util.AttachmentSettings;
import org.silverpeas.core.contribution.content.form.XMLField;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.importexport.attachment.AttachmentDetail;
import org.silverpeas.core.importexport.attachment.AttachmentImportExport;
import org.silverpeas.core.importexport.attachment.AttachmentPK;
import org.silverpeas.core.importexport.form.XMLModelContentType;
import org.silverpeas.core.importexport.model.ImportExportException;
import org.silverpeas.core.importexport.model.RepositoryType;
import org.silverpeas.core.importexport.publication.PublicationContentType;
import org.silverpeas.core.importexport.report.ImportReportManager;
import org.silverpeas.core.importexport.report.MassiveReport;
import org.silverpeas.core.importexport.report.UnitReport;
import org.silverpeas.core.importexport.versioning.DocumentVersion;
import org.silverpeas.core.importexport.versioning.VersioningImport;
import org.silverpeas.core.io.media.MetaData;
import org.silverpeas.core.io.media.MetadataExtractor;
import org.silverpeas.core.mail.MailException;
import org.silverpeas.core.mail.extractor.Extractor;
import org.silverpeas.core.mail.extractor.Mail;
import org.silverpeas.core.mail.extractor.MailAttachment;
import org.silverpeas.core.mail.extractor.MailExtractor;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.pdc.pdc.importexport.PdcImportExport;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.error.SilverpeasTransverseErrorUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
      EntityArrays.ISO8859_1_ESCAPE);

  @Inject
  private PdcImportExport pdcImportExport;

  protected RepositoriesTypeManager() {

  }

  /**
   * Méthode métier du moteur d'importExport créant toutes les publications massives définies au
   * niveau du fichier d'import xml passé en paramètre au moteur d'importExport.
   * @param repositoryTypes - objet contenant toutes les informations de création
   * des publications du path défini
   * @return un objet ComponentReport contenant les informations de création des publications
   * unitaires et nécéssaire au rapport détaillé
   */
  public void processImport(List<RepositoryType> repositoryTypes, ImportSettings settings,
      ImportReportManager reportManager) {
    for (final RepositoryType repType : repositoryTypes) {
      String componentId = repType.getComponentId();
      int topicId = repType.getTopicId();
      String sPath = repType.getPath();

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

          List<File> children = getPathContent(path);
          for (final File file : children) {
            processFile(file, topicId, settings, reportManager, repType, massiveReport, gedIE);
          }
        }
      }
    }
  }

  private void processFile(final File file, final int topicId, final ImportSettings settings,
      final ImportReportManager reportManager, final RepositoryType repType,
      final MassiveReport massiveReport, final GEDImportExport gedIE) {
    if (file.isFile()) {
      settings.setFolderId(String.valueOf(topicId));
      importFile(null, file, reportManager, massiveReport, gedIE, pdcImportExport, settings);
    } else if (file.isDirectory()) {
      switch (repType.getMassiveTypeInt()) {
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
            processImportRecursiveReplicate(reportManager, massiveReport, gedIE, pdcImportExport,
                settings);
          } catch (ImportExportException ex) {
            massiveReport.setError(UnitReport.ERROR_NOT_EXISTS_OR_INACCESSIBLE_DIRECTORY);
          }
          break;
        default:
          SilverLogger.getLogger(this)
              .warn("Unknown repository type: " + repType.getMassiveTypeInt());
          break;
      }
    }
  }

  private PublicationDetail importFile(final PublicationDetail previousSavedPublication, File file,
      ImportReportManager reportManager, MassiveReport massiveReport, GEDImportExport gedIE,
      PdcImportExport pdcIE, ImportSettings settings) {
    String componentId = gedIE.getCurrentComponentId();
    UserDetail userDetail = gedIE.getCurrentUserDetail();
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

      final PublicationDetail pubDetailToSave =
          fetchPublicationDetail(previousSavedPublication, componentId, file, gedIE, pdcIE,
              settings, unitReport);

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

      final AttachmentDescriptor descriptor = new AttachmentDescriptor().setCurrentUser(userDetail)
          .setComponentId(componentId)
          .setResourceId(pubDetailToSave.getPK().getId())
          .setDocumentType(attachment)
          .setFile(file)
          .setContentLanguage(settings.getContentLanguage())
          .setCreationDate(creationDate)
          .setHasToBeIndexed(pubDetailToSave.isIndexable())
          .setComponentVersionActivated(settings.isVersioningUsed())
          .setPublicVersionRequired(
              settings.getVersionType() == DocumentVersion.TYPE_PUBLIC_VERSION);
      final SimpleDocument document = handleFileToAttach(descriptor);

      reportManager.addNumberOfFilesProcessed(1);
      reportManager.addImportedFileSize(document.getSize(), componentId);
      return pubDetailToSave;
    } catch (Exception ex) {
      massiveReport.setError(UnitReport.ERROR_ERROR);
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
      SilverpeasTransverseErrorUtil.throwTransverseErrorIfAny(ex, I18NHelper.defaultLanguage);
      return null;
    }
  }

  private PublicationDetail fetchPublicationDetail(final PublicationDetail previousSavedPublication,
      final String componentId, final File file, final GEDImportExport gedIE,
      final PdcImportExport pdcIE, final ImportSettings settings, final UnitReport unitReport)
      throws PdcException {
    PublicationDetail pubDetailToSave;
    if (!settings.mustCreateOnePublicationForAllFiles() || previousSavedPublication == null) {

      // On récupére les infos nécéssaires à la création de la publication
      pubDetailToSave = PublicationImportExport.convertFileInfoToPublicationDetail(file, settings);
      pubDetailToSave.setPk(new PublicationPK("unknown", "useless", componentId));
      if ((settings.isDraftUsed() && pdcIE.isClassifyingMandatory(componentId)) || settings.
          isDraftUsed()) {
        pubDetailToSave.setStatus(PublicationDetail.DRAFT_STATUS);
        pubDetailToSave.setStatusMustBeChecked(false);
      }
      // Création de la publication
      pubDetailToSave =
          gedIE.createPublicationForMassiveImport(unitReport, pubDetailToSave, settings);
      unitReport.setLabel(pubDetailToSave.getPK().getId());
    } else {
      pubDetailToSave = previousSavedPublication;
    }
    return pubDetailToSave;
  }

  /**
   * Handles the creation or modification of an attached file on the aimed resource.
   */
  public static SimpleDocument handleFileToAttach(final AttachmentDescriptor descriptor)
      throws IOException {
    final String fileName = descriptor.getFile().getName();
    final long fileSize = descriptor.getFile().length();
    boolean publicVersion =
        descriptor.isComponentVersionActivated() && descriptor.isPublicVersionRequired();

    final String mimeType = FileUtil.getMimeType(fileName);
    final SimpleDocumentPK documentPK = new SimpleDocumentPK(null, descriptor.getComponentId());
    if (StringUtil.isDefined(descriptor.getOldSilverpeasId())) {
      if (StringUtil.isInteger(descriptor.getOldSilverpeasId())) {
        documentPK.setOldSilverpeasId(Long.parseLong(descriptor.getOldSilverpeasId()));
      } else {
        documentPK.setId(descriptor.getOldSilverpeasId());
      }
    }

    SimpleDocument document = getAttachmentService().
        findExistingDocument(documentPK, fileName, descriptor.getResourceReference(),
            descriptor.getContentLanguage());

    final boolean needCreation = (document == null || !document.isVersioned());
    if (needCreation) {
      if (descriptor.isComponentVersionActivated()) {
        document = new HistorisedDocument(documentPK, descriptor.resourceId, 0,
            descriptor.getCurrentUser().getId(),
            new SimpleAttachment(fileName, descriptor.getContentLanguage(), fileName, "", fileSize,
                mimeType, descriptor.getCurrentUser().getId(), descriptor.getCreationDate(), null));
        document.setPublicDocument(publicVersion);
      } else {
        document = new SimpleDocument(new SimpleDocumentPK(null, descriptor.getComponentId()),
            descriptor.getResourceId(), 0, false,
            new SimpleAttachment(fileName, descriptor.getContentLanguage(), null, null, fileSize,
                mimeType, descriptor.getCurrentUser().getId(), descriptor.getCreationDate(), null));
      }
      document.setDocumentType(descriptor.getDocumentType());
      setMetadata(document, descriptor.getFile());
    }

    if (needCreation) {
      boolean notifying = !document.isVersioned() || publicVersion;
      document = getAttachmentService().createAttachment(document, descriptor.getFile(),
          descriptor.isHasToBeIndexed(), notifying);
    } else {
      document.setLanguage(descriptor.getContentLanguage());
      document.setPublicDocument(publicVersion);
      document.edit(descriptor.getCurrentUser().getId());
      getAttachmentService().updateAttachment(document, descriptor.getFile(),
          descriptor.isHasToBeIndexed(), publicVersion);
      UnlockContext unlockContext =
          new UnlockContext(document.getId(), descriptor.getCurrentUser().getId(),
              descriptor.getContentLanguage(), "");
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
      SilverLogger.getLogger(this).error("Cannot extract mail data", e);
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
      StringBuilder to = new StringBuilder();
      for (Address recipient : recipients) {
        InternetAddress ia = (InternetAddress) recipient;
        if (StringUtil.isDefined(ia.getPersonal())) {
          to.append(ia.getPersonal()).append(" - ");
        }
        to.append("<a href=\"mailto:")
            .append(ia.getAddress())
            .append("\">")
            .append(ia.getAddress())
            .append("</a></br>");
      }
      XMLField fieldTO = new XMLField("to", to.toString());
      fields.add(fieldTO);

      // save form
      gedIE.createPublicationContent(reportManager, unitReport, Integer.parseInt(pubDetail.getPK().
          getId()), pubContent, userDetail.getId(), null);

      try {
        // extract each file from mail...
        List<AttachmentDetail> documents =
            extractAttachmentsFromMail(componentId, userDetail, extractor);
        // ... and save them
        saveAttachments(componentId, pubDetail, documents, userDetail, gedIE, isVersioningUsed);
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }
    }
  }

  private List<AttachmentDetail> extractAttachmentsFromMail(final String componentId,
      final UserDetail userDetail, final MailExtractor extractor) throws MailException {
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
    return documents;
  }

  private void saveAttachments(final String componentId, final PublicationDetail pubDetail,
      final List<AttachmentDetail> documents, final UserDetail userDetail,
      final GEDImportExport gedIE, final boolean isVersioningUsed) throws IOException {
    if (isVersioningUsed) {
      // versioning mode
      VersioningImport versioningIE = new VersioningImport(userDetail);
      versioningIE.importDocuments(pubDetail.getPK().getId(), componentId, documents,
          pubDetail.isIndexable());
    } else {
      // classic mode
      AttachmentImportExport attachmentIE =
          new AttachmentImportExport(gedIE.getCurrentUserDetail());
      attachmentIE.importAttachments(pubDetail.getPK().getId(), componentId, documents,
          pubDetail.isIndexable());
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
    List<File> children = getPathContent(new File(settings.getPathToImport()));
    for (File file : children) {
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
    List<File> children = getPathContent(path);
    PublicationDetail publication = null;
    for (File file : children) {
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

  private List<File> getPathContent(File path) {
    // Récupération du contenu du dossier
    String[] listContenuStringPath = path.list();

    // Tri alphabétique du contenu
    Arrays.sort(listContenuStringPath);

    return convertListStringToListFile(listContenuStringPath, path.getPath());
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
    if (listFileName != null) {
      for (String aListFileName : listFileName) {
        listFile.add(new File(path + File.separator + aListFileName));
      }
    }
    return listFile;
  }

  public static class AttachmentDescriptor {
    private UserDetail currentUser = null;
    private String componentId = null;
    private String resourceId = null;
    private String oldSilverpeasId = null;
    private DocumentType documentType = null;
    private File file = null;
    private String contentLanguage = null;
    private Date creationDate = null;
    private boolean hasToBeIndexed;
    private boolean isComponentVersionActivated;
    private boolean publicVersionRequired;

    public UserDetail getCurrentUser() {
      return currentUser;
    }

    public AttachmentDescriptor setCurrentUser(final UserDetail currentUser) {
      this.currentUser = currentUser;
      return this;
    }

    public String getComponentId() {
      return componentId;
    }

    public AttachmentDescriptor setComponentId(final String componentId) {
      this.componentId = componentId;
      return this;
    }

    public String getResourceId() {
      return resourceId;
    }

    public AttachmentDescriptor setResourceId(final String resourceId) {
      this.resourceId = resourceId;
      return this;
    }

    public String getOldSilverpeasId() {
      return oldSilverpeasId;
    }

    public AttachmentDescriptor setOldSilverpeasId(final String oldSilverpeasId) {
      this.oldSilverpeasId = oldSilverpeasId;
      return this;
    }

    public DocumentType getDocumentType() {
      return documentType;
    }

    public AttachmentDescriptor setDocumentType(final DocumentType documentType) {
      this.documentType = documentType;
      return this;
    }

    public File getFile() {
      return file;
    }

    public AttachmentDescriptor setFile(final File file) {
      this.file = file;
      return this;
    }

    public String getContentLanguage() {
      return contentLanguage;
    }

    public AttachmentDescriptor setContentLanguage(final String contentLanguage) {
      this.contentLanguage = contentLanguage;
      return this;
    }

    public Date getCreationDate() {
      return creationDate;
    }

    public AttachmentDescriptor setCreationDate(final Date creationDate) {
      this.creationDate = creationDate;
      return this;
    }

    public boolean isHasToBeIndexed() {
      return hasToBeIndexed;
    }

    public AttachmentDescriptor setHasToBeIndexed(final boolean hasToBeIndexed) {
      this.hasToBeIndexed = hasToBeIndexed;
      return this;
    }

    public boolean isComponentVersionActivated() {
      return isComponentVersionActivated;
    }

    public AttachmentDescriptor setComponentVersionActivated(
        final boolean componentVersionActivated) {
      isComponentVersionActivated = componentVersionActivated;
      return this;
    }

    public boolean isPublicVersionRequired() {
      return publicVersionRequired;
    }

    public AttachmentDescriptor setPublicVersionRequired(final boolean publicVersionRequired) {
      this.publicVersionRequired = publicVersionRequired;
      return this;
    }

    public ResourceReference getResourceReference() {
      return new ResourceReference(resourceId, componentId);
    }
  }
}
