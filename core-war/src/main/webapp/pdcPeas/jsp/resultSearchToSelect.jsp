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
<%@ include file="checkAdvancedSearch.jsp"%>

<%
String[][] results = (String[][]) request.getAttribute("ResultArray");
List alSilverContents = (List) request.getAttribute("ResultList");
String pertinencePDC = (String) request.getAttribute("PertinencePDC");

// recuperation du choix de l'utilisateur
String showSndSearchAxis = (String) request.getAttribute("ShowSndSearchAxis");
String theSpace = (String) request.getAttribute("SpaceSelected");
String theComponent = (String) request.getAttribute("ComponentSelected");
String theQuery = (String) request.getAttribute("theQuery");
theQuery = EncodeHelper.javaStringToHtmlString(theQuery);

String theAuthor = (String) request.getAttribute("theAuthor");
String theAfterDate = (String) request.getAttribute("theAfterDate");
String theBeforeDate = (String) request.getAttribute("theBeforeDate");

// pour la recherche PDC pure
// Contenu
String sName = null;
String sDescription = null;
String sURL = null;
String sLocation = "";
String sId			= null;
String sInstanceId	= null;

//pour le thesaurus
Map synonyms = (Map) request.getAttribute("synonyms");
// pour la navigation. Jamais null
String minS = (String) request.getAttribute("Min");
String maxS = (String) request.getAttribute("Max");
String previous = (String) request.getAttribute("previousAuthorized");
String follow = (String) request.getAttribute("followAuthorized");
String totalResultLen = (String) request.getAttribute("TotalResultLen");

Boolean activeSelection = (Boolean) request.getAttribute("ActiveSelection");
if (activeSelection == null) {
	activeSelection = new Boolean(false);
}

int min = (new Integer(minS)).intValue() - 1;
int max = (new Integer(maxS)).intValue();
if (showSndSearchAxis == null)
	showSndSearchAxis = "NO";
if (theSpace == null){
	theSpace = "";
}
if (theComponent == null){
	theComponent = "";
}
if (theQuery == null){
	theQuery = "";
}
if (theAuthor == null){
	theAuthor = "";
}
if (theAfterDate == null){
	theAfterDate = "";
}
if (theBeforeDate == null){
	theBeforeDate = "";
}
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel withCheckFormScript="true"/>
<script type="text/javascript" src="<%=m_context%>/pdcPeas/jsp/javascript/formUtil.js"></script>

<script language="javascript">
	function goBack(){
		document.AdvancedSearch.submit();
	}

	function displayPrevious(skip) {
		document.AdvancedSearch.skip.value = skip;
		document.AdvancedSearch.action = "ViewOtherResults";
		document.AdvancedSearch.submit();
	}

	function displayNext(skip) {
		document.AdvancedSearch.skip.value = skip;
		document.AdvancedSearch.action = "ViewOtherResults";
		document.AdvancedSearch.submit();
	}

	// this function get all checked boxes by the user and sent
	// data to the router
	function getSelectedOjectsFromResultList(){
		var boxItems = document.AdvancedSearch.resultObjects;
		var  selectItems = "";
		if (boxItems != null){
			// au moins une checkbox exist
			var nbBox = boxItems.length;
			if ( (nbBox == null) && (boxItems.checked == true) ){
				// il n'y a qu'une checkbox selectionn�e
				selectItems += boxItems.value;
			} else{
				// search checked boxes
				for (i=0;i<boxItems.length ;i++ ){
					if (boxItems[i].checked == true){
						selectItems += boxItems[i].value+",";
					}
				}
				selectItems = selectItems.substring(0,selectItems.length-1); // erase the last coma
			}
			if ( selectItems.length > 0) {
				// an axis has been selected !
				document.AdvancedSearch.Ids.value = selectItems;
				document.AdvancedSearch.action = "ValidateSelectedObjects";
				document.AdvancedSearch.submit();
			}
		}
	}

</script>

