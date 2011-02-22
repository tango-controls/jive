package jive;

import java.io.StringReader;
import java.util.Vector;

/** A class for parsing input arguments of tango command and attribute. */
public class ArgParser {

  /**
   * Construct an parser for the given string.
   * @param s String to be parsed.
   */
  public ArgParser(String s) throws NumberFormatException {

    if(s==null || s.length()==0)
      throw new NumberFormatException("Empty argument, you must specify a value");

    theStream = new StringReader(s);
    width=0;
    height=0;
    // Done 2 times to initialise nextchar and currentChar
    read_char();
    read_char();
  }

  /**
   * Parse a boolean value.
   * @return The boolean.
   * @throws NumberFormatException In case of failure
   */
  public boolean parse_boolean() throws NumberFormatException {

    String w = read_word();
    if(w!=null) {
      return get_boolean(w);
    } else {
      throw new NumberFormatException("boolean value expected.");
    }

  }

  /**
   * Parse a boolean array.
   * @return The boolean array.
   * @throws NumberFormatException In case of failure
   */
  public boolean[] parse_boolean_array() throws NumberFormatException {

    Vector tmp = parse_array();
    boolean[] ret = new boolean[tmp.size()];
    for(int l=0;l<ret.length;l++) ret[l]=get_boolean((String)tmp.get(l));
    return ret;

  }

  /**
   * Parse a boolean image.
   * @return The boolean image.
   * @throws NumberFormatException In case of failure
   * @see #get_image_width
   * @see #get_image_height
   */
  public boolean[] parse_boolean_image() throws NumberFormatException {

    Vector tmp = parse_image();
    boolean[] ret = new boolean[width*height];
    for(int l=0;l<ret.length;l++) ret[l]=get_boolean((String)tmp.get(l));
    return ret;

  }

  /**
   * Parse a char value.
   * @return The char.
   * @throws NumberFormatException In case of failure
   */
  public byte parse_char() throws NumberFormatException {

    String w = read_word();
    if(w!=null) {
      return get_char(w);
    } else {
      throw new NumberFormatException("char value expected.");
    }

  }

  /**
   * Parse a char array.
   * @return The char array.
   * @throws NumberFormatException In case of failure
   */
  public byte[] parse_char_array() throws NumberFormatException {

    Vector tmp = parse_array();
    byte[] ret = new byte[tmp.size()];
    for(int l=0;l<ret.length;l++) ret[l]=get_char((String)tmp.get(l));
    return ret;

  }

  /**
   * Parse a char image.
   * @return The char image.
   * @throws NumberFormatException In case of failure
   * @see #get_image_width
   * @see #get_image_height
   */
  public byte[] parse_char_image() throws NumberFormatException {

    Vector tmp = parse_image();
    byte[] ret = new byte[width*height];
    for(int l=0;l<ret.length;l++) ret[l]=get_char((String)tmp.get(l));
    return ret;

  }

  /**
   * Parse an unsigned char value (8 bit unsigned).
   * @return The char.
   * @throws NumberFormatException In case of failure
   */
  public short parse_uchar() throws NumberFormatException {

    String w = read_word();
    if(w!=null) {
      return get_uchar(w);
    } else {
      throw new NumberFormatException("unsigned char value expected.");
    }

  }

  /**
   * Parse a char array.
   * @return The char array.
   * @throws NumberFormatException In case of failure
   */
  public short[] parse_uchar_array() throws NumberFormatException {

    Vector tmp = parse_array();
    short[] ret = new short[tmp.size()];
    for(int l=0;l<ret.length;l++) ret[l]=get_uchar((String)tmp.get(l));
    return ret;

  }

  /**
   * Parse a char image.
   * @return The char image.
   * @throws NumberFormatException In case of failure
   * @see #get_image_width
   * @see #get_image_height
   */
  public short[] parse_uchar_image() throws NumberFormatException {

    Vector tmp = parse_image();
    short[] ret = new short[width*height];
    for(int l=0;l<ret.length;l++) ret[l]=get_uchar((String)tmp.get(l));
    return ret;

  }

  /**
   * Parse a short value.
   * @return The short.
   * @throws NumberFormatException In case of failure
   */
  public short parse_short() throws NumberFormatException {

    String w = read_word();
    if(w!=null) {
      return get_short(w);
    } else {
      throw new NumberFormatException("short value expected.");
    }

  }

