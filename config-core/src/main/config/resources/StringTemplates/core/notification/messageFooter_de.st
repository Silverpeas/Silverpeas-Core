$if(notification_receiver_users)$
<i>Diese Nachricht wurde gesendet nutzer : $notification_receiver_users$</i>
$endif$
$if(notification_receiver_groups)$
$if(!notification_receiver_users)$
<i>Diese Nachricht wurde gesendet gruppen : $notification_receiver_groups$</i>
$endif$
$if(notification_receiver_users)$

<i>gruppen : $notification_receiver_groups$</i>
$endif$
$endif$