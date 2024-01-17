<%--
  ~ Copyright (C) 2000 - 2024 Silverpeas
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
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<!DOCTYPE html>
<html>
<head>
  <title>Demo VueJS</title>
  <view:looknfeel/>
  <style>
    .demo-ok {
      color: green;
    }
    .demo-nok {
      color: red;
    }
  </style>
</head>
<body>
<a href="../index.jsp">Go back</a>
<h1>VueJS - Silverpeas's Form Input Components</h1>
<p><strong>Fell free to add more!!!</strong></p>
<div id="root">
  Silverpeas's inputs MUST be included into a silverpeas-form-pane component.<br/>
  silverpeas-form-pane component displays by default validate and cancel buttons.
  <p>
    Text input can be mandatory.<br/>
    <silverpeas-link
        v-bind:title="demo1.mandatory ? 'Input is mandatory' : 'Input is not mandatory'"
        v-on:click="demo1.validationOk = false;demo1.mandatory = !demo1.mandatory">Click this link to change mandatory state.</silverpeas-link>
    <span> | </span>
    <silverpeas-link
        title="demo1.displayInputLabel ? 'Input label is displayed' : 'Input label is not displayed'"
        v-on:click="demo1.displayInputLabel = !demo1.displayInputLabel">Click this link to show/hide input label.</silverpeas-link>
    <silverpeas-form-pane
        v-bind:mandatory-legend="demo1.mandatory"
        v-on:cancel="demo1.validationOk = false;demo1.value = '';demo1.mandatory = true"
        v-on:data-update="demo1.validationOk = true"
        v-on:validation-fail="demo1.validationOk = false">
      <silverpeas-label for="demo-text-input-1" v-if="demo1.displayInputLabel"
                        v-bind:mandatory="demo1.mandatory">An input label</silverpeas-label>
      <silverpeas-text-input
          id="demo-text-input-1"
          v-model="demo1.value"
          v-bind:mandatory="demo1.mandatory"></silverpeas-text-input>
      <span v-bind:class="{'demo-ok':demo1.mandatory && demo1.validationOk}"
            v-if="!demo1.mandatory || demo1.validationOk">{{demo1.value}}</span>
    </silverpeas-form-pane>
  </p>
</div>
<script type="text/javascript">
  window.vm = SpVue.createApp({
    data : function() {
      return {
        demo1 : {
          value : '',
          validationOk : false,
          mandatory : true,
          displayInputLabel : true
        }
      }
    }
  }).mount('#root');
</script>
</body>
</html>