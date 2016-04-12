<!--

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

-->
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<html>
<head>
<view:looknfeel />
<link REL="SHORTCUT ICON" HREF="util/icons/favicon.ico">
<title>Démo de l'utilisation des Web-Services des espaces et applications : menus</title>
<link href="/silverpeas/demo/menu/css/silverpeas-menu.css" rel="stylesheet" type="text/css">
<script type="text/javascript" src="/silverpeas/demo/menu/js/silverpeas-core.js"></script>
<script type="text/javascript" src="/silverpeas/demo/menu/js/silverpeas-menu.js"></script>
</head>
<body>

  <script type="text/javascript">
      var mainMenuOptions = {
        "spaceCallback" : function(menuItem) {
          if (menuItem.getState().isExpanded) {
            var spaceAppearance = menuItem.getAppearance();
            $('#header').html($.spCore.printParams(spaceAppearance));
          }
          $('#body-header').html($.spCore.printParams(menuItem));
        },
        "componentCallback" : function(menuItem) {
          var spaceAppearance = menuItem.getAppearance();
          $('#header').html($.spCore.printParams(spaceAppearance));
          $('#body-header').html($.spCore.printParams(menuItem));
        }
      };
      var personalMenuOptions = {
        "componentCallback" : function(menuItem) {
          $('#body-header').html($.spCore.printParams(menuItem));
        },
        "toolCallback" : function(menuItem) {
          $('#body-header').html($.spCore.printParams(menuItem));
        }
      };

      $(document)
          .ready(
              function() {
                $(".sp-menu-root").menu(mainMenuOptions);

                $(".sp-personal-menu-root").menu('personal',
                    personalMenuOptions);

                $(".sp-personal-not-used-menu-root").menu('personalNotUsed',
                    personalMenuOptions);

                $("#componentIdOK").click(function() {
                  $.menu.reverseExpand($('#componentId').val());
                });

                // Post initialize
                if ($.menu.displayUserContext.userMenuDisplay != $.spCore.definitions.MENU.FAVORITES.DISABLED) {
                  var $button = $('<input>');
                  $button.attr('type', 'button');
                  $button.val($.menu.displayUserContext.userMenuDisplay);
                  $button
                      .click(function() {
                        var value = $(this).val();
                        var nextValue = $.spCore.definitions.MENU.FAVORITES.ALL;
                        if (value == $.spCore.definitions.MENU.FAVORITES.ALL) {
                          nextValue = $.spCore.definitions.MENU.FAVORITES.BOOKMARKS;
                        }
                        $.ajax({
                          url : webContext + '/RAjaxSilverpeasV5/dummy',
                          data : {
                            ResponseId : 'spaceUpdater',
                            Init : 1,
                            UserMenuDisplayMode : nextValue
                          },
                          success : function(data) {
                            $button.val(nextValue);
                            $(".sp-menu-root").menu('refresh', mainMenuOptions);
                          },
                          error : function() {
                          },
                          dataType : 'xml'
                        });
                      });
                  $("#favoriteDisplay").append($button);
                }
              });
    </script>

  <div id="page" style="display: table;">
    <div id="header" style="display: table-row;">
      <center>header</center>
    </div>
    <div id="content" style="display: table-row;">
      <div id="menu" class="sp-menu" style="display: table-cell; width: 300px;">
        <div id="favoriteDisplay" style="width: 300px;"></div>
        <div class="sp-menu-root" style="width: 300px;"></div>
        <div class="sp-personal-menu-root" style="width: 300px;"></div>
        <div class="sp-personal-not-used-menu-root" style="width: 300px;"></div>
      </div>
      <div id="body" style="display: table-cell; width: 100%; border: 1px;">
        <div id="body-header" style="width: 100%;">body-header</div>
        <div id="body-target" style="width: 100%;">body-target</div>
      </div>
      <div id="action" style="display: table-cell; width: 200px; border: 1px;">
        <c:set var="componentIdTitle" value="Lorsque vous allez appuyer sur OK, le menu contenant l'application sera visualisé ... (il est possible de ne renseigner que l'identifiant numérique de l'instance de l'application)" />
        <c:set var="componentIdLabel" value="Renseigner un identifiant (alpha-numérique ou numérique) d'instance d'une application" />
        <label for="componentId" title="${componentIdLabel}"><c:out value="${componentIdLabel}" /> :</label>
        <input id="componentId" name="componentId" type="text" title="${componentIdTitle}">
        <input id="componentIdOK" title="${componentIdTitle}" type="button" value="OK">
      </div>
    </div>
    <div id="footer" style="display: table-row;">
      <center>footer</center>
    </div>
  </div>
</body>
</html>