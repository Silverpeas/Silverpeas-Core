/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.importexport.control;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.SimpleBookmark;
import org.apache.commons.io.IOUtils;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.importexport.admin.AdminImportExport;
import org.silverpeas.core.importexport.attachment.AttachmentDetail;
import org.silverpeas.core.importexport.coordinates.CoordinateImportExport;
import org.silverpeas.core.importexport.coordinates.CoordinatesPositionsType;
import org.silverpeas.core.importexport.model.ImportExportErrorHandler;
import org.silverpeas.core.importexport.model.ImportExportException;
import org.silverpeas.core.importexport.model.PublicationType;
import org.silverpeas.core.importexport.model.SilverPeasExchangeType;
import org.silverpeas.core.importexport.report.ExportPDFReport;
import org.silverpeas.core.importexport.report.ExportReport;
import org.silverpeas.core.importexport.report.HtmlExportGenerator;
import org.silverpeas.core.importexport.report.HtmlExportPublicationGenerator;
import org.silverpeas.core.importexport.report.ImportReport;
import org.silverpeas.core.importexport.report.ImportReportManager;
import org.silverpeas.core.importexport.report.UnitReport;
import org.silverpeas.core.node.coordinates.model.Coordinate;
import org.silverpeas.core.node.coordinates.model.CoordinatePoint;
import org.silverpeas.core.node.importexport.NodeImportExport;
import org.silverpeas.core.node.importexport.NodePositionType;
import org.silverpeas.core.node.importexport.NodeTreeType;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.model.NodeRuntimeException;
import org.silverpeas.core.pdc.pdc.importexport.PdcImportExport;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.WAAttributeValuePair;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.util.logging.SilverLogger;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import static java.io.File.separator;
import static org.silverpeas.core.util.Charsets.UTF_8;
import static org.silverpeas.core.util.CollectionUtil.isNotEmpty;

/**
 * Import/export of resources managed in Silverpeas
 * @author sDevolder.
 */
@Service
public class ImportExport extends AbstractExportProcess {

  private static final SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.importExport.settings.mapping");
  public static final String IFRAME_PUBLICATION = "publications";
  public static final String IFRAME_INDEX_PUBLICATIONS = "indexPublications";
  public static final int EXPORT_FULL = 0;
  public static final int EXPORT_FILESONLY = 1;
  public static final int EXPORT_PUBLICATIONSONLY = 2;

  @Inject
  private CoordinateImportExport coordinateImportExport;
  @Inject
  private NodeImportExport nodeImportExport;
  @Inject
  private PdcImportExport pdcImportExport;
  @Inject
  private RepositoriesTypeManager repositoriesTypeManager;

  private JAXBContext jaxbContext = null;

  protected ImportExport() {

  }

  @PostConstruct
  private void setup() {
    try {
      jaxbContext = JAXBContext.newInstance(SilverPeasExchangeType.class);
    } catch (JAXBException e) {
      SilverLogger.getLogger(this).error("Cannot initialize jaxbContext", e);
    }
  }

