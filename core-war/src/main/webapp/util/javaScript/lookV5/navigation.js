/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
var currentSpaceId = "-1";
var currentSpaceLevel = 0;
var currentSpacePath = "";
var currentRootSpaceId = "-1";
var currentComponentId = "";
var currentAxisId = "-1";
var currentValuePath = "-1";
var displayComponentIcons = false;

var notContextualPDCDisplayed = false;
var notContextualPDCLoaded = false;
// User favorite space variable true/false => enable/disable
var displayUserFavoriteSpace = false;
// When user favorite space is enabled, this following parameter enable/disable the "contains sub favorite space" state.
var enableAllUFSStates = false;

var displayContextualPDC = true;

String.prototype.startsWith = function(str) {
  return this.indexOf(str) === 0;
};


function openMySpace(options) {
  options = extendsObject({
    itemIdToSelect : undefined
  }, options);
  var mySpaceContainer = document.querySelector("#spacePerso");
  var spaceContent = document.getElementById("contentSpace" + "spacePerso");
  var displayMySpace = !spaceContent;
  if (displayMySpace) {
    var args = ['getSpaceInfo', 'ResponseId=spaceUpdater', 'Init=0', 'SpaceId=-10'];
    if (options.itemIdToSelect) {
      args.push('ComponentId=' + options.itemIdToSelect);
    }
    ajaxEngine.sendRequest.apply(this, args).then(function() {
      if (options.itemIdToSelect) {
        __selectComponentItem(options.itemIdToSelect);
      }
      refreshPDCFrame();
    });
    var lastStartLoadTime = Math.max(spLayout.getBody().getContent().getLastStartLoadTime(),
        spLayout.getBody().getLastStartLoadTime());
    var timeElapsedSinceContentStartLoadInMs = new Date().getTime() - lastStartLoadTime;
    var isContentLoadedManually = timeElapsedSinceContentStartLoadInMs < 2000;
    if (!isContentLoadedManually) {
      try {
        var homePage = getPersoHomepage();
        if (homePage.indexOf('?', 0) > 0) {
          homePage = homePage + '&SpaceId=-10';
        } else {
          homePage = homePage + '?SpaceId=-10';
        }
        spWindow.loadContent(getContext() + homePage);
      } catch (e) {
        homePage = getHomepage();
        if (homePage.indexOf('?', 0) > 0) {
          homePage = homePage + '&SpaceId=-20';
        } else {
          homePage = homePage + '?SpaceId=-20';
        }
        spWindow.loadContent(getContext() + homePage);
      }
    }

    if (currentSpaceId !== "-1") {
      closeSpace(currentSpaceId, currentSpaceLevel, true);
      hideTransverseSpace();
    }

    mySpaceContainer.classList.remove("spaceLevelPerso");
    mySpaceContainer.classList.add("spaceLevelPersoOn");
  }
  else {
    var space = spaceContent.parentNode;
    space.removeChild(spaceContent);
    mySpaceContainer.classList.add("spaceLevelPerso");
    mySpaceContainer.classList.remove("spaceLevelPersoOn");
  }
}

function openSpace(spaceId, spaceLevel) {
  closeCurrentComponent();
  if (currentSpaceId === spaceId) {
    closeSpace(spaceId, currentSpaceLevel, true);
    // refreshing the space navigation menu
    ajaxEngine.sendRequest('getSpaceInfo', 'ResponseId=spaceUpdater', 'Init=0',
            'GetPDC=' + displayPDC(), 'SpaceId=' + spaceId);
  }
  else {
    const closePDC = (spaceLevel === 0);
    if (currentSpaceId !== "-1" && spaceLevel === currentSpaceLevel) {
      closeSpace(currentSpaceId, currentSpaceLevel, closePDC);

      if (spaceLevel === 0) {
        hideTransverseSpace();
      }
    }
    else {
      if (spaceLevel === 0 && currentSpacePath.length > 0) {
        closeSpace(currentSpacePath.substring(0, currentSpacePath.indexOf("/")), 0, closePDC);
        hideTransverseSpace();
        currentSpacePath = spaceId;
      }
    }
    if (spaceLevel === currentSpaceLevel) {
      currentSpacePath = currentSpacePath.substring(0,
              currentSpacePath.lastIndexOf("/") + 1) + spaceId;
    }
    if (spaceLevel > currentSpaceLevel) {
      currentSpacePath += "/" + spaceId;
    }
    try {
      // temporary loading message
      const imgSpace = document.getElementById("img" + spaceId);
      imgSpace.setAttribute("src", "icons/silverpeasV5/loading.gif");
      imgSpace.setAttribute("width", "16");
      imgSpace.setAttribute("height", "22");
      imgSpace.setAttribute("align", "absmiddle");
    } catch (e) {
    }
    // refreshing the space navigation menu
    ajaxEngine.sendRequest('getSpaceInfo', 'ResponseId=spaceUpdater', 'Init=0',
            'GetPDC=' + displayPDC(), 'SpaceId=' + spaceId);
  }

  currentSpaceId = spaceId;
  currentSpaceLevel = spaceLevel;

  spWindow.loadContent(getContext() + getHomepage() + "?SpaceId=" + spaceId);

  refreshPDCFrame();

  try {
    openSpecificLookSpace(spaceId, spaceLevel);
  } catch (e) {
    //all looks don't need this callback function
  }
}

function refreshPDCFrame() {
  displayPDCFrame(currentSpaceId, currentComponentId);
}

function displayPDCFrame(spaceId, componentId) {
  if (displayContextualPDC) {
    try {
      spLayout.getFooter().loadPdc({
        "spaces" : spaceId,
        "componentSearch" : componentId,
        "FromPDCFrame" : "true"
      });
    }
    catch (e) {
      //frame named 'pdcFrame' does not exist
    }
  }
}

function hideTransverseSpace() {
  try {
    document.getElementById("spaceTransverse").innerHTML = "";
    document.getElementById("spaceTransverse").style.display = "none";
    document.getElementById("basSpaceTransverse").style.display = "none";
  }
  catch (e) {
    //one of this elements are not present
  }
}

function showTransverseSpace() {
  try {
    document.getElementById("spaceTransverse").style.display = "";
    document.getElementById("basSpaceTransverse").style.display = "";
  }
  catch (e) {
    //one of this elements are not present
  }
}

