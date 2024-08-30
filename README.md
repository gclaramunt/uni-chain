# Uni-chain

## Intro
Unichain is a single node blockchain. Blocks are generated after a certain amount of transactions is received.
Transactions are validates with a hash of the nonce, destination, and amount, signed with the private key corresponding to the origin adress.
Blocks are validated with a hash of the block id, and all the transaction hashes , combined with the previous block hash, and signed with the server private key.
Processed transactions and blocks are stored in an embedded database (H2) and the current state is kept in memory.
The server exposes a gRPC interface (see [unichain.proto](./src/main/protobuf/unichain.proto)) and a convenience client.
It has no external infrastructure dependencies, 

### Possible Enhancements
  * Maybe a block can be closed if a certain time passes without new transactions.
  * Reject transactions from and to the same address to prevent spam.
  * As the network grow, keeping all balances in memory can become impossible, so it can be replaced with direct queries to the db.   
  * Better logging
  * Better error handling code
  * Add ReST endpoints
  * Use a keystore/vault for keys
    

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

## Running

### Initial setup

The first setp is to generate a ecdsa key pair for the server.
E.g. using openssl:
```shell
openssl ecparam -name prime256v1 -genkey -noout -out ecdsa_private_key.pem
```
Export the generated private key in the corresponding environment variable, 
`SERVER_PRIVATE_KEY` for server actions, and `WALLET_PRIVATE_KEY` for the client.

```shell
export SERVER_PRIVATE_KEY="
-----BEGIN EC PRIVATE KEY-----
MHcCAQEEICBBw4kGhvD+xSeg8oc1uFQS+vQSCt0uEdjTwKIM2QTyoAoGCCqGSM49
AwEHoUQDQgAEXXT8kZ/N5fBWtnGAwMkrEVQEYVxJG5B+g724imNN0cJWIg+wpjIC
ZrYebyI3XYdXfL64sxEzxfunzJCeWplDcQ==
-----END EC PRIVATE KEY-----"
```


### Note on key handling
Private keys are read from environment variables. 
Not a fully secure approach for production systems, but e.g. containres can inject them directly from a secure vault.


### Genesis
To set up the database and create the intial block with the treasury populating transaction, you need to run the genesis program, with the amount to put in the treasury as a parameter:
```shell
java -cp target/scala-3.5.0/unichain-assembly-0.0.1-SNAPSHOT.jar gclaramunt.unichain.Genesis 10000
```
The balance will be credited at the address corresponding to the server public key:
`MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEXXT8kZ/N5fBWtnGAwMkrEVQEYVxJG5B+g724imNN0cJWIg+wpjICZrYebyI3XYdXfL64sxEzxfunzJCeWplDcQ==`

###Run the server
Start the server with:
```shell
java -cp target/scala-3.5.0/unichain-assembly-0.0.1-SNAPSHOT.jar gclaramunt.unichain.UnichainServiceGrpcServer
```
It will reconstruct the current state from the database and start listening to grpc connections.

###Running the client
Let's generate two new keypairs for the client (wallets) and export them in two different consoles.

Export Wallet1 PEM Key in console 1
```shell
export WALLET_PRIVATE_KEY="-----BEGIN EC PRIVATE KEY-----
MHcCAQEEII5rs4/l8RO9+PlTRdS/tYOnCbvwTfZYAiveCeZJ/D0boAoGCCqGSM49
AwEHoUQDQgAEH8d8OeXKt7orLzIVH5IwCRVLnWLPbihFz34OH3y2qDEgGJSudsM2
bbE8vHLiv7koyHgEgk0C0Sg0Xl+VyRToNw==
-----END EC PRIVATE KEY-----"
```
with a corresponding public key/address:
`MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEH8d8OeXKt7orLzIVH5IwCRVLnWLPbihFz34OH3y2qDEgGJSudsM2bbE8vHLiv7koyHgEgk0C0Sg0Xl+VyRToNw==`

Export Wallet2 PEM Key in console 2
```shell
export WALLET_PRIVATE_KEY="-----BEGIN EC PRIVATE KEY-----
MHcCAQEEIEkftoKFylQRlgzyXcLnf0bjSnsO0s8mwudjf6K18/NuoAoGCCqGSM49
AwEHoUQDQgAE/7QUbTYabwOR98waCxiPpuVVXvqko4PxaEuEuFfh4wCvRhaSMZAS
uJNvQMxu2ZBA/odSVi9MTVYMmJ9aGZWBTA==
-----END EC PRIVATE KEY-----"
```
with a corresponding public key/address:
`MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE/7QUbTYabwOR98waCxiPpuVVXvqko4PxaEuEuFfh4wCvRhaSMZASuJNvQMxu2ZBA/odSVi9MTVYMmJ9aGZWBTA==`

And export the server private key in another console:
```shell
export WALLET_PRIVATE_KEY="-----BEGIN EC PRIVATE KEY-----
MHcCAQEEICBBw4kGhvD+xSeg8oc1uFQS+vQSCt0uEdjTwKIM2QTyoAoGCCqGSM49
AwEHoUQDQgAEXXT8kZ/N5fBWtnGAwMkrEVQEYVxJG5B+g724imNN0cJWIg+wpjIC
ZrYebyI3XYdXfL64sxEzxfunzJCeWplDcQ==
-----END EC PRIVATE KEY-----"
```
from this console we can check the treasury balance and pay to the other wallets:

