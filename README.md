# uni-chain


openssl ecparam -name prime256v1 -genkey -noout -out ecdsa_private_key.pem

openssl ec -in ecdsa_private_key.pem -pubout -out ecdsa_public_key.pem

## only for testing purposes
export PRIVATE_KEY=$(cat ecdsa_private_key.pem)

Genesis