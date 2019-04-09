package sample;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Survey {

    public String FILE_NAME;
    private FileInputStream excelFile;
    private Workbook survey;
    public String courseCode;
    public int studentCount;
    public int year;
    public int ansCount;
    public double participationRate;
    public String sectionNo;
    public String instructor;
    public ArrayList<Section> sections = new ArrayList<>();
    public int sectionCount=0;
    public int term;

    public Survey() {
    }

    public Survey(String FILE_NAME) throws IOException {

        this.FILE_NAME = FILE_NAME;
        this.excelFile = new FileInputStream(new File(FILE_NAME));
        survey = new XSSFWorkbook(this.excelFile);
        Sheet page = survey.getSheetAt(0); //Selecting spreadsheet
        Iterator<Row> iterator = page.iterator();


        int i=0;


        courseCode = page.getRow(3).getCell(1).getStringCellValue();
        studentCount = ((int) page.getRow(2).getCell(3).getNumericCellValue());
        ansCount = ((int) page.getRow(3).getCell(3).getNumericCellValue());
        participationRate = ((double) ansCount / studentCount) * 100;
        instructor=page.getRow(2).getCell(1).getStringCellValue();

        while(i<courseCode.length()) {
            if (courseCode.charAt(i) == '.') {
                sectionNo = courseCode.substring(i + 1);
                courseCode = courseCode.substring(0, i);
                break;
            }
            i += 1;
        }

        i=0;

        //Reading the survey data
        while(iterator.hasNext()){
            iterator.next();

            if (i > 4) {

                if (page.getRow(i).getCell(1).getCellType() == CellType.STRING) {
                    Section section = new Section(page.getRow(i).getCell(0).getStringCellValue());
                    sections.add(section);
                    sectionCount += 1;

                } else if (page.getRow(i).getCell(1).getCellType() == CellType.BLANK) {
                    String title = page.getRow(i).getCell(0).getStringCellValue();
                    if (title.equals("Written Comments")) {
                        sections.add(new Section(title));
                        sectionCount += 1;
                    } else {
                        Subsection subsection = new Subsection(title);
                        sections.get(sectionCount - 1).subsections.add(subsection);
                    }

                } else {
                    Subsection subsection = new Subsection(page.getRow(i).getCell(0).getStringCellValue());
                    subsection.average = page.getRow(i).getCell(1).getNumericCellValue();
                    subsection.stdDev = page.getRow(i).getCell(2).getNumericCellValue();
                    subsection.ratings[0] = ((int) page.getRow(i).getCell(3).getNumericCellValue());
                    subsection.ratings[1] = ((int) page.getRow(i).getCell(4).getNumericCellValue());
                    subsection.ratings[2] = ((int) page.getRow(i).getCell(5).getNumericCellValue());
                    subsection.ratings[3] = ((int) page.getRow(i).getCell(6).getNumericCellValue());
                    subsection.ratings[4] = ((int) page.getRow(i).getCell(7).getNumericCellValue());
                    subsection.ratings[5] = ((int) page.getRow(i).getCell(8).getNumericCellValue());
                    subsection.ratings[6] = ((int) page.getRow(i).getCell(9).getNumericCellValue());
                    subsection.uniAverage = page.getRow(i).getCell(10).getNumericCellValue();
                    sections.get(sectionCount - 1).subsections.add(subsection);
                }
            }
            i += 1;
        }
    }
    public String toString(){
        return participationRate + "";
    }
}


/*

        FileInputStream excelFile = new FileInputStream(new File(FILE_NAME));
        Workbook survey = new XSSFWorkbook(excelFile);
        Sheet page = survey.getSheetAt(0); //Selecting spreadsheet
        Iterator<Row> iterator = page.iterator();

        int rowCount = page.getPhysicalNumberOfRows();
        int colCount = page.getRow(3).getLastCellNum();

        //iteration counter and section counter
        int i = 0, sectionCount = 0;

        ArrayList<Section> sections = new ArrayList<>();
        String sectionNo="";

        String instructor = page.getRow(2).getCell(1).getStringCellValue();
        String courseCode = page.getRow(3).getCell(1).getStringCellValue();
        int studentCount= ((int) page.getRow(2).getCell(3).getNumericCellValue());
        int ansCount= ((int) page.getRow(3).getCell(3).getNumericCellValue());
        double participationRate =  ((double)ansCount/studentCount)*100;
        System.out.println("Instructor: " + instructor);

        while (i<courseCode.length()) {
            if (courseCode.charAt(i)=='.'){
                sectionNo=courseCode.substring(i+1);
                courseCode=courseCode.substring(0,i);
                break;
            }
            i+=1;
        }
        System.out.println("Course Code: " +courseCode);
        System.out.println("Section: " +sectionNo);
        System.out.println("Participation Rate: " + participationRate);

        i=0;

        //Reading the survey data
        while (iterator.hasNext()) {
            Row currentRow = iterator.next();
            Iterator<Cell> cellIterator = currentRow.iterator();

            if(i>4) {

                if (page.getRow(i).getCell(1).getCellType() == CellType.STRING) {
                    Section section = new Section(page.getRow(i).getCell(0).getStringCellValue());
                    sections.add(section);
                    sectionCount += 1;

                }else if(page.getRow(i).getCell(1).getCellType() == CellType.BLANK){
                    String title = page.getRow(i).getCell(0).getStringCellValue();
                    if(title.equals("Written Comments")) {
                        sections.add(new Section(title));
                        sectionCount+=1;
                    }
                    else {
                        Subsection subsection = new Subsection(title);
                        sections.get(sectionCount-1).subsections.add(subsection);
                    }

                }else {
                    Subsection subsection = new Subsection(page.getRow(i).getCell(0).getStringCellValue());
                    subsection.average = page.getRow(i).getCell(1).getNumericCellValue();
                    subsection.stdDev = page.getRow(i).getCell(2).getNumericCellValue();
                    subsection.ratings[0] = ((int) page.getRow(i).getCell(3).getNumericCellValue());
                    subsection.ratings[1] = ((int) page.getRow(i).getCell(4).getNumericCellValue());
                    subsection.ratings[2] = ((int) page.getRow(i).getCell(5).getNumericCellValue());
                    subsection.ratings[3] = ((int) page.getRow(i).getCell(6).getNumericCellValue());
                    subsection.ratings[4] = ((int) page.getRow(i).getCell(7).getNumericCellValue());
                    subsection.ratings[5] = ((int) page.getRow(i).getCell(8).getNumericCellValue());
                    subsection.ratings[6] = ((int) page.getRow(i).getCell(9).getNumericCellValue());
                    subsection.uniAverage = page.getRow(i).getCell(10).getNumericCellValue();
                    sections.get(sectionCount-1).subsections.add(subsection);
                }
            }
            i += 1;
        }

        System.out.println(sections.get(1));
 */