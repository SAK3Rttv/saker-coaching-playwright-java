package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.testng.annotations.DataProvider;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

public class ExcelUtil {

	private static final String EXCEL_PATH = "src/test/resources/LoginTestData.xlsx";

	public static Object[][] getDataFromExcel(String filePath, int sheetIndex)
			throws IOException, InvalidFormatException {
		FileInputStream inputStream = new FileInputStream(filePath);
		Workbook workbook = WorkbookFactory.create(inputStream);
		Sheet sheet = workbook.getSheetAt(sheetIndex);

		int rowCount = sheet.getLastRowNum();
		Object[][] data = new Object[rowCount][2];

		for (int i = 0; i < rowCount; i++) {
			Row row = sheet.getRow(i + 1);

			String email = getCellValue(row, 0);
			String password = getCellValue(row, 1);

			data[i] = new Object[] { email, password };
		}

		workbook.close();
		inputStream.close();

		return data;
	}
	
	@DataProvider(name = "loginDataProvider")
	public static Object[][] loginDataProvider() throws IOException, InvalidFormatException {
		List<Object[]> rows = new ArrayList<>();
		try (FileInputStream fis = new FileInputStream(EXCEL_PATH); Workbook workbook = WorkbookFactory.create(fis)) {
			Sheet sheet = workbook.getSheetAt(0);
			int lastRow = sheet.getLastRowNum();

//			for (int i = 1; i <= lastRow; i++) {
			for (int i = 1; i < lastRow; i++) {
				Row row = sheet.getRow(i);
				if (row == null)
					continue;

				String testCaseId = cell(row, 0);
				String category = cell(row, 1);
				String description = cell(row, 2);
				String email = cell(row, 3);
				String password = cell(row, 4);
				boolean rememberMe = "yes".equalsIgnoreCase(cell(row, 5));
				String expected = cell(row, 6);
				String errorHint = cell(row, 7);
				String notes = cell(row, 8);

				rows.add(new Object[] { testCaseId, category, description, email, password, rememberMe, expected,
						errorHint, notes });
			}
		}
		return rows.toArray(new Object[0][]);
	}

	private static String getCellValue(Row row, int cellIndex) {
		if (row == null)
			return "";
		Cell cell = row.getCell(cellIndex);
		if (cell == null)
			return "";
		return cell.getStringCellValue().trim();
	}
	
	private static String cell(Row row, int col) {
		Cell c = row.getCell(col);
		if (c == null) return "";
		switch (c.getCellType()) {
		case STRING: return c.getStringCellValue().trim();
		case NUMERIC: return String.valueOf((long) c.getNumericCellValue());
		case BOOLEAN: return String.valueOf(c.getBooleanCellValue());
		default: return "";
		}
	}
}
