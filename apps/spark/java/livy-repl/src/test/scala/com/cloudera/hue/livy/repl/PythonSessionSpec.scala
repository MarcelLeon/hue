package com.cloudera.hue.livy.repl

import com.cloudera.hue.livy.repl.python.PythonSession
import org.json4s.{DefaultFormats, Extraction}
import org.scalatest._
import org.scalatest.matchers.ShouldMatchers

import _root_.scala.concurrent.Await
import _root_.scala.concurrent.duration.Duration

class PythonSessionSpec extends FunSpec with ShouldMatchers with BeforeAndAfter {

  implicit val formats = DefaultFormats

  var session: Session = null

  before {
    session = PythonSession.createPython()
  }

  after {
    session.close()
  }

  describe("A python session") {
    it("should start in the starting or idle state") {
      session.state should (equal (Session.Starting()) or equal (Session.Idle()))
    }

    it("should eventually become the idle state") {
      session.waitForStateChange(Session.Starting())
      session.state should equal (Session.Idle())
    }

    it("should execute `1 + 2` == 3") {
      val result = Await.result(session.execute("1 + 2"), Duration.Inf)
      val expectedResult = Extraction.decompose(Map(
        "status" -> "ok",
        "execution_count" -> 0,
        "data" -> Map(
          "text/plain" -> "3"
        )
      ))

      result should equal (expectedResult)
    }

    it("should execute `x = 1`, then `y = 2`, then `x + y`") {
      var result = Await.result(session.execute("x = 1"), Duration.Inf)
      var expectedResult = Extraction.decompose(Map(
        "status" -> "ok",
        "execution_count" -> 0,
        "data" -> Map(
          "text/plain" -> ""
        )
      ))

      result should equal (expectedResult)

      result = Await.result(session.execute("y = 2"), Duration.Inf)
      expectedResult = Extraction.decompose(Map(
        "status" -> "ok",
        "execution_count" -> 1,
        "data" -> Map(
          "text/plain" -> ""
        )
      ))

      result should equal (expectedResult)

      result = Await.result(session.execute("x + y"), Duration.Inf)
      expectedResult = Extraction.decompose(Map(
        "status" -> "ok",
        "execution_count" -> 2,
        "data" -> Map(
          "text/plain" -> "3"
        )
      ))

      result should equal (expectedResult)
    }

    it("should do table magic") {
      val result = Await.result(session.execute("x = [[1, 'a'], [3, 'b']]\n%table x"), Duration.Inf)
      val expectedResult = Extraction.decompose(Map(
        "status" -> "ok",
        "execution_count" -> 1,
        "data" -> Map(
          "application/vnd.livy.table.v1+json" -> Map(
            "headers" -> List(
              Map("type" -> "INT_TYPE", "name" -> "0"),
              Map("type" -> "STRING_TYPE", "name" -> "1")),
            "data" -> List(List(1, "a"), List(3, "b"))
          )
        )
      ))

      result should equal (expectedResult)
    }
  }
}
