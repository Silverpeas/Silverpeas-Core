<%@ include file="checkPdc.jsp"%>
<%
	String type = (String) request.getAttribute("ViewType");
	List primaryAxis = (List) request.getAttribute("PrimaryAxis");
	List secondaryAxis = (List) request.getAttribute("SecondaryAxis");
	
	AxisHeader axisHeader = (AxisHeader) request.getAttribute("AxisHeader");
	String max = (String) request.getAttribute("MaxAxis");
	String alreadyExist = (String) request.getAttribute("AlreadyExist");
	String modification = (String) request.getAttribute("Modif"); // on modifie l'axe

	String translation = (String) request.getAttribute("Translation");
	if (translation == null || translation.equals("null"))
	{
		translation = I18NHelper.defaultLanguage;
	}
	
	String id = "";
	String name = "";
	String description = "";
	String formAction = "CreateAxis";
	String order = "";
	String primaryChecked = "checked";
	String secondaryChecked = "";
	String errorMessage = "";
	List selectedAxis = primaryAxis; // pour affichage des options du tag select 
	if (type.equals("S")) {
		primaryChecked = "";
		secondaryChecked = "checked";
		selectedAxis = secondaryAxis; // pour affichage des options du tag select 
	}
	if (max != null && max.equals("1")) {
		errorMessage = "<font size=2 color=#FF6600><b>"+resource.getString("pdcPeas.maximumAxisReach")+"</b></font>";
	} else {
		if (axisHeader != null) {
			id = axisHeader.getPK().getId();
			if (id.equals("unknown")) {
				//Creation case
				formAction = "CreateAxis";
			} else {
				//Update case
				formAction = "UpdateAxis";
			}
			name = axisHeader.getName(translation);
			description = axisHeader.getDescription(translation);
			order = new Integer(axisHeader.getAxisOrder()).toString();
			if (alreadyExist != null && alreadyExist.equals("1")) {
				errorMessage = "<font size=2 color=#FF6600><b>"+resource.getString("pdcPeas.axisAlreadyExist")+"</b></font>";
			}
		}
	}

	// pour affichage des options du tag select 
	Iterator it = null;
	int nbItemShowed = 1; // cas ou ce serait le 1er axe de créer
	if (!selectedAxis.isEmpty()){
		it = selectedAxis.iterator();
		if (selectedAxis.size() < 5) {
			nbItemShowed = selectedAxis.size()+2; // par defaut, si la liste n'est pas vide alors nous devons tenir compte des 2 items d'insertions
			if (modification != null) // cas ou nous ne sommes en modification
				nbItemShowed = nbItemShowed - 2;
		} else {
			nbItemShowed = 5;
		}
	}
	AxisHeader tempAxisHeader = null;
	String axisName = "";
%>
<html>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/pdcPeas/jsp/javascript/formUtil.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/i18n.js"></script>

