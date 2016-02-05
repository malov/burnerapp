package com.matruss.burner

import akka.testkit.{ImplicitSender, TestKit}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

@RunWith(classOf[JUnitRunner])
class VoteProcessingActorSpec extends TestKit( MockActorSystem("VoteProcessingActor") ) with ImplicitSender with WordSpecLike with BeforeAndAfterAll{
  
  override def afterAll(): Unit = TestKit.shutdownActorSystem(system)

}
