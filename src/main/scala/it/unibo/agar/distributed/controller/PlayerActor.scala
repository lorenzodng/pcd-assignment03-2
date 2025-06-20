package it.unibo.agar.distributed.controller

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.distributed.controller.GameManagerCommand.*
import it.unibo.agar.distributed.controller.PlayerCommand.*
import it.unibo.agar.distributed.model.Player

object PlayerActor:

  //costruttore
  def apply(id: String, player: Player, worldWidth: Double, worldHeight: Double, speed: Double = 10.0): Behavior[PlayerCommand] =
    waitingForGameManager(id, player, worldWidth, worldHeight, speed) //comportamento principale

  private def waitingForGameManager(id: String, player: Player, worldWidth: Double, worldHeight: Double, speed: Double): Behavior[PlayerCommand] =
    Behaviors.setup: context =>
      Behaviors.receiveMessage:
        case ConnectPlayerToGameManager(gameManagerRef) =>
          gameManagerRef ! Join(id, context.self)
          active(id, player, gameManagerRef, worldWidth, worldHeight, speed) //comportamento una volta che è connesso con il GameManagerActor
        case _ =>
          Behaviors.same
  
  private def active(id: String, initialPlayer: Player, gameManager: ActorRef[GameManagerCommand], worldWidth: Double, worldHeight: Double, speed: Double): Behavior[PlayerCommand] =
    Behaviors.setup: context =>
      var currentPlayer = initialPlayer
      Behaviors.receiveMessage:
        case PlayerMovement(dx, dy) =>
          currentPlayer = currentPlayer.updatePosition(dx, dy, speed, worldWidth, worldHeight)
          gameManager ! ReceivePlayerUpdate(currentPlayer, context.self)
          Behaviors.same
        case Stop =>
          gameManager ! Leave(id)
          Behaviors.stopped
        case ShutdownPlayer =>
          context.system.terminate()
          Behaviors.stopped
        case PlayerUpdated(updatedPlayer) =>
          currentPlayer = updatedPlayer
          Behaviors.same

