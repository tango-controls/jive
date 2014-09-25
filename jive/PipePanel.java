package jive;

import fr.esrf.TangoApi.*;
import fr.esrf.Tango.*;
import fr.esrf.tangoatk.widget.util.ATKConstant;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import fr.esrf.tangoatk.widget.util.chart.JLDataView;
import fr.esrf.tangoatk.widget.util.chart.JLChart;
import fr.esrf.tangoatk.widget.attribute.NumberImageViewer;
import fr.esrf.TangoDs.TangoConst;

import java.util.List;
import java.util.TimeZone;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;

/**
 * The pipe panel.
 */
class PipePanel extends JPanel implements ActionListener,ListSelectionListener,TangoConst,MouseListener,ClipboardOwner,DragGestureListener,DragSourceListener {

  private List<PipeInfo>  pipeInfo;
  private ConsolePanel    console;
  private DeviceProxy     device;
  private CommonPanel     common;

  private JComboBox   arginCombo;
  private JLabel      arginLabel;
  private JLabel      descrLabel;

  private JList       pipeList;
  private JScrollPane pipeView;

  private JTextArea    descrList;
  private JScrollPane  descrView;

  private JButton      readBtn;
  private JButton      writeBtn;
  private JButton      helpBtn;

  private JPopupMenu   copyMenu;
  private JMenuItem    copyAttributeMenuItem;
  private JMenuItem    copyDevAttributeMenuItem;

  /**
   * Construct the pipe panel
   * @param ds DeviceProxy
   */
  PipePanel(DeviceProxy ds,ConsolePanel console,CommonPanel common) throws DevFailed {

    setLayout(null);

    this.console = console;
    this.common = common;
    this.device = ds;
    pipeInfo = getPipeInfo();

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
    for (int i = 0; i < pipeInfo.size(); i++)
      ml.add(i, pipeInfo.get(i).getName());
    pipeList = new JList(ml);
    pipeList.addListSelectionListener(this);
    pipeList.addMouseListener(this);
    DragSource dragSource = DragSource.getDefaultDragSource();
    dragSource.createDefaultDragGestureRecognizer(pipeList,
        DnDConstants.ACTION_MOVE,
        this);

    pipeView = new JScrollPane(pipeList);
    add(pipeView);

    descrList = new JTextArea();
    descrList.setFont(new Font("monospaced", Font.PLAIN, 12));
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

    helpBtn = new JButton("Help");
    helpBtn.setFont(ATKConstant.labelFont);
    add(helpBtn);
    helpBtn.addActionListener(this);

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

    pipeList.setSelectedIndex(0);

    // Popup menu
    copyMenu = new JPopupMenu();
    copyAttributeMenuItem = new JMenuItem("Copy pipe name");
    copyAttributeMenuItem.addActionListener(this);
    copyMenu.add(copyAttributeMenuItem);
    copyDevAttributeMenuItem = new JMenuItem("Copy device/pipe name");
    copyDevAttributeMenuItem.addActionListener(this);
    copyMenu.add(copyDevAttributeMenuItem);

  }

  // -----------------------------------------------------

  public void valueChanged(ListSelectionEvent e) {

    int idx = pipeList.getSelectedIndex();
    if(pipeInfo.size()==0) return; // Empty set
    PipeInfo pi = pipeInfo.get(idx);

    if(!e.getValueIsAdjusting()) {

      if(pi.isWritable()) {
        descrLabel.setText("  Blobname,[ Name1,type1,value1 ],... (Help for details)");
        arginCombo.setEnabled(true);
        writeBtn.setEnabled(true);
        helpBtn.setEnabled(true);
      } else {
        descrLabel.setText("");
        arginCombo.setEnabled(false);
        writeBtn.setEnabled(false);
        helpBtn.setEnabled(false);
      }

      descrList.setText(
          "Name         " + pi.getName() + "\n" +
              "Label        " + pi.getLabel() + "\n" +
              "Writable     " + Boolean.toString(pi.isWritable()) + "\n" +
              "Level        " + pi.getLevel() + "\n" +
              "Descr        " + pi.getDescription() );

      descrList.setCaretPosition(0);

    }

  }

  // -----------------------------------------------------

