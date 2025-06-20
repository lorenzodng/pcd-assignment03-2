package it.unibo.agar.distributed.controller

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import it.unibo.agar.distributed.controller.GameManagerCommand.Subscribe
import it.unibo.agar.distributed.controller.ViewAdapterCommand.{CloseView, ShutdownNotify, UpdateWorldState}
import it.unibo.agar.distributed.view.GlobalView

object GlobalViewAdapterActor:
  def apply(gameManager: ActorRef[GameManagerCommand], globalView: GlobalView): Behavior[ViewAdapterCommand] =
    Behaviors.setup: context =>

      val worldAdapter: ActorRef[ViewAdapterCommand] = context.self

      gameManager ! Subscribe(worldAdapter)

      Behaviors.receiveMessage:
        case UpdateWorldState(world) =>
          globalView.updateWorld(world)
          Behaviors.same
        case ShutdownNotify =>
          gameManager ! GameManagerCommand.ShutdownAll
          Behaviors.same
        case CloseView =>
          javax.swing.SwingUtilities.invokeLater(() => globalView.close())
          Behaviors.stopped
      
