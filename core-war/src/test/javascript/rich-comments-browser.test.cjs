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
const {chromium} = require('playwright');
const assets = path.join(__dirname, '../../main/webapp');
const templateSource = fs.readFileSync(path.join(assets, 'util/javaScript/vuejs/components/comments/silverpeas-comments-templates.jsp'), 'utf8');
const templates = Object.fromEntries([...templateSource.matchAll(/<silverpeas-component-template name="([^"]+)">([\s\S]*?)<\/silverpeas-component-template>/g)].map(([,name, content]) => [name,
  content.replace(/<fmt:message[^>]*\/>/g, tag => /key=['"]([^'"]+)/.exec(tag)?.[1] || 'Label')
      .replace(/\$\{silfn:escapeJs\(([^)]+)\)\}/g, (_, name) => name)
      .replace(/\$\{([^}]+)\}/g, (_, name) => name)
]));
async function setup(comments = []) {
  const browser = await chromium.launch({executablePath: process.env.CHROME_PATH, args: ['--no-sandbox']});
  try {
  const page = await browser.newPage();
  const errors = [];
  page.on('pageerror', error => errors.push(error.message));
  await page.setContent('<base href="http://comments.test/"><div id="app"></div>');
  for (const file of ['util/javaScript/vuejs/vue.js', 'util/javaScript/richEditor/vendor/markdown-it-15.0.3.min.js',
    'util/javaScript/richEditor/silverpeas-markdown.js', 'util/javaScript/richEditor/silverpeas-rich-content.js',
    'util/javaScript/richEditor/vendor/tiptap-3.32.2.min.js', 'util/javaScript/richEditor/silverpeas-rich-editor.js']) {
    await page.addScriptTag({path: path.join(assets, file)});
  }
  await page.addStyleTag({path: path.join(assets, 'util/styleSheets/richEditor.css')});
  await page.evaluate(({templates, comments}) => {
    window.webContext = '/silverpeas';
    window.components = {};
    window.SpVue = {component(name, definition) {components[name] = definition;}};
    window.VueJsAsyncComponentTemplateRepository = class {get(name, definition) {return {...definition, template: templates[name]};}};
    window.VuejsI18nTemplateMixin = {data() {return {messages: {}};}, methods: {addMessages(values) {
      if (Object.entries(values).some(([key,value]) => this.messages[key] !== value)) {this.messages = values;}
      return '';
    }}};
    window.SilverpeasSessionCache = class {get() {} put() {} remove() {}};
    window.whenSilverpeasEntirelyLoaded = callback => callback();
    window.activateUserZoom = () => {};
    window.StringUtil = {isNotDefined: value => !value};
    String.prototype.noHTML = function() {return String(this).replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;');};
    String.prototype.convertNewLineAsHtml = function() {return String(this).replaceAll('\n', '<br>');};
    window.extendsObject = (...objects) => Object.assign({}, ...objects);
    window.calls = [];
    window.savedComments = comments;
    window.failMentions = false;
    window.SilverpeasError = {add() {return this;}, show() {return false;}};
    window.sp = {contribution: {id: {from: () => ({asString: () => 'kmelia1:Publication:16'})}}, url: {format: url => url}, element: {scrollToWhenSilverpeasEntirelyLoaded() {}}, log: {error() {}}, ajaxRequest: url => {
      const request = {url, method: 'GET', params: {}};
      return {
        withParam(name, value) {request.params[name] = value; return this;},
        byPostMethod() {request.method = 'POST'; return this;},
        byPutMethod() {request.method = 'PUT'; return this;},
        byDeleteMethod() {request.method = 'DELETE'; return this;},
        send() {return Promise.resolve({responseText: '{}'});},
        async sendAndPromiseJsonResponse(body) {
          calls.push({...request, body});
          if (url.includes('mention-candidates')) {
            if (failMentions) {throw new Error('Unavailable');}
            return [{id:'3', name:'Élodie Martin'}];
          }
          if (request.method === 'GET') {return JSON.parse(JSON.stringify(savedComments));}
          const comment = {...body, id: body.id || String(savedComments.length + 1), creationDate:'Today',
            author: {id:'2', fullName:'Alice', avatar:''}};
          if (request.method === 'POST') {savedComments.push(comment);}
          else {savedComments = savedComments.map(item => item.id === comment.id ? comment : item);}
          return comment;
        }
      };
    }};
  }, {templates, comments});
  await page.addScriptTag({path: path.join(assets, 'util/javaScript/vuejs/components/comments/silverpeas-comments.js')});
  await page.evaluate(() => {
    const app = Vue.createApp({data() {return {user: {id:'2', fullName:'Alice', anonymous:false, guestAccess:false}};},
      template: '<silverpeas-comments ref="comments" component-id="kmelia1" resource-type="Publication" resource-id="16" from-component-id="kmelia1" :user="user"/>'});
    Object.entries(components).forEach(([name, definition]) => app.component(name, definition));
    for (const name of ['silverpeas-form-pane','silverpeas-fade-transition-group']) {app.component(name, {template:'<div><slot/></div>'});}
    app.component('silverpeas-mandatory-indicator', {template:'<span>*</span>'});
    app.component('silverpeas-popin', {emits:['api'], data() {return {opened:false, callback:undefined};},
      mounted() {this.$emit('api', {open: options => {this.callback = options.callback; this.opened = true; document.body.append(this.$refs.container);}});},
      beforeUnmount() {this.$refs.container.remove();},
      template:'<div><div ref="container" class="test-popin" v-show="opened"><slot/><button class="test-save" @click="callback()">Save</button></div></div>'});
    window.vm = app.mount('#app');
  });
  await page.locator('#edition-box .tiptap').waitFor();
  return {browser, page, errors};
  } catch (failure) {await browser.close(); throw failure;}
}

test('comments use the shared visual editor for formatting mentions and emoji and send explicit Markdown with a plain projection', async () => {
  const {browser, page, errors} = await setup();
  try {
    const editor = page.locator('#edition-box .tiptap');
    await editor.click();
    await editor.press('Control+b');
    await editor.pressSequentially('Bonjour');
    await editor.press('Control+b');
    await editor.pressSequentially(' @El');
    await page.getByRole('option', {name:'@Élodie Martin'}).waitFor();
    await editor.press('Enter');
    await page.locator('#edition-box .sp-editor-tools > button').first().click();
    await page.getByRole('button', {name:'🚀', exact:true}).click();
    await page.getByRole('button', {name:'GML.validate', exact:true}).click();
    await page.waitForFunction(() => calls.some(call => call.method === 'POST'));
    const sent = await page.evaluate(() => calls.find(call => call.method === 'POST'));
    assert.ok(sent.url.endsWith('?rich=true'));
    assert.equal(sent.body.format, 'MARKDOWN');
    assert.ok(sent.body.richText.includes('**Bonjour**'));
    assert.ok(sent.body.richText.includes('@[Élodie Martin](3)'));
    assert.ok(sent.body.richText.includes('🚀'));
    assert.ok(!sent.body.text.includes('**'));
    assert.ok(!sent.body.text.includes('(3)'));
    await page.locator('.sp-rich-content strong').waitFor();
    assert.equal(await page.locator('.sp-rich-content .sp-mention').textContent(), '@Élodie Martin');
    assert.equal(await editor.textContent(), '');
    assert.deepEqual(errors, []);
  } finally {await browser.close();}
});

test('historical comments remain literal and edit visually without interpreting their old Markdown-looking text', async () => {
  const literal = '**literal** [old link](https://example.org)';
  const {browser, page, errors} = await setup([{id:'1', text:literal, format:'PLAIN', author:{id:'2', fullName:'Alice'}, creationDate:'Yesterday'}]);
  try {
    assert.equal(await page.locator('#list-box pre.text').textContent(), literal);
    await page.getByRole('button', {name:'commentUpdateAction', exact:true}).click();
    const editor = page.locator('.test-popin .tiptap');
    assert.equal(await editor.textContent(), literal);
    assert.equal(await editor.locator('strong, a').count(), 0);
    await editor.press('End');
    await editor.pressSequentially(' updated');
    await page.locator('.test-save').click();
    await page.waitForFunction(() => calls.some(call => call.method === 'PUT'));
    const sent = await page.evaluate(() => calls.find(call => call.method === 'PUT').body);
    assert.equal(sent.format, 'MARKDOWN');
    assert.ok(sent.richText.includes('\\*\\*literal'));
    assert.equal(sent.text, literal + ' updated');
    await page.getByRole('button', {name:'commentUpdateAction', exact:true}).click();
    assert.equal(await editor.textContent(), literal + ' updated');
    assert.equal(await page.locator('.test-popin .tiptap').count(), 1);
    assert.deepEqual(errors, []);
  } finally {await browser.close();}
});

test('rich comment rendering escapes HTML and unsafe links and keeps code mentions inert', async () => {
  const source = '<img src=x onerror=alert(1)> [unsafe](javascript:alert(1)) `@[Code](3)` @[Élodie](3)';
  const {browser, page, errors} = await setup([{id:'1', text:'Projection', richText:source, format:'MARKDOWN', author:{id:'2', fullName:'Alice'}, creationDate:'Today'}]);
  try {
    const rendered = page.locator('.sp-rich-content');
    await rendered.waitFor();
    assert.equal(await rendered.locator('img, script, a[href^="javascript:"]').count(), 0);
    assert.equal(await rendered.locator('code').textContent(), '@[Code](3)');
    assert.equal(await rendered.locator('.sp-mention').count(), 1);
    assert.deepEqual(errors, []);
  } finally {await browser.close();}
});

test('failed mention lookup leaves the visual editor usable and unmount releases its DOM', async () => {
  const {browser, page, errors} = await setup();
  try {
    await page.evaluate(() => {failMentions = true;});
    const editor = page.locator('#edition-box .tiptap');
    await editor.click();
    await editor.pressSequentially('@Nobody');
    await page.waitForFunction(() => calls.some(call => call.url.includes('mention-candidates')));
    await editor.press('Escape');
    await editor.press('Control+a');
    await editor.pressSequentially('Still usable');
    assert.equal(await editor.textContent(), 'Still usable');
    await page.evaluate(() => {vm.$.appContext.app.unmount();});
    assert.equal(await page.locator('.tiptap, .sp-editor-tools').count(), 0);
    assert.deepEqual(errors, []);
  } finally {await browser.close();}
});
