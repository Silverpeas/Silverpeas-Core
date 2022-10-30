
(function() {

  const sessionCache = new SilverpeasCache('silverpeas-map-form-common');
  if (typeof sessionCache.get('filter-show-labels') === 'undefined') {
    sessionCache.put('filter-show-labels', true);
  }

  const templateRepository = new VueJsAsyncComponentTemplateRepository(webContext +
      '/map/js/vuejs/components/silverpeas-map-form-common-templates.jsp');

  Vue.component('silverpeas-info-point-bloc-label-value',
      templateRepository.get('info-point-bloc-label-value', {
        props : {
          label : {
            'type' : String,
            'default' : ''
          },
          labelCss : {
            'type' : String,
            'default' : ''
          },
          value : {
            'type' : String,
            'default' : ''
          },
          valueCss : {
            'type' : String,
            'default' : ''
          },
          multiline : {
            'type' : Boolean,
            'default' : false
          },
          hideIfNotDefined : {
            'type' : Boolean,
            'default' : true
          }
        },
        computed : {
          isLabelSlot : function() {
            return !!this.$slots['label'];
          },
          labelClass : function() {
            const css = {'label' : !this.multiline, 'label-multiline' : this.multiline};
            this.labelCss.split(' ').forEach(function(aClass) {
              css[aClass] = true;
            });
            return css;
          },
          isValueSlot : function() {
            return !!this.$slots['default'];
          },
          valueClass : function() {
            const css = {'data' : !this.multiline, 'data-multiline' : this.multiline};
            this.valueCss.split(' ').forEach(function(aClass) {
              css[aClass] = true;
            });
            return css;
          }
        }
      }));

  Vue.component('silverpeas-map-form-mapping-filters',
      templateRepository.get('mapping-filters', {
        mixins : [FormCartoMarkerComponentMixin, VuejsI18nTemplateMixin],
        data : function() {
          return {
            showLabels : true,
            showMinMax : false
          }
        },
        methods : {
          toggle : function() {
            this.showLabels = !this.showLabels;
            sessionCache.put('filter-show-labels', this.showLabels);
          }
        },
        computed : {
          categories : function() {
            const categories = this.cmpInstance.categories;
            const result = []
            let showMinMax = false;
            categories.forEach(function(category) {
              const cssClassList = this.cmpInstance.categories.getCssClassList(category);
              showMinMax |= !!cssClassList.length;
              result.push({
                id : category.getId(),
                label : category.getLabel(),
                infoPointMarkers : this.cmpInstance.categories.getInfoPointMarkers(category),
                color : this.cmpInstance.categories.getColor(category),
                cssClassList : cssClassList
              })
            }.bind(this));
            this.showMinMax = showMinMax;
            if (this.showMinMax) {
              this.showLabels = sessionCache.get('filter-show-labels');
            }
            return result;
          },
          toggleUrl : function() {
            const baseUrl = webContext + '/look/jsp/icons/silverpeasV5/'
            return this.showLabels ? baseUrl + 'extend.gif' : baseUrl + 'reduct.gif';
          },
          toggleLabel : function() {
            return this.showLabels ? this.messages.minimizeLabel : this.messages.maximizeLabel;
          }
        }
      }));

  Vue.component('silverpeas-map-form-mapping-category-filter',
      templateRepository.get('mapping-category-filter', {
        mixins : [FormCartoMarkerComponentMixin],
        props : {
          category : {
            'type' : Object,
            'mandatory' : true
          },
          showLabel : {
            'type' : Boolean,
            'default' : true
          }
        },
        data : function() {
          return {
            disabled : false
          }
        },
        created : function() {
          this.categories = this.cmpInstance.categories
        },
        methods : {
          toggleVisibility : function() {
            const __categoryMapData = this.category.infoPointMarkers;
            __categoryMapData.visible = !this.disabled;
            __categoryMapData.forEach(function(mapData) {
              if (this.disabled) {
                mapData.marker.show();
              } else {
                mapData.marker.hide();
              }
            }.bind(this));
            this.disabled = !this.disabled;
          }
        },
        computed : {
          styles : function() {
            const styles = {};
            if (!this.cssClassList.length) {
              styles.color = this.category.color;
            }
            return styles;
          },
          cssClassList : function() {
            const cssClassList = [];
            Array.prototype.push.apply(cssClassList, this.category.cssClassList);
            if (this.disabled) {
              cssClassList.push('disabled')
            }
            return cssClassList;
          }
        }
      }));
})();