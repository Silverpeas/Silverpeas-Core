ALTER TABLE sb_interest_center RENAME TO sb_interests;
ALTER TABLE sb_interest_center_axis RENAME TO sb_interests_axis;
UPDATE uniqueid SET tablename = 'sb_interests' WHERE tablename = 'sb_interest_center';