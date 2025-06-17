package it.unibo.agar.not_distributed.model

object AIMovement:

  //è una funzione che trova il cibo più vicino nel mondo rispetto al giocatore
  def nearestFood(player: String, world: World): Option[Food] =
    world.foods
      .sortBy(food => world.playerById(player).map(p => p.distanceTo(food)).getOrElse(Double.MaxValue)) //per ogni cibo, calcolo la distanza del giocatore da quel cibo
      .headOption //prendo il primo elemento della lista ordinata
  
  //è una funzione che gestisce il movimento di un giocatore
  def moveAI(name: String, gameManager: GameStateManager): Unit =
    val world = gameManager.getWorld //prendo il mondo corrente
    val aiOpt = world.playerById(name) //prendo il giocatore
    val foodOpt = nearestFood(name, world) //cerco il cibo più vicino
    (aiOpt, foodOpt) match
      case (Some(ai), Some(food)) => //se il giocatore e il cibo esistono
        val dx = food.x - ai.x //allora riduco la distanza tra il cibo e il giocatore
        val dy = food.y - ai.y //allora riduco la distanza tra il cibo e il giocatore
        val distance = math.hypot(dx, dy)
        if (distance > 0) //se la distanza è positiva(cioè non sono nello stesso punto)
          val normalizedDx = dx / distance //normalizzo la distanza ottenuta, così da spostare il giocare di una giusta quantità (nè troppo nè troppo poco)
          val normalizedDy = dy / distance //normalizzo la distanza ottenuta, così da spostare il giocare di una giusta quantità (nè troppo nè troppo poco)
          gameManager.movePlayerDirection(name, normalizedDx, normalizedDy) //sposto il giocatore
      case _ => //se il giocatore e il cibo non esistono non faccio niente
