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

  class ConfigurationConsole {
    constructor(root) {
      this.root = root;
      this.api = root.dataset.api;
      this.messages = new Map(Array.from(root.querySelectorAll('[data-message]'),
          node => [node.dataset.message, node.textContent]));
      this.file = null;
      this.latest = null;
      this.changes = new Map();
      this.rows = new Map();
      this.busy = false;
      this.paths = [];
      this.openFolders = new Map();
      this.activeHelp = null;
      this.bindEvents();
      this.loadFiles();
    }

    element(id) {
      return this.root.querySelector('#configuration-' + id);
    }

    text(key) {
      return this.messages.get(key) || key;
    }

    message(key, error = false) {
      const node = this.element('message');
      node.textContent = key ? this.text(key) : '';
      node.classList.toggle('configuration-error', error);
    }

    bindEvents() {
      this.element('file-filter').addEventListener('input', () => this.renderTree());
      this.element('key-filter').addEventListener('input', () => this.filterRows());
      this.element('modified-only').addEventListener('change', () => this.filterRows());
      this.element('cancel').addEventListener('click', () => this.cancel());
      this.element('reload').addEventListener('click', () => this.loadFile(this.file.path));
      this.element('save').addEventListener('click', () => this.save());
      this.element('compare').addEventListener('click', () => this.compare());
      this.element('rebase').addEventListener('click', () => this.rebase());
      window.addEventListener('scroll', () => this.activeHelp?.position(), true);
      window.addEventListener('resize', () => this.activeHelp?.position());
      window.addEventListener('beforeunload', event => {
        if (this.changes.size) {
          event.preventDefault();
          event.returnValue = '';
        }
      });
    }

    async loadFiles() {
      this.setBusy(true);
      this.message('loading');
      try {
        this.paths = await sp.ajaxRequest(this.api + '/files').sendAndPromiseJsonResponse();
        this.renderTree();
        this.message('');
      } catch (error) {
        this.showError(error, 'loadError');
      } finally {
        this.setBusy(false);
      }
    }

    renderTree() {
      const filter = this.element('file-filter').value.toLocaleLowerCase().trim();
      const tree = {children : new Map()};
      this.paths.filter(path => path.toLocaleLowerCase().includes(filter)).forEach(path => {
        let parent = tree;
        path.split('/').forEach(part => {
          if (!parent.children.has(part)) {
            parent.children.set(part, {children : new Map()});
          }
          parent = parent.children.get(part);
        });
        parent.path = path;
      });
      const container = this.element('tree');
      container.replaceChildren(this.folderContents(tree, '', filter));
      if (!tree.children.size) {
        container.textContent = this.text('noFiles');
      }
      this.updateControls();
    }

    folderContents(folder, prefix, filter) {
      const list = document.createElement('ul');
      Array.from(folder.children).sort(([a], [b]) => a.localeCompare(b)).forEach(([name, node]) => {
        const item = document.createElement('li');
        if (node.path) {
          const button = document.createElement('button');
          button.type = 'button';
          button.textContent = name;
          button.title = node.path;
          button.dataset.path = node.path;
          button.className = 'configuration-file';
          button.setAttribute('aria-current', String(node.path === this.file?.path));
          button.addEventListener('click', () => this.loadFile(node.path));
          item.append(button);
        } else {
          const path = prefix + name + '/';
          const details = document.createElement('details');
          const summary = document.createElement('summary');
          summary.textContent = name;
          details.open = Boolean(filter) || (this.openFolders.get(path) ?? path.split('/').length < 4);
          details.addEventListener('toggle', () => {
            if (!filter) {
              this.openFolders.set(path, details.open);
            }
          });
          details.append(summary, this.folderContents(node, path, filter));
          item.append(details);
        }
        list.append(item);
      });
      return list;
    }

    async readFile(path) {
      return sp.ajaxRequest(this.api + '/file').withParam('path', path).sendAndPromiseJsonResponse();
    }

    async loadFile(path) {
      if (this.busy || (this.changes.size && !window.confirm(this.text('discard')))) {
        return;
      }
      this.setBusy(true);
      this.message('loading');
      try {
        const file = await this.readFile(path);
        this.changes.clear();
        this.displayFile(file);
        this.message('');
      } catch (error) {
        this.showError(error, 'loadError');
      } finally {
        this.setBusy(false);
      }
    }

    displayFile(file) {
      this.activeHelp?.hide();
      this.file = file;
      this.latest = null;
      this.element('conflict').hidden = true;
      this.element('comparison').hidden = true;
      this.element('comparison').querySelector('tbody').replaceChildren();
      this.element('path').textContent = file.path.split('/').join(' / ');
      const help = this.element('file-help');
      help.hidden = !file.documentation?.file;
      help.querySelector('pre').textContent = file.documentation?.file || '';
      this.renderRows();
      this.root.querySelectorAll('[data-path]').forEach(button => {
        button.setAttribute('aria-current', String(button.dataset.path === file.path));
      });
    }

    isSensitive(key) {
      return /password|passwd|secret|credential|token|private[._-]?key/i.test(key);
    }

    renderRows() {
      const body = this.element('properties').querySelector('tbody');
      body.replaceChildren();
      this.rows.clear();
      Object.entries(this.file.properties).sort(([a], [b]) => a.localeCompare(b)).forEach(([key, value], index) => {
        const row = document.createElement('tr');
        const keyCell = document.createElement('th');
        keyCell.scope = 'row';
        const label = document.createElement('label');
        label.htmlFor = 'configuration-value-' + index;
        label.textContent = key;
        keyCell.append(label);
        const documentation = this.file.documentation?.properties || {};
        const comment = Object.hasOwn(documentation, key) ? documentation[key] : '';
        if (comment) {
          keyCell.append(this.help(comment, key));
        }
        const valueCell = document.createElement('td');
        const sensitive = this.isSensitive(key);
        const input = document.createElement(sensitive ? 'input' : 'textarea');
        input.id = label.htmlFor;
        input.autocomplete = 'off';
        input.spellcheck = false;
        if (sensitive) {
          input.type = 'password';
        } else {
          input.rows = value.includes('\n') ? 3 : 1;
        }
        input.value = this.changes.has(key) ? this.changes.get(key) : value;
        input.addEventListener('input', () => this.changed(key, input.value));
        valueCell.append(input);
        if (sensitive) {
          valueCell.append(this.revealButton(input));
        }
        const actions = document.createElement('td');
        const reset = document.createElement('button');
        reset.type = 'button';
        reset.textContent = this.text('reset');
        reset.addEventListener('click', () => {
          input.value = value;
          this.changed(key, value);
        });
        actions.append(reset);
        row.append(keyCell, valueCell, actions);
        body.append(row);
        this.rows.set(key, {row, input, reset});
      });
      this.updateControls();
    }

    help(comment, key) {
      const container = document.createElement('span');
      container.className = 'configuration-help';
      const button = document.createElement('button');
      button.type = 'button';
      button.textContent = '?';
      button.setAttribute('aria-label', this.text('help') + ' : ' + key);
      button.setAttribute('aria-expanded', 'false');
      const content = document.createElement('pre');
      content.textContent = comment;
      content.id = 'configuration-help-' + this.rows.size;
      content.hidden = true;
      content.setAttribute('role', 'tooltip');
      button.setAttribute('aria-describedby', content.id);
      let pinned = false;
      const help = {
        position() {
          const rect = button.getBoundingClientRect();
          content.style.left = Math.max(16, Math.min(rect.left,
              window.innerWidth - content.offsetWidth - 16)) + 'px';
          const below = rect.bottom + 8;
          content.style.top = (below + content.offsetHeight <= window.innerHeight - 16
              ? below : Math.max(16, rect.top - content.offsetHeight - 8)) + 'px';
        },
        hide : () => {
          pinned = false;
          content.hidden = true;
          button.setAttribute('aria-expanded', 'false');
          if (this.activeHelp === help) {
            this.activeHelp = null;
          }
        }
      };
      const show = () => {
        if (this.activeHelp !== help) {
          this.activeHelp?.hide();
        }
        content.hidden = false;
        button.setAttribute('aria-expanded', 'true');
        this.activeHelp = help;
        help.position();
      };
      button.addEventListener('focus', show);
      container.addEventListener('mouseenter', show);
      container.addEventListener('mouseleave', () => {
        if (!pinned && document.activeElement !== button) {
          help.hide();
        }
      });
      button.addEventListener('blur', () => {
        if (!pinned) {
          help.hide();
        }
      });
      button.addEventListener('click', () => {
        pinned = !pinned;
        if (pinned) {
          show();
        } else {
          help.hide();
        }
      });
      button.addEventListener('keydown', event => {
        if (event.key === 'Escape') {
          help.hide();
        }
      });
      container.append(button, content);
      return container;
    }

    revealButton(input) {
      const button = document.createElement('button');
      button.type = 'button';
      button.className = 'configuration-reveal';
      button.textContent = this.text('reveal');
      button.setAttribute('aria-pressed', 'false');
      button.addEventListener('click', () => {
        const reveal = input.type === 'password';
        input.type = reveal ? 'text' : 'password';
        button.textContent = this.text(reveal ? 'hide' : 'reveal');
        button.setAttribute('aria-pressed', String(reveal));
      });
      return button;
    }

    changed(key, value) {
      if (value === this.file.properties[key]) {
        this.changes.delete(key);
      } else {
        this.changes.set(key, value);
      }
      this.updateControls();
    }

    updateControls() {
      this.element('save').disabled = this.busy || !this.changes.size || !this.element('conflict').hidden;
      this.element('cancel').disabled = this.busy || !this.changes.size;
      this.element('reload').disabled = this.busy || !this.file;
      this.element('key-filter').disabled = !this.file;
      this.element('modified-only').disabled = !this.file;
      this.element('count').textContent = this.text('count').replace('{0}', String(this.changes.size));
      this.root.querySelectorAll('[data-path], #configuration-conflict button').forEach(button => {
        button.disabled = this.busy;
      });
      this.rows.forEach(({row, input, reset}, key) => {
        input.disabled = this.busy;
        reset.disabled = this.busy || !this.changes.has(key);
        row.classList.toggle('configuration-modified', this.changes.has(key));
      });
      this.filterRows();
    }

    filterRows() {
      const filter = this.element('key-filter').value.toLocaleLowerCase().trim();
      const modifiedOnly = this.element('modified-only').checked;
      let visible = 0;
      this.rows.forEach(({row}, key) => {
        row.hidden = !key.toLocaleLowerCase().includes(filter) || (modifiedOnly && !this.changes.has(key));
        if (!row.hidden) {
          visible++;
        }
      });
      const empty = this.element('empty');
      empty.hidden = Boolean(visible);
      empty.textContent = this.text(this.file ? 'noKeys' : 'select');
    }

    setBusy(busy) {
      this.busy = busy;
      this.root.setAttribute('aria-busy', String(busy));
      this.updateControls();
    }

    cancel() {
      this.changes.clear();
      this.displayFile(this.file);
      this.message('');
    }

    async save() {
      if (this.busy || !this.changes.size) {
        return;
      }
      this.setBusy(true);
      try {
        const saved = await sp.ajaxRequest(this.api + '/file').withParam('path', this.file.path)
            .byPutMethod().sendAndPromiseJsonResponse({
              revision : this.file.revision,
              properties : Object.fromEntries(this.changes)
            });
        this.changes.clear();
        this.displayFile(saved);
        this.message('saved');
      } catch (error) {
        if (error.status === 409) {
          this.latest = null;
          this.element('comparison').hidden = true;
          this.element('conflict').hidden = false;
          this.message('conflictExplanation', true);
        } else {
          this.showError(error, 'saveError');
        }
      } finally {
        this.setBusy(false);
      }
    }

    async compare() {
      this.setBusy(true);
      try {
        const current = await this.readFile(this.file.path);
        const body = this.element('comparison').querySelector('tbody');
        body.replaceChildren();
        this.changes.forEach((local, key) => {
          const row = document.createElement('tr');
          const absent = this.text('absent');
          const currentValue = Object.hasOwn(current.properties, key) ? current.properties[key] : absent;
          [key, this.file.properties[key], currentValue, local].forEach((value, index) => {
            const cell = document.createElement(index === 0 ? 'th' : 'td');
            cell.textContent = index > 0 && this.isSensitive(key) ? '••••••' : value;
            row.append(cell);
          });
          body.append(row);
        });
        this.latest = current;
        this.element('comparison').hidden = false;
      } catch (error) {
        this.showError(error, 'loadError');
      } finally {
        this.setBusy(false);
      }
    }

    rebase() {
      if (!this.latest || this.busy) {
        return;
      }
      if (Array.from(this.changes.keys()).some(key => !Object.hasOwn(this.latest.properties, key))) {
        this.message('removed', true);
        return;
      }
      this.changes.forEach((value, key) => {
        if (value === this.latest.properties[key]) {
          this.changes.delete(key);
        }
      });
      this.displayFile(this.latest);
      this.message('rebased');
    }

    showError(error, fallback) {
      const key = new Map([[403, 'forbidden'], [400, 'invalid'], [404, 'notFound']]).get(error.status);
      this.message(key || fallback, true);
    }
  }

  whenSilverpeasReady(function() {
    const root = document.querySelector('#configuration-console');
    if (root) {
      new ConfigurationConsole(root);
    }
  });
})();
