[![Build Status](https://travis-ci.org/bitcoin-s/bitcoin-s-core.svg?branch=master)](https://travis-ci.org/bitcoin-s/bitcoin-s-core) [![Coverage Status](https://coveralls.io/repos/github/bitcoin-s/bitcoin-s-core/badge.svg?branch=master)](https://coveralls.io/github/bitcoin-s/bitcoin-s-core?branch=master) [![IRC Network](https://img.shields.io/badge/irc-%23bitcoin--scala-blue.svg "IRC Freenode")](https://webchat.freenode.net/?channels=bitcoin-scala)[![Gitter chat](https://badges.gitter.im/gitterHQ/gitter.png)](https://gitter.im/bitcoin-s-core)

# Bitcoin-S-Core

This is the core functionality of bitcoin-s. The goal is to provide basic data structures that are found in the bitcoin and lightning protocols while minimizing depedencies for security purposes. We aim to have an extremely high level of test coverage in this module to flesh out bugs. We use [property based testing](http://www.scalatest.org/user_guide/property_based_testing) heavily in this library to ensure high quality of code.

# The basics

Every bitcoin protocol data structure (and some other data structures) extends [`NetworkElement`](core/src/main/scala/org/bitcoins/core/protocol/NetworkElement.scala). NetworkElement provides easier methods to convert the data structure to hex or a byte representation. When paired with our [`Factory`](core/src/main/scala/org/bitcoins/core/util/Factory.scala) we can easily serialize and deserialize data structures. Most data structures have companion objects that extends `Factory` to be able to easily create protocol data structures. An example of this is the [`ScriptPubKey`](https://github.com/bitcoin-s/bitcoin-s-core/blob/1c7a7b9f46679a753248d9f55246c272bb3d63b9/src/main/scala/org/bitcoins/core/protocol/script/ScriptPubKey.scala#L462) companion object. You can use this companion object to create a SPK from hex or a byte array.


# Main modules in core

1. [protocol](core/src/main/scala/org/bitcoins/core/protocol) - basic protocol data structures. Useful for serializing/deserializing things
2. [script](core/src/main/scala/org/bitcoins/core/script) - an implementation of Script -- the programming language in bitcoin
3. [wallet](core/src/main/scala/org/bitcoins/core/wallet) - implements signing logic for bitcoin transaction's. This module is not named well as there is NO functionality to persist wallet state to disk as it stands. This will more than likely be renamed in the future.
4. [config](core/src/main/scala/org/bitcoins/core/config) - Contains information about a chain's genesis block and dns seeds
5. [number](core/src/main/scala/org/bitcoins/core/number) - Implements number types that are native in C, i.e. `UInt8`, `UInt32`, `UInt64`, etc.
6. [currency](core/src/main/scala/org/bitcoins/core/number) - Implements currency units in a cryptocurrency protocol
7. [bloom](core/src/main/scala/org/bitcoins/core/bloom) - Implements bloom filters and merkle blocks needed for BIP37

# Examples

## Serializing and deserializing a Transaction

Here is an example scala console session with bitcoins-core

```scala
$ sbt core/console
[info] Loading global plugins from /home/chris/.sbt/0.13/plugins
[info] Loading project definition from /home/chris/dev/bitcoin-s-core/project
[info] Set current project to bitcoin-s-core (in build file:/home/chris/dev/bitcoin-s-core/)
[info] Starting scala interpreter...
[info] 
Welcome to Scala version 2.11.7 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0_151).
Type in expressions to have them evaluated.
Type :help for more information.

scala> import org.bitcoins.core.protocol.transaction._
import org.bitcoins.core.protocol.transaction._

scala> val hexTx = "0100000001ccf318f0cbac588a680bbad075aebdda1f211c94ba28125b0f627f9248310db3000000006b4830450221008337ce3ce0c6ac0ab72509f8$9c1d52701817a2362d6357457b63e3bdedc0c0602202908963b9cf1a095ab3b34b95ce2bc0d67fb0f19be1cc5f7b3de0b3a325629bf01210241d746ca08da0a668735c3e01c1$a02045f2f399c5937079b6434b5a31dfe353ffffffff0210335d05000000001976a914b1d7591b69e9def0feb13254bace942923c7922d88ac48030000000000001976a9145e$90c865c2f6f7a9710a474154ab1423abb5b9288ac00000000"
hexTx: String = 0100000001ccf318f0cbac588a680bbad075aebdda1f211c94ba28125b0f627f9248310db3000000006b4830450221008337ce3ce0c6ac0ab72509f889c1$52701817a2362d6357457b63e3bdedc0c0602202908963b9cf1a095ab3b34b95ce2bc0d67fb0f19be1cc5f7b3de0b3a325629bf01210241d746ca08da0a668735c3e01c1fa02$45f2f399c5937079b6434b5a31dfe353ffffffff0210335d05000000001976a914b1d7591b69e9def0feb13254bace942923c7922d88ac48030000000000001976a9145e690c$65c2f6f7a9710a474154ab1423abb5b9288ac00000000

scala> val tx = Transaction.fromHex(hexTx)
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
tx: org.bitcoins.core.protocol.transaction.Transaction = BaseTransactionImpl(UInt32Impl(1),List(TransactionInputImpl(TransactionOutPointImpl$DoubleSha256DigestImpl(ccf318f0cbac588a680bbad075aebdda1f211c94ba28125b0f627f9248310db3),UInt32Impl(0)),P2PKHScriptSignatureImpl(6b483045022$008337ce3ce0c6ac0ab72509f889c1d52701817a2362d6357457b63e3bdedc0c0602202908963b9cf1a095ab3b34b95ce2bc0d67fb0f19be1cc5f7b3de0b3a325629bf012102$1d746ca08da0a668735c3e01c1fa02045f2f399c5937079b6434b5a31dfe353),UInt32Impl(4294967295))),List(TransactionOutputImpl(SatoshisImpl(Int64Impl($9994000)),P2PKHScriptPubKeyImpl(1976a914b1d7591b69e9def0feb13254bace942923c7922d88ac)), TransactionOutputImpl(SatoshisImpl(Int64Impl(840)),P$PKHScriptPubKeyImpl(1976a9145e690c865c2f6f7a9710a474154ab1423abb5b9288ac))),UInt32Impl(0))

scala> val hexAgain = tx.hex
hexAgain: String = 0100000001ccf318f0cbac588a680bbad075aebdda1f211c94ba28125b0f627f9248310db3000000006b4830450221008337ce3ce0c6ac0ab72509f88$c1d52701817a2362d6357457b63e3bdedc0c0602202908963b9cf1a095ab3b34b95ce2bc0d67fb0f19be1cc5f7b3de0b3a325629bf01210241d746ca08da0a668735c3e01c1f$02045f2f399c5937079b6434b5a31dfe353ffffffff0210335d05000000001976a914b1d7591b69e9def0feb13254bace942923c7922d88ac48030000000000001976a9145e6$0c865c2f6f7a9710a474154ab1423abb5b9288ac00000000

```

This gives us an example of a bitcoin transaction that is encoded in hex format that is deserialized to a native Scala object called a [`Transaction`](https://github.com/bitcoin-s/bitcoin-s-core/blob/6358eb83067909771f989d615b422759222d060a/src/main/scala/org/bitcoins/core/protocol/transaction/Transaction.scala#L14-L42). You could also serialize the transaction to bytes using `tx.bytes` instead of `tx.hex`. These methods are available on every data structure that extends NetworkElement, like [`ECPrivateKey`](https://github.com/bitcoin-s/bitcoin-s-core/blob/6358eb83067909771f989d615b422759222d060a/src/main/scala/org/bitcoins/core/crypto/ECKey.scala#L23-L67), [`ScriptPubKey`](https://github.com/bitcoin-s/bitcoin-s-core/blob/6358eb83067909771f989d615b422759222d060a/src/main/scala/org/bitcoins/core/protocol/script/ScriptPubKey.scala#L23), [`ScriptWitness`](https://github.com/bitcoin-s/bitcoin-s-core/blob/6358eb83067909771f989d615b422759222d060a/src/main/scala/org/bitcoins/core/protocol/script/ScriptWitness.scala#L13), and [`Block`](https://github.com/bitcoin-s/bitcoin-s-core/blob/6358eb83067909771f989d615b422759222d060a/src/main/scala/org/bitcoins/core/protocol/blockchain/Block.scala#L17). 


## Building a signed transaction

Core supports building unsigned transactions and then signing them with a set of private keys. The first important thing to look at is [UTXOSpendingInfo](core/src/main/scala/org/bitcoins/core/wallet/utxo/UTXOSpendingInfo.scala). This contains all of the information needed to create a validly signed [ScriptSignature](core/src/main/scala/org/bitcoins/core/protocol/script/ScriptSignature.scala) that spends this output. 

Our `TxBuilder` class requires you to provide the following

1. destinations - the places we are sending bitcoin to. These are [TransactionOutputs](core/src/main/scala/org/bitcoins/core/protocol/transaction/TransactionOutput.scala) you are sending coins too
2. utxos - these are the [utxos](core/src/main/scala/org/bitcoins/core/wallet/utxo/UTXOSpendingInfo.scala) used to fund your transaction. These must exist in your wallet, and you must know how to spend them (i.e. have the private key)
3. feeRate - the fee rate you want to pay for this transaction
4. changeSPK - where the change from the transaction will be sent. Any extra money after the `creditingAmount` - `destinationAmount` - `fee` 
will be sent here 
5. network - the [Network](core/src/main/scala/org/bitcoins/core/config/NetworkParameters.scala) we are transacting on

After providing this information, you can generate a validly signed bitcoin transaction by calling the `sign` method.

### The [sign api](core/src/main/scala/org/bitcoins/core/crypto/Sign.scala)

This is the API we define to sign things with. It takes in an arbitrary byte vector and returns a `Future[ECDigitalSignature]`. The reason we incorporate Futures here is for extensibility of this API. We would like to provide implementations of this API for hardware devices, which need to be asynchrnous since they may require user input. 

```scala
trait Sign {
  def signFunction: ByteVector => Future[ECDigitalSignature]

  def signFuture(bytes: ByteVector): Future[ECDigitalSignature] =
    signFunction(bytes)

  def sign(bytes: ByteVector): ECDigitalSignature = {
    Await.result(signFuture(bytes), 30.seconds)
  }

  def publicKey: ECPublicKey
}

```

The `ByteVector` that is input is input to the `signFunction` should be the hash that is output from [TransactionSignatureSerializer](core/src/main/scala/org/bitcoins/core/crypto/TransactionSignatureSerializer.scala)'s `hashForSignature` method. Our in memory [ECKey](core/src/main/scala/org/bitcoins/core/crypto/ECKey.scala) types implement the `Sign` api.

If you wanted to implement a new `Sign` api for a hardware wallet, you can easily pass it into the `TxBuilder`/`Signer` classes to allow for you to use those devices to sign with bitcoin-s.

This API is currently used to sign ordinary transactions with our [Signer](core/src/main/scala/org/bitcoins/core/wallet/signer/Signer.scala)s. The `Signer` subtypes (i.e. `P2PKHSigner`) implement the specific functionality needed to produce a valid digital signature for their corresponding script type. 

### Example

For a example of how to use the `TxBuilder` please see [this scala file](doc/src/test/scala/TxBuilderExample.scala)

## Verifying a transaction's script is valid (does not check if utxo is valid)

Transactions are run through the interpreter to check the validity of the Transaction. These are packaged up into an object called ScriptProgram, which contains the following:
  - The transaction that is being checked
  - The specific input index that it is checking
  - The scriptPubKey for the crediting transaction
  - The flags used to verify the script

Here is an example of a transaction spending a scriptPubKey which is correctly evaluated with our interpreter implementation:

```scala
chris@chris:~/dev/bitcoins-core$ sbt core/console

scala> import org.bitcoins.core.protocol.script._
import org.bitcoins.core.protocol.script._

scala> import org.bitcoins.core.protocol.transaction._
import org.bitcoins.core.protocol.transaction._

scala> import org.bitcoins.core.script._
import org.bitcoins.core.script._

scala> import org.bitcoins.core.script.interpreter._
import org.bitcoins.core.script.interpreter._

scala> import org.bitcoins.core.policy._
import org.bitcoins.core.policy._

scala> import org.bitcoins.core.number._
import org.bitcoins.core.number._

scala> import org.bitcoins.core.crypto._
import org.bitcoins.core.crypto._

scala> import org.bitcoins.core.currency._
import org.bitcoins.core.currency._


scala> val spendingTx = Transaction.fromHex("0100000001ccf318f0cbac588a680bbad075aebdda1f211c94ba28125b0f627f9248310db3000000006b4830450221008337ce3ce0c6ac0ab72509f889c1d52701817a2362d6357457b63e3bdedc0c0602202908963b9cf1a095ab3b34b95ce2bc0d67fb0f19be1cc5f7b3de0b3a325629bf01210241d746ca08da0a668735c3e01c1fa02045f2f399c5937079b6434b5a31dfe353ffffffff0210335d05000000001976a914b1d7591b69e9def0feb13254bace942923c7922d88ac48030000000000001976a9145e690c865c2f6f7a9710a474154ab1423abb5b9288ac00000000")
spendingTx: org.bitcoins.core.protocol.transaction.Transaction = BaseTransactionImpl(Int32Impl(1),Vector(TransactionInputImpl(TransactionOutPointImpl(DoubleSha256DigestImpl(ccf318f0cbac588a680bbad075aebdda1f211c94ba28125b0f627f9248310db3),UInt32Impl(0)),P2PKHScriptSignature(6b4830450221008337ce3ce0c6ac0ab72509f889c1d52701817a2362d6357457b63e3bdedc0c0602202908963b9cf1a095ab3b34b95ce2bc0d67fb0f19be1cc5f7b3de0b3a325629bf01210241d746ca08da0a668735c3e01c1fa02045f2f399c5937079b6434b5a31dfe353),UInt32Impl(4294967295))),Vector(TransactionOutputImpl(SatoshisImpl(Int64Impl(89994000)),P2PKHScriptPubKeyImpl(1976a914b1d7591b69e9def0feb13254bace942923c7922d88ac)), TransactionOutputImpl(SatoshisImpl(Int64Impl(840)),P2PKHScriptPubKeyImpl(1976a9145e690c865c2f6f7a9710a474154ab14...

scala> val scriptPubKey = ScriptPubKey.fromAsmHex("76a91431a420903c05a0a7de2de40c9f02ebedbacdc17288ac")
scriptPubKey: org.bitcoins.core.protocol.script.ScriptPubKey = P2PKHScriptPubKeyImpl(1976a91431a420903c05a0a7de2de40c9f02ebedbacdc17288ac)

scala> val output = TransactionOutput(CurrencyUnits.zero, scriptPubKey)
output: org.bitcoins.core.protocol.transaction.TransactionOutput = TransactionOutputImpl(SatoshisImpl(Int64Impl(0)),P2PKHScriptPubKeyImpl(1976a91431a420903c05a0a7de2de40c9f02ebedbacdc17288ac))

scala> val inputIndex = UInt32.zero
inputIndex: org.bitcoins.core.number.UInt32 = UInt32Impl(0)

scala> val btxsc = BaseTxSigComponent(spendingTx,inputIndex,output,Policy.standardScriptVerifyFlags)
btxsc: org.bitcoins.core.crypto.BaseTxSigComponent = BaseTxSigComponentImpl(BaseTransactionImpl(Int32Impl(1),Vector(TransactionInputImpl(TransactionOutPointImpl(DoubleSha256DigestImpl(ccf318f0cbac588a680bbad075aebdda1f211c94ba28125b0f627f9248310db3),UInt32Impl(0)),P2PKHScriptSignature(6b4830450221008337ce3ce0c6ac0ab72509f889c1d52701817a2362d6357457b63e3bdedc0c0602202908963b9cf1a095ab3b34b95ce2bc0d67fb0f19be1cc5f7b3de0b3a325629bf01210241d746ca08da0a668735c3e01c1fa02045f2f399c5937079b6434b5a31dfe353),UInt32Impl(4294967295))),Vector(TransactionOutputImpl(SatoshisImpl(Int64Impl(89994000)),P2PKHScriptPubKeyImpl(1976a914b1d7591b69e9def0feb13254bace942923c7922d88ac)), TransactionOutputImpl(SatoshisImpl(Int64Impl(840)),P2PKHScriptPubKeyImpl(1976a9145e690c865c2f6f7a9710...

scala> val preExecution = PreExecutionScriptProgram(btxsc)
preExecution: org.bitcoins.core.script.PreExecutionScriptProgram = PreExecutionScriptProgramImpl(BaseTxSigComponentImpl(BaseTransactionImpl(Int32Impl(1),Vector(TransactionInputImpl(TransactionOutPointImpl(DoubleSha256DigestImpl(ccf318f0cbac588a680bbad075aebdda1f211c94ba28125b0f627f9248310db3),UInt32Impl(0)),P2PKHScriptSignature(6b4830450221008337ce3ce0c6ac0ab72509f889c1d52701817a2362d6357457b63e3bdedc0c0602202908963b9cf1a095ab3b34b95ce2bc0d67fb0f19be1cc5f7b3de0b3a325629bf01210241d746ca08da0a668735c3e01c1fa02045f2f399c5937079b6434b5a31dfe353),UInt32Impl(4294967295))),Vector(TransactionOutputImpl(SatoshisImpl(Int64Impl(89994000)),P2PKHScriptPubKeyImpl(1976a914b1d7591b69e9def0feb13254bace942923c7922d88ac)), TransactionOutputImpl(SatoshisImpl(Int64Impl(840)),P2PKHS...

scala> val result = ScriptInterpreter.run(preExecution)
result: org.bitcoins.core.script.result.ScriptResult = ScriptOk
```

## Running core tests

To run the entire test suite all you need to do is run the following command
```scala 

chris@chris:~/dev/bitcoins-core$ sbt coreTest/test
[info] Elapsed time: 16.009 sec 
[info] ScalaCheck
[info] Passed: Total 173, Failed 0, Errors 0, Passed 173
[info] ScalaTest
[info] Run completed in 36 seconds, 607 milliseconds.
[info] Total number of tests run: 835
[info] Suites: completed 110, aborted 0
[info] Tests: succeeded 835, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
[info] Passed: Total 1008, Failed 0, Errors 0, Passed 1008
[success] Total time: 38 s, completed Dec 23, 2018 4:39:58 PM
```
