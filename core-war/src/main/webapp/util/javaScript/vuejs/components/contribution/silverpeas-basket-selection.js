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

//# sourceURL=/util/javaScript/vuejs/components/contribution/silverpeas-basket-selection.js

(function() {

  const templateRepository = new VueJsAsyncComponentTemplateRepository(webContext +
      '/util/javaScript/vuejs/components/contribution/silverpeas-basket-selection-templates.jsp');

  Vue.component('silverpeas-basket-selection',
      templateRepository.get('basket-selection-main', {
        mixins : [VuejsApiMixin],
        provide : function() {
          return {
            basketService: this.service
          }
        },
        props : {
          'anchor' : {
            'type' : String,
            'default' : 'right'
          }
        },
        data : function() {
          return {
            service : new BasketService(),
            displayPopin : false,
            loadQueue : sp.promise.newQueue(),
            basketElements : undefined
          };
        },
        created : function() {
          this.extendApiWith({
            updateWith : function(basketElements) {
              this.updateWith(basketElements);
            },
            toggleView : function() {
              if (this.displayPopin) {
                this.close();
              } else {
                this.open();
              }
            },
            close : function() {
              this.close();
            }
          });
        },
        mounted : function() {
          this.loadBasketElements();
        },
        methods : {
          open : function() {
            this.loadBasketElements();
            this.displayPopin = true;
          },
          close : function() {
            this.displayPopin = false;
          },
          updateWith : function(basketElements) {
            this.basketElements = basketElements;
          },
          loadBasketElements : function() {
            this.loadQueue.push(function() {
              this.service.getBasketSelectionElements(BasketService.Filters.dataTransfer).then(function(basketElements) {
                this.updateWith(basketElements);
              }.bind(this));
            }.bind(this));
          },
          deleteBasketElement : function(basketElement) {
            this.loadQueue.push(function() {
              this.service.deleteEntry(basketElement);
            }.bind(this));
          }
        },
        computed : {
          displayed : function() {
            return this.basketElements && this.basketElements.length > 0;
          }
        }
      }));

  Vue.component('silverpeas-publication-basket-selector',
      templateRepository.get('publication-basket-selector-main', {
        inject : ['context'],
        mixins : [VuejsApiMixin, VuejsI18nTemplateMixin],
        provide : function() {
          return {
            basketService: this.service
          }
        },
        data : function() {
          return {
            service : new BasketService(),
            popinApi : undefined,
            basketElements : [],
            currentBasketElement : undefined
          };
        },
        created : function() {
          this.extendApiWith({
            open : function(options) {
              this.open(options);
            }
          });
        },
        methods : {
          selectBasketElement : function(basketElement) {
            this.currentBasketElement = basketElement;
          },
          open : function(options) {
            this.service.withBasketSelectionApi(function(api) {
              api.close();
            });
            this.currentBasketElement = undefined;
            this.basketElements = [];
            this.service.getBasketSelectionElements().then(function(basketElements) {
              this.basketElements = basketElements;
              this.popinApi.open({
                callback : function() {
                  if (this.currentBasketElement) {
                    options.select(this.currentBasketElement);
                  } else {
                    console.log('message', this.messages.noElementSelectedMsg)
                    SilverpeasError.add(this.messages.noElementSelectedMsg).show();
                    return false;
                  }
                }.bind(this)
              });
            }.bind(this));
          }
        }
      }));

  Vue.component('basket-element',
      templateRepository.get('basket-element', {
        props : {
          basketElement : {
            'type' : Object,
            'required' : true
          },
          readOnly : {
            'type' : Boolean,
            'default' : false
          }
        },
        computed : {
          displayDelete : function() {
            return !this.readOnly;
          }
        }
      }));
})();
