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

  const EMPTY_STYLE = new ol.style.Style({});
  const DEFAULT_OL_CLASS = 'ol-overlay-container ol-selectable ';
  const INITIAL_VIEW_FIT_PADDING = [50, 50, 50, 50];

  /**
   * Map API is the main implementation from which a map engine MUST be initialized.
   * @constructor
   */
  window.MapApi = function(target) {
    applyEventDispatchingBehaviorOn(this);
    let __map, __layerApi, __clusterLayer;
    const __markers = [];
    const __markerLayer = new ol.layer.Vector({
      source : new ol.source.Vector()
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
      __rebuildMarkerFeatures();
      return marker;
    };

    let __rebuildMarkerFeaturesTimeout;
    const __rebuildMarkerFeatures = function() {
      clearTimeout(__rebuildMarkerFeaturesTimeout);
      __rebuildMarkerFeaturesTimeout = setTimeout(function() {
        const mapOptions = this.getOptions();
        __markerLayer.getSource().clear();
        const markersByCoords = {};
        __markers.forEach(function(marker) {
          const coordinates = marker.getCoordinates();
          const coordAttr = 'c' + coordinates[0] + ':' + coordinates[1];
          if (!markersByCoords[coordinates]) {
            markersByCoords[coordinates] = [];
          }
          markersByCoords[coordinates].push(marker);
        });

        function __applyMarkerGroupStyle(feature) {
          if (feature.__markerGroup.nbVisible() <= 1) {
            feature.setStyle(EMPTY_STYLE);
            return;
          }
          const image = new ol.style.Circle({
            radius : 17,
            fill : new ol.style.Fill({
              color : '#FFF'
            }),
            stroke : new ol.style.Stroke({
              color : '#000'
            })
          });
          image.setOpacity(mapOptions.groups.opacity);
          feature.setStyle(new ol.style.Style({
            image : image,
            text : new ol.style.Text({
              text : [feature.get('name'), mapOptions.groups.textFontStyle],
              scale : mapOptions.groups.textScale,
              offsetX : mapOptions.groups.textOffsetX,
              offsetY : mapOptions.groups.textOffsetY,
              fill : new ol.style.Fill({
                color : mapOptions.groups.textColor,
              }),
            }),
          }));
        }

        for (let coordAttr in markersByCoords) {
          const markers = markersByCoords[coordAttr];
          const marker = markers[0];
          const coordinates = marker.getCoordinates();
          const point = new ol.geom.Point(coordinates);
          let feature;
          if (markers.length === 1) {
            feature = new ol.Feature({
              geometry : point,
              name : marker.getLabel()
            });
            feature.setStyle(new ol.style.Style({}));
            feature.__marker = marker;
            point.__marker = marker;
          } else {
            const markerGroup = new MapMarkerGroupApi(__map, this, markers);
            feature = new ol.Feature({
              geometry : point,
              name : markerGroup.getLabel()
            });
            feature.__markerGroup = markerGroup;
            point.__markersGroup = markerGroup;
            __applyMarkerGroupStyle(feature);
            markerGroup.addEventListener('changed', function() {
              feature.set('name', markerGroup.getLabel());
              __applyMarkerGroupStyle(feature);
            });
          }
          feature.setId(coordAttr);
          __markerLayer.getSource().addFeature(feature);
        }
      }.bind(this), 0);
    }.bind(this);
    setTimeout(function() {
      __map.on('click', function(e) {
        ((__clusterLayer && __clusterLayer.__groupLayer) || __markerLayer).getFeatures(e.pixel).then(function(clickedFeatures) {
          if (clickedFeatures.length === 1) {
            const feature = clickedFeatures[0];
            const markerGroup = feature.__markerGroup;
            if (markerGroup) {
              if (markerGroup.isDetailsDisplayed()) {
                markerGroup.hideDetails();
                feature.getStyle().getText().setOffsetX(0);
              } else {
                markerGroup.showDetails();
                feature.getStyle().getText().setOffsetX(-6);
              }
              feature.changed();
            }
          }
        });
        this.dispatchEvent('click', e);
      }.bind(this));
      // change mouse cursor when over marker
      __map.on('pointermove', function(e) {
        const pixel = __map.getEventPixel(e.originalEvent);
        const hit = __map.hasFeatureAtPixel(pixel);
        __map.getTarget().style.cursor = hit ? 'pointer' : '';
      });
    }.bind(this), 0);

    /**
     * Removes all the registered markers.
     */
    this.clearMarkers = function() {
      __markers.forEach(function(marker) {
        marker.removeOverlays();
      });
      __markers.length = 0;
    };

    /**
     * Gets the registered markers.
     * @returns {MapMarkerApi[]}
     */
    this.getMarkers = function() {
      return __markers;
    };

    this.refresh = function() {
      __markerLayer.getSource().forEachFeature(function(feature) {
        if (feature.__markerGroup) {
          feature.__markerGroup.refresh();
        }
      });
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
        const fitOptions = {
          size : __map.getSize(),
          padding : INITIAL_VIEW_FIT_PADDING
        };
        if (this.getOptions().autoFitMaxZoom !== -1) {
          fitOptions.maxZoom = this.getOptions().autoFitMaxZoom;
        } else {
          fitOptions.minResolution = __map.getView().getResolution();
        }
        __map.getView().fit(boundingExtent, fitOptions);
      }
    };

    const __createClusterLayer = function() {
      const mapOptions = this.getOptions();
      let clustersVersion;
      const clusters = new ol.layer.Vector({
        source : new ol.source.Cluster({
          source : __markerLayer.getSource(),
          distance : mapOptions.clusters.distance
        }),
        style : function(cluster) {
          if (clustersVersion !== clusters.getRevision()) {
            __clusterLayer.__groupLayer.getSource().clear();
            clustersVersion = clusters.getRevision();
          }
          const features = cluster.get('features');
          let sizeForThreshold = 0;
          let size = 0;
          const items = [];
          features.forEach(function(feature) {
            if (feature.__marker) {
              items.push({feature : feature, markerOrGroup : feature.__marker});
            } else {
              items.push({feature : feature, markerOrGroup : feature.__markerGroup});
            }
          });
          items.forEach(function(item) {
            const markerOrGroup = item.markerOrGroup;
            if (markerOrGroup.isVisible()) {
              sizeForThreshold++;
              if (typeof markerOrGroup.nbVisible === 'function') {
                size = size + markerOrGroup.nbVisible();
              } else {
                size++;
              }
            }
          });

          function __performMarkerDisplay(nbThreshold) {
            items.forEach(function(item) {
              const markerOrGroup = item.markerOrGroup;
              markerOrGroup.setAloneInCluster(sizeForThreshold <= nbThreshold);
              const groupLayerSource = clusters.__groupLayer.getSource();
              if (markerOrGroup.isDisplayed()) {
                if (!groupLayerSource.getFeatureById(item.feature.getId())) {
                  groupLayerSource.addFeature(item.feature);
                }
              } else {
                if (markerOrGroup instanceof MapMarkerGroupApi) {
                  markerOrGroup.hideDetails();
                }
                groupLayerSource.removeFeature(item.feature);
              }
            });
          }

          if (__map.getView().getResolution() < mapOptions.clusters.resolutionThreshold) {
            __performMarkerDisplay(Number.MAX_SAFE_INTEGER);
            return EMPTY_STYLE;
          }

          const nbThreshold = mapOptions.clusters.nbThreshold;
          __performMarkerDisplay(nbThreshold);
          let style;
          if (sizeForThreshold <= nbThreshold) {
            style = EMPTY_STYLE;
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
          }
          return style;
        }
      });
      clusters.__groupLayer = new ol.layer.Vector({
        source : new ol.source.Vector()
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
                const $contentContainer = window.spLayout ? spLayout.getBody().getContainer() : document.body;
                const mapRatio = (__map.getTargetElement().offsetHeight / $contentContainer.offsetHeight).roundHalfUp(2);
                const padding = parseFloat(this.settings.get('c.d.z.p')) * mapRatio;
                __map.getView().fit(extent, {
                  size: __map.getSize(),
                  minResolution : 0.8,
                  duration: 1000,
                  padding: [padding, padding, padding, padding]
                });
              }
            }
          }.bind(this));
        }.bind(this));
      }.bind(this), 0);
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
     *     groups : {
     *        color : the background color of the group representation (#FFF by default),
     *        opacity : the general opacity of the group representation (0.7 by default),
     *        textColor : the text color of the group representation (#000 by default),
     *        textOffsetX : a X offset in pixel of the text location (undefined by default),
     *        textOffsetY : same as X offset but with Y axis (undefined by default),
     *        textScale : a float to scale the text into the group representation (1.4 by default),
     *        textFontStyle : the style of the font of the text (undefined by default)
     *     },
     *     clusters : {
     *        enabled : boolean to enable (true) or not the cluster feature (false, by default),
     *        distance : the distance in pixels between two markers considered as a cluster (40 by default),
     *        resolutionThreshold : resolution under which the clustering is over (not set by default),
     *        nbThreshold : number of features under which the clustering is over (not set by default),
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
          autoFitMaxZoom : this.settings.get('v.f.a.z.max'),
          minZoom : this.settings.get('v.z.min'),
          maxZoom : this.settings.get('v.z.max'),
          defaultZoom : this.settings.get('v.z.d'),
          groups : {},
          clusters : {},
          center : __defaultCoordinates
        }, mapParams);
        if (options.autoFitMaxZoom === '') {
          options.autoFitMaxZoom = this.settings.get('v.f.a.z.max');
        }
        if (options.center instanceof MapLonLat) {
          options.center = mapParams.center.asOlData();
        }
        options.groups = extendsObject({
          color : this.settings.get('g.d.c'),
          opacity : this.settings.get('g.d.o'),
          textColor : this.settings.get('g.d.tc'),
          textOffsetX : this.settings.get('g.d.tox'),
          textOffsetY : this.settings.get('g.d.toy'),
          textScale : this.settings.get('g.d.ts'),
          textFontStyle : this.settings.get('g.d.tfs')
        }, options.groups);
        options.clusters = extendsObject({
          enabled : this.settings.get('c.d.e'),
          distance : this.settings.get('c.d.d'),
          resolutionThreshold : this.settings.get('c.d.r.t'),
          nbThreshold : this.settings.get('c.d.n.t'),
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
        if (options.clusters.enabled) {
          __clusterLayer = __createClusterLayer();
          layers.push(__clusterLayer);
          layers.push(__clusterLayer.__groupLayer);
        } else {
          layers.push(__markerLayer);
        }
        // initializing the map
        __map = new ol.Map({
          controls : ol.control.defaults.defaults({
            zoomOptions : {
              zoomInTipLabel : sp.i18n.get("m.z.i"),
              zoomOutTipLabel : sp.i18n.get("m.z.o")
            }
          }),
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
        __createRightContainer(this);
        __createLayerButtons(this);
        this.dispatchEvent('rendered');
        // controls
        __map.addControl(new ol.control.ZoomSlider());
        const fullScreenCtrl = new ol.control.FullScreen({
          source : __map.getTargetElement().id,
          tipLabel : sp.i18n.get("m.f.m")
        });
        fullScreenCtrl.on('enterfullscreen', function() {
          window.addEventListener('beforeunload', spFscreen.exitFullscreen);
        });
        fullScreenCtrl.on('leavefullscreen', function() {
          window.removeEventListener('beforeunload', spFscreen.exitFullscreen);
        });
        __map.addControl(fullScreenCtrl);
        resolve(this);
      }.bind(this));
    };
    this.enableInitialViewControl = function() {
      const __coordinates = __getCoordinatesForViewFitting();
      if (__coordinates.length) {
        const boundingExtent = ol.extent.boundingExtent(__coordinates);
        const zoomToExtent = new ol.control.ZoomToExtent({
          label : '\u2237',
          tipLabel : sp.i18n.get('m.v.i'),
          extent : boundingExtent
        });
        zoomToExtent.handleZoomToExtent = function() {
          __map.getView().fitInternal(ol.geom.Polygon.fromExtent(boundingExtent), {
            padding : INITIAL_VIEW_FIT_PADDING
          });
        }
        __map.addControl(zoomToExtent);
      }
    }
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

  const MapMarkerGroupApi = function(__map, mapApi, markers) {
    applyEventDispatchingBehaviorOn(this);
    const __self = this;
    this.getCoordinates = function() {
      return markers[0].getCoordinates();
    };
    this.getLabel = function() {
      return this.nbVisible().toString();
    };
    this.getMarkers = function() {
      return markers;
    };
    this.nbDisplayed = function() {
      return markers.filter(function(marker) {
        return marker.isDisplayed();
      }).length;
    };
    this.nbVisible = function() {
      return markers.filter(function(marker) {
        return marker.isVisible();
      }).length;
    };
    this.isVisible = function() {
      return this.nbVisible() > 0;
    };
    this.isDisplayed = function() {
      return this.nbDisplayed() > 0;
    };
    this.oneItemOnlyDisplayed = function() {
      return this.nbDisplayed() === 1;
    };
    this.setAloneInCluster = function(aloneInCluster) {
      markers.forEach(function(marker) {
        return marker.setAloneInCluster(aloneInCluster);
      });
    };
    // marker monitoring
    markers.forEach(function(marker) {
      marker.addEventListener('detailVisibilityChanged', function() {
        if (marker.isDetailVisible()) {
          markers.forEach(function(m) {
            if (m !== marker) {
              m.hideDetail();
            }
          });
        }
      });
    })
    // refresh
    let showDetailsForced = false;
    this.refresh = function() {
      markers.forEach(function(marker, index) {
        const markerOverlay = overlays[index];
        if (!marker.isVisible()) {
          markerOverlay.classList.add("hide");
        } else {
          markerOverlay.classList.remove("hide");
        }
      });
      const nbVisible = this.nbVisible();
      if (nbVisible === 1) {
        showDetailsForced = showDetailsForced || !this.isDetailsDisplayed();
        this.showDetails(true);
      } else if (nbVisible > 1 && showDetailsForced) {
        showDetailsForced = false;
        this.hideDetails(true);
      }
      this.dispatchEvent('changed');
    };
    // overlay management
    const __overlay = new ol.Overlay({
      positioning : markers[0].getMarkerOverlay().getPositioning(),
      autoPan : true,
      className : DEFAULT_OL_CLASS + 'ol-marker-group',
    });
    __map.addOverlay(__overlay);
    const overlays = [];
    const $overlay = document.createElement('div');
    $overlay.classList.add('map-marker-group');
    __overlay.setElement($overlay);
    markers.forEach(function(marker) {
      overlays.push(marker.getMarkerOverlay().getElement());
      $overlay.appendChild(overlays[overlays.length - 1]);
    });
    this.isDetailsDisplayed = function() {
      return !!__overlay.getPosition();
    };
    this.hideDetails = function(noEvent) {
      if (this.isDetailsDisplayed()) {
        __overlay.setPosition(null);
        if (!noEvent) {
          this.dispatchEvent('changed');
        }
      }
    };
    this.showDetails = function(noEvent) {
      if (!this.isDetailsDisplayed()) {
        __overlay.setPosition(this.getCoordinates());
        if (!noEvent) {
          this.dispatchEvent('changed');
        }
      }
    };
  };

  const MapMarkerApi = function(__map, mapApi, options) {
    applyEventDispatchingBehaviorOn(this);
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
    if (__options.position instanceof MapLonLat) {
      __options.position = __options.position.asOlData();
    }
    let __marker, __markerDetail, __$markerDetail;
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
    this.isDisplayed = function() {
      return !!__marker.getPosition();
    };
    this.isVisible = function() {
      return __options.visible;
    };
    this.show = function() {
      __options.visible = true;
      __refreshVisibility();
      mapApi.refresh();
    };
    this.removeOverlays = function() {
      __map.removeOverlay(__marker);
      if (__markerDetail) {
        __map.removeOverlay(__markerDetail);
      }
    };
    this.setAloneInCluster = function(aloneInCluster) {
      __options.aloneInCluster = aloneInCluster;
      __refreshVisibility();
    }
    const __refreshVisibility = function() {
      const reallyDisplayed = this.isDisplayed();
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
      __$markerDetail.parentElement.classList.add('current');
    };
    this.showDetail = function() {
      if (__markerDetail && !this.isDetailVisible()) {
        __markerDetail.setElement(__$markerDetail);
        __markerDetail.setPosition(__options.position);
        this.setCurrentClass();
        __map.updateSize();
        this.dispatchEvent('detailVisibilityChanged');
      }
    };
    this.hideDetail = function() {
      if (this.isDetailVisible()) {
        __markerDetail.setPosition(undefined);
        this.dispatchEvent('detailVisibilityChanged');
      }
    };
    this.isDetailVisible = function() {
      return __markerDetail && !!__markerDetail.getPosition();
    };
    const promises = [];
    if (__options.contentPromise) {
      __markerDetail = new ol.Overlay({
        positioning : __options.infoPositioning,
        autoPan : true,
        className: DEFAULT_OL_CLASS + 'ol-top-marker'
      });
      __map.addOverlay(__markerDetail);
      promises.push(__options.contentPromise.then(function(content) {
        __$markerDetail = document.createElement('div');
        const $tipComponent = document.createElement('silverpeas-map-tip-marker');
        $tipComponent.setAttribute('v-on:api', 'api = $event');
        $tipComponent.setAttribute('v-bind:marker', 'marker');
        if (typeof content === 'string') {
          $tipComponent.innerHTML = content;
        } else if (typeof content.vuejs_def === 'object') {
          const $div = document.createElement('div');
          if (typeof content.vuejs_def.template === 'string') {
            $div.innerHTML = content.vuejs_def.template;
          } else {
            $div.appendChild(content.vuejs_def.template);
          }
          $div.childNodes.forEach(function(child) {
            $tipComponent.appendChild(child);
          })
        } else {
          $tipComponent.appendChild(content);
        }
        __$markerDetail.appendChild($tipComponent);
        return __createVueJsInstance(__map, __$markerDetail, {
          data : function() {
            const data = {
              marker : __self,
              api : undefined
            };
            if (typeof content.vuejs_def === 'object') {
              return extendsObject(data, content.vuejs_def.data());
            }
            return data;
          }
        });
      }.bind(this)));
    }
    // marker
    __marker = new ol.Overlay({
      positioning : __options.positioning,
      className: DEFAULT_OL_CLASS + 'ol-marker'
    });
    __map.addOverlay(__marker);
    const $vuejsDock = document.createElement('div');
    promises.push(__createVueJsInstance(__map, $vuejsDock, {
      data : function() {
        return {
          marker : __self
        };
      },
      template : '<silverpeas-map-marker v-bind:marker="marker"></silverpeas-map-marker>'
    }).then(function() {
      __marker.setElement($vuejsDock);
      __refreshVisibility();
    }.bind(this)));
    this.getMarkerOverlay = function() {
      return __marker;
    }
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
    const $vueJsDoc = document.createElement('div');
    mapInstance.getRightContainer().appendChild($vueJsDoc);
    __createVueJsInstance(mapInstance, $vueJsDoc, {
      template : '<silverpeas-map-mapping-layers></silverpeas-map-mapping-layers>'
    });
  }

  function __createVueJsInstance(mapInstance, $el, options) {
    return new Promise(function(resolve) {
      setTimeout(function() {
        SpVue.createApp(extendsObject(options, {
          provide : {
            mapInstance: mapInstance
          }
        })).mount($el);
        resolve();
      }, 0);
    });
  }
})();