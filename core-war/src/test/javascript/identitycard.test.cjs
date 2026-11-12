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
    removeChild(node) { this.childNodes.splice(this.childNodes.indexOf(node), 1); },
    prepend(node) { this.childNodes.unshift(node); },
    setAttribute(name, value) { this[name] = value; }
  };
}

function card(fields) {
  return {
    fields,
    getAttribute: name => name === 'rel' ? '2' : null,
    querySelectorAll: () => fields,
    querySelector: () => null
  };
}

async function render(user, fields, modelFields = fields, config = {}) {
  const rendered = card(fields);
  if (config.currentVersion) {
    rendered.getAttribute = name => name === 'data-identitycard-version' ? '1' : '2';
  }
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
  let finishTemplate;
  const warnings = [];
  const context = {
    console: { warn: (...args) => warnings.push(args) },
    Promise,
    webContext: '/silverpeas',
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
      if (config.deferTemplate) { finishTemplate = () => options.success(template); }
      else if (config.templateError && options.url.startsWith('/weblib/')) { options.error(); }
      else { options.success(config.templateError ? readFileSync(resolve(webRoot,
          'wysiwyg/jsp/ckeditor/plugins/identitycard/template.html'), 'utf8') : template); }
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
  if (config.deferTemplate) {
    await Promise.resolve();
    await Promise.resolve();
  } else {
    await Promise.all(pending);
  }
  return { finishTemplate, rendered, context, pending, requests, templateRequests, loadedTemplate, warnings };
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

test('a field absent from the profile has its placeholder cleared', async () => {
  const result = await render(user, [field('cellularPhone', 'cellularPhone')]);
  assert.equal(result.rendered.fields[0].firstChild.textContent, '');
});

test('formatted leading content is replaced instead of duplicated', async () => {
  const title = field('title');
  title.childNodes[0] = { nodeType: 1, textContent: 'old title', matches: () => false };
  await render(user, [title]);
  assert.equal(title.childNodes.length, 1);
  assert.equal(title.firstChild.textContent, 'Nouveau titre');
});

test('class order and extra styling do not trigger rebuilding', async () => {
  const title = field('title');
  title.className = 'styled title field';
  title.classList = ['styled', 'title', 'field'];
  const result = await render(user, [title], [field('title')]);
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
  const indicator = { nodeType: 1, matches: selector => selector === '.userzoom-infoConnection' };
  name.childNodes.push(indicator);
  const result = await render(user, [name]);
  assert.equal(name.firstChild.textContent, 'lulu lulu');
  assert.equal(name.childNodes[1], indicator);
  assert.equal(name.rel, '2');
  assert.equal(result.requests, 1);
});

async function edit(user, config = {}) {
  const title = field('title', 'title');
  const email = field('emailAddress', 'email');
  const fields = [title, email];
  fields.forEach(function(item) {
    Object.defineProperty(item, 'textContent', {
      get() { return this.firstChild.textContent; },
      set(value) { this.firstChild.textContent = value; }
    });
  });
  const rendered = card(fields);
  rendered.setAttribute = (name, value) => { rendered[name] = value; };
  const widget = { data: { userId: '2' }, element: { $: rendered } };
  const context = {
    CKEDITOR: { plugins: { add: () => {} } },
    document: { createElement: () => ({ querySelector: () => ({ querySelectorAll: () => fields }) }) },
    webContext: '/silverpeas',
    $: { ajax: options => {
      if (config.staleProfile) { widget.data.userId = '3'; }
      options.success(user);
    } },
    console
  };
  runInNewContext('String.prototype.unescapeHTML = function() { return String(this).replaceAll("&amp;", "&"); };\n' + editorScript +
      '\nidentityCardTemplatePromise = Promise.resolve("template");', context);
  context.updateContent(widget);
  await Promise.resolve();
  await Promise.resolve();
  return { title: title.textContent, email: email.textContent, version: rendered['data-identitycard-version'] };
}

test('the editor keeps empty extended and standard fields without adding spaces', async () => {
  for (const value of ['', null]) {
    const result = await edit({ emailAddress: value, moreData: { title: value } });
    assert.equal(result.title, '');
    assert.equal(result.email, '');
    assert.equal(result.version, '1');
  }
});

test('the editor preserves populated fields and clears missing properties', async () => {
  const result = await edit({ emailAddress: 'lulu@example.org', moreData: { title: 'Direction' } });
  assert.equal(result.title, 'Direction');
  assert.equal(result.email, 'lulu@example.org');
  assert.equal((await edit({})).title, '');
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

test('a failed profile request logs the original error', async () => {
  const result = await render(user, [field('title')], undefined, { profileError: true });
  assert.equal(result.warnings[0][1].message, 'Unavailable');
});

test('a later explicit activation retries a failed card', async () => {
  const result = await render(user, [field('title')], undefined, { profileError: true });
  result.context.window.activateIDCards();
  await Promise.all(result.pending);
  assert.equal(result.pending.length, 2);
  assert.equal(result.templateRequests, 0);
  assert.equal(result.warnings.length, 2);
});

test('cards saved by the new editor require no template request', async () => {
  const result = await render(user, [field('title')], undefined, { currentVersion: true });
  assert.equal(result.templateRequests, 0);
  assert.equal(result.rendered.fields[0].firstChild.textContent, 'Nouveau titre');
});

test('legacy cards are populated before the template request finishes', async () => {
  const result = await render(user, [field('title', 'old')], undefined, { deferTemplate: true });
  assert.equal(result.rendered.fields[0].firstChild.textContent, 'Nouveau titre');
  result.finishTemplate();
  await Promise.all(result.pending);
});

test('the editor loads the same shipped default when no custom template exists', async () => {
  let plugin;
  const urls = [];
  const context = {
    document: { createElement: () => ({ querySelector: () => ({ outerHTML: 'default' }) }) },
    CKEDITOR: { plugins: { add: (name, definition) => { plugin = definition; } }, dtd: { $removeEmpty: { span: 1 } } },
    $: { ajax: options => {
      urls.push(options.url);
      if (urls.length === 1) { options.error(); } else { options.success('default'); }
    } }
  };
  runInNewContext(editorScript, context);
  plugin.beforeInit.call({ path: '/silverpeas/wysiwyg/jsp/ckeditor/plugins/identitycard/' });
  assert.equal(context.CKEDITOR.dtd.$removeEmpty.span, false);
  assert.deepEqual(urls, ['/weblib/ckeditor/identitycard/template.html',
    '/silverpeas/wysiwyg/jsp/ckeditor/plugins/identitycard/template.html']);
});

test('insertion waits for the configured template to finish loading', async () => {
  let plugin;
  let finishTemplate;
  let beforeCommand;
  let executions = 0;
  let cancelled = false;
  const editor = {
    widgets: {
      registered: {},
      add(name, definition) { this.registered[name] = { template: { source: definition.template } }; }
    },
    ui: { addButton: () => {} },
    lang: { identitycard: {} },
    on(name, callback) { if (name === 'beforeCommandExec') { beforeCommand = callback; } },
    execCommand() { executions++; }
  };
  const context = {
    document: { createElement: () => ({ querySelector: () => ({ outerHTML: 'configured template' }) }) },
    CKEDITOR: { plugins: { add: (name, definition) => { plugin = definition; } },
      dtd: { $removeEmpty: {} }, dialog: { add: () => {} } },
    $: { ajax: options => { finishTemplate = options.success; } }
  };
  runInNewContext(editorScript, context);
  plugin.beforeInit.call({ path: '/plugin/' });
  plugin.init.call({ path: '/plugin/' }, editor);
  beforeCommand({ data: { name: 'identitycard' }, cancel() { cancelled = true; } });
  assert.equal(cancelled, true);
  assert.equal(executions, 0);
  finishTemplate('template');
  await Promise.resolve();
  await Promise.resolve();
  await Promise.resolve();
  assert.equal(editor.widgets.registered.identitycard.template.source, 'configured template');
  assert.equal(executions, 1);
});

test('a profile response for a previous selection cannot overwrite the current widget', async () => {
  const result = await edit({ moreData: { title: 'old selection' } }, { staleProfile: true });
  assert.equal(result.title, 'title');
  assert.equal(result.version, undefined);
});