function closeSpace(spaceId, spaceLevel, closePDC) {
  if (spaceLevel === 0) {
    var spaceHeader = document.getElementById(spaceId);
    if (spaceHeader) {
      spaceHeader.setAttribute("class", "spaceLevel1");
      spaceHeader.setAttribute("className", "spaceLevel1");
    }
  }

  try {
    var spaceContent = document.getElementById("contentSpace" + spaceId);
    var space = spaceContent.parentNode;
    space.removeChild(spaceContent);
  }
  catch (e) {
    //closed space have no content
  }

  if (closePDC) {
    removePDC();
  }

  currentSpaceId = "-1";
}

var __selectComponentItem = function(componentId) {
  if (componentId !== currentComponentId) {
    closeCurrentComponent();
  }
  sp.element.querySelectorAll('#' + componentId, spLayout.getBody().getNavigation().getContainer())
      .forEach(function($activeComponentItem) {
        $activeComponentItem.setAttribute("class", "browseComponentActiv");
        $activeComponentItem.setAttribute("className", "browseComponentActiv");
        var $componentImg = $activeComponentItem.querySelector("img[id^='img']");
        if ($componentImg) {
          $componentImg.src = getContext() + "/admin/jsp/icons/silverpeasV5/activComponent.gif";
          $componentImg.width = "20";
          $componentImg.height = "8";
        }
      });

  currentAxisId = "-1";
  currentValuePath = "-1";

  currentComponentId = componentId;
};

function openComponent(componentId, componentLevel, componentURL) {
  if (componentId === 'notifAdmins') {
    sp.messager.open(null, {recipientUsers: 'Administrators', recipientEdition: false});
  } else {
    var timeElapsedSinceContentStartLoadInMs = new Date().getTime() -
        spLayout.getBody().getContent().getLastStartLoadTime();
    var isContentLoadedManually = timeElapsedSinceContentStartLoadInMs < 200;
    __selectComponentItem(componentId);

    //Remove active class on subtree
    jQuery("#" + componentId).parent().find(".spaceOn").removeClass("spaceOn");

    if (componentURL.substring(0, 11).toLowerCase() !== "javascript:") {
      if (!isContentLoadedManually) {
        spWindow.loadContent(getContext() + componentURL);
      }
    } else {
      console.log('ALERT navigation.js#openComponent(componentId, componentLevel, componentURL): componentURL is a javascript statement: ', componentURL);
      eval(componentURL);
    }
  }

  //Envoi de la requete pour afficher le plan de classement du composant
  ajaxEngine.sendRequest('getSpaceInfo', 'ResponseId=spaceUpdater', 'Init=0',
          'GetPDC=' + displayPDC(), 'ComponentId=' + currentComponentId).then(function() {

    refreshPDCFrame();

    try {
      openSpecificLookComponent(componentId, componentLevel);
    } catch (e) {
      //all looks don't need this callback function
    }
  });
}

function closeCurrentComponent() {
  sp.element.querySelectorAll('.browseComponentActiv', spLayout.getBody().getNavigation().getContainer())
      .forEach(function($activeComponentItem) {
        $activeComponentItem.setAttribute("class", "browseComponent");
        $activeComponentItem.setAttribute("className", "browseComponent");
        var $itemImg = $activeComponentItem.querySelector("img[id^='img']");
        if ($itemImg) {
          $itemImg.src = "icons/1px.gif";
          $itemImg.width = "1";
          $itemImg.height = "1";
        }
      });
  currentComponentId = '';
}

function pdcAxisExpand(axisId) {
  if (currentAxisId !== "-1") {
    pdcAxisCollapse(currentAxisId);
  }

  currentAxisId = axisId;
  currentValuePath = "/0/";

  var img = document.getElementById("imgAxis" + axisId);
  img.setAttribute("src", "icons/silverpeasV5/loading.gif");
  img.setAttribute("width", "16");
  img.setAttribute("height", "22");
  img.setAttribute("align", "absmiddle");
  document.getElementById("jsAxis" + axisId).setAttribute("href",
          "javaScript:pdcAxisCollapse('" + axisId + "')");

  //Envoi de la requete pour afficher le contenu de l'axe
  if (isPDCContextual()) {
    ajaxEngine.sendRequest('getSpaceInfo', 'ResponseId=spaceUpdater', 'Init=0',
            'SpaceId=' + currentSpaceId, 'ComponentId=' + currentComponentId, 'AxisId=' + axisId,
            'ValuePath=' + currentValuePath);
  }
  else {
    ajaxEngine.sendRequest('getSpaceInfo', 'ResponseId=spaceUpdater', 'Init=0', 'AxisId=' + axisId,
            'ValuePath=' + currentValuePath);
  }
}

function pdcAxisCollapse(axisId) {
  currentAxisId = "-1";
  currentValuePath = "-1";

  document.getElementById("imgAxis" + axisId).setAttribute("src",
          "icons/silverpeasV5/pdcPeas_maximize.gif");
  document.getElementById("jsAxis" + axisId).setAttribute("href",
          "javaScript:pdcAxisExpand('" + axisId + "')");

  var value = document.getElementById("axisContent" + axisId).firstChild;
  while (value != null) {
    document.getElementById("axisContent" + axisId).removeChild(value);
    value = document.getElementById("axisContent" + axisId).firstChild;
  }
}

function pdcAxisSearch(axisId) {
  currentValuePath = "/0/";
  var query = getContext() + "/RpdcSearch/jsp/AdvancedSearch?mode=clear&ShowResults=1&searchType=2&AxisId=" + axisId + "&ValueId=" + currentValuePath;
  if (isPDCContextual()) {
    query += "&componentSearch=" + currentComponentId + "&spaces=" + currentSpaceId;
  }

  spWindow.loadContent(query);
}

function pdcValueSearch(valuePath) {
  var query = getContext() + "/RpdcSearch/jsp/AdvancedSearch?mode=clear&ShowResults=1&searchType=2&AxisId=" + currentAxisId + "&ValueId=" + valuePath;
  if (isPDCContextual()) {
    query += "&componentSearch=" + currentComponentId + "&spaces=" + currentSpaceId;
  }

  spWindow.loadContent(query);
}

function pdcValueExpand(valuePath) {
  currentValuePath = valuePath;

  var img = document.getElementById("imgValue" + valuePath);
  img.setAttribute("src", getContext() + "/admin/jsp/icons/silverpeasV5/loading.gif");
  img.setAttribute("width", "16");
  img.setAttribute("height", "22");
  img.setAttribute("align", "absmiddle");
  document.getElementById("jsValue" + valuePath).setAttribute("href",
          "javaScript:pdcValueCollapse('" + valuePath + "')");

  //Envoi de la requete pour afficher le contenu de la valeur de l'axe
  if (isPDCContextual()) {
    ajaxEngine.sendRequest('getSpaceInfo', 'ResponseId=spaceUpdater', 'Init=0',
            'SpaceId=' + currentSpaceId, 'ComponentId=' + currentComponentId, 'AxisId=' + currentAxisId,
            'ValuePath=' + valuePath);
  }
  else {
    ajaxEngine.sendRequest('getSpaceInfo', 'ResponseId=spaceUpdater', 'Init=0',
            'AxisId=' + currentAxisId, 'ValuePath=' + valuePath);
  }
}

