delete from ST_AccessLevel where id = 'U';
delete from ST_AccessLevel where id = 'A';

delete from ST_UserSetType where id = 'G';
delete from ST_UserSetType where id = 'R';
delete from ST_UserSetType where id = 'I';
delete from ST_UserSetType where id = 'S';
delete from ST_UserSetType where id = 'M';

delete from ST_User where id = 0;

delete from DomainSP_User where id = 0;

delete from ST_Domain where id = -1;
delete from ST_Domain where id = 0;
