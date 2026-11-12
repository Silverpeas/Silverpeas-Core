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
// Run explicitly with CHROME_BIN=/path/to/chrome node identitycard-editor.browser.cjs.
const assert = require('node:assert/strict');
const { readFileSync, writeFileSync, mkdtempSync, rmSync } = require('node:fs');
const { tmpdir } = require('node:os');
const { resolve, join } = require('node:path');
const { pathToFileURL } = require('node:url');
const { spawnSync } = require('node:child_process');
const webRoot = resolve(__dirname, '../../main/webapp');
const editorRoot = resolve(webRoot, 'wysiwyg/jsp/ckeditor');
const defaultTemplate = readFileSync(resolve(editorRoot, 'plugins/identitycard/template.html'), 'utf8');
const directory = mkdtempSync(join(tmpdir(), 'identitycard-editor-'));
const run = async function(defaultTemplate) {
  const checks = [];
  function check(name, condition) {
    if (!condition) { throw new Error(name); }
    checks.push(name);
  }
  const profile = { firstName: 'Ada', lastName: 'Lovelace', avatar: '', moreData: { title: null, phone: 42 } };
  let customTemplate;
  const templateUrls = [];
  window.webContext = '/silverpeas';
  String.prototype.unescapeHTML = function() {
    const textarea = document.createElement('textarea');
    textarea.innerHTML = String(this);
    return textarea.value;
  };
  window.$ = { ajax: options => {
    if (options.dataType === 'json') { options.success(profile); }
    else {
      templateUrls.push(options.url);
      if (options.url.startsWith('/weblib/') && !customTemplate) { options.error(); }
      else { options.success(customTemplate || defaultTemplate); }
    }
  } };
  function settle() { return new Promise(resolve => setTimeout(resolve, 30)); }
  async function createEditor(id) {
    const area = document.createElement('textarea');
    area.id = id;
    document.body.append(area);
    const editor = CKEDITOR.replace(id, { customConfig: '', extraPlugins: 'identitycard',
      allowedContent: true, versionCheck: false });
    await new Promise(resolve => editor.on('instanceReady', resolve));
    return editor;
  }
  function setData(editor, html) {
    return new Promise(resolve => editor.setData(html, resolve));
  }
  function cardFrom(html) {
    const container = document.createElement('div');
    container.innerHTML = html;
    return container.querySelector('.user-card');
  }
  const editor = await createEditor('first');
  await setData(editor, '<div class="user-card" rel="2"><span class="field userToZoom">old</span></div>');
  await settle();
  let saved = editor.getData();
  let card = cardFrom(saved);
  check('legacy editor cards regain missing fields', !!card.querySelector('.title'));
  check('empty fields serialize without non-breaking spaces', card.querySelector('.title').innerHTML === '');
  check('absent profile properties serialize empty', card.querySelector('.cellularPhone').innerHTML === '');
  check('numeric properties are rendered', card.querySelector('.phone').textContent === '42');
  check('newly saved cards carry the version marker', card.getAttribute('data-identitycard-version') === '1');
  check('editor uses the shipped fallback template', templateUrls.length === 2 && templateUrls[1].endsWith('/plugins/identitycard/template.html'));
  await setData(editor, saved);
  await settle();
  check('empty fields survive reopening', cardFrom(editor.getData()).querySelector('.title').innerHTML === '');
  await new Promise(resolve => editor.setMode('source', resolve));
  await new Promise(resolve => editor.setMode('wysiwyg', resolve));
  await settle();
  check('empty fields survive source mode round trips', cardFrom(editor.getData()).querySelector('.title').innerHTML === '');
  profile.moreData.title = 'R&amp;amp;D';
  await setData(editor, saved);
  await settle();
  check('a profile populated later updates an initially empty field', cardFrom(editor.getData()).querySelector('.title').textContent === 'R&amp;D');
  editor.destroy();
  // Test another editor against a nested custom template, with an intentionally omitted field.
  // Reset the loader to represent an independently loaded editing page.
  identityCardTemplatePromise = undefined;
  customTemplate = '<div class="user-card"><section><span class="field userToZoom">name</span>' +
      '<span class="field title">title</span></section></div>';
  const customEditor = await createEditor('second');
  await setData(customEditor, '<div class="user-card" rel="2"><span class="field userToZoom">old</span></div>');
  await settle();
  card = cardFrom(customEditor.getData());
  check('custom template nesting survives editor repair', !!card.querySelector('section > .title'));
  check('custom excluded fields remain excluded', !card.querySelector('.phone'));
  check('custom nested fields get their profile value', card.querySelector('.title').textContent === 'R&amp;D');
  customEditor.destroy();
  document.getElementById('result').textContent = JSON.stringify({ checks });
};
try {
  const page = join(directory, 'test.html');
  const url = pathToFileURL(resolve(editorRoot, 'ckeditor.js')).href;
  const pluginUrl = pathToFileURL(resolve(editorRoot, 'plugins/identitycard/plugin.js')).href;
  writeFileSync(page, '<!doctype html><meta charset="utf-8"><pre id="result">pending</pre>' +
      '<script src="' + url + '"></script><script src="' + pluginUrl + '"></script><script>(' +
      run.toString() + ')(' + JSON.stringify(defaultTemplate).replaceAll('<', '\\u003c') + ').catch(error => {' +
      'document.getElementById("result").textContent = "FAIL: " + error.stack;});</script>');
  const result = spawnSync(process.env.CHROME_BIN || 'google-chrome', [
    '--headless', '--no-sandbox', '--disable-gpu', '--no-proxy-server', '--allow-file-access-from-files',
    '--user-data-dir=' + join(directory, 'profile'), '--virtual-time-budget=10000', '--dump-dom', 'file://' + page
  ], { encoding: 'utf8', timeout: 30000 });
  assert.equal(result.status, 0, result.stderr);
  const match = result.stdout.match(/<pre id="result">(.*?)<\/pre>/s);
  assert.ok(match, result.stdout);
  assert.ok(match[1].startsWith('{"checks":'), match[1] + '\n' + result.stderr);
  console.log(match[1]);
} finally {
  rmSync(directory, { recursive: true, force: true });
}