  /**
   * Parse a short array.
   * @return The short array.
   * @throws NumberFormatException In case of failure
   */
  public short[] parse_short_array() throws NumberFormatException {

    Vector tmp = parse_array();
    short[] ret = new short[tmp.size()];
    for(int l=0;l<ret.length;l++) ret[l]=get_short((String)tmp.get(l));
    return ret;

  }

  /**
   * Parse a short image.
   * @return The short image.
   * @throws NumberFormatException In case of failure
   * @see #get_image_width
   * @see #get_image_height
   */
  public short[] parse_short_image() throws NumberFormatException {

    Vector tmp = parse_image();
    short[] ret = new short[width*height];
    for(int l=0;l<ret.length;l++) ret[l]=get_short((String)tmp.get(l));
    return ret;

  }

  /**
   * Parse an unsigned short value.
   * @return The ushort.
   * @throws NumberFormatException In case of failure
   */
  public int parse_ushort() throws NumberFormatException {

    String w = read_word();
    if(w!=null) {
      return get_ushort(w);
    } else {
      throw new NumberFormatException("unsigned short value expected.");
    }

  }

  /**
   * Parse an unsigned short array.
   * @return The unsigned short array.
   * @throws NumberFormatException In case of failure
   */
  public int[] parse_ushort_array() throws NumberFormatException {

    Vector tmp = parse_array();
    int[] ret = new int[tmp.size()];
    for(int l=0;l<ret.length;l++)
      ret[l]=get_ushort((String)tmp.get(l));
    return ret;

  }

  /**
   * Parse an unsigned short image.
   * @return The unsigned short image.
   * @throws NumberFormatException In case of failure
   * @see #get_image_width
   * @see #get_image_height
   */
  public int[] parse_ushort_image() throws NumberFormatException {

    Vector tmp = parse_image();
    int[] ret = new int[width*height];
    for(int l=0;l<ret.length;l++)
      ret[l]=get_ushort((String)tmp.get(l));
    return ret;

  }

  /**
   * Parse a long value (32bit signed).
   * @return The long.
   * @throws NumberFormatException In case of failure
   */
  public int parse_long() throws NumberFormatException {

    String w = read_word();
    if(w!=null) {
      return get_long(w);
    } else {
      throw new NumberFormatException("Integer value expected.");
    }

  }

  /**
   * Parse a long array (32bit signed).
   * @return The long array.
   * @throws NumberFormatException In case of failure
   */
  public int[] parse_long_array() throws NumberFormatException {

    Vector tmp = parse_array();
    int[] ret = new int[tmp.size()];
    for(int l=0;l<ret.length;l++) ret[l]=get_long((String)tmp.get(l));
    return ret;

  }

  /**
   * Parse a long image (32bit signed).
   * @return The long image.
   * @throws NumberFormatException In case of failure
   * @see #get_image_width
   * @see #get_image_height
   */
  public int[] parse_long_image() throws NumberFormatException {

    Vector tmp = parse_image();
    int[] ret = new int[width*height];
    for(int l=0;l<ret.length;l++) ret[l]=get_long((String)tmp.get(l));
    return ret;

  }

  /**
   * Parse an unsigned long value (32bit unsigned).
   * @return The ulong.
   * @throws NumberFormatException In case of failure
   */
  public long parse_ulong() throws NumberFormatException {

    String w = read_word();
    if(w!=null) {
      return get_ulong(w);
    } else {
      throw new NumberFormatException("unsigned long value expected.");
    }

  }

  /**
   * Parse an unsigned long array (32bit unsigned).
   * @return The ulong array.
   * @throws NumberFormatException In case of failure
   */
  public long[] parse_ulong_array() throws NumberFormatException {

    Vector tmp = parse_array();
    long[] ret = new long[tmp.size()];
    for(int l=0;l<ret.length;l++)
      ret[l]=get_ulong((String)tmp.get(l));
    return ret;

  }

  /**
   * Parse an unsigned long image (32bit unsigned).
   * @return The ulong image.
   * @throws NumberFormatException In case of failure
   * @see #get_image_width
   * @see #get_image_height
   */
  public long[] parse_ulong_image() throws NumberFormatException {

    Vector tmp = parse_image();
    long[] ret = new long[width*height];
    for(int l=0;l<ret.length;l++)
      ret[l]=get_ulong((String)tmp.get(l));
    return ret;

  }

