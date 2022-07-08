package id.myevent.helper;

import id.myevent.model.dao.EventGuestDao;
import id.myevent.model.dao.ParticipantDao;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Excel Helper.
 */
public class ExcelHelper {
  public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

  /**
   * Data Participant to excel.
   */
  public static ByteArrayInputStream downloadExcel(List<ParticipantDao> participants) {
    String[] HEADERs = {"No", "Name", "Email", "Phone Number", "Status"};
    String SHEET = "Participants";

    try (Workbook workbook = new XSSFWorkbook();
         ByteArrayOutputStream out = new ByteArrayOutputStream();) {
      Sheet sheet = workbook.createSheet(SHEET);
      // Header
      sheet.setColumnWidth(0, 1000);
      sheet.setColumnWidth(1, 5000);
      sheet.setColumnWidth(2, 7000);
      sheet.setColumnWidth(3, 5000);
      sheet.setColumnWidth(4, 7000);
      Row headerRow = sheet.createRow(0);
      for (int col = 0; col < HEADERs.length; col++) {
        Cell cell = headerRow.createCell(col);
        cell.setCellValue(HEADERs[col]);
      }
      int rowIdx = 1;
      for (ParticipantDao participant : participants) {
        Row row = sheet.createRow(rowIdx++);
        row.createCell(0).setCellValue(rowIdx - 1);
        row.createCell(1).setCellValue(participant.getName());
        row.createCell(2).setCellValue(participant.getEmail());
        row.createCell(3).setCellValue(participant.getPhoneNumber());
        row.createCell(4).setCellValue(participant.getStatus());
      }
      workbook.write(out);
      return new ByteArrayInputStream(out.toByteArray());
    } catch (IOException e) {
      throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
    }
  }
}
