package io.pileworx.workshop.team.domain

import io.pileworx.workshop.team.domain.command.{Confirmed, CreateUser, OperationResult}
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.testkit.typed.scaladsl.LogCapturing
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.Entity
import akka.cluster.typed.Cluster
import akka.cluster.typed.Join
import akka.persistence.typed.PersistenceId
import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.should.Matchers

object UserSpec {
  val config = ConfigFactory.parseString("""
      akka.actor.provider = cluster
      akka.remote.classic.netty.tcp.port = 0
      akka.remote.artery.canonical.port = 0
      akka.remote.artery.canonical.hostname = 127.0.0.1

      akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
      akka.persistence.journal.inmem.test-serialization = on
      """)
}

class UserSpec extends ScalaTestWithActorTestKit(UserSpec.config)
  with AnyWordSpecLike
  with Matchers
  with LogCapturing {

  private val sharding = ClusterSharding(system)

  override def beforeAll(): Unit = {
    super.beforeAll()
    Cluster(system).manager ! Join(Cluster(system).selfMember.address)

    sharding.init(Entity(User.TypeKey) { entityContext =>
      User(PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId))
    })
  }

  "User" must {

    val username = "testuser"
    val email = "testuser@pileworx.io"
    val firstName = "Test"
    val middleName = None
    val lastName = "User"

    "create User" in {
      val probe = createTestProbe[OperationResult]()
      val ref = ClusterSharding(system).entityRefFor(User.TypeKey, username)
      ref ! CreateUser(username, email, firstName, middleName, lastName, probe.ref)
      probe.expectMessage(Confirmed)
    }
  }
}