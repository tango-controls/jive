package jive;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.io.*;
import fr.esrf.TangoApi.*;
import fr.esrf.Tango.*;
import fr.esrf.TangoDs.*;

/*
 *  Tango database Tree viewer
 *  Jean-Luc PONS     2002
 *
 *  Simple TreeNode that builds children on the fly.
 *  The key idea is that getChildCount is always called before
 *  any actual children are requested. So getChildCount builds
 *  the children if they don't already exist.
 */

// Inner Class to handle search  
class FindInfo {
   
   public boolean        found;
   public int            index;
   public TangoTreeNode  nodeFound;   
   
   FindInfo() { 
     index=0; found=false; 
   }
      
   FindInfo(int i , boolean f , TangoTreeNode m ) {
     index=i; found=f; nodeFound=m;
   }
   
}

// Inner class to handle the device panel
class JiveMenu extends JDialog {
	   
	   ExecDev p;
	   
	   JiveMenu(Frame parent) {
	     super(parent,true);
	     setTitle("Device Panel");
	     getContentPane().setLayout(new GridLayout(1,0));
	     Rectangle r = parent.getBounds();
     	     int x = r.x + (r.width-450)/2;
     	     int y = r.y + (r.height-650)/2;
             setBounds(x,y,450,650);     
	     p=new ExecDev();
	     getContentPane().add(p);   
	     	     
	     addComponentListener( new ComponentListener() {
               public void componentHidden(ComponentEvent e) {} 
               public void componentMoved(ComponentEvent e) {}	  	     
               public void componentResized(ComponentEvent e) { sizeIt(); }
               public void componentShown(ComponentEvent e) { sizeIt(); }
	     });

	   }
	   
	   public void sizeIt() {
             p.placeComponents(getContentPane().getSize());
	   }
	   
	   public void showDlg(String devname) {	     
	     if( !p.set_device_name( devname) ) {
	       show();
	     }
	   }
	   
}
 
public class TangoTreeNode extends DefaultMutableTreeNode {

  private boolean  areChildrenDefined = false;
  private int      numChildren;
  public  int      type;
  public  int      level;
  private String   value;
  private Database db;


  // Global variable
  
  public static int nbAction=16;
  public static RenameDlg dlg=null;  
  public static String[] att_prop_default = {
    "description","label","unit","standard_unit","display_unit",
    "format","min_value","max_value","min_alarm","max_alarm" 
  };
  
  public static Clipboard the_clipboard = new Clipboard();
  public static Frame parent=null;
  public static atkpanel.MainPanel atkdlg=null;
  public static boolean error_report = true;

  // Search options and control
  public static int        scan_progress;
  static public Stack      searchStack=null;
  static public String     searchText;
  static public boolean    searchIngoreCase;
  static public boolean    searchValues;
  static public boolean    searchAttributes;
  static public boolean    searchUseRegexp;
  static public boolean    searchOnlyLeaf;
  static public boolean    searchStopflag;
  static public TreePath   searchResult;
  static public TreePath[] searchResults;
  static public ThreadDlg  searchDlg;
    
  // *****************************************************************************************************************  
  // Contruct a Tango Tree
  public TangoTreeNode(int level,int type,String value,Database db) {
     // Root node
     this.level=level;
     this.type=type;
     this.value=value;
     this.db=db;
  }
  
  // *****************************************************************************************************************  
  // Return true when tree node is a Leaf
  public boolean isLeaf() {
    
    switch( type ) {
      case 1: // CLASS
        if((level==4) && getParent().toString().equals("PROPERTY"))
	  return true;
	if((level==5) && getParent().getParent().toString().equals("ATTRIBUTE"))
	  return true;      
        return false;
      case 2: // SERVER
	return (level>=4);
      case 3: // DEVICE
        if((level==6) && getParent().toString().equals("PROPERTY"))
	  return true;
	if((level==7) && getParent().getParent().toString().equals("ATTRIBUTE"))
	  return true;
	return false;
      case 4: // PROPERTY
        if(level==3) return true;
        return false;	
      case 5: // TAGS
	return (level>=2);
     default:
	return false;
    }
    
  }
  
  // *****************************************************************************************************************  
  // Return the number of children and create them if they don't already exists
  public int getChildCount() {
    if (!areChildrenDefined)
      defineChildNodes();
    return(super.getChildCount());
  }

  // *****************************************************************************************************************  
  // Add dinamycaly nodes in the tree when the user open a branch.
  private void defineChildNodes() {
    
    //  The flag areChildrenDefined must set before defining children
    // Otherwise you get an infinite recursive loop, since add results
    // in a call to getChildCount.
    
    areChildrenDefined = true;
    int i;
    
    if(level==0) {
       
	 // ROOT node
	 numChildren = 5;
         add(new TangoTreeNode(1,1,"CLASS",db));
         add(new TangoTreeNode(1,2,"SERVER",db));
         add(new TangoTreeNode(1,3,"DEVICE",db));
         add(new TangoTreeNode(1,4,"PROPERTY",db));
         add(new TangoTreeNode(1,5,"TAGS",db));
	 
   } else if(db!=null) {
  
         // Tango DB Browsing
         String[] list = new String[0];
	 String   devname;
	
	 try {	  
         switch(type) {
	 
	   case 1:  //*********************************** CLASS	   
	   
	     try {
	       switch(level) {
	         case 1: list = db.get_class_list("*");break;
		 case 2: list = list = new String[2];list[0]="PROPERTY";list[1]="ATTRIBUTE";break;
		 case 3: if( toString().equals("PROPERTY") ) {
		            list = db.get_class_property_list(getParent().toString(),"*");
			  } else {
		            list = db.get_class_attribute_list(getParent().toString(),"*");
			  }
			  break;
	         case 4: String [] att_list = { toString() };
			  DbAttribute lst[] = db.get_class_attribute_property(getParent().getParent().toString(),att_list);
			  if(lst.length>0) list = lst[0].get_property_list();
			  break;
		 
	       }
	     } catch (DevFailed e) { 
	       showTangoError(e);	 
       	     }
	     
	     break;
	   
	   case 2:  //*********************************** SERVER
	   
	     try {
	       DbServer dbs;
	       
	       switch(level) {
	       
	         case 1:list = db.get_server_list();break;
		 case 2: dbs = new DbServer ( toString() );
	                  list = dbs.get_class_list();
			  break; 
		 case 3: dbs = new DbServer ( getParent().toString() );
	                  list = dbs.get_device_name( toString() );
			  break;
			  
	       }
	     } catch (DevFailed e) { 
	       showTangoError(e);	 
       	     }
	     break;
	     
	   case 3:  //*********************************** DEVICE
	   
	     try {
	       switch(level) {
	       
	         case 1: list = db.get_device_domain("*");break;
	         case 2: list = db.get_device_family(toString()+"/*");break;
	         case 3: list = db.get_device_member(getParent().toString()+"/"+toString()+"/*");break;
		 case 4: list = new String[2];list[0]="PROPERTY";list[1]="ATTRIBUTE";break;
		 case 5: devname = getDevname( this , 1 );
		          if( toString().equals("PROPERTY") ) {
		            list = db.get_device_property_list(devname,"*");
			  } else {
			    DeviceProxy ds = new DeviceProxy(devname);
			    list=ds.get_attribute_list();
			  }
			  break;
	         case 6: devname = getDevname( this , 2 );
			  String [] att_list = { toString() };
			  DbAttribute lst[] = db.get_device_attribute_property(devname,att_list);
			  //System.out.println("Get att prop att list for:" + devname + "/" + toString() + "Nb:" + lst.length );
			  if(lst.length>0) list = lst[0].get_property_list();
			  break;
	       }
	     } catch (DevFailed e) { 
	       showTangoError(e);	 
       	     }
	     break;
	     
	   case 4:  //*********************************** PROPERTY
	   
	     try {
	       switch(level) {
	         case 1: list = db.get_object_list("*");break;
		 case 2: list = db.get_object_property_list(toString(),"*");break;
	       }
	     } catch (DevFailed e) {
               showTangoError(e);
             }
	     break;
	     
	   case 5:  //*********************************** TAGS
	   
	     //try {
	       switch(level) {
	         case 1: list = new String[1];list[0]="Not implemented";break;
	       }
	     //} catch (DevFailed e) { 
	     //  showTangoError(e);	 
       	     //}
	     break;
	     
	 }	 	 
	 } catch (Exception e) {
	   System.out.println(e);
	 }
	 
         numChildren=list.length;
	     
	 for(i=0;i<numChildren;i++)	     
           add(new TangoTreeNode(level+1,type,list[i],db));
	  
    }
    
  }
  
