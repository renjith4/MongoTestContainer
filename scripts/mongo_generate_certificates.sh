#!/bin/sh

# This file generates the certificates and keys needed for mongo testing.
# Gist modified from https://gist.github.com/kevinadi/96090f6f9973ff8c2d019bbe0d9a0f70

# Generate self signed root CA cert
# if you need a different hostname other than localhost then change localhost to `hostname -f`  (i.e with the single quote)
openssl req -nodes -x509 -newkey rsa:2048 -keyout ca.key -out ca.crt -subj "/C=GB/ST=London/L=London/O=MongoDB/OU=root/CN=localhost/emailAddress=me@example.com"


# Generate server cert to be signed
openssl req -nodes -newkey rsa:2048 -keyout server.key -out server.csr -subj "/C=GB/ST=London/L=London/O=MongoDB/OU=server/CN=localhost/emailAddress=me@example.com"

# Sign the server cert
openssl x509 -req -in server.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out server.crt

# Create server PEM file
cat server.key server.crt > server.pem


# Generate client cert to be signed
openssl req -nodes -newkey rsa:2048 -keyout client.key -out client.csr -subj "/C=GB/ST=London/L=London/O=MongoDB/OU=client/CN=localhost/emailAddress=me@example.com"

# Sign the client cert
openssl x509 -req -in client.csr -CA ca.crt -CAkey ca.key -CAserial ca.srl -out client.crt

# Create client PEM file
cat client.key client.crt > client.pem

# Import server certificate to truststore
keytool -import -alias "MongoDB-cert" -file server.crt -keystore truststore.ts -noprompt -storepass "changeit"

#Import client certificate to truststore
openssl pkcs12 -inkey client.key -in client.crt -export -out truststore.pkcs12 -password pass:changeit

# Copy server.pem to test/resources directory
cp server.pem ../test/resources/server.pem

# Copy client.pem to test/resources directory
cp client.pem ../test/resources/client.pem

# Copy ca.crt to test/resources directory
cp ca.crt ../test/resources/ca.crt

# Copy truststore.ts to test resources directory
cp truststore.ts ../test/resources/truststore.ts

# Copy truststore.pkcs12 to test resources directory
cp truststore.pkcs12 ../test/resources/truststore.pkcs12

# Start mongod with SSL
# mkdir -p data/db
# mongod --sslMode requireSSL --sslPEMKeyFile server.pem --sslCAFile ca.crt --dbpath data/db --logpath data/mongod.log --fork

# Connect to mongod with SSL
# mongo --ssl --sslCAFile ca.crt --sslPEMKeyFile client.pem --host localhost

# to get the subject from client certificate
# openssl x509 -in client.pem -inform PEM -subject -nameopt RFC2253
