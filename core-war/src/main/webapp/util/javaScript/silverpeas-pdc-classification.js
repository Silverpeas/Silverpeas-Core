/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
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
 * A JQuery plugin providing some features on the classification of the Silverpeas
 * resources (publication, comments, ...) on the classification plan (named PdC).
 * The supported features are:
 * - preview the classification positions on the PdC of a given content.
 * - render the classification on the PdC of a given content in two modes: edition or view mode. In
 * edition mode, a position on the PdC can be added, deleted or modified in the classification.
 * - render an area to create a classification for a new resource content.
 * - get the positions that were added through the previous function,
 * - validate the classification of a resource content is valid.
 *
 * The classification is expected to be formatted in JSON as:
 * {
 *   uri: the URI of the classification in the Web,
 *   modifiable: a property indicating if this classification can be edited,
 *   positions: [ the positions on the PdC ]
 * }
 * Each position of a classification is represented in JSON as:
 * {
 *  uri: the URI of the position on the PdC in the Web,
 *  id: the position unique identifier,
 *  values: [ the position's values on some of the PdC's axis ]
 * }
 * Each value on a PdC's axis is represented in JSON as:
 * {
 *  id: the unique identifier of the value in the form of an absolute path of node identifier in
 *      the semantic tree representing the axis,
 *  treeId: the unique identifier of the semantic tree to which the value belongs (an empty value
 *          means the value is a single one (no hierarchic value representation),
 *  axisId: the unique identifier of the axis to which the value belongs,
 *  meaning: the meaning carried by the value. It is either a path of terms in a hierarchic
 *           semantic tree or a single term (for a single value),
 *  synonyms: [ the synonyms of the value term as strings ]
 * }
 *
 * In order to edit or to add a position, the PdC configured for the resource is asked to a web
 * service. The sent back PdC is expected to be formatted in JSON as:
 * {
 *  uri: the URI of the PdC configured for the resource,
 *  axis: [ the different PdC axis used by the component instance to classify its contents ]
 * }
 * Each axis is an object formatted in JSON as:
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
 * Each value of an axis should be described in JSON as:
 * {
 *  id: the unique identifier of the value in the form of an absolute path of node identifier in
 *      the semantic tree representing the axis,
 *  treeId: the unique identifier of the semantic tree to which the value belongs (an empty value
 *          means the value is a single one (no hierarchic value representation),
 *  axisId: the unique identifier of the axis to which the value belongs,
 *  term: the localized name of the value,
 *  level: the level of this value in the hierarchic semantic tree from the axis root,
 *  ascendant: is the value an ascendant one from the axis origin that was configured for the
 *             component instance,
 *  origin: is this value the configured (or the default one) axis origin,
 *  synonyms: [ the synonyms of the value term as strings ]
 * }
 */
(function($) {

  /**
   * The parameter settings of the plugin with, for some, the default value.
   */
  var pluginSettings = {
    /**
     * The resource for which the classification on the PdC has to be rendered or edited. It is
     * defined by the web application context under which the resource is published, the component
     * instance that handle it and the identifier of the content to classify or being classified.
     * Optionally, the node to which the content belongs can be indicated. For the creation of a
     * new content, the node has to be indicated so that a default classification associated with it
     * can be requested.
     */
    resource: {
      context: webContext,
      component: '',
      content: '',
      node: ''
    },
    /**
     * The title of the HTML section that will be rendered by this plugin.
     */
    title: 'Classement',
    /**
     * The title of the HTML section within which will be rendered the fields to set or unset the
     * modification attribute of a classification on the PdC
     */
    modificationTitle: 'Validation',
    /**
     * The prefix to use when labelling a given position.
     */
    positionLabel: 'Position',
    /**
     * The label to use for the HTML section into which the positions of the resource will be rendered.
     */
    positionsLabel: 'Positions',
    /**
     * The label to use with a radio button to indicate the classification can be modified.
     */
    canBeModified: "Le contributeur doit valider et peut modifier le classement par défaut",
    /**
     * The label to use with a radio button to indicate the classification cannot be modified.
     */
    cannotBeModified: "Le contributeur ne doit pas valider le classement par défaut",
    /**
     * The label to use for the HTML section into which the positions of a predefined classification
     * coming from a parent node will be rendered.
     */
    inheritedPositionsLabel: "Positions du thème parent",
    /**
     * The messages to print when the validation of a classification fails. The messages depend on
     * the type of the validation failure.
     */
    messages: {
      mandatoryMessage: "Le classement est obligatoire pour la création d'une publication. <br />Veuillez sélectionner une position et la valider.",
      contentMustHaveAPosition: "Le contenu doit disposer au moins d'une position avec les axes obligatoires",
      positionAlreadyInClassification: "La position existe déjà",
      positionMustBeValued: "Veuillez sélectionner au moins une valeur à la position",
      inheritanceMessage: "Vous avez la possibilité de définir un classement par défaut pour les publications de ce thème. <br />Sinon ce thème utilisera le classement défini pour le thème parent."
    },
    /**
     * The different label to use when rendering the HTML section for updating or adding a position
     * onto the PdC
     */
    edition: {
      ok: 'Valider',
      cancel: 'Annuler',
      mandatoryLegend: 'Obligatoire',
      mandatoryIcon: webContext + '/util/icons/mandatoryField.gif',
      invariantLegend: 'invariantes',
      invariantIcon: webContext + '/util/icons/buletColoredGreen.gif',
      mandatoryAxisDefaultValue: 'Veuillez selectionner une valeur'
    },
    /**
     * The attributes of the position adding trigger.
     */
    addition: {
      icon: webContext + '/pdcPeas/jsp/icons/add.gif',
      title: 'Ajouter une nouvelle position'
    },
    /**
     * The attribute of the position update trigger.
     */
    update: {
      icon: webContext + '/util/icons/update.gif',
      title: 'Editer la position'
    },
    /**
     * The attribute of the position deletion trigger with a confirmation message.
     */
    deletion: {
      confirmation: 'Êtes-vous sûr de vouloir supprimer la position ?',
      icon: webContext + '/util/icons/delete.gif',
      title: 'Supprimer la position'
    },
    /**
     * The mode under which this plugin has to be ran. By default, in a view mode.
     * The mode can be either 'view' (for a read-only classification rendering) or 'edition'.
     */
    mode: 'view'
  };

  /**
   * The methods published by the plugin.
   */
  var methods = {
    /**
     * Renders an area within which the different axis of the PdC configured for the specified
     * Silverpeas component are presented in order to create a predefined classification on the PdC
     * for the contents that are published in a given topic of the specified Silverpeas component.
     * The predefined classification is dynamically created and modified by requesting a REST-based
     * web service.
     */
    predefine: function(options) {
      options.mode = 'predefinition';
      return this.each(function() {
        var $this = $(this);
        init($this, options);
        var settings = $this.data('settings');
        loadPdC(settings.pdcURI, function(loadedPdC) {
          loadClassification(settings.defaultClassificationURI, function(loadedClassification) {
            var modification = $('<fieldset>', {
              id: 'classification-mo'
            }).
                    addClass('skinFieldset').appendTo($this);
            var predefinition = $('<fieldset>', {
              id: 'classification-predefinition'
            }).
                    addClass('skinFieldset').
                    data('settings', settings).
                    data('pdc', loadedPdC).
                    data('classification', loadedClassification).
                    appendTo($this);

            var inherited = isInherited(settings, loadedClassification);
            var positionsLabel = settings.labelsPosition;
            if (inherited) {
              positionsLabel = settings.inheritedPositionsLabel;
              $.each(loadedClassification.positions, function(index, position) {
                position.id = null;
              });
            }

            renderModificationAttributeFrame(predefinition, modification);
            prepareClassificationFrame(predefinition);
            renderPositionEditionFrame(predefinition, settings.idPrefix + "pdc-addition-box",
                    settings.addition.title, [], false, function(positions) {
              var classification = predefinition.data('classification');
              if (!areNotAlreadyInClassification(positions, classification)) {
                alert(settings.messages.positionAlreadyInClassification);
              } else {
                for (var i = 0; i < positions.length; i++) {
                  classification.positions.push(positions[i]);
                }
                submitClassification(predefinition, settings.defaultClassificationURI);
              }
            });
            predefinition.pdcPositions({
              id: settings.idPrefix + 'list_pdc_position',
              title: positionsLabel,
              label: settings.labelPosition,
              positions: loadedClassification.positions,
              update: {
                title: settings.update.title,
                icon: settings.update.icon
              },
              addition: {
                activated: false
              },
              deletion: {
                activated: !inherited,
                title: settings.deletion.title,
                icon: settings.deletion.icon
              },
              onUpdate: function(position) {
                openEditionBox(predefinition, settings.defaultClassificationURI, position);
              },
              onDeletion: function(position) {
                if (window.confirm(settings.deletion.confirmation)) {
                  var classification = predefinition.data('classification');
                  removePosition(position, classification.positions);
                  submitClassification(predefinition, settings.defaultClassificationURI);
                }
              }
            });
          }, function(classification, error) {
            $this.data('classification', classification);
            alert(error.message);
          });
        }, function(pdc, error) {
          $this.data('pdc', pdc);
          alert(error.message);
        });
      });
    },
    /**
     * Renders an area within which the different axis of the PdC configured for the specified
     * Silverpeas component are presented in order to create a new classification on the PdC for a
     * not yet published resource content. The positions then can be retrieved with the plugin's
     * function 'positions' and they can be validated by calling the plugin's function
     * 'isClassificationValid' before processing them. As the resource content isn't yet created,
     * it is the responsibility of the client to take into account of the creation of the positions
     * on the PdC added through the plugin.
     */
    create: function(options) {
      options.mode = 'creation';
      return this.each(function() {
        var $this = $(this), classification = new Object();
        init($this, options);
        var settings = $this.data('settings');
        loadPdC(settings.pdcURI, function(loadedPdC) {
          $this.data('pdc', loadedPdC);
          loadClassification(settings.defaultClassificationURI, function(loadedClassification) {
            $this.data('classification', loadedClassification);
            loadedClassification.uri = settings.classificationURI;
            if (loadedClassification.positions.length === 0 || loadedClassification.modifiable) {
              prepareClassificationFrame($this);
              renderPositionEditionFrame($this, settings.idPrefix + "pdc-addition-box",
                      settings.addition.title, [], false, function(positions) {
                var classification = $this.data('classification');
                if (!areNotAlreadyInClassification(positions, classification)) {
                  alert(settings.messages.positionAlreadyInClassification);
                } else {
                  for (var i = 0; i < positions.length; i++) {
                    classification.positions.push(positions[i]);
                  }
                  $this.pdcPositions('refresh', classification.positions);
                }
              });
              $this.pdcPositions({
                id: settings.idPrefix + 'list_pdc_position',
                title: settings.labelsPosition,
                label: settings.labelPosition,
                positions: loadedClassification.positions,
                update: {
                  title: settings.update.title,
                  icon: settings.update.icon
                },
                addition: {
                  activated: false
                },
                deletion: {
                  title: settings.deletion.title,
                  icon: settings.deletion.icon
                },
                onUpdate: function(position) {
                  openEditionBox($this, settings.defaultClassificationURI, position);
                },
                onDeletion: function(position) {
                  if (window.confirm(settings.deletion.confirmation)) {
                    var classification = $this.data('classification');
                    removePosition(position, classification.positions);
                    $this.pdcPositions('refresh', classification.positions);
                  }
                }
              });
            } else {
              $this.attr('style', 'display: none;');
            }
          }, function(classification, error) {
            $this.data('classification', classification);
            alert(error.message);
          });
        }, function(pdc, error) {
          $this.data('pdc', pdc);
          alert(error.message);
        });
      });
    },
    /**
     * Renders an area within which the classification on the PdC of the specified resource is
     * displayed either in a view or an editable mode. If the resource content isn't yet classified
     * on the PdC, an empty classification is opened. In the editable mode, a position can be added,
     * removed or updated dynamically (by requesting a REST-based web service).
     */
    open: function(options) {
      if (options.mode !== 'edition')
        options.mode = 'view';
      return this.each(function() {
        var $this = $(this);
        init($this, options);
        var settings = $this.data('settings');
        loadPdC(settings.pdcURI, function(loadedPdC) {
          $this.data('pdc', loadedPdC);
          loadClassification(settings.classificationURI, function(loadedClassification) {
            $this.data('classification', loadedClassification);
            if (loadedClassification.positions.length === 0 && settings.mode === 'view') {
              $this.hide();
            } else {
              prepareClassificationFrame($this);
              $this.pdcPositions({
                id: settings.idPrefix + 'list_pdc_position',
                title: settings.labelsPosition,
                label: settings.labelPosition,
                positions: loadedClassification.positions,
                update: {
                  activated: (settings.mode === 'edition'),
                  title: settings.update.title,
                  icon: settings.update.icon
                },
                addition: {
                  activated: (settings.mode === 'edition'),
                  title: settings.addition.title,
                  icon: settings.addition.icon
                },
                deletion: {
                  activated: (settings.mode === 'edition'),
                  title: settings.deletion.title,
                  icon: settings.deletion.icon
                },
                onAddition: function() {
                  var classification = $this.data('classification');
                  openEditionBox($this, classification.uri, null);
                },
                onUpdate: function(position) {
                  var classification = $this.data('classification');
                  openEditionBox($this, classification.uri, position);
                },
                onDeletion: function(position) {
                  var classification = $this.data('classification');
                  deletePosition(classification.uri, position, settings.deletion.confirmation,
                          function() {
                            removePosition(position, classification.positions);
                            $this.pdcPositions('refresh', classification.positions);
                          }, function(error) {
                    if (error.status === 409) {
                      alert(settings.messages.contentMustHaveAPosition);
                    }
                  });
                }
              });
            }
          }, function(classification, error) {
            $this.data('classification', classification);
            alert(error.message);
          });
        }, function(pdc, error) {
          $this.data('pdc', pdc);
          alert(error.message);
        });
      });
    },
    /**
     * Renders a preview of the classification on the PdC of a given resource content. If the
     * resource content isn't yet classified, then nothing is displayed.
     */
    preview: function(options) {
      options.mode = 'preview';
      return this.each(function() {
        var $this = $(this);
        init($this, options);
        var settings = $this.data('settings');
        loadPdC(settings.pdcURI, function(loadedPdC) {
          $this.data('pdc', loadedPdC);
          loadClassification(settings.classificationURI, function(loadedClassification) {
            $this.data('classification', loadedClassification);
            if (loadedClassification.positions.length === 0) {
              $this.hide();
            } else {
              prepareClassificationFrame($this);
              $this.pdcPositionsPreview({
                id: settings.idPrefix + 'list_pdc_position',
                label: settings.positionsLabel,
                positions: loadedClassification.positions
              });
            }
          }, function(classification, error) {
            $this.data('classification', classification);
            alert(error.message);
          });
        }, function(pdc, error) {
		$this.hide();
		window.console && window.console.log('Silverpeas \'Taxonomy\' Plugin ERROR : ' + error.message);
        });
      });
    },
    /**
     * Gets the positions on the PdC of the resource. If no positions were set during the use of
     * this plugin, then fetch them from the remote web service. The positions are sent back as
     * the attribute of an object (a classification): {positions: [...]}. If the resource isn't
     * classified onto the PdC, then null is returned.
     * This function is mainly to be used in conjonction with the plugin's function 'create' in
     * order to get the positions that were created with the later.
     */
    positions: function(options) {
      var $this = $(this), classification = $this.data('classification');
      if (!classification) {
        init($this, options);
        var settings = $this.data('settings');
        loadClassification(settings.classificationURI, function(loadedClassification) {
          classification = loadedClassification;
          $this.data('classification', loadedClassification);
        });
        while (!$this.data('classification')) {
        }
      } else {
        if (classification.positions.length === 0)
          classification = null;
      }
      return classification;
    },
    /**
     * Is the classification of the resource valid? The classification is valid if there is at least
     * one position onto the PdC having mandatory axis.
     */
    isClassificationValid: function( ) {
      var $this = $(this), positions = $this.data('classification').positions, axis = $this.data('pdc').axis;
      if (positions.length === 0) {
        return !hasPdCMandoryAxis(axis);
      }
      return true;
    }
  };

  /**
   * The PdC classification namespace.
   */
  $.fn.pdcClassification = function(method, options) {
    if (methods[method]) {
      return methods[ method ].apply(this, Array.prototype.slice.call(arguments, 1));
    } else if (typeof method === 'object' || !method) {
      return methods.init.apply(this, arguments);
    } else {
      $.error('Method ' + method + ' does not exist on jQuery.pdcClassification');
    }
  };

  /**
   * Has the classification plan at least one mandatory axis?
   * When an axis is mandatory, the classification position must have a value onto this axis.
   */
  function hasPdCMandoryAxis(axis) {
    for (var i = 0; i < axis.length; i++) {
      if (axis[i].mandatory) {
        return true;
      }
    }
    return false;
  }

  /**
   * Initializes the plugin with some settings passed as arguments.
   */
  function init($this, options) {
    var settings = $.extend(true, {}, pluginSettings);
    if (options) {
      $.extend(true, settings, options);
    }
    $this.data('settings', settings);
    settings.idPrefix = $this.attr('id') + '_';
    if (!settings.idPrefix)
      settings.idPrefix = '';
    settings.defaultClassificationURI = uriOfPredefinedClassification(settings.resource);
    settings.classificationURI = uriOfPdCClassification(settings.resource);
    settings.pdcURI = uriOfPdC(settings.resource);
  }

  function renderModificationAttributeFrame($this, frame) {
    var settings = $this.data('settings'), classification = $this.data('classification'),
            titleTag = $('<div>').append($('<h4>').addClass('clean').html(settings.modificationTitle));
    if (frame.is('fieldset')) {
      titleTag = $('<legend>').html(settings.modificationTitle);
    }
    titleTag.addClass('header').appendTo(frame);
    $('<div>', {
      id: 'modification'
    }).addClass('field').append($('<input>', {
      type: 'radio',
      name: 'modification',
      value: 'false',
      checked: !classification.modifiable
    })).
            append($('<span>').html(settings.cannotBeModified)).
            append($('<br>')).
            append($('<input>', {
      type: 'radio',
      name: 'modification',
      value: 'true',
      checked: classification.modifiable
    })).
            append($('<span>').html(settings.canBeModified)).appendTo(frame);

    $('.field input:radio').change(function() {
      var classification = $this.data('classification');
      classification.modifiable = $('.field input:radio:checked').val();
      submitClassification($this, settings.defaultClassificationURI);
    });
  }

  /**
   * Prepare the area in which will be rendered the classification on the PdC of a resource.
   */
  function prepareClassificationFrame($this) {
    var settings = $this.data('settings');
    if (settings.mode === 'predefinition' && settings.resource.node)
      $('<div>').addClass('inlineMessage').html(settings.messages.inheritanceMessage).appendTo($this);
    var titleTag = $('<div>').append($('<h4>').addClass('clean').html(settings.title));
    if ($this.is('fieldset')) {
      titleTag = $('<legend>').html(settings.title);
    }
    titleTag.addClass('header').appendTo($this);
    if (settings.mode !== 'preview') {
      var editionBox = $('<div>', {
        id: settings.idPrefix + 'pdc-addition-box'
      }).addClass('pdc-edition-box').addClass('fields').appendTo($this);
      if (settings.mode === 'edition') {
        editionBox.attr("style", "display: none;");
      }
      $('<div>', {
        id: settings.idPrefix + 'pdc-update-box',
        'style': 'display-none'
      }).addClass('pdc-edition-box').addClass('fields').appendTo($this);
    }
  }

  function openEditionBox($this, uri, position) {
    var settings = $this.data('settings');
    var boxId = settings.idPrefix + "pdc-addition-box";
    var title = settings.addition.title;
    var preselectedValues = [];
    if (position && position.values.length > 0) {
      boxId = settings.idPrefix + "pdc-update-box";
      title = settings.update.title;
      preselectedValues = position.values;
    }
    renderPositionEditionFrame($this, boxId, title, preselectedValues, true, function(positions) {
      var selection = [], classification = $this.data('classification');
      for (var i = 0; i < positions.length; i++) {
        var selectedPosition = positions[i];
        if (position && position.values.length > 0) {
          selectedPosition = $.extend(true, {}, position);
          selectedPosition.values = positions[i].values;
        }
        if (isAlreadyInClassification(selectedPosition, classification)) {
          alert(settings.messages.positionAlreadyInClassification);
          selection = [];
          break;
        } else {
          if (settings.mode !== 'edition') {
            var location = findPosition(position.values, classification.positions);
            if (location) {
              classification.positions[location.index] = selectedPosition;
            } else {
              classification.positions.push(selectedPosition);
            }
          } else {
            selection.push(selectedPosition);
          }
        }
      }
      if (settings.mode === 'predefinition') {
        submitClassification($this, uri);
      } else if (settings.mode === 'creation') {
        $this.pdcPositions('refresh', classification.positions);
      } else if (selection.length > 0) {
        for (var s = 0; s < selection.length; s++) {
          submitPosition($this, uri, selection[s]);
        }
      }
    });
  }

  function renderPositionEditionFrame($this, frameId, title, preselectedValues, asDialogBox, onEdition) {
    var settings = $this.data('settings'), positionSavingLabel = settings.edition.ok;
    if (!asDialogBox && frameId.indexOf('pdc-addition-box'))
      positionSavingLabel = settings.addition.title;
    $('#' + frameId).pdcAxisValuesSelector({
      title: title,
      positionError: settings.messages.positionMustBeValued,
      mandatoryAxisText: settings.edition.mandatoryAxisDefaultValue,
      mandatoryAxisError: settings.messages.contentMustHaveAPosition,
      mandatoryAxisIcon: settings.edition.mandatoryIcon,
      mandatoryAxisLegend: settings.edition.mandatoryLegend,
      invariantAxisIcon: settings.edition.invariantIcon,
      invariantAxisLegend: settings.edition.invariantLegend,
      labelOk: positionSavingLabel,
      labelCancel: settings.edition.cancel,
      axis: $this.data('pdc').axis,
      values: preselectedValues,
      dialogBox: asDialogBox,
      multiValuation: frameId.indexOf('pdc-addition-box') > -1,
      onValuesSelected: function(positions) {
        onEdition(positions);
      }
    });
  }

  function submitClassification($this, uri) {
    var method = 'POST', classification = $this.data('classification'), settings = $this.data('settings');
    if (!isInherited(settings, classification)) {
      method = 'PUT';
    } else {
      if (classification.positions.length === 0) {
        return;
      }
      $.each(classification.positions, function(index, position) {
        position.id = null;
      });
    }
    $.ajax({
      url: uri,
      type: method,
      data: $.toJSON(classification),
      contentType: "application/json",
      dataType: "json",
      cache: false,
      success: function(newClassification) {
        $this.data('classification', newClassification);
        var newParameters = null;
        if (isInherited(settings, newClassification)) {
          newParameters = {
            title: settings.inheritedPositionsLabel,
            deletion: false
          };
          $('.field input[value="' + newClassification.modifiable + '"]:radio').attr('checked', true);
        } else {
          newParameters = {
            title: settings.positionsLabel,
            deletion: true
          };
        }
        $this.pdcPositions('refresh', newClassification.positions, newParameters);
      },
      error: function(jqXHR, textStatus, errorThrown) {
        alert(errorThrown);
      }
    });
  }

  /**
   * Submit a new or an updated position into the classification on the PdC of the resource
   * identified by the specified URI.
   */
  function submitPosition($this, uri, position) {
    if (position.id) {
      updatePosition(uri, position, function(classification) {
        $this.data('classification', classification);
        $this.pdcPositions('refresh', classification.positions);
      });
    } else {
      postPosition(uri, position, function(classification) {
        $this.data('classification', classification);
        $this.pdcPositions('refresh', classification.positions);
      });
    }
  }

  function isInherited(settings, classification) {
    return settings.mode === 'predefinition' &&
            escape(classification.uri).search(escape(settings.defaultClassificationURI)) === -1;
  }

})(jQuery);
