/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
const { test } = require('node:test');
const assert = require('node:assert/strict');
const { readFileSync } = require('node:fs');
const { runInNewContext } = require('node:vm');
const { resolve } = require('node:path');

const javascriptDir = resolve(__dirname, '../../main/webapp/util/javaScript');
const script = readFileSync(resolve(javascriptDir, 'silverpeas-user-group-list.js'), 'utf8');
const silverpeas = readFileSync(resolve(javascriptDir, 'silverpeas.js'), 'utf8');
// the real implementation of the class inheritance used by the list of users and groups
const silverpeasClass = silverpeas.substring(
    silverpeas.indexOf("if (typeof window.SilverpeasClass === 'undefined')"),
    silverpeas.indexOf('if (!window.SilverpeasCache)'));

const USERS = ['1', '2', '3'].map(id => ({ id, fullName: 'User ' + id, avatar: '' }));
const GROUPS = ['4', '5'].map(id => ({ id, name: 'Group ' + id, userCount: 1 }));

class Element {
  constructor(tagName) {
    this.tagName = tagName;
    this.children = [];
    this.classes = new Set();
    this.attributes = {};
    this.style = {};
    this.listeners = {};
    this.value = '';
    this.classList = {
      add: (...names) => names.forEach(name => this.classes.add(name)),
      remove: name => this.classes.delete(name),
      contains: name => this.classes.has(name)
    };
  }

  set innerHTML(html) {
    this.children = [];
    this.html = html;
  }

  setAttribute(name, value) {
    this.attributes[name] = value;
  }

  appendChild(child) {
    this.children.push(child);
    return child;
  }

  addEventListener(event, listener) {
    this.listeners[event] = listener;
  }

  find(predicate) {
    return this.children.filter(predicate)
        .concat(...this.children.map(child => child.find(predicate)));
  }

  hasClass(name) {
    return this.classes.has(name);
  }
}

async function listOfUsersAndGroups(nonRemovableUserIds, nonRemovableGroupIds) {
  const root = new Element('div');
  const byIds = (profiles, ids) => Promise.resolve(profiles.filter(p => ids.includes(p.id)));
  const context = {
    webContext: '',
    currentUserId: '0',
    document: {
      createElement: tagName => new Element(tagName),
      querySelector: () => root
    },
    UserGroupListSettings: { get: () => undefined },
    sp: {
      i18n: { get: key => key },
      element: {
        querySelector: (selector, parent) => parent.find(e => e.hasClass('simple-details'))[0],
        querySelectorAll: (selector, parent) => parent.find(e => e.hasClass('access-list'))
      },
      promise: {
        whenAllResolved: promises => Promise.all(promises),
        resolveDirectlyWith: value => Promise.resolve(value)
      }
    },
    User: { get: params => byIds(USERS, params.id) },
    UserGroup: { get: params => byIds(GROUPS, params.ids) },
    StringUtil: { isNotDefined: value => !value },
    // as jQuery.extend, the undefined values are ignored
    extendsObject: (...objects) => Object.assign({}, ...objects.map(object =>
        Object.fromEntries(Object.entries(object).filter(([, value]) => value !== undefined)))),
    applyReadyBehaviorOn: instance => { instance.notifyReady = () => {}; },
    whenSilverpeasReady: callback => callback(),
    activateUserZoom: () => {},
    jQuery: () => ({ on: () => {} })
  };
  context.window = context;
  runInNewContext(silverpeasClass + script, context);
  const list = new context.ListOfUsersAndGroups({
    rootContainerId: 'root',
    userPanelInitUrl: 'UserPanel',
    initialUserIds: USERS.map(user => user.id),
    initialGroupIds: GROUPS.map(group => group.id),
    nonRemovableUserIds,
    nonRemovableGroupIds
  });
  await new Promise(resolve => setImmediate(resolve));
  return list;
}

function removableIdsIn(container, idAttribute) {
  return container.children
      .filter(item => item.children.some(child => child.hasClass('operation')))
      .map(item => item.attributes[idAttribute]);
}

test('the non removable users and groups cannot be removed one by one', async () => {
  const list = await listOfUsersAndGroups(['1', '3'], ['5']);

  assert.deepEqual(removableIdsIn(list.context.userContainer, 'user-id'), ['2']);
  assert.deepEqual(removableIdsIn(list.context.groupContainer, 'group-id'), ['4']);
});

test('all the users and groups can be removed one by one by default', async () => {
  const list = await listOfUsersAndGroups();

  assert.deepEqual(removableIdsIn(list.context.userContainer, 'user-id'), ['1', '2', '3']);
  assert.deepEqual(removableIdsIn(list.context.groupContainer, 'group-id'), ['4', '5']);
});

test('removing all the users and groups keeps the non removable ones', async () => {
  const list = await listOfUsersAndGroups(['1', '3'], ['5']);

  list.removeAll();

  assert.deepEqual(Array.from(list.context.currentUserIds), ['1', '3']);
  assert.deepEqual(Array.from(list.context.currentGroupIds), ['5']);
});

test('removing all is proposed only when there are removable users or groups', async () => {
  const list = await listOfUsersAndGroups(['1', '3'], ['5']);
  assert.equal(list.context.clearButton.style.display, '');

  list.removeAll();
  assert.equal(list.context.clearButton.style.display, 'none');
});
