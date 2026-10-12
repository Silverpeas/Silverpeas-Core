/* Shared publication page behavior. */
var href = window.location.href;
    var baseURL = window.location.protocol + "//" + window.location.host;

    $(function() {
      var instanceId = "";

      loadI18N();

      // get sharing informations according to token
      var token = href.substring(href.indexOf("?") + 1, href.length);
      var uri = baseURL + "/silverpeas/services/sharing/" + token;
      sharingAjax(uri, function(sharing) {
        var d = new Date();
        d.setTime(sharing.creationDate);
        $("#sharingDate").html(d.toLocaleString());
        $("#sharingBy").html(sharing.user);

        if (sharing.expiration) {
          d.setTime(sharing.expiration);
          $("#breadcrumb .validityInfo .expiration").html(d.toLocaleString());
        }
        if (sharing.securityCode) {
          openSecurityCodeDialog(token, baseURL, function() {
            displayPublication();
          });
          } else {
          displayPublication();
        }
      });

      function displayPublication() {
        var url = baseURL + "/silverpeas/services/sharing/publications/" + token;
        $("#breadcrumb").show();
        $("#publication").show();
        sharingAjax(url, function(publication) {
          try {
            $(".publiName").html(publication.name);
            if (publication.description) {
              $(".publiDesc").html(publication.description.replace("\n", "<br/>"));
            } else {
              $(".publiDesc").remove();
            }

            displayAttachments(publication.attachments);

            $("#richContent").html(publication.content);
          } catch (e) {
            //do nothing
            alert(e);
          }
        });
      }

      $(document).on("click", ".shared-file-download", function(event) {
        sharingDownloadLink(event, this.href, $(this).data("filename"));
      });

      function displayAttachments(attachments) {
        if (attachments != null && attachments.length > 0) {
          for (i = 0; i < attachments.length; i++) {
            var attachment = attachments[i];

            // usefull for ZIP purpose
            $("#instanceId").val(attachment.instanceId);

            var label = attachment.logicalName;
            if (attachment.title) {
              label = attachment.title;
            }

            var li = $("<li>").addClass("attachment");

            var checkbox = $("<input>").attr("type", "checkbox").on("change", clickOnFile);
            checkbox.attr("name", "attachmentId").attr("value", attachment.id);
            checkbox.appendTo(li);

            var img = $("<img>").attr("src", attachment.icon);
            var a = $("<a>").attr("href", attachment.sharedUri).addClass("shared-file-download").attr("data-filename", attachment.logicalName);
            a.append(img);
            var label = $("<span>").html(label);
            label.appendTo(a);
            a.appendTo(li);
            if (attachment.description) {
              var p = $("<p>").addClass("description").html(
                      attachment.description.replace("\n", "<br/>"));
              p.appendTo(li);
            }

            var p2 = $("<p>");
            var size = $("<span>").addClass("size");
            size.html(bytesToSize(attachment.size, 0));
            size.appendTo(p2);
            if (attachment.title) {
              var filename = $("<span>").addClass("FileName");
              filename.html(" - " + attachment.logicalName);
              filename.appendTo(p2);
            }
            p2.appendTo(li);

            $("#attachmentList").append(li);
          }
        } else {
          $(".attachments").remove();
        }
      }

      function loadI18N() {
        // load bundles
        window.i18n.properties({
          name : 'messages', path : 'bundle/', mode : 'map'
        });

        $("#sharingTitle").html(window.i18n.prop("sharing.title"));
        $("#expirationLabel").html(window.i18n.prop("expiration"));
        $(".expiration").html(window.i18n.prop("expiration.unlimited"));
        $("#download-link").attr("title", window.i18n.prop("download.a.title"));
        $("#download-link").html(window.i18n.prop("download.a"));
        $("#downloadZip-dialog").attr("title", window.i18n.prop("download.dialog.title"));
        $("#downloadZip-dialog a").html(window.i18n.prop("download.dialog.content"));
        $(".silverpeasLink").attr("title", window.i18n.prop("silverpeas.link"));
        $("#sharingDateLabel").html(window.i18n.prop("sharing.shared"));
        $("#sharingByLabel").html(window.i18n.prop("sharing.by"));
        $(".attachments h4").html(window.i18n.prop("files"));
      }

      $('#download-link').click(function processZip() {
        // get ids of files checked
        var ids = $(':checkbox:checked').map(function() {
          return $(this).val();
        }).get().join(',');
        // generate zip file
        var instanceId = $("#instanceId").val();
        var url = baseURL + "/silverpeas/services/sharing/attachments/" + instanceId + "/" + token +
                "/" + ids + "/zip";
        sharingDownload(url, 'shared-files.zip');
      });

    });

    var downloadlinkshow = false;
    var leastOneCheck = false;

    function clickOnFile() {
      leastOneCheck = false;
      if (!downloadlinkshow) {
        $('#download-link').animate({
          right : '+=87'
        }, 300, function() {
          downloadlinkshow = true;
        });
      } else {
        $("input[type=checkbox]:checked").each(function() {
          leastOneCheck = true;
        });
        if (!leastOneCheck) {
          $('#download-link').animate({
            right : '-=87'
          }, 300, function() {
            downloadlinkshow = false;
          });
        }
      }
    }
