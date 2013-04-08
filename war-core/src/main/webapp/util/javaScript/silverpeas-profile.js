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

/**
 * The base URL of the WEB resources representing user profiles.
 * @type {string}
 */
userRootURL = webContext + '/services/profile/users';

/**
 * The base URL of the WEB resources representing the user groups.
 * @type {string}
 */
groupRootURL = webContext + '/services/profile/groups';

/**
 * The default number of items in a pagination page.
 * @type {number}
 */
var CountPerPage = 6;

/**
 * Creates  a new UserProfile object from a user properties (generally coming from a JSON text).
 * @param [user] the user properties.
 * @class Represents a user profile with additional methods.
 */
function UserProfile(user) {
  var self = this;

  var userProfileManagementParams = $.extend({extended: false}, user);
  var usermgt = new UserProfileManagement({
    id: userProfileManagementParams.id,
    extended: userProfileManagementParams.extended,
    async: userProfileManagementParams.async
  });
  var myrelationships = null;

  if (user != null)
    for (var prop in user)
      this[prop] = user[prop];

  /**
   * The relationships of this user with others users of the platform. By default, the relationships
   * aren't loaded, so you have to load them before with the loadRelationships() method.
   * @return {UserProfile[]} the relationships of this user.
   */
  this.relationships = function () {
    return myrelationships;
  };

  /**
   * Loads the attributes of the profile of this user.
   * Whether a callback is passed as argument, invokes the callback with this user profile once its
   * attributes loaded successfully.
   * @param {function} [callback] the function to call once the user properties loaded.
   * @return {UserProfile} itself
   */
  this.load = function (callback) {
    usermgt.get({
      contacts: false
    }, function (users) {
      if (users.length == 1) {
        for (var prop in users[0])
          self[prop] = users[0][prop];
        if (callback)
          callback(self);
      } else
        alert('Error loading the user of id ' + usermgt.filter.id +
            ': several users match the parameters!');
      usermgt.users = null;
    });
    return self;
  };

  /**
   * Sets this user as having access privileges in the specified component instance
   * (the unique identifier of the component instance).
   * @return {UserProfile} itself.
   * @param {String} component the unique identifier the component instance.
   */
  this.inComponent = function (component) {
    usermgt.filter.component = component;
    return self;
  };

  /**
   * Sets this user group as belonging to the specified user domain in Silverpeas.
   * @param {String} domain the Silverpeas domain identifier.
   * @return {UserProfile} itself.
   */
  this.inDomain = function (domain) {
    usermgt.filter.domain = domain;
    return self;
  };

  /**
   * Sets this user as playing the specified roles (a string of comma-separated list of roles)
   * @param {String} roles the roles the user should play.
   * @return {UserProfile} itself.
   */
  this.withRoles = function (roles) {
    usermgt.filter.roles = roles;
    return self;
  };

  /**
   * Sets this user as having access privileges for the specified resource within a given component
   * instance. The component instance must be set.
   * @param {String} resource a resource unique identifier.
   * @return {UserProfile} itself.
   */
  this.forResource = function (resource) {
    usermgt.filter.resource = resource;
    return self;
  };

  /**
   * Loads the profiles of the users that are part of the relationships of this user and calls
   * the callback function by passing it the loaded user profiles.
   * If the relationships were already loaded and the filtering parameters aren't modified, then
   * no load is yet performed.
   * @param [filter] the optional filtering parameters on the user's relationships.
   * @param [filter.name] a pattern on the name the users should match (if not set, by default all users),
   * @param [filter.pagination] the pagination properties on the users to load.
   * @param filter.pagination.page the page number,
   * @param [filter.pagination.count=CountPerPage] the number of users in each page. By default, if not set, count is set at CountPerPage.
   * @param [filter.reload] a boolean to force the reload of the user's relationships. By default, false.
   * @param {function} [callback] a function to call once the relationships of the user areloaded.
   * @return {UserProfile} itself.
   */
  this.loadRelationships = function (filter, callback) {
    var toload = false;
    if (arguments.length == 1 && typeof arguments[0] == "function") {
      callback = arguments[0];
      filter = null;
    }
    if (myrelationships == null) {
      toload = true;
    }
    if (filter != null) {
      toload |= filter.reload;
      if (filter.name != null && usermgt.filter.name != filter.name) {
        usermgt.filter.name = filter.name;
        toload = true;
      }
      if (filter.pagination != null) {
        if (filter.pagination.count == null)
          filter.pagination.count = CountPerPage;
        if (usermgt.filter.pagination == null || usermgt.filter.pagination.page != filter.pagination.page
            || usermgt.filter.pagination.count != filter.pagination.count) {
          usermgt.filter.pagination = {page: filter.pagination.page, count: filter.pagination.count};
          toload = true;
        }
      } else if (usermgt.filter.pagination != null) {
        usermgt.filter.pagination = null;
        toload = true;
      }
    }
    if (toload)
      usermgt.get({
        contacts: true
      }, function (users) {
        myrelationships = users;
        if (callback)
          callback(users);
      });
    else if (callback)
      callback(myrelationships);
    usermgt.users = null;
    return self;
  }
}

