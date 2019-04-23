package jive;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Message dialog (multiline)
 */
public class MessageDialog extends JDialog {

  public static final int ERROR_MESSAGE = 1;
  public static final int WARNING_MESSAGE = 2;
  public static final int INFORMATION_MESSAGE = 3;


  static void showMessageDialog(Frame parent, String title, String[] msg) {

    MessageDialog dlg = new MessageDialog(parent,title,msg,null);
    dlg.showDialog();

  }

  static void showMessageDialog(Frame parent, String title, String[] msg,int icon) {

    MessageDialog dlg;

    switch(icon) {
      case ERROR_MESSAGE:
        dlg = new MessageDialog(parent,title,msg,UIManager.getIcon("OptionPane.errorIcon"));
        break;
      case WARNING_MESSAGE:
        dlg = new MessageDialog(parent,title,msg,UIManager.getIcon("OptionPane.warningIcon"));
        break;
      case INFORMATION_MESSAGE:
        dlg = new MessageDialog(parent,title,msg,UIManager.getIcon("OptionPane.informationIcon"));
        break;
      default:
        dlg = new MessageDialog(parent,title,msg,null);
        break;
    }

    dlg.showDialog();

  }

  private JScrollPane scroll;
  private JTextArea textArea;
  private JLabel label;
  private JButton closeButton;
  private JPanel innerPanel;
  private JPanel btnPanel;
  private JButton iconButton;

  MessageDialog(Frame parent, String title, String[] msg, Icon icon) {

    super(parent,true);
    innerPanel = new JPanel();
    innerPanel.setLayout(new BorderLayout());

    String message = JiveUtils.stringArrayToString(msg);
    if(msg.length>1) {

      textArea = new JTextArea(message);
      textArea.setMargin(new Insets(5,5,5,10));
      textArea.setEditable(false);
      textArea.setBackground(innerPanel.getBackground());
      textArea.setFont(UIManager.getFont("Label.font"));
      scroll=new JScrollPane(textArea);
      scroll.setBorder(null);
      innerPanel.add(scroll, BorderLayout.CENTER);

      Dimension td = scroll.getViewport().getPreferredSize();
      if(td.width<400) td.width = 400;
      if(td.width>800) td.width = 600;
      if(td.height<60) td.height = 60;
      if(td.height>400) td.height = 400;
      scroll.getViewport().setPreferredSize(td);

    } else {

      label = new JLabel(message);
      innerPanel.add(label, BorderLayout.CENTER);

      Dimension td = label.getPreferredSize();
      if(td.width<400) td.width = 400;
      if(td.width>800) td.width = 600;
      if(td.height<60) td.height = 60;
      if(td.height>400) td.height = 400;
      label.setPreferredSize(td);

    }

    closeButton = new JButton("OK");
    closeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });

    btnPanel = new JPanel();
    btnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    btnPanel.add(closeButton);

    innerPanel.add(btnPanel,BorderLayout.SOUTH);

    if(icon!=null) {
      iconButton = new JButton(icon);
      iconButton.setBorderPainted(false);
      iconButton.setContentAreaFilled(false);
      iconButton.setFocusPainted(false);
      iconButton.setOpaque(false);
      innerPanel.add(iconButton, BorderLayout.WEST);
    }

    setContentPane(innerPanel);

    setTitle(title);

  }

  void showDialog() {

    JiveUtils.centerDialog(this);
    setVisible(true);

  }

  public static void main(String[] args) {

    String[] messages = new String[20];
    for(int i=0;i<messages.length;i++) {
      messages[i] = "This is the line " + i;
    }
    MessageDialog.showMessageDialog(null, "Error", messages, MessageDialog.WARNING_MESSAGE);

  }

}
