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

var CountPerPage = 6; // number of items in a pagination page

function isAUserProfile(object) {
  return object.type && object.type == UserProfileType;
}

function isAUserGroup(object) {
  return object.type && object.type == UserGroupType;
}

function UserProfile(user) {
  
  var self = this;
  var usermgt = new UserProfileManagement({
    id: user.id
  });
  
  if (user != null)
    for (var prop in user)
      this[prop] = user[prop];
  
  /**
   * Loads the attributes of the profile of this user.
   */
  this.load = function() {
    usermgt.get({
      contacts: false
    }, function(users) {
      if (users.length == 1)
        for (var prop in users[0])
          self[prop] = users[0][prop];
      else
        alert('Error loading the user of id ' + usermgt.filter.id +
          ': several users match the parameters!');
      usermgt.users = null;
    });
  }
  
  /**
   * Sets this user as belonging to the specified component instance (the unique identifier
   * of the component instance).
   */
  this.inComponent = function(component) {
    usermgt.filter.component = component;
    return self;
  }
  
  /**
   * Sets this user as playing the specified roles (a string of comma-separated list of roles)
   */
  this.withRoles = function(roles) {
    usermgt.filter.roles = roles;
    return self;
  }
  
  /**
   * Loads the profiles of the users that are part of the relationships of this user and calls
   * the callback function by passing it the loaded user profiles.
   * It accepts as arguments:
   * - optionally a pattern on the name the users should match (by default set at null: all users),
   * - optionally the page number or null if all the users must be fetched (by default set at null)
   * - optionally the page size in users count (by default it is set at CountPerPage). The page size
   *   cannot be set without passing the page number.
   * - the callback operation to call once the users loaded.
   */
  this.onRelationships = function() {
    var arg = 0, name = null, page = null, pagesize = CountPerPage, toload = false;
    if (typeof arguments[arg] == 'string' || arguments[arg] == null)
      name = arguments[arg++];
    if (typeof arguments[arg] == 'number' || arguments[arg] == null) {
      page = arguments[arg++];
      if (typeof arguments[arg] == 'number')
        pagesize = arguments[arg++];
    }
    if (arg == arguments.length || arguments[arg] == null) {
      alert('Error in arguments: the callback is mandtory');
      return self;
    }
    var callback = arguments[arg];
    if (usermgt.users == null) {
      toload = true;
    } 
    if (usermgt.filter.name != name) {
      usermgt.filter.name = name;
      toload = true;
    }
    if (page != null) {
      if (usermgt.filter.pagination == null || usermgt.filter.pagination.page != page ||
        usermgt.filter.pagination.count != pagesize) {
        usermgt.filter.pagination = {
          page: page, 
          count: pagesize
        };
        toload = true;
      }
    }
    if (page == null && usermgt.filter.pagination != null) {
      usermgt.filter.pagination = null;
      toload = true;
    }
    if (toload)
      usermgt.get({
        contacts: true
      }, callback);
    else 
      callback(usermgt.users);
    return self;
  }
  
  /**
   * Gets the loaded relationships.
   * Returns null if the relationships were not loaded.
   */
  this.relationships = function() {
    return usermgt.users;
  }
}

/**
 * A user group object built from its specified JSON representation.
 * It provides additional methods.
 */
