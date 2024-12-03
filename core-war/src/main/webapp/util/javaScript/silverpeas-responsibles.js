/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

(function ($) {

  // Web Context
  if (!webContext) {
    window.webContext = '/silverpeas';
  }

  // Web Service Context
  const webServiceContext = webContext + '/services';

  $.responsibles = {
    labels: {
      platformResponsible: '',
      sendMessage: ''
    },
    renderSpaceResponsibles: function (target, userId, spaceId, onlySpaceManagers) {
      __loadMissingPlugins(false).then(function () {
        $(target).empty();
        __getResponsibles(true, spaceId).then(function (data) {
          __prepareContent($(target), userId, true, data.usersAndGroupsRoles, onlySpaceManagers);
        });
      });
    },
    displaySpaceResponsibles: function (userId, spaceId) {
      __loadMissingPlugins(true).then(function () {
        __display(userId, true, spaceId);
      });
    },
    displayComponentResponsibles: function (userId, componentId) {
      __loadMissingPlugins(true).then(function () {
        __display(userId, false, componentId);
      });
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
    const $display = $('#responsible-popup-content');
    __getResponsibles(isSpace, id).then(function (data) {
      if (!isSpace && !__userAndGroupRolesAreDefined(data)) {
        // No responsible founded, so searching on space on which component is attached ...
        __getJSonData(data.parentURI).then(function (spaceOfComponent) {
          if (spaceOfComponent && spaceOfComponent.id) {
            __getResponsibles(true, spaceOfComponent.id).then(function (data) {
              __render($display, data, true, userId);
            });
          } else {
            __render($display, data, isSpace, userId);
          }
        });
      } else {
        __render($display, data, isSpace, userId);
      }
    });
  }

  function __render($display, data, isSpace, userId) {
    const title = $('<div>')
        .append($('.space-or-component-responsibles-operation').text() + " ")
        .append($('<b>').append(data.label)).html();
    if ($display.length !== 0) {
      $display.dialog('destroy');
      $display.remove();
    }
    $display = $('<div>', {id: 'responsible-popup-content'}).css('display',
        'none').appendTo(document.body);
    __prepareContent($display, userId, isSpace, data.usersAndGroupsRoles, false);

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
    return __getJSonData(
        webServiceContext + '/' + (isSpace ? 'spaces' : 'components') + '/' + id).then(
        function (spaceOrComponent) {
          return __getJSonData(spaceOrComponent.usersAndGroupsRolesURI + '?roles=' +
              (isSpace ? 'Manager' : 'admin')).then(function (usersAndGroupsRoles) {
            result = {
              usersAndGroupsRoles: usersAndGroupsRoles
            };
            $.extend(result, spaceOrComponent);
            return result;
          });
        });
  }

  /**
   * Prepares the content to display and returns a promise when dom is prepared.
   * @param $target
   * @param userId
   * @param isSpace
   * @param usersAndGroupsRoles
   * @param onlySpaceManagers
   * @private
   */
  function __prepareContent($target, userId, isSpace, usersAndGroupsRoles, onlySpaceManagers) {
    const aimedRoles = ['Manager', 'admin'];
    const promises = [];
    let $newLine = null;
    $.each(aimedRoles, function (index, role) {
      const usersAndGroups = usersAndGroupsRoles[role];
      const userPromise = __getAllDataOfUsers(usersAndGroups).then(function (dataOfUsers) {
        if (dataOfUsers.length > 0) {
          $target.append($newLine);
          if (isSpace) {
            const $div = $('<div>', {'id': 'space-admins'});
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
      promises.push(userPromise);
    });
    if (!onlySpaceManagers && isSpace) {
      const adminPromise = User.get({
        accessLevel: ['ADMINISTRATOR']
      }).then(function (users) {
        const administrators = [];
        $(users).each(function (index, administrator) {
          administrators.push(administrator);
        });
        if (administrators.length > 0) {
          const $div = $('<div>', {'id': 'global-admins'});
          $target.append($div);
          $div.append($newLine);
          $div.append($('<h5>',
              {'class': 'textePetitBold title-list-responsible-user'}).append($.responsibles.labels.platformResponsible));
          __prepareRoleResponsibles($div, userId, administrators);
          $newLine = $('<br/>');
        }
      });
      promises.push(adminPromise);
    }
    return sp.promise.whenAllResolved(promises).then(function () {
      __loadUserZoomPlugins();
    });
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
      const $users = $('<ul>', {'class': 'list-responsible-user'});
      $.each(dataOfUsers, function (index, user) {
        const $user = $('<span>').append(' ' + user.fullName);
        if (userId !== user.id && !user.anonymous) {
          $user.addClass('userToZoom');
          $user.attr('rel', user.id);
        }
        const $photoProfil = $('<div>', {'class': 'profilPhoto'}).append($('<a>').append($('<img>',
            {'alt': user.fullName + ' avatar', 'class': 'avatar', src: user.avatar})));
        const $userName = $('<div>', {'class': 'userName'});
        let $action = null;
        if (userId !== user.id && !user.anonymous) {
          $userName.append($('<a>', {'class': 'userToZoom', rel: user.id}).append(user.fullName));
          $action = $('<div>', {'class': 'action'}).append($('<a>',
              {
                href: '#',
                'class': 'link notification'
              }).append($.responsibles.labels.sendMessage)).click(function () {
            sp.messager.open(null, {recipientUsers: user.id, recipientEdition: false});
          });
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
    if (usersAndGroups) {
      const promises = [];
      const dataOfUsers = [];
      const uriOfUsers = [];

      // Users
      if (usersAndGroups.users && usersAndGroups.users.length > 0) {
        const userPromises = [];
        promises.push(new Promise(function (resolve, reject) {
          $.each(usersAndGroups.users, function (index, userUri) {
            if ($.inArray(userUri, uriOfUsers) < 0) {
              uriOfUsers.push(userUri);
              userPromises.push(new Promise(function (resolve, reject) {
                __getJSonData(userUri).then(function (user) {
                  dataOfUsers.push(user);
                  resolve();
                });
              }));
            }
          });
          sp.promise.whenAllResolved(userPromises).then(function () {
            resolve();
          });
        }));
      }

      // Groups
      if (usersAndGroups.groups && usersAndGroups.groups.length > 0) {
        const groupPromises = [];
        promises.push(new Promise(function (resolve, reject) {
          $.each(usersAndGroups.groups, function (index, groupUri) {
            groupPromises.push(new Promise(function (resolve, reject) {
              __getJSonData(groupUri).then(function (group) {
                if (group) {
                  __getJSonData(group.usersUri).then(function (users) {
                    $.each(users, function (index, user) {
                      if ($.inArray(user.uri, uriOfUsers) < 0) {
                        uriOfUsers.push(user.uri);
                        dataOfUsers.push(user);
                      }
                    });
                    resolve();
                  });
                } else {
                  resolve();
                }
              })
            }));
          });
          sp.promise.whenAllResolved(groupPromises).then(function () {
            resolve();
          });
        }));
      }

      // Sorting users by their names
      return sp.promise.whenAllResolved(promises).then(function () {
        dataOfUsers.sort(__sortByName);
        return dataOfUsers;
      });
    }
    return sp.promise.resolveDirectlyWith([]);
  }

  function __userAndGroupRolesAreDefined(data) {
    let roles = data.usersAndGroupsRoles;
    if (typeof roles === 'object') {
      for (let roleName in roles) {
        let role = roles[roleName];
        if ((Array.isArray(role.users) && role.users.length > 0) ||
            (Array.isArray(role.groups) && role.groups.length > 0)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Sort users by their names
   * @param a
   * @param b
   * @return {number}
   * @private
   */
  function __sortByName(a, b) {
    const aName = a.fullName.toLowerCase();
    const bName = b.fullName.toLowerCase();
    return ((aName < bName) ? -1 : ((aName > bName) ? 1 : 0));
  }

  /**
   * Centralized method to load missing plugins.
   * @private
   */
  function __loadMissingPlugins(isPopup) {
    const promises = [];
    if (isPopup && !$.popup) {
      promises.push(new Promise(function (resolve, reject) {
        $.getScript(webContext + "/util/javaScript/silverpeas-popup.js", function () {
          resolve();
        });
      }));
    }
    if (typeof User === 'undefined') {
      promises.push(new Promise(function (resolve, reject) {
        $.getScript(webContext + "/util/javaScript/angularjs/services/silverpeas-profile.js",
            function () {
              resolve();
            });
      }));
    }
    return sp.promise.whenAllResolved(promises);
  }

  /**
   * Centralized method to load user zoom plugins.
   */
  function __loadUserZoomPlugins() {
    $.getScript(webContext + '/util/javaScript/silverpeas-relationship.js');
    $.getScript(webContext + '/util/javaScript/silverpeas-userZoom.js');
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
    return new Promise(function (resolve, reject) {
      // Default options.
      // url, type, dataType are missing.
      let options = {
        cache: false,
        success: function (data) {
          resolve(data);
        },
        error: function (jqXHR, textStatus, errorThrown) {
          reject();
          window.console &&
          window.console.log('Silverpeas Responsible JQuery Plugin - ERROR - ' + errorThrown);
        }
      };

      // Adding settings
      options = $.extend(options, settings);

      // Ajax request
      $.ajax(options);
    });
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
