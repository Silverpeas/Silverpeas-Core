<script>

    var _paq = window._paq = window._paq || [];

    (function() {
      var u = '$matomoUrl$';
      _paq.push(['setTrackerUrl', u + 'matomo.php']);
      _paq.push(['setSiteId', '$siteId$']);

      _paq.push(['setCustomDimension', 1, '$space$']);
      _paq.push(['setCustomDimension', 2, '$component$']);
      _paq.push(['setCustomDimension', 3, '$content$']);
      _paq.push(['setUserId', 'user-$userId$']);

      _paq.push(['setDocumentTitle', '$title$']);
      _paq.push(['setCustomUrl', '$virtualPage$']);
      _paq.push(['enableHeartBeatTimer']);
      _paq.push(['trackPageView']);
      _paq.push(['enableLinkTracking']);

      var d=document,g=d.createElement('script'),s=d.getElementsByTagName('script')[0];
      g.async=true;g.src=u+'matomo.js';s.parentNode.insertBefore(g,s);
    })();

</script>