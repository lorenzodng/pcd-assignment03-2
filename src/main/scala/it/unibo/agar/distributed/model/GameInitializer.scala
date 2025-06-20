package it.unibo.agar.distributed.model

import scala.util.Random

object GameInitializer:

  //genero una lista casuale iniziale di cibi
  def initialFoods(numFoods: Int, width: Int, height: Int, initialMass: Double = 100.0): Seq[Food] =
    (1 to numFoods).map[Food](i => Food(s"f$i", Random.nextInt(width), Random.nextInt(height), initialMass))
