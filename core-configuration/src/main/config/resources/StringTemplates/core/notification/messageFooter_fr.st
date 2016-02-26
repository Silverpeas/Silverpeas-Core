$if(notification_receiver_users)$<div style="padding:10px 0 0 0;">
$elseif(notification_receiver_groups)$<div style="padding:10px 0 0 0;">
$endif$
$if(notification_receiver_users)$
<i>Ce message a été envoyé aux utilisateurs : $notification_receiver_users$</i>
$endif$
$if(notification_receiver_groups)$
$if(!notification_receiver_users)$
<i>Ce message a été envoyé aux groupes : $notification_receiver_groups$</i>
$endif$
$if(notification_receiver_users)$
<br/><i>aux groupes : $notification_receiver_groups$</i>
$endif$
$endif$
$if(notification_receiver_users)$</div>
$elseif(notification_receiver_groups)$</div>
$endif$