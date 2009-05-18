package com.silverpeas.versioning;

import java.rmi.RemoteException;
import java.sql.Connection;

import javax.ejb.CreateException;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.ejb.VersioningBm;
import com.stratelia.silverpeas.versioning.ejb.VersioningBmHome;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

public class VersioningInstanciator extends SQLRequest implements ComponentsInstanciatorIntf {

	private VersioningBm versioningBm = null;

  /** Creates new KmeliaInstanciator */
  public VersioningInstanciator() {
	super("com.silverpeas.versioning");
  }
  
  public void create(Connection con, String spaceId, String componentId, String userId) 
    throws InstanciationException 
  {  
  }

  public void delete(Connection con, String spaceId, String componentId, String userId) throws InstanciationException 
  {
	SilverTrace.info("versioning", "VersioningInstanciator.delete()", "root.MSG_GEN_ENTER_METHOD", "componentId = "+componentId);
   
    //1 - delete data in database
   	try {
		getVersioningBm().deleteDocumentsByInstanceId(componentId);
   	} catch (Exception e) {
		throw new InstanciationException("VersioningInstanciator.delete()",InstanciationException.ERROR, "root.EX_RECORD_DELETE_FAILED", e);
	}
	
	//2 - delete directory where files are stored
	String[] ctx = {"Versioning"};
	String path = FileRepositoryManager.getAbsolutePath(componentId, ctx);
	try {
		FileFolderManager.deleteFolder(path);
	} catch (Exception e)
	{
		throw new InstanciationException("VersioningInstanciator.delete()",InstanciationException.ERROR, "root.DELETING_DATA_DIRECTORY_FAILED", e);
	}
  }
  
  private VersioningBm getVersioningBm() throws UtilException, RemoteException, CreateException
  {
	if (versioningBm == null)
	{
		VersioningBmHome versioningBmHome = (VersioningBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.VERSIONING_EJBHOME, VersioningBmHome.class);
		versioningBm = versioningBmHome.create();
		
	}
	return versioningBm;	
  }
  
  

}