package it.unibo.agar.distributed.model

import scala.concurrent.duration.DurationInt
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.distributed.controller.GameMasterCommand.*
import it.unibo.agar.distributed.controller.PlayerCommand.{PlayerUpdated, ShutdownPlayer}
import it.unibo.agar.distributed.controller.ViewAdapterCommand.{CloseView, GameOver, UpdateWorldState}
import it.unibo.agar.distributed.controller.{GameMasterCommand, PlayerCommand, ViewAdapterCommand}

import scala.collection.mutable

object GameMasterActor:

  def apply(world: World, speed: Double = 10.0, tickIntervalMillis: Long = 30): Behavior[GameMasterCommand] =
    Behaviors.setup: context =>
      var currentWorld: World = world
      val subscribers = mutable.Set.empty[ActorRef[ViewAdapterCommand]]
      val players = mutable.Map.empty[String, ActorRef[PlayerCommand]]

      def generateRandomFood(count: Int): Seq[Food] =
        (1 to count).map: _ =>
          Food(id = java.util.UUID.randomUUID().toString, x = Math.random() * currentWorld.width, y = Math.random() * currentWorld.height)

      def updateWorldAfterMovement(player: Player): World =
        val foodEaten = currentWorld.foods.filter(food => EatingManager.canEatFood(player, food))
        val playerEatsFood = foodEaten.foldLeft(player)((p, food) => p.grow(food))
        val playersEaten = currentWorld.playersExcludingSelf(playerEatsFood).filter(p => EatingManager.canEatPlayer(playerEatsFood, p))
        val playerEatPlayers = playersEaten.foldLeft(playerEatsFood)((p, other) => p.grow(other))
        currentWorld.updatePlayer(playerEatPlayers).removePlayers(playersEaten).removeFoods(foodEaten)

      def randomPlayer(id: String): Player =
        Player(id, Math.random() * currentWorld.width, Math.random() * currentWorld.height, 120.0)

      Behaviors.withTimers: timers =>
        timers.startTimerWithFixedDelay(GenerateFood, GenerateFood, 3.seconds)

        Behaviors.receiveMessage:
          case GenerateFood =>
            val newFoods = generateRandomFood(10)
            currentWorld = currentWorld.copy(foods = currentWorld.foods ++ newFoods)
            subscribers.foreach(_ ! UpdateWorldState(currentWorld))
            Behaviors.same
          case ReceivePlayerUpdate(player, replyTo) =>
            currentWorld = updateWorldAfterMovement(player)
            currentWorld.playerById(player.id).foreach: updatedPlayer =>
              replyTo ! PlayerUpdated(updatedPlayer)
            subscribers.foreach(_ ! UpdateWorldState(currentWorld))
            val maybeWinner = currentWorld.players.find(_.mass >= 10000)
            maybeWinner match
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
            val newPlayer = randomPlayer(playerId)
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
