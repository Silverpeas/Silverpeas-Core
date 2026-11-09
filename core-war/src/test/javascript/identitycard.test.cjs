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

const webRoot = resolve(__dirname, '../../main/webapp');
const viewerScript = readFileSync(resolve(webRoot, 'util/javaScript/silverpeas-identitycard.js'), 'utf8');
const editorScript = readFileSync(resolve(webRoot, 'wysiwyg/jsp/ckeditor/plugins/identitycard/plugin.js'), 'utf8');

function field(name, value = '') {
  return {
    className: 'field ' + name,
    classList: ['field', name],
    childNodes: [{ nodeType: 3, textContent: value }],
    get firstChild() { return this.childNodes[0]; },
    prepend(node) { this.childNodes.unshift(node); },
    setAttribute(name, value) { this[name] = value; }
  };
}

function card(fields) {
  return {
    fields,
    getAttribute: () => '2',
    querySelectorAll: () => fields,
    querySelector: () => null
  };
}

async function render(user, fields, modelFields = fields, config = {}) {
  const rendered = card(fields);
  // This fixture represents the DOM parsed from a custom template, including its wrapper.
  const template = '<div class="user-card"><section>custom layout</section></div>';
  Object.defineProperty(rendered, 'innerHTML', {
    set(value) {
      this.restoredMarkup = value;
      fields.splice(0, fields.length, ...modelFields.map(f => field(f.classList[1])));
    }
  });
  const model = { innerHTML: '<section>custom layout</section>', querySelectorAll: () => modelFields };
  const pending = [];
  let requests = 0;
  let templateRequests = 0;
  let loadedTemplate;
  const warnings = [];
  const context = {
    console: { warn: message => warnings.push(message) },
    Promise,
    whenSilverpeasEntirelyLoaded: callback => callback(),
    setTimeout: () => 1,
    clearTimeout: () => {},
    activateUserZoom: () => {},
    document: {
      createElement: () => ({
        set innerHTML(value) { loadedTemplate = value; },
        querySelector: () => model
      }),
      createTextNode: textContent => ({ nodeType: 3, textContent })
    },
    $: { ajax: options => {
      templateRequests++;
      if (config.templateError) { options.error(); } else { options.success(template); }
    } },
    User: { getExtended: () => {
      requests++;
      return config.profileError ? Promise.reject(new Error('Unavailable')) : Promise.resolve(user);
    } },
    sp: {
      element: {
        querySelectorAll: () => [rendered],
        querySelector: selector => fields.find(f => f.classList.includes(selector.slice(1)))
      },
      promise: { whenAllResolved: promises => { pending.push(...promises); return Promise.allSettled(promises); } }
    }
  };
  context.window = context;
  runInNewContext('String.prototype.unescapeHTML = function() { return String(this).replaceAll("&amp;", "&"); };\n' + viewerScript, context);
  context.window.activateIDCards();
  await Promise.all(pending);
  return { rendered, context, pending, requests, templateRequests, loadedTemplate, warnings };
}

const user = { firstName: 'lulu', lastName: 'lulu', moreData: { title: 'Nouveau titre' } };

test('a title added after an initially empty card is restored from the custom template', async () => {
  const result = await render(user, [field('userToZoom')], [field('userToZoom'), field('title')]);
  assert.equal(result.rendered.fields.find(f => f.classList.includes('title'))?.firstChild.textContent, 'Nouveau titre');
  assert.equal(result.rendered.restoredMarkup, '<section>custom layout</section>');
});

test('fields excluded from a custom template are not added', async () => {
  const result = await render(user, [field('userToZoom')], [field('userToZoom')]);
  assert.equal(result.rendered.fields.length, 1);
  assert.equal(result.rendered.restoredMarkup, undefined);
});

test('an existing title is updated and HTML entities are decoded once', async () => {
  const result = await render({ ...user, moreData: { title: 'R&amp;amp;D' } }, [field('title', 'ancien')]);
  assert.equal(result.rendered.fields[0].firstChild.textContent, 'R&amp;D');
});

test('empty and numeric profile fields do not interrupt the update', async () => {
  for (const value of [null, '', 1234]) {
    const result = await render({ ...user, moreData: { title: value } }, [field('title', 'ancien')]);
    assert.equal(result.rendered.fields[0].firstChild.textContent, value == null ? '' : String(value));
  }
});

test('updating a name preserves the connection indicator and restores its user reference', async () => {
  const name = field('userToZoom', 'ancien');
  const indicator = { nodeType: 1 };
  name.childNodes.push(indicator);
  const result = await render(user, [name]);
  assert.equal(name.firstChild.textContent, 'lulu lulu');
  assert.equal(name.childNodes[1], indicator);
  assert.equal(name.rel, '2');
  assert.equal(result.requests, 1);
});

function edit(user) {
  const title = {
    type: 1,
    getAttribute: () => 'field title',
    setHtml(value) { this.html = value; }
  };
  const email = {
    type: 1,
    getAttribute: () => 'field emailAddress',
    setHtml(value) { this.html = value; }
  };
  const fields = [title, email];
  const widget = {
    data: { userId: '2' },
    element: {
      setAttribute: () => {},
      getChildCount: () => fields.length,
      getChildren: () => ({ getItem: index => fields[index] })
    }
  };
  const jquery = () => ({ attr: () => '2' });
  jquery.ajax = options => options.success(user);
  jquery.each = (object, callback) => Object.entries(object).forEach(([key, value]) => callback(key, value));
  const context = { CKEDITOR: { plugins: { add: () => {} } }, $: jquery, jQuery: jquery };
  runInNewContext(editorScript, context);
  context.updateContent(widget);
  return { title: title.html, email: email.html };
}

test('the editor keeps empty extended and standard fields with a non-breaking space', () => {
  for (const value of ['', null]) {
    const result = edit({ emailAddress: value, moreData: { title: value } });
    assert.equal(result.title, '&nbsp;');
    assert.equal(result.email, '&nbsp;');
  }
});

test('the editor preserves populated fields', () => {
  const result = edit({ emailAddress: 'lulu@example.org', moreData: { title: 'Direction' } });
  assert.equal(result.title, 'Direction');
  assert.equal(result.email, 'lulu@example.org');
});


test('the default template restores a missing title when no custom template is available', async () => {
  const result = await render(user, [field('userToZoom')], [field('userToZoom'), field('title')], { templateError: true });
  assert.ok(result.loadedTemplate.includes('field title'));
  assert.equal(result.rendered.fields[1].firstChild.textContent, 'Nouveau titre');
});

test('a repeated activation does not fetch the same card again', async () => {
  const result = await render(user, [field('title')]);
  result.context.window.activateIDCards();
  await Promise.all(result.pending);
  assert.equal(result.pending.length, 1);
  assert.equal(result.templateRequests, 1);
});

test('a profile request failure leaves the card available for another activation', async () => {
  const result = await render(user, [field('title', 'ancien')], undefined, { profileError: true });
  assert.equal(result.rendered.__userCardActivated, false);
  assert.equal(result.rendered.fields[0].firstChild.textContent, 'ancien');
  assert.equal(result.warnings.length, 1);
});
