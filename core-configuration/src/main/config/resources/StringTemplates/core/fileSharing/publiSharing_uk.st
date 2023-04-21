<b>$senderUser.firstName$ $senderUser.lastName$</b> поділився публікацією <i>$ticket.resource.name$</i> .
$if(ticket.endDate)$Зверніть увагу, ця публікація доступна до $ticket.endDate; format="dd/MM/yyyy"$.
$endif$


$if(additionalMessage)$
Повідомлення :
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$additionalMessage$</div>
$endif$
