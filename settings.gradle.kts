pluginManagement {
    val kotlinVersion: String by settings
    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.allopen") version kotlinVersion
    }
    repositories {
        maven { url = uri("https://plugins.gradle.org/m2/") }
        gradlePluginPortal()
    }
}
rootProject.name = "PerformanceStubForStudy"

include("TicketApi") // апи клиента, который ходит сделать резерв
include("PaymentSystem")//система проведения оплаты
//include "TicketReplicator" //ассинхронный репликатор из кафки в imdg или db
include("Theatre") //апи театра у которого покупаются билеты
include("TicketTransactionHandler") //Система транзакции
include("GatlingTest")
include("StubCommon")