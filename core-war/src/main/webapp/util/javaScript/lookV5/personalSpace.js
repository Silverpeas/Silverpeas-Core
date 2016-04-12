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
 * FLOSS exception.  You should have received a copy of the text describing
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
function listComponents() {
  jQuery.getJSON(getContext() + "/PersonalSpace?Action=GetAvailableComponents&IEFix=" +
      new Date().getTime(), function(data) {
        try {
          var components = "";
          for (var i = 0; i < data.length; ++i) {
            var component = data[i];
            components += getComponentEntry(component.name, component.description, component.label);
          }
          var labels = getPersonalSpaceLabels();
          jQuery("#addComponent").html(labels[0]);
          jQuery("#addComponent").append("<ul id=\"availables\"></ul>");
          jQuery("#availables").html(components);
        } catch (e) {
          //do nothing
          //alert(e);
        }
      });
}

function addComponent(name) {
  jQuery.getJSON(getContext() + "/PersonalSpace?Action=AddComponent&ComponentName=" + name + "&IEFix=" +
      new Date().getTime(), function(data) {
        if (data.successfull == true) {
          jQuery("#" + data.name).remove();
          var newEntry = getPersonalSpaceElement(data.id, 1, "personalComponent", "component",
              false, data.url, data.label);
          jQuery(newEntry).insertBefore('#addComponent');
          if (jQuery("#availables li").size() == 0) {
            jQuery("#addComponent").remove();
          }
        } else {
          alert(data.exception);
        }
      });
}

function removeComponent(id) {
  var labels = getPersonalSpaceLabels();
  if (window.confirm(labels[1])) {
    jQuery.getJSON(getContext() + "/PersonalSpace?Action=RemoveComponent&ComponentId=" + id + "&IEFix=" +
        new Date().getTime(), function(data) {
          if (data.successfull == true) {
            var componentId = data.id;
            jQuery("#" + componentId).remove();

            if (jQuery("#addComponent").length == 0) {
              //Le lien "Ajouter un composant..." n'existe pas, on le crée
              var labels = getPersonalSpaceLabels();
              var newEntry = getPersonalSpaceElement("addComponent", 1, "", "component", false,
                  "javascript:listComponents()", labels[2]);
              jQuery("#contentSpacespacePerso").append(newEntry);
            }
            if (jQuery("#availables").length != 0) {
              //Le lien "Ajouter un composant..." existe, on remet le composant supprimé dedans
              var liElem = getComponentEntry(data.name, data.description, data.label);

              jQuery("#availables").append(liElem);
            }
          } else {
            alert(data.exception);
          }
        });
  }
}

function getComponentEntry(name, description, label) {
  return "<li id=\"" + name + "\"><a href=\"javascript:addComponent('" + name + "')\" title=\"" +
      description + "\">" + label + "</a></li>";
}