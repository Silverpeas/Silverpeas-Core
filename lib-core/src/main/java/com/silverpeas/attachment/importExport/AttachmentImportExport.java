/*
 * Created on 25 janv. 2005
 *
 */
package com.silverpeas.attachment.importExport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.silverpeas.form.importExport.FormTemplateImportExport;
import com.silverpeas.form.importExport.XMLModelContentType;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.ZipManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentException;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Classe de gestion des attachments dans le moteur d'importExport de silverpeas.
 * @author sdevolder
 */
public class AttachmentImportExport {

	//Variables
	private static int		BUFFER_SIZE	=	1024;
	private static String	CONTEXT_ATTACHMENTS 	=	"Images";

	//Méthodes
	/**
	 * Méthode utilisée par l'import massive du moteur d'importExport de silverpeaseffectuant la copie de fichier
	 *  ainsi que sa liaison avec une publication cible.
	 * @param pubId - publication dans laquelle creer l'attachement
	 * @param componentId - id du composant contenant la publication (nécéssaire pour déterminer le chemin physique du fichier importé)
	 * @param attachmentDetail - objet contenant les détails nécéssaires à la création du fichier importé et à sa liaison avec la publication
	 * @throws AttachmentException
	 */
	public void importAttachment(String pubId, String componentId, AttachmentDetail attachmentDetail, boolean indexIt) {
		this.copyFile(componentId, attachmentDetail);
		if (attachmentDetail.getSize()>0)
			this.addAttachmentToPublication(pubId, componentId, attachmentDetail,CONTEXT_ATTACHMENTS, indexIt);
	}

	public AttachmentDetail importWysiwygAttachment(String pubId, String componentId, AttachmentDetail attachmentDetail,String context) {
		AttachmentDetail a_detail = null;
		this.copyFileWysiwyg(componentId, attachmentDetail,context);
		if (attachmentDetail.getSize()>0)
			a_detail = this.addAttachmentToPublication(pubId, componentId, attachmentDetail, context, false);
		return a_detail;
	}

	public List importAttachments(String pubId, String componentId, List attachments, String userId) {
		return importAttachments(pubId, componentId, attachments, userId, false);
	}

	public List importAttachments(String pubId, String componentId, List attachments, String userId, boolean indexIt) {
		List 						copiedAttachments 	= copyFiles(componentId, attachments);
		Iterator 					itAttachments 		= copiedAttachments.iterator();
		AttachmentDetail 			attDetail 			= null;
		FormTemplateImportExport 	xmlIE 				= null;
		while (itAttachments.hasNext()) {
			attDetail = (AttachmentDetail) itAttachments.next();
			attDetail.setAuthor(userId);
			XMLModelContentType xmlContent = attDetail.getXMLModelContentType();
			if (xmlContent != null)
				attDetail.setXmlForm(xmlContent.getName());
						
			this.addAttachmentToPublication(pubId, componentId, attDetail, CONTEXT_ATTACHMENTS, indexIt);
			
			//Store xml content
			try {
				if (xmlContent != null)
				{
					if (xmlIE == null)
						xmlIE = new FormTemplateImportExport();
					
					ForeignPK pk = new ForeignPK(attDetail.getPK().getId(), attDetail.getPK().getInstanceId());
					xmlIE.importXMLModelContentType(pk, "Attachment", xmlContent, attDetail.getAuthor());
				}
			} catch (Exception e) {
				SilverTrace.error("attachment","AttachmentImportExport.importAttachments()","root.MSG_GEN_PARAM_VALUE",e);
			}
		}
		return copiedAttachments;
	}

	private AttachmentDetail copyFile(String componentId, AttachmentDetail a_Detail) {
		String path = getPath(componentId);
		return copyFile(componentId, a_Detail, path);
	}

	private AttachmentDetail copyFileWysiwyg(String componentId, AttachmentDetail a_Detail,String context) {
		String path = getPathWysiwyg(componentId,context);
		a_Detail.setContext(context);
		return copyFile(componentId, a_Detail, path);
	}

	public List copyFiles(String componentId, List attachments) {
		return copyFiles(componentId, attachments, getPath(componentId));
	}

	public List copyFiles(String componentId, List attachments, String path) {
		List 				copiedAttachments 	= new ArrayList();
		Iterator 			itAttachments 		= attachments.iterator();
		AttachmentDetail 	attDetail 			= null;
		while (itAttachments.hasNext()) {
			attDetail = (AttachmentDetail) itAttachments.next();
			this.copyFile(componentId, attDetail, path);
			if (attDetail.getSize()!=0)
				copiedAttachments.add(attDetail);
		}
		return copiedAttachments;
	}

