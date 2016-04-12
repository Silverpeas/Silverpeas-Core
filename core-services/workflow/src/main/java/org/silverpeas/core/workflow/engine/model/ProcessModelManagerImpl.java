/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.workflow.engine.model;

import org.apache.commons.io.FileUtils;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.record.GenericRecordSetManager;
import org.silverpeas.core.exception.UtilException;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.workflow.api.ProcessModelManager;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.model.DataFolder;
import org.silverpeas.core.workflow.api.model.Form;
import org.silverpeas.core.workflow.api.model.Forms;
import org.silverpeas.core.workflow.api.model.ProcessModel;
import org.xml.sax.InputSource;

import javax.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A ProcessModelManager implementation
 */
@Singleton
public class ProcessModelManagerImpl implements ProcessModelManager {

  private static final String selectQuery =
      "select distinct modelId from SB_Workflow_ProcessInstance";

  /**
   * ResourceLocator object to retrieve settings in a properties file
   */
  private static SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.workflow.engine.castorSettings");

  /**
   * The map (modelId -> cached process model).
   */
  private final Map<String, ProcessModel> models = new HashMap<>();

  /**
   * Default constructor
   */
  public ProcessModelManagerImpl() {
  }

  /**
   * @see ProcessModelManager#listProcessModels()
   */
  @Override
  public List<String> listProcessModels() throws WorkflowException {
    try {
      // Recursively search all subdirs for .xml files
      return findProcessModels(getProcessModelDir());
    } catch (UtilException | IOException e) {
      throw new WorkflowException("WorkflowManager.getProcessModels",
          "WorkflowEngine.EX_GETTING_RPOCES_MODELS_FAILED",
          "Workflow Dir : " + getProcessModelDir(), e);
    }
  }

  /**
   * Recursive method to retrieve all process models in and below the given directory
   * @param strProcessModelDir the directory to start with
   * @return a list of strings containing the relative path and file name of the model
   * @throws UtilException
   * @throws IOException
   */
  private List<String> findProcessModels(String strProcessModelDir)
      throws UtilException, IOException {
    Iterator<File> subFoldersIterator =
        FileFolderManager.getAllSubFolder(strProcessModelDir).iterator();
    Iterator<String> subFolderModelsIterator;
    Iterator<File> currentDirModelsIterator =
        FileFolderManager.getAllFile(strProcessModelDir).iterator();
    List<String> processModels = new ArrayList<>();
    File subFolder;

    while (subFoldersIterator.hasNext()) {
      subFolder = subFoldersIterator.next();
      // Get models from subfolders
      subFolderModelsIterator = findProcessModels(subFolder.getCanonicalPath()).iterator();

      // prepend their names with the name of this folder
      while (subFolderModelsIterator.hasNext()) {
        processModels
            .add(subFolder.getName() + File.separatorChar + subFolderModelsIterator.next());
      }
    }
    // Get models from the current folder, do not prepend names
    while (currentDirModelsIterator.hasNext()) {
      String name = currentDirModelsIterator.next().getName();

      if (name.endsWith(".xml")) {
        processModels.add(name);
      }
    }
    // Return the list of process models
    return processModels;
  }

  /**
   * Get a ProcessModel from its modelId. Retrieves the xml descriptor filename from the model Id
   * and load abstract process model information in ProcessModel object
   * @param modelId model id
   * @return ProcessModel object
   */
  @Override
  public ProcessModel getProcessModel(String modelId) throws WorkflowException {
    // Search the processModel in the process model cache.
    ProcessModel cachedModel = getCachedProcessModel(modelId);
    if (cachedModel != null) {
      return cachedModel;
    }

    // The model is not cached, we must build it.
    String fileName = AdministrationServiceProvider.getAdminService()
        .getComponentParameterValue(modelId, PROCESS_XML_FILE_NAME);

    // if file name not found, throw exception
    if (fileName == null) {
      throw new WorkflowException("ProcessModelManagerImpl.getProcessModel",
          "workflowEngine.EX_NO_XML_FILENAME_FOUND", "model/peas id : " + modelId);
    }

    // load the process model from its xml descriptor
    ProcessModelImpl model = (ProcessModelImpl) this.loadProcessModel(fileName, false);

    // set the peas id
    model.setModelId(modelId);

    // cache the model.
    cacheProcessModel(modelId, model, fileName);

    // return the process model
    return model;
  }

