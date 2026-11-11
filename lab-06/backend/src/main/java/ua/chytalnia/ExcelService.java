package ua.chytalnia;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ExcelService {
    private static final List<String> HEADERS = List.of("Назва", "Автор", "Рік", "Категорія", "Мова", "Опис");
    private static final Set<String> LANGUAGES = Set.of("ukr", "eng", "other");
    private static final int MAX_ROWS = 2000;

    private final ArticleRepository articles;

    public ExcelService(ArticleRepository articles) {
        this.articles = articles;
        ZipSecureFile.setMaxEntrySize(16L * 1024 * 1024);
        ZipSecureFile.setMaxFileCount(256);
    }

    public int save(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (file.isEmpty() || filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            throw invalid("Виберіть непорожній файл у форматі .xlsx.");
        }
        List<Article> records = read(file);
        return articles.saveAll(records).size();
    }

    private List<Article> read(MultipartFile file) {
        DataFormatter formatter = new DataFormatter(Locale.ROOT);
        try (InputStream input = file.getInputStream(); XSSFWorkbook workbook = new XSSFWorkbook(input)) {
            if (workbook.getNumberOfSheets() == 0) {
                throw invalid("У файлі немає аркушів.");
            }
            Sheet sheet = workbook.getSheetAt(0);
            Row header = sheet.getRow(0);
            if (header == null || !values(header, formatter).equals(HEADERS)) {
                throw invalid("Заголовки мають бути в такому порядку: " + String.join(", ", HEADERS) + ".");
            }
            if (sheet.getLastRowNum() > MAX_ROWS) {
                throw invalid("Файл може містити до " + MAX_ROWS + " рядків після заголовка.");
            }
            List<Article> records = new ArrayList<>();
            for (Row row : sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }
                List<String> values = values(row, formatter);
                if (values.stream().allMatch(String::isEmpty)) {
                    continue;
                }
                records.add(article(row.getRowNum() + 1, values));
            }
            if (records.isEmpty()) {
                throw invalid("Додайте хоча б один рядок даних після заголовка.");
            }
            return records;
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (IOException | RuntimeException exception) {
            throw invalid("Не вдалося прочитати файл Excel. Перевірте, чи файл відкривається та відповідає шаблону.");
        }
    }

    private List<String> values(Row row, DataFormatter formatter) {
        List<String> values = new ArrayList<>(HEADERS.size());
        for (int column = 0; column < HEADERS.size(); column++) {
            Cell cell = row.getCell(column);
            if (cell != null && (cell.getCellType() == CellType.FORMULA || cell.getCellType() == CellType.ERROR)) {
                throw invalid("Рядок " + (row.getRowNum() + 1) + ", «" + HEADERS.get(column)
                    + "»: вставте значення без формул і помилок Excel.");
            }
            values.add(formatter.formatCellValue(cell).strip());
        }
        return values;
    }

    private Article article(int row, List<String> values) {
        String title = required(row, 0, values.get(0), 120);
        String author = required(row, 1, values.get(1), 80);
        String year = values.get(2);
        if (!year.matches("[1-9][0-9]{3}")) {
            throw invalid("Рядок " + row + ": рік має бути чотиризначним числом.");
        }
        String category = required(row, 3, values.get(3), 60);
        String language = values.get(4).toLowerCase(Locale.ROOT);
        if (!LANGUAGES.contains(language)) {
            throw invalid("Рядок " + row + ": вкажіть мову ukr, eng або other.");
        }
        String summary = required(row, 5, values.get(5), 1000);
        return new Article(new ArticleInput(title, author, summary), Integer.parseInt(year), category, language);
    }

    private String required(int row, int column, String value, int limit) {
        if (value.isEmpty() || value.length() > limit) {
            throw invalid("Рядок " + row + ", «" + HEADERS.get(column)
                + "»: введіть від 1 до " + limit + " символів.");
        }
        return value;
    }

    private ResponseStatusException invalid(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
