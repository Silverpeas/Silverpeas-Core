<%--
  ~ Copyright (C) 2000 - 2021 Silverpeas
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
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
  <style type="text/css">
    .fade-enter-active, .fade-leave-active {
      transition: all 1.5s;
    }
    .fade-leave, .fade-enter-to {
      opacity: 1;
    }
    .fade-enter, .fade-leave-to {
      opacity: 0;
    }
  </style>
</head>
<body>
<a href="../index.jsp">Go back</a>
<h1>VueJS - Silverpeas's List Components</h1>
<p><strong>Fell free to add more!!!</strong></p>
<div id="root">
  <p>
    <silverpeas-link
        title="Destroy list"
        v-on:click.native="demo1.list = undefined">Destroy list.</silverpeas-link>
    <span> | </span>
    <silverpeas-link
        title="Destroy list"
        v-on:click.native="demo1.list = []">Empty list.</silverpeas-link>
    <br/>
    <silverpeas-link
        v-if="demo1.list"
        v-bind:title="demo1.fade ? 'Fade is active on list' : 'Fade is not active on list'"
        v-on:click.native="demo1.fade = !demo1.fade">Click this link to activate/deactivate fade on list.</silverpeas-link>
    <br/>
    <silverpeas-link
        v-if="demo1.list"
        v-bind:title="demo1.header ? 'Header is shown' : 'Header is hidden'"
        v-on:click.native="demo1.header = !demo1.header">Click this link to hide/display header.</silverpeas-link>
    <br/>
    <silverpeas-link
        v-if="demo1.list"
        v-bind:title="demo1.actions ? 'Actions on items are shown' : 'Actions on items are hidden'"
        v-on:click.native="demo1.actions = !demo1.actions">Click this link to hide/display actions on items.</silverpeas-link>
    <br/>
    <silverpeas-link
        v-if="demo1.list"
        v-bind:title="demo1.feminineGender ? 'Feminine gender' : 'Male gender'"
        v-on:click.native="demo1.feminineGender = !demo1.feminineGender">Click this link to change item gender.</silverpeas-link>
    <br/>
    <silverpeas-link
        v-if="demo1.list"
        v-bind:title="demo1.footer ? 'Footer is shown' : 'Footer is hidden'"
        v-on:click.native="demo1.footer = !demo1.footer">Click this link to hide/display footer.</silverpeas-link>
    <silverpeas-button-pane v-if="demo1.list">
      <silverpeas-button v-on:click.native="demo1.addData()">Add data</silverpeas-button>
      <silverpeas-button v-on:click.native="demo1.removeLastData()"
                         v-sp-disable-if="!demo1.list.length">Remove last data</silverpeas-button>
    </silverpeas-button-pane>
    <silverpeas-list v-if="demo1.list"
                     v-bind:with-fade-transition="demo1.fade"
                     v-bind:items="demo1.list"
                     v-bind:item-feminine-gender="demo1.feminineGender">
      <template v-slot:before>
        <transition name="fade" appear>
          <h3 v-if="demo1.header">HEADER PART</h3>
        </transition>
      </template>
      <template v-slot:after>
        <transition name="fade">
          <h3 v-if="demo1.footer">FOOTER PART</h3>
        </transition>
      </template>
      <silverpeas-list-item v-for="item in demo1.list" v-bind:key="item.id">
        <span>Id = {{item.id}}</span> | <span>Content = {{item.content}}</span>
        <template slot="actions" v-if="demo1.actions">
          <silverpeas-button v-on:click.native="demo1.removeData(item)"
                             v-bind:title="'Remove item with id ' + item.id"
                             icon-url="${deleteIconUrl}">Remove</silverpeas-button>
        </template>
      </silverpeas-list-item>
    </silverpeas-list>
  </p>
</div>
<script type="text/javascript">
  var __count = -1;
  window.vm = new Vue({
    el : '#root',
    data : function() {
      return {
        demo1 : {
          feminineGender : false,
          fade : true,
          header : true,
          footer : true,
          actions : true,
          list : undefined,
          addData : function() {
            this.list.push({id:++__count,content:('Content ' + __count)})
          },
          removeLastData : function() {
            this.list.splice(this.list.length - 1, 1);
          },
          removeData : function(item) {
            this.list.removeElement(item);
          }
        }
      }
    }
  });
</script>
</body>
</html>