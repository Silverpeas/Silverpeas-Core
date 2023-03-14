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

  let __counter = 0;
  function __newId() {
    try {
      return __counter;
    } finally {
      __counter = __counter + 1;
    }
  }

  /**
   * silverpeas-user-group-select handles a complex input of users and/or groups.
   */
  SpVue.component('silverpeas-user-group-select', {
    mixins : [VuejsFormInputMixin],
    template : '<div class="silverpeas-user-group-select" v-bind:id="rootContainerId"><span></span></div>',
    emits : ['selection-change'],
    props : {
      currentUserId : {
        'type' : String,
        'default' : ''
      },
      selectionType : {
        'type' : String,
        'default' : 'USER'
      },
      userPanelButtonLabel : {
        'type' : String,
        'default' : ''
      },
      removeButtonLabel : {
        'type' : String,
        'default' : ''
      },
      includeRemovedUsers : {
        'type' : Boolean,
        'default' : false
      },
      hideDeactivatedState : {
        'type' : Boolean,
        'default' : true
      },
      displayUserZoom : {
        'type' : Boolean,
        'default' : true
      },
      displayAvatar : {
        'type' : Boolean,
        'default' : true
      },
      navigationalBehavior : {
        'type' : Boolean,
        'default' : false
      },
      multiple : {
        'type' : Boolean,
        'default' : false
      },
      doNotSelectAutomaticallyOnDropDownOpen : {
        'type' : Boolean,
        'default' : false
      },
      noUserPanel : {
        'type' : Boolean,
        'default' : false
      },
      noSelectionClear : {
        'type' : Boolean,
        'default' : false
      },
      userManualNotificationUserReceiverLimit : {
        'type' : Boolean,
        'default' : false
      },
      componentIdFilter : {
        'type' : String,
        'default' : ''
      },
      initialQuery : {
        'type' : String,
        'default' : ''
      },
      queryInputName : {
        'type' : String,
        'default' : ''
      },
      userInputName : {
        'type' : String,
        'default' : ''
      },
      groupInputName : {
        'type' : String,
        'default' : ''
      },
      initialUserIds : {
        'default' : function() {
          return [];
        }
      },
      initialGroupIds : {
        'default' : function() {
          return [];
        }
      },
      domainFilter : {
        'default' : function() {
          return [];
        }
      },
      roleFilter : {
        'default' : function() {
          return [];
        }
      },
      matchingAllRoles : {
        'default' : false
      },
      groupFilter : {
        'default' : function() {
          return [];
        }
      }
    },
    data : function() {
      return {
        selectionApi : undefined,
        readyPromise : sp.promise.deferred().promise
      };
    },
    mounted : function() {
      this.initialize();
    },
    methods : {
      getInputElementName : function() {
        // this method is used by silverpeas-form-pane APIs
      },
      initialize : function() {
        this.readyPromise = new Promise(function(resolve) {
          this.$el.innerHTML = '';
          const domainIdFilter = Array.isArray(this.domainFilter)
              ? this.domainFilter
              : [this.domainFilter];
          const roleFilter = Array.isArray(this.roleFilter)
              ? this.roleFilter
              : [this.roleFilter];
          const groupFilter = Array.isArray(this.groupFilter)
              ? this.groupFilter
              : [this.groupFilter];
          const initialUserIds = Array.isArray(this.initialUserIds)
              ? this.initialUserIds
              : [this.initialUserIds];
          const initialGroupIds = Array.isArray(this.initialGroupIds)
              ? this.initialGroupIds
              : [this.initialGroupIds];
          const options = {
            rootContainerId : this.rootContainerId,
            includeRemovedUsers : this.includeRemovedUsers,
            hideDeactivatedState : this.hideDeactivatedState,
            domainIdFilter : domainIdFilter,
            componentIdFilter : this.componentIdFilter,
            roleFilter : roleFilter,
            matchingAllRoles : this.matchingAllRoles,
            groupFilter : groupFilter,
            initialQuery : this.initialQuery,
            navigationalBehavior : this.navigationalBehavior,
            doNotSelectAutomaticallyOnDropDownOpen : this.doNotSelectAutomaticallyOnDropDownOpen,
            noUserPanel : this.noUserPanel,
            noSelectionClear : this.noSelectionClear,
            queryInputName : this.queryInputName,
            userInputName : this.userInputName,
            groupInputName : this.groupInputName,
            currentUserId : this.currentUserIdentifier,
            initialUserIds : initialUserIds,
            initialGroupIds : initialGroupIds,
            displayUserZoom : this.displayUserZoom,
            displayAvatar : this.displayAvatar,
            multiple : this.multiple,
            selectionType : this.selectionType,
            readOnly : this.readOnly,
            mandatory : this.mandatory,
            userManualNotificationUserReceiverLimit : this.userManualNotificationUserReceiverLimit,
            onChange : function(selectionAPI) {
              this.$emit('selection-change', {
                selectedUserIds : selectionAPI.getSelectedUserIds(),
                selectedGroupIds : selectionAPI.getSelectedGroupIds()
              });
            }.bind(this)
          };
          if (StringUtil.isDefined(this.userPanelButtonLabel)) {
            options.userPanelButtonLabel = this.userPanelButtonLabel;
          }
          if (StringUtil.isDefined(this.removeButtonLabel)) {
            options.removeButtonLabel = this.removeButtonLabel;
          }
          this.selectionApi = new UserGroupSelect(options);
          this.selectionApi.ready(function() {
            this.extendApiWith({
              focus : function() {
                return this.readyPromise.then(function() {
                  this.$el.querySelector('input').focus();
                }.bind(this));
              },
              refresh : function() {
                return this.initialize();
              },
              updateFilterOptions : function() {
                const domainIdFilter = Array.isArray(this.domainFilter)
                    ? this.domainFilter
                    : [this.domainFilter];
                const roleFilter = Array.isArray(this.roleFilter)
                    ? this.roleFilter
                    : [this.roleFilter];
                const groupFilter = Array.isArray(this.groupFilter)
                    ? this.groupFilter
                    : [this.groupFilter];
                const options = {
                  hideDeactivatedState : this.hideDeactivatedState,
                  domainIdFilter : domainIdFilter,
                  componentIdFilter : this.componentIdFilter,
                  roleFilter : roleFilter,
                  matchingAllRoles : this.matchingAllRoles,
                  groupFilter : groupFilter
                };
                return this.selectionApi.updateFilterOptions(options);
              },
              validateFormInput : function() {
                const isNotValid = this.isMandatory
                    && this.selectionApi.getSelectedUserIds().length === 0
                    && this.selectionApi.getSelectedGroupIds().length === 0;
                if (isNotValid && this.rootFormApi) {
                  this.rootFormApi.errorMessage().add(
                      this.formatMessage(this.rootFormMessages.mandatory,
                          this.getLabelByForAttribute(this.id)));
                }
                return !isNotValid;
              }
            });
            resolve()
          }.bind(this));
        }.bind(this));
        return this.readyPromise;
      }
    },
    watch : {
      domainFilter : function() {
        this.api.updateFilterOptions && this.api.updateFilterOptions();
      },
      roleFilter : function() {
        this.api.updateFilterOptions && this.api.updateFilterOptions();
      },
      matchingAllRoles : function() {
        this.api.updateFilterOptions && this.api.updateFilterOptions();
      },
      groupFilter : function() {
        this.api.updateFilterOptions && this.api.updateFilterOptions();
      },
      hideDeactivatedState : function() {
        this.api.updateFilterOptions && this.api.updateFilterOptions();
      },
      componentIdFilter : function() {
        this.api.updateFilterOptions && this.api.updateFilterOptions();
      }
    },
    computed : {
      identifier : function() {
        return __newId()
      },
      rootContainerId : function() {
        return 'select-user-group-' + this.identifier
      },
      currentUserIdentifier : function() {
        return currentUser && !this.currentUserId ? currentUser.id : this.currentUserId;
      }
    }
  });
})();
