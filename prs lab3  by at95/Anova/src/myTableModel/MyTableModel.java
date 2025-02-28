package myTableModel;

import javax.swing.table.AbstractTableModel;

public class MyTableModel extends AbstractTableModel {
    private String[][] data;

    public MyTableModel(int rows, int cols) {
        data = new String[rows][cols];
        for(int i=1;i<rows-2;i++)
            data[i][0]="  "+i+". mjerenje";
        data[rows-2][0]="  Srednja vrijednost kolona";
        data[rows-1][0]="  Efekti";
        for(int j=1;j<cols;j++)
            data[0][j]=j+". alternativa";
        data[0][cols-1]="Ukupna sr. vrijednost";
    }

    @Override
    public int getRowCount() {
        return data.length;
    }

    @Override
    public int getColumnCount() {
        return data[0].length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    public void setValueAt(Object value, int row, int col) {
        data[row][col] = (String) value;
        fireTableCellUpdated(row, col);
    }

    public boolean isCellEditable(int row, int col) {
        //if((col>0 && row>0) || (col >= data[0].length-2 && row >= data.length-3))
        if(col==0 || row==0 || col==data[0].length-1 || row >= data.length-2)
            return false;
        else
            return true;
    }
}
