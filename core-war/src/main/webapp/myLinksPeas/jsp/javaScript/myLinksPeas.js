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
  window.MyLinksController = function() {
    const categories = sp.element.querySelectorAll('li.category').map(function($category) {
      return new function() {
        const $el = $category;
        const openCloseHandler = function(e) {
          e.preventDefault();
          e.stopPropagation();
          displayed = !displayed;
          this.refresh();
        }.bind(this);
        $el.querySelector('.categoryTitle a').addEventListener('click', openCloseHandler);
        this.hasLinks = function() {
          return !!$el.querySelector('.link-line');
        };
        let lastNbLinks = $el.querySelectorAll('.link-line').length;
        let displayed = false;
        this.isShown = function() {
          return displayed;
        };
        this.show = function() {
          displayed = true;
          this.refresh();
        };
        this.hide = function() {
          displayed = false;
          this.refresh();
        };
        this.refresh = function() {
          const nbLinks = $el.querySelectorAll('.link-line').length;
          if (lastNbLinks !== nbLinks) {
            displayed = true;
          }
          $el.classList.remove('category-only-one-hide');
          if (this.hasLinks()) {
            if ($el.classList.contains('category-hide')) {
              $el.classList.remove('category-hide');
            }
          } else if (!$el.classList.contains('category-hide')) {
            $el.classList.add('category-hide');
          }
          if (displayed) {
            $el.classList.remove('category-links-hide');
          } else if (!$el.classList.contains('category-links-hide')) {
            $el.classList.add('category-links-hide');
          }
          $el.classList.remove('initializing');
          lastNbLinks = nbLinks;
        };
        this.refreshOnlyOne = function() {
          if (!$el.classList.contains('category-only-one-hide')) {
            $el.classList.add('category-only-one-hide');
          }
          $el.classList.remove('category-hide');
        };
      };
    });
    this.refresh = function() {
      const shownCategories = categories.filter(function(category) {
        category.refresh();
        return category.isShown();
      });
      const withLinksCategories = categories.filter(function(category) {
        return category.hasLinks();
      });
      const withoutCategoryOnly = categories.length === 1;
      const allCategoriesHaveNoLink = withLinksCategories.length === 0;
      const onlyOneCategoryWithAtLeastOneLink = withLinksCategories.length === 1;
      const allCategoriesHidden = shownCategories.length === 0;
      if (withoutCategoryOnly || allCategoriesHaveNoLink) {
        categories[0].show();
        categories[0].refreshOnlyOne();
      } else if (onlyOneCategoryWithAtLeastOneLink) {
        withLinksCategories[0].show();
        if (categories[0].hasLinks()) {
          withLinksCategories[0].refreshOnlyOne();
        }
      } else if (allCategoriesHidden) {
        withLinksCategories[0].show();
      }
    }
    this.refresh();
  };
})();
