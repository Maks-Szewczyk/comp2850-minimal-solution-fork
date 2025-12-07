package storage

import model.Color
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * CSV-based task storage.
 * Provides simple persistence for task data using local CSV file.
 *
 * **Privacy note**: Data stored locally only. No cloud services.
 * **Thread safety**: Not thread-safe. Use single-threaded server or add locking.
 *
 * **CSV schema**:
 * ```
 * id,title,completed,created_at
 * 7a9f2c3d-...,\"Buy groceries\",false,2025-10-15T14:32:10
 * ```
 *
 * @property csvFile CSV file path (default: data/tasks.csv)
 */
class ColorStore(
    private val csvFile: File = File("data/colors.csv"),
) {
    companion object helpers{
        private val CSV_FORMAT =
            CSVFormat.DEFAULT
                .builder()
                .setHeader("Colorid", "hex")
                .setSkipHeaderRecord(true)
                .build()

        private const val EMPTY_FILE_SIZE = 0L
    }

    init {
        // Create data directory and file if missing
        csvFile.parentFile?.mkdirs()
        if (!csvFile.exists()) {
            csvFile.createNewFile()
        }

        // Write CSV header if file is empty (for new files or test cases)
        if (csvFile.length() == EMPTY_FILE_SIZE) {
            FileWriter(csvFile).use { writer ->
                CSVPrinter(writer, CSV_FORMAT).use { printer ->
                    printer.printRecord("Colorid", "hex")
                }
            }
        }
    }

    /**
     * Get all tasks from storage.
     *
     * @return List of tasks (empty if no tasks stored)
     */
    fun getAll(): List<Color> {
        if (!csvFile.exists() || csvFile.length() == EMPTY_FILE_SIZE) return emptyList()

        return FileReader(csvFile).use { reader ->
            CSVParser(reader, CSV_FORMAT).use { parser ->
                parser.mapNotNull { record ->
                    try {
                        Color(
                            Colorid = record[0],
                            hex = record[1]
                        )
                    } catch (e: IndexOutOfBoundsException) {
                        // CSV row has missing fields - skip this row
                        System.err.println("Warning: Skipping CSV row with missing fields: ${record.toList()}")
                        null
                    } catch (e: DateTimeParseException) {
                        // Date format is invalid - skip this row
                        System.err.println("Warning: Skipping CSV row with invalid date: ${record.toList()}")
                        null
                    } catch (e: IllegalArgumentException) {
                        // Boolean parsing failed or other validation issue - skip this row
                        System.err.println("Warning: Skipping CSV row with invalid data: ${record.toList()}")
                        null
                    }
                }
            }
        }
    }

    /**
     * Get task by ID.
     *
     * @param Colorid Task ID
     * @return Task if found, null otherwise
     */
    fun getById(Colorid: String): Color? = getAll().find { it.Colorid == Colorid }

    /**
     * Add new task to storage.
     *
     * **Note**: Does not check for duplicate IDs. Caller should ensure uniqueness.
     *
     * @param color Task to add
     */
    fun add(color: Color) {
        writeAll(listOf(color))
        // Ensure CSV file exists with header row
        /* 
        if (!csvFile.exists() || csvFile.length() == EMPTY_FILE_SIZE) {
            csvFile.parentFile?.mkdirs()
        }
        */

        /* 
        FileWriter(csvFile, true).use { writer ->
            CSVPrinter(writer, CSV_FORMAT).use { printer ->
                printer.printRecord(
                    color.Colorid,
                    color.hex
                )
            }
        }
        */
    }

    /**
     * Update existing task.
     * Replaces entire task with new data.
     *
     * **Implementation**: Reads all, replaces matching task, writes all.
     * Not efficient for large datasets, but simple and correct.
     *
     * @param color Task with updated data (ID must match existing task)
     * @return true if task found and updated, false if not found
     */
    fun update(color: Color): Boolean {
        val colors = getAll().toMutableList()
        val index = colors.indexOfFirst { it.Colorid == color.Colorid }

        if (index == -1) return false

        colors[index] = color
        writeAll(colors)
        return true
    }

    /**
     * Delete task by ID.
     *
     * @param Colorid Task ID to delete
     * @return true if task found and deleted, false if not found
     */
    fun delete(Colorid: String): Boolean {
        val tasks = getAll().toMutableList()
        val removed = tasks.removeIf { it.Colorid == Colorid }

        if (removed) {
            writeAll(tasks)
        }

        return removed
    }

    /**
     * Write all tasks to CSV file (overwrites existing file).
     * Used by update() and delete() after modifying task list.
     *
     * @param colors List of tasks to write
     */
    private fun writeAll(colors: List<Color>) {
        FileWriter(csvFile, false).use { writer ->
            CSVPrinter(writer, CSV_FORMAT).use { printer ->
                printer.printRecord("Colorid", "hex")
                colors.forEach { color ->
                    printer.printRecord(
                        color.Colorid,
                        color.hex,
                    )
                }
            }
        }
    }

    /**
     * Clear all tasks (for testing).
     * Deletes CSV file and recreates with header only.
     */
    fun clear() {
        csvFile.delete()
        csvFile.createNewFile()
        FileWriter(csvFile).use { writer ->
            CSVPrinter(writer, CSV_FORMAT).use { printer ->
                printer.printRecord("Colorid", "hex")
            }
        }
    }
}
