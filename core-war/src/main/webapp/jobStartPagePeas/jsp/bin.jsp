<%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib prefix="fmtl" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ include file="check.jsp" %>

<c:set var="language" value="${requestScope.resources.language}"/>

<fmt:setLocale value="${language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<fmt:message key="GML.validate" var="validate"/>

<fmt:message key="JSPP.Bin" var="binTitle"/>
<fmt:message key="JSPP.BinRestore" var="binRestoreOp"/>
<fmt:message key="JSPP.BinDelete" var="binDeleteOp"/>
<fmt:message key="JSPP.BinDeleteConfirm" var="binDeleteConfirm"/>
<fmt:message key="JSPP.BinDeleteConfirmSelected" var="binDeleteConfirmSelected"/>
<fmt:message key="JSPP.BinRestoreSelected" var="binRestoreSelected"/>
<fmt:message key="JSPP.BinAfterRestoreAskNavSpace" var="binAfterRestoreAskNavSpace"/>
<fmt:message key="JSPP.BinAfterRestoreAskNavComponent" var="binAfterRestoreAskNavComponent"/>

<fmt:message key="JSPP.instanceHelpInfo" var="infoIcon" bundle="${icons}"/>
<c:url var="infoIcon" value="${infoIcon}"/>
<fmt:message key="JSPP.restoreAll" var="binRestoreAllIcon" bundle="${icons}"/>
<c:url var="binRestoreAllIcon" value="${binRestoreAllIcon}"/>
<fmt:message key="JSPP.deleteAll" var="binDeleteAllIcon" bundle="${icons}"/>
<c:url var="binDeleteAllIcon" value="${binDeleteAllIcon}"/>
<fmt:message key="JSPP.restore" var="binRestoreIcom" bundle="${icons}"/>
<c:url var="binRestoreIcom" value="${binRestoreIcom}"/>
<fmt:message key="JSPP.delete" var="binDeleteIcon" bundle="${icons}"/>
<c:url var="binDeleteIcon" value="${binDeleteIcon}"/>

<c:set var="removedSpaces" value="${requestScope.Spaces}"/>
<c:set var="removedComponents" value="${requestScope.Components}"/>
<c:set var="emptyBin"
	   value="${fn:length(removedSpaces) == 0 && fn:length(removedComponents) == 0}"/>

<view:sp-page>
<view:sp-head-part withCheckFormScript="true">
<view:includePlugin name="qtip"/>
<view:includePlugin name="popup"/>
<script type="text/javascript">
function removeItem(id) {
	sp.popup.confirm("${silfn:escapeJs(binDeleteConfirm)}", function() {
    performRemove({'ItemId' : id});
  });
}

function remove() {
  sp.popup.confirm("${silfn:escapeJs(binDeleteConfirmSelected)}", function() {
    performRemove(sp.form.serializeJson(document.forms.binForm));
  });
}

function performRemove(params) {
  spProgressMessage.show();
  sp.ajaxRequest('RemoveDefinitely')
      .withParams(params)
      .byPostMethod()
      .sendAndPromiseJsonResponse()
      .then(smoothReload);
}

function restoreItem(id) {
  performRestore({'ItemId' : id});
}

function restore() {
  sp.popup.confirm("${silfn:escapeJs(binRestoreSelected)}", function() {
    performRestore(sp.form.serializeJson(document.forms.binForm));
  });
}

function performRestore(params) {
  spProgressMessage.show();
  sp.ajaxRequest('RestoreFromBin')
      .withParams(params)
      .byPostMethod()
      .sendAndPromiseJsonResponse()
      .then(function (restoreContext) {
        const action = {};
        if (restoreContext.spaceIds.length === 1 && restoreContext.componentIds.length === 0) {
          action.confirmMsg = "${silfn:escapeJs(binAfterRestoreAskNavSpace)}";
          action.execute = function() {
            spAdminWindow.loadSpace(restoreContext.spaceIds[0]);
          }
        } else if (restoreContext.spaceIds.length === 0 && restoreContext.componentIds.length === 1) {
          action.confirmMsg = "${silfn:escapeJs(binAfterRestoreAskNavComponent)}";
          action.execute = function() {
            spAdminWindow.loadComponent(restoreContext.componentIds[0]);
          }
        }
        if (action.confirmMsg) {
          spProgressMessage.hide();
          sp.popup.confirm(action.confirmMsg, {
            forceTitle : '',
            callback : action.execute,
            alternativeCallback : smoothReload,
            callbackOnClose : smoothReload
          });
        } else {
          smoothReload();
        }
      });
}

function smoothReload() {
  spProgressMessage.show();
  sp.ajaxRequest('ViewBin').send().then(function(request) {
    sp.updateTargetWithHtmlContent('#binContainer', request.responseText, true);
    spProgressMessage.hide();
  }, spProgressMessage.hide)
}

function jqCheckAll2(id, name) {
   $("input[name='" + name + "'][type='checkbox']").attr('checked', $('#' + id).is(':checked'));
}

$(document).ready(function() {
  // By supplying no content attribute, the library uses each elements title attribute by default
  $('.item-path').qtip({
    content : {
      text : false,
      title : {
        text : '<fmt:message key="GML.path"/>'
      }
    },
    style : {
      tip : true,
      classes : "qtip-shadow qtip-green"
    },
    position : {
      adjust : {
        method : "flip flip"
      },
      at : "bottom center",
      my : "top left",
      viewport : $(window)
    }
  });

$('img.help').each(function() {
	$(this).qtip({
		style : {
			classes : "qtip-shadow qtip-green"
		}, content : {
			text : "<span>" + $(this).attr("title") + "</span>"
		}, position : {
			my : "bottom left",
			at : "top right",
			adjust : {
				method : "flipinvert"
			},
			viewport : $(window)
		}
	});
});

});
</script>
</view:sp-head-part>
<view:sp-body-part cssClass="page_content_admin">
<view:browseBar componentId="${binTitle}"/>
<view:operationPane>
	<view:operation action="javascript:restore()" altText="${binRestoreOp}"
					icon="${binRestoreAllIcon}"/>
	<view:operation action="javascript:remove()" altText="${binDeleteOp}" icon="${binDeleteAllIcon}"/>
