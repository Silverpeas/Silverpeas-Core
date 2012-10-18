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

function spHumanResource (type, userId) {
  var self = this;
  var _type = type;
  var _photoUrl;
  var _lastName;
  var _firstName;
  var _grade;
  var _phone;
  var _mobilePhone;
  var _mail;
  var _address;

  this.decorate = function(user) {
    _type = type;
    _photoUrl = user.avatar;
    _lastName = user.lastName;
    _firstName = user.firstName;
    _grade = user.moreData['title'];
    _phone = user.moreData['phone'];
    _mobilePhone = user.moreData['cellularPhone'];
    _mail = user.eMail;
    var address = user.moreData['address'];
    if (!$.mapping.valExists(address)) {
      address = "";
    }
    _address = new spAddress(address);
  }

  if ($.mapping.valExists(userId)) {
    var _user = $.mapping.cache['user_' + userId];
    if (!$.mapping.valExists(_user)) {
      _user = new UserProfile({
        id: userId,
        extended: true,
        async: false
      });
      _user.load(function(user){
        $.mapping.cache['user_' + userId] = user;
        _user = user;
      });
    }
    self.decorate(_user);
  }

  this.render = function() {
    if (_user == null) {
      return null;
    }

    var $bloc = $('<div>').addClass('bloc');
    var $header = $('<div>').addClass('row').addClass('title');
    var $body = $('<div>').addClass('row');
    $bloc.append($header).append($body);

    /*
     * Header
     */
    $header.html(_type);

    /*
     * Body
     */
    var $bodyBloc = $('<div>').addClass('bloc');

    // Identity
    var $bodyRow = $('<div>').addClass('row');
    var $bodyRowLeft = $('<div>').addClass('cell').addClass('label').addClass('horizontal-centered');
    var $bodyRowRight = $('<div>').addClass('cell').addClass('data').addClass('vertical-centered');
    $body.append($bodyBloc.append($bodyRow.append($bodyRowLeft).append($bodyRowRight)));
    if ($.mapping.valExists(_photoUrl)) {
      $bodyRowLeft.append($('<img>').attr('src' , _photoUrl).addClass('img'));
    }
    $bodyRowRight.append($('<p>').html(_grade + ' ' + _firstName + ' ' + _lastName));
    if ($.mapping.valExists(_phone)) {
      $bodyRowRight.append($('<p>').html(_phone));
    }
    if ($.mapping.valExists(_mobilePhone)) {
      $bodyRowRight.append($('<p>').html(_mobilePhone));
    }
    if ($.mapping.valExists(_mail)) {
      $bodyRowRight.append($('<p>').html(_mail));
    }

    // Address
    if ($.mapping.valExists(_address)) {
      $bloc.append(_address.render());
    }

    // Return the display result
    return $bloc;
  };
}

function spChief(userId) {
  spHumanResource.apply(this, ["Chef", userId]);
}

function spAssistant(userId) {
  spHumanResource.apply(this, ["Adjoint", userId]);
}

function spAddress(address) {
  var _address;
  if (arguments.length > 1) {
    _address = arguments;
  } else if (arguments.length == 1){
    if ($.isArray(address)) {
      _address = address;
    } else if (address) {
      _address = address.split(',');
    }
  }

  this.render = function() {
    var addressValue = "";
    $(_address).each(function(index, part){
      if ($.mapping.valExists(part)) {
        if (index > 0) {
          addressValue += '<br/>';
        }
        addressValue += part;
      }
    });

    if (!$.mapping.valExists(addressValue)) {
      return null;
    }

    var $bloc = $('<div>').addClass('bloc');
    var $body = $('<div>').addClass('row');
    $bloc.append($body);

    /*
     * Body
     */
    var $bodyBloc = $('<div>').addClass('bloc');

    // Address
    $.mapping.buildInfoWindowBlock($body, "Adresse :", addressValue);

    // Return the display result
    return $bloc;
  }
}

