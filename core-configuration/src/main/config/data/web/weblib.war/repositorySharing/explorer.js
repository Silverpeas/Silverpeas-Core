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
const href = window.location.href;
const baseURL = window.location.protocol + "//" + window.location.host;

$(function () {
  const nbPublicationsPerPage = 10;

  loadI18N();

  // get sharing informations according to token
  const token = href.substring(href.indexOf("?") + 1, href.length);
  const uri = baseURL + "/silverpeas/services/sharing/" + token;
  sharingAjax(uri, function (sharing) {
    if (sharing.expiration) {
      const d = new Date();
      d.setTime(sharing.expiration);
      $("#breadcrumb .validityInfo .expiration").html(d.toLocaleString());
    }
    if (sharing.securityCode) {
      openSecurityCodeDialog(token, baseURL, function () {
        loadTreeview(sharing.webApplicationRootUri);
      });
    } else {
      loadTreeview(sharing.webApplicationRootUri);
    }
  });

  function loadTreeview(rootUri) {
    $("#breadcrumb").show();
    $("#publications").show();
    $("#explorer").show();

    $("#explorer").jstree({
      core: {
        force_text: false,
        data: {
          url: function (node) {
            let nodeId = "";
            const url = rootUri;
            if (node && node.id !== "#") {
              url = node.original.attr["uri"];
            }
            return url;
          },
          headers:
            sharingVerificationCode === null
              ? {}
              : { "X-Verification-Code": sharingVerificationCode },
          success: function (new_data) {
            try {
              // init breadcrumb
              $("#breadcrumb .path").html(new_data.data);
              // store instanceId
              $("#instanceId").val(new_data.attr.componentId);

              // browse children to add sub-child behavior
              if (new_data.children) {
                for (let i = 0; i < new_data.children.length; i++) {
                  const child = new_data.children[i];
                  child.children = true;
                }
              }

              // display publications
              displayPublications(new_data.attr.id, new_data.attr.componentId);
            } catch (e) {}
            return new_data;
          }
        },
        check_callback: false,
        themes: {
          dots: false,
          icons: true
        },
        multiple: false
      }, // the `plugins` array allows you to configure the active plugins on this instance
      plugins: []
    });

    $("#explorer")
      .on("ready.jstree", function (event, data) {
        const root = data.instance.get_node("#0");
        data.instance.select_node(root);
      })
      .on("select_node.jstree", function (e, data) {
        // data.inst is the instance which triggered this event
        // display breadcrumb
        const path = data.instance.get_path(data.node.id, false, false);
        let newPath = "";
        for (let i = 0; i < path.length; i++) {
          if (i != 0) {
            newPath += " > ";
          }
          newPath += path[i];
        }
        $("#breadcrumb .path").html(newPath);

        //open folder in treeview
        data.instance.open_node(data.node);

        hideZipPanel();

        // display publications
        displayPublications(
          data.node.id,
          data.node.original.attr["componentId"]
        );
      });
  }

  function displayPublications(nodeId, instanceId) {
    const url =
      baseURL +
      "/silverpeas/services/sharing/publications/" +
      token +
      "/node/" +
      nodeId +
      "?withAttachments=true";
    sharingAjax(
      url,
      function (data) {
        try {
          $("#publications #publicationsList").empty();
          let nbPublis = 0;
          for (let i = 0; data != null && i < data.length; i++) {
            const li = displayPublication(data[i]);
            if (li.length > 0) {
              $("#publications #publicationsList").append(li);
              nbPublis++;
            }
          }
          // display pagination if needed
          if (nbPublis > nbPublicationsPerPage) {
            $("#pagination").show();
            $("#pagination").smartpaginator({
              totalrecords: parseInt(data.length),
              recordsperpage: nbPublicationsPerPage,
              datacontainer: "publicationsList",
              dataelement: "li.publication",
              next: window.i18n.prop("pagination.next"),
              prev: window.i18n.prop("pagination.prev"),
              first: window.i18n.prop("pagination.first"),
              last: window.i18n.prop("pagination.last"),
              theme: "black"
            });
          } else {
            $("#pagination").hide();
          }
          // display message about empty folder
          if (nbPublis == 0) {
            $("#empty-folder").show();
          } else {
            $("#empty-folder").hide();
          }
        } catch (e) {
          //do nothing
          alert(e);
        }
      },
      "json"
    );
  }

  function displayPublication(publication) {
    const attachments = displayAttachments(publication.attachments);
    let li = "";
    if (attachments.length > 0) {
      li =
        '<li class="publication"><span class="name">' +
        publication.name +
        '</span><p class="description">' +
        publication.description.replace(/\r?\n/g, "<br/>") +
        "</p>";
      li += attachments;
      li += "</li>";
    }
    return li;
  }

  $(document).on("click", ".shared-file-download", function (event) {
    sharingDownloadLink(event, this.href, $(this).data("filename"));
  });
  $(document).on("change", ".shared-file-select", clickOnFile);

  function displayAttachments(attachments) {
    let ul = "";
    if (attachments != null && attachments.length > 0) {
      ul = '<ul class="attachments">';
      for (let i = 0; i < attachments.length; i++) {
        const attachment = attachments[i];
        let label = attachment.logicalName;
        if (attachment.title) {
          label = attachment.title;
        }
        ul +=
          '<li class="attachment"><input type="checkbox" name="attachmentId" value="' +
          attachment.id +
          '" class="shared-file-select"/><a href="' +
          attachment.sharedUri +
          '" class="shared-file-download" data-filename="' +
          attachment.logicalName +
          '"><img alt="" src="' +
          attachment.icon +
          '"/>' +
          label +
          "</a>";
        ul += '<span class="size">' + bytesToSize(attachment.size, 0);
        if (attachment.title) {
          ul += " - " + attachment.logicalName;
        }
        ul += "</span>";
        if (attachment.description) {
          ul +=
            '<span class="description">' + attachment.description + "</span>";
        }
        ul += "</li>";
      }
      ul += "</ul>";
    }
    return ul;
  }

  function loadI18N() {
    // load bundles
    window.i18n.properties({
      name: "messages",
      path: "bundle/",
      mode: "map"
    });

    $("#sharingTitle").html(window.i18n.prop("sharing.title"));
    $("#expirationLabel").html(window.i18n.prop("expiration"));
    $(".expiration").html(window.i18n.prop("expiration.unlimited"));
    $("#download-link").attr("title", window.i18n.prop("download.a.title"));
    $("#download-link").html(window.i18n.prop("download.a"));
    $("#downloadZip-dialog").attr(
      "title",
      window.i18n.prop("download.dialog.title")
    );
    $("#downloadZip-dialog a").html(
      window.i18n.prop("download.dialog.content")
    );
    $(".silverpeasLink").attr("title", window.i18n.prop("silverpeas.link"));
    $("#empty-folder").html(window.i18n.prop("folder.empty"));
  }

  $("#download-link").click(function processZip() {
    // get ids of files checked
    const ids = $(":checkbox:checked")
      .map(function () {
        return $(this).val();
      })
      .get()
      .join(",");
    // generate zip file
    const instanceId = $("#instanceId").val();
    const url =
      baseURL +
      "/silverpeas/services/sharing/attachments/" +
      instanceId +
      "/" +
      token +
      "/" +
      ids +
      "/zip";
    sharingDownload(url, "shared-files.zip");
  });
});

let downloadlinkshow = false;
let leastOneCheck = false;

function clickOnFile() {
  leastOneCheck = false;
  if (!downloadlinkshow) {
    $("#download-link").animate(
      {
        right: "+=87"
      },
      300,
      function () {
        downloadlinkshow = true;
      }
    );
  } else {
    $("input[type=checkbox]:checked").each(function () {
      leastOneCheck = true;
    });
    if (!leastOneCheck) {
      hideZipPanel();
    }
  }
}

function hideZipPanel() {
  leastOneCheck = false;
  if (downloadlinkshow) {
    $("#download-link").animate(
      {
        right: "-=87"
      },
      300,
      function () {
        downloadlinkshow = false;
      }
    );
  }
}
