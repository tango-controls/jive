package jive3;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

class DefaultPanel extends JPanel implements ActionListener {

  private JTextArea   value;
  private JScrollPane valueView;
  private JPanel      btnPanel;
  private JButton     refreshButton;
  private TangoNode   src = null;
  private int size;

  DefaultPanel()  {

    setLayout(new BorderLayout());
    value = new JTextArea();
    value.setEditable(false);
    value.setDragEnabled(true);
    value.setFont(new Font("Monospaced",Font.PLAIN,11));
    value.setBorder(BorderFactory.createLoweredBevelBorder());
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

  private void refreshValue() {

    if (src != null) {
      value.setText(src.getValue());
      value.setCaretPosition(0);
      String title = src.getTitle();
      if(size>1) title += " [" + size + " items]";
      Border b = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), title);
      valueView.setBorder(b);
    } else {
      value.setText("");
      value.setCaretPosition(0);
      Border b = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "...");
      valueView.setBorder(b);
    }

  }

}