function spInfoPoint(group, type, name, photoUrl, latitude, longitude, phone, fax, address, accessUrl, complement, humanResources) {
  var _group = group;
  var _type = type;
  var _name = name;
  var _photoUrl = photoUrl;
  var _latLng = new google.maps.LatLng(latitude, longitude);
  var _phone = phone;
  var _fax = fax;
  var _address = address;
  var _accessUrl = accessUrl;
  var _complement = complement;
  var _humanResources = humanResources;
  this.getGroup = function() {return _group};
  this.getType = function() {return _type};
  this.getName = function() {return _name};
  this.getPhotoUrl = function() {return _photoUrl};
  this.getLatLng = function() {return _latLng};
  this.getAddress = function() {return _address};
  this.getAccessUrl = function() {return _accessUrl};
  this.getComplement = function() {return _complement};
  this.getHumanResources = function() {return _humanResources};

  this.render = function() {
    var $baseContainer = $('<div>').addClass('info-window');
    var $baseContainerBody = $('<div>').addClass('row');
    $baseContainer.append($baseContainerBody);

    /*
     * Body
     */

    var $bodyCell = $('<div>').addClass('cell');
    $baseContainerBody.append($bodyCell)

    // Identity
    var $centerIdentityBloc = $('<div>').addClass('bloc');
    var $centerIdentityBlocRow = $('<div>').addClass('row');
    var $centerIdentityBlocRowLeft = $('<div>').addClass('cell').addClass('label').addClass('horizontal-centered');
    var $centerIdentityBlocRowRight = $('<div>').addClass('cell').addClass('data').addClass('vertical-centered');
    $bodyCell.append(
        $centerIdentityBloc.append(
            $centerIdentityBlocRow
              .append($centerIdentityBlocRowLeft)
              .append($centerIdentityBlocRowRight)));
    if ($.mapping.valExists(_photoUrl)) {
      $centerIdentityBlocRowLeft.append($('<img>').attr('src' , _photoUrl).addClass('img'));
    }
    $centerIdentityBlocRowRight.append($('<p>').html(_group));
    $centerIdentityBlocRowRight.append($('<p>').css('font-weight', 'bold').html(_name));
    $centerIdentityBlocRowRight.append($('<p>').css('font-style', 'italic').html(_type));

    // Téléphone & Fax
    $.mapping.buildInfoWindowBlock($bodyCell, "Téléphone :", _phone);
    $.mapping.buildInfoWindowBlock($bodyCell, "Fax :", _fax);

    // Address
    $bodyCell.append(_address.render());

    // Map access
    if ($.mapping.valExists(_accessUrl)) {
      $.mapping.buildInfoWindowBlock($bodyCell, null, $('<a>')
                                                        .attr('href', _accessUrl)
                                                        .attr('target','_blank')
                                                        .css('font-weight','bold')
                                                        .css('font-style','italic')
                                                        .html("Plan d'accès"));
    }

    // Complement
    var cssRight = [];
    cssRight['width'] = '100%';
    $.mapping.buildInfoWindowBlock($bodyCell, "<p>Complément :<p>",
        _complement, {
          data : 'data-multiline',
          cssRight: cssRight
        });

    // Human resources
    if ($.mapping.valExists(_humanResources)) {
      $bodyCell = $('<div>').addClass('cell').addClass('vertical-separator-margin');
      $baseContainerBody.append($bodyCell);
      $bodyCell = $('<div>').addClass('cell').addClass('vertical-separator');
      $baseContainerBody.append($bodyCell);
      $bodyCell = $('<div>').addClass('cell').addClass('vertical-separator-margin');
      $baseContainerBody.append($bodyCell);
      $bodyCell = $('<div>').addClass('cell');
      $baseContainerBody.append($bodyCell);
      $(_humanResources).each(function(index, humanResource) {
        if ($.mapping.valExists(_humanResources)) {
          var $humanResourceRendered = humanResource.render();
          if ($humanResourceRendered != null) {
            if (index > 0) {
              $bodyCell.append($('<div>').addClass('horizontal-separator-margin'));
              $bodyCell.append($('<div>').addClass('horizontal-separator'));
              $bodyCell.append($('<div>').addClass('horizontal-separator-margin'));
            }
            $bodyCell.append($humanResourceRendered.css('width', '100%'));
          }
        }
      });
    }

    return $baseContainer;
  };
}