  // *****************************************************************************************************************  
  // Format the path
  static public String formatPath(TreePath path) {
  
    String result = "";
    
    if( path==null ) 
      return result;
    
    int n = path.getPathCount();
    TangoTreeNode node = (TangoTreeNode)path.getLastPathComponent();
    
    for(int i=1;i<n;i++) {
      
      result += path.getPathComponent(i);
            
      if( i<(n-1) || !node.isLeaf() )
      switch(node.type) {
       case 1:
	switch(i-1) {
         case 0:result += ":";break;
         case 1:result += " ";break;
         case 2:result += ":";break;
         case 3:result += "/";break;
        default: result += " ";
        }
       break;
       case 2:
	switch(i-1) {
         case 0:result += ":";break;
         case 1:result += " CLASS:";break;
        default: result += " ";
        }

       break;
       case 3:
        switch(i-1) {
         case 0:result += ":";break;
         case 1:result += "/";break;
         case 2:result += "/";break;
         case 3:result += " ";break;
         case 4:result += ":";break;
         case 5:result += "/";break;
        default: result += " ";
        }
       break;
       
       case 4:
       case 5:
	switch(i-1) {
         case 0:result += ":";break;
        default: result += " ";
        }

       break;
      }
      
    }
    
    return result;
  }
  
  // *****************************************************************************************************************  
  public static String stringArrayToString(String[] res) {

    String result = "";
    int i;
      
    if( res != null )
     for(i=0;i<res.length;i++) {
       result += res[i];
       if( (i+1)<res.length ) result += "\n";
     }
     
    return result;  
  }

  // *****************************************************************************************************************  
  // Get the property value (array[0]) according to the given path
  // and returns an empty string when no value is associated
  // with the path
  // The second element is the title of the view which will
  // display the result
  static public String[] getValue(TreePath path) {
  
    TangoTreeNode node   = (TangoTreeNode)path.getLastPathComponent();
    String        result = "";
    String        pname  = "...";
    String[]      ret    = new String[2];
    int           i;
    
    switch(node.type) {
      case 1: // CLASS
      
        // Getting class property value
        if((node.level==4) && node.getParent().toString().equals("PROPERTY"))
	   try {
	     String[] res = node.db.get_class_property(node.getParent().getParent().toString(),node.toString()).extractStringArray();

	     result = stringArrayToString( res );
	     pname = "Property value";
	     if(res.length>=2) pname = pname + " [" + res.length +" items]";
	     	     
	   } catch (Exception e) {             
	     System.out.println(e);e.printStackTrace();           
	   }
	
        // Getting class attribute property value
        if((node.level==5) && node.getParent().getParent().toString().equals("ATTRIBUTE"))
	   try {
	   
	     String [] att_list = { node.getParent().toString() };
	     DbAttribute lst[] = node.db.get_class_attribute_property(node.getParent().getParent().getParent().toString(),att_list);
	     
	     if( lst != null )
	     if( lst.length>0 ) {
	         result += lst[0].get_value(node.toString());
	     }
	     pname = "Property value";
	     
	   } catch (Exception e) {
	     System.out.println(e);e.printStackTrace();           
	   }

      break;
      
      case 2: // SERVER
      case 3: // DEVICE
      
        // Print device info
	if((node.level==4)){
	   
	 try {	        
              try {
	      
	        String devname;
		if( node.type==2) devname = node.toString();
		else              devname = getDevname(node,0);
	      
	        DbDevice dbdev = new DbDevice( devname ); 
		DbDevImportInfo info = dbdev.import_device();
		result = info.toString();
	        pname = "Device info";
              
	      } catch (DevFailed e) { 
	      
		 for (i =0;i < e.errors.length;i++)
                 {
                    result += "Desc -> "   + e.errors[i].desc + "\n";
                    result += "Reason -> " + e.errors[i].reason + "\n";
                    result += "Origin -> " + e.errors[i].origin + "\n";
                 }

	      }
	      
	   } catch (Exception e) {             
	     System.out.println(e);e.printStackTrace();           
	   }
        }
       
        // Print server info
        if((node.type==2) && (node.level==2)){
	   
	 try {	        
              try {
	      
	        DbServer dbserv = new DbServer( node.toString() ); 
		DbServInfo info = dbserv.get_info();
		result = info.toString();
	        pname = "Server info";
              
	      } catch (DevFailed e) { 
	      
		 for (i =0;i < e.errors.length;i++)
                 {
                    result += "Desc -> "   + e.errors[i].desc + "\n";
                    result += "Reason -> " + e.errors[i].reason + "\n";
                    result += "Origin -> " + e.errors[i].origin + "\n";
                 }

	      }
	      
	   } catch (Exception e) {             
	     System.out.println(e);e.printStackTrace();           
	   }
        }

       	if( node.type==2 ) break;
	// Device node only
     
        // Getting device property value
        if((node.level==6) && node.getParent().toString().equals("PROPERTY")) {
	   try {
	   
	     String devname = getDevname(node,2);     
             String[] res = node.db.get_device_property(devname,node.toString()).extractStringArray();	     
	     
	     result = stringArrayToString( res );
	     pname = "Property value";
	     if(res.length>=2) pname = pname + " [" + res.length +" items]";
	     
	   } catch (Exception e) {             
	     System.out.println(e);e.printStackTrace();           
	   }
	}
		
        // Getting device attribute property value
	if((node.level==7) && node.getParent().getParent().toString().equals("ATTRIBUTE")) {
	
	   try {
	   
	     String devname = getDevname(node,3);
	     String [] att_list = { node.getParent().toString() };
	     DbAttribute lst[] = node.db.get_device_attribute_property(devname,att_list);
	     
	     if( lst != null )
	     if( lst.length>0 ) {
	         result += lst[0].get_value(node.toString());
	     }
	     pname = "Property value";
	     
	   } catch (Exception e) {             
	     System.out.println(e);e.printStackTrace();           
	   }
	
	}
	
      break;
      
      case 4: // PROPERTY
      
        // Getting device property value
        if(node.level==3) {
	   try {
	   
             String[] res = node.db.get_property(node.getParent().toString(),node.toString()).extractStringArray();
	     
	     result = stringArrayToString( res );
	     pname = "Property value";
	     if(res.length>=2) pname = pname + " [" + res.length +" items]";
	     	     
	   } catch (Exception e) {             
	     System.out.println(e);e.printStackTrace();           
	   }
	}
      
      break;
      
      case 5: // TAGS
      break;
    }
    
    ret[0] = result;
    ret[1] = pname;    
    return ret;
    
  }

