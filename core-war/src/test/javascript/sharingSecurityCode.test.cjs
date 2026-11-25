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
const {readFileSync} = require('node:fs');
const {resolve} = require('node:path');
const {runInNewContext} = require('node:vm');
const script = readFileSync(resolve(__dirname,
    '../../main/webapp/sharing/jsp/javaScript/securityCode.js'), 'utf8');

function page(generate) {
  let click;
  const errors = [];
  const button = {disabled: false, dataset: {errorMessage: 'Generation failed'}};
  const input = {value: 'previous', isConnected: true, events: [],
    dispatchEvent(event) { this.events.push(event.type); }};
  runInNewContext(script, {
    document: {addEventListener: (name, handler) => { click = handler; },
      getElementById: () => input},
    SilverpeasSecret: {generate},
    Event: class { constructor(type) { this.type = type; } },
    notyError: message => errors.push(message)
  });
  return {input, button, errors, click: () => click({target: {
    closest: selector => selector === '#generateSharingSecurityCode' ? button : null
  }})};
}

test('sharing uses the server profile, fills the field and emits change events', async () => {
  const p = page(async profile => {
    assert.equal(profile, 'sharing');
    return 'new-code';
  });
  await p.click();
  assert.equal(p.input.value, 'new-code');
  assert.deepEqual(p.input.events, ['input', 'change']);
  assert.equal(p.button.disabled, false);
});

test('sharing generation failure preserves the previous code and permits retry', async () => {
  const p = page(async () => { throw new Error('network'); });
  await p.click();
  assert.equal(p.input.value, 'previous');
  assert.deepEqual(p.errors, ['Generation failed']);
  assert.equal(p.button.disabled, false);
});

test('duplicate clicks do not issue concurrent generation requests', async () => {
  let resolve;
  let calls = 0;
  const p = page(() => {
    calls++;
    return new Promise(done => { resolve = done; });
  });
  const pending = p.click();
  assert.equal(p.button.disabled, true);
  await p.click();
  assert.equal(calls, 1);
  resolve('new-code');
  await pending;
  assert.equal(p.input.value, 'new-code');
  assert.equal(p.button.disabled, false);
});