  /*
   * (non-Javadoc)
   * @see ProcessModelManager#createProcessModelDescriptor ()
   */
  @Override
  public ProcessModel createProcessModelDescriptor() throws WorkflowException {
    return new ProcessModelImpl();
  }

  /**
   * Create a ProcessModel from xml descriptor filename. Generate an id for this model and load
   * abstract process model information in ProcessModel object
   * @param processFileName xml descriptor filename.
   * @param peasId Id of processManager instance (peas).
   * @return ProcessModel object
   */
  @Override
  public ProcessModel createProcessModel(String processFileName, String peasId)
      throws WorkflowException {
    ProcessModel model;
    DataFolder folder;
    RecordTemplate template;
    Forms forms;

    try {
      // Load abstract process model
      model = this.loadProcessModel(processFileName, false);
      model.setModelId(peasId);

      // Creates datafolder in database
      folder = model.getDataFolder();
      template = folder.toRecordTemplate(null, null, true);
      getGenericRecordSetManager().createRecordSet(model.getFolderRecordSetName(), template);

      // Creates forms in database
      forms = model.getForms();
      if (forms != null) {
        Iterator<Form> iterForm = forms.iterateForm();
        Form form;

        while (iterForm.hasNext()) {
          form = iterForm.next();
          template = form.toRecordTemplate(null, null);
          getGenericRecordSetManager()
              .createRecordSet(model.getFormRecordSetName(form.getName()), template);
        }
      }
    } catch (FormException | WorkflowException fe) {
      throw new WorkflowException("ProcessModelManagerImpl.createProcessModel",
          "workflowEngine.EX_ERR_INSTANCIATING_MODEL",
          "Process FileName : " + processFileName == null ? "<null>" : processFileName, fe);
    }

    return model;
  }

  /**
   * Delete a ProcessModel with given model id
   * @param instanceId model id
   */
  @Override
  public void deleteProcessModel(String instanceId) throws WorkflowException {
    ProcessModel model = getProcessModel(instanceId);
    String formName = null;
    try {
      Forms forms;

      // delete the folder
      formName = model.getFolderRecordSetName();
      getGenericRecordSetManager().removeRecordSet(formName);

      // delete forms associated to actions
      forms = model.getForms();
      if (forms != null) {
        Iterator<Form> iterForm = forms.iterateForm();

        while (iterForm.hasNext()) {
          formName = iterForm.next().getName();
          getGenericRecordSetManager().removeRecordSet(model.getFormRecordSetName(formName));
        }
      }
    } catch (FormException fe) {
      throw new WorkflowException("ProcessModelManagerImpl.deleteProcessModel",
          "workflowEngine.EX_ERR_UNINSTANCIATING_MODEL",
          "instanceId : " + instanceId == null ? "<null>" :
              instanceId + ", formName = " + formName == null ? "<null>" : formName, fe);
    }
  }

  @Override
  public void deleteProcessModelDescriptor(String strProcessModelFileName)
      throws WorkflowException {
    try {
      File workflowsDirectory = new File(getProcessModelDir());
      File file = new File(workflowsDirectory, strProcessModelFileName);
      File directory = file.getParentFile();

      if (directory.equals(workflowsDirectory)) {
        // remove file only
        FileFolderManager.deleteFile(getProcessModelDir() + strProcessModelFileName);
      } else {
        // remove subdirectory and all its content
        FileUtil.forceDeletion(directory);
      }

      // Clear process model cache
      clearProcessModelCache();
    } catch (Exception e) {
      throw new WorkflowException("WorkflowManager.getProcessModels",
          "WorkflowEngine.EX_GETTING_RPOCES_MODELS_FAILED", "Process Model File name : " +
          (strProcessModelFileName == null ? "<null>" : strProcessModelFileName), e);
    }
  }

