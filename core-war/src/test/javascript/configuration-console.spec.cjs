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
    .replace('<view:looknfeel/>', '<link rel="stylesheet" href="/util/styleSheets/silverpeas-main.css">')
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

async function fixture(options = {}) {
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
  const persisted = [];
  let targets = options.targets || [{name : 'CustomerSettings.xml', revision : 'absent', exists : false}];
  let persistenceConflict = false;
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
    if (/\.(png|gif|svg)$/.test(url.pathname)) {
      const image = path.join(webapp, url.pathname);
      return fs.existsSync(image) ? route.fulfill({body : fs.readFileSync(image),
        contentType : url.pathname.endsWith('.svg') ? 'image/svg+xml'
            : (url.pathname.endsWith('.png') ? 'image/png' : 'image/gif')})
          : route.fulfill({status : 404, body : ''});
    }
    if (url.pathname.endsWith('/files')) {
      return route.fulfill({json : [file.path, 'looks/aurora/skin.properties']});
    }
    if (url.pathname.endsWith('/persistence')) {
      if (route.request().method() === 'POST') {
        const body = route.request().postDataJSON();
        persisted.push(body);
        if (persistenceConflict) {
          return route.fulfill({status : 409, json : {error : 'configuration.conflict'}});
        }
        const saved = {...body.target, revision : 'xml-saved', exists : true};
        targets = targets.map(target => target.name === saved.name ? saved : target);
        return route.fulfill({json : saved});
      }
      return route.fulfill({json : targets});
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
  return {context, page, writes, persisted,
    conflictXml() {persistenceConflict = true;},
    recoverXml() {persistenceConflict = false; targets = targets.map(t => ({...t, revision : 'xml-external'}));},
    fail() {failSave = true;}, externalChange() {
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


test('persists only selected saved keys and automatically selects edited keys after saving', async () => {
  const {context, page, persisted} = await fixture();
  try {
    assert.equal(await page.locator('#configuration-persist').isDisabled(), true);
    await page.getByRole('checkbox', {name : 'Pérenniser la sélection : enabled', exact : true}).check();
    await inputFor(page, 'enabled').fill('true');
    assert.equal(await page.locator('#configuration-persist').isDisabled(), true);
    await page.locator('#configuration-save').click();
    await page.waitForFunction(() => document.querySelector('#configuration-message').textContent.includes('enregistrés'));
    assert.equal(await page.getByRole('checkbox', {name : 'Pérenniser la sélection : enabled', exact : true}).isChecked(), true);
    await page.locator('#configuration-persist').click();
    await page.waitForFunction(() => document.querySelector('#configuration-persistence-message').textContent.includes('CustomerSettings.xml'));
    assert.deepEqual(persisted, [{path : 'org/silverpeas/general.properties', revision : 'initial-saved',
      keys : ['enabled'], target : {name : 'CustomerSettings.xml', revision : 'absent', exists : false}}]);
    assert.equal(Object.hasOwn(persisted[0], 'properties'), false);
    assert.equal(await page.locator('#configuration-persist').isDisabled(), true);
    assert.equal(await inputFor(page, 'enabled').inputValue(), 'true');
  } finally { await context.close(); }
});

test('offers custom filenames in execution order and preserves selection after XML conflicts', async () => {
  const {context, page, persisted, conflictXml, recoverXml} = await fixture({targets : [
    {name : 'Company.xml', revision : 'one', exists : true},
    {name : 'Z-Overrides.xml', revision : 'two', exists : true}
  ]});
  try {
    assert.equal(await page.locator('#configuration-persistence-target').inputValue(), 'Z-Overrides.xml');
    await page.locator('#configuration-persistence-target').selectOption('Company.xml');
    await page.getByRole('checkbox', {name : 'Pérenniser la sélection : enabled', exact : true}).check();
    conflictXml();
    await page.locator('#configuration-persist').click();
    await page.waitForFunction(() => document.querySelector('#configuration-persistence-message').classList.contains('configuration-error'));
    assert.equal(await page.getByRole('checkbox', {name : 'Pérenniser la sélection : enabled', exact : true}).isChecked(), true);
    recoverXml();
    await page.locator('#configuration-persistence-refresh').click();
    await page.waitForFunction(() => !document.querySelector('#configuration-persistence-refresh').disabled);
    await page.locator('#configuration-persist').click();
    await page.waitForFunction(() => document.querySelector('#configuration-persistence-message').textContent.includes('Enregistré'));
    assert.equal(persisted[1].target.name, 'Company.xml');
    assert.equal(persisted[1].target.revision, 'xml-external');
  } finally { await context.close(); }
});


test('uses Silverpeas buttons and icons with accessible names and centers the checkbox label', async () => {
  const {context, page} = await fixture();
  try {
    const commands = page.locator('#configuration-console button:not(.configuration-file)');
    for (const button of await commands.all()) {
      assert.ok((await button.getAttribute('class')).split(' ').includes('sp_button'));
    }
    const refresh = page.getByRole('button', {name : 'Recharger le fichier', exact : true});
    assert.equal(await refresh.getAttribute('title'), 'Recharger le fichier');
    assert.equal(await refresh.locator('.configuration-visually-hidden').count(), 1);
    await page.getByRole('button', {name : 'Afficher', exact : true}).click();
    assert.equal(await inputFor(page, 'apiToken').getAttribute('type'), 'text');
    const hide = page.getByRole('button', {name : 'Masquer', exact : true});
    assert.equal(await hide.getAttribute('data-icon'), 'hide');
    await hide.click();
    assert.equal(await inputFor(page, 'apiToken').getAttribute('type'), 'password');
    const checkbox = await page.locator('#configuration-modified-only').boundingBox();
    const label = await page.locator('.configuration-modified-filter span').boundingBox();
    assert.ok(Math.abs(checkbox.y + checkbox.height / 2 - label.y - label.height / 2) <= 1);
    const colors = await page.evaluate(() => {
      const save = document.querySelector('#configuration-save');
      const probe = document.createElement('button');
      probe.className = 'sp_button';
      document.body.append(probe);
      const result = [getComputedStyle(save).backgroundColor, getComputedStyle(probe).backgroundColor];
      probe.remove();
      return result;
    });
    assert.equal(colors[0], colors[1]);
    if (process.env.CONFIGURATION_SCREENSHOT) {
      await page.screenshot({path : process.env.CONFIGURATION_SCREENSHOT, fullPage : true});
    }
  } finally { await context.close(); }
});


test('offers a single global undo action and uses scalable refresh and undo icons', async () => {
  const {context, page} = await fixture();
  try {
    assert.equal(await page.locator('#configuration-properties thead th').count(), 2);
    assert.equal(await page.locator('#configuration-properties tbody td button:not(.configuration-reveal)').count(), 0);
    const cancel = page.getByRole('button', {name : 'Annuler les modifications', exact : true});
    assert.equal(await cancel.locator('.configuration-visually-hidden').count(), 0);
    assert.equal(await cancel.isDisabled(), true);
    await inputFor(page, 'enabled').fill('true');
    await inputFor(page, 'description').fill('new description');
    await cancel.click();
    assert.equal(await inputFor(page, 'enabled').inputValue(), 'false');
    assert.equal(await inputFor(page, 'description').inputValue(), '<img src=x onerror=alert(1)>');
    assert.equal(await cancel.isDisabled(), true);
    const masks = await page.evaluate(() => [
      getComputedStyle(document.querySelector('#configuration-reload'), '::before').maskImage,
      getComputedStyle(document.querySelector('#configuration-cancel .configuration-command-icon')).maskImage
    ]);
    assert.ok(masks[0].includes('/jobManagerPeas/icons/refresh.svg'));
    assert.ok(masks[1].includes('/jobManagerPeas/icons/undo.svg'));
    if (process.env.CONFIGURATION_SCREENSHOT) {
      await inputFor(page, 'enabled').fill('true');
      await page.screenshot({path : process.env.CONFIGURATION_SCREENSHOT, fullPage : true});
    }
  } finally { await context.close(); }
});


test('shows standalone information and refresh icons without a button background', async () => {
  const {context, page} = await fixture();
  try {
    const icons = page.locator('.configuration-help button, #configuration-reload, #configuration-persistence-refresh');
    for (const icon of await icons.all()) {
      const background = await icon.evaluate(node => {
        const css = getComputedStyle(node);
        return [css.backgroundColor, css.backgroundImage, css.boxShadow];
      });
      assert.deepEqual(background, ['rgba(0, 0, 0, 0)', 'none', 'none']);
    }
    const modernIcons = await page.evaluate(() => Array.from(
      document.querySelectorAll('.configuration-icon-button'),
      node => getComputedStyle(node, '::before').maskImage));
    assert.ok(modernIcons.every(mask => mask.includes('/jobManagerPeas/icons/') && mask.includes('.svg')));
    const reveal = page.getByRole('button', {name : 'Afficher', exact : true});
    await reveal.click();
    const visibilityMask = await page.getByRole('button', {name : 'Masquer', exact : true})
        .evaluate(node => getComputedStyle(node, '::before').maskImage);
    assert.ok(visibilityMask.includes('/visibility-off.svg'));
    await page.getByRole('button', {name : 'Masquer', exact : true}).click();
    const refresh = page.locator('#configuration-reload');
    await refresh.hover();
    assert.equal(await refresh.evaluate(node => getComputedStyle(node).backgroundColor), 'rgba(0, 0, 0, 0)');
    await refresh.focus();
    await page.keyboard.press('Tab');
    await page.keyboard.press('Shift+Tab');
    assert.equal(await refresh.evaluate(node => document.activeElement === node), true);
    assert.equal(await refresh.evaluate(node => getComputedStyle(node).outlineStyle), 'solid');
    assert.equal(await refresh.evaluate(node => getComputedStyle(node).outlineWidth), '2px');
    await refresh.evaluate(node => node.blur());
    await page.locator('.configuration-heading').hover();
    if (process.env.CONFIGURATION_SCREENSHOT) {
      await page.screenshot({path : process.env.CONFIGURATION_SCREENSHOT, fullPage : true});
    }
  } finally { await context.close(); }
});
