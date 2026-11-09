/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
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

(function() {
  let templatePromise;

  function getTemplate() {
    if (!templatePromise) {
      templatePromise = new Promise(function(resolve, reject) {
        $.ajax({
          url : '/weblib/ckeditor/identitycard/template.html',
          dataType : 'html',
          success : resolve,
          error : function() {
            $.ajax({
              url : webContext + '/wysiwyg/jsp/ckeditor/plugins/identitycard/template.html',
              dataType : 'html',
              success : resolve,
              error : reject
            });
          }
        });
      });
    }
    return templatePromise;
  }

  function restoreFields(card, template) {
    const container = document.createElement('div');
    container.innerHTML = template;
    const model = container.querySelector('.user-card');
    if (!model) {
      return;
    }
    const existingKeys = new Set(Array.from(card.querySelectorAll('.field'))
        .flatMap(function(field) { return Array.from(field.classList); }));
    const missingField = Array.from(model.querySelectorAll('.field')).some(function(field) {
      const key = Array.from(field.classList).find(function(name) { return name !== 'field'; });
      return key && !existingKeys.has(key);
    });
    if (missingField) {
      // Rebuild from the model to preserve the order and nesting of custom templates.
      card.innerHTML = model.innerHTML;
    }
  }

  function textValue(value) {
    return value == null ? '' : String(value).unescapeHTML();
  }

  function updateCard(card, user) {
    const values = Object.assign({}, user, user.moreData, {
      userToZoom : textValue(user.firstName) + ' ' + textValue(user.lastName)
    });
    card.querySelectorAll('.field').forEach(function(field) {
      const key = Array.from(field.classList).find(function(name) {
        return name !== 'field' && Object.hasOwn(values, name);
      });
      const value = key === 'userToZoom' ? values[key] : textValue(values[key]);
      // Replace old content, including formatting, while preserving the user-zoom indicator.
      const content = Array.from(field.childNodes).filter(function(node) {
        return node.nodeType !== 1 || !node.matches('.userzoom-infoConnection');
      });
      if (content.length) {
        content[0].textContent = value;
        content.slice(1).forEach(function(node) { field.removeChild(node); });
      } else {
        field.prepend(document.createTextNode(value));
      }
      if (key === 'userToZoom') {
        field.setAttribute('rel', card.getAttribute('rel'));
      }
    });
    const avatar = card.querySelector('.avatar img');
    if (avatar && user.avatar && avatar.getAttribute('src') !== user.avatar) {
      sp.element.cloneAndReplace(avatar, function(clone) {
        clone.setAttribute('src', user.avatar);
      });
    }
  }

  window.activateIDCards = function() {
    whenSilverpeasEntirelyLoaded(function() {
      const promises = [];
      sp.element.querySelectorAll('.user-card').filter(function(card) {
        return !card.__userCardActivated;
      }).forEach(function(card) {
        clearTimeout(timer);
        card.__userCardActivated = true;
        promises.push(User.getExtended(card.getAttribute('rel')).then(function(user) {
          // Populate immediately, including legacy cards whose template still needs loading.
          updateCard(card, user);
          if (card.getAttribute('data-identitycard-version') === '1') {
            return;
          }
          // Only old content can have lost empty fields during CKEditor serialization.
          return getTemplate().then(function(template) {
            restoreFields(card, template);
            updateCard(card, user);
          });
        }).catch(function(error) {
          card.__userCardActivated = false;
          // A later explicit activation can retry this card.
          console.warn('Unable to refresh an identity card.', error);
        }));
      });
      if (promises.length) {
        sp.promise.whenAllResolved(promises).then(activateUserZoom);
      }
    });
  };

  /**
   * Populate all identity cards in the page.
   */
  const timer = setTimeout(activateIDCards, 1000);
  setTimeout(activateIDCards, 100);
})();
