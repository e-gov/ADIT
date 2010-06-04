create tablespace ADIT_INDX
  logging
  datafile 'adit_indx.dbf' 
  size 32m 
  autoextend on 
  next 32m maxsize 2048m
  extent management local;
  
create tablespace ADIT_DATA
  logging
  datafile 'adit_data.dbf' 
  size 32m 
  autoextend on 
  next 32m maxsize 2048m
  extent management local;
  