package it.unibo.agar.distributed.controller

import akka.actor.typed.ActorRef
import it.unibo.agar.distributed.model.{Player, World}
import akka.serialization.jackson.JsonSerializable

trait Message extends JsonSerializable

sealed trait GameMasterCommand extends Message

object GameMasterCommand:
  case object GenerateFood extends GameMasterCommand
  case class ReceivePlayerUpdate(player: it.unibo.agar.distributed.model.Player, replyTo: ActorRef[PlayerCommand]) extends GameMasterCommand
  case class Subscribe(subscriber: ActorRef[ViewAdapterCommand]) extends GameMasterCommand
  case class RequestWorld(replyTo: ActorRef[ViewAdapterCommand]) extends GameMasterCommand
  case class Join(playerId: String, playerRef: ActorRef[PlayerCommand]) extends GameMasterCommand
  case class Leave(playerId: String) extends GameMasterCommand
  case object ShutdownAll extends GameMasterCommand

sealed trait PlayerCommand extends Message
object PlayerCommand:
  case class PlayerMovement(dx: Double, dy: Double) extends PlayerCommand
  case object Stop extends PlayerCommand
  case object ShutdownPlayer extends PlayerCommand
  case class PlayerUpdated(player: Player) extends PlayerCommand
  case class ConnectPlayerToGameMaster(ref: ActorRef[GameMasterCommand]) extends PlayerCommand
  case object Ignore extends PlayerCommand

sealed trait ViewAdapterCommand extends Message
object ViewAdapterCommand:
  case class UpdateWorldState(world: World) extends ViewAdapterCommand
  case object ShutdownNotify extends ViewAdapterCommand
  case object CloseView extends ViewAdapterCommand
  case class GameOver(winner: Player) extends ViewAdapterCommand
  case class ConnectViewToGameMaster(ref: ActorRef[GameMasterCommand]) extends ViewAdapterCommand

case class ResolveGameMaster(ref: ActorRef[GameMasterCommand]) extends Message
