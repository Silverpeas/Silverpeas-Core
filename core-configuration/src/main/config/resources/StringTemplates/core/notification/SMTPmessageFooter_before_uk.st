$if(notification_attachments)$
<p>Додатки:</p>
$notification_attachments:attachmentLinks_fr(attachment=it)$
$endif$

$if(notification_link)$
<div itemscope itemtype="http://schema.org/EmailMessage">
<div itemprop="action" itemscope itemtype="http://schema.org/ViewAction">
<link itemprop="url" href="$notification_link$"/>
<meta itemprop="name" content="$notification_linkLabel$"/>
</div>
</div>
<a target="_blank" href="$notification_link$" style="display:inline-block; margin:20px 0 10px
0;border-radius:10px; border:1px solid #ccc; font-size:12px; колір:#333; font-weight:bold;
text-decoration:none;padding:10px;background: linear-gradient(#fff, #eee) повторювати прокручування 0 0 #eee;&quot;&gt;
<span style="color:#7eb73b">►</span> $notification_linkLabel$
</a>
$endif$
