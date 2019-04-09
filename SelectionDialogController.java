package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectionDialogController {
    @FXML
    public ComboBox<String> xAxisCombo;
    public ComboBox<String> compareByCombo;
    public VBox xAxisVBox;
    public VBox compareByVBox;
    public VBox availableSectionsVBox;
    public VBox availableInstructorsVBox;
    public VBox availableYearsVBox;
    public VBox radarTypeVBox;
    public ListView<String> availableYearsList;
    public ListView<String> availableSectionsList;
    public ListView<String> availableInstructorsList;
    public RadioButton separateRadar;
    public RadioButton combinedRadar;


    private String sectionNames1[]={"None", "Flipped Classroom", "Course", "Instructor", "Labs/studios/recitations etc.", "Teaching Assistant", "Overall Evaluation", "Course Learning Outcomes"};
    private String sectionNames2[]={"None", "Years", "Sections", "Instructor"};
    private ObservableList<String> sections1=FXCollections.observableArrayList(sectionNames1);
    private ObservableList<String> sections2=FXCollections.observableArrayList(sectionNames2);

    public ObservableList<Integer> yearsList=FXCollections.observableArrayList();
    public ObservableList<String> sectionsList=FXCollections.observableArrayList();
    public ObservableList<String> instructorsList=FXCollections.observableArrayList();

    public int chartSelection=-1;
    public boolean singleOrMulti;  //true if multi, false if single



    public SelectionDialogController(){
    }

    @FXML
    public void setup() {
        xAxisCombo.setItems(sections1);
        xAxisCombo.getSelectionModel().selectFirst();
        compareByCombo.setItems(sections2);
        compareByCombo.getSelectionModel().selectFirst();
        availableSectionsList.setItems(sectionsList);
        availableSectionsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        availableInstructorsList.setItems(instructorsList);
        //availableYearsList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        availableInstructorsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        availableYearsList.setItems(intToStringArr());
        radarTypeVBox.setVisible(false);
        setSingleOrMulti();
    }
    public ObservableList<String> intToStringArr(){
        ObservableList<String>output = FXCollections.observableArrayList();
        for (int i = 0; i < yearsList.size(); i++) {
            output.add(String.valueOf(yearsList.get(i)));
        }
        return output;
    }

    @FXML
    public void setYears(){
        System.out.println(compareByCombo.getSelectionModel().getSelectedIndex());
        if (compareByCombo.getSelectionModel().getSelectedIndex()!=-1){
            availableYearsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            availableSectionsList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        }
        else{
            availableYearsList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }
    }

    @FXML
    public void barChartFunction(){
        setup();
        xAxisVBox.setVisible(true);

        setSingleOrMulti();
        this.chartSelection=0;
    }
    @FXML
    public void pieChartFunction(){
        setup();
        xAxisVBox.setVisible(true);
        setSingleOrMulti();
        this.chartSelection=1;

    }

    @FXML
    public void histogramChartFunction(){
        setup();
        xAxisVBox.setVisible(true);
        setSingleOrMulti();
        this.chartSelection=2;

    }
    @FXML
    public void radarChartFunction(){
        setup();
        xAxisVBox.setVisible(true);
        radarTypeVBox.setVisible(singleOrMulti);
        setSingleOrMulti();
        this.chartSelection=3;

    }
    public void setSingleOrMulti(){
        compareByCombo.setVisible(singleOrMulti);
        availableSectionsVBox.setVisible(singleOrMulti);
        availableInstructorsVBox.setVisible(singleOrMulti);
        availableYearsVBox.setVisible(singleOrMulti);
    }

}