function pdcValueCollapse(valuePath) {
  document.getElementById("imgValue" + valuePath).setAttribute("src",
          "icons/silverpeasV5/pdcPeas_maximize.gif");
  document.getElementById("jsValue" + valuePath).setAttribute("href",
          "javaScript:pdcValueExpand('" + valuePath + "')");

  var value = document.getElementById("valueContent" + valuePath).firstChild;
  while (value != null) {
    document.getElementById("valueContent" + valuePath).removeChild(value);
    value = document.getElementById("valueContent" + valuePath).firstChild;
  }
}

function displayPDCNotContextual() {
  if (notContextualPDCDisplayed) {
    notContextualPDCDisplayed = false;
    //emptyPDC();
    document.getElementById("pdc").style.visibility = "hidden";
  }
  else {
    notContextualPDCDisplayed = true;
    document.getElementById("pdc").style.visibility = "visible";
    if (notContextualPDCLoaded === false) {
      notContextualPDCLoaded = true;
      ajaxEngine.sendRequest('getSpaceInfo', 'ResponseId=spaceUpdater', 'Init=0', 'Pdc=1');
    }
  }
  return;
}

function emptyPDC() {
  try {
    var pdc = document.getElementById("pdc");
    pdc.innerHTML = "";
  }
  catch (e) {
  }
}

function isPDCContextual() {
  var contextualPDC = true;
  try {
    contextualPDC = displayContextualPDC();
  } catch (e) {
  }
  return contextualPDC;
}

function removePDC() {
  if (isPDCContextual()) {
    try {
      var pdc = document.getElementById("pdc");
      var space = pdc.parentNode;
      space.removeChild(pdc);
    }
    catch (e) {
      //closed space have no pdc
    }
  }
}

/**
 * This method return current DOM user menu display mode
 * @return string representation of the user menu display mode
 */
function getUserMenuDisplayMode() {
  if (document.getElementById("userMenuDisplayModeId")) {
    // Check value to enable/disable user favorite space
    var userMenuDispMode = document.getElementById("userMenuDisplayModeId").value;
    if (userMenuDispMode === "BOOKMARKS" || userMenuDispMode === "ALL") {
      displayUserFavoriteSpace = true;
      // Check contains user favorite space mode
      enableAllUFSStates = (jQuery("#enableAllUFSpaceStatesId").val() == "true") ? true : false;
    }
    return userMenuDispMode;
  }
  displayUserFavoriteSpace = false;
  return "";
}

/**
 * This Ajax method loads the user space menu
 *
 * @param tabId : the tabbed pan identifier
 */
function openTab(tabId) {
  //alert("opentTab(" + tabId + ") call");
  // Check tab change
  if (tabId !== jQuery("#userMenuDisplayModeId").val()) {
    // Check tab change
    if (tabId === 'ALL') {
      jQuery("#tabsBookMarkSelectedDivId").hide();
      jQuery("#tabsAllSelectedDivId").show();
    } else {
      jQuery("#tabsAllSelectedDivId").hide();
      jQuery("#tabsBookMarkSelectedDivId").show();
    }
    jQuery("#userMenuDisplayModeId").val(tabId);
    jQuery("#spaceMenuDivId").mask(jQuery("#loadingMessageId").val());

    jQuery.ajax({
      url: getContext() + '/RAjaxSilverpeasV5/dummy',
      data: {ResponseId: 'spaceUpdater',
        Init: 1,
        UserMenuDisplayMode: tabId},
      success: function(data) {
        if (jQuery("#spaceMenuDivId").isMasked()) {
          jQuery("#spaceMenuDivId").unmask();
        }
        //alert("Success Data Loaded: data=" + data);
        spaceUpdater = new SpaceUpdater();
        var xmlResponse = data.getElementsByTagName("response")[0];
        spaceUpdater.ajaxUpdate(xmlResponse);
        spaceUpdater.displayTree(xmlResponse.childNodes[0]);
      },
      error: function() {
        notyError("XMLHttpRequest error ");
      },
      dataType: 'xml'
    });
  }
}

/**
 * This Ajax method check the current user space status
 * and change current user favorite space status.
 *
 * @param spaceId : the space identifier we have to change
 */
function changeFavoriteSpace(spaceId) {
  var curImg = jQuery("#favoriteimg" + spaceId);
  var curState = curImg.attr("title");
  if (curState === "favorite") {
    removeFavoriteSpace(spaceId);
  } else if (curState === "favorite_empty" || curState === "favorite_contains") {
    addFavoriteSpace(spaceId);
  }
}

/**
 * This Ajax method add favorite space
 *
 * @param spaceId : the added space identifier
 */
function addFavoriteSpace(spaceId) {
  //alert("addFavoriteSpace(" + spaceId + ") call");

  jQuery.ajax({
    url: getContext() + '/RAjaxAction/userMenu',
    data: {Action: 'addSpace',
      SpaceId: spaceId},
    success: function(data) {
      //updateUserFavoriteSpaceStatus
      jQuery.each(data.spaceids, function(i, item) {
        enableUserFavoriteSpaceStatus(item.spaceid);
      });
      // Check if contains states is enabled to update parent space status
      if (enableAllUFSStates) {
        jQuery.each(data.parentids, function(i, item) {
          enableUserFavoriteParentStatus(item.spaceid);
        });
      }
    },
    error: function() {
      notyError("XMLHttpRequest error ");
    },
    dataType: 'json'
  });
}

var messageBox = null;


/**
 * This Ajax method remove favorite space
 *
 * @param spaceId : the added space identifier
 */
function removeFavoriteSpace(spaceId) {
  //alert("removeFavoriteSpace(" + spaceId + ") call");

  jQuery.ajax({
    url: getContext() + '/RAjaxAction/userMenu',
    data: {Action: 'removeSpace',
      SpaceId: spaceId},
    success: function(data) {
      // Check AJAX servlet response
      if (data.success) {
        // Retrieve current space identifier or many others identifiers
        disableUserFavoriteSpaceStatus(spaceId, data);
      } else {
        // handle servlet response failure (display message box with current error message

      }

    },
    error: function() {
      notyError("XMLHttpRequest error ");
    },
    dataType: 'json'
  });
}

