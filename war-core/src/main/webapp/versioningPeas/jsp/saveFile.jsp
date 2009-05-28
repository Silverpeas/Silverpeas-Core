 %@ page language="java"%>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%@ include file="checkVersion.jsp" %>

<%
    String spaceId = request.getParameter("spaceId");
    String componentId = versioningSC.getComponentId();
    String documentId = request.getParameter("documentId");
    String id = null;
    String context = null;
    String url = "ViewVersions";
    String path = null;
    Part part = null;
    FilePart filePart = null;
    String partName = null;
    String logicalName = null;
    String physicalName = null;
    String mimeType = null;
    Date creationDate = new Date();
    File dir = null;
    int size = 0;
    String type = null;
    String versionType = null;
    String description = null;
    String comments = null;
    String name = null;
    String publicationId = null;
    String radio = null;
    boolean isExistFile = true; // par defaut, le fichier que l'utilisateur veut ajouter existe.

    boolean actifyPublisherEnable = attachmentSettings.getBoolean("ActifyPublisherEnable", false);
				
      SilverpeasMultipartParser mp = new SilverpeasMultipartParser(request);

      //recovery of parameters in the object Part
      while ((part = mp.readNextPart()) != null) {
        partName = part.getName();

        if (part.isParam()) {
          // it's a parameter part
          SilverpeasParamPart paramPart = (SilverpeasParamPart) part;
          if (partName.equals("name"))
              name = paramPart.getStringValue();
          if (partName.equals("description"))
              description = paramPart.getStringValue();
          if (partName.equals("type"))
              type = paramPart.getStringValue();
          if (partName.equals("comments"))
              comments = paramPart.getStringValue();
			if (partName.equals("radio"))
                radio = paramPart.getStringValue();

            if (partName.equals("publicationId"))
                publicationId = paramPart.getStringValue();
            if (partName.equals("versionType"))
                versionType = paramPart.getStringValue();

        } else if(part.isFile()) {
              //it's file part
              filePart = (FilePart) part;
              logicalName = filePart.getFileName();
              if(logicalName != null) {
                  type = logicalName.substring(logicalName.indexOf(".")+1, logicalName.length());
                  physicalName = new Long(new Date().getTime()).toString() + "." +type;
                  mimeType = filePart.getContentType();
                  dir = new File(versioningSC.createPath(spaceId, componentId, null ) + physicalName);
                  size = (int)filePart.writeTo(dir);
              }
          }
      }
          //l'ajout du fichier joint ne se fait que si la taille du fichier (size) est >0
          //sinon cela indique que le fichier n'est pas valide (chemin non valide, fichier non accessible)
          if(size>0){
              //create docPK with spaceId and componentId
              ForeignPK pubPK = new ForeignPK(publicationId, componentId);
              DocumentPK docPK = new DocumentPK(Integer.parseInt(documentId), spaceId, componentId );
              int userId = Integer.parseInt(m_MainSessionCtrl.getUserId());
              
              DocumentVersion documentVersion = null;
              DocumentVersion lastVersion = versioningSC.getLastVersion(docPK);
              if(com.stratelia.silverpeas.versioning.ejb.RepositoryHelper.getJcrDocumentService().isNodeLocked(lastVersion)) {
                url = m_context +  "/versioningPeas/jsp/documentLocked.jsp";
              }else {

                List versions = versioningSC.getDocumentVersions(docPK);
                int majorNumber = 0;
                int minorNumber = 1;
                if ( versions != null && versions.size() > 0 )
                {
                   documentVersion =  (DocumentVersion) versions.get( versions.size()-1 );                 
                   majorNumber = documentVersion.getMajorNumber();
                   minorNumber = documentVersion.getMinorNumber();
                   Document currdoc = versioningSC.getEditingDocument();
                   DocumentVersion newVersion = new DocumentVersion( null, docPK, majorNumber, minorNumber, userId, creationDate,
                  		 					comments, Integer.parseInt(radio), documentVersion.getStatus(),
  							                physicalName, logicalName, mimeType, size, componentId);
                   versioningSC.addNewDocumentVersion(newVersion);
                   
  				//Specific case: 3d file to convert by Actify Publisher
  				if (actifyPublisherEnable)
  				{
  					String extensions = attachmentSettings.getString("Actify3dFiles");
  					SilverStringTokenizer tokenizer = new SilverStringTokenizer(extensions, ",");
  					//3d native file ?
  					boolean fileForActify = false;
  					SilverTrace.info("versioningPeas", "saveFile.jsp", "root.MSG_GEN_PARAM_VALUE", "nb tokenizer ="+tokenizer.countTokens());
  					while (tokenizer.hasMoreTokens() && !fileForActify)
  					{
  						String extension =  tokenizer.nextToken();
  						if (type.equalsIgnoreCase(extension))
  							fileForActify = true;
  					}
  					if (fileForActify)
  					{
  						String dirDestName 			= "v_" + componentId + "_" + documentId;
  						String actifyWorkingPath 	= attachmentSettings.getString("ActifyPathSource") + File.separator + dirDestName;
  		
  						String destPath = FileRepositoryManager.getTemporaryPath() + actifyWorkingPath; 
  						if (!new File(destPath).exists())
  							FileRepositoryManager.createGlobalTempPath(actifyWorkingPath);
  		
  						String destFile			= FileRepositoryManager.getTemporaryPath() + actifyWorkingPath + File.separator + logicalName;
  						FileRepositoryManager.copyFile(versioningSC.createPath(componentId, null) + File.separator + physicalName, destFile);
  					}
  				} 
         }
       }
     }
      else{
          SilverTrace.info("versioningPeas", "saveFile.jsp", "root.MSG_GEN_PARAM_VALUE","dir="+dir.getPath());
          //le fichier à tout de même été créé sur le serveur avec une taille 0!, il faut le supprimer
          FileFolderManager.deleteFolder(dir.getPath());
          isExistFile = false;
      }

      ResourceLocator messages = new ResourceLocator("com.stratelia.webactiv.util.attachment.multilang.attachment", m_MainSessionCtrl.getFavoriteLanguage());
%>
<script language="javascript">
	<% if (isExistFile) { %>
			window.location = '<%=url%>';
	<%	} %>
</script>
