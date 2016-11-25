package jive3;

import fr.esrf.TangoApi.DbServInfo;
import fr.esrf.tangoatk.widget.util.ATKConstant;
import fr.esrf.tangoatk.widget.util.ATKGraphicsUtils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;


@SuppressWarnings("MagicConstant")
public class ServerInfoDlg extends javax.swing.JDialog {

  public static final int RET_CANCEL = JOptionPane.CANCEL_OPTION;
  public static final int RET_OK = JOptionPane.OK_OPTION;

  private DbServInfo server_info;

  private JButton unregisterBtn;
  private boolean unregister = false;
  private Component parent;

  private JRadioButton yesButton;
  private JRadioButton noButton;
  private JComboBox<String> levelCombo;
  private JLabel title;
  private int returnStatus = RET_CANCEL;

  //-======================================================================
  //-======================================================================
  public ServerInfoDlg(JFrame parent) {

    super(parent, true);
    this.parent = parent;
    initComponents();

    //	Initialize ComboBox
    levelCombo.addItem("None");
    for (int i = 1; i <= TreePanelHostCollection.NB_LEVELS; i++) {
      String s = "Level " + i;
      levelCombo.addItem(s);
    }

    setTitle("Change Startup Level");

  }

  private void initComponents() {

    JPanel buttonPanel = new JPanel();
    JButton okButton = new JButton();
    JButton cancelButton = new JButton();
    JPanel centerPanel = new JPanel();
    JLabel jLabel1 = new JLabel();
    yesButton = new javax.swing.JRadioButton();
    yesButton.setFont(ATKConstant.labelFont);
    noButton = new javax.swing.JRadioButton();
    noButton.setFont(ATKConstant.labelFont);
    JLabel jLabel2 = new JLabel();
    levelCombo = new JComboBox();
    levelCombo.setPreferredSize(new Dimension(200,25));
    title = new javax.swing.JLabel();
    title.setForeground(java.awt.Color.black);
    title.setFont(new java.awt.Font("Dialog", 1, 16));

    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent evt) {
        closeDialog(evt);
      }
    }
    );

    buttonPanel.setLayout(new java.awt.FlowLayout(FlowLayout.RIGHT));


    unregisterBtn = new JButton("Remove startup level info");
    unregisterBtn.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        unregisterBtnActionPerformed(evt);
      }
    });

    buttonPanel.add(unregisterBtn);

    okButton.setText("OK");
    okButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        okButtonActionPerformed(evt);
      }
    }
    );

    buttonPanel.add(okButton);

    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cancelButtonActionPerformed(evt);
      }
    }
    );

    buttonPanel.add(cancelButton);

    getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);

    centerPanel.setLayout(new java.awt.GridBagLayout());
    centerPanel.setBorder(BorderFactory.createEtchedBorder());
    java.awt.GridBagConstraints gridBagConstraints;

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.insets = new java.awt.Insets(5, 10, 10, 10);
    centerPanel.add(title, gridBagConstraints);

    jLabel1.setText("Controlled by Astor");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.weightx = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 5);
    centerPanel.add(jLabel1, gridBagConstraints);

    yesButton.setText("Yes");
    yesButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        yesnoButtonActionPerformed(evt);
      }
    }
    );
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.weightx = 0;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 5);
    centerPanel.add(yesButton, gridBagConstraints);

    noButton.setText("No");
    noButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        yesnoButtonActionPerformed(evt);
      }
    }
    );
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 5);
    centerPanel.add(noButton, gridBagConstraints);

    jLabel2.setText("Startup Level");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.weightx = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 10, 5);
    centerPanel.add(jLabel2, gridBagConstraints);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.weightx = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.insets = new java.awt.Insets(2, 2, 10, 5);
    centerPanel.add(levelCombo, gridBagConstraints);

    getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

  }//GEN-END:initComponents

  //============================================================
  //============================================================
  private void unregisterBtnActionPerformed(@SuppressWarnings("UnusedParameters") java.awt.event.ActionEvent evt) {
    //	Ask to confirm
    if (unregister = (JOptionPane.showConfirmDialog(parent,
        "Are you sure to want to remove " +
            server_info.name + " startup info ?",
        "Confirm Dialog",
        JOptionPane.YES_NO_OPTION)) == JOptionPane.OK_OPTION) {
      doClose(RET_OK);
    }
  }

  //============================================================

  private void yesnoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yesnoButtonActionPerformed
    String org = evt.getActionCommand();
    if (org.equals("Yes"))
      updateButtons(true, server_info.startup_level);
    else
      updateButtons(false, 0);
  }

  //============================================================

  private void cancelButtonActionPerformed(@SuppressWarnings("UnusedParameters") java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
    doClose(RET_CANCEL);
  }

  //============================================================

  private void okButtonActionPerformed(@SuppressWarnings("UnusedParameters") java.awt.event.ActionEvent evt) {
    boolean ctrl = (yesButton.getSelectedObjects() != null);
    int level = levelCombo.getSelectedIndex();
    if (!ctrl || level == 0) {
      level = 0;
    }

    //  Check if has changed
    if (ctrl!=server_info.controlled ||
        level!=server_info.startup_level) {
      doClose(RET_OK);
    }
    else
      doClose(RET_CANCEL);

  }


  private void closeDialog(@SuppressWarnings("UnusedParameters") java.awt.event.WindowEvent evt) {
    doClose(RET_CANCEL);
  }

  //============================================================
  private void doClose(int retStatus) {
    returnStatus = retStatus;
    setVisible(false);
    dispose();
  }


  //============================================================
  // Update configuration buttons.
  private void updateButtons(boolean ctrl, int level) {

    yesButton.setSelected(ctrl);
    noButton.setSelected(!ctrl);
    levelCombo.setEnabled(ctrl);

    if (ctrl) {
      server_info.startup_level = level;
      levelCombo.setSelectedIndex(level);
      unregisterBtn.setVisible(false);
    } else {
      levelCombo.setSelectedIndex(0);
      if (server_info.name.length() > 0 && manage_unregister)
        unregisterBtn.setVisible(true);
      else
        unregisterBtn.setVisible(false);
    }

  }

  //============================================================
  // Update configuration buttons and display dialog
  public int showDialog(DbServInfo info) {
    server_info = info;

    title.setText("  " + info.name + " running on " + info.host + "  ");
    ATKGraphicsUtils.centerDialog(this);
    updateButtons(info.controlled, info.startup_level);
    setVisible(true);
    return returnStatus;

  }

  //============================================================
  // Update configuration buttons and display dialog
  private boolean manage_unregister = true;

  public int showDialog(DbServInfo info, int level) {

    server_info = info;
    manage_unregister = false;

    title.setText("  " + "Servers (Level " + level + ")   running on " + info.host + "  ");
    updateButtons(info.controlled, info.startup_level);

    pack();
    setVisible(true);
    return returnStatus;
  }

  //============================================================

  /**
   * Get configuration from buttons and return info object.
   */
  //============================================================
  public DbServInfo getSelection() {
    if (unregister)
      return null;

    boolean ctrl = (yesButton.getSelectedObjects() != null);
    int level = levelCombo.getSelectedIndex();
    if (!ctrl || level == 0) {
      level = 0;
    }

    return new DbServInfo(server_info.name, server_info.host, ctrl, level);
  }

}
