package it.unibo.agar.distributed.controller

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.distributed.controller.GameManagerCommand.Subscribe
import it.unibo.agar.distributed.controller.ViewAdapterCommand.ConnectViewToGameManager
import it.unibo.agar.distributed.controller.ViewAdapterCommand.{ShutdownNotify, UpdateWorldState, GameOver}
import it.unibo.agar.distributed.view.LocalView

object LocalViewAdapterActor:

  def apply(gameManager: ActorRef[GameManagerCommand], localView: LocalView): Behavior[ViewAdapterCommand | ConnectViewToGameManager] =
    if (gameManager == null)
      waitingForGameManager(localView)
    else
      active(gameManager, localView)

  private def waitingForGameManager(localView: LocalView): Behavior[ConnectViewToGameManager | ViewAdapterCommand] =
    Behaviors.setup: context =>
      Behaviors.receive: (context, message) =>
        message match
          case ConnectViewToGameManager(gameManager) =>
            gameManager ! Subscribe(context.self)
            active(gameManager, localView)
  
  def active(gameMaster: ActorRef[GameManagerCommand], localView: LocalView): Behavior[ViewAdapterCommand] =
    Behaviors.receive: (context, message) =>
      message match
        case UpdateWorldState(world) =>
          localView.updateWorld(world)
          Behaviors.same
        case ShutdownNotify =>
          javax.swing.SwingUtilities.invokeLater(() => localView.close())
          Behaviors.stopped
        case GameOver(winner) =>
          localView.gameOverPopup(winner)
          Behaviors.stopped
          