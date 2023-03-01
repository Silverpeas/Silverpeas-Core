<b>$senderUser.firstName$ $senderUser.lastName$</b> общий файл <i>$ticket.resource.name$</i> .
$if(ticket.endDate)$Внимание, файл доступен до $ticket.endDate; format="dd/MM/yyyy"$. $if(limitedAccess)$Количество загрузок ограничено $ticket.nbAccessMax$.$endif$
$endif$


$if(additionalMessage)$
Сообщение :
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$additionalMessage$</div>
$endif$
