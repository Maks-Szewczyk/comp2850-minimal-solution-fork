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
    val hexVal = receiveParameters()["hex"]?.take(7) ?: run {
        println("Color Not recieved")
        return respond(HttpStatusCode.BadRequest)
    }
    val color = Color(hex = hexVal)
    store.add(color)

    if (isHtmxRequest()) {
        println(hexVal)
        respondText(
            """
            <style id="color-theme" hx-swap-oob ="true">
                :root{
                --bg-color: $hexVal;
                --article-color: $hexVal;
                }
                body{
                background-color: var(--bg-color);
                }
                article{
                background-color: var(--article-color);
                }
            </style>
            """.trimIndent(),ContentType.Text.Html
        )
        println("color changed")
    } else {
        respondRedirect("/tasks")
    }
}
