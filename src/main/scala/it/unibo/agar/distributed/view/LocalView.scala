package it.unibo.agar.distributed.view

import akka.actor.typed.ActorRef
import it.unibo.agar.distributed.controller.PlayerCommand
import it.unibo.agar.distributed.controller.PlayerCommand.{PlayerMovement, ShutdownPlayer}
import it.unibo.agar.distributed.model.{Player, World}
import java.awt.Graphics2D
import java.awt.event.WindowAdapter
import javax.swing.JOptionPane
import javax.swing.JOptionPane.showMessageDialog
import scala.swing.*

//finestra locale della pov di ogni giocatore
class LocalView(playerActor: ActorRef[PlayerCommand], playerId: String) extends MainFrame:
  
  private var world: World = World.empty
  title = s"Agar.io - Local View ($playerId)"
  preferredSize = new Dimension(400, 400)

  def updateWorld(newWorld: World): Unit =
    this.world = newWorld
    repaint()
    
  contents = new Panel:
    listenTo(keys, mouse.moves) //metto in ascolto la finestra del movimento del mouse e della tastiera (in realtà gli eventi della tastiera non sono stati gestiti)
    focusable = true
    requestFocusInWindow() //inizializzo l'ascolto
    //metodo che disegna il giocatore sulla finestra locale
    override def paintComponent(g: Graphics2D): Unit =
      val playerOpt = world.players.find(_.id == playerId)
      val (offsetX, offsetY) = playerOpt
        .map(p => (p.x - size.width / 2.0, p.y - size.height / 2.0)) //sposto la visuale in modo che il giocatore sia al centro della finestra
        .getOrElse((0.0, 0.0)) //se il giocatore non c’è, non lo mostro
      AgarViewUtils.drawWorld(g, world, offsetX, offsetY) //disegno il mondo in locale
    //reazione a un evento di movimento del mouse
    reactions += { case e: event.MouseMoved =>
      val mousePos = e.point
      val playerOpt = world.players.find(_.id == playerId)
      playerOpt.foreach: player => //foreach su un Option non itera sugli elementi, ma indica che viene eseguito se l'Option esiste
        val dx = (mousePos.x - size.width / 2) * 0.01
        val dy = (mousePos.y - size.height / 2) * 0.01
        playerActor ! PlayerMovement(dx, dy)
      repaint()
    }

  peer.addWindowListener(new java.awt.event.WindowAdapter {
    override def windowClosing(e: java.awt.event.WindowEvent): Unit = {
      println(s"LocalView window closing for player $playerId, sending Stop message")
      playerActor ! PlayerCommand.Stop
    }
  })

  def gameOverPopup(winner: Player): Unit =
    javax.swing.SwingUtilities.invokeLater(() => 
      showMessageDialog(peer, s"Player '${winner.id}' won!", "Game Over", JOptionPane.INFORMATION_MESSAGE)
      playerActor ! ShutdownPlayer
      this.close()
    )
