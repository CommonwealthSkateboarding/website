package utils;

import be.quodlibet.boxable.BaseTable;
import be.quodlibet.boxable.Cell;
import be.quodlibet.boxable.Row;
import models.skatepark.Camp;
import models.skatepark.Registration;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import play.Logger;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cdelargy on 11/29/14.
 */
public class PdfUtil {

    static String expectedPattern = "yyyy-MM-dd";
    static SimpleDateFormat formatter = new SimpleDateFormat(expectedPattern);

    public static ByteArrayOutputStream getCampPDF(Camp camp) {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        //Set margins
        float margin = 10;

        //Initialize Document
        PDDocument doc = new PDDocument();
        PDPage page = addNewPage(doc);
        float yStartNewPage = page.findMediaBox().getHeight() - (2 * margin);

        //Initialize table
        float tableWidth = page.findMediaBox().getWidth() - (2 * margin);
        boolean drawContent = true;
        float yStart = yStartNewPage;
        float bottomMargin = 70;

        try {
            BaseTable table  = new BaseTable(yStart,yStartNewPage, bottomMargin, tableWidth, margin, doc, page, true, drawContent);

            //Create Header row
            Row headerRow = table.createRow(32f);
            Cell cell = headerRow.createCell(100, camp.title + " (" + DateFormatUtils.format(camp.startDate, "MM/dd/yyyy") + "-" + DateFormatUtils.format(camp.endDate, "MM/dd/yyyy") + ")");
            cell.setFont(PDType1Font.HELVETICA_BOLD);
            cell.setFillColor(Color.BLACK);
            cell.setFontSize(20);
            cell.setTextColor(Color.WHITE);

            table.setHeader(headerRow);

            //Create Fact header row
            Row factHeaderrow = table.createRow(20f);

            cell = factHeaderrow.createCell(50, "Name");
            cell.setFont(PDType1Font.HELVETICA);
            cell.setFontSize(12);
            cell.setFillColor(Color.LIGHT_GRAY);

            cell = factHeaderrow.createCell(40, "Emergency Contact Info");
            cell.setFont(PDType1Font.HELVETICA);
            cell.setFontSize(12);
            cell.setFillColor(Color.LIGHT_GRAY);

            cell = factHeaderrow.createCell(10, "Paid?");
            cell.setFillColor(Color.LIGHT_GRAY);
            cell.setFont(PDType1Font.HELVETICA_OBLIQUE);
            cell.setFontSize(12);

            //Add multiple rows with random facts about Belgium
            for (Registration reg : camp.registrations) {

                Row row = table.createRow(20f);
                //name
                cell = row.createCell(50, reg.participantName);
                cell.setFont(PDType1Font.HELVETICA);
                cell.setFontSize(12);

                StringBuilder emergencyInfo = new StringBuilder();
                if (StringUtils.isNotEmpty(reg.emergencyContactName))
                    emergencyInfo.append(reg.emergencyContactName + " (" + reg.emergencyTelephone + ")");
                if (StringUtils.isNotEmpty(reg.alternateEmergencyContactName))
                    emergencyInfo.append(" | " + reg.alternateEmergencyContactName + " (" + reg.alternateEmergencyTelephone + ")");
                cell = row.createCell(40, emergencyInfo.toString());
                cell.setFont(PDType1Font.HELVETICA);
                cell.setFontSize(12);

                //phone
                cell = row.createCell(10, Formatter.prettyDollarsAndCents(reg.totalPaid));
                cell.setFont(PDType1Font.HELVETICA_OBLIQUE);
                cell.setFontSize(12);
                if (reg.getRemainingDue()>0) {
                    cell.setFillColor(Color.yellow);
                }
            }
            //Timestamp
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss a");
            Row daterow = table.createRow(20f);
            cell = daterow.createCell(100, "Generated on " + dateFormat.format(new Date()));
            cell.setFillColor(Color.LIGHT_GRAY);
            table.draw();
            doc.save(output);
            doc.close();
        } catch (IOException e) {
            Logger.error("Problem rendering PDF", e);
        } catch (COSVisitorException e) {
            Logger.error("Problem rendering PDF", e);
        }
        return output;
    }

    private static PDPage addNewPage(PDDocument doc) {
        PDPage page = new PDPage();
        doc.addPage(page);
        return page;
    }
}
