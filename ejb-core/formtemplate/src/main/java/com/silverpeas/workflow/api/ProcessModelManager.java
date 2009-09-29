package com.silverpeas.workflow.api;

import com.silverpeas.workflow.api.model.*;
import java.util.List;

/**
 * The workflow engine services related to process model management.
 */
public interface ProcessModelManager {
  /**
   * List all the ProcessModels that are stored in the process model directory
   * Retrieves all the files in the directory tree below the process model
   * directory.
   * 
   * @return list of strings containing ProcesModel XML descriptor filenames
   *         with relative paths.
   * @throws WorkflowException
   *           when something goes wrong
   */
  public List listProcessModels() throws WorkflowException;

  /**
   * Get a ProcessModel from its modelId. Retrieves the xml descriptor filename
   * from the model Id and load abstract process model information in
   * ProcessModel object
   * 
   * @param modelId
   *          model id
   * @return ProcessModel object
   * @throws WorkflowException
   *           when something goes wrong
   */
  public ProcessModel getProcessModel(String modelId) throws WorkflowException;

  /**
   * Create a ProcessModel from xml descriptor filename. Generate an id for this
   * model and load abstract process model information in ProcessModel object
   * 
   * @param fileName
   *          xml descriptor filename.
   * @param peasId
   *          Id of processManager instance (peas).
   * @return ProcessModel object
   * @throws WorkflowException
   *           when something goes wrong
   */
  public ProcessModel createProcessModel(String fileName, String peasId)
      throws WorkflowException;

  /**
   * Create a new ProcessModel descriptor that is not yet saved in a XML file.
   * 
   * @return ProcessModel object
   * @throws WorkflowException
   *           when something goes wrong
   */
  public ProcessModel createProcessModelDescriptor() throws WorkflowException;

  /**
   * Delete a ProcessModel with given model id
   * 
   * @param modelId
   *          model id
   * @throws WorkflowException
   *           when something goes wrong
   */
  public void deleteProcessModel(String instanceId) throws WorkflowException;

  /**
   * Delete a ProcessModelDescriptor XML with given path
   * 
   * @param strProcessModelFileName
   *          the relative path and file name of the process file
   * @throws WorkflowException
   *           when something goes wrong or the file cannot be found
   */
  public void deleteProcessModelDescriptor(String strProcessModelFileName)
      throws WorkflowException;

  /**
   * Get the directory where are stored the models
   */
  public String getProcessModelDir();

  /**
   * load a process model definition from xml file to java objects
   * 
   * @param processFileName
   *          the xml file name that contains process model definition
   * @param absolutePath
   *          true if xml file name contains the full path, else concat with the
   *          directory defined in castorSettings.properties
   * @return a ProcessModel object
   * @throws WorkflowException
   *           when something goes wrong or the file cannot be found
   */
  public ProcessModel loadProcessModel(String processFileName,
      boolean absolutePath) throws WorkflowException;

  /**
   * Get all the "process manager" peas ids
   */
  public String[] getAllPeasIds() throws WorkflowException;

  /**
   * Saves a process model definition from java objects to an XML file
   * 
   * @param processFileName
   *          the xml file name that contains process model definition
   * @param process
   *          A processModel object to be saved
   * @throws WorkflowException
   *           when something goes wrong
   */
  public void saveProcessModel(ProcessModel process, String processFileName)
      throws WorkflowException;

  public void clearProcessModelCache() throws WorkflowException;

}