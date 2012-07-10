/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
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
 * Object to manage menu sets
 */
function MenuSet(menuItems, options) {
  var self = this;
  var _menuItems = new Array();
  i = 0;
  $(menuItems).each(function() {
    _menuItems[i++] = new options.menuItemObjectClass(this, options);
  });

  /**
   * Rendering the menu set (default)
   */
  this.defaultRender = function() {
    var $result = $('<ul>').addClass('sp-menu-set');
    for ( var i = 0; i < _menuItems.length; i++) {
      $result = $result.append(_menuItems[i].render());
    }
    return $result;
  };

  /**
   * Rendering the menu set (can be overridden)
   */
  this.render = function() {
    return self.defaultRender();
  };
}

/**
 * Object to manage menu item actions
 */
function MenuItem(menuItem, options) {
  var self = this;
  var _id = null;
  var _menuItem = menuItem;
  var _options = options;
  var _state = {
    isExpanded : false
  };

  /**
   * Gets the id of the menu item
   */
  this.getId = function() {
    if (_id == null) {
      _id = $.menu.buildId(_menuItem);
    }
    return _id;
  };

  /**
   * Gets the state of the menu item
   */
  this.getState = function() {
    return _state;
  };

  /**
   * Sets the state of the menu item
   */
  this.setState = function(state) {
    if ($.isPlainObject(state)) {
      _state = $.extend(_state, state);
    }
    return self.getState();
  };

  /**
   * Gets data of the menu item
   */
  this.getOptions = function() {
    return _options;
  };

  /**
   * Gets parent of the menu item
   */
  this.getParent = function() {
    return _options.parentMenuItem;
  };

  /**
   * Gets data of the menu item
   */
  this.getData = function() {
    return _menuItem;
  };

  /**
   * Gets appearance of the menu item
   */
  this.getAppearance = function() {
    var result = {};

    // Data appearance can be retrieved only for a space
    if (_menuItem.type.indexOf($.menu.definitions.SPACE_TYPE) >= 0) {
      return $.menu.getData(_menuItem.appearanceURI);
    } else if (self.getParent() != null) {
      return self.getParent().getAppearance();
    }

    return {};
  };

  /**
   * Rendering the menu item (default)
   */
  this.defaultRender = function() {
    var $result = $('<div>');

    // Description
    if (_menuItem.description) {
      $result = $result.mouseover(function() {
        $(this).children($.menu.definitions.LABEL_TAG).after(
            $('<div>').addClass('sp-menu-item-description').html(
                _menuItem.description));
      });
      $result = $result.mouseout(function() {
        $(this).children('.sp-menu-item-description').remove();
      });
    }

    // Click
    $result.append($('<' + $.menu.definitions.LABEL_TAG + '>').click(
        function() {
          var $triggers = $(this).closest('li');
          $triggers.trigger('expand');
          $triggers.trigger('select');
          $triggers.trigger('callbacks');
          return false;
        }).append(_menuItem.label));

    return $('<li>')
        .attr('id', self.getId())
        .addClass(self.getStyleClass())
        .on('select', function() {
          $.menu.select(this, self);
          return false;
        })
        .on('forceExpand', function() {
          if (_menuItem.type.indexOf($.menu.definitions.SPACE_TYPE) >= 0) {
            $.menu.expand(this, self, false);
          }
          return false;
        })
        .on('expand', function() {
          if (_menuItem.type.indexOf($.menu.definitions.SPACE_TYPE) >= 0) {
            $.menu.expand(this, self, true);
          }
          return false;
        })
        .on(
            'callbacks',
            function() {
              if (_menuItem.type.indexOf($.menu.definitions.SPACE_TYPE) >= 0) {
                if (_options.spaceCallback != null) {
                  _options.spaceCallback(self);
                }
              } else if (_menuItem.type
                  .indexOf($.menu.definitions.COMPONENT_TYPE) >= 0) {
                if (_options.componentCallback != null) {
                  _options.componentCallback(self);
                }
              } else if (_menuItem.type.indexOf($.menu.definitions.TOOL_TYPE) >= 0) {
                if (_options.toolCallback != null) {
                  _options.toolCallback(self);
                }
              }
              return false;
            }).append($result);
  }

  /**
   * Rendering the menu item (can be overridden)
   */
  this.render = function() {
    return self.defaultRender();
  };

  /**
   * Computes the style classes by the type of the menu item
   */
  this.getStyleClass = function() {
    var styleClass = " ";
    if (_menuItem.type.indexOf($.menu.definitions.SPACE_TYPE) >= 0) {
      styleClass += "sp-space";
    } else if (_menuItem.type.indexOf($.menu.definitions.COMPONENT_TYPE) >= 0) {
      styleClass += "sp-component";
    } else if (_menuItem.type.indexOf($.menu.definitions.TOOL_TYPE) >= 0) {
      styleClass += "sp-tool";
    }
    return styleClass;
  };
}

