package com.matruss.burner

import akka.actor.Props
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

@RunWith(classOf[JUnitRunner])
class RoadMapSpec extends WordSpecLike with Matchers with ScalatestRouteTest with BeforeAndAfterAll {
  
  override def beforeAll(): Unit = {
    system.actorOf( Props(classOf[MockVoterActor]), "voter" )
  }
  override def afterAll():Unit = { system.shutdown() }

}