</view:operationPane>
<view:window>
<view:frame>

<div id="binContainer">
<form name="binForm" action="" method="post">
<c:choose>

<c:when test="${emptyBin}">
	<view:board>
		<div class="inlineMessage"><fmt:message key="JSPP.BinEmpty"/></div>
	</view:board>
</c:when>

<c:otherwise>
<fmt:message key="JSPP.BinRemoveDate" var="deletionDateLabel"/>
<fmt:message key="GML.operation" var="operationsLabel"/>
<c:if test="${fn:length(removedSpaces) > 0}">
<div id="removedSpaces" style="margin-bottom: 1em;">
<fmt:message key="GML.spaces" var="spaceLabel"/>
<c:set var="checkOp">
	<span style="float:left">${operationsLabel}</span>
	<input type="checkbox" id="checkAllSpaces"
		   onkeydown="jqCheckAll2(this.id, 'SpaceIds')"
		   onclick="jqCheckAll2(this.id, 'SpaceIds')"
		   style="float:left;margin-left:5px;padding:0;background-color:unset;"/>
</c:set>
<view:arrayPane var="binContentSpaces"	routingAddress="ViewBin">
<view:arrayColumn title="${spaceLabel}" sortable="true"/>
<view:arrayColumn title="${deletionDateLabel}" sortable="true"/>
<view:arrayColumn title="${checkOp}" sortable="false"/>
<view:arrayLines var="space" items="${removedSpaces}">
<jsp:useBean id="space" type="org.silverpeas.core.admin.space.SpaceInstLight"/>
	<view:arrayLine>
	<c:choose>
		<c:when test="${space.root}">
			<view:arrayCellText text="${silfn:escapeHtml(space.getName(language))}"
								compareOn="${space.getName(language)}"/>
		</c:when>
		<c:otherwise>
			<view:arrayCellText compareOn="${space.getName(language)}">
				<a href="#" class="item-path" title="${silfn:escapeHtml(space.getPath(' > '))}">
					${space.getName(language)}
				</a>
			</view:arrayCellText>
		</c:otherwise>
	</c:choose>
		<view:arrayCellText compareOn="${space.removalDate}"
				text="${silfn:formatDateAndHour(space.removalDate, language)}&nbsp;(${space.removerName})"/>
		<view:arrayCellText>
				<view:icon iconName="${binRestoreIcom}" altText="${binRestoreOp}"
						   action="javascript:onclick=restoreItem('${space.id}')"/>
				<view:icon iconName="${binDeleteIcon}" altText="${binDeleteOp}"
						   action="javascript:onclick=removeItem('${space.id}')"/>
				<input style="vertical-align: middle" type="checkbox" name="SpaceIds" value="${space.id}"/>
		</view:arrayCellText>
	</view:arrayLine>
</view:arrayLines>
</view:arrayPane>
</div>
</c:if>
<c:if test="${fn:length(removedComponents) > 0}">
<div id="removedSpaces" style="margin-top: 1em;">
<fmt:message key="GML.components" var="componentLabel"/>
<c:set var="checkOp">
	<span style="float:left">${operationsLabel}</span>
	<input type="checkbox" id="checkAllComponents"
		   onkeydown="jqCheckAll2(this.id, 'ComponentIds')"
		   onclick="jqCheckAll2(this.id, 'ComponentIds')"
			style="float:left;margin-left:5px;padding:0;vertical-align:middle;background-color:unset;"/>
</c:set>
<view:arrayPane var="binContentComponents"	routingAddress="ViewBin">
	<view:arrayColumn title="${componentLabel}" sortable="true"/>
	<view:arrayColumn title="${deletionDateLabel}" sortable="true"/>
	<view:arrayColumn title="${checkOp}" sortable="false"/>
	<view:arrayLines var="component" items="${removedComponents}">
			<jsp:useBean id="component" type="org.silverpeas.core.admin.component.model.ComponentInstLight"/>
			<view:arrayLine>
				<view:arrayCellText compareOn="${component.getLabel(language)}">
					<a href="#" class="item-path" title="${silfn:escapeHtml(component.getPath(' > '))}">
						${component.getLabel(language)}
					</a>
				</view:arrayCellText>
				<view:arrayCellText compareOn="${component.removalDate}"
						text="${silfn:formatDateAndHour(component.removalDate, language)}&nbsp;(${component.removerName})"/>
				<view:arrayCellText>
					<view:icon iconName="${binRestoreIcom}" altText="${binRestoreOp}"
							   action="javascript:onclick=restoreItem('${component.id}')"/>
					<view:icon iconName="${binDeleteIcon}" altText="${binDeleteOp}"
							   action="javascript:onclick=removeItem('${component.id}')"/>
					<input style="vertical-align: middle" type="checkbox" name="ComponentIds" value="${component.id}"/>
				</view:arrayCellText>
			</view:arrayLine>
		</view:arrayLines>
</view:arrayPane>
</div>
</c:if>
</c:otherwise>
</c:choose>

</form>
</div>
</view:frame>
</view:window>
<view:progressMessage/>
</view:sp-body-part>
</view:sp-page>