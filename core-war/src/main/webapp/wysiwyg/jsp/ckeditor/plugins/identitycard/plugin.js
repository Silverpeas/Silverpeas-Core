/**
 *    Copyright (C) 2000 - 2026 Silverpeas
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as
 *   published by the Free Software Foundation, either version 3 of the
 *   License, or (at your option) any later version.
 *
 *   As a special exception to the terms and conditions of version 3.0 of
 *   the GPL, you may redistribute this Program in connection with Free/Libre
 *   Open Source Software ("FLOSS") applications as described in Silverpeas's
 *   FLOSS exception.  You should have received a copy of the text describing
 *   the FLOSS exception, and it is also available here:
 *   "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

let identityCardTemplatePromise;

function loadIdentityCardTemplate(defaultUrl) {
  if (!identityCardTemplatePromise) {
    identityCardTemplatePromise = new Promise(function(resolve, reject) {
      $.ajax({
        url : '/weblib/ckeditor/identitycard/template.html',
        dataType : 'html',
        success : resolve,
        error : function() {
          $.ajax({url : defaultUrl, dataType : 'html', success : resolve, error : reject});
        }
      });
    }).then(function(template) {
      const container = document.createElement('div');
      container.innerHTML = template;
      const model = container.querySelector('.user-card');
      if (!model) {
        throw new Error('The identity card template must contain a user-card element.');
      }
      return model.outerHTML;
    });
  }
  return identityCardTemplatePromise;
}

CKEDITOR.plugins.add('identitycard', {
  // This plugin requires the Widgets System defined in the 'widget' plugin.
  requires : 'widget',

  // Register the icon used for the toolbar button. It must be the same
  // as the name of the widget.
  icons : 'identitycard',

  lang : 'en,fr',

  beforeInit : function() {
    // The DTD is shared by CKEditor instances. Empty spans carry identity-card field keys.
    CKEDITOR.dtd.$removeEmpty.span = false;
    loadIdentityCardTemplate(this.path + 'template.html');
  },

  // The plugin initialization logic goes inside this method.
  init : function(editor) {

    let templateReady = false;
    identityCardTemplatePromise.then(function(template) {
      editor.widgets.registered.identitycard.template.source = template;
      templateReady = true;
    }).catch(function(error) {
      console.warn('Unable to load the identity card template.', error);
    });
    // A first insertion must wait for the configured template rather than create an empty widget.
    editor.on('beforeCommandExec', function(event) {
      if (event.data.name === 'identitycard' && !templateReady) {
        event.cancel();
        identityCardTemplatePromise.then(function() {
          editor.execCommand('identitycard');
        }).catch(function(error) {
          console.warn('Unable to insert an identity card.', error);
        });
      }
    });

    // Register the editing dialog.
    CKEDITOR.dialog.add('identitycard', this.path + 'dialogs/identitycard.js');

    // Register the identitycard widget.
    editor.widgets.add('identitycard', {

      upcast : function(element) {
        return element.name === 'div' && element.hasClass('user-card');
      },

      // Minimum HTML which is required by this widget to work.
      requiredContent : 'div(user-card)',

      // Define the template of a new Identity Card widget.
      template : '<div class="user-card"></div>',

      dialog : 'identitycard',

      draggable : true,

      init : function() {
        const userId = this.element.getAttribute('rel');
        this.setData('userId', userId);
        updateContent(this);
      },

      data : function() {
        updateContent(this);
      }
    });

    editor.ui.addButton('identitycard', {
      label : editor.lang.identitycard.identitycard, command : 'identitycard',
    });

  }
});

function updateContent(widget) {
  if (!widget.data.userId) {
    return;
  }

  const card = widget.element.$;
  const userId = widget.data.userId;
  card.setAttribute('rel', userId);
  // Repair older widgets before marking them as safe to display without loading a template.
  identityCardTemplatePromise.then(function(template) {
    if (widget.data.userId !== userId) {
      return;
    }
    const container = document.createElement('div');
    container.innerHTML = template;
    const model = container.querySelector('.user-card');
    if (!model) {
      throw new Error('The identity card template must contain a user-card element.');
    }
    const existingKeys = new Set(Array.from(card.querySelectorAll('.field'))
        .flatMap(function(field) { return Array.from(field.classList); }));
    const missingField = Array.from(model.querySelectorAll('.field')).some(function(field) {
      const key = Array.from(field.classList).find(function(name) { return name !== 'field'; });
      return key && !existingKeys.has(key);
    });
    if (missingField) {
      card.innerHTML = model.innerHTML;
    }
    $.ajax({
      type : 'GET',
      url : webContext + '/services/profile/users/' + encodeURIComponent(userId) + '?extended=true',
      dataType : 'json',
      cache : false,
      success : function(user) {
        if (widget.data.userId !== userId) {
          return;
        }
        const values = Object.assign({}, user, user.moreData, {
          userToZoom : identityCardText(user.firstName) + ' ' + identityCardText(user.lastName)
        });
        card.querySelectorAll('.field').forEach(function(field) {
          const key = Array.from(field.classList).find(function(name) {
            return name !== 'field' && Object.hasOwn(values, name);
          });
          field.textContent = key === 'userToZoom' ? values[key] : identityCardText(values[key]);
          if (key === 'userToZoom') {
            field.setAttribute('rel', userId);
          }
        });
        const avatar = card.querySelector('.avatar img');
        if (avatar && user.avatar) {
          avatar.setAttribute('src', user.avatar);
        }
        card.setAttribute('data-identitycard-version', '1');
      },
      error : function(error) {
        console.warn('Unable to update an identity card in the editor.', error);
      }
    });
  }).catch(function(error) {
    console.warn('Unable to prepare an identity card in the editor.', error);
  });
}

function identityCardText(value) {
  return value == null ? '' : String(value).unescapeHTML();
}