  // *****************************************************************************************************************  
  // Build a DbDatum object from the given value (string)  
  public static DbDatum[] makeDbDatum(String prop_name,String value) {

    String[] splitted = value.split("\n");
    DbDatum[] ret = new DbDatum[1];
    
    if( splitted.length <= 1 ) {
      //System.out.println("Making simple string");
      ret[0] = new DbDatum( prop_name , value );      
    } else {
      //System.out.println("Making string array of length " + splitted.length);
      ret[0] = new DbDatum( prop_name , splitted );
    }
    
    return ret;
  }
  
  // *****************************************************************************************************************  
  // Copy all property from a class to the clipboard
  public static void copyClassProperties(String classname,TangoTreeNode node) {
  
     String[] list;     
     String[] list2;
     int i,j;
     String value="%";
            
     try {
	     
       // Get class prop list	     
       list = node.db.get_class_property_list(classname,"*");
       for(i=0;i<list.length;i++) {
         String[] res = node.db.get_class_property(classname,list[i]).extractStringArray();         
         the_clipboard.add( list[i] , stringArrayToString( res ) );       
       }
		
       // Get device attribute prop list
       list=node.db.get_class_attribute_list(classname,"*");
       for(i=0;i<list.length;i++) {
         String att_list[] = { list[i] };
         DbAttribute lst[] = node.db.get_class_attribute_property(classname,att_list);
         if(lst.length>0) {
	   list2 = lst[0].get_property_list();
           for(j=0;j<list2.length;j++) {
             the_clipboard.add( list2[j] , list[i] , lst[0].get_value(j) );
	   }
	 }
       }	     
     
     } catch (DevFailed e) {
       showTangoError(e);
     }
         
  }
  
  // *****************************************************************************************************************  
  // Remove all property from a class
  public static boolean removeClassProperties(String classname,TangoTreeNode node) {
  
     String[] list;     
     String[] list2;
     int i,j;
     String value="%";
            
     try {
	     
       // Get class prop list	     
       list = node.db.get_class_property_list(classname,"*");
       for(i=0;i<list.length;i++) {
         //System.out.println("Removing: " + classname + " PROP " + list[i] );
         node.db.delete_class_property( classname , makeDbDatum(list[i],value) );
       }
		
       // Get device attribute prop list
       list=node.db.get_class_attribute_list(classname,"*");
       for(i=0;i<list.length;i++) {
         String att_list[] = { list[i] };
         DbAttribute lst[] = node.db.get_class_attribute_property(classname,att_list);
         if(lst.length>0) {
	   list2 = lst[0].get_property_list();
           for(j=0;j<list2.length;j++) {
             System.out.println("Removing: " + classname + " ATT " + list[i] + " PROP " + list2[j] );	     
	     // node.db.delete_class_attribute_property( classname , att );
	   }
	 }
       }	     
     
     } catch (DevFailed e) {
       showTangoError(e);
       return false;
     }
     
     return true;   
     
  }

  // *****************************************************************************************************************  
  // Copy all device properties into the clipboard
  public static void copyDeviceProperties(String devname,TangoTreeNode node) {
  
     String[] list;     
     String[] list2;
     int i,j;
     String value="%";
            
     try {
	     
       // Get device prop list	     
       list = node.db.get_device_property_list(devname,"*");
       for(i=0;i<list.length;i++) {
         String[] res = node.db.get_device_property(devname,list[i]).extractStringArray();         
         the_clipboard.add( list[i] , stringArrayToString( res ) );
       }
		
       // Get device attribute prop list
       DeviceProxy ds = new DeviceProxy(devname);
       list=ds.get_attribute_list();
       for(i=0;i<list.length;i++) {
         String att_list[] = { list[i] };
         DbAttribute lst[] = node.db.get_device_attribute_property(devname,att_list);
	 if(lst!=null)
         if(lst.length>0) {
	   list2 = lst[0].get_property_list();
           for(j=0;j<list2.length;j++) {
             the_clipboard.add( list2[j] , list[i] , lst[0].get_value(j) );
	   }
	 }
       }	     
     
     } catch (DevFailed e) {
       showTangoError(e);
     }
          
  }
  
  // *****************************************************************************************************************  
  // Remove all property from a device
  public static boolean removeDeviceProperties(String devname,TangoTreeNode node) {
  
     String[] list;     
     String[] list2;
     int i,j;
     String value="%";
            
     try {
	     
       // Get device prop list	     
       list = node.db.get_device_property_list(devname,"*");
       for(i=0;i<list.length;i++) {
         //System.out.println("Removing: " + devname + " PROP " + list[i] );
         node.db.delete_device_property( devname , makeDbDatum(list[i],value) );
       }
		
       // Get device attribute prop list
       DeviceProxy ds = new DeviceProxy(devname);
       list=ds.get_attribute_list();
       for(i=0;i<list.length;i++) {
         String att_list[] = { list[i] };
         DbAttribute lst[] = node.db.get_device_attribute_property(devname,att_list);
         if(lst.length>0) {
	   list2 = lst[0].get_property_list();
           for(j=0;j<list2.length;j++) {
             //System.out.println("Removing: " + devname + " ATT " + list[i] + " PROP " + list2[j] );	     
	     DbAttribute att = new DbAttribute( list[i] );
	     att.add( list2[j] , value );
	     node.db.delete_device_attribute_property( devname , att );
	   }
	 }
       }	     
     
     } catch (DevFailed e) {
       showTangoError(e);
       return false;
     }
     
     return true;   
     
  }
  
  // *****************************************************************************************************************  
  // Set the property value according to the given path
  // multinline value are interpreted as string array
  // return true if action has been succesfully done
  
