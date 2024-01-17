/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

  sp.i18n.load('org.silverpeas.mylinks.multilang.myLinksBundle');

  const myLinksPeasAsyncComponentRepository = new VueJsAsyncComponentTemplateRepository(
      webContext + '/myLinksPeas/jsp/javaScript/vuejs/mylinkspeas-templates.jsp');

  const Tools = new function() {
    this.NO_CATEGORY_ID = 'noCatId';
    this.getCategoryIdFromLink = function(link) {
      return String(this.parseCategoryId(link.categoryId));
    };
    this.parseCategoryId = function(value) {
      return value ? value : this.NO_CATEGORY_ID;
    };
  }

  SpVue.component('mylinkspeas-widget',
      myLinksPeasAsyncComponentRepository.get('widget', {
        props : {
          'links' : {
            'type' : Array,
            'required': true,
            'default' : []
          },
          'portlet' : {
            'type' : Boolean,
            'default' : false
          }
        },
        provide : function() {
          return {
            context: {
              portlet : this.context
            }
          }
        },
        data : function() {
          return {
            linksByCategory : undefined,
            singleCategoryLinks : undefined,
            context : {
              portlet : this.portlet
            }
          };
        },
        created : function() {
          const __linksByCategory = [];
          MyLinksService.getAllCategoriesOfCurrentUser().then(function(categories) {
            categories.unshift({
              catId : Tools.NO_CATEGORY_ID,
              name : sp.i18n.get('myLinks.withoutCategory')
            });
            categories.forEach(function(category, index) {
              category.catIdAsString = 'catId' + category.catId;
              categories[category.catIdAsString] = index;
            });
            this.links.forEach(function(link) {
              const categoryId = 'catId' + Tools.getCategoryIdFromLink(link);
              let categoryLinks = __linksByCategory[categoryId];
              if (!__linksByCategory[categoryId]) {
                categoryLinks = [];
                const category = categories[categories[categoryId]];
                __linksByCategory.push(category);
                __linksByCategory[categoryId] = categoryLinks;
              }
              categoryLinks.push(link);
            }.bind(this));
            if (__linksByCategory.length === 1 && __linksByCategory[0].catId === Tools.NO_CATEGORY_ID) {
              this.singleCategoryLinks = __linksByCategory[__linksByCategory[0].catIdAsString];
            } else {
              this.linksByCategory = __linksByCategory;
            }
          }.bind(this));
        },
        methods : {
          toggleBookmarks : function() {
            const $otherBookmarks = jQuery(".other-bookmark");
            $otherBookmarks.toggle("slow");
            const allBookmarks = jQuery("#user-favorit-home a");
            if ($otherBookmarks.css("display") === "none") {
              allBookmarks.removeClass("less");
            } else {
              allBookmarks.addClass("less");
            }
          }
        },
        computed : {
          moreLinks : function() {
            let moreLinks = false;
            if (!this.portlet) {
              if (this.singleCategoryLinks) {
                moreLinks = !this.context.portlet && this.singleCategoryLinks.length > 5;
              } else if (this.linksByCategory) {
                moreLinks = !this.context.portlet && this.linksByCategory.length > 5 || this.linksByCategory.filter(function(category) {
                  return this.linksByCategory[category.catIdAsString].length > 5;
                }.bind(this)).length > 0;
              }
            }
            return moreLinks;
          },
          css :function() {
            return {
              'with-category' : this.linksByCategory,
              'without-category' : !this.linksByCategory
            }
          }
        }
      }));

  SpVue.component('mylinkspeas-accordion',
      myLinksPeasAsyncComponentRepository.get('accordion', {
        props : {
          'linksByCategory' : {
            'type' : Array
          }
        }
      }));

  SpVue.component('mylinkspeas-accordion-category-links',
      myLinksPeasAsyncComponentRepository.get('accordion-category-links', {
        props : {
          'category' : {
            'type' : Object
          },
          'links' : {
            'type' : Array
          },
          'index' : {
            'type' : Number
          }
        },
        created : function() {
          this.open = this.index === 0;
        },
        data : function() {
          return {
            open : false
          }
        },
        methods : {
          doOpen : function() {
            this.open = !this.open;
          }
        },
        computed : {
          css : function() {
            return {
              'deployed' : this.open,
              'undeployed' : !this.open,
            }
          }
        }
      }));

  SpVue.component('mylinkspeas-accordion-link',
      myLinksPeasAsyncComponentRepository.get('accordion-link', {
        props : {
          'link' : {
            'type' : Object
          },
          'index' : {
            'type' : Number
          }
        },
        computed : {
          css : function() {
            return {
              'main-bookmark' : this.index <= 4,
              'other-bookmark' : this.index > 4
            };
          },
          internalLink : function() {
            return !this.link.url.toLowerCase().startsWith('http');
          },
          url : function() {
            return this.internalLink ? (webContext + this.link.url) : this.link.url;
          },
          linkCss : function() {
            return {
              'sp-link' : this.internalLink
            };
          },
          target : function() {
            return this.internalLink ? '' : '_blank';
          }
        }
      }));
})();