  /**
   * load a process model definition from xml file to java objects
   * @param processFileName the xml file name that contains process model definition
   * @param absolutePath true if xml file name contains the full path, else concat with the
   * directory defined in castorSettings.properties
   * @return a ProcessModel object
   */
  @Override
  public ProcessModel loadProcessModel(String processFileName, boolean absolutePath)
      throws WorkflowException {
    Mapping mapping = new Mapping();

    // get configuration files url
    String mappingFileName = settings.getString("CastorXMLMappingFileURL");
    boolean debugMode = settings.getBoolean("DebugMode", false);
    String processPath = processFileName;
    try {
      // Format these url
      if (!FileUtil.isWindows()) {
        mappingFileName = mappingFileName.replace('\\', '/');
      } else {
        mappingFileName = "file:///" + mappingFileName.replace('\\', '/');
      }
      if (!absolutePath) {
        processPath = getProcessPath(processFileName);
      }

      // Load mapping and instantiate a Marshaller
      mapping.loadMapping(mappingFileName);
      Unmarshaller unmar = new Unmarshaller(mapping);
      unmar.setValidation(false);
      unmar.setDebug(debugMode);
      // Unmarshall the process model
      ProcessModelImpl process =
          (ProcessModelImpl) unmar.unmarshal(new InputSource(new FileInputStream(processPath)));

      if (debugMode) {
        // Marshall for debugging purpose
        String debugFile = getProcessPath("debug.xml");
        Marshaller mar = new Marshaller(new FileWriter(debugFile));
        mar.setMapping(mapping);
        mar.marshal(process);
      }
      return process;
    } catch (MappingException me) {
      throw new WorkflowException("ProcessModelManagerImpl.loadProcessModel",
          "workflowEngine.EX_ERR_CASTOR_LOAD_XML_MAPPING",
          "Mapping file name : " + (mappingFileName == null ? "<null>" : mappingFileName), me);
    } catch (MarshalException me) {
      throw new WorkflowException("ProcessModelManagerImpl.loadProcessModel",
          "workflowEngine.EX_ERR_CASTOR_UNMARSHALL_PROCESSMODEL",
          "Process File Name : " + (processFileName == null ? "<null>" : processFileName), me);
    } catch (ValidationException ve) {
      throw new WorkflowException("ProcessModelManagerImpl.loadProcessModel",
          "workflowEngine.EX_ERR_CASTOR_INVALID_XML_PROCESSMODEL",
          "Process File Name : " + (processFileName == null ? "<null>" : processFileName), ve);
    } catch (IOException ioe) {
      throw new WorkflowException("ProcessModelManagerImpl.loadProcessModel",
          "workflowEngine.EX_ERR_CASTOR_LOAD_PROCESSMODEL",
          "Process File Name : " + (processFileName == null ? "<null>" : processFileName), ioe);
    }
  }

  /**
   * Saves a process model definition from java objects to an XML file
   * @param processFileName the xml file name that contains process model definition
   * @param process A processModel object to be saved
   * @throws WorkflowException when something goes wrong
   */
  @Override
  public void saveProcessModel(ProcessModel process, String processFileName)
      throws WorkflowException {
    Mapping mapping = new Mapping();
    // get configuration files url
    String mappingFileName = settings.getString("CastorXMLMappingFileURL");
    String schemaFileName = settings.getString("ProcessModesSchemaFileURL");
    String strProcessModelFileEncoding = settings.getString("ProcessModelFileEncoding");
    boolean runOnUnix = !FileUtil.isWindows();
    String processPath = getProcessPath(processFileName);
    try {
      if (runOnUnix) {
        mappingFileName = mappingFileName.replace('\\', '/');
      } else {
        mappingFileName = "file:///" + mappingFileName.replace('\\', '/');
      }
      mapping.loadMapping(mappingFileName);
      File file = new File(processPath);
      OutputStreamWriter writer = new OutputStreamWriter(FileUtils.openOutputStream(file),
          strProcessModelFileEncoding);
      Marshaller mar = new Marshaller(writer);
      mar.setMapping(mapping);
      mar.setNoNamespaceSchemaLocation(schemaFileName);
      mar.setSuppressXSIType(true);
      mar.setValidation(false);
      mar.setEncoding(strProcessModelFileEncoding);
      mar.marshal(process);
      writer.close();
      clearProcessModelCache();
    } catch (MappingException me) {
      throw new WorkflowException("ProcessModelManagerImpl.saveProcessModel",
          "workflowEngine.EX_ERR_CASTOR_LOAD_XML_MAPPING",
          "Mapping file name : " + (mappingFileName == null ? "<null>" : mappingFileName), me);
    } catch (MarshalException me) {
      throw new WorkflowException("ProcessModelManagerImpl.saveProcessModel",
          "workflowEngine.EX_ERR_CASTOR_MARSHALL_PROCESSMODEL",
          "Process file name : " + (processPath == null ? "<null>" : processPath), me);
    } catch (ValidationException ve) {
      throw new WorkflowException("ProcessModelManagerImpl.saveProcessModel",
          "workflowEngine.EX_ERR_CASTOR_INVALID_XML_PROCESSMODEL",
          "Process file name : " + (processPath == null ? "<null>" : processPath), ve);
    } catch (IOException ioe) {
      throw new WorkflowException("ProcessModelManagerImpl.saveProcessModel",
          "workflowEngine.EX_ERR_CASTOR_SAVE_PROCESSMODEL",
          "Process file name : " + (processPath == null ? "<null>" : processPath), ioe);
    }
  }

