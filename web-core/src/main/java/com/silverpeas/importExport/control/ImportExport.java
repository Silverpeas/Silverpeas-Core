/*
 * Copyright (C) 2000 - 2009 Silverpeas
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.importExport.control;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BadPdfFormatException;
import com.lowagie.text.pdf.PRAcroForm;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.SimpleBookmark;
import com.silverpeas.admin.importExport.AdminImportExport;
import com.silverpeas.coordinates.importExport.CoordinateImportExport;
import com.silverpeas.coordinates.importExport.CoordinatesPositionsType;
import com.silverpeas.importExport.model.ImportExportErrorHandler;
import com.silverpeas.importExport.model.ImportExportException;
import com.silverpeas.importExport.model.PublicationType;
import com.silverpeas.importExport.model.PublicationsType;
import com.silverpeas.importExport.model.SilverPeasExchangeType;
import com.silverpeas.importExport.report.ExportPDFReport;
import com.silverpeas.importExport.report.ExportReport;
import com.silverpeas.importExport.report.HtmlExportGenerator;
import com.silverpeas.importExport.report.HtmlExportPublicationGenerator;
import com.silverpeas.importExport.report.ImportReport;
import com.silverpeas.importExport.report.ImportReportManager;
import com.silverpeas.importExport.report.UnitReport;
import com.silverpeas.node.importexport.NodeImportExport;
import com.silverpeas.node.importexport.NodePositionType;
import com.silverpeas.node.importexport.NodeTreesType;
import com.silverpeas.pdc.importExport.PdcImportExport;
import com.silverpeas.pdc.importExport.PdcPositionsType;
import com.silverpeas.util.ZipManager;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.WAAttributeValuePair;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.coordinates.model.Coordinate;
import com.stratelia.webactiv.util.coordinates.model.CoordinatePoint;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.node.model.NodeRuntimeException;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

/**
 * Classe devant être instanciée au niveau controleur pour utiliser le moteur d'import export.
 * @author sDevolder.
 */
public class ImportExport {

  private static final ResourceLocator settings = new ResourceLocator(
          "com.silverpeas.importExport.settings.mapping", "");
  public final static String iframePublication = "publications";
  public final static String iframeIndexPublications = "indexPublications";

  /**
   * Unique constructeur de la classe
   */
  public ImportExport() {
  }

