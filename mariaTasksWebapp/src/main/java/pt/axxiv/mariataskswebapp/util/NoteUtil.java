package pt.axxiv.mariataskswebapp.util;

public class NoteUtil {
    public static String convertToText(String html) {
        if (html == null || html.isEmpty()) return "";

        html = html.replace("\r", "");

        if (!html.contains("<")) {
            return html;
        }

        StringBuilder sb = new StringBuilder();
        int i = 0;

        while (i < html.length()) {

            if (html.startsWith("<br>", i)) {
                int brCount = 0;

                while (html.startsWith("<br>", i)) {
                    brCount++;
                    i += 4;
                }

                for (int j = 0; j < brCount; j++) {
                    sb.append("\n");
                }

                continue;
            }

            if (html.startsWith("<li>", i)) {
                i += 4;
                int end = html.indexOf("</li>", i);
                if (end == -1) break;

                String item = html.substring(i, end).trim();
                sb.append("* ")
                        .append(stripTags(item))
                        .append("\n");

                i = end + 5;
                continue;
            }

            if (html.startsWith("<p>", i)) {
                i += 3;
                int end = html.indexOf("</p>", i);
                if (end == -1) break;

                String text = html.substring(i, end).trim();
                sb.append(stripTags(text)).append("\n");

                i = end + 4;
                continue;
            }

            i++;
        }

        return sb.toString().trim();
    }

    private static String stripTags(String input) {
        return input.replaceAll("<[^>]*>", "").trim();
    }

    public static String convertToHtml(String input) {
        if (input == null || input.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        boolean inList = false;

        int blankCount = 0;

        for (String rawLine : input.split("\\n")) {
            String line = rawLine.trim();

            if (line.isEmpty()) {
                blankCount++;

                if (inList) {
                    sb.append("</ul>");
                    inList = false;
                }

                continue;
            }

            if (blankCount > 0) {
                for (int i = 0; i < blankCount; i++) {
                    sb.append("<br>");
                }
                blankCount = 0;
            }

            if (line.startsWith("* ")) {
                if (!inList) {
                    sb.append("<ul>");
                    inList = true;
                }

                sb.append("<li>")
                        .append(line.substring(2).trim())
                        .append("</li>");
            } else {
                if (inList) {
                    sb.append("</ul>");
                    inList = false;
                }

                sb.append("<p>")
                        .append(line)
                        .append("</p>");
            }
        }

        // close list if still open
        if (inList) {
            sb.append("</ul>");
        }

        // trailing blank lines
        for (int i = 0; i < blankCount; i++) {
            sb.append("<br>");
        }

        return sb.toString();
    }
}
