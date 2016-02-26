/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

(function($) {

  // Web Context
  if (!webContext) {
    var webContext = '/silverpeas';
  }

  // Web Service Context
  var webServiceContext = webContext + '/services';

  var cache = [];

  $.responsibles = {
    labels: {
      platformResponsible: '',
      sendMessage: ''
    },
    renderSpaceResponsibles: function(target, userId, spaceId, onlySpaceManagers) {
      __loadMissingPlugins(false);
      var data = __getResponsibles(true, spaceId);
      $(target).empty();
      __prepareContent($(target), userId, true, data.usersAndGroupsRoles, onlySpaceManagers);
      __loadUserZoomPlugins();
    },
    displaySpaceResponsibles: function(userId, spaceId) {
      __loadMissingPlugins(true);
      __display(userId, true, spaceId);
    },
    displayComponentResponsibles: function(userId, componentId) {
      __loadMissingPlugins(true);
      __display(userId, false, componentId);
    }
  };

  /**
   * Displays the responsible into a popup.
   * @param userId
   * @param isSpace
   * @param id
   * @private
   */
  function __display(userId, isSpace, id) {
    var $display = $('#responsible-popup-content');
    var data = __getResponsibles(isSpace, id);
    if (!isSpace && $.isEmptyObject(data.usersAndGroupsRoles)) {
      // No responsible founded, so searching on space on which component is attached ...
      var spaceOfComponent = __getJSonData(data.parentURI);
      if (spaceOfComponent && spaceOfComponent.id) {
        isSpace = true;
        id = spaceOfComponent.id;
        data = __getResponsibles(isSpace, id);
      }
    }

    var title = $('<div>').append($('.space-or-component-responsibles-operation').text() +
            " ").append($('<b>').append(data.label)).html();
    if ($display.length !== 0) {
      $display.dialog('destroy');
      $display.remove();
    }
    $display = $('<div>', {id: 'responsible-popup-content'}).css('display',
            'none').appendTo(document.body);
    __prepareContent($display, userId, isSpace, data.usersAndGroupsRoles, false);
    __loadUserZoomPlugins();

    // Popup
    $display.popup('free', {
      title: title,
      width: '500px'
    });
  }

  /**
   * Gets the space data.
   * @param isSpace
   * @param id
   * @returns {*}
   * @private
   */
  function __getResponsibles(isSpace, id) {
    var result = cache[(isSpace ? 'space-' : 'component-') + id];
    if (!result) {
      var spaceOrComponent = __getJSonData(webServiceContext + '/' +
              (isSpace ? 'spaces' : 'components') + '/' + id);
      var usersAndGroupsRoles = __getJSonData(spaceOrComponent.usersAndGroupsRolesURI + '?roles=' +
              (isSpace ? 'Manager' : 'admin'));
      result = {
        usersAndGroupsRoles: usersAndGroupsRoles
      };
      $.extend(result, spaceOrComponent);
    }
    return result;
  }

  /**
   * Prepares the content to display.
   * @param $target
   * @param userId
   * @param isSpace
   * @param usersAndGroupsRoles
   * @param onlySpaceManagers
   * @private
   */
  function __prepareContent($target, userId, isSpace, usersAndGroupsRoles, onlySpaceManagers) {
    var $newLine = null;
    $.each(['Manager', 'admin'], function(index, role) {
      var usersAndGroups = usersAndGroupsRoles[role];
      var dataOfUsers = __getAllDataOfUsers(usersAndGroups);
      if (dataOfUsers.length > 0) {
        $target.append($newLine);
        if (isSpace) {
          var $div = $('<div>', {'id':'space-admins'});
          $target.append($div);
          $div.append($('<h5>', {
            'class': 'textePetitBold title-list-responsible-user'
          }).append(usersAndGroups.label));
          __prepareRoleResponsibles($div, userId, dataOfUsers);
        } else {
          __prepareRoleResponsibles($target, userId, dataOfUsers);
        }
        $newLine = $('<br/>');
      }
    });
    if (!onlySpaceManagers && isSpace) {
      User.get({
        accessLevel : ['ADMINISTRATOR']
      }).then(function(users) {
        var administrators = [];
        $(users).each(function(index, administrator) {
          administrators.push(administrator);
        });
        if (administrators.length > 0) {
          var $div = $('<div>', {'id' : 'global-admins'});
          $target.append($div);
          $div.append($newLine);
          $div.append($('<h5>',
              {'class' : 'textePetitBold title-list-responsible-user'}).append($.responsibles.labels.platformResponsible));
          __prepareRoleResponsibles($div, userId, administrators);
          $newLine = $('<br/>');
        }
      });
    }
  }

  /**
   * Prepare role responsibles.
   * @param $target
   * @param userId
   * @param dataOfUsers
   * @private
   */
  function __prepareRoleResponsibles($target, userId, dataOfUsers) {
    if ($.isArray(dataOfUsers) && dataOfUsers.length > 0) {
      var $users = $('<ul>', {'class': 'list-responsible-user'});
      $.each(dataOfUsers, function(index, user) {
        var $user = $('<span>').append(' ' + user.fullName);
        if (userId !== user.id && !user.anonymous) {
          $user.addClass('userToZoom');
          $user.attr('rel', user.id);
        }
        var $photoProfil = $('<div>', {'class': 'profilPhoto'}).append($('<a>').append($('<img>',
                {'class': 'avatar', src: user.avatar})));
        var $userName = $('<div>', {'class': 'userName'});
        var $action = null;
        if (userId !== user.id && !user.anonymous) {
          $userName.append($('<a>', {'class': 'userToZoom', rel: user.id}).append(user.fullName));
          $action = $('<div>', {'class': 'action'}).append($('<a>',
                  {href: '#', 'class': 'link notification'}).append($.responsibles.labels.sendMessage)).messageMe(user);
        } else {
          $userName.append($('<a>').append(user.fullName));
        }
        $users.append($('<li>', {'class': 'intfdcolor'}).append($('<div>',
                {'class': 'content'}).append($photoProfil).append($userName).append($action)));
      });
      $target.append($users);
    }
  }

  /**
   * Gets from users and groups role object, all the users (even those of groups)
   * @param usersAndGroups
   * @private
   */
  function __getAllDataOfUsers(usersAndGroups) {
    var dataOfUsers = [];
    var uriOfUsers = [];
    if (usersAndGroups) {

      // Users
      if (usersAndGroups.users && usersAndGroups.users.length > 0) {
        $.each(usersAndGroups.users, function(index, userUri) {
          if ($.inArray(userUri, uriOfUsers) < 0) {
            uriOfUsers.push(userUri);
            dataOfUsers.push(__getJSonData(userUri));
          }
        });
      }

      // Groups
      if (usersAndGroups.groups && usersAndGroups.groups.length > 0) {
        $.each(usersAndGroups.groups, function(index, groupUri) {
          var group = __getJSonData(groupUri);
          if (group) {
            $.each(__getJSonData(group.usersUri), function(index, user) {
              if ($.inArray(user.uri, uriOfUsers) < 0) {
                uriOfUsers.push(user.uri);
                dataOfUsers.push(user);
              }
            });
          }
        });
      }

      // Sorting users by their names
      dataOfUsers.sort(__sortByName);
    }
    return dataOfUsers;
  }

  /**
   * Sort users by their names
   * @param a
   * @param b
   * @return {number}
   * @private
   */
  function __sortByName(a, b) {
    var aName = a.fullName.toLowerCase();
    var bName = b.fullName.toLowerCase();
    return ((aName < bName) ? -1 : ((aName > bName) ? 1 : 0));
  }

  /**
   * Centralized method to load missing plugins.
   * @private
   */
  function __loadMissingPlugins(isPopup) {
    if (isPopup && !$.popup) {
      $.ajax({
        url: webContext + "/util/javaScript/silverpeas-popup.js",
        async: false,
        dataType: "script"
      });
    }
    if (typeof User === 'undefined') {
      $.ajax({
        url: webContext + "/util/javaScript/angularjs/services/silverpeas-profile.js",
        async: false,
        dataType: "script"
      });
    }
    if (!$.messageMe) {
      $.ajax({
        url: webContext + "/util/javaScript/silverpeas-messageme.js",
        async: false,
        dataType: "script"
      });
    }
  }

  /**
   * Centralized method to load user zoom plugins.
   */
  function __loadUserZoomPlugins() {
    $.ajax({
      url: webContext + "/util/javaScript/silverpeas-invitme.js",
      dataType: "script"
    });
    $.ajax({
      url: webContext + "/util/javaScript/silverpeas-userZoom.js",
      dataType: "script"
    });
  }

  /**
   * Centralizes synchronous ajax request for json response.
   * @param url
   * @return {*}
   * @private
   */
  function __getJSonData(url) {
    return __performAjaxRequest({url: url, type: 'GET', dataType: 'json'});
  }

  /**
   * Private function that performs an ajax request. By default, the request is
   * synchroned, that is to say the javascript running is waiting for the return of ajax request
   * request.
   */
  function __performAjaxRequest(settings) {
    var result = {};

    // Default options.
    // url, type, dataType are missing.
    var options = {
      cache: false,
      async: false,
      success: function(data) {
        result = data;
      },
      error: function(jqXHR, textStatus, errorThrown) {
        window.console &&
                window.console.log('Silverpeas Responsible JQuery Plugin - ERROR - ' + errorThrown);
      }
    };

    // Adding settings
    options = $.extend(options, settings);

    // Ajax request
    $.ajax(options);
    return result;
  }

})(jQuery);

/**
 * Has to be called to get the jQuery responsibles content of a space.
 * @param target
 * @param userId
 * @param spaceId
 * @param onlySpaceManagers
 */
function renderSpaceResponsibles(target, userId, spaceId, onlySpaceManagers) {
  $.responsibles.renderSpaceResponsibles(target, userId, spaceId, onlySpaceManagers);
}

/**
 * Has to be called to display responsibles of a space.
 * @param userId
 * @param spaceId
 */
function displaySpaceResponsibles(userId, spaceId) {
  $.responsibles.displaySpaceResponsibles(userId, spaceId);
}

/**
 * Has to be called to display responsibles of a component.
 * @param userId
 * @param componentId
 */
function displayComponentResponsibles(userId, componentId) {
  $.responsibles.displayComponentResponsibles(userId, componentId);
}
