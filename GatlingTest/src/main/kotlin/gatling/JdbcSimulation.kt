package ru.performance.gatling

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.ScenarioBuilder
import io.gatling.javaapi.core.Simulation
import ru.tinkoff.load.javaapi.JdbcDsl.jdbc
import ru.tinkoff.load.javaapi.JdbcDsl.*
import ru.tinkoff.load.javaapi.actions.*
import ru.tinkoff.load.javaapi.check.simpleCheckType
import ru.tinkoff.load.javaapi.protocol.JdbcProtocolBuilder
import scala.collection.JavaConverters
import scala.collection.JavaConverters.*
import scala.collection.immutable.Map


class JdbcSimulation : Simulation() {

    private fun select(): QueryActionBuilder {
        return jdbc("SELECT CITY")
            .query("SELECT * FROM films.city where city_id = 2")
            .check(
                simpleCheck(simpleCheckType.NonEmpty),
                allResults().saveAs("RR"))
    }

    var dataBase: JdbcProtocolBuilder = DB()
        .url("jdbc:postgresql://localhost:5432/postgres")
        .username("postgres")
        .password("admin")
        .maximumPoolSize(23)
        .protocolBuilder()

    var scn: ScenarioBuilder = scenario("JDBC scenario")
        .exec(select())
        .exec { s ->
            val row = mapAsJavaMapConverter(s.getList<Map<String, Any>>("RR")[0]).asJava()
            println("RR:" + (row!!["city"]))
            s
        }

    init {
        setUp(
            scn.injectOpen(atOnceUsers(1))
        ).protocols(dataBase)
    }
}

