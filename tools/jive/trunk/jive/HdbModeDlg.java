package jive;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import fr.soleil.TangoHdb.HdbTools.Mode.*;

/*
 * HdbModeDlg.java
 *
 * Created on September 4, 2002, 1:32 PM
 */

public class HdbModeDlg extends JDialog implements ActionListener {

  private static int dlgWidth = 400;
  private static int dlgHeight = 265;
  private static int nbMode = 3;
  private static int hdbMode = 0;

  static private Mode[] panelsMode = {
    new PeriodicMode(dlgWidth - 20),
    new AbsoluteMode(dlgWidth - 20),
    new RelativeMode(dlgWidth - 20)
  };

  private JComboBox comboMode;
  private JLabel lbMode;
  private JButton okBtn;
  private JButton okAllBtn;
  private JButton cancelBtn;
  private JPanel descPanel;
  private JTextArea descText;
  private int ret_value;
  private boolean multipleSelection;

  /** Creates new form SearchDlg */
  public HdbModeDlg(String title,java.awt.Frame fparent) {
    super(fparent,title,true);
    multipleSelection=false;
    initComponents();
  }

  public HdbModeDlg(String title,java.awt.Dialog dparent) {
    super(dparent,title,true);
    multipleSelection=true;
    initComponents();
  }

  public void initComponents() {

    getContentPane().setLayout(null);

    lbMode = new JLabel("Mode");
    getContentPane().add(lbMode);
    lbMode.setBounds(5, 5, 100, 25);

    // Create combo mode
    comboMode = new JComboBox();
    getContentPane().add(comboMode);
    comboMode.setBounds(105, 5, 280, 25);
    comboMode.setEditable(false);
    comboMode.removeAllItems();

    for (int i = 0; i < nbMode; i++) {
      comboMode.addItem(panelsMode[i].getName());
      getContentPane().add(panelsMode[i]);
      panelsMode[i].setBounds(5, 30, 380, 100);
      panelsMode[i].setVisible(false);
    }

    comboMode.setSelectedIndex(hdbMode);
    panelsMode[hdbMode].setVisible(true);

    comboMode.addActionListener(this);

    descPanel = new JPanel();
    descPanel.setLayout(new GridLayout(1, 0));
    javax.swing.border.TitledBorder b = BorderFactory.createTitledBorder("Mode descripton");
    b.setTitleFont(new Font("Dialog", Font.PLAIN, 11));
    descPanel.setBorder(b);
    getContentPane().add(descPanel);
    descPanel.setBounds(5, 132, 380, 70);

    descText = new JTextArea("");
    descText.setEditable(false);
    descText.setBackground(getBackground());
    descText.setText(panelsMode[hdbMode].getModeDescription());
    descPanel.add(descText);

    okBtn = new JButton("Apply");
    okBtn.setToolTipText("Apply this mode configuration for the selected attribute only");
    getContentPane().add(okBtn);
    okBtn.setBounds(5, 205, 120, 25);
    okBtn.addActionListener(this);

    if(multipleSelection) {
      okAllBtn = new JButton("Apply to all");
      okAllBtn.setToolTipText("Apply this mode configuration to all \"is_archivied\" HDB items selected.");
      getContentPane().add(okAllBtn);
      okAllBtn.setBounds(130, 205, 125, 25);
      okAllBtn.addActionListener(this);
    }

    cancelBtn = new JButton("Cancel");
    getContentPane().add(cancelBtn);
    cancelBtn.setBounds(260, 205, 120, 25);
    cancelBtn.addActionListener(this);

    ret_value = 0;

    // Place the window
    centerWindow();

  }

  public void centerWindow() {
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Dimension scrsize = toolkit.getScreenSize();
    Dimension appsize = new Dimension(dlgWidth, dlgHeight);
    int x = (scrsize.width - appsize.width)/2;
    int y = (scrsize.height - appsize.height)/2;
    setBounds(x, y, appsize.width, appsize.height);
  }

  // Returns a mode object if ok else returns null.
  public fr.soleil.TangoHdb.HdbTools.Mode.Mode getMode() {

    setVisible(true);

    if (ret_value>0) {

      try {
        switch (hdbMode) {
          case 0: // Periodic
            {
              PeriodicMode panel = (PeriodicMode) panelsMode[hdbMode];
              ModePeriode m = new ModePeriode();
              m.setFrequency(panel.getPeriod());
              fr.soleil.TangoHdb.HdbTools.Mode.Mode ret = new fr.soleil.TangoHdb.HdbTools.Mode.Mode();
              ret.setModeP(m);
              return ret;
            }
          case 1: // Absolu
            {
              AbsoluteMode panel = (AbsoluteMode) panelsMode[hdbMode];
              ModeAbsolu m = new ModeAbsolu();
              m.setFrequency(panel.getPeriod());
              m.setValInf(panel.getMinimum());
              m.setValSup(panel.getMaximum());
              fr.soleil.TangoHdb.HdbTools.Mode.Mode ret = new fr.soleil.TangoHdb.HdbTools.Mode.Mode();
              ret.setModeA(m);
              return ret;
            }
          case 2: // Relatif
            {
              RelativeMode panel = (RelativeMode) panelsMode[hdbMode];
              ModeRelatif m = new ModeRelatif();
              m.setFrequency(panel.getPeriod());
              m.setPercentInf(panel.getMinimum());
              m.setPercentSup(panel.getMaximum());
              fr.soleil.TangoHdb.HdbTools.Mode.Mode ret = new fr.soleil.TangoHdb.HdbTools.Mode.Mode();
              ret.setModeR(m);
              return ret;
            }
        }
      } catch (Exception e) {
      }

    }
    return null;

  }

