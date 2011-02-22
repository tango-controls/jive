package jive;

/****************************************************************************
 *
 * To use these classes, CLASSPATH include a path to this file location
 * (for instance, if this file is located in the same directory than
 *  the source file using these classes then CLASSPATH must contain "./"
 *
 * $Revision$
 *
 */

import java.io.*;
import java.lang.*;
import java.util.Vector;

import fr.esrf.Tango.*;
import fr.esrf.TangoDs.*;
import fr.esrf.TangoApi.*;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Date;

/****************************************************************************
 *
 */
public final class HtmlHelper
	implements TangoConst
{
 String 	id="$Id$";


 /**
  *
  */
 public static void error(PrintWriter out, String my_title, Vector msg)
 {
  head(out, my_title);

  out.println("<P>");
  out.println("<H1>");
  out.println("<FONT COLOR=\"#FF0000\">Error</FONT>");
  out.println("</H1>");

  for(int i=0; i < msg.size(); i++)
  {
   out.println("<P>");
   out.println((String)msg.elementAt(i));
  }

  out.println("<P>");
  bottom(out);
 }





 /**
  *
  */
 public static void head(PrintWriter out, String my_title)
 {
  out.println("<HTML>");
  out.println("<HEAD>");
  out.println("<TITLE>");
  out.println("TANGO Wizard");
  out.println("</TITLE>");
  out.println("</HEAD>");

  out.println("<BODY text=\"#000000\" bgcolor=\"#FFFFFF\" link=\"#0000EF\">");
  out.println("<CENTER>");
  out.println("<H1>");
  out.println("<FONT COLOR=\"#3366FF\">");
  out.println(my_title);
  out.println("</H1>");
  out.println("</FONT>");
 }




 /**
  *
  */
 public static void bottom(PrintWriter out)
 {
  bottom(out, "perez@esrf.fr");
 }

 /**
  *
  */
 public static void bottom(PrintWriter out, String email_author)
 {
  out.println("<hr noshade width=\"100%\">");
  out.println("<table width=92% border=0>");
  out.println("<tr> <td width=50% align=left>");
  out.println("<A HREF=\"/Jive.html\"><font size=+3>JIVE home</font></a></td>");

  out.println("<td width=50% align=right>");
  out.println("<i><a href=\"mailto:" + 
	email_author + "\">" + 
	email_author + "</a>");
  out.println("<br>dynamically generated</i> </td> </tr> </table>");

  out.println("</CENTER>");
  out.println("</BODY>");
  out.println("</HTML>");
 }




   



 /**
  * Extract from a string (typically coming from a string input field) the
  * specified type values and put them in Tango API DeviceData type. 
  *
  * The rules for decoding types from string (i.e. how the user must
  * enter the values in the string input field) are the same as the ones
  * of "xdevmenu" Unix application.
  *
  * Anyway they are explained here for each complicated type.
  *
  * The "argin_array" is the list of elements found after parsing the 
  * "argin_string". This is usefull for array types only as for other types
  * there is no parsing.
  *
  * Return:
  * 	 0 	On normal parsing and element type conversion.
  *	-1	If the type conversion of one element failed (this take
  *	   	place only for array types)
  *
  */
 public static int string_to_devicedata(
	String 		argin_string, 
	DeviceData	send, 
	int		command_in_type,
	Vector		argin_array)
 {
  

  // 
  // Test and treatements common to almost all commands
  //
  if(command_in_type != Tango_DEV_VOID)
  {

   // Ensure that the input string is not empty to avoid string
   // parsing funstions to fail. But allow the empty string input
   // for string type argin commands.
   if((command_in_type != Tango_DEV_STRING) && (argin_string.equals("")))
   {
    argin_array.removeAllElements();
    argin_array.addElement("Empty argument, you must specify a value");

    return -1;
   }

   // By default return as parsed argin string the argin string itself as
   // the parsed argin string is usefull only for array types.
   // Also remove useless white spaces.
   argin_array.addElement(argin_string.trim());
  }


  switch(command_in_type)
  {
   case Tango_DEV_VOID:
	break;
   case Tango_DEV_BOOLEAN:
	boolean b = argin_string.equalsIgnoreCase("true");
   	send.insert(b);
	break;
   case Tango_DEV_USHORT:
	{
	 int tmp_int = check_ushort(argin_string, argin_array);
	 if(tmp_int == -1)
	  return -1;

	 // Note: as there is no unsigned type in Java, we give always
	 // a signed variable but with a different call to the API
   	 send.insert_u((short)(tmp_int));
	}
	break;
   case Tango_DEV_SHORT:
   	send.insert(parse_short(argin_string));
	break;
   case Tango_DEV_ULONG:
	{
   	 long tmp_long = check_ulong(argin_string, argin_array);
	 if(tmp_long == -1)
	  return -1;

         // Note: the insert() method expects a 32bits integer
	 // Note: as there is no unsigned type in Java, we give always
	 // a signed variable but with a different call to the API
   	 send.insert_u((int)(tmp_long));
	}
	break;
   case Tango_DEV_LONG:
        // Note: Java int type is 32bits wide and is therefore mapped
 	//       to Tango_DevLong (Java long type is 64bits wide)
	send.insert(parse_int(argin_string));
	break;
   case Tango_DEV_FLOAT:
   	send.insert(Float.valueOf(argin_string).floatValue());
	break;
   case Tango_DEV_DOUBLE:
   	send.insert(Double.valueOf(argin_string).doubleValue());
	break;
   case Tango_DEV_STRING:
   	send.insert(argin_string);
	break;
   case Tango_DEVVAR_CHARARRAY:
	{
	 // A sequence of characters is expected as argin

	 argin_array.removeAllElements();
	 byte[] ret_dummy = argin_string.getBytes();
	 String bidon = new String();

	 for(int i=0; i < argin_string.length(); i++)
  	 {
	  if(ret_dummy[i] >= 32)
	   bidon = (new Character((char)ret_dummy[i]).toString());
	  else
	   bidon = " ";

 	  bidon += " = 0x" + Integer.toString(ret_dummy[i], 16);

	  argin_array.addElement(bidon);
	 }

   	 send.insert(ret_dummy);
	}
	break;
   case Tango_DEVVAR_USHORTARRAY:
   case Tango_DEVVAR_SHORTARRAY:
	{
	 // A sequence of short is expected as argin, each short
	 // must be separated by a comma and zero or more white spaces.

	 Vector	  dummy = parse_string_1sep(argin_string, ",");

	 argin_array.removeAllElements();
	 short[] ret_dummy = new short[dummy.size()];
	 short bidon;

	 for(int i=0; i < dummy.size(); i++)
  	 {
	  try
	  {
	   if(command_in_type == Tango_DEVVAR_USHORTARRAY)
	   {
	    int tmp_int = check_ushort((String)dummy.elementAt(i), argin_array);
	    if(tmp_int == -1)
	     return -1;

	    bidon = (short)(tmp_int);
	    argin_array.addElement(short_to_ushort(bidon));
	   }
	   else
	   {
	    bidon = parse_short((String)dummy.elementAt(i));
	    argin_array.addElement(Short.toString(bidon));
	   }

	   ret_dummy[i] = bidon;
	  }
	  catch(java.lang.NumberFormatException ignored) 
	  { 
	   argin_array.removeAllElements();
	   argin_array.addElement(
		"Following element not of type short: ");
	   argin_array.addElement((String)dummy.elementAt(i));

 	   return -1;
	  }
 	 }

	 // Note: as there is no unsigned type in Java, we give always
	 // a signed variable but with a different call to the API
	 if(command_in_type == Tango_DEVVAR_USHORTARRAY)
   	  send.insert_u(ret_dummy);
	 else
   	  send.insert(ret_dummy);
	}
	break;
   case Tango_DEVVAR_ULONGARRAY:
   case Tango_DEVVAR_LONGARRAY:
        // Note: Java int type is 32bits wide and is therefore mapped
 	//       to Tango_DevLong (Java long type is 64bits wide)
	{
	 // A sequence of ulong is expected as argin, each ulong
	 // must be separated by a comma and zero or more white spaces.

	 Vector	  dummy = parse_string_1sep(argin_string, ",");

	 argin_array.removeAllElements();
	 int[] ret_dummy = new int[dummy.size()];
	 int bidon;

	 for(int i=0; i < dummy.size(); i++)
  	 {
	  try
	  {
	   if(command_in_type == Tango_DEVVAR_ULONGARRAY)
	   {
	    long tmp_long = check_ulong((String)dummy.elementAt(i),argin_array);
	    if(tmp_long == -1)
	     return -1;

	    bidon = (int)(tmp_long);
	    argin_array.addElement(int_to_uint(bidon));
	   }
	   else
	   {
	    bidon = parse_int((String)dummy.elementAt(i));
	    argin_array.addElement(Integer.toString(bidon));
	   }

	   ret_dummy[i] = bidon;
	  }
	  catch(java.lang.NumberFormatException ignored) 
	  { 
	   argin_array.removeAllElements();
	   argin_array.addElement(
		"Following element not of type Java integer: ");
	   argin_array.addElement((String)dummy.elementAt(i));

 	   return -1;
	  }
 	 }

	 // Note: as there is no unsigned type in Java, we give always
	 // a signed variable but with a different call to the API
	 if(command_in_type == Tango_DEVVAR_ULONGARRAY)
   	  send.insert_u(ret_dummy);
	 else
   	  send.insert(ret_dummy);
	}
	break;
   case Tango_DEVVAR_FLOATARRAY:
	{
	 // A sequence of float is expected as argin, each float
	 // must be separated by a comma and zero or more white spaces.

	 Vector	  dummy = parse_string_1sep(argin_string, ",");

	 argin_array.removeAllElements();
	 float[] ret_dummy = new float[dummy.size()];
	 float bidon;

	 for(int i=0; i < dummy.size(); i++)
  	 {
	  try
	  {
	   bidon = Float.valueOf((String)dummy.elementAt(i)).floatValue();
	   ret_dummy[i] = bidon;
	   argin_array.addElement(Float.toString(bidon));
	  }
	  catch(java.lang.NumberFormatException ignored) 
	  { 
	   argin_array.removeAllElements();
	   argin_array.addElement(
		"Following element not of type float: ");
	   argin_array.addElement((String)dummy.elementAt(i));

 	   return -1;
	  }
 	 }

   	 send.insert(ret_dummy);
	}
	break;

   case Tango_DEVVAR_DOUBLEARRAY:
	{
	 // A sequence of double is expected as argin, each double
	 // must be separated by a comma and zero or more white spaces.

	 Vector	  dummy = parse_string_1sep(argin_string, ",");

	 argin_array.removeAllElements();
	 double[] ret_dummy = new double[dummy.size()];
	 double bidon;

	 for(int i=0; i < dummy.size(); i++)
  	 {
	  try
	  {
	   bidon = Double.valueOf((String)dummy.elementAt(i)).doubleValue();
	   ret_dummy[i] = bidon;
	   argin_array.addElement(Double.toString(bidon));
	  }
	  catch(java.lang.NumberFormatException ignored) 
	  { 
	   argin_array.removeAllElements();
	   argin_array.addElement(
		"Following element not of type double: ");
	   argin_array.addElement((String)dummy.elementAt(i));

 	   return -1;
	  }
 	 }

   	 send.insert(ret_dummy);
	}
	break;

   case Tango_DEVVAR_STRINGARRAY:
	{
	 // A sequence of strings is expected as argin, each string
	 // must be inside double quotes, strings must be separated by
	 // zero or more characters (any character).

	 Vector	  dummy = parse_string_quot1sep(argin_string, ",");

	 String[] ret_dummy = new String[dummy.size()];
	 argin_array.removeAllElements();
	 for(int i=0; i < dummy.size(); i++)
 	 {
	  ret_dummy[i] = (String)dummy.elementAt(i);
	  argin_array.addElement(dummy.elementAt(i));
	 }

   	 send.insert(ret_dummy);
	}
	break;

   case Tango_DEVVAR_LONGSTRINGARRAY:
	{ 
	 // Two arrays expected. Each array is surrounded by brackets []
	 // and separateed by zero or more characters (any character).
	 // The first array is a sequence of longs, each long
         // must be separated by a comma and zero or more white spaces.
	 // The second array is a sequence of strings, strings must be separated
         // by zero or more characters (any character).

	 // Parse first the two arrays
	 Vector two_arrays = parse_string(argin_string, "[", "]");
	 if(two_arrays.size() != 2)
  	 {
	  argin_array.removeAllElements();
	  argin_array.addElement(
		"Parsing error, syntax: [1,2] [\"qwe\",\"ty\"]");
	  return -1;
	 }


	 // Clear return to display
         argin_array.removeAllElements();




	 // Parse long array
	 Vector	  long_array = parse_string_1sep(
		(String)two_arrays.elementAt(0), 
		",");

	 int[] ret_long_array = new int[long_array.size()];
	 int bidon;
	 argin_array.addElement("long array length: " + ret_long_array.length);
	 argin_array.addElement("lvalue:");

	 for(int i=0; i < long_array.size(); i++)
  	 {
	  try
	  {
	   bidon = parse_int((String)long_array.elementAt(i));
	   argin_array.addElement("[" + i + "]\t "+Integer.toString(bidon));

	   ret_long_array[i] = bidon;
	  }
	  catch(java.lang.NumberFormatException ignored) 
	  { 
	   argin_array.removeAllElements();
	   argin_array.addElement(
		"Following element not of type Java integer: ");
	   argin_array.addElement((String)long_array.elementAt(i));

 	   return -1;
	  }
 	 }





	 // Parse string array
         Vector   string_array = parse_string_quot1sep(
		(String)two_arrays.elementAt(1), 
		",");

         String[] ret_string_array = new String[string_array.size()];
	 argin_array.addElement("string array length: " + 
		ret_string_array.length);
	 argin_array.addElement("svalue:");
         for(int i=0; i < string_array.size(); i++)
         {
          ret_string_array[i] = (String)string_array.elementAt(i);
          argin_array.addElement("[" + i + "]\t " + string_array.elementAt(i));
         }


	 // Prepare object to return
	 DevVarLongStringArray ret_dummy = new DevVarLongStringArray(
		ret_long_array,
		ret_string_array);
   	 send.insert(ret_dummy);
	}
	break;

   case Tango_DEVVAR_DOUBLESTRINGARRAY:
	{ 
	 // Two arrays expected. Each array is surrounded by brackets []
	 // and separateed by zero or more characters (any character).
	 // The first array is a sequence of doubles, each double
         // must be separated by a comma and zero or more white spaces.
	 // The second array is a sequence of strings, strings must be separated
         // by zero or more characters (any character).

	 // Parse first the two arrays
	 Vector two_arrays = parse_string(argin_string, "[", "]");
	 if(two_arrays.size() != 2)
  	 {
	  argin_array.removeAllElements();
	  argin_array.addElement(
		"Parsing error, syntax: [1,2] [\"qwe\",\"ty\"]");
	  return -1;
	 }


	 // Clear return to display
         argin_array.removeAllElements();




	 // Parse double array
	 Vector	  double_array = parse_string_1sep(
		(String)two_arrays.elementAt(0), 
		",");

	 double[] ret_double_array = new double[double_array.size()];
	 double bidon;

	 argin_array.addElement("double array length: " + 
		ret_double_array.length);
	 argin_array.addElement("dvalue:");

	 for(int i=0; i < double_array.size(); i++)
  	 {
	  try
	  {
	   bidon = Double.valueOf(
		(String)double_array.elementAt(i)).doubleValue();
	   argin_array.addElement("[" + i + "]\t "+Double.toString(bidon));

	   ret_double_array[i] = bidon;
	  }
	  catch(java.lang.NumberFormatException ignored) 
	  { 
	   argin_array.removeAllElements();
	   argin_array.addElement(
		"Following element not of type double: ");
	   argin_array.addElement((String)double_array.elementAt(i));

 	   return -1;
	  }
 	 }




	 // Parse string array
         Vector   string_array = parse_string_quot1sep(
		(String)two_arrays.elementAt(1), 
		",");

         String[] ret_string_array = new String[string_array.size()];
	 argin_array.addElement("string array length: " + 
		ret_string_array.length);
	 argin_array.addElement("svalue:");
         for(int i=0; i < string_array.size(); i++)
         {
          ret_string_array[i] = (String)string_array.elementAt(i);
          argin_array.addElement("[" + i + "]\t " + string_array.elementAt(i));
         }


	 // Prepare object to return
	 DevVarDoubleStringArray ret_dummy = new DevVarDoubleStringArray(
		ret_double_array,
		ret_string_array);
   	 send.insert(ret_dummy);
	}
	break;

   case Tango_DEV_STATE:
   	send.insert(DevState.from_int(Integer.parseInt(argin_string)));
	break;
   default:
	break;
  }

  // Normal end
  return 0;
 }


 /**
  *
  */
 private static String int_to_uint(int val)
 {
  long tmp_long = (long)(val);
  if(tmp_long < 0)
   tmp_long += 2 * ((long)(Integer.MAX_VALUE) + 1);
  String ret_string = Long.toString(tmp_long);
  return ret_string;
 }



 /**
  *
  */
 private static String short_to_ushort(short val)
 {
  int tmp_int = (int)(val);
  if(tmp_int < 0)
   tmp_int += 2 * (Short.MAX_VALUE + 1);
  String ret_string = Integer.toString(tmp_int);
  return ret_string;
 }




 /**
  *
  */
 private static short parse_short(String argin_string)
 {
  int conv_base;
  String str = argin_string.trim();

  if(str.startsWith("0x") || str.startsWith("0X")) 
  {
   conv_base = 16;
   str = str.substring(2);
  }
  else
   conv_base = 10;

  short tmp_short = Short.parseShort(str, conv_base);
  return tmp_short;
 }





 /**
  *
  */
 private static int parse_int(String argin_string)
 {
  int conv_base;
  String str = argin_string.trim();

  if(str.startsWith("0x") || str.startsWith("0X")) 
  {
   conv_base = 16;
   str = str.substring(2);
  }
  else
   conv_base = 10;

  int tmp_int = Integer.parseInt(str, conv_base);
  return tmp_int;
 }



 /**
  *
  */
 private static long parse_long(String argin_string)
 {
  int conv_base;
  String str = argin_string.trim();

  if(str.startsWith("0x") || str.startsWith("0X")) 
  {
   conv_base = 16;
   str = str.substring(2);
  }
  else
   conv_base = 10;

  long tmp_long = Long.parseLong(str, conv_base);
  return tmp_long;
 }



 /**
  *
  */
 private static int check_ushort(
	String argin_string, 
	Vector  argin_array)
 {
  int tmp_int = parse_int(argin_string);
  if((tmp_int > (Short.MAX_VALUE*2 + 1)) || (tmp_int < 0))
  {
   argin_array.removeAllElements();
   argin_array.addElement(
	"Value " + argin_string + 
	" out of range [0," + (Short.MAX_VALUE*2 + 1) + "]\n");

   return -1;
  }
  return tmp_int;
 }



 /**
  *
  */
 private static long check_ulong(
	String argin_string, 
	Vector  argin_array)
 {
  long tmp_long = parse_long(argin_string);
  if((tmp_long > ((long)(Integer.MAX_VALUE)*2 + 1)) || (tmp_long < 0))
  {
   argin_array.removeAllElements();
   argin_array.addElement(
	"Value " + argin_string + 
	" out of range [0," + ((long)(Integer.MAX_VALUE)*2 + 1) + "]\n");

   return -1;
  }
  return tmp_long;
 }






 /**
  * Extract from a string (typically coming from a string input field) the
  * specified type values and put them in Tango API DeviceData type. 
  *
  */
 public static int string_to_deviceattribute(
	String 		argin_string, 
	DeviceAttribute	send, 
	AttrDataFormat	attr_format,
	int		attr_type,
	Vector		argin_array)
 {
  int ret = 0;

  // Ensure that the input string is not empty to avoid string
  // parsing funstions to fail.
  if(argin_string.equals(""))
  {
   argin_array.removeAllElements();
   argin_array.addElement("Empty argument, you must specify a value");

   return -1;
  }

  // By default return an error message
  argin_array.removeAllElements();
  argin_array.addElement("Not supported type of argin");

  // Parsing
  switch(attr_type)
  {
   case Tango_DEV_SHORT:
	switch(attr_format.value())
  	{
	 case AttrDataFormat._SCALAR:
   		send.insert(parse_short(argin_string));
		break;
	 case AttrDataFormat._SPECTRUM:
	 case AttrDataFormat._IMAGE:
		argin_array.removeAllElements();
		argin_array.addElement(
			"SPECTRUM and IMAGE attributes are not writable");
		break;
	 default:
		ret=-1;
		break;
	}
	break;
   case Tango_DEV_LONG:
        // Note: Java int type is 32bits wide and is therefore mapped
 	//       to Tango_DevLong (Java long type is 64bits wide)
	switch(attr_format.value())
  	{
	 case AttrDataFormat._SCALAR:
		send.insert(parse_int(argin_string));
		break;
	 case AttrDataFormat._SPECTRUM:
	 case AttrDataFormat._IMAGE:
		argin_array.removeAllElements();
		argin_array.addElement(
			"SPECTRUM and IMAGE attributes are not writable");
		break;
	 default:
		ret=-1;
		break;
	}
	break;
   case Tango_DEV_DOUBLE:
	switch(attr_format.value())
  	{
	 case AttrDataFormat._SCALAR:
   		send.insert(Double.valueOf(argin_string).doubleValue());
		break;
	 case AttrDataFormat._SPECTRUM:
	 case AttrDataFormat._IMAGE:
		argin_array.removeAllElements();
		argin_array.addElement(
			"SPECTRUM and IMAGE attributes are not writable");
		break;
	 default:
		ret=-1;
		break;
	}
	break;
   case Tango_DEV_STRING:
	switch(attr_format.value())
  	{
	 case AttrDataFormat._SCALAR:
   		send.insert(argin_string);
		break;
	 case AttrDataFormat._SPECTRUM:
	 case AttrDataFormat._IMAGE:
		argin_array.removeAllElements();
		argin_array.addElement(
			"SPECTRUM and IMAGE attributes are not writable");
		break;
	 default:
		ret=-1;
		break;
	}
	break;
   default:
	ret=-1;
	break;
  }

  return ret;
 }






 /**
  *
  */
 public static String devicedata_to_string(
	DeviceData	received, 
	int		command_out_type)
 {
  return devicedata_to_string(
	received,
	command_out_type,
	-1);
 }
 public static String devicedata_to_string(
	DeviceData	received, 
	int		command_out_type,
	int		answer_limit)
 {
  String ret_string = new String("");

  switch(command_out_type)
  {
   case Tango_DEV_VOID:
	break;
   case Tango_DEV_BOOLEAN:
   	Boolean bidon = new Boolean(received.extractBoolean());
	ret_string = bidon.toString();
	break;
   case Tango_DEV_USHORT:
 	// Note: The extract() return a signed 16bits short therefore we need
	//       to do the cast from signed to unsigned by hand
   	ret_string = short_to_ushort(received.extractUShort());
	break;
   case Tango_DEV_SHORT:
        // Note: Java int type is 32bits wide and is therefore mapped
 	//       to Tango_DevLong (Java long type is 64bits wide)
   	ret_string = short_to_ushort(received.extractShort());
	break;
   case Tango_DEV_ULONG:
 	// Note: The extract() return a signed 32bits int therefore we need
	//       to do the cast from signed to unsigned by hand
   	ret_string = int_to_uint((int)received.extractULong());
	break;
   case Tango_DEV_LONG:
   	ret_string = Integer.toString(received.extractLong());
	break;
   case Tango_DEV_FLOAT:
   	ret_string = Float.toString(received.extractFloat());
	break;
   case Tango_DEV_DOUBLE:
   	ret_string = Double.toString(received.extractDouble());
	break;
   case Tango_DEV_STRING:
   	ret_string = received.extractString();
	break;
   case Tango_DEVVAR_CHARARRAY:
	{
	 byte[] dummy = received.extractByteArray();
	 int idx_limit = dummy.length;
         if((answer_limit >= 0) && (answer_limit < idx_limit))
         {
          idx_limit = answer_limit;
          ret_string += "limiting print to " + answer_limit + "elements\n";
         }
 	 for(int i=0; i<idx_limit; i++)
	 {
	  if(dummy[i] >= 32)
	   ret_string += (new Character((char)dummy[i]).toString());
	  else
	   ret_string += " ";
	  ret_string += " = 0x" + Integer.toString(dummy[i], 16) + "\n";
	 }
	}
	break;
   case Tango_DEVVAR_USHORTARRAY:
 	// Note: The extract() return a signed 16bits short therefore we need
	//       to do the cast from signed to unsigned by hand
	{
	 short[] dummy = received.extractUShortArray ();
         ret_string += "array length: " + dummy.length +"\n";
         int idx_limit = dummy.length;
         if((answer_limit >= 0) && (answer_limit < idx_limit))
 	 {
          idx_limit = answer_limit;
 	  ret_string += "limiting print to " + answer_limit + "elements\n";
	 }
         for(int i=0; i<idx_limit; i++)
	  ret_string += "[" + i + "]\t " + short_to_ushort(dummy[i]) + "\n";
	}
	break;
   case Tango_DEVVAR_SHORTARRAY:
	{
	 short[] dummy = received.extractShortArray ();
         ret_string += "array length: " + dummy.length +"\n";
         int idx_limit = dummy.length;
         if((answer_limit >= 0) && (answer_limit < idx_limit))
         {
          idx_limit = answer_limit;
          ret_string += "limiting print to " + answer_limit + "elements\n";
         }
         for(int i=0; i<idx_limit; i++)
	  ret_string += "[" + i + "]\t " + Short.toString(dummy[i]) + "\n";
	}
	break;
   case Tango_DEVVAR_ULONGARRAY:
	{
	 int[] dummy = received.extractULongArray();
         ret_string += "array length: " + dummy.length +"\n";
         int idx_limit = dummy.length;
         if((answer_limit >= 0) && (answer_limit < idx_limit))
         {
          idx_limit = answer_limit;
          ret_string += "limiting print to " + answer_limit + "elements\n";
         }
         for(int i=0; i<idx_limit; i++)
	  ret_string += "[" + i + "]\t " + int_to_uint(dummy[i]) + "\n";
	}
	break;
   case Tango_DEVVAR_LONGARRAY:
	{
	 int[] dummy = received.extractLongArray();
         ret_string += "array length: " + dummy.length +"\n";
         int idx_limit = dummy.length;
         if((answer_limit >= 0) && (answer_limit < idx_limit))
         {
          idx_limit = answer_limit;
          ret_string += "limiting print to " + answer_limit + "elements\n";
         }
         for(int i=0; i<idx_limit; i++)
	  ret_string += "[" + i + "]\t " + Integer.toString(dummy[i]) + "\n";
	}
	break;
   case Tango_DEVVAR_FLOATARRAY:
	{
	 float[] dummy = received.extractFloatArray();
         ret_string += "array length: " + dummy.length +"\n";
         int idx_limit = dummy.length;
         if((answer_limit >= 0) && (answer_limit < idx_limit))
         {
          idx_limit = answer_limit;
          ret_string += "limiting print to " + answer_limit + "elements\n";
         }
         for(int i=0; i<idx_limit; i++)
	  ret_string += "[" + i + "]\t " + Float.toString(dummy[i]) + "\n";
	}
	break;
   case Tango_DEVVAR_DOUBLEARRAY:
	{
	 double[] dummy = received.extractDoubleArray();
	 ret_string += "array length: " + dummy.length +"\n";
         int idx_limit = dummy.length;
         if((answer_limit >= 0) && (answer_limit < idx_limit))
         {
          idx_limit = answer_limit;
          ret_string += "limiting print to " + answer_limit + "elements\n";
         }
         for(int i=0; i<idx_limit; i++)
	  ret_string += "[" + i + "]\t" + Double.toString(dummy[i]) + "\n";
	}
	break;
   case Tango_DEVVAR_STRINGARRAY:
	{
	 String[] dummy = received.extractStringArray();
         ret_string += "array length: " + dummy.length +"\n";
         int idx_limit = dummy.length;
         if((answer_limit >= 0) && (answer_limit < idx_limit))
         {
          idx_limit = answer_limit;
          ret_string += "limiting print to " + answer_limit + "elements\n";
         }
         for(int i=0; i<idx_limit; i++)
	  ret_string += "[" + i + "]\t " + dummy[i] + "\n";
	}
	break;
   case Tango_DEVVAR_LONGSTRINGARRAY:
	{
	 DevVarLongStringArray dummy = received.extractLongStringArray();

         ret_string += "long array length: " + dummy.lvalue.length +"\n";
	 ret_string += "lvalue:\n";
         int idx_limit = dummy.lvalue.length;
         if((answer_limit >= 0) && (answer_limit < idx_limit))
         {
          idx_limit = answer_limit;
          ret_string += "limiting print to " + answer_limit + "elements\n";
         }

 	 for(int i=0; i<idx_limit; i++)
	  ret_string += "[" + i + "]\t "+Integer.toString(dummy.lvalue[i])+"\n";

         ret_string += "string array length: " + dummy.svalue.length +"\n";
	 ret_string += "svalue:\n";
	 idx_limit = dummy.svalue.length;
         if((answer_limit >= 0) && (answer_limit < idx_limit))
         {
          idx_limit = answer_limit;
          ret_string += "limiting print to " + answer_limit + "elements\n";
         }

 	 for(int i=0; i<idx_limit; i++)
	  ret_string += "[" + i + "]\t " + dummy.svalue[i] + "\n";
	}
	break;
   case Tango_DEVVAR_DOUBLESTRINGARRAY:
	{
	 DevVarDoubleStringArray dummy = received.extractDoubleStringArray();

	 ret_string += "double array length: " + dummy.dvalue.length +"\n";
	 ret_string += "dvalue:\n";
         int idx_limit = dummy.dvalue.length;
         if((answer_limit >= 0) && (answer_limit < idx_limit))
         {
          idx_limit = answer_limit;
          ret_string += "limiting print to " + answer_limit + "elements\n";
         }

         for(int i=0; i<idx_limit; i++)
	  ret_string += "[" + i + "]\t "+Double.toString(dummy.dvalue[i]) +"\n";

         ret_string += "string array length: " + dummy.svalue.length +"\n";
	 ret_string += "svalue:\n";
         idx_limit = dummy.svalue.length;
         if((answer_limit >= 0) && (answer_limit < idx_limit))
         {
          idx_limit = answer_limit;
          ret_string += "limiting print to " + answer_limit + "elements\n";
         }

         for(int i=0; i<idx_limit; i++)
	  ret_string += "[" + i + "]\t " + dummy.svalue[i] + "\n";
	}
	break;
   case Tango_DEV_STATE:
   	ret_string = Tango_DevStateName[
		received.extractDevState().value()];
	break;
   default:
	break;
  }
  
  return ret_string;
 }





 /**
  *
  */
 public static String deviceattribute_to_string(
	DeviceAttribute	received, 
	AttrWriteType   writable,
	AttrDataFormat	attr_format,
	int		attr_type)
 {
  String ret_string = new String("");

  //
  // Add some common information to all attributes
  //

  // Add the date of the measure in two formats
  TimeVal t = received.getTimeVal();
  ret_string += "measure date: " + t.tv_sec + "sec   " + t.tv_usec + "usec\n";

  Date  date = new Date((long)(t.tv_sec*1000.0 + t.tv_usec/1000.0));
  SimpleDateFormat dateformat =
        new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
  dateformat.setTimeZone(TimeZone.getDefault());
  ret_string += "measure date: " + dateformat.format(date) + "\n";

  // Add the quality information
  AttrQuality q = received.getQuality();
  ret_string += "quality: ";
  switch(q.value())
  {
   case AttrQuality._ATTR_VALID:
 	ret_string += "VALID";
	break;
   case AttrQuality._ATTR_INVALID:
 	ret_string += "INVALID";
	break;
   case AttrQuality._ATTR_ALARM:
 	ret_string += "ALARM";
	break;
   default:
 	ret_string += "UNKNOWN";
	break;
  }
  ret_string += "\n";

  // Add dimension of the attribute but only if having a meaning
  switch(attr_format.value())
  {
   case AttrDataFormat._SCALAR:
	break;
   case AttrDataFormat._SPECTRUM:
  	ret_string += "dim x: " + received.getDimX() + "\n";
	break;
   case AttrDataFormat._IMAGE:
  	ret_string += "dim x: " + received.getDimX() + "\n";
  	ret_string += "dim y: " + received.getDimY() + "\n";
	break;
   default:
	break;
  }

  // Add values
  switch(attr_type)
  {
   case Tango_DEV_SHORT:
	{
	 // Get the attribute values
	 short[] dummy = received.extractShortArray();

	 // Treat specific print out for read_write attributes 
  	 // The spectrum and image attributes are only pure read attributes
	 if(attr_format.value() == AttrDataFormat._SCALAR)
	 {
   	  // Add read point value (always available)
	  if(dummy.length > 0)
	  {
 	   if(writable == AttrWriteType.WRITE)
	    ret_string += "last written value: "+short_to_ushort(dummy[0])+"\n";
	   else
	    ret_string += "read point: " + short_to_ushort(dummy[0]) +"\n";
	  }
   	  // Add set point value (available only for read_write attributes)
	  if(dummy.length > 1)
	   ret_string += "set  point: " + short_to_ushort(dummy[1]) +"\n";
	 }
	 else
	 {
	  ret_string += "array length: " + dummy.length +"\n";
 	  for(int i=0; i<dummy.length; i++)
	   ret_string += "[" + i + "]\t" + short_to_ushort(dummy[i]) + "\n";
	 }
	}
	break;
   case Tango_DEV_LONG:
	{
	 int[] dummy = received.extractLongArray();

	 // Treat specific print out for read_write attributes 
  	 // The spectrum and image attributes are only pure read attributes
	 if(attr_format.value() == AttrDataFormat._SCALAR)
	 {
   	  // Add read point value (always available)
	  if(dummy.length > 0)
	   ret_string += "read point: " + Integer.toString(dummy[0]) +"\n";
   	  // Add set point value (available only for read_write attributes)
	  if(dummy.length > 1)
	   ret_string += "set  point: " + Integer.toString(dummy[1]) +"\n";
	 }
	 else
	 {
	  ret_string += "array length: " + dummy.length +"\n";
 	  for(int i=0; i<dummy.length; i++)
	   ret_string += "[" + i + "]\t" + Integer.toString(dummy[i]) + "\n";
	 }
	}
	break;
   case Tango_DEV_DOUBLE:
	{
	 double[] dummy = received.extractDoubleArray();

	 // Treat specific print out for read_write attributes 
  	 // The spectrum and image attributes are only pure read attributes
	 if(attr_format.value() == AttrDataFormat._SCALAR)
	 {
   	  // Add read point value (always available)
	  if(dummy.length > 0)
	   ret_string += "read point: " + Double.toString(dummy[0]) +"\n";
   	  // Add set point value (available only for read_write attributes)
	  if(dummy.length > 1)
	   ret_string += "set  point: " + Double.toString(dummy[1]) +"\n";
	 }
	 else
	 {
	  ret_string += "array length: " + dummy.length +"\n";
 	  for(int i=0; i<dummy.length; i++)
	   ret_string += "[" + i + "]\t" + Double.toString(dummy[i]) + "\n";
	 }
	}
	break;
   case Tango_DEV_STRING:
	if(attr_format.value() == AttrDataFormat._SCALAR)
	{
   	  ret_string = received.extractString();
	} else {
	  String[] dummy = received.extractStringArray();
	  ret_string += "array length: " + dummy.length +"\n";
 	  for(int i=0; i<dummy.length; i++)
	   ret_string += "[" + i + "]\t" + dummy[i] + "\n";
	}
	break;
   default:
        ret_string = new String("Unsupported attribute type");
	break;
  }
  
  return ret_string;
 }













 /**
  *
  */
 private static Vector parse_string(
	String argin_string,
	String separator_string_beg,
	String separator_string_end)
 {
  Vector  dummy = new Vector();
  int	  ind_beg = 0;
  int	  ind_end = 0;
  argin_string = argin_string.trim();
  while((ind_beg != -1) && (ind_end != -1))
  {
   ind_beg = argin_string.indexOf(separator_string_beg, ind_beg);
   ind_end = argin_string.indexOf(separator_string_end, ind_beg + 1);
   if((ind_beg != -1) && (ind_end != -1))
   {
    dummy.addElement(argin_string.substring(ind_beg + 1, ind_end));
    ind_beg = ind_end + 1; 
   }
  }
 
  return dummy;
 }




 /**
  *
  */
 private static Vector parse_string_1sep(
	String argin_string,
	String separator_string)
 {
  Vector  dummy = new Vector();
  String  element_string = null;
  int	  ind_beg = 0;
  int	  ind_end = 0;

  argin_string = argin_string.trim();
  while((ind_beg != -1) && (ind_end != -1))
  {
   ind_end = argin_string.indexOf(separator_string, ind_beg);
   if(ind_end != -1)
   {
    element_string = argin_string.substring(ind_beg, ind_end).trim();
    if(element_string.length() != 0)
     dummy.addElement(element_string);
    ind_beg = ind_end + 1; 
   }
   else
   {
    element_string = argin_string.substring(ind_beg).trim();
    if(element_string.length() != 0)
     dummy.addElement(element_string);
   }
  }
 
  return dummy;
 }
 



 /**
  * Parse the argin string with following expected syntax:
  *    "xx xx",yyyy,"zz zz",ww ww
  * Return a Vector containing:
  *    xx xx
  *    yyyy
  *    zz zz
  *    wwww
  * Note: the quotes are not mandatory
  * Note: the quotes are removed if any
  * Note: a string within quotes is kept as is
  * Note: any white space is removed is not in quotes surrounded string
  */
 private static Vector parse_string_quot1sep(
	String argin_string,
	String separator_string)
 throws
	NumberFormatException

 {
  argin_string = argin_string.trim();

  Vector  dummy       = new Vector();
  int     idx         = 0;
  boolean in_quots    = false;
  int     line_length = argin_string.length();
  char[]  tmp         = argin_string.toCharArray();
  char[]  dest        = new char[line_length];


  // the extra quotes if present
  for(int i=0;i<line_length;i++)
  {
   if(tmp[i] == '"')
    in_quots = !in_quots;
   else if(in_quots)
    dest[idx++] = tmp[i];
   else if(tmp[i] == ',')
   {
    dummy.addElement(new String(dest, 0, idx));
    idx = 0;
   }
   else if((tmp[i] != ' ') && (tmp[i] != '\t'))
    dest[idx++] = tmp[i];
  }

  // don't forget to take the last element
  if(idx != 0)
   dummy.addElement(new String(dest, 0, idx));

  // minimum syntax chek
  if(in_quots)
   throw new NumberFormatException("too many quotes");


  return dummy;
 }
 



 /**
  *
  */
 public static String argin_example(int command_in_type)
 {
  String ret_string = new String("Ex: ");

  
  switch(command_in_type)
  {
   case Tango_DEV_VOID:
	ret_string = new String("");
	break;
   case Tango_DEV_BOOLEAN:
 	ret_string += "true";
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
	ret_string = "no quotes needed";
	break;
   case Tango_DEVVAR_CHARARRAY:
	ret_string += "abc (no quotes needed)";
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
	ret_string += "\"Dance\",\"TANGO\"";
	break;
   case Tango_DEVVAR_LONGSTRINGARRAY:
	ret_string += "[1,2][\"A\",\"B\"]";
	break;
   case Tango_DEVVAR_DOUBLESTRINGARRAY:
	ret_string = "not supported";
	break;
   case Tango_DEV_STATE:
	ret_string += "0 (16bits value)";
	break;
   default:
   	ret_string = new String("");
	break;
  }


  return ret_string;
 }


}

