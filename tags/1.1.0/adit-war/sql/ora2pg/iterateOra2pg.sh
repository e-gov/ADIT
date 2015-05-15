#!/bin/bash

confFileLocation=$1
tableName=$2
firstId=$3
increment=$4
dataLimit=$5
sequence=$6

sed -i s/"^ALLOW[ \t].*/ALLOW $tableName/" $confFileLocation
sed -i s/"^DATA_LIMIT.*/DATA_LIMIT $dataLimit/" $confFileLocation

# sequence value example: SELECT $sequence.nextval FROM dual;
# SELECT max(id) FROM $tableName

sqlSentence="SELECT max(id) FROM  $tableName"

if [ -z "$sequence" ];
then
        sqlSentence="SELECT max(id) FROM  $tableName;"
else
        sqlSentence="SELECT ${sequence}_ID_SEQ.nextval FROM dual;"
fi

maxId=`LD_LIBRARY_PATH=/opt/instantclient/ /opt/instantclient/sqlplus -S "ADIT/quGnpUCqTEIy@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(Host=10.0.13.66)(Port=1521))(CONNECT_DATA=(SERVICE_NAME=adit.arendus.kit)))" << EOF
        set pagesize 0  feedback off    verify off      heading off     echo off;
        $sqlSentence
        exit;
EOF`

echo -e "Exporting data from \nTable name: $tableName \nExpected rows: $maxId"
lastId=0

for (( firstId; firstId<=$maxId; firstId=firstId + $increment))
do
        lastId=$(($firstId + $increment - 1))
        echo -e "Generating output from: $firstId to $lastId"
        sed -i s/"^WHERE.*/WHERE id BETWEEN $firstId AND $lastId/" $confFileLocation
        sed -i s/"^OUTPUT.*/OUTPUT ${tableName}_${firstId}_to_${lastId}.sql/" $confFileLocation
        sh ./ora2pg
done