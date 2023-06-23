
(function() {

  /**
   * Default {@link CartoTargetPointComponentAdapter} implementation.
   * The override if any MUST be indicated to 'component-adapter' attribute.
   */
  window.CartoTargetPointComponentAdapter = SilverpeasClass.extend({
    /**
     * Creates an {@link MapInfoPoint} from a  {@link MapLonLat}.
     * @param mapLonLat an instance of {@link MapLonLat}.
     * @returns {Promise<MapInfoPoint>}
     */
    createInfoPoint : function(mapLonLat) {
      const _location = new MapLocation(mapLonLat, undefined);
      return new (MapInfoPoint.extend({
        getName : function() {
          return '';
        },
        getLocation : function() {
          return _location;
        },
        promiseRender : function() {
          return sp.promise.resolveDirectlyWith({
            vuejs_def : {
              data : function() {
                return {
                  mapLonLat : mapLonLat
                }
              },
              template : '<silverpeas-map-target-point v-bind:map-lon-lat="mapLonLat"></silverpeas-map-target-point>'
            }
          })
        }
      }))();
    }
  });

  /**
   * Create a styled marker
   */
  function __createMarker(mapApi, infoPoint) {
    return infoPoint.getLocation().promiseLonLat().then(function(lonLat) {
      const marker = mapApi.addNewMarker({
        classList : ['target-point'],
        title : infoPoint.getName(),
        position : lonLat.asOlData(),
        contentPromise : infoPoint.promiseRender()
      });
      return marker.promise;
    });
  }

  const templateRepository = new VueJsAsyncComponentTemplateRepository(webContext +
      '/map/js/vuejs/components/silverpeas-map-target-point-templates.jsp');

  const MapTargetPointMixin = {
    emits : ['map-lon-lat-target'],
    props : {
      initialMapLocation : {
        'type' : MapLocation,
        'default' : undefined
      },
      mapOptions : {
        'type' : Object,
        'default' : {}
      },
      adapterClass : {
        'type' : Function,
        'default' : CartoTargetPointComponentAdapter
      },
      readOnly : {
        'type' : Boolean,
        'default' : false
      }
    }
  }

  SpVue.component('silverpeas-map-target-point-button',
      templateRepository.get('target-point-button', {
        mixins : [VuejsApiMixin ,VuejsI18nTemplateMixin, MapTargetPointMixin],
        emits : ['before-open'],
        data : function() {
          return {
            popinApi : undefined
          };
        },
        created : function() {
          this.extendApiWith({
            open : this.open
          });
        },
        methods : {
          open : function() {
            this.$emit('before-open');
            this.popinApi.open();
          }
        },
        computed : {
          title : function() {
            return this.readOnly ? this.messages.targetPointReadOnlyTitle : this.messages.targetPointTitle;
          }
        }
      }));

  const MapTargetPointComponentStarter = function(componentName) {
    const __instantiate = function(cssSelectorOrInputElement, mapOptions, readOnly, insertCallback) {
      readOnly = typeof readOnly === 'undefined' ?  false : readOnly;
      const $sibling = typeof cssSelectorOrInputElement === 'string'
          ? document.querySelector(cssSelectorOrInputElement)
          : cssSelectorOrInputElement;
      const $app = document.createElement('div');
      $app.classList.add(componentName + '-app');
      const $decorator = document.createElement(componentName);
      $decorator.setAttribute('v-bind:initial-map-location', 'initialMapLocation');
      $decorator.setAttribute('v-bind:map-options', 'mapOptions');
      $decorator.setAttribute('v-bind:read-only', 'readOnly');
      $decorator.setAttribute('v-on:before-open', 'performBeforeOpen');
      $decorator.setAttribute('v-on:map-lon-lat-target', 'performTargeted');
      $app.appendChild($decorator);
      insertCallback($app, $sibling);
      const app = SpVue.createApp({
        data : function() {
          return {
            target : $sibling,
            mapOptions : mapOptions,
            initialMapLocation : undefined,
            readOnly : readOnly
          };
        },
        methods : {
          performBeforeOpen : function() {
            app.dispatchEvent('open');
          },
          performTargeted : function(mapLonLat) {
            app.dispatchEvent('map-lon-lat-target', mapLonLat);
            this.setInitialMapLocation(new MapLocation(mapLonLat, undefined));
          },
          setInitialMapLocation : function(initialMapLocation) {
            this.initialMapLocation = initialMapLocation;
          }
        }
      }).mount($app);
      applyEventDispatchingBehaviorOn(app);
      return app;
    };
    this.appendInto = function(cssSelectorOrInputElement, mapOptions, readOnly) {
      return __instantiate(cssSelectorOrInputElement, mapOptions, readOnly, function($app, $sibling) {
        $sibling.appendChild($app);
      });
    };
    this.insertBefore = function(cssSelectorOrInputElement, mapOptions, readOnly) {
      return __instantiate(cssSelectorOrInputElement, mapOptions, readOnly, function($app, $sibling) {
        sp.element.insertBefore($app, $sibling);
      });
    };
    this.insertAfter = function(cssSelectorOrInputElement, mapOptions, readOnly) {
      return __instantiate(cssSelectorOrInputElement, mapOptions, readOnly, function($app, $sibling) {
        sp.element.insertAfter($app, $sibling);
      });
    };
  };

  window.MapTargetPointButton = new MapTargetPointComponentStarter('silverpeas-map-target-point-button');

  SpVue.component('silverpeas-map-target-point-popin',
      templateRepository.get('target-point-popin', {
        mixins : [VuejsApiMixin, VuejsI18nTemplateMixin, MapTargetPointMixin],
        data : function() {
          return {
            popinApi : undefined,
            isOpen : false,
            currentTarget : undefined
          };
        },
        created : function() {
          this.extendApiWith({
            open : this.openMap
          });
        },
        methods : {
          openMap : function() {
            this.popinApi.open({
              callback : function() {
                if (!this.currentTarget) {
                  SilverpeasError.add(this.messages.noTargetMsg).show();
                  return false;
                }
                this.$emit('map-lon-lat-target', this.currentTarget);
                return true;
              }.bind(this)
            });
          }
        }
      }));

  SpVue.component('silverpeas-map-target-point-map',
      templateRepository.get('target-point-map', {
        mixins : [MapTargetPointMixin],
        data : function() {
          return {
            mapApi : undefined,
            lastMapLonLat : undefined
          }
        },
        mounted : function() {
          this.mapApi = new MapApi(this.$el);
          this.mapApi.render(this.mapOptions).then(function() {
            if (!this.readOnly) {
              this.mapApi.addEventListener('click', function(e) {
                const mapEvent = e.detail.data;
                const projection = this.mapApi.proj.fromCoordinates(mapEvent.coordinate);
                this.setMapLonLat(new MapLonLat(projection.lon(), projection.lat()));
              }.bind(this));
            }
            this.setInitialMapLocation(this.initialMapLocation);
          }.bind(this));
        },
        methods : {
          setInitialMapLocation : function(initialMapLocation) {
            let promise = sp.promise.resolveDirectlyWith();
            if (initialMapLocation) {
              promise = initialMapLocation.promiseLonLat();
            }
            promise.then(function(mapLonLat) {
              if (mapLonLat) {
                if (!this.isEqualsToLastMapLonLat(mapLonLat)) {
                  this.setMapLonLat(mapLonLat);
                  setTimeout(function() {
                    this.mapApi.autoFit();
                  }.bind(this), 0);
                }
              } else {
                this.mapApi.clearMarkers();
              }
            }.bind(this));
          },
          setMapLonLat : function(mapLonLat) {
            this.lastMapLonLat = mapLonLat;
            this.mapApi.clearMarkers();
            __createMarker(this.mapApi, this.adapter.createInfoPoint(mapLonLat)).then(function() {
              this.$emit('map-lon-lat-target', mapLonLat);
            }.bind(this));
          },
          isEqualsToLastMapLonLat : function(mapLonLat) {
            return this.lastMapLonLat
                && this.lastMapLonLat.getLatitude() === mapLonLat.getLatitude()
                && this.lastMapLonLat.getLongitude() === mapLonLat.getLongitude();
          }
        },
        computed : {
          adapter : function() {
            return new this.adapterClass();
          }
        },
        watch : {
          initialMapLocation : function(value) {
            this.setInitialMapLocation(value);
          }
        }
      }));

  window.MapTargetPoint = new MapTargetPointComponentStarter('silverpeas-map-target-point-map');

  SpVue.component('silverpeas-map-target-point',
      templateRepository.get('target-point', {
        props : {
          mapLonLat : {
            'type' : MapLonLat,
            'required' : true
          }
        },
        data : function() {
          return {
            mapAddress : undefined
          };
        },
        created : function() {
          this.mapLonLat.promiseAddress().then(function(address) {
            this.mapAddress = address;
          }.bind(this));
        },
        computed : {
          latLon : function() {
            return this.mapLonLat.getLatitude() + ', ' + this.mapLonLat.getLongitude();
          },
          address : function() {
            return this.mapAddress ? this.mapAddress.toHtml().innerHTML : undefined;
          }
        }
      }));
})();