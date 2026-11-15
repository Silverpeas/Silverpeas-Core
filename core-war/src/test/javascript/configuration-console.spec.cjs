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

/* Browser tests for the real JSP markup and script with an isolated, in-memory REST endpoint.
 * Run with Node.js and Playwright installed; CHROME_PATH can select a local Chromium executable.
 */
const {test, before, after} = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const {chromium} = require(process.env.PLAYWRIGHT_MODULE || 'playwright');

const repository = path.resolve(__dirname, '../../../..');
const webapp = path.join(repository, 'core-war/src/main/webapp');
const labelsFile = path.join(repository,
    'core-configuration/src/main/config/properties/org/silverpeas/jobManagerPeas/multilang/configurationConsole_fr.properties');
const labels = Object.fromEntries(fs.readFileSync(labelsFile, 'utf8').split('\n')
    .filter(line => line && !line.startsWith('#')).map(line => {
      const separator = line.indexOf('=');
      return [line.slice(0, separator), line.slice(separator + 1)
          .replace(/\\u([\da-f]{4})/gi, (_, hex) => String.fromCharCode(Number.parseInt(hex, 16)))];
    }));
const escape = text => text.replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('"', '&quot;');
const jsp = fs.readFileSync(path.join(webapp, 'jobManagerPeas/jsp/configuration.jsp'), 'utf8');
const markup = jsp.replace(/<%--[\s\S]*?--%>/g, '').replace(/<%[\s\S]*?%>/g, '')
    .replace(/<c:forTokens[^>]*items="([^"]+)"[^>]*>[\s\S]*?<\/c:forTokens>/g,
        (_, keys) => keys.split(',').map(key => `<span data-message="${key}">${escape(labels[key])}</span>`).join(''))
    .replace(/<fmt:message key=["']([^"']+)["']\s*\/>/g, (_, key) => escape(labels[key]))
    .replace(/<c:url value=["']([^"']+)["']\s*\/>/g, (_, value) => value)
    .replace(/<(?:fmt|view|c):[^>]+\/>/g, '')
    .replaceAll('${configurationApi}', '/services/admin/configuration')
    .replaceAll("${sessionScope['SilverSessionController'].favoriteLanguage}", 'fr');

// Exercise the real request builder, JSON body handling and XMLHttpRequest transport.
// Only the surrounding utility functions are reduced; never simulate withParam/send behavior.
const silverpeas = fs.readFileSync(path.join(webapp, 'util/javaScript/silverpeas.js'), 'utf8');
function fragment(start, end) {
  const from = silverpeas.indexOf(start);
  const until = silverpeas.indexOf(end, from);
  assert.ok(from >= 0 && until > from, 'Silverpeas transport source boundaries must exist');
  return silverpeas.slice(from, until);
}
const urlStart = silverpeas.indexOf('    url : {');
const urlSource = silverpeas.slice(silverpeas.indexOf('{', urlStart),
    silverpeas.indexOf('    loadScript :', urlStart)).trim().replace(/,$/, '');
const ajaxHarness = `
  window.whenSilverpeasReady = callback => document.addEventListener('DOMContentLoaded', callback);
  window.StringUtil = {isDefined : value => value !== undefined && value !== null && String(value).length > 0};
  window.extendsObject = (...values) => Object.assign(...values.filter(value => typeof value === 'object'));
  window.sp = {};
  window.sp.url = ${urlSource};
  ${fragment("if (typeof window.SilverpeasClass === 'undefined')", 'if (!window.SilverpeasCache)')}
  ${fragment('if (!window.SilverpeasAjaxConfig)', "if (typeof window.silverpeasAjax === 'undefined')")}
  ${fragment("if (typeof window.silverpeasAjax === 'undefined')", '  window.silverpeasFormSubmit =')}
  }
  window.sp.ajaxRequest = url => new SilverpeasAjaxConfig(url);
`;

let browser;
before(async () => {
  browser = await chromium.launch({headless : true, executablePath : process.env.CHROME_PATH,
    args : ['--no-sandbox']});
});
after(async () => browser?.close());

async function fixture() {
  const context = await browser.newContext({viewport : {width : 1280, height : 900}});
  const page = await context.newPage();
  let file = {path : 'org/silverpeas/general.properties', revision : 'initial', properties : {
    enabled : 'false', description : '<img src=x onerror=alert(1)>', apiToken : 'test-value'
  }, documentation : {file : 'Documentation générale', properties : {
    enabled : 'Active cette option.\nEffet immédiat à la prochaine lecture.',
    description : '<script>window.injected = true</script>'
  }}};
  let failSave = false;
  const writes = [];
  page.setDefaultTimeout(4000);
  await page.addInitScript({content : ajaxHarness});
  await page.route('http://configuration.test/**', async route => {
    const url = new URL(route.request().url());
    if (url.pathname === '/') {
      return route.fulfill({contentType : 'text/html', body : markup});
    }
    if (url.pathname.endsWith('.js') || url.pathname.endsWith('.css')) {
      return route.fulfill({contentType : url.pathname.endsWith('.js') ? 'text/javascript' : 'text/css',
        body : fs.readFileSync(path.join(webapp, url.pathname), 'utf8')});
    }
    if (url.pathname.endsWith('/files')) {
      return route.fulfill({json : [file.path, 'looks/aurora/skin.properties']});
    }
    if (url.pathname.endsWith('/file')) {
      if (route.request().method() === 'PUT') {
        const body = route.request().postDataJSON();
        writes.push(body);
        if ((body.path || url.searchParams.get('path')) !== file.path) {
          return route.fulfill({status : 400, json : {error : 'configuration.invalid'}});
        }
        if (failSave) {
          return route.fulfill({status : 500, json : {error : 'configuration.ioError'}});
        }
        if (body.revision !== file.revision) {
          return route.fulfill({status : 409, json : {error : 'configuration.conflict'}});
        }
        file = {...file, revision : file.revision + '-saved', properties : {...file.properties, ...body.properties}};
      }
      return route.fulfill({json : file});
    }
    return route.fulfill({status : 404, body : ''});
  });
  await page.goto('http://configuration.test/');
  await page.locator('[data-path="org/silverpeas/general.properties"]').waitFor();
  await page.locator('[data-path="org/silverpeas/general.properties"]').click();
  await page.locator('#configuration-properties tbody tr').first().waitFor();
  return {context, page, writes, fail() {failSave = true;}, externalChange() {
    file = {...file, revision : 'external', properties : {...file.properties, enabled : 'external'}};
  }};
}

