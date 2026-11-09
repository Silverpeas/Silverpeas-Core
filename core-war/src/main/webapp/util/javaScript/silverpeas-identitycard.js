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
      templatePromise = new Promise(function(resolve) {
        $.ajax({
          url : '/weblib/ckeditor/identitycard/template.html',
          dataType : 'html',
          success : resolve,
          error : function() {
            // The editor uses these fields when no custom template is available.
            resolve('<div class="user-card"><div class="avatar"><img alt="avatar"></div>' +
                ['userToZoom', 'emailAddress', 'title', 'phone', 'cellularPhone'].map(function(field) {
                  return '<span class="field ' + field + '"></span>';
                }).join('') + '</div>');
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
    const missingField = Array.from(model.querySelectorAll('.field')).some(function(field) {
      return !Array.from(card.querySelectorAll('.field')).some(function(existing) {
        return existing.className === field.className;
      });
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
      if (!key) {
        return;
      }
      const value = key === 'userToZoom' ? values[key] : textValue(values[key]);
      // Keep the connection indicator and other children added by user zoom.
      if (field.firstChild && field.firstChild.nodeType === 3) {
        field.firstChild.textContent = value;
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
        promises.push(Promise.all([getTemplate(), User.getExtended(card.getAttribute('rel'))])
            .then(function(results) {
              restoreFields(card, results[0]);
              updateCard(card, results[1]);
            }).catch(function() {
              card.__userCardActivated = false;
              console.warn('Unable to refresh an identity card.');
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