/**
 * Creates a UserGroup object from a user group properties (generally coming from a JSON text).
 * @param group the properties of the group.
 * @class Represents a group of users with additional methods.
 */
function UserGroup(group) {

  var self = this;
  var subgroupmgt = new UserGroupManagement();
  var usermgt = new UserProfileManagement();

  if (group != null)
    for (var prop in group)
      this[prop] = group[prop];

  /**
   * The subgroups of this user group. By default, the group's children aren't loaded, so you have
   * to call loadChildren() method before calling this method.
   * @return {UserGroup[]} the direct children of this group.
   */
  this.children = function () {
    return subgroupmgt.groups;
  };

  /**
   * Sets this user group as having access privileges in the specified component instance
   * (the unique identifier of the component instance).
   * @param {String} component the unique identifier of the component instance.
   * @return {UserGroup} itself.
   */
  this.inComponent = function (component) {
    subgroupmgt.filter.component = component;
    usermgt.filter.component = component;
    return self;
  };


  /**
   * Sets this user group as belonging to the specified user domain in Silverpeas.
   * @param {String} domain the unique identifier of the Silverpeas domain.
   * @return {UserGroup} itself.
   */
  this.inDomain = function (domain) {
    subgroupmgt.filter.domain = domain;
    usermgt.filter.domain = domain;
    return self;
  };

  /**
   * Sets this user group as playing the specified roles (a string of comma-separated list of roles)
   * @param {String} roles the roles all the users in this group should play.
   * @return {UserGroup} itself.
   */
  this.withRoles = function (roles) {
    subgroupmgt.filter.roles = roles;
    usermgt.filter.roles = roles;
    return self;
  };

  /**
   * Sets this user group as having access privileges for the specified resource within a given
   * component instance. The component instance must be set.
   * @param {String} resource the unique identifier of the resource belonging to the component
   * instance.
   * @return {UserGroup} itself.
   */
  this.forResource = function (resource) {
    subgroupmgt.filter.resource = resource;
    return self;
  };

  /**
   * Loads the attributes of this user group.
   * Whether a callback is passed as argument, invokes the callback with this user group once its
   * attributes loaded successfully.
   * @param {function} [callback] the function to call once the properties of this group loaded.
   * @return {UserGroup} itself.
   */
  this.load = function (callback) {
    var groupmgt = new UserGroupManagement({
      id: self.id
    });
    groupmgt.get(function (groups) {
      if (groups.length == 1) {
        for (var prop in groups[0])
          self[prop] = groups[0][prop];
        if (callback)
          callback(self);
      } else
        alert('Error loading the user group of id ' + groupmgt.filter.id +
            ': several user groups match the parameters!');
      groupmgt.groups = null;
    });
    return self;
  };

  /**
   * Loads the children (subgroups) of this user group, and calls the specified callback
   * operation by passing it the fetched subgroups.
   * @param [filter] the filtering parameters on the subgroups to load.
   * @param {String} filter.name a pattern on the name of the groups to load.
   * @param [filter.pagination] the pagination properties on the subgroups to load. If no pagination is set then load all the subgroups.
   * @param filter.pagination.page the page number to load.
   * @param [filter.pagination.count=CountPerPage] the number of subgroups in a page. By default, set at CountPerPage)
   * @param {function} [callback] a function to call once the subgroups are loaded.
   * @return {UserGroup} itself.
   */
  this.loadChildren = function (filter, callback) {
    var toload = (subgroupmgt.groups == null);
    if (arguments.length == 1 && typeof arguments[0] == "function") {
      callback = arguments[0];
      filter = null;
    }

    if (filter != null) {
      if (subgroupmgt.filter.name != filter.name) {
        subgroupmgt.filter.name = filter.name;
        toload = true;
      }
      if (filter.pagination != null) {
        if (filter.pagination.count == null)
          filter.pagination.count = CountPerPage;
        if (subgroupmgt.filter.pagination == null || subgroupmgt.filter.pagination.page != filter.pagination.page ||
            subgroupmgt.filter.pagination.count != filter.pagination.count) {
          subgroupmgt.filter.pagination = {page: filter.pagination.page, count: filter.pagination.count};
          toload = true;
        }
      } else if (subgroupmgt.filter.pagination != null) {
        subgroupmgt.filter.pagination = null;
        toload = true;
      }
    }

    if (toload) {
      subgroupmgt.filter.url = self.childrenUri;
      subgroupmgt.get(function (groups) {
        if (callback)
          callback(groups);
      });
    } else if (callback)
      callback(subgroupmgt.groups);
    return self;
  };

  /**
   * Loads the users that belong to this group (and its subgroups), and calls the
   * specified callback operation by passing it the fetched users.
   * @param [filter] the filtering parameters on the users.
   * @param [filter.pagination] the pagination properties on the users to load. If no pagination is set then load all the users.
   * @param filter.pagination.page the page number to load.
   * @param [filter.pagination.count=CountPerPage] the number of users in a page. By default, set at CountPerPage).
   * @param {function} [callback] a function to call once the users are loaded.
   * @return {UserProfile[]} the profile of the users in this group.
   */
  this.loadUsers = function (filter, callback) {
    var toload = false;
    if (arguments.length == 1 && typeof arguments[0] == "function") {
      callback = arguments[0];
      filter = null;
    }

    if (usermgt.users == null) {
      usermgt.filter.group = self.id;
      toload = true;
    }

    if (filter != null) {
      if (usermgt.filter.name != filter.name) {
        usermgt.filter.name = filter.name;
        toload = true;
      }
      if (filter.pagination != null) {
        if (filter.pagination.count == null)
          filter.pagination.count = CountPerPage;
        if (usermgt.filter.pagination == null || usermgt.filter.pagination.page != filter.pagination.page ||
            usermgt.filter.pagination.count != filter.pagination.count) {
          usermgt.filter.pagination = {page: filter.pagination.page, count: filter.pagination.count};
          toload = true;
        }
      } else if (usermgt.filter.pagination != null) {
        usermgt.filter.pagination = null;
        toload = true;
      }
    }
    if (toload)
      usermgt.get(function (users) {
        if (callback)
          callback(users);
      });
    else if (callback)
      callback(usermgt.users);
    return self;
  };

  /**
   * Gets the loaded users in this user group. By default, the users of the group aren't loaded,
   * so you have to call loadUsers() method before calling this method.
   * @return {UserProfile[]}
   */
  this.users = function () {
    return usermgt.users;
  }
}