  /**
   * Parse a long64 value (64bit signed).
   * @return The long64.
   * @throws NumberFormatException In case of failure
   */
  public long parse_long64() throws NumberFormatException {

    String w = read_word();
    if(w!=null) {
      return get_long64(w);
    } else {
      throw new NumberFormatException("long64 value expected.");
    }

  }

  /**
   * Parse an unsigned long array (64bit signed).
   * @return The ulong array.
   * @throws NumberFormatException In case of failure
   */
  public long[] parse_long64_array() throws NumberFormatException {

    Vector tmp = parse_array();
    long[] ret = new long[tmp.size()];
    for(int l=0;l<ret.length;l++)
      ret[l]=get_long64((String)tmp.get(l));
    return ret;

  }

  /**
   * Parse an unsigned long image (64bit signed).
   * @return The long64 image.
   * @throws NumberFormatException In case of failure
   * @see #get_image_width
   * @see #get_image_height
   */
  public long[] parse_long64_image() throws NumberFormatException {

    Vector tmp = parse_image();
    long[] ret = new long[width*height];
    for(int l=0;l<ret.length;l++)
      ret[l]=get_long64((String)tmp.get(l));
    return ret;

  }

  /**
   * Parse a float value.
   * @return The float.
   * @throws NumberFormatException In case of failure
   */
  public float parse_float() throws NumberFormatException {

    String w = read_word();
    if(w!=null) {
      return Float.parseFloat(w);
    } else {
      throw new NumberFormatException("float value expected.");
    }

  }

  /**
   * Parse a float array.
   * @return The float array.
   * @throws NumberFormatException In case of failure
   */
  public float[] parse_float_array() throws NumberFormatException {

    Vector tmp = parse_array();
    float[] ret = new float[tmp.size()];
    for(int l=0;l<ret.length;l++) ret[l]=Float.parseFloat((String)tmp.get(l));
    return ret;

  }

  /**
   * Parse a float image.
   * @return The float image.
   * @throws NumberFormatException In case of failure
   * @see #get_image_width
   * @see #get_image_height
   */
  public float[] parse_float_image() throws NumberFormatException {

    Vector tmp = parse_image();
    float[] ret = new float[width*height];
    for(int l=0;l<ret.length;l++) ret[l]=Float.parseFloat((String)tmp.get(l));
    return ret;

  }
  /**
   * Parse a double value.
   * @return The double.
   * @throws NumberFormatException In case of failure
   */
  public double parse_double() throws NumberFormatException {

    String w = read_word();
    if(w!=null) {
      return Double.parseDouble(w);
    } else {
      throw new NumberFormatException("double value expected.");
    }

  }

  /**
   * Parse a double array.
   * @return The double array.
   * @throws NumberFormatException In case of failure
   */
  public double[] parse_double_array() throws NumberFormatException {

    Vector tmp = parse_array();
    double[] ret = new double[tmp.size()];
    for(int l=0;l<ret.length;l++) ret[l]=Double.parseDouble((String)tmp.get(l));
    return ret;

  }

  /**
   * Parse a double image.
   * @return The double image.
   * @throws NumberFormatException In case of failure
   * @see #get_image_width
   * @see #get_image_height
   */
  public double[] parse_double_image() throws NumberFormatException {

    Vector tmp = parse_image();
    double[] ret = new double[width*height];
    for(int l=0;l<ret.length;l++) ret[l]=Double.parseDouble((String)tmp.get(l));
    return ret;

  }
  /**
   * Parse a string.
   * @return The string.
   * @throws NumberFormatException In case of failure
   */
  public String parse_string() throws NumberFormatException {

    String w = read_word();
    if(w!=null) {
      return w;
    } else {
      throw new NumberFormatException("string expected.");
    }

  }

  /**
   * Parse a string array.
   * @return The string array.
   * @throws NumberFormatException In case of failure
   */
  public String[] parse_string_array() throws NumberFormatException {

    Vector tmp = parse_array();
    String[] ret = new String[tmp.size()];
    for(int l=0;l<ret.length;l++) ret[l]=(String)tmp.get(l);
    return ret;

  }

  /**
   * Parse a string.
   * @return The string image.
   * @throws NumberFormatException In case of failure
   * @see #get_image_width
   * @see #get_image_height
   */
  public String[] parse_string_image() throws NumberFormatException {

    Vector tmp = parse_image();
    String[] ret = new String[width*height];
    for(int l=0;l<ret.length;l++) ret[l]=(String)tmp.get(l);
    return ret;

  }

