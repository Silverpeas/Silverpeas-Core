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
  <title>Demo VueJS - Dynamic components</title>
  <view:looknfeel/>
  <view:includePlugin name="datepicker"/>
  <style>
    .label {
      font-size: 1em;
      font-weight: bolder;
      margin-top: 15px;
    }

    input.dateToPick {
      width: 135px !important;
    }
  </style>
</head>
<body>
<a href="../index.jsp">Go back</a>
<h1>VueJS - Silverpeas's dynamic components with v-model</h1>
<div id="root">
  <div id="main-app-el">
    <div class="label">Using directly 'silverpeas-date-picker' component into template</div>
    <silverpeas-date-picker v-bind:zone-id="zoneId"
                            v-model="filledDateDirect"></silverpeas-date-picker>
    <div>{{ filledDateDirect }}</div>

    <div class="label">Render 'silverpeas-date-picker' component into DOM element by using $renderComponent</div>
    <div id="dock-container"></div>
    <div>{{ filledDate$renderComponent }}</div>

    <div class="label">Render 'my-dynamic-date-picker' using Vue.resolveComponent('silverpeas-date-picker')</div>
    <my-dynamic-date-picker v-bind:zone-id="zoneId"
                            v-model="filledDateVueRenderComponent"></my-dynamic-date-picker>
    <div>{{ filledDateVueRenderComponent }}</div>
  </div>
</div>
<script type="text/javascript">
  //# sourceURL=/silverpeas/demo/vuejs/dynamic-component.js
  SpVue.component('my-dynamic-date-picker', {
    props: ['modelValue', 'zoneId'],
    emits: ['update:modelValue'],
    render : function() {
      return Vue.h(Vue.resolveComponent('silverpeas-date-picker'), {
        zoneId : this.zoneId,
        modelValue: this.modelValue,
        'onUpdate:modelValue': function(value) {
          this.$emit('update:modelValue', value);
        }.bind(this)
      })
    }
  })

  window.vm = SpVue.createApp({
    data : function() {
      return {
        zoneId : 'Europe/Paris',
        filledDateDirect : '2024-04-17T00:00:00+02:00',
        filledDate$renderComponent : '2024-04-18T00:00:00+02:00',
        filledDateVueRenderComponent : '2024-04-19T00:00:00+02:00'
      }
    },
    mounted : function() {
      const $dock = document.querySelector('#dock-container');
      this.$renderComponent('silverpeas-date-picker', {
        zoneId : this.zoneId,
        modelValue : this.filledDate$renderComponent,
        'onUpdate:modelValue': function(value) {
          this.filledDate$renderComponent = value;
        }.bind(this)
      }, $dock);
    }
  }).mount('#root');
</script>
<h1>VueJS - Silverpeas's dynamic app using component with v-model (template will be replaced by render method result)</h1>
<div id="root2">
  <div>
    <p>This app template will be replaced</p>
  </div>
</div>
<script type="text/javascript">
  //# sourceURL=/silverpeas/demo/vuejs/dynamic-component-2.js
  window.vm2 = SpVue.createApp({
    data : function() {
      return {
        zoneId : 'Europe/Paris',
        filledDate : '2024-04-17T00:00:00+02:00'
      }
    },
    render : function() {
      const datePickerVNode = Vue.h(Vue.resolveComponent('silverpeas-date-picker'), {
        zoneId : this.zoneId,
        modelValue: this.filledDate,
        'onUpdate:modelValue': function(value) {
          this.filledDate = value;
        }.bind(this)
      });

      return Vue.h('div', {
        'class' : 'dynamic-app-template-with-render-method'
      }, [
          datePickerVNode,
          Vue.h('p', null, this.filledDate)
      ]);
    }
  }).mount('#root2');
</script>
</body>
</html>