  /**
   * Méthode créant le fichier xml corespondant à l'arbre des objets.
   * @param silverPeasExchangeType - arbre des objets à mapper sur le fichier xml
   * @param xmlToExportPath - chemin et nom du fichier xml à créer
   * @throws ImportExportException
   */
  public void upLoadSilverpeasExchange(SilverPeasExchangeType silverPeasExchangeType,
          String xmlToExportPath) throws ImportExportException {

    Writer writer = null;
    Mapping mapping = new Mapping();

    try {
      String mappingDir = settings.getString("mappingDir");
      String mappingFileName = settings.getString("importExportMapping");
      File mappingFile = new File(mappingDir, mappingFileName);
      // String mappingFilePath = mappingDir + mappingFileName;

      // Load mapping and instantiate a Marshaller
      mapping.loadMapping(mappingFile.getPath());

      writer = new OutputStreamWriter(new FileOutputStream(xmlToExportPath), "UTF-8");
      Marshaller mar = new Marshaller(writer);
      // mar.setEncoding("ISO-8859-1");
      // mar.setSchemaLocation("http://intranoo.oevo.com/websilverpeas/exchange/v308 http://intranoo.oevo.com/websilverpeas/exchange/v308/SilverpeasExchange.xsd");
      // mar.setNamespaceMapping("","http://intranoo.oevo.com/websilverpeas/exchange/v308");

      // URI du schéma et chemin du fichier XSD associé.
      String xsdPublicId = settings.getString("xsdPublicId");
      String xsdSystemId = settings.getString("xsdSystemId");

      // mar.setSchemaLocation(xsdLocation + " " + xsdLocation + "/" +
      // xsdFilename);
      mar.setSchemaLocation(xsdPublicId + " " + xsdSystemId);
      mar.setNamespaceMapping("sp", xsdPublicId);
      mar.setMapping(mapping);
      mar.marshal(silverPeasExchangeType);

    } catch (MappingException me) {
      throw new ImportExportException("ImportExport.loadSilverpeasExchange",
              "importExport.EX_LOADING_XML_MAPPING_FAILED", "XML Filename : ", me);
    } catch (MarshalException me) {
      throw new ImportExportException("ImportExport.loadSilverpeasExchange",
              "importExport.EX_UNMARSHALLING_FAILED", "XML Filename : ", me);
    } catch (ValidationException ve) {
      throw new ImportExportException("ImportExport.loadSilverpeasExchange",
              "importExport.EX_PARSING_FAILED", "XML Filename : ", ve);
    } catch (IOException ioe) {
      throw new ImportExportException("ImportExport.loadSilverpeasExchange",
              "importExport.EX_LOADING_XML_MAPPING_FAILED", "XML Filename : ", ioe);
    } catch (Exception ioe) {
      throw new ImportExportException("ImportExport.loadSilverpeasExchange", "importExport.",
              "XML Filename : ", ioe);
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException ex) {
          // Rien
        }
      }
    }
  }

  /**
   * Méthode retournant l'arbre des objets mappés sur le fichier xml passé en paramètre.
   * @param xmlFileName le fichier xml interprêté par Castor
   * @return Un objet SilverPeasExchangeType contenant le mapping d'un fichier XML Castor
   * @throws ImportExportException
   */
  public SilverPeasExchangeType loadSilverpeasExchange(String xmlFileName)
          throws ImportExportException {

    SilverTrace.debug("importExport", "ImportExportSessionController.loadSilverpeasExchange",
            "root.MSG_GEN_ENTER_METHOD", "xmlFileName = " + xmlFileName);

    try {
      InputSource xmlInputSource = new InputSource(xmlFileName);
      String xsdPublicId = settings.getString("xsdPublicId");
      String xsdSystemId = settings.getString("xsdDefaultSystemId");

      // Load and parse default XML schema for import/export
      SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema",
              "com.sun.org.apache.xerces.internal.jaxp.validation.XMLSchemaFactory", null);
      Schema schema = schemaFactory.newSchema(new StreamSource(xsdSystemId));

      // Create an XML parser for loading XML import file
      SAXParserFactory factory = SAXParserFactory.newInstance(
              "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl", null);
      factory.setValidating(false);
      factory.setNamespaceAware(true);
      factory.setSchema(schema);
      SAXParser parser = factory.newSAXParser();

      // First try to determine to load the XML file using the default
      // XML-Schema
      ImportExportErrorHandler errorHandler = new ImportExportErrorHandler();
      XMLReader xmlReader = parser.getXMLReader();
      xmlReader.setErrorHandler(errorHandler);

      try {
        xmlReader.parse(xmlInputSource);

      } catch (SAXException ex) {
        SilverTrace.debug("importExport", "ImportExportSessionController.loadSilverpeasExchange",
                "root.MSG_GEN_PARAM_VALUE", (new StringBuilder("XML File ")).append(xmlFileName).append(" is not valid according to default schema").toString());

        // If case the default schema is not the one specified by the
        // XML import file, try to get the right XML-schema and
        // namespace (this is done by parsing without validation)
        ImportExportNamespaceHandler nsHandler = new ImportExportNamespaceHandler();
        factory.setSchema(null);
        parser = factory.newSAXParser();
        xmlReader = parser.getXMLReader();
        xmlReader.setContentHandler(nsHandler);
        xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        xmlReader.parse(xmlInputSource);

        // If OK, extract the name and location of the schema
        String nsSpec = nsHandler.getNsSpec();
        if (nsSpec == null || xsdPublicId.equals(nsSpec)) {
          throw ex;
        }

        String nsVersion = extractUriNameIndex(nsSpec);
        if (nsVersion.length() == 0) {
          throw ex;
        }

        String altXsdSystemId = settings.getStringWithParam("xsdSystemId", nsVersion);
        if ((altXsdSystemId == null) || (altXsdSystemId.equals(xsdSystemId))) {
          throw ex;
        }

        SilverTrace.debug("importExport", "ImportExportSessionController.loadSilverpeasExchange",
                "root.MSG_GEN_PARAM_VALUE", (new StringBuilder(
                "Trying again using schema specification located at ")).append(altXsdSystemId).toString());

        // Try again to load, parse and validate the XML import file,
        // using the new schema specification
        schema = schemaFactory.newSchema(new StreamSource(altXsdSystemId));
        factory.setSchema(schema);
        parser = factory.newSAXParser();
        xmlReader = parser.getXMLReader();
        xmlReader.setErrorHandler(errorHandler);
        xmlReader.parse(xmlInputSource);
      }

      SilverTrace.debug("importExport", "ImportExportSessionController.loadSilverpeasExchange",
              "root.MSG_GEN_PARAM_VALUE", "XML Validation complete");

      // Mapping file for Castor
      String mappingDir = settings.getString("mappingDir");
      String mappingFileName = settings.getString("importExportMapping");
      File mappingFile = new File(mappingDir, mappingFileName);
      Mapping mapping = new Mapping();

      // Load mapping and instantiate a Unmarshaller
      mapping.loadMapping(mappingFile.getPath());
      Unmarshaller unmar = new Unmarshaller(SilverPeasExchangeType.class);
      unmar.setMapping(mapping);
      unmar.setValidation(false);

      // Unmarshall the process model
      SilverPeasExchangeType silverpeasExchange = (SilverPeasExchangeType) unmar.unmarshal(xmlInputSource);
      SilverTrace.debug("importExport", "ImportExportSessionController.loadSilverpeasExchange",
              "root.MSG_GEN_PARAM_VALUE", "Unmarshalling complete");
      return silverpeasExchange;

    } catch (MappingException me) {
      throw new ImportExportException("ImportExport.loadSilverpeasExchange",
              "importExport.EX_LOADING_XML_MAPPING_FAILED", "XML Filename " + xmlFileName + ": "
              + me.getLocalizedMessage(), me);
    } catch (MarshalException me) {
      throw new ImportExportException("ImportExport.loadSilverpeasExchange",
              "importExport.EX_UNMARSHALLING_FAILED", "XML Filename " + xmlFileName + ": "
              + me.getLocalizedMessage(), me);
    } catch (ValidationException ve) {
      throw new ImportExportException("ImportExport.loadSilverpeasExchange",
              "importExport.EX_PARSING_FAILED", "XML Filename " + xmlFileName + ": "
              + ve.getLocalizedMessage(), ve);
    } catch (IOException ioe) {
      throw new ImportExportException("ImportExport.loadSilverpeasExchange",
              "importExport.EX_LOADING_XML_MAPPING_FAILED", "XML Filename " + xmlFileName + ": "
              + ioe.getLocalizedMessage(), ioe);
    } catch (ParserConfigurationException ex) {
      throw new ImportExportException("ImportExport.loadSilverpeasExchange",
              "importExport.EX_PARSING_FAILED", "XML Filename " + xmlFileName + ": "
              + ex.getLocalizedMessage(), ex);
    } catch (SAXNotRecognizedException snre) {
      throw new ImportExportException("ImportExport.loadSilverpeasExchange",
              "importExport.EX_PARSING_FAILED", "XML Filename " + xmlFileName + ": "
              + snre.getLocalizedMessage(), snre);
    } catch (SAXNotSupportedException snse) {
      throw new ImportExportException("ImportExport.loadSilverpeasExchange",
              "importExport.EX_PARSING_FAILED", "XML Filename " + xmlFileName + ": "
              + snse.getLocalizedMessage(), snse);
    } catch (SAXException se) {
      throw new ImportExportException("ImportExport.loadSilverpeasExchange",
              "importExport.EX_PARSING_FAILED", "XML Filename " + xmlFileName + ": "
              + se.getLocalizedMessage(), se);
    }
  }

  /**
   * Cherche et retourne un nom de ressource extrait du chemin d'un URI donné.
   * @param uri l'URI dans lequel on cherche le nom de ressource.
   * @return le nom de ressource dans la chaîne uri ou chaîne vide (jamais null) sir uri est
   * <caode>null</code> ou vide ou s'il n'y a pas de ressource indiquée par uri.
   */
  private String extractUriNameIndex(String uri) {

    if ((uri == null) || (uri.length() == 0)) {
      return "";
    }

    int first = 0;
    int last = uri.length();
    int i;

    i = uri.lastIndexOf('#');
    if (i >= 0) {
      last = i;
    }
    i = uri.lastIndexOf('?', last);
    if (i >= 0) {
      last = i;
    }

    i = uri.lastIndexOf('/', last);
    if (i >= 0) {
      first = i + 1;
    }
    i = uri.indexOf("://") + 2;
    if ((i >= 2) && (i > first) && (i < last)) {
      first = i + 1;
    }

    return uri.substring(first, last);
  }

  /**
   * Méthode faisant appel au moteur d'importExport de silver peas, des publications définie dans
   * le fichier xml passé en paramètre sont générées grace à l'outil castor.
   * @param userDetail - information sur l'utilisateur utilisant le moteur importExport
   * @param xmlFileName - fichier xml définissant les import et/ou export à effectuer
   * @return un rapport détaillé sur l'execution de l'import/export
   * @throws ImportExportException
   */
  public ImportReport processImport(UserDetail userDetail, String xmlFileName)
          throws ImportExportException {

    SilverPeasExchangeType silverExType = null;
    ImportReportManager.init();

    // Cas du nom de fichier null ou vide
    if ((xmlFileName == null) || (xmlFileName.length() == 0)) {
      UnitReport unitReport = new UnitReport("No XML file specified");
      unitReport.setError(UnitReport.ERROR_ERROR);
      unitReport.setStatus(UnitReport.STATUS_PUBLICATION_NOT_CREATED);
      ImportReportManager.addUnitReport(unitReport, "");
    }

    // Chargement du descripteur d'import à partir d'un fichier XML
    silverExType = loadSilverpeasExchange(xmlFileName);

    // Créations unitaires des thèmes et sous-thèmes avant d'insérer
    // des publication éventuelles dans ces thèmes et sous-thèmes
    if (silverExType.getNodeTreesType() != null) {
      // Traitement de l'élément <topicTrees>
      NodeTreesTypeManager typeMgr = new NodeTreesTypeManager();
      typeMgr.processImport(userDetail, silverExType.getNodeTreesType(), silverExType.getTargetComponentId());
    }

    // Créations unitaires de nouvelles publications ou modifications
    // de publications existantes
    if (silverExType.getPublicationsType() != null) {
      // Traitement de l'élément <publications>
      PublicationsTypeManager typeMgr = new PublicationsTypeManager();
      typeMgr.processImport(userDetail, silverExType.getPublicationsType(), silverExType.getTargetComponentId(), silverExType.isPOIUsed());
    }

    // Cas des imports en masse de thèmes et de publications
    if (silverExType.getRepositoriesType() != null) {
      // Traitement de l'élément <repositories>
      RepositoriesTypeManager typeMgr = new RepositoriesTypeManager();
      typeMgr.processImport(userDetail, silverExType.getRepositoriesType(), silverExType.isPOIUsed());
    }
    ImportReportManager.setEndDate(new Date());

    return ImportReportManager.getImportReport();
  }

  /**
   * Méthode faisant appel au moteur d'importExport de silver peas, des publications dont les
   * paramètres passés sous forme de WAAttributeValuePair sont exportées grace à l'outil castor.
   * @deprecated
   * @param userDetail - information sur l'utilisateur utilisant le moteur importExport
   * @param itemsToExport - liste de WAAttributeValuePair
   * @return un rapport détaillé sur l'execution de l'import/export
   * @throws ImportExportException
   */
  public ExportReport processExport(UserDetail userDetail, List listItemsToExport)
          throws ImportExportException {
    return processExport(userDetail, "fr", listItemsToExport);
  }

  public ExportReport processExport(UserDetail userDetail, String language, List listItemsToExport)
          throws ImportExportException {
    return processExport(userDetail, language, listItemsToExport, null);
  }

  public ExportReport processExport(UserDetail userDetail, String language, List listItemsToExport,
          String rootId) throws ImportExportException {
    // pour le multilangue
    ResourceLocator resourceLocator = new ResourceLocator(
            "com.silverpeas.importExport.multilang.importExportBundle", language);
    PublicationsTypeManager pub_Typ_Mger = new PublicationsTypeManager();
    PdcImportExport pdcIE = new PdcImportExport();
    NodeImportExport nodeIE = new NodeImportExport();
    AdminImportExport adminIE = new AdminImportExport();
    SilverPeasExchangeType silverPeasExch = new SilverPeasExchangeType();
    List listPubType = null;
    Iterator itListPubType = null;
    ExportReport exportReport = new ExportReport();

    try {
      // Purge le répertoire Temp de Silverpeas
      TempDirectoryManager.purgeTempDir();
      // Stockage de la date de démarage de l'export dans l'objet rapport
      exportReport.setDateDebut(new Date());
      // Création du dossier d'export
      String thisExportDir = generateExportDirName(userDetail, "export");
      String tempDir = FileRepositoryManager.getTemporaryPath();
      File fileExportDir = new File(tempDir + thisExportDir);
      if (!fileExportDir.exists()) {
        try {
          FileFolderManager.createFolder(fileExportDir);
        } catch (UtilException ex) {
          throw new ImportExportException("ImportExport", "importExport.EX_CANT_CREATE_FOLDER", ex);
        }
      }
      // Exportation des publications
      PublicationsType publicationsType;
      try {
        // création des répertoires avec le nom des thèmes et des
        // publications
        publicationsType = pub_Typ_Mger.processExport(exportReport, userDetail, listItemsToExport,
                fileExportDir.getPath(), true);
        if (publicationsType == null) {
          // les noms des thèmes et des publication est trop long ou au moins >
          // 200 caractères
          // création des répertoires avec les Id des thèmes et des
          // publications
          try {
            exportReport = new ExportReport();
            exportReport.setDateDebut(new Date());
            // détruire le répertoire et tout ce qu'il contient
            fileExportDir.delete();
            try {
              FileFolderManager.deleteFolder(fileExportDir.getPath());
            } catch (Exception ex) {
              throw new ImportExportException("ImportExport", "importExport.EX_CANT_DELETE_FOLDER",
                      ex);
            }
            thisExportDir = generateExportDirName(userDetail, "export");
            tempDir = FileRepositoryManager.getTemporaryPath();
            fileExportDir = new File(tempDir + thisExportDir);
            publicationsType = pub_Typ_Mger.processExport(exportReport, userDetail,
                    listItemsToExport, fileExportDir.getPath(), false);
          } catch (IOException e) {
            throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", e);
          }
        }
      } catch (IOException e1) {
        throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", e1);
      }
      silverPeasExch.setPublicationsType(publicationsType);

      // Récupération de la liste de id des composants
      List<String> listComponentId = new ArrayList<String>();
      listPubType = publicationsType.getListPublicationType();
      itListPubType = listPubType.iterator();
      while (itListPubType.hasNext()) {
        PublicationType pubType = (PublicationType) itListPubType.next();
        listComponentId.add(pubType.getComponentId());
      }

      // Exportation des composants liés aux publications exportées
      silverPeasExch.setComponentsType(adminIE.getComponents(listComponentId));
      // Exportation des Arbres de topics liés aux publications exportées
      NodeTreesType nodeTreesType = nodeIE.getTrees(listComponentId);
      silverPeasExch.setNodeTreesType(nodeTreesType);

      // Exportation des pdcs liés aux publications exportées
      List listClassifyPosition = new ArrayList();
      listPubType = publicationsType.getListPublicationType();
      itListPubType = listPubType.iterator();
      while (itListPubType.hasNext()) {
        PublicationType pubType = (PublicationType) itListPubType.next();
        PdcPositionsType pdcPos = pubType.getPdcPositionsType();
        if (pdcPos != null) {
          listClassifyPosition.addAll(pdcPos.getListClassifyPosition());
        }
      }
      if (listClassifyPosition.isEmpty()) {
        silverPeasExch.setPdcType(pdcIE.getPdc(listClassifyPosition));
      }

      if (rootId == null) {
        // dans le cas de l'export depuis le moteur de recherche, créer l'index
        // "a plat"
        // Création du sommaire HTML
        File fileHTML = new File(tempDir + thisExportDir + File.separator + "index.html");

        HtmlExportGenerator h = new HtmlExportGenerator(exportReport, fileExportDir.getName());
        FileWriter fileWriter = null;
        try {
          fileHTML.createNewFile();
          fileWriter = new FileWriter(fileHTML.getPath());
          fileWriter.write(h.toHTML());
        } catch (IOException ex) {
          throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", ex);
        } finally {
          try {
            fileWriter.close();
          } catch (Exception ex) {
          }
        }
        // Fin création du sommaire HTML
      } else {
        // dans le cas de l'export d'un composant ou d'un thème, créer l'index
        // en treeview

        // Création des sommaires HTML par Thèmes
        // --------------------------------------
        HtmlExportGenerator h = new HtmlExportGenerator(exportReport, fileExportDir.getName(),
                resourceLocator);
        FileWriter fileWriter = null;

        Hashtable topicIds = new Hashtable();
        // parcours la liste des publicationType
        Iterator itPubli = publicationsType.getListPublicationType().iterator();
        while (itPubli.hasNext()) {
          PublicationType publicationType = (PublicationType) itPubli.next();
          // pour chaque publication : parcourir ses noeuds
          Iterator itTopic = publicationType.getNodePositionsType().getListNodePositionType().iterator();
          while (itTopic.hasNext()) {
            NodePositionType nodePositionType = (NodePositionType) itTopic.next();
            // pour chaque topic : récupérer l'Id
            int topicId = nodePositionType.getId();
            // ajouter la référence
            String pubId = Integer.toString(publicationType.getId());
            // recherche s'il existe une ligne pour ce topic
            List pubIds = new ArrayList();
            if (topicIds.get(Integer.toString(topicId)) != null) {
              // le topic existe, ajouter la publication à sa liste
              pubIds = (List) topicIds.get(Integer.toString(topicId));
            }
            pubIds.add(pubId);
            topicIds.put(Integer.toString(topicId), pubIds);
          }
        }

        // parcours de la liste des topics et création du fichier
        Set keys = topicIds.keySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
          String topicId = (String) it.next();
          // créer le fichier
          File fileTopicHTML = new File(tempDir + thisExportDir + File.separator + "indexTopic"
                  + topicId + ".html");

          fileWriter = null;
          try {
            fileTopicHTML.createNewFile();
            fileWriter = new FileWriter(fileTopicHTML.getPath());
            fileWriter.write(h.toHTML(fileTopicHTML.getName(), (List) topicIds.get(topicId)));
          } catch (IOException ex) {
            throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", ex);
          } finally {
            try {
              fileWriter.close();
            } catch (Exception ex) {
            }
          }
        }
        // création d'un fichier sommaire vide pour les topics vides
        File fileTopicHTML = new File(tempDir + thisExportDir + File.separator
                + "indexTopicEmpty.html");
        fileWriter = null;
        try {
          fileTopicHTML.createNewFile();
          fileWriter = new FileWriter(fileTopicHTML.getPath());
          fileWriter.write(h.toHTML(fileTopicHTML.getName()));
        } catch (IOException ex) {
          throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", ex);
        } finally {
          try {
            fileWriter.close();
          } catch (Exception ex) {
          }
        }
        // FIN : création des sommaires HTML par Thèmes

        // création du fichier index contenant l'arborescence des thèmes
        // -------------------------------------------------------------
        File fileHTML = new File(tempDir + thisExportDir + File.separator + "index.html");
        fileWriter = null;
        try {
          fileHTML.createNewFile();
          fileWriter = new FileWriter(fileHTML.getPath());
          Set topics = topicIds.keySet();
          fileWriter.write(h.indexToHTML(fileHTML.getName(), topics, nodeTreesType, rootId));
        } catch (IOException ex) {
          throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", ex);
        } finally {
          try {
            fileWriter.close();
          } catch (Exception ex) {
          }
        }
        // FIN : création du fichier index contenant l'arborescence

        // Création du répertoire pour le treeview
        // ---------------------------------------
        try {
          // créer le répertoire pour le zip
          FileFolderManager.createFolder(tempDir + thisExportDir + File.separator + "treeview");
          // le remplir avec le contenu du répertoire "treeview" sur disque
          String chemin = (settings.getString("mappingDir"));
          if (chemin.startsWith("file:")) {
            chemin = chemin.substring(8);
          }
          chemin = chemin + "treeview";
          Collection files = FileFolderManager.getAllFile(chemin);
          Iterator itFiles = files.iterator();
          while (itFiles.hasNext()) {
            File file = (File) itFiles.next();
            File newFile = new File(tempDir + thisExportDir + File.separator + "treeview"
                    + File.separator + file.getName());
            FileRepositoryManager.copyFile(file.getPath(), newFile.getPath());
          }
        } catch (Exception e) {
          throw new ImportExportException("ImportExport", "importExport.EX_CANT_CREATE_FOLDER", e);
        }
        // FIN : création du répertoire pour le treeview
      }

      // Création du fichier XML de mapping
      upLoadSilverpeasExchange(silverPeasExch, fileExportDir.getPath() + File.separator
              + "importExport.xml");

      // Création du zip
      try {
        String zipFileName = fileExportDir.getName() + ".zip";
        long zipFileSize = ZipManager.compressPathToZip(fileExportDir.getPath(), tempDir
                + zipFileName);
        exportReport.setZipFileName(zipFileName);
        exportReport.setZipFileSize(zipFileSize);
        exportReport.setZipFilePath(FileServerUtils.getUrlToTempDir(zipFileName, zipFileName,
                "application/zip"));
      } catch (Exception ex) {
      }
      // Stockage de la date de fin de l'export dans l'objet rapport
      exportReport.setDateFin(new Date());
    } catch (IOException e1) {
      throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", e1);
    } catch (NodeRuntimeException ex) {
      throw new ImportExportException("importExport", "ImportExport.processExport()", ex);
    } catch (PdcException ex) {
      throw new ImportExportException("importExport", "ImportExport.processExport()", ex);
    }
    return exportReport;
  }

  /**
   * @param userDetail
   * @param language
   * @param listItemsToExport
   * @param rootId
   * @return
   * @throws ImportExportException
   */
  public ExportPDFReport processExportPDF(UserDetail userDetail, String language,
          List listItemsToExport, String rootId) throws ImportExportException {
    ExportPDFReport report = new ExportPDFReport();
    report.setDateDebut(new Date());

    PublicationsTypeManager pubTypeManager = new PublicationsTypeManager();

    String fileExportName = generateExportDirName(userDetail, "fusion");
    String tempDir = FileRepositoryManager.getTemporaryPath();

    File fileExportDir = new File(tempDir + fileExportName);
    if (!fileExportDir.exists()) {
      try {
        FileFolderManager.createFolder(fileExportDir);
      } catch (UtilException ex) {
        throw new ImportExportException("ImportExport", "importExport.EX_CANT_CREATE_FOLDER", ex);
      }
    }

    File pdfFileName = new File(tempDir + fileExportName + ".pdf");

    ArrayList pdfList;
    try {
      // création des répertoires avec le nom des thèmes et des publications
      pdfList = pubTypeManager.processPDFExport(report, userDetail, listItemsToExport,
              fileExportDir.getPath(), true);

      AttachmentDetail attDetail;
      try {
        int pageOffset = 0;
        ArrayList master = new ArrayList();
        Document document = null;
        PdfCopy writer = null;

        if (pdfList.size() != 0) {
          for (int nbFiles = 0; nbFiles < pdfList.size(); nbFiles++) {
            attDetail = (AttachmentDetail) pdfList.get(nbFiles);

            PdfReader reader = new PdfReader(fileExportDir.getPath() + File.separator
                    + attDetail.getLogicalName());
            reader.consolidateNamedDestinations();
            int nbPages = reader.getNumberOfPages();
            List bookmarks = SimpleBookmark.getBookmark(reader);
            if (bookmarks != null) {
              if (pageOffset != 0) {
                SimpleBookmark.shiftPageNumbers(bookmarks, pageOffset, null);
              }
              master.addAll(bookmarks);
            }
            pageOffset += nbPages;

            if (nbFiles == 0) {
              document = new Document(reader.getPageSizeWithRotation(1));
              writer = new PdfCopy(document, new FileOutputStream(pdfFileName));
              document.open();
            }

            for (int i = 1; i <= nbPages; i++) {
              PdfImportedPage page = writer.getImportedPage(reader, i);
              writer.addPage(page);
            }

            PRAcroForm form = reader.getAcroForm();
            if (form != null) {
              writer.copyAcroForm(reader);
            }
          }

          if (!master.isEmpty()) {
            writer.setOutlines(master);
          }
          writer.flush();
          document.close();
        } else {
          return null;
        }

      } catch (BadPdfFormatException e) {
        // Erreur lors de la copie
        throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", e);
      } catch (DocumentException e) {
        // Impossible de copier le document
        throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", e);
      }

    } catch (IOException e) {
      // Pb avec le répertoire de destination
      throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", e);
    }

    report.setPdfFileName(pdfFileName.getName());
    report.setPdfFileSize(pdfFileName.length());
    report.setPdfFilePath(FileServerUtils.getUrlToTempDir(pdfFileName.getName(), pdfFileName.getName(), "application/pdf"));

    report.setDateFin(new Date());

    return report;
  }

  /**
   * Export Kmax Publications
   * @param userDetail
   * @param language
   * @param listItemsToExport
   * @param combination
   * @param timeCriteria
   * @return
   * @throws ImportExportException
   */
  public ExportReport processExportKmax(UserDetail userDetail, String language,
          List listItemsToExport, ArrayList combination, String timeCriteria)
          throws ImportExportException {
    ResourceLocator resourceLocator = new ResourceLocator(
            "com.silverpeas.importExport.multilang.importExportBundle", language);

   
    PublicationsTypeManager pub_Typ_Mger = new PublicationsTypeManager();
    AdminImportExport adminIE = new AdminImportExport();
    SilverPeasExchangeType silverPeasExch = new SilverPeasExchangeType();
    List listPubType = null;
    Iterator itListPubType = null;
    ExportReport exportReport = new ExportReport();
    CoordinateImportExport coordinateImportExport = new CoordinateImportExport();
    GEDImportExport gedIE = null;
    OrganizationController orgController = new OrganizationController();

    try {
       // Purge le répertoire Temp de Silverpeas
    TempDirectoryManager.purgeTempDir();

      // Stockage de la date de démarage de l'export dans l'objet rapport
      exportReport.setDateDebut(new Date());
      // Création du dossier d'export
      String thisExportDir = generateExportDirName(userDetail, "export");

      String tempDir = FileRepositoryManager.getTemporaryPath();

      File fileExportDir = new File(tempDir + thisExportDir);
      if (!fileExportDir.exists()) {
        try {
          FileFolderManager.createFolder(fileExportDir);
        } catch (UtilException ex) {
          throw new ImportExportException("ImportExport", "importExport.EX_CANT_CREATE_FOLDER", ex);
        }
      }

      // Exportation des publications
      PublicationsType publicationsType;
      try {
        // création des répertoires avec le nom des publications
        publicationsType = pub_Typ_Mger.processExport(exportReport, userDetail, listItemsToExport,
                fileExportDir.getPath(), false);
      } catch (IOException e) {
        throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", e);
      }
      silverPeasExch.setPublicationsType(publicationsType);

      // Récupération de la liste de id des composants
      HashSet listComponentId = new HashSet();
      listPubType = publicationsType.getListPublicationType();
      itListPubType = listPubType.iterator();
      String componentId = null;
      while (itListPubType.hasNext()) {
        PublicationType pubType = (PublicationType) itListPubType.next();
        listComponentId.add(pubType.getComponentId());
        componentId = pubType.getComponentId();
      }

      // Exportation des composants liés aux publications exportées
      silverPeasExch.setComponentsType(adminIE.getComponents(new ArrayList(listComponentId)));

      // ================ EXPORT SELECTED PUBLICATIONS ======================

      if (combination != null) {
        // Publication list searched by criterias, create publications index in
        // flat mode

        // Création du sommaire HTML
        File fileHTML = new File(tempDir + thisExportDir + File.separator + "index.html");

        HtmlExportGenerator h = new HtmlExportGenerator(exportReport, fileExportDir.getName(),
                resourceLocator);
        FileWriter fileWriter = null;
        try {
          fileHTML.createNewFile();
          fileWriter = new FileWriter(fileHTML.getPath());
          // Create header with axes and values selected
          List positionsLabels = new ArrayList();
          positionsLabels = coordinateImportExport.getCombinationLabels(combination, componentId);
          fileWriter.write(h.kmaxPublicationsToHTML(positionsLabels, timeCriteria,
                  iframePublication));
        } catch (IOException ex) {
          throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", ex);
        } finally {
          try {
            fileWriter.close();
          } catch (Exception ex) {
          }
        }
      } else {
        // ================ EXPORT ALL PUBLICATIONS OF COMPONENT
        // ======================

        // Publication detail empty
        File emptyFileHTML = new File(tempDir + thisExportDir + File.separator + "empty.html");
        FileWriter fileWriter = null;
        try {
          emptyFileHTML.createNewFile();
          fileWriter = new FileWriter(emptyFileHTML.getPath());
        } catch (IOException ex) {
          throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", ex);
        } finally {
          try {
            fileWriter.close();
          } catch (Exception ex) {
          }
        }

        // Create unbalanced file html index
        ComponentInst componentInst = orgController.getComponentInst(componentId);
        gedIE = ImportExportFactory.createGEDImportExport(userDetail, componentId);

        String unbalancedFileNameRelativePath = "index-2.html";
        File unclassifiedFileHTML = new File(tempDir + thisExportDir + File.separator
                + unbalancedFileNameRelativePath);
        HtmlExportGenerator h = new HtmlExportGenerator(exportReport, fileExportDir.getName(),
                resourceLocator);
        try {
          unclassifiedFileHTML.createNewFile();
          fileWriter = new FileWriter(unclassifiedFileHTML.getPath(), true);
          fileWriter.write(h.toHtmlPublicationsByPositionStart());
        } catch (IOException ex) {
          throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", ex);
        } finally {
          try {
            fileWriter.close();
          } catch (Exception ex) {
          }
        }

        // Fill unbalanced file html index
        List unbalancedPublications = PublicationImportExport.getUnbalancedPublications(componentId);
        Iterator unbalancedPublicationsDetails = unbalancedPublications.iterator();
        String publicationFileNameRelativePath = "";
        String componentLabel = FileServerUtils.replaceAccentChars(componentInst.getLabel());
        while (unbalancedPublicationsDetails.hasNext()) {
          PublicationDetail pubDetail = (PublicationDetail) unbalancedPublicationsDetails.next();
          PublicationType publicationType = gedIE.getPublicationCompleteById(new Integer(pubDetail.getId()).toString(), componentId);
          publicationFileNameRelativePath = componentLabel + File.separator + pubDetail.getId()
                  + File.separator + "index.html";
          HtmlExportPublicationGenerator unbalanced = new HtmlExportPublicationGenerator(
                  publicationType, null, null, publicationFileNameRelativePath);
          exportReport.addHtmlIndex(pubDetail.getId(), unbalanced);
          fileWriter = null;
          try {
            fileWriter = new FileWriter(unclassifiedFileHTML.getPath(), true);
            fileWriter.write(unbalanced.toHtmlSommairePublication(iframePublication));
          } catch (IOException ex) {
            throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", ex);
          } finally {
            try {
              fileWriter.close();
            } catch (Exception ex) {
            }
          }
        }

        // Create HTML summary with the search axis
        File fileHTML = new File(tempDir + thisExportDir + File.separator + "index.html");

        h = new HtmlExportGenerator(exportReport, fileExportDir.getName(), resourceLocator);
        try {
          fileHTML.createNewFile();
          fileWriter = new FileWriter(fileHTML.getPath());
          // Create header with axes and values selected
          List axis = coordinateImportExport.getAxis(componentId);
          fileWriter.write(h.kmaxAxisToHTML(axis, language));
        } catch (IOException ex) {
          throw new ImportExportException("ImportExport", "root.EX_CANT_WRITE_FILE", ex);
        } finally {
          try {
            fileWriter.close();
          } catch (Exception ex) {
          }
        }

        // Create HTML summary
        // --------------------------------------
        String exportPath = fileExportDir.getPath();
        String exportSummaryPath = exportPath;
        List listAxis = coordinateImportExport.getAxisHeadersWithChildren(componentId, true, true);

        // Remove unclassified node
        listAxis.remove(0);
        int nbAxis = listAxis.size();

        Iterator itListAxis = listAxis.iterator();
        // Get list axis with values
        List listAxisWithChildren = new ArrayList();
        while (itListAxis.hasNext()) {
          NodeDetail currentAxisNodeDetail = (NodeDetail) itListAxis.next();
          Collection childrenNodeDetails = currentAxisNodeDetail.getChildrenDetails();
          if (childrenNodeDetails != null && childrenNodeDetails.size() > 0) {
            listAxisWithChildren.add(childrenNodeDetails);
          }
        }

        // Create List with all nodes Details
        List nodesIds = new ArrayList();
        Iterator itListAxisWithChildren = listAxisWithChildren.iterator();
        while (itListAxisWithChildren.hasNext()) {
          ArrayList currentAxis = (ArrayList) itListAxisWithChildren.next();
          Iterator itAxisNodesDetail = currentAxis.iterator();
          while (itAxisNodesDetail.hasNext()) {
            String nodeId = new Integer(((NodeDetail) itAxisNodesDetail.next()).getId()).toString();
            nodesIds.add(nodeId);
          }
        }

        // Create List of index files positions (ex: index-2-3-x-y-z....html)
        List indexFilesPositions = new ArrayList();
        // Process all filename combinations
        for (int i = 1; i <= nodesIds.size(); i++) {
          int tuple = i;
          indexFilesPositions = coordinateImportExport.coupleIds(indexFilesPositions, nodesIds, 0,
                  0, tuple, null, nbAxis);
        }

        // Create positions index files
        Iterator itIndexFilesPositions = indexFilesPositions.iterator();
        while (itIndexFilesPositions.hasNext()) {
          // fileName / index-x-x.html
          String positionNameId = (String) itIndexFilesPositions.next();

          // Create positions index file
          fileHTML = new File(exportSummaryPath + File.separator + positionNameId);

          // Write file positions
          fileWriter = null;
          try {
            fileHTML.createNewFile();
            fileWriter = new FileWriter(fileHTML.getPath());
            fileWriter.write(h.toHtmlPublicationsByPositionStart());
          } finally {
            try {
              fileWriter.close();
            } catch (Exception ex) {
              SilverTrace.debug("importExport", "PublicationTypeManager.processExport",
                      "root.MSG_GEN_PARAM_VALUE", "Exception = " + ex);
            }
          }
        }

        // Publications to export
        exportPath = fileExportDir.getPath();
        exportSummaryPath = exportPath;
        Iterator itListItemsToExport = listItemsToExport.iterator();

        while (itListItemsToExport.hasNext()) {
          List filesPositionsHTMLToFill = new ArrayList();
          WAAttributeValuePair attValue = (WAAttributeValuePair) itListItemsToExport.next();
          String pubId = attValue.getName();
          componentId = attValue.getValue();
          gedIE = ImportExportFactory.createGEDImportExport(userDetail, componentId);
          String positionFileNameHTML = "index";

          // Récupération du PublicationType
          PublicationType publicationType = gedIE.getPublicationCompleteById(pubId, componentId);
          publicationType.setCoordinatesPositionsType(new CoordinatesPositionsType());

          List listCoordinatesPositions = new ArrayList();

          Collection coordinates = gedIE.getPublicationCoordinates(pubId, componentId);
          Iterator itCoordinates = coordinates.iterator();
          while (itCoordinates.hasNext()) {
            positionFileNameHTML = "index";
            Coordinate coordinate = (Coordinate) itCoordinates.next();
            Collection coordinatesPoints = coordinate.getCoordinatePoints();
            Iterator itCoordinatesPoints = coordinatesPoints.iterator();
            while (itCoordinatesPoints.hasNext()) {
              // Build html position file to fill
              CoordinatePoint coordinatePoint = (CoordinatePoint) itCoordinatesPoints.next();
              positionFileNameHTML += "-" + coordinatePoint.getNodeId();
              listCoordinatesPositions.add(coordinatePoint);
            }
            if (!filesPositionsHTMLToFill.contains(positionFileNameHTML + ".html")) {
              filesPositionsHTMLToFill.add(positionFileNameHTML + ".html");
            }

            List nodeIds = new ArrayList();
            StringTokenizer st = new StringTokenizer(positionFileNameHTML, "-");
            String nodeId = "";
            while (st.hasMoreTokens()) {
              nodeId = st.nextToken();
              if (!nodeId.equals("index")) {
                NodeDetail currentNodeDetail = coordinateImportExport.getNodeHeader(new NodePK(
                        new Integer(nodeId).toString(), componentId));
                nodeIds.add(new Integer(currentNodeDetail.getId()).toString());
                if (currentNodeDetail.getLevel() >= 3) {
                  // if subvalue of axis, add this node
                  nodeIds = addNodeToList(nodeIds, currentNodeDetail);
                } else {
                  List axisChildren = coordinateImportExport.getAxisChildren(currentNodeDetail.getNodePK(), false);
                  // if Axis, add all nodes of this axis
                  for (int i = 0; i < axisChildren.size(); i++) {
                    NodeDetail nodeDetail = (NodeDetail) axisChildren.get(i);
                    nodeIds.add(new Integer(nodeDetail.getId()).toString());
                  }
                }
              }
            }

            List otherPositionsFilesNameHTML = new ArrayList();
            int tuple = nbAxis;
            otherPositionsFilesNameHTML = coordinateImportExport.coupleIds(
                    otherPositionsFilesNameHTML, nodeIds, 0, 0, tuple, null, nbAxis);
            for (int cpt = 0; cpt < otherPositionsFilesNameHTML.size(); cpt++) {
              String otherPositionFileNameHTML = (String) otherPositionsFilesNameHTML.get(cpt);
              if (!filesPositionsHTMLToFill.contains(otherPositionFileNameHTML)) {
                filesPositionsHTMLToFill.add(otherPositionFileNameHTML);
              }
            }
          }

          publicationType.getCoordinatesPositionsType().setCoordinatesPositions(
                  listCoordinatesPositions);
          SilverTrace.debug("importExport", "ImportExport.processExportKmax",
                  "root.MSG_GEN_PARAM_VALUE", "coordinatePositions added");

          publicationFileNameRelativePath = componentLabel + File.separator + pubId
                  + File.separator + "index.html";

          HtmlExportPublicationGenerator s = new HtmlExportPublicationGenerator(publicationType,
                  null, null, publicationFileNameRelativePath);
          exportReport.addHtmlIndex(pubId, s);

          Iterator itFilesPositionsHTMLToFill = filesPositionsHTMLToFill.iterator();
          while (itFilesPositionsHTMLToFill.hasNext()) {
            fileHTML = new File(exportSummaryPath + File.separator
                    + (String) itFilesPositionsHTMLToFill.next());
            SilverTrace.debug("importExport", "ImportExport.processExportKmax",
                    "root.MSG_GEN_PARAM_VALUE", "pubId = " + pubId);
            fileWriter = null;
            try {
              if (fileHTML.exists()) {
                fileWriter = new FileWriter(fileHTML.getPath(), true);
                fileWriter.write(s.toHtmlSommairePublication(iframePublication));
              }
            } finally {
              try {
                fileWriter.close();
              } catch (Exception ex) {
                SilverTrace.debug("importExport", "PublicationTypeManager.processExport",
                        "root.MSG_GEN_PARAM_VALUE", "Exception = " + ex);
              }
            }
          }
        }
      }

      // Création du fichier XML de mapping
      upLoadSilverpeasExchange(silverPeasExch, fileExportDir.getPath() + File.separator
              + "importExport.xml");

      // Création du zip
      try {
        String zipFileName = fileExportDir.getName() + ".zip";
        long zipFileSize = ZipManager.compressPathToZip(fileExportDir.getPath(), tempDir
                + zipFileName);
        exportReport.setZipFileName(zipFileName);
        exportReport.setZipFileSize(zipFileSize);
        exportReport.setZipFilePath(FileServerUtils.getUrlToTempDir(zipFileName, zipFileName,
                "application/zip"));
      } catch (Exception ex) {
      }
      // Stockage de la date de fin de l'export dans l'objet rapport
      exportReport.setDateFin(new Date());
    } catch (Exception ex) {
      throw new ImportExportException("importExport", "ImportExport.processExportKmax()", ex);
    }
    return exportReport;
  }

  /**
   * Méthode générant le nom de l'export nommé: "exportAAAA-MM-JJ-hh'H'mm'm'ss's'_userId"
   * @param userDetail - UserDetail de l'utilisateur
   * @param name : nom du fichier final
   * @return - la chaine représentant le nom généré du répertoire d'exportation
   */
  public String generateExportDirName(UserDetail userDetail, String name) {
    StringBuilder sb = new StringBuilder(name);
    Date date = new Date();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH'H'mm'm'ss's'");
    String dateFormatee = dateFormat.format(date);
    sb.append(dateFormatee);
    sb.append("_").append(userDetail.getId());
    return sb.toString();
  }

  public void writeImportToLog(ImportReport importReport, ResourcesWrapper resource) {
    if (importReport != null) {
      String reportLogFile = settings.getString("importExportLogFile");
      ResourceBundle resources = java.util.ResourceBundle.getBundle(
              "com.stratelia.silverpeas.silvertrace.settings.silverTrace", new Locale("", ""));
      String reportLogPath = resources.getString("ErrorDir");
      File file = new File(reportLogPath + File.separator + reportLogFile);
      FileWriter fileWriter = null;

      try {
        if (!file.exists()) {
          file.createNewFile();
        }
        fileWriter = new FileWriter(file.getPath(), true);

        fileWriter.write(importReport.writeToLog(resource));
      } catch (Exception ex) {
        ex.printStackTrace();
      } finally {
        try {
          fileWriter.close();
        } catch (Exception e) {
        }
      }
    }
  }

  /**
   * Add father of nodeDetail to List
   * @param nodesIds
   * @param nodeDetail
   * @param cie
   * @return
   */
  private List addNodeToList(List nodesIds, NodeDetail nodeDetail) {
    CoordinateImportExport cie = new CoordinateImportExport();
    // Add father
    nodesIds.add(new Integer(nodeDetail.getFatherPK().getId()).toString());
    if (nodeDetail.getLevel() >= 4) {
      NodeDetail parentNodeDetail = cie.getNodeHeader(nodeDetail.getFatherPK());
      addNodeToList(nodesIds, parentNodeDetail);
    }
    return nodesIds;
  }
}
