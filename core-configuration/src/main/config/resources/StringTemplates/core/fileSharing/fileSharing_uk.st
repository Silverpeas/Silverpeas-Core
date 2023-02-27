<b>$senderUser.firstName$ $senderUser.lastName$</b> спільний файл <i>$ticket.resource.name$</i> .
$if(ticket.endDate)$Увага, файл доступний до $ticket.endDate; format="dd/MM/yyyy"$. $if(limitedAccess)$Кількість завантажень обмежена $ticket.nbAccessMax$.$endif$
$endif$


$if(additionalMessage)$
Повідомлення :
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$additionalMessage$</div>
$endif$