  /**
   * Returns the width of the last parse image.
   */
  public int get_image_width() {
    return width;
  }

  /**
   * Returns the height of the last parse image.
   */
  public int get_image_height() {
    return height;
  }

  // ********************************************************************************************
  // Private stuff
  // ********************************************************************************************

  private StringReader theStream;
  private char nextChar;
  private char currentChar;
  private boolean backSlashed;
  private int  width;
  private int  height;

  // ****************************************************
  private void read_char() {

    backSlashed = false;

    try {
      int c = theStream.read();
      currentChar = nextChar;
      if(c<0) {
        nextChar = 0;
      } else {
        nextChar = (char) c;
      }
    } catch (Exception e) {
      nextChar = 0;
      currentChar = 0;
    }

    /* Treat \" here */
    if(currentChar=='\\' && nextChar=='"') {
      read_char(); // return '"'
      backSlashed = true;
    }

  }

  // ****************************************************
  private void jump_space() {
    while (currentChar <= 32 && currentChar > 0) read_char();
  }

  // ****************************************************
  private String read_word() throws NumberFormatException {

    StringBuffer ret_word = new StringBuffer();

    /* Jump space and comments */
    jump_space();

    /* Treat special character */
    if (currentChar == ',' || currentChar == '[' || currentChar == ']') {
      ret_word.append(currentChar);
      read_char();
      return ret_word.toString();
    }

    /* Treat string */
    if (currentChar=='"' && !backSlashed) {
      read_char();
      while ((currentChar != '"' || backSlashed) && currentChar != 0 && currentChar != '\n') {
        ret_word.append(currentChar);
        read_char();
      }
      if (currentChar == 0 || currentChar == '\n') {
        NumberFormatException e = new NumberFormatException("Unterminated string.");
        throw e;
      }
      read_char();
      return ret_word.toString();
    }

    /* Treat other word */
    while (currentChar > 32 && currentChar != '[' && currentChar != ']' && currentChar != ',') {
      ret_word.append(currentChar);
      read_char();
    }

    if (ret_word.length() == 0) {
      return null;
    }

    return ret_word.toString();
  }

  // ****************************************************
  private void jump_sep(String sep) throws NumberFormatException {
    String w = read_word();
    if(w==null)
      throw new NumberFormatException("Separator " + sep + " expected.");
    if(!w.equals(sep))
      throw new NumberFormatException("Separator " + sep + " expected.");
  }

  // ****************************************************
  private boolean is_array_end() {
    if(currentChar==0)   return true;
    if(currentChar==']') return true;
    return false;
  }

  // ****************************************************
  private Vector parse_array() throws NumberFormatException {

    Vector ret = new Vector();
    boolean isClosed=false;

    jump_space();
    if(currentChar=='[') {
      isClosed = true;
      jump_sep("[");
      jump_space();
    }

    while(!is_array_end()) {
      ret.add(read_word());
      jump_space();
      if(!is_array_end()) {
        jump_sep(",");
        jump_space();
      }
    }

    if(isClosed) jump_sep("]");

    return ret;

  }

  // ****************************************************
  private Vector parse_image() throws NumberFormatException {

    // Read the fist line
    Vector ret = parse_array();
    jump_space();
    width  = ret.size();
    height = 1;

    while(currentChar=='[') {

      Vector tmp = parse_array();
      if(tmp.size()!=width)
        throw new NumberFormatException("All lines in an image must have the same size.");
      ret.addAll(tmp);
      height++;
      jump_space();

    }

    return ret;

  }

  // ****************************************************
  private boolean get_boolean(String w) throws NumberFormatException {

    if(w.equalsIgnoreCase("true") || w.equalsIgnoreCase("1"))
      return true;

    if(w.equalsIgnoreCase("false") || w.equalsIgnoreCase("0"))
      return false;

    throw new NumberFormatException("invalid boolean value "+w+" [true,false or 0,1].");

  }

  // ****************************************************
  private long get_number(String w,String type,long min,long max) throws NumberFormatException {

    int conv_base=10;
    if (w.startsWith("0x") || w.startsWith("0X")) {
      w = w.substring(2);
      conv_base=16;
    }

    long ret = Long.parseLong(w,conv_base);

    if(ret<min || ret>max)
      throw new NumberFormatException(type+" value "+ret+" out of range ["+min+","+max+"].");

    return ret;

  }

