package superposition.math

import cats.ContravariantMonoidal
import cats.data.NonEmptyList
import cats.syntax.contravariant.toContravariantOps
import cats.syntax.functor.toFunctorOps
import superposition.math.QExpr.QExpr

import scala.Function.const
import scala.math.sqrt

/** A quantum gate.
  *
  * @tparam A the type of the gate's argument
  */
sealed trait Gate[A] {
  /** Applies the gate within a universe.
    *
    * @param value the value of the argument to the gate
    * @param universe the universe in which to apply the gate
    * @return the universes produced by the gate
    */
  def apply(value: A)(universe: Universe): NonEmptyList[Universe]

  /** The reverse of the gate. */
  def adjoint: Gate[A]
}

/** Gate operations and type classes. */
object Gate {

  /** An instance of the contravariant monoidal type class for gates. */
  implicit object GateCM extends ContravariantMonoidal[Gate] {
    override def contramap[A, B](gate: Gate[A])(f: B => A): Gate[B] = new Gate[B] {
      override def apply(value: B)(universe: Universe): NonEmptyList[Universe] = gate(f(value))(universe)

      override def adjoint: Gate[B] = contramap(gate.adjoint)(f)
    }

    override def product[A, B](gate1: Gate[A], gate2: Gate[B]): Gate[(A, B)] = new Gate[(A, B)] {
      override def apply(value: (A, B))(universe: Universe): NonEmptyList[Universe] =
        gate1(value._1)(universe) flatMap gate2(value._2)

      override def adjoint: Gate[(A, B)] = contramap(product(gate2.adjoint, gate1.adjoint))(_.swap)
    }

    override val unit: Gate[Unit] = Identity()
  }

  /** Operations on gates.
    *
    * @param gate the gate to apply the operations to
    */
  implicit final class Ops[A](val gate: Gate[A]) extends AnyVal {

    /** Applies the gate within all of the universes using the same argument.
      *
      * @param value the argument to the gate
      * @param universes the universes in which to apply the gate
      * @return the new universes
      */
    def applyToAll(value: A)(universes: Iterable[Universe]): Iterable[Universe] =
      universes flatMap (gate(value)(_).toList)

    /** Returns a new gate that applies this gate and then the other gate.
      *
      * @param other the gate to apply after this gate
      * @return the sequential composition of the two gates
      */
    def andThen(other: Gate[A]): Gate[A] =
      ContravariantMonoidal[Gate].product(gate, other) contramap (value => (value, value))

    /** A new gate that applies this gate to each argument in the sequence in order. */
    def multi: Gate[Seq[A]] = new Gate[Seq[A]] {
      override def apply(values: Seq[A])(universe: Universe): NonEmptyList[Universe] = values match {
        case Nil => NonEmptyList.one(universe)
        case x :: xs => gate(x)(universe) flatMap gate.multi(xs)
      }

      override def adjoint: Gate[Seq[A]] = gate.adjoint.multi contramap (_.reverse)
    }

    /** Returns a new gate that controls the argument to the original gate by mapping it based on its value and the
      * state of the universe.
      *
      * To preserve unitarity, the new gate must not change the state of any qudits used by the mapping function to map
      * the argument.
      *
      * @param f a mapping function that receives the gate argument and the universe
      * @tparam B the type of the new argument
      * @throws AssertionError if the mapping function violates unitarity
      * @return the controlled gate
      */
    def controlledMap[B](f: QExpr[B => A]): Gate[B] = new Gate[B] {
      override def apply(value: B)(universe: Universe): NonEmptyList[Universe] = {
        val newUniverses = gate(f(universe)(value))(universe)
        assert(newUniverses forall (f(_)(value) == f(universe)(value)))
        newUniverses
      }

      override def adjoint: Gate[B] = gate.adjoint controlledMap f
    }

    /** Returns a new gate that applies the original gate if the universe satisfies the predicate, and otherwise applies
      * the identity gate instead.
      *
      * @param predicate the predicate that must be satisfied to apply the original gate
      * @return the controlled gate
      */
    def controlled(predicate: QExpr[Boolean]): Gate[A] = /*_*/
      multi.controlledMap(predicate map (if (_) Seq(_) else const(Seq.empty))) /*_*/

    /** Returns a new gate that maps its argument to a sequence and applies the original gate with each value in the
      * sequence.
      *
      * @param f the mapping function that returns a sequence
      * @tparam B the type of the new argument
      * @return the mapped and flattened gate
      */
    def flatMap[B](f: B => Seq[A]): Gate[B] = multi contramap f

    /** Returns a new gate that applies the original gate if the argument satisfies the predicate, and otherwise applies
      * the identity gate instead.
      *
      * @param predicate the predicate that must be satisfied to apply the original gate
      * @return the filtered gate
      */
    def filter(predicate: A => Boolean): Gate[A] = flatMap(List(_) filter predicate)
  }

}

/** The identity gate leaves every universe unchanged.
  *
  * @tparam A the type of the gate's argument
  */
case class Identity[A]() extends Gate[A] {
  override def apply(value: A)(universe: Universe): NonEmptyList[Universe] = NonEmptyList.one(universe)

  override def adjoint: Gate[A] = this
}

/** The X or NOT gate swaps the |0⟩ and |1⟩ basis states of a qubit. */
case object X extends Gate[StateId[Boolean]] {
  override def apply(id: StateId[Boolean])(universe: Universe): NonEmptyList[Universe] =
    NonEmptyList.one(universe.updatedStateWith(id)(!_))

  override def adjoint: Gate[StateId[Boolean]] = this
}

/** The Z gate leaves the |0⟩ basis state of a qubit unchanged and maps |1⟩ to -|1⟩. */
case object Z extends Gate[StateId[Boolean]] {
  override def apply(id: StateId[Boolean])(universe: Universe): NonEmptyList[Universe] =
    NonEmptyList.one(universe * Complex(if (universe.state(id)) -1 else 1))

  override def adjoint: Gate[StateId[Boolean]] = this
}

/** The Hadamard gate maps the |0⟩ basis state to |+⟩ and |1⟩ to |-⟩. */
case object H extends Gate[StateId[Boolean]] {
  override def apply(id: StateId[Boolean])(universe: Universe): NonEmptyList[Universe] =
    NonEmptyList.of(
      universe / Complex((if (universe.state(id)) -1 else 1) * sqrt(2)),
      universe.updatedStateWith(id)(!_) / Complex(sqrt(2)))

  override def adjoint: Gate[StateId[Boolean]] = this
}

/** The translate gate applies vector addition to a qudit that represents a two-dimensional vector. */
case object Translate extends Gate[(StateId[Vector2[Int]], Vector2[Int])] {
  override def apply(value: (StateId[Vector2[Int]], Vector2[Int]))(universe: Universe): NonEmptyList[Universe] =
    value match {
      case (id, delta) => NonEmptyList.one(universe.updatedStateWith(id)(_ + delta))
    }

  override def adjoint: Gate[(StateId[Vector2[Int]], Vector2[Int])] =
    this contramap { case (id, delta) => (id, -delta) }
}
