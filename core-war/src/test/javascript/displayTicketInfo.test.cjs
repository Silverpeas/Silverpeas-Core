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
    '../../main/webapp/sharing/jsp/javaScript/displayTicketInfo.js'), 'utf8');

function page(status, noCode = false) {
  let loaded;
  let click;
  let options;
  let downloaded = false;
  let fetched = false;
  const error = { hidden: true, textContent: '' };
  const input = { value: 'correct' };
  const link = {
    href: '/LinkFile/Key/token/shared.html',
    dataset: { noCode: String(noCode), filename: 'shared.html', lockedError: 'locked',
      downloadError: 'invalid', verificationError: 'error', confirmLabel: 'Bestätigen' },
    addEventListener: (_, listener) => { click = listener; }
  };
  const download = { click: () => { downloaded = true; }, remove: () => {} };
  const dialog = { dialog: value => { if (typeof value === 'object') options = value; } };
  runInNewContext(script, {
    document: {
      addEventListener: (_, listener) => { loaded = listener; },
      getElementById: id => ({ downloadLink: link, securityCode: input,
        securityCodeError: error })[id],
      createElement: () => download,
      body: { appendChild: () => {} }
    },
    jQuery: () => dialog,
    fetch: async () => {
      fetched = true;
      return { status, ok: status === 200, headers: { get: () => 'text/html' },
        blob: async () => ({ type: 'text/html' }) };
    },
    URL: { createObjectURL: () => 'blob:shared', revokeObjectURL: () => {} },
    setTimeout: () => {}
  });
  loaded();
  let prevented = false;
  click({ preventDefault: () => { prevented = true; } });
  return { error, input, download, wasDownloaded: () => downloaded, wasFetched: () => fetched,
    wasPrevented: () => prevented, confirmLabel: () => options.buttons[0].text,
    confirm: () => options.buttons[0].click() };
}

async function settle() {
  await new Promise(resolve => setImmediate(resolve));
}

test('a successful HTML response downloads with its filename', async () => {
  const view = page(200);
  view.confirm();
  await settle();
  assert.equal(view.wasDownloaded(), true);
  assert.equal(view.download.download, 'shared.html');
  assert.equal(view.error.hidden, true);
});

for (const [status, message] of [[401, 'invalid'], [403, 'invalid'], [429, 'locked']]) {
  test(`HTTP ${status} displays an error and does not download`, async () => {
    const view = page(status);
    view.confirm();
    await settle();
    assert.equal(view.wasDownloaded(), false);
    assert.equal(view.error.hidden, false);
    assert.equal(view.error.textContent, message);
  });
}

test('an unprotected link keeps the browser download behavior', () => {
  const view = page(200, true);
  assert.equal(view.wasPrevented(), false);
  assert.equal(view.wasFetched(), false);
});

const repositoryScript = readFileSync(resolve(__dirname,
    '../../../../core-configuration/src/main/config/data/web/weblib.war/repositorySharing/sharing.js'),
    'utf8');

test('repository download failures use the translated message', async () => {
  let message;
  const context = {
    fetch: async () => ({ ok: false, status: 403 }),
    window: { i18n: { prop: key => `translated:${key}` } },
    alert: value => { message = value; },
    console: { error: () => {} }
  };
  runInNewContext(repositoryScript, context);
  await context.sharingDownload('/file', 'file.html');
  assert.equal(message, 'translated:sharing.download.error');
});

test('repository code errors use hidden and become visible after rejection', async () => {
  let options;
  const dialog = { innerHTML: '' };
  const error = { hidden: false, textContent: '' };
  const input = { value: 'wrong' };
  const context = {
    document: { getElementById: id => ({ securityDialog: dialog,
      securityCode: input, securityCodeError: error })[id] },
    window: { i18n: { prop: key => key } },
    $: () => ({ dialog: value => { options = value; } }),
    fetch: async () => ({ ok: true, status: 200, json: async () => false })
  };
  runInNewContext(repositoryScript, context);
  context.openSecurityCodeDialog('token', '', () => assert.fail('Code was rejected'));
  assert.match(dialog.innerHTML, /aria-live="polite" hidden/);
  options.buttons[0].click();
  assert.equal(error.hidden, true);
  await settle();
  assert.equal(error.hidden, false);
  assert.equal(error.textContent, 'sharing.security.invalid');
});

test('the confirmation button uses the label supplied by the localized JSP', () => {
  const jsp = readFileSync(resolve(__dirname,
      '../../main/webapp/sharing/jsp/displayTicketInfo.jsp'), 'utf8');
  assert.match(jsp, /data-confirm-label="<fmt:message key='GML\.ok'\/>"/);
  assert.equal(page(200).confirmLabel(), 'Bestätigen');
});
