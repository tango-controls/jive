package jive3;

import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PropertyEditorDlg extends JDialog implements ActionListener {

  private PropertyNode source;
  private String propName;

  private JTextField searchText;
  private JButton searchButton;
  private JButton searchNextButton;
  private JCheckBox searchMathCase;
  private JButton applyButton;
  private JButton dismissButton;
  private JPanel innerPanel;
  private JPanel buttonPanel;
  private JScrollPane scrollPane;
  private JTextEditor textArea;

  private final static String separatorList[] = { "," , ";" , "=" , "(" , ")" , "{" , "}" };
  private final static Color separatorColor = new Color(160,70,160);

  private final static String keyword1List[] = {"true","false"};
  private final static Color keyword1Color = new Color(30,120,30);

  private final static String keyword2List[] = {"DevBoolean","DevShort","DevUShort","DevULong","DevLong","DevULong64","DevLong64","DevFloat","DevDouble","DevEnum"};
  private final static Color keyword2Color = new Color(30,120,120);

  PropertyEditorDlg(JFrame parent) {

    super(parent,true);

    innerPanel = new JPanel();
    innerPanel.setLayout(new BorderLayout());

    textArea = new JTextEditor();
    textArea.setBackground(Color.WHITE);
    scrollPane = new JScrollPane(textArea);
    textArea.setEditable(true);
    textArea.setDefaultForegroundColor(new Color(50,50,50));
    textArea.setScrollPane(scrollPane);
    textArea.addActionListener(this);
    innerPanel.add(scrollPane,BorderLayout.CENTER);

    buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridBagLayout());
    innerPanel.add(buttonPanel,BorderLayout.SOUTH);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets.left = 3;
    gbc.insets.right = 3;

    searchText = new JTextField();
    searchText.setEditable(true);
    searchText.setPreferredSize(new Dimension(150,25));
    buttonPanel.add(searchText,gbc);
    searchMathCase = new JCheckBox("Match case");
    searchMathCase.setSelected(false);
    buttonPanel.add(searchMathCase,gbc);
    searchButton = new JButton("Search");
    searchButton.addActionListener(this);
    buttonPanel.add(searchButton,gbc);
    searchNextButton = new JButton("Search Next");
    searchNextButton.addActionListener(this);
    buttonPanel.add(searchNextButton,gbc);

    JPanel dummyPanel = new JPanel();
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1.0;
    buttonPanel.add(dummyPanel,gbc);
    gbc.fill = GridBagConstraints.NONE;

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
    parse();
    setTitle("Edit property " + source.getName()+"/"+propName);
    requestFocus();
    ATKGraphicsUtils.centerDialog(this);
    
  }

  public void actionPerformed(ActionEvent e) {

    Object src = e.getSource();

    if( src==dismissButton ) {
      setVisible(false);
    } else if (src==applyButton) {
      source.setProperty(propName,textArea.getText());
      setVisible(false);
    } else if (src==textArea) {
      parse();
    } else if (src==searchButton) {
      textArea.searchText(searchText.getText(),searchMathCase.isSelected());
    } else if (src==searchNextButton) {
      textArea.searchNext(searchMathCase.isSelected());
    }

  }

  private void parse() {

    String text = textArea.getText();
    textArea.clearStyleAndColor();

    // Highlight separators
    for (int i = 0; i < separatorList.length; i++) {

      boolean end = false;
      int idx = 0;
      while (!end) {

        idx = text.indexOf(separatorList[i], idx);
        end = (idx == -1);
        if (!end) {
          textArea.setForeground(separatorColor, idx, separatorList[i].length());
          textArea.setStyle(Font.BOLD, idx, separatorList[i].length());
          idx += separatorList[i].length();
        }

      }

    }

    // Highlight keyword
    for (int i = 0; i < keyword1List.length; i++) {

      boolean end = false;
      int idx = 0;
      while (!end) {

        idx = text.indexOf(keyword1List[i], idx);
        end = (idx == -1);
        if (!end) {
          textArea.setForeground(keyword1Color, idx, keyword1List[i].length());
          //textArea.setStyle(Font.BOLD, idx, keyword1List[i].length());
          idx += keyword1List[i].length();
        }

      }

    }

    for (int i = 0; i < keyword2List.length; i++) {

      boolean end = false;
      int idx = 0;
      while (!end) {

        idx = text.indexOf(keyword2List[i], idx);
        end = (idx == -1);
        if (!end) {
          textArea.setForeground(keyword2Color, idx, keyword2List[i].length());
          textArea.setStyle(Font.BOLD, idx, keyword2List[i].length());
          idx += keyword2List[i].length();
        }

      }

    }

  }


}
