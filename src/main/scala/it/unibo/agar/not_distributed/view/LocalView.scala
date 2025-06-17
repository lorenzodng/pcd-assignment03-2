package it.unibo.agar.not_distributed.view

import it.unibo.agar.not_distributed.model.MockGameStateManager
import java.awt.Graphics2D
import scala.swing.*

//finestra locale della pov di ogni giocatore
class LocalView(manager: MockGameStateManager, playerId: String) extends MainFrame:

  title = s"Agar.io - Local View ($playerId)"
  preferredSize = new Dimension(400, 400)

  contents = new Panel:
    listenTo(keys, mouse.moves) //metto in ascolto la finestra del movimento del mouse e della tastiera (in realtà gli eventi della tastiera non sono stati gestiti)
    focusable = true
    requestFocusInWindow() //inizializzo l'ascolto

    //metodo che disegna il giocatore sulla finestra locale
    override def paintComponent(g: Graphics2D): Unit =
      val world = manager.getWorld
      val playerOpt = world.players.find(_.id == playerId)
      val (offsetX, offsetY) = playerOpt
        .map(p => (p.x - size.width / 2.0, p.y - size.height / 2.0)) //sposto la visuale in modo che il giocatore sia al centro della finestra
        .getOrElse((0.0, 0.0)) //se il giocatore non c’è, non lo mostro
      AgarViewUtils.drawWorld(g, world, offsetX, offsetY) //disegno il mondo in locale

    //reazione a un evento di movimento del mouse
    reactions += { case e: event.MouseMoved =>
      val mousePos = e.point
      val playerOpt = manager.getWorld.players.find(_.id == playerId)
      playerOpt.foreach: player =>
        val dx = (mousePos.x - size.width / 2) * 0.01
        val dy = (mousePos.y - size.height / 2) * 0.01
        manager.movePlayerDirection(playerId, dx, dy)
      repaint()
    }
