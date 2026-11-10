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
const {test} = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');

const source = fs.readFileSync(path.resolve(__dirname,
    '../../main/webapp/jobDomainPeas/js/userTwoFactor.js'), 'utf8');

function page() {
  const state = {params: {}, submitted: false};
  const dataset = {userId: '42', adminToken: 'secret & token', confirmation: 'Reset 2FA?'};
  const jQuery = value => {
    if (typeof value === 'function') {
      value();
      return;
    }
    assert.equal(value, '#reset-two-factor a');
    return {on: (event, handler) => {
      assert.equal(event, 'click');
      state.click = handler;
    }};
  };
  jQuery.popup = {confirm: (message, callback) => {
    state.message = message;
    state.confirm = callback;
  }};
  const form = {
    byPostMethod() { state.method = 'POST'; return this; },
    addParam(name, value) { state.params[name] = value; return this; },
    submit() { state.submitted = true; }
  };
  const sp = {formRequest: url => { state.url = url; return form; }};
  const document = {getElementById: id => {
    assert.equal(id, 'two-factor-reset-data');
    return {dataset};
  }};
  vm.runInNewContext(source, {jQuery, sp, document});
  state.click({preventDefault: () => { state.navigationPrevented = true; }});
  return state;
}

test('clicking reset prevents navigation and asks for confirmation', () => {
  const state = page();
  assert.equal(state.navigationPrevented, true);
  assert.equal(state.message, 'Reset 2FA?');
  assert.equal(state.submitted, false);
  assert.equal(state.url, undefined);
});

test('confirmation sends the user and security token in a POST form', () => {
  const state = page();
  state.confirm();
  assert.equal(state.method, 'POST');
  assert.equal(state.url, 'userResetTwoFactor');
  assert.deepEqual(state.params, {Iduser: '42', 'X-ATKN': 'secret & token'});
  assert.equal(state.submitted, true);
});