	/**
	 * Méthode de copie de fichier utilisée par la méthode importAttachement(String,String,AttachmentDetail)
	 * @param componentId - id du composant contenant la publication à laquelle est destiné l'attachement
	 * @param a_Detail - objet contenant les informations sur le fichier à copier
	 * @param path - chemin où doit être copié le fichier
	 * @return renvoie l'objet des informations sur le fichier à copier complété par les nouvelles données issues de la copie
	 * @throws AttachmentException
	 */
	public AttachmentDetail copyFile(String componentId, AttachmentDetail a_Detail, String path) {

		String fileToUpload 	= a_Detail.getPhysicalName();

		//Préparation des paramètres du fichier à creer
		String logicalName 		= fileToUpload.substring(fileToUpload.lastIndexOf(File.separator)+1);
		String type 			= logicalName.substring(logicalName.lastIndexOf(".") + 1, logicalName.length());
		String mimeType 		= AttachmentController.getMimeType(logicalName);
		String physicalName 	= new Long(new Date().getTime()).toString() + "." + type;

		File fileToCreate = new File(path + physicalName);
		while (fileToCreate.exists())
		{
			SilverTrace.info("attachment","AttachmentImportExport.copyFile()","root.MSG_GEN_PARAM_VALUE","fileToCreate already exists=" + fileToCreate.getAbsolutePath());
			
			//To prevent overwriting
			physicalName = new Long(new Date().getTime()).toString() + "." + type;
			fileToCreate = new File(path+physicalName);
		}
		SilverTrace.info("attachment","AttachmentImportExport.copyFile()","root.MSG_GEN_PARAM_VALUE","fileName=" + logicalName);

		long size = 0;
		try
		{
			//Copie du fichier dans silverpeas
			size = copyFileToDisk(fileToUpload, fileToCreate);
		}
		catch (Exception e)
		{
			SilverTrace.error("attachment","AttachmentImportExport.copyFile()","attachment.EX_FILE_COPY_ERROR", e);
		}

		//Compléments sur les attachmentDetail
		a_Detail.setSize(size);
		a_Detail.setType(mimeType);
		a_Detail.setPhysicalName(physicalName);
		a_Detail.setLogicalName(logicalName);

		AttachmentPK pk = new AttachmentPK("unknown", "useless", componentId);
		a_Detail.setPK(pk);

		return a_Detail;
	}

	private long copyFileToDisk(String from, File to) throws AttachmentException
	{
		FileInputStream		fl_in	=	null;
		FileOutputStream	fl_out	=	null;

		long size = 0;
		try {
			fl_in = new FileInputStream(from);
		}catch(FileNotFoundException ex) {
			throw new AttachmentException("AttachmentsType.copyFileToDisk()", SilverpeasException.ERROR, "attachment.EX_FILE_TO_UPLOAD_NOTFOUND", ex);
		}
		try {
			fl_out = new FileOutputStream(to);

			byte[] data = new byte[BUFFER_SIZE];
			int bytes_readed = fl_in.read(data);
			while (bytes_readed > 0)
			{
				size += bytes_readed;
				fl_out.write(data, 0, bytes_readed);
				bytes_readed = fl_in.read(data);
			}
			fl_in.close();
			fl_out.close();
		}catch(Exception ex) {
			throw new AttachmentException("AttachmentsType.copyFileToDisk()", SilverpeasException.ERROR, "attachment.EX_FILE_COPY_ERROR", ex);
		}
		return size;
	}

	/**
	 * Méthode utilisée par la méthode importAttachement(String,String,AttachmentDetail) pour creer un attachement sur la publication
	 * créée dans la méthode citée.
	 * @param pubId - id de la publication dans laquelle créer l'attachment
	 * @param componentId - id du composant contenant la publication
	 * @param a_Detail - obejt contenant les informations nécéssaire à la création de l'attachment
	 * @return AttachmentDetail créé
	 */
	private AttachmentDetail addAttachmentToPublication(String pubId, String componentId, AttachmentDetail a_Detail, String context, boolean indexIt) {

		AttachmentDetail	ad_toCreate			=	null;
		int					incrementSuffixe	=	0;
		AttachmentPK		atPK				=	new AttachmentPK(null, componentId);
		AttachmentPK		foreignKey			=	new AttachmentPK(pubId, componentId);
		Vector				attachments			=	AttachmentController.searchAttachmentByCustomerPK(foreignKey);
		int i = 0;
		
		String 	logicalName = a_Detail.getLogicalName();
		String	userId		= a_Detail.getAuthor();
		String	updateRule	= a_Detail.getImportUpdateRule();
		if (updateRule == null || updateRule.length() == 0 || updateRule.equalsIgnoreCase("null"))
			updateRule = AttachmentDetail.IMPORT_UPDATE_RULE_ADD;
		
		SilverTrace.info("attachment", "AttachmentImportExport.addAttachmentToPublication()", "root.MSG_GEN_PARAM_VALUE", "updateRule="+updateRule);

		//Vérification s'il existe un attachment de même nom, si oui, ajout d'un suffixe au nouveau fichier
		while (i < attachments.size()) {
			ad_toCreate	= (AttachmentDetail) attachments.get(i);
			if (ad_toCreate.getLogicalName().equals(logicalName))//si les tailles sont différentes, on
			{
				if ((ad_toCreate.getSize() != a_Detail.getSize()) && updateRule.equalsIgnoreCase(AttachmentDetail.IMPORT_UPDATE_RULE_ADD)) {
					logicalName = a_Detail.getLogicalName();
					int Extposition = logicalName.lastIndexOf(".");
					if (Extposition != -1)
						logicalName = logicalName.substring(0,Extposition) + "_" + (++incrementSuffixe) + logicalName.substring(Extposition,logicalName.length());
					else logicalName += "_" + (++incrementSuffixe);
					//On reprend la boucle au début pour vérifier que le nom généré n est pas lui meme un autre nom d'attachment de la publication
					i=0;
				}
				else {//on efface l'ancien fichier joint et on stoppe la boucle
					AttachmentController.deleteAttachment(ad_toCreate);
					break;
				}
			}
			else i++;
		}
		a_Detail.setLogicalName(logicalName);

		//On instancie l'objet attachment à creer
		ad_toCreate = new AttachmentDetail(atPK, a_Detail.getPhysicalName(), a_Detail.getLogicalName(), null, a_Detail.getType(), a_Detail.getSize(), context, new Date(), foreignKey, userId);
		ad_toCreate.setTitle(a_Detail.getTitle());
		ad_toCreate.setInfo(a_Detail.getInfo());
		ad_toCreate.setXmlForm(a_Detail.getXmlForm());
		AttachmentController.createAttachment(ad_toCreate, indexIt);

		return ad_toCreate;
	}

