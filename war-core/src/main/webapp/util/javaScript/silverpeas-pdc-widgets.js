/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public Liceanse for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Provides both some functions and widget to work with the PdC in order to edit the classifications
 * on the PdC of resources.
 * 
 * Provides as functions:
 * - a function to load the different axis of the PdC,
 * - a function to load the classification on the PdC of a given resource.
 * 
 * Provides as widgets ready to be used:
 * - a widget to render some positions on the PdC,
 * - a widget to render a preview of some positions on the PdC,
 * - a widget to render the different axis of the PdC in order to create a new
 * position or to update an existing position on the PdC,
 * 
 * Each widget accepts a function for each action it supports. Theses functions will be invoked at
 * action triggering.
 */

/**
 * Build the URI identifying the classification on the PdC of the specified resource.
 * The resource is an object of type:
 * {
 *   context: the name of Silverpeas web application context,
 *   component: the unique identifier of the component instance into which contents are published,
 *   content: the unique identifier of the classified content.
 * }
 */
function uriOfPdCClassification( resource ) {
  var uri = resource.context + '/services/pdc/' + resource.component + '/';
  if (resource.content != null && resource.content.length > 0) {
    uri += resource.content;
  } else {
    uri += 'new';
  }
  return uri;
}

/**************************************************************************************************/

/**
 * Build the URI identifying the PdC parameterized for the specified resource.
 * The resource is an object of type:{
 *   context: the name of Silverpeas web application context,
 *   component: the unique identifier of the component instance for which the PdC has been eventually
 *   parametrized,
 *   content: the unique identifier of the content to classify or null. If this parameter is set,
 *   the axis are then parameterized according to the existing classification of the content (if any).
 * }
 */
function uriOfPdC( resource ) {
  var uri = resource.context + '/services/pdc/' + resource.component;
  if (resource.content != null && resource.content.length > 0) {
    uri += '?contentId=' + resource.content;
  }
  return uri;
}

/**************************************************************************************************/

/**
 * Build the URI identifying the predefined classification on the PdC for the specified resource.
 * The resource is an object of type:
 * {
 *   context: the name of Silverpeas web application context,
 *   component: the unique identifier of the component instance for which a predefined classification
 *   has been be set,
 *   node: the unique identifier of the node for which a predefined classification has been set or
 *   null. If null, the predefined classification for the component instance is fetched.
 * }
 */
function uriOfPredefinedClassification( resource ) {
  var uri = resource.context + '/services/pdc/' + resource.component + '/classification';
  if (resource.node != null && resource.node.length > 0) {
    uri += '?nodeId=' + resource.node;
  }
  return uri;
}

/**************************************************************************************************/

/**
 * Loads the PdC parameterized for the resource located at the specified URI.
 * If the PdC is correctly get, the function onSuccess is then performed, otherwise the function
 * onError is invoked.
 * Both of the functions should accept as parameter the loaded PdC. The onError callback function
 * accepts as additional parameter the error coming from the server. The error is an object of type:
 * {
 *   status: the error status,
 *   message: the error message
 * }
 *
 * The PdC is an object of type:
 * {
 *  uri: the URI of the PdC configured for the resource,
 *  axis: [ the different PdC axis used by the component instance to classify its contents ]
 * }
 * Each axis of the PdC is an object of type:
 * {
 *  id: the identifier of the axis,
 *  name: the localized name of the axis,
 *  originValue: the value used as the origin of the axis,
 *  mandatory: if this axis must be taken into account in a resource classification,
 *  invariant: if the axis is an invariant one (meaning that a classification with this axis can
 *             have only one value, whatever the positions),
 *  invariantValue: if the axis is invariant, the identifier of the invariant value used. If null,
 *                  then the invariant value isn't already set in the classification of the given
 *                  resource,
 *  values: [ the values of this axis ]
 * }
 * Each axis's value is an object of type:
 * {
 *  id: the unique identifier of the value in the form of an absolute path of node identifier in
 *      the sementic tree representing the axis,
 *  treeId: the unique identifier of the sementic tree to which the value belongs (an empty value
 *          means the value is a single one (no hierarchic value representation),
 *  axisId: the unique identifier of the axis to which the value belongs,
 *  term: the localized name of the value,
 *  level: the level of this value in the hierarchic sementic tree from the axis root,
 *  ascendant: is the value an ascendant one from the axis origin that was configured for the
 *             component instance,
 *  origin: is this value the configured (or the default one) axis origin,
 *  synonyms: [ the synonyms of the value term as strings ]
 * }
 */
