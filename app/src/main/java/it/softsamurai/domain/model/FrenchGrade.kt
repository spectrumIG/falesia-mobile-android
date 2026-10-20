package it.softsamurai.domain.model

private val GRADE_PATTERN = Regex("""([1-9])([abcABC])?(\+)?""")
private const val LETTERS = "abc"
private const val STEPS_PER_LETTER = 2
private const val STEPS_PER_NUMBER = LETTERS.length * STEPS_PER_LETTER

/**
 * A French sport-climbing grade (`3a` … `9c+`), comparable by difficulty.
 *
 * falesiaonline.it writes grades as free text: [parse] reads the first grade it finds, so `6a/6a+` is `6a`,
 * `7b (proj)` is `7b` and `?` is not a grade. A number without letter (`4`) counts as its `a`.
 */
@JvmInline
value class FrenchGrade private constructor(private val ordinal: Int) : Comparable<FrenchGrade> {

    /** The number of the grade: 6 for `6b+`. */
    val number: Int get() = ordinal / STEPS_PER_NUMBER + 1

    override fun compareTo(other: FrenchGrade): Int = ordinal.compareTo(other.ordinal)

    override fun toString(): String {
        val letter = LETTERS[(ordinal % STEPS_PER_NUMBER) / STEPS_PER_LETTER]
        val plus = if (ordinal % STEPS_PER_LETTER == 1) "+" else ""
        return "$number$letter$plus"
    }

    companion object {
        fun parse(text: String): FrenchGrade? {
            val match = GRADE_PATTERN.find(text) ?: return null
            val (number, letter, plus) = match.destructured
            val letterIndex = if (letter.isEmpty()) 0 else LETTERS.indexOf(letter.lowercase())
            return FrenchGrade(
                (number.toInt() - 1) * STEPS_PER_NUMBER +
                        letterIndex * STEPS_PER_LETTER +
                        if (plus.isEmpty()) 0 else 1,
            )
        }
    }
}

/** Difficulty bands used to filter routes. */
enum class GradeBand {
    /** Up to `5c+`. */
    UpTo5,

    /** `6a` … `6c+`. */
    Six,

    /** `7a` … `7c+`. */
    Seven,

    /** `8a` and harder. */
    EightPlus;

    companion object {
        private const val SIX = 6
        private const val SEVEN = 7

        fun of(grade: FrenchGrade): GradeBand = when {
            grade.number < SIX -> UpTo5
            grade.number == SIX -> Six
            grade.number == SEVEN -> Seven
            else -> EightPlus
        }
    }
}
