var offset = 0;
var currentScope = '';
function init(scope) {
  currentScope = scope;
  offset = 0;
  var url = getFeedURL() + '&type=' + scope + '&offset=0&Init=true';
  displayFeedContent(url);
}

function displayFeedContent(url) {
  $('.inprogress').show();
  $('.linkMore').hide();
  url += "&IEFix=" + Math.round(new Date().getTime());
  $.getJSON(url, function(data) {
    var listEmpty = true;
    var html = '';
    $.each(data, function(key, map) {
      $.each(map, function(i, listSocialInfo) {
        // for each pertinent day...
        listEmpty = false;
        if (i == 0) {
          html += '<p class="date textePetitBold">' + listSocialInfo.day + '</p>';
          html += getSeparator();
        } else {
          $.each(listSocialInfo, function(index, socialInfo) {
            // for each information
            if (socialInfo.type == 'RELATIONSHIP') {
              html += getRelationFragment(socialInfo);
            } else if (socialInfo.type == 'STATUS') {
              html += getStatusFragment(socialInfo);
            } else {
              html += getFragment(socialInfo);
            }
          });
        }
      });
    });
    $('#newsFeed-content').append(html);
    $('.inprogress').hide();
    $('#scope-' + currentScope).attr('class', 'active');

    if (!listEmpty) {
      $('.linkMore').show();
    }
    offset++;
  });
}

function getNext() {
  var url = getFeedURL() + '&type=' + currentScope + '&offset=' + offset;
  displayFeedContent(url);
}

function getFragment(socialInfo) {
  var fragment = '';

  fragment += '<div class="' + socialInfo.type.toLowerCase() + ' a_new">';
  fragment += getAvatarFragment(socialInfo.author);
  fragment += '<div class="txt">';
  fragment += '<p><a href="' + getApplicationContext() + '/Rprofil/jsp/Main?userId=' +
  socialInfo.author.id + '" class="name">' + socialInfo.author.displayedName + '</a> ' +
  socialInfo.label + ' <a href="' + socialInfo.url + '" class="publicationName txtColor">' +
  socialInfo.title + '</a> ' + socialInfo.hour + '</p>';
  fragment += '<p class="detail decoration-' + socialInfo.type.toLowerCase() + '">' +
  socialInfo.description + '&nbsp;</p>';
  fragment += '</div>';
  fragment += '</div>';
  fragment += getSeparator();

  return fragment;
}

function getStatusFragment(socialInfo) {
  var fragment = '';

  fragment += '<div class="' + socialInfo.type.toLowerCase() + ' a_new">';
  fragment += getAvatarFragment(socialInfo.author);

  fragment += '<div class="txt">';
  fragment += '<p><a href="' + getApplicationContext() + '/Rprofil/jsp/Main?userId=' +
  socialInfo.author.id + '" class="name">' + socialInfo.author.displayedName + '</a> ' +
  socialInfo.title + ' ' + socialInfo.hour + '</p>';
  fragment += '<p class="detail decoration-' + socialInfo.type.toLowerCase() + '">' +
  socialInfo.description + '</p>';
  fragment += '</div>';

  fragment += '</div>';
  fragment += getSeparator();

  return fragment;
}

function getRelationFragment(socialInfo) {
  var fragment = '';

  fragment += '<div class="' + socialInfo.type.toLowerCase() + ' a_new">';
  fragment += getAvatarFragment(socialInfo.author);

  fragment += '<div class="txt">';
  fragment += '<p><a href="' + getApplicationContext() + '/Rprofil/jsp/Main?userId=' +
  socialInfo.author.id + '" class="name">' + socialInfo.author.displayedName + '</a> ' +
  socialInfo.label + ' ' + socialInfo.hour + '</p>';
  fragment += '<p class="' + socialInfo.type.toLowerCase() + ' detail">';
  fragment += '<span class="profilPhoto">';
  fragment += '<img src="' + socialInfo.title.profilPhoto + '" alt="viewUser" class="avatar"/>';
  fragment += '</span>';
  fragment += '</p>';
  fragment += '</div>';

  fragment += '</div>';
  fragment += getSeparator();

  return fragment;
}

function getAvatarFragment(author) {
  var fragment = '';
  fragment += '<div class="profilPhoto">';
  fragment +=
      '<a href="' + getApplicationContext() + '/Rprofil/jsp/Main?userId=' + author.id + '">';
  fragment += '<img class="defaultAvatar" alt="" src="' + author.profilPhoto + '"/>';
  fragment += '</a>';
  fragment += '</div>';
  return fragment;
}

function getSeparator() {
  return '<hr class="sep"/>';
}

function changeScope(newScope) {
  $('.sousNavBulle a').attr('class', '');
  $('#scope-' + newScope).attr('class', 'active');
  $('#newsFeed-content').html('');
  init(newScope);
}