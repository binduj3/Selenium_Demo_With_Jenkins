package utils;

import org.apache.poi.ss.usermodel.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads a sheet into a list of rows; the first row is the header (column names).
 * No static workbook/sheet fields, so it's safe when scenarios run in parallel.
 */
public class ExcelReader {

    private static final DataFormatter FORMATTER = new DataFormatter();

    public static List<Map<String, String>> read(String filePath, String sheetName) {
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet '" + sheetName + "' not found in " + filePath);
            }

            Row header = sheet.getRow(sheet.getFirstRowNum());
            List<Map<String, String>> rows = new ArrayList<>();

            for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                Map<String, String> data = new LinkedHashMap<>();
                boolean empty = true;
                for (int c = 0; c < header.getLastCellNum(); c++) {
                    String key = FORMATTER.formatCellValue(header.getCell(c)).trim();
                    String value = FORMATTER.formatCellValue(row.getCell(c)).trim();
                    if (!value.isEmpty()) empty = false;
                    data.put(key, value);
                }
                if (!empty) rows.add(data);
            }
            return rows;

        } catch (IOException e) {
            throw new UncheckedIOException("Could not read Excel file: " + filePath, e);
        }
    }
}
