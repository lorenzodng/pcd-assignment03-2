package it.unibo.agar.distributed.model

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.distributed.controller.FoodGeneratorCommand.SubscribeGameManager
import it.unibo.agar.distributed.controller.GameManagerCommand.*
import it.unibo.agar.distributed.controller.PlayerCommand.{PlayerUpdated, ShutdownPlayer}
import it.unibo.agar.distributed.controller.ViewAdapterCommand.{CloseView, GameOver, UpdateWorldState}
import it.unibo.agar.distributed.controller.CollisionManagerCommand.CheckCollisions
import it.unibo.agar.distributed.controller.{CollisionManagerCommand, FoodGeneratorCommand, GameManagerCommand, PlayerCommand, ViewAdapterCommand}

import scala.collection.mutable

object GameManagerActor:

  def apply(world: World, foodGenerator: ActorRef[FoodGeneratorCommand], collisionManager: ActorRef[CollisionManagerCommand], speed: Double = 10.0, tickIntervalMillis: Long = 30): Behavior[GameManagerCommand] =
    Behaviors.setup: context =>
      var currentWorld: World = world
      val subscribers = mutable.Set.empty[ActorRef[ViewAdapterCommand]]
      val players = mutable.Map.empty[String, ActorRef[PlayerCommand]]

      foodGenerator ! SubscribeGameManager(context.self)
      
      // Riceve nuovo cibo dal FoodGeneratorActor
      def onFoodGenerated(newFoods: Seq[Food]): Unit =
        currentWorld = currentWorld.copy(foods = currentWorld.foods ++ newFoods)

      Behaviors.receiveMessage:
        case UpdateWorldAfterFood(newFoods) =>
          onFoodGenerated(newFoods)
          subscribers.foreach(_ ! UpdateWorldState(currentWorld))
          Behaviors.same
        case ReceivePlayerUpdate(player, replyTo) =>
          collisionManager ! CheckCollisions(player, currentWorld, context.self)
          Behaviors.same
        case UpdateWorldAfterCollision(updatedWorld, updatedPlayer) =>
          currentWorld = updatedWorld
          subscribers.foreach(_ ! UpdateWorldState(updatedWorld))
          players.get(updatedPlayer.id).foreach(_ ! PlayerUpdated(updatedPlayer))
          updatedWorld.players.find(_.mass >= 10000) match
            case Some(winner) =>
              subscribers.foreach(_ ! GameOver(winner))
              context.system.terminate()
              Behaviors.stopped
            case None =>
              Behaviors.same
        case Subscribe(sub) =>
          subscribers.add(sub)
          sub ! UpdateWorldState(currentWorld)
          Behaviors.same
        case Join(playerId, playerRef) =>
          context.log.info(s"Player $playerId joined the game.")
          players.put(playerId, playerRef)
          val newPlayer = Player(playerId, Math.random() * currentWorld.width, Math.random() * currentWorld.height, 120.0)
          currentWorld = currentWorld.addPlayer(newPlayer)
          playerRef ! PlayerUpdated(newPlayer)
          subscribers.foreach(_ ! UpdateWorldState(currentWorld))
          Behaviors.same
        case Leave(playerId) =>
          context.log.info(s"Player $playerId left the game.")
          players.remove(playerId)
          currentWorld = currentWorld.removePlayer(playerId)
          subscribers.foreach(_ ! UpdateWorldState(currentWorld))
          Behaviors.same
        case ShutdownAll =>
          players.values.foreach(_ ! ShutdownPlayer)
          subscribers.foreach(_ ! CloseView)
          context.system.terminate()
          Behaviors.stopped
      
    
