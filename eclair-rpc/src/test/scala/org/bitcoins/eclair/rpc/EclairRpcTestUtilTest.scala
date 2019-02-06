package org.bitcoins.eclair.rpc
import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.bitcoins.core.util.BitcoinSLogger
import org.bitcoins.eclair.rpc.client.EclairRpcClient
import org.bitcoins.rpc.BitcoindRpcTestUtil
import org.scalatest.{AsyncFlatSpec, BeforeAndAfterAll}
import org.slf4j.LoggerFactory

import scala.concurrent.duration.DurationInt
import scala.concurrent.Await

class EclairRpcTestUtilTest extends AsyncFlatSpec with BeforeAndAfterAll {

  private val logger = LoggerFactory.getLogger(getClass)

  private implicit val actorSystem: ActorSystem =
    ActorSystem.create("EclairRpcTestUtilTest")

  private val bitcoindRpcF = BitcoindRpcTestUtil.startedBitcoindRpcClient()
  private val clients =
    Vector.newBuilder[EclairRpcClient]

  override def beforeAll: Unit = {
    //not async safe
    bitcoindRpcF.flatMap(_.generate(200))
  }

  override def afterAll: Unit = {
    clients.result().foreach(EclairRpcTestUtil.shutdown)
    TestKit.shutdownActorSystem(actorSystem)
  }

  behavior of "EclairRpcTestUtilTest"

  it must "spawn four nodes and create a channel link between them" in {
    val nodes4F = bitcoindRpcF.flatMap { bitcoindRpc =>
      val nodes = EclairRpcTestUtil.createNodeLink(bitcoindRpc)

      nodes.map { n4 =>
        clients ++= List(n4.c1, n4.c2, n4.c3, n4.c4)
      }

      nodes
    }

    nodes4F.flatMap { n4 =>
      val first = n4.c1
      val second = n4.c2
      val third = n4.c3
      val fourth = n4.c4

      for {
        nodeInfoFirst <- first.getInfo
        channelsFirst <- first.channels
        nodeInfoSecond <- second.getInfo
        channelsSecond <- second.channels
        nodeInfoThird <- third.getInfo
        channelsThird <- third.channels
        nodeInfoFourth <- fourth.getInfo
        channelsFourth <- fourth.channels
      } yield {
        assert(channelsFirst.length == 1)
        assert(channelsFirst.exists(_.nodeId == nodeInfoSecond.nodeId))

        assert(channelsSecond.length == 2)
        assert(channelsSecond.exists(_.nodeId == nodeInfoFirst.nodeId))
        assert(channelsSecond.exists(_.nodeId == nodeInfoThird.nodeId))

        assert(channelsThird.length == 2)
        assert(channelsThird.exists(_.nodeId == nodeInfoSecond.nodeId))
        assert(channelsThird.exists(_.nodeId == nodeInfoFourth.nodeId))

        assert(channelsFourth.length == 1)
        assert(channelsFourth.exists(_.nodeId == nodeInfoThird.nodeId))
      }
    }

  }
}
