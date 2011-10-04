package jive;

import fr.esrf.TangoApi.CommandInfo;
import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoApi.AttributeInfo;
import fr.esrf.TangoApi.DeviceAttribute;
import fr.esrf.Tango.*;
import fr.esrf.tangoatk.widget.util.ATKConstant;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.chart.JLDataView;
import fr.esrf.tangoatk.widget.util.chart.JLChart;
import fr.esrf.tangoatk.widget.util.chart.JLAxis;
import fr.esrf.tangoatk.widget.attribute.NumberImageViewer;
import fr.esrf.TangoDs.TangoConst;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * The command panel.
 */
class AttributePanel extends JPanel implements ActionListener,ListSelectionListener,TangoConst,MouseListener,ClipboardOwner,DragGestureListener,DragSourceListener {

  private AttributeInfo[] attList;
  private ConsolePanel    console;
  private DeviceProxy     device;
  private CommonPanel     common;

  private JComboBox   arginCombo;
  private JLabel      arginLabel;
  private JLabel      descrLabel;

  private JList       attributeList;
  private JScrollPane attributeView;

  private JTextArea    descrList;
  private JScrollPane  descrView;

  private JButton      readBtn;
  private JButton      writeBtn;
  private JButton      plotBtn;

  private JFrame       chartDlg = null;
  private JLChart      chart;
  private JLDataView   plotData;

  private JFrame imageDlg = null;
  private NumberImageViewer image;

  private JPopupMenu   copyMenu;
  private JMenuItem    copyAttributeMenuItem;
  private JMenuItem    copyDevAttributeMenuItem;

  /**
   * Construct the device panel
   * @param ds DeviceProxy
   */
  AttributePanel(DeviceProxy ds,ConsolePanel console,CommonPanel common) throws DevFailed {

    setLayout(null);

    this.console = console;
    this.common = common;
    this.device = ds;
    attList = getAttributeList();

    arginLabel = new JLabel("Argin value");
    arginLabel.setFont(ATKConstant.labelFont);
    add(arginLabel);

    descrLabel = new JLabel();
    descrLabel.setFont(ATKConstant.labelFont);
    descrLabel.setHorizontalAlignment(JLabel.RIGHT);
    add(descrLabel);

    arginCombo = new JComboBox();
    arginCombo.setEditable(true);
    arginCombo.setFont(ATKConstant.labelFont);
    add(arginCombo);

    DefaultListModel ml = new DefaultListModel();
    for (int i = 0; i < attList.length; i++)
      ml.add(i, attList[i].name);
    attributeList = new JList(ml);
    attributeList.addListSelectionListener(this);
    attributeList.addMouseListener(this);
    DragSource dragSource = DragSource.getDefaultDragSource();
    dragSource.createDefaultDragGestureRecognizer(attributeList,
            DnDConstants.ACTION_MOVE,
            this);

    attributeView = new JScrollPane(attributeList);
    add(attributeView);

    descrList = new JTextArea();
    descrList.setFont(new Font("monospaced",Font.PLAIN,12));
    descrList.setEditable(false);
    descrView = new JScrollPane(descrList);
    add(descrView);

    readBtn = new JButton("Read");
    readBtn.setFont(ATKConstant.labelFont);
    add(readBtn);
    readBtn.addActionListener(this);

    writeBtn = new JButton("Write");
    writeBtn.setFont(ATKConstant.labelFont);
    add(writeBtn);
    writeBtn.addActionListener(this);

    plotBtn = new JButton("Plot");
    plotBtn.setFont(ATKConstant.labelFont);
    add(plotBtn);
    plotBtn.addActionListener(this);

    addComponentListener(new ComponentListener() {

          public void componentHidden(ComponentEvent e) {}

          public void componentMoved(ComponentEvent e) {}

          public void componentResized(ComponentEvent e) {
            placeComponents(getSize());
          }

          public void componentShown(ComponentEvent e) {
            placeComponents(getSize());
          }
    });

    // Default

    attributeList.setSelectedIndex(0);

    // Popup menu
    copyMenu = new JPopupMenu();
    copyAttributeMenuItem = new JMenuItem("Copy attribute");
    copyAttributeMenuItem.addActionListener(this);
    copyMenu.add(copyAttributeMenuItem);
    copyDevAttributeMenuItem = new JMenuItem("Copy device/attribute");
    copyDevAttributeMenuItem.addActionListener(this);
    copyMenu.add(copyDevAttributeMenuItem);

  }

  // -----------------------------------------------------

