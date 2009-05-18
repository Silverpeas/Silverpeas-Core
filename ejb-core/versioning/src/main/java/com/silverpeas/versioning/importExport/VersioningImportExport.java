/*
 * Created on 11 févr. 2005
 *
 */
package com.silverpeas.versioning.importExport;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.ZipManager;
import com.silverpeas.versioning.VersioningIndexer;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.ejb.VersioningBm;
import com.stratelia.silverpeas.versioning.ejb.VersioningBmHome;
import com.stratelia.silverpeas.versioning.ejb.VersioningRuntimeException;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * @author neysseri
 *
 */
public class VersioningImportExport {
	
	private VersioningBm 		versioningBm 	= null;
	private VersioningIndexer	indexer			= new VersioningIndexer();

	public int importDocuments(String objectId, String componentId, List attachments, int userId, boolean indexIt, String topicId) throws RemoteException
	{
		return importDocuments(objectId, componentId, attachments, userId, DocumentVersion.TYPE_PUBLIC_VERSION, indexIt, topicId);
	}

	public int importDocuments(String objectId, String componentId, List attachments, int userId, boolean indexIt) throws RemoteException
	{
		return importDocuments(objectId, componentId, attachments, userId, DocumentVersion.TYPE_PUBLIC_VERSION, indexIt, null);
	}

	public int importDocuments(String objectId, String componentId, List attachments, int userId, int versionType, boolean indexIt) throws RemoteException
	{
		return importDocuments(objectId, componentId, attachments, userId, versionType, indexIt, null);
	}
	
	/**
	 * 
	 * @param objectId
	 * @param componentId
	 * @param attachments
	 * @param userId
	 * @return
	 * @throws RemoteException
	 */
	public int importDocuments(String objectId, String componentId, List attachments, int userId, int versionType, boolean indexIt, String topicId) throws RemoteException
	{
		SilverTrace.info("versioning", "VersioningImportExport.importDocuments()", "root.GEN_PARAM_VALUE",componentId);
		int nbFilesProcessed = 0;
		
		ForeignPK pubPK = new ForeignPK(objectId, componentId);
		
		//get existing documents of object
		List documents = getVersioningBm().getDocuments(pubPK);
		
		AttachmentDetail 	attachment 	= null;
		DocumentVersion		version		= null;
		Document			document	= null;
		for (int a=0; a < attachments.size(); a++)
		{
			attachment 	= (AttachmentDetail) attachments.get(a);
			version 	= new DocumentVersion(attachment);
			version.setAuthorId(userId);
			version.setInstanceId(componentId);
			document 	= isDocumentExist(documents, attachment); 
			if (document != null)
			{
				//Un document portant le même nom existe déjà
				//On ajoute une nouvelle version au document
				List versions = getVersioningBm().getDocumentVersions(document.getPk());
				if (versions != null && versions.size() > 0)
				{
					DocumentVersion lastVersion =  (DocumentVersion) versions.get(versions.size()-1);
					version.setMimeType(attachment.getType());
					if (attachment.getType() == null)
						version.setMimeType("dummy");
					version.setMajorNumber(lastVersion.getMajorNumber());
					version.setMinorNumber(lastVersion.getMinorNumber());
					version.setType(versionType);
					version.setStatus(DocumentVersion.STATUS_VALIDATION_NOT_REQ);
					version.setCreationDate(new Date());
					
					if (!isUndefined(attachment.getInfo()))
						version.setComments(attachment.getInfo());
					
					version = getVersioningBm().addDocumentVersion(document, version);
				}
			}
			else
			{
				//Il n'y a pas de document portant le même nom
				//On crée un nouveau document
				DocumentPK docPK = new DocumentPK(-1, "useless", componentId);
				document = new Document(docPK, pubPK, attachment.getLogicalName(), "", -1, userId, new Date(), null, componentId, null, null, 0, 0);
				if (!isUndefined(attachment.getTitle()))
					document.setName(attachment.getTitle());
				if (!isUndefined(attachment.getInfo()))
					document.setDescription(attachment.getInfo());
					
				//document.setDescription(attachment.getDescription());
				//et on y ajoute la première version
				if (versionType == DocumentVersion.TYPE_PUBLIC_VERSION)
				{
					version.setMajorNumber(1);
					version.setMinorNumber(0);
				}
				else
				{
					version.setMajorNumber(0);
					version.setMinorNumber(1);
				}
				version.setType(versionType);
				version.setStatus(DocumentVersion.STATUS_VALIDATION_NOT_REQ);
				version.setCreationDate(new Date());
				version.setMimeType(attachment.getType());
				if (attachment.getType() == null)
					version.setMimeType("dummy");
				docPK = getVersioningBm().createDocument(document, version);
				document.setPk(docPK);

				VersioningUtil versioningUtil = new VersioningUtil(componentId, document, new Integer(userId).toString(), topicId);
				versioningUtil.setFileRights(document);
				getVersioningBm().updateWorkList(document);
				getVersioningBm().updateDocument(document);
			}
			
			nbFilesProcessed++;
			indexer.createIndex(document, version);
		}
		return nbFilesProcessed;
	}
	