/**
 * The root user group. It is a factice user group from which all the users in all groups or all
 * the root groups in Silverpeas can be fetched.
 * @type {UserGroup}
 */
var rootUserGroup = new UserGroup({
  id: 'all',
  childrenUri: null,
  name: 'Groupes'
});

/**
 *
 */
/**
 * Creates a UserProfileManagement instance dedicated to manage the communication with the WEB
 * service on user profiles.
 * @param [params] the policy parameters on the user fetch.
 * @param [params.async=true] the asynchronous property of the communication with the WEB service.
 * @param [params.id] the unique identifier of a user profile
 * @param [params.extended=false] a boolean indicating if the complete user profiles has to be taken into account.
 * @param [params.contacts=false] a boolean indicating if the relationships of the user has to be taken into account.
 * @param [params.group] the unique identifier of a user group the users should be in.
 * @param [params.name] a pattern about a user name (* is a wildcard)
 * @param [params.component] a unique identifier of a component instance to which the users must have right accesses.
 * @param [params.domain] an unique identifier of a domain the users should belong to.
 * @param [params.resource] an unique identifier of a resource in a component instance the users must have right accesses.
 * @param [params.roles] a String of comma-separated of role names. The roles the users should play.
 * @param [params.pagination] the pagination parameters on the users to fetch.
 * @param params.pagination.page the page number.
 * @param params.pagination.count the number of users in each page.
 * @class Object dedicated to the management of the user profiles at client side.
 * It that wraps the communication with the remote service that works on the user profiles.
 * You can give some parameters to filter the users on which this object works.
 */
