<%--
  Copyright (C) 2000 - 2013 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception.  You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title>Demo keycodes</title>
  <view:looknfeel/>
  <script type="text/javascript">
    $(document).ready(function() {
      $('#keycode').on('keydown', function(e) {
        e.preventDefault();
        var keyCode = (e.which) ? e.which : e.keyCode;
        var keyLabel;
        switch (keyCode) {
          case  8:
            keyLabel = "backspace"; //  backspace
            break;
          case  9:
            keyLabel = "tab"; //  tab
            break;
          case  13:
            keyLabel = "enter"; //  enter
            break;
          case  16:
            keyLabel = "shift"; //  shift
            break;
          case  17:
            keyLabel = "ctrl"; //  ctrl
            break;
          case  18:
            keyLabel = "alt"; //  alt
            break;
          case  19:
            keyLabel = "pause/break"; //  pause/break
            break;
          case  20:
            keyLabel = "caps lock"; //  caps lock
            break;
          case  27:
            keyLabel = "escape"; //  escape
            break;
          case  32:
            keyLabel = "space"; // space
            break;
          case  33:
            keyLabel = "page up"; // page up, to avoid displaying alternate character and confusing people
            break;
          case  34:
            keyLabel = "page down"; // page down
            break;
          case  35:
            keyLabel = "end"; // end
            break;
          case  36:
            keyLabel = "home"; // home
            break;
          case  37:
            keyLabel = "left arrow"; // left arrow
            break;
          case  38:
            keyLabel = "up arrow"; // up arrow
            break;
          case  39:
            keyLabel = "right arrow"; // right arrow
            break;
          case  40:
            keyLabel = "down arrow"; // down arrow
            break;
          case  45:
            keyLabel = "insert"; // insert
            break;
          case  46:
            keyLabel = "delete"; // delete
            break;
          case  91:
            keyLabel = "left window"; // left window
            break;
          case  92:
            keyLabel = "right window"; // right window
            break;
          case  93:
            keyLabel = "select key"; // select key
            break;
          case  96:
            keyLabel = "numpad 0"; // numpad 0
            break;
          case  97:
            keyLabel = "numpad 1"; // numpad 1
            break;
          case  98:
            keyLabel = "numpad 2"; // numpad 2
            break;
          case  99:
            keyLabel = "numpad 3"; // numpad 3
            break;
          case  100:
            keyLabel = "numpad 4"; // numpad 4
            break;
          case  101:
            keyLabel = "numpad 5"; // numpad 5
            break;
          case  102:
            keyLabel = "numpad 6"; // numpad 6
            break;
          case  103:
            keyLabel = "numpad 7"; // numpad 7
            break;
          case  104:
            keyLabel = "numpad 8"; // numpad 8
            break;
          case  105:
            keyLabel = "numpad 9"; // numpad 9
            break;
          case  106:
            keyLabel = "multiply"; // multiply
            break;
          case  107:
            keyLabel = "add"; // add
            break;
          case  109:
            keyLabel = "subtract"; // subtract
            break;
          case  110:
            keyLabel = "decimal point"; // decimal point
            break;
          case  111:
            keyLabel = "divide"; // divide
            break;
          case  112:
            keyLabel = "F1"; // F1
            break;
          case  113:
            keyLabel = "F2"; // F2
            break;
          case  114:
            keyLabel = "F3"; // F3
            break;
          case  115:
            keyLabel = "F4"; // F4
            break;
          case  116:
            keyLabel = "F5"; // F5
            break;
          case  117:
            keyLabel = "F6"; // F6
            break;
          case  118:
            keyLabel = "F7"; // F7
            break;
          case  119:
            keyLabel = "F8"; // F8
            break;
          case  120:
            keyLabel = "F9"; // F9
            break;
          case  121:
            keyLabel = "F10"; // F10
            break;
          case  122:
            keyLabel = "F11"; // F11
            break;
          case  123:
            keyLabel = "F12"; // F12
            break;
          case  144:
            keyLabel = "num lock"; // num lock
            break;
          case  145:
            keyLabel = "scroll lock"; // scroll lock
            break;
          case  186:
            keyLabel = ";"; // semi-colon
            break;
          case  187:
            keyLabel = "="; // equal-sign
            break;
          case  188:
            keyLabel = ","; // comma
            break;
          case  189:
            keyLabel = "-"; // dash
            break;
          case  190:
            keyLabel = "."; // period
            break;
          case  191:
            keyLabel = "/"; // forward slash
            break;
          case  192:
            keyLabel = "`"; // grave accent
            break;
          case  219:
            keyLabel = "["; // open bracket
            break;
          case  220:
            keyLabel = "\\"; // back slash
            break;
          case  221:
            keyLabel = "]"; // close bracket
            break;
          case  222:
            keyLabel = "'"; // single quote
            break;
          default :
            keyLabel = String.fromCharCode(keyCode);
            break;
        }
        $(this).val(keyLabel);
        $('#keycodeResult').val('' + keyCode);
        return false;
      });

      var i;
      var $list = $('#list');
      var $div;
      $list.append($div);
      var nbDisplayed =  0;
      var keyCode;
      var keyCodeUndefined = String.fromCharCode(0);
      for (i = 0; i <= 10000; i++) {
        keyCode = String.fromCharCode(i);
        if (nbDisplayed%50 == 0 && (!$div || $div.html().length > 0)) {
          $div = $('<div>').css('float', 'left').css('margin-right', '20px');
          $list.append($div);
        }
        if (keyCode != keyCodeUndefined) {
          nbDisplayed++;
          if ($div.html().length > 0) {
            $div.append('<br/>');
          }
          $div.append('<b>' + i + '</b> : ' + keyCode);
        }
      }
    });
  </script>
</head>
<body style="height: 100%">
<div id="list" style="display: inline-table">
  <div id="container" style="">
    <label for="keycode">Push a key :</label>
    <input id="keycode" type="text" size="10" maxlength="255">
    <input id="keycodeResult" type="text" size="10" maxlength="255" disabled="true">
  </div>
</div>
</body>
</html>
