<%--<%@ include file="check.jsp" %>--%>
<view:tabs >
  <view:tab label="<%=multilang.getString("invitation.tab.receive")%>" action="Main" selected="false" />
  <view:tab label="<%=multilang.getString("invitation.tab.sent")%>" action="InvitationsSent" selected="true" />
</view:tabs>
<ol  style="list-style: none" >
  <%
      for (int i = 0; i < invitationUsers.size(); i++) {
        InvitationUser invitationUser = (InvitationUser) invitationUsers.get(i);
  %>
  <li style="margin: 3px; ">
    <view:board>
      <table  width="100%" >
          <tr>
            <td>
              <a href="createPhoto">
                <img   src="<%=m_context + invitationUser.getUserDetail().getProfilPhoto()%>" width="60" height="70" border="0" alt="viewUser" />
              </a>
            </td>
            <td width="60%" style="vertical-align: top">
              <table  width="100%"   >
                <tr>
                  <td>
                    <b><a href="viewUser?UserId=<%=invitationUser.getUserDetail().getUserId()%>"
                          class="link"><%=invitationUser.getUserDetail().getLastName() + " " + invitationUser.
                                     getUserDetail().getFirstName()%>
                      </a></b>
                  </td>
                </tr>
                <tr>
                    <td><b><fmt:message key="invitation.dateInvitation" /></b><%=" " + DateUtil.formatDate(new java.util.Date(invitationUser.getInvitation().
                          getInvitationDate().getTime())) + " "%><b><fmt:message key="invitation.dateInvitationSuffix"/></b> <%=" " + DateUtil.formatTime(new java.util.Date(invitationUser.getInvitation().
                                  getInvitationDate().getTime()))%></td>
                </tr>
                <tr>
                  <%
                          if (StringUtil.isDefined(
                              invitationUser.getInvitation().getMessage().trim())) {
                  %>
                  <td>
                    <textarea  rows="2" cols="59"
                               style="font-weight:700; color:blue; "><%=invitationUser.getInvitation().getMessage().trim()%>

                    </textarea>

                  </td>
                  <%
                          }
                  %>
                </tr>
              </table>
            <td  width="40%"  style="text-align: right">
              <b>  <a href="#" style="color: blue"><fmt:message key="userInvitation.sendMessage"/>
                </a></b>
              <br/>
              <br/>
            </td>
          </tr>
      </table>
    </view:board>
  </li>
  <%
      }
  %>
</ol>
