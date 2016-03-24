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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>


<%@ page import="org.silverpeas.core.util.LocalizationBundle" %>
<%@ page import="org.silverpeas.core.util.ResourceLocator" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.Encode" %>

<%
    String code = (String) request.getParameter("Code");

  LocalizationBundle scc =
      ResourceLocator.getLocalizationBundle("org.silverpeas.form.multilang.formBundle",
          request.getLocale().getLanguage());
    String iconsPath = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
	String iconsPathWysiwyg =iconsPath+"/util/icons/wysiwyg/";
	//icones
	String pxSrc = iconsPath + "/util/icons/colorPix/1px.gif";
%>



<HTML>
<HEAD>
<view:script src="/util/javaScript/checkForm.js"/>


<TITLE><%=scc.getString("GML.popupTitle")%></TITLE>

<META http-equiv=Content-Type content="text/html; charset=windows-1252">
<STYLE>

SELECT {
	BACKGROUND: #eeeeff; FONT: 8pt verdana,arial,sans-serif
}
.toolbar {
	BORDER-RIGHT: black 1px solid; BORDER-TOP: black 1px solid; BACKGROUND: lightgrey; MARGIN-BOTTOM: 3pt; OVERFLOW: hidden; BORDER-LEFT: black 1px solid; BORDER-BOTTOM: black 1px solid; HEIGHT: 28px
}
.mode LABEL {
	FONT: bold 10pt verdana,geneva,arial,sans-serif
}
.mode .current {
	COLOR: darkgreen
}
.heading {
	BACKGROUND: lightgrey; COLOR: navy
}
</STYLE>

<SCRIPT>

var bLoad=false,public_description=new Editor

/***********************************/
function Editor() {
this.put_html=put_html;
this.get_html=get_html;
this.testHTML=testHTML
this.bReady = false
}

/***********************************/
function cleanupHTML() {
	var i=0
  bodyTags=idEdit.document.body.all, i
  for (i=bodyTags.tags("FONT").length-1;i >= 0;i--)
	if (bodyTags.tags("FONT")[i].style.backgroundColor="#ffffff") {
		bodyTags.tags("FONT")[i].style.backgroundColor=""
		if (bodyTags.tags("FONT")[i].outerHTML.substring(0,6)=="<FONT>")
			bodyTags.tags("FONT")[i].outerHTML=bodyTags.tags("FONT")[i].innerHTML
	}
}


/***********************************/
function testHTML(bAllowHead,extras) {
  mW.click()
  var badStuff=new Array("IFRAME","SCRIPT","LAYER","ILAYER","OBJECT","APPLET","EMBED","FORM","INPUT","BUTTON","TEXTAREA"),headStuff=new Array("HTML","BODY","TITLE","BASE","LINK","META","STYLE"),hasStuff=new Array(),bodyTags=idEdit.document.body.all,i=0
  for (i=0;i<badStuff.length;i++)
    if (bodyTags.tags(badStuff[i]).length>0)
      hasStuff[hasStuff.length]=badStuff[i]
  if (!bAllowHead)
    for (i=0;i<headStuff.length;i++)
      if (bodyTags.tags(headStuff[i]).length>0)
        hasStuff[hasStuff.length]=headStuff[i]
  if (extras!=null)
    for (i=0;i<extras.length;i++)
      if (bodyTags.tags(extras[i]).length>0)
        hasStuff[hasStuff.length]=extras[i]
  var str=""
  if (hasStuff.length>0) {
    str="<%=scc.getString("RemoveHtmlTags")%>"
    for (i=0;i<hasStuff.length;i++)
       str+="\n "+hasStuff[i]
    str+= "\n<%=scc.getString("RememberHtml")%>\n<%=scc.getString("Brackets")%>"
    setTimeout("mH.click()",0)
  }
  return get_html()
}

/***********************************/
function get_html() {
if (bMode) {
 cleanupHTML()
 return idEdit.document.body.innerHTML
}
else
return idEdit.document.body.innerText;
}

/***********************************/
function put_html(sVal) {
if (bMode)
idEdit.document.body.innerHTML=sVal
else
idEdit.document.body.innerText=sVal
}

/**************************************/
<%
if (code.equals("")) {
%>
  var sHeader="<BODY STYLE=\"font:10pt geneva,arial,sans-serif\">",bMode=true,sel=null
<%
} else {
out.println("var theCode=\""+Encode.javaStringToJsString(code)+"\"");
%>
    var sHeader=theCode,bMode=true,sel=null
<%
}
%>

