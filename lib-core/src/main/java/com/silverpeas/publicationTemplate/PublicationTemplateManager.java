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
package com.silverpeas.publicationTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.InputSource;

import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordSet;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.form.record.GenericRecordSet;
import com.silverpeas.form.record.GenericRecordSetManager;
import com.silverpeas.form.record.IdentifiedRecordTemplate;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

/**
 * The PublicationTemplateManager manages all the PublicationTemplate for all the Job'Peas.
 * It is a singleton.
 */
public class PublicationTemplateManager {

  private static final PublicationTemplateManager instance = new PublicationTemplateManager();
  // PublicationTemplates instances associated to silverpeas components. Theses templates should
  // already exist and be loaded.
  // map externalId -> PublicationTemplate
  private final Map<String, PublicationTemplate> externalTemplates = new HashMap<String, PublicationTemplate>();
  // All of the PublicationTemplates loaded in silverpeas and identified by their XML file.
  // map templateFileName -> PublicationTemplate to avoid multiple marshalling
  private final Map<String, PublicationTemplateImpl> templates = new HashMap<String, PublicationTemplateImpl>();
  public static String mappingPublicationTemplateFilePath = null;
  public static String mappingRecordTemplateFilePath = null;
  public static String templateDir = null;
  public static String defaultTemplateDir = null;

  static {
    ResourceLocator templateSettings =
        new ResourceLocator("com.silverpeas.publicationTemplate.settings.template", "");
    ResourceLocator mappingSettings =
        new ResourceLocator("com.silverpeas.publicationTemplate.settings.mapping", "");

    templateDir = templateSettings.getString("templateDir");
    defaultTemplateDir = System.getenv("SILVERPEAS_HOME") + "/data/templateRepository/";

    String mappingDir = mappingSettings.getString("mappingDir");
    String mappingPublicationTemplateFileName = mappingSettings.getString("templateFilesMapping");
    String mappingRecordTemplateFileName = mappingSettings.getString("templateMapping");

    mappingPublicationTemplateFilePath = makePath(mappingDir, mappingPublicationTemplateFileName);
    mappingRecordTemplateFilePath = makePath(mappingDir, mappingRecordTemplateFileName);
  }

  private PublicationTemplateManager() {
  }

  /**
   * Gets the single instance of this manager.
   * @return the single instance of PublicationTemplateManager.
   */
  public static PublicationTemplateManager getInstance() {
    return instance;
  }

  public static String makePath(String dirName, String fileName) {
    if (!StringUtil.isDefined(dirName)) {
      return fileName;
    }
    if (!StringUtil.isDefined(fileName)) {
      return dirName;
    }

    if (dirName.charAt(dirName.length() - 1) == '/' || dirName.charAt(dirName.length() - 1) == '\\') {
      return dirName.replace('\\', '/') + fileName.replace('\\', '/');
    }
    return dirName.replace('\\', '/') + "/" + fileName.replace('\\', '/');
  }

  public GenericRecordSet addDynamicPublicationTemplate(String externalId, String templateFileName)
      throws PublicationTemplateException {
    String fileName = templateFileName;
    try {
      if (!fileName.endsWith(".xml")) {
        fileName += ".xml";
      }
      PublicationTemplate thePubTemplate = loadPublicationTemplate(fileName);
      RecordTemplate recordTemplate = thePubTemplate.getRecordTemplate();
      return getGenericRecordSetManager().createRecordSet(externalId, recordTemplate, fileName);
    } catch (FormException e) {
      throw new PublicationTemplateException(
          "PublicationTemplateManager.addDynamicPublicationTemplate", "form.EXP_INSERT_FAILED",
          "externalId=" + externalId + ", templateFileName=" + templateFileName, e);
    }
  }

  public PublicationTemplate getPublicationTemplate(String externalId)
      throws PublicationTemplateException {
    return getPublicationTemplate(externalId, null);
  }

  /**
   * Returns the PublicationTemplate having the given externalId.
   * @param externalId
   * @param templateFileName 
   * @return
   * @throws PublicationTemplateException  
   */
  public PublicationTemplate getPublicationTemplate(String externalId,
      String templateFileName) throws PublicationTemplateException {
    String currentTemplateFileName = templateFileName;
    PublicationTemplate thePubTemplate = externalTemplates.get(externalId);
    if (thePubTemplate == null) {
      if (templateFileName == null) {
        try {
          RecordSet set = getGenericRecordSetManager().getRecordSet(externalId);
          IdentifiedRecordTemplate template = (IdentifiedRecordTemplate) set.getRecordTemplate();
          currentTemplateFileName = template.getTemplateName();
        } catch (Exception e) {
          throw new PublicationTemplateException(
              "PublicationTemplateManager.getPublicationTemplate", "form.EXP_INSERT_FAILED",
              "externalId=" + externalId + ", templateFileName=" + templateFileName, e);
        }
      }
      thePubTemplate = loadPublicationTemplate(currentTemplateFileName);
      thePubTemplate.setExternalId(externalId);
      externalTemplates.put(externalId, thePubTemplate);
    }

    return thePubTemplate;
  }

