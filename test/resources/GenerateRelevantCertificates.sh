#!/usr/bin/env bash

echo "Creating CA Pem files"
openssl req -passout pass:password -new -x509 -days 3650 -extensions v3_ca -keyout ca_private.pem -out ca.pem -subj "/CN=CA/OU=MongoDB Inc/O=Dev Team/L=London/ST=Greater London/C=GB"

echo "Creating User Certificate: Generate CSR"
openssl req -newkey rsa:2048 -nodes -out read-write.csr -keyout read-write.key -subj '/CN=readWrite/OU=MongoDB Inc/O=Dev Team/L=London/ST=Greater London/C=GB'

echo "Creating User Certificate: Generate certificate file"
openssl x509 -passin pass:password -sha256 -req -days 365 -in read-write.csr -signkey read-write.key -CA ca.pem -CAkey ca_private.pem -CAcreateserial -out read-write.crt

echo "Creating User Certificate: Combile to one"
cat read-write.crt read-write.key > read-write.pem

echo "Creating DB Certificate: Creating Key"
openssl req -newkey rsa:2048 -nodes -out db.csr -keyout db.key -subj '/CN=localhost/OU=webmaster/O=Dev Team/L=London/ST=Greater London/C=GB'

echo "Creating DB Certificate: Generate certificate file"
openssl x509 -passin pass:password -sha256 -req -days 365 -in db.csr -signkey db.key -CA ca.pem -CAkey ca_private.pem -CAcreateserial -out db.crt

echo "Creating DB Certificate: Combine file"
cat db.crt db.key > db.pem

echo "Importing certificate to truststore"
openssl pkcs12 -export -in db.pem -out keystore.pkcs12