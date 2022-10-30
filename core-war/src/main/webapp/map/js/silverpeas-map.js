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
 * Silverpeas's map is built upon OpenLayers.
 * Its aim is to provide a simple API as a centralized way to display a map with some locations.
 */
(function () {
  if (!window.MapSettings) {
    window.MapSettings = {
      get : function() {
        return undefined;
      }
    };
  }
  Object.defineProperty(MapSettings, 'getCategoryColors', {
    enumerable : false, value : function() {
      const json = this.get('ip.c.c');
      return json ? JSON.parse(json) : [];
    }
  });
  Object.defineProperty(MapSettings, 'getXyzProviders', {
    enumerable : false, value : function() {
      const json = this.get('xyz.p');
      return json ? JSON.parse(json) : [];
    }
  });
  Object.defineProperty(MapSettings, 'getBmProviders', {
    enumerable : false, value : function() {
      const json = this.get('bm.p');
      return json ? JSON.parse(json) : undefined;
    }
  });

  const Projection = {
    fromLonLat : function(lon, lat) {
      const __lon = typeof lon === 'string' ? parseFloat(lon) : lon;
      const __lat = typeof lat === 'string' ? parseFloat(lat) : lat;
      const fromLonLat = ol.proj.fromLonLat([__lon, __lat]);
      fromLonLat.y = function() {
        return this[1];
      };
      fromLonLat.x = function() {
        return this[0];
      };
      fromLonLat.lat = function() {
        return __lat;
      };
      fromLonLat.lon = function() {
        return __lon;
      };
      return fromLonLat;
    },
    fromCoordinates : function(coordinates) {
      const toLonLat = ol.proj.toLonLat(coordinates);
      toLonLat.y = function() {
        return coordinates[1];
      };
      toLonLat.x = function() {
        return coordinates[0];
      };
      toLonLat.lat = function() {
        return this[1];
      };
      toLonLat.lon = function() {
        return this[0];
      };
      return toLonLat;
    }
  };

  /**
   * Map API is the main implementation from which a map engine MUST be initialized.
   * @constructor
   */
  window.MapApi = function(target) {
    applyEventDispatchingBehaviorOn(this);
    let __map, __layerApi, __clusterLayer;
    const __markers = [];
    const __markerLayer = new ol.layer.Vector({
      source : new ol.source.Vector(),
    });

    const $target = jQuery(target)[0];
    this.getMainContainer = function() {
      return $target;
    };
    this.getLayers = function() {
      return __layerApi;
    };
    /**
     * Adding a new marker into the map.<br/>
     * Must be called after {@link MapApi#render} method call.<br/>
     * Options is an object composed as following:
     * <pre>
     *   {
     *     color : 'white', // the color of the marker
     *     title : 'N/A', // the title of the marker
     *     position : [0, 0], // the position of the marker (provided by Utils.fromLonLat).
     *     positioning : 'bottom-left', // the positioning of the marker against the location.
     *     infoPositioning : 'top-center', // the positioning of the info popin against the marker.
     *     visible : true, // flag to handle visibility of the marker.
     *     contentPromise : undefined // a promise that permits to display the marker when it is
     *                                   ready to be.
     *   }
     * </pre>
     * @param options
     * @returns {MapMarkerApi}
     */
    this.addNewMarker = function(options) {
      const marker = new MapMarkerApi(__map, this, options);
      __markers.push(marker);
      const coordinates = marker.getCoordinates();
      const point = new ol.geom.Point(coordinates);
      const feature = new ol.Feature({
        geometry: point,
        name: marker.getLabel()
      });
      feature.setStyle(new ol.style.Style({}));
      feature.__marker = marker;
      point.__marker = marker;
      __markerLayer.getSource().addFeature(feature);
      return marker;
    };

    /**
     * Gets the registered markers.
     * @returns {MapMarkerApi[]}
     */
    this.getMarkers = function() {
      return __markers;
    };

    this.refresh = function() {
      if (__clusterLayer) {
        __clusterLayer.changed();
      }
    }

    const __getCoordinatesForViewFitting = function() {
      const allHidden = __markers.length > 0 && __markers.filter(function(marker) {return marker.isVisible();}).length === 0;
      return __markers.filter(function(marker) {
        return allHidden || marker.isVisible();
      }).map(function(marker) {
        return marker.getCoordinates();
      });
    };

    /**
     * This method tries to auto-fit the map according to the different elements set ont it.
     */
    this.autoFit = function() {
      const __coordinates = __getCoordinatesForViewFitting();
      if (__coordinates.length) {
        const boundingExtent = ol.extent.boundingExtent(__coordinates);
        __map.getView().fit(boundingExtent, {
          size : __map.getSize(),
          minResolution : __map.getView().getResolution(),
          padding : [50, 50, 50, 50]
        });
      }
    };

    const styleCache = {};
    const __createClusterLayer = function() {
      const mapOptions = this.getOptions();
      const clusters = new ol.layer.Vector({
        source : new ol.source.Cluster({
          source : __markerLayer.getSource(),
          distance : mapOptions.clusters.distance
        }),
        style : function(cluster) {
          const features = cluster.get('features');
          let size = 0;
          features.forEach(function(feature) {
            if (feature.__marker.isVisible()) {
              size++;
            }
          });
          features.forEach(function(feature) {
            feature.__marker.setAloneInCluster(size <= 1);
          });
          let style = styleCache[size];
          if (!style) {
            if (size <= 1) {
              styleCache[size] = new ol.style.Style({});
            } else {
              const image = new ol.style.Circle({
                radius : Math.min(20, 12 + size),
                fill : new ol.style.Fill({
                  color : mapOptions.clusters.color
                })
              });
              image.setOpacity(mapOptions.clusters.opacity);
              style = new ol.style.Style({
                image : image,
                text : new ol.style.Text({
                  text : [size.toString(), mapOptions.clusters.textFontStyle],
                  scale : mapOptions.clusters.textScale,
                  offsetX : mapOptions.clusters.textOffsetX,
                  offsetY : mapOptions.clusters.textOffsetY,
                  fill : new ol.style.Fill({
                    color : mapOptions.clusters.textColor,
                  }),
                }),
              });
              styleCache[size] = style;
            }
          }
          return style;
        }
      });
      setTimeout(function() {
        __map.on('click', function(e) {
          clusters.getFeatures(e.pixel).then(function(clickedFeatures) {
            if (clickedFeatures.length) {
              // Get clustered Coordinates
              const features = clickedFeatures[0].get('features');
              if (features.length > 1) {
                const extent = ol.extent.boundingExtent(features.map(function(feature) {
                  return feature.getGeometry().getCoordinates();
                }));
                __map.getView().fit(extent, {
                  size: __map.getSize(),
                  minResolution : 0.8,
                  duration: 1000,
                  padding: [200, 200, 200, 200]
                });
              }
            }
          });
        });
        // change mouse cursor when over marker
        __map.on('pointermove', function(e) {
          const pixel = __map.getEventPixel(e.originalEvent);
          const hit = __map.hasFeatureAtPixel(pixel);
          __map.getTarget().style.cursor = hit ? 'pointer' : '';
        });
      }, 0);
      return clusters;
    }.bind(this);

    const defaultLon = this.settings.get('v.c.d.lon');
    const defaultLat = this.settings.get('v.c.d.lat');
    const __defaultCoordinates = this.proj.fromLonLat(defaultLon, defaultLat);

    /**
     * After some parametrization, this method MUST be called to display the MAP before adding for
     * example some markers (withe {@link MapApi#addNewMarker}).<br/>
     * mapParams parameter is an object composed as following:
     * <pre>
     *   {
     *     minZoom : number representing the minimal zoom given to user (5 by default),
     *     maxZoom : number representing the maximal zoom given to user (20 by default),
     *     defaultZoom : number representing the default zoom when rendering the map (10 by default),
     *     center : ol.proj.fromLonLat([longitude, latitude]),
     *     clusters : {
     *        enabled : boolean to enable (true) or not the cluster feature (false, by default),
     *        distance : the distance in pixels between two markers considered as a cluster (40 by default),
     *        color : the background color of the cluster representation (#000 by default),
     *        opacity : the general opacity of the cluster representation (0.7 by default),
     *        textColor : the text color of the cluster representation (#fff by default),
     *        textOffsetX : a X offset in pixel of the text location (undefined by default),
     *        textOffsetY : same as X offset but with Y axis (2 by default),
     *        textScale : a float to scale the text into the cluster representation (1.75 by default),
     *        textFontStyle : the style of the font of the text (undefined by default)
     *     }
     *   }
     * </pre>
     * @param mapParams the map parameters.
     * @returns {Promise<unknown>}
     */
    this.render = function(mapParams) {
      return new Promise(function(resolve) {
        __layerApi = new MapLayerApi();
        // merging default options with given ones
        const options = extendsObject(false, {
          minZoom : this.settings.get('v.z.min'),
          maxZoom : this.settings.get('v.z.max'),
          defaultZoom : this.settings.get('v.z.d'),
          clusters : {},
          center : __defaultCoordinates
        }, mapParams);
        options.clusters = extendsObject({
          enabled : this.settings.get('c.d.e'),
          distance : this.settings.get('c.d.d'),
          color : this.settings.get('c.d.c'),
          opacity : this.settings.get('c.d.o'),
          textColor : this.settings.get('c.d.tc'),
          textOffsetX : this.settings.get('c.d.tox'),
          textOffsetY : this.settings.get('c.d.toy'),
          textScale : this.settings.get('c.d.ts'),
          textFontStyle : this.settings.get('c.d.tfs')
        }, options.clusters);
        this.getOptions = function() {
          return options;
        };
        // layers
        const layers = [];
        Array.prototype.push.apply(layers, __layerApi.__layers);
        __clusterLayer = __createClusterLayer();
        if (options.clusters.enabled) {
          layers.push(__clusterLayer);
        } else {
          layers.push(__markerLayer);
        }
        // initializing the map
        __map = new ol.Map({
          view : new ol.View({
            center : options.center,
            zoom : options.defaultZoom,
            minZoom : options.minZoom,
            maxZoom : options.maxZoom
          }),
          layers : layers,
          loadTilesWhileAnimating : true,
          loadTilesWhileInteracting : true,
          target : $target
        });
        __map.addControl(new ol.control.ZoomSlider());
        __createRightContainer(this);
        __createLayerButtons(this);
        this.dispatchEvent('rendered');
        resolve(this);
      }.bind(this));
    };
  };

  /**
   * Management of different layers that can be handled by Silverpeas's Map plugin.
   * @constructor
   */
  const MapLayerApi = function() {
    const __preload = 4;
    let __layerNames;
    const osm = new ol.layer.Tile({
      visible : true,
      preload : __preload,
      source : new ol.source.OSM()
    });
    this.__layers = [osm];
    __layerNames = ['Open Street Map'];
    const __xyzProviders = this.settings.getXyzProviders();
    if (__xyzProviders && __xyzProviders.length > 0) {
      function createXyzUrl(provider, description) {
        let url = provider.template.url;
        provider.template.descriptionVars.forEach(function(varName) {
          url = url.replace('{' + varName + '}', description[varName]);
        });
        return url;
      }
      __xyzProviders.forEach(function(xyzProvider) {
        xyzProvider.descriptions.forEach(function(xyzDescription) {
          const tile = new ol.layer.Tile({
            visible : false,
            preload : __preload,
            source : new ol.source.XYZ({
              url : createXyzUrl(xyzProvider, xyzDescription),
              attributions : xyzProvider.attribution.replace('{year}', new Date().getFullYear())
            })
          });
          __layerNames.push(xyzDescription.name);
          this.__layers.push(tile);
        }.bind(this));
      }.bind(this));
    }
    const __bmProvider = this.settings.getBmProviders();
    if (__bmProvider) {
      __bmProvider.descriptions.forEach(function(bmDescription) {
        const tile = new ol.layer.Tile({
          visible : false,
          preload : __preload,
          source : new ol.source.BingMaps({
            imagerySet : bmDescription['scheme'],
            key : __bmProvider['apiKey']
          })
        });
        __layerNames.push(bmDescription.name);
        this.__layers.push(tile);
      }.bind(this));
    }

    const __get = function(layerName) {
      const layerIndex = __layerNames.indexOf(layerName);
      return this.__layers[layerIndex];
    }.bind(this);
    this.getNames = function() {
      return __layerNames;
    };
    this.select = function(layerName) {
      let layerIndex = __layerNames.indexOf(layerName);
      if (layerIndex < 0) {
        layerIndex = 0;
      }
      for(let i = 0 ; i < this.__layers.length ; i++) {
        this.__layers[i].setVisible(i === layerIndex);
      }
      return __layerNames[layerIndex];
    };
    this.isSelected = function(layerName) {
      return __get(layerName).getVisible();
    };
  };

  const MapMarkerApi = function(__map, mapApi, options) {
    const __self = this;
    const __options = extendsObject(false, {
      color : 'white',
      classList : [],
      title : 'N/A',
      position : [0, 0],
      positioning : 'bottom-left',
      infoPositioning : 'top-center',
      visible : true,
      aloneInCluster : !mapApi.getOptions().clusters.enabled,
      contentPromise : undefined
    }, options);
    let __marker, __markerDetail, __markerDetailVm;
    this.getCoordinates = function() {
      return __options.position;
    };
    this.getLabel = function() {
      return __options.title;
    };
    this.getColor = function() {
      return __options.color;
    };
    this.getCssClassList = function() {
      return __options.classList;
    };
    this.isVisible = function() {
      return __options.visible;
    };
    this.show = function() {
      __options.visible = true;
      __refreshVisibility();
      mapApi.refresh();
    };
    this.setAloneInCluster = function(aloneInCluster) {
      __options.aloneInCluster = aloneInCluster;
      __refreshVisibility();
    }
    const __refreshVisibility = function() {
      const reallyDisplayed = !!__marker.getPosition();
      if (__options.visible) {
        if (!reallyDisplayed) {
          if (__options.aloneInCluster) {
            __marker.setPosition(__options.position);
          }
        } else if (!__options.aloneInCluster) {
          __marker.setPosition(undefined);
          this.hideDetail();
        }
      } else if (reallyDisplayed) {
        __marker.setPosition(undefined);
        this.hideDetail();
      }
    }.bind(this);
    this.hide = function() {
      __options.visible = false;
      __refreshVisibility();
      mapApi.refresh();
    };
    this.setCurrentClass = function() {
      const $previous = document.querySelector('.ol-top-marker.current');
      if ($previous) {
        $previous.classList.remove('current');
      }
      __markerDetailVm.$el.parentElement.classList.add('current');
    };
    this.showDetail = function() {
      if (__markerDetail) {
        __markerDetail.setElement(__markerDetailVm.$el);
        __markerDetail.setPosition(__options.position);
        this.setCurrentClass();
        __map.updateSize();
      }
    };
    this.hideDetail = function() {
      if (__markerDetail) {
        __markerDetail.setPosition(undefined);
      }
    };
    this.isDetailVisible = function() {
      return __markerDetail && !!__markerDetail.getPosition();
    };
    const promises = [];
    const defaultOlClass = 'ol-overlay-container ol-selectable ';
    if (__options.contentPromise) {
      __markerDetail = new ol.Overlay({
        positioning : __options.infoPositioning,
        autoPan : true,
        className: defaultOlClass + 'ol-top-marker'
      });
      __map.addOverlay(__markerDetail);
      promises.push(__options.contentPromise.then(function(content) {
        const $tipComponent = document.createElement('silverpeas-map-tip-marker');
        $tipComponent.setAttribute('v-on:api', 'api = $event');
        $tipComponent.setAttribute('v-bind:marker', 'marker');
        if (typeof content === 'string') {
          $tipComponent.innerHTML = content;
        } else {
          $tipComponent.appendChild(content);
        }
        return __createVueJsInstance(__map, $tipComponent, {
          data : function() {
            return {
              marker : __self,
              api : undefined
            };
          }
        }).then(function(vm) {
          __markerDetailVm = vm;
        }.bind(this));
      }.bind(this)));
    }
    // marker
    __marker = new ol.Overlay({
      positioning : __options.positioning,
      className: defaultOlClass + 'ol-marker'
    });
    __map.addOverlay(__marker);
    const $markerComponent = document.createElement('silverpeas-map-marker');
    $markerComponent.setAttribute('v-bind:marker', 'marker');
    promises.push(__createVueJsInstance(__map, $markerComponent, {
      data : function() {
        return {
          marker : __self
        };
      }
    }).then(function(vm) {
      __marker.setElement(vm.$el);
      __refreshVisibility();
    }.bind(this)));
    this.promise = sp.promise.whenAllResolved(promises);
  };

  /**
   * Map location object.
   * @param lonLat instance of MapLonLat or undefined.
   * @param address instance of MapAddress or undefined.
   * @constructor
   */
  window.MapLocation = function(lonLat, address) {
    this.promiseLonLat = function() {
      return lonLat ? sp.promise.resolveDirectlyWith(lonLat) : address.promiseLonLat();
    };
    this.promiseAddress = function() {
      return address ? sp.promise.resolveDirectlyWith(address) : lonLat.promiseAddress();
    };
  };

  [MapApi, MapLayerApi, MapMarkerApi, MapLocation, MapLonLat, MapAddress].forEach(function(api) {
    let __prototype = api.prototype || api;
    Object.defineProperty(__prototype, 'proj', {
      enumerable : false, value : Projection
    });
    Object.defineProperty(__prototype, 'settings', {
      enumerable : false, value : MapSettings
    });
  });

  /**
   * Create layer container
   */
  function __createRightContainer(mapInstance) {
    const $rightContainer = document.createElement('div');
    $rightContainer.classList.add('right-container');
    mapInstance.getMainContainer().appendChild($rightContainer);
    mapInstance.getRightContainer = function() {
      return $rightContainer;
    };
  }

  /**
   * Create layer container
   */
  function __createLayerButtons(mapInstance) {
    const $component = document.createElement('silverpeas-map-mapping-layers');
    mapInstance.getRightContainer().appendChild($component);
    __createVueJsInstance(mapInstance, $component);
  }

  function __createVueJsInstance(mapInstance, $el, options) {
    return new Promise(function(resolve) {
      setTimeout(function() {
        resolve(new Vue(extendsObject(options, {
          el : $el,
          provide : function() {
            return {
              mapInstance: mapInstance
            }
          }
        })));
      }, 0);
    });
  }
})();