  public void valueChanged(ListSelectionEvent e) {

    int idx = attributeList.getSelectedIndex();
    if(attList.length==0) return; // Empty set
    AttributeInfo ai = attList[idx];

    if(!e.getValueIsAdjusting()) {

      if(isWritable(ai)) {
        descrLabel.setText(getExample(ai));
        arginCombo.setEnabled(true);
        writeBtn.setEnabled(true);
      } else {
        descrLabel.setText("");
        arginCombo.setEnabled(false);
        writeBtn.setEnabled(false);
      }

      descrList.setText(
        "Name         " + ai.name + "\n" +
        "Label        " + ai.label + "\n" +
        "Writable     " + getWriteString(ai) + "\n" +
        "Data format  " + getFormatString(ai) + "\n" +
        "Data type    " + Tango_CmdArgTypeName[ai.data_type] + "\n" +
        "Max Dim X    " + ai.max_dim_x + "\n" +
        "Max Dim Y    " + ai.max_dim_y + "\n" +
        "Unit         " + ai.unit + "\n" +
        "Std Unit     " + ai.standard_unit + "\n" +
        "Disp Unit    " + ai.display_unit + "\n" +
        "Format       " + ai.format + "\n" +
        "Min value    " + ai.min_value + "\n" +
        "Max value    " + ai.max_value + "\n" +
        "Min alarm    " + ai.min_alarm + "\n" +
        "Max alarm    " + ai.max_alarm
      );
      descrList.setCaretPosition(0);

      plotBtn.setEnabled(isPlotable(ai));

    }

  }

  // -----------------------------------------------------

