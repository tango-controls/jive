package jive;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/*
 * SearchDlg.java
 *
 * Created on September 4, 2002, 1:32 PM
 */

public class SearchDlg extends JDialog {

    /** Creates new form SearchDlg */
    public SearchDlg(java.awt.Frame parent, boolean modal,String title,String slabel) {
        
	super(parent, modal);
	setTitle(title);
		
        jLabel1 = new javax.swing.JLabel();
        textToSearch = new javax.swing.JTextField();
        ignoreCase = new javax.swing.JCheckBox();
        searchValues = new javax.swing.JCheckBox();
        searchAttribute = new javax.swing.JCheckBox();
        searchUseRegexp = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        startButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        
        getContentPane().setLayout(null);
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
	      hide();
              dispose();
            }
        });
        
        jLabel1.setText(slabel);
        jLabel1.setForeground(java.awt.Color.black);
        getContentPane().add(jLabel1);
        jLabel1.setBounds(10, 7, 100, 20);
        
        textToSearch.setText(textToFind);
        getContentPane().add(textToSearch);
        textToSearch.setBounds(110, 10, 210, 20);
        
        ignoreCase.setText("Ignore case");
        getContentPane().add(ignoreCase);
        ignoreCase.setBounds(20, 50, 91, 24);
	ignoreCase.setSelected(dIgnoreCase);
	        
        searchValues.setText("Search in property value");
        getContentPane().add(searchValues);
        searchValues.setBounds(20, 70, 169, 24);
        searchValues.setSelected(dSearchValues);
        
        searchAttribute.setText("Search in attribure property");
        getContentPane().add(searchAttribute);
        searchAttribute.setBounds(20, 90, 193, 24);
        searchAttribute.setSelected(dSearchAttributes);

        searchUseRegexp.setText("Use regular expression");
        getContentPane().add(searchUseRegexp);
        searchUseRegexp.setBounds(20, 110, 193, 24);
        searchUseRegexp.setSelected(dSearchUseRegexp);
        
        jPanel1.setBorder(new javax.swing.border.TitledBorder("Search options"));
        getContentPane().add(jPanel1);
        jPanel1.setBounds(10, 30, 210, 110);
        
        startButton.setText("Start");
        getContentPane().add(startButton);
        startButton.setBounds(220, 80, 100, 27);
        startButton.addMouseListener(new MouseAdapter() {
          public void mouseClicked(MouseEvent evt) {
	    ret_value=true;
	    hide();
            dispose();
          }
        });
        
        cancelButton.setText("Cancel");
        getContentPane().add(cancelButton);
        cancelButton.setBounds(220, 112, 100, 27);
        cancelButton.addMouseListener(new MouseAdapter() {
          public void mouseClicked(MouseEvent evt) {
	    hide();
            dispose();
          }
        });

	Rectangle r = parent.getBounds();
     	int x = r.x + (r.width-340)/2;
     	int y = r.y + (r.height-180)/2;
     	setBounds(x,y,340,180);

	ret_value = false;
    }

    public boolean showDlg() {
      show();    
      
      if( ret_value ) {
        dIgnoreCase=ignoreCase.isSelected();
        dSearchValues=searchValues.isSelected();
        dSearchAttributes=searchAttribute.isSelected();
        dSearchUseRegexp=searchUseRegexp.isSelected();
        textToFind = textToSearch.getText();
      }
      
      return ret_value;
    }

    private JLabel     jLabel1;
    private JTextField textToSearch;
    private JCheckBox  ignoreCase;
    private JCheckBox  searchValues;
    private JCheckBox  searchAttribute;
    private JCheckBox  searchUseRegexp;
    private JPanel     jPanel1;
    private JButton    startButton;
    private JButton    cancelButton;

    private boolean    ret_value;
    
    // Default option value
    static public boolean dIgnoreCase=false;
    static public boolean dSearchValues=false;
    static public boolean dSearchAttributes=false;
    static public boolean dSearchUseRegexp=false;
    static public String  textToFind = "";
    
    // End of variables declaration
}
