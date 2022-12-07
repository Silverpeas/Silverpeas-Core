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
<%@ page import="org.silverpeas.core.util.JSONCodec" %>
<%@ page import="org.silverpeas.core.i18n.I18NHelper" %>
<%@ page import="java.util.Optional" %>
<%@ page import="org.silverpeas.core.util.StringUtil" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<c:set var="language" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<jsp:useBean id="language" type="java.lang.String"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.multilang.generalMultilang"/>

<c:set var="allUserLanguagesAsJsArray"><%=Optional
    .of(language)
    .map(l -> JSONCodec.encodeArray(a -> {
      I18NHelper.getAllUserTranslationsOfContentLanguages(l).forEach( t ->
          a.addJSONObject(o -> o.put("id", t.getCode()).put("label", t.getLabel())));
      return a;
    }))
    .orElse(StringUtil.EMPTY)%></c:set>

<c:url var="mandatoryIcons" value="/util/icons/mandatoryField.gif"/>

<fmt:message var="validateLabel" key="GML.validate"/>
<fmt:message var="cancelLabel" key="GML.cancel"/>
<c:set var="theFieldLabel"><fmt:message key='GML.thefield'/></c:set>
<c:set var="mandatoryMessage">${theFieldLabel} {0} <fmt:message key='GML.MustBeFilled'/></c:set>
<c:set var="mustContainsURLMessage">${theFieldLabel} {0} <fmt:message key='GML.MustContainsURL'/></c:set>
<c:set var="mustBeDifferentFromMessage">${theFieldLabel} {0} <fmt:message key='GML.MustBeDifferentFrom'/> {1}</c:set>
<c:set var="mustBePositiveIntegerMessage">${theFieldLabel} {0} <fmt:message key='GML.MustContainsPositiveNumber'/></c:set>
<c:set var="nbMaxMessage">${theFieldLabel} {0} <fmt:message key='GML.data.error.message.string.limit'><fmt:param value="{1}"/></fmt:message></c:set>
<c:set var="correctDateMessage">${theFieldLabel} {0} <fmt:message key='GML.MustContainsCorrectDate'/></c:set>
<c:set var="correctHourMessage">${theFieldLabel} {0} <fmt:message key='GML.MustContainsCorrectHour'/></c:set>
<c:set var="correctPeriodMessage">${theFieldLabel} {1} <fmt:message key='GML.MustContainsPostDateTo'/> {0}</c:set>
<c:set var="correctEndDateIncludedPeriodMessage">${theFieldLabel} {1} <fmt:message key='GML.MustContainsPostOrEqualDateTo'/> {0}</c:set>

<!-- ########################################################################################### -->
<silverpeas-component-template name="form-pane">
  <div class="silverpeas-form-pane">
    <div v-sp-init>
      {{addMessages({
      mandatory : '${silfn:escapeJs(mandatoryMessage)}',
      mustContainsURLMessage : '${silfn:escapeJs(mustContainsURLMessage)}',
      mustBeDifferentFrom : '${silfn:escapeJs(mustBeDifferentFromMessage)}',
      mustBePositiveInteger : '${silfn:escapeJs(mustBePositiveIntegerMessage)}',
      nbMax : '${silfn:escapeJs(nbMaxMessage)}',
      correctDate : '${silfn:escapeJs(correctDateMessage)}',
      correctTime : '${silfn:escapeJs(correctHourMessage)}',
      correctPeriod : '${silfn:escapeJs(correctPeriodMessage)}',
      correctEndDateIncludedPeriod : '${silfn:escapeJs(correctEndDateIncludedPeriodMessage)}'
      })}}
    </div>
    <div v-if="isHeader" class="header"><slot name="header"></slot></div>
    <div v-if="isBody" class="body"><slot></slot></div>
    <div v-if="isFooter" class="footer"><slot name="footer"></slot></div>

    <div v-if="isLegend" class="legend">
      <slot name="legend"></slot>
      <template v-if="mandatoryLegend">
        <img alt="mandatory" src="${mandatoryIcons}" width="5" height="5"/>&nbsp;
        <fmt:message key='GML.requiredField'/>
      </template>
    </div>

    <div v-if="!isManualActions">
      <silverpeas-button-pane>
        <silverpeas-button v-on:click.native="api.validate()"> ${validateLabel} </silverpeas-button>
        <silverpeas-button v-on:click.native="api.cancel()">${cancelLabel}</silverpeas-button>
      </silverpeas-button-pane>
    </div>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="button">
  <a class="silverpeas-button"
     v-bind:class="{'sp_button':!isIconBehavior,'sp_icon':isIconBehavior}"
     v-bind:title="title" href="javascript:void(0)">
    <slot v-if="!isIconBehavior"></slot>
    <img v-else v-bind:src="iconUrl" alt=""/></a>
