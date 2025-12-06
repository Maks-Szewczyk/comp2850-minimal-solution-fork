package routes

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.encodeURLParameter
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import isHtmxRequest
import model.Task
import model.Color
import model.ValidationResult
import model.HexValidationResult
import renderTemplate
import storage.TaskStore
import storage.ColorStore
import utils.Page
import utils.jsMode
import utils.logValidationError
import utils.timed

private const val PAGE_SIZE = 10

fun Routing.configureColorRoutes(store: ColorStore = ColorStore()) {
    post("/tasks/colors/select") { call.handleColor(store) }
}

/**
 * Week 8: Handle paginated task list view with HTMX fragment support.
 */
private suspend fun ApplicationCall.handleColor(store: ColorStore) {
    val hexVal = receiveParameters()["hex"]?: run {
        println("Color Not recieved")
        return respond(HttpStatusCode.BadRequest)
    }
    val color = Color(hex = hexVal)
    store.add(color)

    if (isHtmxRequest()) {
        respondText("""<div id="status" hx-swap-oob="true">Color updated to $hexVal</div>""")
    } else {
        respondRedirect("/tasks")
    }
}