function updateUserFavoriteSpaceStatus(spaceId) {
  var curDiv = jQuery("#favoriteimg" + spaceId);
  var curState = curDiv.attr("title");
  if (curState === "favorite") {
    curDiv.addClass("favorite_empty");
    curDiv.removeClass("favorite");
  } else if (curState === "favorite_empty" || curState === "favorite_contains") {
    curDiv.addClass("favorite");
    curDiv.removeClass("favorite_empty");
  }
}

/**
 * Disable user favorite space status
 */
function disableUserFavoriteSpaceStatus(spaceId, data) {
  var curDiv = jQuery("#favoriteimg" + spaceId);
  if (enableAllUFSStates) {
    if (data.spacestate === "contains") {
      curDiv.attr("src", "/silverpeas/util/icons/iconlook_contains_favorites_12px.gif");
      curDiv.attr("title", "favorite_contains");
    } else {
      curDiv.attr("src", "/silverpeas/util/icons/iconlook_favorites_empty_12px.gif");
      curDiv.attr("title", "favorite_empty");
    }
    jQuery.each(data.parentids, function(i, item) {
      disableParentSpaceStatus(item.spaceid, item.spacestate);
    });
  } else {
    curDiv.attr("src", "/silverpeas/util/icons/iconlook_favorites_empty_12px.gif");
    curDiv.attr("title", "favorite_empty");
  }
}

function disableParentSpaceStatus(spaceId, spaceStatus) {
  var curDiv = jQuery("#favoriteimg" + spaceId);
  if (spaceStatus === "contains") {
    curDiv.attr("src", "/silverpeas/util/icons/iconlook_contains_favorites_12px.gif");
    curDiv.attr("title", "favorite_contains");
  } else if (spaceStatus === "favorite") {
    curDiv.attr("src", "/silverpeas/util/icons/iconlook_favorites_12px.gif");
    curDiv.attr("title", "favorite");
  } else {
    curDiv.attr("src", "/silverpeas/util/icons/iconlook_favorites_empty_12px.gif");
    curDiv.attr("title", "favorite_empty");
  }
}

function enableUserFavoriteSpaceStatus(spaceId) {
  var curDiv = jQuery("#favoriteimg" + spaceId);

  if (curDiv) {
    curDiv.attr("src", "/silverpeas/util/icons/iconlook_favorites_12px.gif");
    curDiv.attr("title", "favorite");
  }
}

function enableUserFavoriteParentStatus(spaceId) {
  var curDiv = jQuery("#favoriteimg" + spaceId);
  if (curDiv) {
    //alert("curDiv.attr(title) = " + curDiv.attr("title"));
    if (curDiv.attr("title") === "favorite_empty") {
      curDiv.attr("title", "favorite_contains");
      curDiv.attr("src", "/silverpeas/util/icons/iconlook_contains_favorites_12px.gif");
    }
  }
}

function displayFavoriteSpaceIcon(space, spaceId, newSpace) {
  if (displayUserFavoriteSpace && jQuery("#userMenuDisplayModeId").val() === "ALL") {
    var favSpace = space.getAttribute("favspace");
    var favDiv = document.createElement("div");
    favDiv.setAttribute("id", "favdiv");
    var spaceActionLink = document.createElement("a");
    spaceActionLink.setAttribute("onfocus", "this.blur()");
    spaceActionLink.setAttribute("href", "javaScript:changeFavoriteSpace('" + spaceId + "');");
    var imgFavorite = document.createElement("img");
    imgFavorite.setAttribute("id", "favoriteimg" + spaceId);
    // Be careful to not change title value (because of external JQuery reference)
    if (favSpace == "true") {
      imgFavorite.setAttribute("src", "/silverpeas/util/icons/iconlook_favorites_12px.gif");
      imgFavorite.setAttribute("title", "favorite");
    } else if (favSpace === "contains" && enableAllUFSStates) {
      imgFavorite.setAttribute("src",
              "/silverpeas/util/icons/iconlook_contains_favorites_12px.gif");
      imgFavorite.setAttribute("title", "favorite_contains");
    } else { // false
      imgFavorite.setAttribute("src", "/silverpeas/util/icons/iconlook_favorites_empty_12px.gif");
      imgFavorite.setAttribute("title", "favorite_empty");
    }
    spaceActionLink.appendChild(imgFavorite);
    favDiv.appendChild(spaceActionLink);
    newSpace.appendChild(favDiv);
  }
}

