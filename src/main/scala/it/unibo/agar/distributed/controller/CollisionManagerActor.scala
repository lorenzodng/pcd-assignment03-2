package it.unibo.agar.distributed.controller

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.distributed.model.{EatingManager, Food, Player, World}
import it.unibo.agar.distributed.controller.GameManagerCommand.UpdateWorldAfterCollision
import it.unibo.agar.distributed.controller.CollisionManagerCommand.CheckCollisions

object CollisionManagerActor:

  def apply(): Behavior[CollisionManagerCommand] =
    Behaviors.receive: (context, message) =>
      message match
        case CheckCollisions(player, world, replyTo) =>
          val foodEaten = world.foods.filter(food => EatingManager.canEatFood(player, food))
          val playerEatsFood = foodEaten.foldLeft(player)((p, food) => p.grow(food))
          val playersEaten = world.playersExcludingSelf(playerEatsFood).filter(p => EatingManager.canEatPlayer(playerEatsFood, p))
          val playerEatPlayers = playersEaten.foldLeft(playerEatsFood)((p, other) => p.grow(other))
          val newWorld = world.updatePlayer(playerEatPlayers).removePlayers(playersEaten).removeFoods(foodEaten)
          replyTo ! UpdateWorldAfterCollision(newWorld, playerEatPlayers)
          Behaviors.same
