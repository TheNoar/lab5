import org.scalatest._
import jsy.lab5.ast._
import Lab5._

class Lab5Spec extends FlatSpec {
  
  "mapFirstDoWith" should "map the first element where f returns Some" in {
     val l1 = List(1, 2, -3, 4, -5)
     val gold1 = List(1, 2, 3, 4, -5)
     def dowith[W]: DoWith[W,List[Int]] = mapFirstWith[W,Int] { (i: Int) => if (i < 0) Some(doreturn(-i)) else None } (l1)
     assertResult((true,gold1)) { dowith(true) }
     assertResult((42,gold1)) { dowith(42) }
  }

  // Probably want to write some tests for castOk, typeInfer, substitute, and step.
  
  "DoNeg" should "negate values" in {
    val e = Unary(Neg, N(42))
    val (mp:Mem, ep: Expr) = step(e)(Mem.empty)
    assert(mp.isEmpty)
    assertResult(N(-42)) { ep }
  }
  
  "DoArith" should "sum numbers" in {
    val e = Binary(Plus, N(2), N(3))
    val (mp:Mem, ep: Expr) = step(e)(Mem.empty)
    assert(mp.isEmpty)
    assertResult(N(5)) { ep }
  }
  
  "DoInequalityString" should "compare strings with GE" in {
    val e = Binary(Ge, S("abc"), S("xyz"))
    val (mp:Mem, ep: Expr) = step(e)(Mem.empty)
    assert(mp.isEmpty)
    assertResult(B(false)) { ep }
  }
  
  "DoPlusString" should "concat strings" in {
    val e = Binary(Plus, S("abc"), S("xyz"))
    val (mp:Mem, ep: Expr) = step(e)(Mem.empty)
    assert(mp.isEmpty)
    assertResult(S("abcxyz")) { ep }
  }

  "DoSeq" should "produce second element in sequence" in {
    val e = Binary(Seq, N(1), Binary(Plus, N(2), N(3)))
    val (mp:Mem, ep: Expr) = step(e)(Mem.empty)
    assert(mp.isEmpty)
    assertResult(Binary(Plus, N(2), N(3))) { ep }
  }
  
  "DoAndTrue" should "not shortcircuiting boolean and" in {
    val e = Binary(And, B(true), Decl(MVar, "x", B(true), Var("x")))
    val (mp:Mem, ep: Expr) = step(e)(Mem.empty)
    assert(mp.isEmpty)
    assertResult(Decl(MVar, "x", B(true), Var("x"))) { ep }
  }
  
  "DoObject" should "instantiate an object in memory" in {
    val e = Obj(Map("a" -> N(42), "b" -> N(47)))
    val (mp:Mem, a: A) = step(e)(Mem.empty)
    assertResult(e) { mp.get(a).get }
  }
  
  "DoGetField" should "access a field from an object in memory" in {
    val setup = Obj(Map("a" -> N(42), "b" -> N(47)))
    val (m:Mem, addr: A) = step(setup)(Mem.empty)
    val e = GetField(addr, "b")
    val (mp, ep: Expr) = step(e)(m)
    assert(m == mp)
    assertResult(N(47)) { ep }
  }

  "DoVar" should "declare a variable" in {
    val e = Decl(MVar, "x", N(42), Var("x"))
    val (mp: Mem, ep: Expr) = step(e)(Mem.empty)
    assert(ep match {
      case Unary(Deref, a@A(_)) =>
        // Verify memory correctly references N(42)
        mp.get(a).get == N(42)
      case _ => false
    })
  }

  "SearchCall1" should "step its function" in {
    // (true ? function (n: number) { return n } : null)(42)
    val e = Call(If(B(true),Function(None,Left(List(("n",TNumber))),None,Var("n")),Null),List(N(42)))
    val (_, ep: Expr) = step(e)(Mem.empty)
    assertResult(
      Call(Function(None, Left(List(("n", TNumber))), None, Var("n")), List(N(42)))) {
      ep
    }
  }

}
