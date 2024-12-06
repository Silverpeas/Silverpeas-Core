$if(several)$
$senderName$ $participation$ in the events <b>$contributionName$</b>$if(contributionStartDate)$ from <i>$contributionStartDate$</i>$endif$
$else$
$senderName$ $participation$ in the event <b>$contributionName$</b>$if(contributionStartDate)$$if(contributionEndDate)$ from <i>$contributionStartDate$</i> to <i>$contributionEndDate$</i>$else$ for <i>$contributionStartDate$</i>$endif$$endif$
$endif$