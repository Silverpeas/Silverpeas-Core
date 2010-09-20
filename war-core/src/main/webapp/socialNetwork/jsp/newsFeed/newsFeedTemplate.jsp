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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">
<%@ include file="check.jsp" %>
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title><fmt:message key="invitation.action.title" /> </title>
    <view:looknfeel />
    <script type="text/javascript" src="<c:url value="/util/javaScript/animation.js" />" ></script>
    <script type="text/javascript">
      
      <%--*****************   profil Body *******************************************--%>
        var offset=0;
        var hscroll ;var vscroll;
        var type='ALL';
        var urlDirectory='${urlDirectory}';
        var inprogress = '<img id="inprogress" src="'+'${progress}'+'" alt=""/>';
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
                  html+='<div class="socialDate">'+listSocialInfo.day+'<\/div>'
                }else
                {
                  $.each(listSocialInfo, function(index,socialInfo){
                    var urlProfil='${urlProfil}'+socialInfo.author.id;
                    html+='<div class="socialTable">';
                    html+='<div class="profilPhoto">';
                    html+=' <a href="'+urlProfil+'"><img src="'+socialInfo.author.profilPhoto+'" /><\/a>';
                    html+='<\/div>';
                    if(socialInfo.type=='RELATIONSHIP')
                    {
                      html+='<div class="titleAndDes">';
                      html+='<div class="socialTitle">';
                      
                      html+='<span class="socialActivity">';
                      html+='<a href="'+urlProfil+'"><span>'+socialInfo.author.displayedName+'<\/span><\/a> ${relationShipSuffix} '+'<a href="'+socialInfo.url+
                        '" >'+socialInfo.title.displayedName+'<\/a>'+' ${relationShipPrefix} ';
                      html+='<\/span>';
                      html+='<span class="socialIcon">';
                      html+='<img src="'+socialInfo.title.profilPhoto+'" width="16" height="16" />';
                      html+='<\/span>';
                      html+='<span class="socialHour">';
                      html+=socialInfo.hour;
                      html+='<\/span>';
                      html+='<\/div>';
                      html+='<\/div>';
                    }else  if(socialInfo.type=='STATUS')
                    {
                      html+='<div class="titleAndDes">';
                      html+='<div class="socialTitle">';
                     
                      html+='<span class="socialActivity">';
                      html+=' <a href="#"><span>'+socialInfo.title+'<\/span><\/a> ${statusSuffix} ';
                      html+='<\/span>';
                      html+='<span class="socialIcon">';
                      html+='<img  src="'+socialInfo.icon+'" width="16" height="16" />';
                      html+='<\/span>';
                      html+='<span class="socialHour">';
                      html+=socialInfo.hour;
                      html+='<\/span>';
                      html+='<\/div>';
                      html+='<div class="socialDescription"  >';
                      html+=socialInfo.description;
                      html+='<\/div>';
                      html+='<\/div>';
                      html+='<\/div>';
                    }else
                    { html+='<div class="titleAndDes">';
                      html+='<div class="socialTitle">';
                      html+='<span class="socialActivity">';
                      html+=' <a href="'+urlProfil+'"><span>'+socialInfo.author.displayedName+'<\/a> '+ socialInfo.label +' <a href="'+socialInfo.url+
                        '" >'+socialInfo.title+'<\/span><\/a>';
                      html+='<\/span>';
                      html+='<span class="socialIcon">';
                      html+='<img src="'+socialInfo.icon+'" class="'+socialInfo.type+'" width="16" height="16" />';
                      html+='<\/span>';
                      html+='<span class="socialHour">';
                      html+=socialInfo.hour;
                      html+='<\/span>';
                      html+='<\/div>';
                      html+='<div class="socialDescription" >';
                      html+=socialInfo.description;
                      html+='<\/div>';
                      html+='<\/div>';
                      html+='<\/div>';
                    }
                    html+='<\/div>';
                    //html+=' <br />';

                  });
                }
              });
              html+='<\/li>';
              html+='<\/td><\/tr><\/table>';



            });
            html+='<\/ol>';

            $("#inprogress").remove();
            $('.SocialInformations').append(html);

            window.scrollTo(hscroll, vscroll);

            if(listEmpty){
              $('#getNext').hide();
            }else {
              $('#getNext').show();
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
        function doPaganation(index)
        {
          directoryPagination('Pagination',index);
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
        function directoryPagination(action,index){
          var url=urlDirectory+action+'&Index='+index;
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
  <body id="newsFeed">
    <view:window>
      <div id="navigation">
          <%@include file="newsFeedNavigation.jsp" %>
      </div>
      <div id="content">
          <%@include file="newsFeedBody.jsp" %>
      </div>
    </view:window>

  </body>
</html>