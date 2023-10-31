package ru.performance.gatling

import io.gatling.javaapi.core.CoreDsl.*
import io.gatling.javaapi.core.ScenarioBuilder
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status
import io.gatling.javaapi.http.HttpProtocolBuilder
import java.time.Duration


class MainHttpSampleSimulation: Simulation() {

    var httpProtocol: HttpProtocolBuilder = http.baseUrl("http://localhost:8080")
        .header("Content-Type", "application/json")
        .header("Accept-Encoding", "gzip")
        .check(status().`is`(200))

    var scn: ScenarioBuilder = scenario("Root end point calls")
        .exec(http("list of events").get("/theatre/events"));

        init {
            setUp(scn.injectOpen(constantUsersPerSec(1.0).during(Duration.ofSeconds(60))))
                .protocols(httpProtocol)
                .assertions(
                    global().responseTime().percentile3().lt(1000),
                    global().successfulRequests().percent().gt(95.0)
                )
            ;
        }
}

