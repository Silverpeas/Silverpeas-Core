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
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:url var="deleteIconUrl" value="/util/icons/delete.gif"/>

<!DOCTYPE html>
<html>
<head>
  <title>Demo VueJS</title>
  <view:looknfeel/>
  <style>
    .attached-popin-demo .silverpeas-attached-popin-content {
      width: 200px;
      min-height: 100px;
      max-height: 300px;
    }

    .attached-popin-demo-content {
      background-color: darkgray;
    }
  </style>
</head>
<body>
<a href="../index.jsp">Go back</a>
<h1>VueJS - Silverpeas's Popin Components</h1>
<p><strong>Fell free to add more!!!</strong></p>
<div id="root">
  <p>
  <div style="display: inline-block">
    <input type="text" v-model="demo1.header">
    <span> | </span>
    <a href="javascript:void(0)" v-on:click="demo1.addItem()">Add item</a>
    <span> | </span>
    <a href="javascript:void(0)" v-on:click="demo1.addSeveralItems(10)">Add 10 items</a>
    <span> | </span>
    <a href="javascript:void(0)" v-on:click="demo1.removeItem()">Remove item</a>
    <span> | </span>
    <a href="javascript:void(0)" v-on:click="demo1.removeSeveralItems(10)">Remove 10 items</a>
    <span> | </span>
    <a href="javascript:void(0)" v-on:click="demo1.removeAllItems()">Remove all</a>
    <span> | </span>
  </div>
  <silverpeas-button-pane>
    <silverpeas-button id="source"
                       v-on:click="popinDisplay = !popinDisplay">
      Show an overlay on this button !
    </silverpeas-button>
    <silverpeas-attached-popin to-element="source"
                               class="attached-popin-demo"
                               anchor="right"
                               v-if="popinDisplay">
      <template v-slot:header>{{demo1.header}}</template>
      <template v-slot:default>
        <div class="attached-popin-demo-content">
          <silverpeas-list v-bind:items="demo1.contentItems">
            <silverpeas-list-item v-for="item in demo1.contentItems" v-bind:key="item.title">
              <div>{{item.title}}</div>
              <div>{{item.content}}</div>
            </silverpeas-list-item>
          </silverpeas-list>
        </div>
      </template>
    </silverpeas-attached-popin>
    <silverpeas-button id="source2"
                       v-on:click="popinDisplay2 = !popinDisplay2">
      Show an overlay on this button ! (with scroll end event and list transition)
    </silverpeas-button>
    <silverpeas-attached-popin to-element="source2"
                               class="attached-popin-demo"
                               v-if="popinDisplay2"
                               v-bind:scroll-end-event="75"
                               v-on:scroll-end="demo1.addSeveralItems()">
      <template v-slot:header>{{demo1.header}}</template>
      <div class="attached-popin-demo-content">
        <silverpeas-list v-bind:items="demo1.contentItems"
                         v-bind:with-fade-transition="true">
          <silverpeas-list-item v-for="item in demo1.contentItems" v-bind:key="item.title">
            <div>{{item.title}}</div>
            <div>{{item.content}}</div>
          </silverpeas-list-item>
        </silverpeas-list>
      </div>
    </silverpeas-attached-popin>
  </silverpeas-button-pane>
  <iframe src="javascript:void(0)" style="width: 1200px;height: 300px;background-color: #0a6aa1" title="background"></iframe>
  </p>
</div>
<script type="text/javascript">
  window.vm = SpVue.createApp({
    data : function() {
      return {
        popinDisplay : false,
        popinDisplay2 : false,
        demo1 : {
          header : 'Header',
          contentItems : [],
          count : 0,
          addItem : function() {
            this.count++;
            this.contentItems.push({
              title : 'Title ' + this.count,
              content : 'Content ' + this.count
            });
          },
          addSeveralItems : function(nb) {
            nb = nb ? nb : 10;
            for (let i = 0; i < nb; i++) {
              this.addItem();
            }
          },
          removeItem : function() {
            this.contentItems.splice(0, 1);
          },
          removeSeveralItems : function(nb) {
            this.contentItems.splice(0, nb);
          },
          removeAllItems : function() {
            this.contentItems = [];
          }
        }
      }
    }
  }).mount('#root');
</script>
</body>
</html>