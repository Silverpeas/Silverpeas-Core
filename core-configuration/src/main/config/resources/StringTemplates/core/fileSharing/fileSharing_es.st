<b>$senderUser.firstName$ $senderUser.lastName$</b> archivo compartido <i>$ticket.resource.name$</i> .
$if(ticket.endDate)$Atención el archivo está disponible hasta el $ticket.endDate; format="dd/MM/yyyy"$. $if(limitedAccess)$El número de descargas está limitado a $ticket.nbAccessMax$.$endif$
$endif$


$if(additionalMessage)$
Mensaje :
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$additionalMessage$</div>
$endif$
