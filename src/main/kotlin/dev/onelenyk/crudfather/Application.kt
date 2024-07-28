package dev.onelenyk.crudfather

import dev.onelenyk.crudfather.app.Server

fun main(args: Array<String>): Unit {
    val server = Server()
    server.start()
    return
}