$if(notification_link)$
  <div itemscope itemtype="http://schema.org/EmailMessage">
    <div itemprop="action" itemscope itemtype="http://schema.org/ViewAction">
      <link itemprop="url" href="$notification_link$"/>
      <meta itemprop="name" content="$notification_linkLabel$"/>
    </div>
  </div>
  <a target="_blank" href="$notification_link$" style="display:inline-block; margin:20px 0 10px
  0;border-radius:10px; border:1px solid #ccc; font-size:12px; color:#333; font-weight:bold;
  text-decoration:none;padding:10px;background: linear-gradient(#fff, #eee) repeat scroll 0 0 #eee;">
  <span style="color:#7eb73b">  &#9658;</span>  $notification_linkLabel$
  </a>
$endif$