function loadPdC( uri, onSuccess, onError ) {
  $.ajax({
    url: uri,
    type: 'GET',
    dataType: 'json',
    cache: false,
    success: function(pdc) {
      onSuccess(pdc);
    },
    error: function(jqXHR, textStatus, errorThrown) {
      onError({
        uri: uri, 
        axis: []
      }, { status: jqXHR.status, message: errorThrown });
    }
  })
}
  
/**************************************************************************************************/

/**
 * Loads the classification on the PdC of the resource located by the specified URL.
 * If the classification is correctly get, the function onSuccess is then performed, otherwise the
 * function onError is invoked.
 * Both of the functions should accept as parameter the loaded PdC. The onError callback function
 * accepts as additional parameter the error coming from the server. The error is an object of type:
 * {
 *   status: the error status,
 *   message: the error message
 * }
 * 
 * The classification is an object of type:
 * {
 *   uri: the URI of the classification in the Web,
 *   modifiable: a property indicating if this classification can be edited, 
 *   positions: [ the positions on the PdC ]
 * }
 * Each position on the PdC in a classification is an object of type:
 * {
 *  uri: the URI of the position on the PdC in the Web,
 *  id: the position unique identifier,
 *  values: [ the position's values on some of the PdC's axis ]
 * }
 * Each value on a PdC's axis is an object of type:
 * {
 *  id: the unique identifier of the value in the form of an absolute path of node identifier in
 *      the sementic tree representing the axis,
 *  treeId: the unique identifier of the sementic tree to which the value belongs (an empty value
 *          means the value is a single one (no hierarchic value representation),
 *  axisId: the unique identifier of the axis to which the value belongs,
 *  meaning: the meaning vehiculed by the value. It is either a path of terms in a hierarchic
 *           sementic tree or a single term (for a single value),
 *  synonyms: [ the synonyms of the value term as strings ]
 * }      
 */
function loadClassification( uri, onSuccess, onError ) {
  $.ajax({
    url: uri,
    type: 'GET',
    dataType: 'json',
    cache: false,
    success: function(classification) {
      onSuccess(classification);
    },
    error: function(jqXHR, textStatus, errorThrown) {
      onError({
        uri: uri, 
        positions: []
      }, { status: jqXHR.status, message: errorThrown });
    }
  });
}

/**************************************************************************************************/

/**
 * Asks fo the deletion of the specified position to the classification on the PdC identified by the
 * specified URI.
 * If the position is successfully deleted, then the onSuccess callback function is invoked without
 * any parameters. Otherwise, the optional onError callback function
 * is invoked with as parameters the error coming from the server. If no onError callback
 * is passed then an error message is displayed to the user. The rror is an object of type:
 * {
 *   status: the error status,
 *   message: the error message
 * }
 * If a confirmation message is passed as parameter, a confirmation asking box will be displayed to
 * the user and the position will be effectively deleted once the action confirmed.
 * If no confirmation message is passed as parameter (if null or empty), then the position will be
 * silently deleted.
 */
function deletePosition( uri, position, confirmationMsg, onSuccess, onError ) {
  var confirmed = true;
  if (confirmationMsg != null && confirmationMsg.length > 0)
    confirmed = window.confirm( confirmationMsg );
  if (confirmed) {
    var uri_parts = uri.match(/[a-zA-Z0-9:=\/]+/gi);
    var uri_position = uri_parts[0] + '/' + position.id + '?' + uri_parts[1];
    $.ajax({
      url: uri_position,
      type: "DELETE",
      success: function() {
        onSuccess();
      },
      error: function(jqXHR, textStatus, errorThrown) {
        if (onError == null)
        alert(errorThrown);
      else
        onError({ status: jqXHR.status, message: errorThrown });
      }
    });
  }
}

/**************************************************************************************************/

/**
 * Posts the specified position in the classification on the PdC identified by the specified URI.
 * If the position is successfully posted, then the onSuccess callback function is invoked with as
 * parameter the updated classification on the PdC. Otherwise, the optional onError callback function
 * is invoked with as parameter the error coming from the server. If no onError callback
 * is passed then an error message is displayed to the user. The error is an object of type:
 * {
 *   status: the error status,
 *   message: the error message
 * }
 */
