package it.unibo.agar.distributed.controller

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import it.unibo.agar.distributed.controller.GameMasterCommand.Subscribe
import it.unibo.agar.distributed.controller.ViewAdapterCommand.ConnectViewToGameMaster
import it.unibo.agar.distributed.controller.ViewAdapterCommand.{ShutdownNotify, UpdateWorldState, GameOver}
import it.unibo.agar.distributed.view.LocalView

object LocalViewAdapterActor:

  def apply(gameMaster: ActorRef[GameMasterCommand], localView: LocalView): Behavior[ViewAdapterCommand | ConnectViewToGameMaster] =
    if (gameMaster == null)
      waitingForGameMaster(localView)
    else
      active(gameMaster, localView)

  def waitingForGameMaster(localView: LocalView): Behavior[ConnectViewToGameMaster | ViewAdapterCommand] =
    Behaviors.setup: context =>
      Behaviors.receive: (context, message) =>
        message match
          case ConnectViewToGameMaster(gameMaster) =>
            gameMaster ! Subscribe(context.self)
            active(gameMaster, localView)
  
  def active(gameMaster: ActorRef[GameMasterCommand], localView: LocalView): Behavior[ViewAdapterCommand] =
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
          