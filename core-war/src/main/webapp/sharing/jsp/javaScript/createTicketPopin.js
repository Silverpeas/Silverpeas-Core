/*
 * Copyright (C) 2000 - 2026 Silverpeas
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

function createSharingTicketPopup(sharingParam) {
  cleanForm();
  $("#hiddenType").val(sharingParam.type);
  $("#hiddenComponentId").val(sharingParam.componentId);
  $("#displayNameId").text(sharingParam.name);
  $("#hiddenObjectId").val(sharingParam.id);

  toggleContinuous();

  if (sharingParam.type === "Node") {
    $("#objectName").text(sp.i18n.get("GML.theme"));
  } else if (sharingParam.type === "Publication") {
    $("#objectName").text(sp.i18n.get("GML.publication"));
  } else {
    //Attachment
    $("#objectName").text(sp.i18n.get("GML.file"));
  }

  $("#sharingticket-popup-content").popup("validation", {
    title: sp.i18n.get("sharing.createTicket"),
    width: "700px",
    callback: function () {
      return ifCorrectFormExecute(function () {
        createTicket();
      });
    }
  });
}

function toggleContinuous(effect) {
  const continuousTicket = $("#validity").val() === "0";
  const nbAccessShown = $("#hiddenType").val() === "Attachment";
  if (continuousTicket) {
    $(".threshold").hide(effect);
  } else {
    $(".threshold").show(effect);
    if (nbAccessShown) {
      $("#nbAccessMaxArea").show();
    } else {
      $("#nbAccessMaxArea").hide();
    }
  }
}

function ifCorrectFormExecute(callback) {
  let errorMsg = "";
  let errorNb = 0;
  const nb = $("#nbAccessMax").val();
  const nbMin = 0;
  const endDate = $("#endDate").val();
  const nbAccessShown = $("#hiddenType").val() === "Attachment";

  if ($("#validity").val() === "1") {
    if (nbAccessShown && isWhitespace(nb)) {
      errorMsg +=
        "  - " +
        sp.i18n.get("GML.theField") +
        " '" +
        sp.i18n.get("sharing.nbAccessMax") +
        "' " +
        sp.i18n.get("GML.MustBeFilled") +
        "\n";
      errorNb++;
    }
    if (nbAccessShown && !isInteger(nb)) {
      errorMsg +=
        "  - " +
        sp.i18n.get("GML.theField") +
        " '" +
        sp.i18n.get("sharing.nbAccessMax") +
        "' " +
        sp.i18n.get("GML.MustContainsNumber") +
        "\n";
      errorNb++;
    }
    if (nb > 10000 || nb < 0) {
      errorMsg +=
        "  - " +
        sp.i18n.get("GML.theField") +
        " '" +
        sp.i18n.get("sharing.nbAccessMax") +
        "' " +
        sp.i18n.get("sharing.maxValue") +
        " " +
        nbMin +
        " " +
        sp.i18n.get("GML.and") +
        " 10000\n";
      errorNb++;
    }
    if (isWhitespace(endDate)) {
      errorMsg +=
        "  - " +
        sp.i18n.get("GML.theField") +
        " '" +
        sp.i18n.get("sharing.endDate") +
        "' " +
        sp.i18n.get("GML.MustBeFilled") +
        "\n";
      errorNb++;
    } else if (
      !isDateOK(
        endDate,
        document.getElementById("sharingticket-popup-content").dataset.language
      )
    ) {
      errorMsg +=
        "  - " +
        sp.i18n.get("GML.theField") +
        " '" +
        sp.i18n.get("sharing.endDate") +
        "' " +
        sp.i18n.get("GML.MustContainsCorrectDate") +
        "\n";
      errorNb++;
    }
  }

  switch (errorNb) {
    case 0:
      callback.call(this);
      return true;
    case 1:
      errorMsg =
        sp.i18n.get("GML.ThisFormContains") +
        " 1 " +
        sp.i18n.get("GML.error") +
        " : \n" +
        errorMsg;
      jQuery.popup.error(errorMsg);
      return false;
    default:
      errorMsg =
        sp.i18n.get("GML.ThisFormContains") +
        " " +
        errorNb +
        " " +
        sp.i18n.get("GML.errors") +
        " :\n" +
        errorMsg;
      jQuery.popup.error(errorMsg);
      return false;
  }
}

function createTicket() {
  const ajaxUrl =
    webContext + "/services/mytickets/" + $("#hiddenComponentId").val();

  const newTicket = {
    sharedObjectId: $("#hiddenObjectId").val(),
    sharedObjectType: $("#hiddenType").val(),
    componentId: $("#hiddenComponentId").val(),
    validity: $("#validity").val(),
    uri: "",
    endDateStr: $("#endDate").val(),
    endDateFormat: $("#endDateFormat").val(),
    nbAccessMax: $("#nbAccessMax").val(),
    users: $("#users").val(),
    externalEmails: $("#externalEmails").val(),
    additionalMessage: $("#additionalMessage").val(),
    securityCode: $("#securityCode").val()
  };

  jQuery.ajax({
    url: ajaxUrl,
    type: "POST",
    data: $.toJSON(newTicket),
    contentType: "application/json",
    cache: false,
    dataType: "json",
    async: true,
    success: function (result) {
      $("#message-content-url").html(
        $("<a>", { text: result.url, href: result.url, target: "_blank" })
      );
      showInformation();
      cleanForm();
    },
    error: function (xhr, ajaxOptions, thrownError) {
      const errorDocument = new DOMParser().parseFromString(
        xhr.responseText,
        "text/html"
      );
      const data = errorDocument.body.innerHTML;
      $("#sharingticket-error-content").html(data);
      $("#sharingticket-error-content").popup("basic", {
        title: "Error"
      });
      cleanForm();
    }
  });
}

function cleanForm() {
  $("#hiddenObjectId").val("");
  $("#hiddenType").val("");
  $("#hiddenComponentId").val("");
  $("#validity").val("0");
  $("#endDate").val("");
  $("#endDateFormat").val("");
  $("#nbAccessMax").val("");
  $("#users").val("");
  $("#users_name").val("");
  $("#externalEmails").val("");
  $("#additionalMessage").val("");
  $("#securityCode").val("");
}

function showInformation() {
  $("#sharingticket-message-content").popup("information", {
    title:
      sp.i18n.get("sharing.tickets") +
      " > " +
      sp.i18n.get("sharing.confirmTicket"),
    callback: function () {
      return true;
    }
  });
}

$(function () {
  const content = document.getElementById("sharingticket-popup-content");
  sp.i18n.load({
    bundle: "org.silverpeas.sharing.multilang.fileSharingBundle",
    withGeneral: true,
    language: content.dataset.language
  });
  $("#validity").on("change", function () {
    toggleContinuous();
  });
  $("#selectSharingUsers").on("click", function (event) {
    event.preventDefault();
    SP_openWindow(
      webContext +
        "/RselectionPeasWrapper/jsp/open?formName=ticketForm&elementId=users&elementName=users_name&selectedUsers=" +
        $("#users").val() +
        "&selectionMultiple=true",
      "selectUser",
      800,
      600,
      ""
    );
  });
  applyTokenSecurity("#sharingticket-popup-content");
});
