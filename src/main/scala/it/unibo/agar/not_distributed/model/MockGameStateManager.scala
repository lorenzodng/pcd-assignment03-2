package it.unibo.agar.not_distributed.model

trait GameStateManager:

  def getWorld: World
  def movePlayerDirection(id: String, dx: Double, dy: Double): Unit

//classe di gestione del gioco
class MockGameStateManager(var world: World, val speed: Double = 10.0) extends GameStateManager:

  private var directions: Map[String, (Double, Double)] = Map.empty //mappa per le direzioni di movimento richieste per ogni giocatore

  def getWorld: World = world //metodo che restituisce il mondo attuale

  //metodo che muove il giocatore in una specifica posizione
  def movePlayerDirection(id: String, dx: Double, dy: Double): Unit =
    directions = directions.updated(id, (dx, dy)) //sostituisce automaticamente le coordinate

  //metodo che aggiorna il mondo per tutti i giocatori
  def tick(): Unit =
    directions.foreach:
      case (id, (dx, dy)) =>
        world.playerById(id) match
          case Some(player) =>
            world = updateWorldAfterMovement(updatePlayerPosition(player, dx, dy))
          case None =>

  //metodo che aggiorna la posizione di un giocatore
  private def updatePlayerPosition(player: Player, dx: Double, dy: Double): Player =
    val newX = (player.x + dx * speed).max(0).min(world.width) //calcolo la nuova posizione senza uscire dai margini laterali
    val newY = (player.y + dy * speed).max(0).min(world.height) //calcolo la nuova posizione senza uscire dai margini superiore e inferiore
    player.copy(x = newX, y = newY)

  //metodo che aggiorna il mondo
  private def updateWorldAfterMovement(player: Player): World =
    val foodEaten = world.foods.filter(food => EatingManager.canEatFood(player, food)) //filtro tutti i cibi del mondo che il giocatore (in input) può mangiare
    val playerEatsFood = foodEaten.foldLeft(player)((p, food) => p.grow(food)) //per ogni cibo mangiato da parte del giocatore, incremento la massa
    val playersEaten = world.playersExcludingSelf(player).filter(player => EatingManager.canEatPlayer(playerEatsFood, player)) //filtro tutti i giocatori del mondo che il giocatore (in input) può mangiare, escludendo il giocatore attuale
    val playerEatPlayers = playersEaten.foldLeft(playerEatsFood)((p, other) => p.grow(other)) //per ogni giocatore mangiato da parte del giocatore, incremento la massa tenendo conto anche dei cibi mangiati
    world.updatePlayer(playerEatPlayers).removePlayers(playersEaten).removeFoods(foodEaten) //aggiorno il mondo
