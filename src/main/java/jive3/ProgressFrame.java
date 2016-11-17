package jive3;

import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ATKConstant;

import javax.swing.*;
import java.awt.*;

/**
 * A class to display a progress frame
 */
public class ProgressFrame extends JFrame {

  static ProgressFrame prgFrame = null;

  static public void displayProgress(String title) {
    if(prgFrame==null)
      prgFrame = new ProgressFrame();
    ATKGraphicsUtils.centerFrameOnScreen(prgFrame);
    prgFrame.setTitle(title);
    prgFrame.setValue("",0);
    prgFrame.setVisible(true);
  }

  static public void setProgress(String message,int prg) {
    if(prgFrame!=null) prgFrame.setValue(message,prg);
  }

  static public void hideProgress() {
    if(prgFrame!=null) prgFrame.setVisible(false);
  }

  // -------------------------------------------------------

  private JProgressBar prgBar;
  private JLabel       prgLabel;

  ProgressFrame() {

    JPanel innerPanel = new JPanel(null);
    innerPanel.setPreferredSize(new Dimension(300,50));
    prgBar = new JProgressBar();
    prgBar.setMaximum(100);
    prgBar.setMinimum(0);
    prgBar.setBounds(5,30,290,15);
    innerPanel.add(prgBar);
    prgLabel = new JLabel("");
    prgLabel.setFont(ATKConstant.labelFont);
    prgLabel.setBounds(5,5,290,25);
    innerPanel.add(prgLabel);
    setContentPane(innerPanel);

  }

  private void forceRepaint() {
    Graphics g = getGraphics();
    paint(g);
    g.dispose();
  }

  void setValue(String txt,int p) {
    if(p>100) p=100;
    if(p<0) p=0;
    prgBar.setValue(p);
    prgLabel.setText(txt);
    forceRepaint();
  }

}
