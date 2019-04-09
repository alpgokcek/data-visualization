package sample;

import java.io.IOException;
import java.util.ArrayList;

public class Course {
    public String courseCode;
    public String instructor;
    public ArrayList<Survey> surveys;

    public Course() {
        surveys=new ArrayList<>();
    }

    public String toString(){
        return courseCode + " " + instructor + " " +surveys;
    }
}
