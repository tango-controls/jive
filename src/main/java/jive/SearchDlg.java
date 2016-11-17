package jive;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/*
 * SearchDlg.java
 *
 * Created on September 4, 2002, 1:32 PM
 */

public class SearchDlg extends JDialog {

  /** Creates new form SearchDlg */
  public SearchDlg(java.awt.Frame parent, boolean modal, String title, String slabel) {

    super(parent, modal);
    setTitle(title);

    jLabel1 = new javax.swing.JLabel();
    textToSearch = new javax.swing.JComboBox();
    textToSearchInValue = new javax.swing.JTextField();
    ignoreCase = new javax.swing.JCheckBox();
    searchValues = new javax.swing.JComboBox();
    searchAttribute = new javax.swing.JCheckBox();
    searchCommand = new javax.swing.JCheckBox();
    searchUseRegexp = new javax.swing.JCheckBox();
    jPanel1 = new javax.swing.JPanel();
    startButton = new javax.swing.JButton();
    cancelButton = new javax.swing.JButton();

    getContentPane().setLayout(null);

    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent evt) {
        setVisible(false);
        dispose();
      }
    });

    jLabel1.setText(slabel);
    jLabel1.setForeground(java.awt.Color.black);
    getContentPane().add(jLabel1);
    jLabel1.setBounds(7, 5, 120, 20);

    textToSearch.setEditable(true);
    textToSearch.addItem("is_polled");
    textToSearch.addItem("is_archived");
    textToSearch.addItem("polling_period");
    textToSearch.addItem("COMMAND/State/is_polled");
    textToSearch.addItem("ATTRIBUTE/my_att/is_polled");
    textToSearch.addItem("ATTRIBUTE/my_att/is_archived");
    textToSearch.setSelectedItem(textToFind);
    getContentPane().add(textToSearch);
    textToSearch.setBounds(130, 8, 210, 20);

    ignoreCase.setText("Ignore case");
    getContentPane().add(ignoreCase);
    ignoreCase.setBounds(17, 70, 223, 24);
    ignoreCase.setSelected(dIgnoreCase);

    searchValues.setEditable(false);
    searchValues.addItem("Do not check value");
    searchValues.addItem("Having value equals to :");
    searchValues.addItem("Having value containing :");
    searchValues.setSelectedIndex(dSearchValues);
    getContentPane().add(searchValues);
    searchValues.setBounds(7, 30, 195, 24);

    textToSearchInValue.setText(textToFindValue);
    getContentPane().add(textToSearchInValue);
    textToSearchInValue.setBounds(215, 30, 125, 24);

    searchAttribute.setText("Search within attribute node");
    getContentPane().add(searchAttribute);
    searchAttribute.setBounds(17, 90, 223, 24);
    searchAttribute.setSelected(dSearchAttributes);

    searchCommand.setText("Search within command node");
    getContentPane().add(searchCommand);
    searchCommand.setBounds(17, 110, 223, 24);
    searchCommand.setSelected(dSearchCommands);

    searchUseRegexp.setText("Use regular expression");
    getContentPane().add(searchUseRegexp);
    searchUseRegexp.setBounds(17, 130, 223, 24);
    searchUseRegexp.setSelected(dSearchUseRegexp);

    Border b = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Search options");
    jPanel1.setBorder(b);
    getContentPane().add(jPanel1);
    jPanel1.setBounds(6, 55, 238, 108);

    startButton.setText("Start");
    getContentPane().add(startButton);
    startButton.setBounds(250, 100, 90, 27);
    startButton.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        ret_value = true;
        setVisible(false);
        dispose();
      }
    });

    cancelButton.setText("Cancel");
    getContentPane().add(cancelButton);
    cancelButton.setBounds(250, 133, 90, 27);
    cancelButton.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        setVisible(false);
        dispose();
      }
    });

    ret_value = false;
  }

  public boolean showDlg() {

    JiveUtils.centerDialog(this,345,165);
    setVisible(true);

    if (ret_value) {
      dIgnoreCase = ignoreCase.isSelected();
      dSearchValues = searchValues.getSelectedIndex();
      dSearchAttributes = searchAttribute.isSelected();
      dSearchCommands = searchCommand.isSelected();
      dSearchUseRegexp = searchUseRegexp.isSelected();
      textToFind = (String) textToSearch.getSelectedItem();
      textToFindValue = textToSearchInValue.getText();
    }

    return ret_value;
  }

  private JLabel jLabel1;
  private JComboBox textToSearch;
  private JCheckBox ignoreCase;
  private JComboBox searchValues;
  private JTextField textToSearchInValue;
  private JCheckBox searchAttribute;
  private JCheckBox searchCommand;
  private JCheckBox searchUseRegexp;
  private JPanel jPanel1;
  private JButton startButton;
  private JButton cancelButton;

  private boolean ret_value;

  // Default option value
  static public boolean dIgnoreCase = false;
  static public int dSearchValues = 0;
  static public boolean dSearchAttributes = false;
  static public boolean dSearchCommands = false;
  static public boolean dSearchUseRegexp = false;
  static public String textToFind = "";
  static public String textToFindValue = "";

  // End of variables declaration
}
