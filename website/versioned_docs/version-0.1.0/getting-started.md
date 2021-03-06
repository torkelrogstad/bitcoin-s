---
id: version-0.1.0-getting-started
title: Add Bitcoin-S to your project
original_id: getting-started
---

## REPL

You can try out Bitcoin-S in a REPL in a matter of seconds. Run the provided
["try bitcoin-s"](https://github.com/bitcoin-s/bitcoin-s-core/blob/master/try-bitcoin-s.sh)
script, which has no dependencies other than an installed JDK. The script
downloads and installs [Coursier](https://get-coursier.io/) and uses it to
fetch the [Ammonite](https://ammonite.io) REPL and the latest version of
Bitcoin-S. It then drops you into immediately into a REPL session.

```bash
$ curl -s https://raw.githubusercontent.com/bitcoin-s/bitcoin-s/master/try-bitcoin-s.sh | bash
Loading...
Welcome the Bitcoin-S REPL, powered by Ammonite
Check out our documentation and examples at
https://bitcoin-s.org/docs/getting-started
@ val priv = ECPrivateKey()
@ val pub = priv.publicKey
@ val spk = P2WPKHWitnessSPKV0(pub)
@ val address = Bech32Address(spk, MainNet)
@ address.value # Tada! You've just made a Bech32 address
res4: String = "bc1q7ynsz7tamtnvlmts4snrl7e98jc9d8gqwsjsr5"
```

## Build tools

If you want to add Bitcoin-S to your project, follow the
instructions for your build tool

### sbt

Add this to your `build.sbt`:

```scala
libraryDependencies +="org.bitcoin-s" % "bitcoin-s-secp256k1jni" % "0.1.0"

libraryDependencies += "org.bitcoin-s" %% "bitcoin-s-core" % "0.1.0"

libraryDependencies += "org.bitcoin-s" %% "bitcoin-s-bitcoind-rpc" % "0.1.0"

libraryDependencies += "org.bitcoin-s" %% "bitcoin-s-eclair-rpc" % "0.1.0"

libraryDependencies += "org.bitcoin-s" %% "bitcoin-s-testkit" % "0.1.0"

libraryDependencies += "org.bitcoin-s" %% "bitcoin-s-zmq" % "0.1.0"
```


### Nightly builds

You can also run on the bleeding edge of Bitcoin-S, by
adding a snapshot build to your `build.sbt`. The most
recent snapshot published is `0.1.0+45-91633375+20190620-1322-SNAPSHOT`.

To fetch snapshots, you will need to add the correct
resolver in your `build.sbt`:

```sbt
resolvers += Resolver.sonatypeRepo("snapshots")
```


### Mill

TODO
