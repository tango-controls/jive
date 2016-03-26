package jive3;

import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PropertyEditorDlg extends JDialog implements ActionListener {

  private PropertyNode source;
  private String propName;

  private JButton applyButton;
  private JButton dismissButton;
  private JPanel innerPanel;
  private JPanel buttonPanel;
  private JScrollPane scrollPane;
  private JTextArea textArea;

  PropertyEditorDlg(JFrame parent) {

    super(parent,true);

    innerPanel = new JPanel();
    innerPanel.setLayout(new BorderLayout());

    textArea = new JTextArea();
    scrollPane = new JScrollPane(textArea);
    innerPanel.add(scrollPane,BorderLayout.CENTER);

    buttonPanel = new JPanel();
    buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    innerPanel.add(buttonPanel,BorderLayout.SOUTH);

    applyButton = new JButton("Apply");
    applyButton.addActionListener(this);
    buttonPanel.add(applyButton);

    dismissButton = new JButton("Cancel");
    dismissButton.addActionListener(this);
    buttonPanel.add(dismissButton);

    innerPanel.setPreferredSize(new Dimension(640,480));
    setContentPane(innerPanel);
    
  }

  public void setSource(PropertyNode source,String propName,String propValue) {

    this.source = source;
    this.propName = propName;
    textArea.setText(propValue);
    setTitle("Edit property " + source.getName()+"/"+propName);
    ATKGraphicsUtils.centerDialog(this);
    
  }

  public void actionPerformed(ActionEvent e) {

    Object src = e.getSource();

    if( src==dismissButton ) {
      setVisible(false);
    } else if (src==applyButton) {
      source.setProperty(propName,textArea.getText());
      setVisible(false);
    }

  }

}
