<b>$senderUser.firstName$ $senderUser.lastName$</b> have shared the following publication <i>$ticket.resource.name$</i>.
$if(ticket.endDate)$Be careful this publication is available until $ticket.endDate; format="MM/dd/yyyy"$.
$endif$


$if(additionalMessage)$
Message :
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$additionalMessage$</div>
$endif$