  static public boolean setValue(TreePath path,String value) {
  
    TangoTreeNode node   = (TangoTreeNode)path.getLastPathComponent();
    int           i;
    
    switch(node.type) {
    
      case 1: // CLASS
      
        // Remove all property for a class
	if(node.level==2 && value.equals("%")) {	
          String classname = node.toString();
	  int ok = JOptionPane.showConfirmDialog(parent,"This will erase all class and attribute properties for "+classname+"\n Do you want to continue ?","Confirm delete class properties",JOptionPane.YES_NO_OPTION);
	  if( ok == JOptionPane.YES_OPTION ) return removeClassProperties( classname , node );
	  else			             return false;
	}
      
        // Set/Remove class property value
        if((node.level==4) && node.getParent().toString().equals("PROPERTY"))
	   try {
	     if(value.equals("%")) 
	       node.db.delete_class_property( node.getParent().getParent().toString() , makeDbDatum(node.toString(),value) );	   	     
	     else 
	       node.db.put_class_property( node.getParent().getParent().toString() , makeDbDatum(node.toString(),value) );	   
	   } catch (DevFailed e) {             
	     showTangoError(e);
	     return false;
	   }
	
        // Set/Remove class attribute property value
        if((node.level==5) && node.getParent().getParent().toString().equals("ATTRIBUTE"))
	   try {
	     DbAttribute att = new DbAttribute( node.getParent().toString() );
	     att.add( node.toString() , value );
	     	     
	     if(!value.equals("%"))	        
	       node.db.put_class_attribute_property( node.getParent().getParent().getParent().toString() , att );
	     //else
	     //  node.db.delete_class_attribute_property( node.getParent().getParent().getParent().toString() , att );
	       	     
	   } catch (DevFailed e) {             
	     showTangoError(e);
	     return false;
	   }

      break;
      
      case 2: // Server
	
	if( node.level==2 ) {
	  if(value.equals("%")) {
	    try {
	      node.db.delete_server(node.toString());
	    } catch (DevFailed e) {
	      showTangoError(e);
	      return false;
	    }
	  }
	}
	      
        if( node.level==3 ) {
	
	   try {
	    
	      if(value.equals("%")) {
	        // remove all device for the specified class
	        for(i=0;i<node.getChildCount();i++)
		  node.db.delete_device( node.getChildAt(i).toString() );		
	      } 
	      
	   } catch (DevFailed e) {
	      showTangoError(e);
	      return false;
	   }
	    
	}
      
        if( node.level==4 ) {
	   try {
	     if(value.equals("%")) 
	       node.db.delete_device( node.toString() );
	     else {
	       node.db.add_device( node.toString() , 
	                           node.getParent().toString() , 
				   node.getParent().getParent().toString() );
	     }
	   } catch (DevFailed e) {
	      showTangoError(e);
	      return false;
	   }
	}
	break;
            
      case 3: // DEVICE
      
        // Remove all property for a device
	if(node.level==4 && value.equals("%")) {	
          String devname = getDevname(node,0);  
	  int ok = JOptionPane.showConfirmDialog(parent,"This will erase all device and attribute properties for "+devname+"\n Do you want to continue ?","Confirm delte device properties",JOptionPane.YES_NO_OPTION);
	  if( ok == JOptionPane.YES_OPTION ) return removeDeviceProperties( devname , node );
  	  else			             return false;
	}
      
        // Set/Remove device property value
        if((node.level==6) && node.getParent().toString().equals("PROPERTY")) {
	   try {
	   
	     String devname = getDevname(node,2);
	     
	     if(value.equals("%")) 
	       node.db.delete_device_property( devname , makeDbDatum(node.toString(),value) );
	     else
	       node.db.put_device_property( devname , makeDbDatum(node.toString(),value) );
	     	     	     	     
	   } catch (DevFailed e) {
	      showTangoError(e);
	      return false;
	   }
	}
	
        // Set/Remove device attribute property value
	if((node.level==7) && node.getParent().getParent().toString().equals("ATTRIBUTE")) {
	
	   try {
	     
	     String devname = getDevname(node,3);
	     DbAttribute att = new DbAttribute( node.getParent().toString() );
	     att.add( node.toString() , value );
	     if(value.equals("%")) 
	       node.db.delete_device_attribute_property( devname , att );
	     else
	       node.db.put_device_attribute_property( devname , att );
	     	   	     
	   } catch (DevFailed e) {
	      showTangoError(e);
	      return false;
	   }
	
	}
	
      break;
      case 4: // PROPERTY
      
        // Set/Remove property value
        if(node.level==3) {
	   try {
	   
	     if(value.equals("%")) 
	       node.db.delete_property(node.getParent().toString(),makeDbDatum(node.toString(),value) );
	     else
               node.db.put_property(node.getParent().toString(),makeDbDatum(node.toString(),value));
	     	     	     
	   } catch (DevFailed e) {
	      showTangoError(e);
	      return false;
	   }
	}

      break;
      case 5: // TAGS
      break;
    }
    
    return true;
        
  }
  
  // *****************************************************************************************************************  
  // Retreive the device name in the given tree path
  public static String getDevname( TangoTreeNode node , int l ) {
  
     TreeNode father = node;
     
     for( int i=0 ; i<l ; i++ ) 
       father = father.getParent();
     
     return father.getParent().getParent().toString()+"/"+
            father.getParent().toString()+"/"+
	    father.toString();
	    
  }
  
  // *****************************************************************************************************************  
  // Show a tango error
  public static void showTangoError(DevFailed e) {
   
    String result = "";
    
      if( error_report && e!=null ) {
      
        for (int i =0;i < e.errors.length;i++)
        {
          result += "Desc -> "   + e.errors[i].desc + "\n";
          result += "Reason -> " + e.errors[i].reason + "\n";
          result += "Origin -> " + e.errors[i].origin + "\n";
        }
	
	if( result.length()>0 )
          JOptionPane.showMessageDialog(parent, result , "Tango error", JOptionPane.ERROR_MESSAGE);
	  
      }
      
  }

  // *****************************************************************************************************************  
  // Show a Jive error
  public static void showJiveError(String msg) {   
    if( error_report ) JOptionPane.showMessageDialog(parent, msg , "Jive error", JOptionPane.ERROR_MESSAGE);      
  }
  
  // *****************************************************************************************************************
  // Check if a property already exists
  static public FindInfo propertyExist(String prop_name , DefaultTreeModel model , TangoTreeNode node )
  {  
	// Check that property doesn't already exists
        int numChild = model.getChildCount(node);
        int i=0;boolean found=false;
	TangoTreeNode   elem=null;
		
        while(i<numChild && !found)
        {
	  elem = (TangoTreeNode)model.getChild(node,i);
	  found =  elem.toString().compareToIgnoreCase( prop_name ) == 0;
	  if(!found) i++;
        }
	
	return new FindInfo(i,found,elem);  
  }
  
  // *****************************************************************************************************************
  // Add a "New property" to the database and keep the tree synchronised  
  static public TreePath addProperty(String prop_name , DefaultTreeModel model , TreePath path , String value,int pos) {

        TangoTreeNode node   = (TangoTreeNode)path.getLastPathComponent();
	FindInfo      fi;
	
	// Add it
	fi = propertyExist(prop_name,model,node);
	
	if( !fi.found ) {
	   TangoTreeNode n = new TangoTreeNode(node.level+1,node.type,prop_name,node.db);
	   model.insertNodeInto(n,node,pos);
	   TreePath np = path.pathByAddingChild(n);
           setValue(np,value);
	   return np;
	} else { 
	  if( prop_name.substring(0,12).toLowerCase().equals("new property") )
	  {
	    // Find new property number
	    int n = 2;
	    FindInfo f = propertyExist("New property#" + n,model,node);
	    while( f.found ) {
	      n=n+1;
	      f = propertyExist("New property#" + n,model,node);
	    }
	    return addProperty("New property#" + n,model,path,value,pos);
	  }
        }
        return fi.nodeFound.getCompletePath();
	
  }

  // *****************************************************************************************************************
  // Remove a property from the database and keep the tree synchronised
  static public void removeProperty(DefaultTreeModel model , TreePath path) {

       TangoTreeNode  node = (TangoTreeNode)path.getLastPathComponent();
       TangoTreeNode pnode = (TangoTreeNode)path.getParentPath().getLastPathComponent();
              
       if( setValue(path,"%") ) model.removeNodeFromParent(node);
  }
  
  // *****************************************************************************************************************
  // Try to expand old path and return longest new valid path
  static public TreePath convertOldPath(DefaultTreeModel model,TangoTreeNode root,TreePath path) {

    TreePath np = new TreePath(root);
    
    if( path!=null ) {
    
      FindInfo f;
      int l = path.getPathCount();
      int i = 1;
      boolean end = false;
      TangoTreeNode r = root;
    
      while( !end && i<l ) {
        TangoTreeNode old_node = (TangoTreeNode)path.getPathComponent(i);
        f = propertyExist(old_node.toString(),model,r);
        end = !f.found;
        if(!end) {
	   r = (TangoTreeNode)model.getChild(r,f.index);
	   np = np.pathByAddingChild(r);
	   i++;
        }
      }
      
    }  
    
    return np;
  }