<script language="Javascript">

	function isCorrectForm(){	
		var name = stripInitialWhitespace(document.axisForm.Name.value);
		if (isWhitespace(name)) {		
			alert("<%=resource.getString("pdcPeas.emptyName")%>");
			document.axisForm.Name.focus();
			return false;
		} else {
			if (document.axisForm.Description.value.length > 1000) {
               alert("<%=resource.getString("pdcPeas.lenDescription")%>");
               document.axisForm.Description.focus();
               return false;
            } else {
               return true;
            }
		}
	}

	function sendData() {
		if (isCorrectForm()) {
			document.axisForm.submit();
		}
	}

	function changeList(axisType){

		<%! 
			AxisHeader h = null; // un axis header pour construire la nouvelle liste
			String nom = ""; // le nom de l'axis header
			String desc = "";
			String ordre = ""; // son ordre
			int o; // son ordre
			int item; // place de l'item dans l'objet html SELECT			
		%>
		// effacer toutes les options actuelles
		var longueur_list = document.axisForm.Order.length;
		for (i=0;i<longueur_list ;i++ ){
			document.axisForm.Order.options[0] = null;
		}

		if (axisType == 'P'){
			// définition des noms d'axe et de leur ordre de l'axe primaire
			<%
			if (!primaryAxis.isEmpty()){
				// la liste n'est pas vide, on construit les instructions javascript pour reconstuire dynamique la liste
				Iterator IteratorP = primaryAxis.iterator();
				item = 0;
				while (IteratorP.hasNext()){
					h = (AxisHeader)IteratorP.next();
					nom = Encode.javaStringToHtmlString(h.getName());
					desc = Encode.javaStringToHtmlString(h.getDescription());
					o = h.getAxisOrder();
					ordre = new Integer(o).toString();
					if (!(h.getPK().getId()).equals(id)){
						out.println("document.axisForm.Order.options["+item+"] = new Option(\""+nom+"\",\""+nom+sepOptionValueTag+ordre+"\");");
						item++;
					}
				}	
				o++;
				// ajoute le dernier element
				if (modification == null) // création
					out.println("document.axisForm.Order.options["+item+"] = new Option(\"<"+resource.getString("pdcPeas.EndTag")+">\",\""+o+"\",true,\"selected\");");
				else
					out.println("document.axisForm.Order.options["+item+"] = new Option(\"<"+resource.getString("pdcPeas.EndTag")+">\",\""+o+"\");");
			} else {
				out.println("document.axisForm.Order.options[0] = new Option(\"<"+resource.getString("pdcPeas.EndTag")+">\",\"0\",true,\"selected\");");
			}
			out.println("document.axisForm.Order.size = 5"); // définition en dur de la taille de la liste :-(
			%>
		} else {
			// définition des noms d'axe et de leur ordre
			<% 
			if (!secondaryAxis.isEmpty()){
				Iterator IteratorS = secondaryAxis.iterator();
				item = 0;
				while (IteratorS.hasNext()){
					h = (AxisHeader)IteratorS.next();
					nom = Encode.javaStringToHtmlString(h.getName());
					desc = Encode.javaStringToHtmlString(h.getDescription());
					o = h.getAxisOrder();
					ordre = new Integer(o).toString();
					if (!(h.getPK().getId()).equals(id)){
						out.println("document.axisForm.Order.options["+item+"] = new Option(\""+nom+"\",\""+nom+sepOptionValueTag+ordre+"\");");
						item++;
					}
				}
				o++;
				// ajoute le dernier element
				if (modification == null) // création
					out.println("document.axisForm.Order.options["+item+"] = new Option(\"<"+resource.getString("pdcPeas.EndTag")+">\",\""+o+"\",true,\"selected\");");
				else
					out.println("document.axisForm.Order.options["+item+"] = new Option(\"<"+resource.getString("pdcPeas.EndTag")+">\",\""+o+"\");");
			} else {
				out.println("document.axisForm.Order.options[0] = new Option(\"<"+resource.getString("pdcPeas.EndTag")+">\",\"0\",true,\"selected\");");
			}
			out.println("document.axisForm.Order.size = 5"); // définition en dur de la taille de la liste :-(
			%>
		}
	}
	
	// gestion des traductions
	
	<%
	if (axisHeader != null)
	{
		String lang = "";
		Iterator codes = axisHeader.getTranslations().keySet().iterator();

		while (codes.hasNext())
		{
			lang = (String) codes.next();
			out.println("var name_"+lang+" = \""+Encode.javaStringToJsString(axisHeader.getName(lang))+"\";\n");
			out.println("var desc_"+lang+" = \""+Encode.javaStringToJsString(axisHeader.getDescription(lang))+"\";\n");
		}
	}
	%>

	function showTranslation(lang)
	{
		showFieldTranslation('AxisName', 'name_'+lang);
		showFieldTranslation('AxisDescription', 'desc_'+lang);
	}

	function removeTranslation()
	{
		document.axisForm.submit();
	}
</script>
</HEAD>
<BODY marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF" onload="storeItems(document.axisForm.Order);document.axisForm.Name.focus()">
<%	
   	browseBar.setDomainName(resource.getString("pdcPeas.pdc"));
    browseBar.setComponentName(resource.getString("pdcPeas.pdcDefinition"));

    TabbedPane tabbedPane = gef.getTabbedPane();
	tabbedPane.addTab("Axe", "", true);
	// affichage dans la browsebar du bon message : creation ou modification ainsi que de l'onglet Gestionnaire
	if (formAction.equals("CreateAxis"))
	{
		browseBar.setPath(resource.getString("pdcPeas.createAxis"));
		tabbedPane.addTab("Gestionnaires", "ViewManager", false, false);
	}
	else {
		browseBar.setPath(resource.getString("pdcPeas.editAxis"));
		tabbedPane.addTab("Gestionnaires", "ViewManager", false);
	}

	out.println(window.printBefore());
    out.println(tabbedPane.print());
    out.println(frame.printBefore());
    out.println(board.printBefore());
