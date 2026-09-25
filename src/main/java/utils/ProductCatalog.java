package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads the "Products" sheet of TestData.xlsx once and caches it - this is
 * catalog/reference data (product slug -> display name + price), not a
 * per-test-case DataProvider. CartTest and CheckoutTest read from here instead
 * of hardcoding "Sauce Labs Backpack" / 29.99 as Java string/double literals,
 * so a price change on the site only needs a one-line edit in the sheet.
 */
public class ProductCatalog {

    public record Product(String slug, String name, double price) {}

    private static final Logger log = LogManager.getLogger(ProductCatalog.class);
    private static final String FILE_NAME = "testdata/TestData.xlsx";
    private static final String SHEET_NAME = "Products";
    private static Map<String, Product> catalog;

    public static synchronized Map<String, Product> load() {
        if (catalog != null) return catalog;

        Map<String, Product> result = new LinkedHashMap<>();
        try (InputStream is = ProductCatalog.class.getClassLoader().getResourceAsStream(FILE_NAME);
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheet(SHEET_NAME);
            if (sheet == null) {
                throw new RuntimeException("Sheet '" + SHEET_NAME + "' not found in " + FILE_NAME);
            }

            DataFormatter formatter = new DataFormatter();
            for (int rowIdx = 1; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row == null) continue;
                String slug = formatter.formatCellValue(row.getCell(0));
                String name = formatter.formatCellValue(row.getCell(1));
                double price = Double.parseDouble(formatter.formatCellValue(row.getCell(2)));
                result.put(slug, new Product(slug, name, price));
            }
            log.info("Loaded {} products from {}", result.size(), FILE_NAME);
        } catch (IOException e) {
            log.error("Failed to read {}", FILE_NAME, e);
            throw new RuntimeException("Failed to read " + FILE_NAME, e);
        }

        catalog = result;
        return catalog;
    }

    public static Product get(String slug) {
        Product product = load().get(slug);
        if (product == null) {
            throw new IllegalArgumentException("No product found in Products sheet for slug: " + slug);
        }
        return product;
    }
}
