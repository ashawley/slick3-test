package com.github.dnvriend

import slick.driver.PostgresDriver.api._

class MonadicJoinTest extends TestSpec {

  import CoffeeRepository._

  /**
   * Monadic joins are created with flatMap. They are theoretically more powerful
   * than applicative joins because the right-hand side may depend on the left-hand side.
   * However, this is not possible in standard SQL, so Slick has to compile them down to
   * applicative joins, which is possible in many useful cases but not in all of them
   * (and there are cases where it is possible in theory but Slick cannot perform the
   * required transformation yet).
   *
   * If a monadic join cannot be properly translated, it will fail at runtime.
   */

  /**
   * A cross-join is created with a flatMap operation on a Query (i.e. by introducing
   * more than one generator in a for-comprehension):
   */

  "MonadicJoin" should "crossJoin" in {
    val monadicCrossJoin = for {
      c <- coffees
      s <- suppliers
    } yield (c.name, s.name)

    db.run(monadicCrossJoin.result).futureValue shouldBe List(
      ("Colombian", "Acme, Inc."),
      ("Colombian", "Superior Coffee"),
      ("Colombian", "The High Ground"),
      ("French_Roast", "Acme, Inc."),
      ("French_Roast", "Superior Coffee"),
      ("French_Roast", "The High Ground"),
      ("Espresso", "Acme, Inc."),
      ("Espresso", "Superior Coffee"),
      ("Espresso", "The High Ground"),
      ("Colombian_Decaf", "Acme, Inc."),
      ("Colombian_Decaf", "Superior Coffee"),
      ("Colombian_Decaf", "The High Ground"),
      ("French_Roast_Decaf", "Acme, Inc."),
      ("French_Roast_Decaf", "Superior Coffee"),
      ("French_Roast_Decaf", "The High Ground")
    )
  }

  it should "innerJoin" in {
    // If you add a filter expression, it becomes an inner join:
    val monadicInnerJoin = for {
      c <- coffees
      s <- suppliers if c.supID === s.id
    } yield (c.name, s.name)

    db.run(monadicInnerJoin.result).futureValue shouldBe List(
      ("Colombian", "Acme, Inc."),
      ("French_Roast", "Superior Coffee"),
      ("Espresso", "The High Ground"),
      ("Colombian_Decaf", "Acme, Inc."),
      ("French_Roast_Decaf", "Superior Coffee")
    )
  }
}
