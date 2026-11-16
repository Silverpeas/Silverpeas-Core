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
      this.selectedKeys = new Set();
      this.targets = [];
      this.persistenceBusy = false;
      this.bindEvents();
      this.loadFiles();
      this.loadPersistenceTargets();
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
      this.element('persist').addEventListener('click', () => this.persist());
      this.element('persistence-refresh').addEventListener('click', () => this.loadPersistenceTargets());
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
      if (this.busy || this.persistenceBusy || (this.changes.size && !window.confirm(this.text('discard')))) {
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

    displayFile(file, selectedKeys = []) {
      this.activeHelp?.hide();
      this.file = file;
      this.selectedKeys = new Set(Array.from(selectedKeys).filter(key => Object.hasOwn(file.properties, key)));
      if (this.targets.length) {
        this.persistenceMessage('');
      }
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
        const selection = document.createElement('input');
        selection.type = 'checkbox';
        selection.className = 'configuration-persistence-check';
        selection.setAttribute('aria-label', this.text('persist') + ' : ' + key);
        selection.checked = this.selectedKeys.has(key);
        selection.addEventListener('change', () => {
          if (selection.checked) {
            this.selectedKeys.add(key);
          } else {
            this.selectedKeys.delete(key);
          }
          this.updateControls();
        });
        keyCell.append(selection, label);
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
        if (sensitive) {
          const control = document.createElement('div');
          control.className = 'configuration-value-control';
          control.append(input, this.revealButton(input));
          valueCell.append(control);
        } else {
          valueCell.append(input);
        }
        const actions = document.createElement('td');
        const reset = document.createElement('button');
        reset.type = 'button';
        this.setIconButton(reset, 'reset', 'undo');
        reset.addEventListener('click', () => {
          input.value = value;
          this.changed(key, value);
        });
        actions.append(reset);
        row.append(keyCell, valueCell, actions);
        body.append(row);
        this.rows.set(key, {row, input, reset, selection});
      });
      this.updateControls();
    }

    help(comment, key) {
      const container = document.createElement('span');
      container.className = 'configuration-help';
      const button = document.createElement('button');
      button.type = 'button';
      this.setIconButton(button, 'help', 'help');
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

    setIconButton(button, label, icon) {
      button.classList.add('sp_button', 'configuration-command', 'configuration-icon-button');
      button.dataset.icon = icon;
      button.title = this.text(label);
      button.setAttribute('aria-label', this.text(label));
      button.replaceChildren();
    }

    revealButton(input) {
      const button = document.createElement('button');
      button.type = 'button';
      button.className = 'configuration-reveal';
      this.setIconButton(button, 'reveal', 'show');
      button.setAttribute('aria-pressed', 'false');
      button.addEventListener('click', () => {
        const reveal = input.type === 'password';
        input.type = reveal ? 'text' : 'password';
        this.setIconButton(button, reveal ? 'hide' : 'reveal', reveal ? 'hide' : 'show');
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
      const busy = this.busy || this.persistenceBusy;
      this.element('save').disabled = busy || !this.changes.size || !this.element('conflict').hidden;
      this.element('cancel').disabled = busy || !this.changes.size;
      this.element('reload').disabled = busy || !this.file;
      this.element('persist').disabled = busy || !this.file || !this.selectedKeys.size ||
          Boolean(this.changes.size) || !this.targets.length;
      this.element('persistence-target').disabled = busy || !this.targets.length;
      this.element('persistence-refresh').disabled = busy;
      this.element('persistence-count').textContent = this.text('persistenceCount')
          .replace('{0}', String(this.selectedKeys.size)) + (this.changes.size ? ' ' + this.text('persistenceSaveFirst') : '');
      this.element('key-filter').disabled = !this.file;
      this.element('modified-only').disabled = !this.file;
      this.element('count').textContent = this.text('count').replace('{0}', String(this.changes.size));
      this.root.querySelectorAll('[data-path], #configuration-conflict button').forEach(button => {
        button.disabled = busy;
      });
      this.rows.forEach(({row, input, reset, selection}, key) => {
        input.disabled = busy;
        reset.disabled = busy || !this.changes.has(key);
        selection.disabled = busy;
        selection.checked = this.selectedKeys.has(key);
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
      this.root.setAttribute('aria-busy', String(busy || this.persistenceBusy));
      this.updateControls();
    }

    cancel() {
      this.changes.clear();
      this.displayFile(this.file, this.selectedKeys);
      this.message('');
    }

    async save() {
      if (this.busy || !this.changes.size) {
        return;
      }
      this.setBusy(true);
      try {
        const saved = await sp.ajaxRequest(this.api + '/file')
            .byPutMethod().sendAndPromiseJsonResponse({
              path : this.file.path,
              revision : this.file.revision,
              properties : Object.fromEntries(this.changes)
            });
        const changedKeys = new Set([...this.selectedKeys, ...this.changes.keys()]);
        this.changes.clear();
        this.displayFile(saved, changedKeys);
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
      this.displayFile(this.latest, this.selectedKeys);
      this.message('rebased');
    }

    showError(error, fallback) {
      const key = new Map([[403, 'forbidden'], [400, 'invalid'], [404, 'notFound']]).get(error.status);
      this.message(key || fallback, true);
    }

    persistenceMessage(key, error = false, target = '', count = 0) {
      const node = this.element('persistence-message');
      node.textContent = key ? this.text(key).replace('{0}', target).replace('{1}', String(count)) : '';
      node.classList.toggle('configuration-error', error);
    }

    renderPersistenceTargets(preferred) {
      const select = this.element('persistence-target');
      select.replaceChildren();
      this.targets.forEach(target => {
        const option = document.createElement('option');
        option.value = target.name;
        option.textContent = target.name + (target.exists ? '' : ' ' + this.text('persistenceNew'));
        select.append(option);
      });
      select.value = this.targets.some(target => target.name === preferred) ? preferred
          : (this.targets.at(-1)?.name || '');
    }

    async loadPersistenceTargets() {
      if (this.persistenceBusy) {
        return;
      }
      const preferred = this.element('persistence-target').value;
      this.persistenceBusy = true;
      this.updateControls();
      try {
        this.targets = await sp.ajaxRequest(this.api + '/persistence').sendAndPromiseJsonResponse();
        this.renderPersistenceTargets(preferred);
        this.persistenceMessage('');
      } catch (error) {
        this.targets = [];
        this.renderPersistenceTargets();
        this.persistenceMessage(error.status === 403 ? 'forbidden' : 'persistenceLoadError', true);
      } finally {
        this.persistenceBusy = false;
        this.setBusy(this.busy);
      }
    }

    async persist() {
      const target = this.targets.find(item => item.name === this.element('persistence-target').value);
      if (this.busy || this.persistenceBusy || this.changes.size || !this.selectedKeys.size || !target) {
        return;
      }
      this.persistenceBusy = true;
      this.updateControls();
      const count = this.selectedKeys.size;
      try {
        const saved = await sp.ajaxRequest(this.api + '/persistence').byPostMethod()
            .sendAndPromiseJsonResponse({path : this.file.path, revision : this.file.revision,
              keys : Array.from(this.selectedKeys), target});
        this.targets = this.targets.map(item => item.name === saved.name ? saved : item);
        this.renderPersistenceTargets(saved.name);
        this.selectedKeys.clear();
        this.persistenceMessage('persistenceSaved', false, saved.name, count);
      } catch (error) {
        const key = error.status === 409 ? 'persistenceConflict' : 'persistenceError';
        this.persistenceMessage(error.status === 403 ? 'forbidden' : key, true);
      } finally {
        this.persistenceBusy = false;
        this.setBusy(this.busy);
      }
    }
  }

  whenSilverpeasReady(function() {
    const root = document.querySelector('#configuration-console');
    if (root) {
      new ConfigurationConsole(root);
    }
  });
})();
