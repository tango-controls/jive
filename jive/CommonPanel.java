package jive;

import fr.esrf.TangoApi.DeviceProxy;
import fr.esrf.TangoApi.DeviceData;
import fr.esrf.tangoatk.widget.util.ATKConstant;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevInfo;
import fr.esrf.Tango.DevSource;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;

/**
 * Common panel
 */
class CommonPanel extends JPanel implements ActionListener {

  private DeviceProxy  device = null;
  private DeviceProxy  deviceAdm = null;
  private ConsolePanel console;
  private int answerLimitMin = 0;
  private int answerLimitMax = 1024;

  private JLabel      sourceLabel;
  private JComboBox   sourceCombo;
  private JLabel      timeoutLabel;
  private JTextField  timeoutText;
  private JLabel      blackBoxLabel;
  private JTextField  blackBoxText;
  private JLabel      limitMinLabel;
  private JTextField  limitMinText;
  private JLabel limitLengthLabel;
  private JTextField limitLengthText;

  // Generic commands
  private JButton     restartBtn;
  private JButton     pollStatusBtn;
  private JButton     infoBtn;
  private JButton     pingBtn;
  private JButton     setTimeoutBtn;
  private JButton     blackBoxBtn;
  private JButton     limitMinBtn;
  private JButton     limitLengthBtn;

  /**
   * Construct the common panel
   */
  CommonPanel(DeviceProxy ds,ConsolePanel console) throws DevFailed {

    device = ds;
    this.console = console;

    setLayout(null);

    sourceLabel = new JLabel("Source");
    sourceLabel.setFont(ATKConstant.labelFont);
    add(sourceLabel);

    sourceCombo = new JComboBox();
    sourceCombo.addItem("CACHE");
    sourceCombo.addItem("CACHE_DEVICE");
    sourceCombo.addItem("DEVICE");
    sourceCombo.setFont(ATKConstant.labelFont);
    sourceCombo.setSelectedIndex(1);
    sourceCombo.addActionListener(this);
    add(sourceCombo);

    timeoutLabel = new JLabel("Timeout (ms)");
    timeoutLabel.setFont(ATKConstant.labelFont);
    add(timeoutLabel);

    timeoutText = new JTextField();
    timeoutText.setText(Integer.toString(ds.get_timeout_millis()));
    timeoutText.setFont(ATKConstant.labelFont);
    timeoutText.setMargin(JiveUtils.noMargin);
    add(timeoutText);

    setTimeoutBtn = new JButton("Apply");
    setTimeoutBtn.setFont(ATKConstant.labelFont);
    setTimeoutBtn.addActionListener(this);
    add(setTimeoutBtn);

    blackBoxLabel = new JLabel("BlackBox (nb cmd)");
    blackBoxLabel.setFont(ATKConstant.labelFont);
    add(blackBoxLabel);

    blackBoxText = new JTextField();
    blackBoxText.setText("10");
    blackBoxText.setFont(ATKConstant.labelFont);
    blackBoxText.setMargin(JiveUtils.noMargin);
    add(blackBoxText);

    blackBoxBtn = new JButton("Execute");
    blackBoxBtn.setFont(ATKConstant.labelFont);
    blackBoxBtn.addActionListener(this);
    add(blackBoxBtn);

    limitMinLabel = new JLabel("Answer limit (min)");
    limitMinLabel.setFont(ATKConstant.labelFont);
    add(limitMinLabel);

    limitMinText = new JTextField();
    limitMinText.setText("0");
    limitMinText.setFont(ATKConstant.labelFont);
    limitMinText.setMargin(JiveUtils.noMargin);
    add(limitMinText);

    limitMinBtn = new JButton("Apply");
    limitMinBtn.setFont(ATKConstant.labelFont);
    limitMinBtn.addActionListener(this);
    add(limitMinBtn);

    limitLengthLabel = new JLabel("Answer limit (length)");
    limitLengthLabel.setFont(ATKConstant.labelFont);
    add(limitLengthLabel);

    limitLengthText = new JTextField();
    limitLengthText.setText("1024");
    limitLengthText.setFont(ATKConstant.labelFont);
    limitLengthText.setMargin(JiveUtils.noMargin);
    add(limitLengthText);

    limitLengthBtn = new JButton("Apply");
    limitLengthBtn.setFont(ATKConstant.labelFont);
    limitLengthBtn.addActionListener(this);
    add(limitLengthBtn);

    infoBtn = new JButton("Device Info");
    infoBtn.addActionListener(this);
    infoBtn.setFont(ATKConstant.labelFont);
    add(infoBtn);

    pingBtn = new JButton("Ping Device");
    pingBtn.setFont(ATKConstant.labelFont);
    pingBtn.addActionListener(this);
    add(pingBtn);

    pollStatusBtn = new JButton("Polling status");
    pollStatusBtn.setFont(ATKConstant.labelFont);
    pollStatusBtn.addActionListener(this);
    add(pollStatusBtn);

    restartBtn = new JButton("Restart");
    restartBtn.addActionListener(this);
    restartBtn.setFont(ATKConstant.labelFont);
    add(restartBtn);

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

    try {
      ds.ping();
      deviceAdm = ds.get_adm_dev();
    } catch(DevFailed e) {
      pingBtn.setEnabled(false);
      pollStatusBtn.setEnabled(false);
      restartBtn.setEnabled(false);
    }

  }

