package jive3;

import jive.JiveUtils;

/**
 * A special node for property
 */
abstract public class PropertyNode extends TangoNode {

  abstract String getName();

  abstract String[][] getProperties();

  abstract void setProperty(String propName, String value);

  abstract void deleteProperty(String propName);

  abstract void viewHistory();

  abstract void saveProperties();

  public String getDescription(String name) {
    return "No description available";
  }

  void rename(String oldName,String value,String newName) {
    deleteProperty(oldName);
    setProperty(newName,value);
  }

  public Action[] getAction() {
    if(JiveUtils.readOnly)
      return new Action[0];
    else
      return new Action[]{
          TreePanel.getAction(TreePanel.ACTION_COPY),
          TreePanel.getAction(TreePanel.ACTION_PASTE),
          TreePanel.getAction(TreePanel.ACTION_VIEW_HISTORY),
          TreePanel.getAction(TreePanel.ACTION_SAVE_PROP)
      };
  }

  public void defaultPropertyAction(int number) {

    switch(number) {

      case TreePanel.ACTION_COPY:
        JiveUtils.the_clipboard.clear();
        String[][] props = getProperties();
        for(int i=0;i<props.length;i++)
          JiveUtils.the_clipboard.add(props[i][0],props[i][1]);
        break;

      case TreePanel.ACTION_PASTE:
        JiveUtils.the_clipboard.parse();
        for(int i=0;i<JiveUtils.the_clipboard.getObjectPropertyLength();i++)
          setProperty(JiveUtils.the_clipboard.getObjectPropertyName(i),
                      JiveUtils.the_clipboard.getObjectPropertyValue(i));
        parentPanel.refreshValues();
        break;

      case TreePanel.ACTION_VIEW_HISTORY:
        viewHistory();
        break;

      case TreePanel.ACTION_SAVE_PROP:
        saveProperties();
        break;

    }

  }

}
