package com.github.dnvriend

import slick.driver.PostgresDriver.api._

class ZipJoinTest extends TestSpec {

  import CoffeeRepository._

  /**
   * In addition to the usual applicative join operators supported by relational databases
   * (which are based off a cross join or outer join), Slick also has zip joins which create
   * a pairwise join of two queries. The semantics are again the same as for Scala collections,
   * using the zip and zipWith methods:
   */

  "ZipJoins" should "zipJoin" in {
    val zipJoinQuery = for {
      (c, s) <- coffees zip suppliers
    } yield (c.name, s.name)

    db.run(zipJoinQuery.result).futureValue shouldBe List(
      ("Colombian", "Acme, Inc."),
      ("French_Roast", "Superior Coffee"),
      ("Espresso", "The High Ground")
    )
  }

  it should "zipWithJoin" in {
    val zipWithJoin = for {
      res <- coffees.zipWith(suppliers, (c: Coffees, s: Suppliers) => (c.name, s.name))
    } yield res

    db.run(zipWithJoin.result).futureValue shouldBe List(
      ("Colombian", "Acme, Inc."),
      ("French_Roast", "Superior Coffee"),
      ("Espresso", "The High Ground")
    )
  }

  /**
   * A particular kind of zip join is provided by zipWithIndex. It zips a query result with an
   * infinite sequence starting at 0. Such a sequence cannot be represented by an SQL database and
   * Slick does not currently support it, either. The resulting zipped query, however, can be represented
   * in SQL with the use of a row number function, so zipWithIndex is supported as a primitive operator:
   */

  it should "zipWithIndex" in {
    val zipWithIndexJoin = for {
      (c, idx) <- coffees.zipWithIndex
    } yield (c.name, idx)

    db.run(zipWithIndexJoin.result).futureValue shouldBe List(
      ("Colombian", 0),
      ("French_Roast", 1),
      ("Espresso", 2),
      ("Colombian_Decaf", 3),
      ("French_Roast_Decaf", 4)
    )
  }
}
