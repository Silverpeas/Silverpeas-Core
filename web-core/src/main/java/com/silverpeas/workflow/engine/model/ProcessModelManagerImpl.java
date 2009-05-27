package com.silverpeas.workflow.engine.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
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
import java.util.Vector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.mapping.MappingException;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.xml.sax.InputSource;

import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.form.record.GenericRecordSetManager;
import com.silverpeas.workflow.api.ProcessModelManager;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.model.DataFolder;
import com.silverpeas.workflow.api.model.Form;
import com.silverpeas.workflow.api.model.Forms;
import com.silverpeas.workflow.api.model.ProcessModel;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.SilverpeasSettings;
import com.stratelia.webactiv.beans.admin.Admin;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

/**
 * A ProcessModelManager implementation
 */
public class ProcessModelManagerImpl implements ProcessModelManager
{
    /**
     * ResourceLocator object to retrieve messages in a properties file
     */
    private static ResourceLocator settings =
        new ResourceLocator("com.silverpeas.workflow.engine.castorSettings", "fr");

    /**
     * The ProcessModelManagerImpl shares a silverpeas Admin object
     */
    static private Admin admin = null;

    /**
     * The map (modelId -> cached process model).
     */
    private Map models = new HashMap();
    private String dbName = JNDINames.WORKFLOW_DATASOURCE;

    /**
    * Default constructor
    */
    public ProcessModelManagerImpl()
    {
        synchronized (ProcessModelManagerImpl.class)
        {
            if (admin == null)
            {
                admin = new Admin();
            }
        }
    }

    /**
     * @see com.silverpeas.workflow.api.ProcessModelManager#listProcessModels()
     */
    public List listProcessModels() throws WorkflowException
    {
        try
        {
            // Recursively search all subdirs for .xml files
            //
            return findProcessModels( getProcessModelDir() );
        }
        catch (UtilException e)
        {
            throw new WorkflowException("WorkflowManager.getProcessModels",
                                        "WorkflowEngine.EX_GETTING_RPOCES_MODELS_FAILED",
                                        "Workflow Dir : " + getProcessModelDir(),
                                        e);
        }
        catch (IOException e ) {
            throw new WorkflowException("WorkflowManager.getProcessModels",
                                        "WorkflowEngine.EX_GETTING_RPOCES_MODELS_FAILED",
                                        "Workflow Dir : " + getProcessModelDir(),
                                        e);
        }
    }
    
    /**
     * Recursive method to retrieve all process models in and below the given directory 
     *     
     * @param processModelDir the directory to start with
     * @return a list of strings containing the relative path and file name of the model
     * @throws UtilException 
     * @throws IOException 
     */
    private List findProcessModels( String strProcessModelDir ) throws UtilException, IOException
    {
        Iterator   subFoldersIterator = FileFolderManager.getAllSubFolder( strProcessModelDir ).iterator(),
                    subFolderModelsIterator,
                    currentDirModelsIterator = FileFolderManager.getAllFile( strProcessModelDir ).iterator();
        List       models = new ArrayList();
        File       subFolder;
        
        while ( subFoldersIterator.hasNext() )
        {
            subFolder = (File) subFoldersIterator.next();
            
            // Get models from subfolders, 
            //
            subFolderModelsIterator = findProcessModels( subFolder.getCanonicalPath() ).iterator();
            
            // prepend their names with the name of this folder
            //
            while ( subFolderModelsIterator.hasNext() )
                models.add( subFolder.getName() + File.separator + subFolderModelsIterator.next() );
        }
            
          // Get models from the current folder, do not prepend names
          //
          while ( currentDirModelsIterator.hasNext() ) {
        	  String name = ((File)currentDirModelsIterator.next()).getName();

        	  if ( name.endsWith(".xml") )
        		  models.add( name );
          }

        // Return the list of process models
        //
        return models;
    }

