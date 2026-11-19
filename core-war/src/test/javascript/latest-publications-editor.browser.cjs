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
const assert = require('node:assert/strict');
const {mkdtempSync, rmSync, writeFileSync} = require('node:fs');
const {tmpdir} = require('node:os');
const {join, resolve} = require('node:path');
const {pathToFileURL} = require('node:url');
const {spawnSync} = require('node:child_process');

function checkEditor() {
  const result = document.getElementById('result');
  function check(condition, message) {
    if (!condition) {
      throw new Error(message);
    }
  }
  window.addEventListener('error', event => { result.textContent = event.message; });
  async function checkUserZoom() {
    jQuery.userZoom.initialized = true;
    jQuery.userZoom.currentUser = {
      onMyInvitations: () => Promise.resolve(),
      hasRelationWith: () => Promise.resolve(true)
    };
    jQuery.fn.relationShip = function() { return this; };
    const frame = document.createElement('iframe');
    frame.srcdoc = '<div class="silverpeas-latest-publications" data-space-id="1" ' +
        'data-limit="5" data-include-subspaces="false"></div>';
    window.fetch = async () => new Response('<span class="userToZoom" rel="2">Ada</span>',
        {headers: {'Content-Type': 'text/html'}});
    document.body.append(frame);
    for (let attempt = 0; attempt < 30; attempt++) {
      const author = frame.contentDocument?.querySelector('.userzoom-infoConnection');
      if (author) {
        jQuery(author.parentElement).trigger('mouseenter');
        await new Promise(resolve => setTimeout(resolve, 850));
        check(frame.contentDocument.querySelector('.userzoom-tooltip')?.textContent.includes('Ada'),
            'Hover did not open the user zoom tooltip in the iframe');
        return;
      }
      await new Promise(resolve => setTimeout(resolve, 50));
    }
    throw new Error('User zoom was not initialized after loading the iframe fragment');
  }
  const editor = CKEDITOR.replace('editor', {
    customConfig: '',
    extraPlugins: 'latestpublications,emoji',
    language: 'fr',
    toolbar: [['Latestpublications', 'EmojiPanel']],
    versionCheck: false
  });
  editor.on('instanceReady', function() {
    const button = document.querySelector('.cke_button__latestpublications');
    const label = button.querySelector('.cke_button__latestpublications_label');
    check(label.textContent === 'Publications', 'Unexpected toolbar label');
    check(getComputedStyle(label).display !== 'none', 'Toolbar label is hidden');
    check(button.title === 'Insérer les dernières publications d’un espace', 'Unexpected tooltip');
    const icon = button.querySelector('.cke_button_icon');
    check(getComputedStyle(icon).backgroundImage.includes('icons/'), 'Missing fragment icon');
    const emoji = document.querySelector('.cke_button__emojipanel .cke_button_icon');
    check(getComputedStyle(emoji).backgroundImage.includes('emoji-toolbar.png'),
        'Emoji button does not use the Silverpeas icon');
    editor.on('dialogShow', function(event) {
      const titleId = event.data.getElement().getAttribute('aria-labelledby');
      check(document.getElementById(titleId).textContent === 'Dernières publications',
          'Unexpected dialog title');
      checkUserZoom().then(() => { result.textContent = 'PASS'; })
          .catch(error => { result.textContent = error.message; });
    });
    button.click();
  });
}

const dir = mkdtempSync(join(tmpdir(), 'latest-publications-editor-'));
try {
  const page = join(dir, 'test.html');
  const webRoot = resolve(__dirname, '../../main/webapp');
  function script(path) {
    return '<script src="' + pathToFileURL(resolve(webRoot, path)) + '"></script>';
  }
  const setup = `<script>
    window.webContext = '/silverpeas';
    window.activateRelationShip = function() {};
    window.sp = {i18n: {get: key => key}};
    window.User = {get: async () => ({id: '2', fullName: 'Ada', connected: false})};
  </script>`;
  writeFileSync(page, '<!doctype html><meta charset="utf-8"><textarea id="editor"></textarea>' +
      '<pre id="result">pending</pre>' + setup +
      script('util/javaScript/jquery/jquery-3.3.1.min.js') +
      script('util/javaScript/jquery/jquery-ui.min.js') +
      script('util/javaScript/silverpeas-userZoom.js') +
      script('util/javaScript/silverpeas-latest-publications.js') +
      script('wysiwyg/jsp/ckeditor/ckeditor.js') + '<script>(' +
      checkEditor.toString() + ')();</script>');
  const chrome = spawnSync(process.env.CHROME_BIN || 'google-chrome', [
    '--headless', '--no-sandbox', '--disable-gpu', '--no-proxy-server', '--allow-file-access-from-files',
    '--user-data-dir=' + join(dir, 'profile'), '--virtual-time-budget=10000', '--dump-dom',
    pathToFileURL(page).href
  ], {encoding: 'utf8', timeout: 30000});
  assert.equal(chrome.status, 0, chrome.error || chrome.stderr);
  assert.equal(chrome.stdout.match(/<pre id="result">(.*?)<\/pre>/s)?.[1], 'PASS');
} finally {
  rmSync(dir, {recursive: true, force: true});
}
