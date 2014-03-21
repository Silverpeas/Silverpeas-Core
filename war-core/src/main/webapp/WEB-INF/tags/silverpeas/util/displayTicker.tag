<%--
  Copyright (C) 2000 - 2013 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib prefix="plugins" tagdir="/WEB-INF/tags/silverpeas/plugins" %>

<%@ attribute name="ticker" required="true" type="com.silverpeas.look.Ticker" description="The ticker to display" %>
<%@ attribute name="language" required="false" type="java.lang.String" description="The language to display content" %>

<c:if test="${ticker != null}">
<script type="text/javascript">
$(document).ready(function() {
	$('#js-news').ticker({
	    speed: ${ticker.getParam('speed')},
	    pauseOnItems: ${ticker.getParam('pauseOnItems')},
	    htmlFeed: true,
	    controls: ${ticker.getParam('controls')},
	    displayType: '${ticker.getParam('displayType')}',
	    fadeInSpeed: ${ticker.getParam('fadeInSpeed')},
	    fadeOutSpeed: ${ticker.getParam('fadeOutSpeed')},
	    titleText: '${ticker.label}'
	});
});
</script>
<ul id="js-news" class="js-hidden">
	<c:forEach var="item" items="${ticker.items}">
		<li class="news-item">
			<c:if test="${silfn:isDefined(item.hour)}">
	    	  <span>${item.hour} - </span>
	    	</c:if>
	    	<c:if test="${ticker.linkOnItem}">
	    		<a href="${item.permalink}" target="_top">
	    	</c:if>
	    	<span title="${silfn:escapeHtml(item.getDescription(language))}">${item.getName(language)}</span>
	    	<c:if test="${ticker.linkOnItem}">
	    		</a>
	    	</c:if>
	    </li>
	</c:forEach>
</ul>
</c:if>