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
    return this.defaultRender();
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
    return this.getState();
  };

  /**
   * Gets data of the menu item
   */
  this.getOptions = function() {
    return $.extend({}, _options);
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
        .attr('id', this.getId())
        .addClass(this.getStyleClass())
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
    return this.defaultRender();
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
        var options = menuItem.getOptions();
        options.url = menuItem.getData().contentURI;
        options.parentMenuItem = menuItem;
        options.async = false;
        $(target).menu(options);
        menuItem.setState({
          isExpanded : true
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

  // Attach the menu method to jQuery
  $.fn.menu = function(options) {

    // Default options
    var defaults = {
      "async" : true,
      "menuSetObjectClass" : MenuSet,
      "menuItemObjectClass" : MenuItem,
      "isRendered" : true,
      "isPersonal" : false,
      "isPersonalNotUsed" : false,
      "rootURL" : '/spaces',
      "url" : null,
      "spaceCallback" : null,
      "componentCallback" : null,
      "toolCallback" : null,
      "callback" : null,
      "parentMenuItem" : null
    }

    // Getting options given from parameters
    var options = $.extend(defaults, options);

    // Computing the right calling URL
    var url = options.url;
    if (options.isPersonal) {
      if (options.isPersonalNotUsed) {
        url = $.menu.webServiceContext + options.rootURL
            + '/personal?getNotUsedComponents=true';
      } else {
        url = $.menu.webServiceContext + options.rootURL
            + '/personal?getUsedComponents=true&getUsedTools=true';
      }
    } else if (url == null) {
      url = $.menu.webServiceContext + options.rootURL;
    }

    // Iterate over the current set of matched elements
    return this.each(function() {
      var $this = $(this);

      // Loading data
      $.ajax({
        url : url,
        type : 'GET',
        dataType : 'json',
        cache : false,
        async : options.async,
        success : function(data, status, jqXHR) {
          var menuSet = new options.menuSetObjectClass(data, options);
          if (options.isRendered) {
            $this.append(menuSet.render());
          }

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
