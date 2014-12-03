<b>$senderUser.firstName$ $senderUser.lastName$</b> has shared file <i>$ticket.resource.name$</i>.
$if(ticket.endDate)$Be careful this download is available until $ticket.endDate; format="MM/dd/yyyy"$. $if(limitedAccess)$There is also a limited access number : $ticket.nbAccessMax$.$endif$
$endif$


$if(additionalMessage)$
Message :
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$additionalMessage$</div>
$endif$