var SpaceUpdater = SilverpeasClass.extend({
  initialize: function() {
    this.useHighlighting = true;
    this.lastPersonSelected = null;
  },
  ajaxUpdate: function(ajaxResponse) {
    //alert("ajaxUpdate call, ajaxResponse=" + ajaxResponse);
    var nbElements = ajaxResponse.childNodes.length;
    if (!nbElements) {
      return;
    }
    //console.log("nbElements="+nbElements + ",ajaxResponse.childNodes[0].tagName=" + ajaxResponse.childNodes[0].tagName);
    if (ajaxResponse.childNodes[0].tagName === "spacePerso") {
      this.displayMySpace(ajaxResponse.childNodes[0]);
    }
    else {
      //console.log("currentSpaceId=" + currentSpaceId);
      if (currentSpaceId == "-1") {
        if (ajaxResponse.childNodes[0].tagName === "item") {
          //it's a transversal space
          this.displaySpaceTransverse(ajaxResponse.childNodes[0], "true");

          //display others spaces
          this.displayTree(ajaxResponse.childNodes[1]);
          this.displayAxis(ajaxResponse.childNodes[2]);
        }
        else {
          if (ajaxResponse.childNodes[0].tagName === "spaces") {
            this.displayTree(ajaxResponse.childNodes[0]);
            this.displayAxis(ajaxResponse.childNodes[1]);
          }
          else {
            if (ajaxResponse.childNodes[0].tagName === "pdc") {
              if (currentAxisId !== "-1") {
                this.displayValue(ajaxResponse.childNodes[0]);
              }
              else {
                this.displayAxis(ajaxResponse.childNodes[0]);
              }
            }
          }
        }
      }
      else {
        if (currentSpaceId !== -1) {
          //hide inProgress image
          var imgSpace = document.getElementById("img" + currentSpaceId);
          try {
            imgSpace.setAttribute("src", "icons/1px.gif");
            imgSpace.setAttribute("width", "0");
            imgSpace.setAttribute("height", "0");
          }
          catch (e) {
          }
        }
        if (nbElements === 2) {
          var child = ajaxResponse.childNodes[0];
          var type = child.getAttribute("type");
          if (type === "spaceTransverse") {
            this.displaySpaceTransverse(ajaxResponse.childNodes[0]);
          }
          else {
            this.displaySpace(ajaxResponse.childNodes[0], "false");
          }
          this.displayAxis(ajaxResponse.childNodes[1]);
        }
        else {
          if (ajaxResponse.childNodes[0].tagName === "pdc") {
            if (currentAxisId !== "-1") {
              this.displayValue(ajaxResponse.childNodes[0]);
            }
            else {
              this.displayAxis(ajaxResponse.childNodes[0]);
            }
          }
        }
      }
    }
  },
  displayTree: function(tree) {
    //alert("displayTree call ... reset spaces innerHTML");
    document.getElementById("spaces").innerHTML = "";
    var nbSpaces = tree.childNodes.length;
    //alert("nb spaces = "+nbSpaces);
    for (var i = 0; i < nbSpaces; i++) {
      var space = tree.childNodes[i];

      //create new entry
      var spaceId = space.getAttribute("id");
      var open = space.getAttribute("open");
      var look = space.getAttribute("look");
      var css = space.getAttribute("css");

      var newSpaceURL = document.createElement("a");
      newSpaceURL.setAttribute("href",
              "javaScript:openSpace('" + spaceId + "', 0, '" + look + "', '" + css + "')");
      newSpaceURL.setAttribute("onfocus", "this.blur()");
      newSpaceURL.setAttribute("class", "spaceURL");
      newSpaceURL.setAttribute("className", "spaceURL");

      var newSpaceLabel = document.createTextNode(space.getAttribute("name"));
      newSpaceURL.appendChild(newSpaceLabel);

      var imgSpace = document.createElement("img");
      imgSpace.setAttribute("id", "img" + spaceId);
      imgSpace.setAttribute("src", "icons/1px.gif");
      imgSpace.setAttribute("align", "absmiddle");
      imgSpace.setAttribute("border", "0");
      imgSpace.setAttribute("width", "0");
      imgSpace.setAttribute("height", "0");


      var newSpace = document.createElement("div");
      newSpace.setAttribute("id", spaceId);
      newSpace.setAttribute("class", "spaceLevel1");
      newSpace.setAttribute("className", "spaceLevel1");
      newSpace.appendChild(imgSpace);
      newSpace.appendChild(newSpaceURL);

      displayFavoriteSpaceIcon(space, spaceId, newSpace);

      //add new entry to list
      document.getElementById("spaces").appendChild(newSpace);

      if (open == "true") {
        currentRootSpaceId = spaceId;
        this.displaySpace(space, "true");
      }
    }
    // Add alert message if user is in display favorite space mode without favorite space selected.
    if (displayUserFavoriteSpace && jQuery("#userMenuDisplayModeId").val() === "BOOKMARKS" && nbSpaces === 0) {
      jQuery('#spaces').html("<span class='noFavoriteSpace'>" + jQuery('#noFavoriteSpaceMsgId').val() + "</span> ");
    }

    try {
      treeLoaded();
    } catch (e) {
      //all looks don't need this callback function
    }
  },
  displaySpaceTransverse: function(space) {
    document.getElementById("spaceTransverse").innerHTML = "";
    showTransverseSpace();

    //create new entry
    var spaceId = space.getAttribute("id");
    var open = "true";
    var look = space.getAttribute("look");
    var css = space.getAttribute("css");

    var newSpaceURL = document.createElement("a");
    newSpaceURL.setAttribute("href",
            "javaScript:openSpace('" + spaceId + "', 0, '" + look + "', '" + css + "')");
    newSpaceURL.setAttribute("onfocus", "this.blur()");
    newSpaceURL.setAttribute("class", "spaceURL");
    newSpaceURL.setAttribute("className", "spaceURL");

    var newSpaceLabel = document.createTextNode(space.getAttribute("name"));
    newSpaceURL.appendChild(newSpaceLabel);

    var imgSpace = document.createElement("img");
    imgSpace.setAttribute("id", "img" + spaceId);
    imgSpace.setAttribute("src", "icons/1px.gif");
    imgSpace.setAttribute("align", "absmiddle");
    imgSpace.setAttribute("border", "0");
    imgSpace.setAttribute("width", "0");
    imgSpace.setAttribute("height", "0");

    var newSpace = document.createElement("div");
    newSpace.setAttribute("id", spaceId);
    newSpace.setAttribute("class", "spaceLevel1");
    newSpace.setAttribute("className", "spaceLevel1");
    newSpace.appendChild(imgSpace);
    newSpace.appendChild(newSpaceURL);

    //Add favorite space icon on space transverse
    displayFavoriteSpaceIcon(space, spaceId, newSpace);


    //add new entry to list
    document.getElementById("spaceTransverse").appendChild(newSpace);

    if (open === "true") {
      currentRootSpaceId = spaceId;
      this.displaySpace(space, "true");
    }
  },
  displaySpace: function(spaceContent, init) {

    currentSpaceId = spaceContent.getAttribute("id");
    currentSpaceLevel = parseInt(spaceContent.getAttribute("level"));

    //alert("currentSpaceLevel = "+currentSpaceLevel);

    var spaceHeader = document.getElementById(currentSpaceId);
    if (currentSpaceLevel === 0) {
      currentRootSpaceId = currentSpaceId;
      spaceHeader.setAttribute("class", "spaceLevel1On");
      spaceHeader.setAttribute("className", "spaceLevel1On");
    } else {
      jQuery('#' + currentSpaceId).addClass("spaceOn");
    }

    if (init == "true") {
      if (currentSpacePath === "") {
        currentSpacePath = currentSpaceId;
      }
      else {
        currentSpacePath += "/" + currentSpaceId;
      }
    }

    //var nbItems = spaceContent.childNodes.length;
    //alert("displaySpace, nbItems = "+nbItems);

    try {
      var spaceContentDiv = document.getElementById("contentSpace" + currentSpaceId);
      document.getElementById(currentSpaceId).removeChild(spaceContentDiv);
    } catch (e) {
      //the div does not exist
    }

    spaceContentDiv = document.createElement("div");
    spaceContentDiv.setAttribute("id", "contentSpace" + currentSpaceId);
    spaceContentDiv.setAttribute("class", "contentSpace");
    spaceContentDiv.setAttribute("className", "contentSpace");

    document.getElementById(currentSpaceId).appendChild(spaceContentDiv);

    var item = spaceContent.firstChild;

    //alert("nb spaces = "+nbSpaces);
    while (item != null) {
      var itemId = item.getAttribute("id");
      var itemLevel = item.getAttribute("level");
      var itemKind = item.getAttribute("kind");
      var itemType = item.getAttribute("type");
      var itemOpen = item.getAttribute("open");
      var itemURL = item.getAttribute("url");

      //create new entry
      var newEntry = document.createElement("div");
      newEntry.setAttribute("id", itemId);
      if (itemType === "component") {
        newEntry.setAttribute("class", "browseComponent");
        newEntry.setAttribute("className", "browseComponent");
      } else {
        newEntry.setAttribute("class", "browseSpace");
        newEntry.setAttribute("className", "browseSpace");
      }

      var newEntryURL = document.createElement("a");
      newEntryURL.setAttribute("onfocus", "this.blur()");

      var newEntryIcon = document.createElement("img");
      newEntryIcon.setAttribute("align", "absmiddle");

      var newEntryIconSel = document.createElement("img");

      if (itemType === "component") {
        newEntryIconSel.setAttribute("id", "img" + itemId);
        if (itemOpen === "true" || itemOpen === true) {
          newEntry.setAttribute("class", "browseComponentActiv");
          newEntry.setAttribute("className", "browseComponentActiv");
          newEntryIconSel.setAttribute("src", "icons/silverpeasV5/activComponent.gif");
        }
        else {
          newEntryIconSel.setAttribute("src", "icons/1px.gif");
        }
        newEntryURL.setAttribute("href",
                "javaScript:openComponent('" + itemId + "'," + itemLevel + ",'" + itemURL + "')");

        if (displayComponentsIcons()) {
          newEntryIcon.setAttribute("src",
                  getContext() + "/util/icons/component/" + itemKind + "Small.gif");
          newEntryIcon.setAttribute("class", "browseIconComponent");
          newEntryIcon.setAttribute("className", "browseIconComponent");
        }
        else {
          newEntryIcon.setAttribute("src", "icons/1px.gif");
        }
      } else {
        var look = item.getAttribute("look");
        var css = item.getAttribute("css");

        newEntryIcon.setAttribute("id", "img" + itemId);
        newEntryIcon.setAttribute("src", "icons/1px.gif");
        newEntryURL.setAttribute("href",
                "javaScript:openSpace('" + itemId + "'," + itemLevel + ",'" + look + "', '" + css + "')");
      }
      var newEntryLabel = document.createTextNode(item.getAttribute("name"));
      newEntryURL.appendChild(newEntryLabel);

      newEntry.appendChild(newEntryIcon);
      newEntry.appendChild(newEntryURL);

      if (itemType === "component") {
        newEntry.appendChild(newEntryIconSel);
      } else {
        // Space type
        displayFavoriteSpaceIcon(item, itemId, newEntry);
      }

      //add new entry to list
      spaceContentDiv.appendChild(newEntry);

      if (itemOpen === "true") {
        if (itemType === "space") {
          this.displaySpace(item, "true");
        }
        if (itemType === "component") {
          currentSpaceLevel = parseInt(itemLevel) - 1;
          currentComponentId = itemId;
        }
      }
      item = item.nextSibling;
    }
  },
  displayMySpace: function(spaceContent) {

    var spacePersoId = spaceContent.getAttribute("id");

    //var nbItems = spaceContent.childNodes.length;
    //alert("displaySpace, nbItems = "+nbItems);

    try {
      var spaceContentDiv = document.getElementById("contentSpace" + spacePersoId);
      document.getElementById(spacePersoId).removeChild(spaceContentDiv);
    } catch (e) {
      //the div does not exist
    }

    spaceContentDiv = document.createElement("div");
    spaceContentDiv.setAttribute("id", "contentSpace" + spacePersoId);
    spaceContentDiv.setAttribute("class", "contentSpace");
    spaceContentDiv.setAttribute("className", "contentSpace");

    document.getElementById(spacePersoId).appendChild(spaceContentDiv);

    var item = spaceContent.firstChild;
    while (item != null) {
      var itemId = item.getAttribute("id");
      var itemLevel = item.getAttribute("level");
      var itemKind = item.getAttribute("kind");
      var itemType = item.getAttribute("type");
      var itemOpen = item.getAttribute("open");
      var itemURL = item.getAttribute("url");

      var newEntry = getPersonalSpaceElement(itemId, itemLevel, itemKind, itemType, itemOpen,
              itemURL, item.getAttribute("name"));

      //add new entry to list
      spaceContentDiv.appendChild(newEntry);

      item = item.nextSibling;
    }
  },
  displayAxis: function(axisList) {
    removePDC();
    if (!axisList) {
      return;
    }

    var nbAxis = axisList.childNodes ? axisList.childNodes.length : 0;

    if (nbAxis > 0) {
      //if (currentSpaceLevel == 0)
      //{
      if (isPDCContextual() && document.getElementById("pdc") == null) {
        var pdcText = document.createTextNode(getPDCLabel());

        var pdcLabel = document.createElement("div");
        pdcLabel.setAttribute("id", "pdcLabel");
        pdcLabel.appendChild(pdcText);

        var pdcDiv = document.createElement("div");
        pdcDiv.setAttribute("id", "pdc");
        pdcDiv.appendChild(pdcLabel);

        document.getElementById(currentRootSpaceId).appendChild(pdcDiv);
      }
      //}
    }

    var axis = axisList.firstChild;
    //alert("nb spaces = "+nbSpaces);
    while (axis != null) {
      //create new entry
      var axisId = axis.getAttribute("id");
      var nbObj = axis.getAttribute("nbObjects");

      var axisURL = document.createElement("a");
      axisURL.setAttribute("href", "javaScript:pdcAxisSearch('" + axisId + "')");
      axisURL.setAttribute("onfocus", "this.blur()");
      axisURL.setAttribute("class", "browseAxis");
      axisURL.setAttribute("className", "browseAxis");

      var iconURL = document.createElement("a");
      iconURL.setAttribute("id", "jsAxis" + axisId);
      iconURL.setAttribute("href", "javaScript:pdcAxisExpand('" + axisId + "')");
      iconURL.setAttribute("onfocus", "this.blur()");

      var icon = document.createElement("img");
      icon.setAttribute("id", "imgAxis" + axisId);
      icon.setAttribute("src", "icons/silverpeasV5/pdcPeas_maximize.gif");
      icon.setAttribute("align", "absmiddle");
      icon.setAttribute("border", "0");
      icon.setAttribute("width", "15");
      icon.setAttribute("height", "15");
      icon.setAttribute("onfocus", "this.blur()");

      iconURL.appendChild(icon);

      var axisLabel = document.createTextNode(axis.getAttribute("name") + " (" + nbObj + ")");
      axisURL.appendChild(axisLabel);

      var newAxis = document.createElement("div");
      newAxis.setAttribute("id", "axis" + axisId);
      newAxis.appendChild(iconURL);
      //newAxis.appendChild(axisClass);
      newAxis.appendChild(axisURL);

      var newAxisContent = document.createElement("div");
      newAxisContent.setAttribute("id", "axisContent" + axisId);

      //add new entry to list
      document.getElementById("pdc").appendChild(newAxis);
      document.getElementById("pdc").appendChild(newAxisContent);

      axis = axis.nextSibling;
    }
  },
  displayValue: function(values) {
    //var nbValue = values.childNodes.length;
    var value = values.firstChild;
    //alert("nb spaces = "+nbSpaces);

    var valueIds = currentValuePath.split("/");
    //alert(valueIds);

    while (value != null) {
      //create new entry
      var valuePath = value.getAttribute("id");
      var nbObj = value.getAttribute("nbObjects");
      var valueLevel = parseInt(value.getAttribute("level"));

      var valueURL = document.createElement("a");
      valueURL.setAttribute("href", "javaScript:pdcValueSearch('" + valuePath + "')");
      valueURL.setAttribute("onfocus", "this.blur()");
      valueURL.setAttribute("class", "browseValue");
      valueURL.setAttribute("className", "browseValue");

      var iconURL = document.createElement("a");
      iconURL.setAttribute("id", "jsValue" + valuePath);
      iconURL.setAttribute("href", "javaScript:pdcValueExpand('" + valuePath + "')");
      iconURL.setAttribute("onfocus", "this.blur()");

      var icon = document.createElement("img");
      icon.setAttribute("id", "imgValue" + valuePath);
      icon.setAttribute("src", "icons/silverpeasV5/pdcPeas_maximize.gif");
      icon.setAttribute("align", "absmiddle");
      icon.setAttribute("border", "0");
      icon.setAttribute("width", "15");
      icon.setAttribute("height", "15");

      iconURL.appendChild(icon);

      var valueLabel = document.createTextNode(value.getAttribute("name") + " (" + nbObj + ")");
      valueURL.appendChild(valueLabel);

      var newValue = document.createElement("div");
      newValue.setAttribute("id", "value" + valuePath);

      value = value.nextSibling;

      var iconT = document.createElement("img");
      if (value == null) {
        iconT.setAttribute("src", getContext() + "/util/icons/minusTreeL.gif");
      }
      else {
        iconT.setAttribute("src", getContext() + "/util/icons/minusTreeT.gif");
      }
      iconT.setAttribute("align", "absmiddle");
      iconT.setAttribute("border", "0");
      iconT.setAttribute("width", "15");
      iconT.setAttribute("height", "15");

      //var motherValuePath = valuePath.substring(0, valuePath.lastIndexOf("/")+1);
      //alert(motherValuePath);
      if (valueLevel > 1) {
        var ancetre = "/0/";
        for (var v = 0; v < valueIds.length; v++) {
          if (valueIds[v] !== "" && valueIds[v] !== "0") {
            //alert("valueIds[v] = "+valueIds[v]);

            ancetre += valueIds[v] + "/";

            //alert("ancetre = "+ancetre);

            var iconIndent = document.createElement("img");
            iconIndent.setAttribute("align", "absmiddle");
            iconIndent.setAttribute("border", "0");
            iconIndent.setAttribute("width", "15");
            iconIndent.setAttribute("height", "15");

            if (document.getElementById("value" + ancetre) != null && document.getElementById("value" + ancetre).nextSibling != null) {
              iconIndent.setAttribute("src", getContext() + "/util/icons/minusTreeI.gif");
              iconIndent.setAttribute("width", "15");
              iconIndent.setAttribute("height", "15");
            }
            else {
              iconIndent.setAttribute("src", "icons/1px.gif");
              iconIndent.setAttribute("width", "15");
              iconIndent.setAttribute("height", "15");
            }

            newValue.appendChild(iconIndent);
          }
        }
      }

      newValue.appendChild(iconT);
      newValue.appendChild(iconURL);
      newValue.appendChild(valueURL);

      var newValueContent = document.createElement("div");
      newValueContent.setAttribute("id", "valueContent" + valuePath);

      newValue.appendChild(newValueContent);

      //add new entry to list
      if (currentValuePath !== "-1" && currentValuePath !== "/0/") {
        document.getElementById("valueContent" + currentValuePath).appendChild(newValue);
      }
      else {
        document.getElementById("axisContent" + currentAxisId).appendChild(newValue);
      }
    }

    var img = null;
    if (currentValuePath === "/0/") {
      img = document.getElementById("imgAxis" + currentAxisId);
      img.setAttribute("src", "icons/silverpeasV5/pdcPeas_minimize.gif");
    }
    else {
      img = document.getElementById("imgValue" + currentValuePath);
      img.setAttribute("src", "icons/silverpeasV5/pdcPeas_minimize.gif");
    }
    img.setAttribute("width", "15");
    img.setAttribute("height", "15");
  }
});

