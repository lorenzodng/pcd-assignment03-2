package it.unibo.agar.not_distributed.view

import it.unibo.agar.not_distributed.model.MockGameStateManager
import java.awt.Graphics2D
import scala.swing.*

//finestra generale in cui osservare tutti i giocatori
class GlobalView(manager: MockGameStateManager) extends MainFrame:

  title = "Agar.io - Global View"
  preferredSize = new Dimension(800, 800) //imposto la dimensione della finestra di gioco

  contents = new Panel:
    override def paintComponent(g: Graphics2D): Unit =
      val world = manager.getWorld
      AgarViewUtils.drawWorld(g, world) //disegno il mondo
