package gclaramunt

import gclaramunt.unichain.blockchain.CryptoOps.decodePEMKeys

package object unichain:

  val serverPrvKeyStr: String =
    """
      |-----BEGIN EC PRIVATE KEY-----
      |MHcCAQEEICBBw4kGhvD+xSeg8oc1uFQS+vQSCt0uEdjTwKIM2QTyoAoGCCqGSM49
      |AwEHoUQDQgAEXXT8kZ/N5fBWtnGAwMkrEVQEYVxJG5B+g724imNN0cJWIg+wpjIC
      |ZrYebyI3XYdXfL64sxEzxfunzJCeWplDcQ==
      |-----END EC PRIVATE KEY-----
      |""".stripMargin
  
  val (serverPrvKey, serverPubKey) = decodePEMKeys(serverPrvKeyStr)

  val (w1PrvKey, w1PubKey) = decodePEMKeys(
    """
      |-----BEGIN EC PRIVATE KEY-----
      |MHcCAQEEII5rs4/l8RO9+PlTRdS/tYOnCbvwTfZYAiveCeZJ/D0boAoGCCqGSM49
      |AwEHoUQDQgAEH8d8OeXKt7orLzIVH5IwCRVLnWLPbihFz34OH3y2qDEgGJSudsM2
      |bbE8vHLiv7koyHgEgk0C0Sg0Xl+VyRToNw==
      |-----END EC PRIVATE KEY-----
      |""".stripMargin)

  val (w2PrvKey, w2PubKey) = decodePEMKeys(
    """
      |-----BEGIN EC PRIVATE KEY-----
      |MHcCAQEEIEkftoKFylQRlgzyXcLnf0bjSnsO0s8mwudjf6K18/NuoAoGCCqGSM49
      |AwEHoUQDQgAE/7QUbTYabwOR98waCxiPpuVVXvqko4PxaEuEuFfh4wCvRhaSMZAS
      |uJNvQMxu2ZBA/odSVi9MTVYMmJ9aGZWBTA==
      |-----END EC PRIVATE KEY-----
      |""".stripMargin)