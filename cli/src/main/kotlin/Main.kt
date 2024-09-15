package dev.schuberth.template.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.mordant.rendering.Theme

import dev.schuberth.template.lib.sayHello

import kotlin.system.exitProcess

fun main(args: Array<String>) {
    Main().main(args)
    exitProcess(0)
}

class Main : CliktCommand() {
    override fun run() {
        echo(Theme.Companion.Default.info(sayHello()))
    }
}
