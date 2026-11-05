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
(() => {
  'use strict';
  const {Editor, Node, StarterKit, MarkdownSerializer, defaultMarkdownSerializer, Plugin, PluginKey} = SilverpeasTiptap;
  const EMOJI = ['😀','😊','😂','😍','🤔','😮','😢','👍','👎','👏','🙌','❤️','🎉','🚀','✅','🙏','💡','👀'];
  const filter = (people, query) => {
    const fold = value => value.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase();
    return people.filter(person => fold(person.name).includes(fold(query))).slice(0, 8);
  };
  const DEFAULT_LABELS = {formatLabel:'Formatting', emojiLabel:'Emoji', mentionLabel:'Mention',
    lengthError:'Text is too long', messageRequired:'Enter a message', linkText:'Link text', linkUrl:'URL',
    linkApply:'Apply', linkInvalid:'Invalid URL', closeImage:'Close', formatBold:'Bold', formatItalic:'Italic',
    formatStrike:'Strikethrough', formatBullets:'Bullet list', formatNumbers:'Numbered list',
    formatQuote:'Quotation', formatCode:'Code', formatLink:'Link', removeQuote:'Remove quotation'};
  const editors = new Map();
  let editorNumber = 0;
  const nodes = defaultMarkdownSerializer.nodes;
  const marks = defaultMarkdownSerializer.marks;
  const serializer = new MarkdownSerializer({
    text:nodes.text, paragraph:nodes.paragraph, blockquote:nodes.blockquote,
    codeBlock:(state, node) => nodes.code_block(state, {textContent:node.textContent, attrs:{params:node.attrs.language || ''}}),
    bulletList:nodes.bullet_list,
    orderedList:(state, node) => state.renderList(node, ' '.repeat(String(node.attrs.start + node.childCount - 1).length + 2), index => (node.attrs.start + index) + '. '),
    listItem:nodes.list_item, hardBreak:state => state.write('\n'),
    spMention:(state, node) => state.write('@[' + node.attrs.label + '](' + node.attrs.id + ')')
  }, {
    bold:marks.strong, italic:marks.em, code:marks.code, link:marks.link,
    strike:{open:'~~', close:'~~', mixable:true, expelEnclosingWhitespace:true}
  });
  const mention = Node.create({
    addOptions:() => ({className:'sp-mention'}),
    name:'spMention', group:'inline', inline:true, atom:true, selectable:false,
    addAttributes() {
      return {
        id:{default:'', parseHTML:element => element.dataset.spMention},
        label:{default:'', parseHTML:element => element.textContent.replace(/^@/, '')}
      };
    },
    parseHTML:() => [{tag:'span[data-sp-mention]', getAttrs:element => /^[0-9]{1,40}$/.test(element.dataset.spMention) ? null : false}],
    renderHTML({node}) { return ['span', {'data-sp-mention':node.attrs.id, class:this.options.className, contenteditable:'false'}, '@' + node.attrs.label]; },
    renderText:({node}) => '@' + node.attrs.label
  });
  function enhance(textarea, configuration = {}) {
    const {people = [], filterPeople = filter, emoji = EMOJI} = configuration;
    const tool = configuration.tool || ((label, icon) => button(label, icon === 'emoji' ? '☺' : '@', '', () => {}));
    const classes = names => names + (configuration.classPrefix
        ? ' ' + names.split(' ').filter(name => name.startsWith('sp-')).map(name => configuration.classPrefix + name.slice(2)).join(' ') : '');
    function button(label, text, className, action) {
      const node = document.createElement('button');
      node.type = 'button';
      node.className = classes('sp-icon-button ' + className);
      node.title = label;
      node.setAttribute('aria-label', label);
      node.textContent = text;
      node.addEventListener('mousedown', event => event.preventDefault());
      node.addEventListener('click', action);
      return node;
    }
    const isQuotation = () => configuration.quotation?.isQuotation(editor.state.doc.firstChild) || false;
    if (editors.has(textarea)) { return; }
    const labels = {...DEFAULT_LABELS, ...configuration.labels};
    const original = {name:textarea.name, hidden:textarea.hidden, disabled:textarea.disabled, required:textarea.required};
    const abort = new AbortController();
    const maximum = textarea.maxLength > 0 ? textarea.maxLength : (configuration.maxLength || 8000);
    textarea.parentElement.classList.add('sp-rich-host');
    const holder = document.createElement('div');
    holder.className = classes('sp-rich-editor');
    if (configuration.integrated) { holder.classList.add('sp-rich-editor--integrated'); }
    const tools = document.createElement('div');
    tools.className = classes('sp-editor-tools');
    const error = document.createElement('p');
    error.className = classes('sp-rich-error');
    error.id = 'sp-editor-error-' + (++editorNumber);
    error.setAttribute('role', 'status');
    error.hidden = true;
    const body = document.createElement('input');
    body.type = 'hidden';
    body.name = configuration.fieldName || textarea.name || 'body';
    const initial = textarea.value;
    textarea.name = body.name + 'Display';
    textarea.hidden = true;
    textarea.disabled = true;
    textarea.required = false;
    textarea.after(holder, tools, error, body);
    const source = doc => serializer.serialize(doc, {tightLists:true});
    let syncing = false;
    let editor;
    let disposed = false;
    let refresh = () => {};
    const synchronize = () => {
      const value = source(editor.state.doc);
      body.value = value;
      textarea.value = value;
      syncing = true;
      textarea.dispatchEvent(new Event('input', {bubbles:true}));
      syncing = false;
      error.hidden = true;
      refresh();
    };
    const limit = new Plugin({
      key:new PluginKey('silverpeasRichTextLength'),
      filterTransaction(transaction, state) {
        if (!transaction.docChanged) { return true; }
        const length = source(transaction.doc).length;
        if (length <= maximum || length <= source(state.doc).length) { return true; }
        error.textContent = labels.lengthError;
        error.hidden = false;
        return false;
      }
    });
    editor = new Editor({
      element:holder,
      extensions:[StarterKit.configure({heading:false, horizontalRule:false, underline:false, trailingNode:false,
        link:{openOnClick:false, autolink:false, linkOnPaste:false, protocols:['http','https'],
          isAllowedUri:url => /^(https?:\/\/|\/(?!\/))/i.test(url)}}), mention.configure({className:classes('sp-mention')})],
      content:SilverpeasMarkdown.renderHtml(initial),
      editorProps:{attributes:{role:'textbox', 'aria-multiline':'true', 'aria-label':textarea.placeholder || labels.formatLabel,
        'aria-describedby':error.id, 'data-placeholder':textarea.placeholder || ''}},
      onUpdate:synchronize,
      onSelectionUpdate:() => refresh()
    });
    editor.registerPlugin(limit);
    editors.set(textarea, {editor, body, isQuotation, destroy:() => {
      disposed = true; abort.abort(); editor.destroy(); [holder, tools, error, body, format, linkForm].forEach(node => node.remove());
      Object.assign(textarea, original); delete textarea.dataset.enhanced;
    }});
    textarea.dataset.enhanced = 'true';
    const visual = editor.view.dom;
    const removeQuote = button(labels.removeQuote, '×', 'sp-remove-quote', () => {
      if (isQuotation()) {
        editor.chain().focus().deleteRange({from:0, to:editor.state.doc.firstChild.nodeSize}).run();
      }
    });
    holder.append(removeQuote);
    if (!configuration.quotation) { removeQuote.hidden = true; }
    const reset = value => {
      editor.commands.setContent(SilverpeasMarkdown.renderHtml(value), {emitUpdate:false});
      synchronize();
    };
    textarea.addEventListener('input', () => { if (!syncing) { reset(textarea.value); } }, {signal:abort.signal});
    textarea.form?.addEventListener('submit', event => {
      const attachments = [...textarea.form.querySelectorAll('input[type=file]')].some(input => input.files.length);
      if (body.value.length > maximum || (!editor.getText().trim() && !attachments && textarea.dataset.allowEmpty !== 'true')) {
        event.preventDefault();
        event.stopImmediatePropagation();
        error.textContent = body.value.length > maximum ? labels.lengthError : labels.messageRequired;
        error.hidden = false;
        editor.commands.focus();
      }
    }, {capture:true, signal:abort.signal});
    visual.addEventListener('paste', event => {
      const forwarded = new ClipboardEvent('paste', {clipboardData:event.clipboardData, cancelable:true});
      textarea.dispatchEvent(forwarded);
      if (forwarded.defaultPrevented) { event.preventDefault(); }
    }, true);
    const format = document.createElement('div');
    format.className = classes('sp-format-tools');
    format.hidden = true;
    format.setAttribute('role', 'toolbar');
    format.setAttribute('aria-label', labels.formatLabel);
    const toggle = button(labels.formatLabel, 'Aa', 'sp-format-toggle', () => {
      format.hidden = !format.hidden;
      toggle.setAttribute('aria-expanded', String(!format.hidden));
    });
    toggle.setAttribute('aria-expanded', 'false');
    const commands = [
      ['bold','B','bold',chain => chain.toggleBold()], ['italic','I','italic',chain => chain.toggleItalic()],
      ['strike','S','strike',chain => chain.toggleStrike()], ['bullets','•','bulletList',chain => chain.toggleBulletList()],
      ['numbers','1.','orderedList',chain => chain.toggleOrderedList()], ['quote','❝','blockquote',chain => chain.toggleBlockquote()],
      ['code','</>','codeBlock',chain => chain.toggleCodeBlock()]
    ];
    const states = commands.map(([name, text, mark, command]) => {
      const control = button(labels['format' + name[0].toUpperCase() + name.slice(1)], text, 'sp-format-' + name,
          () => command(editor.chain().focus()).run());
      format.append(control);
      return {control, mark};
    });
    const linkForm = document.createElement('div');
    linkForm.className = classes('sp-link-editor');
    linkForm.hidden = true;
    const linkLabel = document.createElement('input');
    const linkUrl = document.createElement('input');
    linkLabel.type = 'text';
    linkUrl.type = 'url';
    linkLabel.setAttribute('aria-label', labels.linkText);
    linkUrl.setAttribute('aria-label', labels.linkUrl);
    linkLabel.placeholder = labels.linkText;
    linkUrl.placeholder = 'https://';
    const linkError = document.createElement('span');
    linkError.setAttribute('role', 'status');
    const closeLink = () => { linkForm.hidden = true; editor.view.focus(); };
    const apply = button(labels.linkApply, '✓', 'sp-link-apply', () => {
      const url = linkUrl.value.trim();
      if (!/^(https?:\/\/|\/(?!\/))/i.test(url)) { linkError.textContent = labels.linkInvalid; return; }
      const label = linkLabel.value.trim() || url;
      editor.chain().focus().extendMarkRange('link').insertContent({type:'text', text:label, marks:[{type:'link', attrs:{href:url}}]}).run();
      closeLink();
    });
    const cancelLink = button(labels.closeImage, '×', 'sp-link-cancel', closeLink);
    linkForm.append(linkLabel, linkUrl, apply, cancelLink, linkError);
    const link = button(labels.formatLink, '↗', 'sp-format-link', () => {
      if (editor.isActive('link')) { editor.commands.extendMarkRange('link'); }
      const {from, to} = editor.state.selection;
      linkLabel.value = editor.state.doc.textBetween(from, to, ' ');
      linkUrl.value = editor.getAttributes('link').href || '';
      linkError.textContent = '';
      linkForm.hidden = false;
      linkLabel.focus();
    });
    format.append(link);
    const picker = document.createElement('div');
    picker.className = classes('sp-emoji-picker');
    picker.hidden = true;
    const emojiButton = tool(labels.emojiLabel, 'emoji');
    emojiButton.setAttribute('aria-expanded', 'false');
    emojiButton.addEventListener('mousedown', event => event.preventDefault());
    emojiButton.addEventListener('click', () => { picker.hidden = !picker.hidden; emojiButton.setAttribute('aria-expanded', String(!picker.hidden)); });
    emoji.forEach(value => picker.append(button(value, value, '', () => {
      editor.chain().focus().insertContent({type:'text', text:value}).run();
      picker.hidden = true;
      emojiButton.setAttribute('aria-expanded', 'false');
    })));
    const mentionButton = tool(labels.mentionLabel, 'mention');
    mentionButton.addEventListener('mousedown', event => event.preventDefault());
    mentionButton.addEventListener('click', () => editor.chain().focus().insertContent({type:'text', text:'@'}).run());
    tools.append(emojiButton, mentionButton, toggle, picker);
    tools.after(format, linkForm);
    const list = document.createElement('div');
    list.className = classes('sp-mention-list');
    list.id = error.id + '-mentions';
    list.setAttribute('role', 'listbox');
    list.setAttribute('aria-label', labels.mentionLabel);
    list.hidden = true;
    tools.append(list);
    visual.setAttribute('aria-controls', list.id);
    visual.setAttribute('aria-autocomplete', 'list');
    let active = 0;
    let options = [];
    let query;
    const closeMentions = () => {
      mentionGeneration++;
      list.hidden = true;
      visual.setAttribute('aria-expanded', 'false');
      visual.removeAttribute('aria-activedescendant');
    };
    const highlight = () => {
      options.forEach((control, index) => control.setAttribute('aria-selected', String(index === active)));
      if (options[active]) { visual.setAttribute('aria-activedescendant', options[active].id); }
    };
    let mentionGeneration = 0;
    refresh = async () => {
      removeQuote.hidden = !isQuotation();
      states.forEach(({control, mark}) => control.setAttribute('aria-pressed', String(editor.isActive(mark))));
      link.setAttribute('aria-pressed', String(editor.isActive('link')));
      const {$from, empty} = editor.state.selection;
      const text = $from.parent.textBetween(0, $from.parentOffset, '', '\ufffc');
      query = empty && !editor.isActive('codeBlock') && !editor.isActive('code') && /(?:^|\s)@([^\s@\[\]]*)$/u.exec(text);
      list.replaceChildren();
      options = [];
      if (!query) { closeMentions(); return; }
      const from = $from.pos - query[1].length - 1;
      const token = ++mentionGeneration;
      let candidates;
      try { candidates = typeof people === 'function' ? await people(query[1]) : people; }
      catch (unavailable) { if (token === mentionGeneration) { closeMentions(); } return; }
      if (disposed || token !== mentionGeneration || !textarea.isConnected) { return; }
      filterPeople(candidates, query[1]).forEach(person => {
        const label = person.name.replace(/[\[\]\r\n]/g, ' ');
        const control = button('@' + label, '@' + label, 'sp-mention-option', () => {
          editor.chain().focus().insertContentAt({from, to:from + query[1].length + 1}, [
            {type:'spMention', attrs:{id:person.id, label}}, {type:'text', text:' '}
          ]).run();
          closeMentions();
        });
        control.id = list.id + '-' + options.length;
        control.setAttribute('role', 'option');
        options.push(control);
        list.append(control);
      });
      active = 0;
      list.hidden = !options.length;
      visual.setAttribute('aria-expanded', String(!list.hidden));
      highlight();
    };
    visual.addEventListener('keydown', event => {
      if (event.isComposing) { return; }
      if (!list.hidden && ['ArrowUp','ArrowDown','Enter'].includes(event.key)) {
        event.preventDefault();
        event.stopImmediatePropagation();
        if (event.key === 'Enter') { options[active].click(); }
        else { active = (active + (event.key === 'ArrowDown' ? 1 : -1) + options.length) % options.length; highlight(); }
      } else if (event.key === 'Escape' && (!list.hidden || !picker.hidden || !linkForm.hidden)) {
        event.preventDefault();
        event.stopPropagation();
        closeMentions();
        picker.hidden = true;
        emojiButton.setAttribute('aria-expanded', 'false');
        closeLink();
      } else if (event.key === 'Escape' && configuration.cancelOnEscape?.(textarea)) {
        event.preventDefault();
        event.stopImmediatePropagation();
        visual.dispatchEvent(new CustomEvent(configuration.cancelEvent || 'sp:cancel-edit', {bubbles:true}));
      }
    }, true);
    visual.addEventListener('blur', closeMentions);
    for (const panel of [format, linkForm]) {
      panel.addEventListener('keydown', event => {
        if (event.key === 'Escape') {
          event.preventDefault();
          event.stopPropagation();
          format.hidden = true;
          toggle.setAttribute('aria-expanded', 'false');
          closeLink();
        } else if (event.key === 'Enter' && panel === linkForm) { event.preventDefault(); apply.click(); }
      });
    }
    synchronize();
  }
  const observe = () => {
    const observer = new MutationObserver(() => {
      editors.forEach((state, textarea) => {
        if (!textarea.isConnected) { state.destroy(); editors.delete(textarea); }
      });
    });
    observer.observe(document.body, {childList:true, subtree:true});
  };
  if (document.readyState === 'loading') { document.addEventListener('DOMContentLoaded', observe, {once:true}); }
  else { observe(); }
  window.SilverpeasRichEditor = {
    enhance,
    getContent:textarea => editors.get(textarea)?.body.value ?? textarea.value,
    destroy:textarea => { const state = editors.get(textarea); if (state) { state.destroy(); editors.delete(textarea); } },
    quote:(textarea, quotation) => {
      const editor = editors.get(textarea)?.editor;
      if (!editor) { return false; }
      const node = {type:'blockquote', content:[
        {type:'paragraph', content:[{type:'text', text:quotation.author, marks:[{type:'link', attrs:{href:quotation.url}}]}]},
        {type:'paragraph', content:[{type:'text', text:quotation.excerpt}]}
      ]};
      const before = editor.state.doc;
      editor.commands.insertContentAt({from:0, to:editors.get(textarea).isQuotation() ? before.firstChild.nodeSize : 0}, node);
      if (editor.state.doc === before) { return false; }
      editor.commands.focus('end');
      return true;
    },
    focus:textarea => editors.get(textarea)?.editor.commands.focus(),
    reset:(textarea, value) => {
      const state = editors.get(textarea);
      if (state) { textarea.value = value; textarea.dispatchEvent(new Event('input', {bubbles:true})); }
    }
  };
})();
