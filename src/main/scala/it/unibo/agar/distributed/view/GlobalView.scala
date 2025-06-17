package it.unibo.agar.distributed.view

import akka.actor.typed.ActorRef
import it.unibo.agar.distributed.controller.ViewAdapterCommand
import it.unibo.agar.distributed.controller.ViewAdapterCommand.ShutdownNotify
import it.unibo.agar.distributed.model.{Player, World}
import java.awt.Graphics2D
import java.awt.event.{WindowAdapter, WindowEvent}
import javax.swing.JOptionPane
import javax.swing.JOptionPane.showMessageDialog
import scala.swing.*

class GlobalView extends MainFrame:

  private var world: World = World.empty
  private var globalViewActor: Option[ActorRef[ViewAdapterCommand]] = None

  def setGlobalViewActor(actor: ActorRef[ViewAdapterCommand]): Unit =
    globalViewActor = Some(actor)

  title = "Agar.io - Global View"
  preferredSize = new Dimension(800, 800)

  def updateWorld(newWorld: World): Unit =
    javax.swing.SwingUtilities.invokeLater(() => 
      this.world = newWorld
      repaint() //aggiorna la finestra
    )

  contents = new Panel:
    override def paintComponent(g: Graphics2D): Unit =
      AgarViewUtils.drawWorld(g, world) //disegna sempre lo stato più recente del mondo
  
  peer.addWindowListener(new WindowAdapter {
    override def windowClosing(e: WindowEvent): Unit = {
      println("GlobalView is closing. Exiting application.")
      globalViewActor match
        case Some(actor) => actor ! ShutdownNotify
        case None => 
    }
  })