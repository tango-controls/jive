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

  void rename(String oldName,String value,String newName) {
    deleteProperty(oldName);
    setProperty(newName,value);
  }

  public int[] getAction() {
    if(JiveUtils.readOnly)
      return new int[0];      
    else
      return new int[]{TreePanel.ACTION_COPY,TreePanel.ACTION_PASTE};
  }

  public void execAction(int number) {
    switch(number) {

      case TreePanel.ACTION_COPY:
        JiveUtils.the_clipboard.clear();
        String[][] props = getProperties();
        for(int i=0;i<props.length;i++)
          JiveUtils.the_clipboard.add(props[i][0],props[i][1]);
        break;

      case TreePanel.ACTION_PASTE:
        for(int i=0;i<JiveUtils.the_clipboard.getObjectPropertyLength();i++)
          setProperty(JiveUtils.the_clipboard.getObjectPropertyName(i),
                      JiveUtils.the_clipboard.getObjectPropertyValue(i));
        parentPanel.refreshValues();
        break;

    }
  }

}