1. Check balance
```shell
java -cp target/scala-3.5.0/unichain-assembly-0.0.1-SNAPSHOT.jar gclaramunt.unichain.UnichainGrpcClient MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEXXT8kZ/N5fBWtnGAwMkrEVQEYVxJG5B+g724imNN0cJWIg+wpjICZrYebyI3XYdXfL64sxEzxfunzJCeWplDcQ==
...
Balance of address MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEXXT8kZ/N5fBWtnGAwMkrEVQEYVxJG5B+g724imNN0cJWIg+wpjICZrYebyI3XYdXfL64sxEzxfunzJCeWplDcQ== = 10000
```
2. Send from treasury to wallet 1
```shell
java -cp target/scala-3.5.0/unichain-assembly-0.0.1-SNAPSHOT.jar gclaramunt.unichain.UnichainGrpcClient MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEH8d8OeXKt7orLzIVH5IwCRVLnWLPbihFz34OH3y2qDEgGJSudsM2bbE8vHLiv7koyHgEgk0C0Sg0Xl+VyRToNw== 10
...
Response: success
Success
```

3. Send from treasury to wallet 2
```shell
java -cp target/scala-3.5.0/unichain-assembly-0.0.1-SNAPSHOT.jar gclaramunt.unichain.UnichainGrpcClient MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE/7QUbTYabwOR98waCxiPpuVVXvqko4PxaEuEuFfh4wCvRhaSMZASuJNvQMxu2ZBA/odSVi9MTVYMmJ9aGZWBTA== 20
...
Response: success
Success
```
4. Check the balances
```shell
java -cp target/scala-3.5.0/unichain-assembly-0.0.1-SNAPSHOT.jar gclaramunt.unichain.UnichainGrpcClient MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEXXT8kZ/N5fBWtnGAwMkrEVQEYVxJG5B+g724imNN0cJWIg+wpjICZrYebyI3XYdXfL64sxEzxfunzJCeWplDcQ==
Balance of address MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEXXT8kZ/N5fBWtnGAwMkrEVQEYVxJG5B+g724imNN0cJWIg+wpjICZrYebyI3XYdXfL64sxEzxfunzJCeWplDcQ== = 9970

java -cp target/scala-3.5.0/unichain-assembly-0.0.1-SNAPSHOT.jar gclaramunt.unichain.UnichainGrpcClient MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE/7QUbTYabwOR98waCxiPpuVVXvqko4PxaEuEuFfh4wCvRhaSMZASuJNvQMxu2ZBA/odSVi9MTVYMmJ9aGZWBTA==
Balance of address MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE/7QUbTYabwOR98waCxiPpuVVXvqko4PxaEuEuFfh4wCvRhaSMZASuJNvQMxu2ZBA/odSVi9MTVYMmJ9aGZWBTA== = 20
   
java -cp target/scala-3.5.0/unichain-assembly-0.0.1-SNAPSHOT.jar gclaramunt.unichain.UnichainGrpcClient MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEH8d8OeXKt7orLzIVH5IwCRVLnWLPbihFz34OH3y2qDEgGJSudsM2bbE8vHLiv7koyHgEgk0C0Sg0Xl+VyRToNw==
Balance of address MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEH8d8OeXKt7orLzIVH5IwCRVLnWLPbihFz34OH3y2qDEgGJSudsM2bbE8vHLiv7koyHgEgk0C0Sg0Xl+VyRToNw== = 10
```

5. From wallet 1 send to wallet 2
```shell
java -cp target/scala-3.5.0/unichain-assembly-0.0.1-SNAPSHOT.jar gclaramunt.unichain.UnichainGrpcClient MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE/7QUbTYabwOR98waCxiPpuVVXvqko4PxaEuEuFfh4wCvRhaSMZASuJNvQMxu2ZBA/odSVi9MTVYMmJ9aGZWBTA== 7
...
java -cp target/scala-3.5.0/unichain-assembly-0.0.1-SNAPSHOT.jar gclaramunt.unichain.UnichainGrpcClient MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE/7QUbTYabwOR98waCxiPpuVVXvqko4PxaEuEuFfh4wCvRhaSMZASuJNvQMxu2ZBA/odSVi9MTVYMmJ9aGZWBTA==
Balance of address MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE/7QUbTYabwOR98waCxiPpuVVXvqko4PxaEuEuFfh4wCvRhaSMZASuJNvQMxu2ZBA/odSVi9MTVYMmJ9aGZWBTA== = 27

java -cp target/scala-3.5.0/unichain-assembly-0.0.1-SNAPSHOT.jar gclaramunt.unichain.UnichainGrpcClient MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEXXT8kZ/N5fBWtnGAwMkrEVQEYVxJG5B+g724imNN0cJWIg+wpjICZrYebyI3XYdXfL64sxEzxfunzJCeWplDcQ==
Balance of address MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEH8d8OeXKt7orLzIVH5IwCRVLnWLPbihFz34OH3y2qDEgGJSudsM2bbE8vHLiv7koyHgEgk0C0Sg0Xl+VyRToNw== = 3
```

6. Try to overdraft wallet 1
```shell
java -cp target/scala-3.5.0/unichain-assembly-0.0.1-SNAPSHOT.jar gclaramunt.unichain.UnichainGrpcClient MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEXXT8kZ/N5fBWtnGAwMkrEVQEYVxJG5B+g724imNN0cJWIg+wpjICZrYebyI3XYdXfL64sxEzxfunzJCeWplDcQ== 500

io.grpc.StatusRuntimeException: INTERNAL: Source final balance can't be less than 0

```