    /**
     * Get a ProcessModel from its modelId.
     * Retrieves the xml descriptor filename from the model Id
     * and load abstract process model information in ProcessModel object
     *
     * @param    modelId        model id
     * @return    ProcessModel object
     */
    public ProcessModel getProcessModel(String modelId) throws WorkflowException
    {
        // Search the processModel in the process model cache.
        ProcessModel cachedModel = getCachedProcessModel(modelId);
        if (cachedModel != null)
            return cachedModel;

        // The model is not cached, we must build it.
        //ArrayList parameters = null;
        //String[] parameterNames = null;
        //String[] parameterValues = null;
        String fileName = null;

        // Get the component parameters
        // the modelId is the peasId.
        //parameters = admin.getComponentParameters(modelId);
        //parameterNames = (String[]) parameters.get(0);
        //parameterValues = (String[]) parameters.get(2);

        // Search for parameter "XMLFileName"
        //for (int i = 0; fileName == null && parameterNames != null && i < parameterNames.length; i++)
        /*{
            if (parameterNames[i].equals("XMLFileName"))
            {
                fileName = parameterValues[i];
            }
        }*/

        fileName = admin.getComponentParameterValue(modelId, ComponentsInstanciatorIntf.PROCESS_XML_FILE_NAME);

        // if file name not found, throw exception
        if (fileName == null)
            throw new WorkflowException(
                "ProcessModelManagerImpl.getProcessModel",
                "workflowEngine.EX_NO_XML_FILENAME_FOUND",
                "model/peas id : " + modelId);

        // load the process model from its xml descriptor
        ProcessModelImpl model = (ProcessModelImpl) this.loadProcessModel(fileName, false);

        // set the peas id
        model.setModelId(modelId);

        // cache the model.
        cacheProcessModel(modelId, model, fileName);

        // return the process model
        return (ProcessModel) model;
    }

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.api.ProcessModelManager#createProcessModelDescriptor()
     */
    public ProcessModel createProcessModelDescriptor() throws WorkflowException
    {
        return new ProcessModelImpl();
    }

    /**
     * Create a ProcessModel from xml descriptor filename.
     * Generate an id for this model
     * and load abstract process model information in ProcessModel object
     *
     * @param    processFileName        xml descriptor filename.
     * @param    peasId            Id of processManager instance (peas).
     * @return    ProcessModel object
     */
    public ProcessModel createProcessModel(String processFileName, String peasId) throws WorkflowException
    {
        ProcessModel model = null;
        DataFolder folder = null;
        RecordTemplate template = null;
        Forms forms = null;

        try
        {
            // Load abstract process model
            model = this.loadProcessModel(processFileName, false);
            model.setModelId(peasId);

            // Creates datafolder in database
            folder = model.getDataFolder();
            template = folder.toRecordTemplate(null, null, true);
            GenericRecordSetManager.createRecordSet(model.getFolderRecordSetName(), template);

            // Creates forms in database 
            forms = model.getForms();
            if ( forms != null )
            {
                Iterator iterForm = forms.iterateForm();
                Form     form;
                
                while ( iterForm.hasNext() )
                {
                    form = (Form)iterForm.next();
                    template = form.toRecordTemplate(null, null);
                    GenericRecordSetManager.createRecordSet(model.getFormRecordSetName(form.getName()), template);
                }
            }
        }
        catch (FormException fe)
        {
            throw new WorkflowException(
                "ProcessModelManagerImpl.createProcessModel",
                "workflowEngine.EX_ERR_INSTANCIATING_MODEL",
                "Process FileName : " 
                  + processFileName == null ? "<null>" : processFileName,
                fe);
        }
        catch (WorkflowException we)
        {
            throw new WorkflowException(
                "ProcessModelManagerImpl.createProcessModel",
                "workflowEngine.EX_ERR_INSTANCIATING_MODEL",
                "Process FileName : " 
                + processFileName == null ? "<null>" : processFileName,
                we);
        }

        return model;
    }

    /**
     * Delete a ProcessModel with given model id
     *
     * @param    modelId        model id
     */
    public void deleteProcessModel(String instanceId) throws WorkflowException
    {
        ProcessModel model         = getProcessModel(instanceId);
        String         formName     = null;
        try
        {
            Forms forms = null;
            
            //delete the folder
            formName = model.getFolderRecordSetName();
            GenericRecordSetManager.removeRecordSet(formName);
            
            //delete forms associated to actions
            forms = model.getForms();
            if ( forms != null )
            {
                Iterator iterForm = forms.iterateForm();
                
                while ( iterForm.hasNext() )
                {
                    formName = ( (Form)iterForm.next() ).getName();
                    GenericRecordSetManager.removeRecordSet(model.getFormRecordSetName(formName));
                    SilverTrace.info("workflowEngine", "ProcessModelManagerImpl.deleteProcessModel", "root.MSG_GEN_PARAM_VALUE", instanceId +" : Removing form '"+formName+"' successfully done");
                }
            }
        }
        catch (FormException fe)
        {
            throw new WorkflowException("ProcessModelManagerImpl.deleteProcessModel",
                                        "workflowEngine.EX_ERR_UNINSTANCIATING_MODEL",
                                        "instanceId : "
                                          + instanceId == null ? "<null>" : instanceId 
                                          + ", formName = " + formName == null ? "<null>" : formName,
                                        fe);
        }
    }