  private void placeComponents(Dimension dim) {

    sourceLabel.setBounds(10,10,120,25);
    sourceCombo.setBounds(130,10,150,25);

    timeoutLabel.setBounds(10,40,120,25);
    timeoutText.setBounds(130,40,60,25);
    setTimeoutBtn.setBounds(190,40,90,25);

    blackBoxLabel.setBounds(10,70,120,25);
    blackBoxText.setBounds(130,70,60,25);
    blackBoxBtn.setBounds(190,70,90,25);

    limitMinLabel.setBounds(10,100,120,25);
    limitMinText.setBounds(130,100,60,25);
    limitMinBtn.setBounds(190,100,90,25);

    limitLengthLabel.setBounds(10,130,120,25);
    limitLengthText.setBounds(130,130,60,25);
    limitLengthBtn.setBounds(190,130,90,25);

    infoBtn.setBounds(300,10,110,25);
    pingBtn.setBounds(300,40,110,25);
    pollStatusBtn.setBounds(300,70,110,25);
    restartBtn.setBounds(300,100,110,25);

  }

  public void actionPerformed(ActionEvent e) {

    Object src = e.getSource();

    if( src == limitMinBtn ) {
      try {
        answerLimitMin = Integer.parseInt(limitMinText.getText());
      } catch(NumberFormatException ex) {
        JOptionPane.showMessageDialog(this,"Invalid answer limit\n"+ex.getMessage());
      }
    } else if( src == limitLengthBtn) {
      try {
        answerLimitMax = Integer.parseInt(limitLengthText.getText()) + answerLimitMin;
      } catch(NumberFormatException ex) {
        JOptionPane.showMessageDialog(this,"Invalid answer lenght\n"+ex.getMessage());
      }
    } else if ( src == blackBoxBtn ) {
      try {
        int nbCmd = Integer.parseInt(blackBoxText.getText());
        long t0 = System.currentTimeMillis();
        String[] out = device.black_box(nbCmd);
        long t1 = System.currentTimeMillis();
        console.print("------------------------------------------\n");
        console.print("Command: " + device.name() + "/BlackBox\n");
        console.print("Duration: " + (t1-t0) + " msec\n\n");
        for (int i = 0; i < out.length; i++)
          console.print("[" + i + "]\t " + out[i] + "\n");

      } catch(NumberFormatException e1) {
        JOptionPane.showMessageDialog(this,"Invalid command number\n"+e1.getMessage());
      } catch (DevFailed e2) {
        ErrorPane.showErrorMessage(this,device.name(),e2);
      }
    } else if ( src == infoBtn ) {
      try {
        long t0 = System.currentTimeMillis();
        DevInfo out = device.info();
        long t1 = System.currentTimeMillis();
        console.print("----------------------------------------------------\n");
        console.print("Command: " + device.name() + "/Info\n");
        console.print("Duration: " + (t1-t0) + " msec\n\n");
        console.print("Server: " + out.server_id + "\n");
        console.print("Server host: " + out.server_host + "\n");
        console.print("Server version: " + out.server_version + "\n");
        console.print("Class: " + out.dev_class + "\n");
        console.print(out.doc_url + "\n");
      } catch (DevFailed e1) {
        ErrorPane.showErrorMessage(this,device.name(),e1);
      }
    } else if ( src == pingBtn ) {
      try {
        long t0 = System.currentTimeMillis();
        device.ping();
        long t1 = System.currentTimeMillis();
        console.print("------------------------------------------\n");
        console.print("Command: " + device.name() + "/Ping\n");
        console.print("Duration: " + (t1-t0) + " msec\n\n");
        console.print("Device is alive\n");
      } catch (DevFailed e1) {
        ErrorPane.showErrorMessage(this,device.name(),e1);
      }
    }  else if ( src == pollStatusBtn ) {
      try {
        DeviceData argin = new DeviceData();
        argin.insert(device.name());
        DeviceData argout = deviceAdm.command_inout("DevPollStatus",argin);
        String[] pollStatus = argout.extractStringArray();
        console.print("----------------------------------------------------\n");
        for(int i=0;i<pollStatus.length;i++)
          console.print(pollStatus[i]+"\n\n");
      } catch (DevFailed e1) {
        ErrorPane.showErrorMessage(this,device.name(),e1);
      }
    } else if ( src == restartBtn ) {
      try {
        DeviceData argin = new DeviceData();
        argin.insert(device.name());
        deviceAdm.command_inout("DevRestart",argin);
        console.print("----------------------------------------------------\n");
          console.print("Restart OK\n\n");
      } catch (DevFailed e1) {
        ErrorPane.showErrorMessage(this,device.name(),e1);
      }
    } else if ( src == setTimeoutBtn ) {
      try {
        int timeout = Integer.parseInt(timeoutText.getText());
        device.set_timeout_millis(timeout);
      } catch(NumberFormatException e1) {
        JOptionPane.showMessageDialog(this,"Invalid timeout value\n"+e1.getMessage());
      } catch (DevFailed e2) {
        ErrorPane.showErrorMessage(this,device.name(),e2);
      }
    } else if ( src == sourceCombo ) {
      try {
        int idx = sourceCombo.getSelectedIndex();
        switch(idx) {
          case 0:
            device.set_source(DevSource.CACHE);
            break;
          case 1:
            device.set_source(DevSource.CACHE_DEV);
            break;
          case 2:
            device.set_source(DevSource.DEV);
            break;
        }
      } catch (DevFailed e1) {
        ErrorPane.showErrorMessage(this,device.name(),e1);
      }
    }

  }

  public int getAnswerLimitMin() {
    return answerLimitMin;
  }

  public int getAnswerLimitMax() {
    return answerLimitMax;
  }

  public Dimension getPreferredSize() {
    return getMinimunSize();
  }

  public Dimension getMinimunSize() {
    return new Dimension(470,135);
  }


}