function postPosition( uri, position, onSuccess, onError ) {
  $.ajax({
    url: uri,
    type: 'POST',
    data: $.toJSON(position),
    contentType: "application/json",
    dataType: "json",
    cache: false,
    success: function(classification) {
      onSuccess(classification);
    },
    error: function(jqXHR, textStatus, errorThrown) {
      if (onError == null)
        alert(errorThrown);
      else
        onError({ status: jqXHR.status, message: errorThrown });
    }
  });
}

/**************************************************************************************************/

/**
 * Asks for updating the specified position in the classification on the PdC identified by the specified URI.
 * If the position is successfully updated, then the onSuccess callback function is invoked with as
 * parameter the updated classification on the PdC.  Otherwise, the optional onError callback function
 * is invoked with as parameter the error coming from the server. If no onError callback
 * is passed then an error message is displayed to the user. The error is an object of type:
 * {
 *   status: the error status,
 *   message: the error message
 * }
 */
function updatePosition( uri, position, onSuccess, onError ) {
  var uri_parts = uri.match(/[a-zA-Z0-9:=\/]+/gi);
  var uri_position = uri_parts[0] + '/' + position.id + '?' + uri_parts[1];
  $.ajax({
    url: uri_position,
    type: 'PUT',
    data: $.toJSON(position),
    contentType: "application/json",
    dataType: "json",
    cache: false,
    success: function(classification) {
      onSuccess(classification);
    },
    error: function(jqXHR, textStatus, errorThrown) {
      if (onError == null)
        alert(errorThrown);
      else
        onError({ status: jqXHR.status, message: errorThrown });
    }
  });
}

/**************************************************************************************************/

/**
 * Finds in the specifieds array of positions a position having the specified values on the axis of
 * the PdC.
 * If no such position exists in the array, then returns null. Otherwise is returned an object of
 * type:
 * {
 *   index: the index of the position found in the array of positions,
 *   position: the found position
 * }
 */
function findPosition( withValues, inSomePositions ) {
  var position = null;
  for (var p = 0; p < inSomePositions.length; p++) {
    if (inSomePositions[p].values.length == withValues.length) {
      position = {
        index: p, 
        position: inSomePositions[p]
      };
      for (var v = 0; v < withValues.length; v++) {
        if (withValues[v].id != inSomePositions[p].values[v].id) {
          position = null;
          break;
        }
      }
      if (position != null)
        break;
    }
  }
  return position;
}

/**************************************************************************************************/

/**
 * Is the specified position already in the the specified classification?
 * A position is in a classification if it already exists a position with exactly the same values.
 */
function isAlreadyInClassification( position, classification ) {
  return findPosition(position.values, classification.positions) != null;
}

/**************************************************************************************************/

/**
 * Removes the specified position from the specified array of positions.
 */
function removePosition( position, positions ) {
  for(var i = 0; i < positions.length; i++) {
    if (positions[i].id == position.id) {
      positions.splice(i, 1);
      break;
    }
  }
}

/**************************************************************************************************/

/**
 * The widget to render the positions on the PdC of a resource.
 * According to the settings, the positions can be edited (deleted or updated) and a button to
 * invoke the adding of a new positions can be rendered.
 */
