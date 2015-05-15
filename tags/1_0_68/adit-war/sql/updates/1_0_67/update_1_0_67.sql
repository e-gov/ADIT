--!!!!!! this script needs select priveleges on adit_dvk schemas DHL_MESSAGE_RECIPIENT table and select/update priveleges on adit schemas document, document_sharing tables!!!!!
update adit.document_sharing docsh set docsh.dvk_id=
  (
    select mr.dhl_id from adit_dvk.DHL_MESSAGE_RECIPIENT mr join adit_dvk.dhl_message dm on dm.dhl_message_id=mr.dhl_message_id where mr.dhl_message_id=
      (select doc.dvk_id from adit.document doc join adit.document_sharing ds on ds.document_id=doc.id where ds.id=docsh.id) and dm.is_incoming=0
  ) 
  where docsh.id in 
  (
    select ds.id from adit.document_sharing ds where sharing_type='send_dvk' and ds.dvk_id is null
  )