  public void mouseClicked(MouseEvent e) {
    Object src = e.getSource();

    if( src==attributeList ) {

      if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount()==2) {
        readAttribute();
      }

      if(e.getButton() == MouseEvent.BUTTON3) {
        copyMenu.show(attributeList, e.getX() , e.getY());
      }

    }
  }

  public void mousePressed(MouseEvent e) {}
  public void mouseReleased(MouseEvent e) {}
  public void mouseEntered(MouseEvent e) {}
  public void mouseExited(MouseEvent e) {}

  // -----------------------------------------------------

  public void dragGestureRecognized(DragGestureEvent dragGestureEvent) {
    dragGestureEvent.startDrag(DragSource.DefaultMoveDrop,new TransferableString(getCopyString(false)),this);
  }

  public void dragDropEnd(DragSourceDropEvent dragSourceDropEvent) {
  }

  public void dragEnter(DragSourceDragEvent dragSourceDragEvent) {
    DragSourceContext context = dragSourceDragEvent.getDragSourceContext();
    context.setCursor(DragSource.DefaultMoveDrop);
  }

  public void dragExit(DragSourceEvent dragSourceEvent) {
  }

  public void dragOver(DragSourceDragEvent dragSourceDragEvent) {
  }

  public void dropActionChanged(DragSourceDragEvent dragSourceDragEvent) {
  }

  // -----------------------------------------------------

  public void actionPerformed(ActionEvent e) {

    Object src = e.getSource();

    if(src == readBtn) {
       readAttribute();
    } else if(src == writeBtn) {
       writeAttribute();
    } else if(src == plotBtn) {
       plotAttribute();
    } else if(src==copyAttributeMenuItem) {
      copyAttributeToClipboard(true);
    } else if(src==copyDevAttributeMenuItem) {
      copyAttributeToClipboard(false);
    }

  }

  public void lostOwnership(java.awt.datatransfer.Clipboard clipboard, Transferable contents) {
  }

  public Dimension getPreferredSize() {
    return getMinimunSize();
  }

  public Dimension getMinimunSize() {
    return new Dimension(470,210);
  }


  // -----------------------------------------------------
  private String getCopyString(boolean copyAttOnly) {

    String copyStr = "";

    AttributeInfo ai = attList[attributeList.getSelectedIndex()];
    if( copyAttOnly ) {
      copyStr = ai.name;
    } else {
      copyStr = device.get_name() + "/" + ai.name;
    }

    return copyStr;

  }

  private void copyAttributeToClipboard(boolean copyAttOnly) {

    StringSelection stringSelection = new StringSelection( getCopyString(copyAttOnly) );
    java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents( stringSelection, this );

  }

  // -----------------------------------------------------
  // Conversion
  // -----------------------------------------------------

  static String getFormatString(AttributeInfo ai) {

    switch (ai.data_format.value()) {
      case AttrDataFormat._SCALAR:
        return "Scalar";
      case AttrDataFormat._SPECTRUM:
        return "Spectrum";
      case AttrDataFormat._IMAGE:
        return "Image";
    }
    return  "Unknown";

  }

  static String getWriteString(AttributeInfo ai) {

    switch(ai.writable.value()) {

      case AttrWriteType._READ:
        return "READ";
      case AttrWriteType._READ_WITH_WRITE:
        return "READ_WITH_WRITE";
      case AttrWriteType._READ_WRITE:
        return "READ_WRITE";
      case AttrWriteType._WRITE:
        return "WRITE";

    }

    return  "Unknown";

  }

  // -----------------------------------------------------
  // Private stuff
  // -----------------------------------------------------
  private AttributeInfo[] getAttributeList() throws DevFailed {

    int         i,j;
    boolean     end;
    AttributeInfo tmp;

    AttributeInfo[] lst = device.get_attribute_info();

    //Sort the list
    end = false;
    j=lst.length-1;
    while(!end) {
      end = true;
      for(i=0;i<j;i++) {
        if(lst[i].name.compareToIgnoreCase(lst[i+1].name)>0) {
          end = false;
          tmp = lst[i];
          lst[i] = lst[i+1];
          lst[i+1] = tmp;
        }
      }
      j--;
    }

    return lst;

  }


  private void placeComponents(Dimension dim) {

    arginLabel.setBounds(10,0,80,20);
    descrLabel.setBounds(90,0,dim.width-100,20);
    arginCombo.setBounds(10,20,dim.width-20,25);

    attributeView.setBounds(10,50,190,dim.height-60);
    attributeView.revalidate();

    descrView.setBounds(205,50,dim.width-215,dim.height-90);
    descrView.revalidate();

    int dim2 = (dim.width - 220) / 3;

    readBtn.setBounds(205,dim.height-35,dim2,25);
    writeBtn.setBounds(205+dim2+2,dim.height-35,dim2,25);
    plotBtn.setBounds(205+2*dim2+5,dim.height-35,dim2,25);

  }

  private void addArgin(String text) {
    // Add the input string if not already done
    boolean found = false;
    int i = 0;
    while(i<arginCombo.getItemCount() && !found) {
      found = text.equals(arginCombo.getItemAt(i).toString());
      if(!found) i++;
    }
    if(!found) arginCombo.addItem(text);
  }

  private boolean isPlotable(AttributeInfo ai) {
    if((ai.data_type==Tango_DEV_STRING) ||
       (ai.data_type==Tango_DEV_STATE)  ||
       (ai.data_type==Tango_DEV_BOOLEAN))
      return false;

    return (ai.data_format.value() == AttrDataFormat._SPECTRUM) ||
           (ai.data_format.value() == AttrDataFormat._IMAGE);
  }

  private boolean isWritable(AttributeInfo ai) {

    return (ai.writable.value() == AttrWriteType._READ_WITH_WRITE) ||
           (ai.writable.value() == AttrWriteType._READ_WRITE)      ||
           (ai.writable.value() == AttrWriteType._WRITE);

  }

  private void writeAttribute() {

    try {

      AttributeInfo ai = attList[attributeList.getSelectedIndex()];
      String att = ai.name;
      String arginStr = (String)arginCombo.getSelectedItem();
      if(arginStr!=null) addArgin(arginStr);
      DeviceAttribute argin = new DeviceAttribute(att);
      insertData(arginStr, argin, ai);
      long t0 = System.currentTimeMillis();
      device.write_attribute(argin);
      long t1 = System.currentTimeMillis();
      console.print("----------------------------------------------------\n");
      console.print("Attribute: " + device.name() + "/" + att + "\n");
      console.print("Duration: " + (t1 - t0) + " msec\n");
      console.print("Write OK\n");

    } catch (NumberFormatException e1) {
      JOptionPane.showMessageDialog(this, "Invalid argin syntaxt\n" + e1.getMessage());
    } catch (DevFailed e2) {
      ErrorPane.showErrorMessage(this, device.name(), e2);
    }

  }

  private void readAttribute() {

    try {

      AttributeInfo ai = attList[attributeList.getSelectedIndex()];
      String att = ai.name;
      long t0 = System.currentTimeMillis();
      DeviceAttribute argout = device.read_attribute(att);
      long t1 = System.currentTimeMillis();
      console.print("----------------------------------------------------\n");
      console.print("Attribute: " + device.name() + "/" + att + "\n");
      console.print("Duration: " + (t1-t0) + " msec\n");
      console.print(extractData(argout,ai));

    } catch(NumberFormatException e1) {
      JOptionPane.showMessageDialog(this,"Invalid argin syntaxt\n"+e1.getMessage());
    } catch (DevFailed e2) {
      ErrorPane.showErrorMessage(this,device.name(),e2);
    }

  }

  private void plotAttribute() {

    try {

      AttributeInfo ai = attList[attributeList.getSelectedIndex()];
      String att = ai.name;
      DeviceAttribute argout = device.read_attribute(att);

      switch (ai.data_format.value()) {

        case AttrDataFormat._SPECTRUM:

          double[] values = extractSpectrumPlotData(argout, ai);
          if (chartDlg == null) {
            chart = new JLChart();
            chart.setPreferredSize(new Dimension(640, 480));
            plotData = new JLDataView();
            chart.getY1Axis().addDataView(plotData);
            chart.getY1Axis().setAutoScale(true);
            chart.getXAxis().setAnnotation(JLAxis.VALUE_ANNO);
            chartDlg = new JFrame("Plot attribute [" + device.name() + "]");
            chartDlg.setContentPane(chart);
          }
          plotData.reset();
          plotData.setName(att);
          for (int i = 0; i < values.length; i++) plotData.add(i+common.getAnswerLimitMin(), values[i]);
          chart.repaint(); // Commit change
          if (!chartDlg.isVisible()) {
            ATKGraphicsUtils.centerFrameOnScreen(chartDlg);
            chartDlg.setVisible(true);
          }
          break;

        case AttrDataFormat._IMAGE:

          double[][] ivalues = extractImagePlotData(argout, ai);
          if (imageDlg == null) {
            image = new NumberImageViewer();
            image.setAutoZoom(true);
            imageDlg = new JFrame("Plot attribute [" + device.name() + "]");
            imageDlg.setContentPane(image);
          }
          image.setData(ivalues);
          if (!imageDlg.isVisible()) {
            ATKGraphicsUtils.centerFrameOnScreen(imageDlg);
            imageDlg.setVisible(true);
          }

          break;

      }
    } catch(NumberFormatException e1) {
      JOptionPane.showMessageDialog(this,"Invalid argin syntaxt\n"+e1.getMessage());
    } catch (DevFailed e2) {
      ErrorPane.showErrorMessage(this,device.name(),e2);
    }

  }

  private String getExample(AttributeInfo ai) {

    String ret_string = "";

    switch (ai.data_type) {

      case Tango_DEV_STATE:
        ret_string += "0 (16bits value)";
        break;
      case Tango_DEV_UCHAR:
        ret_string += "10 or 0xa (unsigned 8bits)";
        break;
      case Tango_DEV_BOOLEAN:
        ret_string += "true,false or 0,1";
        break;
      case Tango_DEV_USHORT:
        ret_string += "10 or 0xa (unsigned 16bits)";
        break;
      case Tango_DEV_SHORT:
        ret_string += "10 or 0xa (signed 16bits)";
        break;
      case Tango_DEV_LONG:
        ret_string += "10 or 0xa (signed 32bits)";
        break;
      case Tango_DEV_ULONG:
        ret_string += "10 or 0xa (unsigned 32bits)";
        break;
      case Tango_DEV_LONG64:
        ret_string += "10 or 0xa (signed 64bits)";
        break;
      case Tango_DEV_ULONG64:
        ret_string += "10 or 0xa (unsigned 64bits)";
        break;
      case Tango_DEV_FLOAT:
        ret_string += "2.3 (32bits float)";
        break;
      case Tango_DEV_DOUBLE:
        ret_string += "2.3 (64bits float)";
        break;
      case Tango_DEV_STRING:
        ret_string = "quotes needed for string with space or special char";
        break;
      default:
        ret_string = new String("");
        break;

    }

    if (ai.data_format.value() == AttrDataFormat._SPECTRUM) {
      return "Array of " + ret_string + " Ex: a,b,c";
    } else if (ai.data_format.value() == AttrDataFormat._IMAGE) {
      return "Image of " + ret_string + " Ex: [a,b,c][d,e,f][g,h,i]";
    } else {
      return "Ex: " + ret_string;
    }

  }


  private int getLimitMax(boolean checkLimit,StringBuffer retStr,int length,boolean writable) {

    if (length > 1) {
      if (writable)
        retStr.append("Write length: " + length + "\n");
      else
        retStr.append("Read length: " + length + "\n");
    }

    if( checkLimit ) {
      if(length>common.getAnswerLimitMax()) {
        retStr.append("Array cannot be fully displayed. (You may change the AnswerLimitMax)\n");
        return common.getAnswerLimitMax();
      } else {
        return length;
      }
    } else {
      return length;
    }
    
  }

  private int getLimitMin(boolean checkLimit,StringBuffer retStr,int length) {

    if( checkLimit ) {
      if(length<=common.getAnswerLimitMin()) {
        retStr.append("Array cannot be displayed. (You may change the AnswerLimitMin)\n");
        return length;
      } else {
        return common.getAnswerLimitMin();
      }
    } else {
      return 0;
    }

  }

  private int getLimitMaxForPlot(int length) {

    if(length>common.getAnswerLimitMax()) {
      return common.getAnswerLimitMax();
    } else {
      return length;
    }

  }

  private int getLimitMinForPlot(int length) {

   if(length<=common.getAnswerLimitMin()) {
     return length;
   } else {
     return common.getAnswerLimitMin();
   }

  }

  private void insertData(String argin,DeviceAttribute send,AttributeInfo ai) throws NumberFormatException {

    ArgParser arg = new ArgParser(argin);

    switch (ai.data_type) {

      case Tango_DEV_UCHAR:
        switch (ai.data_format.value()) {
          case AttrDataFormat._SCALAR:
            send.insert_uc(arg.parse_uchar());
            break;
          case AttrDataFormat._SPECTRUM:
            send.insert_uc(arg.parse_uchar_array());
            break;
          case AttrDataFormat._IMAGE:
            send.insert_uc(arg.parse_uchar_image(),arg.get_image_width(),arg.get_image_height());
            break;
        }
        break;

      case Tango_DEV_BOOLEAN:
        switch (ai.data_format.value()) {
          case AttrDataFormat._SCALAR:
            send.insert(arg.parse_boolean());
            break;
          case AttrDataFormat._SPECTRUM:
            send.insert(arg.parse_boolean_array());
            break;
          case AttrDataFormat._IMAGE:
            send.insert(arg.parse_boolean_image(),arg.get_image_width(),arg.get_image_height());
            break;
        }
        break;

      case Tango_DEV_SHORT:
        switch (ai.data_format.value()) {
          case AttrDataFormat._SCALAR:
            send.insert(arg.parse_short());
            break;
          case AttrDataFormat._SPECTRUM:
            send.insert(arg.parse_short_array());
            break;
          case AttrDataFormat._IMAGE:
            send.insert(arg.parse_short_image(),arg.get_image_width(),arg.get_image_height());
            break;
        }
        break;

      case Tango_DEV_USHORT:
        switch (ai.data_format.value()) {
          case AttrDataFormat._SCALAR:
            send.insert_us(arg.parse_ushort());
            break;
          case AttrDataFormat._SPECTRUM:
            send.insert_us(arg.parse_ushort_array());
            break;
          case AttrDataFormat._IMAGE:
            send.insert_us(arg.parse_ushort_image(),arg.get_image_width(),arg.get_image_height());
            break;
        }
        break;

      case Tango_DEV_LONG:
        switch (ai.data_format.value()) {
          case AttrDataFormat._SCALAR:
            send.insert(arg.parse_long());
            break;
          case AttrDataFormat._SPECTRUM:
            send.insert(arg.parse_long_array());
            break;
          case AttrDataFormat._IMAGE:
            send.insert(arg.parse_long_image(),arg.get_image_width(),arg.get_image_height());
            break;
        }
        break;

      case Tango_DEV_ULONG:
        switch (ai.data_format.value()) {
          case AttrDataFormat._SCALAR:
            send.insert_ul(arg.parse_ulong());
            break;
          case AttrDataFormat._SPECTRUM:
            send.insert_ul(arg.parse_ulong_array());
            break;
          case AttrDataFormat._IMAGE:
            send.insert_ul(arg.parse_ulong_image(),arg.get_image_width(),arg.get_image_height());
            break;
        }
        break;

      case Tango_DEV_LONG64:
        switch (ai.data_format.value()) {
          case AttrDataFormat._SCALAR:
            send.insert(arg.parse_long64());
            break;
          case AttrDataFormat._SPECTRUM:
            send.insert(arg.parse_long64_array());
            break;
          case AttrDataFormat._IMAGE:
            send.insert(arg.parse_long64_image(),arg.get_image_width(),arg.get_image_height());
            break;
        }
        break;

      case Tango_DEV_ULONG64:
        switch (ai.data_format.value()) {
          case AttrDataFormat._SCALAR:
            send.insert_u64(arg.parse_long64());
            break;
          case AttrDataFormat._SPECTRUM:
            send.insert_u64(arg.parse_long64_array());
            break;
          case AttrDataFormat._IMAGE:
            send.insert_u64(arg.parse_long64_image(),arg.get_image_width(),arg.get_image_height());
            break;
        }
        break;

      case Tango_DEV_FLOAT:
        switch (ai.data_format.value()) {
          case AttrDataFormat._SCALAR:
            send.insert(arg.parse_float());
            break;
          case AttrDataFormat._SPECTRUM:
            send.insert(arg.parse_float_array());
            break;
          case AttrDataFormat._IMAGE:
            send.insert(arg.parse_float_image(),arg.get_image_width(),arg.get_image_height());
            break;
        }
        break;

      case Tango_DEV_DOUBLE:
        switch (ai.data_format.value()) {
          case AttrDataFormat._SCALAR:
            send.insert(arg.parse_double());
            break;
          case AttrDataFormat._SPECTRUM:
            send.insert(arg.parse_double_array());
            break;
          case AttrDataFormat._IMAGE:
            send.insert(arg.parse_double_image(),arg.get_image_width(),arg.get_image_height());
            break;
        }
        break;

      case Tango_DEV_STRING:
        switch (ai.data_format.value()) {
          case AttrDataFormat._SCALAR:
            send.insert(arg.parse_string());
            break;
          case AttrDataFormat._SPECTRUM:
            send.insert(arg.parse_string_array());
            break;
          case AttrDataFormat._IMAGE:
            send.insert(arg.parse_string_image(),arg.get_image_width(),arg.get_image_height());
            break;
        }
        break;

      default:
        throw new NumberFormatException("Attribute type not supported code=" + ai.data_type);

    }

  }

  private void printArrayItem(StringBuffer str,int idx,boolean printIdx,
                              String value,boolean writeable) {
    if(!writeable) {
      if(printIdx)
        str.append("Read [" + idx + "]\t" + value + "\n");
      else
        str.append("Read:\t" + value + "\n");
    } else {
      if(printIdx)
        str.append("Set [" + idx + "]\t" + value + "\n");
      else
        str.append("Set:\t" + value + "\n");
    }
  }

  private String extractData(DeviceAttribute data,AttributeInfo ai) {

    StringBuffer ret_string = new StringBuffer();

    try {

      // Add the date of the measure in two formats
      TimeVal t = data.getTimeVal();
      java.util.Date date = new java.util.Date((long) (t.tv_sec * 1000.0 + t.tv_usec / 1000.0));
      SimpleDateFormat dateformat =
              new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
      dateformat.setTimeZone(TimeZone.getDefault());
      ret_string.append("measure date: " + dateformat.format(date) + " + " + (t.tv_usec/1000) + "ms\n");

      // Add the quality information
      AttrQuality q = data.getQuality();

      ret_string.append("quality: ");
      switch (q.value()) {
        case AttrQuality._ATTR_VALID:
          ret_string.append("VALID\n");
          break;
        case AttrQuality._ATTR_INVALID:
          ret_string.append("INVALID\n");
          return ret_string.toString();
        case AttrQuality._ATTR_ALARM:
          ret_string.append("ALARM\n");
          break;
        case AttrQuality._ATTR_CHANGING:
          ret_string.append("CHANGING\n");
          break;
        case AttrQuality._ATTR_WARNING:
          ret_string.append("WARNING\n");
          break;
        default:
          ret_string.append("UNKNOWN\n");
          break;
      }

      // Add dimension of the attribute but only if having a meaning
      boolean printIndex = true;
      boolean checkLimit = true;
      switch (ai.data_format.value()) {
        case AttrDataFormat._SCALAR:
          printIndex = false;
          checkLimit = false;
          break;
        case AttrDataFormat._SPECTRUM:
          ret_string.append("dim x: " + data.getDimX() + "\n");
          break;
        case AttrDataFormat._IMAGE:
          ret_string.append("dim x: " + data.getDimX() + "\n");
          ret_string.append("dim y: " + data.getDimY() + "\n");
          break;
        default:
          break;
      }

      // Add values
      switch (ai.data_type) {

        case Tango_DEV_STATE:
          {
            DevState[] dummy = data.extractDevStateArray();
            int nbRead = data.getNbRead();
            int nbWritten = dummy.length - nbRead;
            int start = getLimitMin(checkLimit,ret_string,nbRead);
            int end = getLimitMax(checkLimit,ret_string,nbRead,false);
            for (int i = start; i < end; i++)
              printArrayItem(ret_string,i,printIndex,Tango_DevStateName[dummy[i].value()],false);
            if( isWritable(ai) ) {
              start = getLimitMin(checkLimit,ret_string,nbWritten);
              end = getLimitMax(checkLimit,ret_string,nbWritten,true);
              for (int i = start; i < end; i++)
                printArrayItem(ret_string,i,printIndex,Tango_DevStateName[dummy[i+nbRead].value()],true);
            }
          }
          break;

        case Tango_DEV_UCHAR:
          {
            short[] dummy = data.extractUCharArray();
            int nbRead = data.getNbRead();
            int nbWritten = dummy.length - nbRead;
            int start = getLimitMin(checkLimit,ret_string,nbRead);
            int end = getLimitMax(checkLimit,ret_string,nbRead,false);
            for (int i = start; i < end; i++)
              printArrayItem(ret_string,i,printIndex,Short.toString(dummy[i]),false);
            if( isWritable(ai) ) {
              start = getLimitMin(checkLimit,ret_string,nbWritten);
              end = getLimitMax(checkLimit,ret_string,nbWritten,true);
              for (int i = start; i < end; i++)
                printArrayItem(ret_string,i,printIndex,Short.toString(dummy[i+nbRead]),true);
            }
          }
          break;

        case Tango_DEV_SHORT:
          {
            short[] dummy = data.extractShortArray();
            int nbRead = data.getNbRead();
            int nbWritten = dummy.length - nbRead;
            int start = getLimitMin(checkLimit,ret_string,nbRead);
            int end = getLimitMax(checkLimit,ret_string,nbRead,false);
            for (int i = start; i < end; i++)
              printArrayItem(ret_string,i,printIndex,Short.toString(dummy[i]),false);
            if( isWritable(ai) ) {
              start = getLimitMin(checkLimit,ret_string,nbWritten);
              end = getLimitMax(checkLimit,ret_string,nbWritten,true);
              for (int i = start; i < end; i++)
                printArrayItem(ret_string,i,printIndex,Short.toString(dummy[i+nbRead]),true);
            }
          }
          break;

        case Tango_DEV_BOOLEAN:
          {
            boolean[] dummy = data.extractBooleanArray();
            int nbRead = data.getNbRead();
            int nbWritten = dummy.length - nbRead;
            int start = getLimitMin(checkLimit,ret_string,nbRead);
            int end = getLimitMax(checkLimit,ret_string,nbRead,false);
            for (int i = start; i < end; i++)
              printArrayItem(ret_string,i,printIndex,Boolean.toString(dummy[i]),false);
            if( isWritable(ai) ) {
              start = getLimitMin(checkLimit,ret_string,nbWritten);
              end = getLimitMax(checkLimit,ret_string,nbWritten,true);
              for (int i = start; i < end; i++)
                printArrayItem(ret_string,i,printIndex,Boolean.toString(dummy[i+nbRead]),true);
            }
          }
          break;

        case Tango_DEV_USHORT:
          {
            int[] dummy = data.extractUShortArray();
            int nbRead = data.getNbRead();
            int nbWritten = dummy.length - nbRead;
            int start = getLimitMin(checkLimit,ret_string,nbRead);
            int end = getLimitMax(checkLimit,ret_string,nbRead,false);
            for (int i = start; i < end; i++)
              printArrayItem(ret_string,i,printIndex,Integer.toString(dummy[i]),false);
            if( isWritable(ai) ) {
              start = getLimitMin(checkLimit,ret_string,nbWritten);
              end = getLimitMax(checkLimit,ret_string,nbWritten,true);
              for (int i = start; i < end; i++)
                printArrayItem(ret_string,i,printIndex,Integer.toString(dummy[i+nbRead]),true);
            }
          }
          break;

        case Tango_DEV_LONG:
          {
            int[] dummy = data.extractLongArray();
            int nbRead = data.getNbRead();
            int nbWritten = dummy.length - nbRead;
            int start = getLimitMin(checkLimit,ret_string,nbRead);
            int end = getLimitMax(checkLimit,ret_string,nbRead,false);
            for (int i = start; i < end; i++)
              printArrayItem(ret_string,i,printIndex,Integer.toString(dummy[i]),false);
            if( isWritable(ai) ) {
              start = getLimitMin(checkLimit,ret_string,nbWritten);
              end = getLimitMax(checkLimit,ret_string,nbWritten,true);
              for (int i = start; i < end; i++)
                printArrayItem(ret_string,i,printIndex,Integer.toString(dummy[i+nbRead]),true);
            }
          }
          break;

        case Tango_DEV_ULONG:
          {
            long[] dummy = data.extractULongArray();
            int nbRead = data.getNbRead();
            int nbWritten = dummy.length - nbRead;
            int start = getLimitMin(checkLimit,ret_string,nbRead);
            int end = getLimitMax(checkLimit,ret_string,nbRead,false);
            for (int i = start; i < end; i++)
              printArrayItem(ret_string,i,printIndex,Long.toString(dummy[i]),false);
            if( isWritable(ai) ) {
              start = getLimitMin(checkLimit,ret_string,nbWritten);
              end = getLimitMax(checkLimit,ret_string,nbWritten,true);
              for (int i = start; i < end; i++)
                printArrayItem(ret_string,i,printIndex,Long.toString(dummy[i+nbRead]),true);
            }
          }
          break;

        case Tango_DEV_LONG64:
          {
            long[] dummy = data.extractLong64Array();
            int nbRead = data.getNbRead();
            int nbWritten = dummy.length - nbRead;
            int start = getLimitMin(checkLimit,ret_string,nbRead);
            int end = getLimitMax(checkLimit,ret_string,nbRead,false);
            for (int i = start; i < end; i++)
              printArrayItem(ret_string,i,printIndex,Long.toString(dummy[i]),false);
            if( isWritable(ai) ) {
              start = getLimitMin(checkLimit,ret_string,nbWritten);
              end = getLimitMax(checkLimit,ret_string,nbWritten,true);
              for (int i = start; i < end; i++)
                printArrayItem(ret_string,i,printIndex,Long.toString(dummy[i+nbRead]),true);
            }
          }
          break;
        
        case Tango_DEV_ULONG64:
          {
            long[] dummy = data.extractULong64Array();
            int nbRead = data.getNbRead();
            int nbWritten = dummy.length - nbRead;
            int start = getLimitMin(checkLimit,ret_string,nbRead);
            int end = getLimitMax(checkLimit,ret_string,nbRead,false);
            for (int i = start; i < end; i++)
              printArrayItem(ret_string,i,printIndex,Long.toString(dummy[i]),false);
            if( isWritable(ai) ) {
              start = getLimitMin(checkLimit,ret_string,nbWritten);
              end = getLimitMax(checkLimit,ret_string,nbWritten,true);
              for (int i = start; i < end; i++)
                printArrayItem(ret_string,i,printIndex,Long.toString(dummy[i+nbRead]),true);
            }
          }
          break;

        case Tango_DEV_DOUBLE:
          {
            double[] dummy = data.extractDoubleArray();
            int nbRead = data.getNbRead();
            int nbWritten = dummy.length - nbRead;
            int start = getLimitMin(checkLimit,ret_string,nbRead);
            int end = getLimitMax(checkLimit,ret_string,nbRead,false);
            for (int i = start; i < end; i++)
              printArrayItem(ret_string,i,printIndex,Double.toString(dummy[i]),false);
            if( isWritable(ai) ) {
              start = getLimitMin(checkLimit,ret_string,nbWritten);
              end = getLimitMax(checkLimit,ret_string,nbWritten,true);
              for (int i = start; i < end; i++)
                printArrayItem(ret_string,i,printIndex,Double.toString(dummy[i+nbRead]),true);
            }
          }
          break;

        case Tango_DEV_FLOAT:
          {
            float[] dummy = data.extractFloatArray();
            int nbRead = data.getNbRead();
            int nbWritten = dummy.length - nbRead;
            int start = getLimitMin(checkLimit,ret_string,nbRead);
            int end = getLimitMax(checkLimit,ret_string,nbRead,false);
            for (int i = start; i < end; i++)
              printArrayItem(ret_string,i,printIndex,Float.toString(dummy[i]),false);
            if( isWritable(ai) ) {
              start = getLimitMin(checkLimit,ret_string,nbWritten);
              end = getLimitMax(checkLimit,ret_string,nbWritten,true);
              for (int i = start; i < end; i++)
                printArrayItem(ret_string,i,printIndex,Float.toString(dummy[i+nbRead]),true);
            }
          }
          break;

        case Tango_DEV_STRING:
          {
            String[] dummy = data.extractStringArray();
            int nbRead = data.getNbRead();
            int nbWritten = dummy.length - nbRead;
            int start = getLimitMin(checkLimit,ret_string,nbRead);
            int end = getLimitMax(checkLimit,ret_string,nbRead,false);
            for (int i = start; i < end; i++)
              printArrayItem(ret_string,i,printIndex,dummy[i],false);
            if( isWritable(ai) ) {
              start = getLimitMin(checkLimit,ret_string,nbWritten);
              end = getLimitMax(checkLimit,ret_string,nbWritten,true);
              for (int i = start; i < end; i++)
                printArrayItem(ret_string,i,printIndex,dummy[i+nbRead],true);
            }
          }
          break;

        case Tango_DEV_ENCODED:
          {
            printIndex = true;
            DevEncoded e = data.extractDevEncoded();
            ret_string.append("Format: " + e.encoded_format + "\n");
            int nbRead = e.encoded_data.length;
            int start = getLimitMin(checkLimit,ret_string,nbRead);
            int end = getLimitMax(checkLimit,ret_string,nbRead,false);
            for (int i = start; i < end; i++) {
              short vs = (short)e.encoded_data[i];
              vs = (short)(vs & 0xFF);
              printArrayItem(ret_string,i,printIndex,Short.toString(vs),false);
            }
          }
          break;

        default:
          ret_string.append("Unsupported attribute type code="+ai.data_type+"\n");
          break;
      }

    } catch (DevFailed e) {

      ErrorPane.showErrorMessage(this,device.name() + "/" + ai.name,e);

    }

    return ret_string.toString();

  }

  private double[] extractSpectrumPlotData(DeviceAttribute data,AttributeInfo ai) {

    double[] ret = new double[0];
    int i;

    try {

      int start = getLimitMinForPlot(data.getNbRead());
      int end = getLimitMaxForPlot(data.getNbRead());

      switch (ai.data_type) {

        case Tango_DEV_UCHAR:
          {
            short[] dummy = data.extractUCharArray();
            ret = new double[end-start];
            for(i=start;i<end;i++)
              ret[i-start] = (double)dummy[i];
          }
          break;

        case Tango_DEV_SHORT:
          {
            short[] dummy = data.extractShortArray();
            ret = new double[end-start];
            for(i=start;i<end;i++)
              ret[i-start] = (double)dummy[i];
          }
          break;

        case Tango_DEV_USHORT:
          {
            int[] dummy = data.extractUShortArray();
            ret = new double[end-start];
            for(i=start;i<end;i++)
              ret[i-start] = (double)dummy[i];
          }
          break;

        case Tango_DEV_LONG:
          {
            int[] dummy = data.extractLongArray();
            ret = new double[end-start];
            for(i=start;i<end;i++)
              ret[i-start] = (double)dummy[i];
          }
          break;

        case Tango_DEV_DOUBLE:
          {
            double[] dummy = data.extractDoubleArray();
            ret = new double[end-start];
            for(i=start;i<end;i++)
              ret[i-start] = dummy[i];
          }
          break;

        case Tango_DEV_FLOAT:
          {
            float[] dummy = data.extractFloatArray();
            ret = new double[end-start];
            for(i=start;i<end;i++)
              ret[i-start] = (double)dummy[i];
          }
          break;

      }

    } catch (DevFailed e) {

      ErrorPane.showErrorMessage(this, device.name() + "/" + ai.name, e);

    }

    return ret;

  }

  private double[][] extractImagePlotData(DeviceAttribute data,AttributeInfo ai) {

    double[][] ret = new double[0][0];
    int i,j,k,dimx,dimy;

    try {

      dimx = data.getDimX();
      dimy = data.getDimY();

      switch (ai.data_type) {

        case Tango_DEV_UCHAR:
          {
            short[] dummy = data.extractUCharArray();
            ret = new double[dimy][dimx];
            for(j=0,k=0;j<dimy;j++)
              for(i=0;i<dimx;i++)
                ret[j][i] = (double)dummy[k++];
          }
          break;

        case Tango_DEV_SHORT:
          {
            short[] dummy = data.extractShortArray();
            ret = new double[dimy][dimx];
            for(j=0,k=0;j<dimy;j++)
              for(i=0;i<dimx;i++)
                ret[j][i] = (double)dummy[k++];
          }
          break;

        case Tango_DEV_USHORT:
          {
            int[] dummy = data.extractUShortArray();
            ret = new double[dimy][dimx];
            for(j=0,k=0;j<dimy;j++)
              for(i=0;i<dimx;i++)
                ret[j][i] = (double)dummy[k++];
          }
          break;

        case Tango_DEV_LONG:
          {
            int[] dummy = data.extractLongArray();
            ret = new double[dimy][dimx];
            for(j=0,k=0;j<dimy;j++)
              for(i=0;i<dimx;i++)
                ret[j][i] = (double)dummy[k++];
          }
          break;

        case Tango_DEV_DOUBLE:
          {
            double[] dummy = data.extractDoubleArray();
            ret = new double[dimy][dimx];
            for(j=0,k=0;j<dimy;j++)
              for(i=0;i<dimx;i++)
                ret[j][i] = dummy[k++];
          }
          break;

        case Tango_DEV_FLOAT:
          {
            float[] dummy = data.extractFloatArray();
            ret = new double[dimy][dimx];
            for(j=0,k=0;j<dimy;j++)
              for(i=0;i<dimx;i++)
                ret[j][i] = (double)dummy[k++];
          }
          break;

      }

    } catch (DevFailed e) {

      ErrorPane.showErrorMessage(this, device.name() + "/" + ai.name, e);

    }

    return ret;

  }

}