(function( $ ){
  
  var methods = {
    /**
     * The default method when invoking the plugin on an HTML element.
     */
    init: function( options ) {
      var settings = $.extend(true, {
        id                 : "list_pdc_position", /* the HTML element identifier to use for the top element of the widget */
        title              : "Positions", /* the title to display with the widget */
        label              : "Position", /* the label to display with a position index */
        positions          : [], /* the positions to render */
        update             : {
          activated: true, /* is the update of a position activated? */
          title    :     'Editer la position', /* text to render with the update button */
          icon     :      '/silverpeas/util/icons/update.gif' /* icon representing the position update */
        },
        addition           : {
          activated: true, /* is the addition of a new position activated? */
          title    :     'Ajouter une nouvelle position', /* text to render with the addition button */
          icon     :      '/silverpeas/pdcPeas/jsp/icons/add.gif' /* the icon representing the position addition */
        },
        deletion: {
          activated: true, /* is the deletion of a position activated? */
          icon     : '/silverpeas/util/icons/delete.gif', /* text to render with the deletion button */
          title    : 'Supprimer la position' /* the icon representing the position deletion */
        },
        onAddition         : function($this) {}, /* the function to invoke when the adding of a new position is asked. 
                                                    This function can the use the widget to select some values from the PdC's axis in order to create a new position */
        onDeletion         : function($this, position) {}, /* the function to invoke when a position is deleted */
        onUpdate           : function($this, position) {} /* the function to invoke when a position is updated */
      }, options);
      
      return this.each(function() {
        var $this = $(this);
        $this.data('settings', settings);
        renderPositionsFrame($this, settings);
        renderPositions($this, settings);
      });
    },
    
    /**
     * This method is used to refresh the positions listed by this plugin.
     */
    refresh: function( positions ) {
      return this.each(function() {
        var $this = $(this), settings = $this.data('settings');
        settings.positions = positions;
        $('#' + settings.id + '_allpositions').children().remove();
        renderPositions($this, settings);
      });
    }
  };
  
  $.fn.pdcPositions = function( method ) {
    if ( methods[method] ) {
      return methods[method].apply( this, Array.prototype.slice.call( arguments, 1 ));
    } else if ( typeof method === 'object' || ! method ) {
      return methods.init.apply( this, arguments );
    } else {
      $.error( 'Method ' +  method + ' does not exist on jQuery.pdcPositions' );
    }    
  }
  
  function renderPositionsFrame( $this, settings ) {
    $('<div>', {
      id: settings.id
    }).addClass('field').
    append($('<label>', {
      'for': settings.id + '_allpositions'
    }).html(settings.title)).
    append($('<div>', {
      id: settings.id + '_allpositions'
    }).addClass('champs')).
    appendTo($('<div>').addClass('fields').appendTo($this));
  }
  
  function renderPositions( $this, settings ) {
    if (settings.positions.length > 0) {
      $('label[for="' + settings.id + '_allpositions"]').show();
      var positionsSection = $('<ul>').addClass('list_pdc_position').appendTo($("#" + settings.id + '_allpositions'));
      $.each(settings.positions, function(posindex, aPosition) {
        var posId = posindex + 1, values =  [];
        var currentPositionSection = $('<li>').appendTo(positionsSection);
        var positionLabel = $('<span>').addClass('pdc_position').
        html(settings.label + ' ' + posId).appendTo(currentPositionSection);

        $.each(aPosition.values, function(valindex, value) {
          values.push('<li>' + value.meaning + '<i>' + value.synonyms.join(', ') + '</i></li>');
        });

        if (settings.update.activated) {
          positionLabel.append(
            $('<a>',{
              href: '#', 
              title: settings.update.title + ' ' + posId
            }).addClass('edit').
            append($('<img>', {
              src: settings.update.icon,  
              alt: settings.update.title
            }).click(function () {
              settings.onUpdate($this, aPosition);
            })));
        }
         
        if (settings.deletion.activated) {
          positionLabel.append($('<a>', {
            href: '#', 
            title: settings.deletion.title + ' ' + posId
          }).addClass('delete').
            append($('<img>', {
              src: settings.deletion.icon,  
              alt: settings.deletion.title
            }).click(function () {
              settings.onDeletion($this, aPosition);
            })));
        }
          
        currentPositionSection.append($('<ul>').html(values.join('')));
      });
      
    } else {
      $('label[for="' + settings.id + '_allpositions"]').hide();
    }
    if (settings.addition.activated) {
      $('<a>', {
        href: '#'
      }).addClass('add_position').html(settings.addition.title).click(function() {
        settings.onAddition($this);
      }).appendTo($("#" + settings.id + '_allpositions'))
    }
  }
  
})( jQuery );

/**************************************************************************************************/

/**
 * A Widget to render a preview of the positions on the PdC of a resource.
 * This widget is a more simple one that the above as the positions cannot be edited and as 
 * the rendering is more simple.
 */
(function( $ ){
  
  $.fn.pdcPositionsPreview = function( options ) {
    var settings = $.extend(true, {
      id                 : "list_pdc_position", /* the HTML element identifier to use for the top element of the widget */
      title              : "Classement", /* the title to display with the widget */
      positions          : [] /* the positions to render */
    }, options);
    
    return this.each(function() {
      var $this = $(this);
      $this.data('settings', settings);
      renderPositionsFrame($this, settings);
      renderPositions($this, settings);
    });
  };
  
  function renderPositionsFrame( $this, settings ) {
    $('<div>', {
      id: settings.id
    }).append($('<div>', {
      id: settings.id + '_allpositions'
    })).appendTo($this);
  }
  
  function renderPositions( $this, settings ) {
    if (settings.positions.length > 0) {
      
      $('label[for="' + settings.id + '"]').show();
      var positionsSection = $('<ul>').addClass('list_pdc_position').appendTo($("#" + settings.id));
      $.each(settings.positions, function(posindex, aPosition) {
        var posId = posindex + 1, values =  [];
        var currentPositionSection = $('<li>').appendTo(positionsSection);
        var positionLabel = $('<span>').addClass('pdc_position').
        html(settings.lLabel + ' ' + posId).appendTo(currentPositionSection);

        $.each(aPosition.values, function(valindex, value) {
          var text = '<li title="' + value.meaning + '">', path = value.meaning.split('/');
          if (path.length > 2)
            text += path[0] + '/ ... /' + path[path.length -1];
          else text += value.meaning;
          text += '</li>';
          values.push(text);
        });
          
        currentPositionSection.append($('<ul>').html(values.join('')));
      });
      
    } else {
      $('label[for="' + settings.id + '"]').hide();
    }
  }
  
})( jQuery );

