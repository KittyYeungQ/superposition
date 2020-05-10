package superposition.game.entity

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Texture
import superposition.game.ResourceResolver.resolve
import superposition.game.component.{ClassicalPosition, Goal, Multiverse, SpriteView}
import superposition.game.entity.Exit.KeyTexture
import superposition.math.Vector2
import superposition.quantum.StateId

import scala.Function.const

/** An exit allows the player to leave a level.
  *
  * @param multiverse the multiverse
  * @param cell the cell position of the exit
  * @param playerCell the cell position of the player
  */
final class Exit(
    multiverse: Multiverse,
    cell: Vector2[Int],
    playerCell: () => StateId[Vector2[Int]])
  extends Entity {
  add(new ClassicalPosition((cell map (_.toDouble)) + Vector2(0.5, 0.5), cell))
  add(new Goal(playerCell))
  add(new SpriteView(texture = const(KeyTexture)))
}

/** Contains the sprite textures for exits. */
private object Exit {
  /** The sprite texture for a key. TODO: This should probably look more like an exit. */
  private val KeyTexture = new Texture(resolve("sprites/key.png"))
}