package it.unibo.agar.not_distributed.model

sealed trait Entity:

  def id: String
  def mass: Double
  def x: Double
  def y: Double
  def radius: Double = math.sqrt(mass / math.Pi)

  //funzione che calcola la distanza rispetto a una certa entità
  def distanceTo(other: Entity): Double =
    val dx = x - other.x
    val dy = y - other.y
    math.hypot(dx, dy)

//classe giocatore
case class Player(id: String, x: Double, y: Double, mass: Double) extends Entity:

  //funzione che incrementa la massa di un'entità
  def grow(entity: Entity): Player =
    copy(mass = mass + entity.mass)

//classe cibo
case class Food(id: String, x: Double, y: Double, mass: Double = 100.0) extends Entity

//classe mondo
case class World(width: Int, height: Int, players: Seq[Player], foods: Seq[Food]):

  //metodo che restituisce tutti i giocatori tranne uno specifico
  def playersExcludingSelf(player: Player): Seq[Player] =
    players.filterNot(_.id == player.id)

  //metodo che cerca un giocatore
  def playerById(id: String): Option[Player] =
    players.find(_.id == id)

  //metodo che aggiorna un giocatore
  def updatePlayer(player: Player): World =
    copy(players = players.map(p => if (p.id == player.id) player else p)) //(creo un nuovo mondo:) scorro la lista players: per ogni giocatore, se l'id coincide con quello del giocatore di input, lo sostituisco con quello di input, altrimenti no

  //metodo che rimuove un insieme di giocatori
  def removePlayers(ids: Seq[Player]): World =
    copy(players = players.filterNot(p => ids.map(_.id).contains(p.id))) //(creo un nuovo mondo:) scorro la lista players: per ogni giocatore, seleziono quelli che non sono presenti nella lista creata con map contenente gli id dei giocatori da eliminare

  //metodo che rimuove un insieme di cibi
  def removeFoods(ids: Seq[Food]): World =
    copy(foods = foods.filterNot(f => ids.contains(f)))
