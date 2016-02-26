/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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

  var ICON_GROUP_SYNC = webContext + "/jobDomainPeas/jsp/icons/scheduledGroup.gif";
  var ICON_GROUP = webContext + "/util/icons/groupe.gif";
  var ICON_USER = webContext + "/util/icons/user.gif";
  var ICON_USER_BLOCKED = webContext + "/util/icons/user-blocked.png";
  var ICON_USER_EXPIRED = webContext + "/util/icons/user-expired.png";

  var LABEL_DELETE = UserGroupListBundle.get("GML.delete");
  var LABEL_DELETE_ALL = UserGroupListBundle.get("GML.deleteAll");
  var LABEL_CONFIRM_DELETE_ALL = UserGroupListBundle.get("GML.confirmation.deleteAll");
  var LABEL_REMOVE = UserGroupListBundle.get("GML.action.remove");
  var LABEL_REMOVE_ALL = UserGroupListBundle.get("GML.action.removeAll");
  var LABEL_KEEP = UserGroupListBundle.get("GML.action.keep");
  var LABEL_UPDATE = UserGroupListBundle.get("GML.modify");
  var LABEL_SELECT = UserGroupListBundle.get("GML.action.select");
  var LABEL_LIST_CHANGED = UserGroupListBundle.get("GML.list.changed.message");

  window.ListOfUsersAndGroups = function(options) {
    applyReadyBehaviorOn(this);

    this.options = extendsObject({
      userPanelId : '',
      currentUserId : '',
      rootContainerId : "user-group-list-root-container",
      initialUserIds : [],
      initialGroupIds : [],
      userPanelCallback : false,
      jsSaveCallback : false,
      formSaveSelector : '',
      displayUserZoom : true,
      displayAvatar : true
    }, options);

    var initialUserIds = __convertToString(this.options.initialUserIds);
    var initialGroupIds = __convertToString(this.options.initialGroupIds);

    this.context = {
      readOnly : (typeof this.options.userPanelCallback !== 'string' || !this.options.userPanelCallback),
      userPanelSaving : false,
      currentUserIds : [].concat(initialUserIds),
      currentGroupIds : [].concat(initialGroupIds)
    };

    if (!this.context.readOnly) {
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

    var currentUserId = this.options.currentUserId;
    if (typeof currentUserId !== 'string') {
      currentUserId = "" + currentUserId;
    }
    this.context.currentUserId = currentUserId;

    this.refreshAll = function() {
      this.refreshCommons();
      refreshUserData();
      refreshGroupData();
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

    var hasListChanged = function() {
      var i;
      var hasChanged = this.context.currentUserIds.length !== initialUserIds.length ||
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
      if (!this.context.readOnly) {
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

    var refreshUserData = function() {
      var userIds = this.context.currentUserIds;
      var processUsers = function(userProfiles) {
        this.context.userItems = [];
        userProfiles.forEach(function(userProfile) {
          this.context.userItems.push(new UserItem(userProfile, this));
        }.bind(this));
        refreshContainer(this.context.userContainer, this.context.userItems);
        activateUserZoom();
      }.bind(this);
      if (userIds.length) {
        User.get({
          id : userIds
        }).then(function(users) {
          processUsers.call(this, users);
        }.bind(this));
      } else {
        processUsers.call(this, []);
      }
    }.bind(this);

    var refreshGroupData = function() {
      var groupIds = this.context.currentGroupIds;
      var processGroups = function(groupProfiles) {
        this.context.groupItems = [];
        groupProfiles.forEach(function(groupProfile) {
          this.context.groupItems.push(new UserGroupItem(groupProfile, this));
        }.bind(this));
        refreshContainer(this.context.groupContainer, this.context.groupItems);
      }.bind(this);
      if (groupIds.length) {
        UserGroup.get({
          ids : groupIds
        }).then(function(groups) {
          processGroups.call(this, groups);
        }.bind(this));
      } else {
        processGroups.call(this, []);
      }
    }.bind(this);

    var refreshContainer = function(container, items) {
      container.innerHTML = '';
      items.forEach(function(item) {
        container.appendChild(item.getElement());
      });
    };

    var processUserPanelChanges = function() {
      this.context.userPanelChanges++;
      if (this.context.userPanelChanges > 1) {
        if (this.context.userPanelSaving) {
          this.context.saveCallback.call(this);
        } else {
          this.refreshAll();
        }
      }
    }.bind(this);

    whenSilverpeasReady(function() {
      this.context.rootContainer = document.querySelector("#" + this.options.rootContainerId);
      __decorateContainer(this);

      if (!this.context.readOnly) {
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
          this.context.userPanelChanges = 0;
          SP_openUserPanel({
            url : this.options.userPanelCallback,
            params : {
              "UserPanelCurrentUserIds" : this.context.currentUserIds,
              "UserPanelCurrentGroupIds" : this.context.currentGroupIds
            }
          }, "userPanel");
        }.bind(this));

        jQuery(this.context.userSelectionInput).on('change', function() {
          var userPanelValue = this.context.userSelectionInput.value;
          this.context.currentUserIds = userPanelValue ? userPanelValue.split(',') : [];
          processUserPanelChanges();
        }.bind(this));

        jQuery(this.context.groupSelectionInput).on('change', function() {
          var groupPanelValue = this.context.groupSelectionInput.value;
          this.context.currentGroupIds = groupPanelValue ? groupPanelValue.split(',') : [];
          processUserPanelChanges();
        }.bind(this));
      }

      this.refreshAll();
      this.notifyReady();
    }.bind(this));
  };

  /**
   * Render the container.
   * @param instance
   * @private
   */
  function __decorateContainer(instance) {
    var rootContainer = instance.context.rootContainer;
    rootContainer.classList.add("fields");
    if (!instance.context.readOnly) {
      var buttonPanel = document.createElement("div");
      buttonPanel.classList.add("buttonPanel");

      var userPanelButton = document.createElement("a");
      userPanelButton.classList.add("explorePanel");
      userPanelButton.setAttribute("href", "#");
      userPanelButton.innerHTML =
          "<span>" + (instance.context.userPanelSaving ? LABEL_UPDATE : LABEL_SELECT) + "</span>";
      instance.context.userPanelButton = userPanelButton;

      var clearButton = document.createElement("a");
      clearButton.classList.add("emptyList");
      clearButton.setAttribute("href", "#");
      clearButton.innerHTML =
          "<span>" + (instance.context.userPanelSaving ? LABEL_DELETE_ALL : LABEL_REMOVE_ALL) + "</span>";
      clearButton.style.display = 'none';
      instance.context.clearButton = clearButton;

      buttonPanel.appendChild(clearButton);
      buttonPanel.appendChild(userPanelButton);
      rootContainer.appendChild(buttonPanel);
    }

    var lists = document.createElement('div');
    lists.classList.add("field", "entireWidth");
    if (!instance.context.readOnly) {
      lists.classList.add("contentWithButtonPanel");
    }

    if (!instance.context.userPanelSaving) {
      var messagePanel = document.createElement('div');
      messagePanel.classList.add("inlineMessage");
      messagePanel.style.display = 'none';
      messagePanel.innerHTML = "<span>" + LABEL_LIST_CHANGED + "</span>";
      lists.appendChild(messagePanel);

      instance.context.messagePanel = messagePanel;
    }

    var groups = document.createElement('ul');
    groups.classList.add("access-list", "group");
    var users = document.createElement('ul');
    users.classList.add("access-list", "user");
    lists.appendChild(groups);
    lists.appendChild(users);
    rootContainer.appendChild(lists);

    instance.context.groupContainer = groups;
    instance.context.userContainer = users;

    var userPanelSelectionInput = document.createElement("input");
    userPanelSelectionInput.setAttribute("type", "hidden");
    userPanelSelectionInput.setAttribute("id", instance.options.userPanelId + "-userIds");
    userPanelSelectionInput.setAttribute("name",
        instance.options.userPanelId + "UserPanelCurrentUserIds");
    userPanelSelectionInput.value = 'initialized';
    var groupPanelSelectionInput = document.createElement("input");
    groupPanelSelectionInput.setAttribute("type", "hidden");
    groupPanelSelectionInput.setAttribute("id", instance.options.userPanelId + "-groupIds");
    groupPanelSelectionInput.setAttribute("name",
        instance.options.userPanelId + "UserPanelCurrentGroupIds");
    groupPanelSelectionInput.value = 'initialized';
    rootContainer.appendChild(userPanelSelectionInput);
    rootContainer.appendChild(groupPanelSelectionInput);

    instance.context.groupSelectionInput = groupPanelSelectionInput;
    instance.context.userSelectionInput = userPanelSelectionInput;
  }

  var Item = SilverpeasClass.extend({
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
      var index = this.handledIds.indexOf(this.profile.id);
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

  var UserItem = Item.extend({
    initialize : function(profile, instance) {
      this.parent.initialize.call(this, profile, instance);
      this.element = __createUserElement(this);
      this.handledIds = this.instance.context.currentUserIds;
    },
    getFullName : function() {
      return this.profile.fullName;
    }
  });

  var UserGroupItem = Item.extend({
    initialize : function(profile, instance) {
      this.parent.initialize.call(this, profile, instance);
      this.element = __createGroupElement(this);
      this.handledIds = this.instance.context.currentGroupIds;
    },
    getFullName : function() {
      return this.profile.name + " (" + this.profile.userCount + ")";
    },
    isSynchronized : function() {
      return this.profile.synchronized;
    }
  });

  function __createUserElement(userItem) {
    var li = document.createElement("li");
    li.classList.add("type-user");
    li.setAttribute("user-id", userItem.getId());
    var avatar = document.createElement("img");
    avatar.setAttribute("alt", "");
    if (userItem.instance.options.displayAvatar) {
      avatar.classList.add("user-avatar");
      avatar.setAttribute("src", userItem.profile.avatar);
    } else {
      if (userItem.profile.blockedState) {
        avatar.setAttribute("src", ICON_USER_BLOCKED);
      } else if (userItem.profile.expiredState) {
        avatar.setAttribute("src", ICON_USER_EXPIRED);
      } else {
        avatar.setAttribute("src", ICON_USER);
      }
    }
    li.appendChild(avatar);
    var span = document.createElement("span");
    if (userItem.instance.context.currentUserId !== userItem.getId()) {
      span.classList.add("userToZoom");
      span.setAttribute("rel", userItem.getId());
    }
    span.innerHTML = userItem.getFullName();
    li.appendChild(span);
    if (!userItem.instance.context.readOnly) {
      li.appendChild(__createOperationFragment(userItem));
    }
    return li;
  }

  function __createGroupElement(groupItem) {
    var li = document.createElement("li");
    li.classList.add("type-group");
    li.setAttribute("group-id", groupItem.getId());
    var avatar = document.createElement("img");
    avatar.setAttribute("src", groupItem.isSynchronized() ? ICON_GROUP_SYNC : ICON_GROUP);
    avatar.setAttribute("alt", "");
    li.appendChild(avatar);
    var span = document.createElement("span");
    span.innerHTML = groupItem.getFullName();
    li.appendChild(span);
    if (!groupItem.instance.context.readOnly) {
      li.appendChild(__createOperationFragment(groupItem));
    }
    return li;
  }

  function __createOperationFragment(item) {
    var label = (item.instance.context.userPanelSaving ? LABEL_DELETE : LABEL_REMOVE);
    var op = document.createElement("div");
    op.classList.add("operation");
    var a = document.createElement("a");
    a.setAttribute("href", "#");
    a.setAttribute("title", label);
    var img = document.createElement("img");
    img.setAttribute("border", "0");
    img.setAttribute("title", label);
    img.setAttribute("alt", label);
    img.setAttribute("src", "../../util/icons/delete.gif");
    a.appendChild(img);
    op.appendChild(a);

    a.addEventListener("click", function(e) {
      e.preventDefault();
      e.stopPropagation();
      new Promise(function(resolve) {
        if (!item.removed) {
          if (item.instance.context.userPanelSaving) {
            var confirmLabel = UserGroupListBundle.get("GML.confirmation.delete", item.getFullName());
            jQuery.popup.confirm("<p>" + confirmLabel + "</p>", function() {
              item.remove();
              item.instance.context.saveCallback.call(this);
              return true;
            });
          } else {
            item.remove();
            resolve({
              label : LABEL_KEEP, imgSrc : "../../util/icons/refresh.gif"
            });
          }
        } else {
          item.restore();
          resolve({
            label : (item.instance.context.userPanelSaving ? LABEL_DELETE : LABEL_REMOVE),
            imgSrc : "../../util/icons/delete.gif"
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
      var array = [];
      element.forEach(function(value) {
        array.push("" + value);
      });
      return array;
    }
    return element;
  }
})();
