<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkThesaurus.jsp"%>
<%
	Collection vocas = (Collection) request.getAttribute("listVoca");
	ArrayList axis = (ArrayList) request.getAttribute("listAxis");
	Collection terms = (Collection) request.getAttribute("listTerms");
	String idVoca = (String) request.getAttribute("idVoca");
	String idAxis = (String) request.getAttribute("idAxis");
	idAxis = (idVoca == null) ? null : idAxis;
    String showSynonyms = (String) request.getAttribute("showSynonyms");
    Hashtable synonyms = (Hashtable) request.getAttribute("synonyms");
    Iterator itVoca = vocas.iterator();
	Iterator itAxis = axis.iterator();
	Iterator itTerm = terms.iterator();
    String name;
    String id;
    boolean selected;

%>
<HTML>
<HEAD>
<style type="text/css">

.axe:hover {
	font-size: 10px;
	font-weight: normal;
	color: White;
	background-color : navy;
	text-decoration: none;
	border:1 solid  rgb(255,150,0);
}

.axe {
	font-size: 10px;
	font-weight: normal;
	color: navy;
	background-color : White;
	text-decoration: none;
	border:1 solid  rgb(150,150,150);
}

.showIt {
	display: block;
}
.hideIt {
	display: none;
}
.colorRed {
    color:red
}
</style>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
<SCRIPT LANGUAGE="JavaScript">

function Deletes()
{
	if (existVocaSelected())
	{
		if (window.confirm("<%=resource.getString("thesaurus.MessageSuppressionSelectedVoca")%>")) {
      jQuery('#genericForm').attr('action', "DeleteVoca").submit();
		}
	}
	else
		alert("<%=resource.getString("thesaurus.MessageSelectVoca")%>");
}
function Update()
{
	if (existVocaSelected())
    jQuery('#genericForm').attr('action', "UpdateVocaQuery").submit();
	else
		alert("<%=resource.getString("thesaurus.MessageSelectVoca")%>");
}
function ManageSynonyms()
{
	if ((existVocaSelected())&&(existTermSelected()))
		self.location = "ManageSynonyms";
	else
	{
		if (!existVocaSelected())
		{
			if (!existTermSelected())
				alert("<%=resource.getString("thesaurus.MessageSelectVocaTerm")%>");
			else alert("<%=resource.getString("thesaurus.MessageSelectVoca")%>");
		}
		else alert("<%=resource.getString("thesaurus.MessageSelectTerm")%>");

	}
}
function ManageAssignments()
{
	//if (existVocaSelected())
		self.location = "ManageAssignments";
	/*else
		alert("<%=resource.getString("thesaurus.MessageSelectVoca")%>");*/
}
function EditAssignments()
{
	if (existVocaSelected())
		self.location = "EditAssignments";
	else
		alert("<%=resource.getString("thesaurus.MessageSelectVoca")%>");
}
function existTermSelected()
{
	if ((document.forms[0].idTerm.options.selectedIndex != -1)&&(document.forms[0].idTerm.options.selectedIndex != 0))
		return true;
	else return false;
}
function existVocaSelected()
{
	if ((document.forms[0].idVoca.options.selectedIndex != -1)&&(document.forms[0].idVoca.options.selectedIndex != 0))
		return true;
	else return false;
}
function SetVoca()
{
	document.forms[0].action = "SetVoca";
	document.forms[0].submit();
}
function SetAxis()
{
	if (existVocaSelected()) {
        document.forms[0].action = "SetAxis";
        document.forms[0].submit();
    } else {
        alert('<%=resource.getString("thesaurus.MessageSelectVoca")%>');
        document.forms[0].idAxis.options.selectedIndex = 0;
    }

}

function setColorToRed(field){
    var coll = document.all[field.name];
    for (i=0; i< coll.length; i++ )
        if (coll[i].id == 'status_'+field.id)
            coll[i].value= 'unverified';
    field.className = 'colorRed';
}

function showButtonClick()
{

    if (document.all.idAxis.value != '0') {
        document.all.termsCell.className = 'showIt';
    } else {
        alert('<%=resource.getString("thesaurus.MessageSelectAxis")%>');
    }
}

function validateSynonyms(termId)
{
    document.forms[0].action = "validateSynonyms";
    document.forms[0].termId.value = termId;
    document.forms[0].submit();
}

