# uni-chain

Please submit the project as a public GitHub repository, containing a README explaining how
to run the project (including tests) the architectural design and decisions made, and how this
project would be extended for production readiness.


## Intro
Unichain is a single node blockchain. Blocks are generated after a certain amount of transactions is received. 
Transactions and blocks are stored in a database (right now it uses H2) and the current state is kept in memory. 


### Possible Enhancements
  * Maybe a block can be closed if a certain time passes without new transactions.
  * Reject transactions from and to the same address to prevent spam.
  * As the network grow, keeping all balances in memory can become impossible, so it can be replaced with direct queries to the db.   
  * Better logging
    

## Building and testing
To build, only java ( I've used openjdk 22.0.2 ) and sbt are needed.
To build the code:
```console
sbt compile
```
The code includes munit test suites to cover different scenarios, to run them use:
```console
sbt test
```

To assemble the fat jar use:
```console
sbt assembly
```

openssl ecparam -name prime256v1 -genkey -noout -out ecdsa_private_key.pem

## only for testing purposes


export SERVER_PRIVATE_KEY="
-----BEGIN EC PRIVATE KEY-----
MHcCAQEEICBBw4kGhvD+xSeg8oc1uFQS+vQSCt0uEdjTwKIM2QTyoAoGCCqGSM49
AwEHoUQDQgAEXXT8kZ/N5fBWtnGAwMkrEVQEYVxJG5B+g724imNN0cJWIg+wpjIC
ZrYebyI3XYdXfL64sxEzxfunzJCeWplDcQ==
-----END EC PRIVATE KEY-----"


1206  java -cp target/scala-3.5.0/unichain-assembly-0.0.1-SNAPSHOT.jar gclaramunt.unichain.Genesis 10000
java -cp target/scala-3.5.0/unichain-assembly-0.0.1-SNAPSHOT.jar gclaramunt.unichain.UniChainServiceGrpcServer


export SERVER_PRIVATE_KEY=" 
-----BEGIN EC PRIVATE KEY-----
MHcCAQEEICBBw4kGhvD+xSeg8oc1uFQS+vQSCt0uEdjTwKIM2QTyoAoGCCqGSM49
AwEHoUQDQgAEXXT8kZ/N5fBWtnGAwMkrEVQEYVxJG5B+g724imNN0cJWIg+wpjIC
ZrYebyI3XYdXfL64sxEzxfunzJCeWplDcQ==
-----END EC PRIVATE KEY-----"

server address
MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEXXT8kZ/N5fBWtnGAwMkrEVQEYVxJG5B+g724imNN0cJWIg+wpjICZrYebyI3XYdXfL64sxEzxfunzJCeWplDcQ==


wallet1 PEM Key
-----BEGIN EC PRIVATE KEY-----
MHcCAQEEII5rs4/l8RO9+PlTRdS/tYOnCbvwTfZYAiveCeZJ/D0boAoGCCqGSM49
AwEHoUQDQgAEH8d8OeXKt7orLzIVH5IwCRVLnWLPbihFz34OH3y2qDEgGJSudsM2
bbE8vHLiv7koyHgEgk0C0Sg0Xl+VyRToNw==
-----END EC PRIVATE KEY-----

w1 address
MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEH8d8OeXKt7orLzIVH5IwCRVLnWLPbihFz34OH3y2qDEgGJSudsM2bbE8vHLiv7koyHgEgk0C0Sg0Xl+VyRToNw==

wallet2 PEM Key
-----BEGIN EC PRIVATE KEY-----
MHcCAQEEIEkftoKFylQRlgzyXcLnf0bjSnsO0s8mwudjf6K18/NuoAoGCCqGSM49
AwEHoUQDQgAE/7QUbTYabwOR98waCxiPpuVVXvqko4PxaEuEuFfh4wCvRhaSMZAS
uJNvQMxu2ZBA/odSVi9MTVYMmJ9aGZWBTA==
-----END EC PRIVATE KEY-----

w2 address
MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE/7QUbTYabwOR98waCxiPpuVVXvqko4PxaEuEuFfh4wCvRhaSMZASuJNvQMxu2ZBA/odSVi9MTVYMmJ9aGZWBTA==


Start server
java -cp target/scala-3.5.0/unichain-assembly-0.0.1-SNAPSHOT.jar gclaramunt.unichain.UnichainServiceGrpcServer


send from treasury to wallet 1
java -cp target/scala-3.5.0/unichain-assembly-0.0.1-SNAPSHOT.jar gclaramunt.unichain.UnichainGrpcClient MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEH8d8OeXKt7orLzIVH5IwCRVLnWLPbihFz34OH3y2qDEgGJSudsM2bbE8vHLiv7koyHgEgk0C0Sg0Xl+VyRToNw== 10
send from treasury to wallet 2
java -cp target/scala-3.5.0/unichain-assembly-0.0.1-SNAPSHOT.jar gclaramunt.unichain.UnichainGrpcClient MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE/7QUbTYabwOR98waCxiPpuVVXvqko4PxaEuEuFfh4wCvRhaSMZASuJNvQMxu2ZBA/odSVi9MTVYMmJ9aGZWBTA== 10



1187  export export WALLET_PRIVATE_KEY="
-----BEGIN EC PRIVATE KEY-----
MHcCAQEEICBBw4kGhvD+xSeg8oc1uFQS+vQSCt0uEdjTwKIM2QTyoAoGCCqGSM49
AwEHoUQDQgAEXXT8kZ/N5fBWtnGAwMkrEVQEYVxJG5B+g724imNN0cJWIg+wpjIC
ZrYebyI3XYdXfL64sxEzxfunzJCeWplDcQ==
-----END EC PRIVATE KEY-----"
1188  java -cp target/scala-3.5.0/unichain-assembly-0.0.1-SNAPSHOT.jar gclaramunt.unichain.UnichainGrpcClient MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE/7QUbTYabwOR98waCxiPpuVVXvqko4PxaEuEuFfh4wCvRhaSMZASuJNvQMxu2ZBA/odSVi9MTVYMmJ9aGZWBTA== 10
1189  java -cp target/scala-3.5.0/unichain-assembly-0.0.1-SNAPSHOT.jar gclaramunt.unichain.UnichainGrpcClient MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEH8d8OeXKt7orLzIVH5IwCRVLnWLPbihFz34OH3y2qDEgGJSudsM2bbE8vHLiv7koyHgEgk0C0Sg0Xl+VyRToNw== 10
1190  java -cp target/scala-3.5.0/unichain-assembly-0.0.1-SNAPSHOT.jar gclaramunt.unichain.UnichainGrpcClient MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEH8d8OeXKt7orLzIVH5IwCRVLnWLPbihFz34OH3y2qDEgGJSudsM2bbE8vHLiv7koyHgEgk0C0Sg0Xl+VyRToNw==
1191  java -cp target/scala-3.5.0/unichain-assembly-0.0.1-SNAPSHOT.jar gclaramunt.unichain.UnichainGrpcClient MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE/7QUbTYabwOR98waCxiPpuVVXvqko4PxaEuEuFfh4wCvRhaSMZASuJNvQMxu2ZBA/odSVi9MTVYMmJ9aGZWBTA==
1192  java -cp target/scala-3.5.0/unichain-assembly-0.0.1-SNAPSHOT.jar gclaramunt.unichain.UnichainGrpcClient MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEH8d8OeXKt7orLzIVH5IwCRVLnWLPbihFz34OH3y2qDEgGJSudsM2bbE8vHLiv7koyHgEgk0C0Sg0Xl+VyRToNw==
1193  java -cp target/scala-3.5.0/unichain-assembly-0.0.1-SNAPSHOT.jar gclaramunt.unichain.UnichainGrpcClient MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE/7QUbTYabwOR98waCxiPpuVVXvqko4PxaEuEuFfh4wCvRhaSMZASuJNvQMxu2ZBA/odSVi9MTVYMmJ9aGZWBTA==
1194  java -cp target/scala-3.5.0/unichain-assembly-0.0.1-SNAPSHOT.jar gclaramunt.unichain.UnichainGrpcClient MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEH8d8OeXKt7orLzIVH5IwCRVLnWLPbihFz34OH3y2qDEgGJSudsM2bbE8vHLiv7koyHgEgk0C0Sg0Xl+VyRToNw==
1195  java -cp target/scala-3.5.0/unichain-assembly-0.0.1-SNAPSHOT.jar gclaramunt.unichain.UnichainGrpcClient MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEXXT8kZ/N5fBWtnGAwMkrEVQEYVxJG5B+g724imNN0cJWIg+wpjICZrYebyI3XYdXfL64sxEzxfunzJCeWplDcQ==
1196  history