  // *****************************************************************************************************************  
  // Show the device/server dialog and exec action
  static public void showServerDlg(ServerDlg sdlg,TreePath path,DefaultTreeModel model,JTree tree) {

    TangoTreeNode node = (TangoTreeNode)path.getLastPathComponent();
    int i;
        
    if(node.level==3) {
      // go back to server
      path = path.getParentPath();
      node = (TangoTreeNode)path.getLastPathComponent();
    }
    
    if( sdlg.showDlg() )
    {
    
       String[] devices   = sdlg.getDeviceNames();
       String   server    = sdlg.getServerName();
       String   classname = sdlg.getClassName();

       // Add a node for the server (if not exists)
       if(node.level==1) path=addProperty( server , model , path , "" , 0);       
       
       // Add a node for the class (if not exists)       
       TreePath np = addProperty( classname , model , path , "" , 0);
       if(np!=null) { tree.setSelectionPath(np); } 	

       // Ad devices	  
       try {	    
	 for( i=0 ; i<devices.length ; i++ )
	   node.db.add_device( new DbDevInfo( devices[i] , classname , server ) );	      
       } catch (DevFailed e) { 
	 showTangoError(e);	 
       }
       
    }
      
  }
    
  // *****************************************************************************************************************  
  // Execute a tree popup menu action 
  static public void execAction(int a,TreePath path,JTree tree,DefaultTreeModel model,Rectangle bounds) {

    TangoTreeNode    node  = (TangoTreeNode)path.getLastPathComponent();
    TreePath         np;
    int 	     i;
    ServerDlg 	     sdlg;
    
    switch(a) {
    
      case 0:   // ------ CUT
      case 1:   // ------ COPY
	the_clipboard.clear();
      case 101: // ------ Multiple copy
      	        
	// Single copy
	if( node.isLeaf() ) {
	  String val = getValue(path)[0];
	  if( (node.type==3 && node.level==6) || (node.type==1 && node.level==4) )
	    // Adding property to the clipbaord
	    the_clipboard.add( node.toString() , val );
	  else
	    // Adding attribute property to the clipbaord
	    the_clipboard.add( node.toString() , node.getParent().toString() , val );          
	}
	
	// Full device propterty copy
	if((node.type==3 && node.level==4)) {
	  String devname = getDevname(node,0);
	  copyDeviceProperties(devname,node);
	}
	
	// Full class propterty copy
	if((node.type==1 && node.level==2)) {
	  copyClassProperties(node.toString(),node);
	}

        if(a==0) removeProperty(model,path);	// Cut
	break;

      case 2: // ------ PASTE
      
	try {
	
	  // Paste object property into a device
	  if( (node.type==3) && (node.level==5) && (node.toString().equals("PROPERTY")) ) {
	    String devname = getDevname(node,1);	  
	    for(i=0;i<the_clipboard.getObjectPropertyLength();i++)
	    {
	     node.db.put_device_property( devname , 
	       makeDbDatum(the_clipboard.getObjectPropertyName(i),the_clipboard.getObjectPropertyValue(i)) );
	    }
	  }
	
	  // Paste attribute property into a device
	  if( (node.type==3) && (node.level==5) && (node.toString().equals("ATTRIBUTE")) ) {
	    String devname = getDevname(node,1);
	    for(i=0;i<the_clipboard.getAttPropertyLength();i++)
	    {
	      DbAttribute att = new DbAttribute( the_clipboard.getAttName(i) );
	      att.add( the_clipboard.getAttPropertyName(i) , the_clipboard.getAttPropertyValue(i) );
   	      node.db.put_device_attribute_property( devname , att );
	    }
	  }
	  
	  // Paste single attribute property in an attribute
	  // Ignore attribute name
	  if( (node.type==3) && (node.level==6) && (node.getParent().toString().equals("ATTRIBUTE")) ) {
	    String devname = getDevname(node,2);
	    for(i=0;i<the_clipboard.getAttPropertyLength();i++)
	    {
	      DbAttribute att = new DbAttribute( node.toString() );
	      att.add( the_clipboard.getAttPropertyName(i) , the_clipboard.getAttPropertyValue(i) );
   	      node.db.put_device_attribute_property( devname , att );
	    }
	  }
	  
	  
	  // Paste object property into a class
	  if( (node.type==1) && (node.level==3) && (node.toString().equals("PROPERTY")) ) {
	    String classname = node.getParent().toString(); 
	    for(i=0;i<the_clipboard.getObjectPropertyLength();i++)
	    {
	     node.db.put_class_property( classname , 
	       makeDbDatum(the_clipboard.getObjectPropertyName(i),the_clipboard.getObjectPropertyValue(i)) );
	    }
	  }
	
	  // Paste attribute property into a device
	  if( (node.type==1) && (node.level==3) && (node.toString().equals("ATTRIBUTE")) ) {
	    String classname = node.getParent().toString(); 
	    for(i=0;i<the_clipboard.getAttPropertyLength();i++)
	    {
	      DbAttribute att = new DbAttribute( the_clipboard.getAttName(i) );
	      att.add( the_clipboard.getAttPropertyName(i) , the_clipboard.getAttPropertyValue(i) );
   	      node.db.put_class_attribute_property( classname , att );
	    }
	  }
	  
	  // Full device properties paste
	  if(node.type==3 && node.level==4) {
	    String devname = getDevname(node,0);
	    for(i=0;i<the_clipboard.getObjectPropertyLength();i++)
	    {
	     node.db.put_device_property( devname , 
	       makeDbDatum(the_clipboard.getObjectPropertyName(i),the_clipboard.getObjectPropertyValue(i)) );
	    }
	    for(i=0;i<the_clipboard.getAttPropertyLength();i++)
	    {
	      DbAttribute att = new DbAttribute( the_clipboard.getAttName(i) );
	      att.add( the_clipboard.getAttPropertyName(i) , the_clipboard.getAttPropertyValue(i) );
   	      node.db.put_device_attribute_property( devname , att );
	    }	    	    
	  }
	  
	  
	} catch (DevFailed e) {
	  showTangoError(e);	
	}
	
      
      break;
      
      case 3:   // ------ DELETE
      case 103: // ------ Multiple delete
        removeProperty(model,path);	
      break;
      
      case 4: // ------ ADD device
      {
	String devname = JOptionPane.showInputDialog(parent,"Enter device name","Add device",JOptionPane.QUESTION_MESSAGE);
	np = addProperty( devname , model , path , "" , 0);	
	if(np!=null) {	tree.setSelectionPath(np); }        
      }
      break;
      
      case 5: // ------ Create server
      
        sdlg = new ServerDlg(parent);
	sdlg.setValidFields(true,true);
	sdlg.setDefaults("","");
	showServerDlg(sdlg,path,model,tree);
	
      break;
      
      case 6: // ------ Create new property
	
	if( node.level==1 ) {
	  // special case for object property
	  String inputValue = JOptionPane.showInputDialog(parent,"Enter object name","New property",JOptionPane.QUESTION_MESSAGE);
	  np = addProperty( inputValue , model , path , "" , 0);
	  if( np!=null ) np = addProperty( "New property" , model , np , "" , 0);	  	  
	} else
	  np = addProperty( "New property" , model , path , "" , 0);	
	
	if(np!=null) {	tree.setSelectionPath(np); }
      break;
      
      case 8: // ------ Add a class to an existing server
	
	sdlg = new ServerDlg(parent);
	sdlg.setValidFields(false,true);
	sdlg.setDefaults(node.toString(),"");
	showServerDlg(sdlg,path,model,tree);
	
      break;      
      
      case 9: // ------ Rename property
        	
        if( ((node.level==5) && node.getParent().getParent().toString().equals("ATTRIBUTE")) 
         || ((node.level==7) && node.getParent().getParent().toString().equals("ATTRIBUTE")) )
	  dlg = new RenameDlg(parent,path.getLastPathComponent().toString(),att_prop_default,bounds);
	else
	  dlg = new RenameDlg(parent,path.getLastPathComponent().toString(),bounds);
	
	if( dlg.showDlg() ) {
	   String new_name = dlg.getNewName();
	   FindInfo f1 = propertyExist(new_name,model,(TangoTreeNode)node.getParent());
	   FindInfo f2 = propertyExist(node.toString(),model,(TangoTreeNode)node.getParent());	   
	   if( f1.found && (f1.index!=f2.index) ) {
	     // Duplicate name	     
	     showJiveError("Name already exists.");
	   } else {
	     //Rename property
	     String value = getValue(path)[0];
	     removeProperty(model,path);
	     addProperty( dlg.getNewName() , model , path.getParentPath() , value  , f2.index );
	   }
	};
	
	//Destroy the dialog
	dlg = null;
      break;
      
      case 10: // ------ Monitor a device
      {
	String[] args = new String[1];
	
	if(node.isLeaf())
	  args[0]  = node.toString();
	else
	  args[0]  = getDevname(node,0);
	  
	System.out.println("Running AtkPanel " + args[0]);
        atkdlg = new atkpanel.MainPanel(args,false);
      }
      break;
      
      case 11:	// ------- Test a device
      {
	
	String devname;
	if(node.isLeaf()) devname = node.toString();
	else     	  devname = getDevname(node,0);
		
	JiveMenu m = new JiveMenu(parent);	
	m.showDlg(devname);
	
      }
	
      break;

      case 12:	// ------- Show properties
      {
	
        // Show devices properties
	if((node.type==3 && node.level==4)) {
          String devname = getDevname(node,0);	  
	  // Define children
	  node.getChildCount();	  
	  // Go to device properties
	  node = (TangoTreeNode)node.getChildAt(0);	  
	  node.editProperties( "device:" + devname );	  	  
        }

        // Show device attribute properties
	if( (node.type==3) && (node.level==6) && (node.getParent().toString().equals("ATTRIBUTE")) ) {
	  String att_name = node.toString();
	  node.editProperties( "attribute:" + att_name );	  	
	}
	
        // Show Class properties
	if((node.type==1 && node.level==2)) {
          String classname = node.toString();	  
	  // Define children
	  node.getChildCount();	  
	  // Go to device properties
	  node = (TangoTreeNode)node.getChildAt(0);	  
	  node.editProperties( "class:" + classname );	  	  
        }

        // Show class attribute properties
	if( (node.type==1) && (node.level==4) && (node.getParent().toString().equals("ATTRIBUTE")) ) {
	  String att_name = node.toString();
	  node.editProperties( "attribute:" + att_name );	  	
	}
	
	// Show device properties (from server brwosing)
	if((node.type==2) && (node.level==4)) {
	  
	  String devname        = node.toString();
	  TangoTreeNode root    = (TangoTreeNode)path.getPathComponent(0);
	  TangoTreeNode devnode = (TangoTreeNode)root.getChildAt(2);          
	  
	  // Search the device node in the tree
	  TangoTreeNode n = findDeviceNode( devnode , devname );
	  
	  if( n!=null ) {
	    n.getChildCount();	  
	    n = (TangoTreeNode)n.getChildAt(0);	  
	    n.editProperties( "device:" + devname );
	  }
	  
	}
	      
      }
      break;
      
      case 13:	// ------- Test the admin server
      {
	
	String devname = "dserver/" + node.toString();
		
	JiveMenu m = new JiveMenu(parent);	
	m.showDlg(devname);
	
      }
      break;
      
      case 14:	// ------- Unexport devices of a server
      {
         int ok = JOptionPane.showConfirmDialog(parent,"This will unexport all devices of "+node.toString()+"\n Do you want to continue ?","Confirm unexport device",JOptionPane.YES_NO_OPTION);	 
	 if( ok == JOptionPane.YES_OPTION ) {
	   try {
	     System.out.println(" Unexport device of" + node.toString() );
	     node.db.unexport_server( node.toString() );
	   } catch (DevFailed e) {
	     showTangoError(e);
	   }	
	 }
	 
      }
      break;

      case 15:	// ------- Save server data
      {
      
	 int ok = JOptionPane.YES_OPTION;
         JFileChooser chooser = new JFileChooser();
         int returnVal = chooser.showSaveDialog(parent);
	 
         if(returnVal == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();	    
	    if( f!=null ) { 
	      if( f.exists() ) ok=JOptionPane.showConfirmDialog(parent,"Do you want to overwrite "+f.getName()+" ?","Confirm overwrite",JOptionPane.YES_NO_OPTION);
	      if( ok == JOptionPane.YES_OPTION ) {
	        node.saveServerData(f.getAbsolutePath());
	      }	    
	    }
         }	 
 
      }
      break;
      

    }
  }
  
