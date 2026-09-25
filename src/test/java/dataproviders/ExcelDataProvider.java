package dataproviders;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.testng.annotations.DataProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Single Excel-backed data provider class for the whole suite, with a single
 * read method shared by every sheet: each row becomes one Map<String,String>
 * keyed by header cell text, wrapped as a 1-element Object[] row so TestNG
 * hands the whole row to the test method as one argument (e.g.
 * row.get("FirstName"), row.get("CartItemSlugs")).
 *
 * readSheet takes both the workbook file name and the sheet name, not just
 * the sheet name - all test data currently lives in one workbook
 * (TestData.xlsx), but this keeps the method reusable as-is for a future
 * second workbook (e.g. readSheet("RegressionData.xlsx", "Products"))
 * without touching a hardcoded file constant.
 */
public class ExcelDataProvider {

    private static final Logger log = LogManager.getLogger(ExcelDataProvider.class);
    private static final String TEST_DATA_DIR = "testdata/";

    @DataProvider(name = "loginData")
    public static Object[][] getLoginData() {
        return readSheet("TestData.xlsx", "Login");
    }

    @DataProvider(name = "checkoutData")
    public static Object[][] getCheckoutData() {
        return readSheet("TestData.xlsx", "Checkout");
    }

    /** Each row -> a single Map<String,String> keyed by header text, wrapped as a 1-element Object[] row. */
    private static Object[][] readSheet(String fileName, String sheetName) {
        String classpathPath = TEST_DATA_DIR + fileName;

        try (InputStream is = ExcelDataProvider.class.getClassLoader().getResourceAsStream(classpathPath);
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new RuntimeException("Sheet '" + sheetName + "' not found in " + classpathPath);
            }

            int lastRow = sheet.getLastRowNum();
            int columnCount = sheet.getRow(0).getLastCellNum();

            DataFormatter formatter = new DataFormatter();
            String[] headers = new String[columnCount];
            Row headerRow = sheet.getRow(0);
            for (int colIdx = 0; colIdx < columnCount; colIdx++) {
                headers[colIdx] = formatter.formatCellValue(headerRow.getCell(colIdx));
            }

            Object[][] data = new Object[lastRow][1]; // rows 1..lastRow (row 0 is the header); each row = {Map<String,String>}
            for (int rowIdx = 1; rowIdx <= lastRow; rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                Map<String, String> rowMap = new LinkedHashMap<>();
                for (int colIdx = 0; colIdx < columnCount; colIdx++) {
                    Cell cell = row.getCell(colIdx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    rowMap.put(headers[colIdx], formatter.formatCellValue(cell));
                }
                data[rowIdx - 1][0] = rowMap;
            }
            log.info("Loaded {} rows ({} columns each) from sheet '{}' in {}",
                    data.length, columnCount, sheetName, classpathPath);
            return data;

        } catch (IOException e) {
            log.error("Failed to read sheet '{}' from {}", sheetName, classpathPath, e);
            throw new RuntimeException("Failed to read sheet '" + sheetName + "' from " + classpathPath, e);
        }
    }
}
