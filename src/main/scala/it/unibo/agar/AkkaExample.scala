package it.unibo.agar

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object PingPong:

  sealed trait PingPongMessage extends Message
  
  case class Ping(ref: ActorRef[Pong]) extends PingPongMessage //è un messaggio
  case class Pong(ref: ActorRef[Ping]) extends PingPongMessage //è un messaggio

  def ping(): Behavior[PingPongMessage] =
    Behaviors.setup: context =>
      Behaviors.receiveMessage:
        case Ping(ref) =>
          context.log.info("Received Ping, sending Pong" + context.self)
          ref ! Pong(context.self)
          ping() //equivale a Behaviors.same
        case Pong(_) =>
          context.log.info("Received Pong while in Ping state, ignoring")
          Behaviors.same

  def pong(): Behavior[PingPongMessage] =
    Behaviors.setup: context =>
      Behaviors.receiveMessage:
        case Pong(ref) =>
          context.log.info("Received Pong, sending Ping" + context.self)
          ref ! Ping(context.self)
          pong() //equivale a Behaviors.same
        case Ping(_) =>
          context.log.info("Received Ping while in Pong state, ignoring")
          Behaviors.same
@main
def multipleRuns(): Unit =
  val pinger = startup("agario", 25251)(PingPong.ping()) //creo un attore pinger sulla porta 25251 che ha come comportamento ping()
  val ponger = startup("agario", 25252)(PingPong.pong()) //creo un attore ponger sulla porta 25252 che ha come comportamento pong()
  pinger ! PingPong.Ping(ponger) //invio un messaggio Pinger contenente come parametro ponger, a pinger.
