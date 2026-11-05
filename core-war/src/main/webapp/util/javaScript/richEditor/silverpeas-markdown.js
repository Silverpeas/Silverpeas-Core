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
(function(root, factory) {
  const parser = typeof module !== 'undefined' && module.exports
      ? require('./vendor/markdown-it-15.0.3.min.js') : root.markdownit;
  const api = factory(root, parser);
  if (typeof module !== 'undefined' && module.exports) { module.exports = api; }
  else { root.SilverpeasMarkdown = api; }
})(globalThis, (root, factory) => {
  'use strict';
  const md = factory?.('zero', {html:false, breaks:true, linkify:false, typographer:false, maxNesting:20});
  const smileys = new Map([
    [':)', '🙂'], [':-)', '🙂'], [';)', '😉'], [';-)', '😉'],
    [':D', '😄'], [':-D', '😄'], [':(', '🙁'], [':-(', '🙁'],
    [':p', '😛'], [':P', '😛'], [':-p', '😛'], [':-P', '😛'],
    [':o', '😮'], [':O', '😮'], [':-o', '😮'], [':-O', '😮'],
    [':/', '😕'], [':-/', '😕'], [":'(", '😢'], ['<3', '❤️']
  ]);
  const smileyPattern = /https?:\/\/[^\s<>]+|(^|[\s([{])(:-?[)(DPpOo/]|;-?\)|:'\(|<3)(?=$|[\s.,!?;:)\]}])/g;
  if (md) {
    md.core.ruler.after('inline', 'sp_smileys', state => {
      state.tokens.filter(token => token.type === 'inline').forEach(inline => {
        let links = 0;
        inline.children.forEach(token => {
          if (token.type === 'link_open') { links++; }
          else if (token.type === 'link_close') { links--; }
          else if (token.type === 'text' && links === 0) {
            token.content = token.content.replace(smileyPattern, (match, prefix, face) =>
                face ? prefix + smileys.get(face) : match);
          }
        });
      });
    });
    md.enable(['paragraph','text','newline','escape','emphasis','strikethrough','backticks','fence','code','blockquote','list','link','autolink']);
    md.validateLink = value => {
      if (!/^(https?:\/\/|\/)/i.test(value)) { return false; }
      try { return ['http:','https:'].includes(new URL(value, 'https://silverpeas.invalid/').protocol); }
      catch (failure) { return false; }
    };
    md.inline.ruler.before('text', 'sp_mention', (state, silent) => {
      const full = /^@\[([^\]\r\n]{1,300})\]\(([0-9]{1,40})\)/.exec(state.src.slice(state.pos));
      const pendingAt = !full && state.pending.endsWith('@');
      const match = full || (pendingAt && /^\[([^\]\r\n]{1,300})\]\(([0-9]{1,40})\)/.exec(state.src.slice(state.pos)));
      if (!match) { return false; }
      if (!silent) {
        if (pendingAt) { state.pending = state.pending.slice(0, -1); }
        const token = state.push('sp_mention', 'span', 0);
        token.content = match[1];
        token.meta = {id:match[2]};
      }
      state.pos += match[0].length;
      return true;
    });
    md.renderer.rules.sp_mention = (tokens, index) => {
      const token = tokens[index];
      return '<span data-sp-mention="' + token.meta.id + '">' + md.utils.escapeHtml('@' + token.content) + '</span>';
    };
  }
  function renderHtml(text) { return md ? md.render(text) : null; }
  function render(text) {
    if (!md) { return null; }
    const parsed = new DOMParser().parseFromString(renderHtml(text), 'text/html');
    const fragment = document.createDocumentFragment();
    fragment.append(...parsed.body.childNodes);
    return fragment;
  }

  function enhanceEditor(textarea, tools, insert, labels = {}) {
    if (!md || textarea.dataset.markdownTools) { return; }
    textarea.dataset.markdownTools = 'true';
    const toggle = document.createElement('button');
    toggle.type = 'button';
    toggle.className = 'sp-icon-button sp-format-toggle';
    toggle.textContent = 'Aa';
    toggle.title = labels.formatLabel;
    toggle.setAttribute('aria-label', labels.formatLabel);
    toggle.setAttribute('aria-expanded', 'false');
    const bar = document.createElement('div');
    bar.className = 'sp-format-tools';
    bar.hidden = true;
    bar.setAttribute('role', 'toolbar');
    bar.setAttribute('aria-label', labels.formatLabel);
    let selection = [textarea.selectionStart, textarea.selectionEnd];
    const remember = () => { selection = [textarea.selectionStart, textarea.selectionEnd]; };
    for (const event of ['select','keyup','click','input']) { textarea.addEventListener(event, remember); }
    const wrap = (before, after) => {
      const [start, end] = selection;
      if (textarea.value.slice(start - before.length, start) === before && textarea.value.slice(end, end + after.length) === after) {
        insert(textarea, '', end, end + after.length);
        insert(textarea, '', start - before.length, start);
        textarea.setSelectionRange(start - before.length, end - before.length);
        remember();
        return;
      }
      if (textarea.value.length + before.length + after.length > textarea.maxLength) { return; }
      // Two separate insertions preserve tracked @mentions inside the selection.
      insert(textarea, after, end, end);
      insert(textarea, before, start, start);
      textarea.setSelectionRange(start + before.length, end + before.length);
      remember();
    };
    const prefix = value => {
      const start = selection[0] === 0 ? 0 : textarea.value.lastIndexOf('\n', selection[0] - 1) + 1;
      const end = selection[1];
      const lines = textarea.value.slice(start, end).split('\n');
      if (textarea.value.length + lines.length * value.length > textarea.maxLength) { return; }
      const positions = [start];
      for (let pos = start; pos < end; pos++) { if (textarea.value[pos] === '\n' && pos + 1 < end) { positions.push(pos + 1); } }
      positions.reverse().forEach(pos => insert(textarea, value, pos, pos));
      textarea.setSelectionRange(start, end + positions.length * value.length);
      remember();
    };
    const actions = [
      ['bold','B',() => wrap('**','**')], ['italic','I',() => wrap('*','*')], ['strike','S',() => wrap('~~','~~')],
      ['bullets','•',() => prefix('- ')], ['numbers','1.',() => prefix('1. ')], ['quote','❝',() => prefix('> ')],
      ['code','</>',() => wrap('```\n','\n```')], ['link','↗',() => wrap('[','](https://)')]
    ];
    actions.forEach(([name, text, action]) => {
      const button = document.createElement('button');
      button.type = 'button';
      button.className = 'sp-icon-button sp-format-' + name;
      button.textContent = text;
      button.title = labels['format' + name[0].toUpperCase() + name.slice(1)];
      button.setAttribute('aria-label', button.title);
      button.addEventListener('click', action);
      bar.append(button);
    });
    toggle.addEventListener('click', () => {
      bar.hidden = !bar.hidden;
      toggle.setAttribute('aria-expanded', String(!bar.hidden));
    });
    bar.addEventListener('keydown', event => {
      if (event.key === 'Escape') { event.preventDefault(); event.stopPropagation(); bar.hidden = true; toggle.setAttribute('aria-expanded','false'); toggle.focus(); }
    });
    textarea.addEventListener('keydown', event => {
      if ((event.ctrlKey || event.metaKey) && !event.altKey && !event.isComposing && ['b','i'].includes(event.key.toLowerCase())) {
        event.preventDefault();
        remember();
        const marker = event.key.toLowerCase() === 'b' ? '**' : '*';
        wrap(marker, marker);
      }
    });
    tools.append(toggle);
    tools.after(bar);
  }
  return {renderHtml, render, enhanceEditor,
    fromPlainText:text => text.replace(/([\\`*_{}\[\]()#+.!>~|-])/g, '\\$1')};
});