  // ****************************************************************
  // Find a device node
  static public TangoTreeNode findDeviceNode( TangoTreeNode dev_node , String devname ) {
    int i;
    int count;
    boolean found;
    TangoTreeNode n=null;
    
    String[] devnames = devname.split("/");
    
    if( devnames.length!=3 ) {
      showJiveError("Invalid device name:" + devname);
      return null;
    }    
    
    // find device domain    
    count = dev_node.getChildCount();
    i=0;found=false;
    while(!found && i<count) {
      n = (TangoTreeNode) dev_node.getChildAt(i);
      found=(devnames[0].compareToIgnoreCase( n.toString() )==0);
      if(!found) i++;
    }
        
    if( !found ) {
      showJiveError("Device domain not found:" + devnames[0]);
      return null;
    }

    
    // find device family    
    dev_node = n;
    count = dev_node.getChildCount();
    i=0;found=false;
    while(!found && i<count) {
      n = (TangoTreeNode) dev_node.getChildAt(i);
      found=(devnames[1].compareToIgnoreCase( n.toString() )==0);
      if(!found) i++;
    }
        
    if( !found ) {
      showJiveError("Device family not found:" + devnames[1]);
      return null;
    }
    
    // find device member 
    dev_node = n;
    count = dev_node.getChildCount();
    i=0;found=false;
    while(!found && i<count) {
      n = (TangoTreeNode) dev_node.getChildAt(i);
      found=(devnames[2].compareToIgnoreCase( n.toString() ) == 0);
      if(!found) i++;
    }
        
    if( !found ) {
      showJiveError("Device member not found:" + devnames[2]);
      return null;
    }
    
    return n;    
  }
  
  // *****************************************************************************************************************  
  public static void printFormatedRes(String name,String[] value,FileWriter fw) 
  throws IOException
  {  
    int k,j,shift;
    String to_write;    
    
    shift = name.length();
    fw.write( name , 0 ,  shift );
        
    for(j=0;j<value.length;j++) {
      // Zero length not allowed
      if( value[j].length() ==0 )
        value[j] = " ";
	
      // Quote resource with space
      if( value[j].indexOf(' ') != -1 ||  value[j].indexOf('/') != -1 )
        value[j] = "\"" + value[j] + "\"";
      
      // Justify
        if(j>0) { for(k=0;k<shift;k++) fw.write(32); }
      
      // Array
      if(j<value.length-1)
        to_write = value[j] + ",\\ \n";
      else
        to_write = value[j] + "\n";
      
      fw.write( to_write , 0 , to_write.length() );
    }      
    
  }
  
