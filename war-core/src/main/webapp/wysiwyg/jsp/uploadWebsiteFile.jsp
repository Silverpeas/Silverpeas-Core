<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.ButtonPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.Button"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="java.io.File"%>
<%@ page import="com.oreilly.servlet.multipart.*"%>
<%@ page import="com.oreilly.servlet.MultipartRequest"%>
<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>
<%@ include file="checkScc.jsp" %>

<% 
	String language = (String) request.getParameter("Language");
	String thePath = (String) request.getParameter("Path");
	
    String iconsPath = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL")+ "/util/icons/wysiwyg/";
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
    ResourceLocator message = new ResourceLocator("com.stratelia.silverpeas.wysiwyg.multilang.wysiwygBundle", language);
    ResourceLocator generalMessage = GeneralPropertiesManager.getGeneralMultilang(language);

    //Le cadre
    Board board = gef.getBoard();

    FilePart filePart;
    boolean uploadFile = false;
    boolean uploadOk = true;
		String fichierName = "";
	 	String urlPath = "";
		
    //Icons
    String mandatoryField = m_context + "/util/icons/mandatoryField.gif";
    try {
		if (! ((String) session.getValue("prems")).equals("premiere fois")) { /* deuxieme fois */
			SilverpeasMultipartParser mp = new SilverpeasMultipartParser(request);
			Part part;
		    while ((part = mp.readNextPart()) != null) {
		        String name = part.getName();
		        if (part.isParam()) {
		            SilverpeasParamPart paramPart = (SilverpeasParamPart) part;
		        }
		        else if (part.isFile()) {
		            filePart = (FilePart) part;
		            /* creation du fichier sur le serveur */
		            fichierName = filePart.getFileName();
		            String type = fichierName.substring(fichierName.lastIndexOf(".")+1, fichierName.length());
		            File fichier = new File(thePath, fichierName);
		            long size = filePart.writeTo(fichier);
		            if (size <= 0) 
		            	uploadOk = false;
		            if (! uploadOk) {//le fichier n'est pas bon, on supprime le fichier
		            	scc.deleteFile(thePath, fichierName);
				    }
		    			else { 
		    				uploadFile = true;
							 } 
		       } //partFile
		   } //ferme le while
		}
			 if (uploadOk) {
			 		urlPath = thePath.substring(thePath.indexOf("/website"));
			  %>
			    <script language="javascript">
				window.opener.document.frm_upload.txt_object.value='<%=Encode.javaStringToJsString(urlPath+'/'+fichierName)%>';
				window.close();
				</script>
			<% }
   }
   catch(Exception e) {
        SilverTrace.warn("wysiwyg", "JSPuploadFile", "wysiwyg.EXE_UPLOAD_FILE_FAILED",null, e);
   }
   finally {
   
   }
   
%>
	<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
	<script language="javascript">
		function isCorrect(nom) {
	    	if (nom.indexOf("&")>-1 || nom.indexOf(";")>-1 || nom.indexOf("+")>-1 ||
		        nom.indexOf("%")>-1 || nom.indexOf("#")>-1 || nom.indexOf("'")>-1 ||
		        nom.indexOf("²")>-1 || nom.indexOf("é")>-1 || nom.indexOf("è")>-1 ||
		        nom.indexOf("ç")>-1 || nom.indexOf("à")>-1 || nom.indexOf("^")>-1 ||
		        nom.indexOf("ù")>-1 || nom.indexOf("°")>-1 || /*nom.indexOf("¨")>-1 ||*/
		        nom.indexOf("£")>-1 || nom.indexOf("µ")>-1 || nom.indexOf("§")>-1 ||
		        nom.indexOf("¤")>-1) {
		    }
		    return true;
	     }
	    function B_VALIDER_ONCLICK() {                  
	        if (checkString(document.descriptionFile.fichier, "<%=message.getString("ErreurmPrefix")+message.getString("FichierUpload")+message.getString("ErreurmSuffix")%>")) {
	            var file = document.descriptionFile.fichier.value;
	            var indexSlash = file.lastIndexOf("\\");
	            var cheminFile = file.substring(0, indexSlash);
	            
	            if (cheminFile == "") 
	                alert("<%=message.getString("ErreurFichierUpload")%>");
	            else {
	                var indexPoint = file.lastIndexOf(".");
	                var nomFile = file.substring(indexSlash + 1, indexPoint);
	                var ext = file.substring(indexPoint + 1);
	                if ((ext.toLowerCase() != "gif") && (ext.toLowerCase()!= "jpg") && 
	        			(ext.toLowerCase() != "bmp") && (ext.toLowerCase() != "png") && 
	        			(ext.toLowerCase() != "pcd") && (ext.toLowerCase() != "tga") && 
	        			(ext.toLowerCase() != "tif") && ext.toLowerCase() != "swf") 
	                        alert("<%=message.getString("ErreurFichierUpload")%>");
	                else if (! isCorrect(nomFile)) // verif caractères speciaux contenus dans le nom du fichier
	            alert("<%=message.getString("NameFile")%> <%=message.getString("MustNotContainSpecialChar")%>\n<%=Encode.javaStringToJsString(message.getString("Char7"))%>\n");
	            else {                     
	                    <%session.putValue("prems", "deuxieme fois");%>
	                    document.descriptionFile.submit();
	                }
	            } 
	       }
	    }
	</script>
	</HEAD>

<% out.println(board.printBefore()); %>

	<BODY bgcolor="white" topmargin="15" leftmargin="20" onClose="">
	  <FORM NAME="descriptionFile" ACTION="uploadFile.jsp" METHOD="POST" ENCTYPE="multipart/form-data">
		<table>      
	    <TR>
	        <TD class="txtlibform"><%=message.getString("FichierUpload")%> : </TD>
	        <td valign="top">&nbsp;<input type="file" name="fichier">&nbsp;<img border="0" src="<%=mandatoryField%>" width="5" height="5"></td>
	    </TR>
        <TR> 
            <TD colspan="2">(<img border="0" src="<%=mandatoryField%>" width="5" height="5"> 
              : <%=message.getString("RequiredField")%>)</TD>
        </TR>
        </table>      
	</FORM>
  <%
  out.println(board.printAfter());
    
	//fin du code
	ButtonPane buttonPane = gef.getButtonPane();
	Button validerButton = (Button) gef.getFormButton(message.getString("Valider"), "javascript:onClick=B_VALIDER_ONCLICK();", false);
	buttonPane.addButton(validerButton);
          
  String bodyPart ="<center>";
  bodyPart += buttonPane.print();
  bodyPart +="</center><br>";
	out.println(bodyPart);

 %>
  </BODY>       
  </HTML>
