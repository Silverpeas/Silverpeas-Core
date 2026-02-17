<script>
var _paq = window._paq = window._paq || [];

_paq.push(['setCustomDimension', 1, 'space-$spaceId$']);
_paq.push(['setCustomDimension', 2, 'component-$componentId$']);
_paq.push(['setUserId', 'user-$userId$']);

_paq.push(['trackPageView']);
_paq.push(['enableLinkTracking']);

(function() {
  var u = '<matomoUrl>';
  _paq.push(['setTrackerUrl', u + 'matomo.php']);
  _paq.push(['setSiteId', '<siteId>']);
  var d=document,g=d.createElement('script'),s=d.getElementsByTagName('script')[0];
  g.async=true;g.src=u+'matomo.js';s.parentNode.insertBefore(g,s);
})();
</script>