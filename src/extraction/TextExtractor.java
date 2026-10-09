package extraction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class TextExtractor {

    public static String extractFromTxt(Path filePath) throws IOException {
        if (filePath == null || !Files.exists(filePath)) {
            throw new IOException("TXT file not found.");
        }
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        }
        String extracted = content.toString();
        if (extracted == null || extracted.trim().isEmpty()) {
            throw new IOException("Selected file is empty.");
        }
        return extracted;
    }

    public static String extractFromPdf(Path filePath) throws IOException {
        if (filePath == null || !Files.exists(filePath)) {
            throw new IOException("PDF file not found.");
        }

        try (PDDocument document = Loader.loadPDF(filePath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            if (text == null || text.trim().isEmpty()) {
                throw new IOException("Unable to extract text from PDF.");
            }
            return text;
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("Unable to extract text from PDF.")) {
                throw e;
            }
            throw new IOException("Unable to extract text from PDF.", e);
        } catch (Exception e) {
            throw new IOException("Unable to extract text from PDF.", e);
        }
    }

    public static String extractText(Path filePath) throws IOException {
        if (filePath == null) {
            throw new IOException("Please select a valid file.");
        }

        String fileName = filePath.getFileName().toString();
        String lower = fileName.toLowerCase();

        if (lower.endsWith(".txt")) {
            return extractFromTxt(filePath);
        }
        if (lower.endsWith(".pdf")) {
            return extractFromPdf(filePath);
        }
        throw new IOException("Please select a TXT or PDF assignment.");
    }
}