function UserProfileManagement(params) {

  var self = this;

  /**
   * The filtering parameters to consider in the user profile fetch. Any of its property is optional:
   * @property {String} [ids] requested user identifiers
   * @property {String} [id] the unique identifier of a user profile
   * @property {Boolean} [extended=false] a boolean indicating if the complete user profiles has to be taken into account.
   * @property {Boolean} [contacts=false] a boolean indicating if the relationships of the user has to be taken into account.
   * @property {String} [group] the unique identifier of a user group the users should be in.
   * @property {String} [name] a pattern about a user name (* is a wildcard)
   * @property {String} [component] a unique identifier of a component instance to which the users must have right accesses.
   * @property {String} [domain] an unique identifier of a domain the users should belong to.
   * @property {String} [resource] an unique identifier of a resource in a component instance the users must have right accesses.
   * @property {String} [roles] a String of comma-separated of role names. The roles the users should play.
   * @property [pagination] the pagination parameters on the users to fetch.
   * @property {Number} pagination.page the page number.
   * @property {Number} pagination.count the number of users in each page.
   * @type {{id: null, extended: false, contacts: false, group: null, name: null, component: null, domain: null, resource: null, roles: null, pagination: null}
   */
  this.filter = {
    ids: null, // user identifiers
    id: null, // the unique identifier of a user to get,
    extended: false, // a flag to load user full details
    contacts: false, // the contacts of the referred user. The id must be set to be taken into account
    group: null, // a user group unique identifier
    name: null, // a pattern about a user name (* is a wildcard)
    component: null, // a component instance identifier
    domain: null, // an identifier of the user domain the user belongs to
    resource: null, // an identifier of a resource belonging to a component instance (the identifier
    // must be set the resource type following by resource identifier in Silverpeas).
    roles: null, // a string with a comma-separated role names
    pagination: null // pagination data in the form of
    // { page: <number of the page>, count: <count of users to fetch> }
  };

  /**
   * The asynchronous property of the communication with the remote WEB resource.
   * @default true
   * @type {boolean}
   */
  this.async = true;

  /**
   * The filtering parameters about the user profiles to get.
   */
  function setUpFilter(params) {
    if (params)
      for (var prop in params)
        if (prop != "async")
          self.filter[prop] = params[prop];
        else
          self.async = params.async;
  }

  /**
   * Decorates the user profiles with usefull method to improve the usability.
   */
  function decorate(users) {
    var decoratedUsers = [];
    if (users instanceof Array) {
      for (var i = 0; i < users.length; i++)
        decoratedUsers.push(new UserProfile(users[i]));
    } else {
      decoratedUsers.push(users);
    }
    return decoratedUsers;
  }

  setUpFilter(params);

  /**
   * The users it manages.
   * @default null
   * @type {UserProfile[]}
   */
  this.users = null;

  /**
   * Gets the user profiles matching the specified filtering parameters (if any) and passes them to
   * the specified callback 'loaded'.
   * If the parameters are passed, then they override those defined in the constructor.
   * The method returns the object itself.
   * @param [params] the parameters to consider when getting the users.
   * @param [params.async=true] the asynchronous property of the communication with the WEB service.
   * @param [params.ids] requested user identifiers.
   * @param [params.id] the unique identifier of a user profile
   * @param [params.extended=false] a boolean indicating if the complete user profiles has to be taken into account.
   * @param [params.contacts=false] a boolean indicating if the relationships of the user has to be taken into account.
   * @param [params.group] the unique identifier of a user group the users should be in.
   * @param [params.name] a pattern about a user name (* is a wildcard)
   * @param [params.component] a unique identifier of a component instance to which the users must have right accesses.
   * @param [params.domain] an unique identifier of a domain the users should belong to.
   * @param [params.resource] an unique identifier of a resource in a component instance the users must have right accesses.
   * @param [params.roles] a String of comma-separated of role names. The roles the users should play.
   * @param [params.pagination] the pagination parameters on the users to fetch.
   * @param params.pagination.page the page number.
   * @param params.pagination.count the number of users in each page.
   * @param {function} loaded a function to call once the user profiles are loaded.
   * @return {UserProfileManagement} itself.
   */
  this.get = function (params, loaded) {
    var urlOfUsers = userRootURL, separator = '?', application = '/application/';
    if (arguments.length == 1) {
      loaded = arguments[0];
      params = null;
    }
    setUpFilter(params);
    if (self.filter.ids) {
      $(self.filter.ids).each(function(index, id) {
        urlOfUsers += separator + 'ids=' + id;
        separator = '&';
      });
    }
    if (self.filter.id) {
      urlOfUsers += '/' + self.filter.id;
      if (self.filter.contacts)
        urlOfUsers += '/contacts';
      if (self.filter.extended) {
        urlOfUsers += separator + 'extended=' + self.filter.extended;
        separator = '&';
      }
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
      if (self.filter.resource) {
        urlOfUsers += separator + 'resource=' + self.filter.resource;
        separator = '&';
      }
    }
    if (self.filter.domain) {
      urlOfUsers += separator + 'domain=' + self.filter.domain;
      separator = '&';
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
      async: self.async,
      success: function (users, status, jqXHR) {
        self.users = decorate(users);
        self.users.maxlength = jqXHR.getResponseHeader('X-Silverpeas-UserSize');
        loaded(self.users);
      },
      error: function (jqXHR, textStatus, errorThrown) {
        window.console &&
        window.console.log(('Silverpeas Profile JQuery Plugin - UserProfileManagement - ERROR - ' +
            (errorThrown && errorThrown.length > 0 ? errorThrown :
                'Unknown error, could be reload of javascript while an ajax request is being ...')));
      }
    });
    return self;
  }
}