</silverpeas-component-template>

<fmt:message key="GML.none" var="noneLabel"/>
<fmt:message key="GML.noneF" var="noneLabelFemale"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="list">
  <div class="silverpeas-list-container">
    <div v-sp-init>
      {{addMessages({
      noItemLabel : '${noneLabel}',
      noItemLabelFemale : '${noneLabelFemale}'
      })}}
    </div>
    <slot name="before"></slot>
    <template v-if="isData">
      <template v-if="withFadeTransition">
        <transition-group name="normal-fade" tag="ul" class="silverpeas-list" appear
                          v-on:before-enter="$emit('before-enter',$event)"
                          v-on:enter="$emit('enter',$event)"
                          v-on:after-enter="$emit('after-enter',$event)"
                          v-on:before-leave="$emit('before-leave',$event)"
                          v-on:leave="$emit('leave',$event)"
                          v-on:after-leave="$emit('after-leave',$event)">
          <slot></slot>
        </transition-group>
      </template>
      <ul v-else class="silverpeas-list">
        <slot></slot>
      </ul>
    </template>
    <div class="no-item" v-if="!isData && !$slots.noItem" v-html="noItemMessage"></div>
    <slot v-if="!isData" name="noItem"></slot>
    <slot name="after"></slot>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="list-item">
  <li class="silverpeas-list-item">
    <slot name="header"></slot>
    <div class="silverpeas-list-item-body">
      <div class="silverpeas-list-item-content">
        <slot></slot>
      </div>
      <div v-if="$slots.actions" class="silverpeas-list-item-actions">
        <silverpeas-button-pane>
          <slot name="actions"></slot>
        </silverpeas-button-pane>
      </div>
    </div>
    <slot name="footer"></slot>
  </li>
</silverpeas-component-template>

