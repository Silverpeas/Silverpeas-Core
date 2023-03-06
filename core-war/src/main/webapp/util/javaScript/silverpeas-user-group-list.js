/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

(function() {

  if (window.UserGroupSelect && window.ListOfUsersAndGroups) {
    return;
  }

  const SELECT_NB_ITEM_PER_TYPE = 20;
  const SELECTION_TYPE = {
    USER : 0,
    GROUP : 1,
    USER_GROUP : 2,
    decode : function(value) {
      let decoded;
      if (typeof value === 'string') {
        decoded = SELECTION_TYPE[value.toUpperCase()];
      }
      return !decoded ? SELECTION_TYPE.USER : decoded;
    }
  };

  const ICON_USER_GROUP_PANEL = webContext + "/util/icons/create-action/add-existing-group.png";
  const ICON_USER_PANEL = webContext + "/util/icons/user.png";
  const ICON_GROUP_PANEL = webContext + "/util/icons/groups.png";
  const ICON_GROUP_SYNC = webContext + "/jobDomainPeas/jsp/icons/scheduledGroup.gif";
  const ICON_GROUP = webContext + "/util/icons/groupe.gif";
  const ICON_USER = webContext + "/util/icons/user.gif";
  const ICON_USER_DEACTIVATED = webContext + "/util/icons/user-deactivated.png";
  const ICON_USER_REMOVED = webContext + "/util/icons/user-deactivated.png";
  const ICON_USER_BLOCKED = webContext + "/util/icons/user-blocked.png";
  const ICON_USER_EXPIRED = webContext + "/util/icons/user-expired.png";

  const USER_MANUAL_NOTIFICATION_USER_RECEIVER_LIMIT_VALUE = UserGroupListSettings.get("u.m.n.u.r.l.v");
  const DOMAIN_RESTRICTION = UserGroupListSettings.get("d.r");
  const NB_DOMAINS = UserGroupListSettings.get("d.nb");

  const USER_MANUAL_NOTIFICATION_USER_RECEIVER_LIMIT_MESSAGE = sp.i18n.get("n.m.r.l.m.w");
  const LABEL_GROUPS = sp.i18n.get("GML.group_s");
  const LABEL_USERS = sp.i18n.get("GML.user_s");
  const LABEL_AND = sp.i18n.get("GML.and");
  const LABEL_DELETE = sp.i18n.get("GML.delete");
  const LABEL_DELETE_ALL = sp.i18n.get("GML.deleteAll");
  const LABEL_CONFIRM_DELETE_ALL = sp.i18n.get("GML.confirmation.deleteAll");
  const LABEL_REMOVE = sp.i18n.get("GML.action.remove");
  const LABEL_REMOVE_ALL = sp.i18n.get("GML.action.removeAll");
  const LABEL_KEEP = sp.i18n.get("GML.action.keep");
  const LABEL_UPDATE = sp.i18n.get("GML.modify");
  const LABEL_SELECT = sp.i18n.get("GML.action.select");
  const LABEL_LIST_CHANGED = sp.i18n.get("GML.list.changed.message");

  let __globalIdCounter = 0;

  const UserGroupRequester = function(options) {
    const __applyCommonParameters = function(params) {
      this.options = extendsObject({
        includeRemovedUsers : false,
        hideDeactivatedState : false,
        domainIdFilter : '',
        resourceIdFilter : '',
        componentIdFilter : ''
      }, options);
      let _params = params;
      if (typeof _params === 'undefined') {
        _params = {};
      }
      if (typeof _params === 'object') {
        if (!_params.id && !_params.ids) {
          _params.withChildren = true;
          if (this.options.hideDeactivatedState) {
            _params.userStatesToExclude = ['DEACTIVATED'];
          }
          if (this.options.domainIdFilter) {
            _params.domain = this.options.domainIdFilter;
          }
          if (this.options.componentIdFilter) {
            _params.component = this.options.componentIdFilter;
          }
          if (this.options.resourceIdFilter) {
            _params.resource = this.options.resourceIdFilter;
          }
          if (this.options.roleFilter) {
            _params.roles = this.options.roleFilter.join(',');
            _params.matchingAllRoles = !!this.options.matchingAllRoles;
          }
          if (this.options.groupFilter) {
            _params.group = this.options.groupFilter;
          }
        }
        if (this.options.includeRemovedUsers) {
          _params.includeRemoved = true;
        }
      }
      if (_params.limit) {
        _params.page = {number : 1, size : _params.limit};
      }
      return _params;
    }.bind(this);

    this.getUsers = function(params) {
      const finalParams = __applyCommonParameters(params);
      return User.get(finalParams).then(function(users) {
        if (finalParams.limit && finalParams.limit < users.length) {
          users.splice(finalParams.limit, 1);
          users.hasMore = true;
        }
        return users;
      });
    };

    this.getUserGroups = function(params) {
      const finalParams = __applyCommonParameters(params);
      return UserGroup.get(finalParams).then(function(groups) {
        if (finalParams.limit && finalParams.limit < groups.length) {
          groups.splice(finalParams.limit, 1);
          groups.hasMore = true;
        }
        return groups;
      });
    }
  };

  const UserGroupSelectize = function(userGroupSelectInstance) {
    applyReadyBehaviorOn(this);

    /**
     * Refreshes the items according the current id selection (kind of reset).
     * @returns {*}
     */
    this.refresh = function(silent) {
      this.clear();
      const __userDeferred = sp.promise.deferred();
      const __groupDeferred = sp.promise.deferred();
      const __initializeByAjaxDone = [__userDeferred.promise, __groupDeferred.promise];
      // Groups
      if (userGroupSelectInstance.context.currentGroupIds.length) {
        __requester.getUserGroups({ids : userGroupSelectInstance.context.currentGroupIds}).then(
            function(groups) {
              groups.forEach(function(group) {
                const item = new SelectUserGroupItem(group, userGroupSelectInstance);
                __applyItemSelectizeData(item, 'group-', 1);
                __selectize.addOption(item);
                __selectize.addItem(item.id, true);
              });
              __groupDeferred.resolve();
            });
      } else {
        __groupDeferred.resolve();
      }
      // Users
      if (userGroupSelectInstance.context.currentUserIds.length) {
        __requester.getUsers({id : userGroupSelectInstance.context.currentUserIds}).then(
            function(users) {
              users.forEach(function(user) {
                const item = new SelectUserItem(user, userGroupSelectInstance);
                __applyItemSelectizeData(item, 'user-', 1);
                __selectize.addOption(item);
                __selectize.addItem(item.id, true);
              });
              __userDeferred.resolve();
            });
      } else {
        __userDeferred.resolve();
      }
      return sp.promise.whenAllResolved(__initializeByAjaxDone).then(function() {
        if (!silent) {
          userGroupSelectInstance.ready(function() {
            __onChange();
          });
        }
      });
    };

    /**
     * Counts the total number of selected users by taking into account simple users and users of
     * groups.
     * @returns {int}
     */
    this.getTotalOfSelectedUsers = function() {
      let count = 0;
      __selectize.items.forEach(function(selectedId) {
        count = count + __selectize.options[selectedId].getUserCount();
      });
      return count;
    };

    /**
     * Shows the item selection. Nothing is done if already displayed.
     */
    this.show = function() {
      __selectize.open();
    };

    /**
     * Hides the item selection. Nothing is done if already hided.
     */
    this.hide = function() {
      __selectize.close();
    };

    /**
     * Focuses the item selection.
     */
    this.focus = function() {
      __selectize.focus();
    };

    /**
     * Focuses the item selection.
     */
    this.clear = function() {
      __selectize.clear(true);
    };

    /*
    Initializations
     */

    const __sequential = sp.promise.resolveDirectlyWith();
    const __requester = new UserGroupRequester(userGroupSelectInstance.options);
    const __applyItemSelectizeData = function(item, idPrefix, score) {
      item.id = idPrefix + item.getId();
      item.name = item.getFullName();
      item.score = score;
    };
    const __isUserId = function(id) {
      return id.startsWith("user-")
    };
    const __normalizeId = function(id) {
      return id.replace(/[^0-9]/g, '');
    };
    const __updateIdContainers = function(id, allValues) {
      const _allValues = typeof allValues === 'string' ? [allValues] : allValues;
      const isAdd = Array.isArray(_allValues);
      const isUserId = __isUserId(id);
      const idContainer = isUserId ? userGroupSelectInstance.context.currentUserIds :
          userGroupSelectInstance.context.currentGroupIds;
      if (isAdd) {
        if (userGroupSelectInstance.options.userManualNotificationUserReceiverLimit &&
            this.getTotalOfSelectedUsers() > USER_MANUAL_NOTIFICATION_USER_RECEIVER_LIMIT_VALUE) {
          __selectize.removeItem(id, true);
          __selectize.refreshOptions(true);
          userGroupSelectInstance.showUserManualNotificationUserReceiverLimitMessage();
          return;
        }
        idContainer.removeAll();
        _allValues.filter(function(anId) {
          return isUserId ? __isUserId(anId) : !__isUserId(anId);
        }).forEach(function(anId) {
          idContainer.addElement(__normalizeId(anId));
        });
      } else {
        idContainer.removeElement(__normalizeId(id));
      }
      __onChange();
      userGroupSelectInstance.refreshCommons();
    }.bind(this);

    const __ifPreparedItemsWith = function(query) {
      const __itemDeferred = sp.promise.deferred();
      __sequential.then(function() {
        __selectize.loadedSearches = {};
        userGroupSelectInstance.context.searchInput.processQuery(query, function(ajaxPerformed) {
          for (let itemId in __selectize.options) {
            __selectize.options[itemId].score = 0;
            if (__selectize.items.indexOf(itemId) < 0) {
              __selectize.removeOption(itemId);
            }
          }
          __selectize.refreshOptions(true);

          if (ajaxPerformed) {
            const all = [];
            let currentTotalOfSelectedUsers;
            if (userGroupSelectInstance.options.userManualNotificationUserReceiverLimit) {
              currentTotalOfSelectedUsers = this.getTotalOfSelectedUsers();
            }

            function __addItemOption(idPrefix, item) {
              __applyItemSelectizeData(item, idPrefix, 1);
              if (typeof currentTotalOfSelectedUsers === 'undefined' ||
                  (currentTotalOfSelectedUsers + item.getUserCount()) <=
                  USER_MANUAL_NOTIFICATION_USER_RECEIVER_LIMIT_VALUE) {
                all.push(item);
              } else {
                userGroupSelectInstance.showUserManualNotificationUserReceiverLimitMessage();
              }
            }

            userGroupSelectInstance.context.groupItems.forEach(function(item) {
              __addItemOption('group-', item);
            });
            userGroupSelectInstance.context.userItems.forEach(function(item) {
              __addItemOption('user-', item);
            });
            __itemDeferred.resolve(all);
          }
        }.bind(this));
      }.bind(this));
      return __itemDeferred.promise;
    }.bind(this);

    const selectizePlugins = [];
    if (userGroupSelectInstance.options.queryInputName) {
      selectizePlugins.push({
        name : 'QueryInputUsedInForm',
        options : {inputName : userGroupSelectInstance.options.queryInputName}
      });
    }
    if (userGroupSelectInstance.options.doNotSelectAutomaticallyOnDropDownOpen) {
      selectizePlugins.push('DoNotSelectAutomaticallyOnDropDownOpen');
    }
    if (userGroupSelectInstance.options.navigationalBehavior) {
      selectizePlugins.push('NavigationalBehavior');
    }
    if (userGroupSelectInstance.options.selectOnTabulationKeyDown) {
      selectizePlugins.push('SelectOnTabulationKeyDown');
    }
    const __selectize = jQuery(userGroupSelectInstance.context.searchInput).selectize({
      plugins : selectizePlugins,
      valueField : 'id',
      placeholder : "          ",
      options : [],
      create : false,
      highlight : false,
      hideSelected : true,
      loadThrottle : 300,
      maxItems : userGroupSelectInstance.options.multiple ? null : 1,
      maxOptions : (SELECT_NB_ITEM_PER_TYPE * 2) - 1,
      render : {
        option : function(data, escape) {
          const option = data.getElement();
          option.classList.add('option');
          return option;
        },
        item : function(data, escape) {
          const item = data.getElement();
          item.classList.add('item');
          return item;
        }
      },
      score : function(search) {
        return function(item) {
          return item.score;
        };
      },
      load : function(query, callback) {
        __ifPreparedItemsWith(query).then(function(items) {
          callback(items);
        })
      }
    })[0].selectize;

    function __onChange() {
      const c = userGroupSelectInstance.context;
      let forceChangeTrigger = false;
      if (!c._last_currentUserIds) {
        c._last_currentUserIds = [];
        c._last_currentGroupIds = [];
        forceChangeTrigger = true;
      }
      const userIdsChange = !sp.object.areExistingValuesEqual(__copyAndSort(c._last_currentUserIds), __copyAndSort(c.currentUserIds));
      const groupIdsChange = !sp.object.areExistingValuesEqual(__copyAndSort(c._last_currentGroupIds), __copyAndSort(c.currentGroupIds));
      c._last_currentUserIds = [].concat(c.currentUserIds);
      c._last_currentGroupIds = [].concat(c.currentGroupIds);
      if (forceChangeTrigger || userIdsChange || groupIdsChange) {
        const o = userGroupSelectInstance.options;
        if (typeof o.onChange === 'function') {
          setTimeout(function() {
            try {
              o.onChange(userGroupSelectInstance);
            } finally {
              if (o.navigationalBehavior) {
                userGroupSelectInstance.removeAll();
              }
            }
          }, 0);
        } else {
          if (o.navigationalBehavior) {
            userGroupSelectInstance.removeAll();
          }
        }
      }
    }

    setTimeout(function() {
      if (userGroupSelectInstance.options.initialQuery) {
        __selectize.setTextboxValue(userGroupSelectInstance.options.initialQuery);
      }
      this.refresh(true).then(function() {
        __selectize.on('item_remove', function(id) {
          __updateIdContainers(id);
        });
        __selectize.on('item_add', function(id) {
          __updateIdContainers(id, __selectize.getValue());
        });
        setTimeout(function() {
          this.notifyReady();
          setTimeout(function() {
            if (userGroupSelectInstance.options.initialQuery) {
              __ifPreparedItemsWith(userGroupSelectInstance.options.initialQuery).then(function(items) {
                items.forEach(function(item) {
                  __selectize.addOption(item);
                });
              });
            }
          }, 0);
        }.bind(this), 0);
      }.bind(this));
    }.bind(this), 0);
  };

  window.UserGroupSelect = function(options) {
    applyReadyBehaviorOn(this);
    const __idCounter = __globalIdCounter;

    this.options = extendsObject({
      includeRemovedUsers : false,
      hideDeactivatedState : true,
      domainIdFilter : '',
      componentIdFilter : '',
      resourceIdFilter : '',
      groupFilter : '',
      roleFilter : [],
      matchingAllRoles : false,
      selectOnTabulationKeyDown : true,
      navigationalBehavior : false,
      doNotSelectAutomaticallyOnDropDownOpen : false,
      noUserPanel : false,
      noSelectionClear : false,
      initialQuery : false,
      readOnly : false,
      hidden : false,
      mandatory : false,
      userPanelId : '',
      initUserPanelUserIdParamName : '',
      initUserPanelGroupIdParamName : '',
      userInputName : '',
      groupInputName : '',
      queryInputName : '',
      currentUserId : '',
      rootContainerId : "select-user-group-container",
      initialUserIds : [],
      initialGroupIds : [],
      displayUserZoom : true,
      displayAvatar : true,
      layout : 'inline',
      displaySelection : true,
      multiple : false,
      userManualNotificationUserReceiverLimit : false,
      selectionType : 'USER',
      userPanelButtonLabel : LABEL_SELECT,
      removeButtonLabel : LABEL_REMOVE,
      onChange : undefined
    }, options);
    this.options.selectionType = SELECTION_TYPE.decode(this.options.selectionType);
    if (USER_MANUAL_NOTIFICATION_USER_RECEIVER_LIMIT_VALUE <= 0 || !this.options.multiple) {
      this.options.userManualNotificationUserReceiverLimit = false;
    }
    if (this.options.multiple && this.options.removeButtonLabel === LABEL_REMOVE) {
      this.options.removeButtonLabel = LABEL_REMOVE_ALL;
    }

    if (StringUtil.isNotDefined(this.options.userPanelId)) {
      __globalIdCounter = __globalIdCounter + 1;
      this.options.userPanelId = "user-group-select-" + __idCounter;
    }

    if (this.options.navigationalBehavior) {
      this.options.noSelectionClear = true;
    }

    const initialUserIds = __convertToString(this.options.initialUserIds);
    const initialGroupIds = __convertToString(this.options.initialGroupIds);

    this.context = {
      hidden : this.options.hidden,
      readOnly : this.options.readOnly,
      userPanelFormName : this.options.readOnly ? this.options.userPanelId : '',
      currentUserIds : [].concat(initialUserIds),
      currentGroupIds : [].concat(initialGroupIds),
      userItems : [],
      groupItems : []
    };

    this.updateFilterOptions = function(filterOptions) {
      if (typeof filterOptions.hideDeactivatedState !== 'undefined') {
        this.options.hideDeactivatedState = filterOptions.hideDeactivatedState;
      }
      if (typeof filterOptions.domainIdFilter !== 'undefined') {
        this.options.domainIdFilter = filterOptions.domainIdFilter;
      }
      if (typeof filterOptions.resourceIdFilter !== 'undefined') {
        this.options.resourceIdFilter = filterOptions.resourceIdFilter;
      }
      if (typeof filterOptions.componentIdFilter !== 'undefined') {
        this.options.componentIdFilter = filterOptions.componentIdFilter;
      }
      if (typeof filterOptions.roleFilter !== 'undefined') {
        this.options.roleFilter = filterOptions.roleFilter;
        if (typeof filterOptions.matchingAllRoles === 'boolean') {
          this.options.matchingAllRoles = filterOptions.matchingAllRoles;
        }
      }
      if (typeof filterOptions.groupFilter !== 'undefined') {
        this.options.groupFilter = filterOptions.groupFilter;
      }
    };

    this.removeAll = function() {
      this.context.currentUserIds = [];
      this.context.currentGroupIds = [];
      this.refreshCommons();
      if(!this.options.navigationalBehavior) {
        this.context.dropPanel.refresh();
      } else {
        this.context.dropPanel.clear();
      }
    };

    this.refreshCommons = function() {
      this.context.userSelectionInput.value = this.context.currentUserIds;
      this.context.groupSelectionInput.value = this.context.currentGroupIds;
    };
    
    this.getSelectedUserIds = function() {
      return this.context.currentUserIds;
    };

    this.getSelectedGroupIds = function() {
      return this.context.currentGroupIds;
    };

    this.existsSelection = function() {
      return this.getSelectedUserIds().length > 0 || this.getSelectedGroupIds().length > 0;
    };
    
    this.showUserManualNotificationUserReceiverLimitMessage = function() {
      this.context.userManualNotificationUserReceiverLimitMessageTip.show();
    };

    this.focus = function() {
      this.ready(function() {
        this.context.dropPanel.focus();
      }.bind(this));
    };

    const __requester = new UserGroupRequester(this.options);

    const _doSearchWith = function(search, callback) {
      const userDeferred = sp.promise.deferred();
      const groupDeferred = sp.promise.deferred();
      const searchDone = [userDeferred.promise, groupDeferred.promise];
      // Query
      const query = encodeURIComponent(search + '*');
      // Users
      if (this.options.selectionType !== SELECTION_TYPE.GROUP) {
        __requester.getUsers({name : query, limit : SELECT_NB_ITEM_PER_TYPE}).then(function(users) {
          this.context.userItems = [];
          users.forEach(function(user) {
            this.context.userItems.push(new SelectUserItem(user, this));
          }.bind(this));
          userDeferred.resolve();
        }.bind(this));
      } else {
        userDeferred.resolve();
      }
      // Groups
      if (this.options.selectionType !== SELECTION_TYPE.USER) {
        __requester.getUserGroups({name : query, limit : SELECT_NB_ITEM_PER_TYPE}).then(
            function(groups) {
              this.context.groupItems = [];
              groups.forEach(function(group) {
                this.context.groupItems.push(new SelectUserGroupItem(group, this));
              }.bind(this));
              groupDeferred.resolve();
            }.bind(this));
      } else {
        groupDeferred.resolve();
      }
      return sp.promise.whenAllResolved(searchDone).then(function() {
        callback(true);
      }.bind(this));
    }.bind(this);

    const __internalOnReady = function() {
      if (!this.context.readOnly && !this.context.hidden) {
        Mousetrap(this.context.rootContainer).bind('esc', function(e) {
          this.context.dropPanel.hide();
          this.context.searchInput.focus();
        }.bind(this));

        let __queryRunning = false;
        let __lastUnperformedQuery = "";
        const __performSearch = function(value, callback) {
          __queryRunning = true;
          __lastUnperformedQuery = "";
          const __doLastUnperformedQuery = function() {
            __queryRunning = false;
            if (__lastUnperformedQuery) {
              __performSearch(__lastUnperformedQuery, callback);
            }
          };
          _doSearchWith(value, callback).then(__doLastUnperformedQuery, __doLastUnperformedQuery);
        };
        this.context.searchInput.processQuery = function(value, callback) {
          let __value = (value || "").toLowerCase();
          __value = __value.trim();
          if (__value && __value.length > 2) {
            if (!__queryRunning) {
              __performSearch(__value, callback);
            } else {
              __lastUnperformedQuery = __value;
            }
          } else {
            callback(false);
            this.context.dropPanel.hide();
          }
        }.bind(this);
        this.context.searchInput.addEventListener('focus', function() {
          const __value = (this.context.searchInput.value || "").toLowerCase();
          if (__value.length > 2) {
            this.context.dropPanel.show();
          }
        }.bind(this));
      }
    }.bind(this);

    const __notifyReady = function() {
      __internalOnReady();
      setTimeout(function() {
        this.notifyReady();
      }.bind(this), 100);
    }.bind(this);

    whenSilverpeasReady(function() {
      this.context.rootContainer = document.querySelector("#" + this.options.rootContainerId);
      this.context.rootContainer.classList.add('select-user-group-container');

      if (!this.context.readOnly && !this.context.hidden) {
        const __searchContainer = document.createElement('div');
        __searchContainer.classList.add('search-input-container');
        this.context.searchInput = document.createElement('select');
        __searchContainer.appendChild(this.context.searchInput);
        this.context.searchInput.id = 'search-' + __idCounter;
        this.context.searchInput.classList.add('search-input','search');
        this.context.rootContainer.appendChild(__searchContainer);
        __searchContainer.appendChild(this.context.searchInput);
        this.context.dropPanel = new UserGroupSelectize(this);

        if (!this.options.noUserPanel) {
          const __userPanelSelect = document.createElement('a');
          __userPanelSelect.href = 'javascript:void(0)';
          __userPanelSelect.classList.add('user-panel-button');
          if (this.options.noSelectionClear) {
            __userPanelSelect.classList.add('alone');
          }
          const __userPanelSelectIcon = document.createElement('img');
          __userPanelSelectIcon.setAttribute("title", this.options.userPanelButtonLabel);
          __userPanelSelectIcon.setAttribute("alt", this.options.userPanelButtonLabel);
          switch (this.options.selectionType) {
            case SELECTION_TYPE.USER_GROUP :
              __userPanelSelectIcon.src = ICON_USER_GROUP_PANEL;
              break;
            case SELECTION_TYPE.GROUP :
              __userPanelSelectIcon.src = ICON_GROUP_PANEL;
              break;
            default:
              __userPanelSelectIcon.src = ICON_USER_PANEL;
              break;
          }
          __userPanelSelect.appendChild(__userPanelSelectIcon);
          __searchContainer.appendChild(__userPanelSelect);
          __userPanelSelect.addEventListener('click', function() {
            __openUserPanel(this);
          }.bind(this));
        }

        if (!this.options.noSelectionClear) {
          const __clear = document.createElement('a');
          __clear.href = 'javascript:void(0)';
          __clear.classList.add('remove-button');
          const __clearIcon = document.createElement("img");
          __clearIcon.setAttribute("border", "0");
          __clearIcon.setAttribute("title", this.options.removeButtonLabel);
          __clearIcon.setAttribute("alt", this.options.removeButtonLabel);
          __clearIcon.setAttribute("src", webContext + "/util/icons/delete.gif");
          __clear.appendChild(__clearIcon);
          __searchContainer.appendChild(__clear);
          __clear.addEventListener('click', function() {
            this.removeAll();
          }.bind(this));
        }

        if (this.options.mandatory) {
          const __mandatoryIcon = document.createElement("img");
          __mandatoryIcon.setAttribute("src", webContext + "/util/icons/mandatoryField.gif");
          __mandatoryIcon.setAttribute("width", "5");
          __mandatoryIcon.setAttribute("height", "5");
          __mandatoryIcon.classList.add('mandatory-icon');
          __searchContainer.appendChild(__mandatoryIcon);
        }

        this.context.userManualNotificationUserReceiverLimitMessageTip =
            TipManager.simpleInfo(__searchContainer,
                USER_MANUAL_NOTIFICATION_USER_RECEIVER_LIMIT_MESSAGE, {
                  position : {
                    my : "bottom center",
                    at : "top center"
                  },
                  show : {
                    event : 'none'
                  },
                  hide : {
                    event : 'unfocus'
                  }
                });

        __createHiddenInputs(this, this.context.rootContainer);

        jQuery(this.context.userSelectionInput).on('change', function() {
          // For now, the user panel sets USER and GROUP identifiers and then trigger a change on
          // USER and an other one on GROUP.
          // So, only USER change event is listened...
          const userPanelValue = this.context.userSelectionInput.value;
          const groupPanelValue = this.context.groupSelectionInput.value;
          this.context.currentUserIds = userPanelValue ? userPanelValue.split(',') : [];
          this.context.currentGroupIds = groupPanelValue ? groupPanelValue.split(',') : [];
          this.context.dropPanel.refresh(!this.options.multiple);
        }.bind(this));
        this.refreshCommons();
        this.context.dropPanel.ready(function() {
          __notifyReady();
        }.bind(this));
      } else {
        __createHiddenInputs(this, this.context.rootContainer);
        this.refreshCommons();
        if (!this.context.hidden) {
          const listOptions = extendsObject({}, this.options);
          listOptions.userSelectionInput = this.context.userSelectionInput;
          listOptions.groupSelectionInput = this.context.groupSelectionInput;
          new ListOfUsersAndGroups(listOptions).ready(function() {
            __notifyReady();
          }.bind(this));
        } else {
          __notifyReady();
        }
      }
    }.bind(this));
  };

  window.ListOfUsersAndGroups = function(options) {
    applyReadyBehaviorOn(this);
    const __idCounter = __globalIdCounter;

    this.options = extendsObject({
      simpleDetailsWhenRecipientTotalExceed : 0,
      includeRemovedUsers : false,
      hideDeactivatedState : true,
      domainIdFilter : '',
      resourceIdFilter : '',
      userPanelId : '',
      initUserPanelUserIdParamName : '',
      initUserPanelGroupIdParamName : '',
      userInputName : '',
      groupInputName : '',
      currentUserId : '',
      rootContainerId : "user-group-list-root-container",
      initialUserIds : [],
      initialGroupIds : [],
      userPanelInitUrl : false,
      jsSaveCallback : false,
      formSaveSelector : '',
      displayUserZoom : true,
      displayAvatar : true,
      groupSelectionInput : false,
      userSelectionInput : false,
      readOnly : undefined
    }, options);

    if (StringUtil.isNotDefined(this.options.userPanelId)) {
      __globalIdCounter = __globalIdCounter + 1;
      this.options.userPanelId = "user-group-list-" + __idCounter;
    }

    const initialUserIds = __convertToString(this.options.initialUserIds);
    const initialGroupIds = __convertToString(this.options.initialGroupIds);

    this.context = {
      type : 'list',
      readOnly : typeof this.options.readOnly === 'boolean' && this.options.readOnly,
      userPanelSaving : false,
      currentUserIds : [].concat(initialUserIds),
      currentGroupIds : [].concat(initialGroupIds)
    };
    this.context.displayActionPanel = !this.options.readOnly && typeof this.options.userPanelInitUrl === 'string';

    if (this.context.displayActionPanel) {
      if (typeof this.options.jsSaveCallback === 'function') {
        this.context.userPanelSaving = true;
        this.context.saveCallback = this.options.jsSaveCallback;
      } else if (typeof this.options.formSaveSelector === 'string' &&
          this.options.formSaveSelector) {
        this.context.userPanelSaving = true;
        this.context.saveCallback = function() {
          if (jQuery.progressMessage) {
            jQuery.progressMessage();
          }
          try {
            document.querySelector(this.options.formSaveSelector).submit();
          } catch (e) {
            if (jQuery.closeProgressMessage) {
              jQuery.closeProgressMessage();
            }
          }
        }.bind(this);
      }
    }

    let __currentUserId = currentUserId;
    if (this.options.currentUserId && typeof this.options.currentUserId !== 'string') {
      __currentUserId = "" + this.options.currentUserId;
    }
    this.context.currentUserId = __currentUserId;

    const __requester = new UserGroupRequester(this.options);

    this.refreshAll = function() {
      this.refreshCommons();
      const promises = [refreshUserData(), refreshGroupData()];
      return sp.promise.whenAllResolved(promises).then(function() {
        let simpleDetailsWhenRecipientTotalExceed = this.options.simpleDetailsWhenRecipientTotalExceed;
        if (simpleDetailsWhenRecipientTotalExceed && simpleDetailsWhenRecipientTotalExceed > 0) {
          let nbGroups = this.context.currentGroupIds.length;
          let nbUsers = this.context.currentUserIds.length;
          let parentElement = this.context.rootContainer;
          if ((nbGroups + nbUsers) > simpleDetailsWhenRecipientTotalExceed) {
            sp.element.querySelectorAll('.access-list', parentElement).forEach(function(list) {
              list.classList.add('hide');
            });
            let $simpleDetails = sp.element.querySelector('.simple-details', parentElement);
            $simpleDetails.classList.remove('hide');
            let simpleDetails = '';
            if (nbGroups > 0) {
              simpleDetails += nbGroups + ' ' + LABEL_GROUPS;
            }
            if (nbUsers > 0) {
              if (nbGroups > 0) {
                simpleDetails += ' ' + LABEL_AND + ' ';
              }
              simpleDetails += nbUsers + ' ' + LABEL_USERS;
            }
            $simpleDetails.innerHTML = simpleDetails;
          } else {
            sp.element.querySelectorAll('.access-list', parentElement).forEach(function(list) {
              list.classList.remove('hide');
            });
            let $simpleDetails = sp.element.querySelector('.simple-details', parentElement);
            $simpleDetails.classList.add('hide');
          }
        }
      }.bind(this));
    };

    this.removeAll = function() {
      this.context.currentUserIds = [];
      this.context.currentGroupIds = [];
      if (this.context.userPanelSaving) {
        this.refreshCommons();
      } else {
        this.refreshAll();
      }
    };

    const hasListChanged = function() {
      let i;
      let hasChanged = this.context.currentUserIds.length !== initialUserIds.length ||
          this.context.currentGroupIds.length !== initialGroupIds.length;
      if (!hasChanged && this.context.currentUserIds.length === initialUserIds.length) {
        for (i = 0; i < initialUserIds.length; i++) {
          hasChanged = this.context.currentUserIds.indexOf(initialUserIds[i]) < 0;
          if (hasChanged) {
            break;
          }
        }
      }
      if (!hasChanged && this.context.currentGroupIds.length === initialGroupIds.length) {
        for (i = 0; i < initialGroupIds.length; i++) {
          hasChanged = this.context.currentGroupIds.indexOf(initialGroupIds[i]) < 0;
          if (hasChanged) {
            break;
          }
        }
      }
      return hasChanged;
    }.bind(this);

    this.refreshCommons = function() {
      if (this.context.displayActionPanel) {
        if (!this.context.userPanelSaving) {
          if (hasListChanged()) {
            this.context.messagePanel.style.display = '';
          } else {
            this.context.messagePanel.style.display = 'none';
          }
        }
        if (this.context.currentUserIds.length || this.context.currentGroupIds.length) {
          this.context.clearButton.style.display = '';
        } else {
          this.context.clearButton.style.display = 'none';
        }
      }
      this.context.userSelectionInput.value = this.context.currentUserIds;
      this.context.groupSelectionInput.value = this.context.currentGroupIds;
    };

    const refreshUserData = function() {
      const userIds = this.context.currentUserIds;
      const processUsers = function(userProfiles) {
        this.context.userItems = [];
        userProfiles.forEach(function(userProfile) {
          this.context.userItems.push(new UserItem(userProfile, this));
        }.bind(this));
        __refreshContainer(this.context.userContainer, this.context.userItems);
        activateUserZoom();
      }.bind(this);
      if (userIds.length) {
        return __requester.getUsers({
          id : userIds
        }).then(function(users) {
          processUsers.call(this, users);
        }.bind(this));
      }
      processUsers.call(this, []);
      return sp.promise.resolveDirectlyWith();
    }.bind(this);

    const refreshGroupData = function() {
      const groupIds = this.context.currentGroupIds;
      const processGroups = function(groupProfiles) {
        this.context.groupItems = [];
        groupProfiles.forEach(function(groupProfile) {
          this.context.groupItems.push(new UserGroupItem(groupProfile, this));
        }.bind(this));
        __refreshContainer(this.context.groupContainer, this.context.groupItems);
      }.bind(this);
      if (groupIds.length) {
        return __requester.getUserGroups({
          ids : groupIds
        }).then(function(groups) {
          processGroups.call(this, groups);
        }.bind(this));
      }
      processGroups.call(this, []);
      return sp.promise.resolveDirectlyWith();
    }.bind(this);

    const processUserPanelChanges = function() {
      if (this.context.displayActionPanel) {
        if (this.context.userPanelSaving) {
          this.context.saveCallback.call(this);
        } else {
          this.refreshAll();
        }
      } else {
        this.refreshAll();
      }
    }.bind(this);

    whenSilverpeasReady(function() {
      this.context.rootContainer = document.querySelector("#" + this.options.rootContainerId);
      __decorateContainer(this);

      if (this.context.displayActionPanel) {
        this.context.clearButton.addEventListener('click', function(e) {
          e.preventDefault();
          e.stopPropagation();
          if (this.context.userPanelSaving) {
            jQuery.popup.confirm("<p>" + LABEL_CONFIRM_DELETE_ALL + "</p>", function() {
              this.removeAll();
              this.context.saveCallback.call(this);
              return true;
            }.bind(this));
          } else {
            this.removeAll();
          }
        }.bind(this));

        this.context.userPanelButton.addEventListener('click', function(e) {
          e.preventDefault();
          e.stopPropagation();
          __openUserPanel(this);
        }.bind(this));
      }

      if (!this.context.readOnly) {
        jQuery(this.context.userSelectionInput).on('change', function() {
          // For now, the user panel sets USER and GROUP identifiers and then trigger a change on
          // USER and an other one on GROUP.
          // So, only USER change event is listened...
          const userPanelValue = this.context.userSelectionInput.value;
          const groupPanelValue = this.context.groupSelectionInput.value;
          this.context.currentUserIds = userPanelValue ? userPanelValue.split(',') : [];
          this.context.currentGroupIds = groupPanelValue ? groupPanelValue.split(',') : [];
          processUserPanelChanges();
        }.bind(this));
      }

      this.refreshAll().then(function() {
        this.notifyReady();
      }.bind(this));
    }.bind(this));
  };
  
  function __openUserPanel(instance) {
    const params = {
      "includeRemovedUsers" : instance.options.includeRemovedUsers,
      "formName" : instance.context.userPanelFormName,
      "domainIdFilter" : instance.options.domainIdFilter,
      "resourceIdFilter" : instance.options.resourceIdFilter,
      "instanceId" : instance.options.componentIdFilter,
      "showDeactivated" : !instance.options.hideDeactivatedState
    };
    if (instance.options.roleFilter) {
      params["roles"] = instance.options.roleFilter.join(',');
      if (typeof instance.options.matchingAllRoles === 'boolean') {
        params["matchingAllRoles"] = instance.options.matchingAllRoles;
      }
    }
    let uri = instance.options.userPanelInitUrl;
    if (!uri) {
      uri = webContext  + '/RselectionPeasWrapper/jsp/open';
      params["selectedUserLimit"] = instance.options.userManualNotificationUserReceiverLimit;
      params["selectionMultiple"] = instance.options.multiple;
      params["formName"] = 'hotSetting';
      params["elementId"] = instance.options.userPanelId;
      params["selectable"] = instance.options.selectionType;
      params[instance.options.multiple ? "selectedUsers" : "selectedUser"] = instance.context.currentUserIds;
      params[instance.options.multiple ? "selectedGroups" : "selectedGroup"] = instance.context.currentGroupIds;
    } else {
      if (StringUtil.isNotDefined(instance.options.initUserPanelUserIdParamName)) {
        instance.options.initUserPanelUserIdParamName = "UserPanelCurrentUserIds";
      }
      if (StringUtil.isNotDefined(instance.options.initUserPanelGroupIdParamName)) {
        instance.options.initUserPanelGroupIdParamName = "UserPanelCurrentGroupIds";
      }
      params[instance.options.initUserPanelUserIdParamName] = instance.context.currentUserIds;
      params[instance.options.initUserPanelGroupIdParamName] = instance.context.currentGroupIds;
    }
    SP_openUserPanel({url : uri, params : params}, "userPanel");
  }

  function __createHiddenInputs(instance, rootContainer) {
    instance.context.userSelectionInput = instance.options.userSelectionInput;
    if (!instance.context.userSelectionInput) {
      const userInputId = instance.options.userPanelId + "-userIds";
      if (StringUtil.isNotDefined(instance.options.userInputName)) {
        instance.options.userInputName = userInputId;
      }
      const userPanelSelectionInput = document.createElement("input");
      userPanelSelectionInput.setAttribute("type", "hidden");
      userPanelSelectionInput.setAttribute("id", userInputId);
      userPanelSelectionInput.setAttribute("name", instance.options.userInputName);
      userPanelSelectionInput.value = 'initialized';
      rootContainer.appendChild(userPanelSelectionInput);
      instance.context.userSelectionInput = userPanelSelectionInput;
    }

    instance.context.groupSelectionInput = instance.options.groupSelectionInput;
    if (!instance.context.groupSelectionInput) {
      const groupInputId = instance.options.userPanelId + "-groupIds";
      if (StringUtil.isNotDefined(instance.options.groupInputName)) {
        instance.options.groupInputName = groupInputId;
      }
      const groupPanelSelectionInput = document.createElement("input");
      groupPanelSelectionInput.setAttribute("type", "hidden");
      groupPanelSelectionInput.setAttribute("id", groupInputId);
      groupPanelSelectionInput.setAttribute("name", instance.options.groupInputName);
      groupPanelSelectionInput.value = 'initialized';
      rootContainer.appendChild(groupPanelSelectionInput);
      instance.context.groupSelectionInput = groupPanelSelectionInput;
    }
  }

  function __refreshContainer(container, items) {
    container.innerHTML = '';
    items.forEach(function(item) {
      container.appendChild(item.getElement());
    });
  }

  /**
   * Render the container.
   * @param instance
   * @private
   */
  function __decorateContainer(instance) {
    const rootContainer = instance.context.rootContainer;
    if (instance.context.displayActionPanel) {
      rootContainer.classList.add("fields");
      const buttonPanel = document.createElement("div");
      buttonPanel.classList.add("buttonPanel");

      const userPanelButton = document.createElement("a");
      userPanelButton.classList.add("explorePanel");
      userPanelButton.setAttribute("href", "javascript:void(0)");
      userPanelButton.innerHTML =
          "<span>" + (instance.context.userPanelSaving ? LABEL_UPDATE : LABEL_SELECT) + "</span>";
      instance.context.userPanelButton = userPanelButton;

      const clearButton = document.createElement("a");
      clearButton.classList.add("emptyList");
      clearButton.setAttribute("href", "javascript:void(0)");
      clearButton.innerHTML =
          "<span>" + (instance.context.userPanelSaving ? LABEL_DELETE_ALL : LABEL_REMOVE_ALL) + "</span>";
      clearButton.style.display = 'none';
      instance.context.clearButton = clearButton;

      buttonPanel.appendChild(clearButton);
      buttonPanel.appendChild(userPanelButton);
      rootContainer.appendChild(buttonPanel);
    }

    const lists = document.createElement('div');
    lists.classList.add("field", "entireWidth");
    if (instance.context.displayActionPanel) {
      lists.classList.add("contentWithButtonPanel");
    }

    if (!instance.context.userPanelSaving) {
      const messagePanel = document.createElement('div');
      messagePanel.classList.add("inlineMessage");
      messagePanel.style.display = 'none';
      messagePanel.innerHTML = "<span>" + LABEL_LIST_CHANGED + "</span>";
      lists.appendChild(messagePanel);

      instance.context.messagePanel = messagePanel;
    }

    let simpleDetails = document.createElement('div');
    simpleDetails.classList.add('simple-details', 'hide');
    const groups = document.createElement('ul');
    groups.classList.add("access-list", "group");
    const users = document.createElement('ul');
    users.classList.add("access-list", "user");
    lists.appendChild(simpleDetails);
    lists.appendChild(groups);
    lists.appendChild(users);
    rootContainer.appendChild(lists);

    instance.context.groupContainer = groups;
    instance.context.userContainer = users;
    __createHiddenInputs(instance, rootContainer);
  }

  const Item = SilverpeasClass.extend({
    initialize : function(profile, instance) {
      this.profile = profile;
      this.instance = instance;
    },
    getId : function() {
      return this.profile.id;
    },
    getElement : function() {
      return this.element;
    },
    remove : function() {
      const index = this.handledIds.indexOf(this.profile.id);
      this.handledIds.splice(index, 1);
      this.element.style.opacity = 0.5;
      this.removed = true;
      this.instance.refreshCommons();
    },
    restore : function() {
      this.handledIds.push(this.profile.id);
      this.element.style.opacity = 1;
      this.removed = false;
      this.instance.refreshCommons();
    }
  });

  const UserItem = Item.extend({
    initialize : function(profile, instance) {
      this._super(profile, instance);
      this.element = __createUserElement(this);
      this.handledIds = this.instance.context.currentUserIds;
    },
    getFullName : function() {
      return this.profile.fullName;
    },
    getDomain : function() {
      return this.profile.domainName;
    },
    getUserCount : function() {
      return 1;
    }
  });

  const SelectUserItem = UserItem.extend({
    initialize : function(profile, instance) {
      this.isSelectElement = true;
      this._super(profile, instance);
    }
  });

  const UserGroupItem = Item.extend({
    initialize : function(profile, instance) {
      this._super(profile, instance);
      this.element = __createGroupElement(this);
      this.handledIds = this.instance.context.currentGroupIds;
    },
    getFullName : function() {
      return this.profile.name + " (" + this.getUserCount() + ")";
    },
    isSynchronized : function() {
      return this.profile['synchronized'];
    },
    getDomain : function() {
      return this.profile.domainName;
    },
    getUserCount : function() {
      return this.profile.userCount;
    }
  });

  const SelectUserGroupItem = UserGroupItem.extend({
    initialize : function(profile, instance) {
      this.isSelectElement = true;
      this._super(profile, instance);
    }
  });

  function __hideDomain(item) {
    return item.instance.options.domainIdFilter || DOMAIN_RESTRICTION || NB_DOMAINS < 3;
  }

  function __createUserElement(userItem) {
    const intoList = !userItem.isSelectElement;
    const item = document.createElement(intoList ? "li" : "div");
    item.classList.add("type-user");
    item.setAttribute("user-id", userItem.getId());
    const avatar = document.createElement("img");
    avatar.setAttribute("alt", "");
    if (userItem.instance.options.displayAvatar) {
      avatar.classList.add("user-avatar");
      avatar.setAttribute("src", userItem.profile.avatar);
    } else {
      if (userItem.profile.blockedState) {
        avatar.setAttribute("src", ICON_USER_BLOCKED);
      } else if (userItem.profile.expiredState) {
        avatar.setAttribute("src", ICON_USER_EXPIRED);
      } else if (userItem.profile.deactivatedState) {
        avatar.setAttribute("src", ICON_USER_DEACTIVATED);
      } else if (userItem.profile.removedState) {
        avatar.setAttribute("src", ICON_USER_REMOVED);
      } else {
        avatar.setAttribute("src", ICON_USER);
      }
    }
    item.appendChild(avatar);
    const $infos = document.createElement("div");
    if (intoList) {
      if (userItem.instance.context.currentUserId !== userItem.getId()) {
        $infos.classList.add("userToZoom");
        $infos.setAttribute("rel", userItem.getId());
      }
      $infos.innerHTML = userItem.getFullName();
    } else {
      const $main = document.createElement("div");
      $main.classList.add("main-info");
      $main.innerHTML = userItem.getFullName();
      $infos.appendChild($main);
      const $extra = document.createElement("div");
      $extra.classList.add("extra-info");
      $extra.innerHTML = userItem.getDomain();
      $infos.appendChild($extra);
      if (__hideDomain(userItem)) {
        item.classList.add("hidden-domain");
      }
    }
    ['blocked', 'expired', 'deactivated', 'removed'].forEach(function(state) {
      if (!!userItem.profile[state + 'State']) {
        item.classList.add(state + "-state");
        const $state = document.createElement("div");
        $state.classList.add("user-state");
        $state.innerHTML = '(' + sp.i18n.get('GML.user.account.state.' + state.toUpperCase() + '.short').toLowerCase() + ')';
        $infos.appendChild($state);
      }
    });
    item.appendChild($infos);
    if (intoList && !userItem.instance.context.readOnly) {
      item.appendChild(__createOperationFragment(userItem));
    }
    return item;
  }

  function __createGroupElement(groupItem) {
    const intoList = !groupItem.isSelectElement;
    const item = document.createElement(intoList ? "li" : "div");
    item.classList.add("type-group");
    item.setAttribute("group-id", groupItem.getId());
    const avatar = document.createElement("img");
    avatar.setAttribute("src", groupItem.isSynchronized() ? ICON_GROUP_SYNC : ICON_GROUP);
    avatar.setAttribute("alt", "");
    item.appendChild(avatar);
    const span = document.createElement("span");
    if (intoList) {
      span.innerHTML = groupItem.getFullName();
    } else {
      const mainInfo = document.createElement("div");
      mainInfo.classList.add("main-info");
      mainInfo.innerHTML = groupItem.getFullName();
      span.appendChild(mainInfo);
      const extraInfo = document.createElement("div");
      extraInfo.classList.add("extra-info");
      extraInfo.innerHTML = groupItem.getDomain();
      span.appendChild(extraInfo);
      if (__hideDomain(groupItem)) {
        item.classList.add("hidden-domain");
      }
    }
    item.appendChild(span);
    if (intoList && !groupItem.instance.context.readOnly) {
      item.appendChild(__createOperationFragment(groupItem));
    }
    return item;
  }

  function __createOperationFragment(item) {
    const label = (item.instance.context.userPanelSaving ? LABEL_DELETE : LABEL_REMOVE);
    const op = document.createElement("div");
    op.classList.add("operation");
    const a = document.createElement("a");
    a.setAttribute("href", "javascript:void(0)");
    a.setAttribute("title", label);
    const img = document.createElement("img");
    img.setAttribute("border", "0");
    img.setAttribute("title", label);
    img.setAttribute("alt", label);
    img.setAttribute("src", webContext + "/util/icons/delete.gif");
    a.appendChild(img);
    op.appendChild(a);

    a.addEventListener("click", function(e) {
      e.preventDefault();
      e.stopPropagation();
      new Promise(function(resolve) {
        if (!item.removed) {
          if (item.instance.context.userPanelSaving) {
            const confirmLabel = sp.i18n.get("GML.confirmation.delete", item.getFullName());
            jQuery.popup.confirm("<p>" + confirmLabel + "</p>", function() {
              item.remove();
              item.instance.context.saveCallback.call(this);
              return true;
            });
          } else {
            item.remove();
            resolve({
              label : LABEL_KEEP, imgSrc : webContext + "/util/icons/refresh.gif"
            });
          }
        } else {
          item.restore();
          resolve({
            label : (item.instance.context.userPanelSaving ? LABEL_DELETE : LABEL_REMOVE),
            imgSrc : webContext + "/util/icons/delete.gif"
          });
        }
      }).then(function(data) {
        img.setAttribute("src", data.imgSrc);
        img.setAttribute("title", data.label);
        img.setAttribute("alt", data.label);
        a.setAttribute("title", data.label);
      });
      return false;
    });

    return op;
  }

  function __convertToString(element) {
    if (typeof element === 'object' && element.length && typeof element[0] !== 'string') {
      const array = [];
      element.forEach(function(value) {
        array.push("" + value);
      });
      return array;
    }
    return element;
  }
  
  function __copyAndSort(data) {
    return [].concat(data).sort();
  }
})();