</SCRIPT>
</HEAD>
<BODY  marginheight="5" marginwidth="5" leftmargin="5" topmargin="5" bgcolor="#FFFFFF">
<%
	browseBar.setComponentName(componentLabel, "Main");
	browseBar.setPath(resource.getString("thesaurus.thesaurus"));

	operationPane.addOperation(resource.getIcon("thesaurus.OPcreateVoc"),
	resource.getString("thesaurus.OPcreateVoc"), "CreateVocaQuery");
	//operationPane.addLine();
	operationPane.addOperation(resource.getIcon("thesaurus.OPeditVoc"),
	resource.getString("thesaurus.OPeditVoc"), "javascript:Update();");
	//operationPane.addLine();
	operationPane.addOperation(resource.getIcon("thesaurus.OPdeleteVoc"),
	resource.getString("thesaurus.OPdeleteVoc"), "javascript:Deletes();");
	operationPane.addLine();
	operationPane.addOperation(resource.getIcon("thesaurus.OPmanageAffectations"),
	resource.getString("thesaurus.OPmanageAffectations"), "javascript:ManageAssignments();");
	//operationPane.addLine();
	operationPane.addOperation(resource.getIcon("thesaurus.OPaffectVoc_to_users"),
	resource.getString("thesaurus.OPaffectVoc_to_users"), "javascript:EditAssignments();");
    out.println(window.printBefore());
	out.println(frame.printBefore());
%>

