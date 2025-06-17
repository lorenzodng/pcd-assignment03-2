package it.unibo.agar.distributed.view

import it.unibo.agar.distributed.model.World
import java.awt.Color
import java.awt.Graphics2D

object AgarViewUtils:

  private val playerBorderColor = Color.black
  private val playerLabelOffsetX = 10 //posizione etichetta nome
  private val playerLabelOffsetY = 0 //posizione etichetta nome
  private val playerInnerOffset = 2 //cerchio interno (no bordo)
  private val playerInnerBorder = 4 //cerchio esterno (bordo)
  private val playerPalette: Array[Color] =
    Array(Color.blue, Color.orange, Color.cyan, Color.pink, Color.yellow, Color.red, Color.green, Color.lightGray) //colori dei giocatori

  //funzione che preleva il colore dal giocatore
  private def playerColor(id: String): Color = id match
    case pid if pid.startsWith("p") =>
      val idx = pid.drop(1).toIntOption.getOrElse(0) //prelevo solo il numero dopo p (p1 -> 1) in modo da identificare il colore
      playerPalette(idx % playerPalette.length) //scelgo il colore in funzione del numero prelevato in precedenza dall'id del giocatore
    case _ => Color.gray //altrimenti, associo il colore grigio

  //funzione generale per disegnare il mondo a schermo
  def drawWorld(g: Graphics2D, world: World, offsetX: Double = 0, offsetY: Double = 0): Unit =

    //funzione innestata che calcola le coordinate della finestra in cui viene mostrato il gioco
    def toScreenCenter(x: Double, y: Double, radius: Int): (Int, Int) =
      ((x - offsetX - radius).toInt, (y - offsetY - radius).toInt)

    //funzione innestata che calcola la posizione in cui disegnare l'etichetta (nome) del giocare
    def toScreenLabel(x: Double, y: Double): (Int, Int) =
      ((x - offsetX - playerLabelOffsetX).toInt, (y - offsetY - playerLabelOffsetY).toInt)

    //disegna il cibo sullo schermo
    g.setColor(Color.green)
    world.foods.foreach: food =>
      val radius = food.radius.toInt
      val diameter = radius * 2
      val (foodX, foodY) = toScreenCenter(food.x, food.y, radius)
      g.fillOval(foodX, foodY, diameter, diameter)

    //disegna i giocatori sullo schermo
    world.players.foreach: player =>
      val radius = player.radius.toInt
      val diameter = radius * 2
      val (borderX, borderY) = toScreenCenter(player.x, player.y, radius)
      g.setColor(playerBorderColor)
      g.drawOval(borderX, borderY, diameter, diameter)
      g.setColor(playerColor(player.id))
      val (innerX, innerY) = toScreenCenter(player.x, player.y, radius - playerInnerOffset)
      g.fillOval(innerX, innerY, diameter - playerInnerBorder, diameter - playerInnerBorder)
      g.setColor(playerBorderColor)
      val (labelX, labelY) = toScreenLabel(player.x, player.y)
      g.drawString(player.id, labelX, labelY)
