package it.unibo.agar.distributed.model

object EatingManager:

  private val MASS_MARGIN = 1.1 //costante di dimensione

  //verifico se due entità (giocatore e giocatore o giocatore e cibo) collidono
  private def collides(e1: Entity, e2: Entity): Boolean =
    e1.distanceTo(e2) < (e1.radius + e2.radius)

  //verifico se un giocatore può mangiare il cibo
  def canEatFood(player: Player, food: Food): Boolean =
    collides(player, food) && player.mass > food.mass

  //verifico se un giocatore può mangiare un giocatore
  def canEatPlayer(player: Player, other: Player): Boolean =
    collides(player, other) && player.mass > other.mass * MASS_MARGIN

