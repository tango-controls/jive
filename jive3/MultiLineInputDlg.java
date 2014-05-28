package jive3;

import fr.esrf.tangoatk.widget.util.ATKConstant;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import jive.JiveUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Multiline text input
 */
public class MultiLineInputDlg extends JDialog implements ActionListener {

  boolean okFlag;

  private JButton applyButton;
  private JButton cancelButton;
  private JTextArea inputText;

  MultiLineInputDlg(JFrame parent,String title,String applyText,String[] defaultValue) {

    super(parent, true);

    okFlag = false;

    JPanel innerPanel = new JPanel();
    innerPanel.setPreferredSize(new Dimension(400,300));
    innerPanel.setLayout(new BorderLayout());

    inputText = new JTextArea();
    inputText.setText(JiveUtils.stringArrayToString(defaultValue));
    inputText.setEditable(true);
    JScrollPane textView = new JScrollPane(inputText);
    innerPanel.add(textView,BorderLayout.CENTER);

    JLabel descrLabel = new JLabel("Enter property value:");
    descrLabel.setFont(ATKConstant.labelFont);
    innerPanel.add(descrLabel,BorderLayout.NORTH);

    JPanel btnPanel = new JPanel();
    btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

    applyButton = new JButton(applyText);
    applyButton.addActionListener(this);
    btnPanel.add(applyButton);

    cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(this);
    btnPanel.add(cancelButton);

    innerPanel.add(btnPanel,BorderLayout.SOUTH);

    setContentPane(innerPanel);
    setTitle(title);
    pack();

  }

  String getText() {
    return inputText.getText();
  }

  public void actionPerformed(ActionEvent evt) {

    Object src = evt.getSource();
    if( src==applyButton ) {
      okFlag = true;
      setVisible(false);
    } else if ( src==cancelButton ) {
      okFlag = false;
      setVisible(false);
    }

  }

  /**
   * Return a string array containing line(s) of text.
   * @param parent JFrame parent
   * @param title Title of the dialog
   * @param applyText Text displayed in the apply button
   * @param delfaultValue Value displayed in the text area
   * @return Line(s) of text or null is cancel is pressed
   */
  public static String[] getInputText(JFrame parent,String title,String applyText,String[] delfaultValue) {

    MultiLineInputDlg dlg = new MultiLineInputDlg(parent,title,applyText,delfaultValue);
    ATKGraphicsUtils.centerDialog(dlg);
    dlg.setVisible(true);

    if(!dlg.okFlag) {
      return null;
    } else {
      return JiveUtils.makeStringArray(dlg.getText());
    }

  }

}
