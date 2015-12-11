package glint

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import glint.models.client.BigMatrix
import org.scalatest.{FlatSpec, Matchers}

/**
  * BigMatrix test specification
  */
class BigMatrixSpec extends FlatSpec with SystemTest with Matchers {

  "A BigMatrix" should "store Double values" in withMaster { _ =>
    withServer { _ =>
      withClient { client =>
        val model = whenReady(client.matrix[Double](49, 6)) {
          identity
        }
        val result = whenReady(model.push(Array(0L), Array(1), Array(0.54))) {
          identity
        }
        assert(result)
        val future = model.pull(Array(0L), Array(1))
        val value = whenReady(future) {
          identity
        }
        assert(value(0) == 0.54)
      }
    }
  }

  it should "store Float values" in withMaster { _ =>
    withServer { _ =>
      withClient { client =>
        val model = whenReady(client.matrix[Float](49, 6)) {
          identity
        }
        val result = whenReady(model.push(Array(10L, 0L, 48L), Array(0, 1, 5), Array(0.0f, 0.54f, 0.33333f))) {
          identity
        }
        assert(result)
        val future = model.pull(Array(10L, 0L, 48L), Array(0, 1, 5))
        val value = whenReady(future) {
          identity
        }
        value should equal(Array(0.0f, 0.54f, 0.33333f))
      }
    }
  }

  it should "store Int values" in withMaster { _ =>
    withServer { _ =>
      withClient { client =>
        val model = whenReady(client.matrix[Int](23, 10)) {
          identity
        }
        val result = whenReady(model.push(Array(1L, 5L, 20L), Array(0, 1, 8), Array(0, -1000, 23451234))) {
          identity
        }
        assert(result)
        val future = model.pull(Array(1L, 5L, 20L), Array(0, 1, 8))
        val value = whenReady(future) {
          identity
        }
        value should equal(Array(0, -1000, 23451234))
      }
    }
  }

  it should "store Long values" in withMaster { _ =>
    withServer { _ =>
      withClient { client =>
        val model = whenReady(client.matrix[Long](23, 10)) {
          identity
        }
        val result = whenReady(model.push(Array(1L, 5L, 20L), Array(0, 8, 1), Array(0L, -789300200100L, 987100200300L))) {
          identity
        }
        assert(result)
        val future = model.pull(Array(1L, 5L, 20L), Array(0, 8, 1))
        val value = whenReady(future) {
          identity
        }
        value should equal(Array(0L, -789300200100L, 987100200300L))
      }
    }
  }

  it should "aggregate values through addition" in withMaster { _ =>
    withServer { _ =>
      withClient { client =>
        val model = whenReady(client.matrix[Int](9, 100)) {
          identity
        }
        val result1 = whenReady(model.push(Array(0L, 2L, 5L, 8L), Array(0, 10, 99, 80), Array(100, 100, 20, 30))) {
          identity
        }
        val result2 = whenReady(model.push(Array(0L, 2L, 5L, 8L), Array(0, 10, 99, 80), Array(1, -1, 2, 3))) {
          identity
        }
        assert(result1)
        assert(result2)
        val future = model.pull(Array(0L, 2L, 5L, 8L), Array(0, 10, 99, 80))
        val value = whenReady(future) {
          identity
        }
        value should equal(Array(101, 99, 22, 33))
      }
    }
  }

  it should "deserialize without an ActorSystem in scope" in {
    var ab: Array[Byte] = Array.empty[Byte]
    withMaster { _ =>
      withServer { _ =>
        withClient { client =>
          val model = whenReady(client.matrix[Int](9, 10)) {
            identity
          }
          val bos = new ByteArrayOutputStream
          val out = new ObjectOutputStream(bos)
          out.writeObject(model)
          out.close()
          ab = bos.toByteArray

          val bis = new ByteArrayInputStream(ab)
          val in = new ObjectInputStream(bis)
          val matrix = in.readObject().asInstanceOf[BigMatrix[Int]]
          whenReady(matrix.push(Array(0L), Array(1), Array(12))) {
            identity
          }
          val result = whenReady(matrix.pull(Array(0L), Array(1))) {
            identity
          }
          result should equal(Array(12))
        }
      }
    }
  }

}
