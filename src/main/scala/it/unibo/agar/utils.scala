package it.unibo.agar

import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import com.typesafe.config.ConfigFactory

val seeds = List(2551, 2552) // seed used in the configuration

//funzione che consente ad Akka di configurare ed interpretare gli attori su due porte diverse, come se fossero in remoto
def startup[X](file: String = "base-cluster", port: Int)(root: => Behavior[X]): ActorSystem[X] =
  val config = ConfigFactory.parseString(s"""akka.remote.artery.canonical.port=$port""").withFallback(ConfigFactory.load(file))
  ActorSystem(root, file, config)

def startupWithRole[X](role: String, port: Int)(root: => Behavior[X]): ActorSystem[X] =
  val config = ConfigFactory
    .parseString(s"""
      akka.remote.artery.canonical.port=$port
      akka.cluster.roles = [$role]
      """)
    .withFallback(ConfigFactory.load("base-cluster"))

  // Create an Akka system
  ActorSystem(root, "ClusterSystem", config)