%>
  <table width="100%" border="0" cellspacing="0" cellpadding="4">
    <form action="<%=formAction%>" name="axisForm" method="post">

    	<%=I18NHelper.getFormLine(resource, axisHeader, translation)%>

		<input type="hidden" name="Id" value="<%=id%>">
	  <% if (errorMessage != null && errorMessage.length() > 0) { %>
		<tr> 
			<td colspan=2 nowrap align=center><%=errorMessage%></td>
		</tr>
	  <% } %>
      <tr> 
        <td width="30%" class="txtlibform" nowrap><%=resource.getString("GML.nom")%>&nbsp;:</td>
        <td nowrap><input type="text" style="text-align:left;" name="Name" id="AxisName" maxlength="25" size="30" value="<%=Encode.javaStringToHtmlString(name)%>" onKeyUP="javascript:highlightItem(document.axisForm.Order,this.value)">&nbsp;<img src="<%=resource.getIcon("pdcPeas.mandatoryField")%>" width="5" align="absmiddle"></td>
      </tr>
      <tr> 
	  <td width="30%" class="txtlibform" valign="top" nowrap><%=resource.getString("pdcPeas.definition")%>&nbsp;:</td>
        <td><TEXTAREA style="width:100%" name="Description" id="AxisDescription" rows="3"><%=Encode.javaStringToHtmlString(description)%></TEXTAREA></td>
     </tr>
      <tr>
        <td class="txtlibform" nowrap><%=resource.getString("GML.type")%>&nbsp;:</td>
        <td nowrap>
          <input type="radio" name="Type" value="P" <%=primaryChecked%> onClick="javascript:changeList('P')"><span class="textePetitBold">&nbsp;<%=resource.getString("pdcPeas.primary")%></span><br>
          <input type="radio" name="Type" value="S" <%=secondaryChecked%> onClick="javascript:changeList('S')"><span class="textePetitBold">&nbsp;<%=resource.getString("pdcPeas.secondary")%></span></td>
      </tr>
      <tr> 
        <td class="txtlibform" valign="top"><%=resource.getString("pdcPeas.position")%>&nbsp;:</td>
        <td class="textePetitBold"><%=resource.getString("pdcPeas.brothersAxis")%>&nbsp;:<br>
			<%
				out.println("<select name=\"Order\" size=\""+new Integer(nbItemShowed).toString()+"\">");
				// test s'il ne s'agit pas du premier axe que l'on créait.
				if (!selectedAxis.isEmpty()){
					// affiche les axes frères
					while (it.hasNext()){
						tempAxisHeader = (AxisHeader)it.next();
						axisName = Encode.javaStringToHtmlString(tempAxisHeader.getName());
						order = (new Integer( tempAxisHeader.getAxisOrder() )).toString();
						if (!(tempAxisHeader.getPK().getId()).equals(id)) {
							out.println("<option value=\""+axisName+sepOptionValueTag+order+"\">"+axisName+"</option>");
						} 
					}
					// calcul le dernier ordre
					int newOrder_tmp = (new Integer(order)).intValue() + 1;
					String newOrder = (new Integer(newOrder_tmp)).toString();
					if (modification == null) // création
						out.println("<option value=\""+newOrder+"\" selected>&lt;"+resource.getString("pdcPeas.EndTag")+"&gt;</option>");
					else
						out.println("<option value=\""+newOrder+"\">&lt;"+resource.getString("pdcPeas.EndTag")+"&gt;</option>");
				} else{
					out.println("<option value=\"0\" selected>&lt;"+resource.getString("pdcPeas.EndTag")+"&gt;</option>");
				}
				out.println("</select>");
			%>
		</td>
      </tr>
      <tr> 
        <td nowrap>( <img src="<%=resource.getIcon("pdcPeas.mandatoryField")%>" width="5" align="absmiddle">&nbsp;: <%=resource.getString("GML.requiredField")%> )</td>
        <td nowrap>&nbsp;</td>
      </tr>
	</form>
  </table> 
  <%
  	out.println(board.printAfter());
  
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:sendData()", false));
	buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:window.close()", false));
    out.println("<center><BR/>"+buttonPane.print()+"</center>");
  %>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
</HTML>