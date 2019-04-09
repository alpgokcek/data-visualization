package sample;

import java.util.ArrayList;

public class Section {
    public String sectionTitle;
    public ArrayList<Subsection> subsections;

    public Section(String sectionTitle) {
        this.sectionTitle = sectionTitle;
        this.subsections = new ArrayList<>();
    }

    public double calculateSectionAverage(){
        int subsectionCount=subsections.size();
        double sum=0;
        for(int i=0; i<subsectionCount; i++){
            sum+=subsections.get(i).average;
        }
        double average = sum / subsectionCount;
        return average;
    }

    public String toString(){
        String output = sectionTitle + ": \n" + subsections.toString() + "\n";
        return output;
    }
}