</HEAD>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
	browseBar.setComponentName(resource.getString("pdcPeas.ResultPage"));

	if (activeSelection.booleanValue())
		operationPane.addOperation(resource.getIcon("pdcPeas.folder_to_valid"), resource.getString("pdcPeas.tracker_to_select"), "javascript:getSelectedOjectsFromResultList()");

    out.println(window.printBefore());
    out.println(frame.printBefore());

%>
<form name="AdvancedSearch" action="ViewAdvancedSearch" method="post">
<input type="hidden" name="query" value="<%=theQuery%>">
<input type="hidden" name="spaces" value="<%=theSpace%>">
<input type="hidden" name="componentSearch" value="<%=theComponent%>">
<input type="hidden" name="authorSearch" value="<%=theAuthor%>">
<input type="hidden" name="afterdate" value="<%=theAfterDate%>">
<input type="hidden" name="beforedate" value="<%=theBeforeDate%>">
<input type="hidden" name="beforedate" value="<%=theBeforeDate%>">
<input type="hidden" name="ShowSndSearchAxis" value="<%=showSndSearchAxis%>">
<input type="hidden" name="skip">
<input type="hidden" name="Ids" value="">

<center><table width="98%" border="0" cellspacing="0" cellpadding="0" class=intfdcolor4><!--tablcontour-->
        <tr>
          <td>
		<table border="0" cellspacing="0" cellpadding="5" class="contourintfdcolor" width="100%"><!--tabl1-->
              <tr>
                <td align="center">
                  <table border="0" cellspacing="0" cellpadding="5" class="intfdcolor4" width="100%">
				    <tr>
					  <td align="center">
     <%
  int skip = 0;
	if ( totalResultLen.equals("0") ){
		out.println("<SPAN class='matchingWord'>"+resource.getString("pdcPeas.SearchFind")+" "+theQuery+"</SPAN>");
		out.println("&nbsp;&nbsp;-&nbsp;&nbsp;");
		out.println("<span class=textePetitBold>"+resource.getString("pdcPeas.NoResult")+"</span>");
		out.println("<hr size=1 width=\"100%\" color=AFB8C9><br>");
		Set synonymsSet = synonyms.entrySet();
		Iterator it = synonymsSet.iterator();
		while (it.hasNext())
		{
			Entry entry = (Map.Entry) it.next();
			String key = (String) entry.getKey();
			Collection values = (Collection) entry.getValue();
			Iterator itVal = values.iterator();
			if (itVal.hasNext())
			{
				out.print(resource.getString("pdcPeas.synSearchExtension") + " " + key + " : ");
				String value = (String) itVal.next();
				out.print(value);
			}
			while (itVal.hasNext())
			{
				String value = (String) itVal.next();
				out.print(", " + value);
			}
			out.println("<BR>");
		}
		out.println("<BR>");

	}
	else if (alSilverContents == null){

		out.println("<SPAN class='matchingWord'>"+resource.getString("pdcPeas.SearchFind")+" "+theQuery+"</SPAN>");
		out.println("&nbsp;&nbsp;-&nbsp;&nbsp;");
		if (results.length == 1){
			out.println("<span class=textePetitBold>"+resource.getString("pdcPeas.OneResult")+"</span>");
			out.println("<hr size=1 width=\"100%\" color=AFB8C9><br>");
		} else if (results.length <= 10){
			out.println("<span class=textePetitBold>"+totalResultLen+" "+resource.getString("pdcPeas.OneResultPage")+"</span>");
			out.println("<hr size=1 width=\"100%\" color=AFB8C9><br>");
		} else{
			if (minS.equals(maxS)){
				out.println("<span class=textePetitBold>"+minS+ " / "+totalResultLen+" "+resource.getString("pdcPeas.ManyResultPages")+"</span>");
			} else {
				out.println("<span class=textePetitBold>"+minS+" - "+maxS+ " / "+totalResultLen+" "+resource.getString("pdcPeas.ManyResultPages")+"</span>");
			}
			out.println("<hr size=1 width=\"100%\" color=AFB8C9><br>");
		 }
		Set synonymsSet = synonyms.entrySet();
		Iterator it = synonymsSet.iterator();
		while (it.hasNext())
		{
			Entry entry = (Map.Entry) it.next();
			String key = (String) entry.getKey();
			Collection values = (Collection) entry.getValue();
			Iterator itVal = values.iterator();
			if (itVal.hasNext())
			{
				out.print(resource.getString("pdcPeas.synSearchExtension") + " " + key + " : ");
				String value = (String) itVal.next();
				out.print(value);
			}
			while (itVal.hasNext())
			{
				String value = (String) itVal.next();
				out.print(", " + value);
			}
			out.println("<BR>");
		}
		out.println("<BR>");

		ArrayPane pane = gef.getArrayPane("PdcPeas", "", request, session);
		pane.addArrayColumn(resource.getString("pdcPeas.Score"));
		pane.addArrayColumn(resource.getString("pdcPeas.Document"));
		pane.addArrayColumn(resource.getString("pdcPeas.Domain"));
		pane.addArrayColumn(resource.getString("pdcPeas.Preview"));
		pane.setSortable(false);
		pane.setVisibleLineNumber(-1);
		ArrayLine line = null;
		NumberFormat percent = NumberFormat.getPercentInstance();
		String resultType = "";
		String buff = "";
		String stars = "";
		//for (int i=0; i<results.length ; i++){
		for (int i=min; i<max ; i++){
			line = pane.addArrayLine();
			// affiche la pertinence
			StringTokenizer st = new StringTokenizer(results[i][0]);
			while (st.hasMoreTokens()){
				stars += "<img src="+m_context+"/pdcPeas/jsp/icons/"+st.nextToken()+">";
			}
			line.addArrayCellText(stars);
			stars = "";

			// affiche le titre
			line.addArrayCellLink(EncodeHelper.javaStringToHtmlString(results[i][1]),results[i][2]);

			// affiche l'emplacement
			line.addArrayCellText(EncodeHelper.javaStringToHtmlString(results[i][3]));

			// affiche l'extrait du document
			line.addArrayCellText(results[i][4]);
		}
		out.println(pane.print());
	} else if ( (alSilverContents != null) && (alSilverContents.size()>0) ){

		if (alSilverContents.size() == 1){
			out.println("<span class=textePetitBold>"+resource.getString("pdcPeas.OneResult")+"</span>");
			out.println("<hr size=1 width=\"100%\" color=AFB8C9><br>");
		} else if (alSilverContents.size() <= 10){
			out.println("<span class=textePetitBold>"+totalResultLen+" "+resource.getString("pdcPeas.OneResultPage")+"</span>");
			out.println("<hr size=1 width=\"100%\" color=AFB8C9><br>");
		} else{
			if (minS.equals(maxS)){
				out.println("<span class=textePetitBold>"+minS+ " / "+totalResultLen+" "+resource.getString("pdcPeas.ManyResultPages")+"</span>");
			} else {
				out.println("<span class=textePetitBold>"+minS+" - "+maxS+ " / "+totalResultLen+" "+resource.getString("pdcPeas.ManyResultPages")+"</span>");
			}
			out.println("<hr size=1 width=\"100%\" color=AFB8C9><br>");
		 }

		Set synonymsSet = synonyms.entrySet();
		Iterator it = synonymsSet.iterator();
		while (it.hasNext())
		{
			Entry entry = (Map.Entry) it.next();
			String key = (String) entry.getKey();
			Collection values = (Collection) entry.getValue();
			Iterator itVal = values.iterator();
			if (itVal.hasNext())
			{
				out.print(resource.getString("pdcPeas.synSearchExtension") + " " + key + " : ");
				String value = (String) itVal.next();
				out.print(value);
			}
			while (itVal.hasNext())
			{
				String value = (String) itVal.next();
				out.print(", " + value);
			}
			out.println("<BR>");
		}
		out.println("<BR>");

		ArrayLine arrayLine = null;
		ArrayPane pane = gef.getArrayPane("PdcPeas", "", request, session);
		if (!activeSelection.booleanValue())
			pane.addArrayColumn(resource.getString("pdcPeas.Score"));
		pane.addArrayColumn(resource.getString("pdcPeas.Document"));
		pane.addArrayColumn(resource.getString("pdcPeas.Domain"));
		pane.addArrayColumn(resource.getString("pdcPeas.Preview"));
		if (activeSelection.booleanValue())
			pane.addArrayColumn("");
		pane.setSortable(false);
		pane.setVisibleLineNumber(-1);

	List alSilverContentIds = (List)request.getAttribute("SilverContentId");
	for(int nI=min; nI < max; nI++){
		arrayLine = pane.addArrayLine();
		sName = ((GlobalSilverContent)alSilverContents.get(nI)).getName();
		sDescription = ((GlobalSilverContent)alSilverContents.get(nI)).getDescription();
		sURL = ((GlobalSilverContent)alSilverContents.get(nI)).getURL();
		sLocation = ((GlobalSilverContent)alSilverContents.get(nI)).getLocation();
		sId				= ((GlobalSilverContent)alSilverContents.get(nI)).getId();
		sInstanceId		= ((GlobalSilverContent)alSilverContents.get(nI)).getInstanceId();

		// Encode the ? with %3f
		String sBuf = sURL;
		int nIndex = sURL.indexOf("?");
		if(nIndex != -1)		{
				sBuf = sURL.substring(0, nIndex) + "%3f" + sURL.substring(nIndex+1, sURL.length());
				sURL = sBuf;
		}
		// Encode the = with %3d
		sBuf = sURL;
		nIndex = sURL.indexOf("=");
		while(nIndex != -1){
				sBuf = sURL.substring(0, nIndex) + "%3d" + sURL.substring(nIndex+1, sURL.length());
				sURL = sBuf;
				nIndex = sURL.indexOf("=");
		}

		String stars = "";
		StringTokenizer st = new StringTokenizer(pertinencePDC);
		while (st.hasMoreTokens()){
			stars += "<img src="+m_context+"/pdcPeas/jsp/icons/"+st.nextToken()+">";
		}
		if (!activeSelection.booleanValue())
			arrayLine.addArrayCellText(stars);
		stars = "";
		String contentid = ((Integer)alSilverContentIds.get(nI)).toString();
		if (activeSelection.booleanValue())
			arrayLine.addArrayCellText("<a href=\"GlobalContentForward?contentURL="+sURL+"&contentId="+contentid+"\" target=_blank><span class=textePetitBold>"+EncodeHelper.javaStringToHtmlString(sName)+"</span></a>");
		else
			arrayLine.addArrayCellLink("<span class=textePetitBold>"+EncodeHelper.javaStringToHtmlString(sName)+"</span>","GlobalContentForward?contentURL="+sURL+"&contentId="+contentid);
	    arrayLine.addArrayCellText(EncodeHelper.javaStringToHtmlString(sLocation));
	    arrayLine.addArrayCellText(EncodeHelper.javaStringToHtmlString(sDescription));
		if (activeSelection.booleanValue())
			arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"resultObjects\" value=\""+sId+"-"+sInstanceId+"\">");

	}
	out.println(pane.print());
}
		  %>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </table>
	</td>
  </tr>
</table>
</form>
<%

ButtonPane buttonPane = gef.getButtonPane();
Button backButton = (Button) gef.getFormButton(resource.getString("GML.back"), "javascript:goBack()", false);
Button previousButton = null;
Button nextButton = null;

buttonPane.addButton(backButton);
if (previous.equals("yes")) {
    previousButton = (Button) gef.getFormButton(resource.getString("pdcPeas.PreviousResults"), "javascript:onClick=displayPrevious('-1')", false);
    buttonPane.addButton(previousButton);
  }
if (follow.equals("yes")) {
    nextButton = (Button) gef.getFormButton(resource.getString("pdcPeas.NextResults"), "javascript:onClick=displayNext('+1')", false);
    buttonPane.addButton(nextButton);
  }
buttonPane.setHorizontalPosition();
out.println(buttonPane.print());


	out.println(frame.printAfter());
		out.println(window.printAfter());
%>
</center>
</body>
</html>
