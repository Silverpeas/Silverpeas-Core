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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Silverpeas's map form provides a simple API as a centralized way to manage form data with
 * Silverpeas's Map APIs.
 */
(function () {

  /**
   * Common implementation for {@link MapApi} components which have to render
   * HTML with VueJs. Add it to a component or a vue instance by attribute mixins.
   *
   * @example:
   * Vue.component('my-component', {
   *   mixins : [CartoComponentMixin]
   * });
   */
  window.CartoComponentMixin = {
    inject : ['mapInstance']
  };

  /**
   * Common implementation for {@link MapApi} components which have to render
   * HTML with VueJs. Add it to a component or a vue instance by attribute mixins.
   *
   * @example:
   * Vue.component('my-component', {
   *   mixins : [CartoComponentMixin]
   * });
   */
  window.CartoComponentMarkerMixin = {
    mixins : [VuejsApiMixin, CartoComponentMixin],
    props : {
      marker : {
        'type' : Object,
        'required' : true
      }
    },
    created : function() {
      this.extendApiWith({
        toggleWindow : this.toggleWindow
      });
    },
    methods : {
      toggleWindow : function() {
        if (this.marker.isDetailVisible()) {
          this.marker.hideDetail();
        } else {
          this.marker.showDetail();
        }
      },
      setCurrentClass : function() {
        this.marker.setCurrentClass();
      }
    },
    computed : {
      cssClassList : function() {
        return this.marker.getCssClassList();
      }
    }
  };
})();