  /**
   * Removes the PublicationTemplate having the given externalId.
   * @param externalId 
   * @throws PublicationTemplateException 
   */
  public void removePublicationTemplate(String externalId) throws PublicationTemplateException {
    try {
      getGenericRecordSetManager().removeRecordSet(externalId);
    } catch (FormException e) {
      throw new PublicationTemplateException(
          "PublicationTemplateManager.removePublicationTemplate",
          "form.EXP_DELETE_FAILED", "externalId=" + externalId, e);
    }
  }

  /**
   * load a publicationTemplate definition from xml file to java objects
   * @param xmlFileName the xml file name that contains publication template definition
   * @return a PublicationTemplate object
   * @throws PublicationTemplateException  
   */
  public PublicationTemplate loadPublicationTemplate(String xmlFileName) throws
      PublicationTemplateException {
    SilverTrace.info("form", "PublicationTemplateManager.loadPublicationTemplate",
        "root.MSG_GEN_ENTER_METHOD", "xmlFileName=" + xmlFileName);
    try {
      PublicationTemplateImpl publicationTemplate = templates.get(xmlFileName);
      if (publicationTemplate != null) {
        return publicationTemplate.basicClone();
      }
      String xmlFilePath = makePath(templateDir, xmlFileName);

      File xmlFile = new File(xmlFilePath);
      if (xmlFile == null || !xmlFile.exists()) {
        //file does not exist in directory, try to locate it in default one
        xmlFilePath = makePath(defaultTemplateDir, xmlFileName);
        xmlFile = new File(xmlFilePath);
      }

      // Load mapping and instantiate a Marshaller
      Mapping mapping = new Mapping();
      mapping.loadMapping(mappingPublicationTemplateFilePath);
      Unmarshaller unmar = new Unmarshaller(mapping);

      // Unmarshall the process model
      publicationTemplate = (PublicationTemplateImpl) unmar.unmarshal(new InputSource(
          new FileInputStream(xmlFile)));
      publicationTemplate.setFileName(xmlFileName);

      templates.put(xmlFileName, publicationTemplate);

      return publicationTemplate.basicClone();
    } catch (MappingException me) {
      throw new PublicationTemplateException("PublicationTemplateManager.loadPublicationTemplate",
          "form.EX_ERR_CASTOR_LOAD_XML_MAPPING", "Publication Template FileName : " + xmlFileName,
          me);
    } catch (MarshalException me) {
      throw new PublicationTemplateException("PublicationTemplateManager.loadPublicationTemplate",
          "form.EX_ERR_CASTOR_UNMARSHALL_PUBLICATION_TEMPLATE", "Publication Template FileName : "
          + xmlFileName, me);
    } catch (ValidationException ve) {
      throw new PublicationTemplateException("PublicationTemplateManager.loadPublicationTemplate",
          "form.EX_ERR_CASTOR_INVALID_XML_PUBLICATION_TEMPLATE",
          "Publication Template FileName : " + xmlFileName, ve);
    } catch (IOException ioe) {
      throw new PublicationTemplateException("PublicationTemplateManager.loadPublicationTemplate",
          "form.EX_ERR_CASTOR_LOAD_PUBLICATION_TEMPLATE", "Publication Template FileName : "
          + xmlFileName, ioe);
    }
  }

