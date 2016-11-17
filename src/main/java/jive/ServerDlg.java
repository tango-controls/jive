package jive;


import jive3.IServerAction;
import jive3.JTextTips;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 *
 * @author  pons
 */

public class ServerDlg extends JFrame {

  private JTextTips serverText;
  private JTextTips classText;
  private JTextArea deviceText;
  private JScrollPane deviceView;
  private JButton ok;
  private JButton cancel;

  private JPanel jp;

  private final static Color backColor = new Color(240,240,240);
  private final IServerAction okAction;

  // Construction without predefined values
  public ServerDlg(IServerAction action) {

    okAction = action;

    getContentPane().setLayout(null);
    getContentPane().setPreferredSize(new Dimension(400,280));

    setTitle("Create/Edit a server");

    jp = new JPanel(null);
    jp.setBackground(backColor);
    jp.setBorder(BorderFactory.createLoweredBevelBorder());
    getContentPane().add(jp);

    serverText = new JTextTips();
    serverText.setEditable(true);
    serverText.setBackground(backColor);
    serverText.setBorder(BorderFactory.createTitledBorder("Server  (ServerName/Instance)"));

    classText = new JTextTips();
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

    cancel.addActionListener(
        new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            setVisible(false);
            dispose();
          }
        }
    );

    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        // Check if server name has a correct format
        String serverName = serverText.getText();
        if (serverName.indexOf('/') == -1) {
          JiveUtils.showJiveError("Server name must be entered as Name/Instance");
          return;
        } else if (serverName.indexOf('/') != serverName.lastIndexOf('/')) {
          JiveUtils.showJiveError("Server name must be entered as Name/Instance");
          return;
        } else {
          okAction.doJob(serverName, getClassName(), getDeviceNames());
        }

        setVisible(false);
        dispose();
      }
    });

  }

  public void setServerList(String[] list) {
    serverText.setTips(list);
  }

  public void setClassList(String[] list) {
    classText.setTips(list);
  }

  public void setValidFields(boolean s, boolean c) {
    serverText.setEditable(s);
    classText.setEditable(c);
  }

  public void setDefaults(String s, String c) {
    serverText.setTextInternal(s);
    classText.setTextInternal(c);
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
    String[] ret;
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

