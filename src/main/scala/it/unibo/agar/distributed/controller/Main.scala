package it.unibo.agar.distributed.controller

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import it.unibo.agar.distributed.controller.PlayerCommand.{ConnectPlayerToGameMaster, Ignore}
import it.unibo.agar.distributed.controller.ViewAdapterCommand.ConnectViewToGameMaster
import it.unibo.agar.distributed.model.{GameInitializer, GameMasterActor, Player, World}
import it.unibo.agar.distributed.view.{GlobalView, LocalView}
import it.unibo.agar.startup
import scala.concurrent.ExecutionContext.Implicits.global

// service key per Receptionist
object GameMasterKey:
  val Key: ServiceKey[GameMasterCommand] = ServiceKey[GameMasterCommand]("GameMaster")

object Main:
  private val width = 800
  private val height = 800
  private val numFoods = 100
  private val foods = GameInitializer.initialFoods(numFoods, width, height)
  private val initialWorld = World(width, height, Seq.empty, foods)

  /** Nodo GameMasterActor, su porta 25251 */
  @main def startGameMaster(): Unit =
    val system = startup("agario", 25251):
      Behaviors.setup[GameMasterCommand]: context =>
        val gameMaster = context.spawn(GameMasterActor(initialWorld), "GameMaster")
        context.system.receptionist ! Receptionist.Register(GameMasterKey.Key, gameMaster)
        val globalView = new GlobalView()
        val globalViewActor = context.spawn(GlobalViewAdapterActor(gameMaster, globalView), "GlobalViewAdapter")
        globalView.setGlobalViewActor(globalViewActor)
        javax.swing.SwingUtilities.invokeLater(() => globalView.open())
        println("GameMasterActor created and registered.")
        Behaviors.receiveMessagePartial[GameMasterCommand] { _ => Behaviors.same }
    
    println("GameMasterActor started on port 25251")
    system.whenTerminated.onComplete(_ => System.exit(0))

  /** Nodo PlayerActor, su porta 25252 */
  @main def startPlayer1(): Unit =
    val player = Player("p1", 0, 0, 0)
    val system = startup("agario", 25252):
      playerNodeBehavior(player, width, height)
    
    println("Player1 started on port 25252")
    system.whenTerminated.onComplete(_ => System.exit(0))

  /** Nodo PlayerActor2, su porta 25253 */
  @main def startPlayer2(): Unit =
    val player = Player("p2", 0, 0, 0)
    val system = startup("agario", 25253):
      playerNodeBehavior(player, width, height)
    
    println("Player2 started on port 25253")
    system.whenTerminated.onComplete(_ => System.exit(0))

  def connected(gameMasterRef: ActorRef[GameMasterCommand]): Behavior[PlayerCommand | ResolveGameMaster] =
    Behaviors.receiveMessage:
      case msg =>
        Behaviors.same

  /** Comportamento comune a tutti i player */
  def playerNodeBehavior(playerData: Player, width: Int, height: Int): Behavior[PlayerCommand | ResolveGameMaster] =
    Behaviors.setup: context =>
      val playerActor = context.spawn(PlayerActor(playerData.id, playerData, width, height), s"PlayerActor-${playerData.id}")
      val localView = new LocalView(playerActor, playerData.id)
      val localViewAdapter = context.spawn(LocalViewAdapterActor(null, localView), s"LocalViewAdapter-${playerData.id}")
      javax.swing.SwingUtilities.invokeLater(() => localView.open())

      // adapter per convertire Receptionist.Listing in messaggi ResolveGameMaster(set.head) e Ignore
      val listingAdapter = context.messageAdapter[Receptionist.Listing]:
        case GameMasterKey.Key.Listing(set) if set.nonEmpty =>
          println(s"GameMaster found: $set")
          ResolveGameMaster(set.head)
        case _ =>
          println("GameMaster not found.")
          Ignore

      //invio un messaggio al receptionist per cerca il GameMaster
      context.system.receptionist ! Receptionist.Subscribe(GameMasterKey.Key, listingAdapter)

      Behaviors.receiveMessage:
        case ResolveGameMaster(gameMasterRef) =>
          println("GameMaster resolved.")
          playerActor ! ConnectPlayerToGameMaster(gameMasterRef)
          localViewAdapter ! ConnectViewToGameMaster(gameMasterRef)
          connected(gameMasterRef) // Cambio comportamento dopo aver risolto
        case Ignore =>
          Behaviors.same




  


    
