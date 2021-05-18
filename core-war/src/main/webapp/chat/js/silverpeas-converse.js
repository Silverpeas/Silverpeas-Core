/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

(function() {

  /**
   * Setups the vcard management in order to get the display of user avatar and user name.
   * @private
   */
  function __setupVCard(chatOptions) {
    chatOptions.whitelisted_plugins.push('silverpeas-vcard');
    converse.plugins.add('silverpeas-vcard', {
      dependencies: ["converse-vcard"],
      initialize : function() {
        const _converse = this._converse;
        const __superGetFn = _converse.api.vcard.get;
        const __superSetFn = _converse.api.vcard.set;
        Object.assign(_converse.api.vcard, {
          get : function(model, force) {
            let _curJid = model;
            if (typeof model === 'object') {
              _curJid = model.attributes.jid;
            }
            return __superGetFn.call(this, model, force).then(function(vCard) {
              for (let key in vCard) {
                if (typeof vCard[key] === 'undefined') {
                  vCard[key] = '';
                }
              }
              if (_curJid === chatOptions.jid) {
                extendsObject(false, chatOptions.vcard, vCard);
              }
              const contact = _converse.roster.findWhere({'jid' : _curJid});
              if (contact && contact.attributes) {
                const attrs = contact.attributes;
                const newAttrs = ['image', 'image_type'].filter(function(t) {
                  return !!(attrs[t] && vCard[t] && attrs[t] !== vCard[t]);
                });
                if (newAttrs.length > 0) {
                  const newData = {};
                  newAttrs.forEach(function(t) {
                    newData[t] = vCard[t];
                  });
                  contact.save(newData).then(function() {
                    sp.log.info('model update success', _curJid);
                    return vCard;
                  }, function() {
                    sp.log.info('model update error', _curJid);
                    return vCard;
                  });
                }
              }
              return vCard;
            });
          },
          set : function(jid, data) {
            if (jid === chatOptions.jid) {
              data['fn'] = chatOptions.vcard.fn;
              data['fullname'] = chatOptions.vcard.fn;
              data['nickname'] = chatOptions.vcard.fn;
              if (data['image'] !== chatOptions.vcard['image']) {
                delete data['image_hash'];
              }
            }
            return __superSetFn.call(this, jid, data);
          }
        })
      }
    });
  }

  /**
   * Setups the box resize management according to Silverpeas's constraints.
   * @private
   */
  function __setupResize(chatOptions) {
    chatOptions.whitelisted_plugins.push('silverpeas-resize');
    converse.plugins.add('silverpeas-resize', {
      dependencies: ["converse-dragresize"],
      initialize : function() {
        const _converse = this._converse;
        let __isDragging = false;
        const __startDrag = function() {
          if (!__isDragging) {
            spWindow.startsBoxResize();
            __isDragging = true;
          }
        };
        const __endDrag = function() {
          if (__isDragging) {
            spWindow.endsBoxResize();
            __isDragging = false;
          }
        };
        _converse.api.listen.on('startDiagonalResize', __startDrag);
        _converse.api.listen.on('startHorizontalResize', __startDrag);
        _converse.api.listen.on('startVerticalResize', __startDrag);
        document.addEventListener('mouseup', __endDrag);
      }
    });
  }

  /**
   * Setups rooms addons.
   * Rooms API is used on room creation.
   * For now, it configures automatically the creation of a new groupchat room in order to set
   * automatically the topic (to get a readable title for the room).
   * @private
   */
  function __setupRoomsAddons(chatOptions) {
    chatOptions.whitelisted_plugins.push('silverpeas-rooms');
    converse.plugins.add('silverpeas-rooms', {
      dependencies: ["converse-muc"],
      initialize : function() {
        const _converse = this._converse;
        const __superGetFn = _converse.api.rooms.get;
        Object.assign(_converse.api.rooms, {
          get : function(jid, attrs, create) {
            if (create && attrs && typeof jid === 'string' &&
                StringUtil.isNotDefined(attrs.id) &&
                StringUtil.isDefined(attrs.jid) &&
                StringUtil.isNotDefined(attrs.name)) {
              const roomName = Strophe.unescapeNode(attrs.jid.replace(/@.+$/, ''));
              if (roomName.length !== attrs.jid.length) {
                Object.assign(attrs, {
                  'auto_configure' : true,
                  'roomconfig' : {
                    'roomname' : roomName,
                    'membersonly' : true
                  }
                });
              }
            }
            return __superGetFn.call(this, jid, attrs, create);
          }
        });
      }
    });
  }

  /**
   * Setups chatboxes addons.
   * Chatboxes API is used on room display.
   * For now the notifications are handled in the way that unread message counter for MUC is
   * updated num_unread or num_unread_general instead of only num_unread.
   * @private
   */
  function __setupChatboxesAddons(chatOptions) {
    chatOptions.whitelisted_plugins.push('silverpeas-chatboxes');
    converse.plugins.add('silverpeas-chatboxes', {
      dependencies: ["silverpeas-rooms", "converse-chatboxes"],
      initialize : function() {
        const _converse = this._converse;
        if (!!_converse.settings['notify_all_room_messages']) {
          _converse.api.listen.on('chatBoxesInitialized', function() {
            _converse.chatboxes.on('change:num_unread_general', function(chatbox) {
              // num_unread_general means both the direct messages and the messages sent to other
              // participants
              chatbox.save({
                'num_unread' : chatbox.get('num_unread_general')
              });
            });
          });
        }
      }
    });
  }

  /**
   * Setups the common stuffs in order to get the chat working into Silverpeas.
   * @private
   */
  function __setupSilverpeas(chatOptions) {
    chatOptions.whitelisted_plugins.push('silverpeas-setup');
    converse.plugins.add('silverpeas-setup', {
      dependencies: ["silverpeas-vcard"],
      initialize : function() {
        const _converse = this._converse;
        window._converse = _converse;
        const urlAsDataPromise = sp.base64.urlAsData(chatOptions.userAvatarUrl);
        const refreshUserAvatar = function() {
          const promises = [];
          for (let key in _converse.promises) {
            if (key.endsWith('Initialized')) {
              let promise = _converse.promises[key];
              if (sp.promise.isOne(promise)) {
                promises.push(promise);
              }
            }
          }
          return sp.promise.whenAllResolved(promises).then(function() {
            return _converse.api.vcard.get(chatOptions.jid, true).then(function(vCard) {
              urlAsDataPromise.then(function(data) {
                const avatarData = data['justData'];
                if (avatarData !== vCard['image']) {
                  vCard['image'] = avatarData;
                  vCard['image_type'] = data['type'];
                  try {
                    _converse.api.vcard.set(chatOptions.jid, vCard).then(function() {
                      sp.log.info('vcard update success');
                    }, function() {
                      sp.log.error('vcard update error', arguments);
                    });
                  } catch (e) {
                    sp.log.error('vcard update error', e);
                  }
                } else {
                  sp.log.info('vcard is up to date');
                }
              });
            });
          });
        };
        _converse.api.listen.on('connected', function() {
          _converse.promises.roomsPanelRendered.then(function() {
            if (!chatOptions.acl.groupchat.creation) {
              sp.element.querySelectorAll('#conversejs .controlbox-padded a').forEach(function($el, index) {
                if (index !== 0) {
                  $el.remove();
                }
              });
            }
            refreshUserAvatar().then(function() {
              return _converse.api.controlbox.open();
            });
          });
        });
      },
    });
  }

  window.SilverChat = new function() {
    let __settings = {
      'whitelisted_plugins' : []
    };
    this.init = function(chatOptions) {
      __settings = extendsObject(__settings, chatOptions);
      __setupVCard(__settings);
      __setupResize(__settings);
      __setupRoomsAddons(__settings);
      __setupSilverpeas(__settings);
      __setupChatboxesAddons(__settings);
      return this;
    };
    this.start = function() {
      converse.initialize({
        'loglevel' : __settings.debug ? 'debug' : 'error',
        'i18n' : __settings.language,
        'assets_path' : '/silverpeas/chat/converse/',
        'sounds_path' : '/silverpeas/chat/converse/',
        'play_sounds' : false,
        'bosh_service_url' : __settings.url,
        'allow_logout' : false,
        'auto_login' : true,
        'auto_reconnect' : true,
        'jid' : __settings.jid,
        'default_domain' : __settings.domain,
        'domain_placeholder' : __settings.domain,
        'password' : __settings.password,
        'autocomplete_add_contact' : false,
        'notification_icon' : __settings.notificationLogo,
        'muc_domain' : 'conference.' + __settings.domain,
        'locked_muc_domain' : 'hidden',
        'muc_disable_slash_commands' : true,
        'locked_muc_nickname' : true,
        'nickname' : __settings.vcard.fn,
        'auto_register_muc_nickname' : true,
        'notify_all_room_messages': true,
        'auto_join_on_invite': false,
        'roster_groups' : false,
        'allow_adhoc_commands' : false,
        'allow_contact_removal' : false,
        'allow_contact_requests' : false,
        'allow_registration' : false,
        'show_controlbox_by_default' : false,
        'discover_connection_methods' : false,
        'whitelisted_plugins' : __settings.whitelisted_plugins
      });
    };
    this.stop = function() {
      _converse.off();
      return _converse.api.user.logout();
    };
  }
})();
