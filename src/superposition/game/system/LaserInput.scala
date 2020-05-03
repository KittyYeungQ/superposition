package superposition.game.system

import com.badlogic.ashley.core.{ComponentMapper, Entity, Family}
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx.input
import superposition.game.component.{Beam, Multiverse, Quantum}
import superposition.game.system.LaserInput._
import superposition.math.Vector2i
import superposition.quantum.{StateId, Universe}

import scala.Function.const

final class LaserInput extends IteratingSystem(Family.all(classOf[Beam], classOf[Quantum]).get) {
  override def processEntity(entity: Entity, deltaTime: Float): Unit = {
    val multiverse = QuantumMapper.get(entity).multiverse
    val beam = BeamMapper.get(entity)
    if (input.isButtonJustPressed(0) && multiverse.selected(beam.source)) {
      multiverse.applyGate(beam.gate.multi controlled const(universe => hits(multiverse, universe, beam)), ())
      multiverse.updateMetaWith(beam.lastTarget)(const(universe => target(multiverse, universe, beam)))
      multiverse.updateMetaWith(beam.elapsedTime) { time => universe =>
        if (target(multiverse, universe, beam).isEmpty) time else 0
      }
    }
    multiverse.updateMetaWith(beam.elapsedTime)(time => const(time + deltaTime))
  }
}

private object LaserInput {
  private val BeamMapper: ComponentMapper[Beam] = ComponentMapper.getFor(classOf[Beam])

  private val QuantumMapper: ComponentMapper[Quantum] = ComponentMapper.getFor(classOf[Quantum])

  private def target(multiverse: Multiverse, universe: Universe, beam: Beam): Option[Vector2i] =
    if (multiverse.allOn(universe, beam.controls))
      beam.path.take(Beam.Length) find { cell =>
        multiverse.isBlocked(universe, cell) || multiverse.allInCell(universe, cell).nonEmpty
      }
    else None

  private def hits(multiverse: Multiverse, universe: Universe, beam: Beam): Seq[StateId[Boolean]] =
    target(multiverse, universe, beam).iterator.to(Seq) flatMap (cell => multiverse.primaryBits(universe, cell))
}
