package superposition.entity

import cats.syntax.applicative.catsSyntaxApplicativeId
import cats.syntax.functor.toFunctorOps
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color.{GREEN, WHITE}
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import superposition.component._
import superposition.game.ResourceResolver.resolve
import superposition.math.QExpr.QExpr
import superposition.math.{BitSeq, Vector2}

/** A door blocks movement unless its control expression is true.
  *
  * @param multiverse the multiverse the lock belongs to
  * @param cell the cell position of the lock
  * @param code the code for the lock
  * @param control the control for the lock
  */
final class Lock(id: Int, multiverse: Multiverse, cell: Vector2[Int], code: Seq[Boolean], control: QExpr[BitSeq])
  extends Entity {
  locally {
    val texture = new TextureRegion(new Texture(resolve(s"sprites/lock_${code.length}.png")))
    add(new EntityId(id))
    add(new ClassicalPosition((cell map (_.toDouble)) + Vector2(0.5, 0.5)))
    add(new LockCode(code))
    add(new Renderable(1, control))
    add(new SpriteView(texture.pure[QExpr], color = control map (bits => if (bits.equals(code)) GREEN else WHITE)))
  }
}
