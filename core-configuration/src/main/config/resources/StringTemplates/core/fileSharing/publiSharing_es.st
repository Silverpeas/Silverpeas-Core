<b>$senderUser.firstName$ $senderUser.lastName$</b> compartió la publicación <i>$ticket.resource.name$</i> .
$if(ticket.endDate)$Atención esta publicación está disponible hasta el $ticket.endDate; format="dd/MM/yyyy"$.
$endif$


$if(additionalMessage)$
Mensaje :
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$additionalMessage$</div>
$endif$
