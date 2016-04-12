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

package org.silverpeas.core.workflow.api;

import java.util.List;

import org.silverpeas.core.workflow.api.model.ProcessModel;

/**
 * The workflow engine services related to process model management.
 */
public interface ProcessModelManager {

  /**
   * The name of the parameter holding the process file name in a workflow application descriptor.
   */
  String PROCESS_XML_FILE_NAME = "XMLFileName";

  /**
   * List all the ProcessModels that are stored in the process model directory Retrieves all the
   * files in the directory tree below the process model directory.
   * @return list of strings containing ProcesModel XML descriptor filenames with relative paths.
   * @throws WorkflowException when something goes wrong
   */
  List<String> listProcessModels() throws WorkflowException;

  /**
   * Get a ProcessModel from its modelId. Retrieves the xml descriptor filename from the model Id
   * and load abstract process model information in ProcessModel object
   * @param modelId model id
   * @return ProcessModel object
   * @throws WorkflowException when something goes wrong
   */
  ProcessModel getProcessModel(String modelId) throws WorkflowException;

  /**
   * Create a ProcessModel from xml descriptor filename. Generate an id for this model and load
   * abstract process model information in ProcessModel object
   * @param fileName xml descriptor filename.
   * @param peasId Id of processManager instance (peas).
   * @return ProcessModel object
   * @throws WorkflowException when something goes wrong
   */
  ProcessModel createProcessModel(String fileName, String peasId)
      throws WorkflowException;

  /**
   * Create a new ProcessModel descriptor that is not yet saved in a XML file.
   * @return ProcessModel object
   * @throws WorkflowException when something goes wrong
   */
  ProcessModel createProcessModelDescriptor() throws WorkflowException;

  /**
   * Delete a ProcessModel with given model id
   * @param instanceId component instance identifier
   * @throws WorkflowException when something goes wrong
   */
  void deleteProcessModel(String instanceId) throws WorkflowException;

  /**
   * Delete a ProcessModelDescriptor XML with given path
   * @param strProcessModelFileName the relative path and file name of the process file
   * @throws WorkflowException when something goes wrong or the file cannot be found
   */
  void deleteProcessModelDescriptor(String strProcessModelFileName)
      throws WorkflowException;

  /**
   * Get the directory where are stored the models
   */
  String getProcessModelDir();

  /**
   * load a process model definition from xml file to java objects
   * @param processFileName the xml file name that contains process model definition
   * @param absolutePath true if xml file name contains the full path, else concat with the
   * directory defined in castorSettings.properties
   * @return a ProcessModel object
   * @throws WorkflowException when something goes wrong or the file cannot be found
   */
  ProcessModel loadProcessModel(String processFileName,
      boolean absolutePath) throws WorkflowException;

  /**
   * Get all the "process manager" peas ids
   */
  String[] getAllPeasIds() throws WorkflowException;

  /**
   * Saves a process model definition from java objects to an XML file
   * @param processFileName the xml file name that contains process model definition
   * @param process A processModel object to be saved
   * @throws WorkflowException when something goes wrong
   */
  void saveProcessModel(ProcessModel process, String processFileName)
      throws WorkflowException;

  void clearProcessModelCache() throws WorkflowException;

}