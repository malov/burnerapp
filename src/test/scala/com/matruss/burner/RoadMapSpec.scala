package com.matruss.burner

import akka.actor.Props
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.HttpEntity

import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import org.junit.runner.RunWith
import org.matruss.burner.RoadMap
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class RoadMapSpec extends WordSpecLike with Matchers with ScalatestRouteTest with BeforeAndAfterAll {
  
  override def beforeAll(): Unit = {
    system.actorOf( Props(classOf[MockVoterActor]), "voter" )
  }
  override def afterAll():Unit = { system.shutdown() }

  class TestListener extends RoadMap  {

    implicit def ec = system.dispatcher
    implicit def timeout = Timeout(10.seconds)

    protected def voter = system.actorSelection("/user/voter")
  }

  private var tr = new TestListener

  "Road Map" should {
    "return OK for proper voting request" in {
      val vaid = """{"type": "inboundText", "payload": "Hello", "fromNumber": "+12222222222", "toNumber": "+13333333333" }"""
      val entity = HttpEntity(`application/json`, vaid)
      Post("/burner/event",entity) ~> tr.roads() ~> check { status shouldEqual OK}
    }
    "return list of picture for a proper request to fetch picture" in {
      import RoadMap._
      Get("/burner/report") ~> tr.roads() ~> check {
        status shouldEqual OK
        response.entity shouldEqual buildResponse(Fixture.pseq).entity
      }
    }
    "reject request for invalid endpoint" in {
      Get("/burner/whatever") ~> tr.roads() ~> check { handled shouldEqual false }
      Post("/whatever/burener") ~> tr.roads() ~> check { handled shouldEqual false }
    }
    "reject voting request for improper payload" in {
      val invalid = """{"type": "inboundText", "message": "Hello", "fromNumber": "+12222222222", "toNumber": "+13333333333" }"""
      val entity = HttpEntity(`application/json`, invalid)
      Post("/burner/event",entity) ~> tr.roads() ~> check {
        rejection.getClass.getSimpleName shouldEqual "MalformedRequestContentRejection"
      }
    }
    "return error if voter actor returns error" in {}
    Get("/burner/report") ~> tr.roads(true) ~> check { handled shouldEqual false }
    Post("/burner/event") ~> tr.roads(true) ~> check { handled shouldEqual false }
  }
}
