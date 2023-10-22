package ru.performance.gatling

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.ScenarioBuilder
import io.gatling.javaapi.core.Simulation
import ru.tinkoff.load.javaapi.JdbcDsl.jdbc
import ru.tinkoff.load.javaapi.JdbcDsl.*
import ru.tinkoff.load.javaapi.actions.*
import ru.tinkoff.load.javaapi.protocol.JdbcProtocolBuilder


class JdbcSimulation : Simulation() {

    private fun select(): QueryActionBuilder {
        return jdbc("SELECT SOME")
            .query("SELECT * FROM TEST_TABLE")
            .check(allResults().saveAs("RR"))
    }

    var dataBase: JdbcProtocolBuilder = DB()
        .url("jdbc:h2:mem:test")
        .username("sa")
        .password("")
        .maximumPoolSize(23)
        .protocolBuilder()


    var scn: ScenarioBuilder = scenario("JDBC scenario")
        .exec(select())

    init {
        setUp(
            scn.injectOpen(atOnceUsers(1))
        ).protocols(dataBase)
    }
}

