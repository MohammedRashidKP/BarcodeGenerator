package barcodegenerator;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;

/**
 * FXML Controller class
 *
 * @author RashidKP
 */
public class MainViewController implements Initializable {

    @FXML
    private Button btn;
    @FXML
    private TextField price;
    @FXML
    private TextField totalCount;
    @FXML
    private TextField startIndex;
    @FXML
    private TextField productName;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        btn.setOnAction(e -> {
            try {
                code();
            } catch (IOException | DocumentException ex) {
                Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private void code() throws FileNotFoundException, IOException, BadElementException, DocumentException {

        File folder = new File(System.getProperty("user.dir"));
        File fList[] = folder.listFiles();

        for (File f : fList) {
            if (f.getName().endsWith(".png") || f.getName().endsWith(".pdf")) {
                f.delete();
            }
        }
        LocalDateTime localDateTime = LocalDateTime.now();
        String fileName = String.valueOf(localDateTime.toEpochSecond(ZoneOffset.UTC));
        Code128Bean code128 = new Code128Bean();
        code128.setHeight(15f);
        code128.setModuleWidth(0.3);
        code128.setQuietZone(10);
        code128.doQuietZone(true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapCanvasProvider canvas = new BitmapCanvasProvider(baos, "image/x-png", 400, BufferedImage.TYPE_BYTE_BINARY, false, 0);
        code128.generateBarcode(canvas, "MRP "+price.getText());
        canvas.finish();

        FileOutputStream fos = new FileOutputStream(fileName + ".png");
        fos.write(baos.toByteArray());
        fos.flush();
        fos.close();

        Image png = Image.getInstance(baos.toByteArray());
        png.setAbsolutePosition(0, 705);
        png.scalePercent(25);

        Document document;
        document = new Document();
        PdfPTable table = new PdfPTable(4);
        table.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
        int totalNumber = Integer.valueOf(totalCount.getText());
        int start = Integer.valueOf(startIndex.getText());
        int count = findFullColumnCount(totalNumber);
        
        
        for (int aw = 0; aw < totalNumber; aw++) {
            Font font = new Font(Font.FontFamily.TIMES_ROMAN, 9.0f, Font.NORMAL, BaseColor.BLACK);
            Chunk chunk = new Chunk("        "+productName.getText(), font);
            Paragraph p = new Paragraph(chunk);
            PdfPTable intable = new PdfPTable(1);
            intable.getDefaultCell().setBorder(PdfPCell.NO_BORDER);
            intable.addCell(p);
            intable.addCell(png);
            intable.getDefaultCell().setBorder(0);
            table.addCell(intable);
        }
//        Paragraph p = new Paragraph(productName.getText());
//        p.add(png);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fileName + ".pdf"));
        document.open();
        document.add(table);
        document.close();

        writer.close();

        if (Desktop.isDesktopSupported()) {
            try {
                File myFile = new File(fileName + ".pdf");
                Desktop.getDesktop().open(myFile);
            } catch (IOException ex) {
                // no application registered for PDFs
            }
        }
    }

    private int findFullColumnCount(int totalNumber) {
        int fullColumnCount = totalNumber;
        for(int i = fullColumnCount; i>0; i--){
            if(fullColumnCount%4 ==0){
                break;
                
            }
        }
        return fullColumnCount;
    }
}
