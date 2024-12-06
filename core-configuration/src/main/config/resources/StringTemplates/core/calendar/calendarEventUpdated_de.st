$if(several)$
$senderName$ hat allen Veranstaltungen <b>$contributionName$</b>$if(contributionStartDate)$ ab dem <i>$contributionDate$</i> geändert$else$ geändert$endif$
$else$
$senderName$ hat die Veranstaltung <b>$contributionName$</b>$if(contributionStartDate)$$if(contributionEndDate)$ vom <i>$contributionStartDate$</i> bis zum <i>$contributionEndDate$</i> geändert$else$ für den <i>$contributionStartDate$</i> bearbeitet$endif$$else$ geändert$endif$
$endif$