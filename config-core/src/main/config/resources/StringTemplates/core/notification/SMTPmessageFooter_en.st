$if(notification_link)$
  <div itemscope itemtype="http://schema.org/EmailMessage">
    <div itemprop="action" itemscope itemtype="http://schema.org/ViewAction">
      <link itemprop="url" href="$notification_link$"/>
      <meta itemprop="name" content="$notification_linkLabel$"/>
    </div>
  </div>
  <a target="_blank" href="$notification_link$" style="display:inline-block; margin:10px 0 20px
  0;border-radius:10px; border:1px solid #ccc; font-size:12px; color:#333; font-weight:bold;
  text-decoration:none;padding:10px;background: linear-gradient(#fff, #eee) repeat scroll 0 0 #eee;">
  <span style="color:#7eb73b">  &#9658;</span>  $notification_linkLabel$
  </a>
$endif$

</div>
<div style="background-color:#353535;color:#FFF;padding:10px;font-family:Century Gothic,Arial;font-size:12px;">
Notification sent from <a style="color:#FFF;" href="$notification_serverurl$">your Silverpeas platform</a> -
<img height="22px" style="vertical-align:middle" src="http://www.silverpeas.com/images/favicon.png" alt="" />
</div>