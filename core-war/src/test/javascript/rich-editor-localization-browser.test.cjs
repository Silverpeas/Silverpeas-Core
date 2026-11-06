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
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const {chromium} = require('playwright');
const assets = path.join(__dirname, '../../main/webapp/util/javaScript/richEditor');
const bundles = path.join(__dirname, '../../../../core-configuration/src/main/config/properties/org/silverpeas/richEditor/multilang');
const source = fs.readFileSync(path.join(assets, 'silverpeas-rich-editor.js'), 'utf8');
const keys = JSON.parse(/const LABEL_KEYS = (\[[^;]+\]);/.exec(source)[1].replaceAll("'", '"'));
for (const locale of ['fr', 'en', 'de']) {
  test('native bundle supplies all shared editor labels without caller overrides: ' + locale, async () => {
    const values = Object.fromEntries([...fs.readFileSync(path.join(bundles, 'richEditorBundle_' + locale + '.properties'), 'utf8')
      .matchAll(/^(richEditor\.[^=]+)=(.*)$/gm)].map(([,key,value]) => [key, value.replace(/\\u([0-9a-f]{4})/gi, (_,hex) => String.fromCharCode(parseInt(hex,16)))]));
    for (const key of keys) { assert.ok(values['richEditor.' + key], key); }
    const browser = await chromium.launch({executablePath:process.env.CHROME_PATH, args:['--no-sandbox']});
    try {
      const page = await browser.newPage();
      await page.setContent('<form><textarea name="body" maxlength="30" required></textarea><button type="submit">OK</button></form>');
      await page.evaluate(values => {window.RichEditorBundle = {get:key => values[key]};}, values);
      for (const file of ['vendor/markdown-it-15.0.3.min.js', 'silverpeas-markdown.js', 'silverpeas-rich-content.js',
        'vendor/tiptap-3.32.2.min.js', 'silverpeas-rich-editor.js']) {
        await page.addScriptTag({path:path.join(assets, file)});
      }
      await page.evaluate(() => SilverpeasRichEditor.enhance(document.querySelector('textarea')));
      assert.equal(await page.locator('.sp-format-toggle').getAttribute('title'), values['richEditor.formatLabel']);
      assert.equal(await page.locator('.sp-link-apply').getAttribute('title'), values['richEditor.linkApply']);
      await page.locator('button[type=submit]').click();
      assert.equal(await page.locator('.sp-rich-error').textContent(), values['richEditor.messageRequired']);
      assert.ok((await page.locator('button[title]').evaluateAll(buttons => buttons.map(button => button.title)))
        .every(label => label && !label.includes('undefined') && !label.includes('???')));
    } finally { await browser.close(); }
  });
}