/**************************************************************************************************/

/**
 * A Widget to render the axis of the PdC in order to select some of their values to create or to
 * update a position.
 */
(function( $ ){
  
  function getSelectedValues( values ) {
    var selectedValues = [];
    $.each(values, function(i, value) {
      if(value != null) {
        selectedValues.push({
          id: value.id, 
          axisId: value.axisId, 
          treeId: value.treeId,
          meaning: value.meaning,
          synonyms: value.synonyms
        });
      }
    });
    return selectedValues;
  }
  
  function preselectedValuesFrom( values ) {
    var preselectedValues = [];
    $.each(values, function(i, value) {
      preselectedValues[value.axisId] = value;
    });
    return preselectedValues;
  }
  
  function areMandatoryAxisValued( axis, values ) {
    for (var iaxis = 0; iaxis < axis.length; iaxis++) {
      if (axis[iaxis].mandatory) {
        var isValued = false;
        for (var ival = 0; ival < values.length; ival++) {
          if (values[ival].axisId == axis[iaxis].id) {
            isValued = true;
            break;
          }
        }
        if (!isValued) return false;
      }
    }
    return true;
  }
  
  function informOfANewPosition( $this, settings, values ) {
    var selectedValues = getSelectedValues(values);
    if (values.length == 0)
      alert(settings.positionError)
    else if (areMandatoryAxisValued(settings.axis, selectedValues)) {
      if (settings.dialogBox)
        $this.dialog("destroy");
      settings.onValuesSelected($this, selectedValues);
    }
    else
      alert(settings.mandatoryAxisError);
  }
  
  $.fn.pdcAxisValuesSelector = function( options ) {
    var settings = $.extend(true, {
      id                  : "pdc-edition-box", /* the HTML element identifier to use for the top element of the widget */
      title               : "Editer", /* the title to display with the widget */
      positionError       : "Veuillez sélectionner au moins une valeur à la position",
      mandatoryAxisText   : "Veuillez selectionner une valeur",
      mandatoryAxisError  : "Le contenu doit disposer au moins d'une position avec les axes obligatoires",
      mandatoryAxisIcon   : '/silverpeas/util/icons/mandatoryField.gif',
      mandatoryAxisLegend : 'Obligatoire',
      invariantAxisIcon   : '/silverpeas/util/icons/buletColoredGreen.gif',
      invariantAxisLegend : 'invariantes',
      labelOk             : 'Valider', /* the label of the validation button */
      labelCancel         : 'Annuler', /* the label of the canceling button in the dialog box */
      dialogBox           : true, /* is the selector should be displayed as a modal dialog box? */
      axis                : [], /* the different axis of the PdC to render */
      values              : [], /* the values to pre-select in the widget */
      onValuesSelected    : function($this, values) {} /* function invoked when a set of values have been selected through the widget */
    }, options);
    
    return this.each(function() {
      var $this = $(this), selectedValues = preselectedValuesFrom(settings.values), hasMandatoryAxis = false,
      hasInvariantAxis = false;
      $this.children().remove();
      
      // browse the axis of the PdC and for each of them print out a select HTML element
      $.each(settings.axis, function(axisIndex, anAxis) {
        var currentAxisDiv = $('<div>').addClass('champs').appendTo($('<div>').addClass('field').
          append($('<label >', {
            'for': settings.id + '_' + anAxis.id
          }).addClass('txtlibform').html(anAxis.name)).
          appendTo($this));
        var mandatoryField = '';
        if (anAxis.mandatory) {
          mandatoryField = 'mandatoryField'
        }
        var axisValuesSelection = $('<select>', {
          'id': settings.id + '_' + anAxis.id,  
          'name': anAxis.name
        }).addClass(mandatoryField).appendTo(currentAxisDiv).change( function() {
          var theValue = $(this).children(':selected').attr('value');
          if (theValue == 0) {
            selectedValues[anAxis.id] = null;
          } else {
            selectedValues[anAxis.id] = anAxis.values[theValue];
          }
        });
        var path = [];
      
        // browse the values of the current axis and for each of them print out an option HTML element
        // within the select (representing the current axis)
        if (anAxis.mandatory && anAxis.values[anAxis.values.length - 2].ascendant)
          selectedValues[anAxis.id] = anAxis.values[anAxis.values.length - 1];
        $.each(anAxis.values, function(valueIndex, aValue) {
          var level = '';
          path.splice(aValue.level, path.length - aValue.level);
          path[aValue.level] = aValue.term;
          aValue.meaning = path.join(' / ');
          if (aValue.id != '/0/') {
            for (var i = 0; i < aValue.level; i++) {
              level += '&nbsp;&nbsp;';
            }
            var option =
            $('<option>').attr('value', valueIndex).html(level + aValue.term).appendTo(axisValuesSelection);
            if (aValue.ascendant) {
              option.attr('value', 'A').attr('disabled', 'disabled').addClass("intfdcolor51");
            }
            if (anAxis.invariantValue != null && anAxis.invariantValue != aValue.id) {
              selectedValues[anAxis.id] = aValue;
              option.attr('disabled', 'disabled');
            }
            if ((selectedValues[anAxis.id] != null && aValue.id == selectedValues[anAxis.id].id)) {
              option.attr('selected', true);
            }
          }
        });
      
        var option = $('<option>').attr('value', '0').html('&nbsp;').prependTo(axisValuesSelection);
        if (anAxis.mandatory) {
          option.attr('disabled', 'disabled').addClass('emphasis').html(settings.mandatoryAxisText);
        }
        if (selectedValues[anAxis.id] == null) {
          option.attr('selected', true);
        }
        if (selectedValues[anAxis.id] != null) {
          $('<span>').html('<i>' + selectedValues[anAxis.id].synonyms.join(', ') + '</i>&nbsp;').appendTo(currentAxisDiv);
        }
        if (anAxis.mandatory) {
          hasMandatoryAxis = true;
          $('<img>', {
            src: settings.mandatoryAxisIcon,
            alt: settings.mandatoryAxisLegend, 
            width: '5px',
            height: '5px'
          }).appendTo(currentAxisDiv.append(' '));
        }
        if (anAxis.invariant) {
          hasInvariantAxis = true;
          $('<img>', {
            src: settings.invariantAxisIcon, 
            alt: settings.invariantAxisLegend,
            width: '10px',
            height: '10px'
          }).appendTo(currentAxisDiv);
        }
      });
    
      $this.append($('<br>').attr('clear', 'all'));
      if (!settings.dialogBox) {
        $this.append($('<a>').attr('href', '#').
          addClass('valid_position').
          addClass('milieuBoutonV5').
          html(settings.labelOk).click(function() {
            informOfANewPosition($this, settings, selectedValues);
          }));
      }
      if (settings.axis.length > 0) {
        var legende = $('<p>').addClass('legende');
        if (hasMandatoryAxis) {
          legende.append($('<img>', {
            src: settings.mandatoryAxisIcon, 
            alt: settings.mandatoryAxisLegend,
            width: '5px',
            height: '5px'
          })).append($('<span>').html('&nbsp;:' + settings.mandatoryAxisLegend + ' '));
        }
        if (hasInvariantAxis) {
          legende.append(
            $('<img>', {
              src: settings.invariantAxisIcon, 
              alt: settings.invariantAxisLegend,
              width: '10px',
              height: '10px'
            })).
          append($('<span>').html('&nbsp;:' + settings.invariantAxisLegend));
        }
        if (hasMandatoryAxis || hasInvariantAxis) {
          legende.appendTo($this);
        }
      }
      
      if (settings.dialogBox) {
        $this.dialog({
          width: 640,
          modal: true,
          title: settings.title,
          buttons: [{
            text: settings.labelOk,
            click: function() {
              informOfANewPosition($this, settings, selectedValues);
            }
          }, {
            text: settings.labelCancel,
            click: function() {
              $this.dialog("destroy");
            }
          }
          ],
          close: function() {
            $this.dialog("destroy");
          }
        })
      }
    });
  };
  
})( jQuery );