function UserGroup(group) {
  
  var self        = this;
  var subgroupmgt = new UserGroupManagement();
  var usermgt     = new UserProfileManagement();
  
  if (group != null)
    for (var prop in group)
      this[prop] = group[prop];
  
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
   * It accepts as arguments:
   * - optionally a pattern on the name the users should match.
   * - the callback function to call once the subgroups loaded.
   */
  this.onChildren = function() {
    var arg = 0, name = null;
    if (arguments.length == 2)
      name = arguments[arg++];
    var callback = arguments[arg];
    if (subgroupmgt.filter.name != name || subgroupmgt.groups == null) {
      subgroupmgt.get({
        name: name,
        url: self.childrenUri
      },callback);
    } else 
      callback(subgroupmgt.groups);
    return self;
  }
  
  /**
   * Loads the nth page of the users that belong to this group (and its subgroups), and calls the
   * specified callback operation by passing it the fetched users.
   * If no page is specified, load all the users.
   * It accepts as arguments:
   * - optionally a pattern on the name the users should match (by default set at null: all users),
   * - optionally the page number or null if all the users must be fetched (by default set at null)
   * - optionally the page size in users count (by default it is set at CountPerPage). The page size
   *   cannot be set without passing the page number.
   * - the callback operation to call once the users loaded.
   */
  this.onUsers = function() {
    var arg = 0, name = null, page = null, pagesize = CountPerPage, toload = false;
    if (typeof arguments[arg] == 'string' || arguments[arg] == null)
      name = arguments[arg++];
    if (typeof arguments[arg] == 'number' || arguments[arg] == null) {
      page = arguments[arg++];
      if (typeof arguments[arg] == 'number')
        pagesize = arguments[arg++];
    }
    if (arg == arguments.length || arguments[arg] == null) {
      alert('Error in arguments: the callback is mandtory');
      return self;
    }
    var callback = arguments[arg];
    if (usermgt.users == null) {
      usermgt.filter.group = self.id;
      toload = true;
    } 
    if (usermgt.filter.name != name) {
      usermgt.filter.name = name;
      toload = true;
    }
    if (page != null) {
      if (usermgt.filter.pagination == null || usermgt.filter.pagination.page != page ||
        usermgt.filter.pagination.count != pagesize) {
        usermgt.filter.pagination = {
          page: page, 
          count: pagesize
        };
        toload = true;
      }
    }
    if (page == null && usermgt.filter.pagination != null) {
      usermgt.filter.pagination = null;
      toload = true;
    }
    if (toload)
      usermgt.get(callback);
    else 
      callback(usermgt.users);
    return self;
  }
  
  /**
   * Gets the loaded subgroups of this user group.
   * Null is returned if the subgroups are not loaded.
   */
  this.children = function() {
    return subgroupmgt.groups;
  }
  
  /**
   * Gets the loaded users in this user group.
   * Null is returned if the users are not loaded.
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
  childrenUri: null,
  name: 'Groupes'
});

/**
 * Object dedicated to the management of the user profiles at client side.
 * It that wraps the communication with the remote service that works on the user profiles.
 * You can give some parameters to filter the users on which this object works.
 */
function UserProfileManagement(params) {
  
  var self = this;
  
  this.filter = {
    id: null, // the unique identifier of a user to get
    contacts: false, // the contacts of the refered user. The id must be set to be taken into account
    group: null, // a user group unique identifier
    name: null, // a pattern about a user name (* is a wildcard)
    component: null, // a component instance identifier
    roles: null, // a string with a comma-separated role names
    pagination: null // pagination data in the form of 
  // { page: <number of the page>, count: <count of users to fetch> }
  };
  
  /**
   * The filtering parameters about the user profiles to get.
   */
  function setUpFilter(params) {
    if (params)
      for(var prop in params)
        self.filter[prop] = params[prop];
  }
  
  /**
   * Decorates the user profiles with usefull method to improve the usability.
   */
  function decorate(users) {
    var decoratedUsers = [];
    for (var i = 0; i < users.length; i++)
      decoratedUsers.push(new UserProfile(users[i]));
    return decoratedUsers;
  }
  
  setUpFilter(params);
  
  // the users it manages
  this.users = null;
  
  /**
   * Gets the user profiles matching the filters (if any) and passes them to the
   * specified callback 'loaded'.
   * The method returns the object itself.
   */
  this.get = function(filter, loaded) {
    var urlOfUsers = userRootURL, separator = '?', indexInCache = 0, application = '/application/';
    if (arguments.length == 1) {
      loaded = arguments[0];
      filter = null;
    }
    setUpFilter(filter);
    if (self.filter.id) {
      urlOfUsers += '/' + self.filter.id;
      if (self.filter.contacts)
        urlOfUsers += '/contacts';
      application = separator + 'application=';
      if (self.filter.component)
        separator = '&';
    }
    if (self.filter.component) {
      urlOfUsers += application + self.filter.component;
      if (self.filter.roles) {
        urlOfUsers += separator + 'roles=' + self.filter.roles;
        separator = '&';
      }
    }
    if (self.filter.group) {
      urlOfUsers += separator + 'group=' + self.filter.group;
      separator = '&';
    }
    if (self.filter.name && self.filter.name != '*') {
      urlOfUsers += separator + 'name=' + self.filter.name;
      separator = '&';
    }
    if (self.filter.pagination) {
      urlOfUsers += separator + 'page=' + self.filter.pagination.page + ';' + self.filter.pagination.count;
    } 
    $.ajax({
      url: urlOfUsers,
      type: 'GET',
      dataType: 'json',
      cache: false,
      success: function(users, status, jqXHR) {
        self.users = decorate(users);
        self.users.maxlength = jqXHR.getResponseHeader('X-Silverpeas-UserSize');
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
 * You can give some parameters to contraints the user groups on which this object should work.
 */
function UserGroupManagement(params) {
  
  var self = this;
  
  this.filter = {
    url: null, // the URL at which the groups have to be get. If not set, the URL of the root groups
    // is taken and the other filtering paramaters are parsed. Whatever the url is set or
    // not, the filtering paramater name is always parsed.
    name: null, // a pattern about a group name (* is a wildcard)
    component: null, // a component instance identifier
    roles: null // a string with a comma-separated role names
  };
  
  /**
   * The filtering parameters about the user groups to get.
   */
  function setUpFilter(params) {
    if (params)
      for(var prop in params)
        self.filter[prop] = params[prop];
  }
  
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
  
  setUpFilter(params);
  
  // the groups it manages.
  this.groups = null;
  
  /**
   * Gets the user profiles matching the filters (if any) and passes them to the specified callback
   * 'loaded'.
   * The method returns the object itself.
   */
  this.get = function(filter, loaded) {
    var urlOfGroups = groupRootURL, separator = '?';
    if (arguments.length == 1) {
      loaded = arguments[0];
      filter = null;
    }
    setUpFilter(filter);
    if (self.filter.url)
      urlOfGroups = self.filter.url;
    else if (self.filter.component) {
      urlOfGroups += '/application/' + self.filter.component;
      if (self.filter.roles) {
        urlOfGroups += separator + 'roles=' + self.filter.roles;
        separator = '&';
      }
    }
    if (this.filter.name && self.filter.name != '*') {
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
