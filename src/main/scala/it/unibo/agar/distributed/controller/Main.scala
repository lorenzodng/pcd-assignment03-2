package it.unibo.agar.distributed.controller

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import it.unibo.agar.distributed.controller.PlayerCommand.{ConnectPlayerToGameManager, Ignore}
import it.unibo.agar.distributed.controller.ViewAdapterCommand.ConnectViewToGameManager
import it.unibo.agar.distributed.model.{GameInitializer, GameManagerActor, Player, World}
import it.unibo.agar.distributed.view.{GlobalView, LocalView}
import it.unibo.agar.startup
import scala.concurrent.ExecutionContext.Implicits.global

// service key per Receptionist
object GameManagerKey:
  val Key: ServiceKey[GameManagerCommand] = ServiceKey[GameManagerCommand]("GameMaster")

object Main:
  private val width = 800
  private val height = 800
  private val numFoods = 100
  private val foods = GameInitializer.initialFoods(numFoods, width, height)
  private val initialWorld = World(width, height, Seq.empty, foods)

  /** Nodo GameManagerActor, su porta 25251 */
  @main def startGameMaster(): Unit =
    val system = startup("agario", 25251):
      Behaviors.setup[GameManagerCommand]: context =>
        val foodGenerator = context.spawn(FoodGeneratorActor(initialWorld.width, initialWorld.height), "FoodGenerator")
        val collisionManager = context.spawn(CollisionManagerActor(), "CollisionManager")
        val gameManager = context.spawn(GameManagerActor(initialWorld, foodGenerator, collisionManager), "GameManager")
        context.system.receptionist ! Receptionist.Register(GameManagerKey.Key, gameManager)
        val globalView = new GlobalView()
        val globalViewActor = context.spawn(GlobalViewAdapterActor(gameManager, globalView), "GlobalViewAdapter")
        globalView.setGlobalViewActor(globalViewActor)
        javax.swing.SwingUtilities.invokeLater(() => globalView.open())
        println("GameManagerActor created and registered.")
        Behaviors.receiveMessagePartial[GameManagerCommand] { _ => Behaviors.same }
    
    println("GameManagerActor started on port 25251")
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

  private def connected(gameManagerRef: ActorRef[GameManagerCommand]): Behavior[PlayerCommand | ResolveGameManager] =
    Behaviors.receiveMessage:
      case msg =>
        Behaviors.same

  /** Comportamento comune a tutti i player */
  private def playerNodeBehavior(playerData: Player, width: Int, height: Int): Behavior[PlayerCommand | ResolveGameManager] =
    Behaviors.setup: context =>
      val playerActor = context.spawn(PlayerActor(playerData.id, playerData, width, height), s"PlayerActor-${playerData.id}")
      val localView = new LocalView(playerActor, playerData.id)
      val localViewAdapter = context.spawn(LocalViewAdapterActor(null, localView), s"LocalViewAdapter-${playerData.id}")
      javax.swing.SwingUtilities.invokeLater(() => localView.open())

      // adapter per convertire Receptionist.Listing in messaggi ResolveGameMaster(set.head) e Ignore
      val listingAdapter = context.messageAdapter[Receptionist.Listing]:
        case GameManagerKey.Key.Listing(set) if set.nonEmpty =>
          println(s"GameManager found: $set")
          ResolveGameManager(set.head)
        case _ =>
          println("GameManager not found.")
          Ignore

      //invio un messaggio al receptionist per cerca il GameMaster
      context.system.receptionist ! Receptionist.Subscribe(GameManagerKey.Key, listingAdapter)

      Behaviors.receiveMessage:
        case ResolveGameManager(gameManagerRef) =>
          println("GameManager resolved.")
          playerActor ! ConnectPlayerToGameManager(gameManagerRef)
          localViewAdapter ! ConnectViewToGameManager(gameManagerRef)
          connected(gameManagerRef) // Cambio comportamento dopo aver risolto
        case Ignore =>
          Behaviors.same




  


    
