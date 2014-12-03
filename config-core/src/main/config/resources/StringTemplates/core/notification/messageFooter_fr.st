$if(notification_receiver_users)$
<i>Ce message a été envoyé aux utilisateurs : $notification_receiver_users$</i>
$endif$
$if(notification_receiver_groups)$
$if(!notification_receiver_users)$
<i>Ce message a été envoyé aux groupes : $notification_receiver_groups$</i>
$endif$
$if(notification_receiver_users)$

<i>aux groupes : $notification_receiver_groups$</i>
$endif$
$endif$