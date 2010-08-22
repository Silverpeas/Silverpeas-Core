<%--<%@ include file="check.jsp" %>--%>
<style type="text/css">

  #listBody {
    list-style:none outside none;
    padding-left:0;
  }

  #listBody #photo{
    float :left;
    margin-right:4px;

  }

  #listBody #information a{
    color: blue;
    font-size: 12px;
    font-weight: bold;
    margin-bottom: 3px;
  }
   #listBody #information {
    vertical-align: top;
    float :left;
  }
  #listBody #button {
    float:right;
    margin-top:10px;
  }

  #listBody #button #message{
    /*width: 40%;*/
    text-align: right;

  }
</style>
<view:tabs >
  <view:tab label="<%=multilang.getString("invitation.tab.receive")%>" action="Main" selected="true" />
  <view:tab label="<%=multilang.getString("invitation.tab.sent")%>" action="InvitationsSent" selected="false" />
</view:tabs>
<ol id="listBody">
  <%
      for (int i = 0; i < invitationUsers.size(); i++) {
        InvitationUser invitationUser = (InvitationUser) invitationUsers.get(i);
  %>
  <li style="margin: 3px; ">

  <view:board >
    <!--      <div id="body">-->
    <!--        <tr>-->
    <div id="photo">
      <a href="${urlProfil}<%=invitationUser.getUserDetail().getUserId()%>">
        <img src="<%=m_context + invitationUser.getUserDetail().getProfilPhoto()%>"
             width="60" height="70" border="0" alt="viewUser" />
      </a>
    </div>
    <!--          <div id="information">-->
    <div id ="information" >

      <b>
        <a href="${urlProfil}<%=invitationUser.getUserDetail().getUserId()%>"
           class="link"><%=invitationUser.getUserDetail().getLastName() + " " + invitationUser.
                     getUserDetail().getFirstName()%>
        </a>
      </b><br>
      <b>
      <fmt:message key="invitation.dateInvitation" /></b><%=" " + DateUtil.formatDate(new java.util.Date(invitationUser.getInvitation().
                getInvitationDate().getTime())) + " "%><b><fmt:message key="invitation.dateInvitationSuffix"/></b> <%=" " + DateUtil.formatTime(new java.util.Date(invitationUser.getInvitation().
                        getInvitationDate().getTime()))%>
      <br>

      <%if (StringUtil.isDefined(
                  invitationUser.getInvitation().getMessage().trim())) {
      %>
      <textarea id="bloctext" rows="3" cols="59" disabled><%=invitationUser.getInvitation().getMessage().trim()%>

      </textarea>
    <%
            }
    %>
    <!--              </tr>-->

    </div>
    <!--            </div>-->
    <div id="button">
      <div id="message">
        <b>  <a href="#" style="color: blue"><fmt:message key="userInvitation.sendMessage" /></a></b><br><br>
      </div>


      <%
              ButtonPane buttonPane = gef.getButtonPane();
              buttonPane.addButton((Button) gef.getFormButton(multilang.getString(
                  "invitation.action.confirme"), "AcceptInvitation?UserId=" + invitationUser.
                  getInvitation().getId(), false));
              buttonPane.addButton((Button) gef.getFormButton(multilang.getString(
                  "invitation.action.ignore"), "IgnoreInvitation?UserId=" + invitationUser.
                  getInvitation().getId(), false));

              out.println(buttonPane.print().replaceAll(
                  "width=\"100\"", " ").replaceAll("<table",
                  "<table align=\"right\" "));
      %>
    </div>
    <!--        </tr>-->
    <!--      </div> -->
  </view:board>
</li>
<%
    }

%>
</ol>
