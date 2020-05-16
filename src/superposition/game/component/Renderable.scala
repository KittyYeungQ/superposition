package superposition.game.component

import com.badlogic.ashley.core.{Component, ComponentMapper}
import superposition.quantum.Universe

/** The renderable component allows an entity to be rendered on the screen.
  *
  * @param layer the layer to render in
  * @param dependentState the quantum state that the renderers depend on
  */
final class Renderable(val layer: Int, val dependentState: Universe => Any) extends Component

/** Contains the component mapper for the renderable component. */
object Renderable {
  /** The component mapper for the renderable component. */
  val Mapper: ComponentMapper[Renderable] = ComponentMapper.getFor(classOf[Renderable])
}