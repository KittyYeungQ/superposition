package superposition

import engine.core.Behavior
import engine.core.Behavior.Component
import extras.physics.PhysicsComponent

private class GameObject extends Component {
  Behavior.track(classOf[GameObject])

  val physics: PhysicsComponent = require(classOf[PhysicsComponent])

  var universe: Universe = _

  var copy: Universe => Unit = _

  var draw: () => Unit = _

  override protected def onCreate(): Unit = universe.add(this)
}