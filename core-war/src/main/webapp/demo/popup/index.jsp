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
<title>Demo popups</title>
<view:looknfeel/>
<view:includePlugin name="popup"/>
<script type="text/javascript">
function showBasic() {
	$('#message').popup('basic', {
		title : "Title of the popup"
	});
}

function showInformation() {
	$('#message').popup('information', {
	    callback : function() {
	      alert("Button has been clicked !");
	      return true;
	    }
	});
}

function showHelp() {
	$('#message').popup('help');
}

function showValidation() {
	$('#message').popup('validation', {
	    title : "Title of the popup",
		callback : function() {
	      alert("Message validated by the user !");
	      return true;
	    }
	});
}

function showConfirmation() {
	$('#message').popup('confirmation', {
		title : "Title of the popup",
	    callback : function() {
	      alert("Message confirmed by the user !");
	      return true;
	    }
	});
}

</script>
</head>
<body>
<h1>Usage examples of Silverpeas plugin 'popup'</h1>
<ul>
<li><a href="javascript:onclick=showBasic()">Display basic message (No buttons)</a></li>
<li><a href="javascript:onclick=showInformation()">Display information message (One button)</a></li>
<li><a href="javascript:onclick=showHelp()">Display help message (One button + specific class)</a></li>
<li><a href="javascript:onclick=showValidation()">Display validation message (Two buttons)</a></li>
<li><a href="javascript:onclick=showConfirmation()">Afficher message de confirmation (Two buttons + one icon)</a></li>
</ul>

<div id="message" style="display: none">
Content of the message to be displayed...
</div>
</body>
</html>