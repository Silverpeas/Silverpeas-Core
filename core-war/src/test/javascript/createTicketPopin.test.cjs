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
const { resolve } = require('node:path');
const { runInNewContext } = require('node:vm');
const script = readFileSync(resolve(__dirname,
    '../../main/webapp/sharing/jsp/javaScript/createTicketPopin.js'), 'utf8');

function page() {
  const elements = new Map();
  const errors = [];
  const bundles = [];
  const dates = [];
  const windows = [];
  let request;
  let secured;
  let ready;
  function $(selector) {
    if (typeof selector === 'function') { ready = selector; return; }
    if (!elements.has(selector)) {
      const state = { value: '', visible: true, handlers: {} };
      state.val = function(value) {
        if (arguments.length) { state.value = value; return state; }
        return state.value;
      };
      state.text = value => { state.content = value; return state; };
      state.html = value => { state.content = value; return state; };
      state.show = () => { state.visible = true; return state; };
      state.hide = () => { state.visible = false; return state; };
      state.on = (event, handler) => { state.handlers[event] = handler; return state; };
      state.popup = (type, options) => { state.dialog = { type, options }; return state; };
      elements.set(selector, state);
    }
    return elements.get(selector);
  }
  $.toJSON = JSON.stringify;
  $.ajax = options => { request = options; };
  $.popup = { error: message => errors.push(message) };
  const context = { $, jQuery: $, webContext: '/silverpeas',
    sp: { i18n: { load: options => bundles.push(options), get: key => '[' + key + ']' } },
    document: { getElementById: () => ({ dataset: { language: 'de' } }) },
    isWhitespace: value => !value.trim(), isInteger: value => /^\d+$/.test(value),
    isDateOK: (date, language) => { dates.push([date, language]); return date === '31.12.2026'; },
    applyTokenSecurity: selector => { secured = selector; },
    SP_openWindow: (...args) => windows.push(args) };
  runInNewContext(script, context);
  ready();
  return { $, context, errors, bundles, dates, windows, request: () => request,
    secured: () => secured };
}

for (const [type, key] of [['Node', 'GML.theme'], ['Publication', 'GML.publication'],
  ['Attachment', 'GML.file']]) {
  test(`the ${type} popup loads translations and initializes the object`, () => {
    const view = page();
    view.context.createSharingTicketPopup({ type, componentId: 'kmelia1', id: '42', name: 'Title' });
    assert.equal(view.$('#hiddenType').val(), type);
    assert.equal(view.$('#hiddenObjectId').val(), '42');
    assert.equal(view.$('#objectName').content, '[' + key + ']');
    assert.equal(view.$('#sharingticket-popup-content').dialog.options.title, '[sharing.createTicket]');
    assert.equal(view.bundles[0].withGeneral, true);
    assert.equal(view.bundles[0].language, 'de');
    assert.equal(view.secured(), '#sharingticket-popup-content');
  });
}

test('limited tickets validate fields and use the JSP language for dates', () => {
  const view = page();
  view.context.createSharingTicketPopup({ type: 'Attachment' });
  view.$('#validity').val('1');
  let called = false;
  assert.equal(view.context.ifCorrectFormExecute(() => { called = true; }), false);
  assert.equal(called, false);
  assert.ok(view.errors[0].includes('[sharing.endDate]'));
  view.$('#nbAccessMax').val('3');
  view.$('#endDate').val('31.12.2026');
  assert.equal(view.context.ifCorrectFormExecute(() => { called = true; }), true);
  assert.equal(called, true);
  assert.deepEqual(view.dates, [['31.12.2026', 'de']]);
});

test('submitting a continuous ticket sends the security code and selected object', () => {
  const view = page();
  view.context.createSharingTicketPopup({ type: 'Publication', componentId: 'kmelia1', id: '42' });
  view.$('#securityCode').val('secret');
  assert.equal(view.$('#sharingticket-popup-content').dialog.options.callback(), true);
  const request = view.request();
  assert.equal(request.url, '/silverpeas/services/mytickets/kmelia1');
  assert.equal(request.type, 'POST');
  const data = JSON.parse(request.data);
  assert.equal(data.securityCode, 'secret');
  assert.equal(data.sharedObjectId, '42');
  request.success({ url: 'https://example.org/shared' });
  assert.equal(view.$('#securityCode').val(), '');
  assert.equal(view.$('#sharingticket-message-content').dialog.options.title,
      '[sharing.tickets] > [sharing.confirmTicket]');
});

test('external handlers update validity and open user selection', () => {
  const view = page();
  view.$('#hiddenType').val('Attachment');
  view.$('#validity').val('1');
  view.$('#validity').handlers.change();
  assert.equal(view.$('.threshold').visible, true);
  assert.equal(view.$('#nbAccessMaxArea').visible, true);
  view.$('#hiddenType').val('Node');
  view.$('#validity').handlers.change();
  assert.equal(view.$('#nbAccessMaxArea').visible, false);
  view.$('#validity').val('0');
  view.$('#validity').handlers.change();
  assert.equal(view.$('.threshold').visible, false);
  view.$('#users').val('12,34');
  let prevented = false;
  view.$('#selectSharingUsers').handlers.click({ preventDefault: () => { prevented = true; } });
  assert.equal(prevented, true);
  assert.ok(view.windows[0][0].includes('selectedUsers=12,34&selectionMultiple=true'));
});
