<b>$senderUser.firstName$ $senderUser.lastName$</b> a partagé la publication <i>$ticket.resource.name$</i>.
$if(ticket.endDate)$Attention cette publication est disponible jusqu'au $ticket.endDate; format="dd/MM/yyyy"$.
$endif$


$if(additionalMessage)$
Message :
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$additionalMessage$</div>
$endif$