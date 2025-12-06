package data

import java.io.File
import java.util.concurrent.atomic.AtomicInteger

/**
 * Simple task data model for Week 7.
 *
 * **Week 7**: Editable title field
 * **Week 8 evolution**: Add `createdAt` timestamp for sorting
 */
data class Color(
    val Colorid: Int,
    var hex: String,
)

/**
 * In-memory repository with CSV persistence.
 *
 * **Week 7**: Added find() and update() methods for inline edit
 * **Week 10 evolution**: Refactor to class with UUID for production-readiness
 */
object ColorRepository {
    private val file = File("data/colors.csv")
    private val colors = mutableListOf<Color>()
    private val idCounter = AtomicInteger(1)

    init {
        file.parentFile?.mkdirs()
        if (!file.exists()) {
            file.writeText("Colorid,hex\n")
        } else {
            file.readLines().drop(1).forEach { line ->
                val parts = line.split(",", limit = 2)
                if (parts.size == 2) {
                    val id = parts[0].toIntOrNull() ?: return@forEach
                    colors.add(Color(id, parts[1]))
                    idCounter.set(maxOf(idCounter.get(), id + 1))
                }
            }
        }
    }

    fun all(): List<Color> = colors.toList()

    fun add(hex: String): Color {
        val color = Color(idCounter.getAndIncrement(), hex)
        colors.add(color)
        persist()
        return color
    }

    fun delete(Colorid: Int): Boolean {
        val removed = colors.removeIf { it.Colorid == Colorid }
        if (removed) persist()
        return removed
    }

    fun find(Colorid: Int): Color? = colors.find { it.Colorid == Colorid }

    fun update(color: Color) {
        colors.find { it.Colorid == color.Colorid }?.let { it.hex = color.hex }
        persist()
    }
    private fun persist() {
        file.writeText("Colorid,hex\n" + colors.joinToString("\n") { "${it.Colorid},${it.hex}" })
    }
}
