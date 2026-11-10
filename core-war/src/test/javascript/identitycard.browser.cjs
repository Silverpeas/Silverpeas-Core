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
// Run explicitly with CHROME_BIN=/path/to/chrome node identitycard.browser.cjs.
const assert = require('node:assert/strict');
const { readFileSync, writeFileSync, mkdtempSync, rmSync } = require('node:fs');
const { tmpdir } = require('node:os');
const { resolve, join } = require('node:path');
const { spawnSync } = require('node:child_process');
const script = readFileSync(resolve(__dirname,
    '../../main/webapp/util/javaScript/silverpeas-identitycard.js'), 'utf8');
const directory = mkdtempSync(join(tmpdir(), 'identitycard-'));
const run = async function(viewer) {
  const checks = [];
  async function render(markup, template, user) {
    const frame = document.createElement('iframe');
    document.body.append(frame);
    const w = frame.contentWindow;
    w.document.body.innerHTML = '<div class="user-card" rel="2">' + markup + '</div>';
    w.eval("String.prototype.unescapeHTML = function() { const t = document.createElement('textarea'); t.innerHTML = this; return t.value; };");
    w.setTimeout = () => 1;
    w.clearTimeout = () => {};
    w.whenSilverpeasEntirelyLoaded = fn => fn();
    w.$ = { ajax: options => options.success('<div class="user-card">' + template + '</div>') };
    w.User = { getExtended: () => Promise.resolve(user) };
    w.sp = { element: { querySelectorAll: selector => Array.from(w.document.querySelectorAll(selector)) },
      promise: { whenAllResolved: promises => Promise.all(promises) } };
    const finished = new Promise(resolve => { w.activateUserZoom = resolve; });
    w.eval(viewer);
    const card = w.document.querySelector('.user-card');
    const original = card.firstChild;
    w.activateIDCards();
    await finished;
    return { card, original };
  }
  function check(name, condition) {
    if (!condition) { throw new Error(name); }
    checks.push(name);
  }
  const user = { firstName: 'Ada', lastName: 'Lovelace', moreData: { title: 'R&amp;amp;D' } };
  let result = await render('<span class="field userToZoom">old</span>',
      '<section><span class="field userToZoom">name</span><span class="field title"><b>title</b></span>' +
      '<span class="field cellularPhone"><i>cellularPhone</i></span></section>', user);
  check('restored absent properties are empty', result.card.querySelector('.cellularPhone').textContent === '');
  check('restored formatted fields decode once', result.card.querySelector('.title').textContent === 'R&amp;D');
  check('custom nesting survives restoration', !!result.card.querySelector('section > .title'));
  result = await render('<span class="styled title field"><strong>old</strong><em>stale</em></span>',
      '<span class="field title">title</span>', user);
  check('class order and styling preserve existing field', result.card.firstChild === result.original);
  check('formatted and trailing stale content is replaced', result.card.firstChild.textContent === 'R&amp;D');
  check('leading formatting is preserved', !!result.card.querySelector('.title > strong'));
  result = await render('<span class="field userToZoom"><span class="userzoom-infoConnection">online</span></span>',
      '<span class="field userToZoom">name</span>', user);
  check('indicator-only field gets its name', result.card.firstChild.firstChild.textContent === 'Ada Lovelace');
  check('connection indicator survives', result.card.querySelector('.userzoom-infoConnection').textContent === 'online');
  document.getElementById('result').textContent = JSON.stringify({ checks });
};
try {
  const page = join(directory, 'test.html');
  writeFileSync(page, '<!doctype html><pre id="result">pending</pre><script>(' + run.toString() + ')(' +
      JSON.stringify(script).replaceAll('<', '\\u003c') + ').catch(error => {' +
      'document.getElementById("result").textContent = "FAIL: " + error.stack;});</script>');
  const result = spawnSync(process.env.CHROME_BIN || 'google-chrome', [
    '--headless', '--no-sandbox', '--disable-gpu', '--no-proxy-server',
    '--user-data-dir=' + join(directory, 'profile'), '--virtual-time-budget=3000', '--dump-dom', 'file://' + page
  ], { encoding: 'utf8', timeout: 30000 });
  assert.equal(result.status, 0, result.stderr);
  const match = result.stdout.match(/<pre id="result">(.*?)<\/pre>/s);
  assert.ok(match, result.stdout);
  assert.ok(match[1].startsWith('{"checks":'), match[1]);
  console.log(match[1]);
} finally {
  rmSync(directory, { recursive: true, force: true });
}