  // ****************************************************
  private byte get_char(String w) throws NumberFormatException {
    if(w.startsWith("'")) {
      if(w.endsWith("'") && w.length()==3) {
        return (byte)w.charAt(1);
      } else {
        throw new NumberFormatException("Invalid char value for input string " + w);
      }
    } else {
      return (byte)get_number(w,"char",-128L,127L);
    }
  }

  // ****************************************************
  private short get_uchar(String w) throws NumberFormatException {
    return (short)get_number(w,"unsigned char",0L,255L);
  }

  // ****************************************************
  private short get_short(String w) throws NumberFormatException {
    return (short)get_number(w,"short",-32768L,32767L);
  }

  // ****************************************************
  private int get_ushort(String w) throws NumberFormatException {
    return (int)get_number(w,"unsigned short",0L,65535L);
  }

  // ****************************************************
  private int get_long(String w) throws NumberFormatException {
    return (int)get_number(w,"long",-2147483648L,2147483647L);
  }

  // ****************************************************
  private long get_ulong(String w) throws NumberFormatException {
    return get_number(w,"unsigned long",0,4294967295L);
  }

  // ****************************************************
  private long get_long64(String w) throws NumberFormatException {
    return get_number(w,"long64",-9223372036854775808L,9223372036854775807L);
  }

  // ****************************************************
  // Debug stuff
  // ****************************************************
  static private void print_array(boolean[] a) {
    System.out.print("Length=" + a.length + ": ");
    for(int i=0;i<a.length;i++) {
      System.out.print(a[i]);
      System.out.print(" ");
    }
    System.out.println();
  }
  static private void print_array(byte[] a) {
    System.out.print("Length=" + a.length + ": ");
    for(int i=0;i<a.length;i++) {
      System.out.print(a[i]);
      System.out.print(" ");
    }
    System.out.println();
  }
  static private void print_array(short[] a) {
    System.out.print("Length=" + a.length + ": ");
    for(int i=0;i<a.length;i++) {
      System.out.print(a[i]);
      System.out.print(" ");
    }
    System.out.println();
  }
  static private void print_array(int[] a) {
    System.out.print("Length=" + a.length + ": ");
    for(int i=0;i<a.length;i++) {
      System.out.print(a[i]);
      System.out.print(" ");
    }
    System.out.println();
  }
  static private void print_array(float[] a) {
    System.out.print("Length=" + a.length + ": ");
    for(int i=0;i<a.length;i++) {
      System.out.print(a[i]);
      System.out.print(" ");
    }
    System.out.println();
  }
  static private void print_array(double[] a) {
    System.out.print("Length=" + a.length + ": ");
    for(int i=0;i<a.length;i++) {
      System.out.print(a[i]);
      System.out.print(" ");
    }
    System.out.println();
  }
  static private void print_array(String[] a) {
    System.out.print("Length=" + a.length + ": ");
    for(int i=0;i<a.length;i++) {
      System.out.print(a[i]);
      System.out.print(" ");
    }
    System.out.println();
  }

  static public void main(String[] args) {

    final ArgParser a1 = new ArgParser("true");
    final ArgParser a2 = new ArgParser("7,8,127.123,1e6,57.8");
    final ArgParser a3 = new ArgParser("[1.0,2,3.5] [4,5.7,6] [7,8,127.123] [10.1,11.2,12]");
    final ArgParser a4 = new ArgParser("toto,\"ta ta\",titi");
    final ArgParser a5 = new ArgParser("[34,5] [toto,\"ta ta\",titi]");
    final ArgParser a6 = new ArgParser("[7,8,-9,16,5700,0x10]");
    final ArgParser a7 = new ArgParser("\"test with \\\" ,quote\"");
    final ArgParser a8 = new ArgParser("\\\"quote\\\"");

    try {
      System.out.println(a1.parse_boolean());
      print_array(a2.parse_float_array());
      print_array(a3.parse_double_image());
      print_array(a4.parse_string_array());
      print_array(a5.parse_long_array());
      print_array(a5.parse_string_array());
      print_array(a6.parse_short_array());
      System.out.println(a7.parse_string());
      System.out.println(a8.parse_string());
    } catch (NumberFormatException e) {
      System.out.println("Getting error:" + e);
    }

  }

}
