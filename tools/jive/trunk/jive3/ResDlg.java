package jive3;

import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import jive.JiveUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileReader;
import java.io.IOException;

/**
 * A confirmation dialog for loading resources
 */
public class ResDlg extends JDialog implements ActionListener {

  private JTextArea   resText;
  private JScrollPane resScrollPane;
  private JButton     loadButton;
  private JButton     cancelButton;
  private boolean     okFlag;

  public ResDlg(JFrame parent,String fileName) {

    super(parent,true);

    JPanel innerPanel = new JPanel();
    innerPanel.setLayout(new BorderLayout());

    resText = new JTextArea();
    resText.setFont(new Font("Monospaced",Font.PLAIN,12));
    resScrollPane = new JScrollPane(resText);
    resScrollPane.setPreferredSize(new Dimension(600,400));
    innerPanel.add(resScrollPane,BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

    loadButton = new JButton("Load");
    loadButton.addActionListener(this);
    buttonPanel.add(loadButton);
    cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(this);
    buttonPanel.add(cancelButton);
    innerPanel.add(buttonPanel,BorderLayout.SOUTH);

    readFile(fileName);
    setTitle("Load resource [" + fileName + "]");
    setContentPane(innerPanel);

  }

  private void readFile(String fileName) {

    try {
      StringBuffer sb = new StringBuffer();
      FileReader f = new FileReader(fileName);
      while(f.ready()) {
        sb.append((char)f.read());
      }
      f.close();
      resText.setText(sb.toString());
    } catch (IOException e) {
      JiveUtils.showJiveError("Cannot read " + fileName + "\n" + e.getMessage());
    }

  }

  public void actionPerformed(ActionEvent e) {

    Object src = e.getSource();
    if( src==loadButton ) {
      okFlag=true;
      setVisible(false);
    } else if ( src==cancelButton ) {
      setVisible(false);
    }

  }

  public boolean showDlg() {

    okFlag = false;
    ATKGraphicsUtils.centerDialog(this);
    setVisible(true);
    return okFlag;
    
  }

}
