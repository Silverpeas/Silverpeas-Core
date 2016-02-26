ALTER TABLE sb_agenda_holidays WITH NOCHECK ADD
	 CONSTRAINT pk_sb_agenda_holidays PRIMARY KEY  CLUSTERED
	(
		userid,
		holidaydate
	)
;
