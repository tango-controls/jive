package jive;

import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;
import fr.esrf.tangoatk.widget.util.ErrorPane;
import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceProxy;

import javax.swing.*;
import java.awt.*;

public class ExecDev extends JPanel {

  /**
   * Constrcut the panel
   */
  public ExecDev(String devName) throws DevFailed {

    DeviceProxy ds = new DeviceProxy(devName);
    setLayout(new BorderLayout());
    ConsolePanel console = new ConsolePanel();
    CommonPanel  common  = new CommonPanel(ds,console);
    JTabbedPane tab = new JTabbedPane();
    tab.add("Commands",new CommandPanel(ds,console,common));
    tab.add("Attributes",new AttributePanel(ds,console,common));
    tab.add("Admin",common);
    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    splitPane.setTopComponent(tab);
    splitPane.setBottomComponent(console);
    splitPane.setDividerLocation(235);
    add(splitPane,BorderLayout.CENTER);

  }

  /**
    * Main function : Launch the device panel
    * @param args Device name
   */
  static public void main(String args[]) {

    if (args.length != 1) {

      System.out.println("Usage: tg_devtest devicename");

    } else {

      try {
        ExecDev p = new ExecDev(args[0]);
        JFrame f = new JFrame();
        f.setTitle("Device panel [" + args[0] +"]");
        f.setContentPane(p);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ATKGraphicsUtils.centerFrameOnScreen(f);
        f.setVisible(true);
      } catch(DevFailed e) {
        ErrorPane.showErrorMessage(null,args[0],e);
      }

    }

  }

} // end of class ExecDev
