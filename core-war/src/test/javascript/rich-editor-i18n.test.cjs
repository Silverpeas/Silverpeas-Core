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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/* Visual editing only; canonical Markdown remains in the existing native form. */
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const source = path.join(__dirname, '../../main/webapp/util/javaScript/vuejs/components/comments/silverpeas-comments-templates.jsp');
const template = fs.readFileSync(source, 'utf8');
const keys = [...new Set([...template.matchAll(/key=['"](richEditor\.[^'"]+)['"]/g)].map(match => match[1]))];

test('every comment editor label has a translation in the default French English and German bundles', () => {
  assert.equal(keys.length, 19);
  for (const locale of ['', '_fr', '_en', '_de']) {
    const file = path.join(__dirname, '../../../../core-configuration/src/main/config/properties/org/silverpeas/richEditor/multilang/richEditorBundle' + locale + '.properties');
    const values = new Map(fs.readFileSync(file, 'utf8').split('\n').filter(line => line.startsWith('richEditor.'))
      .map(line => [line.slice(0, line.indexOf('=')), line.slice(line.indexOf('=') + 1)]));
    for (const key of keys) {
      assert.ok(values.get(key)?.trim(), locale + ': ' + key);
      assert.notEqual(values.get(key), key);
    }
  }
});

test('deployed JSP resolves all editor labels through the external Silverpeas configuration',
  {skip:!process.env.SILVERPEAS_TEST_URL}, async () => {
    const response = await fetch(process.env.SILVERPEAS_TEST_URL + '/util/javaScript/vuejs/components/comments/silverpeas-comments-templates.jsp');
    assert.equal(response.status, 200);
    const rendered = await response.text();
    const labels = [...rendered.matchAll(/data-[a-z-]+="([^"]*)"/g)].map(match => match[1]).filter(value => value !== 'true');
    assert.equal(labels.length, keys.length);
    assert.ok(labels.every(label => label.trim() && !label.includes('???')), JSON.stringify(labels));
  });
