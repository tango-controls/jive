/*
 *  Tango database viewer/editor
 *  Jean-Luc PONS     2002
 *
 *  File: TangoFileReader.java
 *  Reads a tango resource file and load it in the database
 */

package jive;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.Database;
import fr.esrf.TangoApi.DbAttribute;
import fr.esrf.TangoApi.DbDatum;

import java.io.FileReader;
import java.io.IOException;

public class TangoFileReader {

  /* Lexical coce */

  private final int NUMBER = 1;
  private final int STRING = 2;
  private final int COMA = 3;
  private final int COLON = 4;
  private final int SLASH = 5;
  private final int ASLASH = 6;
  private final int ARROW = 7;

  private final String[] lexical_word = {
    "NULL",
    "NUMBER",
    "STRING",
    "COMA",
    "COLON",
    "SLASH",
    "BackSLASH",
    "->"
  };

  private int CrtLine;
  private int StartLine;
  private char CurrentChar;
  private char NextChar;

  private Database db;
  private boolean DELETE_ENTRY;
  private String word;

  // ****************************************************
  //  Constructor
  // ****************************************************
  public TangoFileReader(Database d) {
    db = d;
  }

  // ****************************************************
  // read the next character in the file
  // ****************************************************
  private void read_char(FileReader f) throws IOException {

    CurrentChar = NextChar;
    if (f.ready())
      NextChar = (char) f.read();
    else
      NextChar = 0;
    if (CurrentChar == '\n') CrtLine++;
  }

  // ****************************************************
  // Go to the next line                                      */
  // ****************************************************
  private void jump_line(FileReader f) throws IOException {

    while (CurrentChar != '\n' && CurrentChar != 0) read_char(f);
    read_char(f);
  }

  // ****************************************************
  // Go to the next significant character
  // ****************************************************
  private void jump_space(FileReader f) throws IOException {

    while (CurrentChar <= 32 && CurrentChar > 0) read_char(f);
  }

  // ****************************************************
  // Read the next word in the file                           */
  // ****************************************************
  private String read_word(FileReader f) throws IOException {

    StringBuffer ret_word = new StringBuffer();

    /* Jump space and comments */
    jump_space(f);
    while (CurrentChar == '#') {
      jump_line(f);
      jump_space(f);
    }

    /* Jump C like comments */
    if (CurrentChar == '/') {
      read_char(f);
      if (CurrentChar == '*') {
        boolean end = false;
        read_char(f);
        while (end) {
          while (CurrentChar != '*')
            read_char(f);
          read_char(f);
          end = (CurrentChar == '/');
        }
        read_char(f);
        jump_space(f);
      } else {
        return "/";
      }
    }

    StartLine = CrtLine;

    /* Treat special character */
    if (CurrentChar == ':' || CurrentChar == '/' || CurrentChar == ',' ||
            CurrentChar == '\\' || (CurrentChar == '-' && NextChar == '>')) {
      if (CurrentChar != '-') {
        ret_word.append(CurrentChar);
      } else {
        ret_word.append(CurrentChar);
        read_char(f);
        ret_word.append(CurrentChar);
      }
      read_char(f);
      return ret_word.toString();
    }

    /* Treat string */
    if (CurrentChar == '"') {
      read_char(f);
      while (CurrentChar != '"' && CurrentChar != 0 && CurrentChar != '\n') {
        ret_word.append(CurrentChar);
        read_char(f);
      }
      if (CurrentChar == 0 || CurrentChar == '\n') {
        IOException e = new IOException("String too long at line " + StartLine);
        throw e;
      }
      read_char(f);
      return ret_word.toString();
    }

    /* Treat other word */
    while (CurrentChar > 32 && CurrentChar != ':' && CurrentChar != '/'
            && CurrentChar != '\\' && CurrentChar != ',') {
      if (CurrentChar == '-' && NextChar == '>')
        break;
      ret_word.append(CurrentChar);
      read_char(f);
    }

    if (ret_word.length() == 0) {
      return null;
    }

    return ret_word.toString();
  }

