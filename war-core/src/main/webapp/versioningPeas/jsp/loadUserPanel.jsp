<%@ page import="com.stratelia.silverpeas.peasCore.URLManager"%>
<%@ include file="checkVersion.jsp" %>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%
String pfile = request.getParameter("pfile");
String statusDoc = request.getParameter("statusDoc");
String fromUp = request.getParameter("fromUp");
String urlUP;
if (fromUp != null &&  "fromUp".equals(fromUp)) {
    if ("update".equals(statusDoc)) {
        urlUP =  m_context+ "/RVersioningPeas/jsp/versions.jsp?";
    } else {
        urlUP =  m_context+ "/RVersioningPeas/jsp/newDocument.jsp?";
    }
    if (pfile != null && !"".equals(pfile)) {
        if ("Reader".equals(pfile)) {
            urlUP += "tab_number=2&from_action=2";
        } else {
            String style_type = String.valueOf(versioningSC.getEditingDocument().getTypeWorkList());
            urlUP += "tab_number=3&from_action=3&style_type="+style_type + "&lines=" + versioningSC.getEditingDocument().getWorkList().size();
        }
    }

    urlUP += "&profile=admin&reset=true";
} else {
    urlUP = m_context+"/RVersioningPeas/jsp/fromVersioning?pfile="+pfile+"&statusDoc="+statusDoc;
}
 
%>
<html>
<head>
<script language="Javascript">
location.replace("<%=urlUP%>");
</script>
</head>
<body></body>
</html>