  /**
   * Méthode créant le fichier xml corespondant à l'arbre des objets.
   * @param silverPeasExchangeType - arbre des objets à mapper sur le fichier xml
   * @param xmlToExportPath - chemin et nom du fichier xml à créer
   * @throws ImportExportException
   */
  void saveToSilverpeasExchangeFile(SilverPeasExchangeType silverPeasExchangeType,
      String xmlToExportPath) throws ImportExportException {
    try {
      // URI du schéma et chemin du fichier XSD associé.
      String xsdPublicId = settings.getString("xsdPublicId");
      String xsdSystemId = settings.getString("xsdDefaultSystemId");

      Marshaller mar = jaxbContext.createMarshaller();
      mar.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
      mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      mar.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, xsdPublicId + " " + xsdSystemId);
      mar.marshal(silverPeasExchangeType, new File(xmlToExportPath));

    } catch (JAXBException me) {
      throw new ImportExportException("ImportExport.loadSilverpeasExchange",
          "importExport.EX_UNMARSHALLING_FAILED", "XML Filename : ", me);
    }
  }

  /**
   * Méthode retournant l'arbre des objets mappés sur le fichier xml passé en paramètre.
   * @param xmlFileName le fichier xml interprêté par JAXB
   * @return Un objet SilverPeasExchangeType contenant le mapping d'un fichier XML
   * @throws ImportExportException
   */
  private SilverPeasExchangeType loadSilverpeasExchange(String xmlFileName) throws ImportExportException {
    try {
      File xmlInputSource = new File(xmlFileName);
      String xsdSystemId = settings.getString("xsdDefaultSystemId");

      SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      Schema schema = sf.newSchema(new URL(xsdSystemId));

      // Unmarshall the import model
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      unmarshaller.setSchema(schema);
      unmarshaller.setEventHandler(new ImportExportErrorHandler());
      SilverPeasExchangeType silverpeasExchange =
          (SilverPeasExchangeType) unmarshaller.unmarshal(xmlInputSource);

      return silverpeasExchange;

    } catch (JAXBException me) {
      throw new ImportExportException("ImportExport.loadSilverpeasExchange",
          "importExport.EX_UNMARSHALLING_FAILED",
          "XML Filename " + xmlFileName + ": " + me.getLocalizedMessage(), me);
    } catch (MalformedURLException ue) {
      throw new ImportExportException("ImportExport.loadSilverpeasExchange",
          "importExport.EX_UNMARSHALLING_FAILED",
          "XML Filename " + xmlFileName + ": " + ue.getLocalizedMessage(), ue);
    } catch (SAXException ve) {
      throw new ImportExportException("ImportExport.loadSilverpeasExchange",
          "importExport.EX_PARSING_FAILED",
          "XML Filename " + xmlFileName + ": " + ve.getLocalizedMessage(), ve);
    }
  }

  /**
   * Méthode faisant appel au moteur d'importExport de silver peas, des publications définie dans
   * le
   * fichier xml passé en paramètre sont générées grace à JAXB.
   * @param userDetail - information sur l'utilisateur utilisant le moteur importExport
   * @param xmlFileName - fichier xml définissant les import et/ou export à effectuer
   * @return un rapport détaillé sur l'execution de l'import/export
   * @throws ImportExportException
   */
  public ImportReport processImport(UserDetail userDetail, String xmlFileName)
      throws ImportExportException {

    SilverPeasExchangeType silverExType;
    ImportReportManager reportManager = new ImportReportManager();
    // Cas du nom de fichier null ou vide
    if (!StringUtil.isDefined(xmlFileName)) {
      UnitReport unitReport = new UnitReport("No XML file specified");
      unitReport.setError(UnitReport.ERROR_ERROR);
      unitReport.setStatus(UnitReport.STATUS_PUBLICATION_NOT_CREATED);
      reportManager.addUnitReport(unitReport, "");
    }

    // Chargement du descripteur d'import à partir d'un fichier XML
    silverExType = loadSilverpeasExchange(xmlFileName);

    // Créations unitaires des thèmes et sous-thèmes avant d'insérer
    // des publication éventuelles dans ces thèmes et sous-thèmes
    if (silverExType.getNodeTreesType() != null) {
      // Traitement de l'élément <topicTrees>
      NodeTreesTypeManager typeMgr = new NodeTreesTypeManager();
      typeMgr.processImport(userDetail, silverExType.getNodeTreesType(), silverExType.
          getTargetComponentId(), reportManager);
    }

    // Créations unitaires de nouvelles publications ou modifications
    // de publications existantes
    if (silverExType.getPublicationsType() != null) {
      // Traitement de l'élément <publications>
      PublicationsTypeManager typeMgr = getPublicationsTypeManager();
      ImportSettings importSettings = new ImportSettings(null, userDetail, silverExType.
          getTargetComponentId(), null, false, silverExType.isPOIUsed(), ImportSettings.FROM_XML);
      typeMgr.processImport(silverExType.getPublicationsType(), importSettings, reportManager);
    }

    // Cas des imports en masse de thèmes et de publications
    if (silverExType.getRepositoriesType() != null) {
      // Traitement de l'élément <repositories>
      ImportSettings importSettings = new ImportSettings(null, userDetail, silverExType.
          getTargetComponentId(), null, false, silverExType.isPOIUsed(), ImportSettings.FROM_XML);
      repositoriesTypeManager
          .processImport(silverExType.getRepositoriesType(), importSettings, reportManager);
    }
    reportManager.reportImportEnd();
    return reportManager.getImportReport();
  }

  public ExportReport processExport(UserDetail userDetail, String language,
      List<WAAttributeValuePair> listItemsToExport, NodePK rootPK, int mode)
      throws ImportExportException {
    ExportReport report = null;
    switch (mode) {
      case ImportExport.EXPORT_FULL:
        report = processExport(userDetail, language, listItemsToExport, rootPK);
        break;
      case ImportExport.EXPORT_FILESONLY:
        report = processExportOfFilesOnly(userDetail, listItemsToExport, rootPK);
        break;
      default:
        report = processExportOfPublicationsOnly(userDetail, listItemsToExport, rootPK);
    }
    return report;
  }

  private ExportReport processExport(UserDetail userDetail, String language,
      List<WAAttributeValuePair> listItemsToExport, NodePK rootPK) throws ImportExportException {
    // pour le multilangue
    LocalizationBundle messages = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.importExport.multilang.importExportBundle", language);
    PublicationsTypeManager pubTypMgr = getPublicationsTypeManager();
    AdminImportExport adminIE = new AdminImportExport();
    SilverPeasExchangeType silverPeasExch = new SilverPeasExchangeType();
    ExportReport exportReport = new ExportReport();

    try {
      // Stockage de la date de démarage de l'export dans l'objet rapport
      exportReport.setDateDebut(new Date());
      // Création du dossier d'export
      String thisExportDir = generateExportDirName(userDetail, "export");
      String tempDir = FileRepositoryManager.getTemporaryPath();
      File fileExportDir = new File(tempDir + thisExportDir);
      if (!fileExportDir.exists()) {
        try {
          FileFolderManager.createFolder(fileExportDir);
        } catch (org.silverpeas.core.util.UtilException ex) {
          throw new ImportExportException("ImportExport", "importExport.EX_CANT_CREATE_FOLDER", ex);
        }
      }
      // Exportation des publications
      List<PublicationType> publicationsType;
      // création des répertoires avec le nom des thèmes et des publications
      publicationsType = pubTypMgr.processExport(exportReport, userDetail, listItemsToExport,
          fileExportDir.getPath(), true, true, rootPK);
      if (publicationsType.isEmpty()) {
        // les noms des thèmes et des publication est trop long ou au moins > 200 caractères
        // création des répertoires avec les Id des thèmes et des publications
        exportReport = new ExportReport();
        exportReport.setDateDebut(new Date());
        // détruire le répertoire et tout ce qu'il contient
        try {
          FileFolderManager.deleteFolder(fileExportDir.getPath());
        } catch (Exception ex) {
          throw new ImportExportException("ImportExport", "importExport.EX_CANT_DELETE_FOLDER", ex);
        }
        thisExportDir = generateExportDirName(userDetail, "export");
        tempDir = FileRepositoryManager.getTemporaryPath();
        fileExportDir = new File(tempDir + thisExportDir);
        publicationsType = pubTypMgr.processExport(exportReport, userDetail, listItemsToExport,
            fileExportDir.getPath(), false, true, rootPK);
      }
      silverPeasExch.setPublicationsType(publicationsType);

      // Récupération de la liste de id des composants
      Set<String> componentIds = new HashSet<>();
      List<ClassifyPosition> listClassifyPosition = new ArrayList<>();
      List<PublicationType> listPubType = publicationsType;
      for (PublicationType pubType : listPubType) {
        componentIds.add(pubType.getComponentId());
        List<ClassifyPosition> pdcPos = pubType.getPdcPositionsType();
        if (pdcPos != null) {
          listClassifyPosition.addAll(pdcPos);
        }
      }
      List<String> listComponentId = new ArrayList<String>(componentIds);
      // Exportation des composants liés aux publications exportées
      silverPeasExch.setComponentsType(adminIE.getComponents(listComponentId));
      // Exportation des Arbres de topics liés aux publications exportées
      List<NodeTreeType> nodeTreesType = nodeImportExport.getTrees(listComponentId);
      silverPeasExch.setNodeTreesType(nodeTreesType);
      // Exportation des pdcs liés aux publications exportées
      if (!listClassifyPosition.isEmpty()) {
        silverPeasExch.setPdcType(pdcImportExport.getPdc(listClassifyPosition));
      }

      if (rootPK == null) {
        // dans le cas de l'export depuis le moteur de recherche, créer l'index "a plat"
        createSummary(exportReport, thisExportDir, tempDir, fileExportDir);
      } else {
        // dans le cas de l'export d'un composant ou d'un thème, créer l'index en treeview
        HtmlExportGenerator htmlGenerator = new HtmlExportGenerator(exportReport, fileExportDir.
            getName(), messages);
        Map<String, List<String>> topicIds = prepareTopicsMap(publicationsType);
        Set<String> keys = topicIds.keySet();
        for (String topicId : keys) {
          createTopicHtmlFile(thisExportDir, tempDir, htmlGenerator, topicIds, topicId);
        }
        createEmptySummary(thisExportDir, tempDir, htmlGenerator);
        createTreeview(rootPK.getId(), thisExportDir, tempDir, nodeTreesType, htmlGenerator,
            topicIds);
        createExportDirectory(thisExportDir, tempDir);
      }
      // Création du fichier XML de mapping
      try {
        saveToSilverpeasExchangeFile(silverPeasExch,
            fileExportDir.getPath() + File.separatorChar + "importExport.xml");
      } catch (ImportExportException iex) {
        SilverLogger.getLogger(this).error(iex.getMessage(), iex);
      }
      // Création du zip
      createZipFile(fileExportDir, exportReport);
    } catch (NodeRuntimeException | PdcException ex) {
      throw new ImportExportException("importExport", "ImportExport.processExport()", ex);
    }
    return exportReport;
  }

  private void createSummary(ExportReport exportReport, String thisExportDir, String tempDir,
      File fileExportDir) throws ImportExportException {
    File fileHTML = new File(tempDir + thisExportDir + separator + "index.html");
    HtmlExportGenerator h = new HtmlExportGenerator(exportReport, fileExportDir.getName());
    Writer fileWriter = null;
    try {
      fileHTML.createNewFile();
      fileWriter = new OutputStreamWriter(new FileOutputStream(fileHTML.getPath()), UTF_8);
      fileWriter.write(h.toHTML());
    } catch (IOException ex) {
      throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", ex);
    } finally {
      IOUtils.closeQuietly(fileWriter);
    }
  }

  private void createEmptySummary(String thisExportDir, String tempDir,
      HtmlExportGenerator htmlGenerator) throws ImportExportException {
    final File fileExportDir = new File(tempDir, thisExportDir);
    if (!fileExportDir.exists()) {
      try {
        FileFolderManager.createFolder(fileExportDir);
      } catch (org.silverpeas.core.util.UtilException ex) {
        throw new ImportExportException("ImportExport", "importExport.EX_CANT_CREATE_FOLDER", ex);
      }
    }
    final File fileTopicHTML = new File(fileExportDir, "indexTopicEmpty.html");
    try {
      if (!fileTopicHTML.createNewFile()) {
        SilverLogger.getLogger(this).warn("{0} already exists!", fileTopicHTML.getAbsolutePath());
      }
      try (final Writer fileWriter = new OutputStreamWriter(new FileOutputStream(fileTopicHTML.getPath()), UTF_8)) {
        fileWriter.write(htmlGenerator.toHTML(fileTopicHTML.getName()));
      }
    } catch (IOException ex) {
      throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", ex);
    }
  }

  private void createExportDirectory(String thisExportDir, String tempDir)
      throws ImportExportException {
    try {
      // créer le répertoire pour le zip
      FileFolderManager.createFolder(tempDir + thisExportDir + separator + "treeview");
      // le remplir avec le contenu du répertoire "treeview" sur disque
      String chemin = settings.getString("mappingDir");
      if (chemin.startsWith("file:")) {
        chemin = chemin.substring(8);
      }
      chemin = chemin + "treeview";
      Collection<File> files = FileFolderManager.getAllFile(chemin);
      for (File file : files) {
        File newFile =
            new File(tempDir + thisExportDir + separator + "treeview" + separator + file.getName());
        FileRepositoryManager.copyFile(file.getPath(), newFile.getPath());
      }
    } catch (Exception e) {
      throw new ImportExportException("ImportExport", "importExport.EX_CANT_CREATE_FOLDER", e);
    }
  }

  private void createTreeview(String rootId, String thisExportDir, String tempDir,
      List<NodeTreeType> nodeTreesType, HtmlExportGenerator htmlGenerator,
      Map<String, List<String>> topicIds) throws ImportExportException {
    Writer fileWriter;
    File fileHTML = new File(tempDir + thisExportDir + separator + "index.html");
    fileWriter = null;
    try {
      fileHTML.createNewFile();
      fileWriter = new OutputStreamWriter(new FileOutputStream(fileHTML.getPath()), UTF_8);
      Set<String> topics = topicIds.keySet();
      fileWriter
          .write(htmlGenerator.indexToHTML(fileHTML.getName(), topics, nodeTreesType, rootId));
    } catch (IOException ex) {
      throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", ex);
    } finally {
      IOUtils.closeQuietly(fileWriter);
    }
  }

  private Map<String, List<String>> prepareTopicsMap(List<PublicationType> listPubType) {
    Map<String, List<String>> topicIds = new HashMap<>(listPubType.size());

    for (PublicationType publicationType : listPubType) {
      String pubId = Integer.toString(publicationType.getId());
      // pour chaque publication : parcourir ses noeuds
      List<NodePositionType> listNodePositionType = publicationType.getNodePositionsType();
      for (NodePositionType nodePositionType : listNodePositionType) {
        // pour chaque topic : récupérer l'Id
        String topicId = String.valueOf(nodePositionType.getId());
        List<String> pubIds;
        if (topicIds.containsKey(topicId)) {
          pubIds = topicIds.get(topicId);
        } else {
          pubIds = new ArrayList<>(listPubType.size());
        }
        pubIds.add(pubId);
        topicIds.put(topicId, pubIds);
      }
    }
    return topicIds;
  }

  private void createTopicHtmlFile(String thisExportDir, String tempDir, HtmlExportGenerator h,
      Map<String, List<String>> topicIds, String topicId) throws ImportExportException {
    File fileTopicHTML = new File(tempDir + thisExportDir, "indexTopic" + topicId + ".html");
    Writer fileWriter = null;
    try {
      fileTopicHTML.createNewFile();
      fileWriter = new OutputStreamWriter(new FileOutputStream(fileTopicHTML.getPath()), UTF_8);
      fileWriter.write(h.toHTML(fileTopicHTML.getName(), topicIds.get(topicId)));
    } catch (IOException ex) {
      throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", ex);
    } finally {
      IOUtils.closeQuietly(fileWriter);
    }
  }

  /**
   * @param userDetail
   * @param itemsToExport
   * @return
   * @throws ImportExportException
   */
  public ExportPDFReport processExportPDF(UserDetail userDetail,
      List<WAAttributeValuePair> itemsToExport, NodePK rootPK) throws ImportExportException {
    ExportPDFReport report = new ExportPDFReport();
    report.setDateDebut(new Date());

    PublicationsTypeManager pubTypeManager = getPublicationsTypeManager();

    String fileExportName = generateExportDirName(userDetail, "fusion");
    String tempDir = FileRepositoryManager.getTemporaryPath();

    File fileExportDir = new File(tempDir + fileExportName);
    if (!fileExportDir.exists()) {
      try {
        FileFolderManager.createFolder(fileExportDir);
      } catch (org.silverpeas.core.util.UtilException ex) {
        throw new ImportExportException("ImportExport", "importExport.EX_CANT_CREATE_FOLDER", ex);
      }
    }

    Document document = null;
    PdfCopy writer = null;
    List<PdfReader> readers = new ArrayList<>();
    File pdfFileName = new File(tempDir + fileExportName + ".pdf");
    try {
      // création des répertoires avec le nom des thèmes et des publications
      final List<AttachmentDetail> pdfList = pubTypeManager
          .processPDFExport(report, userDetail, itemsToExport, fileExportDir.getPath(), true,
              rootPK);
      final List<HashMap<String, Object>> master = new ArrayList<>();
      if (!pdfList.isEmpty()) {
        int pageOffset = 0;
        for (AttachmentDetail attDetail : pdfList) {
          final PdfReader reader = getPdfReader(fileExportDir, attDetail);
          if (reader != null) {
            readers.add(reader);
            reader.consolidateNamedDestinations();
            int nbPages = reader.getNumberOfPages();
            final List<HashMap<String, Object>> bookmarks = SimpleBookmark.getBookmark(reader);
            if (writer == null) {
              document = new Document(reader.getPageSizeWithRotation(1));
              writer = new PdfCopy(document, new FileOutputStream(pdfFileName));
              writer.setMergeFields();
              document.open();
            }
            if (mergeDocument(attDetail, reader, writer)) {
              if (isNotEmpty(bookmarks)) {
                if (pageOffset != 0) {
                  SimpleBookmark.shiftPageNumbers(bookmarks, pageOffset, null);
                }
                master.addAll(bookmarks);
              }
              pageOffset += nbPages;
            }
          }
        }

        if (!master.isEmpty()) {
          writer.setOutlines(master);
        }
      } else {
        return null;
      }
    } catch (DocumentException | IOException e) {
      // Pb avec le répertoire de destination
      throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", e);
    } finally {
      if (writer != null) {
        writer.flush();
        writer.close();
      }
      if (document != null) {
        document.close();
      }
      for (PdfReader reader : readers) {
        try {
          reader.close();
        } catch (Exception e) {
          SilverLogger.getLogger(this).warn(e);
        }
      }
    }

    report.setPdfFileName(pdfFileName.getName());
    report.setPdfFileSize(pdfFileName.length());
    report.setPdfFilePath(FileServerUtils.getUrlToTempDir(pdfFileName.getName()));

    report.setDateFin(new Date());

    return report;
  }

  private PdfReader getPdfReader(final File fileExportDir,
      final AttachmentDetail attDetail) {
    try {
      return new PdfReader(
          fileExportDir.getPath() + File.separatorChar + attDetail.getLogicalName());
    } catch (IOException ioe) {
      // Attached file is not physically present on disk, ignore it and log event
      SilverLogger.getLogger(this)
          .error("Cannot find PDF {0}", new String[]{attDetail.getLogicalName()}, ioe);
    }
    return null;
  }

  private boolean mergeDocument(final AttachmentDetail attDetail, final PdfReader reader,
      final PdfCopy writer) {
    try {
      writer.addDocument(reader);
      return true;
    } catch (Exception e) {
      // Can't import PDF file, ignore it and log event
      SilverLogger.getLogger(this)
          .error("Cannot merge PDF {0}", new String[]{attDetail.getLogicalName()}, e);
    }
    return false;
  }

  /**
   * Export Kmax Publications
   * @param userDetail
   * @param language
   * @param itemsToExport
   * @param combination
   * @param timeCriteria
   * @return
   * @throws ImportExportException
   */
  public ExportReport processExportKmax(UserDetail userDetail, String language,
      List<WAAttributeValuePair> itemsToExport, List<String> combination, String timeCriteria)
      throws ImportExportException {
    LocalizationBundle messages = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.importExport.multilang.importExportBundle", language);

    PublicationsTypeManager pubTypMgr = getPublicationsTypeManager();
    AdminImportExport adminIE = new AdminImportExport();
    SilverPeasExchangeType silverPeasExch = new SilverPeasExchangeType();
    ExportReport exportReport = new ExportReport();
    GEDImportExport gedIE;
    try {
      // Stockage de la date de démarage de l'export dans l'objet rapport
      exportReport.setDateDebut(new Date());
      // Création du dossier d'export
      String thisExportDir = generateExportDirName(userDetail, "export");

      String tempDir = FileRepositoryManager.getTemporaryPath();

      File fileExportDir = new File(tempDir + thisExportDir);
      if (!fileExportDir.exists()) {
        try {
          FileFolderManager.createFolder(fileExportDir);
        } catch (org.silverpeas.core.util.UtilException ex) {
          throw new ImportExportException("ImportExport", "importExport.EX_CANT_CREATE_FOLDER", ex);
        }
      }

      // Exportation des publications
      List<PublicationType> publicationsType;
      // création des répertoires avec le nom des publications
      publicationsType =
          pubTypMgr.processExport(exportReport, userDetail, itemsToExport, fileExportDir.getPath(),
              false, true, null);

      // Récupération de la liste de id des composants
      Set<String> listComponentId = new HashSet<>();
      String componentId = null;
      for (PublicationType pubType : publicationsType) {
        listComponentId.add(pubType.getComponentId());
        componentId = pubType.getComponentId();
      }

      // Exportation des composants liés aux publications exportées
      silverPeasExch.setComponentsType(adminIE.getComponents(new ArrayList<>(listComponentId)));

      // ================ EXPORT SELECTED PUBLICATIONS ======================
      if (combination != null) {
        // Création du sommaire HTML
        File fileHTML = new File(tempDir + thisExportDir + separator + "index.html");

        HtmlExportGenerator h =
            new HtmlExportGenerator(exportReport, fileExportDir.getName(), messages);
        Writer fileWriter = null;
        try {
          fileHTML.createNewFile();
          fileWriter = new OutputStreamWriter(new FileOutputStream(fileHTML.getPath()), UTF_8);
          // Create header with axes and values selected
          List<String> positionsLabels =
              coordinateImportExport.getCombinationLabels(combination, componentId);
          fileWriter
              .write(h.kmaxPublicationsToHTML(positionsLabels, timeCriteria, IFRAME_PUBLICATION));
        } catch (IOException ex) {
          throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", ex);
        } finally {
          IOUtils.closeQuietly(fileWriter);
        }
      } else {
        // ================ EXPORT ALL PUBLICATIONS OF COMPONENT
        // Publication detail empty
        File emptyFileHTML = new File(tempDir + thisExportDir + File.separatorChar + "empty.html");
        Writer fileWriter = null;
        try {
          emptyFileHTML.createNewFile();
          fileWriter = new OutputStreamWriter(new FileOutputStream(emptyFileHTML.getPath()), UTF_8);
        } catch (IOException ex) {
          throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", ex);
        } finally {
          IOUtils.closeQuietly(fileWriter);
        }

        // Create unbalanced file html index
        ComponentInst componentInst = OrganizationControllerProvider.getOrganisationController()
            .getComponentInst(componentId);
        gedIE = ImportExportFactory.createGEDImportExport(userDetail, componentId);

        String unbalancedFileNameRelativePath = "index-2.html";
        File unclassifiedFileHTML =
            new File(tempDir + thisExportDir + File.separatorChar + unbalancedFileNameRelativePath);
        HtmlExportGenerator h =
            new HtmlExportGenerator(exportReport, fileExportDir.getName(), messages);
        try {
          unclassifiedFileHTML.createNewFile();
          fileWriter =
              new OutputStreamWriter(new FileOutputStream(unclassifiedFileHTML.getPath(), true),
                  UTF_8);
          fileWriter.write(h.toHtmlPublicationsByPositionStart());
        } catch (IOException ex) {
          throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", ex);
        } finally {
          IOUtils.closeQuietly(fileWriter);
        }
        String publicationFileNameRelativePath;
        // Fill unbalanced file html index
        List<PublicationDetail> unbalancedPublications = PublicationImportExport.
            getUnbalancedPublications(componentId);
        String componentLabel = FileServerUtils.replaceAccentChars(componentInst.getLabel());
        for (PublicationDetail pubDetail : unbalancedPublications) {
          PublicationType publicationType =
              gedIE.getPublicationCompleteById(String.valueOf(pubDetail.getId()), componentId);
          publicationFileNameRelativePath =
              componentLabel + separator + pubDetail.getId() + separator + "index.html";
          pubTypMgr.fillPublicationType(gedIE, publicationType, null);
          int nbThemes = pubTypMgr.getNbThemes(gedIE, publicationType, null);
          HtmlExportPublicationGenerator unbalanced =
              new HtmlExportPublicationGenerator(publicationType, null,
                  publicationFileNameRelativePath, nbThemes);
          exportReport.addHtmlIndex(pubDetail.getId(), unbalanced);
          fileWriter = null;
          try {
            fileWriter =
                new OutputStreamWriter(new FileOutputStream(unclassifiedFileHTML.getPath(), true),
                    UTF_8);
            fileWriter.write(unbalanced.toHtmlSommairePublication(IFRAME_PUBLICATION));
          } catch (IOException ex) {
            throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", ex);
          } finally {
            IOUtils.closeQuietly(fileWriter);
          }
        }

        // Create HTML summary with the search axis
        File fileHTML = new File(tempDir + thisExportDir + separator + "index.html");
        h = new HtmlExportGenerator(exportReport, fileExportDir.getName(), messages);
        try {
          fileHTML.createNewFile();
          fileWriter = new OutputStreamWriter(new FileOutputStream(fileHTML.getPath()), UTF_8);
          // Create header with axes and values selected
          List<NodeDetail> axis = coordinateImportExport.getAxis(componentId);
          fileWriter.write(h.kmaxAxisToHTML(axis, language));
        } catch (IOException ex) {
          throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", ex);
        } finally {
          IOUtils.closeQuietly(fileWriter);
        }

        // Create HTML summary
        // --------------------------------------
        String exportPath = fileExportDir.getPath();
        String exportSummaryPath = exportPath;
        List<NodeDetail> listAxis =
            coordinateImportExport.getAxisHeadersWithChildren(componentId, true, true);

        // Remove unclassified node
        listAxis.remove(0);
        int nbAxis = listAxis.size();
        // Get list axis with values
        List<Collection<NodeDetail>> listAxisWithChildren = new ArrayList<>();
        for (NodeDetail currentAxisNodeDetail : listAxis) {
          Collection<NodeDetail> childrenNodeDetails = currentAxisNodeDetail.getChildrenDetails();
          if (childrenNodeDetails != null && !childrenNodeDetails.isEmpty()) {
            listAxisWithChildren.add(childrenNodeDetails);
          }
        }

        // Create List with all nodes Details
        List<String> nodesIds = new ArrayList<>();
        for (Collection<NodeDetail> currentAxis : listAxisWithChildren) {
          for (NodeDetail axisNode : currentAxis) {
            String nodeId = axisNode.getId();
            nodesIds.add(nodeId);
          }
        }

        // Create List of index files positions (ex: index-2-3-x-y-z....html)
        List<String> indexFilesPositions = new ArrayList<>();
        // Process all filename combinations
        for (int i = 1; i <= nodesIds.size(); i++) {
          indexFilesPositions = coordinateImportExport
              .coupleIds(indexFilesPositions, nodesIds, 0, 0, i, null, nbAxis);
        }
        // Create positions index files
        for (String positionNameId : indexFilesPositions) {
          // fileName / index-x-x.html
          // Create positions index file
          fileHTML = new File(exportSummaryPath + separator + positionNameId);
          // Write file positions
          fileWriter = null;
          try {
            fileHTML.createNewFile();
            fileWriter = new OutputStreamWriter(new FileOutputStream(fileHTML.getPath()), UTF_8);
            fileWriter.write(h.toHtmlPublicationsByPositionStart());
          } finally {
            try {
              fileWriter.close();
            } catch (Exception ex) {
            }
          }
        }
        // Publications to export
        exportPath = fileExportDir.getPath();
        exportSummaryPath = exportPath;
        for (WAAttributeValuePair attValue : itemsToExport) {
          List<String> filesPositionsHTMLToFill = new ArrayList<>();
          String pubId = attValue.getName();
          componentId = attValue.getValue();
          gedIE = ImportExportFactory.createGEDImportExport(userDetail, componentId);
          StringBuilder positionFileNameHTML = new StringBuilder();

          // Récupération du PublicationType
          PublicationType publicationType = gedIE.getPublicationCompleteById(pubId, componentId);
          pubTypMgr.fillPublicationType(gedIE, publicationType, null);
          publicationType.setCoordinatesPositionsType(new CoordinatesPositionsType());
          List<CoordinatePoint> listCoordinatesPositions = new ArrayList<>();
          Collection<Coordinate> coordinates = gedIE.getPublicationCoordinates(pubId, componentId);
          for (Coordinate coordinate : coordinates) {
            positionFileNameHTML.append("index");
            Collection<CoordinatePoint> coordinatesPoints = coordinate.getCoordinatePoints();
            for (CoordinatePoint coordinatePoint : coordinatesPoints) {
              positionFileNameHTML.append("-").append(coordinatePoint.getNodeId());
              listCoordinatesPositions.add(coordinatePoint);
            }
            if (!filesPositionsHTMLToFill.contains(positionFileNameHTML + ".html")) {
              filesPositionsHTMLToFill.add(positionFileNameHTML + ".html");
            }

            List<String> nodeIds = new ArrayList<>();
            StringTokenizer st = new StringTokenizer(positionFileNameHTML.toString(), "-");
            while (st.hasMoreTokens()) {
              String nodeId = st.nextToken();
              if (!"index".equals(nodeId)) {
                NodeDetail currentNodeDetail = coordinateImportExport
                    .getNodeHeader(new NodePK(String.valueOf(nodeId), componentId));
                nodeIds.add(currentNodeDetail.getId());
                if (currentNodeDetail.getLevel() >= 3) {
                  // if subvalue of axis, add this node
                  nodeIds = addNodeToList(nodeIds, currentNodeDetail);
                } else {
                  List<NodeDetail> axisChildren =
                      coordinateImportExport.getAxisChildren(currentNodeDetail.getNodePK(), false);
                  // if Axis, add all nodes of this axis
                  for (NodeDetail nodeDetail : axisChildren) {
                    nodeIds.add(nodeDetail.getId());
                  }
                }
              }
            }

            List<String> otherPositionsFilesNameHTML = new ArrayList<>();
            otherPositionsFilesNameHTML = coordinateImportExport
                .coupleIds(otherPositionsFilesNameHTML, nodeIds, 0, 0, nbAxis, null, nbAxis);
            for (String otherPositionFileNameHTML : otherPositionsFilesNameHTML) {
              if (!filesPositionsHTMLToFill.contains(otherPositionFileNameHTML)) {
                filesPositionsHTMLToFill.add(otherPositionFileNameHTML);
              }
            }
          }

          publicationType.getCoordinatesPositionsType()
              .setCoordinatesPositions(listCoordinatesPositions);
          publicationFileNameRelativePath =
              componentLabel + separator + pubId + separator + "index.html";
          int nbThemes = pubTypMgr.getNbThemes(gedIE, publicationType, null);
          HtmlExportPublicationGenerator s =
              new HtmlExportPublicationGenerator(publicationType, null,
                  publicationFileNameRelativePath, nbThemes);
          exportReport.addHtmlIndex(pubId, s);

          for (String filePositions : filesPositionsHTMLToFill) {
            fileHTML = new File(exportSummaryPath + separator + filePositions);
            fileWriter = null;
            try {
              if (fileHTML.exists()) {
                fileWriter =
                    new OutputStreamWriter(new FileOutputStream(fileHTML.getPath(), true), UTF_8);
                fileWriter.write(s.toHtmlSommairePublication(IFRAME_PUBLICATION));
              }
            } finally {
              IOUtils.closeQuietly(fileWriter);
            }
          }
        }
      }
      // Création du fichier XML de mapping
      saveToSilverpeasExchangeFile(silverPeasExch,
          fileExportDir.getPath() + separator + "importExport.xml");

      // Création du zip
      createZipFile(fileExportDir, exportReport);
    } catch (Exception ex) {
      throw new ImportExportException("importExport", "ImportExport.processExportKmax()", ex);
    }
    return exportReport;
  }

  public void writeImportToLog(ImportReport importReport, MultiSilverpeasBundle resource) {
    if (importReport != null) {
      String reportLogFile = settings.getString("importExportLogFile");
      File file = new File(reportLogFile);
      Writer fileWriter = null;
      try {
        if (!file.exists()) {
          file.createNewFile();
        }
        fileWriter = new OutputStreamWriter(new FileOutputStream(file.getPath(), true), UTF_8);
        fileWriter.write(importReport.writeToLog(resource));
      } catch (IOException ex) {
        SilverLogger.getLogger(this).error(ex.getMessage(), ex);
      } finally {
        IOUtils.closeQuietly(fileWriter);
      }
    }
  }

  /**
   * Add father of nodeDetail to List
   * @param nodesIds
   * @param nodeDetail
   * @return
   */
  private List<String> addNodeToList(List<String> nodesIds, NodeDetail nodeDetail) {
    // Add father
    nodesIds.add(String.valueOf(nodeDetail.getFatherPK().getId()));
    if (nodeDetail.getLevel() >= 4) {
      NodeDetail parentNodeDetail = coordinateImportExport.getNodeHeader(nodeDetail.getFatherPK());
      addNodeToList(nodesIds, parentNodeDetail);
    }
    return nodesIds;
  }

  private ExportReport processExportOfFilesOnly(UserDetail userDetail,
      List<WAAttributeValuePair> listItemsToExport, NodePK rootPK) throws ImportExportException {
    PublicationsTypeManager pubTypMgr = getPublicationsTypeManager();
    ExportReport exportReport = new ExportReport();

    // Creates export folder
    File fileExportDir = createExportDir(userDetail);

    // Export files attached to publications
    try {
      pubTypMgr.processExportOfFilesOnly(exportReport, userDetail, listItemsToExport,
          fileExportDir.getPath(), rootPK);
    } catch (IOException e1) {
      throw new ImportExportException("ImportExport", "root.EX_CANT_EXPORT_FILES", e1);
    }
    // Create ZIP file
    createZipFile(fileExportDir, exportReport);
    return exportReport;
  }

  private ExportReport processExportOfPublicationsOnly(UserDetail userDetail,
      List<WAAttributeValuePair> listItemsToExport, NodePK rootPK) throws ImportExportException {
    PublicationsTypeManager pubTypMgr = getPublicationsTypeManager();
    ExportReport exportReport = new ExportReport();

    // Stockage de la date de démarrage de l'export dans l'objet rapport
    exportReport.setDateDebut(new Date());

    File fileExportDir = createExportDir(userDetail);

    // création des répertoires avec le nom des thèmes et des publications
    pubTypMgr.processExport(exportReport, userDetail, listItemsToExport, fileExportDir.getPath(),
        true, rootPK != null, rootPK);

    // Création du zip
    createZipFile(fileExportDir, exportReport);
    return exportReport;
  }

  private PublicationsTypeManager getPublicationsTypeManager() {
    return ServiceProvider.getService(PublicationsTypeManager.class);
  }
}
