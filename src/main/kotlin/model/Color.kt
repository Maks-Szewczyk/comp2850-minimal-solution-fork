package model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Color data model.
 * Represents a single todo item in the task manager.
 *
 * **Privacy note**: No PII stored. Tasks are anonymous and associated
 * only with session IDs (also anonymous).
 *
 * @property Colorid Unique identifier (UUID format)
 * @property hex Color hex (3-100 characters, validated)
 */
data class Color(
    val Colorid: String = UUID.randomUUID().toString(),
    val hex: String
) {
    companion object {
        /**
         * Minimum allowed length for task title.
         */
        const val MIN_TITLE_LENGTH = 3

        /**
         * Maximum allowed length for task title.
         */
        const val MAX_TITLE_LENGTH = 100

        /**
         * Validate task title against business rules.
         *
         * **WCAG 2.2 AA compliance**:
         * - Clear, specific error messages (3.3.1)
         * - Errors should be linked to inputs via aria-describedby (3.3.1)
         *
         * **Rules**:
         * - Required (cannot be blank)
         * - Minimum 3 characters
         * - Maximum 100 characters
         *
         * @param hex Title to validate
         * @return HexValidationResult.Success or HexValidationResult.Error(message)
         */
        fun validate(hex: String): HexValidationResult =
            when {
                hex.isBlank() ->
                    HexValidationResult.Error("Title is required. Please enter a task description.")

                hex.length < MIN_TITLE_LENGTH ->
                    HexValidationResult.Error(
                        "Title must be at least $MIN_TITLE_LENGTH characters. Currently: ${hex.length} characters.",
                    )

                hex.length > MAX_TITLE_LENGTH ->
                    HexValidationResult.Error(
                        "Title must be less than $MAX_TITLE_LENGTH characters. Currently: ${hex.length} characters.",
                    )

                else -> HexValidationResult.Success
            }
    }

    /**
     * Convert task to CSV row format.
     * Used by TaskStore for persistence.
     *
     * **CSV escaping**:
     * - Title is quoted
     * - Double quotes within title are escaped as ""
     *
     * @return CSV row string (no trailing newline)
     */
    fun toCSV(): String {
        val escapedTitle = hex.replace("\"", "\"\"")
        return "$Colorid,\"$escapedTitle\""
    }

    /**
     * Convert task to Pebble template context map.
     * Used when rendering templates.
     *
     * **Template usage**:
     * ```pebble
     * <li id="task-{{ task.id }}">
     *   <span>{{ task.title }}</span>
     *   <time datetime="{{ task.createdAtISO }}">{{ task.createdAt }}</time>
     * </li>
     * ```
     *
     * @return Map suitable for Pebble template context
     */
    fun toPebbleContext(): Map<String, Any> =
        mapOf(
            "Colorid" to Colorid,
            "hex" to hex,
        )
}

/**
 * Validation result for task operations.
 * Sealed class ensures exhaustive when() expressions.
 */
sealed class HexValidationResult {
    /**
     * Validation passed, operation can proceed.
     */
    data object Success : HexValidationResult()

    /**
     * Validation failed with specific error message.
     *
     * @property message Human-readable error for display to person using system
     */
    data class Error(
        val message: String,
    ) : HexValidationResult()
}
