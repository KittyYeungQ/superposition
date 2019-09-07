package superposition

import engine.core.Game
import engine.util.math.Vec2d
import extras.tiles.Tilemap

/**
 * Stores game levels.
 */
private object Levels {
  /**
   * Creates level 1.
   */
  def createLevel1(): Unit = {
    lazy val multiverse: Multiverse = new Multiverse(List(universe), Tilemap.load(getClass.getResource("level1.tmx")))
    lazy val universe = new Universe(multiverse)
    // TODO
//    for (i <- 0 until 2) {
//      universe.add(new Quball(universe, UniversalId(i), new Vec2d(1 + i, 1)))
//    }
//    universe.add(new Door(universe, UniversalId(2), new Vec2d(4, 1)))
    universe.add(new Player(universe, UniversalId(3), Cell(0, 0)))
//    universe.add(new Laser(universe, UniversalId(4), new Vec2d(-2, -2), new Vec2d(0, -1)))
    Game.create(multiverse)
  }
}
