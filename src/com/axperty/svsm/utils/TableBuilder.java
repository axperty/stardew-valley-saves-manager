// In TableUtils.java
package com.axperty.svsm.utils;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;
import java.util.ResourceBundle;

public class TableBuilder {

    // Create the table
    public static JTable createTable(ResourceBundle bundle) {
        JTable table = new JTable();
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn(bundle.getString("table.farm_name.title"));
        model.addColumn(bundle.getString("table.id.title"));
        model.addColumn(bundle.getString("table.last_played.title"));
        table.setModel(model);
        table.setRowHeight(30);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return table;
    }

    // Update Lists
    public static void updateTables(JTable androidTable, JTable steamTable) {
        updateAndroidTable(androidTable);
        updateSteamTable(steamTable);
    }

    // Update Android Table
    public static void updateAndroidTable(JTable androidTable) {
        DefaultTableModel model = (DefaultTableModel) androidTable.getModel();
        model.setRowCount(0); // Clear existing data
        List<String[]> saveData = GetData.getAndroidSavesData();
        for (String[] data : saveData) {
            model.addRow(data);
        }
    }

    // Update Steam Table
    public static void updateSteamTable(JTable steamTable) {
        DefaultTableModel model = (DefaultTableModel) steamTable.getModel();
        model.setRowCount(0); // Clear existing data
        List<String[]> saveData = GetData.getSteamSavesData();
        for (String[] data : saveData) {
            model.addRow(data);
        }
    }
}