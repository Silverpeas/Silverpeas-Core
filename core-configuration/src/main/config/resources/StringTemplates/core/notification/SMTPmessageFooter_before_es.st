$if(notification_attachments)$
<p>Ficheros adjuntos :</p>
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
0;borde-radio:10px; borde: 1px sólido #ccc; tamaño de fuente: 12px; color:#333; fuente-peso:negrita;
text-decoration:none;padding:10px;background: linear-gradient(#fff, #eee) repetir desplazamiento 0 0 #eee;&quot;&gt;
<span style="color:#7eb73b">►</span> $notification_linkLabel$
</a>
$endif$
