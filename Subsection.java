package sample;

public class Subsection {
    public String subsectionTitle;
    public double average;
    public double stdDev;
    public int[] ratings;
    public double uniAverage;

    public Subsection(String title) {
        this.subsectionTitle = title;
        this.average = 0;
        this.stdDev = 0;
        this.ratings=new int[7];
        this.uniAverage=0;
    }
    public String toString(){
        String output = subsectionTitle;
        return output;
    }
}
