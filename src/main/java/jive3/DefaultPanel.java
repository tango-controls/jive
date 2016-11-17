package jive3;

import jive.JiveUtils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

class DefaultPanel extends JPanel implements ActionListener {

  private JEditorPane value;
  private JScrollPane valueView;
  private JPanel      btnPanel;
  private JButton     refreshButton;
  private TangoNode   src = null;
  private int size;

  DefaultPanel()  {

    setLayout(new BorderLayout());
    value = new JEditorPane();
    value.setEditable(false);
    value.setDragEnabled(true);
    value.setBorder(BorderFactory.createLoweredBevelBorder());
    value.setContentType("text/html");
    valueView = new JScrollPane(value);
    Border b = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"...");
    valueView.setBorder(b);
    add(valueView,BorderLayout.CENTER);
    btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    refreshButton = new JButton("Refresh");
    refreshButton.addActionListener(this);
    btnPanel.add(refreshButton);
    add(btnPanel,BorderLayout.SOUTH);

  }

  public void actionPerformed(ActionEvent e) {
    Object src = e.getSource();
    if( src==refreshButton ) {
      refreshValue();
    }
  }

  void setSource(TangoNode src,int size) {
    this.src = src;
    this.size = size;
    refreshValue();
  }

  public void setText(String str) {

    StringBuffer strBuff = new StringBuffer();

    strBuff.append("<html><body>\n" +
                   "<div style='white-space:nowrap; font-family:\"Monospaced\"; font-size:9px;'><pre>\n");
    strBuff.append(str);
    strBuff.append("</pre></div></body></html>\n");

    value.setText(strBuff.toString());

  }

  private void refreshValue() {

    if (src != null) {
      setText(src.getValue());
      value.setCaretPosition(0);
      String title = src.getTitle();
      if(size>1) title += " [" + size + " items]";
      Border b = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title);
      valueView.setBorder(b);
    } else {
      setText("");
      value.setCaretPosition(0);
      Border b = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "...");
      valueView.setBorder(b);
    }

  }

}
