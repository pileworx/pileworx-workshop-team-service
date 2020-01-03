package io.pileworx.workshop.team.domain

import io.pileworx.workshop.team.domain.command.{Confirmed, CreateUser, OperationResult, UpdateUser}
import akka.actor.testkit.typed.scaladsl.{LogCapturing, ScalaTestWithActorTestKit}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity}
import akka.cluster.typed.{Cluster, Join}
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
      User(entityContext.entityId, Set.empty)
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

    "update User" in {
      val probe = createTestProbe[OperationResult]()
      val ref = ClusterSharding(system).entityRefFor(User.TypeKey, username)
      ref ! UpdateUser(email, firstName, middleName, lastName, probe.ref)
      probe.expectMessage(Confirmed)
    }

    "fail update of User if no User exists" in {
      val invalidUser = "invalid"
      val probe = createTestProbe[OperationResult]()
      val ref = ClusterSharding(system).entityRefFor(User.TypeKey, invalidUser)
      ref ! UpdateUser(email, firstName, middleName, lastName, probe.ref)
      probe.expectNoMessage()
    }
  }
}