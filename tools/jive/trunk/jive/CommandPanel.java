package jive;

import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoApi.CommandInfo;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevVarLongStringArray;
import fr.esrf.Tango.DevVarDoubleStringArray;
import fr.esrf.Tango.DevState;
import fr.esrf.tangoatk.widget.util.ATKConstant;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.chart.JLChart;
import fr.esrf.tangoatk.widget.util.chart.JLDataView;
import fr.esrf.tangoatk.widget.util.chart.JLAxis;
import fr.esrf.TangoDs.TangoConst;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * The command panel.
 */
class CommandPanel extends JPanel implements ActionListener,ListSelectionListener,TangoConst,MouseListener,ClipboardOwner,DragGestureListener,DragSourceListener {

  private CommandInfo[] cmdList;
  private ConsolePanel  console;
  private DeviceProxy   device;
  private CommonPanel   common;

  private JList       commandList;
  private JScrollPane commandView;

  private JComboBox   arginCombo;
  private JLabel      arginLabel;
  private JLabel      descrLabel;

  private JLabel      arginTypeLabel;
  private JTextField  arginTypeText;
  private JLabel      argoutTypeLabel;
  private JTextField  argoutTypeText;

  private JButton      argDescrBtn;

  private JButton      executeBtn;
  private JButton      plotBtn;

  private JFrame       chartDlg = null;
  private JLChart      chart;
  private JLDataView   plotData;

  private JFrame       argDescriptionDlg = null;
  private JScrollPane  arginDescrView;
  private JScrollPane  argoutDescrView;
  private JTextArea    arginDescrText;
  private JTextArea    argoutDescrText;

  private JPopupMenu   copyMenu;
  private JMenuItem    copyCommandMenuItem;
  private JMenuItem    copyDevCommandMenuItem;

  /**
   * Construct the device panel
   * @param ds DeviceProxy
   */
  CommandPanel(DeviceProxy ds,ConsolePanel console,CommonPanel common) throws DevFailed {

    setLayout(null);

    this.console = console;
    this.common = common;
    this.device = ds;
    cmdList = getCommandList();

    DefaultListModel ml = new DefaultListModel();
    for (int i = 0; i < cmdList.length; i++)
      ml.add(i, cmdList[i].cmd_name);
    commandList = new JList(ml);
    commandList.addListSelectionListener(this);
    commandList.addMouseListener(this);
    DragSource dragSource = DragSource.getDefaultDragSource();
    dragSource.createDefaultDragGestureRecognizer(commandList,
            DnDConstants.ACTION_MOVE,
            this);
    commandView = new JScrollPane(commandList);
    add(commandView);

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

    arginTypeLabel = new JLabel("Argin Type");
    arginTypeLabel.setFont(ATKConstant.labelFont);
    add(arginTypeLabel);

    arginTypeText = new JTextField();
    arginTypeText.setMargin(JiveUtils.noMargin);
    arginTypeText.setEditable(false);
    add(arginTypeText);

    argDescrBtn = new JButton("Show description");
    argDescrBtn.setFont(ATKConstant.labelFont);
    argDescrBtn.addActionListener(this);
    add(argDescrBtn);

    argoutTypeLabel = new JLabel("Argout Type");
    argoutTypeLabel.setFont(ATKConstant.labelFont);
    add(argoutTypeLabel);

    argoutTypeText = new JTextField();
    argoutTypeText.setMargin(JiveUtils.noMargin);
    argoutTypeText.setEditable(false);
    add(argoutTypeText);

    argDescrBtn = new JButton("Show description");
    argDescrBtn.setFont(ATKConstant.labelFont);
    argDescrBtn.addActionListener(this);
    add(argDescrBtn);

    executeBtn = new JButton("Execute");
    executeBtn.setFont(ATKConstant.labelFont);
    add(executeBtn);
    executeBtn.addActionListener(this);
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

    commandList.setSelectedIndex(0);

    // Popup menu
    copyMenu = new JPopupMenu();
    copyCommandMenuItem = new JMenuItem("Copy command");
    copyCommandMenuItem.addActionListener(this);
    copyMenu.add(copyCommandMenuItem);
    copyDevCommandMenuItem = new JMenuItem("Copy device->command");
    copyDevCommandMenuItem.addActionListener(this);
    copyMenu.add(copyDevCommandMenuItem);

  }