/**
 * Silverpeas plugin build upon JQuery to display a document map.
 * It uses the JQuery UI framework.
 */
(function( $ ){

  $.mapping = {
    webServiceContext : webContext + '/services',
    initialized: false,
    doInitialize: function() {
      if (!$.mapping.initialized) {
        $.mapping.initialized = true;
      }
    },
    colors: ["#ff5555", "#5555ff", "#558055", "#A52A2A"],
    valExists : function(value) {
      return (value && value != null);
    },
    cache : new Array(),
    buildInfoWindowBlock: function($container, label, value, classes) {
      if ($.mapping.valExists(value)) {
        var _classes = $.extend({label: 'label', data: 'data', cssLeft: [], cssRight: []}, (classes ? classes: {}));
        var $infoBloc = $('<div>').addClass('bloc');
        var $infoBlocRow = $('<div>').addClass('row');
        var $infoBlocRowLeft = $('<div>').addClass('cell').addClass(_classes.label);
        var $infoBlocRowRight = $('<div>').addClass('cell').addClass(_classes.data);
        $container.append(
            $infoBloc.append(
                $infoBlocRow
                .append($infoBlocRowLeft)
                .append($infoBlocRowRight)));
        for (var css in _classes.cssLeft) {
          $infoBlocRowLeft.css(css, _classes.cssLeft[css]);
        }
        for (var css in _classes.cssRight) {
          $infoBlocRowRight.css(css, _classes.cssRight[css]);
        }
        if ($.mapping.valExists(label)) {
          $infoBlocRowLeft.html(label);
        }
        if (value.browser) {
          $infoBlocRowRight.append(value);
        } else {
          $infoBlocRowRight.html(value);
        }
      }
    }
  }

  /**
   * The different mapping methods handled by the plugin.
   */
  var methods = {

      init : function( options ) {
        __openMapping($(this), options);
      }
  };

  /**
   * The mapping Silverpeas plugin based on JQuery.
   * This JQuery plugin abstrats the way an HTML element (usually a form or a div) is rendered
   * within a JQuery UI dialog.
   *
   * Here the mapping namespace in JQuery.
   */
  $.fn.mapping = function( method ) {
    $.mapping.doInitialize();
    if ( methods[method] ) {
      return methods[ method ].apply( this, Array.prototype.slice.call( arguments, 1 ));
    } else if ( typeof method === 'object' || ! method ) {
      return methods.init.apply( this, arguments );
    } else {
      $.error( 'Method ' +  method + ' does not exist on jQuery.mapping' );
    }
  };

  /**
   * Private function that handles the mapping opening.
   * Be careful, options have to be well initialized before this function call
   */
  function __openMapping($this, options) {

    if (!$this.length) {
      return $this;
    }

    return $this.each(function() {
      var $_this = $(this);
      try {
        __openMap($_this, options);
      } catch (e) {
        alert(e);
      }
    })
  }

  function __openMap($this, params) {

    var mapOptions = {
      zoom      : 10, // default Zoom
      center    : __calculateMappingCoordinates(params), // middle coordinates
      mapTypeId : google.maps.MapTypeId.ROADMAP, // map type, different possible values HYBRID, ROADMAP, SATELLITE, TERRAIN
      maxZoom   : 20
    };

    var context = $.extend(params, {
      map : new google.maps.Map($this.get(0), mapOptions),
      groups : new Array(),
      groupMapData : new Array(),
      getGroupIndex: function(group){
        var index = $.inArray(group, context.groups);
        if (index < 0) {
          index = context.groups.length;
          context.groups.push(group);
          context.groupMapData.push(new Array());
        }
        return index
      },
      getGroupMapData : function (group) {
        return context.groupMapData[context.getGroupIndex(group)];
      },
      getGroupColor : function (group) {
        return $.mapping.colors[context.getGroupIndex(group)];
      }
    });

    // Markers
    __createMarkers(context);

    // Filters
    __createFilters(context);
  }

  /**
   * Create filters
   */
  function __createFilters(context) {
    if (context.spInfoPoints && $.isArray(context.spInfoPoints)) {
      var $baseContainer = new $('<div>')
                                  .addClass('mapping-filters')
                                  .css('display', 'none')
                                  .css('position', 'absolute')
                                  .css('top', '0px')
                                  .css('left', '0px');

      $(context.groups).each(function(index, group){
        __createGroupFilter(context, group, $baseContainer);
      });

      var $target = $(document.body);
      $target.append($baseContainer);
      var top = 50;
      var left = $target.outerWidth(true) - $baseContainer.outerWidth(true) - 10;
      $baseContainer.offset({ top: top, left: left });
      $baseContainer.fadeIn();
    }
  }

  /**
   * Create a group filter
   */
  function __createGroupFilter(context, group, $baseContainer) {
    var $groupContainer = $('<div>')
                            .css('color', context.getGroupColor(group))
                            .css('opacity', '1')
                            .css('margin', '10 10 10 10')
                            .css('text-align', 'left');
    var $groupLabel = $('<span>').css('cursor', 'pointer').click(function() {
      var opacity = $groupContainer.css('opacity');
      var visible;
      if (eval(opacity) == 1) {
        visible = false;
        $groupContainer.css('opacity', '0.5');
        $groupContainer.css('color', 'black');
      } else {
        visible = true;
        $groupContainer.css('opacity', '1');
        $groupContainer.css('color', context.getGroupColor(group));
      }
      $(context.getGroupMapData(group)).each(function(index, mapData){
        mapData.marker.setVisible(visible);
        if (!visible) {
          mapData.infoWindow.close();
        }
      });
    }).html(group);

    $groupContainer.append($groupLabel);
    $baseContainer.append($groupContainer);
  }

  /**
   * Create markers
   */
  function __createMarkers(context) {
    if (context.spInfoPoints && $.isArray(context.spInfoPoints)) {
      var points = context.spInfoPoints;
      $(points).each(function(index, point){
        __createMarker(context, point);
      });
    }
  }

  /**
   * Create a styled marker
   */
  function __createMarker(context, spInfoPoint) {
    var mapData = context.getGroupMapData(spInfoPoint.getGroup());
    var marker = new StyledMarker({
      styleIcon : new StyledIcon(StyledIconTypes.BUBBLE, {
        color : context.getGroupColor(spInfoPoint.getGroup()),
        text : spInfoPoint.getName()
      }),
      position : spInfoPoint.getLatLng(),
      map : context.map
    });

    var infoWindow = new google.maps.InfoWindow({
      content  : $('<div>').append(spInfoPoint.render()).html(),
      position : spInfoPoint.getLatLng()
    });

    google.maps.event.addListener(marker, 'click', function() {
      infoWindow.open(context.map, marker);
    });


    mapData.push({point: spInfoPoint, marker: marker, infoWindow : infoWindow});
  }

  /**
   * Calculate center point
   */
  function __calculateMappingCoordinates(context) {
    if (context.spInfoPoints && $.isArray(context.spInfoPoints)) {
      var points = context.spInfoPoints;
      if (points.length > 1) {
        var latitudeMin = 90;
        var latitudeMax = -90;
        var longitudeMin = 180;
        var longitudeMax = -180;
        var curLat;
        $(points).each(function(index, point){
          curLat = point.getLatLng().lat();
          curLng = point.getLatLng().lng();
          if (curLat < latitudeMin) {
            latitudeMin = curLat;
          }
          if (curLat > latitudeMax) {
            latitudeMax = curLat;
          }
          if (curLng < longitudeMin) {
            longitudeMin = curLng;
          }
          if (curLng > longitudeMax) {
            longitudeMax = curLng;
          }
        });
        return new google.maps.LatLng((latitudeMin + latitudeMax) / 2,
                                      (longitudeMin + longitudeMax) / 2);
      } else {
        return points[0].getLatLng();
      }
    }
    return null;
  }
})( jQuery );