/***********************************/

function displayError() {alert("<%=scc.getString("FormattingToolbar")%>");idEdit.focus()}

/***********************************/

function format(what,opt) {
 if (!bMode) {
   displayError()
   return
 }
 if (opt=="removeFormat"){
   what=opt;opt=null
 }
 if (opt=="CustomFont")
	opt = prompt("<%=scc.getString("FormatText")%>","Geneva, Arial, Sans-Serif")
 if ((opt=="") && (what=="forecolor"))
	opt = prompt("<%=scc.getString("FormatColor")%>","Black")
 if (bMode) {
   if (opt==null)
     idEdit.document.execCommand(what)
   else
     idEdit.document.execCommand(what,"",opt)
   var s=idEdit.document.selection.createRange()/*,p=s.parentElement() */
   idEdit.focus()
 }
 sel=null
}


/***********************************/
function getEl(sTag,start) {
  while ((start!=null) && (start.tagName!=sTag))
    start = start.parentElement
  return start
}

/***********************************/
function createLink() {
 if (!bMode) {
   displayError()
   return
 }
 var isA = getEl("A",idEdit.document.selection.createRange().parentElement())
 var str = prompt("<%=scc.getString("URL")%>",isA ? isA.href : "http:\/\/")

 if (str!=null) {
  if ((str.substring(0, 7) != "http:\/\/")  && (str.substring(0, 6) != "ftp:\/\/")) {
      alert("<%=scc.getString("LienURL")%>")
      idEdit.focus()
  }
  else {
     if ((idEdit.document.selection.type=="None") && (!isA)) {
       idEdit.focus()
       var sel=idEdit.document.selection.createRange()
       sel.pasteHTML("<A HREF="+"\""+str+"\">"+str+"</A> ")
       sel.select()
     }
     else
       format("CreateLink",str)
   }
  }

 else {
   idEdit.focus()
 }
}

/***********************************/
function setMode(bNewMode) {
 if (bNewMode!=bMode) {
  if (bNewMode) {
   var sContents=idEdit.document.body.innerText
   idEdit.document.open()
   idEdit.document.write(sHeader)
   idEdit.document.close()
   idEdit.document.body.innerHTML=sContents
  }
  else {
   cleanupHTML()
   var sContents=idEdit.document.body.innerHTML
   idEdit.document.open()
   idEdit.document.write("<BODY style=\"font:10pt courier, monospace\">")
   idEdit.document.close()
   idEdit.document.body.innerText=sContents
  }
  bMode=bNewMode
  for (var i=0;i<htmlOnly.children.length;i++)
   htmlOnly.children[i].disabled=(!bMode)
 }
 modeA.className=bMode?"current":"";modeB.className=bMode?"":"current"
 idEdit.focus()
}

/***********************************/
function MM_findObj(n, d) { //v3.0
  var p,i,x;  if(!d) d=document; if((p=n.indexOf("?"))>0&&parent.frames.length) {
    d=parent.frames[n.substring(p+1)].document; n=n.substring(0,p);}
  if(!(x=d[n])&&d.all) x=d.all[n]; for (i=0;!x&&i<d.forms.length;i++) x=d.forms[i][n];
  for(i=0;!x&&d.layers&&i<d.layers.length;i++) x=MM_findObj(n,d.layers[i].document); return x;
}

/***********************************/
/* Functions that swaps images. */
function MM_swapImage() { //v3.0
  var i,j=0,x,a=MM_swapImage.arguments; document.MM_sr=new Array; for(i=0;i<(a.length-2);i+=3)
   if ((x=MM_findObj(a[i]))!=null){document.MM_sr[j++]=x; if(!x.oSrc) x.oSrc=x.src; x.src=a[i+2];}
}

/***********************************/
function MM_swapImgRestore() { //v3.0
  var i,x,a=document.MM_sr; for(i=0;a&&i<a.length&&(x=a[i])&&x.oSrc;i++) x.src=x.oSrc;
}


</SCRIPT>

<SCRIPT event=DocumentComplete() for=EditCtrl>
/*if (!bLoad) {
		setTimeout("initEditor(true)",0)
		idBox.style.visibility=''
		idBox.focus()
}*/
bLoad=true
</SCRIPT>