    /* (non-Javadoc)
     * @see com.silverpeas.workflow.api.ProcessModelManager#deleteProcessModelDescriptor(java.lang.String)
     */
    public void deleteProcessModelDescriptor(String strProcessModelFileName ) throws WorkflowException
    {
        try
        {
            FileFolderManager.deleteFile( getProcessModelDir() + strProcessModelFileName );
            
            // Clear process model cache
            //
            clearProcessModelCache();
        } 
        catch (UtilException e)
        {
            throw new WorkflowException("WorkflowManager.getProcessModels",
                    "WorkflowEngine.EX_GETTING_RPOCES_MODELS_FAILED",
                    "Process Model File name : " + (strProcessModelFileName == null 
                                                      ? "<null>" 
                                                      : strProcessModelFileName ),
                    e);
        }
    }

    /**
     * load a process model definition from xml file to java objects
     * @param    processFileName        the xml file name that contains process model definition
     * @param    absolutePath        true if xml file name contains the full path, else concat with the 
     *                                directory defined in castorSettings.properties
     * @return  a ProcessModel object
     */
    public ProcessModel loadProcessModel(String processFileName, boolean absolutePath) throws WorkflowException
    {
        Mapping mapping         = new Mapping();
        String     mappingFileName = settings.getString("CastorXMLMappingFileURL");
        String     processModelDir = settings.getString("ProcessModelDir");
        String     debugMode        = "false";
        try
        {
            // get configuration files url
            mappingFileName = settings.getString("CastorXMLMappingFileURL");
            processModelDir = settings.getString("ProcessModelDir");
            debugMode        = settings.getString("DebugMode", "false");
            
            ResourceLocator settings = new ResourceLocator("com.stratelia.webactiv.util.attachment.Attachment", "");
            boolean runOnUnix = SilverpeasSettings.readBoolean(settings, "runOnSolaris", false);
            
            //Format these url
            if (runOnUnix)
                mappingFileName = mappingFileName.replace('\\', '/');
            else            
                mappingFileName = "file:///" + mappingFileName.replace('\\', '/');

            if (!absolutePath)
            {
                processModelDir = processModelDir.replace('\\', '/');
                if (processModelDir.length() > 0 && processModelDir.charAt(processModelDir.length() - 1) != '/')
                {
                    processModelDir += "/";
                }
                processFileName = processModelDir + processFileName;
            }

            // Load mapping and instantiate a Marshaller
            mapping.loadMapping(mappingFileName);
            Unmarshaller unmar = new Unmarshaller(mapping);
            unmar.setValidation(false);
            if ("true".equals(debugMode))
                unmar.setDebug(true);

            // Unmarshall the process model
            ProcessModelImpl process =
                (ProcessModelImpl) unmar.unmarshal(new InputSource(new FileReader(processFileName)));

            if ("true".equals(debugMode))
            {
                // Marshall for debugging purpose
                processFileName = processModelDir.replace('\\', '/') + "debug.xml";
                Marshaller mar = new Marshaller(new FileWriter(processFileName));
                mar.setMapping(mapping);
                mar.marshal(process);
            }
 
            return process;
        }
        catch (MappingException me)
        {
            throw new WorkflowException(
                "ProcessModelManagerImpl.loadProcessModel",
                "workflowEngine.EX_ERR_CASTOR_LOAD_XML_MAPPING",
                "Mapping file name : " + ( mappingFileName == null 
                                           ? "<null>" 
                                           : mappingFileName ),
                me);
        }
        catch (MarshalException me)
        {
            throw new WorkflowException(
                "ProcessModelManagerImpl.loadProcessModel",
                "workflowEngine.EX_ERR_CASTOR_UNMARSHALL_PROCESSMODEL",
                "Process File Name : "  + ( processFileName == null 
                                            ? "<null>" 
                                            : processFileName ),
                me);
        }
        catch (ValidationException ve)
        {
            throw new WorkflowException(
                "ProcessModelManagerImpl.loadProcessModel",
                "workflowEngine.EX_ERR_CASTOR_INVALID_XML_PROCESSMODEL",
                "Process File Name : " + ( processFileName == null 
                                          ? "<null>" 
                                          : processFileName ),
                ve);
        }
        catch (IOException ioe)
        {
            throw new WorkflowException(
                "ProcessModelManagerImpl.loadProcessModel",
                "workflowEngine.EX_ERR_CASTOR_LOAD_PROCESSMODEL",
                "Process File Name : " + ( processFileName == null 
                                          ? "<null>" 
                                          : processFileName ),
                ioe);
        }
    }