<% // Ici debute le code de la page %>
<center><br><br>
<FORM METHOD=POST ACTION="">
<input type="hidden" name="termId">
<table width="100%" border="0" cellspacing="10" cellpadding="0">
<tr>
   <td width="100%">
    <table width="98%" border="0" cellspacing="0" cellpadding="1">
        <tr>
          <td>
            <table cellpadding="0" cellspacing="0" border="0" width="50%" class="line">
              <tr>
                <td>
                  <table cellpadding="2" cellspacing="1" border="0" width="100%" >
                     <tr>
                        <td class=intfdcolor align=center nowrap width="100%" height="24">
                            &nbsp;<span class="txtnav"><%=resource.getString("thesaurus.vocabulaire")%> :&nbsp;</span>
                          <span class=selectNS>
                            <select name="idVoca" onChange="javascript:SetVoca();">
                                <option value="0" selected><%=resource.getString("GML.select")%></option>
                                <%
                                Vocabulary voca;
                                while (itVoca.hasNext()){
                                    voca = (Vocabulary) itVoca.next();
                                    name = Encode.javaStringToHtmlString(voca.getName());
                                    id = voca.getPK().getId();
                                    selected = false;
                                    if (id.equals(idVoca))
                                        selected = true;
                                %>  <option value="<%=id%>" <%
                                    if (selected)
                                        out.println(" selected");
                                    %> ><%=name%></option>
                                <%
                                }
                                %>
                             </select></span>
                        </td>
                      </tr>
                  </table>
                </td>
              </tr>
            </table>
          </td>


          <td>
            <table cellpadding="0" cellspacing="0" border="0" width="50%" class="line">
              <tr>
                <td>
                  <table cellpadding="2" cellspacing="1" border="0" width="100%" >
                    <tr>
                      <td class="intfdcolor" align="center" nowrap width="100%" height="24">
                                &nbsp;<span class="txtnav"><%=resource.getString("thesaurus.axe")%> :&nbsp;</span>
                                <span class=selectNS>
                                <select name="idAxis" onChange="javascript:SetAxis();">
                                    <option value="0" selected><%=resource.getString("GML.select")%></option>
                                    <%
                                        AxisHeader ax;
                                        while (itAxis.hasNext())  {
                                            ax = (AxisHeader)itAxis.next();
                                            name = Encode.javaStringToHtmlString(ax.getName());
                                            id = ax.getPK().getId();
                                            selected = false;
                                            if (id.equals(idAxis))
                                                selected = true;
                                        %>
                                            <option value="<%=id%>"
                                            <%
                                            if (selected)
                                                out.println(" selected");
                                            %>
                                            ><%=name%></option>
                                        <%
                                        }
                                    %>
                              </select></span>
                  </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
          </td>
          <td id="showButtonCell">
            <table cellpadding="0" cellspacing="0" border="0" width="50%" class="line">
              <tr>
                <td>
                  <table cellpadding="2" cellspacing="1" border="0" width="100%" >
                    <tr>
                      <td class="intfdcolor" align="center" nowrap width="100%" height="24">
                        <%
                            ButtonPane buttonPane = gef.getButtonPane();
                            Button showButton = (Button) gef.getFormButton(resource.getString("thesaurus.show"), "javascript:onClick=showButtonClick()", false);
                            buttonPane.addButton(showButton);
                            buttonPane.setHorizontalPosition();
                            out.println(buttonPane.print());
                        %>
                      </td>
                    </tr>
                  </table>
                </td>
              </tr>
            </table>
          </td>
          <td width="100%">
           &nbsp;
          </td>
        </tr>

      </table>
    </td>
   </tr>
   <tr>
   <% if ("yes".equals(showSynonyms)) { %>
        <td id="termsCell" class='showIt' marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 >
   <% } else { %>
        <td id="termsCell" class='hideIt' marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 >
    <% } %>
    <%=boardStart%>
    <table align="left" border=0 cellpadding=1 cellspacing=0 height="100%">
        <%
            int i;
            int j;
            Button validateButton;
            Value value;
            String  valueName;
            String  valueId;
            int valueLevel;
            String increment;
            StringBuffer synonymsFields;
            Collection names;
            Iterator itNames;
            String[] strNames;
            String status;
            boolean isChild;
            String parentId;
            Hashtable hSynonym;

            while (idAxis!= null && itTerm!= null && itTerm.hasNext()) {
              value		= (Value) itTerm.next();
              valueName	= value.getName();
              valueId		= value.getValuePK().getId();
              valueLevel	= value.getLevelNumber();
              increment	= "";
              synonymsFields = new StringBuffer(openBorder);
              names = (Collection) synonyms.get(valueId);
              itNames = names.iterator();
              strNames = new String[maxSyn];
              status = "";
              for (i = 0; i < strNames.length; i++) {
                  if (itNames.hasNext()) {
                   hSynonym = (Hashtable) itNames.next();
                   status = (String) hSynonym.get("status");
                   synonymsFields.append("<input type=\"hidden\" id=\"status_field"+valueId+"_"+i+"\" name=\"field"+valueId+"\" value=\""+status+"\">");
                   name = Encode.javaStringToHtmlString((String)hSynonym.get("name"));
                    if ("verified".equals(status) && !"".equals(name)) {
                        synonymsFields.append("<input type=\"text\" onchange=\"setColorToRed(this)\" id=\"field"+valueId+"_"+i+"\" name=\"field"+valueId+"\" value=\""+name+"\" size=\"12\">&nbsp;\n");
                    } else {
                       synonymsFields.append("<input type=\"text\" class=\"colorRed\" id=\"field"+valueId+"_"+i+"\" name=\"field"+valueId+"\" value=\""+name+"\" size=\"12\">&nbsp;\n");
                    }

                }  else {
                    synonymsFields.append("<input type=\"hidden\" id=\"status_field"+valueId+"_"+i+"\" name=\"field"+valueId+"\" value=\"unferified\">");
                    synonymsFields.append("<input type=\"text\" class=\"colorRed\" id=\"field"+valueId+"_"+i+"\" name=\"field"+valueId+"\" value=\"\" size=\"12\">&nbsp;\n");
                }
              }
              synonymsFields.append(closeBorder);

              isChild = false;
              parentId = "";

               out.println("<tr>");
               out.println("<td background=\""+m_context+"/pdcPeas/jsp/icons/quadrillage.gif\" width=\"50%\">");
               for (j = 0; j < valueLevel; j++) {
                    increment += "<img src=\""+m_context+"/util/icons/shim.gif\" width=\"18\" align=\"absmiddle\">";
               }

               if (valueLevel == 0) {
                    //this is the root
                   isChild		= false;
                   //cntParent	= 0;
                   out.print("<p><img ");
                   out.println(" src=\""+m_context+"/pdcPeas/jsp/icons/pdcPeas_target.gif\" width=\"15\" align=\"absmiddle\"><a id=\"a\" class=\"axe\">&nbsp;&nbsp;&nbsp;&nbsp;"+Encode.javaStringToHtmlString(valueName)+"&nbsp;&nbsp;&nbsp;</a><img src=\""+m_context+"/util/icons/shim.gif\" width=\"18\" align=\"absmiddle\">");
                   out.println("</td><td>");
                   out.println(synonymsFields.toString());
                   out.println("</td>");
                   out.println("<td>"+openBorder+"<a class=\"intfdcolor\" href=\"javascript:validateSynonyms('"+valueId+"')\" ><img border=0 src=\""+m_context+"/util/icons/ok.gif\" alt=\"" + resource.getString("thesaurus.validateSynonyms") + "\" align=\"absmiddle\"></a>"+closeBorder+"</td>");
                   out.println("</tr>");
               } else {
                        //there is a child value
                       isChild	= true;
                       parentId = valueId;
                       //cntParent++;
                       out.print(increment+"<img id=\"but\"");
                       out.println(" src=\""+m_context+"/pdcPeas/jsp/icons/pdcPeas_target.gif\" width=\"15\" align=\"absmiddle\"><a id=\"a\" class=\"axe\">&nbsp;&nbsp;&nbsp;&nbsp;"+Encode.javaStringToHtmlString(valueName)+"&nbsp;&nbsp;&nbsp;</a><img src=\""+m_context+"/util/icons/shim.gif\" width=\"18\" align=\"absmiddle\">");
                       out.println("</td><td>");
                       out.println("   " + synonymsFields.toString());
                       out.println("</td>");
                       out.println("<td>"+openBorder+"<a class=\"intfdcolor\" href=\"javascript:validateSynonyms('"+valueId+"')\" ><img border=0 src=\""+m_context+"/util/icons/ok.gif\" alt=\"" + resource.getString("thesaurus.validateSynonyms") + "\" align=\"absmiddle\"></a>"+closeBorder+"</td>");
                        out.println("</tr>");
                                 //there is no child values
                        if (isChild && !value.getMotherId().equals(parentId)) {
                            isChild = false;
                        }
                      }
               }
        %>
        </table>
        <%=boardEnd%>
      </td>
     </tr>
    </table>
</FORM>
<br><br></center>

<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
<form id="genericForm" action="" method="POST"></form>
</BODY>
</HTML>
