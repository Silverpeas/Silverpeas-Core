
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>

<%@page import="com.stratelia.webactiv.util.GeneralPropertiesManager" %>
<%@page import="com.stratelia.silverpeas.peasCore.URLManager" %>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttonPanes.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.buttons.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.ResourceLocator"%>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}"  />






<%
            ResourceLocator multilang = new ResourceLocator("com.silverpeas.directory.multilang.DirectoryBundle", request.getLocale());
            GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
            String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
            String action = (String) request.getParameter("Action");
            String txtMessage = Encode.htmlStringToJavaString((String) request.getAttribute("txtMessage"));
            String popupMode = (String) request.getParameter("popupMode");

            String recipient = Encode.htmlStringToJavaString((String) request.getParameter("Recipient"));
            String name = ((String) request.getParameter("Name"));


            if (action == null) {
                action = "NotificationView";
            }

            if (txtMessage == null) {
                txtMessage = "123";
            }
            if (popupMode == null) {
                popupMode = "Yes";
            }

           
            if ((action.equals("SendInvitation") || action.equals("CancelSendInvitation")) && popupMode.equals("Yes")) {

%>
<HTML>
    <BODY
        onLoad="javascript:window.close()">
    </BODY>
</HTML>
<% } else {%>
<html>
    <head>

        <title> <%= multilang.getString("userInvitation.titleprefix")+" "+ name+" "+multilang.getString("userInvitation.titleSuffix") %>  </title>
        <view:looknfeel />

    </head>
    <body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5"  onLoad="toggleZoneMessage();">
        <script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
        <script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
        <script language="JavaScript">

            function Submit(){

                SP_openUserPanel('about:blank', 'OpenUserPanel', 'menubar=no,scrollbars=no,statusbar=no');

                //document.invitationSenderForm.action = "<%=m_context + URLManager.getURL(URLManager.CMP_NOTIFICATIONUSER)%>"+"SetTarget";
                document.invitationSenderForm.target = "OpenUserPanel";
                document.invitationSenderForm.submit();
            }
            function ClosePopup(){
                window.close()
            }
            function toggleZoneMessage() {
                if( document.getElementById("zoneMessage").style.display=='none' ){
                    document.getElementById("actionZoneMessage").style.display = 'none';
                    document.getElementById("zoneMessage").style.display = '';
                    document.getElementById("zoneMessage").focus() ;
                }else{
                    document.getElementById("actionZoneMessage").style.display = '';
                    document.getElementById("zoneMessage").style.display = 'none'
                    document.getElementById("zoneMessage").focus() ;
                }
            }
            function SubmitWithAction(action)
            {
                document.invitationSenderForm.action = action;
                document.invitationSenderForm.submit();
               
                
            }


        </script>

        <table border="0" width="100%" style=" height: 100%">
            <tr>
                <td align="center" height="100%" style="vertical-align: top" >







                    <view:board>
                        <form name="invitationSenderForm" Action=""  Method="POST">


                            <tr style="vertical-align: top">
                                <td style="vertical-align: top">
                                    <img src="<%=m_context%>/directory/jsp/icons/Photo_profil.jpg" width="60" height="70" border="0" alt="viewUser" />
                                </td>
                                <td style="vertical-align: top">
                                    <b><%=name%></b> <%=" "+multilang.getString("userInvitation.remark")  %>
                                </td>
                            </tr>


                            <tr id="zoneMessage" c >
                                <td class="txtlibform" valign="top" colspan="2" >
                                    <%=multilang.getString("userInvitation.message")+": "%><br>
                                    <textarea type="text" name="txtMessage"  value="" cols="49" rows="4"></textarea>

                                </td>

                            </tr>
                            <tr id="actionZoneMessage" >
                                <td colspan="2">
                                    <b><a href="#" style="color: blue" onclick="toggleZoneMessage();" ><%=multilang.getString("userInvitation.action.remark")%></a></b>
                                </td>
                            </tr>
                            <input type="hidden" name="Recipient" value="<%=recipient%>">


                        </form>
                    </view:board>
                </td>
            </tr>
            <tr>
                <td align="center" style="vertical-align: bottom; height: 100%">


                    <view:board>
                        <div align="center" style="vertical-align: bottom;  height: 100%" >
                            
                            <%
                                            ButtonPane buttonPane = gef.getButtonPane();
                                            buttonPane.addButton((Button) gef.getFormButton(multilang.getString("userInvitation.action.send"), "javascript:SubmitWithAction('SendInvitation')", false));
                                            buttonPane.addButton((Button) gef.getFormButton(multilang.getString("userInvitation.action.cancel"), "javascript:SubmitWithAction('CancelSendInvitation')", false));
                                            out.println(buttonPane.print());
                            %>
                        </div>
                    </view:board>

                </td>
            </tr>
        </table>

    </body>
</html>
<% }%>