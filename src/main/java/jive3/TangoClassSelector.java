package jive3;

import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;

import javax.swing.*;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import jive.JiveUtils;

/**
 * A simple panel for selecting Tango class
 */
public class TangoClassSelector extends JDialog implements ActionListener {

  private String[] devList;
  private String[] classList;
  private String[] classInfo;

  private JLabel label1;
  private JList  classSel;
  private JScrollPane classScroll;
  private JButton okButton;
  private JButton cancelButton;

  private boolean okFlag = false;

  /**
   * Construct a Tango Class Seleclector panel
   * @param list a list a devnsame/classname
   */
  public TangoClassSelector(JFrame parent,String[] list,String selectField) {

    super(parent,true);

    devList = list;
    buildClassList();

    setLayout(null);
    setTitle("Class for " + selectField + " node selection");
    label1 = new JLabel(classList.length + " class(es) found, choose class to select");
    label1.setBounds(10,10,300,25);
    add(label1);

    classSel = new JList();
    classSel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    classSel.setListData(classInfo);

    classScroll = new JScrollPane(classSel);
    classScroll.setBounds(10,40,300,300);
    add(classScroll);

    okButton = new JButton("Select");
    okButton.setBounds(10,350,80,25);
    okButton.addActionListener(this);
    add(okButton);

    cancelButton = new JButton("Cancel");
    cancelButton.setBounds(230,345,80,25);
    cancelButton.addActionListener(this);
    add(cancelButton);

  }

  public void actionPerformed(ActionEvent e) {
    Object src = e.getSource();
    if( src==okButton ) {
      okFlag = true;
      setVisible(false);
    } else if ( src==cancelButton ) {
      okFlag = false;
      setVisible(false);
    }
  }

  private void buildClassList() {

    Vector classVector = new Vector();
    int countDev[] = new int[32768];

    for(int i=0;i<devList.length;i+=2) {
      int idx = classVector.indexOf(devList[i+1]);
      if( idx<0 ) {
        classVector.add(devList[i+1]);
        countDev[classVector.size()-1]=1;
      } else {
        countDev[idx]++;
      }
    }

    classList = new String[classVector.size()];
    classInfo = new String[classVector.size()];
    for(int i=0;i<classList.length;i++) {
      classList[i]=(String)classVector.get(i);
      classInfo[i]=(String)classVector.get(i) + "     (" + Integer.toString(countDev[i]) + " devices)";
    }

  }

  /**
   * Returns selected class
   */

  public String getSelectedClass() {

    ATKGraphicsUtils.centerDialog(this,320,380);
    setVisible(true);
    dispose();
    if( !okFlag )
      return null;

    int id = classSel.getSelectedIndex();
    if(id<0) {
      JiveUtils.showJiveError("No class selected");
      return null;
    } else {
      return classList[id];
    }

  }

}