  /**
   * save a publicationTemplate definition from java objects to xml file
   * @param template the PublicationTemplate to save
   * @throws PublicationTemplateException  
   */
  public void savePublicationTemplate(PublicationTemplate template) throws
      PublicationTemplateException {
    SilverTrace.info("form", "PublicationTemplateManager.savePublicationTemplate",
        "root.MSG_GEN_ENTER_METHOD", "template = " + template.getFileName());

    FileWriter writer = null;
    String xmlFileName = template.getFileName();

    try {
      // Format these url
      String xmlFilePath = makePath(templateDir, xmlFileName);

      // Load mapping and instantiate a Marshaller
      Mapping mapping = new Mapping();
      mapping.loadMapping(mappingPublicationTemplateFilePath);

      String encoding = "UTF-8";

      FileOutputStream fos = new FileOutputStream(xmlFilePath);
      OutputStreamWriter osw = new OutputStreamWriter(fos, encoding);

      // writer = new FileWriter(xmlFilePath);
      // Marshaller mar = new Marshaller(writer);
      Marshaller mar = new Marshaller(osw);

      mar.setEncoding(encoding);
      mar.setMapping(mapping);

      // Marshall the template
      mar.marshal(template);
    } catch (MappingException me) {
      throw new PublicationTemplateException("PublicationTemplateManager.loadPublicationTemplate",
          "form.EX_ERR_CASTOR_LOAD_XML_MAPPING", "Publication Template FileName : " + xmlFileName,
          me);
    } catch (MarshalException me) {
      throw new PublicationTemplateException("PublicationTemplateManager.loadPublicationTemplate",
          "form.EX_ERR_CASTOR_UNMARSHALL_PUBLICATION_TEMPLATE", "Publication Template FileName : "
          + xmlFileName, me);
    } catch (ValidationException ve) {
      throw new PublicationTemplateException("PublicationTemplateManager.loadPublicationTemplate",
          "form.EX_ERR_CASTOR_INVALID_XML_PUBLICATION_TEMPLATE",
          "Publication Template FileName : " + xmlFileName, ve);
    } catch (IOException ioe) {
      throw new PublicationTemplateException("PublicationTemplateManager.loadPublicationTemplate",
          "form.EX_ERR_CASTOR_LOAD_PUBLICATION_TEMPLATE", "Publication Template FileName : "
          + xmlFileName, ioe);
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          // do nothing
        }
      }
    }
  }

  public List<PublicationTemplate> getPublicationTemplates(boolean onlyVisibles)
      throws PublicationTemplateException {
    List<PublicationTemplate> publicationTemplates = new ArrayList<PublicationTemplate>();
    Collection<File> templateNames;
    try {
      templateNames = FileFolderManager.getAllFile(templateDir);
    } catch (UtilException e1) {
      throw new PublicationTemplateException("PublicationTemplateManager.getPublicationTemplates",
          "form.EX_ERR_CASTOR_LOAD_PUBLICATION_TEMPLATES", e1);
    }
    for (File templateFile : templateNames) {
      String fileName = templateFile.getName();
      try {
        String extension = FileRepositoryManager.getFileExtension(fileName);
        if ("xml".equalsIgnoreCase(extension)) {
          PublicationTemplate template =
              loadPublicationTemplate(fileName.substring(fileName.lastIndexOf(File.separator) + 1,
              fileName.length()));
          if (onlyVisibles) {
            if (template.isVisible()) {
              publicationTemplates.add(template);
            }
          } else {
            publicationTemplates.add(template);
          }
        }
      } catch (Exception e) {
        SilverTrace.error("form", "PublicationTemplateManager.getPublicationTemplates",
            "form.EX_ERR_CASTOR_LOAD_PUBLICATION_TEMPLATE", "fileName = " + fileName);
      }
    }

    return publicationTemplates;
  }

  public List<PublicationTemplate> getPublicationTemplates()
      throws PublicationTemplateException {
    return getPublicationTemplates(true);
  }

  public List<PublicationTemplate> getSearchablePublicationTemplates()
      throws PublicationTemplateException {
    List<PublicationTemplate> searchableTemplates = new ArrayList<PublicationTemplate>();

    List<PublicationTemplate> publicationTemplates = getPublicationTemplates();
    Iterator<PublicationTemplate> iterator = publicationTemplates.iterator();
    PublicationTemplate template = null;
    while (iterator.hasNext()) {
      template = iterator.next();
      try {
        if (template.getSearchForm() != null) {
          searchableTemplates.add(template);
        }
      } catch (PublicationTemplateException e) {
        // Catch exception here in case of one of searchable form is malformed
        // Valid forms must be displayed in search screen
        SilverTrace.warn("form", "PublicationTemplateManager.getSearchablePublicationTemplates",
            "form.ERROR_ONE_ILL_FORM", template.getName() + " is malformed");
      }
    }

    return searchableTemplates;
  }

  public void removePublicationTemplateFromCaches(String fileName) {
    SilverTrace.info("form", "PublicationTemplateManager.removePublicationTemplateFromCaches",
        "root.MSG_GEN_ENTER_METHOD", "fileName = " + fileName);
    List<String> externalIdsToRemove = new ArrayList<String>();
    Collection<PublicationTemplate> publicationTemplates = externalTemplates.values();
    for (PublicationTemplate template : publicationTemplates) {
      if (template.getFileName().equals(fileName)) {
        externalIdsToRemove.add(template.getExternalId());
      }
    }
    for (String externalId : externalIdsToRemove) {
      externalTemplates.remove(externalId);
    }

    templates.remove(fileName);

    getGenericRecordSetManager().removeTemplateFromCache(fileName);
  }

  /**
   * Gets an instance of a GenericRecordSet objects manager.
   * @return a GenericRecordSetManager instance.
   */
  private static GenericRecordSetManager getGenericRecordSetManager() {
    return GenericRecordSetManager.getInstance();
  }
}