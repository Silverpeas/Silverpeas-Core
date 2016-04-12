<div id="resource">
Ressource : $resource.name$<br/>
	<div id="resource_body" style="margin-left:15px;">
		$if(resource.description)$- description : $resource.description$<br/>$endif$
		- emplacement : $resource.location$<br/>
		$if(resource.url)$- accès : <a href="$resource.url$" target=_blank>Cliquer ici ...</a><br/>$endif$
		- notification$if(resource.severalNotifications)$s$endif$ :
		<div id="resource_item" style="margin-left:25px;">
			$first(resource.notifications):messageBodyResourceNotificationItem_fr(notification=it)$
			$rest(resource.notifications):{$if(!it.previousHasMessage)$<br/>$endif$$messageBodyResourceNotificationItem_fr(notification=it)$}$
		</div>
	</div>
</div>