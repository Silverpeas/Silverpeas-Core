$if(notification_receiver_users)$<div style="padding:10px 0 0 0;">
$elseif(notification_receiver_groups)$<div style="padding:10px 0 0 0;">
$endif$
$if(notification_receiver_users)$
<i>Це повідомлення було надіслано користувачам: $notification_receiver_users$</i>
$endif$
$if(notification_receiver_groups)$
$if(!notification_receiver_users)$
<i>Це повідомлення було надіслано групам: $notification_receiver_groups$</i>
$endif$
$if(notification_receiver_users)$
<br/><i>до груп: $notification_receiver_groups$</i>
$endif$
$endif$
$if(notification_receiver_users)$</div>
$elseif(notification_receiver_groups)$</div>
$endif$