    /**
     * Saves a process model definition from java objects to an XML file 
     * @param    processFileName        the xml file name that contains process model definition
     * @param   process A processModel object to be saved
     * @throws  WorkflowException when something goes wrong
     */
    public void saveProcessModel(ProcessModel process, String processFileName) throws WorkflowException
    {
        Mapping mapping         = new Mapping();
        String     mappingFileName = settings.getString("CastorXMLMappingFileURL"); // get configuration files url
        String     schemaFileName  = settings.getString("ProcessModesSchemaFileURL");
        String     processModelDir = settings.getString("ProcessModelDir");
        String  strProcessModelFileEncoding = settings.getString("ProcessModelFileEncoding");
        Marshaller mar;
        ResourceLocator resourceSettings = new ResourceLocator("com.stratelia.webactiv.util.attachment.Attachment", "");
        boolean runOnUnix = SilverpeasSettings.readBoolean(resourceSettings, "runOnSolaris", false);

        try
        {
            // TODO remove the 'default' as a name of role etc, set to null
            //
            // TODO before marshalling, remove empty collections from the process
            //
            
            //Format these urls
            //
            if ( runOnUnix )
                mappingFileName = mappingFileName.replace('\\', '/');
            else            
                mappingFileName = "file:///" + mappingFileName.replace('\\', '/');

            processModelDir = processModelDir.replace('\\', '/');

            if ( processModelDir.length() > 0 && processModelDir.charAt(processModelDir.length() - 1) != '/' )
            {
                processModelDir += "/";
            }
            
            processFileName = processModelDir + processFileName;

            mapping.loadMapping(mappingFileName);

            mar = new Marshaller( new OutputStreamWriter( new FileOutputStream( processFileName),
                                                          strProcessModelFileEncoding ) ); 
            mar.setMapping(mapping);
            mar.setNoNamespaceSchemaLocation( schemaFileName );
            mar.setSuppressXSIType(true); // TODO try to use setMarshalExtendedType( false );
            mar.setValidation( false );
            mar.setEncoding( strProcessModelFileEncoding );
            mar.marshal(process);
            
            // once the model saved, clear the cache of the models 
            // ( should be reloaded automatically later)
            //
            clearProcessModelCache();
        }
        catch (MappingException me)
        {
            throw new WorkflowException(
                "ProcessModelManagerImpl.saveProcessModel",
                "workflowEngine.EX_ERR_CASTOR_LOAD_XML_MAPPING",
                "Mapping file name : " + ( mappingFileName == null 
                                           ? "<null>" 
                                           : mappingFileName ),
                me);
        }
        catch (MarshalException me)
        {
            throw new WorkflowException(
                "ProcessModelManagerImpl.saveProcessModel",
                "workflowEngine.EX_ERR_CASTOR_MARSHALL_PROCESSMODEL", 
                "Process file name : " + ( processFileName == null 
                                           ? "<null>" 
                                           : processFileName ),
                me);
        }
        catch (ValidationException ve)
        {
            throw new WorkflowException(
                "ProcessModelManagerImpl.saveProcessModel",
                "workflowEngine.EX_ERR_CASTOR_INVALID_XML_PROCESSMODEL",
                "Process file name : " + ( processFileName == null 
                                           ? "<null>" 
                                           : processFileName ),
                ve);
        }
        catch (IOException ioe)
        {
            throw new WorkflowException(
                "ProcessModelManagerImpl.saveProcessModel",
                "workflowEngine.EX_ERR_CASTOR_SAVE_PROCESSMODEL",
                "Process file name : " + ( processFileName == null 
                                           ? "<null>" 
                                           : processFileName ),
                ioe);
        }
    }

