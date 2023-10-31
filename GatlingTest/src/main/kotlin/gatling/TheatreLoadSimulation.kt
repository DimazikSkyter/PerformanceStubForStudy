package gatling

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import io.gatling.javaapi.core.CoreDsl
import io.gatling.javaapi.core.ScenarioBuilder
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl
import io.gatling.javaapi.http.HttpProtocolBuilder
import ru.tinkoff.load.javaapi.JdbcDsl
import ru.tinkoff.load.javaapi.actions.QueryActionBuilder
import ru.tinkoff.load.javaapi.check.simpleCheckType
import java.lang.Exception
import kotlin.random.Random

class TheatreLoadSimulation: Simulation() {
    private val theatreUrl: String = "http://localhost:8080"
    private val random: Random = Random.Default

    private var eventProtocol: HttpProtocolBuilder = HttpDsl.http.baseUrl(theatreUrl)
        .check(HttpDsl.status().`is`(200))

    var scn: ScenarioBuilder = CoreDsl.scenario("Make purchase")
        .exec(
            HttpDsl.http("Get event list")
                .get("/theatre/events")
                .check(CoreDsl.jsonPath("$").notNull().saveAs("eventsBodyStr"))
        )
        .exec { session ->
            val listOfEvents: List<String> = jacksonObjectMapper()
                .readValue<List<String>>(session.getString("eventsBodyStr"),
                    jacksonTypeRef<List<String>>())
            return@exec session.set("eventName", listOfEvents[0])
        }.exec (HttpDsl
            .http { session -> "List of free seats of event ${session.getString("eventName")}" }
            .get{ session -> "/theatre/seats/${session.getString("eventName")}" }
            .check(CoreDsl.jsonPath("$.seats[*].place").saveAs("seat")))
        .exec (
            HttpDsl.http("Make a reserve").post("/theatre/reserve")
                .header("XREQUEST_ID", "123123")
                .formParam("event") { session -> session.get<String>("eventName")!! }
                .formParam("seat") { session -> session.get<String>("seat")!! }
                .check(CoreDsl.jsonPath("$.reserveId").saveAs("reserveId"))
        ).exec(HttpDsl.http("Make purchase").post("/theatre/purchase")
                    .formParam("reserve_id"){session -> session.getInt("reserveId")}
                    .check(CoreDsl.jsonPath("$.result").isEL("true"))

        )


    private fun select(): QueryActionBuilder {
        return JdbcDsl.jdbc("SELECT CITY")
            .query("SELECT * FROM films.city where city_id = 2")
            .check(
                JdbcDsl.simpleCheck(simpleCheckType.NonEmpty),
                JdbcDsl.allResults().saveAs("RR")
            )
    }

    init {
        setUp(
            scn.injectOpen(CoreDsl.atOnceUsers(1))
        ).protocols(eventProtocol)
    }
}