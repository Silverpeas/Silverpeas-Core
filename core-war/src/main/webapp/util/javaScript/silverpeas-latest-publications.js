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
(function() {
  'use strict';
  const selector = '.silverpeas-latest-publications';
  const documentKey = Symbol.for('silverpeas.latestPublications.observed');
  const frames = new WeakSet();
  const blocks = new WeakSet();
  const script = document.currentScript;
  // Layout fragments are evaluated by jQuery, which does not retain the script URL.
  const endpoint = typeof webContext === 'string'
      ? new URL(webContext.replace(/\/$/, '') + '/LatestPublications', document.baseURI)
      : script && script.src
          ? new URL('../../LatestPublications', script.src)
          : new URL('/silverpeas/LatestPublications', document.baseURI);

  function load(block) {
    if (blocks.has(block) || block.closest('[contenteditable="true"], .cke, .cke_widget_wrapper')) {
      return;
    }
    blocks.add(block);
    const spaceId = block.dataset.spaceId;
    const limit = block.dataset.limit;
    const includeSubspaces = block.dataset.includeSubspaces;
    if (!/^[1-9][0-9]*$/.test(spaceId || '') || !/^[1-9][0-9]*$/.test(limit || '') ||
        !['true', 'false'].includes(includeSubspaces)) {
      block.dataset.publicationsState = 'error';
      return;
    }
    const url = new URL(endpoint);
    url.search = new URLSearchParams({spaceId, limit, includeSubspaces});
    block.dataset.publicationsState = 'loading';
    block.setAttribute('aria-busy', 'true');
    fetch(url, {credentials: 'same-origin', headers: {'Accept': 'text/html'}})
        .then(function(response) {
          if (!response.ok || !response.headers.get('Content-Type')?.includes('text/html') || response.redirected) {
            throw new Error('Unable to load latest publications.');
          }
          return response.text();
        }).then(function(html) {
          block.innerHTML = html;
          block.dataset.publicationsState = 'loaded';
        }).catch(function(error) {
          block.dataset.publicationsState = 'error';
          console.warn('Latest publications could not be loaded.', error);
        }).finally(function() { block.removeAttribute('aria-busy'); });
  }

  function inspectFrame(frame) {
    // Editing frames must never receive live content which could then be saved.
    if (frame.closest('.cke') || frame.classList.contains('cke_wysiwyg_frame')) {
      return;
    }
    function inspect() {
      try {
        if (frame.contentDocument && frame.contentDocument.designMode !== 'on') {
          observe(frame.contentDocument);
        }
      } catch (ignored) {
        // Cross-origin frames are outside this renderer's scope.
      }
    }
    if (!frames.has(frame)) {
      frames.add(frame);
      frame.addEventListener('load', inspect);
    }
    inspect();
  }

  function scan(root) {
    if (root.nodeType === 1 && root.matches(selector)) load(root);
    root.querySelectorAll(selector).forEach(load);
    if (root.nodeType === 1 && root.matches('iframe')) inspectFrame(root);
    root.querySelectorAll('iframe').forEach(inspectFrame);
  }

  function observe(doc) {
    if (doc[documentKey] || !doc.documentElement || doc.designMode === 'on' ||
        doc.body?.isContentEditable) return;
    doc[documentKey] = true;
    doc.addEventListener('click', function(event) {
      const target = event.target;
      if (event.defaultPrevented || event.button !== 0 || event.ctrlKey || event.metaKey ||
          event.shiftKey || event.altKey || !target.closest || target.closest('a, button, input')) return;
      const row = target.closest('.latest-publications li');
      const link = row && row.querySelector('a.publication-name');
      if (link) link.click();
    });
    scan(doc);
    new MutationObserver(function(records) {
      records.forEach(function(record) {
        record.addedNodes.forEach(function(node) {
          if (node.nodeType === 1) scan(node);
        });
      });
    }).observe(doc.documentElement, {childList: true, subtree: true});
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function() { observe(document); });
  } else {
    observe(document);
  }
})();
