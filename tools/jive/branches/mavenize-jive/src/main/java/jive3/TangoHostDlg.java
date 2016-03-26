package jive3;

import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Created by PONS on 28/11/14.
 */
public class TangoHostDlg extends JDialog implements ActionListener,KeyListener {

  private JButton     okButton;
  private JButton     cancelButton;
  private JComboBox   hostCombo;
  private JLabel      infoLabel;
  private boolean     okFlag;

  public TangoHostDlg(JFrame parent,String[] knowHost) {

    super(parent,true);

    JPanel innerPanel = new JPanel();
    innerPanel.setLayout(new BorderLayout());
    innerPanel.setBorder(new EmptyBorder(10,10,10,10));

    infoLabel = new JLabel("Enter tango host (ex gizmo:20000)");
    innerPanel.add(infoLabel, BorderLayout.NORTH);

    JPanel comboPanel = new JPanel();
    comboPanel.setLayout(new BorderLayout());
    comboPanel.setBorder(new EmptyBorder(5,0,5,0));

    hostCombo = new JComboBox();
    hostCombo.getEditor().getEditorComponent().addKeyListener(this);
    for(int i=0;i<knowHost.length;i++)
      hostCombo.addItem(knowHost[i]);
    hostCombo.setEditable(true);
    hostCombo.setSelectedItem("");
    comboPanel.add(hostCombo,BorderLayout.CENTER);
    innerPanel.add(comboPanel,BorderLayout.CENTER);

    JPanel btnPanel = new JPanel();
    btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

    okButton = new JButton("Ok");
    okButton.addActionListener(this);
    btnPanel.add(okButton);

    cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(this);
    btnPanel.add(cancelButton);

    innerPanel.add(btnPanel,BorderLayout.SOUTH);

    setTitle("Change tango host");
    setContentPane(innerPanel);
    ATKGraphicsUtils.centerDialog(this);

  }

  public void keyTyped(KeyEvent e) {}
  public void keyPressed(KeyEvent e) {}
  public void keyReleased(KeyEvent e) {
    if(e.getKeyCode()==KeyEvent.VK_ENTER) {
      okFlag = true;
      setVisible(false);
    }
  }

  public void actionPerformed(ActionEvent evt) {

    Object src = evt.getSource();

    if( src==okButton ) {
      okFlag = true;
      setVisible(false);
    } else if ( src==cancelButton ) {
      okFlag = false;
      setVisible(false);
    }

  }

  public String getTangoHost() {

    setVisible(true);
    if(okFlag) {
      return hostCombo.getSelectedItem().toString();
    } else {
      return null;
    }

  }

}
