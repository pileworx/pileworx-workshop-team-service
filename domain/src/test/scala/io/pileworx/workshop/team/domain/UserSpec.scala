package io.pileworx.workshop.team.domain

import io.pileworx.workshop.team.domain.command.{Confirmed, CreateUser, OperationResult}
import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.actor.testkit.typed.scaladsl.LogCapturing
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.Entity
import akka.cluster.typed.Cluster
import akka.cluster.typed.Join
import akka.persistence.typed.PersistenceId
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{Matchers, WordSpecLike}

object UserSpec {
  val config: Config = ConfigFactory.load("test.conf")
}

class UserSpec extends ScalaTestWithActorTestKit(UserSpec.config)
  with WordSpecLike
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