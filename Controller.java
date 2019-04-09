package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;

import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.knowm.xchart.*;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.Styler;


import javax.imageio.ImageIO;
import javax.print.DocFlavor;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Controller {

    File currentFile;
    Section mainSection;
    ArrayList<Course> courses = new ArrayList<>();
    ObservableList<File> openedFiles = FXCollections.observableArrayList();
    ObservableList<File> selectedFiles = FXCollections.observableArrayList();
    ObservableList<String> listViewList = FXCollections.observableArrayList();
    HashMap<String, Node> graphs = new HashMap<>();
    //ObservableList<Object> nodesOnPane = FXCollections.observableArrayList();
    private int sectionPositionForMulti = -1;
    private int subsectionPositionForMulti = -1;


    @FXML
    public Text text;
    public ImageView image;
    public ListView<File> list;
    public SplitPane splitPane;
    public Text section;
    public Text instructor;
    public Text courseCode;
    public Text noOfStudents;
    public Text participationRate;
    public Text yearOfSurvey;
    public TextField searchField;
    public RadioButton multipleRadio;
    public RadioButton singleRadio;
    public CheckMenuItem graph1;
    public CheckMenuItem graph2;
    public CheckMenuItem graph3;
    public CheckMenuItem graph4;
    public CheckMenuItem graph5;
    public CheckMenuItem graph6;
    public CheckMenuItem graph7;
    public CheckMenuItem writtenComments;
    public CheckMenuItem graphShowAll;
    public Button clearPane;
    public Button clearFiles;
    public Button pdfButton;
    public ScrollPane scrollPane;
    public BorderPane borderPane;
    public FlowPane flowPane;
    public ImageView mefLogo;
    public ListView<String> listView;
    public TextArea golfTextArea;
    public HBox hBoxWrittenComments;


    public Controller() throws IOException {
    }


    public void openFile() throws IOException {
        list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        scrollPane.setContent(flowPane);

        FileChooser fileChooser = new FileChooser();

        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Microsoft Excel File","*.xlsx"));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Microsoft Excel File","*.xls"));


        List<File> newFiles = fileChooser.showOpenMultipleDialog(new Stage());

        this.openedFiles.addAll(newFiles);
        list.setItems(openedFiles);
        list.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(File file, boolean empty) {
                super.updateItem(file, empty);
                setText(file == null ? null : file.getName());
            }
        });

        flowPane.setAlignment(Pos.CENTER);
        flowPane.setPadding(new Insets(10, 10, 10,10));



    }

    @FXML
    public void setClearPane() {
        flowPane.getChildren().clear();
        graph1.setSelected(false);
        graph2.setSelected(false);
        graph3.setSelected(false);
        graph4.setSelected(false);
        graph5.setSelected(false);
        graph6.setSelected(false);
        graph7.setSelected(false);
        writtenComments.setSelected(false);
        graphShowAll.setSelected(false);
        graphs.clear();
    }

    @FXML
    public void setClearFiles() {
        selectedFiles.clear();
        currentFile = null;
        openedFiles.clear();
        list.getItems().removeAll();

    }

    @FXML
    public void showSelectDialog() throws IOException {
        Dialog<ButtonType> dialog = new Dialog<ButtonType>();
        dialog.initOwner(borderPane.getScene().getWindow());
        dialog.setTitle("Graph Selection");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("SelectionDialog.fxml"));
        SelectionDialogController sdc;
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
            sdc = fxmlLoader.getController();

        } catch (IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }
        sdc.sectionsList = getSectionsYearsInstructors(0);
        sdc.yearsList = getSectionsYearsInstructors(1);
        sdc.instructorsList = getSectionsYearsInstructors(2);
        if (!multipleRadio.isSelected()) {
            sdc.singleOrMulti = false;
            sdc.setSingleOrMulti();
        } else {
            sdc.singleOrMulti = true;
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            chartGenerator(sdc.xAxisCombo.getSelectionModel().getSelectedItem(), sdc.chartSelection, sdc.compareByCombo.getSelectionModel().getSelectedItem(), sdc.availableYearsList.getSelectionModel().getSelectedItems(),
                    sdc.availableInstructorsList.getSelectionModel().getSelectedItems(), sdc.availableSectionsList.getSelectionModel().getSelectedItems());
        }

    }

    public ObservableList getSectionsYearsInstructors(int sel) {
        // sel==0 --> sections || sel==1 --> years
        if (sel == 0) {
            ObservableList<String> output = FXCollections.observableArrayList();
            for (int i = 0; i < selectedFiles.size(); i++) {
                int indices[] = findCourse(selectedFiles.get(i));
                String section = (courses.get(indices[0]).surveys.get(indices[1]).sectionNo);
                if (!output.contains(section)) {
                    output.add(section);
                }
            }
            return output;
        }

        if (sel == 1) {
            ObservableList<Integer> output = FXCollections.observableArrayList();
            for (int i = 0; i < selectedFiles.size(); i++) {
                int indices[] = findCourse(selectedFiles.get(i));
                int years = (courses.get(indices[0]).surveys.get(indices[1]).year);
                if (!output.contains(years)) {
                    output.add(years);
                }
            }
            return output;
        }
        if (sel == 2) {
            ObservableList<String> output = FXCollections.observableArrayList();
            for (int i = 0; i < selectedFiles.size(); i++) {
                int indices[] = findCourse(selectedFiles.get(i));
                String instructor = (courses.get(indices[0]).instructor);
                if (!output.contains(instructor)) {
                    output.add(instructor);
                }
            }
            return output;
        }
        return null;
    }


    public int getSectionIndex(String input, int i) {
        if (i == 0) {
            if (input.equals("None")) return -1;
            else if (input.equals("Flipped Classroom")) return 0;
            else if (input.equals("Course")) return 1;
            else if (input.equals("Instructor")) return 2;
            else if (input.equals("Labs/studios/recitations etc.")) return 3;
            else if (input.equals("Teaching Assistant")) return 4;
            else if (input.equals("Overall Evaluation")) return 5;
            else if (input.equals("Course Learning Outcomes")) return 6;
            else return -1;
        } else {
            if (input.equals("None")) return -1;
            else if (input.equals("Years")) return 0;
            else if (input.equals("Sections")) return 1;
            else if (input.equals("Instructor")) return 2;
            else return -1;
        }
    }


    public void chooseFile(File file) throws IOException {
        Survey survey = setCourse(file);

        this.section.setText(survey.sectionNo);
        this.courseCode.setText(survey.courseCode);
        this.instructor.setText(survey.instructor);
        this.participationRate.setText("%" + String.format("%.1f", survey.participationRate));
        this.yearOfSurvey.setText(String.valueOf(survey.year));
        this.noOfStudents.setText(String.valueOf(survey.studentCount));

    }

    @FXML
    public void updateCurrentFile() throws IOException {
        currentFile=null;
        courses.clear();
        if (singleRadio.isSelected() || singleRadio.isPressed()) {
            try {
                selectedFiles.clear();
                this.currentFile = list.getSelectionModel().getSelectedItem();
                listView.getItems().clear();
                chooseFile(currentFile);
            } catch (Exception e) {
                System.out.println(e);
                noFileSelectedError();
            }
        } else {
            selectedFiles.clear();
            selectedFiles.addAll(list.getSelectionModel().getSelectedItems());
            for (int i = 0; i < selectedFiles.size(); i++) {
                setCourse(selectedFiles.get(i));
            }
        }
        setClearPane();
    }

    public void noFileSelectedError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error has been occured.");
        alert.setContentText("A file must be selected from the list.");
        alert.showAndWait();
    }


    public void chartGenerator(String sectionTitle, int chartType, String compareByTitle, ObservableList<String> yearOfSelection, ObservableList<String> selectedInstructor, ObservableList<String> selectedSections) {
        if (singleRadio.isSelected()) {
            int indices[];
            indices = findCourse(currentFile);
            int sectionSel = getSectionIndex(sectionTitle, 0);
            Survey survey = courses.get(indices[0]).surveys.get(indices[1]);
            //bar chart
            if (chartType == 0) {
                List<Double> averages = new ArrayList<>();
                List<String> sectionTitles = new ArrayList<>();
                averages.clear();
                sectionTitles.clear();
                for (int i = 0; i < 7; i++) {
                    averages.add(courses.get(indices[0]).surveys.get(indices[1]).sections.get(i).calculateSectionAverage());
                    sectionTitles.add(survey.sections.get(i).sectionTitle);
                }
                CategoryChart barChart = new CategoryChartBuilder()
                        .width(635).height(480).title("Score Histogram").xAxisTitle("Score").yAxisTitle("Number").build();
                barChart.addSeries(sectionTitle, sectionTitles, averages);
                SwingNode swingNode = generateBarChart(barChart);
                String title = barChart.getTitle();
                int a=1;
                while (graphs.containsKey(barChart.getTitle())){
                    title=title+" - "+a;
                    a+=1;
                }
                graphs.put(title,swingNode);
            }

            //pie chart
            else if (chartType == 1) {
                PieChart pieChart = new PieChartBuilder().width(635).height(480).title(sectionTitle + " Pie Chart").build();
                pieChart.addSeries("Section Average", survey.sections.get(sectionSel).calculateSectionAverage());
                pieChart.addSeries("Other", 5 - survey.sections.get(sectionSel).calculateSectionAverage());
                pieChart.setTitle("Section Average Rate");
                SwingNode swingNode=generatePieChart(pieChart);
                String title = pieChart.getTitle();
                int a=1;
                while (graphs.containsKey(pieChart.getTitle())){
                    title=title+" - "+a;
                    a+=1;
                }
                graphs.put(title,swingNode);
            }

            else if (chartType == 2) {
                int[] temp = new int[7];
                int[] titles = {-1, 0, 1, 2, 3, 4, 5};
                Section section = survey.sections.get(sectionSel);
                for (Subsection subsection : section.subsections) {
                    for (int j = 0; j < subsection.ratings.length; j++) {
                        temp[j] += subsection.ratings[j];
                    }
                }
                CategoryChart barChart = new CategoryChartBuilder()
                        .width(635).height(480).title(sectionTitle+"Score Histogram").xAxisTitle("Score").yAxisTitle("Number").build();
                barChart.addSeries("Section Histogram", titles, temp);
                SwingNode swingNode1 = generateBarChart(barChart);
                String title = barChart.getTitle();
                int a=1;
                while (graphs.containsKey(barChart.getTitle())){
                    title=title+" - "+a;
                    a+=1;
                }
                graphs.put(title,swingNode1);
            } else {
                RadarChart radarChart = new RadarChartBuilder().width(635).height(480).title(sectionTitle + " Radar Chart").build();

                ArrayList<Double> scores = new ArrayList<>();
                ArrayList<String> sectionTitles = new ArrayList<>();

                for (Subsection subsection : survey.sections.get(sectionSel).subsections) {
                    sectionTitles.add(subsection.toString());
                    scores.add(subsection.average);
                }
                int size = scores.size();
                double[] doubles = new double[size];
                String[] titles = new String[size];
                for (int i = 0; i < scores.size(); i++) {
                    doubles[i] = scores.get(i) / 5;
                    titles[i] = sectionTitles.get(i);
                }

                radarChart.setVariableLabels(titles);
                radarChart.getStyler().setAxisTitleVisible(false);
                radarChart.addSeries(sectionTitle, doubles);
                SwingNode swingNode = generateRadarChart(radarChart);
                String title = radarChart.getTitle();
                int a=1;
                while (graphs.containsKey(radarChart.getTitle())){
                    title=title+" - "+a;
                    a+=1;
                }
                graphs.put(title,swingNode);

            }


        } else {
            ArrayList<Survey> surveys = new ArrayList<>();
            for (int i = 0; i < selectedFiles.size(); i++) {
                int indices[] = findCourse(selectedFiles.get(i));
                if (selectedSections.contains(courses.get(indices[0]).surveys.get(indices[1]).sectionNo)&&yearOfSelection.contains(String.valueOf(courses.get(indices[0]).surveys.get(indices[1]).year))
                &&selectedInstructor.contains(courses.get(indices[0]).surveys.get(indices[1]).instructor)) {
                    surveys.add(courses.get(indices[0]).surveys.get(indices[1]));
                }
            }

            int sectionSel = getSectionIndex(sectionTitle, 0); //index of section

            if (chartType == 0) { //bar chart
                CategoryChart barChart = new CategoryChartBuilder()
                        .title("Score Histogram").xAxisTitle("Sections").yAxisTitle("Distribution")
                        .width(635).height(480).build();

                ArrayList<Double> scores = new ArrayList<>();
                ArrayList<String> titles = new ArrayList<>();
                for (int i = 0; i < surveys.size(); i++) {
                    scores.add(surveys.get(i).sections.get(sectionSel).calculateSectionAverage());
                    titles.add(surveys.get(i).courseCode + " : " + surveys.get(i).year);
                }
                barChart.addSeries(" ", titles, scores);
                SwingNode swingNode = generateBarChart(barChart);
                String title = "Comparison of " + sectionTitle + " Bar Chart";
                int a=1;
                while (graphs.containsKey(title)){
                    title=title+" - "+a;
                    a+=1;
                }
                graphs.put(title,swingNode);

            }
            else if (chartType == 1) { //pie chart

                PieChart pieChart = new PieChartBuilder().width(635).height(480).title(sectionTitle + " Pie Chart").build();
                String title = surveys.get(0).sections.get(sectionSel).sectionTitle;
                for (int i = 0; i < surveys.size(); i++) {
                    pieChart.addSeries((surveys.get(i).courseCode + " - " + surveys.get(i).year + ":" + surveys.get(i).sectionNo),
                            surveys.get(i).sections.get(sectionSel).calculateSectionAverage());
                }

                pieChart.setTitle("Comparison of " + title + " with Surveys");
                SwingNode swingNode = generatePieChart(pieChart);
                title = pieChart.getTitle();
                int a=1;
                while (graphs.containsKey(pieChart.getTitle())){
                    title=title+" - "+a;
                    a+=1;
                }
                graphs.put(title,swingNode);

            } else if (chartType == 2) { //histogram chart

                int compareBySel = getSectionIndex(compareByTitle, 1);

                //year
                if (compareBySel == 0) {
                    Optional<String> result = multipleDialogPane(0);
                    ArrayList<Survey> yearsOfCourse = new ArrayList<>();
                    for (int i = 0; i < selectedFiles.size(); i++) {
                        int indices[] = findCourse(selectedFiles.get(i));
                        if (courses.get(indices[0]).surveys.get(indices[1]).courseCode.replace(" ", "").equals(result.get().replace(" ", "").toUpperCase()) && yearOfSelection.contains(String.valueOf(courses.get(indices[0]).surveys.get(indices[1]).year))) {
                            if (selectedSections.contains(courses.get(indices[0]).surveys.get(indices[1]).sectionNo)) {
                                yearsOfCourse.add(courses.get(indices[0]).surveys.get(indices[1]));
                            }
                        }
                    }

                    CategoryChart barChart = new CategoryChartBuilder()
                            .width(635).height(480).title("Score Histogram").xAxisTitle("Score").yAxisTitle("Number").build();
                    setStyle(barChart);

                    for (Survey survey : yearsOfCourse) {
                        ArrayList<Double> temp = new ArrayList<>();
                        ArrayList<String> sectionNames = new ArrayList<>();
                        for (Section section : survey.sections) {
                            temp.add(section.calculateSectionAverage());
                            sectionNames.add(section.sectionTitle);
                        }
                        temp.remove(sectionNames.size()-1);
                        sectionNames.remove(sectionNames.size()-1);
                        barChart.addSeries(survey.courseCode + " : " + survey.year + " - " + survey.sectionNo, sectionNames, temp);
                    }
                    SwingNode swingNode=generateBarChart(barChart);
                    String title =sectionTitle + " compared by Year";
                    int a=1;
                    while (graphs.containsKey(title)){
                        title=title+" - "+a;
                        a+=1;
                    }
                    graphs.put(title,swingNode);
                }

                //sections
                else if (compareBySel == 1) {
                    Optional<String> result = multipleDialogPane(0);
                    ArrayList<Survey> sectionsOfCourse = new ArrayList<>();
                    for (int i = 0; i < selectedFiles.size(); i++) {
                        int indices[] = findCourse(selectedFiles.get(i));
                        if (courses.get(indices[0]).surveys.get(indices[1]).courseCode.replace(" ", "").equals(result.get().replace(" ", "").toUpperCase()) && courses.get(indices[0]).surveys.get(indices[1]).year == Integer.parseInt(yearOfSelection.get(0))) {
                            if (selectedSections.contains(courses.get(indices[0]).surveys.get(indices[1]).sectionNo)) {
                                sectionsOfCourse.add(courses.get(indices[0]).surveys.get(indices[1]));
                            }
                        }
                    }
                    CategoryChart barChart = new CategoryChartBuilder()
                            .width(635).height(480).title("Score Histogram").xAxisTitle("Score").yAxisTitle("Number").build();
                    setStyle(barChart);

                    for (Survey survey : sectionsOfCourse) {
                        ArrayList<Double> temp = new ArrayList<>();
                        ArrayList<String> sectionNames = new ArrayList<>();
                        for (Section section : survey.sections) {
                            temp.add(section.calculateSectionAverage());
                            sectionNames.add(section.sectionTitle);
                        }
                        temp.remove(sectionNames.size()-1);
                        sectionNames.remove(sectionNames.size()-1);
                        barChart.addSeries(survey.courseCode + " : " + survey.sectionNo, sectionNames, temp);
                    }
                    SwingNode swingNode = generateBarChart(barChart);
                    String title =sectionTitle + " compared by Sections";
                    int a=1;
                    while (graphs.containsKey(title)){
                        title=title+" - "+a;
                        a+=1;
                    }
                    graphs.put(title,swingNode);

                }

                //instructor
                else if (compareBySel == 2) {
                    ArrayList<Survey> coursesOfInstructor = new ArrayList<>();
                    for (int i = 0; i < selectedFiles.size(); i++) {
                        int indices[] = findCourse(selectedFiles.get(i));
                        if (courses.get(indices[0]).surveys.get(indices[1]).year == Integer.parseInt(yearOfSelection.get(0)) && courses.get(indices[0]).instructor.equals(coursesOfInstructor.get(0))) {
                            coursesOfInstructor.add(courses.get(indices[0]).surveys.get(indices[1]));
                        }
                    }

                    CategoryChart barChart = new CategoryChartBuilder()
                            .width(635).height(480).title("Score Histogram").xAxisTitle("Score").yAxisTitle("Number").build();
                    setStyle(barChart);

                    for (Survey survey : coursesOfInstructor) {
                        ArrayList<Double> temp = new ArrayList<>();
                        ArrayList<String> sectionNames = new ArrayList<>();
                        for (Section section : survey.sections) {
                            temp.add(section.calculateSectionAverage());
                            sectionNames.add(section.sectionTitle);
                        }
                        temp.remove(sectionNames.size()-1);
                        sectionNames.remove(sectionNames.size()-1);
                        barChart.addSeries(survey.courseCode, sectionNames, temp);
                    }
                    SwingNode swingNode = generateBarChart(barChart);
                    String title =sectionTitle + " compared by Instructor";
                    int a=1;
                    while (graphs.containsKey(title)){
                        title=title+" - "+a;
                        a+=1;
                    }
                    graphs.put(title,swingNode);
                }

            } else { //chart type ==3 && radar-chart

                RadarChart radarChart = new RadarChartBuilder().width(635).height(480).title(sectionTitle + "Radar").build();
                double[] scores = new double[surveys.size()];
                String[] titles = new String[surveys.size()];

                for (int i = 0; i < surveys.size(); i++) {
                    scores[i] = surveys.get(i).sections.get(sectionSel).calculateSectionAverage() / 5;
                    titles[i] = surveys.get(i).courseCode;
                }
                radarChart.setVariableLabels(titles);
                radarChart.addSeries(sectionTitle, scores);
                SwingNode swingNode = generateRadarChart(radarChart);
                String title =sectionTitle + " Radar Chart";
                int a=1;
                while (graphs.containsKey(title)){
                    title=title+" - "+a;
                    a+=1;
                }
                graphs.put(title,swingNode);
            }
        }
    }


    public void search() {
        FilteredList<File> filteredData = new FilteredList<>(openedFiles, s -> true);
        this.searchField.textProperty().addListener(obs -> {
            String filter = searchField.getText().toLowerCase();
            if (filter == null || filter.length() == 0) {
                filteredData.setPredicate(s -> true);
            } else {
                filteredData.setPredicate(s -> s.getName().toLowerCase().contains(filter));
            }
        });
        list.setItems(filteredData);
    }

    /**
     * setCourse function takes a File as input and creates a Survey object
     * and if the course of the survey is in the "courses" ObservableList,
     * adds the survey to the that course's "survey" ArrayList. If not,
     * creates a new course and adds the survey to survey ArrayList.
     * If the section exists, returns the position of that section.
     *
     * @param file Survey file
     * @return the position of the survey in course list
     * @throws IOException if file path is wrong, throws IOException to prevent crashing
     */

    public Survey setCourse(File file) throws IOException {
        String[] fileNameParts = file.getName().split("_");
        String parts2[] = fileNameParts[3].split("\\.");
        Survey survey = new Survey(file.getAbsolutePath());
        survey.sectionNo = (fileNameParts[1]);
        survey.year = Integer.valueOf(fileNameParts[2]);
        survey.term = Integer.valueOf(fileNameParts[3].replace(" ", ".").split("\\.")[0]);
        for (int i = 0; i < courses.size(); i++) {
            if (fileNameParts[0].equals(courses.get(i).courseCode.replace(" ", ""))) {
                for (int j = 0; j < courses.get(i).surveys.size(); j++) {
                    if (survey.sectionNo.equals(courses.get(i).surveys.get(j)) && survey.year == courses.get(i).surveys.get(j).year && survey.term == courses.get(i).surveys.get(j).term) {
                        return courses.get(i).surveys.get(j);
                    }
                }
                courses.get(i).surveys.add(survey);
                int index = courses.get(i).surveys.size();
                return courses.get(i).surveys.get(index - 1);
            }
        }

        Course course = new Course();
        course.instructor = survey.instructor;
        course.courseCode = survey.courseCode;
        courses.add(course);
        courses.get(courses.size() - 1).surveys.add(survey);
        int index = courses.get(courses.size() - 1).surveys.size() - 1;
        return courses.get(courses.size() - 1).surveys.get(index);
    }

    /**
     * This function sets the style of the charts to look good
     *
     * @param chart type : XChart
     */

    public void setStyle(Chart chart) {
        Color backgorundColor = new Color(244, 244, 244,10);
        Color blue = new Color(13, 180, 225, 100);
        Color grey = new Color(181, 181, 181, 100);
        Color red = new Color(252, 106, 103, 100);
        Color green = new Color(165, 245, 106, 100);
        Color yellow = new Color(255, 250, 126, 100);
        Color lightblue = new Color(151, 249, 234, 100);
        Color pink = new Color(240, 147, 196, 100);
        Color[] colors = {blue, green, red, grey, yellow, lightblue, pink};
        chart.getStyler().setSeriesColors(colors);
        chart.getStyler().setChartBackgroundColor(backgorundColor);
        chart.getStyler().setPlotBackgroundColor(backgorundColor);
        chart.getStyler().setPlotBorderVisible(false);
        chart.getStyler().setLegendBackgroundColor(backgorundColor);
        chart.getStyler().setLegendBorderColor(backgorundColor);
        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
        chart.getStyler().setLegendLayout(Styler.LegendLayout.Horizontal);
    }

    /**
     * findCourse function takes a file as input and searches the "courses" observablelist.
     * first index of output array is the course index in courses list. second index is
     * surveys position in that specific course's "survey" arraylist.
     *
     * @param file Survey file
     * @return indices of the course.
     */

    public int[] findCourse(File file) {
        int output[] = {-1, -1};
        String parts[] = file.getName().split("_");
        String section = (parts[1]);
        int year = Integer.parseInt(parts[2]);
        int term = Integer.parseInt(parts[3].replace(" ", ".").split("\\.")[0]);
        String coursecode = parts[0];
        for (int i = 0; i < courses.size(); i++) {
            if (coursecode.equals(courses.get(i).courseCode.replace(" ", ""))) {
                output[0] = i;
                for (int j = 0; j < courses.get(i).surveys.size(); j++) {
                    if (section.equals(courses.get(i).surveys.get(j).sectionNo) && year == courses.get(i).surveys.get(j).year && term == courses.get(i).surveys.get(j).term) {
                        output[1] = j;
                        return output;
                    }
                }
                return output;
            }
        }
        return output;

    }


    /**
     * STATIC CHARTS
     */

    /**
     * generateRadarChart takes a radarchart, edit the style of it and then,
     * adds that radar chart to flowpane.
     *
     * @param radarChart
     * @return swingnode of that radarchart
     */

    public SwingNode generateRadarChart(RadarChart radarChart) {

        radarChart.setRadarRenderStyle(RadarChart.RadarRenderStyle.Polygon);
        setStyle(radarChart);
        radarChart.getStyler().setToolTipsEnabled(true);
        radarChart.getStyler().setLegendSeriesLineLength(635);
        XChartPanel<RadarChart> radarChartXChartPanel = new XChartPanel<>(radarChart);
        SwingNode swingNode = new SwingNode();
        swingNode.setContent(radarChartXChartPanel);
        flowPane.getChildren().add(swingNode);
        //nodesOnPane.add(swingNode);
        return swingNode;
    }

    /**
     * generatePieChart takes a pieChart, edit the style of it and then,
     * adds that pie chart to flowpane.
     *
     * @param pieChart
     * @return swing node of that pie chart
     */

    public SwingNode generatePieChart(PieChart pieChart) {
        SwingNode swingNode = new SwingNode();
        setStyle(pieChart);
        pieChart.getStyler().setLegendSeriesLineLength(500);
        pieChart.getStyler().setToolTipsEnabled(true);
        pieChart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
        pieChart.getStyler().setLegendLayout(Styler.LegendLayout.Horizontal);
        XChartPanel<PieChart> pieChartXChartPanel = new XChartPanel<>(pieChart);
        swingNode.setContent(pieChartXChartPanel);
        flowPane.getChildren().add(swingNode);
        return swingNode;
    }

    /**
     * generateBarChart takes a barChart, edit the style of it and then,
     * adds that bar chart to flowpane.
     *
     * @param barChart
     * @return swing node of that bar chart
     */

    public SwingNode generateBarChart(CategoryChart barChart) {
        barChart.getStyler().setToolTipsEnabled(true);
        setStyle(barChart);
        barChart.getStyler().setLegendSeriesLineLength(635);
        XChartPanel<CategoryChart> categoryChartXChartPanel = new XChartPanel<>(barChart);
        barChart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideE);
        barChart.getStyler().setXAxisLabelRotation(45);
        SwingNode swingNode1 = new SwingNode();
        swingNode1.setContent(categoryChartXChartPanel);
        flowPane.getChildren().add(swingNode1);
        return swingNode1;
    }


    /**
     * this method includes 2 graphs. one for single file analysis
     * the other is for multi file analysis. for single, creates a
     * section distributions chart, for multi, creates a
     */
    @FXML
    public void sectionDistributions() {
        if (singleRadio.isSelected()) {
            if (graph3.isSelected()) {
                int indices[];
                try {
                    indices = findCourse(currentFile);
                } catch (Exception e) {
                    noFileSelectedError();
                    return;
                }
                Survey survey = courses.get(indices[0]).surveys.get(indices[1]);
                for (int i = 0; i < 7; i++) {
                    int[] temp = new int[7];
                    for (Subsection subsection : survey.sections.get(i).subsections) {
                        for (int j = 0; j < subsection.ratings.length; j++) {
                            temp[j] += subsection.ratings[j];
                        }
                    }
                    PieChart pie_chart = new PieChartBuilder().width(635).height(480).title("Pie Chart").build();
                    pie_chart.addSeries("N/A", temp[0]);
                    pie_chart.addSeries("NO", temp[1]);
                    pie_chart.addSeries("Score 1", temp[2]);
                    pie_chart.addSeries("Score 2", temp[3]);
                    pie_chart.addSeries("Score 3", temp[4]);
                    pie_chart.addSeries("Score 4", temp[5]);
                    pie_chart.addSeries("Score 5", temp[6]);
                    pie_chart.setTitle(survey.sections.get(i).sectionTitle);

                    setStyle(pie_chart);
                    pie_chart.getStyler().setToolTipsEnabled(true);
                    SwingNode swingNode = new SwingNode();
                    XChartPanel<PieChart> pieChartXChartPanel = new XChartPanel<>(pie_chart);
                    swingNode.setContent(pieChartXChartPanel);
                    flowPane.getChildren().add(swingNode);
                    //nodesOnPane.add(swingNode);
                    graphs.put("graph3" + i, swingNode);

                }
            } else {
                for (int i = 0; i < 7; i++) {
                    flowPane.getChildren().remove(graphs.get("graph3" + i));
                    graphs.remove(graphs.get("graph3" + i));
                }

            }
        } else if (multipleRadio.isSelected()) {
            if (graph3.isSelected()) {
                Optional<String> result = multipleDialogPane(2);
                if (result.isPresent()) {
                    for (int j = 0; j < selectedFiles.size(); j++) {
                        if (selectedFiles.get(j).getName().split("_")[0].equals(result.get().replace(" ", "").toUpperCase())) {
                            for (int i = 0; i < courses.size(); i++) {
                                if (courses.get(i).courseCode.replace(" ", "").equals(result.get().replace(" ", "").toUpperCase())) {
                                    sectionsBarforMulti(courses.get(i), 2);
                                    return;
                                }
                            }
                        }
                    }
                }

            } else {
                flowPane.getChildren().remove(graphs.get("graphm3"));
                graphs.remove(graphs.get("graphm3"));

            }
        }
    }

    public Optional<String> multipleDialogPane(int sel) {
        Optional<String> result1 = null;
        if (sel == 0 || sel == 1 || sel == 2) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Input Needed");
            dialog.setHeaderText("Multi-file Analysis");
            dialog.setContentText("Please enter a course code:");
            result1 = dialog.showAndWait();
        }
        if (sel == 1 || sel == 2) {
            String options[] = new String[6];
            for (int i = 0; i < 6; i++) {
                options[i] = (courses.get(0).surveys.get(0).sections.get(i).sectionTitle);
            }
            ChoiceDialog dialog = new ChoiceDialog("Select a Section", Arrays.asList(options));
            dialog.setTitle("Input Needed");
            dialog.setHeaderText("Multi-file Analysis \n Select a section:");
            dialog.showAndWait();
            for (int i = 0; i < 6; i++) {
                if (options[i].equals(dialog.getSelectedItem().toString())) {
                    sectionPositionForMulti = i;
                    break;
                }
            }
        }
        if (sel == 2) {
            ArrayList<Subsection> arr = courses.get(0).surveys.get(0).sections.get(sectionPositionForMulti).subsections;
            String options[] = new String[arr.size()];
            for (int i = 0; i < arr.size(); i++) {
                options[i] = (arr.get(i).subsectionTitle);
            }
            ChoiceDialog<String> dialog = new ChoiceDialog<>("Select a subsection", options);
            dialog.setTitle("Input Needed");
            dialog.setHeaderText("Multi-file Analysis \n Select a subsection:");
            dialog.showAndWait();
            for (int i = 0; i < arr.size(); i++) {
                if (options[i].equals(dialog.getSelectedItem().toString())) {
                    subsectionPositionForMulti = i;
                    break;
                }
            }
        }
        return result1;
    }


    @FXML
    public void participationRate() {
        if (singleRadio.isSelected()) {
            if (graph1.isSelected()) {
                int indices[];
                try {
                    indices = findCourse(currentFile);
                } catch (Exception e) {
                    noFileSelectedError();
                    return;
                }
                Survey survey = courses.get(indices[0]).surveys.get(indices[1]);
                PieChart pieChart = new PieChartBuilder().width(635).height(480).title("Pie Chart").build();
                pieChart.addSeries("Students Participated to Survey", survey.participationRate);
                pieChart.addSeries("Not Participated", 100 - survey.participationRate);
                pieChart.setTitle("Participation Rate");
                pieChart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
                pieChart.getStyler().setLegendLayout(Styler.LegendLayout.Horizontal);
                pieChart.getStyler().setToolTipsEnabled(true);
                SwingNode swingNode = generatePieChart(pieChart);
                graphs.put("graph1", swingNode);
            } else {
                flowPane.getChildren().remove(graphs.get("graph1"));
                graphs.remove(graphs.get("graph1"));

            }
        } else if (multipleRadio.isSelected()) {
            if (graph1.isSelected()) {
                Optional<String> result = multipleDialogPane(0);

                if (result.isPresent()) {
                    for (int j = 0; j < selectedFiles.size(); j++) {
                        if (selectedFiles.get(j).getName().split("_")[0].equals(result.get().replace(" ", "").toUpperCase())) {
                            for (int i = 0; i < courses.size(); i++) {
                                if (courses.get(i).courseCode.replace(" ", "").equals(result.get().replace(" ", "").toUpperCase())) {
                                    sectionsBarforMulti(courses.get(i), 0);
                                    return;
                                }
                            }
                        }
                    }
                }

            } else {
                for (int j = 0; j < 7; j++) {
                    flowPane.getChildren().remove(graphs.get("graphm1" + j));
                    graphs.remove(graphs.get("graphm1" + j));
                }
            }
        }
    }

    @FXML
    public void sectionAverage() {
        if (singleRadio.isSelected()) {
            if (graph2.isSelected()) {
                int indices[];
                try {
                    indices = findCourse(currentFile);
                } catch (Exception e) {
                    noFileSelectedError();
                    return;
                }
                Survey survey = courses.get(indices[0]).surveys.get(indices[1]);
                List<Double> averages = new ArrayList<>();
                List<String> sectionTitles = new ArrayList<>();
                averages.clear();
                sectionTitles.clear();
                for (int i = 0; i < 7; i++) {
                    averages.add(courses.get(indices[0]).surveys.get(indices[1]).sections.get(i).calculateSectionAverage());
                    sectionTitles.add(survey.sections.get(i).sectionTitle);
                }

                CategoryChart barChart = new CategoryChartBuilder()
                        .width(635).height(480).title("Score Histogram").xAxisTitle("Score").yAxisTitle("Number").build();
                barChart.addSeries("Score", sectionTitles, averages);
                SwingNode swingNode1 = generateBarChart(barChart);
                //nodesOnPane.add(swingNode1);
                graphs.put("graph2", swingNode1);
            } else {
                flowPane.getChildren().remove(graphs.get("graph2"));
                graphs.remove(graphs.get("graph2"));
            }
        } else if (multipleRadio.isSelected()) {
            if (graph2.isSelected()) {
                Optional<String> result = multipleDialogPane(1);
                if (result.isPresent()) {
                    for (int j = 0; j < selectedFiles.size(); j++) {
                        if (selectedFiles.get(j).getName().split("_")[0].equals(result.get().replace(" ", "").toUpperCase())) {
                            for (int i = 0; i < courses.size(); i++) {
                                if (courses.get(i).courseCode.replace(" ", "").equals(result.get().replace(" ", "").toUpperCase())) {
                                    sectionsBarforMulti(courses.get(i), 1);
                                    return;
                                }
                            }
                        }
                    }
                }

            } else {
                flowPane.getChildren().remove(graphs.get("graphm2"));
                graphs.remove(graphs.get("graphm2"));

            }
        }
    }

    @FXML
    public void sectionAveragesRadar() {
        if (graph4.isSelected()) {
            int indices[];
            try {
                indices = findCourse(currentFile);
            } catch (Exception e) {
                noFileSelectedError();
                return;
            }
            Survey survey = courses.get(indices[0]).surveys.get(indices[1]);
            RadarChart radarChart = new RadarChartBuilder().width(635).height(480).title("Section Averages").build();
            ArrayList<Double> averages = new ArrayList<>();
            ArrayList<String> sectionTitles = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                averages.add(survey.sections.get(i).calculateSectionAverage());
                sectionTitles.add(survey.sections.get(i).sectionTitle);
            }
            int size = averages.size();
            double[] doubles = new double[size];
            String[] titles = new String[size];
            for (int i = 0; i < averages.size(); i++) {
                doubles[i] = averages.get(i) / 5;
                titles[i] = sectionTitles.get(i);
            }
            radarChart.setVariableLabels(titles);
            radarChart.addSeries("Section Grade", doubles);
            radarChart.getStyler().setAxisTitleVisible(false);

            SwingNode swingNode = generateRadarChart(radarChart);
            //generateLabel(sectionTitles);
            graphs.put("graph4", swingNode);
        } else {
            flowPane.getChildren().remove(graphs.get("graph4"));
            graphs.remove(graphs.get("graph4"));

        }
    }

    @FXML
    public void learningOutcomes() {
        if (graph5.isSelected()) {
            int indices[];
            try {
                indices = findCourse(currentFile);
            } catch (Exception e) {
                noFileSelectedError();
                return;
            }
            Survey survey = courses.get(indices[0]).surveys.get(indices[1]);
            RadarChart radarChart = new RadarChartBuilder().width(635).height(480).title("Learning Outcomes").build();
            ArrayList<Double> scores = new ArrayList<>();
            ArrayList<String> sectionTitles = new ArrayList<>();

            for (Subsection subsection : survey.sections.get(6).subsections) {
                sectionTitles.add(subsection.toString());
                scores.add(subsection.average);
            }
            int size = scores.size();
            double[] doubles = new double[size];
            String[] titles = new String[size];
            for (int i = 0; i < scores.size(); i++) {
                doubles[i] = scores.get(i) / 5;
                titles[i] = sectionTitles.get(i);
            }
            radarChart.setVariableLabels(titles);


            setStyle(radarChart);
            radarChart.addSeries("Learning Outcomes", doubles);
            //radarChart.getStyler().setAxisTitleFont(new Font("Verdana", Font.BOLD, 5));
            radarChart.getStyler().setToolTipsEnabled(true);
            radarChart.getStyler().setAxisTitleVisible(false);

            SwingNode swingNode = generateRadarChart(radarChart);
            //generateLabel(sectionTitles);

            graphs.put("graph5", swingNode);
        } else {
            flowPane.getChildren().remove(graphs.get("graph5"));
            graphs.remove(graphs.get("graph5"));

        }
    }


    @FXML
    public void subSectionAveragesRadar() throws IOException {
        if (graph6.isSelected()) {
            int indices[];
            try {
                indices = findCourse(currentFile);
            } catch (Exception e) {
                noFileSelectedError();
                return;
            }
            Survey survey = courses.get(indices[0]).surveys.get(indices[1]);
            RadarChart radarChart = new RadarChartBuilder().width(635).height(480).title("Sub-Section Averages").build();
            ArrayList<Double> averages = new ArrayList<>();
            ArrayList<String> sectionTitles = new ArrayList<>();
            ArrayList<Double> uniAverages = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                for (Subsection subsection : survey.sections.get(i).subsections) {
                    averages.add(subsection.average);
                    uniAverages.add(subsection.uniAverage);
                    sectionTitles.add(subsection.subsectionTitle);
                }
            }
            int size = averages.size();
            double[] doubles = new double[size];
            String[] titles = new String[size];
            double[] uniAvr = new double[size];
            for (int i = 0; i < averages.size(); i++) {
                doubles[i] = averages.get(i) / 5;
                titles[i] = String.valueOf(i + 1);
                uniAvr[i] = uniAverages.get(i) / 5;
            }
            radarChart.setVariableLabels(titles);
            setStyle(radarChart);
            radarChart.addSeries("Subsection Grade", doubles);
            radarChart.addSeries("University Average", uniAvr);
            for (int i = 0; i < averages.size(); i++) {
                titles[i] = sectionTitles.get(i);
            }
            radarChart.setVariableLabels(titles);
            radarChart.getStyler().setToolTipsEnabled(true);
            radarChart.getStyler().setAxisTitleVisible(false);

            SwingNode swingNode = generateRadarChart(radarChart);
            //generateLabel(sectionTitles);
            graphs.put("graph6", swingNode);
        } else {
            flowPane.getChildren().remove(graphs.get("graph6"));
            graphs.remove(graphs.get("graph6"));

        }
    }

    @FXML
    public void showWrittenComments() {
        if (writtenComments.isSelected()) {
            HBox hBox;
            Text text = new Text("Written Comments:");
            text.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 20));
            text.setUnderline(true);
            hBox = new HBox(text);
            hBox.setPadding(new Insets(50, 0, 20, 50));
            //flowPane.getChildren().add(hBox);
            int indices[];
            try {
                indices = findCourse(currentFile);
            } catch (Exception e) {
                noFileSelectedError();
                return;
            }
            if (listViewList.size()!=0){
                for (int i = 0; i < listViewList.size(); i++) {
                    courses.get(indices[0]).surveys.get(indices[1]).sections.get(7).subsections.get(i+1).subsectionTitle=listViewList.get(i);
                }
            }
            for (int i = 1; i < courses.get(indices[0]).surveys.get(indices[1]).sections.get(7).subsections.size(); i++) {
                listView.getItems().add(courses.get(indices[0]).surveys.get(indices[1]).sections.get(7).subsections.get(i).subsectionTitle);
            }
            VBox vBox=new VBox();
            vBox.getChildren().add(hBox);
            vBox.getChildren().add(hBoxWrittenComments);
            flowPane.getChildren().add(vBox);
            //nodesOnPane.add(hBoxWrittenComments);
            hBoxWrittenComments.setVisible(true);

            graphs.put("writtencomments", hBoxWrittenComments);
        } else {
            flowPane.getChildren().remove(graphs.get("writtencomments"));
            graphs.remove(graphs.get("writtenComments"));
            hBoxWrittenComments.setVisible(false);

        }

    }

    /**
     * creates sections bar chart for multi file analysis
     *
     * @param course Course object
     * @param sel    if 0, creates a section average change in years bar chart,
     *               if 1, creates a subsection average change in years bar chart of a selected section
     *               if 2, creates a selected subsection's change in years bar chart.
     */

    public void sectionsBarforMulti(Course course, int sel) {
        ObservableList<Integer> years = getSectionsYearsInstructors(1);
        ArrayList<Survey> temp = new ArrayList<>();
        for (Survey survey : course.surveys) {
            for (int i = 0; i < years.size(); i++) {
                if (survey.year == years.get(i)) {
                    temp.add(survey);
                }
            }
        }
        if (sel == 0) {
            for (int i = 0; i < 7; i++) {
                ArrayList<Double> scores = new ArrayList<>();
                for (Survey survey : temp) {
                    scores.add(survey.sections.get(i).calculateSectionAverage());
                }
                CategoryChart barChart = new CategoryChartBuilder()
                        .width(635).height(480).title(course.surveys.get(0).sections.get(i).sectionTitle).xAxisTitle("Years").yAxisTitle("Scores").build();
                barChart.addSeries("Score", years, scores);
                SwingNode swingNode1 = generateBarChart(barChart);
                graphs.put("graphm1" + i, swingNode1);
            }
        } else if (sel == 1) {
            ArrayList<Double> scores = new ArrayList<>();
            for (Survey survey : temp) {
                scores.add(survey.sections.get(sectionPositionForMulti).calculateSectionAverage());
            }
            CategoryChart barChart = new CategoryChartBuilder()
                    .width(635).height(480).title(course.surveys.get(0).sections.get(sectionPositionForMulti).sectionTitle).xAxisTitle("Years").yAxisTitle("Scores").build();
            barChart.addSeries("Score", years, scores);
            SwingNode swingNode1 = generateBarChart(barChart);
            graphs.put("graphm2", swingNode1);
        } else {
            ArrayList<Double> scores = new ArrayList<>();
            for (Survey survey : temp) {
                scores.add(survey.sections.get(sectionPositionForMulti).subsections.get(subsectionPositionForMulti).average);
            }
            CategoryChart barChart = new CategoryChartBuilder()
                    .width(635).height(480).title(course.surveys.get(0).sections.get(sectionPositionForMulti).subsections.get(subsectionPositionForMulti).subsectionTitle).xAxisTitle("Years").yAxisTitle("Scores").build();
            barChart.addSeries("Score", years, scores);
            SwingNode swingNode1 = generateBarChart(barChart);
            graphs.put("graphm3", swingNode1);
        }
    }


    @FXML
    public void updateMenu() {
        if (multipleRadio.isSelected()) {
            graph1.setText("Sections Bar Chart");
            graph2.setText("Section Bar Chart");
            graph3.setText("Subsection Bar Chart");
            graph4.setVisible(false);
            graph5.setVisible(false);
            graph6.setVisible(false);
            graph7.setVisible(false);
            writtenComments.setVisible(false);
        }
        if (singleRadio.isSelected()) {
            graph1.setText("Participation Rate");
            graph2.setText("Sections Bar Chart");
            graph3.setText("Section Distributions");
            graph4.setText("Section Averages As Radar");
            graph5.setText("Learning Outcomes as Radar");
            graph6.setVisible(true);
            graph7.setVisible(true);

        }
    }

    @FXML
    public void showAllGraphs() throws IOException {
        if (graphShowAll.isSelected()) {

            setClearPane();
            graph1.setSelected(true);
            graph2.setSelected(true);
            graph3.setSelected(true);
            graph4.setSelected(true);
            graph5.setSelected(true);
            graph6.setSelected(true);
            graph7.setSelected(true);
            writtenComments.setSelected(true);
            graphShowAll.setSelected(true);
            if (singleRadio.isSelected()) {
                participationRate();
                sectionAverage();
                sectionDistributions();
                sectionAveragesRadar();
                learningOutcomes();
                subSectionAveragesRadar();
                histogram();
                showWrittenComments();
            } else {
                participationRate();
                sectionAverage();
                sectionDistributions();
            }
        } else setClearPane();

    }

    @FXML
    public void listToTArea() {
        String textAreaString = "";
        Object item = listView.getSelectionModel().getSelectedItem();
        textAreaString += String.format("%s%n", (String) item);
        this.golfTextArea.setText(textAreaString);
    }
    @FXML
    public void histogram(){
        if (graph7.isSelected()) {
            int indices[] = findCourse(currentFile);
            Survey survey = courses.get(indices[0]).surveys.get(indices[1]);
            int[] titles = {-1, 0, 1, 2, 3, 4, 5};
            for (int i = 0; i < 7; i++) {
                int[] temp = new int[7];
                Section section = survey.sections.get(i);
                for (Subsection subsection : section.subsections) {
                    for (int j = 0; j < subsection.ratings.length; j++) {
                        temp[j] += subsection.ratings[j];
                    }
                }
                CategoryChart barChart = new CategoryChartBuilder()
                        .width(635).height(480).title("Score Histogram").xAxisTitle("Score").yAxisTitle("Number").build();
                barChart.addSeries(section.sectionTitle+" Histogram", titles, temp);
                SwingNode swingNode1 = generateBarChart(barChart);
                //flowPane.getChildren().add(swingNode1);
                graphs.put("graph7" + i, swingNode1);
            }
        }
        else {
            for (int i = 0; i < 7; i++) {
                flowPane.getChildren().remove(graphs.get("graph7"+i));
                graphs.remove(graphs.get("graph7"+i));
            }
        }

    }



    @FXML
    public void tAreaToList() {
        int index = listView.getSelectionModel().getSelectedIndex();
        listView.getItems().set(index, golfTextArea.getText());
        listView.refresh();
    }

    /**
     * @throws Exception
     */
    @FXML
    public void exportPDF() throws Exception {
        ObservableList<Node> nodesOnPane = flowPane.getChildren();
        ArrayList<String> strings = new ArrayList<>();
        String titles[]=new String[nodesOnPane.size()];
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extensionFilter);
        String path = fileChooser.showSaveDialog(new Stage()).getAbsolutePath();
        int index1=0;
        boolean flag1=false;
        boolean flag2=false;

        File file = new File("runtime-outputs");

        if (file.mkdir()) {
            System.out.println("Directory is created!"); }

        for (int i = 0; i < nodesOnPane.size(); i++) {
            if (nodesOnPane.get(i) instanceof VBox) {

                System.out.println(Arrays.toString(listView.getItems().toArray()));
                index1=i;
                titles[index1]="writtencomments";
                flag1=true;
                //strings.add(Arrays.toString(listView.getItems().toArray()));
            } else if (nodesOnPane.get(i) instanceof SwingNode) {
                JComponent jComponent = ((SwingNode) nodesOnPane.get(i)).getContent();
                BufferedImage bi = new BufferedImage(jComponent.getWidth(), jComponent.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics g = bi.createGraphics();
                jComponent.paint(g);
                g.dispose();
                try {
                    ImageIO.write(bi, "png", new File("runtime-outputs"+File.separator+"graph" + (i + 1) + ".png"));
                    strings.add("runtime-outputs"+File.separator+"graph" + (i + 1) + ".png");
                    titles[i]=getKey(nodesOnPane.get(i));
                    flag2=true;
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
        System.out.println("Old Version : "+Arrays.toString(titles));

        if (flag1 && flag2){
            String temp = titles[index1];
            titles[index1]=titles[titles.length-1];
            titles[titles.length-1]=temp;
        }
        System.out.println("New Version : "+Arrays.toString(titles));
        GraphToPDF graphToPDF = new GraphToPDF();
        boolean flag=graphToPDF.generatePDF(strings.toArray(new String[0]),listView.getItems().toArray(new String[0]), titles,flag1,path);
        if (flag) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText("Student Survey Analysis Report");
            alert.setContentText("Report Saved Successfully!");
            alert.showAndWait();
        }
        else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Information Dialog");
            alert.setHeaderText("Student Survey Analysis Report");
            alert.setContentText("An error occured. Please restart the application and try again.");
            alert.showAndWait();
        }
    }

    public String getKey(Node swingNode){
        String[] keys = graphs.keySet().toArray(new String[0]);
        int i = 0;
        while(keys.length>i){
            if (graphs.get(keys[i]).equals(swingNode)){
                return keys[i];
            }
            i++;
        }
        return null;
    }

    /**
     *
     */

    @FXML
    public void quit() {
        Platform.exit();
    }
}