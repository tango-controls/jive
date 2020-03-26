package jive3;

import fr.esrf.Tango.*;
import fr.esrf.TangoApi.*;
import fr.esrf.tangoatk.widget.util.ATKConstant;
import fr.esrf.tangoatk.widget.util.ErrorPane;
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

public class MultipleDeviceSelectionDlg extends JFrame implements ActionListener {

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
    int count;
    public String toString() {
      return name;
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
  JList attList;
  JTextField attValueText;
  JButton writeAttBtn;
  JButton readAttBtn;

  ArrayList<DItem> items;
  ArrayList<AItem> attItems;

  public MultipleDeviceSelectionDlg() {
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

    JPanel attributePanel = new JPanel();
    attributePanel.setLayout(new BorderLayout());
    attributePanel.setBorder(BorderFactory.createEtchedBorder());

    JPanel valuePanel = new JPanel();
    valuePanel.setLayout(new GridBagLayout());
    gbc.gridx = GridBagConstraints.RELATIVE;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 0;

    JLabel arginLabel = new JLabel("Argin");
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
        return column == 1;
      }

      public void setValueAt(Object aValue, int row, int column) {
      }

    };

    theTable = new JTable(dm);
    editor = new MultiLineCellEditor(theTable);
    theTable.setDefaultEditor(String.class, editor);
    MultiLineCellRenderer renderer = new MultiLineCellRenderer(false, false, false);
    theTable.setDefaultRenderer(String.class, renderer);
    theTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane textView = new JScrollPane(theTable);
    getContentPane().add(textView, BorderLayout.CENTER);


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
      int totalCount = 0;
      Vector<String> errStr = new Vector<String>();
      for (int i = 0; i < arg.svalue.length; i += 2) {

        DItem pi = new DItem();
        pi.devName = arg.svalue[i];
        pi.className = arg.svalue[i + 1];
        pi.value = "";
        items.add(pi);

        // Build attribute config
        try {

          DeviceProxy ds = new DeviceProxy(pi.devName);
          AttributeInfoEx[] conf = ds.get_attribute_info_ex();
          for (int j = 0; j < conf.length; j++)
            addAttribute(conf[j]);

        } catch (DevFailed e) {

          errStr.add(pi.devName+":"+e.errors[0].desc);

        }

        totalCount++;

      }

      if(errStr.size()>0) {
        JiveUtils.showJiveErrors(errStr);
      }

      // Remove non common item
      for (int i = 0; i < attItems.size(); ) {
        if (attItems.get(i).count != totalCount)
          attItems.remove(i);
        else
          i++;
      }

      // Populate list
      DefaultListModel ml = new DefaultListModel();
      for (int i = 0; i < attItems.size(); i++)
        ml.add(i, attItems.get(i));
      attList.setModel(ml);

      if (items.size() == 0)
        JOptionPane.showMessageDialog(this, "No device found");

      infoLabel.setText(items.size() + " item(s)");

      refreshTable();

    } catch (DevFailed e) {
      JiveUtils.showTangoError(e);
    }

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

    }

  }

  private void writeAtts(AItem att) throws NumberFormatException {

    DeviceAttribute send = new DeviceAttribute(att.name);
    ArgParser arg = new ArgParser(attValueText.getText());

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

}
