<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ include file="checkAttachment.jsp"%>
<%@ page import="com.stratelia.webactiv.util.attachment.model.AttachmentDetailI18N"%>
<%@ page import="com.silverpeas.util.i18n.I18NHelper"%>

<%
	String id			= "";
	String componentId	= "";
	String context		= "";
	String url			= "";
	String title		= "";
	String attachmentId	= "";
	long size	= 0;
	String type			= "";
    String mimeType 	= "";
    String description 	= "";
	String logicalName	= "";
	String physicalName	= "";
	String dNdVisible 	= "";
	
	String pIndexIt		= null;
	boolean indexIt		= true;
   
	AttachmentPK		atPK	= null;
	AttachmentDetail	ad		= null;
		
	DiskFileUpload 	dfu 	= new DiskFileUpload();
	List 			items 	= dfu.parseRequest(request);
	  
	  id 			= getParameterValue(items, "Id");
	  componentId 	= getParameterValue(items, "ComponentId");
	  context 		= getParameterValue(items, "Context");
	  url 			= getParameterValue(items, "Url");
	  title 		= getParameterValue(items, "Title");
	  description	= getParameterValue(items, "Description");
	  pIndexIt 		= getParameterValue(items, "IndexIt");
	  attachmentId 	= getParameterValue(items, "IdAttachment");
	  dNdVisible 	= getParameterValue(items, "DNDVisible");
	  
	  if ("1".equals(pIndexIt))
		  indexIt = true;
	  else if ("0".equals(pIndexIt))
		  indexIt = false;
	  
	  atPK	= new AttachmentPK(attachmentId, componentId);
	  ad	= AttachmentController.searchAttachmentByPK(atPK);
	  
	  //Just to retrieve i18n infos
	  AttachmentDetail dummy = new AttachmentDetail();
	  I18NHelper.setI18NInfo(dummy, items);
	  
  	  FileItem file = getUploadedFile(items, "file_upload");
	  if (file != null && StringUtil.isDefined(file.getName()))
	  {
		  logicalName = file.getName();
			  
		  if (runOnUnix())
			  logicalName = logicalName.replace('\\', File.separatorChar);
		  
		  logicalName 	= logicalName.substring(logicalName.lastIndexOf(File.separator)+1, logicalName.length());
		  type			= FileRepositoryManager.getFileExtension(logicalName);
			
		  physicalName 	= new Long(new Date().getTime()).toString() + "." +type;
		  
		  mimeType 		= file.getContentType();
		  size 			= file.getSize();
		  
		  if (mimeType.equals("application/x-zip-compressed"))
		  {
			  if (type.equalsIgnoreCase("jar") || type.equalsIgnoreCase("ear") || type.equalsIgnoreCase("war"))
				  mimeType = "application/java-archive";
			  else if (type.equalsIgnoreCase("3D"))
				  mimeType = "application/xview3d-3d";
		  }
		  else if (type.equalsIgnoreCase("rtf"))
			  mimeType = AttachmentController.getMimeType(logicalName);
		  
		  
		  if (I18NHelper.isI18N)
		  {
			  AttachmentDetail toDelete = new AttachmentDetail();
			  
			  AttachmentDetailI18N translation = (AttachmentDetailI18N) ad.getTranslation(dummy.getLanguage());
			  if (translation == null)
			  {
				  //case of a translation creation
			  }
			  else
			  {
				  //case of a translation update
				  String fileToDelete = translation.getPhysicalName();
				  
				  toDelete.setContext(ad.getContext());
				  toDelete.setPK(ad.getPK());
				  toDelete.setPhysicalName(fileToDelete);
				  
				  System.out.println("fileToDelete = "+fileToDelete+", ad.getContext() = "+ad.getContext()+", ad.getPK() = "+ad.getPK().toString()+", toDelete.getAttachmentGroup() = "+toDelete.getAttachmentGroup());
				  
				  AttachmentController.deleteFileAndIndex(toDelete);
			  }
		  }
		  else
		  {
			  AttachmentController.deleteFileAndIndex(ad);
		  }
		  
		  String path = AttachmentController.createPath(componentId, context);
		  File dir = new File(path+physicalName);
		  file.write(dir);
			
		  // Mise a jour des valeurs
		  ad.setSize(size);
		  ad.setPhysicalName(physicalName);
		  ad.setLogicalName(logicalName);
		  ad.setType(mimeType);
		  ad.setCreationDate(null);
		  
		  SilverTrace.info("attachment", "updateFile.jsp", "root.MSG_GEN_PARAM_VALUE","mimetype="+mimeType);
	  }
	  else
	  {
		  if (I18NHelper.isI18N)
			  ad.setPhysicalName(null);
	  }
		  
	ad.setAuthor(m_MainSessionCtrl.getUserId());
	ad.setTitle(title);
	ad.setInfo(description);
	
	I18NHelper.setI18NInfo(ad, items);
		
	AttachmentController.updateAttachment(ad, indexIt);

	String paramDelimiter = "?";
	if (url.indexOf("?") != -1) {
			//Il existe déjà un '?' dans la chaine url
			paramDelimiter = "&";
	}
	String returnURL = URLManager.getApplicationURL()+ url + paramDelimiter + "Id="+id+"&Component="+componentId+"&IndexIt="+pIndexIt+"&DNDVisible="+dNdVisible;
%>
<HTML>
	<BODY>
	<script language='javascript'>
		window.opener.location.href='<%=returnURL%>';
		window.close();
	</script>
	</BODY>	
</HTML>