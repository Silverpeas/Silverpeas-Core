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

const script = readFileSync(resolve(__dirname,
    '../../main/webapp/selection/jsp/javaScript/selection.js'), 'utf8');

function newSelection(multiselection, lockedIds) {
  const context = { console: { info: () => {} } };
  runInNewContext(script + '\nthis.Selection = Selection;', context);
  return new context.Selection(multiselection, 0, lockedIds);
}

function user(id) {
  return { id, uri: '/users/' + id };
}

function select(selection, ...ids) {
  ids.forEach(id => selection.add(user(id)));
}

function idsOf(selection) {
  return Array.from(selection.items, item => item.id);
}

test('a locked item cannot be removed from the selection', () => {
  const selection = newSelection(true, ['1']);
  select(selection, '1', '2');

  selection.remove(user('1'));

  assert.deepEqual(idsOf(selection), ['1', '2']);
});

test('an unlocked item can be removed from the selection', () => {
  const selection = newSelection(true, ['1']);
  select(selection, '1', '2');

  selection.remove(user('2'));

  assert.deepEqual(idsOf(selection), ['1']);
});

test('clearing the selection keeps the locked items', () => {
  const selection = newSelection(true, ['1', 3]);
  select(selection, '1', '2', '3');

  selection.clear();

  assert.deepEqual(idsOf(selection), ['1', '3']);
});

test('clearing the selection without any locked items empties it', () => {
  const selection = newSelection(true);
  select(selection, '1', '2');

  selection.clear();

  assert.deepEqual(idsOf(selection), []);
});

test('a locked item isn\'t replaced in a single selection', () => {
  const selection = newSelection(false, ['1']);
  selection.add(user('1'));

  selection.add(user('2'));

  assert.deepEqual(idsOf(selection), ['1']);
});

test('an unlocked item is replaced in a single selection', () => {
  const selection = newSelection(false, ['1']);
  selection.add(user('2'));

  selection.add(user('3'));

  assert.deepEqual(idsOf(selection), ['3']);
});

test('a locked item not yet selected can be selected', () => {
  const selection = newSelection(true, ['1']);

  selection.add(user('1'));

  assert.equal(selection.isLocked(user('1')), true);
  assert.deepEqual(idsOf(selection), ['1']);
});