function getPersonalSpaceElement(itemId, itemLevel, itemKind, itemType, itemOpen, itemURL,
        itemName) {
  //create new entry
  var newEntry = document.createElement("div");
  newEntry.setAttribute("id", itemId);
  newEntry.setAttribute("class", "browseComponent");
  newEntry.setAttribute("className", "browseComponent");

  var newEntryURL = document.createElement("a");
  newEntryURL.setAttribute("onfocus", "this.blur()");
  var newEntryIcon = document.createElement("img");
  var newEntryIconSel = document.createElement("img");

  newEntryIconSel.setAttribute("id", "img" + itemId);
  newEntryIconSel.setAttribute("src", "icons/1px.gif");
  newEntryURL.setAttribute("href",
          "javaScript:openComponent('" + itemId + "'," + itemLevel + ",'" + itemURL + "')");

  newEntryIcon.setAttribute("src", "icons/1px.gif");

  var newEntryLabel = document.createTextNode(itemName);
  newEntryURL.appendChild(newEntryLabel);

  newEntry.appendChild(newEntryIcon);
  newEntry.appendChild(newEntryURL);

  newEntry.appendChild(newEntryIconSel);

  if (itemKind === "personalComponent") {
    newEntry.onmouseover = function() {
      document.getElementById('imgDel' + this.id).style.visibility = 'visible';
    };

    newEntry.onmouseout = function() {
      document.getElementById('imgDel' + this.id).style.visibility = 'hidden';
    };

    var deleteURL = document.createElement("a");
    deleteURL.setAttribute("onfocus", "this.blur()");
    deleteURL.setAttribute("href", "javaScript:removeComponent('" + itemId + "')");

    var deleteIcon = document.createElement("img");
    deleteIcon.setAttribute("id", "imgDel" + itemId);
    deleteIcon.setAttribute("src", getContext() + "/util/icons/delete.gif");
    deleteIcon.style.visibility = "hidden";
    deleteIcon.setAttribute("width", "12");
    deleteIcon.setAttribute("height", "12");
    deleteIcon.setAttribute("align", "absmiddle");

    deleteURL.appendChild(deleteIcon);
    newEntry.appendChild(deleteURL);
  }

  return newEntry;
}