<fmt:message var="permalinkLabel" key="GML.permalink"/>
<fmt:message var="permalinkCopyLabel" key="GML.permalink.copy"/>
<fmt:message var="permalinkCopyOkMessage" key="GML.permalink.copy.ok"/>
<fmt:message var="permalinkHelp" key="GML.permalink.help"/>
<c:url var="linkIconUrl" value="/util/icons/link.gif"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="permalink">

  <fmt:message var="permalinkLabel" key="GML.permalink"/>
  <fmt:message var="permalinkCopyLabel" key="GML.permalink.copy"/>
  <fmt:message var="permalinkCopyOkMessage" key="GML.permalink.copy.ok"/>
  <fmt:message var="permalinkHelp" key="GML.permalink.help"/>

  <c:url var="linkIconUrl" value="/util/icons/link.gif"/>

  <div class="permalink" v-bind:class="{'simple' : simple}">
    <div v-sp-init>
      {{addMessages({
      copyOk : '${silfn:escapeJs(permalinkCopyOkMessage)}'
      })}}
    </div>
    <div v-if="isFull" ref="fullContainer"></div>
    <div v-else>
      <a v-bind:class="{'sp-permalink': !noHrefHook, 'sp-direct-permalink': noHrefHook}" v-bind:href="link" title="${permalinkLabel}">
        <img src="${linkIconUrl}" alt="${permalinkLabel}" />
      </a>
      <input ref="linkInput" type="text" v-bind:value="link" />
      <silverpeas-button title="${permalinkCopyLabel}" v-on:click.native="copyLink()" class="copy-to-clipboard">${permalinkCopyLabel}</silverpeas-button>
    </div>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="popin">
  <div>
    <div class="silverpeas-popin" style="display: none" ref="container">
      <slot></slot>
    </div>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="attached-popin">
  <silverpeas-fade-transition v-bind:duration-type="fadeDurationType">
    <div class="silverpeas-attached-popin"
         ref="popin"
         v-bind:style="{'minWidth':minWidth+'px','maxWidth':maxWidth+'px','minHeight':minHeight+'px','maxHeight':maxHeight+'px'}">
      <div v-if="$slots.header" class="silverpeas-attached-popin-header"><slot name="header"></slot></div>
      <div class="silverpeas-attached-popin-content" ref="content"><slot></slot></div>
    </div>
  </silverpeas-fade-transition>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="link">
  <a href="javascript:void(0)" v-bind:alt="help" v-on:click="hideTitle"><slot></slot></a>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="label">
  <label class="silverpeas-label" v-bind:id="id" v-bind:for="forId">
    <slot></slot>
    <silverpeas-mandatory-indicator v-if="isMandatory"></silverpeas-mandatory-indicator>
  </label>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="text-input">
  <div class="silverpeas-text-input">
    <input type="text"
           v-bind:id="id" v-bind:name="name"
           v-bind:class="inputClass" v-bind:size="size"
           v-bind:maxlength="maxlength" v-bind:disabled="disabled"
           v-model="model"/>
    <silverpeas-mandatory-indicator v-if="mandatory"></silverpeas-mandatory-indicator>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="hidden-input">
  <div class="silverpeas-hidden-input">
    <input type="hidden"
           v-bind:id="id" v-bind:name="name"
           v-model="model"/>
    <silverpeas-mandatory-indicator v-if="mandatory"></silverpeas-mandatory-indicator>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="url-input">
  <silverpeas-text-input class="silverpeas-url-input"
         v-bind:id="id" v-bind:label-id="labelId" v-bind:name="name"
         v-bind:title="title" v-bind:placeholder="placeholder"
         v-bind:class="inputClass" v-bind:size="size"
         v-bind:maxlength="maxlength" v-bind:disabled="disabled" v-bind:mandatory="mandatory"
         v-model="model"></silverpeas-text-input>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="multiline-text-input">
  <div class="silverpeas-multiline-text-input">
    <textarea v-bind:cols="cols" v-bind:rows="rows"
              v-bind:id="id" v-bind:name="name" v-bind:class="inputClass"
              v-bind:maxlength="maxlength" v-bind:disabled="disabled"
              v-model="model"></textarea>
    <silverpeas-mandatory-indicator v-if="mandatory"></silverpeas-mandatory-indicator>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="radio-input">
  <div class="silverpeas-radio-input">
    <input type="radio"
           v-bind:id="id" v-bind:name="name"
           v-bind:class="cssClasses" v-bind:disabled="disabled"
           v-bind:value="value" v-model="model"/>
    <silverpeas-mandatory-indicator v-if="mandatory"></silverpeas-mandatory-indicator>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="checkbox-input">
  <div class="silverpeas-checkbox-input">
    <input type="checkbox"
           v-bind:id="id" v-bind:name="name"
           v-bind:class="cssClasses" v-bind:disabled="disabled"
           v-bind:value="value" v-model="model"/>
    <silverpeas-mandatory-indicator v-if="mandatory"></silverpeas-mandatory-indicator>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="select">
  <div class="silverpeas-select">
    <select v-bind:id="id" v-bind:name="name"
            v-bind:disabled="disabled" v-bind:class="inputClass"
            v-model="model">
      <slot></slot>
    </select>
    <silverpeas-mandatory-indicator v-if="mandatory"></silverpeas-mandatory-indicator>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="select-language">
  <silverpeas-select class="silverpeas-select-language"
                     v-bind:id="id" v-bind:label-id="labelId" v-bind:name="name"
                     v-bind:title="title"
                     v-bind:class="inputClass"
                     v-bind:disabled="disabled" v-bind:mandatory="mandatory"
                     v-model="model">
    <option v-for='language in ${allUserLanguagesAsJsArray}'
            v-bind:key="language.id" v-bind:value="language.id">{{language.label}}</option>
  </silverpeas-select>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<fmt:message var="fromDateLabel" key='GML.date.from'/>
<fmt:message var="atLabel" key='GML.date.hour.to'/>
<fmt:message var="toLabel" key='GML.date.to'/>
<silverpeas-component-template name="event-period">
  <div class="event-period">
    <div v-if="isInDays()">
      <span v-if="onSameDay()">{{startDate() | displayAsDate}}</span>
      <span v-if="!onSameDay()">${fromDateLabel} {{startDate() | displayAsDate}}</span>
      <span v-if="!onSameDay()">${toLabel} {{endDate() | displayAsDate}}</span>
    </div>
    <div v-if="!isInDays() && onSameDay()">
      <span>{{startDate() | displayAsDate}} - {{startDate() | displayAsTime}} ${atLabel} {{endDate() | displayAsTime}}</span>
    </div>
    <div v-if="!isInDays() && !onSameDay()">
      <span>${fromDateLabel} {{startDate() | displayAsDate}} ${atLabel} {{startDate() | displayAsTime}}</span>
      <span>${toLabel} {{endDate() | displayAsDate}} ${atLabel} {{endDate() | displayAsTime}}</span>
    </div>
  </div>
</silverpeas-component-template>