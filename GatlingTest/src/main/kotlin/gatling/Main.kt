package ru.performance.gatling

import io.gatling.app.Gatling
import io.gatling.core.config.GatlingPropertiesBuilder
import java.util.*
import java.util.stream.Collectors

class Main {

    companion object {
        @JvmStatic fun main(args: Array<String>) {
            val simulations: Set<String> = setOf(HttpSampleSimulation::class.java.name)
            simulations.forEach(Main::runGatlingSimulation)
        }
        private fun runGatlingSimulation(simulationFileName: String) {
            System.out.printf("Starting %s simulation%n", simulationFileName)
            val gatlingPropertiesBuilder = GatlingPropertiesBuilder()
            gatlingPropertiesBuilder.simulationClass(simulationFileName)
            gatlingPropertiesBuilder.resultsDirectory("test-reports")
            try {
                Gatling.fromMap(gatlingPropertiesBuilder.build())
            } catch (exception: Exception) {
                System.err.printf(
                    "Something went wrong for simulation %s %s%n", simulationFileName, exception
                )
            }
        }
    }
}