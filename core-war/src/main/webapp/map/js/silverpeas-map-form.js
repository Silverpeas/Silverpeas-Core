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

/**
 * Silverpeas's map form provides a simple API as a centralized way to manage form data with
 * Silverpeas's Map APIs.
 */
(function () {

  /**
   * Class that MUST be extended by specifics in order to be taken into account into this
   * centralize way to display form data on a map.
   * @type {(function(): *)|*}
   */
  window.CartoMarkerComponentAdapter = SilverpeasClass.extend({
    /**
     * Constructor.
     * @param cmpInstance the linked {@link FormCartoMarkerComponent} instance.
     */
    initialize : function(cmpInstance) {
      this.cmpInstance = cmpInstance;
    },
    /**
     * Treatment performed before initializing the Map data.
     * @returns {Promise<*>|[Promise<*>]}
     */
    beforeAll : function() {
      return sp.promise.resolveDirectlyWith();
    },
    /**
     * Treatment performed before create all MapInfoPoint instances.
     */
    beforeAllInfoPoints : function() {
    },
    /**
     * Creates an InfoPoint.
     * @param formDataItem an item of formData.
     * @returns {MapInfoPoint}
     */
    createInfoPoint : function(formDataItem) {
      if (window) {
        throw new Error("createInfoPoint MUST be overridden")
      }
      // that represents an example, the function MUST be overridden
      return new MapInfoPoint();
    },
    /**
     * Treatment performed just after all MapInfoPoint instances have been created.
     */
    afterAllInfoPoints : function() {
    },
    /**
     * Gets the class to instantiate in order to manage the categories of couples of
     * {@link MapInfoPoint} and marker instances.
     * @returns {function(): MapInfoPointMarkerCategories}
     */
    getClassOfMapInfoPointMarkerCategories : function() {
      return MapInfoPointMarkerCategories;
    },

    /**
     * Call after all data have been computed and rendered about the map.
     */
    afterAll : function() {
      this.cmpInstance.mapApi.autoFit();
    }
  });

  /**
   * Object that handle information and redering of a point.
   * @type {(function(): *)|*}
   */
  window.MapInfoPoint = SilverpeasClass.extend({
    /**
     * Constructor.
     */
    initialize : function() {
      this.visible = true
    },
    /**
     * Gets the category name of the point.
     * @returns {MapInfoPointCategory}
     */
    getCategory : function() {
      return new MapInfoPointCategory('', '');
    },
    /**
     * Gets the name of the point.
     * @returns {string}
     */
    getName : function() {
      return 'N/A';
    },
    /**
     * Gets the MapLocation instance of the point.
     * @returns {MapLocation}
     */
    getLocation : function() {
    },
    /**
     * Indicates if it is a VueJs rendering.<br/>
     * If true, <silverpeas-info-point> is automatically inserted.<br/>
     * The implementation of a such component MUST use the mixin:
     * {@link FormCartoMarkerComponentMixin}
     * @returns {boolean}
     */
    isVueJsRendering : function() {
      return false;
    },
    /**
     * If {@link #isVueJsRendering()} returns false, this method is used for HTML rendering.
     * Promises the render of the form data item.
     * @returns {Promise<HTMLBaseElement>}
     */
    promiseRender : function() {
      return sp.promise.resolveDirectlyWith(document.createElement('div'));
    }
  });

  /**
   * represents a category of an {@link MapInfoPoint} instance.
   * @param id an identifier.
   * @param label a label.
   * @constructor
   */
  window.MapInfoPointCategory = function(id, label) {
    this.getId = function() {
      return id;
    };
    this.getLabel = function() {
      return label;
    };
  };

  /**
   * This class allows to handle the categories of couple of {@link MapInfoPoint} and marker
   * instances.
   * @type {(function(): *)|*}
   */
  window.MapInfoPointMarkerCategories = SilverpeasClass.extend({
    /**
     * Constructor taking as first parameter the instance of {@link FormCartoMarkerComponent} which
     * is instantiating this class.
     */
    initialize : function() {
      this.categories = [];
      this.categoryInfoPointMarkers = [];
    },
    /**
     * Gets the number of managed categories.
     * @returns {number}
     */
    size : function() {
      return this.categories.length;
    },
    /**
     * Browses over all registered category names
     */
    forEach : function() {
      Array.prototype.forEach.apply(this.categories, arguments);
    },
    /**
     * Gets the index of the category into the registered ones.
     * @param category a category name.
     * @returns {*}
     */
    getIndex : function(category) {
      let index = -1;
      this.categories.forEach(function(cat, idx) {
        if (cat.getId() === category.getId()) {
          index = idx;
        }
      });
      if (index < 0) {
        index = this.categories.length;
        this.categories.push(category);
        const infoPoints = [];
        this.categoryInfoPointMarkers.push(infoPoints);
        infoPoints.visible = true;
      }
      return index;
    },
    /**
     * Gets all couples of {@link MapInfoPoint} and marker instances registered with the given
     * category.
     * @param category a category name.
     * @returns {[MapInfoPoint]}
     */
    getInfoPointMarkers : function(category) {
      return this.categoryInfoPointMarkers[this.getIndex(category)];
    },
    /**
     * Gets the array of css to fill into class attribute of DOM element used for categories.
     * @param category a category name.
     * @returns {*[]} empty array means nothing to set and color is used.
     */
    getCssClassList : function(category) {
      return [];
    },
    /**
     * Gets the color attributed to a category.
     * @param category a category name.
     * @returns {*}
     */
    getColor : function(category) {
      const colors = MapSettings.getCategoryColors();
      return colors[this.getIndex(category) % colors.length];
    }
  });

  /**
   * Dedicated to manage a Map with markers on a Map from data fetch from Silverpeas's
   * Form Services.
   * @param cssSelector the CSS selector into which the Map MUST be initialized and displayed.
   * @param formData the form data coming from Silverpeas's Form Services.
   * @param adapterClass class of {@link CartoMarkerComponentAdapter} that will be instantiated.
   * @returns {Promise<Awaited<unknown>[]>} a promise that ensure the right loading of context.
   * @constructor
   */
  window.FormCartoMarkerComponent = function(cssSelector, formData, adapterClass) {
    this.rootEl = document.querySelector(cssSelector);
    this.userService = new MapUserService();
    this.adapter = new adapterClass(this);
    this.context = {
      currentUser : currentUser,
      formData : formData,
      markers : [],
      filters : {}
    };
    let promises = this.adapter.beforeAll();
    if (sp.promise.isOne(promises)) {
      promises = [promises];
    }
    this.sortFormDataByFieldName = function(fieldName) {
      this.context.formData.sort(function(a, b) {
        const aName = a.fields[fieldName].value.displayedValue.toLowerCase();
        const bName = b.fields[fieldName].value.displayedValue.toLowerCase();
        if (aName === bName) {
          return 0;
        } else {
          return aName < bName ? -1 : 1;
        }
      });
    };
    this.open = function(mapOptions) {
      return sp.promise.whenAllResolved(promises).then(function() {
        this.mapApi = new MapApi(this.rootEl);
        // Info points
        if (!this.context.infoPoints && this.context.formData) {
          this.adapter.beforeAllInfoPoints();
          this.context.infoPoints = __createInfoPointsFromFormData(this);
          this.adapter.afterAllInfoPoints();
        }
        this.categories = new (this.adapter.getClassOfMapInfoPointMarkerCategories())();
        this.mapApi.render(mapOptions).then(function() {
          // Markers
          __createMarkers(this).then(function() {
            // Filters
            __createFilters(this);
            // End of processing
            this.adapter.afterAll();
          }.bind(this));
        }.bind(this));
      }.bind(this));
    };
  };

  /**
   * Create data to display on the map from form data array.
   * @param cmpInstance the instance of {@link FormCartoMarkerComponent}.
   * @private
   */
  function __createInfoPointsFromFormData(cmpInstance) {
    const formData = cmpInstance.context.formData;
    if (Array.isArray(formData)) {
      return formData.map(function(item) {
        const mapInfoPoint = cmpInstance.adapter.createInfoPoint(item);
        mapInfoPoint.formDataItem = item;
        item.infoPoint = new (MapInfoPoint.extend({
          getCategory : function() {
            return mapInfoPoint.getCategory();
          },
          getName : function() {
            return mapInfoPoint.getName();
          },
          getLocation : function() {
            return mapInfoPoint.getLocation();
          },
          isVueJsRendering : function() {
            return mapInfoPoint.isVueJsRendering();
          }
        }))();
        return mapInfoPoint;
      });
    }
    return [];
  }

  /**
   * Create markers
   */
  function __createMarkers(cmpInstance) {
    const context = cmpInstance.context;
    if (Array.isArray(context.infoPoints)) {
      return sp.promise.whenAllResolved(context.infoPoints.map(function(point) {
        return __createMarker(cmpInstance, point);
      }));
    }
    return sp.promise.resolveDirectlyWith();
  }

  /**
   * Create a styled marker
   */
  function __createMarker(cmpInstance, infoPoint) {
    const categories = cmpInstance.categories;
    return infoPoint.getLocation().promiseLonLat().then(function(lonLat) {
      if (!lonLat) {
        return;
      }
      const category = infoPoint.getCategory();
      const infoPointMarkers = categories.getInfoPointMarkers(category);
      let contentPromise;
      const $baseContainer = document.createElement('div');
      $baseContainer.classList.add('info-window');
      categories.getCssClassList(category).forEach(function(css) {
        $baseContainer.classList.add(css);
      });
      if (infoPoint.isVueJsRendering()) {
        const $component = document.createElement('silverpeas-info-point');
        $component.setAttribute('v-bind:form-data-item', 'formDataItem')
        $baseContainer.appendChild($component);
        contentPromise = sp.promise.resolveDirectlyWith($baseContainer);
        __createVueJsInstance(cmpInstance, $component, {
          data : function() {
            return {
              formDataItem : infoPoint.formDataItem
            }
          }
        });
      } else {
        contentPromise = infoPoint.promiseRender().then(function($content) {
          $baseContainer.appendChild($content);
          return $baseContainer;
        });
      }
      const marker = cmpInstance.mapApi.addNewMarker({
        color : categories.getColor(category),
        classList : categories.getCssClassList(category),
        title : infoPoint.getName(),
        position : lonLat.asOlData(),
        visible : infoPoint.visible,
        contentPromise : contentPromise
      });
      infoPointMarkers.push({point : infoPoint, marker : marker});
      infoPointMarkers.visible = infoPoint.visible;
      return marker.promise;
    });
  }

  /**
   * Create filters
   */
  function __createFilters(cmpInstance) {
    if (cmpInstance.categories.size() > 0) {
      const $component = document.createElement('silverpeas-map-form-mapping-filters');
      cmpInstance.mapApi.getRightContainer().appendChild($component);
      __createVueJsInstance(cmpInstance, $component);
    }
  }

  function __createVueJsInstance(cmpInstance, $el, options) {
    return new Promise(function(resolve) {
      setTimeout(function() {
        resolve(new Vue(extendsObject(options, {
          el : $el,
          provide : function() {
            return {
              cmpInstance: cmpInstance
            }
          }
        })));
      }, 0);
    });
  }
})();