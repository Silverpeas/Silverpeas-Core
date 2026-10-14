<div style="border-bottom:1px solid #C0C0C0; padding:5px 10px; margin:0 0 5px;">
	<div style="border-bottom:1px solid #CCCCCC;	padding:0 0 5px; margin:0; font-weight:100;	font-size:12px; color:#444444">
        <span>$resource.location$</span>
    </div>
    $if(resource.thumbnail)$<img style="margin:20px 0;" src="$resource.thumbnail$" alt="$resource.name$" height="$resource.thumbnailHeight$"/>$endif$
    <h2 style="color:#DF6900">
    $if(resource.url)$<a style="color:#DF6900" target="_blank" href="$resource.url$">$resource.name$</a>$else$$resource.name$$endif$
    </h2>
	$if(resource.description)$<p>$resource.description$</p>$endif$
	<ul style="margin:0 0 0 15px; padding:15px 0 0 15px; list-style-type: disc;">$resource.notifications:{n | $messageBodyResourceNotificationItem_de(notification=n)$}$</ul>
	$if(!resource.attachmentLinks.empty)$
  <p>Angehängte Dateien:</p>
    resource.attachmentLinks:{attachment | $attachmentLinks_de(attachment)$}$
  $endif$
	$if(resource.url)$<div><a target="_blank" href="$resource.url$" style="display:inline-block; margin:20px 0;border-radius:10px; border:1px solid #ccc; font-size:12px; color:#333; font-weight:bold;text-decoration:none;padding:10px;background: linear-gradient(#fff, #eee) repeat scroll 0 0 #eee;"> &#9658; $resource.linkLabel$</a></div>$endif$
</div>