	/**
	 * Méthode de récupération des attachements et de copie des fichiers dans le dossier d'exportation
	 * @param pk - PrimaryKey de l'obijet dont on veut les attachments?
	 * @param exportPath - Répertoire dans lequel copier les fichiers
	 * @param relativeExportPath chemin relatif du fichier copié
	 * @return une liste des attachmentDetail trouvés
	 */
	public Vector getAttachments(WAPrimaryKey pk,String exportPath,String relativeExportPath, String extensionFilter) {

		//Récupération des attachments
		Vector listAttachment = AttachmentController.searchAttachmentByCustomerPK(pk);
		Vector listToReturn = new Vector();
		if ((listAttachment != null) && (listAttachment.size() == 0))//Si on reçoit une liste vide, on retourne null
			listAttachment = null;
		if (listAttachment != null) {
			Iterator itListAttachment = listAttachment.iterator();
			//Pour chaque attachment trouvé, on copie le fichier dans le dossier d'exportation
			while (itListAttachment.hasNext()) {
				AttachmentDetail attDetail = (AttachmentDetail) itListAttachment.next();
				if (!attDetail.getContext().equals(CONTEXT_ATTACHMENTS)) {
					//ce n est pas un fichier joint mais un fichier appartenant surement au wysiwyg si le context
					//est different de images et ce quelque soit le type du fichier
					continue;//on ne copie pas le fichier
				}
				
				if (extensionFilter == null) {
					try {
						copyAttachment(attDetail, pk, exportPath);
						
						// Le nom physique correspond maintenant au fichier copié
						attDetail.setPhysicalName(relativeExportPath + File.separator
								+ ZipManager.transformStringToAsciiString(attDetail.getLogicalName()));

					} catch (IOException ex) {
						// TODO: gerer ou ne pas gerer telle est la question
						ex.printStackTrace();
					}

					listToReturn.add(attDetail);
				
				} else if (attDetail.getExtension().equalsIgnoreCase(extensionFilter)) {
					try {
						copyAttachment(attDetail, pk, exportPath);
						// Le nom physique correspond maintenant au fichier copié
						attDetail.setLogicalName(ZipManager.transformStringToAsciiString(attDetail.getLogicalName()));

					} catch (Exception ex) {
						// TODO: gerer ou ne pas gerer telle est la question
						ex.printStackTrace();
					}
					
					listToReturn.add(attDetail);
				}
			}
		}
		
		return listToReturn;
	}

	private void copyAttachment(AttachmentDetail attDetail, WAPrimaryKey pk, String exportPath) throws FileNotFoundException, IOException {
		String fichierJoint = AttachmentController.createPath(pk.getInstanceId(), attDetail.getContext()) + File.separator
		+ attDetail.getPhysicalName();
		
		String fichierJointExport = exportPath + File.separator
				+ ZipManager.transformStringToAsciiString(attDetail.getLogicalName());
		
		FileRepositoryManager.copyFile(fichierJoint, fichierJointExport);
	}
	

	/**
	 * Méthode récupérant le chemin d'accès au dossier de stockage des fichiers importés dans un composant.
	 * @param componentId - id du composant dont on veut récuperer le chemin de stockage de ses fichiers importés
	 * @return le chemin recherché
	 */
	private String getPath(String componentId)
	{
		String path = AttachmentController.createPath(componentId, CONTEXT_ATTACHMENTS);
		SilverTrace.info("attachment", "AttachmentImportExport.getPath()", "root.MSG_GEN_PARAM_VALUE", "path=" + path);
		return path;
	}

	private String getPathWysiwyg(String componentId,String context)
	{
		String path = AttachmentController.createPath(componentId, context);
		SilverTrace.info("attachment", "AttachmentImportExport.getPath()", "root.MSG_GEN_PARAM_VALUE", "path=" + path);
		return path;
	}
}