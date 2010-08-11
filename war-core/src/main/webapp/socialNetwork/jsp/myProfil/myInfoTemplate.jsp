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
<%@ include file="check.jsp" %>
<html>
  <head>
    <view:looknfeel />
    <title><fmt:message key="invitation.action.title" /> </title>
    <style type="text/css">
      #mask {
        position:absolute;
        left:0;
        top:0;
        z-index:9000;
        background-color:#000;
        display:none;
      }

      #boxes .window {
        position:absolute;
        left:0;
        top:0;
        width:440px;
        height:200px;
        display:none;
        z-index:9999;
        padding:20px;
      }

      #boxes #dialog {
        background-color:#FFFFFF;
        height:150px;
        padding:10px;
        width:500px;
      }

      #page{
        width : 100%;
        border : 0;
      }

      #profil80{
        height : 600px;
        vertical-align: top;
      }

      #profilHead {
        height:120px;
        vertical-align:top;
        width:auto;
      }

      #profilCore{
        float : left;
        height : auto;
        width : 100%;
        vertical-align: top;
      }

      #navigation{
        width : 20%;
        float:left;
      }

      #headAndCore{
        float: left;
        width : 75%;
      }

      .format{
        width:100%;
        border: 0; 
      }
    </style>
    <script type="text/javascript" src="<c:url value="/util/javaScript/jquery/jquery-1.3.2.min.js" />" ></script>
    <script type="text/javascript"
    src="<c:url value="/util/javaScript/animation.js"/>"></script>
    <script language="JavaScript">
      var properties =new Array();
      <c:forEach items="${properties}" var="property" varStatus="status">
        properties.push("<c:out value='${properties[status.index]}' escapeXml='false' />");
      </c:forEach>

           

      <%--*****************   profil body *******************************************--%>
        function enableFields()

        {
                
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
          url+='&status='+status;
          $.getJSON(url, function(data) {
            $('.StatusDiv').empty();
            var html='';
            html+='<textarea onblur="javascript:updateStatus(\'${urlUpdateStatus}\')" id="enabledStat"';
            html+='type="text"  rows="3" >'+data.status+'</textarea>';

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
            html+='type="text"  >'+data.status+'</textarea>';
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
  <body  bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" >
    <view:operationPane>
      <view:operation action="javascript:openPopupChangePhoto()" altText="Changer mon photo" icon="" />
      <view:operation action="javascript:enableStatusZone()" altText="Changer mon statut" icon="" />
      <view:operation action="javascript:enableFields()" altText="Changer mes infos" icon="" />
    </view:operationPane>
    <view:window>
      <div id="page">
        <div id ="navigation">
          <view:board>
            <%@include file="myProfilNavigation.jsp" %>
          </view:board>
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
      </div>
    </view:window>

  </body>
</html>