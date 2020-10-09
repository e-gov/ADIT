# ADIT - Arenduse jaoks paigaldusjuhend docker-compose abil

## Postgre andmebaasi jooksutamine ja esialgne andmete import docker-composega

cd docker-compose
docker build . -t postgres94et
docker-compose up

Nüüd peaksid saama sisse logidaa:
adit_admin/xxx or adit_user/yyy
see 
jdbc:postgresql://localhost:5430/adit

Konteineri käivitamine, seiskamine:
docker-compose stop
docker-compose up
