package ru.white.xml_parser_java.controller;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import ru.white.xml_parser_java.service.GraphService;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class GraphController {

    @FXML
    private Button but;

    @FXML
    private Pane main;

    @FXML
    public void initialize() {
        GraphService service = new GraphService();
//        main.getChildren().add(service.getGraph());

        but.setOnAction(actionEvent -> {
            createPDF();
        });

//        WebEngine webEngine = webView.getEngine();
//        Document document = Jsoup.parse(
//                "<html>" +
//                "  <body>" +
//                "   <div id='foo'>" +
//                "     <p id='bar'>TEST</p>" +
//                "   </div>" +
//                "  </body>" +
//                "</html>");


//        webView.getEngine().loadContent("<html>порадуй дядю</html>", "text/html");

//        String html = String.valueOf(this.getClass().getResource("/html/index.html"));
//        webEngine.load(document.toString());
//
//        File f = new File("full\\path\\to\\webView\\main.html");
//        webEngine.load(f.toURI().toString());
    }

    public void createPDF() {
        try (Document document = new Document()) {
            PdfWriter.getInstance(document, new FileOutputStream("C:\\Users\\Igor\\Desktop\\XMLTest\\Файл-хуяил.pdf"));
            document.open();

            Paragraph docTitle = new Paragraph();
            Font docTitleDateFont = new Font(null, 12, Font.BOLD);
            docTitle.setAlignment(Element.ALIGN_CENTER);
            docTitle.add(new Chunk("Ну вот твой файл", docTitleDateFont));
            document.add(docTitle);


            WritableImage writableImage = main.snapshot(new SnapshotParameters(), null);
            BufferedImage bImg2 = SwingFXUtils.fromFXImage(writableImage, new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB));

//            ImageIO.write(bImg2, "png", new File("chart.png"));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bImg2, "png", baos);
            Image iTextImage = Image.getInstance(baos.toByteArray());

//            Image image = Image.getInstance("C:\\Users\\Igor\\Desktop\\asd.png");
//            image.scaleAbsolute(600f, 300f); //image width,height
            document.add(iTextImage);


        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }
    }
}
