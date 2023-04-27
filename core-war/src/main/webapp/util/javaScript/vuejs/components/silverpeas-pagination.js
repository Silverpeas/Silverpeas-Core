/*
 * Copyright (C) 2000 - 2023 Silverpeas
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

  const commonAsyncComponentRepository = new VueJsAsyncComponentTemplateRepository(
      webContext + '/util/javaScript/vuejs/components/silverpeas-pagination-templates.jsp');

  const ICON_PATH = webContext + PaginationSettings.get("p.i.p");
  const NUMBER_OF_PAGES_AROUND = PaginationSettings.get('p.n.o.p.a');
  const INDEX_THRESHOLD = PaginationSettings.get('p.i.t');
  const NUMBER_PER_PAGE_THRESHOLD = PaginationSettings.get('p.n.p.p.t');
  const JUMPER_THRESHOLD = PaginationSettings.get('p.j.t');
  const NB_ITEM_PER_PAGE_LIST = PaginationSettings.get('p.n.i.p.p');
  const PAGINATION_ALL_THRESHOLD = PaginationSettings.get('p.p.a.t');

  /**
   * silverpeas-pagination is an HTML element that allows to paginate list of items in a Silverpeas
   * way by using the VueJS framework.
   *
   * Handled attributes:
   * @param items the list of items represented by an array (MUST not be null).
   * @param initialNbItemsPerPage (Optional) permits to override the default label displayed when
   *     no item exists.
   * @param displayAtTop (Optional) boolean at true to display pagination at top of list of items.
   *     False otherwise (by default).
   *
   * The child TAGs can access the slot "page" variable like the following exemple.
   *
   * @example
   *          <silverpeas-pagination v-bind:items="items" v-slot="{ page }">
   *            <silverpeas-list>
   *              <silverpeas-list-item v-for="item in page.items">{{item}}</silverpeas-list-item>
   *            <silverpeas-list>
   *          </silverpeas-pagination>
   */
  SpVue.component('silverpeas-pagination',
      commonAsyncComponentRepository.get('pagination', {
        props : {
          items : {
            'type' : Array,
            'required' : true
          },
          initialNbItemsPerPage : {
            'type' : Number,
            'default' : NUMBER_PER_PAGE_THRESHOLD
          },
          displayAtTop : {
            'type' : Boolean,
            'default' : false
          }
        },
        data : function() {
          return {
            barApi : undefined,
            nbItemsPerPage : -1,
            currentPage : 1
          }
        },
        methods : {
          reloadPagination : function() {
            Vue.nextTick(function() {
              if (this.displayPagination) {
                this.barApi.loadPagination();
              }
            }.bind((this)));
          },
          getNbItemsPerPage : function() {
            if (this.nbItemsPerPage === -1) {
              this.nbItemsPerPage = this.initialNbItemsPerPage;
            }
            return this.nbItemsPerPage;
          },
          setNbItemsPerPage : function(nbItemsPerPage) {
            this.nbItemsPerPage = nbItemsPerPage;
          },
          setCurrentPage : function(currentPage) {
            this.currentPage = currentPage;
            if (!this.displayAtTop) {
              Vue.nextTick(function() {
                sp.element.focus(this.$refs.top);
              }.bind(this));
            }
          }
        },
        computed : {
          nbPages : function() {
            const nbPages = this.items.length / this.getNbItemsPerPage();
            const rounded = nbPages.roundDown(0);
            return nbPages === rounded ? rounded : (rounded + 1);
          },
          displayPagination : function() {
            return this.items.length > INDEX_THRESHOLD;
          },
          page : function() {
            if (this.currentPage > this.nbPages) {
              this.currentPage = this.nbPages;
            }
            const page = {
              numPage : this.currentPage,
              nbPages : this.nbPages,
              nbAllItems : this.items.length,
              items : this.items
            };
            if (this.displayPagination) {
              const nbItemsPerPage = this.getNbItemsPerPage();
              const firstIndex = (this.currentPage - 1) * nbItemsPerPage;
              const lastIndex = Math.min(this.items.length, firstIndex + nbItemsPerPage) - 1;
              page.items = this.items.slice(firstIndex, lastIndex + 1);
              page.nbItemsPerPage = nbItemsPerPage;
              page.firstIndex = firstIndex;
              page.lastIndex = lastIndex;
            }
            return page;
          }
        },
        watch : {
          'nbPages' : function() {
            this.reloadPagination();
          }
        }
      }));

  SpVue.component('silverpeas-pagination-bar',
      commonAsyncComponentRepository.get('pagination-bar', {
        mixins : [VuejsApiMixin],
        emits : ['change', 'page-size-change'],
        props : {
          page : {
            'type' : Object,
            'required' : true
          }
        },
        data : function() {
          return {
            jumperEnabled : false
          }
        },
        mounted : function() {
          this.extendApiWith({
            loadPagination : this.loadPagination
          });
          this.loadPagination();
        },
        methods : {
          loadPagination : function() {
            const $container = jQuery(this.$refs.pagination);
            $container.html('');
            $container.smartpaginator({
              display: 'single',
              totalrecords: this.page.nbAllItems,
              recordsperpage: this.page.nbItemsPerPage,
              length: 6,
              initval : this.page.numPage,
              first: __createImage(ICON_PATH + '/arrows/arrowDoubleLeft.png', sp.i18n.get('g.p.f')),
              prev: __createImage(ICON_PATH + '/arrows/arrowLeft.png', sp.i18n.get('g.p.p')),
              next: __createImage(ICON_PATH + '/arrows/arrowRight.png', sp.i18n.get('g.p.n')),
              last: __createImage(ICON_PATH + '/arrows/arrowDoubleRight.png', sp.i18n.get('g.p.l')),
              theme: 'pageNav',
              onchange: function(pageNb) {
                this.$emit('change', pageNb)
                return true;
              }.bind(this)
            });
          },
          toggleGotoPage : function() {
            this.jumperEnabled = !this.jumperEnabled;
            if (this.jumperEnabled) {
              Vue.nextTick(function() {
                this.$refs.pageJumper.value = this.page.numPage;
                this.$refs.pageJumper.select();
                this.$refs.pageJumper.focus();
              }.bind(this));
            }
          },
          gotoPage : function() {
            const numPage = this.$refs.pageJumper.value;
            const $shortText = this.$refs.pagination.querySelector('.short input[type="text"]');
            const $shortButton = this.$refs.pagination.querySelector('.short input[type="button"]');
            $shortText.value = numPage;
            $shortButton.click();
            this.$refs.pageJumper.select();
          }
        },
        computed : {
          displayPageOn : function() {
            return NUMBER_OF_PAGES_AROUND < this.page.nbPages;
          },
          displayGotoPage : function() {
            return JUMPER_THRESHOLD < this.page.nbPages;
          },
          displayNumberPerPage : function() {
            return NUMBER_PER_PAGE_THRESHOLD < this.page.nbAllItems;
          },
          numberPerPageList : function() {
            const nbAllItems = this.page.nbAllItems;
            const list = NB_ITEM_PER_PAGE_LIST
                .filter(function(nbPerPage) {
                  return nbPerPage < nbAllItems;
                })
                .map(function(nbPerPage) {
                  return {
                    nb : nbPerPage,
                    label : nbPerPage,
                    title : sp.i18n.get('g.p.p.p', nbPerPage)
                  }
                });
            if (PAGINATION_ALL_THRESHOLD > nbAllItems) {
              list.push({
                nb : PAGINATION_ALL_THRESHOLD,
                label : sp.i18n.get('g.p.a'),
                title : sp.i18n.get('g.p.a.t', PAGINATION_ALL_THRESHOLD)
              });
            }
            return list;
          }
        }
      }));

  function __createImage(url, label) {
    const $img = document.createElement('img')
    $img.setAttribute("src", url);
    $img.setAttribute("alt", label);
    return $img;
  }
})();
