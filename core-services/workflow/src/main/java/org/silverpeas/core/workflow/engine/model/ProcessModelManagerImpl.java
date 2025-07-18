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
package org.silverpeas.core.workflow.engine.model;

import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.record.GenericRecordSetManager;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.core.workflow.api.ProcessModelManager;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.model.DataFolder;
import org.silverpeas.core.workflow.api.model.Form;
import org.silverpeas.core.workflow.api.model.Forms;
import org.silverpeas.core.workflow.api.model.ProcessModel;
import org.silverpeas.core.workflow.util.WorkflowUtil;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A ProcessModelManager implementation
 */
@Singleton
public class ProcessModelManagerImpl implements ProcessModelManager {

  private static final String SELECT_QUERY =
      "select distinct modelId from SB_Workflow_ProcessInstance";

  /**
   * ResourceLocator object to retrieve settings in a properties file
   */
  private static final SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.workflow.engine.settings");

  /**
   * The map (modelId -> cached process model).
   */
  private final Map<String, ProcessModel> models = new HashMap<>();
  private JAXBContext jaxbContext = null;

  @PostConstruct
  private void setup() {
    try {
      jaxbContext = JAXBContext.newInstance(ProcessModelImpl.class);
    } catch (JAXBException e) {
      SilverLogger.getLogger(this).error("Cannot initialize jaxbContext", e);
    }
  }

  /**
   * @see ProcessModelManager#listProcessModels()
   */
  @Override
  public List<String> listProcessModels() throws WorkflowException {
    try {
      // Recursively search all subdirs for .xml files
      return findProcessModels(getProcessModelDir());
    } catch (RuntimeException | IOException e) {
      throw new WorkflowException("WorkflowManager.getProcessModels",
          "WorkflowEngine.EX_GETTING_RPOCES_MODELS_FAILED",
          "Workflow Dir : " + getProcessModelDir(), e);
    }
  }

  /**
   * Recursive method to retrieve all process models in and below the given directory
   * @param strProcessModelDir the directory to start with
   * @return a list of strings containing the relative path and file name of the model
   * @throws IOException if an IO error occurs
   */
  private List<String> findProcessModels(String strProcessModelDir) throws IOException {
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
        .getComponentParameterValue(modelId, WorkflowUtil.PROCESS_XML_FILE_NAME);

    // if file name not found, throw exception
    if (fileName == null) {
      throw new WorkflowException("ProcessModelManagerImpl.getProcessModel",
          "workflowEngine.EX_NO_XML_FILENAME_FOUND", "model/peas id : " + modelId);
    }

    // load the process model from its xml descriptor
    ProcessModelImpl model = (ProcessModelImpl) this.loadProcessModel(fileName);

    // set the peas id
    model.setModelId(modelId);

    // cache the model.
    cacheProcessModel(modelId, model);

    // return the process model
    return model;
  }

  /*
   * (non-Javadoc)
   * @see ProcessModelManager#createProcessModelDescriptor ()
   */
  @Override
  public ProcessModel createProcessModelDescriptor() {
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

    Objects.requireNonNull(processFileName, "The process model file is null!");
    try {
      // Load abstract process model
      model = this.loadProcessModel(processFileName);
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
          "workflowEngine.EX_ERR_INSTANCIATING_MODEL", "Process FileName : " + processFileName, fe);
    }

    return model;
  }

  /**
   * Delete a ProcessModel with given model id
   * @param instanceId model id
   */
  @Override
  public void deleteProcessModel(String instanceId) throws WorkflowException {
    Objects.requireNonNull(instanceId);

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
          "instanceId : " + instanceId + ", formName = " + formName, fe);
    }
  }

  @Override
  public void deleteProcessModelDescriptor(String strProcessModelFileName)
      throws WorkflowException {
    Objects.requireNonNull(strProcessModelFileName);
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
          strProcessModelFileName, e);
    }
  }

  /**
   * load a process model definition from xml file to java objects
   * @param processFileName the xml file name that contains process model definition
   * @return a ProcessModel object
   */
  @Override
  public ProcessModel loadProcessModel(String processFileName)
      throws WorkflowException {
    boolean debugMode = settings.getBoolean("DebugMode", false);
    String processPath = getProcessPath(processFileName);
    try {
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      ProcessModelImpl process = (ProcessModelImpl) unmarshaller.unmarshal(new File(processPath));

      if (debugMode) {
        // Marshall for debugging purpose

        String debugFile = getProcessPath("debug."+ new Date().getTime()+".xml");
        Marshaller mar = jaxbContext.createMarshaller();
        mar.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        mar.marshal(process, new File(debugFile));
      }
      return process;
    } catch (JAXBException e) {
      throw new WorkflowException("ProcessModelManagerImpl.loadProcessModel",
          "workflowEngine.EX_ERR_LOAD_XML_MAPPING",
          "Process path : " + processPath, e);
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
    // get configuration files url
    String schemaFileName = settings.getString("ProcessModelSchemaFileURL", null);

    String processPath = getProcessPath(processFileName);
    try {
      Marshaller marshaller = jaxbContext.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, schemaFileName);
      marshaller.marshal(process, new File(processPath));

      clearProcessModelCache();
    } catch (JAXBException e) {
      throw new WorkflowException("ProcessModelManagerImpl.saveProcessModel",
          "workflowEngine.EX_ERR_LOAD_XML_MAPPING",
          "Process path = " + processPath, e);
    }
  }

  /**
   * Get the directory where are stored the models
   */
  @Override
  public String getProcessModelDir() {
    String dir = FileUtil.convertPathToServerOS(settings.getString("ProcessModelDir", null));
    if (!dir.endsWith(File.separator)) {
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
      prepStmt = con.prepareStatement(SELECT_QUERY);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        peasIds.add(rs.getString(1));
      }
      return peasIds.toArray(new String[0]);
    } catch (SQLException se) {
      throw new WorkflowException("ProcessModelManagerImpl.getAllPeasId",
          "workflowEngine.EX_ERR_GET_ALL_PEAS_IDS",
          "sql query : " + SELECT_QUERY, se);
    } finally {
      try {
        DBUtil.close(rs, prepStmt);
        if (con != null) {
          con.close();
        }
      } catch (SQLException se) {
        SilverLogger.getLogger(this).error(se);
      }
    }
  }

  /**
   * Search the cache for the required process model.
   */
  private ProcessModel getCachedProcessModel(String modelId) {
    return models.get(modelId);
  }

  /**
   * Put the given process model in the the cache.
   */
  private void cacheProcessModel(String modelId, ProcessModel model) {
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
    if (!processModelDir.isEmpty() && !processModelDir.endsWith("/")) {
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
