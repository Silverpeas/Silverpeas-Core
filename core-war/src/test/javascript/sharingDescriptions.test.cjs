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

const sharingDirectory = resolve(__dirname,
    '../../../../core-configuration/src/main/config/data/web/weblib.war/repositorySharing');

function loadFunction(file, name) {
  const source = readFileSync(resolve(sharingDirectory, file), 'utf8');
  const declaration = new RegExp('^([ \\t]*)function ' + name + '\\(', 'm').exec(source);
  assert.ok(declaration, `Missing function ${name}`);
  const end = source.indexOf('\n' + declaration[1] + '}', declaration.index);
  assert.notEqual(end, -1);
  return source.slice(declaration.index, end + declaration[1].length + 2);
}

function render(file, description) {
  const html = new Map();
  const $ = selector => {
    let key = selector;
    const element = new Proxy({}, {
      get: (_, method) => (...args) => {
        if (method === 'addClass') key = '.' + args[0];
        if (method === 'html') html.set(key, args[0]);
        return element;
      }
    });
    return element;
  };
  const publication = { name: 'Publication', description,
    attachments: [{ id: '1', logicalName: 'document.txt', description, size: 1 }] };
  const context = { $, baseURL: '', token: 'token', clickOnFile: () => {},
    bytesToSize: () => '1 B', sharingAjax: (_, callback) => callback(publication),
    alert: error => { throw error; } };
  const functions = loadFunction(file, 'displayPublication') + '\n' +
      loadFunction(file, 'displayAttachments');
  if (file === 'explorer.js') {
    return runInNewContext(functions + '\ndisplayPublication(publication)',
        { ...context, publication });
  }
  runInNewContext(functions + '\ndisplayPublication()', context);
  return html;
}

const cases = [
  ['LF', 'first\nsecond\nthird', 'first<br/>second<br/>third'],
  ['CRLF', 'first\r\nsecond\r\nthird', 'first<br/>second<br/>third'],
  ['consecutive line breaks', 'first\n\nthird', 'first<br/><br/>third'],
  ['existing markup', '<b>first</b>\nsecond\nthird', '<b>first</b><br/>second<br/>third']
];

for (const [name, description, expected] of cases) {
  test(`explorer publication renders all ${name}`, () => {
    assert.ok(render('explorer.js', description).includes(
        '<p class="description">' + expected + '</p>'));
  });
  test(`publication and attachment descriptions render all ${name}`, () => {
    const html = render('publication.js', description);
    assert.equal(html.get('.publiDesc'), expected);
    assert.equal(html.get('.description'), expected);
  });
}