  // ****************************************************
  // Read the next word in the file
  // And allow / inside
  // ****************************************************
  private String read_full_word(FileReader f) throws IOException {

    String ret_word = "";

    StartLine = CrtLine;
    jump_space(f);

    /* Treat special character */
    if (CurrentChar == ',' || CurrentChar == '\\') {
      ret_word += CurrentChar;
      read_char(f);
      return ret_word;
    }

    /* Treat string */
    if (CurrentChar == '"') {
      read_char(f);
      while (CurrentChar != '"' && CurrentChar != 0 && CurrentChar != '\n') {
        ret_word += CurrentChar;
        read_char(f);
      }
      if (CurrentChar == 0 || CurrentChar == '\n') {
        IOException e = new IOException("String too long at line " + StartLine);
        throw e;
      }
      read_char(f);
      return ret_word;
    }

    /* Treat other word */
    while (CurrentChar > 32 && CurrentChar != '\\' && CurrentChar != ',') {
      ret_word += CurrentChar;
      read_char(f);
    }

    if (ret_word.length() == 0) {
      return null;
    }

    return ret_word;
  }

  // ****************************************************
  // return the lexical classe of the next word               */
  // ****************************************************
  private int class_lex(String word) {

    /* exepction */

    if (word == null) return 0;
    if (word.length() == 0) return STRING;

    /* Special character */

    if (word.equals("/")) return SLASH;
    if (word.equals("\\")) return ASLASH;
    if (word.equals(",")) return COMA;
    if (word.equals(":")) return COLON;
    if (word.equals("->")) return ARROW;

    return STRING;
  }

  private DbDatum[] makeDbDatum(String prop_name, String[] value) {

    DbDatum[] ret = new DbDatum[1];
    DELETE_ENTRY = false;

    if (value.length == 0) {
      throw new IllegalStateException("Unexpected empty value");
    } else if (value.length == 1) {
      // Make simple string
      ret[0] = new DbDatum(prop_name, value[0]);
      DELETE_ENTRY = value[0].compareTo("%") == 0;
    } else {
      // Make string array
      ret[0] = new DbDatum(prop_name, value);
    }

    return ret;
  }

  private void checkAttDatum(String[] value) {

    DELETE_ENTRY = false;

    if (value.length == 0) {
      throw new IllegalStateException("Unexpected empty value");
    } else if (value.length == 1) {
      DELETE_ENTRY = value[0].compareTo("%") == 0;
    }

  }

  private String prtValue(String[] value) {
    int j;

    String ret = ": ";
    for (j = 0; j < value.length; j++) {
      ret += value[j];
      if (j < value.length - 1) ret += ',';
    }

    return ret;
  }

  // ****************************************************
  // Put a attribute property in the database     */
  // ****************************************************
  private void put_tango_dev_attr_prop(String devname, String att_name, String prop_name, String[] arr) throws DevFailed {

    //System.out.println( "put_tango_dev_attr_prop " + devname + "/" + att_name + "->" + prop_name + prtValue(arr) );

    checkAttDatum(arr);
    DbAttribute att = new DbAttribute(att_name);
    if(arr.length==0) {
      throw new IllegalStateException("Unexpected empty value");
    } else if( arr.length==1 ) {
      att.add(prop_name, arr[0]);
    } else {
      att.add(prop_name, arr);
    }

    if (DELETE_ENTRY)
      db.delete_device_attribute_property(devname, att);
    else
      db.put_device_attribute_property(devname, att);

  }

  // ****************************************************
  // Put a attribute property in the database
  // ****************************************************
  private void put_tango_class_attr_prop(String classname, String att_name, String prop_name, String[] arr) throws DevFailed {

    //System.out.println( "put_tango_class_attr_prop " + classname + "/" + att_name + "->" + prop_name + prtValue(arr) );

    checkAttDatum(arr);
    DbAttribute att = new DbAttribute(att_name);
    if(arr.length==0) {
      throw new IllegalStateException("Unexpected empty value");
    } else if( arr.length==1 ) {
      att.add(prop_name, arr[0]);
    } else {
      att.add(prop_name, arr);
    }

    if (DELETE_ENTRY) {
      //db.delete_class_attribute_property( classname , att );
    } else
      db.put_class_attribute_property(classname, att);

  }

