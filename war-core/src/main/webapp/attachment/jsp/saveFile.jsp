<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ include file="checkAttachment.jsp"%>
<%@ page import="com.silverpeas.util.i18n.I18NHelper"%>
<%@ page import="com.silverpeas.util.StringUtil"%>
<%@ page import="java.util.StringTokenizer"%>

<%
  String id				= null;
  String componentId	= null;
  String context		= null;
  String url			= null;
  String pIndexIt		= null;
  boolean indexIt		= true;

  String path			= null;
  
  String logicalName	= null;
  String physicalName	= null;
  String mimeType		= null;
  Date creationDate		= new Date();
  long size				= 0;
  String type			= null;
  String title			= null;
  String info			= null;
  boolean isExistFile	= true; // par defaut, le fichier que l'utilisateur veut ajouter existe.

  ResourceLocator settings = new ResourceLocator("com.stratelia.webactiv.util.attachment.Attachment", "");
  boolean actifyPublisherEnable = settings.getBoolean("ActifyPublisherEnable", false);
  
  DiskFileUpload 	dfu 	= new DiskFileUpload();
  List 				items 	= dfu.parseRequest(request);
  
  id 			= getParameterValue(items, "Id");
  componentId 	= getParameterValue(items, "ComponentId");
  context 		= getParameterValue(items, "Context");
  path 			= getParameterValue(items, "Path");
  url 			= getParameterValue(items, "Url");
  title 		= getParameterValue(items, "Title");
  info 			= getParameterValue(items, "Description");
  pIndexIt 		= getParameterValue(items, "IndexIt");
  
  if ("1".equals(pIndexIt))
	  indexIt = true;
  else if ("0".equals(pIndexIt))
	  indexIt = false;
  
  FileItem file = getUploadedFile(items, "file_upload");
  if (file != null)
  {
	  logicalName = file.getName();
	  if(StringUtil.isDefined(logicalName)) {
		  
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
		  
		  File dir = new File(path+physicalName);
		  
		  file.write(dir);
		  
		  SilverTrace.info("attachment", "SaveFile.jsp", "root.MSG_GEN_PARAM_VALUE","mimetype="+mimeType);
	  }
  }
 
	//l'ajout du fichier joint ne se fait que si la taille du fichier (size) est >0 
	//sinon cela indique que le fichier n'est pas valide (chemin non valide, fichier non accessible)
	if (size>0)
	{
		//create AttachmentPK with componentId
		AttachmentPK atPK = new AttachmentPK(null, componentId);

		//create foreignKey with spaceId, componentId and id
		//use AttachmentPK to build the foreign key of customer object.
		AttachmentPK foreignKey =  new AttachmentPK(id, componentId);

		//create AttachmentDetail Object
		AttachmentDetail ad = new AttachmentDetail(atPK, physicalName, logicalName, null, mimeType, size, context, creationDate, foreignKey);
		ad.setAuthor(m_MainSessionCtrl.getUserId());
		ad.setTitle(title);
		ad.setInfo(info);
		
		I18NHelper.setI18NInfo(ad, items);
		
		AttachmentController.createAttachment(ad, indexIt);
		
		//Specific case: 3d file to convert by Actify Publisher
		if (actifyPublisherEnable)
		{
			String extensions = settings.getString("Actify3dFiles");
			StringTokenizer tokenizer = new StringTokenizer(extensions, ",");
			//3d native file ?
			boolean fileForActify = false;
			while (tokenizer.hasMoreTokens() && !fileForActify)
			{
				String extension =  tokenizer.nextToken();
				if (type.equalsIgnoreCase(extension))
					fileForActify = true;
			}
			if (fileForActify)
			{
				String dirDestName 			= "a_" + componentId + "_" + id;
				String actifyWorkingPath 	= settings.getString("ActifyPathSource") + File.separator + dirDestName;

				String destPath = FileRepositoryManager.getTemporaryPath() + actifyWorkingPath; 
				if (!new File(destPath).exists())
					FileRepositoryManager.createGlobalTempPath(actifyWorkingPath);

				String destFile			= FileRepositoryManager.getTemporaryPath() + actifyWorkingPath + File.separator + logicalName;
				FileRepositoryManager.copyFile(AttachmentController.createPath(componentId, "Images") + File.separator + physicalName, destFile);
			}
		} 
	}
	/*else{
			SilverTrace.info("attachment", "SaveFile.jsp", "root.MSG_GEN_PARAM_VALUE","dir="+dir.getPath());
			//le fichier à tout de même été créé sur le serveur avec une taille 0!, il faut le supprimer
			FileFolderManager ffm = new FileFolderManager();
			ffm.deleteFolder(dir.getPath());
			isExistFile = false;
	}*/
%>
<HTML>
<HEAD>
		<TITLE>_________________/ Silverpeas - Corporate portal organizer \_________________/</TITLE>
		<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
		<% out.println(gef.getLookStyleSheet()); %>
</HEAD>
<BODY>
<%
	Frame frame=gef.getFrame();
	out.println(frame.printBefore());
    String paramDelimiter = "?";
    if (url.indexOf("?") != -1) {
            //Il existe déjà un '?' dans la chaine url
            paramDelimiter = "&";
    }
%>
<center>
<% if (!isExistFile){ %>
		<table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
		  <tr>
		    <td valign="top" align="center"> <!-- SEPARATION NAVIGATION / CONTENU DU COMPOSANT -->
		      <table border="0" cellspacing="0" cellpadding="5" width="100%" align="center" class="contourintfdcolor">
		        <tr>
		          <td align="center">
		                <B>
		                <%
	                        if (!isExistFile){
	                                out.println(messages.getString("fichierInexistant"));
	                        }
		                %>
		          </td>
		        </tr>
					</table> 
			   </td>
       </tr>  
      </table><br>
     <%
        ButtonPane buttonPane2 = gef.getButtonPane();
        buttonPane2.addButton((Button) gef.getFormButton(resources.getString("GML.back"), "javascript:window.opener.location.href='"+m_Context+ url + paramDelimiter +"Id="+id + "&Component=" + componentId + "'; window.close()", false));
        out.println(buttonPane2.print());
        out.println(frame.printMiddle());
        out.println(frame.printAfter());
		%>
<% } else { %>
	<script language='javascript'>
		window.opener.location.href="<%=m_Context%><%=url%><%=paramDelimiter%>" + "Id=<%=id%>&Component=<%=componentId%>";
		window.close();
	</script>
<% } %>
</BODY>
<script language='javascript'>
	window.focus(); 
</script>
</HTML>