<META content="Microsoft FrontPage 4.0" name=GENERATOR>
</HEAD>

<BODY onselectstart="return false"
style="PADDING-RIGHT: 2pt; PADDING-LEFT: 2pt; PADDING-BOTTOM: 2pt; MARGIN: 0pt; CURSOR: default; PADDING-TOP: 2pt"
onload=doLoad()>


<CENTER>
<DIV id=idBox style="VISIBILITY: hidden; WIDTH: 98%; TEXT-ALIGN: center">
<table width="100%" border="0" cellspacing="0" cellpadding="1" bgcolor="#000000">
  <tr>
    <td>
      <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr>
          <td bgcolor="#ffffff" colspan="2"><img src="<%=iconsPath%>1px.gif" width="1" height="1"></td>
        </tr>
        <tr>
				  <td bgcolor="#ADBACE"><img src="<%=iconsPath%>1px.gif" width="1" height="30"></td>
		      <td bgcolor="#ADBACE" width="100%" valign="middle" nowrap>
					<div align="center">
						<table border="0" cellpadding="0" cellspacing="0">
							<tr>
								<td id=htmlOnly valign=center noWrap>

										<SELECT onchange="format('formatBlock',this[this.selectedIndex].value); this.selectedIndex=0">
						        <OPTION class=heading selected><%=scc.getString("Paragraph")%>
						        <OPTION value="<P>"><%=scc.getString("Normal")%>
						        <OPTION value="<H1>"><%=scc.getString("Heading1")%>
						        <OPTION value="<H2>"><%=scc.getString("Heading2")%>
						        <OPTION value="<H3>"><%=scc.getString("Heading3")%>
						        <OPTION value="<H4>"><%=scc.getString("Heading4")%>
						        <OPTION value="<H5>"><%=scc.getString("Heading5")%>
						        <OPTION value="<H6>"><%=scc.getString("Heading6")%>
						        <OPTION value="<PRE>"><%=scc.getString("Pre")%>
						        <!--<OPTION style="COLOR: darkred" value=removeFormat><%=scc.getString("ClearFormatting")%>-->
						       </OPTION></SELECT>&nbsp;&nbsp;

						      <SELECT onchange="format('fontname',this[this.selectedIndex].value); this.selectedIndex=0">
						        <OPTION class=heading selected><%=scc.getString("Font")%>
						        <OPTION value=geneva,arial,sans-serif>Arial
						        <OPTION value=verdana,geneva,arial,sans-serif>Verdana
						        <OPTION value=times,serif>Times
						        <OPTION value="courier, monospace">Courier
						        <OPTION style="COLOR: navy" value=CustomFont><%=scc.getString("Other")%>
						       </OPTION></SELECT>&nbsp;&nbsp;

						      <SELECT onchange="format('fontSize',this[this.selectedIndex].value); this.selectedIndex=0">
						        <OPTION class=heading selected><%=scc.getString("GML.size")%>
						        <OPTION value=1>8<OPTION value=2>10<OPTION value=3>12<OPTION value=4>14<OPTION value=5>18<OPTION value=6>24<OPTION value=7>36
						      </OPTION></SELECT>&nbsp;&nbsp;

						      <SELECT onchange="format('forecolor',this[this.selectedIndex].style.color); this.selectedIndex=0">
						        <OPTION class=heading selected><%=scc.getString("Color")%>
                      <OPTION style="COLOR: black"><%=scc.getString("Black")%></OPTION>
                      <OPTION style="COLOR: #800000"><%=scc.getString("Brown")%></OPTION>
                      <OPTION style="COLOR: darkgreen"><%=scc.getString("DarkGreen")%></OPTION>
                      <OPTION style="COLOR: #008000"><%=scc.getString("Green")%></OPTION>
                      <OPTION style="COLOR: #808000"><%=scc.getString("Olive")%></OPTION>
                      <OPTION style="COLOR: #000080"><%=scc.getString("Navy")%></OPTION>
                      <OPTION style="COLOR: #800080"><%=scc.getString("Purple")%></OPTION>
                      <OPTION style="COLOR: gray"><%=scc.getString("Gray")%></OPTION>
                      <OPTION style="COLOR: #C0C0C0"><%=scc.getString("Silver")%></OPTION>
                      <OPTION style="COLOR: #FF0000"><%=scc.getString("Red")%></OPTION>
                      <OPTION style="COLOR: darkred"><%=scc.getString("DarkRed")%></OPTION>
                      <OPTION style="COLOR: #00FF00"><%=scc.getString("FluoGreen")%></OPTION>
                      <OPTION style="COLOR: #FFFF00"><%=scc.getString("Yellow")%></OPTION>
                      <OPTION style="COLOR: #0000FF"><%=scc.getString("Blue")%></OPTION>
                      <OPTION style="COLOR: #FF00FF"><%=scc.getString("Fuchsia")%></OPTION>
                      <OPTION style="COLOR: #00FFFF"><%=scc.getString("Turquoise")%></OPTION>
                      <OPTION style="COLOR: #FFFFFF"><%=scc.getString("White")%></OPTION>
						        <OPTION><%=scc.getString("Other")%>
						      </OPTION></SELECT>&nbsp;&nbsp;


								</td>
							</tr>
						</table>
			    </div>
          </td>
        </tr>
				<tr>
          <td bgcolor="#999999"><img src="<%=iconsPath%>1px.gif" width="1" height="1"></td>
        </tr>
      </table>
			<table width="100%" border="0" cellspacing="0" cellpadding="0">
				<tr>
          <td bgcolor="#ffffff"><img src="<%=iconsPath%>1px.gif" width="1" height="1"></td>
        </tr>
        <tr>
          <td bgcolor="#ADBACE" width="100%" align="center"> <a href onclick="format('bold');" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('Image1','','<%=iconsPathWysiwyg%>gras2.gif',1)">
            </a>
            <table width="100" border="0" cellspacing="0" cellpadding="0">
              <tr>
                  <td width="5%">
                          <a href onclick="format('bold');" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('Image1','','<%=iconsPathWysiwyg%>gras2.gif',1)">
                                  <img src="<%=iconsPathWysiwyg%>gras.gif" width="30" height="30" border="0" name="Image1" alt="<%=scc.getString("Bold")%>" title="<%=scc.getString("Bold")%>">
                          </a>
                  </td>
                  <td width="5%">
			  <a href onclick="format('italic');" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('Image2','','<%=iconsPathWysiwyg%>italique2.gif',1)">
				  <img src="<%=iconsPathWysiwyg%>italique.gif" width="30" height="30" border="0" name="Image2" alt="<%=scc.getString("Italic")%>" title="<%=scc.getString("Italic")%>">
			  </a>
		  </td>
                  <td widtjavascripth="5%">
			  <a href onclick="format('underline');" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('Image3','','<%=iconsPathWysiwyg%>souligne2.gif',1)">
				  <img src="<%=iconsPathWysiwyg%>souligne.gif" width="30" height="30" border="0" name="Image3" alt="<%=scc.getString("Underline")%>" title="<%=scc.getString("Underline")%>">
			  </a>
		  </td>
                  <td width="5%"><img src="<%=iconsPathWysiwyg%>separation.gif" width="15" height="30"></td>
                  <td width="5%">
			  <a href onclick="format('justifyleft');" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('Image5','','<%=iconsPathWysiwyg%>gauche2.gif',1)">
				  <img src="<%=iconsPathWysiwyg%>gauche.gif" width="30" height="30" border="0" name="Image5" alt="<%=scc.getString("LeftJustify")%>" title="<%=scc.getString("LeftJustify")%>">
			  </a>
		  </td>
                  <td width="5%">
			  <a href onclick="format('justifycenter');" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('Image6','','<%=iconsPathWysiwyg%>centre2.gif',1)">
				<img src="<%=iconsPathWysiwyg%>centre.gif" width="30" height="30" border="0" name="Image6" alt="<%=scc.getString("CenterJustify")%>" title="<%=scc.getString("CenterJustify")%>">
			  </a>
		  </td>
                  <td width="5%">
			  <a href onclick="format('justifyright');" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('Image7','','<%=iconsPathWysiwyg%>droite2.gif',1)">
				<img src="<%=iconsPathWysiwyg%>droite.gif" width="30" height="30" border="0" name="Image7" alt="<%=scc.getString("RightJustify")%>" title="<%=scc.getString("RightJustify")%>">
			  </a>
		  </td>
                  <td width="5%"><img src="<%=iconsPathWysiwyg%>separation.gif" width="15" height="30"></td>
                  <td width="5%">
			  <a href onclick="format('insertorderedlist');" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('Image8','','<%=iconsPathWysiwyg%>1232.gif',1)">
				<img src="<%=iconsPathWysiwyg%>123.gif" width="30" height="30" border="0" name="Image8" alt="<%=scc.getString("OrderedList")%>" title="<%=scc.getString("OrderedList")%>">
			  </a>
		  </td>
                  <td width="5%">
			  <a href onclick="format('insertunorderedlist');" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('Image9','','<%=iconsPathWysiwyg%>puces2.gif',1)">
				<img src="<%=iconsPathWysiwyg%>puces.gif" width="30" height="30" border="0" name="Image9" alt="<%=scc.getString("UnorderedList")%>" title="<%=scc.getString("UnorderedList")%>">
			  </a>
		  </td>
                  <td width="5%">
			  <a href onclick="format('outdent');" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('Image101','','<%=iconsPathWysiwyg%>tabgauche2.gif',1)">
				<img src="<%=iconsPathWysiwyg%>tabgauche.gif" width="30" height="30" border="0" name="Image101" alt="<%=scc.getString("RemoveIndent")%>" title="<%=scc.getString("RemoveIndent")%>">
			  </a>
		  </td>
                  <td width="5%">
			  <a href onclick="format('indent');" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('Image11','','<%=iconsPathWysiwyg%>tabdroite2.gif',1)">
				<img src="<%=iconsPathWysiwyg%>tabdroite.gif" width="30" height="30" border="0" name="Image11" alt="<%=scc.getString("Indent")%>" title="<%=scc.getString("Indent")%>">
			  </a>
		  </td>
                  <td width="5%"><img src="<%=iconsPathWysiwyg%>separation.gif" width="15" height="30"></td>
                  <td width="5%">
			<a href onclick="createLink();" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('Image12','','<%=iconsPathWysiwyg%>lien2.gif',1)">
				<img src="<%=iconsPathWysiwyg%>lien.gif" width="30" height="30" border="0" name="Image12" alt="<%=scc.getString("Hyperlink")%>" title="<%=scc.getString("Hyperlink")%>">
			</a>
		  </td>

	              </tr>
            </table>
	  </td>
        </tr>
        <tr>
          <td bgcolor="#999999"><img src="<%=iconsPath%>1px.gif" width="1" height="1"></td>
        </tr>
      </table>
    </td>
  </tr>