	/**
	 * 
	 * @param pk
	 * @param exportPath
	 * @param relativeExportPath
	 * @param extensionFilter : permet de filtrer un type de document en particulier, par son extension.
	 * @return
	 * @throws RemoteException
	 */
	public Vector exportDocuments(WAPrimaryKey pk, String exportPath, String relativeExportPath, String extensionFilter) throws RemoteException
	{
		Vector 			attachments	= new Vector();
		String 			componentId = pk.getInstanceId();
		ForeignPK 	pubPK 		= new ForeignPK(pk.getId(), componentId);
		
		//get existing documents of object
		List documents = getVersioningBm().getDocuments(pubPK);
			
		//retrieve last public versions of each document
		Document 			document 	= null;
		DocumentVersion 	version		= null;
		for (int d=0; d<documents.size(); d++)
		{
			document 	= (Document) documents.get(d);
			version		= getVersioningBm().getLastPublicDocumentVersion(document.getPk());
			if (version != null) {
				AttachmentDetail attachment = getAttachmentDetail(document, version);

				if (extensionFilter == null) {
					attachments.add(copyAttachment(attachment, exportPath, relativeExportPath, componentId));
				
				} else if (attachment.getExtension().equalsIgnoreCase(extensionFilter)) {
					attachments.add(copyAttachment(attachment, exportPath, relativeExportPath, componentId));
				}
			}
		}

		if (attachments.size()==0)
			return null;
		else
			return attachments;
	}

	private AttachmentDetail copyAttachment(AttachmentDetail attachment, String exportPath, String relativeExportPath, String componentId) {
		AttachmentDetail attachmentCopy = attachment;
		String fichierJoint 		= getVersioningPath(componentId) + File.separator + attachmentCopy.getPhysicalName();
		String fichierJointExport 	= exportPath + File.separator + ZipManager.transformStringToAsciiString(attachmentCopy.getLogicalName());
		try
		{
			FileRepositoryManager.copyFile(fichierJoint, fichierJointExport);
		}
		catch (IOException e)
		{
			SilverTrace.warn("versioning", "VersioningImportExport.exportDocuments", "root.EX_FILE_NOT_FOUND", e);
		}			
		//Le nom physique correspond maintenant au fichier copié
		attachmentCopy.setPhysicalName(relativeExportPath + File.separator + ZipManager.transformStringToAsciiString(attachmentCopy.getLogicalName()));

		return attachmentCopy;
	}
	
	public String getVersioningPath(String componentId)
	{
		return indexer.createPath("useless", componentId);
	}
	
	private VersioningBm getVersioningBm() {
		if (versioningBm == null) {
			try 
			{
				VersioningBmHome versioningBmHome =
					(VersioningBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.VERSIONING_EJBHOME, VersioningBmHome.class);
				versioningBm = versioningBmHome.create();
			}
			catch (Exception e) {
				throw new VersioningRuntimeException("VersioningImportExport.getVersioningBm()",SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",e);
			}
		}
		return versioningBm;
	}
	
	private Document isDocumentExist(List documents, AttachmentDetail attachment)
	{
		String documentName = attachment.getTitle();
		if (documentName == null || documentName.length() == 0)
			documentName = attachment.getLogicalName();
		
		Document document = null;
		for (int d=0; d<documents.size(); d++)
		{
			document = (Document) documents.get(d);
			if (document.getName().equalsIgnoreCase(documentName))
				return document;
		}
		return null;
	}
	
	private AttachmentDetail getAttachmentDetail(Document document, DocumentVersion version)
	{
		AttachmentPK pk = new AttachmentPK("useless", "useless", version.getPk().getInstanceId());
		AttachmentDetail attachment = new AttachmentDetail(pk, version.getPhysicalName(), version.getLogicalName(), version.getComments(), version.getMimeType(), version.getSize(), "Versioning", version.getCreationDate(), document.getForeignKey());
		attachment.setTitle(document.getName());
		
		String info = document.getDescription();
		String versionComments = version.getComments();
		if (!isUndefined(info))
		{
			if (!isUndefined(versionComments))
				info += " "+versionComments;
		}
		else
		{
			if (!isUndefined(versionComments))
				info = versionComments;
		}
		attachment.setInfo(info);
		
		return attachment;
	}
	
	private boolean isUndefined(String param)
	{
		return (param == null || param.length() == 0 || "".equals(param));
	}

}