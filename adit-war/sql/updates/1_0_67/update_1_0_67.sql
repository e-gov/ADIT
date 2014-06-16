--!!!!!! this script needs select priveleges on adit_dvk schemas DHL_MESSAGE_RECIPIENT table and select/update priveleges on adit schemas document, document_sharing tables!!!!!
update &&ADIT_SCHEMA..document_sharing docsh set docsh.dvk_id=
  (
    select distinct(mr.dhl_id) from &&ADIT_DVK_SCHEMA..DHL_MESSAGE_RECIPIENT mr join &&ADIT_DVK_SCHEMA..dhl_message dm on dm.dhl_message_id=mr.dhl_message_id where mr.dhl_message_id=
      (select doc.dvk_id from &&ADIT_SCHEMA..document doc join &&ADIT_SCHEMA..document_sharing ds on ds.document_id=doc.id where ds.id=docsh.id) and dm.is_incoming=0
  ) 
  where docsh.id in 
  (
    select ds.id from &&ADIT_SCHEMA..document_sharing ds where sharing_type='send_dvk' and ds.dvk_id is null
  )