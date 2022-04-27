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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

//# sourceURL=/jobStartPagePeas/jsp/javascript/vuejs/admin-navigation.js

(function() {

  sp.i18n.load('org.silverpeas.mylinks.multilang.myLinksBundle');

  const adminNavigationAsyncComponentRepository = new VueJsAsyncComponentTemplateRepository(
      webContext + '/jobStartPagePeas/jsp/javascript/vuejs/admin-navigation-templates.jsp');

  Vue.component('admin-navigation',
      adminNavigationAsyncComponentRepository.get('root', {
        mixins : [VuejsApiMixin],
        data : function() {
          return {
            selectorSpaces : undefined,
            selectorCurrentSpace : undefined,
            tree : undefined
          };
        },
        created : function() {
          this.extendApiWith({
            injectData : function(data) {
              data = extendsObject({
                rootSpaces : undefined,
                currentRootSpace : undefined,
                spacePath : undefined,
                spaces : undefined,
                applications : undefined,
                currentApplication : undefined
              }, data);
              this.selectorSpaces = data.rootSpaces;
              this.selectorCurrentSpace = data.currentRootSpace;
              const currentTreeLevel = [];
              currentTreeLevel.spacePath = data.spacePath;
              currentTreeLevel.currentApplication = data.currentApplication;
              Array.prototype.push.apply(currentTreeLevel, data.spaces);
              Array.prototype.push.apply(currentTreeLevel, data.applications);
              this.tree = currentTreeLevel;
            }
          });
        },
        methods : {
          loadSelectedSpace : function(space) {
            if (!space) {
              jumpToSpace();
            } else if (space.type === 2) {
              jumpToSpace(space.id);
            } else {
              jumpToSubSpace(space.id);
            }
          },
          loadSelectedApplication : function(application) {
            jumpToComponent(application.id);
          }
        }
      }));

  Vue.component('admin-navigation-space-selector',
      adminNavigationAsyncComponentRepository.get('space-selector', {
        props : {
          spaces : {
            'type' : Array,
            'required' : true
          },
          currentSpace : {
            'type' : Object
          }
        },
        data : function() {
          return {
            selectedSpaceId : "none"
          }
        },
        mounted : function() {
          this.updateSelectedSpaceId();
        },
        methods : {
          selected : function() {
            const space = this.selectedSpaceId === 'none' ? undefined : this.spaces.filter(function(s) {
              return s.id === this.selectedSpaceId;
            }.bind(this))[0];
            this.$emit('space-select', space);
          },
          updateSelectedSpaceId : function() {
            this.selectedSpaceId = this.currentSpace ? this.currentSpace.id : "none";
          }
        },
        watch : {
          currentSpace : function() {
            this.updateSelectedSpaceId();
          }
        }
      }));

  Vue.component('admin-navigation-tree-level',
      adminNavigationAsyncComponentRepository.get('tree-level', {
        props : {
          tree : {
            'type' : Array,
            'required' : true
          }
        },
        computed : {
          spacePath : function() {
            return this.tree.spacePath || [];
          },
          spaces : function() {
            return this.tree.filter(function(item) {
              return item.type === 2 || item.type === 3;
            });
          },
          applications : function() {
            return this.tree.filter(function(item) {
              return !item.type;
            });
          },
          currentApplication : function() {
            return this.tree.currentApplication ? this.tree.currentApplication : {}
          }
        }
      }));

  Vue.component('admin-navigation-tree-space',
      adminNavigationAsyncComponentRepository.get('tree-space', {
        props : {
          space : {
            'type' : Object,
            'required' : true
          },
          level : {
            'type' : Number,
            'required' : true
          }
        },
        computed : {
          domId : function() {
            return 'navSpace' + this.space.id;
          }
        }
      }));

  Vue.component('admin-navigation-tree-application',
      adminNavigationAsyncComponentRepository.get('tree-application', {
        props : {
          application : {
            'type' : Object,
            'required' : true
          },
          level : {
            'type' : Number,
            'required' : true
          }
        },
        computed : {
          iconUrl : function() {
            return webContext + '/util/icons/component/' + this.application.name + 'Small.gif';
          }
        }
      }));
})();