/**
 * Creates a UserGroupManagement instance dedicated to manage the communication with the WEB
 * service on user profiles.
 * @param [params] the policy parameters on the user fetch.
 * @param [params.async=true] the asynchronous property of the communication with the WEB service.
 * @param [params.ids] requested group identifiers.
 * @param [params.id] the unique identifier of a user profile
 * @param [params.name] a pattern about a group name (* is a wildcard)
 * @param [params.component] a unique identifier of a component instance to which the users in the group^must have right accesses.
 * @param [params.domain] an unique identifier of a domain the users in the group should belong to.
 * @param [params.resource] an unique identifier of a resource in a component instance the users in the group must have right accesses.
 * @param [params.roles] a String of comma-separated of role names. The roles the users in the group should play.
 * @param [params.pagination] the pagination parameters on the user groups to fetch.
 * @param params.pagination.page the page number.
 * @param params.pagination.count the number of groups in each page.
 * @class Object dedicated to the management of user groups at client side.
 * It that wraps the communication with the remote service that works on the user groups.
 * You can give some parameters to constraints the user groups on which this object should work.
 */
function UserGroupManagement(params) {

  var self = this;

  /**
   * The filtering parameters to consider in the user group fetch. Any of its property is optional:
   * @property {String} [ids] requested user identifiers
   * @property {String} [id] the unique identifier of a user group
   * @property {String} [url] the URL at which the user group is located. If null, the URL is then the one of the virtual root group.
   * @property {String} [name] a pattern about a group name (* is a wildcard)
   * @property {String} [component] a unique identifier of a component instance to which the users in the group^must have right accesses.
   * @property {String} [domain] an unique identifier of a domain the users in the group should belong to.
   * @property {String} [resource] an unique identifier of a resource in a component instance the users in the group must have right accesses.
   * @property {String} [roles] a String of comma-separated of role names. The roles the users in the group should play.
   * @property [pagination] the pagination parameters on the user groups to fetch.
   * @property {Number} pagination.page the page number.
   * @property {Number} pagination.count the number of groups in each page.
   * @type {{id: null, url: null, name: null, component: null, domain: null, roles: null, resource: null}}
   */
  this.filter = {
    ids: null,
    id: null,
    url: null,
    name: null,
    component: null,
    domain: null,
    roles: null,
    resource: null,
    pagination: null // pagination data in the form of
    // { page: <number of the page>, count: <count of users to fetch> }
  };

  /**
   * The asynchronous property of the communication with the remote WEB resource.
   * @default true
   * @type {boolean}
   */
  this.async = true;

  /**
   * The filtering parameters about the user groups to get.
   */
  function setUpFilter(params) {
    if (params)
      for (var prop in params)
        if (prop != "async")
          self.filter[prop] = params[prop];
        else
          self.async = params.async;
  }

  /**
   * Decorates the user groups with useful method to improve the usability:
   * - children(loaded) with loaded a callback called with as argument the loaded groups,
   * - users([name], loaded) with name an optional argument to filter the users by their name, and
   * loaded a callback called with as argument the loaded users.
   */
  function decorate(groups) {
    var decoratedGroups = [];
    if (groups instanceof Array) {
      for (var i = 0; i < groups.length; i++)
        decoratedGroups.push(new UserGroup(groups[i]));
    } else {
      decoratedGroups.push(groups);
    }
    return decoratedGroups;
  }

  setUpFilter(params);

  /**
   * The groups it manages.
   * @default null
   * @type {UserGroup[]}
   */
  this.groups = null;

  /**
   * Gets the user groups matching the filters (if any) and passes them to the
   * specified callback 'loaded'.
   * If the filters are passed, then they override those defined in the constructor.
   * The method returns the object itself.
   * @param [params] the parameters to consider when getting the groups.
   * @param [params.async=true] the asynchronous property of the communication with the WEB service.
   * @param [params.ids] requested group identifiers.
   * @param [params.id] the unique identifier of a user profile
   * @param [params.name] a pattern about a group name (* is a wildcard)
   * @param [params.component] a unique identifier of a component instance to which the users in the group^must have right accesses.
   * @param [params.domain] an unique identifier of a domain the users in the group should belong to.
   * @param [params.resource] an unique identifier of a resource in a component instance the users in the group must have right accesses.
   * @param [params.roles] a String of comma-separated of role names. The roles the users in the group should play.
   * @param [params.pagination] the pagination parameters on the user groups to fetch.
   * @param params.pagination.page the page number.
   * @param params.pagination.count the number of groups in each page.
   * @param {function} loaded a function to call once the user groups are loaded.
   * @return {UserGroupManagement} itself.
   */
  this.get = function (params, loaded) {
    var urlOfGroups = groupRootURL, separator = '?';
    if (arguments.length == 1) {
      loaded = arguments[0];
      params = null;
    }
    setUpFilter(params);
    if (self.filter.ids) {
      $(self.filter.ids).each(function(index, id) {
        urlOfGroups += separator + 'ids=' + id;
        separator = '&';
      });
    }
    if (self.filter.id)
      urlOfGroups = groupRootURL + '/' + self.filter.id;
    else if (self.filter.url)
      urlOfGroups = self.filter.url;
    else if (self.filter.component) {
      urlOfGroups += '/application/' + self.filter.component;
      if (self.filter.roles) {
        urlOfGroups += separator + 'roles=' + self.filter.roles;
        separator = '&';
      }
      if (self.filter.resource) {
        urlOfGroups += separator + 'resource=' + self.filter.resource;
        separator = '&';
      }
    }
    if (self.filter.domain) {
      urlOfGroups += separator + "domain=" + self.filter.domain;
      separator = '&';
    }
    if (self.filter.id == null && self.filter.name && self.filter.name != '*') {
      urlOfGroups += separator + "name=" + self.filter.name;
      separator = '&';
    }
    if (self.filter.pagination) {
      urlOfGroups += separator + 'page=' + self.filter.pagination.page + ';' + self.filter.pagination.count;
    }
    $.ajax({
      url: urlOfGroups,
      type: 'GET',
      dataType: 'json',
      cache: false,
      async: self.async,
      success: function (groups, status, jqXHR) {
        self.groups = decorate(groups);
        self.groups.maxlength = jqXHR.getResponseHeader('X-Silverpeas-GroupSize');
        loaded(self.groups);
      },
      error: function (jqXHR, textStatus, errorThrown) {
        window.console &&
        window.console.log(('Silverpeas Profile JQuery Plugin - UserGroupManagement - ERROR - ' + (
            errorThrown && errorThrown.length > 0 ? errorThrown :
                'Unknown error, could be reload of javascript while an ajax request is being ...')));
      }
    });
    return self;
  }
}