  public boolean getContinueState() {
    return ret_value==2;
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == comboMode) {
      int id = comboMode.getSelectedIndex();
      for (int i = 0; i < nbMode; i++) panelsMode[i].setVisible(i == id);
      descText.setText(panelsMode[id].getModeDescription());
    }
    if (e.getSource() == okBtn) {
      ret_value = 1;
      setVisible(false);
    }
    if (e.getSource() == okAllBtn) {
      ret_value = 2;
      setVisible(false);
    }
    if (e.getSource() == cancelBtn) {
      ret_value = 0;
      setVisible(false);
    }
  }

  // End of variables declaration
}

// Base mode class

class Mode extends JPanel {
  private JLabel lbPeriod;
  private JTextField txtPeriod;

  Mode(int desiredWidth) {
    setLayout(null);
    javax.swing.border.TitledBorder b = BorderFactory.createTitledBorder("Mode Parameters");
    b.setTitleFont(new Font("Dialog", Font.PLAIN, 11));
    setBorder(b);

    lbPeriod = new JLabel("Period (ms)");
    add(lbPeriod);
    lbPeriod.setBounds(5, 15, desiredWidth / 2 - 5, 25);

    txtPeriod = new JTextField();
    txtPeriod.setText("10000");
    add(txtPeriod);
    txtPeriod.setBounds(desiredWidth / 2, 15, desiredWidth / 2 - 5, 25);

  }

  public String getName() {
    return "";
  }

  public String getModeDescription() {
    return "";
  }

  public int getPeriod() throws NumberFormatException {
    int p;
    try {
      p = Integer.parseInt(txtPeriod.getText());
    } catch (NumberFormatException e) {
      JiveUtils.showJiveError("Invalid period for archiving mode.");
      // Rethrow the exception
      throw e;
    }
    return p;
  }

}

// Class for Periodic mode

class PeriodicMode extends Mode {

  public PeriodicMode(int desiredWidth) {
    super(desiredWidth);
  }

  public String getName() {
    return "Periodic Mode";
  };

  public String getModeDescription() {
    return "Value is archivied at each clock pulse";
  };

}

// Class for Absolute Mode

class AbsoluteMode extends Mode {

  private JLabel lbMin;
  private JLabel lbMax;
  private JTextField txtMin;
  private JTextField txtMax;

  public AbsoluteMode(int desiredWidth) {
    super(desiredWidth);

    lbMin = new JLabel("Minimun value");
    add(lbMin);
    lbMin.setBounds(5, 40, desiredWidth / 2 - 5, 25);

    txtMin = new JTextField();
    add(txtMin);
    txtMin.setBounds(desiredWidth / 2, 40, desiredWidth / 2 - 5, 25);

    lbMax = new JLabel("Maximun value");
    add(lbMax);
    lbMax.setBounds(5, 65, desiredWidth / 2 - 5, 25);

    txtMax = new JTextField();
    add(txtMax);
    txtMax.setBounds(desiredWidth / 2, 65, desiredWidth / 2 - 5, 25);

  }

  public String getName() {
    return "Absolute Mode";
  };

  public String getModeDescription() {
    return "Value is archivied when the value goes out of given range";
  };

  public int getMinimum() throws NumberFormatException {
    int p;
    try {
      p = Integer.parseInt(txtMin.getText());
    } catch (NumberFormatException e) {
      JiveUtils.showJiveError("Invalid minimum value for archiving mode.");
      // Rethrow the exception
      throw e;
    }
    return p;
  }

  public int getMaximum() throws NumberFormatException {
    int p;
    try {
      p = Integer.parseInt(txtMax.getText());
    } catch (NumberFormatException e) {
      JiveUtils.showJiveError("Invalid maximum value for archiving mode.");
      // Rethrow the exception
      throw e;
    }
    return p;
  }

}

// Class for Absolute Mode

class RelativeMode extends Mode {

  private JLabel lbMin;
  private JLabel lbMax;
  private JTextField txtMin;
  private JTextField txtMax;

  public RelativeMode(int desiredWidth) {
    super(desiredWidth);

    lbMin = new JLabel("Max percent");
    add(lbMin);
    lbMin.setBounds(5, 40, desiredWidth / 2 - 5, 25);

    txtMin = new JTextField();
    add(txtMin);
    txtMin.setBounds(desiredWidth / 2, 40, desiredWidth / 2 - 5, 25);

    lbMax = new JLabel("Min percent");
    add(lbMax);
    lbMax.setBounds(5, 65, desiredWidth / 2 - 5, 25);

    txtMax = new JTextField();
    add(txtMax);
    txtMax.setBounds(desiredWidth / 2, 65, desiredWidth / 2 - 5, 25);

  }

  public String getName() {
    return "Relative Mode";
  };

  public String getModeDescription() {
    return "Value is archivied when the value becomes greater or smaller\nthan a percent of the last recorded value";
  };

  public int getMinimum() throws NumberFormatException {
    int p;
    try {
      p = Integer.parseInt(txtMin.getText());
    } catch (NumberFormatException e) {
      JiveUtils.showJiveError("Invalid minimum value for archiving mode.");
      // Rethrow the exception
      throw e;
    }
    return p;
  }

  public int getMaximum() throws NumberFormatException {
    int p;
    try {
      p = Integer.parseInt(txtMax.getText());
    } catch (NumberFormatException e) {
      JiveUtils.showJiveError("Invalid maximum value for archiving mode.");
      // Rethrow the exception
      throw e;
    }
    return p;
  }

}
