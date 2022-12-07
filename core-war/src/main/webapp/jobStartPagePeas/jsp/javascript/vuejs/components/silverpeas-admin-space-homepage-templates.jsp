<%--
  ~ Copyright (C) 2000 - 2022 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<c:set var="language" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.jobStartPagePeas.multilang.jobStartPagePeasBundle"/>

<fmt:message key="JSPP.main" var="defaultStartPageLabel"/>
<fmt:message key="JSPP.peas" var="peasLabel"/>
<fmt:message key="JSPP.portlet" var="portletLabel"/>
<fmt:message key="JSPP.webPage" var="webPageLabel"/>
<fmt:message key="JSPP.SpaceHomepage.URL.help" var="webPageHelpMessage"/>
<fmt:message key="JSPP.SpaceHomepage.save.success" var="successSaveMessage"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="space-homepage-popin">
  <div class="space-homepage-management">
    <div v-sp-init>
      {{addMessages({
      successSaveMessage : '${silfn:escapeJs(successSaveMessage)}'
      })}}
    </div>
    <silverpeas-popin v-if="spacePath && spaceApps"
                      v-on:api="homepagePopinApi = $event"
                      v-bind:title="title"
                      v-bind:dialog-class="'space-homepage-management'"
                      type="validation"
                      v-bind:minWidth="650">
      <silverpeas-form-pane v-on:api="homepageFormApi = $event"
                            v-bind:manual-actions="true"
                            v-bind:mandatory-legend="homepageFormMandatoryLegend"
                            v-on:data-update="validate">
        <silverpeas-admin-space-homepage-form
            v-bind:space-path="spacePath"
            v-bind:space-apps="spaceApps"
            v-bind:homepage="homepage"
            v-on:choice="homepageFormMandatoryLegend = ($event === 'HTML_PAGE')"></silverpeas-admin-space-homepage-form>
      </silverpeas-form-pane>
    </silverpeas-popin>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="space-homepage-form">
  <div class="space-homepage-form">
    <div class="space-path">{{spacePath.format()}}</div>
    <div class="field">
      <div class="champs">
        <input id="shpDefaultStartPage" type="radio" name="choix" value="STANDARD" v-model="choice"/>
      </div>
      <silverpeas-label class="txtlibform" for="shpDefaultStartPage">${defaultStartPageLabel}</silverpeas-label>
    </div>
    <div class="field">
      <div class="champs">
        <input id="shpPeas" type="radio" name="choix" value="COMPONENT_INST" v-model="choice"/>
      </div>
      <silverpeas-label class="txtlibform" for="shpPeas">${peasLabel}</silverpeas-label>
      <select name="peas" size="1" v-model="appId">
        <option v-for="app in spaceApps" v-bind:value="app.id">{{app.label}}</option>
      </select>
    </div>
    <div class="field">
      <div class="champs">
        <input id="shpPortlet" type="radio" name="choix" value="PORTLET" v-model="choice"/>
      </div>
      <silverpeas-label class="txtlibform" for="shpPortlet">${portletLabel}</silverpeas-label>
    </div>
    <div class="field">
      <div class="champs">
        <input id="shpWebPage" type="radio" name="choix" value="HTML_PAGE" v-model="choice"/>
      </div>
      <silverpeas-label class="txtlibform" for="shpWebPage"
                        v-bind:mandatory="urlIsMandatory">${webPageLabel}</silverpeas-label>
      <silverpeas-text-input id="shpWebPageExtra" label-id="shpWebPage" name="URL"
                             v-bind:size="60" v-bind:maxlength="255" v-model="url"/>
      <template v-if="urlIsMandatory">
        <silverpeas-fade-transition>
          <div class="inlineMessage" id="urlhelp" v-html="'${silfn:escapeJs(webPageHelpMessage)}'"></div>
        </silverpeas-fade-transition>
      </template>
    </div>
  </div>
</silverpeas-component-template>