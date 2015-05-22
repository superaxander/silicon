/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package viper
package silicon
package tests

import org.scalatest.FunSuite
import state.terms._
import state.terms.implicits._

class TriggerGeneratorTests extends FunSuite {
  test("Work in simple cases") {
    val i = Var("i", sorts.Int)
    val s = Var("S", sorts.Seq(sorts.Int))
    val t = SeqAt(s, i)

    assert(TriggerGenerator.generateTriggerGroups(i :: Nil, t) match {
      case Seq((Seq(Trigger(Seq(`t`))), Seq())) => true
      case other => false
    })
  }

  test("Fail in these cases") {
    val i = Var("i", sorts.Int)
    val s = Var("S", sorts.Seq(sorts.Int))
    val t = SeqAt(s, Plus(i, 1))

    assert(TriggerGenerator.generateTriggerGroups(i :: Nil, t).isEmpty)
  }

//  ignore should "not do stupid stuff" in {
//    val x = Var("x", sorts.Int)
//    val y = Var("y", sorts.Int)
//    val xs = Var("xs", sorts.Seq(sorts.Int))
//
//    val lhs =
//      And(
//        SeqIn(SeqRanged(0, SeqLength(xs)), x),
//        SeqIn(SeqRanged(0, SeqLength(xs)), y),
//        SeqAt(xs, x) === SeqAt(xs, y))
//
//    val rhs = x === y
//
////    val forall = Forall(x :: y :: Nil, Implies(lhs, rhs), Seq[Trigger]())
////    println(forall)
//
//    TriggerGenerator.generateTriggerGroups(x :: y :: Nil, Implies(lhs, rhs)) foreach { case (triggers, vars) =>
//      println(s"\ntriggers = ${triggers.mkString("\n  ", "\n  ", "")}")
//      println(s"\nvars = $vars")
//    }
//
//    TriggerGenerator.generateStrictestTrigger(x :: y :: Nil, Implies(lhs, rhs)) foreach { case (triggers, vars) =>
//      println(s"\ntriggers = $triggers")
//      println(s"\nvars = $vars")
//    }
//  }
}
