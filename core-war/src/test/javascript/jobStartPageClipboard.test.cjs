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

/*
 * Tests of the copy and the cut of spaces and of applications into the clipboard from the
 * administration pages of a space or of an application.
 * @author mmoquillon
 */
const { test } = require('node:test');
const assert = require('node:assert/strict');
const { readFileSync } = require('node:fs');
const { runInNewContext } = require('node:vm');
const { resolve } = require('node:path');

const webapp = resolve(__dirname, '../../main/webapp');
const windowScript = readFileSync(resolve(webapp, 'util/javaScript/silverpeas-window.js'), 'utf8');
// only the function computing the Silverpeas main window, without the spWindow plugin
const mainWindowScript = windowScript.substring(0,
    windowScript.indexOf('function _spWindow_isSilverpeasMainWindow'));
const clipboardScript = readFileSync(
    resolve(webapp, 'jobStartPagePeas/jsp/javascript/clipboard.js'), 'utf8');

/**
 * The Silverpeas main window, with the hidden frame refreshing the clipboard.
 */
function newMainWindow() {
  const mainWindow = { IdleFrame: { location: { href: 'Idle.jsp' } } };
  mainWindow.top = mainWindow;
  mainWindow.window = mainWindow;
  return mainWindow;
}

/**
 * Runs the clipboard script in an administration page loaded within a frame of the main window.
 */
function inAFrameOf(mainWindow) {
  const frame = { top: mainWindow, opener: null };
  frame.window = frame;
  return run(frame);
}

/**
 * Runs the clipboard script in an administration page opened in a popup by a page of the main
 * window, as from the result of a search in the administration.
 */
function inAPopupOpenedFrom(mainWindow) {
  const popup = { opener: { top: mainWindow } };
  popup.top = popup;
  popup.window = popup;
  return run(popup);
}

function run(currentWindow) {
  const context = { window: currentWindow, top: currentWindow.top };
  runInNewContext(mainWindowScript + clipboardScript, context);
  return context;
}

test('copying an application from a frame of the main window puts it into the clipboard', () => {
  const mainWindow = newMainWindow();
  const page = inAFrameOf(mainWindow);

  page.clipboardCopy('Component', 'kmelia1');

  assert.equal(mainWindow.IdleFrame.location.href, 'copy?Type=Component&Id=kmelia1');
});

test('copying an application from a popup puts it into the clipboard of the main window', () => {
  const mainWindow = newMainWindow();
  const page = inAPopupOpenedFrom(mainWindow);

  page.clipboardCopy('Component', 'kmelia1');

  assert.equal(mainWindow.IdleFrame.location.href, 'copy?Type=Component&Id=kmelia1');
});

test('cutting an application from a popup puts it into the clipboard of the main window', () => {
  const mainWindow = newMainWindow();
  const page = inAPopupOpenedFrom(mainWindow);

  page.clipboardCut('Component', 'kmelia1');

  assert.equal(mainWindow.IdleFrame.location.href, 'Cut?Type=Component&Id=kmelia1');
});

test('copying a space from a popup puts it into the clipboard of the main window', () => {
  const mainWindow = newMainWindow();
  const page = inAPopupOpenedFrom(mainWindow);

  page.clipboardCopy('Space', 'WA3');

  assert.equal(mainWindow.IdleFrame.location.href, 'copy?Type=Space&Id=WA3');
});

test('cutting a space from a frame of the main window puts it into the clipboard', () => {
  const mainWindow = newMainWindow();
  const page = inAFrameOf(mainWindow);

  page.clipboardCut('Space', 'WA3');

  assert.equal(mainWindow.IdleFrame.location.href, 'Cut?Type=Space&Id=WA3');
});
