package superposition

/**
 * A cell in a grid.
 *
 * @param row    the row number of this cell
 * @param column the column number of this cell
 */
private final case class Cell(row: Long, column: Long) {
  /**
   * The cell above this one.
   */
  def up: Cell = Cell(row + 1, column)

  /**
   * The cell below this one.
   */
  def down: Cell = Cell(row - 1, column)

  /**
   * The cell to the left of this one.
   */
  def left: Cell = Cell(row, column - 1)

  /**
   * The cell to the right of this one.
   */
  def right: Cell = Cell(row, column + 1)
}
