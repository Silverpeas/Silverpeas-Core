$if(notification_attachments)$
<p>Вложения:</p>
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
0; радиус границы: 10 пикселей; граница: 1px сплошная #ccc; размер шрифта: 12px; цвет:#333; вес шрифта: полужирный;
украшение текста: нет; отступы: 10 пикселей; фон: линейный градиент (#fff, #eee) повторите прокрутку 0 0 #eee;&quot;&gt;
<span style="color:#7eb73b">►</span> $notification_linkLabel$
</a>
$endif$
