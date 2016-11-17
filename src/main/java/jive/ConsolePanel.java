package jive;

import fr.esrf.tangoatk.widget.util.ATKConstant;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Console panel
 */
class ConsolePanel extends JPanel implements ActionListener {

  private JTextArea    textArea;
  private JScrollPane  textView;
  private JButton      clearBtn;
  private JButton      closeBtn;
  private StringBuffer consoleBuffer;

  ConsolePanel() {

    setLayout(new BorderLayout());

    textArea = new JTextArea();
    textArea.setEditable(false);
    textArea.setFont( new Font("monospaced",Font.PLAIN,12) );
    textView = new JScrollPane(textArea);
    add(textView,BorderLayout.CENTER);
    textView.setPreferredSize(new Dimension(0,280));

    JPanel innerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    clearBtn = new JButton("Clear history");
    clearBtn.setFont(ATKConstant.labelFont);
    clearBtn.addActionListener(this);
    closeBtn = new JButton("Dismiss");
    closeBtn.setFont(ATKConstant.labelFont);
    closeBtn.addActionListener(this);
    innerPanel.add(clearBtn);
    innerPanel.add(closeBtn);
    add(innerPanel,BorderLayout.SOUTH);
    consoleBuffer = new StringBuffer();

  }

  public void actionPerformed(ActionEvent e) {

    Object src = e.getSource();
    if(src==clearBtn) {
      consoleBuffer = new StringBuffer(0);
      textArea.setText("");
    } else if(src==closeBtn) {
      ATKGraphicsUtils.getWindowForComponent(this).dispose();
      ATKGraphicsUtils.getWindowForComponent(this).setVisible(false);
    }

  }

  void print(String s) {
    consoleBuffer.append(s);
    textArea.setText(consoleBuffer.toString());
  }

}
