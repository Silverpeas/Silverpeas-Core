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

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="check.jsp" %>

<html xmlns="http://ww<w.w3.org/1999/xhtml">
  <head>
    <title><fmt:message key="invitation.action.title" /></title>
    <view:looknfeel />
    <link rel="stylesheet" type="text/css" href="<c:url value="/socialNetwork/jsp/css/socialNetwork.css"/>"/>
    <link rel="stylesheet" type="text/css"
          href="<c:url value="/socialNetwork/jsp/myProfil/myProfil.css"/>" />
    <link rel="stylesheet" type="text/css" href="<c:url value="/directory/jsp/directoryPopup.css"/>"/>
    <script type="text/javascript"
    src="<c:url value="/util/javaScript/jquery/jquery-1.3.2.min.js" />"></script>
    <script type="text/javascript" src="<c:url value="/directory/jsp/directory.js" />" ></script>
    <script type="text/javascript">
	
	
      <%--*****************   profil Body *******************************************--%>
        var offset=0;
        var hscroll ;var vscroll
        var inprogress = '<img id="inprogress" src="/silverpeas/util/icons/inProgress.gif" width="40" height="40" alt=""/>';
        function getNext(url,socialType)
        {
          hscroll = (document.all ? document.scrollLeft : window.pageXOffset);
          vscroll = (document.all ? document.scrollTop : window.pageYOffset);
          $('.SocialInformations').append(inprogress);
          $.getJSON(url,{type:socialType,offset:offset},
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
	
                    html+='<div class="socialTable">';
                    if(socialInfo.type=='RELATIONSHIP')
                    {
                      html+='<div class="titleAndDes">';
                      html+='<div class="socialTitle">';
                      html+='<span class="socialIcon">';
                      html+='<img src="'+socialInfo.icon+'" width="16" height="16" />';
                      html+='<\/span>';
                      html+='<span class="socialActivity">';
                      html+=socialInfo.author+' ${relationShipSuffix} '+'  <a href="'+socialInfo.url+
                        '" >'+socialInfo.title+'<\/a>'+' ${relationShipPrefix} ';
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

                      html+='<span class="socialIcon">';
                      html+='<img  src="'+socialInfo.icon+'" width="16" height="16" />';
                      html+='<\/span>';

                      html+='<span class="socialActivity">';
                      html+=' <a href="#"><span>'+socialInfo.title+'<\/span><\/a>'+' ${statusSuffix} ';
                      html+='<\/span>';

                      html+='<span class="socialHour">';
                      html+=socialInfo.hour;
                      html+='<\/span>';

                      html+='<\/div>';

                      html+='<div class="socialDescription"  >';
                      html+=socialInfo.description;
                      html+='<\/div>';
                      
                      html+='<\/div>';
                    }else
                    {
                     
                      html+='<div class="titleAndDes">';
                      html+='<div class="socialTitle">';
                      html+='<span class="socialIcon">';
                      html+='<img src="'+socialInfo.icon+'" class="'+socialInfo.type+'" width="16" height="16" />';
                      html+='<\/span>';

                      html+='<span class="socialActivity">';
                      html+='<a href="'+socialInfo.url+'" >'+socialInfo.title+'<\/a>  ';
                      html+='<\/span>';

                      html+='<span class="socialHour">';
                      html+=socialInfo.hour;
                      html+='<\/span>';

                      html+='<\/div>';
                      html+='<div class="socialDescription" >';
                      html+=socialInfo.description;
                      html+='<\/div>';

                      html+='<\/div>';
                    }
                    html+='<\/div>';
	
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
              objDiv = document.getElementById("getNext").style;
              objDiv.visibility='hidden';
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
	
	
      <%--*****************   profil Head *******************************************--%>
        function updateStatus(url)
        {
	
          var status = $('textarea').val();
          url+='&status='+status;
          $.getJSON(url, function(data) {
            $('.StatusDiv').empty();
            var html='';
            html+='<textarea onblur="javascript:updateStatus(\'${urlUpdateStatus}\')" id="enabledStat"';
            html+='type="text"  rows="3" >'+data.status+'<\/textarea>';
	
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
            html+='type="text"  rows="3" >'+data.status+'<\/textarea>';
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
            $('.window').hide();
          });
	
        });
	
	
	
    </script>
  </head>
  <body>
    <fmt:message key="profil.actions.changePhoto" var="changePhoto"/>
    <fmt:message key="profil.actions.changeStatus" var="changeStatus"/>
    <fmt:message key="profil.actions.changeInfos" var="changeInfos"/>
    <view:operationPane>
      <view:operation action="javascript:openPopupChangePhoto()"
                      altText="${changePhoto}" icon="" />
      <view:operation action="javascript:enableStatusZone()"
                      altText="${changeStatus}" icon="" />

    </view:operationPane>
    <view:window>

      <div id="navigation">
        <%@include file="myProfilNavigation.jsp"%>
      </div>

      <div id="headAndCore">

        <div id="profilHead">
          <view:board>
            <%@include file="myProfilHead.jsp"%>
          </view:board>
        </div>
        <div id="profilCore">
          <view:board>
            <%@include file="myProfilBody.jsp"%>
          </view:board>
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
      <div id="maskDirectory" class="maskClass"></div>
    </div>
  </body>
</html>