
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
      }
    }
  }

  SpVue.component('silverpeas-map-target-point-button',
      templateRepository.get('target-point-button', {
        mixins : [VuejsApiMixin, MapTargetPointMixin],
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
        }
      }));

  window.MapTargetPointButton = new function() {
    const __instantiate = function(cssSelectorOrInputElement, mapOptions, insertCallback) {
      const $input = typeof cssSelectorOrInputElement === 'string'
          ? document.querySelector(cssSelectorOrInputElement)
          : cssSelectorOrInputElement;
      const $app = document.createElement('div');
      const $decorator = document.createElement('silverpeas-map-target-point-button');
      $decorator.setAttribute('v-bind:initial-map-location', 'initialMapLocation');
      $decorator.setAttribute('v-bind:map-options', 'mapOptions');
      $decorator.setAttribute('v-on:before-open', 'performBeforeOpen');
      $decorator.setAttribute('v-on:map-lon-lat-target', 'performTargeted');
      $app.appendChild($decorator);
      insertCallback($app, $input);
      const app = SpVue.createApp({
        data : function() {
          return {
            target : $input,
            mapOptions : mapOptions,
            initialMapLocation : undefined
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
    this.insertAfter = function(cssSelectorOrInputElement, mapOptions) {
      return __instantiate(cssSelectorOrInputElement, mapOptions, function($app, $input) {
        sp.element.insertAfter($app, $input);
      });
    };
  };

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
            mapApi : undefined
          }
        },
        mounted : function() {
          this.mapApi = new MapApi(this.$el);
          this.mapApi.render(this.mapOptions).then(function() {
            this.mapApi.addEventListener('click', function(e) {
              const mapEvent = e.detail.data;
              const projection = this.mapApi.proj.fromCoordinates(mapEvent.coordinate);
              this.setMapLonLat(new MapLonLat(projection.lon(), projection.lat()), true);
            }.bind(this));
            let promise = sp.promise.resolveDirectlyWith();
            if (this.initialMapLocation) {
              promise = this.initialMapLocation.promiseLonLat();
            }
            promise.then(function(mapLonLat) {
              if (mapLonLat) {
                this.setMapLonLat(mapLonLat);
                setTimeout(function() {
                  this.mapApi.autoFit();
                }.bind(this), 0);
              }
            }.bind(this));
          }.bind(this));
        },
        methods : {
          setMapLonLat : function(mapLonLat, notify) {
            this.mapApi.clearMarkers();
            __createMarker(this.mapApi, this.adapter.createInfoPoint(mapLonLat)).then(function() {
              if (notify) {
                this.$emit('map-lon-lat-target', mapLonLat);
              }
            }.bind(this));
          }
        },
        computed : {
          adapter : function() {
            return new this.adapterClass();
          }
        }
      }));

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