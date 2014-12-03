$if(notification_receiver_users)$
<i>This message has been sent to users : $notification_receiver_users$</i>
$endif$
$if(notification_receiver_groups)$
$if(!notification_receiver_users)$
<i>This message has been sent to groups : $notification_receiver_groups$</i>
$endif$
$if(notification_receiver_users)$

<i>to groups : $notification_receiver_groups$</i>
$endif$
$endif$