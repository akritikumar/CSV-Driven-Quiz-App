package quiz;

/**
 * Model class representing one multiple-choice quiz question.
 *
 * Expected CSV format:
 * Question,OptionA,OptionB,OptionC,OptionD,CorrectAnswer
 *
 * CorrectAnswer may be:
 *  - A single letter: "A", "B", "C", or "D" (case-insensitive), OR
 *  - The exact option text (case-insensitive, trimmed) that matches one of the options.
 */
public class QuizQuestion {
    private final String question;
    private final String[] options; // length 4
    private final String correctAnswerRaw; // as read from CSV (could be "A" or option text)

    public QuizQuestion(String question, String[] options, String correctAnswerRaw) {
        this.question = question == null ? "" : question.trim();
        this.options = new String[4];
        for (int i = 0; i < 4; i++) {
            if (options != null && i < options.length && options[i] != null) {
                this.options[i] = options[i].trim();
            } else {
                this.options[i] = "";
            }
        }
        this.correctAnswerRaw = correctAnswerRaw == null ? "" : correctAnswerRaw.trim();
    }

    public String getQuestion() {
        return question;
    }

    /**
     * Returns a copy of the options array to avoid external mutation.
     */
    public String[] getOptions() {
        return options.clone();
    }

    /**
     * Returns the raw value found in the CSV for the correct answer.
     */
    public String getCorrectAnswerRaw() {
        return correctAnswerRaw;
    }

    /**
     * Returns a normalized representation of the correct answer:
     * - If CSV used a letter (A-D), this returns the normalized option text that corresponds to that letter.
     * - Otherwise returns normalized(correctAnswerRaw).
     *
     * Normalization: trimmed, collapse whitespace, lower-case.
     */
    public String getCorrectAnswerNormalized() {
        String ca = correctAnswerRaw == null ? "" : correctAnswerRaw.trim();
        if (ca.matches("(?i)^[ABCD]$")) {
            int idx = letterToIndex(ca.charAt(0));
            if (idx >= 0 && idx < options.length) {
                return normalize(options[idx]);
            }
        }
        return normalize(ca);
    }

    /**
     * Returns true if the provided selectedOptionText matches the correct answer.
     * Accepts either:
     *  - the option text (full or normalized), or
     *  - the letter A/B/C/D (case-insensitive) if the UI passes that
     */
    public boolean isCorrect(String selectedOptionText) {
        if (selectedOptionText == null) return false;
        String sel = selectedOptionText.trim();

        // If CSV correct answer was a letter, compare by index.
        String ca = correctAnswerRaw == null ? "" : correctAnswerRaw.trim();
        if (ca.matches("(?i)^[ABCD]$")) {
            int correctIdx = letterToIndex(ca.charAt(0));
            // If caller passed the letter too, accept it.
            if (sel.matches("(?i)^[ABCD]$")) {
                return letterToIndex(sel.charAt(0)) == correctIdx;
            }
            // Otherwise compare selected text normalized with the correct option text.
            if (correctIdx >= 0 && correctIdx < options.length) {
                return normalize(options[correctIdx]).equals(normalize(sel));
            }
            return false;
        }

        // If CSV correct answer was option text (not letter), compare normalized texts.
        return normalize(ca).equals(normalize(sel));
    }

    // --------------------
    // Utility helpers
    // --------------------

    private static int letterToIndex(char c) {
        char up = Character.toUpperCase(c);
        if (up >= 'A' && up <= 'D') return up - 'A';
        return -1;
    }

    private static String normalize(String s) {
        if (s == null) return "";
        // collapse internal whitespace to single space, trim, lowercase
        return s.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    @Override
    public String toString() {
        return "QuizQuestion{" +
                "question='" + question + '\'' +
                ", options=[" + options[0] + ", " + options[1] + ", " + options[2] + ", " + options[3] + "]" +
                ", correctAnswerRaw='" + correctAnswerRaw + '\'' +
                '}';
    }
}
