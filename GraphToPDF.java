package sample;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;


public class GraphToPDF {
    private int graphCount=1;
    public GraphToPDF() {
    }


    public boolean generatePDF(String paths[], String comments[], String[] titles, boolean flag, String pdfPath) throws IOException {
        try {
            PDDocument doc = new PDDocument();
            for (int i = 0; i < paths.length; i += 2) {
                String[] doubleTitle = new String[2];
                String[] doubleInput = new String[2];
                doubleTitle[0] = generateTitle(titles[i]);
                doubleInput[0] = paths[i];
                if (i + 1 < paths.length) {
                    doubleTitle[1] = generateTitle(titles[i + 1]);
                    doubleInput[1] = paths[i + 1];
                } else {
                    doubleTitle[1] = "";
                    doubleInput[1] = "";
                }
                addImageAsNewPage(doc, doubleInput, doubleTitle);
            }
            if (flag) {
                showComments(comments, doc);
            }
            doc.save(pdfPath);
            graphCount=1;
            return true;
        }
        catch (Exception e){
            System.out.println(e);
            graphCount=1;
            return false;
        }
    }

    private void showComments(String[] comments, PDDocument doc) {
        PDRectangle pageSize = PDRectangle.A4;
        PDPage page = new PDPage(pageSize);
        doc.addPage(page);
        try (PDPageContentStream contents = new PDPageContentStream(doc, page)) {
            contents.beginText();
            contents.setFont(PDType1Font.HELVETICA_BOLD, 15);
            contents.newLineAtOffset(75, 700);
            String text = generateTitle("Written Comments");
            contents.showText(text);
            contents.endText();
            //Begin the Content stream
            ArrayList<String> wrT = new ArrayList<>();
            String s;
            for (int i = 0; i < comments.length; i++) {
                String comment = (i + 1) + " - " + comments[i].replace("ö", "o").replace("ü", "u").replace("ı", "i").replace("ç", "c").replace("ğ", "g").replace("ş", "s").replace("[", "").replace("]", "").replace("\n", "").replace("\r", "");

                int size = comment.length();
                if (size < 100) {
                    wrT.add(comment);
                    continue;
                }
                int j = 0;
                while (size != 0) {
                    int endBound = (j + 1) * 100;
                    if (endBound >= comment.length()) {
                        endBound = comment.length();
                    }
                    wrT.add(comment.substring(j * 100, endBound));
                    size -= endBound - (j * 100);
                    j += 1;
                }
            }
            for (int i = 0; i < wrT.size(); i++) {
                contents.beginText();
                contents.setFont(PDType1Font.HELVETICA, 10);
                contents.newLineAtOffset(75, 600 - i * 30);
                s = wrT.get(i);
                contents.showText(s);
                contents.endText();
            }

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void addImageAsNewPage(PDDocument doc, String[] imagePaths, String[] sectionTitles) {
        PDImageXObject image2 =null;
        try {
            PDImageXObject image1 = PDImageXObject.createFromFile(imagePaths[0], doc);
            if (!imagePaths[1].equals("")) {
                image2 = PDImageXObject.createFromFile(imagePaths[1], doc);
            }
            PDRectangle pageSize = PDRectangle.A4;

            int originalWidth1 = image1.getWidth();
            int originalHeight1 = image1.getHeight();
            float pageWidth1 = pageSize.getWidth();
            float pageHeight1 = pageSize.getHeight();
            float ratio1 = Math.min(pageWidth1 / originalWidth1, pageHeight1 / originalHeight1) / 1.5f;
            float scaledWidth1 = originalWidth1 * ratio1;
            float scaledHeight1 = originalHeight1 * ratio1;
            float x1 = (pageWidth1 - scaledWidth1) / 2;
            float y1 = (pageHeight1 - scaledHeight1) / 1.25f;

            int originalWidth2 = 0; int originalHeight2 = 0;
            float pageWidth2 =0; float pageHeight2=0;
            float ratio2=0; float scaledWidth2=0;
            float scaledHeight2=0;
            float x2=0; float y2=0;

            if (!imagePaths[1].equals("")) {
                originalWidth2 = image2.getWidth();
                originalHeight2 = image2.getHeight();
                pageWidth2 = pageSize.getWidth();
                pageHeight2 = pageSize.getHeight();
                ratio2 = Math.min(pageWidth2 / originalWidth2, pageHeight2 / originalHeight2) / 1.5f;
                scaledWidth2 = originalWidth2 * ratio2;
                scaledHeight2 = originalHeight2 * ratio2;
                x2 = (pageWidth2 - scaledWidth2) / 2;
                y2 = (pageHeight2 - scaledHeight2) / 1.25f;
            }
            PDPage page = new PDPage(pageSize);
            doc.addPage(page);
            try (PDPageContentStream contents = new PDPageContentStream(doc, page)) {
                contents.drawImage(image1, x1, y1, scaledWidth1, scaledHeight1);
                contents.beginText();
                contents.newLineAtOffset(x1, y1 * 1.7f);
                contents.setFont(PDType1Font.HELVETICA_BOLD, 15);
                contents.showText("Graph "+graphCount+": "+sectionTitles[0]);
                contents.endText();
                graphCount++;
                if (!sectionTitles[1].equals("") || !imagePaths[1].equals("")) {
                    contents.drawImage(image2, x2, y2 / 8, scaledWidth2, scaledHeight2);
                    contents.beginText();
                    contents.newLineAtOffset(x2, y2 / 1.2f);
                    contents.setFont(PDType1Font.HELVETICA_BOLD, 15);
                    contents.showText("Graph "+graphCount+": "+sectionTitles[1]);
                    contents.endText();
                    graphCount++;
                }

            }
            System.out.println("Added: " + imagePaths[0]);
            try {
                System.out.println("Added: " + imagePaths[1]);
            } catch (Exception e){
                System.out.println();
            }
        } catch (IOException e) {
            System.err.println("Failed to process: " + imagePaths[0]);
            e.printStackTrace(System.err);
        }
    }

    private String generateTitle(String input) {
        if (input.substring(0, 6).equals("graphm")) {
            if (input.substring(0, 7).equals("graphm1")) return "Sections' Score Bar Chart";
            else if (input.substring(0, 7).equals("graphm2")) return "Selected Section's Score Bar Chart";
            else if (input.substring(0, 7).equals("graphm3")) return "Selected Subsection's Bar Chart";
            else return input;
        } else if (input.substring(0, 5).equals("graph")) {
            if (input.equals("graph1")) return "Student Participation Pie Chart";
            else if (input.equals("graph2")) return "Sections Scores Histogram";
            else if (input.substring(0, 6).equals("graph3")) return "Section Distributions";
            else if (input.equals("graph4")) return "Section Averages As Radar Chart";
            else if (input.equals("graph5")) return "Learning Outcomes As Radar Chart";
            else if (input.equals("graph6")) return "Subsection Averages As Radar";
            else if (input.substring(0, 6).equals("graph7")) return "Sections Score Histogram";
            else return input;
        } else if (input.equals("writtencomments")) {
            return "Written Comments";
        } else return input;

    }

}