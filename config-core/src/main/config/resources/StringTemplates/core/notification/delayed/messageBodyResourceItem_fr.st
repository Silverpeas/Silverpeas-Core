Ressource : $resource.name$
	- emplacement : $resource.location$
	- accès : <a href="$resource.url$" target=_blank>Cliquer ici ...</a>
	- notification$if(resource.severalNotifications)$s$endif$ :
		$first(resource.notifications):notification/delayed/messageBodyResourceNotificationItem_fr(notification=it)$$rest(resource.notifications):{$\n$$notification/delayed/messageBodyResourceNotificationItem_fr(notification=it)$}$