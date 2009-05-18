package com.silverpeas.form;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspWriter;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.form.fieldType.FileField;
import com.silverpeas.form.fieldType.UserField;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.ejb.VersioningBm;
import com.stratelia.silverpeas.versioning.ejb.VersioningBmHome;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.model.Worker;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.ProfileInst;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

public abstract class AbstractForm implements Form {
	
	private List 			fieldTemplates 	= new ArrayList();
	private String 			title 			= "";
	private VersioningBm 	versioningBm 	= null;

	public AbstractForm(RecordTemplate template) throws FormException
	{
		if (template != null)
		{
			FieldTemplate fields[] = template.getFieldTemplates();
			int size = fields.length;
			FieldTemplate fieldTemplate;
			for (int i=0 ; i<size ; i++)
			{
				fieldTemplate = fields[i];
				this.fieldTemplates.add(fieldTemplate);
			}
		}
	}
	
	public List getFieldTemplates()
	{
		return fieldTemplates;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	/**
	 * Set the form title
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}
	
	/**
	   * Prints the javascripts which will be used to control
	   * the new values given to the data record fields.
	   *
	   * The error messages may be adapted to a local language.
	   * The RecordTemplate gives the field type and constraints.
	   * The RecordTemplate gives the local label too.
	   *
	   * Never throws an Exception
	   * but log a silvertrace and writes an empty string when :
	   * <UL>
	   * <LI> a field is unknown by the template.
	   * <LI> a field has not the required type.
	   * </UL>
	   */
	public void displayScripts(JspWriter jw, PagesContext PagesContext)
	{
		try
		{
			String language = PagesContext.getLanguage();
			StringWriter sw = new StringWriter();
			PrintWriter out = new PrintWriter(sw, true);
			Iterator itFields = null;
			
			if (fieldTemplates != null)
				itFields = this.fieldTemplates.iterator();
			
			FieldTemplate fieldTemplate = null;
			if (itFields != null && itFields.hasNext())
			{
				//while (itFields.hasNext())
				//{
					fieldTemplate = (FieldTemplate) itFields.next();
					
					//out.println("<script type=\"text/javascript\" src=\"/weblib/xmlforms/"+fieldTemplate.getTemplateName()+"/"+fieldTemplate.getFieldName()+".js\"></script>");
					out.println("<script type=\"text/javascript\" src=\"/weblib/xmlforms/"+fieldTemplate.getTemplateName()+".js\"></script>");
				//}
			}
			
			out.println(Util.getJavascriptIncludes());
			out.println("<script type=\"text/javascript\">");
			out.println("	var errorNb = 0;");
			out.println("	var errorMsg = \"\";");
			out.println("function addXMLError(message) {");
			out.println("	errorMsg+=\"  - \"+message+\"\\n\";");
			out.println("	errorNb++;");
			out.println("}");
			out.println("function getXMLField(fieldName) {");
			out.println("	return document.getElementById(fieldName);");
			out.println("}");
			out.println("function isCorrectForm() {");
			out.println("	errorMsg = \"\";");
			out.println("	errorNb = 0;");
			out.println("	var field;");
			out.println("	\n");
			if (fieldTemplates != null)
				itFields = this.fieldTemplates.iterator();
			if ((itFields != null) && (itFields.hasNext()))
			{
				PagesContext pc = new PagesContext(PagesContext);
				pc.incCurrentFieldIndex(1);
				while (itFields.hasNext())
				{
					fieldTemplate = (FieldTemplate) itFields.next();
					if (fieldTemplate != null)
					{
						String fieldDisplayerName = fieldTemplate.getDisplayerName();
						String fieldType = fieldTemplate.getTypeName();
						FieldDisplayer fieldDisplayer = null;
						try
						{
							if ((fieldDisplayerName == null)||(fieldDisplayerName.equals(""))) 
								fieldDisplayerName = TypeManager.getDisplayerName(fieldType);
													
							fieldDisplayer = TypeManager.getDisplayer(fieldType, fieldDisplayerName);
							
							if (fieldDisplayer != null)
							{
								//out.println("	field = document.forms[" + pc.getFormIndex() + "].elements[\"" + fieldTemplate.getFieldName() + "\"];");
								out.println("	field = document.getElementById(\""+fieldTemplate.getFieldName()+"\");");
								out.println("	if (field != null) {");
								fieldDisplayer.displayScripts(out, fieldTemplate, pc);
								out.println("}\n");
								pc.incCurrentFieldIndex(fieldDisplayer.getNbHtmlObjectsDisplayed(fieldTemplate, pc));
							}
						}
						catch (FormException fe)
						{
							SilverTrace.error("form", "XmlForm.display", "form.EXP_UNKNOWN_FIELD", null, fe);
						}
					}
				}
			}
			out.println("	\n");
			out.println("	switch(errorNb)");
			out.println("	{");
			out.println("	case 0 :");
			out.println("		result = true;");
			out.println("		break;");
			out.println("	case 1 :");
			out.println("		errorMsg = \""+Util.getString("GML.ThisFormContains", language)+" 1 "+Util.getString("GML.error", language)+" : \\n \" + errorMsg;");
			out.println("		window.alert(errorMsg);");
			out.println("		result = false;");
			out.println("		break;");
			out.println("	default :");
			out.println("		errorMsg = \""+Util.getString("GML.ThisFormContains", language) +"\" + errorNb + \" "+Util.getString("GML.errors", language)+" :\\n \" + errorMsg;");
			out.println("		window.alert(errorMsg);");
			out.println("		result = false;");
			out.println("		break;");
			out.println("	}");
			out.println("	return result;");
			out.println("}");
			out.println("	\n");
			out.println("</script>");
			out.flush(); 
			jw.write(sw.toString());
		}
		catch (java.io.IOException fe)
		{
			SilverTrace.error("form", "XmlForm.display", "form.EXP_CANT_WRITE", null, fe);
		}
	}
	
	public abstract void display(JspWriter out, PagesContext PagesContext, DataRecord record);
	
	/**
	   * Updates the values of the dataRecord using the RecordTemplate
	   * to extra control information (readOnly or mandatory status).
	   *
	   * The fieldName must be used to retrieve the HTTP parameter from the request.
	   *
	   * @throw FormException if the field type is not a managed type.
	   * @throw FormException if the field doesn't accept the new value.
	   */
	public List update(List items, DataRecord record, PagesContext pagesContext)
	{
		List attachmentIds = new ArrayList();
		Iterator itFields = null;
		if (fieldTemplates != null)
			itFields = this.fieldTemplates.iterator();
		if ((itFields != null) && (itFields.hasNext()))
		{
			FieldDisplayer 	fieldDisplayer 	= null;
			FieldTemplate 	fieldTemplate 	= null; 
			while (itFields.hasNext())
		  	{
				fieldTemplate = (FieldTemplate) itFields.next();
			  	if (fieldTemplate != null)
			  	{
					String fieldName 			= fieldTemplate.getFieldName();
				  	String fieldType 			= fieldTemplate.getTypeName();
				  	String fieldDisplayerName 	= fieldTemplate.getDisplayerName();
				  	try
				  	{
						if ((fieldDisplayerName == null)||(fieldDisplayerName.equals("")))
							fieldDisplayerName = TypeManager.getDisplayerName(fieldType);
					  	fieldDisplayer = TypeManager.getDisplayer(fieldType, fieldDisplayerName);
					  	if (fieldDisplayer != null)
					  	{
					  		String itemName 	= fieldTemplate.getFieldName();
					  		String itemValue	= null;
					  		boolean updateField = true;
					  		
					  		if (fieldType.equals(UserField.TYPE))
								itemName = itemName+UserField.PARAM_NAME_SUFFIX;
							if (fieldType.equals(FileField.TYPE))
							{
								if ("image".equals(fieldDisplayerName))
								{
									itemValue = processUploadedImage(items, itemName, pagesContext);
								}
								else
								{
									itemValue = processUploadedFile(items, itemName, pagesContext);
									//itemValue is the new attachment's id
									
									if (StringUtil.isDefined(itemValue))
										attachmentIds.add(itemValue);
								}
								
								String param = getParameterValue(items, itemName+FileField.PARAM_NAME_SUFFIX);
								if (param != null)
								{
									if (param.startsWith("remove_"))
									{
										//Il faut supprimer le fichier
										String attachmentId = param.substring("remove_".length());
										deleteAttachment(attachmentId, pagesContext);
									}
									else if(itemValue != null && isInteger(param))
									{
										//Y'avait-il un déjà un fichier ?
										//Il faut remplacer le fichier donc supprimer l'ancien
										deleteAttachment(param, pagesContext);
									}
									else if (itemValue == null)
									{
										//pas de nouveau fichier, ni de suppression
										//le champ ne doit pas être mis à jour
										updateField = false;
									}
								}
							}
							else
							{
								if (fieldDisplayerName.equals("checkbox"))
								{
									itemValue = getParameterValues(items, itemName);
								}
								else
								{
									itemValue = getParameterValue(items, itemName);
								}
								
								if (pagesContext.getUpdatePolicy() == PagesContext.ON_UPDATE_IGNORE_EMPTY_VALUES && !StringUtil.isDefined(itemValue))
									updateField = false;
							}
							
							if (updateField)
								fieldDisplayer.update(itemValue, record.getField(fieldName), fieldTemplate, pagesContext);
					  	}
				  	}
				  	catch (FormException fe)
				  	{
						SilverTrace.error("form", "XmlForm.update", "form.EXP_UNKNOWN_FIELD", null, fe);
				  	}
				  	catch (Exception e)
				  	{
						SilverTrace.error("form", "XmlForm.update", "form.EXP_UNKNOWN_FIELD", null, e);
				  	}
			  	}
		  	}
		}
		return attachmentIds;
	}
	
	private boolean isInteger(String s)
	{
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
  
	private String getParameterValue(List items, String parameterName)
	{
		SilverTrace.debug("form", "XmlForm.getParameterValue", "root.MSG_GEN_ENTER_METHOD", "parameterName = "+parameterName);
		FileItem item = getParameter(items, parameterName);
		if (item != null && item.isFormField()) {
			SilverTrace.debug("form", "XmlForm.getParameterValue", "root.MSG_GEN_EXIT_METHOD", "parameterValue = "+item.getString());
			return item.getString();
		}
		return null;
	}
	
	private String getParameterValues(List items, String parameterName)
	{
		SilverTrace.debug("form", "XmlForm.getParameterValues", "root.MSG_GEN_ENTER_METHOD", "parameterName = "+parameterName);
		String values = "";
		List params = getParameters(items, parameterName);
		FileItem item = null;
		for(int p=0; p<params.size(); p++)
		{
			item = (FileItem) params.get(p);
			values += item.getString();
			if (p<params.size()-1) {
				values += "##";
			}
		}
		SilverTrace.debug("form", "XmlForm.getParameterValues", "root.MSG_GEN_EXIT_METHOD", "parameterValue = "+values);
		return values;
	}
	
	private FileItem getParameter(List items, String parameterName)
	{
		Iterator iter = items.iterator();
		FileItem item = null;
		while (iter.hasNext()) {
			item = (FileItem) iter.next();
			if (parameterName.equals(item.getFieldName())) {
				return item;
			}
		}
		return null;
	}
	
	//for multi-values parameter (like checkbox)
	private List getParameters(List items, String parameterName)
	{
		List parameters = new ArrayList();
		Iterator iter = items.iterator();
		FileItem item = null;
		while (iter.hasNext()) {
			item = (FileItem) iter.next();
			if (parameterName.equals(item.getFieldName())) {
				parameters.add(item);
			}
		}
		return parameters;
	}
	
	private boolean runOnUnix()
	{
		ResourceLocator settings = new ResourceLocator("com.stratelia.webactiv.util.attachment.Attachment", "");
        return settings.getBoolean("runOnSolaris", false);
	}
	
	private String processUploadedFile(List items, String parameterName, PagesContext pagesContext) throws Exception
	{
		String attachmentId = null;
		FileItem item = getParameter(items, parameterName);
		if (!item.isFormField())
		{
			String	componentId		= pagesContext.getComponentId();
			String	userId			= pagesContext.getUserId();
			String 	objectId		= pagesContext.getObjectId();
			String 	logicalName 	= item.getName();
			String 	physicalName 	= null;
			String 	mimeType 		= null;
			String 	context 		= "Images";
			File 	dir 			= null;
			long 	size 			= 0;
			VersioningUtil versioningUtil = new VersioningUtil();
			if(StringUtil.isDefined(logicalName)) {
								
		        if (runOnUnix())
		        {
		        	logicalName = logicalName.replace('\\', File.separatorChar);
    				SilverTrace.info("form", "XmlForm.processUploadedFile", "root.MSG_GEN_PARAM_VALUE", "fullFileName on Unix = "+logicalName);
		        }
				
				logicalName = logicalName.substring(logicalName.lastIndexOf(File.separator)+1, logicalName.length());
				String type = FileRepositoryManager.getFileExtension(logicalName);
				mimeType = item.getContentType();
				if ( mimeType.equals("application/x-zip-compressed") )
				{
					if (type.equalsIgnoreCase("jar") || type.equalsIgnoreCase("ear") || type.equalsIgnoreCase("war"))
						mimeType = "application/java-archive";
					else if (type.equalsIgnoreCase("3D"))
						mimeType = "application/xview3d-3d";
				}
				physicalName = new Long(new Date().getTime()).toString()+"."+type;

				String path = "";
				if (pagesContext.isVersioningUsed())
					path = versioningUtil.createPath("useless", componentId, "useless");
				else
					path = AttachmentController.createPath(componentId, context);
				dir = new File(path+physicalName);
				size = item.getSize();
				item.write(dir);
				
				//l'ajout du fichier joint ne se fait que si la taille du fichier (size) est >0 
				//sinon cela indique que le fichier n'est pas valide (chemin non valide, fichier non accessible)
				if(size>0)
				{
					AttachmentDetail ad = createAttachmentDetail(objectId, componentId, physicalName, logicalName, mimeType, size, context, userId);

					if (pagesContext.isVersioningUsed())
					{
						//mode versioning
						attachmentId = createDocument(objectId, ad);
					}
					else
					{
						//mode classique
						ad = AttachmentController.createAttachment(ad, true);
						attachmentId = ad.getPK().getId();
					}
				}
				else
				{
					//le fichier à tout de même été créé sur le serveur avec une taille 0!, il faut le supprimer
					if (dir != null)
						FileFolderManager.deleteFolder(dir.getPath());
				}
			}
		}
		return attachmentId;
	}
	
	private String processUploadedImage(List items, String parameterName, PagesContext pagesContext) throws Exception
	{
		String attachmentId = null;
		FileItem item = getParameter(items, parameterName);
		if (!item.isFormField())
		{
			String	componentId		= pagesContext.getComponentId();
			String	userId			= pagesContext.getUserId();
			String 	objectId		= pagesContext.getObjectId();
			String 	logicalName 	= item.getName();
			String 	physicalName 	= null;
			String 	type 			= null;
			String 	mimeType 		= null;
			String 	context 		= "XMLFormImages";
			File 	dir 			= null;
			long 	size 			= 0;
			if(StringUtil.isDefined(logicalName)) 
			{
				if (runOnUnix())
		        {
		        	logicalName = logicalName.replace('\\', File.separatorChar);
    				SilverTrace.info("form", "XmlForm.processUploadedImage", "root.MSG_GEN_PARAM_VALUE", "fullFileName on Unix = "+logicalName);
		        }
				
				logicalName = logicalName.substring(logicalName.lastIndexOf(File.separator)+1, logicalName.length());
				type 		= FileRepositoryManager.getFileExtension(logicalName);
				mimeType 	= item.getContentType();
							
				physicalName = new Long(new Date().getTime()).toString()+"."+type;

				String path = AttachmentController.createPath(componentId, context);
				dir = new File(path+physicalName);
				size = item.getSize();
				item.write(dir);
				
				//l'ajout du fichier joint ne se fait que si la taille du fichier (size) est >0 
				//sinon cela indique que le fichier n'est pas valide (chemin non valide, fichier non accessible)
				if(size>0)
				{
					AttachmentDetail ad = createAttachmentDetail(objectId, componentId, physicalName, logicalName, mimeType, size, context, userId);
					ad = AttachmentController.createAttachment(ad, true);
					attachmentId = ad.getPK().getId();
				}
				else
				{
					//le fichier à tout de même été créé sur le serveur avec une taille 0!, il faut le supprimer
					if (dir != null)
						FileFolderManager.deleteFolder(dir.getPath());
				}
			}
		}
		return attachmentId;
	}
	
	private AttachmentDetail createAttachmentDetail(String objectId, String componentId, String physicalName, String logicalName, String mimeType, long size, String context, String userId)
	{
		//create AttachmentPK with spaceId and componentId
		AttachmentPK atPK = new AttachmentPK(null, "useless", componentId);

		//create foreignKey with spaceId, componentId and id
		//use AttachmentPK to build the foreign key of customer object.
		AttachmentPK foreignKey = new AttachmentPK("-1", "useless", componentId);
		if (objectId != null)
			foreignKey.setId(objectId);

		//create AttachmentDetail Object
		AttachmentDetail ad = new AttachmentDetail(atPK, physicalName, logicalName, null, mimeType, size, context, new Date(), foreignKey);
		ad.setAuthor(userId);

		return ad;
	}
	
	private void deleteAttachment(String attachmentId, PagesContext pageContext)
	{
		SilverTrace.info("form", "XmlForm.deleteAttachment", "root.MSG_GEN_ENTER_METHOD", "attachmentId = "+attachmentId+", componentId = "+pageContext.getComponentId());
		AttachmentPK pk = new AttachmentPK(attachmentId, pageContext.getComponentId());
		AttachmentController.deleteAttachment(pk);
	}
	
	private String createDocument(String objectId, AttachmentDetail attachment) throws RemoteException
	{
		String			componentId = attachment.getPK().getInstanceId();
		int				userId		= Integer.parseInt(attachment.getAuthor());
		ForeignPK 		pubPK 		= new ForeignPK("-1", componentId);
		if (objectId != null)
			pubPK.setId(objectId);
		
		//Création d'un nouveau document
		DocumentPK docPK = new DocumentPK(-1, "useless", componentId);
		Document document = new Document(docPK, pubPK, attachment.getLogicalName(), "", -1, userId, new Date(), null, null, null, null, 0, 0);
		
		document.setWorkList(getWorkers(componentId, userId));
		
		DocumentVersion version = new DocumentVersion(attachment);
		version.setAuthorId(userId);

		//et on y ajoute la première version
		version.setMajorNumber(1);
		version.setMinorNumber(0);
		version.setType(DocumentVersion.TYPE_PUBLIC_VERSION);
		version.setStatus(DocumentVersion.STATUS_VALIDATION_NOT_REQ);
		version.setCreationDate(new Date());

		docPK = getVersioningBm().createDocument(document, version);
		document.setPk(docPK);
		
		return docPK.getId();
	}
	
	private ArrayList getWorkers(String componentId, int creatorId)
	{
		ArrayList workers 	= new ArrayList();
	
		OrganizationController orga = new OrganizationController();
		ComponentInst component = orga.getComponentInst(componentId);
		
		List profilesInst = component.getAllProfilesInst();
		List profiles = new ArrayList();
		ProfileInst profileInst = null;
		for (int p=0; p<profilesInst.size(); p++)
		{
			profileInst = (ProfileInst) profilesInst.get(p);
			profiles.add(profileInst.getName());
		}
	
		String[] userIds = orga.getUsersIdsByRoleNames(componentId, profiles);
	
		int 	userId 	= -1;
		Worker 	worker 	= null;
		boolean	find	= false;
		for (int u=0; u<userIds.length; u++)
		{
			userId = Integer.parseInt(userIds[u]);
		
			if (!find && (userId == creatorId))
				find = true;
			
			worker = new Worker(userId, 0, 0, true, true, componentId);
			workers.add(worker);
		}
		if (!find)
		{
			worker = new Worker(creatorId, 0, 0, true, true, componentId);
			workers.add(worker);
		}
	
		Worker lastWorker = (Worker) workers.get(workers.size() - 1);
		lastWorker.setApproval(true);
	
		return workers;
	}
	
	private VersioningBm getVersioningBm()
	{
		if (versioningBm == null)
		{
			try {
				VersioningBmHome vscEjbHome = (VersioningBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.VERSIONING_EJBHOME, VersioningBmHome.class);
				versioningBm = vscEjbHome.create();
			} catch (Exception e) {
				// NEED
				//throw new ...RuntimeException("VersioningSessionController.initEJB()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_REMOTE_OBJECT",e);
			}
		}
		return versioningBm;
	}
	
}