    /**
     * Get the directory where are stored the models
     */
    public String getProcessModelDir()
    {
        String dir = settings.getString("ProcessModelDir");

        if (dir != null && dir.lastIndexOf("\\") != dir.length())
        {
            dir += "\\";
        }
        return dir;
    }

    /**
     * Get all the "process manager" peas ids
     */
    public String[] getAllPeasIds() throws WorkflowException
    {
        Connection con = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String selectQuery = null;
        Vector peasIds = new Vector();

        try
        {
            con = this.getConnection();

            selectQuery = "select distinct modelId from SB_Workflow_ProcessInstance";
            prepStmt = con.prepareStatement(selectQuery);
            rs = prepStmt.executeQuery();

            while (rs.next())
            {
                peasIds.add(rs.getString(1));
            }

            return (String[]) peasIds.toArray(new String[0]);
        }
        catch (SQLException se)
        {
            throw new WorkflowException(
                "ProcessModelManagerImpl.getAllPeasId",
                "workflowEngine.EX_ERR_GET_ALL_PEAS_IDS",
                "sql query : " + selectQuery == null ? "<null>" : selectQuery,
                se);
        }
        finally
        {
            try
            {
                DBUtil.close(rs, prepStmt);
                if (con != null) con.close();
            }
            catch (SQLException se)
            {
                SilverTrace.error(
                    "workflowEngine",
                    "ProcessModelManagerImpl.getAllPeasId",
                    "root.EX_RESOURCE_CLOSE_FAILED",
                    se);
            }
        }
    }

    /**
     * Search the cache for the required process model.
     */
    private ProcessModel getCachedProcessModel(String modelId) throws WorkflowException
    {
        ProcessModel model = (ProcessModel) models.get(modelId);
        return model;
    }

    /**
     * Put the given process model in the the cache.
     */
    private void cacheProcessModel(String modelId, ProcessModel model, String filename) throws WorkflowException
    {
        synchronized( models )
        {
            models.put(modelId, model);
        }
    }

    /**
     * Clear the process model cache.
     */
    public void clearProcessModelCache()
    {
        synchronized( models )
        {
            models.clear();
        }
    }
    
    /**
     * @return the DB connection
     */
    private Connection getConnection() throws WorkflowException
    {
        Connection con = null;
        try
        {
            //con = DBUtil.makeConnection(dbName);
            Context        ctx = new InitialContext();
            DataSource  src = (DataSource) ctx.lookup(dbName);
            con = src.getConnection();
            return con;
        }
        catch (NamingException e)
        {
            //throw new UtilException("Schema.Schema", SilverpeasException.ERROR, "root.EX_DATASOURCE_NOT_FOUND", e);
            //the JNDI name have not been found in the current context
            //The caller is not takes place in any context (web application nor ejb container)
            //So lookup operation cannot find JNDI properties !
            //This is absolutly normal according to the j2ee specification
            //Unfortunately, only BES takes care about this spec. This exception doesn't appear with orion or BEA !
            try {
                // Get the initial Context
                Context ctx = new InitialContext() ;
                // Look up the datasource directly without JNDI access
                DataSource dataSource = (DataSource) ctx.lookup (JNDINames.DIRECT_DATASOURCE);
                //Create a connection object
                con = dataSource.getConnection();
                return con;
            } catch(NamingException ne) {
                throw new WorkflowException("ProcessModelManagerImpl.getConnection",
                                            "root.EX_DATASOURCE_NOT_FOUND",
                                            "Data source "+JNDINames.DIRECT_DATASOURCE+" not found",
                                            ne);
            } catch(SQLException se) {
                throw new WorkflowException("ProcessModelManagerImpl.getConnection", "can't get connection for dataSource "+JNDINames.DIRECT_DATASOURCE, se);
            }
        }
        catch (SQLException se)
        {
            throw new WorkflowException("ProcessModelManagerImpl.getConnection()",
                                        "root.EX_CONNECTION_OPEN_FAILED",
                                        se);
        }
    }
}