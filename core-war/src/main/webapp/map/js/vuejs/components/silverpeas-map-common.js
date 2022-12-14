
(function() {

  const sessionCache = new SilverpeasCache('silverpeas-map-common');

  const templateRepository = new VueJsAsyncComponentTemplateRepository(webContext +
      '/map/js/vuejs/components/silverpeas-map-common-templates.jsp');

  SpVue.component('silverpeas-map-marker',
      templateRepository.get('marker', {
        mixins : [CartoComponentMarkerMixin],
        props : {
          marker : {
            'type' : Object,
            'required' : true
          }
        },
        computed : {
          styles : function() {
            const styles = {};
            if (!this.cssClassList.length) {
              styles.backgroundColor = this.marker.getColor();
            }
            return styles;
          }
        }
      }));

  SpVue.component('silverpeas-map-tip-marker',
      templateRepository.get('tip-marker', {
        mixins : [CartoComponentMarkerMixin],
        props : {
          marker : {
            'type' : Object,
            'required' : true
          }
        }
      }));

  SpVue.component('silverpeas-map-mapping-layers',
      templateRepository.get('mapping-layers', {
        mixins : [CartoComponentMixin],
        data : function() {
          return {
            layerNames : [],
            currentLayerName : undefined
          };
        },
        created : function() {
          this.layerNames = this.mapInstance.getLayers().getNames();
          if (this.layerNames.length > 1) {
            this.select(sessionCache.get('current-layer'));
          }
        },
        methods : {
          select : function(layerName) {
            this.currentLayerName = this.mapInstance.getLayers().select(layerName);
            sessionCache.put('current-layer', this.currentLayerName);
          }
        },
        computed : {
          layers : function() {
            const result = []
            this.layerNames.forEach(function(layer) {
              result.push({
                id : layer,
                label : layer,
                selected : layer === this.currentLayerName
              })
            }.bind(this));
            return result;
          }
        }
      }));

  SpVue.component('silverpeas-map-mapping-layer',
      templateRepository.get('mapping-layer', {
        mixins : [CartoComponentMixin],
        emits : ['layer-change'],
        props : {
          layer : {
            'type' : Object,
            'required' : true
          }
        },
        computed : {
          cssClassList : function() {
            return this.layer.selected ? '' : 'not-selected';
          }
        }
      }));
})();