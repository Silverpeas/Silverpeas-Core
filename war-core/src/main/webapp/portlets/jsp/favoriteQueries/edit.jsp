<%@ page import="javax.portlet.RenderRequest" %>
<%@ page import="javax.portlet.RenderResponse" %>
<%@ page import="javax.portlet.PortletPreferences" %>
<%@ page import="javax.portlet.PortletURL" %>
<%@ page import="com.silverpeas.portlets.FormNames" %>
<%@ page session="false" %>

<%@ taglib uri="/WEB-INF/portlet.tld" prefix="portlet" %>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt" %>

<portlet:defineObjects/>
<portlet:actionURL var="actionURL"/>
<fmt:setBundle basename="com.silverpeas.portlets.multilang.portletsBundle"/>

<%
    RenderRequest pReq = (RenderRequest)request.getAttribute("javax.portlet.request");
    RenderResponse rRes = (RenderResponse)request.getAttribute("javax.portlet.response");
    PortletPreferences pref = pReq.getPreferences();
    String error = pReq.getParameter(FormNames.ERROR_BAD_VALUE);
%>               
    <!-- START error -->
<%
    if (error != null) {
%>      
      <div class="portlet-msg-error" style="color: red">
          <fmt:message key="portlets.portlet.lastPublis.error"/>
      </div>
      <br>
<%
        }
%>

    <form name="inputForm" target="_self" method="POST" action="<%=actionURL.toString()%>">
        <table border="0" width="100%" style="align: center">

            <!-- START "url" text box -->
            <tr>
                <td><fmt:message key="portlets.portlet.lastPublis.pref.nbPublis" /> :</td>
                <td><input class="portlet-form-input-field" name="<%=FormNames.TEXTBOX_NB_PUBLIS%>" value="<%=pref.getValue("nbPublis","")%>" type="text" size="10"/></td>
            </tr>
                     
            <!-- START "finished" and "cancel" buttons -->
            <tr>
                <td colspan="2" style="text-align: center; vertical-align: top">
                    <input class="portlet-form-button" name="<%=FormNames.SUBMIT_FINISHED%>" type="submit" value="<fmt:message key="portlets.validate"/>"/>
                    <input class="portlet-form-button" name="<%=FormNames.SUBMIT_CANCEL%>" type="submit" value="<fmt:message key="portlets.cancel"/>"/>
                </td>
            </tr>
        </table>
        <!-- END "finished" and "cancel" buttons -->
    </form>