  // ****************************************************
  // Put a resource to tango database
  // ****************************************************
  private void put_tango_res(String devname, String resname, String[] arr) throws DevFailed {

    //System.out.println( "put_tango_res " + devname + "->" + resname + prtValue(arr) );

    DbDatum[] d = makeDbDatum(resname, arr);
    if (DELETE_ENTRY)
      db.delete_device_property(devname, d);
    else
      db.put_device_property(devname, d);
  }

  // ****************************************************
  // Put a resource to tango database             */
  // ****************************************************
  private void put_free_tango_res(String freename, String resname, String[] arr) throws DevFailed {

    //System.out.println( "put_free_tango_res " + freename + "->" + resname + prtValue(arr) );

    DbDatum[] d = makeDbDatum(resname, arr);
    if (DELETE_ENTRY)
      db.delete_property(freename, d);
    else
      db.put_property(freename, d);
  }

  // ****************************************************
  // Put a resource to tango database
  // ****************************************************
  private void put_tango_res_class(String classname, String resname, String[] arr) throws DevFailed {

    //System.out.println( "put_tango_res_class " + classname + "->" + resname + prtValue(arr) );

    DbDatum[] d = makeDbDatum(resname, arr);
    if (DELETE_ENTRY)
      db.delete_class_property(classname, d);
    else
      db.put_class_property(classname, d);
  }

  // ****************************************************
  // Return True if the device exists in the list
  // ****************************************************
  private boolean IsMember(String dev_name, String[] list) {
    int i = 0;
    boolean found = false;

    while (i < list.length && !found) {
      found = list[i].equalsIgnoreCase(dev_name);
      if (!found) i++;
    }

    return found;
  }


  // ****************************************************
  // Add tango devices in the database without clearing export infos
  // of devices already exported
  // ****************************************************
  private void add_tango_devices(String _class, String server, String[] arr) throws DevFailed {

    //System.out.println( "add_tango_devices " + _class + "/" + server + prtValue(arr) );

    String[] dev_list;
    int i;

    dev_list = db.get_device_name(server , _class);

    /* Add new devices */
    for (i = 0; i < arr.length; i++) {
      if (!IsMember(arr[i], dev_list)) {
        db.add_device(arr[i], _class, server);
      }
    }

    /* Remove old devices */
    for (i = 0; i < dev_list.length; i++) {
      if (!IsMember(dev_list[i], arr))
        db.delete_device(dev_list[i]);
    }

  }

  // ****************************************************
  // Check lexical word
  // ****************************************************
  private void CHECK_LEX(int lt, int le) {
    if (lt != le)
      throw new IllegalStateException("Error at line " + StartLine + ", " + lexical_word[le] + " expected");
  }

  // ****************************************************
  // Parse a resource value
  // ****************************************************
  private String[] parse_resource_value(FileReader f) throws IOException {

    int i,lex;
    String[] ret = new String[1024];
    int nbr;

    /* Resource value */
    lex = COMA;
    nbr = 0;

    while ((lex == COMA || lex == ASLASH) && word != null) {

      word = read_full_word(f);
      lex = class_lex(word);

      /* allow ... ,\ syntax */
      if (lex == ASLASH) {
        word = read_full_word(f);
        lex = class_lex(word);
      }

      CHECK_LEX(lex, STRING);

      ret[nbr] = word;
      nbr++;

      word = read_word(f);
      lex = class_lex(word);

    }

    String[] r = new String[nbr];
    for (i = 0; i < nbr; i++) r[i] = ret[i];
    return r;

  }

