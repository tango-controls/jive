package jive3;

import fr.esrf.Tango.*;
import fr.esrf.TangoApi.*;
import fr.esrf.tangoatk.widget.util.ATKConstant;
import jive.ArgParser;
import jive.JiveUtils;
import jive.MultiLineCellEditor;
import jive.MultiLineCellRenderer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Vector;

import static fr.esrf.TangoDs.TangoConst.*;

public class MultipleTestDeviceDlg extends JFrame implements ActionListener {

  class DItem {
    String devName;
    String className;
    String value;
  };

  class AItem {
    String name;
    int type;
    AttrDataFormat format;
    String[] enum_labels;
    boolean writeable;
    int count;
    public String toString() {
      return name + " ("+ format.toString() + " " + Tango_CmdArgTypeName[type] + " " + (writeable?"RW":"RO") + ")";
    }
  };

  class CItem {
    String name;
    int in_type;
    int out_type;
    int count;
    public String toString() {
      return name + " (" + Tango_CmdArgTypeName[in_type] +"," + Tango_CmdArgTypeName[out_type] + ")";
    }
  };

  Database db;
  String colName[] = {"Name", "Class", "Value"};
  private MultiLineCellEditor editor;
  JTextField selectionText;
  JLabel selectionLabel;
  JButton searchButton;
  JButton dismissButton;
  JLabel infoLabel;
  DefaultTableModel dm;
  JTable theTable;
  JButton removeButton;
  JList attList;
  JTextField attValueText;
  JButton writeAttBtn;
  JButton readAttBtn;
  JList cmdList;
  JTextField cmdValueText;
  JButton execCmdBtn;

  ArrayList<DItem> items;
  ArrayList<AItem> attItems;
  ArrayList<CItem> cmdItems;

  public MultipleTestDeviceDlg() {
    this.db = null;
    initComponents();
  }