</table>


</FORM>
<SCRIPT event="ShowContextMenu(xPos, yPos)" for=EditCtrl>//important</SCRIPT>

<SCRIPT>
/***********************************/
function initEditor(bWhichEditor) {
        // IE5 Secret Sauce
	if (bWhichEditor)
                idEdit = EditCtrl.DOM.parentWindow
	else {
		EditCtrl.document.designMode="On"
		idEdit = EditCtrl
	}
	idEdit.document.open()
        idEdit.document.write(sHeader)
	idEdit.document.close()
//	if (external.raiseEvent)
	//	external.raiseEvent("onready",window.event)
}

/***********************************/
function doLoad() {
	idBox.style.visibility=''
//	if (!(navigator.userAgent.indexOf("MSIE 5")>0)) {
		initEditor(false)
		bLoad=true;
		idEdit.focus()
//	}

}

/*************************************************/

//if (navigator.userAgent.indexOf("MSIE 5")>0) {
//	document.write("<OBJECT WIDTH=100% classid=\"clsid:2D360201-FFF5-11d1-8D03-00A0C959BC0A\" ID=EditCtrl></OBJECT>")
//}
//else {
</SCRIPT>

<SCRIPT>
	document.write("<IFRAME NAME=EditCtrl WIDTH=100%></IFRAME>")
//}


</SCRIPT>



<DIV class=mode id=tb3><NOBR>
<INPUT id=mW onclick=setMode(true) type=radio
CHECKED name=rMode><LABEL class=current id=modeA for=mw><%=scc.getString("WYSIWYG")%></LABEL>

<INPUT id=mH onclick=setMode(false) type=radio name=rMode><LABEL id=modeB
for=mH><%=scc.getString("HTML")%></LABEL>

<BR><BR><BR>

</NOBR></DIV></DIV>
<SCRIPT>
	setTimeout("document.all.EditCtrl.style.height=document.body.offsetHeight-90",0)
	window.onresize = new Function("document.all.EditCtrl.style.height=document.body.offsetHeight-90")
</SCRIPT>
</DIV>


</BODY></HTML>