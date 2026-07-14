package dev.schuberth.template.lib

import java.lang.invoke.MethodHandles

import org.apache.logging.log4j.kotlin.loggerOf

private val logger = loggerOf(MethodHandles.lookup().lookupClass())

@Suppress("FunctionOnlyReturningConstant")
fun sayHello(): String {
    val message = "Hello world!"

    // Use a log level with high priority for demo purposes.
    logger.error { message }

    return message
}
