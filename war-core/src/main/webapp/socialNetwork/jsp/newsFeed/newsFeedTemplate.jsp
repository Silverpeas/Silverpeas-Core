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
    <link rel="stylesheet" type="text/css" href="<c:url value="/directory/jsp/directory.css"/>"/>
    <style type="text/css">

      .message_list {
        list-style: none;
        margin: 0;
        padding: 0;
        width: 100%;
      }

      .message_list li {
        padding: 0px;
        margin: 3px;
      }

      #navigation {
        float: left;
        width: 20%;
        margin: 2px;
        padding: 2px;
      }
     #navigation #myInfo{
        float: right;
      }

      #navigation .box {
        background-color:#FFFFFF;
        background-image:url(/silverpeas/admin/jsp/icons/silverpeasV5/fondBoard.jpg);
        background-position:right top;
        background-repeat:no-repeat;
        margin-left:auto;
        margin-right:auto;
        margin-bottom: 15px;
        border:1px solid #CCCCCC;
      }
      #content {
        float: left;
        width: 75%;
        margin: 2px;
        padding: 2px;
      }
      * ul {
        margin: 0;
        padding: 0;
      }
      .ItemOn{
        background-image:url(/silverpeas/admin/jsp/icons/silverpeasV5/milieuOngletOff.gif);
        background-repeat:repeat-x;
        color:#FFFFFF;
        font-size:11px;
        font-weight:bold;
        height:20px;
        text-align:left;
      }
      * li{
        display:list-item;
        list-style-type:none;
        padding: 3px;
      }
      .newsFeedBodyTitle li {
        text-align:center;
      }
      .newsFeedBodyTitle li a{
        font-size: 15px;
        font-weight: bold;
        text-align:center;

      }
      .SocialInformations a{
        color: blue;
        font-weight: bold;

      }
    </style>


    <title><fmt:message key="invitation.action.title" /> </title>
    <script type="text/javascript" src="<c:url value="/util/javaScript/jquery/jquery-1.3.2.min.js" />" ></script>
    <script type="text/javascript" src="<c:url value="/util/javaScript/animation.js" />" ></script>
    <script language="JavaScript">


      <%--*****************   profil Body *******************************************--%>
        var offset=0;
        var hscroll ;var vscroll;
        var type='ALL';
        var urlDirectory='${urlDirectory}';
        var inprogress = '<img id="inprogress" src="/silverpeas/util/icons/inProgress.gif" alt=""/>';
        function getNext(url)
        {
          url=url+'&type='+type+'&offset='+offset;
      <%-- return the last postion of scrollPane--%>
          hscroll = (document.all ? document.scrollLeft : window.pageXOffset);
          vscroll = (document.all ? document.scrollTop : window.pageYOffset);
          // make user DIV emty and div directory invisible
          $('#user').empty();
          $('.directory').hide();
          $('.SocialInformations').append(inprogress);
          $.getJSON(url,
          function(data){
            var listEmpty=true;
            var html='<ol class="message_list">';
            $.each(data, function(key,map){
              html+='<li>';
              html+='<table cellpadding=\"5\" cellspacing=\"0\" border=\"0\" width=\"98%\" class=\"tableBoard\"><tr><td nowrap=\"nowrap\">';
              $.each(map, function(i,listSocialInfo){

                listEmpty=false;
                if(i==0)
                {
                  html+='<b>'+listSocialInfo.day+'</b><br>'
                }else
                {
                  $.each(listSocialInfo, function(index,socialInfo){
                    var urlProfil='${urlProfil}'+socialInfo.author.id;
                    html+='<table  cellspacing="0" cellpadding="4">';
                    html+='<tr>';
                    if(socialInfo.type=='RELATIONSHIP')
                    {
                      html+='<td rowspan="1">';
                      html+=' <a href="'+urlProfil+'"><img src="'+socialInfo.author.profilPhoto+'" width="32" height="32" /></a>';
                      html+='</td>';
                      html+='<td style="width: 16px;">';
                      html+='<img src="'+socialInfo.title.profilPhoto+'" width="16" height="16" />';
                      html+='</td>';
                      html+='<td>';
                      html+='<b><a href="'+urlProfil+'">'+socialInfo.author.lastName+' '+socialInfo.author.firstName+'</a> ${relationShipSuffix} '+'<a href="'+socialInfo.url+
                        '" >'+socialInfo.title.lastName+' '+socialInfo.title.firstName+'</a>'+' ${relationShipPrefix} '+'</b>  '+socialInfo.hour;
                      html+='</td></tr></table>'
                    }else  if(socialInfo.type=='STATUS')
                    {
                      html+='<td rowspan="2">';
                      html+=' <a href="'+urlProfil+'"><img src="'+socialInfo.author.profilPhoto+'" width="32" height="32" /></a>';
                      html+='</td>';

                      html+='<td style="width: 16px;">';
                      html+='<img src="'+socialInfo.icon+'" width="16" height="16" />';
                      html+='</td>';

                      html+='<td>';
                      html+='<b><a href="#"  >'+socialInfo.title+'</a>'+' ${statusSuffix} '+'</b>  '+socialInfo.hour;
                      html+='</td></tr><tr>';
                      html+='<td colspan="2">'+socialInfo.description;
                      html+='</td> </tr> </table>'
                    }else
                    { html+='<td rowspan="2">';
                      html+=' <a href="'+urlProfil+'"><img src="'+socialInfo.author.profilPhoto+'" width="32" height="32" /></a>';
                      html+='</td>';

                      html+='<td style="width: 16px;">';
                      html+='<img src="'+socialInfo.icon+'" width="16" height="16" />';
                      html+='</td>';

                      html+='<td>';
                     
                       html+='<b><a href="'+urlProfil+'">'+socialInfo.author.lastName+' '+socialInfo.author.firstName+'</a> '+ socialInfo.label +' <a href="'+socialInfo.url+
                        '" >'+socialInfo.title+'</a></b>  '+socialInfo.hour;
                      html+='</td></tr><tr>';
                      html+='<td colspan="2">'+socialInfo.description;
                      html+='</td> </tr> </table>'
                    }
                    html+=' <br>';

                  });
                }
              });
              html+='</li>';
              html+='</td></tr></table>';



            });
            html+='</ol>';

            $("#inprogress").remove();
            $('.SocialInformations').append(html);

            window.scrollTo(hscroll, vscroll);

            if(listEmpty){
              $('#getNext').hide();
            }else {
              $('#getNext').show()();
            }


          });

          offset++;// incrementer the offset (First element )

          var contenu = $.trim($(".list").html());
          if (contenu == ""){
            $(".list").remove();
          }

        }


        function zoom(test)
        {
          alert(test);
          var event =  window.event;
          var Id='PHOTO'+event.target.id ;


          $("table#"+Id).css({'z-index' : '40'}); /*Add a higher z-index value so this image stays on top*/
          $("table#"+Id).find('img').animate({
            marginTop: '0px', /* The next 4 lines will vertically align this image */
            marginLeft: '0px',
            top: '50%',
            left: '50%',
            width: '174px', /* Set new width */
            height: '174px' /* Set new height */

          }, 200)
        }
        
        function backToDefault(test)
        {
          var event =  window.event;
          var Id='PHOTO'+event.target.id ;

          $("div#"+Id).css({'z-index' : '0'}); /* Set z-index back to 0 */
          $("div#"+Id).find('img').animate({
            marginTop: '0', /* Set alignment back to default */
            marginLeft: '0',
            top: '0',
            left: '0',
            width: '32px', /* Set width back to default */
            height: '32px' /* Set height back to default */

          }, 400);
        }
        $(document).ready(function(){
          $("#relationshipActionNewsFeed a").click(function(){
            $("#relationshipActionNewsFeed a").parent().removeClass("ItemOn");
            $("#actionNewsFeed a").parent().removeClass("ItemOn");
            $(this).parent().toggleClass("ItemOn");
            $(".newsFeedBodyTitle").empty();
            $(this).parent().clone().appendTo(".newsFeedBodyTitle");
          });
          $("#actionNewsFeed a").click(function(){
            $("#actionNewsFeed a").parent().removeClass("ItemOn");
            $("#relationshipActionNewsFeed a").parent().removeClass("ItemOn");
            $(this).parent().toggleClass("ItemOn");
            $(".newsFeedBodyTitle").empty();
            $(this).parent().clone().appendTo(".newsFeedBodyTitle");
      <%--initialiser tout--%>
            offset=0;
            type=$(this).attr('name');
            $('.SocialInformations').empty();
            getNext('${urlServlet}');
          });

        });
        function indexDirectory(index){
          var action='pagination&currentPage='+index;
          directory(action);
        }
         function getAllUsersDirectory(){
          urlDirectory='${urlDirectory}';
          directory('Main');
        }
          function getAllContactsDirectory(){
          urlDirectory='${urlContactsDirectory}';
          directory('Main');
        }

        function directory(action){
          var url=urlDirectory+action+'&key='+$('#key').attr("value");
          $('.SocialInformations').empty();
          $('#getNext').hide();
          $('#users').empty();
          $('#directory').show();
          $('#key').attr("value",'');
          $.ajax({ // Requete ajax
            dataType: "html",
            type: "GET",
            url: url,
            async: true,
            success: function(data){
              $('#users').append(data);
            }
          });
        }
        function OpenPopup(usersId,name ){

          usersId=usersId+'&Name='+name
          options="location=no, menubar=no,toolbar=no,scrollbars=yes, resizable        , alwaysRaised"
          SP_openWindow('<c:url value="/Rdirectory/jsp/NotificationView?Recipient=" />'+usersId , 'strWindowName', '500', '200',options );

        }
        function OpenPopupInvitaion(usersId,name){
          usersId=usersId+'&Name='+name
          options="directories=no, menubar=no,toolbar=no,scrollbars=yes, resizable=no ,alwaysRaised"
          SP_openWindow('<c:url value="/Rinvitation/jsp/invite?Recipient="/>'+usersId, 'strWindowName', '350', '200','directories=no, menubar=no,toolbar=no,scrollbars=yes, resizable=no ,alwaysRaised');
        }
       
    

    </script>
  </head>
  <body  bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" >
    <view:window>
      <div id="navigation">
        <view:board>
          <%@include file="newsFeedNavigation.jsp" %>
        </view:board>
      </div>
      <div id="content">
        <view:board>
          <%@include file="newsFeedBody.jsp" %>
        </view:board>
      </div>
    </view:window>

  </body>
</html>