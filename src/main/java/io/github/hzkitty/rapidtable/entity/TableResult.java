package io.github.hzkitty.rapidtable.entity;

import java.util.Arrays;
import java.util.List;

public class TableResult {

    private String htmlStr;

    private List<float[]> cellBoxes;

    private List<int[]> logicPoints;

    private double elapse;

    public TableResult(String htmlStr, List<float[]> cellBoxes, List<int[]> logicPoints, double elapse) {
        this.htmlStr = htmlStr;
        this.cellBoxes = cellBoxes;
        this.logicPoints = logicPoints;
        this.elapse = elapse;
    }

    public String getHtmlStr() {
        return htmlStr;
    }

    public void setHtmlStr(String htmlStr) {
        this.htmlStr = htmlStr;
    }

    public List<float[]> getCellBoxes() {
        return cellBoxes;
    }

    public void setCellBoxes(List<float[]> cellBoxes) {
        this.cellBoxes = cellBoxes;
    }

    public List<int[]> getLogicPoints() {
        return logicPoints;
    }

    public void setLogicPoints(List<int[]> logicPoints) {
        this.logicPoints = logicPoints;
    }

    public double getElapse() {
        return elapse;
    }

    public void setElapse(double elapse) {
        this.elapse = elapse;
    }

    @Override
    public String toString() {
        return "TableResult{" +
                "htmlStr='" + htmlStr + '\'' +
                ", cellBoxes=" + (cellBoxes != null ? Arrays.deepToString(cellBoxes.toArray()) : "null") +
                ", logicPoints=" + (logicPoints != null ? Arrays.deepToString(logicPoints.toArray()) : "null") +
                ", elapse=" + elapse +
                '}';
    }
}