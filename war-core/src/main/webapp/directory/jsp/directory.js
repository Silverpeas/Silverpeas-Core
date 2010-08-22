/*

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

*/
var urlDirectory='';

function indexDirectory(index){
  var action='pagination&currentPage='+index;
  directory(action);
}
function getAllUsersDirectory(url){
  urlDirectory=url;
  openPopupDiretory()
  directory('Main');
}
function getAllContactsDirectory(url){
  urlDirectory=url;
  openPopupDiretory()
  directory('Main');
}

function getCommonContactsDirectory(url){
  urlDirectory=url;
  openPopupDiretory()
  directory('Main');
}

function directory(action){
  var url=urlDirectory+action+'&key='+$('#key').attr("value");
  $('#users').empty();
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
 function openPopupDiretory()
        {
          //Get the A tag
          var id ='#directory';
          //Get the screen height and width
          

          //Get the window height and width

          var winH = $(window).height();
          var winW = $(window).width();
          var topH=winH/2-$(id).height()/2;
          var topW=winW/2-$(id).width()/2
          //Set the popup window to center
          $('#mask').css('top', topH-5 );
          $('#mask').css('left',topW-5);
          $(id).css('top', topH );
          $(id).css('left',topW);

          //transition effect
          $('#mask').fadeIn(1000);
          $('#mask').fadeTo("slow",0.8);
          $(id).fadeIn(2000);

        }
        $(document).ready(function() {
          //if close button is clicked
          $('.window .close').click(function (e) {
            //Cancel the link behavior
            e.preventDefault();
            $('#users').empty();
            $('#mask').hide();
            $('.window').hide();
          });

          //if mask is clicked
          $('#mask').click(function () {
          $('#users').empty();
            $(this).hide();
            $('.window').hide();
          });

        });


