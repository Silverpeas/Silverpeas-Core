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
const { readFileSync } = require('node:fs');
const { resolve } = require('node:path');
const { runInNewContext } = require('node:vm');
const { webcrypto } = require('node:crypto');
const script = readFileSync(resolve(__dirname,
    '../../main/webapp/util/javaScript/silverpeas-secret.js'), 'utf8');

function page() {
  const copied = [];
  class Element {
    constructor() {
      this.dataset = {};
      this.children = [];
      this.handlers = {};
      this.events = [];
      this.value = '';
      this.isConnected = true;
      this.disabled = false;
      this.readOnly = false;
    }
    append(...children) { this.children.push(...children); }
    setAttribute(name, value) { this[name] = value; }
    insertAdjacentElement(position, element) { this.controls = element; }
    addEventListener(name, handler) { this.handlers[name] = handler; }
    dispatchEvent(event) { this.events.push(event.type); this.handlers[event.type]?.(event); }
  }
  let mutation;
  const context = {
    window: {}, crypto: webcrypto,
    navigator: { clipboard: { writeText: async value => copied.push(value) } },
    document: { createElement: () => new Element() },
    Event: class { constructor(type) { this.type = type; } },
    MutationObserver: class {
      constructor(callback) { mutation = callback; }
      observe() {}
    }
  };
  runInNewContext(script, context);
  const input = new Element();
  const confirmation = new Element();
  input.form = { querySelector: () => confirmation };
  input.dataset.passwordConfirmation = 'confirmation';
  const labels = { generate: 'Generate', copy: 'Copy', generated: 'Generated',
    copied: 'Copied', error: 'Generation failed', copyError: 'Copy failed' };
  return { context, input, confirmation, copied, labels,
    secret: context.window.SilverpeasSecret, mutate: () => mutation() };
}

async function runTests() {
  const results = [];
  async function check(name, run) { await run(); results.push(name); }
  await check('sharing codes keep their length, four character families and diversity', () => {
    const {secret} = page();
    const values = new Set();
    for (let i = 0; i < 500; i++) {
      const value = secret.generate();
      assert.equal(value.length, 32);
      assert.match(value, /[a-z]/);
      assert.match(value, /[A-Z]/);
      assert.match(value, /[0-9]/);
      assert.match(value, /[^a-zA-Z0-9]/);
      values.add(value);
    }
    assert.equal(values.size, 500);
  });
  await check('generation has no insecure fallback when Web Crypto is unavailable', () => {
    const p = page();
    p.context.crypto = undefined;
    assert.throws(() => p.secret.generate());
  });
  await check('generation fills both fields and emits events without submitting', async () => {
    const p = page();
    p.secret.addControls(p.input, {labels: p.labels, generate: async () => 'new-secret'});
    const [generate, copy, status] = p.input.controls.children;
    assert.equal(generate.type, 'button');
    assert.equal(copy.disabled, true);
    await generate.handlers.click();
    assert.equal(p.input.value, 'new-secret');
    assert.equal(p.confirmation.value, 'new-secret');
    assert.deepEqual(p.input.events, ['input', 'change']);
    assert.deepEqual(p.confirmation.events, ['input', 'change']);
    assert.equal(status.textContent, 'Generated');
    assert.equal(copy.disabled, false);
    await copy.handlers.click();
    assert.deepEqual(p.copied, ['new-secret']);
    assert.equal(status.textContent, 'Copied');
  });
  await check('failed generation preserves both previous values and allows retry', async () => {
    const p = page();
    p.input.value = 'previous';
    p.confirmation.value = 'previous';
    p.secret.addControls(p.input, {labels: p.labels,
      generate: async () => { throw new Error('network'); }});
    const [generate, , status] = p.input.controls.children;
    await generate.handlers.click();
    assert.equal(p.input.value, 'previous');
    assert.equal(p.confirmation.value, 'previous');
    assert.equal(status.textContent, 'Generation failed');
    assert.equal(generate.disabled, false);
  });
  await check('a disabled field cannot be generated or overwritten by a pending request', async () => {
    const p = page();
    let resolve;
    let calls = 0;
    p.secret.addControls(p.input, {labels: p.labels, generate: () => {
      calls++;
      return new Promise(done => { resolve = done; });
    }});
    const [generate, copy] = p.input.controls.children;
    const pending = generate.handlers.click();
    await generate.handlers.click();
    assert.equal(calls, 1);
    p.input.disabled = true;
    p.mutate();
    resolve('discarded');
    await pending;
    assert.equal(p.input.value, '');
    assert.equal(p.confirmation.value, '');
    assert.equal(generate.disabled, true);
    assert.equal(copy.disabled, true);
  });
  await check('clipboard failure is reported without discarding the secret', async () => {
    const p = page();
    p.input.value = 'keep';
    p.context.navigator.clipboard.writeText = async () => { throw new Error('denied'); };
    p.secret.addControls(p.input, {labels: p.labels, generate: async () => 'unused'});
    const [, copy, status] = p.input.controls.children;
    await copy.handlers.click();
    assert.equal(status.textContent, 'Copy failed');
    assert.equal(p.input.value, 'keep');
  });
  await check('an invalid server response preserves the current password', async () => {
    const p = page();
    p.input.value = 'keep';
    p.secret.addControls(p.input, {labels: p.labels, generate: async () => undefined});
    const [generate, , status] = p.input.controls.children;
    await generate.handlers.click();
    assert.equal(p.input.value, 'keep');
    assert.equal(status.textContent, 'Generation failed');
  });
  await check('initializing twice does not add duplicate controls', () => {
    const p = page();
    const options = {labels: p.labels, generate: async () => 'secret'};
    p.secret.addControls(p.input, options);
    const controls = p.input.controls;
    p.secret.addControls(p.input, options);
    assert.equal(p.input.controls, controls);
  });
  return results;
}
module.exports = {runTests};
if (require.main === module) {
  require('node:test')('shared secret controls and sharing generator', runTests);
}