  // *****************************************************************************************************************  
  // Save all data belonging to a server
  public void saveServerData(String filename) {

    int i,j,k,l;
    String to_write;
    
    try {
      FileWriter fw = new FileWriter(filename);		  
      
      // Write server definitions
      DbServer dbs = new DbServer ( toString() );
      String[] class_list = dbs.get_class_list();
      Date date = new Date(System.currentTimeMillis());
      to_write = "#\n#Server " + toString() + " backup , created " + date + "\n";
      fw.write( to_write , 0 ,  to_write.length() );
      to_write = "#\n\n";
      fw.write( to_write , 0 ,  to_write.length() );

      to_write = "#Server defition\n\n";
      fw.write( to_write , 0 ,  to_write.length() );
      
      for(i=0;i<class_list.length;i++) {
	 String[] dev_list = dbs.get_device_name( class_list[i] );
         to_write = toString() + "/DEVICE/" + class_list[i] + ": ";
	 printFormatedRes( to_write , dev_list , fw );
      }
      
      
      for(i=0;i<class_list.length;i++) {

	 to_write = "\n#############################################\n";
         fw.write( to_write , 0 , to_write.length() );        
	 to_write = "# CLASS " + class_list[i] + "\n\n";
         fw.write( to_write , 0 ,  to_write.length() );        
         String[] prop_list;
	 
         prop_list = db.get_class_property_list(class_list[i],"*");
	 for( j=0 ; j< prop_list.length ; j++ ) {	   
	   if( prop_list[j].indexOf(' ') != -1 ) prop_list[j] = "\"" + prop_list[j] + "\"";
           to_write = "CLASS/" + class_list[i] + "->" + prop_list[j] + ": ";
           String[] value = db.get_class_property(class_list[i],prop_list[j]).extractStringArray();         
	   printFormatedRes( to_write , value , fw );	 
	 }      
	 
	 to_write = "\n# CLASS " + class_list[i] + " attribute properties\n\n";
         fw.write( to_write , 0 ,  to_write.length() );

	 String[] att_list = db.get_class_attribute_list(class_list[i],"*");
         DbAttribute lst[] = db.get_class_attribute_property(class_list[i],att_list);
	 
	 for(k=0;k<lst.length;k++) {
           prop_list = lst[k].get_property_list();
	   for( j=0 ; j< prop_list.length ; j++ ) {	   
	     if( prop_list[j].indexOf(' ') != -1 ) prop_list[j] = "\"" + prop_list[j] + "\"";
             to_write = "CLASS/" + class_list[i] + "/" + att_list[k] + "->" + prop_list[j] + ": ";
	     String value[] = new String[1];
	     value[0] = lst[k].get_value(j);
	     printFormatedRes( to_write , value , fw );	 
	   }
	 }
	 
	 // Devices
	 String[] dev_list = dbs.get_device_name( class_list[i] );
	
	 for(l=0;l<dev_list.length;l++) {

	   to_write = "\n# DEVICE " + dev_list[l] + " properties\n\n";
           fw.write( to_write , 0 ,  to_write.length() );
	 	 
           prop_list = db.get_device_property_list(dev_list[l],"*");
	   for( j=0 ; j< prop_list.length ; j++ ) {	   
	     if( prop_list[j].indexOf(' ') != -1 ) prop_list[j] = "\"" + prop_list[j] + "\"";
             to_write = dev_list[l] + "->" + prop_list[j] + ": ";
             String[] value = db.get_device_property(dev_list[l],prop_list[j]).extractStringArray();         
	     printFormatedRes( to_write , value , fw );	 
	   }      
	 
	   to_write = "\n# DEVICE " + dev_list[l] + " attribute properties\n\n";
           fw.write( to_write , 0 ,  to_write.length() );

	   try {
	     
	     DeviceProxy ds = new DeviceProxy( dev_list[l] );
	     att_list = ds.get_attribute_list();
             lst = db.get_device_attribute_property(dev_list[l],att_list);
	 
	     for(k=0;k<lst.length;k++) {
               prop_list = lst[k].get_property_list();
	       for( j=0 ; j< prop_list.length ; j++ ) {	   
	         if( prop_list[j].indexOf(' ') != -1 ) prop_list[j] = "\"" + prop_list[j] + "\"";
                 to_write = dev_list[l] + "/" + att_list[k] + "->" + prop_list[j] + ": ";
	         String value[] = new String[1];
	         value[0] = lst[k].get_value(j);
	         printFormatedRes( to_write , value , fw );	 
	       }
	     }
	     
	   } catch( DevFailed e ) {
	   
	     showTangoError(e);
	     showJiveError("Attribute properties for " + dev_list[l] + " has not been saved !");
	   
	   }
	   
	 }
	   	 
      }
      
      
      fw.close();
    } catch (Exception e) {
      showJiveError("Cannot write to " + filename + " !\n" +  e.getMessage());
    }
    
  }
  
  // *****************************************************************************************************************  
  // Edit all the properties of the node  
  public void editProperties(String info) {
  
    int count = getChildCount();
    int i;
    
    if( count > 0 ) {
	    
      TreePath[] paths     = new TreePath[count];
      Object     rows[][]  = new Object[count][2];

      for(i=0;i<count;i++) {
        TangoTreeNode n = (TangoTreeNode)getChildAt(i);
        paths[i] = n.getCompletePath();
        String[] values = TangoTreeNode.getValue(paths[i]);
	rows[i][0] = TangoTreeNode.formatPath(paths[i]);
	rows[i][1] = values[0];
      }
    
      DetailsDlg dlg = new DetailsDlg(parent,rows,paths);
      dlg.show();
          
    } else {   
    
      showJiveError("No properties defined for " + info );
    
    }    
    
  }
  
  // *****************************************************************************************************************  
  // Get the possible action according
  // to selection path
  // Return a boolean array
  // arr[0] = cut
  // arr[1] = copy
  // arr[2] = paste
  // arr[3] = delete
  // arr[4] = add device
  // arr[5] = create server
  // arr[6] = create new property
  // arr[7] = change tango host
  // arr[8] = add class to server
  // arr[9] = Rename
  // arr[10]= Monitor a device
  // arr[11]= Test a device
  // arr[12]= Show properties
  // arr[13]= Test admin server
  // arr[14]= Unexport devices
  // arr[15]= Save server data
  
