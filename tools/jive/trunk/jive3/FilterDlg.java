package jive3;

import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Filtering dialog
 */
public class FilterDlg extends JDialog implements ActionListener {

  private JButton    okBtn;
  private JButton    cancelBtn;
  private JLabel     nameLabel;
  private JTextField filterText;
  private boolean    okFlag;

  public FilterDlg(JFrame parent) {

    super(parent,true);
    JPanel innerPanel = new JPanel();
    innerPanel.setLayout(null);
    innerPanel.setPreferredSize(new Dimension(320,70));

    nameLabel = new JLabel();
    nameLabel.setBounds(5,10,100,25);
    innerPanel.add(nameLabel);
    filterText = new JTextField();
    filterText.setEditable(true);
    filterText.setBounds(110,10,205,25);
    innerPanel.add(filterText);

    okBtn = new JButton("Apply filter");
    okBtn.setBounds(5,40,150,25);
    okBtn.addActionListener(this);
    innerPanel.add(okBtn);

    cancelBtn = new JButton("Cancel");
    cancelBtn.setBounds(225,40,90,25);
    cancelBtn.addActionListener(this);
    innerPanel.add(cancelBtn);
    
    setContentPane(innerPanel);
    setTitle("Filtering");

  }

  public void setLabelName(String name) {
    nameLabel.setText(name);
  }

  public void setFilter(String filter) {
    filterText.setText(filter);
  }

  public String getFilterText() {
    return filterText.getText();
  }

  public boolean showDialog() {

    okFlag = false;
    ATKGraphicsUtils.centerDialog(this);
    setVisible(true);
    return okFlag;

  }

  public void actionPerformed(ActionEvent e) {

    Object src = e.getSource();
    if( src==okBtn ) {
      okFlag = true;
      setVisible(false);
    } else if ( src==cancelBtn ) {
      okFlag = false;
      setVisible(false);
    }

  }

}
