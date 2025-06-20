package it.unibo.agar.distributed.controller

import akka.actor.typed.ActorRef
import it.unibo.agar.distributed.model.{Food, Player, World}
import akka.serialization.jackson.JsonSerializable

trait Message extends JsonSerializable

sealed trait GameManagerCommand extends Message
object GameManagerCommand:
  case object RandomFood extends GameManagerCommand
  case class UpdateWorldAfterFood(food: Seq[Food]) extends GameManagerCommand
  case class UpdateWorldAfterCollision(world: World, player: Player) extends GameManagerCommand
  case class ReceivePlayerUpdate(player: it.unibo.agar.distributed.model.Player, replyTo: ActorRef[PlayerCommand]) extends GameManagerCommand
  case class Subscribe(subscriber: ActorRef[ViewAdapterCommand]) extends GameManagerCommand
  case class RequestWorld(replyTo: ActorRef[ViewAdapterCommand]) extends GameManagerCommand
  case class Join(playerId: String, playerRef: ActorRef[PlayerCommand]) extends GameManagerCommand
  case class Leave(playerId: String) extends GameManagerCommand
  case object ShutdownAll extends GameManagerCommand

sealed trait PlayerCommand extends Message
object PlayerCommand:
  case class PlayerMovement(dx: Double, dy: Double) extends PlayerCommand
  case object Stop extends PlayerCommand
  case object ShutdownPlayer extends PlayerCommand
  case class PlayerUpdated(player: Player) extends PlayerCommand
  case class ConnectPlayerToGameManager(ref: ActorRef[GameManagerCommand]) extends PlayerCommand
  case object Ignore extends PlayerCommand

sealed trait ViewAdapterCommand extends Message
object ViewAdapterCommand:
  case class UpdateWorldState(world: World) extends ViewAdapterCommand
  case object ShutdownNotify extends ViewAdapterCommand
  case object CloseView extends ViewAdapterCommand
  case class GameOver(winner: Player) extends ViewAdapterCommand
  case class ConnectViewToGameManager(ref: ActorRef[GameManagerCommand]) extends ViewAdapterCommand

sealed trait FoodGeneratorCommand extends Message
object FoodGeneratorCommand:
  case class GenerateFood(foodCount: Int) extends FoodGeneratorCommand
  case class SubscribeGameManager(gameManager: ActorRef[GameManagerCommand]) extends FoodGeneratorCommand
  
sealed trait CollisionManagerCommand extends Message
object CollisionManagerCommand:
  case class CheckCollisions(player: Player, currentWorld: World, replyTo: ActorRef[GameManagerCommand]) extends CollisionManagerCommand

case class ResolveGameManager(ref: ActorRef[GameManagerCommand]) extends Message
