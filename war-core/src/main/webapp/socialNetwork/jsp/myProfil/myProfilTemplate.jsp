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
    <style type="text/css">


      /* message display page */
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

      table.PHOTO td img {
        width: 32px; height: 32px; /* Set the small thumbnail size */
        -ms-interpolation-mode: bicubic; /* IE Fix for Bicubic Scaling */
        left: 0; top: 0;


      }
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
        width:375px;
        height:203px;
        padding:10px;
        background-color:#ffffff;
      }
      
      #page{
        width: 100%;
        border: 0;
      }
      
       #profil80{
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
        width : 75%;
      }
      
      .format{
        width:100%;
        border: 0; 
      }
      
      #bloc{
         border:0; 
         width:98%; 
      }
    </style>
    <title><fmt:message key="invitation.action.title" /> </title>
    <script type="text/javascript" src="<c:url value="/util/javaScript/jquery/jquery-1.3.2.min.js" />" ></script>
    <script language="JavaScript"><!--


      <%--*****************   profil Body *******************************************--%>
        var offset=0;
        var hscroll ;var vscroll

        function getNext(url)
        {
        
          hscroll = (document.all ? document.scrollLeft : window.pageXOffset);
          vscroll = (document.all ? document.scrollTop : window.pageYOffset);
          $.getJSON(url+offset,
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

                    html+='<table border="0">';
                    html+='<tr ><td>';
                    if(socialInfo.type=='RELATIONSHIP')
                    {
                      html+='<img src="'+socialInfo.icon+'" width="32" height="32" />';
                      html+='</td>';
                      html+='<td>';
                      html+='<b>'+socialInfo.author+' ${relationShipSuffix} '+'<a href="'+socialInfo.url+
                        '" style="color: blue" >'+socialInfo.title+'</a>'+' ${relationShipPrefix} '+'</b>  '+socialInfo.hour;
                      html+='</td></tr><tr><td></td>';
                      html+='</tr> </table>'
                    }else  if(socialInfo.type=='STATUS')
                    {
                      html+='<img src="'+socialInfo.icon+'" width="32" height="32" />';
                      html+='</td>';
                      html+='<td>';
                      html+='<b><a href="#" style="color: blue" >'+socialInfo.title+'</a>'+' ${statusSuffix} '+'</b>  '+socialInfo.hour;
                      html+='</td></tr><tr><td></td>';
                      html+='<td>'+socialInfo.description;
                      html+='</td> </tr> </table>'
                    }else
                    {
                      html+='<img src="'+socialInfo.icon+'" width="32" height="32" />';
                      html+='</td>';
                      html+='<td>';
                      html+='<a href="'+socialInfo.url+'" style="color: blue" ><b>'+socialInfo.title+'</b></a>  '+socialInfo.hour;
                      html+='</td></tr><tr><td></td>';
                      html+='<td>'+socialInfo.description;
                      html+='</td> </tr> </table>'
                    }
                    html+=' <br>';

                  });
                }
              });
            
              html+='</td></tr></table>';
              html+='</li>';



            });
            html+='</ol>';

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
            html+='type="text"  rows="3" >'+data.status+'</textarea>';
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



    --></script>
  </head>
  <body  bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5" >
    <view:operationPane>

      <view:operation action="javascript:openPopupChangePhoto()" altText="Changer mon photo" icon="" />
      <view:operation action="javascript:enableStatusZone()" altText="Changer mon statut" icon="" />

    </view:operationPane>
    <view:window>
    <div id="page">     
        <div>
          <div id="navigation" >
            <view:frame>
              <view:board>
                <%@include file="myProfilNavigation.jsp" %>
              </view:board>
            </view:frame>
          </div>
          <div id="headAndCore">
            <view:frame>
<!--              <div class="format">-->
                <div><div id="profil80">
                   <div class="format" >
                      <div>
                        <div id="profilHead" >
                          <view:board>

                            <%@include file="myProfilHead.jsp" %>

                          </view:board>

                        </div>
                      </div>
                      <div>
                        <div id="profilCore">

<!--                          <view:board>-->
                            <%@include file="myProfilBody.jsp" %><!--
                          </view:board>
                        --></div>
                      </div>
                    </div>
                  </div></div>

              </div>

            </view:frame>
          </div>
        </div>    
    </div>
      
    </view:window>

  </body>
</html>