  public void mouseClicked(MouseEvent e) {
    Object src = e.getSource();

    if( src== pipeList) {

      if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount()==2) {
        readPipe();
      }

      if(e.getButton() == MouseEvent.BUTTON3) {
        copyMenu.show(pipeList, e.getX() , e.getY());
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
      readPipe();
    } else if(src == writeBtn) {
      writePipe();
    } else if(src == helpBtn) {

      String helpMessage =
          "Argin syntax for a blob:\n" +
          " Blobname,[ Name1,type1,value1 ],[ Name2,typ2,value2 ],... \n" +
          " where the value is formatted as \n" +
          "   D,1,2.5,3,4      (double)\n" +
          "   F,1.0,2.3,4,4    (float)\n" +
          "  UC,1,2,3,4        (unsigned char)\n" +
          "   S,1,-2,3,4       (short)\n" +
          "  US,1,2,3,4        (unsigned short)\n" +
          "   L,1,-2,3,4       (long)\n" +
          "  UL,1,2,3,4        (unsigned long)\n" +
          "  LL,1,2,3,4        (int64)\n" +
          "  ST,ON,OFF,STANDY  (state)\n" +
          " STR,\"str 1\",str2   (string)\n" +
          " [ blob ]   (inner blob)\n";
      JOptionPane.showMessageDialog(this,helpMessage,"Argin syntax",JOptionPane.INFORMATION_MESSAGE);

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
  private String getCopyString(boolean copyNameOnly) {

    String copyStr = "";

    PipeInfo ai = pipeInfo.get(pipeList.getSelectedIndex());
    if( copyNameOnly ) {
      copyStr = ai.getName();
    } else {
      copyStr = device.get_name() + "/" + ai.getName();
    }

    return copyStr;

  }

  private void copyAttributeToClipboard(boolean copyNameOnly) {

    StringSelection stringSelection = new StringSelection( getCopyString(copyNameOnly) );
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
  private List<PipeInfo> getPipeInfo() throws DevFailed {

    return device.getPipeConfig();

  }

  private void placeComponents(Dimension dim) {

    arginLabel.setBounds(10,0,80,20);
    descrLabel.setBounds(90,0,dim.width-100,20);
    arginCombo.setBounds(10,20,dim.width-20,25);

    pipeView.setBounds(10, 50, 190, dim.height - 60);
    pipeView.revalidate();

    descrView.setBounds(205,50,dim.width-215,dim.height-90);
    descrView.revalidate();

    int dim2 = (dim.width - 220) / 3;

    readBtn.setBounds(205,dim.height-35,dim2,25);
    writeBtn.setBounds(205+dim2+2,dim.height-35,dim2,25);
    helpBtn.setBounds(205 + 2 * dim2 + 5, dim.height - 35, dim2, 25);

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


  private void writePipe() {

    try {

      PipeInfo pi = pipeInfo.get(pipeList.getSelectedIndex());
      String arginStr = (String)arginCombo.getSelectedItem();
      if(arginStr!=null) addArgin(arginStr);
      DevicePipe argin = new DevicePipe(pi.getName(),parseData(arginStr));
      long t0 = System.currentTimeMillis();
      device.writePipe(argin);
      long t1 = System.currentTimeMillis();
      console.print("----------------------------------------------------\n");
      console.print("Pipe: " + device.name() + "/" + pi.getName() + "\n");
      console.print("Duration: " + (t1 - t0) + " msec\n");
      console.print("Write OK\n");

    } catch (NumberFormatException e1) {
      JOptionPane.showMessageDialog(this, "Invalid argin syntaxt\n" + e1.getMessage());
    } catch (DevFailed e2) {
      ErrorPane.showErrorMessage(this, device.name(), e2);
    }

  }

  private void indent(int level) {
    for(int i=0;i<level;i++)
      console.print("  ");
  }

  private void indent(int level,StringBuffer str) {
    for(int i=0;i<level;i++)
      str.append("  ");
  }

  private void printPipeBlop(PipeBlob pb,int level) throws DevFailed {

     indent(level);
     console.print("[" + pb.getName() + "]\n");
     for(PipeDataElement item : pb) {

       int type = item.getType();
       indent(level+1);
       console.print(item.getName() + ":" + TangoConst.Tango_CmdArgTypeName[type]);
       switch(type) {
         case TangoConst.Tango_DEV_PIPE_BLOB:
           console.print("\n");
           printPipeBlop(item.extractPipeBlob(), level + 1);
           break;
         default:
           console.print(extractData(level+1,item));
       }

     }

  }

  private void readPipe() {

    try {

      PipeInfo pi = pipeInfo.get(pipeList.getSelectedIndex());
      long t0 = System.currentTimeMillis();
      DevicePipe argout = device.readPipe(pi.getName());
      long t1 = System.currentTimeMillis();
      console.print("----------------------------------------------------\n");
      console.print("Pipe: " + device.name() + "/" + pi.getName() + "\n");
      console.print("Duration: " + (t1-t0) + " msec\n");

      // Add the date of the measure in two formats
      TimeVal t = argout.getDevPipeDataObject().time;
      java.util.Date date = new java.util.Date((long) (t.tv_sec * 1000.0 + t.tv_usec / 1000.0));
      SimpleDateFormat dateformat =
          new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
      dateformat.setTimeZone(TimeZone.getDefault());
      console.print("measure date: " + dateformat.format(date) + " + " + (t.tv_usec / 1000) + "ms\n");
      printPipeBlop(argout.getPipeBlob(),0);

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


  private int getLimitMax(int level, StringBuffer retStr, int length) {

    if (length > common.getAnswerLimitMax()) {
      indent(level, retStr);
      retStr.append("Array cannot be fully displayed. (You may change the AnswerLimitMax)\n");
      return common.getAnswerLimitMax();
    } else {
      return length;
    }

  }


  private int getLimitMin(int level, StringBuffer retStr, int length) {

    if (length <= common.getAnswerLimitMin()) {
      indent(level, retStr);
      retStr.append("Array cannot be displayed. (You may change the AnswerLimitMin)\n");
      return length;
    } else {
      return common.getAnswerLimitMin();
    }

  }

  private PipeBlob parseData(String argin) throws NumberFormatException {

    ArgParser arg = new ArgParser(argin);
    return arg.parse_pipe();

  }

  private void printArrayItem(StringBuffer str,int idx,boolean printIdx,
                              String value,int level) {

     indent(level+1, str);
     if(printIdx)
       str.append("Read [" + idx + "]\t" + value + "\n");
     else
       str.append("Read:\t" + value + "\n");

  }

  private String extractData(int level,PipeDataElement data) {

    StringBuffer ret_string = new StringBuffer();
    boolean printIndex;

    try {

      // Add values
      switch (data.getType()) {

        case Tango_DEV_STATE:
        {
          DevState[] dummy = data.extractDevStateArray();
          printIndex = dummy.length>1;
          if(printIndex) ret_string.append(" Array length=" + Integer.toString(dummy.length));
          ret_string.append("\n");
          int start = getLimitMin(level,ret_string,dummy.length);
          int end = getLimitMax(level,ret_string,dummy.length);
          for (int i = start; i < end; i++)
            printArrayItem(ret_string,i,printIndex,Tango_DevStateName[dummy[i].value()],level);
        }
        break;

        case Tango_DEV_UCHAR:
        {
          short[] dummy = data.extractUCharArray();
          printIndex = dummy.length>1;
          if(printIndex) ret_string.append(" Array length=" + Integer.toString(dummy.length));
          ret_string.append("\n");
          int start = getLimitMin(level,ret_string,dummy.length);
          int end = getLimitMax(level,ret_string,dummy.length);
          for (int i = start; i < end; i++)
            printArrayItem(ret_string,i,printIndex,Short.toString(dummy[i]),level);
        }
        break;

        case Tango_DEV_SHORT:
        {
          short[] dummy = data.extractShortArray();
          printIndex = dummy.length>1;
          if(printIndex) ret_string.append(" Array length=" + Integer.toString(dummy.length));
          ret_string.append("\n");
          int start = getLimitMin(level,ret_string,dummy.length);
          int end = getLimitMax(level,ret_string,dummy.length);
          for (int i = start; i < end; i++)
            printArrayItem(ret_string,i,printIndex,Short.toString(dummy[i]),level);
        }
        break;

        case Tango_DEV_BOOLEAN:
        {
          boolean[] dummy = data.extractBooleanArray();
          printIndex = dummy.length>1;
          if(printIndex) ret_string.append(" Array length=" + Integer.toString(dummy.length));
          ret_string.append("\n");
          int start = getLimitMin(level,ret_string,dummy.length);
          int end = getLimitMax(level,ret_string,dummy.length);
          for (int i = start; i < end; i++)
            printArrayItem(ret_string,i,printIndex,Boolean.toString(dummy[i]),level);
        }
        break;

        case Tango_DEV_USHORT:
        {
          int[] dummy = data.extractUShortArray();
          printIndex = dummy.length>1;
          if(printIndex) ret_string.append(" Array length=" + Integer.toString(dummy.length));
          ret_string.append("\n");
          int start = getLimitMin(level,ret_string,dummy.length);
          int end = getLimitMax(level,ret_string,dummy.length);
          for (int i = start; i < end; i++)
            printArrayItem(ret_string,i,printIndex,Integer.toString(dummy[i]),level);
        }
        break;

        case Tango_DEV_LONG:
        {
          int[] dummy = data.extractLongArray();
          printIndex = dummy.length>1;
          if(printIndex) ret_string.append(" Array length=" + Integer.toString(dummy.length));
          ret_string.append("\n");
          int start = getLimitMin(level,ret_string,dummy.length);
          int end = getLimitMax(level,ret_string,dummy.length);
          for (int i = start; i < end; i++)
            printArrayItem(ret_string,i,printIndex,Integer.toString(dummy[i]),level);
        }
        break;

        case Tango_DEV_ULONG:
        {
          long[] dummy = data.extractULongArray();
          printIndex = dummy.length>1;
          if(printIndex) ret_string.append(" Array length=" + Integer.toString(dummy.length));
          ret_string.append("\n");
          int start = getLimitMin(level,ret_string,dummy.length);
          int end = getLimitMax(level,ret_string,dummy.length);
          for (int i = start; i < end; i++)
            printArrayItem(ret_string,i,printIndex,Long.toString(dummy[i]),level);
        }
        break;

        case Tango_DEV_LONG64:
        {
          long[] dummy = data.extractLong64Array();
          printIndex = dummy.length>1;
          if(printIndex) ret_string.append(" Array length=" + Integer.toString(dummy.length));
          ret_string.append("\n");
          int start = getLimitMin(level,ret_string,dummy.length);
          int end = getLimitMax(level,ret_string,dummy.length);
          for (int i = start; i < end; i++)
            printArrayItem(ret_string,i,printIndex,Long.toString(dummy[i]),level);
        }
        break;

        case Tango_DEV_ULONG64:
        {
          long[] dummy = data.extractULong64Array();
          printIndex = dummy.length>1;
          if(printIndex) ret_string.append(" Array length=" + Integer.toString(dummy.length));
          ret_string.append("\n");
          int start = getLimitMin(level,ret_string,dummy.length);
          int end = getLimitMax(level,ret_string,dummy.length);
          for (int i = start; i < end; i++)
            printArrayItem(ret_string,i,printIndex,Long.toString(dummy[i]),level);
        }
        break;

        case Tango_DEV_DOUBLE:
        {
          double[] dummy = data.extractDoubleArray();
          printIndex = dummy.length>1;
          if(printIndex) ret_string.append(" Array length=" + Integer.toString(dummy.length));
          ret_string.append("\n");
          int start = getLimitMin(level,ret_string,dummy.length);
          int end = getLimitMax(level,ret_string,dummy.length);
          for (int i = start; i < end; i++)
            printArrayItem(ret_string,i,printIndex,Double.toString(dummy[i]),level);
        }
        break;

        case Tango_DEV_FLOAT:
        {
          float[] dummy = data.extractFloatArray();
          printIndex = dummy.length>1;
          if(printIndex) ret_string.append(" Array length=" + Integer.toString(dummy.length));
          else ret_string.append("\n");
          int start = getLimitMin(level,ret_string,dummy.length);
          int end = getLimitMax(level,ret_string,dummy.length);
          for (int i = start; i < end; i++)
            printArrayItem(ret_string,i,printIndex,Float.toString(dummy[i]),level);
        }
        break;

        case Tango_DEV_STRING:
        {
          String[] dummy = data.extractStringArray();
          printIndex = dummy.length>1;
          if(printIndex) ret_string.append(" Array length=" + Integer.toString(dummy.length));
          ret_string.append("\n");
          int start = getLimitMin(level,ret_string,dummy.length);
          int end = getLimitMax(level,ret_string,dummy.length);
          for (int i = start; i < end; i++)
            printArrayItem(ret_string,i,printIndex,dummy[i],level);
        }
        break;

        case Tango_DEV_ENCODED:
        {
          DevEncoded[] e = data.extractDevEncodedArray();
          for (int j = 0; j < e.length; j++) {
            if(e.length>1) {
              indent(level,ret_string);
              console.print("Item #"+j+"\n");
            }
            indent(level, ret_string);
            ret_string.append("Format: " + e[j].encoded_format + "\n");
            printIndex = e[j].encoded_data.length>1;
            if(printIndex) ret_string.append(" Array length=" + Integer.toString(e[j].encoded_data.length));
            ret_string.append("\n");
            int start = getLimitMin(level, ret_string, e[j].encoded_data.length);
            int end = getLimitMax(level, ret_string, e[j].encoded_data.length);
            for (int i = start; i < end; i++) {
              short vs = (short) e[j].encoded_data[i];
              vs = (short) (vs & 0xFF);
              printArrayItem(ret_string, i, printIndex, Short.toString(vs), level);
            }
          }
        }
        break;

        default:
          indent(level,ret_string);
          ret_string.append("Unsupported type code="+data.getType()+"\n");
          break;
      }

    } catch (DevFailed e) {

      ErrorPane.showErrorMessage(this,device.name() + "/" + data.getName(),e);

    }

    return ret_string.toString();

  }

}
