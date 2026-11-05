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
  const markdown = typeof module !== 'undefined' && module.exports ? require('./silverpeas-markdown.js') : root.SilverpeasMarkdown;
  const api = factory(root, markdown);
  if (typeof module !== 'undefined' && module.exports) { module.exports = api; }
  else { root.SilverpeasRichContent = api; }
})(globalThis, (root, markdown) => {
  'use strict';
  const TOKEN = /@\[([^\]\r\n]{1,300})\]\(([0-9]{1,40})\)|https?:\/\/[^\s<>]+/gi;
  const fold = text => text.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();

  function tokenize(text) {
    const tokens = [];
    let offset = 0;
    for (const match of text.matchAll(TOKEN)) {
      if (match.index > offset) {
        tokens.push({type: 'text', text: text.slice(offset, match.index)});
      }
      if (match[1]) {
        tokens.push({type: 'mention', text: '@' + match[1], id: match[2]});
      } else {
        let urlText = match[0].replace(/[.,!?;:]+$/, '');
        while (urlText.endsWith(')') && urlText.split(')').length > urlText.split('(').length) {
          urlText = urlText.slice(0, -1);
        }
        try {
          const url = new URL(urlText);
          if (url.protocol !== 'http:' && url.protocol !== 'https:') {
            throw new Error('Unsupported URL');
          }
          tokens.push({type: 'link', text: urlText, href: url.href});
          if (urlText.length < match[0].length) {
            tokens.push({type: 'text', text: match[0].slice(urlText.length)});
          }
        } catch (failure) {
          tokens.push({type: 'text', text: match[0]});
        }
      }
      offset = match.index + match[0].length;
    }
    if (offset < text.length) {
      tokens.push({type: 'text', text: text.slice(offset)});
    }
    return tokens;
  }

  function decodeMentions(value) {
    const mentions = [];
    let removed = 0;
    const text = value.replace(/@\[([^\]\r\n]{1,300})\]\(([0-9]{1,40})\)/g, (match, name, id, offset) => {
      const start = offset - removed;
      const label = '@' + name;
      removed += match.length - label.length;
      mentions.push({start, end: start + label.length, name, id});
      return label;
    });
    return {text, mentions};
  }
  function moveMentions(previous, value, mentions) {
    let start = 0;
    while (start < previous.length && start < value.length && previous[start] === value[start]) {
      start++;
    }
    let end = previous.length;
    let nextEnd = value.length;
    while (end > start && nextEnd > start && previous[end - 1] === value[nextEnd - 1]) { end--; nextEnd--; }
    const delta = nextEnd - end;
    return mentions.flatMap(mention => {
      if (mention.end <= start) {
        return [mention];
      }
      if (mention.start >= end) {
        return [{...mention, start: mention.start + delta, end: mention.end + delta}];
      }
      return [];
    });
  }
  function encodeMentions(value, mentions) {
    let result = value;
    [...mentions].sort((a, b) => b.start - a.start).forEach(mention => {
      if (value.slice(mention.start, mention.end) === '@' + mention.name) {
        result = result.slice(0, mention.start) + '@[' + mention.name + '](' + mention.id + ')' + result.slice(mention.end);
      }
    });
    return result;
  }

  function filterPeople(people, query) {
    const search = fold(query);
    return people.filter(person => fold(person.name).includes(search)).slice(0, 8);
  }


  function render(text, options = {}) {
    const content = markdown.render(text) || document.createDocumentFragment();
    content.querySelectorAll('[data-sp-mention]').forEach(mention => {
      if (mention.closest('a')) { mention.replaceWith(document.createTextNode(mention.textContent)); return; }
      const link = document.createElement('a');
      link.href = (options.profileBase || '') + '?userId=' + encodeURIComponent(mention.dataset.spMention);
      link.className = options.mentionClass || 'sp-mention';
      link.textContent = mention.textContent;
      mention.replaceWith(link);
    });
    const decorate = (link, original, automatic) => {
      if (options.decorateLink) { options.decorateLink(link, original, automatic); return; }
      const url = new URL(link.href, document.baseURI);
      if (url.origin === location.origin) { link.classList.add('sp-link'); link.target = '_self'; }
      else { link.target = '_blank'; link.rel = 'noopener noreferrer'; }
    };
    content.querySelectorAll('a:not(.' + (options.mentionClass || 'sp-mention') + ')')
        .forEach(link => decorate(link, link.textContent, false));
    const walker = document.createTreeWalker(content, NodeFilter.SHOW_TEXT);
    const nodes = [];
    while (walker.nextNode()) {
      if (!walker.currentNode.parentElement?.closest('a, pre, code') && /https?:\/\/|@\[/i.test(walker.currentNode.textContent)) {
        nodes.push(walker.currentNode);
      }
    }
    nodes.forEach(node => {
      const replacement = document.createDocumentFragment();
      tokenize(node.textContent).forEach(token => {
        if (token.type === 'text') { replacement.append(document.createTextNode(token.text)); return; }
        const link = document.createElement('a');
        link.textContent = token.text;
        if (token.type === 'mention') {
          link.href = (options.profileBase || '') + '?userId=' + encodeURIComponent(token.id);
          link.className = options.mentionClass || 'sp-mention';
        } else { link.href = token.href; decorate(link, token.text, true); }
        replacement.append(link);
      });
      node.replaceWith(replacement);
    });
    while (content.lastChild?.nodeType === Node.TEXT_NODE && !content.lastChild.textContent.trim()) {
      content.lastChild.remove();
    }
    return content;
  }
  function renderHtml(text, options) {
    const container = document.createElement('div');
    container.append(render(text, options));
    return container.innerHTML;
  }
  return {render, renderHtml, tokenize, filterPeople, decodeMentions, moveMentions, encodeMentions};
});