  static public boolean[] getAction(TreePath path) {

    TangoTreeNode  node   = (TangoTreeNode)path.getLastPathComponent();
    boolean[]      arr    = new boolean[nbAction];
    int            i;
    
    for(i=0;i<nbAction;i++) arr[i]=false;
    
    switch(node.type) {
    
      case 0: // ROOT NODE
      
        arr[7] = true;
	return arr;
      
      case 1: // CLASS
      
        if((node.level==2)) {
	  arr[0] = true;
	  arr[1] = true;
	  arr[3] = true;
	  arr[12] = true;
	}
      
	if( node.level==2 || node.level==3 ) {
	  arr[2] = !the_clipboard.empty();
	}
      
        if((node.level==3) && node.toString().equals("PROPERTY")) {
	  arr[6] = true;
	}	
	
        if((node.level==4) && node.getParent().toString().equals("PROPERTY")) {
	  arr[0] = true;
	  arr[1] = true;
	  arr[3] = true;
	  arr[9] = true;
	}
	  
        if((node.level==4) && node.getParent().toString().equals("ATTRIBUTE")) {
	  arr[12] = true;
	  arr[6] = true;
	}
	
        if((node.level==5) && node.getParent().getParent().toString().equals("ATTRIBUTE")) {
	  arr[0] = true;
	  arr[1] = true;
	  arr[3] = true;
	  arr[9] = true;
        }
	
      break;
      
      case 2: // SERVER
      
         if(node.level==1) {
	   arr[5]=true;
	 }
	 
         if(node.level==2) {
	   arr[3] = true;
	   arr[8] = true;
	   arr[13] = true;
	   arr[14] = true;
	   arr[15] = true;
	 }
	 
         if(node.level==3) {
	   arr[3] = true;
	   arr[4] = true;
	 }
	 
         if(node.level==4) {
	   arr[3] = true;
	   arr[10] = true;
	   arr[11] = true;
	   arr[12] = true;
	 }
	   
      break;
      case 3: // DEVICE	
      
	if( node.level==4 || node.level==5 ) {
	  arr[2] = !the_clipboard.empty();
	}
            
        if((node.level==4)) {
	  arr[0]  = true;
	  arr[1]  = true;
	  arr[3]  = true;
	  arr[10] = true;
	  arr[11] = true;
	  arr[12] = true;
	}

        if((node.level==5) && node.toString().equals("PROPERTY")) {
	  arr[6] = true;
	  arr[2] = !the_clipboard.empty();
	}
	
        if((node.level==6) && node.getParent().toString().equals("PROPERTY")) {
	  arr[0] = true;
	  arr[1] = true;
	  arr[3] = true;
	  arr[9] = true;
	}
	
        if((node.level==6) && node.getParent().toString().equals("ATTRIBUTE")) {
	  arr[6] = true;
	  arr[12] = true;
	  arr[2] = !the_clipboard.empty();
	}
	  
        if((node.level==7) && node.getParent().getParent().toString().equals("ATTRIBUTE")) {
	  arr[0] = true;
	  arr[1] = true;
	  arr[3] = true;
	  arr[9] = true;
        }
      
      break;
      case 4: // PROPERTY
      
        if((node.level==1)) {
	  arr[6] = true;
        }
	
        if((node.level==2)) {
	  arr[6] = true;
	  arr[2] = !the_clipboard.empty();
        }
      
        if((node.level==3)) {
	  arr[0] = true;
	  arr[1] = true;
	  arr[3] = true;
	  arr[9] = true;
        }
      
      break;
      case 5: // TAGS
      break;
    }
    
    return arr;
  
  }

  // *****************************************************************************************************************  
  // Initialise search
  static public void InitiateSearch(TangoTreeNode root,boolean ignorecase,boolean svalue,boolean sattribute,boolean sregexp,boolean sonlyleaf) {
     searchStack = new Stack();
     searchStack.push( root );
     searchIngoreCase=ignorecase;
     searchValues=svalue;
     searchAttributes=sattribute;
     searchUseRegexp=sregexp;
     searchOnlyLeaf=sonlyleaf;
  }

  // *****************************************************************************************************************  
  // Find a text in the tree ( text can be a regular expression )
  // return null when no path can be found
  static public TreePath findText(String text) {
      
    searchText = text;            
    Thread doSearch = new Thread() {
      public void run() {
        searchResult = findText_sub(searchText);
        searchDlg.hide();
      }
    };   
     
    searchStopflag=false;
    searchResult=null;    
    searchDlg = new ThreadDlg(parent,doSearch);    
    searchDlg.show();
    return searchResult;     
  }

  static public TreePath[] findMultipleText(String text) {
      
    searchText = text;            
    
    Thread doSearch = new Thread() {
      public void run() {
        
	boolean end   = false;
	Vector  pvect = new Vector();
	
	while( !end ) {
          searchResult = findText_sub(searchText);
          
	  if( searchResult!=null ) {
	    pvect.add( searchResult );
	  } else {
	    end = true;
	  }
	}
	
	if( pvect.size() > 0 ) {
	  searchResults = new TreePath[pvect.size()];
	  for(int i=0;i<pvect.size();i++) searchResults[i] = (TreePath) pvect.get(i);	  
	} else {
	  searchResults = null;
	}
	
	searchDlg.hide();
      }
    };   
     
    searchStopflag=false;
    searchResults=null;    
    searchDlg = new ThreadDlg(parent,doSearch);    
    searchDlg.show();
    return searchResults;     
  }

  static public TreePath findText_sub(String text) {

     int i;
     Pattern p=null;
     
     if( searchUseRegexp ) {
       try {
         p = Pattern.compile(text);
       } catch (PatternSyntaxException e) {
         error_report = true;
         showJiveError("Invalid regular expression\n" + e.getDescription());
	 return null;
       }
     }
      
     if( searchStack != null )     
     while( !searchStack.empty() && !searchStopflag ) {
     
       TangoTreeNode node =  (TangoTreeNode) searchStack.pop();        
         
       scan_progress++;
       String str1,str2;
              
       if( node.isLeaf() || !searchOnlyLeaf ) {
       
         if( searchIngoreCase ) {
	   str1 = node.toStringEx().toLowerCase();
	   str2 = text.toLowerCase();
         } else {	   
	   str1 = node.toStringEx();
	   str2 = text;
         }
	
         //System.out.println(str1);
       
         // Check property name
         if( searchUseRegexp ) {
	   if(p.matcher(str1).matches()) return node.getCompletePath();	 	 
         } else {
	   if(str1.indexOf(str2) != -1) return node.getCompletePath();	 
         }
       
         // Check property value	 
         if( searchValues ) {
	   if( searchIngoreCase ) {
	     str1 = getValue(new TreePath(node))[0].toLowerCase();
	   } else {
	     str1 = getValue(new TreePath(node))[0];
	   }
	   
           if( searchUseRegexp ) {
	     if(p.matcher(str1).matches()) return node.getCompletePath();	 	 
           } else {
	     if(str1.indexOf(str2) != -1) return node.getCompletePath();	 
           }
         }
	 	 
       }
       
       // Push children
       if( !node.toString().equals("ATTRIBUTE") || searchAttributes ) {
         int count = node.getChildCount();
         for(i=count-1;i>=0;i--) searchStack.push( node.getChildAt(i) );
       }
       
     } 
     // No item found
     return null;
  }
   
  // *****************************************************************************************************************  
  public String toString() {
    return(value);
  }
  
  // *****************************************************************************************************************  
  // Return the complete path of the node
  public TreePath getCompletePath() {
    int i;
        
    // Construct the path
    TangoTreeNode   node  = this;
    TangoTreeNode[] nodes = new TangoTreeNode[node.level+1];	 
    for(i=nodes.length-1;i>=0;i--) {
      nodes[i] = node;
      node=(TangoTreeNode)node.getParent();
    }
    return new TreePath(nodes);
    
  }
  
  // *****************************************************************************************************************  
  public String toStringEx() {

    String ret = "";
    int i;
    
    // Construct full string
    TangoTreeNode   node  = this;
    TangoTreeNode[] nodes = new TangoTreeNode[node.level+1];	 
    for(i=nodes.length-1;i>=0;i--) {
      nodes[i] = node;
      node=(TangoTreeNode)node.getParent();
    }
    for(i=0;i<nodes.length;i++) {
      ret += nodes[i].toString();
      ret += "/";
    }   
    return ret;
  
  }
  
}
