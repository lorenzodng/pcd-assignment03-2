package it.unibo.agar.distributed.controller

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.distributed.controller.FoodGeneratorCommand.{GenerateFood, SubscribeGameManager}
import it.unibo.agar.distributed.controller.GameManagerCommand.UpdateWorldAfterFood
import scala.concurrent.duration.*
import it.unibo.agar.distributed.model.Food

object FoodGeneratorActor:

  def apply(worldWidth: Double, worldHeight: Double): Behavior[FoodGeneratorCommand] =
    Behaviors.setup: context =>
      Behaviors.withTimers: timers =>
        var gameManagerOpt: Option[ActorRef[GameManagerCommand]] = None

        def generateRandomFood(foodCount: Integer): Seq[Food] =
          (1 to foodCount).map: _ =>
            Food(java.util.UUID.randomUUID().toString, Math.random() * worldWidth, Math.random() * worldHeight)

        timers.startTimerWithFixedDelay(GenerateFood(20), 3.seconds) //ogni 3 secondi, invio nuovamente il messaggio a questo attore

        Behaviors.receiveMessage:
          case SubscribeGameManager(gameManager) =>
            gameManagerOpt = Some(gameManager)
            Behaviors.same
          case GenerateFood(foodCount) =>
            gameManagerOpt.foreach: gm =>
              val newFoods = generateRandomFood(foodCount)
              gm ! UpdateWorldAfterFood(newFoods) 
            Behaviors.same