var AjaxEngine = function() {
  var requestURLS = {};
  var ajaxObjects = {};
  this.registerRequest = function(name, url) {
    requestURLS[name] = url;
  };
  this.registerAjaxObject = function(name, url) {
    ajaxObjects[name] = url;
  };
  var _createQueryString = function( theArgs, offset ) {
    var queryList = {};
    for ( var i = offset ; i < theArgs.length ; i++ ) {
      var anArg = theArgs[i];

      if ( anArg.name != undefined && anArg.value != undefined ) {
        queryList[anArg.name] = encodeURIComponent(anArg.value);
      }
      else {
        var ePos  = anArg.indexOf('=');
        var argName  = anArg.substring( 0, ePos );
        var argValue = anArg.substring( ePos + 1 );
        queryList[argName] = encodeURIComponent(argValue);

      }
    }

    var d = new Date();
    var trick = d.getYear() + "ie" + d.getMonth() + "t" + d.getDate() + "r" + d.getHours() + "i" +
        d.getMinutes() + "c" + d.getSeconds() + "k" + d.getMilliseconds();

    queryList["&ietrick"] = trick;

    return queryList;
  };
  var _processAjaxResponse = function(sendParameters, xmlResponseElements ) {
    for ( var i = 0 ; i < xmlResponseElements.length ; i++ ) {
      var responseElement = xmlResponseElements[i];

      // only process nodes of type element.....
      if ( responseElement.nodeType != 1 )
        continue;

      var responseType = responseElement.getAttribute("type");
      var responseId   = responseElement.getAttribute("id");

      if ( responseType == "object" )
        _processAjaxObjectUpdate(sendParameters, ajaxObjects[ responseId ], responseElement );
      else
        notyError('unrecognized AjaxResponse type : ' + responseType );
    }
  };

  var _processAjaxObjectUpdate = function(sendParameters, ajaxObject, responseElement ) {
    ajaxObject.ajaxUpdate( responseElement );
    var eventData = {
      currentSpaceId : StringUtil.defaultStringIfNotDefined(sendParameters['SpaceId']),
      currentComponentId : StringUtil.defaultStringIfNotDefined(sendParameters['ComponentId'])
    };
    eventData.isPersonalSpace = eventData.currentSpaceId === '-10';
    var eventName = sendParameters["Init"] === '1' ? 'load' : 'changeselected';
    spLayout.getBody().getNavigation().dispatchEvent(eventName, eventData);
  };
  this.sendRequest = function(requestName, options) {
    var requestURL = requestURLS[requestName];
    if (!requestURL) {
      return;
    }

    // Allow for backwards Compatibility
    if (arguments.length >= 2 && typeof arguments[1] === 'string') {
      options = {parameters : _createQueryString(arguments, 1)};
    }

    var ajaxConfig = sp.ajaxRequest(requestURL).withParams(options.parameters);
    if (options.parameters["Init"] === '1' || options.parameters["SpaceId"]) {
      var eventData = {
        src : options.parameters,
        currentSpaceId : StringUtil.defaultStringIfNotDefined(options.parameters['SpaceId']),
        currentComponentId : StringUtil.defaultStringIfNotDefined(options.parameters['ComponentId'])
      };
      eventData.isPersonalSpace = eventData.currentSpaceId === '-10';
      spLayout.getBody().getNavigation().dispatchEvent("start-load", eventData);
    }
    return silverpeasAjax(ajaxConfig).then(function(request) {
      var response = request.responseXML.getElementsByTagName("ajax-response");
      if (response && response.length === 1) {
        _processAjaxResponse(options.parameters, response[0].childNodes);
      }
    });
  }
};

