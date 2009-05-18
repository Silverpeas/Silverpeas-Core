/*
 * Created on 11 févr. 2005
 *
 */
package com.silverpeas.versioning;

import java.io.File;
import java.rmi.RemoteException;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.ejb.VersioningRuntimeException;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;

/**
 * @author neysseri
 *
 */
public class VersioningIndexer {
	
	public VersioningIndexer()
	{
	}
	
	public void createIndex(Document documentToIndex, DocumentVersion lastVersion) throws RemoteException
	{
		SilverTrace.info("versioning", "VersioningIndexer.createIndex()", "root.MSG_GEN_ENTER_METHOD", "documentToIndex = "+documentToIndex.toString());
	
		String space 		= documentToIndex.getPk().getSpace();
		String component 	= documentToIndex.getPk().getComponentName();
		String fk 			= documentToIndex.getForeignKey().getId();

		try
		{
			FullIndexEntry indexEntry = new FullIndexEntry(component, "Versioning"+documentToIndex.getPk().getId(), fk);
			indexEntry.setTitle(lastVersion.getLogicalName());
			indexEntry.setPreView(documentToIndex.getName()+" v "+lastVersion.getMajorNumber()+"."+lastVersion.getMinorNumber());
	
			//retrieve user who have upload latest version
			String userId = new Integer(lastVersion.getAuthorId()).toString();
			indexEntry.setCreationUser(userId);
			indexEntry.setCreationDate(lastVersion.getCreationDate());
	
			String path = createPath(space, component) + File.separator + lastVersion.getPhysicalName();

			String encoding = null;
			String format = lastVersion.getMimeType();
			String lang = "fr";

			indexEntry.addFileContent(path, encoding, format, lang);
			IndexEngineProxy.addIndexEntry(indexEntry);
		}
		catch (Exception e)
		{
			SilverTrace.warn("versioning", "VersioningIndexer.createIndex()", "root.EX_INDEX_FAILED");
		}
	}
	
	public void removeIndex(Document documentToIndex, DocumentVersion lastVersion) throws RemoteException
	{
		String component 	= documentToIndex.getPk().getComponentName();
		String fk 			= documentToIndex.getForeignKey().getId();
		
		IndexEntryPK indexEntry = new IndexEntryPK(component, "Versioning"+documentToIndex.getPk().getId(), fk);
		
		IndexEngineProxy.removeIndexEntry(indexEntry);
	}

	/**
	 * to create path to version
	 * @return String
	 * @exception VersioningRuntimeException
	 * @author  Michael Nikolaenko
	 * @version 1.0
	 */
	public String createPath(String spaceId, String componentId)
	{
		String[] 	ctx = { "Versioning" };
		String		path = FileRepositoryManager.getAbsolutePath(componentId, ctx);

		try
		{
			File d = new File(path);
			if (!d.exists())
			{
				FileFolderManager.createFolder(path);
			}
			return path;
		}
		catch (Exception e)
		{
			throw new VersioningRuntimeException("VersioningIndexer.createPath()",	SilverpeasException.ERROR, "root.EX_CANT_CREATE_FILE", e);
		}
	}

}