  // -----------------------------------------------------

  public void valueChanged(ListSelectionEvent e) {

    int idx = commandList.getSelectedIndex();
    if(!e.getValueIsAdjusting()) {
      arginCombo.setEnabled(cmdList[idx].in_type!=Tango_DEV_VOID);
      descrLabel.setText(getExample(cmdList[idx].in_type));
      arginTypeText.setText(Tango_CmdArgTypeName[cmdList[idx].in_type]);
      argoutTypeText.setText(Tango_CmdArgTypeName[cmdList[idx].out_type]);
      if( argDescriptionDlg!=null ) {
        arginDescrText.setText("Argin description:\n"+cmdList[idx].in_type_desc);
        arginDescrText.setCaretPosition(0);
        argoutDescrText.setText("Argout description:\n"+cmdList[idx].out_type_desc);
        argoutDescrText.setCaretPosition(0);
        arginDescrView.revalidate();
        argoutDescrView.revalidate();
      }
      plotBtn.setEnabled(isPlotable(cmdList[idx].out_type));
    }

  }

  // -----------------------------------------------------

  public void mouseClicked(MouseEvent e) {

    Object src = e.getSource();

    if(src==commandList) {

      if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount()==2) {
        executeCommand();
      }

      if(e.getButton() == MouseEvent.BUTTON3) {
        copyMenu.show(commandList, e.getX() , e.getY());
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

    if(src == executeBtn) {
      executeCommand();
    } else if(src == plotBtn) {
      plotCommand();
    } else if(src == argDescrBtn) {
      showDescription();
    } else if(src==copyCommandMenuItem) {
      copyCommandToClipboard(true);
    } else if(src==copyDevCommandMenuItem) {
      copyCommandToClipboard(false);
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
  // Private stuff
  // -----------------------------------------------------
  private String getCopyString(boolean copyCmdOnly) {

    String copyStr = "";

    CommandInfo ci = cmdList[commandList.getSelectedIndex()];
    if( copyCmdOnly ) {
      copyStr = ci.cmd_name;
    } else {
      copyStr = device.get_name() + "->" + ci.cmd_name;
    }
    if( arginCombo.isEnabled() ) {
      String arginStr = (String)arginCombo.getEditor().getItem();
      if(arginStr!=null && arginStr.length()>0) {
        addArgin(arginStr);
        copyStr += "(" + arginStr + ")";
      }
    }

    return copyStr;

  }

  private void copyCommandToClipboard(boolean copyCmdOnly) {

    StringSelection stringSelection = new StringSelection(getCopyString(copyCmdOnly));
    java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    clipboard.setContents( stringSelection, this );

  }

  private CommandInfo[] getCommandList() throws DevFailed {

    int         i,j;
    boolean     end;
    CommandInfo tmp;

    CommandInfo[] lst = device.command_list_query();

    //Sort the list
    end = false;
    j=lst.length-1;
    while(!end) {
      end = true;
      for(i=0;i<j;i++) {
        if(lst[i].cmd_name.compareToIgnoreCase(lst[i+1].cmd_name)>0) {
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

  private boolean isPlotable(int outType) {

    switch(outType) {
      case Tango_DEVVAR_CHARARRAY:
      case Tango_DEVVAR_USHORTARRAY:
      case Tango_DEVVAR_SHORTARRAY:
      case Tango_DEVVAR_ULONGARRAY:
      case Tango_DEVVAR_LONGARRAY:
      case Tango_DEVVAR_FLOATARRAY:
      case Tango_DEVVAR_DOUBLEARRAY:
        return true;
    }

    return false;

  }

  private void placeComponents(Dimension dim) {

    arginLabel.setBounds(10,0,80,20);
    descrLabel.setBounds(90,0,dim.width-100,20);
    arginCombo.setBounds(10,20,dim.width-20,25);

    commandView.setBounds(10,50,190,dim.height-60);
    commandView.revalidate();

    int dim2 = (dim.width - 220) / 2;

    arginTypeLabel.setBounds(205,50,dim2,20);
    arginTypeText.setBounds(205,70,dim2,25);
    argoutTypeLabel.setBounds(210+dim2,50,dim2,20);
    argoutTypeText.setBounds(210+dim2,70,dim2,25);

    int btnWidth = 200;
    int org = (dim.width - 220 - btnWidth)/2;

    argDescrBtn.setBounds(205+org,dim.height-105,btnWidth,25);
    executeBtn.setBounds(205+org,dim.height-70,btnWidth,25);
    plotBtn.setBounds(205+org,dim.height-35,btnWidth,25);

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

  private void plotCommand() {

    try {

      CommandInfo ci = cmdList[commandList.getSelectedIndex()];
      String arginStr = (String)arginCombo.getSelectedItem();
      if(arginStr!=null) addArgin(arginStr);
      DeviceData argin = new DeviceData();
      insertData(arginStr,argin,ci.in_type);
      String cmd = ci.cmd_name;
      DeviceData argout = device.command_inout(cmd,argin);
      double[] values = extractPlotData(argout,ci.out_type);
      if( chartDlg == null ) {
        chart = new JLChart();
        chart.setPreferredSize(new Dimension(640,480));
        plotData = new JLDataView();
        chart.getY1Axis().addDataView(plotData);
        chart.getY1Axis().setAutoScale(true);
        chart.getXAxis().setAnnotation(JLAxis.VALUE_ANNO);
        chartDlg = new JFrame("Plot command [" + device.name() + "]");
        chartDlg.setContentPane(chart);
      }
      plotData.reset();
      plotData.setName(cmd);
      for(int i=0;i<values.length;i++) plotData.add(i,values[i]);
      chart.repaint();
      if(!chartDlg.isVisible()) {
        ATKGraphicsUtils.centerFrameOnScreen(chartDlg);
        chartDlg.setVisible(true);
      }

    } catch(NumberFormatException e1) {
      JOptionPane.showMessageDialog(this,"Invalid argin syntaxt\n"+e1.getMessage());
    } catch (DevFailed e2) {
      ErrorPane.showErrorMessage(this,device.name(),e2);
    }

  }

  private void executeCommand() {

    try {

      CommandInfo ci = cmdList[commandList.getSelectedIndex()];
      String arginStr = (String)arginCombo.getSelectedItem();
      if(arginStr!=null) addArgin(arginStr);
      DeviceData argin = new DeviceData();
      insertData(arginStr,argin,ci.in_type);
      String cmd = ci.cmd_name;
      long t0 = System.currentTimeMillis();
      DeviceData argout = device.command_inout(cmd,argin);
      long t1 = System.currentTimeMillis();
      console.print("----------------------------------------------------\n");
      console.print("Command: " + device.name() + "/" + cmd + "\n");
      console.print("Duration: " + (t1-t0) + " msec\n");
      if(ci.out_type==Tango_DEV_VOID) {
        console.print("Command OK\n");
      } else {
        console.print("Output argument(s) :\n");
        console.print(extractData(argout,ci.out_type));
      }
    } catch(NumberFormatException e1) {
      JOptionPane.showMessageDialog(this,"Invalid argin syntaxt\n"+e1.getMessage());
    } catch (DevFailed e2) {
      ErrorPane.showErrorMessage(this,device.name(),e2);
    }

  }

  private String getExample(int type) {

    String ret_string = new String("Ex: ");

    switch (type) {
      case Tango_DEV_VOID:
        ret_string = new String("");
        break;
      case Tango_DEV_STATE:
        ret_string += "0 (16bits value)";
        break;
      case Tango_DEV_BOOLEAN:
        ret_string += "true,false or 0,1";
        break;
      case Tango_DEV_UCHAR:
        ret_string += "10 or 0xa (unsigned 8bits)";
        break;
      case Tango_DEV_USHORT:
        ret_string += "10 or 0xa (unsigned 16bits)";
        break;
      case Tango_DEV_SHORT:
        ret_string += "10 or 0xa (signed 16bits)";
        break;
      case Tango_DEV_ULONG:
        ret_string += "10 or 0xa (unsigned 32bits)";
        break;
      case Tango_DEV_LONG:
        ret_string += "10 or 0xa (signed 32bits)";
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
      case Tango_DEVVAR_CHARARRAY:
        ret_string += "2,0xa,'A' (signed 8bits)";
        break;
      case Tango_DEVVAR_USHORTARRAY:
        ret_string += "2,0xa,4 (unsigned 16bits)";
        break;
      case Tango_DEVVAR_SHORTARRAY:
        ret_string += "2,0xa,4 (signed 16bits)";
        break;
      case Tango_DEVVAR_ULONGARRAY:
        ret_string += "2,0xa,4 (unsigned 32bits)";
        break;
      case Tango_DEVVAR_LONGARRAY:
        ret_string += "2,0xa,4 (signed 32bits)";
        break;
      case Tango_DEVVAR_FLOATARRAY:
        ret_string += "2.3,4 (32bits floats)";
        break;
      case Tango_DEVVAR_DOUBLEARRAY:
        ret_string += "2.3,4 (64bits floats)";
        break;
      case Tango_DEVVAR_STRINGARRAY:
        ret_string += "Dance,\"the TANGO\" (quotes needed for string with space or special char)";
        break;
      case Tango_DEVVAR_LONGSTRINGARRAY:
        ret_string += "[1,2][A,\"B C\",D] (quotes needed for string with space or special char)";
        break;
      case Tango_DEVVAR_DOUBLESTRINGARRAY:
        ret_string = "[1.0,2.0][A,\"B C\",D] (quotes needed for string with space or special char)";
        break;
      default:
        ret_string = new String("");
        break;
    }

    return ret_string;
  }

  private int getLimitMax(StringBuffer retStr,int length) {

    retStr.append("array length: " + length + "\n");
    if(length>common.getAnswerLimitMax()) {
      retStr.append("Array cannot be fully displayed. (You may change the AnswerLimitMax)\n");
      return common.getAnswerLimitMax();
    } else {
      return length;
    }

  }

  private int getLimitMin(StringBuffer retStr,int length) {

    if(length<=common.getAnswerLimitMin()) {
      retStr.append("Array cannot be displayed. (You may change the AnswerLimitMin)\n");
      return length;
    } else {
      return common.getAnswerLimitMin();
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

  private void insertData(String argin,DeviceData send,int outType) throws NumberFormatException {

    if(outType==Tango_DEV_VOID) return;

    ArgParser arg = new ArgParser(argin);

    switch (outType) {
      case Tango_DEV_BOOLEAN:
        send.insert(arg.parse_boolean());
        break;
      case Tango_DEV_USHORT:
        send.insert_us(arg.parse_ushort());
        break;
      case Tango_DEV_SHORT:
        send.insert(arg.parse_short());
        break;
      case Tango_DEV_ULONG:
        send.insert_ul(arg.parse_ulong());
        break;
      case Tango_DEV_LONG:
        send.insert(arg.parse_long());
        break;
      case Tango_DEV_FLOAT:
        send.insert(arg.parse_float());
        break;
      case Tango_DEV_DOUBLE:
        send.insert(arg.parse_double());
        break;
      case Tango_DEV_STRING:
        send.insert(arg.parse_string());
        break;
      case Tango_DEVVAR_CHARARRAY:
        send.insert(arg.parse_char_array());
        break;
      case Tango_DEVVAR_USHORTARRAY:
        send.insert_us(arg.parse_ushort_array());
        break;
      case Tango_DEVVAR_SHORTARRAY:
        send.insert(arg.parse_short_array());
        break;
      case Tango_DEVVAR_ULONGARRAY:
        send.insert_ul(arg.parse_ulong_array());
        break;
      case Tango_DEVVAR_LONGARRAY:
        send.insert(arg.parse_long_array());
        break;
      case Tango_DEVVAR_FLOATARRAY:
        send.insert(arg.parse_float_array());
        break;
      case Tango_DEVVAR_DOUBLEARRAY:
        send.insert(arg.parse_double_array());
        break;
      case Tango_DEVVAR_STRINGARRAY:
        send.insert(arg.parse_string_array());
        break;
      case Tango_DEVVAR_LONGSTRINGARRAY:
        send.insert(new DevVarLongStringArray(arg.parse_long_array(),arg.parse_string_array()));
        break;
      case Tango_DEVVAR_DOUBLESTRINGARRAY:
        send.insert(new DevVarDoubleStringArray(arg.parse_double_array(),arg.parse_string_array()));
        break;
      case Tango_DEV_STATE:
        send.insert(DevState.from_int(arg.parse_ushort()));
        break;

      default:
        throw new NumberFormatException("Command type not supported code=" + outType);

    }

  }

  private String extractData(DeviceData data,int outType) {

    StringBuffer ret_string = new StringBuffer();

    switch (outType) {

      case Tango_DEV_VOID:
        break;
      case Tango_DEV_BOOLEAN:
        ret_string.append(Boolean.toString(data.extractBoolean()));
        ret_string.append("\n");
        break;
      case Tango_DEV_USHORT:
        ret_string.append(Integer.toString(data.extractUShort()));
        ret_string.append("\n");
        break;
      case Tango_DEV_SHORT:
        ret_string.append(Short.toString(data.extractShort()));
        ret_string.append("\n");
        break;
      case Tango_DEV_ULONG:
        ret_string.append(Long.toString(data.extractULong()));
        ret_string.append("\n");
        break;
      case Tango_DEV_LONG:
        ret_string.append(Integer.toString(data.extractLong()));
        ret_string.append("\n");
        break;
      case Tango_DEV_FLOAT:
        ret_string.append(Float.toString(data.extractFloat()));
        ret_string.append("\n");
        break;
      case Tango_DEV_DOUBLE:
        ret_string.append(Double.toString(data.extractDouble()));
        ret_string.append("\n");
        break;
      case Tango_DEV_STRING:
        ret_string.append(data.extractString());
        ret_string.append("\n");
        break;
      case Tango_DEVVAR_CHARARRAY:
        {
          byte[] dummy = data.extractByteArray();
          int start = getLimitMin(ret_string,dummy.length);
          int end   = getLimitMax(ret_string,dummy.length);
          for (int i = start; i < end; i++) {
            ret_string.append("[" + i + "]\t " + Integer.toString(dummy[i]));
            if (dummy[i] >= 32)
              ret_string.append(" '" + (new Character((char) dummy[i]).toString()) + "'");
            else
              ret_string.append(" '.'");
            ret_string.append("\n");
          }
        }
        break;
      case Tango_DEVVAR_USHORTARRAY:
        {
          int[] dummy = data.extractUShortArray();
          int start = getLimitMin(ret_string,dummy.length);
          int end   = getLimitMax(ret_string,dummy.length);
          for (int i = start; i < end; i++)
            ret_string.append("[" + i + "]\t " + Integer.toString(dummy[i]) + "\n");
        }
        break;
      case Tango_DEVVAR_SHORTARRAY:
        {
          short[] dummy = data.extractShortArray();
          int start = getLimitMin(ret_string,dummy.length);
          int end   = getLimitMax(ret_string,dummy.length);
          for (int i = start; i < end; i++)
            ret_string.append("[" + i + "]\t " + Short.toString(dummy[i]) + "\n");
        }
        break;
      case Tango_DEVVAR_ULONGARRAY:
        {
          long[] dummy = data.extractULongArray();
          int start = getLimitMin(ret_string,dummy.length);
          int end   = getLimitMax(ret_string,dummy.length);
          for (int i = start; i < end; i++)
            ret_string.append("[" + i + "]\t " + Long.toString(dummy[i]) + "\n");
        }
        break;
      case Tango_DEVVAR_LONGARRAY:
        {
          int[] dummy = data.extractLongArray();
          int start = getLimitMin(ret_string,dummy.length);
          int end   = getLimitMax(ret_string,dummy.length);
          for (int i = start; i < end; i++)
            ret_string.append("[" + i + "]\t " + Integer.toString(dummy[i]) + "\n");
        }
        break;
      case Tango_DEVVAR_FLOATARRAY:
        {
          float[] dummy = data.extractFloatArray();
          int start = getLimitMin(ret_string,dummy.length);
          int end   = getLimitMax(ret_string,dummy.length);
          for (int i = start; i < end; i++)
            ret_string.append("[" + i + "]\t " + Float.toString(dummy[i]) + "\n");
        }
        break;
      case Tango_DEVVAR_DOUBLEARRAY:
        {
          double[] dummy = data.extractDoubleArray();
          int start = getLimitMin(ret_string,dummy.length);
          int end   = getLimitMax(ret_string,dummy.length);
          for (int i = start; i < end; i++)
            ret_string.append("[" + i + "]\t" + Double.toString(dummy[i]) + "\n");
        }
        break;
      case Tango_DEVVAR_STRINGARRAY:
        {
          String[] dummy = data.extractStringArray();
          int start = getLimitMin(ret_string,dummy.length);
          int end   = getLimitMax(ret_string,dummy.length);
          for (int i = start; i < end; i++)
            ret_string.append("[" + i + "]\t " + dummy[i] + "\n");
        }
        break;
      case Tango_DEVVAR_LONGSTRINGARRAY:
        {
          DevVarLongStringArray dummy = data.extractLongStringArray();
          int start = getLimitMin(ret_string,dummy.lvalue.length);
          int end = getLimitMax(ret_string,dummy.lvalue.length);
          ret_string.append("lvalue:\n");
          for (int i = start; i < end; i++)
             ret_string.append("[" + i + "]\t " + Integer.toString(dummy.lvalue[i]) + "\n");

          start = getLimitMin(ret_string,dummy.svalue.length);
          end = getLimitMax(ret_string,dummy.svalue.length);
          ret_string.append("svalue:\n");
          for (int i = start; i <end; i++)
            ret_string.append("[" + i + "]\t " + dummy.svalue[i] + "\n");
        }
        break;
      case Tango_DEVVAR_DOUBLESTRINGARRAY:
        {
          DevVarDoubleStringArray dummy = data.extractDoubleStringArray();
          int start = getLimitMin(ret_string,dummy.dvalue.length);
          int end = getLimitMax(ret_string,dummy.dvalue.length);
          ret_string.append("dvalue:\n");
          for (int i = start; i < end; i++)
            ret_string.append("[" + i + "]\t " + Double.toString(dummy.dvalue[i]) + "\n");

          start = getLimitMin(ret_string,dummy.svalue.length);
          end = getLimitMax(ret_string,dummy.svalue.length);
          ret_string.append("svalue:\n");
          for (int i = start; i < end; i++)
            ret_string.append("[" + i + "]\t " + dummy.svalue[i] + "\n");
        }
        break;

      case Tango_DEV_STATE:
        ret_string.append(Tango_DevStateName[data.extractDevState().value()]);
        ret_string.append("\n");
        break;

      default:
        ret_string.append("Unsupported command type code="+outType);
        ret_string.append("\n");
        break;
    }

    return ret_string.toString();
  }

  private double[] extractPlotData(DeviceData data,int outType) {

    double[] ret = new double[0];
    int i;

    switch (outType) {

      case Tango_DEVVAR_CHARARRAY:
        {
          byte[] dummy = data.extractByteArray();
          int start = this.getLimitMinForPlot(dummy.length);
          int end   = this.getLimitMaxForPlot(dummy.length);
          ret = new double[end-start];
          for(i=start;i<end;i++) ret[i-start] = (double)dummy[i];
        }
        break;
      case Tango_DEVVAR_USHORTARRAY:
        {
          int[] dummy = data.extractUShortArray();
          int start = this.getLimitMinForPlot(dummy.length);
          int end   = this.getLimitMaxForPlot(dummy.length);
          ret = new double[end-start];
          for(i=start;i<end;i++) ret[i-start] = (double)dummy[i];
        }
        break;
      case Tango_DEVVAR_SHORTARRAY:
        {
          short[] dummy = data.extractShortArray();
          int start = this.getLimitMinForPlot(dummy.length);
          int end   = this.getLimitMaxForPlot(dummy.length);
          ret = new double[end-start];
          for(i=start;i<end;i++) ret[i-start] = (double)dummy[i];
        }
        break;
      case Tango_DEVVAR_ULONGARRAY:
        {
          long[] dummy = data.extractULongArray();
          int start = this.getLimitMinForPlot(dummy.length);
          int end   = this.getLimitMaxForPlot(dummy.length);
          ret = new double[end-start];
          for(i=start;i<end;i++) ret[i-start] = (double)dummy[i];
        }
        break;
      case Tango_DEVVAR_LONGARRAY:
        {
          int[] dummy = data.extractLongArray();
          int start = this.getLimitMinForPlot(dummy.length);
          int end   = this.getLimitMaxForPlot(dummy.length);
          ret = new double[end-start];
          for(i=start;i<end;i++) ret[i-start] = (double)dummy[i];
        }
        break;
      case Tango_DEVVAR_FLOATARRAY:
        {
          float[] dummy = data.extractFloatArray();
          int start = this.getLimitMinForPlot(dummy.length);
          int end   = this.getLimitMaxForPlot(dummy.length);
          ret = new double[end-start];
          for(i=start;i<end;i++) ret[i-start] = (double)dummy[i];
        }
        break;
      case Tango_DEVVAR_DOUBLEARRAY:
        {
          double dummy[] = data.extractDoubleArray();
          int start = this.getLimitMinForPlot(dummy.length);
          int end   = this.getLimitMaxForPlot(dummy.length);
          ret = new double[end-start];
          for(i=start;i<end;i++) ret[i-start] = dummy[i];
        }
        break;

    }

    return ret;

  }

  private void showDescription() {

    if( argDescriptionDlg==null ) {

      JSplitPane innerPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

      // Create the frame
      arginDescrText = new JTextArea();
      arginDescrText.setEditable(false);
      arginDescrView = new JScrollPane(arginDescrText);
      arginDescrView.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
      arginDescrView.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      innerPane.setTopComponent(arginDescrView);

      argoutDescrText = new JTextArea();
      argoutDescrText.setEditable(false);
      argoutDescrView = new JScrollPane(argoutDescrText);
      argoutDescrView.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
      argoutDescrView.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      innerPane.setBottomComponent(argoutDescrView);

      argoutDescrText.setPreferredSize(new Dimension(400,150));
      arginDescrText.setPreferredSize(new Dimension(400,150));

      argDescriptionDlg = new JFrame("Command description [" + device.name() + "]");
      argDescriptionDlg.setContentPane(innerPane);

    }

    // Update fields
    int idx = commandList.getSelectedIndex();
    arginDescrText.setText("Argin description:\n"+ cmdList[idx].in_type_desc);
    argoutDescrText.setText("Argout description:\n"+ cmdList[idx].out_type_desc);

    if(!argDescriptionDlg.isVisible()) {
      ATKGraphicsUtils.centerFrameOnScreen(argDescriptionDlg);
      argDescriptionDlg.setVisible(true);
    } else {
      argDescriptionDlg.toFront();
    }

  }


}
