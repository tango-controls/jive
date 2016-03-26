package jive;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 *
 * @author  pons
 */

public class ServerDlg extends JDialog {

  private JTextField serverText;
  private JTextField classText;
  private JTextArea deviceText;
  private JScrollPane deviceView;
  private JButton ok;
  private JButton cancel;

  private JPanel jp;

  boolean ret_value = false;
  private final static Color backColor = new Color(240,240,240);

  // Construction without predefined values
  public ServerDlg(Frame parent) {
    super(parent, true);
    getContentPane().setLayout(null);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent evt) {
        ret_value = false;
        setVisible(false);
        dispose();
      }
    });

    setTitle("Create/Edit a server");

    jp = new JPanel(null);
    jp.setBackground(backColor);
    jp.setBorder(BorderFactory.createLoweredBevelBorder());
    getContentPane().add(jp);

    serverText = new JTextField();
    serverText.setEditable(true);
    serverText.setBackground(backColor);
    serverText.setBorder(BorderFactory.createTitledBorder("Server  (ServerName/Instance)"));

    classText = new JTextField();
    classText.setEditable(true);
    classText.setBackground(backColor);
    classText.setBorder(BorderFactory.createTitledBorder("Class"));

    deviceText = new JTextArea();
    deviceText.setEditable(true);
    deviceText.setBackground(backColor);
    deviceView = new JScrollPane(deviceText);
    deviceView.setBorder(BorderFactory.createTitledBorder("Devices"));
    deviceView.setBackground(backColor);

    ok = new JButton();
    ok.setText("Register server");
    getContentPane().add(ok);

    cancel = new JButton();
    cancel.setText("Cancel");
    getContentPane().add(cancel);

    jp.add(serverText);
    jp.add(classText);
    jp.add(deviceView);
    serverText.setBounds(5, 5, 380, 40);
    classText.setBounds(5, 45, 380, 40);
    deviceView.setBounds(5, 85, 380, 145);
    jp.setBounds(5, 8, 389, 234);

    ok.setBounds(5, 248, 150, 27);
    cancel.setBounds(315, 248, 80, 27);

    cancel.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        ret_value = false;
        setVisible(false);
        dispose();
      }
    });

    ok.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        // Check if server name has a correct format
        String s = serverText.getText();
        if (s.indexOf('/') == -1) {
          JiveUtils.showJiveError("Server name must be entered as Name/Instance");
          return;
        } else if (s.indexOf('/') != s.lastIndexOf('/')) {
          JiveUtils.showJiveError("Server name must be entered as Name/Instance");
          return;
        } else {
          ret_value = true;
        }

        setVisible(false);
        dispose();
      }
    });

  }

  public void setValidFields(boolean s, boolean c) {
    serverText.setEditable(s);
    classText.setEditable(c);
  }

  public void setDefaults(String s, String c) {
    serverText.setText(s);
    classText.setText(c);
  }

  public boolean showDlg() {
    JiveUtils.centerDialog(this,400,280);
    setVisible(true);
    return ret_value;
  }

  public String getServerName() {
    return serverText.getText();
  }

  public String getClassName() {
    return classText.getText();
  }

  public String[] getDeviceNames() {

    String value = deviceText.getText();
    String[] splitted = value.split("\n");
    String[] ret = new String[1];
    int i,j;

    for (i = 0, j = 0; i < splitted.length; i++) {
      if (splitted[i].length() > 0) j++;
    }

    ret = new String[j];

    for (i = 0, j = 0; i < splitted.length; i++) {
      if (splitted[i].length() > 0) {
        ret[j] = splitted[i];
        j++;
      }
    }

    return ret;

  }

}

