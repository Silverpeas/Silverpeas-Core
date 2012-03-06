/* 
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

userRootURL = webContext + '/services/profile/users';
groupRootURL = webContext + '/services/profile/groups';

var UserGroupType = 'UserGroup';
var UserProfileType = 'UserProfile';

function isAUserProfile(object) {
  return object.type && object.type == UserProfileType;
}

function isAUserGroup(object) {
  return object.type && object.type == UserGroupType;
}

/**
 * A user group object built from its specified JSON representation.
 */
function UserGroup(group) {
  
  var self        = this;
  var subgroupmgt = new UserGroupManagement();
  var usermgt     = new UserProfileManagement();
  
  if (group != null)
    for (var prop in group)
      this[prop] = group[prop];

  /**
   * The type of the object (UserGroup)
   */
  this.type = UserGroupType;
  
  /**
   * Sets this user group as belonging to the specified component instance (the unique identifier
   * of the component instance).
   */
  this.inComponent = function(component) {
    subgroupmgt.filter.component = component;
    usermgt.filter.component = component;
    return self;
  }
  
  /**
   * Sets this user group as playing the specified roles (a string of comma-separated list of roles)
   */
  this.withRoles = function(roles) {
    subgroupmgt.filter.roles = roles;
    usermgt.filter.roles = roles;
    return self;
  }
  
  /**
   * Loads the children (subgroups) of this user group, and calls the specified callback operation
   * by passing it the fetched subgroups.
   * It accepts as a first optional argument a pattern on the name the users should match.
   */
  this.onChildren = function() {
    var arg = 0, name = null;
    if (arguments.length == 2)
      name = arguments[arg++];
    var callback = arguments[arg];
    if (subgroupmgt.filter.name != name || subgroupmgt.groups == null) {
      subgroupmgt.filter.name = name;
      subgroupmgt.filter.url = self.childrenUri;
      subgroupmgt.get(callback);
    } else 
      callback(subgroupmgt.groups);
    return self;
  }
  
  /**
   * Loads the users that belong to this group (and its subgroups), and calls the specified
   * callback operation by passing it the fetched users.
   * It accepts as a first optional argument a pattern on the name the users should match.
   */
  this.onUsers = function() {
    var arg = 0, name = null;
    if (arguments.length == 2)
      name = arguments[arg++];
    var callback = arguments[arg];
    if (usermgt.filter.name != name || usermgt.users == null) {
      usermgt.filter.name = name;
      usermgt.filter.group = self.id;
      usermgt.get(callback);
    } else 
      callback(usermgt.users);
    return self;
  }
  
  /**
   * Gets the loaded subgroups of this user group.
   */
  this.children = function() {
    return subgroupmgt.groups;
  }
  
  /**
   * Gets the loaded users in this user group.
   */
  this.users = function() {
    return usermgt.users;
  }
}

/**
 * The root user group. It is a factice user group from which all the users or all the root groups
 * in Silverpeas can be fetched.
 */
var rootUserGroup = new UserGroup({
  childrenUri: groupRootURL,
  name: 'Groupes'
});

/**
 * Object dedicated to the management of the user profiles at client side.
 * It that wraps the communication with the remote service that works on the user profiles.
 * You can give some filtering parameters with the filter attribute and call the get method to load
 * from the remote service the user profiles that match tyour filtering paramaters. If no filter
 * is set then all the available user profiles is loaded.
 */
function UserProfileManagement(params) {
  
  var self = this;
  
  /**
   * Decorates the user profiles with usefull method to improve the usability.
   */
  function decorate(users) {
    for (var i = 0; i < users.length; i++) {
      users[i].type = UserProfileType;
    }
    return users;
  }
  
  /**
   * The filtering parameters about the user profiles to get.
   */
  if (!params)
    params = {};
  this.filter = $.extend({}, {
    group: null, // a user group unique identifier
    name: null, // a pattern about a user name (* is a wildcard)
    component: null, // a component instance identifier
    roles: null // a string with a comma-separated role names
  }, params);
  
  // the users it manages
  this.users = null;
  
  /**
   * Gets the user profiles matching the filters (if any) and passes them to the
   * specified callback 'loaded'.
   * The method returns the object itself.
   */
  this.get = function(loaded) {
    var urlOfUsers = userRootURL, separator = '?';
    if (self.filter.component) {
      urlOfUsers += '/application/' + self.filter.component;
      if (self.filter.roles) {
        urlOfUsers += separator + 'roles=' + self.filter.roles;
        separator = '&';
      }
    }
    if (self.filter.group) {
      urlOfUsers += separator + 'group=' + self.filter.group;
      separator = '&';
    }
    if (self.filter.name) {
      urlOfUsers += separator + 'name=' + self.filter.name;
    } 
    $.ajax({
      url: urlOfUsers,
      type: 'GET',
      dataType: 'json',
      cache: false,
      success: function(users) {
        self.users = decorate(users);
        loaded(self.users);
      },
      error: function(jqXHR, textStatus, errorThrown) {
        alert(errorThrown);
      }
    });
    return self;
  }
}

/**
 * Object dedicated to the management of user groups at client side.
 * It that wraps the communication with the remote service that works on the user groups.
 * You can give some filtering parameters with the filter attribute and call the get method to load
 * from the remote service the user groups that match tyour filtering paramaters. If no filter
 * is set then all the available user groups is loaded.
 */
function UserGroupManagement(params) {
  
  var self = this;
  
  /**
   * Decorates the user groups with usefull method to improve the usability:
   * - children(loaded) with loaded a callback called with as argument the loaded groups,
   * - users([name], loaded) with name an optional argument to filter the users by their name, and
   * loaded a callback called with as argument the loaded users.
   */
  function decorate(groups) {
    var decoratedGroups = [];
    for (var i = 0; i < groups.length; i++)
      decoratedGroups.push(new UserGroup(groups[i]));
    return decoratedGroups;
  }
  
  /**
   * The filtering parameters about the user groups to get.
   */
  if (!params)
    params = {};
  this.filter = $.extend({}, {
    url: null, // the URL at which the groups have to be get. If not set, the URL of the root groups
    // is taken and the other filtering paramaters are parsed. Whatever the url is set or
    // not, the filtering paramater name is always parsed.
    name: null, // a pattern about a group name (* is a wildcard)
    component: null, // a component instance identifier
    roles: null // a string with a comma-separated role names
  }, params);
  
  // the groups it manages.
  this.groups = null;
  
  /**
   * Gets the user profiles matching the filters (if any) and passes them to the specified callback
   * 'loaded'.
   * The method returns the object itself.
   */
  this.get = function(loaded) {
    var urlOfGroups = groupRootURL, separator = '?';
    if (self.filter.url)
      urlOfGroups = self.filter.url;
    else if (self.filter.component) {
      urlOfGroups += '/application/' + self.filter.component;
      if (self.filter.roles) {
        urlOfGroups += separator + 'roles=' + self.filter.roles;
        separator = '&';
      }
    }
    if (this.filter.name) {
      urlOfGroups += separator + "name=" + this.filter.name;
    }
    $.ajax({
      url: urlOfGroups,
      type: 'GET',
      dataType: 'json',
      cache: false,
      success: function(groups) {
        self.groups = decorate(groups);
        loaded(self.groups);
      },
      error: function(jqXHR, textStatus, errorThrown) {
        alert(errorThrown);
      }
    });
    return self;
  }
}
