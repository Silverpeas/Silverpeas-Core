<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="check.jsp" %>

<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title><fmt:message key="invitation.action.title" /> </title>
    <view:looknfeel />
    <link rel="stylesheet" type="text/css" href="<c:url value="/directory/jsp/directoryPopup.css"/>"/>
    <script type="text/javascript" src="<c:url value="/util/javaScript/animation.js"/>"></script>
    <script type="text/javascript" src="<c:url value="/directory/jsp/directory.js" />" ></script>
    <script type="text/javascript">
      var properties =new Array();
      <c:forEach items="${properties}" var="property" varStatus="status">
        properties.push("<c:out value='${properties[status.index]}' escapeXml='false' />");
      </c:forEach>

           

      <%--*****************   profil body *******************************************--%>
        function enableFields() {
                
          for( i=0; i<properties.length;i++)
          {
            var id=properties[i];
            document.getElementById(id).style.backgroundColor="#FFFFFF";
            document.getElementById(id).disabled=false;
          }
          document.getElementById("myInfoUpdate").style.visibility='hidden' ;
          document.getElementById("myInfoAction").style.visibility='visible' ;
        }
        function desabledFields()
        {
               
          for( i=0; i<properties.length;i++)
          {
            var id=properties[i];
            document.getElementById(id).style.backgroundColor="#F2F2F2";
            document.getElementById(id).disabled=true;
          }
          document.getElementById("myInfoAction").style.visibility='hidden' ;
        }
        function submitUpdate()
        {
          document.myInfoUpdateForm.action = 'updateMyInfos';
          document.myInfoUpdateForm.submit();
        }
        function afficheButtonEdit(visible)
        { document.getElementById("actionEditStat").style.display = 'none';
        }
      <%--*****************   profil Head *******************************************--%>
        function updateStatus(url)
        {
          var status = $('textarea').val();
          url+='&amp;status='+status;
          $.getJSON(url, function(data) {
            $('.StatusDiv').empty();
            var html='';
            html+='<textarea onblur="javascript:updateStatus(\'${urlUpdateStatus}\')" id="enabledStat"';
            html+='type="text" rows="3" >'+data.status+'<\/textarea>';

            $('.StatusDiv').append(html);
            desableStatusZone();
          });
        }

        function getLastStatus(url)
        {
          $.getJSON(url, function(data) {
            $('.StatusDiv').empty();
            var html='';
            html+='<textarea onblur="javascript:updateStatus(\'${urlUpdateStatus}\')" id="enabledStat" ';
            html+='type="text"  >'+data.status+'<\/textarea>';
            $('.StatusDiv').append(html);
            desableStatusZone();
          });
        }
        function enableStatusZone()
        {
          document.getElementById("enabledStat").style.backgroundColor="#FFFFFF";
          document.getElementById("enabledStat").value='';
          document.getElementById("enabledStat").disabled=false;
          document.getElementById("enabledStat").focus();
        }
        function desableStatusZone()
        {
          document.getElementById("enabledStat").style.backgroundColor="#F2F2F2";
          document.getElementById("enabledStat").disabled=true;

        }
       
       
        function openPopupChangePhoto()
        {

          //Get the A tag
          var id ='#dialog'

          //Get the screen height and width
          var maskHeight = $(document).height();
          var maskWidth = $(window).width();

          //Set heigth and width to mask to fill up the whole screen
          $('#mask').css({'width':maskWidth,'height':maskHeight});

          //transition effect
          $('#mask').fadeIn(1000);
          $('#mask').fadeTo("slow",0.8);

          //Get the window height and width
          var winH = $(window).height();
          var winW = $(window).width();

          //Set the popup window to center
          $(id).css('top',  winH/2-$(id).height()/2);
          $(id).css('left', winW/2-$(id).width()/2);

          //transition effect
          $(id).fadeIn(2000);

        }
        $(document).ready(function() {
          //if close button is clicked
          $('.window .close').click(function (e) {
            //Cancel the link behavior
            e.preventDefault();

            $('#mask').hide();
            $('.window').hide();
          });

          //if mask is clicked
          $('#mask').click(function () {
            $(this).hide();
            $('.window').s.hide();
          });
         
        });

        
    </script>
  </head>
  <body id="myProfile">
   <fmt:message key="profil.actions.changePhoto" var="changePhoto"/>
    <fmt:message key="profil.actions.changeStatus" var="changeStatus"/>
    <fmt:message key="profil.actions.changeInfos" var="changeInfos"/>
    <view:operationPane>
      <view:operation action="javascript:openPopupChangePhoto()"
                      altText="${changePhoto}" icon="" />
      <view:operation action="javascript:enableStatusZone()"
                      altText="${changeStatus}" icon="" />
      <view:operation action="javascript:enableFields()" altText="${changeInfos}" icon="" />
    </view:operationPane>
    <view:window>
<!--      <div id="myInfotemplate">-->
        <div id ="navigation">
            <%@include file="myProfilNavigation.jsp" %>
        </div>
        <div id="headAndCore">
            <div id="profilHead" >
              <view:board>
                <%@include file="myProfilHead.jsp" %>
              </view:board>
            </div>
            <div id="profilCore">
                <%@include file="myInfosBody.jsp" %>
             </div>
        </div>
    </view:window>
<div id="boxesDirectory">
       <div class="windowDirectory" id="directory" >
        <div id="directoryHeader">
          <a href="#"class="close">Fermer</a>
        </div>
        <div id="indexAndSearch"><div id="search">
            <form name="search1" action="javascript:directory('searchByKey')" method="post">
              <input type="text" name="key" value="" id="key" size="40" maxlength="60"
                     style="height: 20px"  />
              <img src="<c:url value="/directory/jsp/icons/advsearch.jpg" />"
                width="10" height="10" alt="advsearch" />
            </form>
          </div>
        </div>
        <div id="users">

        </div>
      </div>
                  <!-- Mask to cover the whole screen -->
        <div id="maskDirectory"></div>
      </div>
  </body>
</html>