  /**
   * Get the directory where are stored the models
   */
  @Override
  public String getProcessModelDir() {
    String dir = FileUtil.convertPathToServerOS(settings.getString("ProcessModelDir", null));
    if (dir != null && !dir.endsWith(File.separator)) {
      dir = dir + File.separatorChar;
    }
    return dir;
  }

  /**
   * Get all the "process manager" peas ids
   */
  @Override
  public String[] getAllPeasIds() throws WorkflowException {
    Connection con = null;
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      List<String> peasIds = new ArrayList<>();
      con = this.getConnection();
      prepStmt = con.prepareStatement(selectQuery);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        peasIds.add(rs.getString(1));
      }
      return peasIds.toArray(new String[peasIds.size()]);
    } catch (SQLException se) {
      throw new WorkflowException("ProcessModelManagerImpl.getAllPeasId",
          "workflowEngine.EX_ERR_GET_ALL_PEAS_IDS",
          "sql query : " + selectQuery == null ? "<null>" : selectQuery, se);
    } finally {
      try {
        DBUtil.close(rs, prepStmt);
        if (con != null) {
          con.close();
        }
      } catch (SQLException se) {
        SilverTrace.error("workflowEngine", "ProcessModelManagerImpl.getAllPeasId",
            "root.EX_RESOURCE_CLOSE_FAILED", se);
      }
    }
  }

  /**
   * Search the cache for the required process model.
   */
  private ProcessModel getCachedProcessModel(String modelId) {
    ProcessModel model = models.get(modelId);
    return model;
  }

  /**
   * Put the given process model in the the cache.
   */
  private void cacheProcessModel(String modelId, ProcessModel model, String filename) {
    synchronized (models) {
      models.put(modelId, model);
    }
  }

  /**
   * Clear the process model cache.
   */
  @Override
  public void clearProcessModelCache() {
    synchronized (models) {
      models.clear();
    }
  }

  /**
   * @return the DB connection
   */
  private Connection getConnection() throws WorkflowException {
    Connection con;
    try {
      con = DBUtil.openConnection();
      return con;
    } catch (SQLException se) {
      throw new WorkflowException("ProcessModelManagerImpl.getConnection()",
          "root.EX_CONNECTION_OPEN_FAILED", se);
    }
  }

  protected String getProcessPath(String processFileName) {
    String processModelDir = settings.getString("ProcessModelDir");
    processModelDir = processModelDir.replace('\\', '/');
    if (processModelDir.length() > 0 && !processModelDir.endsWith("/")) {
      processModelDir = processModelDir + '/';
    }
    return processModelDir + processFileName;
  }

  /**
   * Gets an instance of a GenericRecordSet objects manager.
   * @return a GenericRecordSetManager instance.
   */
  private GenericRecordSetManager getGenericRecordSetManager() {
    return GenericRecordSetManager.getInstance();
  }
}