var spaceUpdater;
var ajaxEngine;

whenSilverpeasReady(function() {
  hideTransverseSpace();

  spaceUpdater = new SpaceUpdater();
  ajaxEngine = new AjaxEngine();
  ajaxEngine.registerRequest('getSpaceInfo', getContext() + '/RAjaxSilverpeasV5/dummy');
  ajaxEngine.registerAjaxObject('spaceUpdater', spaceUpdater);

  //Check displayUserMenuDisplayMode in order to enable/disable user favorite space feature
  ajaxEngine.sendRequest('getSpaceInfo', 'ResponseId=spaceUpdater', 'Init=1',
      'GetPDC=' + displayPDC(), 'SpaceId=' + getSpaceIdToInit(),
      'ComponentId=' + getComponentIdToInit(), 'UserMenuDisplayMode=' + getUserMenuDisplayMode());

  try {
    displayContextualPDC = displayContextualPDC();
  } catch (e) {
    displayContextualPDC = true;
  }

  displayPDCFrame(getSpaceIdToInit(), getComponentIdToInit());

  var __scrollListener = function() {
    var $activeItem = currentComponentId && document.getElementById(currentComponentId);
    if (!$activeItem) {
      $activeItem = currentSpaceId && document.getElementById(currentSpaceId);
      $activeItem = $activeItem ? sp.element.querySelector('.spaceURL', $activeItem) : $activeItem;
    }
    if ($activeItem) {
      sp.element.scrollToIfNotFullyInView($activeItem, spLayout.getBody().getNavigation().getContainer());
    }
  };
  spLayout.getBody().getContent().addEventListener('load', __scrollListener, '__id__navigation-part');
  spLayout.getBody().getNavigation().addEventListener('load', __scrollListener, '__id__navigation-part');
  spLayout.getBody().getNavigation().addEventListener('changeselected', __scrollListener, '__id__navigation-part');
  spLayout.getFooter().addEventListener('pdcshow', __scrollListener, '__id__navigation-part');
  spLayout.getFooter().addEventListener('pdchide', __scrollListener, '__id__navigation-part');
});