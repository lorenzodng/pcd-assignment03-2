package it.unibo.agar.distributed.controller

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.distributed.controller.GameMasterCommand.*
import it.unibo.agar.distributed.controller.PlayerCommand.*
import it.unibo.agar.distributed.model.Player

object PlayerActor:

  //costruttore
  def apply(id: String, player: Player, worldWidth: Double, worldHeight: Double, speed: Double = 10.0): Behavior[PlayerCommand] =
    waitingForGameMaster(id, player, worldWidth, worldHeight, speed) //comportamento principale

  private def waitingForGameMaster(id: String, player: Player, worldWidth: Double, worldHeight: Double, speed: Double): Behavior[PlayerCommand] =
    Behaviors.setup: context =>
      Behaviors.receiveMessage:
        case ConnectPlayerToGameMaster(gameMasterRef) =>
          gameMasterRef ! Join(id, context.self)
          active(id, player, gameMasterRef, worldWidth, worldHeight, speed) //comportamento una volta che è connesso con il GameMasterActor
        case _ =>
          Behaviors.same
  
  private def active(id: String, initialPlayer: Player, gameMaster: ActorRef[GameMasterCommand], worldWidth: Double, worldHeight: Double, speed: Double): Behavior[PlayerCommand] =
    Behaviors.setup: context =>
      var currentPlayer = initialPlayer
      Behaviors.receiveMessage:
        case PlayerMovement(dx, dy) =>
          currentPlayer = currentPlayer.updatePosition(dx, dy, speed, worldWidth, worldHeight)
          gameMaster ! ReceivePlayerUpdate(currentPlayer, context.self)
          Behaviors.same
        case Stop =>
          gameMaster ! Leave(id)
          Behaviors.stopped
        case ShutdownPlayer =>
          context.system.terminate()
          Behaviors.stopped
        case PlayerUpdated(updatedPlayer) =>
          currentPlayer = updatedPlayer
          Behaviors.same