  void initComponents() {

    getContentPane().setLayout(new BorderLayout());

    // Search line
    JPanel innerPanel = new JPanel();
    innerPanel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.VERTICAL;
    JLabel selectionLabel = new JLabel(" Selection ");
    selectionLabel.setFont(ATKConstant.labelFont);
    innerPanel.add(selectionLabel, gbc);

    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.weightx = 1;
    gbc.fill = GridBagConstraints.BOTH;
    selectionText = new JTextField();
    selectionText.addActionListener(this);
    selectionText.setFont(ATKConstant.labelFont);
    innerPanel.add(selectionText, gbc);

    gbc.gridx = 2;
    gbc.gridy = 0;
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.VERTICAL;
    searchButton = new JButton("Search");
    searchButton.addActionListener(this);
    innerPanel.add(searchButton, gbc);

    // Bottom panel
    JPanel bottomPanel = new JPanel();
    bottomPanel.setLayout(new BorderLayout());

    JTabbedPane tabPane = new JTabbedPane();

    // Attribute Panel
    JPanel attributePanel = new JPanel();
    attributePanel.setLayout(new BorderLayout());
    attributePanel.setBorder(BorderFactory.createEtchedBorder());

    JPanel valuePanel = new JPanel();
    valuePanel.setLayout(new GridBagLayout());
    gbc.gridx = GridBagConstraints.RELATIVE;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 0;

    JLabel arginLabel = new JLabel("Argin ");
    valuePanel.add(arginLabel,gbc);
    gbc.weightx = 1.0;
    attValueText = new JTextField();
    attValueText.setEditable(true);
    valuePanel.add(attValueText,gbc);

    gbc.weightx = 0.0;
    readAttBtn = new JButton("Read");
    readAttBtn.addActionListener(this);
    valuePanel.add(readAttBtn,gbc);
    writeAttBtn = new JButton("Write");
    writeAttBtn.addActionListener(this);
    valuePanel.add(writeAttBtn,gbc);

    attributePanel.add(valuePanel,BorderLayout.NORTH);

    attList = new JList();
    JScrollPane attListScrool = new JScrollPane(attList);
    attributePanel.add(attListScrool,BorderLayout.CENTER);

    tabPane.add(attributePanel,"Attribute");

    // Command Panel
    JPanel commandPanel = new JPanel();
    commandPanel.setLayout(new BorderLayout());
    commandPanel.setBorder(BorderFactory.createEtchedBorder());

    JPanel cmdValuePanel = new JPanel();
    cmdValuePanel.setLayout(new GridBagLayout());
    gbc.gridx = GridBagConstraints.RELATIVE;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 0;

    JLabel cmdArginLabel = new JLabel("Argin ");
    cmdValuePanel.add(cmdArginLabel,gbc);
    gbc.weightx = 1.0;
    cmdValueText = new JTextField();
    cmdValueText.setEditable(true);
    cmdValuePanel.add(cmdValueText,gbc);

    gbc.weightx = 0.0;
    execCmdBtn = new JButton("Execute");
    execCmdBtn.addActionListener(this);
    cmdValuePanel.add(execCmdBtn,gbc);

    commandPanel.add(cmdValuePanel,BorderLayout.NORTH);

    cmdList = new JList();
    JScrollPane cmdListScrool = new JScrollPane(cmdList);
    commandPanel.add(cmdListScrool,BorderLayout.CENTER);

    tabPane.add(commandPanel,"Command");


    // Bottom Panel

    JPanel bottomBtnPanel = new JPanel();
    bottomBtnPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    infoLabel = new JLabel("0 item(s)");
    infoLabel.setFont(ATKConstant.labelFont);
    bottomBtnPanel.add(infoLabel);

    dismissButton = new JButton("Dismiss");
    dismissButton.addActionListener(this);
    bottomBtnPanel.add(dismissButton);

    bottomPanel.add(tabPane,BorderLayout.CENTER);
    bottomPanel.add(bottomBtnPanel,BorderLayout.SOUTH);

    getContentPane().add(innerPanel, BorderLayout.NORTH);
    getContentPane().add(bottomPanel, BorderLayout.SOUTH);

    // Table model
    dm = new DefaultTableModel() {

      public Class getColumnClass(int columnIndex) {
        return String.class;
      }

      public boolean isCellEditable(int row, int column) {
        return false;
      }

      public void setValueAt(Object aValue, int row, int column) {
      }

    };

    JPanel tablePanel = new JPanel();
    tablePanel.setLayout(new BorderLayout());

    theTable = new JTable(dm);
    editor = new MultiLineCellEditor(theTable);
    theTable.setDefaultEditor(String.class, editor);
    MultiLineCellRenderer renderer = new MultiLineCellRenderer(false, true, false);
    theTable.setDefaultRenderer(String.class, renderer);
    theTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    JScrollPane textView = new JScrollPane(theTable);

    tablePanel.add(textView,BorderLayout.CENTER);

    JPanel tableButtonPanel = new JPanel();
    tableButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
    removeButton = new JButton("Remove items");
    removeButton.addActionListener(this);
    tableButtonPanel.add(removeButton);

    tablePanel.add(tableButtonPanel, BorderLayout.SOUTH);

    getContentPane().add(tablePanel, BorderLayout.CENTER);

    setTitle("Multiple device selection");
    setPreferredSize(new Dimension(800, 600));

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        exitDlg();
        super.windowClosing(e);
      }
    });

    items = new ArrayList<DItem>();
    attItems = new ArrayList<AItem>();
    cmdItems = new ArrayList<CItem>();
    clear();

  }

  private void exitDlg() {


  }

  public void clear() {
    items.clear();
    infoLabel.setText(items.size() + " item(s)");
    String[][] prop = new String[0][2];
    dm.setDataVector(prop, colName);
    editor.updateRows();
    theTable.getColumnModel().getColumn(1).setPreferredWidth(250);
    theTable.validate();
    ((JPanel) getContentPane()).revalidate();
  }

  public void setDatabase(Database db) {
    this.db = db;
  }

  private void selectDevice() {
    refresh();
  }

  private void refresh() {

    String selectText = selectionText.getText().trim();

    String[] fields = selectText.split("/");

    if (fields.length != 3 && fields.length != 4) {
      JiveUtils.showJiveError("Invalid selection pattern, 3 or 4 slash separated fields expected");
      return;
    }

    try {

      items.clear();
      String request;

      // Select devices

      if (fields.length == 3) {

        fields[0] = fields[0].replace('*', '%');
        fields[1] = fields[1].replace('*', '%');
        fields[2] = fields[2].replace('*', '%');

        request = "select name,class from device where domain like '" + fields[0] + "' and family like '" +
                fields[1] + "' and member like '" + fields[2] + "'";

      } else {

        fields[0] = fields[0].replace('*', '%');
        fields[1] = fields[1].replace('*', '%');
        fields[2] = fields[2].replace('*', '%');
        fields[3] = fields[3].replace('*', '%');

        request = "select name,class from device where domain like '" + fields[0] + "' and family like '" +
                fields[1] + "' and member like '" + fields[2] + "' and class like '" + fields[3] + "'";

      }

      DeviceData argin = new DeviceData();
      argin.insert(request);
      DeviceData argout = db.command_inout("DbMySqlSelect", argin);

      DevVarLongStringArray arg = argout.extractLongStringArray();

      items.clear();
      for (int i = 0; i < arg.svalue.length; i += 2) {

        DItem pi = new DItem();
        pi.devName = arg.svalue[i];
        pi.className = arg.svalue[i + 1];
        pi.value = "";
        items.add(pi);

      }

      if (items.size() == 0)
        JOptionPane.showMessageDialog(this, "No device found");

      infoLabel.setText(items.size() + " item(s)");

      refreshCommandAndAttribute();
      refreshTable();

    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

  }

  private void refreshCommandAndAttribute() {

    Vector<String> errStr = new Vector<String>();

    for(int i=0;i<items.size();i++) {

      DItem pi = items.get(i);

      // Build attribute config
      try {

        DeviceProxy ds = new DeviceProxy(pi.devName);
        AttributeInfoEx[] conf = ds.get_attribute_info_ex();
        for (int j = 0; j < conf.length; j++)
          addAttribute(conf[j]);

        CommandInfo[] cmdInfo = ds.command_list_query();
        for (int j = 0; j < cmdInfo.length; j++)
          addCommand(cmdInfo[j]);

      } catch (DevFailed e) {

        errStr.add(pi.devName+":"+e.errors[0].desc);

      }

    }

    if(errStr.size()>0) {
      JiveUtils.showJiveErrors(errStr);
    }

    // Remove non common item
    for (int i = 0; i < attItems.size(); ) {
      if (attItems.get(i).count != items.size())
        attItems.remove(i);
      else
        i++;
    }

    for (int i = 0; i < cmdItems.size(); ) {
      if (cmdItems.get(i).count != items.size())
        cmdItems.remove(i);
      else
        i++;
    }

    // Populate list
    DefaultListModel ml = new DefaultListModel();
    for (int i = 0; i < attItems.size(); i++)
      ml.add(i, attItems.get(i));
    attList.setModel(ml);

    ml = new DefaultListModel();
    for (int i = 0; i < cmdItems.size(); i++)
      ml.add(i, cmdItems.get(i));
    cmdList.setModel(ml);

  }

  private void addAttribute(AttributeInfoEx e) {

    boolean found = false;
    int i = 0;
    while( !found && i<attItems.size() )  {
      found = attItems.get(i).name.equalsIgnoreCase(e.name) &&
              attItems.get(i).type == e.data_type &&
              attItems.get(i).format == e.data_format;
      if(!found) i++;
    }

    if(!found) {
      AItem it = new AItem();
      it.name = e.name;
      it.type = e.data_type;
      it.format = e.data_format;
      it.writeable = (e.writable == AttrWriteType.READ_WRITE) || (e.writable==AttrWriteType.WRITE);
      it.enum_labels = e.enum_label;
      it.count = 1;
      attItems.add(it);
    } else {
      attItems.get(i).count += 1;
    }

  }

  private void addCommand(CommandInfo e) {

    boolean found = false;
    int i = 0;
    while( !found && i<cmdItems.size() )  {
      found = cmdItems.get(i).name.equalsIgnoreCase(e.cmd_name) &&
              cmdItems.get(i).in_type == e.in_type &&
              cmdItems.get(i).out_type == e.out_type;
      if(!found) i++;
    }

    if(!found) {
      CItem it = new CItem();
      it.name = e.cmd_name;
      it.in_type = e.in_type;
      it.out_type = e.out_type;
      it.count = 1;
      cmdItems.add(it);
    } else {
      cmdItems.get(i).count += 1;
    }

  }

  private void refreshTable() {

    String[][] prop = new String[items.size()][3];
    for (int i = 0; i < items.size(); i++) {
      DItem it = items.get(i);
      prop[i][0] = it.devName;
      prop[i][1] = it.className;
      prop[i][2] = it.value;
    }
    dm.setDataVector(prop, colName);

    editor.updateRows();
    theTable.getColumnModel().getColumn(1).setPreferredWidth(250);
    theTable.validate();
    ((JPanel) getContentPane()).revalidate();

    infoLabel.setText(items.size() + " item(s)");

  }

  public void actionPerformed(ActionEvent evt) {

    Object src = evt.getSource();

    if (src == searchButton || src == selectionText) {

      selectDevice();

    } else if (src == dismissButton) {

      exitDlg();
      setVisible(false);

    } else if(src == readAttBtn) {

      int attId = attList.getSelectedIndex();
      if(attId>=0) {
        AItem ai = attItems.get(attId);
        for(int i=0;i<items.size();i++) {
          items.get(i).value = readAtt(items.get(i),ai);
        }
        refreshTable();
      }

    } else if(src==writeAttBtn) {

      int attId = attList.getSelectedIndex();
      if(attId>=0) {
        AItem ai = attItems.get(attId);
        try {
          writeAtts(ai);
        } catch (NumberFormatException e) {
          JiveUtils.showJiveError(e.getMessage());
        }

      }

    } else if(src==execCmdBtn) {

      int cmdId = cmdList.getSelectedIndex();
      if(cmdId>=0) {
        CItem ci = cmdItems.get(cmdId);
        try {
          execCommand(ci);
        } catch (NumberFormatException e) {
          JiveUtils.showJiveError(e.getMessage());
        }

      }

    } else if(src==removeButton) {

      int[] sel = theTable.getSelectedRows();
      for(int i=sel.length-1;i>=0;i--) {
        items.remove(sel[i]);
      }
      refreshCommandAndAttribute();
      refreshTable();

    }

  }

  private void writeAtts(AItem att) throws NumberFormatException {

    DeviceAttribute send = new DeviceAttribute(att.name);
    ArgParser arg = new ArgParser(attValueText.getText());

    if (items.size() > 1) {
      Vector propChange = new Vector();
      propChange.add(att.name);
      propChange.add(attValueText.getText());
      if (!MultiChangeConfirmDlg.confirmChange(propChange, items.size(),"", "Attribute")) {
        return;
      }
    }

    switch (att.type) {

      case Tango_DEV_STATE:
        switch (att.format.value()) {
          case AttrDataFormat._SCALAR:
            send.insert(arg.parse_state());
            break;
          case AttrDataFormat._SPECTRUM:
            send.insert(arg.parse_state_array());
            break;
          case AttrDataFormat._IMAGE:
            send.insert(arg.parse_state_image(),arg.get_image_width(),arg.get_image_height());
            break;
        }
        break;

      case Tango_DEV_UCHAR:
        switch (att.format.value()) {
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
        switch (att.format.value()) {
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
        switch (att.format.value()) {
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
        switch (att.format.value()) {
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
        switch (att.format.value()) {
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
        switch (att.format.value()) {
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
        switch (att.format.value()) {
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
        switch (att.format.value()) {
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
        switch (att.format.value()) {
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
        switch (att.format.value()) {
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
        switch (att.format.value()) {
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

      case Tango_DEV_ENUM:

        // Convert string to short array
        switch (att.format.value())
        {
          case AttrDataFormat._SCALAR: {
            String in = arg.parse_string();
            short idx = (short) JiveUtils.isInsideArray(in, att.enum_labels);
            if (idx < 0)
              throw new NumberFormatException("\"" + in + "\" not known in enum\nPossible values are:\n"+
                      JiveUtils.stringArrayToString(att.enum_labels));
            send.insert(idx);
          }
          break;
          case AttrDataFormat._SPECTRUM:
          {
            String[] in = arg.parse_string_array();
            short[] idx = new short[in.length];
            for (int i = 0; i < in.length; i++) {
              idx[i] = (short) JiveUtils.isInsideArray(in[i], att.enum_labels);
              if (idx[i] < 0)
                throw new NumberFormatException("\"" + in[i] + "\" not known in enum\nPossible values are:\n"+
                        JiveUtils.stringArrayToString(att.enum_labels));
            }
            send.insert(idx);
          }
          break;
          case AttrDataFormat._IMAGE:
          {
            String[] in = arg.parse_string_image();
            int width = arg.get_image_width();
            int height = arg.get_image_height();
            short[] idx = new short[in.length];
            for (int i = 0; i < in.length; i++) {
              idx[i] = (short) JiveUtils.isInsideArray(in[i], att.enum_labels);
              if (idx[i] < 0)
                throw new NumberFormatException("\"" + in[i] + "\" not known in enum\nPossible values are:\n"+
                        JiveUtils.stringArrayToString(att.enum_labels));
            }
            send.insert(idx, width, height);
          }
          break;
        }
        break;

      default:
        throw new NumberFormatException("Attribute type not supported code=" + att.type);

    }

    Vector<String> errList = new Vector<String>();

    for(int i=0;i<items.size();i++) {

      DItem di = items.get(i);

      try {
        DeviceProxy ds = new DeviceProxy(di.devName);
        ds.write_attribute(send);
      } catch (DevFailed e) {
        errList.add(di.devName+":"+e.errors[0].desc);
      }

    }

    if(errList.size()>0) {
      JiveUtils.showJiveErrors(errList);
    }

  }

  private String readAtt(DItem di, AItem att) {


    StringBuffer ret_string = new StringBuffer();

    try {

      DeviceProxy ds = new DeviceProxy(di.devName);
      DeviceAttribute data = ds.read_attribute(att.name);

      // Add the quality information
      if( data.getQuality().value() == AttrQuality._ATTR_INVALID )
        return "INVALID";

      int nbRead = data.getNbRead();

      switch (att.type) {

        case Tango_DEV_STATE:
        {
          DevState[] dummy = data.extractDevStateArray();
          for (int i = 0; i < nbRead; i++) {
            ret_string.append(Tango_DevStateName[dummy[i].value()]);
            if(i<nbRead-1) ret_string.append("\n");
          }
        }
        break;

        case Tango_DEV_UCHAR:
        {
          short[] dummy = data.extractUCharArray();
          for (int i = 0; i < nbRead; i++) {
            ret_string.append(dummy[i]);
            if(i<nbRead-1) ret_string.append("\n");
          }
        }
        break;

        case Tango_DEV_SHORT:
        {
          short[] dummy = data.extractShortArray();
          for (int i = 0; i < nbRead; i++) {
            ret_string.append(dummy[i]);
            if(i<nbRead-1) ret_string.append("\n");
          }
        }
        break;

        case Tango_DEV_BOOLEAN:
        {
          boolean[] dummy = data.extractBooleanArray();
          for (int i = 0; i < nbRead; i++) {
            ret_string.append(dummy[i]);
            if(i<nbRead-1) ret_string.append("\n");
          }
        }
        break;

        case Tango_DEV_USHORT:
        {
          int[] dummy = data.extractUShortArray();
          for (int i = 0; i < nbRead; i++) {
            ret_string.append(dummy[i]);
            if(i<nbRead-1) ret_string.append("\n");
          }
        }
        break;

        case Tango_DEV_LONG:
        {
          int[] dummy = data.extractLongArray();
          for (int i = 0; i < nbRead; i++) {
            ret_string.append(dummy[i]);
            if(i<nbRead-1) ret_string.append("\n");
          }
        }
        break;

        case Tango_DEV_ULONG:
        {
          long[] dummy = data.extractULongArray();
          for (int i = 0; i < nbRead; i++) {
            ret_string.append(dummy[i]);
            if(i<nbRead-1) ret_string.append("\n");
          }
        }
        break;

        case Tango_DEV_LONG64:
        {
          long[] dummy = data.extractLong64Array();
          for (int i = 0; i < nbRead; i++) {
            ret_string.append(dummy[i]);
            if(i<nbRead-1) ret_string.append("\n");
          }
        }
        break;

        case Tango_DEV_ULONG64:
        {
          long[] dummy = data.extractULong64Array();
          for (int i = 0; i < nbRead; i++) {
            ret_string.append(dummy[i]);
            if(i<nbRead-1) ret_string.append("\n");
          }
        }
        break;

        case Tango_DEV_DOUBLE:
        {
          double[] dummy = data.extractDoubleArray();
          for (int i = 0; i < nbRead; i++) {
            ret_string.append(dummy[i]);
            if(i<nbRead-1) ret_string.append("\n");
          }
        }
        break;

        case Tango_DEV_FLOAT:
        {
          float[] dummy = data.extractFloatArray();
          for (int i = 0; i < nbRead; i++) {
            ret_string.append(dummy[i]);
            if(i<nbRead-1) ret_string.append("\n");
          }
        }
        break;

        case Tango_DEV_STRING:
        {
          String[] dummy = data.extractStringArray();
          for (int i = 0; i < nbRead; i++) {
            ret_string.append(dummy[i]);
            if(i<nbRead-1) ret_string.append("\n");
          }
        }
        break;

        case Tango_DEV_ENCODED:
        {
          DevEncoded e = data.extractDevEncoded();
          ret_string.append(e.encoded_format);
        }
        break;

        case Tango_DEV_ENUM:
        {

          short[] dummy = data.extractShortArray();
          for (int i = 0; i < nbRead; i++) {
            ret_string.append(dummy[i] + " (" + att.enum_labels[i] + ")");
            if (i < nbRead - 1) ret_string.append("\n");
          }

        }
        break;

        default:
          ret_string.append("Unsupported attribute type code="+att.type+"\n");
          break;
      }

    } catch (DevFailed e) {

      ret_string.append(e.errors[0].desc);

    }

    return ret_string.toString();

  }

  private void execCommand(CItem ci) throws NumberFormatException {

    ArgParser arg=null;
    DeviceData send = null;

    if(ci.in_type != Tango_DEV_VOID) {
      arg = new ArgParser(cmdValueText.getText());
      try {
        send = new DeviceData();
      } catch (DevFailed e) {
        JiveUtils.showTangoError(e);
        return;
      }
    }

    if (items.size() > 1) {
      Vector propChange = new Vector();
      propChange.add(ci.name);
      if(arg==null) {
        propChange.add("None");
      } else {
        propChange.add(cmdValueText.getText());
      }
      if (!MultiChangeConfirmDlg.confirmChange(propChange, items.size(),"", "Command")) {
        return;
      }
    }

    if(send!=null) {

      switch (ci.in_type) {
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
        case Tango_DEV_LONG64:
          send.insert(arg.parse_long64());
          break;
        case Tango_DEV_ULONG64:
          send.insert_u64(arg.parse_long64());
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
        case Tango_DEVVAR_LONG64ARRAY:
          send.insert(arg.parse_long64_array());
          break;
        case Tango_DEVVAR_ULONG64ARRAY:
          send.insert_u64(arg.parse_long64_array());
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
          send.insert(new DevVarLongStringArray(arg.parse_long_array(), arg.parse_string_array()));
          break;
        case Tango_DEVVAR_DOUBLESTRINGARRAY:
          send.insert(new DevVarDoubleStringArray(arg.parse_double_array(), arg.parse_string_array()));
          break;
        case Tango_DEV_STATE:
          send.insert(DevState.from_int(arg.parse_ushort()));
          break;

        default:
          throw new NumberFormatException("Command type not supported code=" + ci.in_type);

      }

    }

    Vector<String> errList = new Vector<String>();

    for(int i=0;i<items.size();i++) {

      DItem di = items.get(i);

      try {

        DeviceProxy ds = new DeviceProxy(di.devName);
        DeviceData data;
        if(send==null) {
          data = ds.command_inout(ci.name);
        } else {
          data = ds.command_inout(ci.name, send);
        }

        switch (ci.out_type) {

          case Tango_DEV_VOID:
            break;
          case Tango_DEV_BOOLEAN:
            items.get(i).value = Boolean.toString(data.extractBoolean());
            break;
          case Tango_DEV_USHORT:
            items.get(i).value = Integer.toString(data.extractUShort());
            break;
          case Tango_DEV_SHORT:
            items.get(i).value = Short.toString(data.extractShort());
            break;
          case Tango_DEV_ULONG:
            items.get(i).value = Long.toString(data.extractULong());
            break;
          case Tango_DEV_ULONG64:
            items.get(i).value = Long.toString(data.extractULong64());
            break;
          case Tango_DEV_LONG:
            items.get(i).value = Integer.toString(data.extractLong());
            break;
          case Tango_DEV_LONG64:
            items.get(i).value = Long.toString(data.extractLong64());
            break;
          case Tango_DEV_FLOAT:
            items.get(i).value = Float.toString(data.extractFloat());
            break;
          case Tango_DEV_DOUBLE:
            items.get(i).value = Double.toString(data.extractDouble());
            break;
          case Tango_CONST_DEV_STRING:
          case Tango_DEV_STRING:
            items.get(i).value = data.extractString();
            break;
          case Tango_DEVVAR_CHARARRAY:
          {
            byte[] dummy = data.extractByteArray();
            StringBuffer ret_string = new StringBuffer();
            for (int j = 0; j < dummy.length; j++) {
              ret_string.append(Integer.toString(dummy[j]));
              if(i<dummy.length-1)
                ret_string.append("\n");
            }
            items.get(i).value = ret_string.toString();
          }
          break;
          case Tango_DEVVAR_USHORTARRAY:
          {
            int[] dummy = data.extractUShortArray();
            StringBuffer ret_string = new StringBuffer();
            for (int j = 0; j < dummy.length; j++) {
              ret_string.append(Integer.toString(dummy[j]));
              if(i<dummy.length-1)
                ret_string.append("\n");
            }
            items.get(i).value = ret_string.toString();
          }
          break;
          case Tango_DEVVAR_SHORTARRAY:
          {
            short[] dummy = data.extractShortArray();
            StringBuffer ret_string = new StringBuffer();
            for (int j = 0; j < dummy.length; j++) {
              ret_string.append(Integer.toString(dummy[j]));
              if(i<dummy.length-1)
                ret_string.append("\n");
            }
            items.get(i).value = ret_string.toString();
          }
          break;
          case Tango_DEVVAR_ULONGARRAY:
          {
            long[] dummy = data.extractULongArray();
            StringBuffer ret_string = new StringBuffer();
            for (int j = 0; j < dummy.length; j++) {
              ret_string.append(Long.toString(dummy[j]));
              if(i<dummy.length-1)
                ret_string.append("\n");
            }
            items.get(i).value = ret_string.toString();
          }
          break;
          case Tango_DEVVAR_ULONG64ARRAY:
          {
            long[] dummy = data.extractULong64Array();
            StringBuffer ret_string = new StringBuffer();
            for (int j = 0; j < dummy.length; j++) {
              ret_string.append(Long.toString(dummy[j]));
              if(i<dummy.length-1)
                ret_string.append("\n");
            }
            items.get(i).value = ret_string.toString();
          }
          break;
          case Tango_DEVVAR_LONGARRAY:
          {
            int[] dummy = data.extractLongArray();
            StringBuffer ret_string = new StringBuffer();
            for (int j = 0; j < dummy.length; j++) {
              ret_string.append(Integer.toString(dummy[j]));
              if(i<dummy.length-1)
                ret_string.append("\n");
            }
            items.get(i).value = ret_string.toString();
          }
          break;
          case Tango_DEVVAR_LONG64ARRAY:
          {
            long[] dummy = data.extractULong64Array();
            StringBuffer ret_string = new StringBuffer();
            for (int j = 0; j < dummy.length; j++) {
              ret_string.append(Long.toString(dummy[j]));
              if(i<dummy.length-1)
                ret_string.append("\n");
            }
            items.get(i).value = ret_string.toString();
          }
          break;
          case Tango_DEVVAR_FLOATARRAY:
          {
            float[] dummy = data.extractFloatArray();
            StringBuffer ret_string = new StringBuffer();
            for (int j = 0; j < dummy.length; j++) {
              ret_string.append(Float.toString(dummy[j]));
              if(i<dummy.length-1)
                ret_string.append("\n");
            }
            items.get(i).value = ret_string.toString();
          }
          break;
          case Tango_DEVVAR_DOUBLEARRAY:
          {
            double[] dummy = data.extractDoubleArray();
            StringBuffer ret_string = new StringBuffer();
            for (int j = 0; j < dummy.length; j++) {
              ret_string.append(Double.toString(dummy[j]));
              if(i<dummy.length-1)
                ret_string.append("\n");
            }
            items.get(i).value = ret_string.toString();
          }
          break;
          case Tango_DEVVAR_STRINGARRAY:
          {
            String[] dummy = data.extractStringArray();
            StringBuffer ret_string = new StringBuffer();
            for (int j = 0; j < dummy.length; j++) {
              ret_string.append(dummy[j]);
              if(i<dummy.length-1)
                ret_string.append("\n");
            }
            items.get(i).value = ret_string.toString();
          }
          break;
          case Tango_DEVVAR_LONGSTRINGARRAY:
          {
            DevVarLongStringArray dummy = data.extractLongStringArray();
            StringBuffer ret_string = new StringBuffer();
            ret_string.append("svalue:\n");
            for (int j = 0; j < dummy.svalue.length; j++) {
              ret_string.append(dummy.svalue[j]);
            }
            ret_string.append("lvalue:\n");
            for (int j = 0; j < dummy.lvalue.length; j++) {
              ret_string.append(dummy.lvalue[j]);
              if(i<dummy.lvalue.length-1)
                ret_string.append("\n");
            }
            items.get(i).value = ret_string.toString();
          }
          break;
          case Tango_DEVVAR_DOUBLESTRINGARRAY:
          {
            DevVarDoubleStringArray dummy = data.extractDoubleStringArray();
            StringBuffer ret_string = new StringBuffer();
            ret_string.append("svalue:\n");
            for (int j = 0; j < dummy.svalue.length; j++) {
              ret_string.append(dummy.svalue[j]);
            }
            ret_string.append("dvalue:\n");
            for (int j = 0; j < dummy.dvalue.length; j++) {
              ret_string.append(dummy.dvalue[j]);
              if(i<dummy.dvalue.length-1)
                ret_string.append("\n");
            }
            items.get(i).value = ret_string.toString();
          }
          break;

          case Tango_DEV_STATE:
            items.get(i).value = Tango_DevStateName[data.extractDevState().value()];
            break;

          default:
            errList.add("Unsupported command type code="+ci.out_type);
            break;
        }

      } catch (DevFailed e) {
        errList.add(di.devName+":"+e.errors[0].desc);
      }

    }

    if(errList.size()>0) {
      JiveUtils.showJiveErrors(errList);
    }

    if(ci.out_type!=Tango_DEV_VOID)
      refreshTable();

  }



}
