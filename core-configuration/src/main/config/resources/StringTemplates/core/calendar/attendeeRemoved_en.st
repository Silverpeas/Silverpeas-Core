$if(several)$
$senderName$ has removed your participation in all the events <b>$contributionName$</b><i>$contributionDate$</i>$if(contributionStartDate)$ from <i>$contributionStartDate$</i>$endif$
$else$
$senderName$ has removed your participation in the event <b>$contributionName$</b>$if(contributionStartDate)$$if(contributionEndDate)$ from <i>$contributionStartDate$</i> to <i>$contributionEndDate$</i>$else$ for <i>$contributionStartDate$</i>$endif$$endif$
$endif$