/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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
function MenuSet(domMenuRootContainer, parentMenuItem, menuItems, options) {
  var self = this;
  var _content = null;
  var $_render = null;

  this.getMenuItems = function() {
    return (_content == null) ? null : _content.getFirst();
  };

  /**
   * Verify if a menu is displayable
   */
  this.isDisplayable = function(menuData) {
    return ($.menu.displayUserContext.userMenuDisplay != $.spCore.definitions.MENU.FAVORITES.BOOKMARKS
            || menuData.type.indexOf($.spCore.definitions.SPACE_TYPE) < 0
            || menuData.favorite != "false");
  };

  /**
   * Build menu items
   */
  $(menuItems).each(function() {
    if (self.isDisplayable(this)) {
      var _current = new options.menuItemObjectClass(domMenuRootContainer, parentMenuItem, this, options);
      if (_content != null) {
        _content.add(_current);
      }
      _content = _current;
    }
  });

  /**
   * Rendering the menu set (default)
   */
  this.defaultRender = function() {
    if ($_render == null) {
      $_render = $('<ul>').addClass('sp-menu-set');
    } else {
      $_render.empty();
    }
    var _current = self.getMenuItems();
    if (_current == null) {
      return null;
    }
    while (_current != null) {
      $_render.append(_current.render());
      _current = _current.getNext();
    }
    return $_render;
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
function MenuItem(domMenuRootContainer, parentMenuItem, menuData, options) {
  LinkedItem.apply(this, null);
  var self = this;
  var _domMenuRootContainer = domMenuRootContainer;
  var _parent = parentMenuItem;
  var _id = null;
  var _menuData = menuData;
  var _options = options;
  var _state = {
    isExpanded : false
  };
  var $_favoriteContainer = null;

  /**
   * Gets the id of the menu item
   */
  this.getId = function() {
    if (_id == null) {
      _id = $.menu.buildId(_menuData);
    }
    return _id;
  };

  /**
   * Gets the dom target container
   */
  this.getDomMenuRootContainer = function() {
    return _domMenuRootContainer;
  }

  /**
   * Gets the parent menu item
   */
  this.getParent = function () {
    return _parent;
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
   * Gets data of the menu item
   */
  this.getData = function() {
    return _menuData;
  };

  /**
   * Sets data of the menu item
   */
  this.setData = function(menuData) {
    _menuData = menuData;
  };

  /**
   * Refreshing the display of the menu item
   */
  this.refreshDisplay = function() {
    if ($.menu.displayUserContext.userMenuDisplay == $.spCore.definitions.MENU.FAVORITES.ALL
        && _menuData.type.indexOf($.spCore.definitions.SPACE_TYPE) >= 0) {
      self.renderFavorite();
    }
  }

  /**
   * Gets appearance of the menu item
   */
  this.getAppearance = function() {
    var result = {};

    // Data appearance can be retrieved only for a space
    if (_menuData.type.indexOf($.spCore.definitions.SPACE_TYPE) >= 0) {
      return $.spCore.getJSonData(_menuData.appearanceURI);
    } else if (self.getParent() != null) {
      return self.getParent().getAppearance();
    }

    return {};
  };

  /**
   * Rendering the menu item (default)
   */
  this.defaultRender = function() {

    // Root
    var $menuRootRender = $('<li>');
    $menuRootRender.attr('id', self.getId());
    $menuRootRender.addClass(self.getMenuRootStyleClass());
    $menuRootRender.on('select', function() {
      $.menu.select(this, self);
      return false;
    });

    // Expanding on root
    if (_menuData.type.indexOf($.spCore.definitions.SPACE_TYPE) >= 0) {
      $menuRootRender.on('forceExpand', function() {
        $.menu.expand(this, self, false);
        return false;
      });
      $menuRootRender.on('expand', function() {
        $.menu.expand(this, self, true);
        return false;
      });
    }

    // Callbacks on root
    if (_menuData.type.indexOf($.spCore.definitions.SPACE_TYPE) >= 0
        && _options.spaceCallback != null) {
      $menuRootRender.on('callbacks', function() {
        _options.spaceCallback(self);
        return false;
      });
    } else if (_menuData.type.indexOf($.spCore.definitions.COMPONENT_TYPE) >= 0
        && _options.componentCallback != null) {
      $menuRootRender.on('callbacks', function() {
        _options.componentCallback(self);
        return false;
      });
    } else if (_menuData.type.indexOf($.spCore.definitions.TOOL_TYPE) >= 0
        && _options.toolCallback != null) {
      $menuRootRender.on('callbacks', function() {
        _options.toolCallback(self);
        return false;
      });
    }

    // Menu label
    var $menuLabel = $('<div>');
    $menuLabel.addClass(self.getMenuLabelStyleClass());
    $menuLabel.append($('<a>')
        .attr('href','#')
        .click(function() {
          var $triggers = $(this).closest('li');
          if (_menuData.type.indexOf($.spCore.definitions.SPACE_TYPE) >= 0) {
            $triggers.trigger('expand');
          }
          $triggers.trigger('select');
          $triggers.trigger('callbacks');
          return false;
        }).append($('<span>').html(_menuData.label)));
    $menuRootRender.append($menuLabel);

    // Description
    if (_menuData.description && _menuData.description.length > 0) {
      var $menuDescription = $('<div>');
      $menuLabel.addClass(self.getMenuLabelStyleClass());
    }

    // Menu favorite
    if ($.menu.displayUserContext.userMenuDisplay == $.spCore.definitions.MENU.FAVORITES.ALL
        && _menuData.type.indexOf($.spCore.definitions.SPACE_TYPE) >= 0) {
      $menuRootRender.append(self.renderFavorite());
    }

    // Return the built menu
    return $menuRootRender;
  }

  /**
   * Render favorite
   */
  this.renderFavorite = function() {
    if ($.menu.displayUserContext.userMenuDisplay == $.spCore.definitions.MENU.FAVORITES.ALL
        && _menuData.type.indexOf($.spCore.definitions.SPACE_TYPE) >= 0) {
      if ($_favoriteContainer == null) {
        $_favoriteContainer = $('<div>');
        $_favoriteContainer.addClass('space-management-favorite');
      } else {
        $_favoriteContainer.empty();
      }
    } else {
      if ($_favoriteContainer != null) {
        $_favoriteContainer.remove();
      }
      return null;
    }
    var favorite = $('<a>');
    $_favoriteContainer.append(favorite);
    favorite.attr('href', '#');
    if (_menuData.favorite != "false") {
      if (_menuData.favorite == "true") {
        favorite.css('opacity', '1');
        favorite.click(function(){
          $.menu.removeFromUserFavorites(self);
          return false;
        });
      } else {
        favorite.css('opacity', '0.3');
        favorite.click(function(){
          $.menu.addToUserFavorites(self);
          return false;
        });
      }
      favorite.append(
          $('<img>').attr('src', '/silverpeas/util/icons/iconlook_favorites_12px.gif')
      );
    } else {
      favorite.css('opacity', '1');
      favorite.click(function(){
        $.menu.addToUserFavorites(self);
        return false;
      });
      favorite.append(
          $('<img>').attr('src', '/silverpeas/util/icons/iconlook_favorites_empty_12px.gif')
      );
    }
    return $_favoriteContainer;
  };

  /**
   * Rendering the menu item (can be overridden)
   */
  this.render = function() {
    return self.defaultRender();
  };

  /**
   * Computes the style classes by the type of the menu root item
   */
  this.getMenuRootStyleClass = function() {
    var styleClass = " ";
    if (_menuData.type.indexOf($.spCore.definitions.SPACE_TYPE) >= 0) {
      styleClass += "sp-space level-" + _menuData.level;
    } else if (_menuData.type.indexOf($.spCore.definitions.COMPONENT_TYPE) >= 0) {
      styleClass += "sp-component " + _menuData.name;
    } else if (_menuData.type.indexOf($.spCore.definitions.TOOL_TYPE) >= 0) {
      styleClass += "sp-tool";
    }
    return styleClass;
  };

  /**
   * Computes the style classes by the type of the menu label item
   */
  this.getMenuLabelStyleClass = function() {
    var styleClass = " ";
    if (_menuData.type.indexOf($.spCore.definitions.SPACE_TYPE) >= 0) {
      styleClass += "sp-space-name";
    }
    return styleClass;
  };

  /**
   * Computes the style classes by the type of the menu label item
   */
  this.getMenuDescriptionStyleClass = function() {
    var styleClass = " ";
    if (_menuData.type.indexOf($.spCore.definitions.SPACE_TYPE) >= 0) {
      styleClass += "sp-space-description";
    } else if (_menuData.type.indexOf($.spCore.definitions.COMPONENT_TYPE) >= 0) {
      styleClass += "sp-component-description";
    } else if (_menuData.type.indexOf($.spCore.definitions.TOOL_TYPE) >= 0) {
      styleClass += "sp-tool-description";
    }
    return styleClass;
  };

  /**
   * Selection behaviour
   */
  this.select = function() {
    var menuItem = self;
    while (menuItem) {
      $('#' + menuItem.getId()).children('div').children('a').addClass('sp-menu-selected');
      menuItem = menuItem.getParent();
    }
  };
}

// An anonymous function to wrap around the menu to avoid conflict
(function($) {

  $.menu = {
    initialized: false,
    doInitialize : function() {
      if (! $.menu.initialized) {
        $.menu.initialized = true;
        $.menu.loadDisplayUserContext();
      }
    },

    // Context / Settings
    webServiceContext : webContext + '/services',
    displayUserContext : null,

    // Context / Nav
    lastMenuItemSelected : null,

    /**
     * Loading display user context data
     */
    loadDisplayUserContext : function() {
      $.menu.displayUserContext = $.spCore.getJSonData($.menu.webServiceContext + "/display/userContext");
    },

    /**
     * Loading display user context data
     */
    addToUserFavorites : function(menuItem) {
      __setFavorite(menuItem, true);
    },

    /**
     * Loading display user context data
     */
    removeFromUserFavorites : function(menuItem) {
      __setFavorite(menuItem, false);
    },

    /**
     * Selects the given menu
     *
     * @param target
     * @param menuItem
     */
    select : function(target, menuItem) {
      $('.sp-menu-selected').removeClass('sp-menu-selected');
      if (menuItem.getData().type.indexOf($.spCore.definitions.COMPONENT_TYPE) >= 0
          || menuItem.getData().type.indexOf($.spCore.definitions.TOOL_TYPE) >= 0) {
        $.spCore.loadHtml(webContext + menuItem.getData().url, "body-target");
      }
      $.menu.lastMenuItemSelected = menuItem;
      menuItem.select();
    },

    /**
     * Refreshes the given menu
     */
    refresh : function($target) {
      var lastMenuItemSelected = $.menu.lastMenuItemSelected;
      $target.empty();
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
    reverseExpand : function(id, isSpaceId) {
      var data = (isSpaceId) ? $.menu.getSpaceData(id) : $.menu.getComponentData(id);
      if ($.isPlainObject(data)) {
        var path = [];
        var parentURI = data.parentURI;
        path.push(data);
        while (parentURI) {
          var space = $.spCore.getJSonData(parentURI);
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
      return menu.type + '-' + menu.id;
    },

    /**
     * Gets component data from a given id
     *
     * @returns
     */
    getComponentData : function(id) {
      return $.spCore.getJSonData($.menu.webServiceContext + "/components/" + id);
    },

    /**
     * Gets space data from a given id
     *
     * @returns
     */
    getSpaceData : function(id) {
      return $.spCore.getJSonData($.menu.webServiceContext + "/spaces/" + id);
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
      return __build(null, $(this), settings);
    },

    /**
     * The default menu handler. It accepts one parameter that is an object with
     * below attributes: - spaceCallback : the callback to invoke when the user
     * expands a space, - componentCallback : the callback to invoke when the
     * user clicks on component, - toolCallback : the callback to invoke when
     * the user clicks on tool, - callback : the callback to invoke after menu
     * initialization
     */
    refresh : function(options) {
      var $this = $(this);
      $.menu.loadDisplayUserContext();

      var lastMenuItemSelected = $.menu.lastMenuItemSelected;
      $this.empty();

      var result = $this.menu(options);
      if (lastMenuItemSelected != null
          && $this.get(0) == lastMenuItemSelected.getDomMenuRootContainer()) {
        $.menu.reverseExpand(lastMenuItemSelected.getData().id,
            (lastMenuItemSelected.getData().type
                .indexOf($.spCore.definitions.SPACE_TYPE) >= 0));
      }

      // Menu
      return result;
    },

  /**
   * The expand menu handler. It accepts two parameters that are a MenuItem and
   * a callback that is invoked after a well performed treatment.
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
        "callback" : callback
      }));

      // Menu
      return __build(menuItem, $(this), settings);
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
                + '/spaces/personal?getUsedComponents=true&getUsedTools=true'
      }));

      // Menu
      return __build(null, $(this), settings);
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
            + '/spaces/personal?getNotUsedComponents=true'
      }));

      // Menu
      return __build(null, $(this), settings);
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
    $.menu.doInitialize();
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
        "url" : $.menu.webServiceContext + '/spaces'
    }
    if (options) {
      $.extend(settings, options);
    }
    return settings;
  }

  /**
   * Private function that centralizes the build of the menu
   */
  function __build(parentMenuItem, $this, options) {

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
          var domMenuRootContainer;
          if (parentMenuItem != null) {
            domMenuRootContainer = parentMenuItem.getDomMenuRootContainer();
          } else {
            domMenuRootContainer = $this.get(0);
          }
          var menuSet = new options.menuSetObjectClass(domMenuRootContainer, parentMenuItem, data, options);
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

  /**
   * Private function that centralizes the favorite user action handling
   */
  function __setFavorite(menuItem, addOrRemove) {
    if (menuItem != null) {
      var data;
      if (addOrRemove != null) {
        data = $.extend(menuItem.getData(), {});
        data.favorite = addOrRemove;
        data = $.spCore.putJSonData(menuItem.getData().uri, data);
      } else {
        data = $.spCore.getJSonData(menuItem.getData().uri);
      }
      if (!$.isEmptyObject(data)) {
        menuItem.setData(data);
        menuItem.refreshDisplay();
      }
      __setFavorite(menuItem.getParent(), null);
    }
  }
})(jQuery);
