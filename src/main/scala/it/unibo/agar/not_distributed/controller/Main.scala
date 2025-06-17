package it.unibo.agar.not_distributed.controller

import it.unibo.agar.not_distributed.model.{AIMovement, GameInitializer, MockGameStateManager, World}
import it.unibo.agar.not_distributed.view.{GlobalView, LocalView}
import java.awt.Window
import java.util.Timer
import java.util.TimerTask
import scala.swing.*
import scala.swing.Swing.onEDT

object Main extends SimpleSwingApplication:

  private val width = 1000
  private val height = 1000
  private val numPlayers = 4 //vengono mostrati solo due giocatori (p1 gestito dall'ia e p2 gestito dall'utente. Gli altri due appariranno quindi fermi)
  private val numFoods = 100
  private val players = GameInitializer.initialPlayers(numPlayers, width, height)
  private val foods = GameInitializer.initialFoods(numFoods, width, height)
  private val manager = new MockGameStateManager(World(width, height, players, foods))

  private val timer = new Timer()
  private val task: TimerTask = new TimerTask: //avvio un timer che
    override def run(): Unit =
      AIMovement.moveAI("p1", manager) //muove automaticamente il giocatore p1
      manager.tick() //aggiorna lo stato del gioco
      onEDT(Window.getWindows.foreach(_.repaint())) //ridisegna tutte le finestre aperte
  timer.scheduleAtFixedRate(task, 0, 30) //ripeto ogni 30ms

  override def top: Frame =
    new GlobalView(manager).open() //creo una finestra generale
    new LocalView(manager, "p1").open() //creo una finestra locale per p1
    new LocalView(manager, "p2").open() //creo una finestra locale per p2, che sarà il giocatore gestito dall'utente

    new Frame { visible = false }