// An anonymous function to wrap around the menu to avoid conflict
(function($) {

  $.menu = {
    definitions : {
      LABEL_TAG : 'a',
      SPACE_TYPE : 'space',
      COMPONENT_TYPE : 'component',
      TOOL_TYPE : 'tool'
    },
    webServiceContext : webContext + '/services',

    /**
     * Selects the given menu
     *
     * @param target
     * @param menuItem
     */
    select : function(target, menuItem) {
      $('.sp-menu-selected').removeClass('sp-menu-selected');
      $(target).children('div').children($.menu.definitions.LABEL_TAG)
          .addClass('sp-menu-selected');
      var menuItem = menuItem.getParent();
      while (menuItem) {
        $('#' + menuItem.getId()).children('div').children(
            $.menu.definitions.LABEL_TAG).addClass('sp-menu-selected');
        menuItem = menuItem.getParent();
      }
    },

    /**
     * Expands or collapses the given menu
     *
     * @param target
     * @param menuItem
     */
    expand : function(target, menuItem, collapseIfPossible) {
      if (!menuItem.getState().isExpanded) {
        $(target).menu('expand', menuItem, function () {
          menuItem.setState({
            isExpanded : true
          });
        });
      } else if (collapseIfPossible) {
        $(target).children().remove('ul');
        menuItem.setState({
          isExpanded : false
        });
      }
    },

    /**
     * Expands the path to achieve the component represented by the given id
     *
     * @param componentId
     */
    reverseExpand : function(componentId) {
      var component = $.menu.getComponentData(componentId);
      if ($.isPlainObject(component)) {
        var path = [];
        var parentURI = component.parentURI;
        path.push(component);
        while (parentURI) {
          var space = $.menu.getData(parentURI);
          path.push(space);
          parentURI = space.parentURI;
        }
        path.reverse();
        var $oldMenu;
        var $currentMenu;
        for ( var i = 0; i < path.length; i++) {
          $oldMenu = $currentMenu;
          $currentMenu = $('[id$=' + $.menu.buildId(path[i]) + ']');
          $currentMenu.trigger('forceExpand');
        }
        $currentMenu.trigger('select');
        if ($oldMenu) {
          $oldMenu.trigger('callbacks');
        }
        $currentMenu.trigger('callbacks');
      }
    },

    /**
     * Building an unique id from given menu data
     *
     * @returns
     */
    buildId : function(menu) {
      var menuData = menu;
      if (menu instanceof MenuItem) {
        menuId = menu.getData();
      }
      return menuData.type + '-' + menuData.id;
    },

    /**
     * Gets component data from a given id
     *
     * @returns
     */
    getComponentData : function(id) {
      return $.menu.getData($.menu.webServiceContext + "/components/" + id);
    },

    /**
     * Centralizes synchronous ajax request
     *
     * @returns
     */
    getData : function(url) {
      var result = {};
      $.ajax({
        url : url,
        type : 'GET',
        dataType : 'json',
        cache : false,
        async : false,
        success : function(data, status, jqXHR) {
          result = data;
        },
        error : function(jqXHR, textStatus, errorThrown) {
          alert(errorThrown);
        }
      });
      return result;
    },

    /**
     * Debug function
     *
     * @param object
     * @returns
     */
    printParams : function(object) {
      var $result = $('<div>').attr('style',
          'display: table; padding: 5px; margin: 5px;');
      var object = object;
      if (object instanceof MenuItem) {
        object = object.getData();
      }
      if ($.isPlainObject(object)) {
        var content = $.param(object).replace(/%3A/g, ":").replace(/%2F/g, "/")
            .replace(/%C3%A9/g, "é").replace(/%C3%A8/g, "è").replace(/%C3%89/g,
                'E').replace(/%C3%88/g, 'E').replace(/%C3%AA/g, 'ê').replace(
                /%C3%A0/g, 'à').replace(/\+/g, " ").replace(/%23/g, "#")
            .replace(/%C3%A2/g, "â");
        var splittedContent = content.split("&");
        for ( var i = 0; i < splittedContent.length; i++) {
          var $line = $('<div>').attr('style', 'display: table-row;')
          var splittedLine = splittedContent[i].split("=");
          for ( var j = 0; j < splittedLine.length; j++) {
            $line.append($('<div>').attr('style',
                'display: table-cell; padding: 2px;').html(splittedLine[j]));
          }
          $result.append($line);
        }
      }
      return $result;
    }
  }

  /**
   * The different methods on menus handled by the plugin.
   */
  var methods = {

  /**
   * The default menu handler.
   * It accepts one parameter that is an object with below attributes:
   * - spaceCallback : the callback to invoke when the user expands a space,
   * - componentCallback : the callback to invoke when the user clicks on component,
   * - toolCallback : the callback to invoke when the user clicks on tool,
   * - callback : the callback to invoke after menu initialization
   */
    load : function(options) {

      // Common settings
      var settings = __extendCommonSettings(options);

      // Internal settings
      $.extend(settings, __buildInternalSettings());

      // Menu
      return __build($(this), settings);
    },

  /**
   * The expand menu handler.
   * It accepts two parameters that are a MenuItem and a callback that is invoked after a well
   * performed treatment.
   */
    expand : function(menuItem, callback) {

      if (!menuItem instanceof MenuItem) {
        alert('expand method error, the parameter is not a MenuItem as required')
        return false;
      }

      // Common settings
      var settings = __extendCommonSettings(menuItem.getOptions());

      // Internal settings
      $.extend(settings, __buildInternalSettings({
        "async" : false,
        "url" : menuItem.getData().contentURI,
        "parentMenuItem" : menuItem,
        "callback" : callback
      }));

      // Menu
      return __build($(this), settings);
    },

  /**
   * The personal menu handler. It accepts one parameter that is an object
   * with the same attributes as the root method.
   */
    personal : function(options) {

      // Common settings
      var settings = __extendCommonSettings(options);

      // Internal settings
      $.extend(settings, __buildInternalSettings({
        "url" : $.menu.webServiceContext
            + '/spaces/personal?getUsedComponents=true&getUsedTools=true',
      }));

      // Menu
      return __build($(this), settings);
    },

  /**
   * The not used personal component menu handler. It accepts one parameter
   * that is an object with the same attributes as the root method.
   */
    personalNotUsed : function(options) {

      // Common settings
      var settings = __extendCommonSettings(options);

      // Internal settings
      $.extend(settings, __buildInternalSettings({
        "url" : $.menu.webServiceContext
            + '/spaces/personal?getNotUsedComponents=true',
      }));

      // Menu
      return __build($(this), settings);
    }
  }

  /**
   * The menu handler Silverpeas plugin based on JQuery.
   * This JQuery plugin abstrats the way an HTML element (usually a div) is rendered
   * within menus.
   *
   * Here the menu namespace in JQuery in which methods on messages are provided.
   */
  $.fn.menu = function( method ) {
    if ( methods[method] ) {
      return methods[ method ].apply( this, Array.prototype.slice.call( arguments, 1 ));
    } else if ( typeof method === 'object' || ! method ) {
      return methods.load.apply( this, arguments );
    } else {
      $.error( 'Method ' +  method + ' does not exist on jQuery.menu' );
    }
  };

  /**
   * Private function that centralizes extension of common settings
   */
  function __extendCommonSettings(options) {
    var settings = {
        "spaceCallback" : null,
        "componentCallback" : null,
        "toolCallback" : null,
        "callback" : null
    }
    if (options) {
      $.extend(settings, options);
    }
    return settings;
  }

  /**
   * Private function that centralizes extension of internal settings
   */
  function __buildInternalSettings(options) {
    var settings = {
        "menuSetObjectClass" : MenuSet,
        "menuItemObjectClass" : MenuItem,
        "async" : true,
        "isPersonalNotUsed" : false,
        "url" : $.menu.webServiceContext + '/spaces',
        "parentMenuItem" : null
    }
    if (options) {
      $.extend(settings, options);
    }
    return settings;
  }

  /**
   * Private function that centralizes the build of the menu
   */
  function __build($this, options) {

    // Iterate over the current set of matched elements
    return $this.each(function() {
      var $this = $(this);

      // Loading data
      $.ajax({
        url : options.url,
        type : 'GET',
        dataType : 'json',
        cache : false,
        async : options.async,
        success : function(data, status, jqXHR) {
          var menuSet = new options.menuSetObjectClass(data, options);
          $this.append(menuSet.render());

          // Callback
          if (options.callback) {
            options.callback(options, menuSet);
          }
        },
        error : function(jqXHR, textStatus, errorThrown) {
          alert(errorThrown);
        }
      });
    });
  };
})(jQuery);
