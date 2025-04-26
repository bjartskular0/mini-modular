package dev.akerstrom.app

import io.ktor.server.application.*
import io.ktor.server.routing.*

class PluginsRoutingNode(
    parent: RoutingNode?,
    selector: RouteSelector,
    developmentMode: Boolean = false,
    environment: ApplicationEnvironment
) : RoutingNode(parent, selector, developmentMode, environment) {


    fun removeRoute(selector: RouteSelector) {
        val existingEntry = (children as MutableList).removeIf { it.selector == selector }
    }
}

fun RoutingNode.removeRoute(selector: RouteSelector): RoutingNode? {
    (children as MutableList).let { children ->
        val existingEntry = children.firstOrNull { it.selector == selector }
        existingEntry?.let(children::remove)
        return existingEntry
    }
}