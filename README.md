
### How to run the app
```sbt run```

In your browser and go to

```localhost:9000/api-docs```

From Swagger-UI interface you can perform all the CRUD operations

### How to run the tests
```sbt test```

## Generating SSL certificate for mongodb

Certificates are already generated and the needed files are stored in test/resources directory. If you need to recreate it then do the following

Go to the scripts and run the 'mongo_generate_certificates.sh' file
`./mongo_generate_certificates.sh`

It should create all necessary files and copy over server.pem, client.pem and truststore to test/resources directory

For reference use the following articles
* https://docs.mongodb.com/manual/appendix/security/appendixA-openssl-ca/
* https://docs.mongodb.com/manual/appendix/security/appendixB-openssl-server/#appendix-server-certificate
* https://docs.mongodb.com/manual/appendix/security/appendixC-openssl-client/#appendix-client-certificate

## Running mongodb locally for dev

Start mongod with SSL
```
cd test/resources
mkdir -p data/db
mongod --sslMode requireSSL --sslPEMKeyFile server.pem --sslCAFile ca.crt --dbpath data/db --logpath data/mongod.log --fork
```

Connect to mongod with SSL
``` 
mongo --ssl --sslCAFile ca.crt --sslPEMKeyFile client.pem --host localhost 
```
Once you are connected then run the following to create user for X.509 authentication

```
db.getSiblingDB("$external").runCommand(
  {
    createUser: "emailAddress=me@example.com,CN=localhost,OU=client,O=MongoDB,L=London,ST=London,C=GB",
    roles: [
         { role: "readWrite", db: "testdb" },
         { role: "userAdminAnyDatabase", db: "admin" }
    ],
    writeConcern: { w: "majority" , wtimeout: 5000 }
  }
);
```
Assuming you haven't changed values inside mongo_generate_certificates file. If you have changed then get the subject value correctly by

`openssl x509 -in client.pem -inform PEM -subject -nameopt RFC2253`

If you prefer to run it in a docker container then use the following

``docker run  
  -e MONGO_INITDB_ROOT_USERNAME=test 
  -e MONGO_INITDB_ROOT_PASSWORD=test 
  -e MONGO_INITDB_DATABASE=admin 
  -p 27017:27017
  -v  `pwd`/test/resources/:/etc/ssl/
  mongo:4.0.8-xenial
  --sslMode requireSSL --sslPEMKeyFile /etc/ssl/server.pem``
  
  For connecting to local db 
  
  `mongo -ssl localhost:27017/admin -u test -p test --sslPEMKeyFile test/resources/client.pem --sslAllowInvalidCertificates`
  
  once you are connected, run your commands. If you need a UI, use NoSQLBooster
    
    
## Running Tests from IntelliJ

Tests need the truststore and the password to connect to mongodb database. 
So if you need to run any tests which is using mongodb then add the following as VM parameters in Test Configurations of IntelliJ

`-Djavax.net.ssl.trustStore=test/resources/truststore.ts -Djavax.net.ssl.trustStorePassword=changeit`

One side effect of using a custom truststore is that when you need to connect to a new service (in future) then you will need to add those certificates to truststore
For example if you encounter “unable to find valid certification path to requested target” then do the following


`openssl s_client -showcerts -connect <service-domain-name>:<port>`

For example Amazon secrets manager has got the following value secretsmanager.eu-west-1.amazonaws.com:443

Then store those individual certificates (including the begin and end certificate line) as individual files.
After that import each one to the truststore stored in test/resources directory

`keytool -importcert -alias caRoot -file CARoot.crt -keystore truststore.ts -storepass changeit`

### Acknowledgements

https://github.com/ricsirigu/play26-swagger-reactivemongo
https://www.grainger.xyz/creating-x-509-certificates-for-mongodb/
https://docs.mongodb.com/manual/tutorial/configure-x509-client-authentication/#add-x-509-certificate-subject-as-a-user

