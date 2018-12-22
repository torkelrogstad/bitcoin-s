[![Build Status](https://travis-ci.org/bitcoin-s/bitcoin-s-core.svg?branch=master)](https://travis-ci.org/bitcoin-s/bitcoin-s-core) [![Coverage Status](https://coveralls.io/repos/github/bitcoin-s/bitcoin-s-core/badge.svg?branch=master)](https://coveralls.io/github/bitcoin-s/bitcoin-s-core?branch=master) [![IRC Network](https://img.shields.io/badge/irc-%23bitcoin--scala-blue.svg "IRC Freenode")](https://webchat.freenode.net/?channels=bitcoin-scala)[![Gitter chat](https://badges.gitter.im/gitterHQ/gitter.png)](https://gitter.im/bitcoin-s-core)

# Bitcoin-S-Core

## Design Principles
  - Immutable data structures everywhere
  - Algebraic Data Types to allow the compiler to check for exhaustiveness on match statements
  - Using [property based testing](http://www.scalatest.org/user_guide/property_based_testing) to test robustness of code
  - Minimize dependencies to reduce attack surface

## Projects

1. core - this is where protocol data structures live, like [Transactions](core/src/main/scala/org/bitcoins/core/protocol/transaction/Transaction.scala) or [Blocks](core/src/main/scala/org/bitcoins/core/protocol/blockchain/Block.scala), or [PrivateKeys](core/src/main/scala/org/bitcoins/core/crypto/ECKey.scala). For more info visit the [core/README.md](core/README.md)

2. core-test - this is where all of the core test cases live

3. rpc - this is a rpc client implementation for bitcoind. If you need to interact with bitcoind please read [rpc/README.md](rpc/README.md)

4. eclair-rpc - this is a rpc client implementation for eclair, which is a Lightning Network implementation. For more information please read [eclair-rpc/README.md](eclair-rpc-README.md)

5. bench - benchmarks for bitcoin-s-core. For more information please read [bench/README.md](bench/README.md)

6. testkit - This is a useful testkit for testing bitcoin related applications, you can spin up bitcoind nodes and lightning nodes arbitrarily and set them in specific states. For more information please read [testkit/README.md](testkit/README.md)

7. zmq - bitcoind has a setting that publishes information about the state of the network over ZMQ. This project implements a subscriber that allows you to read and parse that information. For more information see [zmq/README.md](zmq/README.md)


## Artifacts

First you need to add our bintray to your resolvers to be able access published artifacts. With sbt, this can be done like this:

```
resolvers += Resolver.bintrayRepo("bitcoin-s", "bitcoin-s-core"),
```

Now you should be able to add bitcoin-s-core artifacts like this:

```
"org.bitcoins" % "bitcoin-s-secp256k1jni" % "0.0.1"

"org.bitcoins" %% "bitcoin-s-core" % "0.0.1" withSources() withJavadoc()

"org.bitcoins" %% "bitcoin-s-bitcoind-rpc" % "0.0.1" withSources() withJavadoc()

"org.bitcoins" %% "bitcoin-s-eclair-rpc" % "0.0.1" withSources() withJavadoc()

"org.bitcoins" %% "bitcoin-s-testkit" % "0.0.1" withSources() withJavadoc()

"org.bitcoins" %% "bitcoin-s-zmq" % "0.0.1" withSources() withJavadoc()
```