  // ****************************************************
  // Parse a resource file
  // Return error as String (zero length when sucess)
  // ****************************************************
  public String parse_res_file(String file_name) {
    FileReader f;
    boolean eof = false;
    int lex;

    String domain;
    String family;
    String member;
    String name;
    String prop_name;

    int i;
    CrtLine = 1;
    NextChar = ' ';
    CurrentChar = ' ';

    try {

      /* OPEN THE FILE                  */
      f = new FileReader(file_name);

      /* CHECK BEGINING OF CONFIG FILE  */
      word = read_word(f);
      if (word == null)
        return file_name + " is empty...";
      lex = class_lex(word);

      /* PARSE                          */
      while (!eof) {
        switch (lex) {
          /* Start a resource mame */
          case STRING:

            /* Domain */
            domain = word;
            word = read_word(f);
            lex = class_lex(word);
            CHECK_LEX(lex, SLASH);

            /* Family */
            word = read_word(f);
            lex = class_lex(word);
            CHECK_LEX(lex, STRING);
            family = word;
            word = read_word(f);
            lex = class_lex(word);

            switch (lex) {

              case SLASH:

                /* Member */
                word = read_word(f);
                lex = class_lex(word);
                CHECK_LEX(lex, STRING);
                member = word;
                word = read_word(f);
                lex = class_lex(word);

                switch (lex) {

                  case SLASH:
                    /* We have a 4 fields name */
                    word = read_word(f);
                    lex = class_lex(word);
                    CHECK_LEX(lex, STRING);
                    name = word;

                    word = read_word(f);
                    lex = class_lex(word);

                    switch (lex) {

                      case COLON:
                        {
                          /* Device definition */
                          String[] values = parse_resource_value(f);
                          lex = class_lex(word);

                          if (member.equalsIgnoreCase("device")) {
                            /* Device definition */
                            add_tango_devices(name, domain + "/" + family, values);
                          }
                        }
                        break;

                      case ARROW:
                        {
                          /* We have an attribute property definition */
                          word = read_word(f);
                          lex = class_lex(word);
                          CHECK_LEX(lex, STRING);
                          prop_name = word;

                          /* jump : */
                          word = read_word(f);
                          lex = class_lex(word);
                          CHECK_LEX(lex, COLON);

                          /* Resource value */
                          String[] values = parse_resource_value(f);
                          lex = class_lex(word);

                          /* Device attribute definition */
                          put_tango_dev_attr_prop(domain + "/" + family + "/" + member, name, prop_name, values);
                        }
                        break;

                      default:
                        return "COLON or -> expected at line " + StartLine;

                    }
                    break;

                  case ARROW:

                    /* We have a device property or attribute class definition */

                    word = read_word(f);
                    lex = class_lex(word);
                    CHECK_LEX(lex, STRING);
                    prop_name = word;

                    /* jump : */
                    word = read_word(f);
                    lex = class_lex(word);
                    if( lex==SLASH ) {
                      // The property name contains a slash
                      // Used for sub device property
                      word = read_word(f);
                      lex = class_lex(word);
                      CHECK_LEX(lex, STRING);
                      prop_name = prop_name + "/" + word;
                      word = read_word(f);
                      lex = class_lex(word);
                    }

                    CHECK_LEX(lex, COLON);

                    /* Resource value */
                    String[] values = parse_resource_value(f);
                    lex = class_lex(word);

                    if (domain.equalsIgnoreCase("class")) {

                      /* Class attribute property definition */
                      put_tango_class_attr_prop(family, member, prop_name, values);

                    } else {

                      /* Device property definition */
                      put_tango_res(domain + "/" + family + "/" + member, prop_name, values);

                    }
                    break;

                  default:
                    return "SLASH or -> expected at line " + StartLine;

                }
                break;

              case ARROW:

                /* We have a class property */
                /* Member */
                word = read_word(f);
                lex = class_lex(word);
                CHECK_LEX(lex, STRING);
                member = word;
                word = read_word(f);
                lex = class_lex(word);

                /* Resource value */
                String[] values = parse_resource_value(f);
                lex = class_lex(word);

                /* Class resource */
                if (domain.equalsIgnoreCase("class")) {
                  put_tango_res_class(family, member, values);
                } else if (domain.equalsIgnoreCase("free")) {
                  put_free_tango_res(family, member, values);
                } else {
                  return "Invlalid class property syntax on " + domain + "/" + family + "/" + member;
                }
                break;

              default:
                return "SLASH or -> expected at line " + StartLine;
            }
            break;

          default:
            return "Invalid resource name get " + lexical_word[lex] + " instead of STRING al line " + StartLine;
        }

        eof = (word == null);
      }

      return "";

    } catch (Exception ex) {

      if (ex instanceof DevFailed) {

        String result = "";
        DevFailed e = (DevFailed) ex;
        for (i = 0; i < e.errors.length; i++) {
          result += "Desc -> " + e.errors[i].desc + "\n";
          result += "Reason -> " + e.errors[i].reason + "\n";
          result += "Origin -> " + e.errors[i].origin + "\n";
        }

        return result;

      } else {
        return ex.getMessage();
      }

    }


  }

}
