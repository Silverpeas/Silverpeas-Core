/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

(function(){
  window.activateIDCards = function() {
    whenSilverpeasEntirelyLoaded(function() {
      const promises = [];
      sp.element.querySelectorAll('.user-card')
          .filter(function($userCard) {
            return !$userCard.__userCardActivated;
          }).forEach(function($userCard) {
              clearTimeout(__timer);
              $userCard.__userCardActivated = true;
              promises.push(User.getExtended($userCard.getAttribute('rel')).then(function(user) {
                const nodeToUpdate = [{
                  element : sp.element.querySelector('.userToZoom', $userCard),
                  textValue : user.firstName + ' ' + user.lastName
                }];
                for (let key in user) {
                  if (user.hasOwnProperty(key)) {
                    const val = user[key];
                    if (key === 'avatar') {
                      nodeToUpdate.push({
                        element : sp.element.querySelector('.' + key + ' img', $userCard),
                        srcValue : val
                      });
                    } else if (key === 'moreData') {
                      for (let keyMore in val) {
                        if (val.hasOwnProperty(keyMore)) {
                          nodeToUpdate.push({
                            element : sp.element.querySelector('.' + keyMore, $userCard),
                            textValue : val[keyMore]
                          });
                        }
                      }
                    } else {
                      nodeToUpdate.push({
                        element : sp.element.querySelector('.' + key, $userCard),
                        textValue : user[key]
                      });
                    }
                  }
                }
                nodeToUpdate.filter(function(data) {
                  return !!data.element;
                }).forEach(function(data) {
                  if (data.srcValue) {
                    if (data.element.getAttribute('src') !== data.srcValue) {
                      sp.element.cloneAndReplace(data.element, function(clone) {
                        clone.setAttribute('src', data.srcValue);
                      });
                    }
                  } else if (data.element.childNodes.length) {
                    data.element.childNodes[0].textContent = data.textValue;
                  } else {
                    data.element.innerText = data.textValue;
                  }
                });
              }));
          });
      if (promises.length) {
        sp.promise.whenAllResolved(promises).then(activateUserZoom);
      }
    });
  }

  /**
   * Populate all identity card in page.
   */
  const __timer = setTimeout(activateIDCards, 1000);
  setTimeout(activateIDCards, 100);
})();