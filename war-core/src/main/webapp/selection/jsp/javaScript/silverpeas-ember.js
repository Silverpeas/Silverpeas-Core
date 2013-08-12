/*
 * Copyright (C) 2000-2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Writer Free/Libre
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

(function() {
  ProfileAdapter = DS.RESTAdapter.extend({
    namespace: webContext + '/services/profile',
    findByURL: function(store, type, type, url, params, array) {
      var $adapter = this, data = undefined;
      if (params) {
        data = {data: params};
      }
      return this.ajax(url, "GET", data).then(function(json, headers) {
        $adapter.didFindQuery(store, type, json, headers, array);
      }).then(null, DS.rejectionHandler);
    },
    findQuery: function(store, type, query, recordArray) {
      var root = this.rootForType(type), adapter = this;

      return this.ajax(this.buildURL(root), "GET", {
        data: query
      }).then(function(json, headers) {
        adapter.didFindQuery(store, type, json, headers, recordArray);
      }).then(null, DS.rejectionHandler);
    },
    didFindQuery: function(store, type, payload, headers, recordArray) {
      var loader = DS.loaderFor(store);

      loader.populateArray = function(data) {
        data.maxlength = headers['X-Silverpeas-GroupSize'];
        recordArray.load(data);
      };

      get(this, 'serializer').extractMany(loader, payload, type);
    },
    ajax: function(url, type, hash) {
      var adapter = this;

      return new Ember.RSVP.Promise(function(resolve, reject) {
        hash = hash || {};
        hash.url = url;
        hash.type = type;
        hash.dataType = 'json';
        hash.context = adapter;

        if (hash.data && type !== 'GET') {
          hash.contentType = 'application/json; charset=utf-8';
          hash.data = JSON.stringify(hash.data);
        }

        hash.success = function(json, status, jqXHR) {
          Ember.run(null, resolve, json, jqXHR.getAllResponseHeaders());
        };

        hash.error = function(jqXHR, textStatus, errorThrown) {
          Ember.run(null, reject, jqXHR);
        };

        Ember.$.ajax(hash);
      });
    }
  });

  ProfileStore = DS.Store.extend({
    adapter: ProfileAdapter,
    find: function(type, query) {
      if (query && typeof query === 'object' && query.url) {
        return this.findByURL(type, query.url, query.filter);
      }
      return this._super(type, query);
    },
    findByURL: function(type, url, params) {
      var array = DS.AdapterPopulatedRecordArray.create({type: type, url: url, content: Ember.A(
                []), store: this});
      var adapter = this.adapterForType(type);

      Ember.assert("You tried to load an URL but you have no adapter (for " + type + ")", adapter);
      Ember.assert("You tried to load an URL but your adapter does not implement `findByURL`",
              adapter.findByURL);

      adapter.findByURL(this, type, url, params, array);

      return array;
    }
  });

  User = DS.Model.extend({
    firstName: DS.attr('string'),
    lastName: DS.attr('string'),
    avatar: DS.attr('string'),
    eMail: DS.attr('string'),
    domainName: DS.attr('string'),
    specificId: DS.attr('string'),
    domainId: DS.attr('string'),
    login: DS.attr('string'),
    accessLevel: DS.attr('string'),
    uri: DS.attr('string'),
    webPage: DS.attr('string'),
    tchatPage: DS.attr('string'),
    fullName: DS.attr('string'),
    language: DS.attr('string'),
    connected: DS.attr('boolean'),
    anonymous: DS.attr('bolean'),
    status: DS.attr('string'),
    relationships: function() {
      if (arguments.length > 0) {
        return User.find({url: this.get('contactsUri'), filter: arguments[0]});
      } else {
        return User.find({url: this.get('contactsUri')});
      }
    }
  });

  UserGroup = DS.Model.extend({
    name: DS.attr('string'),
    description: DS.attr('string'),
    domainName: DS.attr('string'),
    specificId: DS.attr('string'),
    domainId: DS.attr('string'),
    uri: DS.attr('string'),
    childrenUri: DS.attr('string'),
    usersUri: DS.attr('string'),
    userCount: DS.attr('number'),
    subgroups: function() {
      if (arguments.length > 0) {
        return UserGroup.find({url: this.get('childrenUri'), filter: arguments[0]});
      } else {
        return UserGroup.find({url: this.get('childrenUri')});
      }
    },
    users: function() {
      if (arguments.length > 0) {
        return User.find({group: this.get('id'), filter: arguments[0]});
      } else {
        return User.find({group: this.get('id')});
      }
    }
  });
})();

(function() {
  Selection = Ember.Object.extend({
    item: [],
    multipleSelection: false,
    pagesize: 6,
    startpage: 0,
    currentpage: function() {
      return (this.pagesize === 0 ? this.items : this.items.slice(this.startpage, this.startpage + this.pagesize));
    }.property(),
    page: function(pagenumber) {
      this.startpage = (pagenumber - 1) * this.pagesize;
    }.property(),
    add: function(item) {
      if (this.multipleSelection) {
        if (item instanceof Array)
          this.items = item;
        else
          this.items.push(item);
      } else {
        this.items.splice(0, 1);
        this.items[0] = (item instanceof Array ? item[0] : item);
      }
    },
    remove: function(item) {
      var index = this.indexOf(item);
      this.items.splice(index, 1);
    },
    clear: function() {
      this.items = [];
    },
    indexOf: function(item) {
      for (var i = 0; i < this.items.length; i++)
        if (item.id === this.items[i].id)
          return i;
      return -1;
    },
    length: function() {
      return this.items.length;
    },
    itemIdsAsString: function() {
      var ids = '';
      for (var i = 0; i < this.items.length - 1; i++)
        ids += this.items[i].id + ',';
      if (this.items.length > 0)
        ids += this.items[this.items.length - 1].id;
      return ids;
    }.property(),
    itemNamesAsString: function() {
      var names = '';
      for (var i = 0; i < this.items.length - 1; i++)
        names += this.items[i].name + ',';
      if (this.items.length > 0)
        names += this.items[this.items.length - 1].name;
      return names;
    }.property()
  });
})();