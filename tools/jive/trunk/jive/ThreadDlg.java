package jive;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Class for handling a long task in an asynchronous way without blocking GUI
 *
 * @author  pons
 */

public class ThreadDlg extends JDialog {

  static public boolean   stopflag;
  static public ThreadDlg progDlg=null;

  private JLabel textArea;
  private JButton okButton;
  private Thread subProc;
  private JPanel innerPanel;
  private JProgressBar progressBar;
  private boolean hasProgress;

  // Construction
  public ThreadDlg(Frame parent, String title, boolean progress, Thread process) {

    super(parent, true);
    getContentPane().setLayout(null);
    okButton = new JButton("Stop");
    okButton.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        if (!stopflag) {
          stopflag = true;
        } else {
          JOptionPane.showMessageDialog(null, "Your cancel request has already been registered.\nA thread may be blocked in a device timeout.\nWait a while and if nothing happens restart jive.");
        }
      }

    });

    innerPanel = new JPanel();
    innerPanel.setOpaque(false);
    innerPanel.setBorder(BorderFactory.createEtchedBorder());

    this.hasProgress = progress;
    if (!hasProgress) {
      textArea = new JLabel();
      textArea.setText(title);
      textArea.setBounds(5, 5, 200, 40);
      textArea.setHorizontalAlignment(JLabel.CENTER);
      getContentPane().add(textArea);
    } else {
      progressBar = new JProgressBar();
      progressBar.setMaximum(100);
      progressBar.setMinimum(0);
      progressBar.setValue(0);
      progressBar.setBounds(5, 5, 200, 40);
      getContentPane().add(progressBar);
    }

    okButton.setBounds(50, 52, 100, 23);
    innerPanel.setBounds(0, 0, 210, 80);

    getContentPane().add(okButton);
    getContentPane().add(innerPanel);
    setUndecorated(true);

    // Add a thread listener
    subProc = process;

    stopflag = false;

    progDlg = this;
    // Add window listener to start the subProc
    // when the dialog is displayed
    addWindowListener(new WindowAdapter() {
      public void windowOpened(WindowEvent e) {
        subProc.start();
      }
    });

  }

  public void setProgress(int p) {
    if (hasProgress) progressBar.setValue(p);
  }

  public void showDlg() {

    JiveUtils.centerDialog(this,210,80);
    setVisible(true);

  }
  
  public void hideDlg() {
    setVisible(false);
    progDlg = null;
  }

}