const inputFor = (page, key) => page.getByLabel(key, {exact : true});

test('shows folder tree, plain-text help and values, and masks sensitive fields', async () => {
  const {context, page} = await fixture();
  try {
    assert.equal(await inputFor(page, 'apiToken').getAttribute('type'), 'password');
    assert.equal(await inputFor(page, 'description').inputValue(), '<img src=x onerror=alert(1)>');
    const help = page.locator('tr').filter({has : page.locator('label', {hasText : /^enabled$/})})
        .locator('.configuration-help button');
    await help.focus();
    await help.press('Enter');
    assert.equal(await help.locator('..').locator('pre').textContent(),
        'Active cette option.\nEffet immédiat à la prochaine lecture.');
    const tooltip = await help.locator('..').locator('pre').boundingBox();
    assert.ok(tooltip.y >= 0 && tooltip.y + tooltip.height <= 900);
    assert.equal(await page.evaluate(() => window.injected), undefined);
    await page.locator('#configuration-file-help summary').click();
    assert.equal(await page.locator('#configuration-file-help pre').textContent(), 'Documentation générale');
    await page.locator('#configuration-file-filter').fill('aurora');
    assert.equal(await page.locator('[data-path]').count(), 1);
    if (process.env.CONFIGURATION_SCREENSHOT) {
      await page.locator('#configuration-file-filter').fill('');
      await page.screenshot({path : process.env.CONFIGURATION_SCREENSHOT, fullPage : true});
    }
  } finally { await context.close(); }
});

test('edits, filters, undoes and saves only changed keys with the revision', async () => {
  const {context, page, writes} = await fixture();
  try {
    await inputFor(page, 'enabled').fill('true');
    await page.locator('#configuration-modified-only').check();
    assert.equal(await page.locator('#configuration-properties tbody tr:visible').count(), 1);
    await page.locator('#configuration-save').click();
    await page.waitForFunction(() => document.querySelector('#configuration-message').textContent.includes('enregistrés'));
    assert.deepEqual(writes, [{path : 'org/silverpeas/general.properties', revision : 'initial', properties : {enabled : 'true'}}]);
    assert.equal(await page.locator('#configuration-save').isDisabled(), true);
    await page.locator('#configuration-modified-only').uncheck();
    await inputFor(page, 'enabled').fill('false');
    await page.locator('#configuration-cancel').click();
    assert.equal(await inputFor(page, 'enabled').inputValue(), 'true');
  } finally { await context.close(); }
});

test('keeps local input on conflicts, compares and explicitly rebases before saving', async () => {
  const {context, page, writes, externalChange} = await fixture();
  try {
    await inputFor(page, 'enabled').fill('true');
    externalChange();
    await page.locator('#configuration-save').click();
    await page.locator('#configuration-conflict').waitFor({state : 'visible'});
    assert.equal(await inputFor(page, 'enabled').inputValue(), 'true');
    assert.equal(await page.locator('#configuration-save').isDisabled(), true);
    await page.locator('#configuration-compare').click();
    await page.locator('#configuration-comparison').waitFor({state : 'visible'});
    assert.equal(await page.locator('#configuration-comparison tbody td').nth(1).textContent(), 'external');
    await page.locator('#configuration-rebase').click();
    assert.equal(await inputFor(page, 'enabled').inputValue(), 'true');
    await page.locator('#configuration-save').click();
    await page.waitForFunction(() => document.querySelector('#configuration-message').textContent.includes('enregistrés'));
    assert.equal(writes[1].revision, 'external');
  } finally { await context.close(); }
});

test('keeps edits after a failed save and guards file navigation', async () => {
  const {context, page, fail} = await fixture();
  try {
    await inputFor(page, 'enabled').fill('true');
    fail();
    await page.locator('#configuration-save').click();
    await page.waitForFunction(() => document.querySelector('#configuration-message').classList.contains('configuration-error'));
    assert.equal(await inputFor(page, 'enabled').inputValue(), 'true');
    await page.locator('#configuration-file-filter').fill('skin');
    page.once('dialog', dialog => dialog.dismiss());
    await page.locator('[data-path="looks/aurora/skin.properties"]').click();
    assert.equal(await inputFor(page, 'enabled').inputValue(), 'true');
    await page.setViewportSize({width : 600, height : 900});
    assert.equal(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth), true);
  } finally { await context.close(); }
});
