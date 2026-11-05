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
const {test}=require('node:test');
const assert=require('node:assert/strict');
const path=require('node:path');
const {chromium}=require('playwright');
const web=path.resolve(__dirname,'../../main/webapp');

test('catalogue preserves categories and distinguishes add, open existing and disabled application cards',async()=>{
  const browser=await chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
  try {
    const page=await browser.newPage();
    await page.setContent(`<ul class="applicationsTypeList"><li class="applicationsType"><a class="linkSee" href="#"><span>01 Collaboration</span></a><ul>
      <li class="application"><a id="add" href="CreateInstance?ComponentName=app" aria-disabled="false"><h3>Application libre</h3><p class="applicationDescription">Description</p></a></li>
      <li class="application"><a id="existing" class="sp-link" href="/Component/discussion12" aria-disabled="false"><h3>Discussion</h3><p class="applicationDescription">Description</p><span class="application-capacity">Limite atteinte : 1 instance par espace.</span><span class="application-open-existing">Ouvrir l’application existante</span></a></li>
      <li class="application"><a id="full" href="#" aria-disabled="true"><h3>Application pleine</h3><p class="applicationDescription">Description</p><span class="application-capacity">Limite atteinte : 2 instances par espace.</span></a></li>
    </ul></li></ul>`);
    await page.addScriptTag({path:path.join(web,'util/javaScript/jquery/jquery-3.3.1.min.js')});
    await page.addStyleTag({path:path.join(web,'util/styleSheets/silverpeas-main.css')});
    await page.addStyleTag({path:path.join(web,'jobStartPagePeas/jsp/javascript/components-catalogue.css')});
    await page.addScriptTag({path:path.join(web,'jobStartPagePeas/jsp/javascript/components-catalogue.js')});
    await page.evaluate(()=>document.dispatchEvent(new Event('DOMContentLoaded')));
    assert.equal(await page.locator('.linkSee span').textContent(),'Collaboration');
    assert.equal(await page.locator('#add').getAttribute('href'),'CreateInstance?ComponentName=app');
    assert.equal(await page.locator('#existing').getAttribute('href'),'/Component/discussion12');
    await page.locator('#existing').hover();
    assert.equal(await page.locator('.application-open-existing').isVisible(),true);
    assert.equal(await page.locator('#existing .applicationDescription').isVisible(),false);
    assert.equal(await page.locator('#existing h3').isVisible(),true);
    assert.equal(await page.locator('.application-capacity').first().evaluate(n=>getComputedStyle(n).backgroundImage),'none');
    assert.equal(await page.locator('#full').evaluate(node=>!node.dispatchEvent(new MouseEvent('click',{bubbles:true,cancelable:true}))),true);
    assert.equal(await page.locator('#add').evaluate(node=>!node.dispatchEvent(new MouseEvent('click',{bubbles:true,cancelable:true}))),false);
  } finally {await browser.close();}
});
