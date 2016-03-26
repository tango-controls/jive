package jive;

/**
 * Class contanining Tooltip help text
 *
 * @author  pons
 */

public class TangoHelp {

  static public String getHelp(String name,int type,int level) {

    String help=null;

    if (name.equals("poll_ring_depth"))
      help = "Polling buffer length";

    if (name.equals("poll_old_factor"))
      help = "Number of polling period before getting \"Data too old\" exception";

    if (name.equals("is_polled"))
      help = "Indicates if the object is polled\nPossible value are: Yes No";

    if (name.equals("polling_period"))
      help = "Polling period in millisec";

    if (name.equals("logging_level"))
      help = "The logging_level property controls the initial logging level of a device.\n"+
             "This property is overwritten by the verbose command line option (-v)";

    if (name.equals("current_logging_level"))
      help = "Logging level of a device. Its set of possible values is:\n" +
             "OFF, FATAL, ERROR, WARNING, INFO or DEBUG.";

    if (name.equals("logging_target"))
      help = "The logging_target property is a multi-valued property containing the initial\n" +
             "target list.";

    if (name.equals("current_logging_target"))
      help = "Set of logging target. Each entry must have the following format: target_type::target_name\n" +
             "(where target_type is one of the supported target types and target_name, the name of the\n"+
             "target). Supported target types are: console, file and device.\n" +
             "For a device target, target_name must contain the name of a log consumer device.\n"+
             "For a file target, target_name is the name of the file to log to. If omitted the device's\n"+
             "name is used to build the file name (domain_family_member.log).\n"+
             "Finally, target_name is ignored in the case of a console target. The TLS does not report\n" +
             "any error occurred while trying to setup the initial targets.\n\n" +
             "Logging_target property example:\n" +
             "    console::cout\n" +
             "    file::/home/me/mydevice.log\n"+
             "    device::tmp/log/1\n\n";


    if (name.equals("logging_rft"))
      help = "The logging_rft property specifies the rolling file threshold (rft), of the device's file targets.\n"+
             "This threshold is expressed in Kb in the range [500, 20480]. When the size of a log file reaches the\n"+
             "so-called rolling-file-threshold (rft), it is backuped as \"current_log_file_name\" + \"_1\" and a new\n"+
             "current_log_file_name is opened. Obviously, there is only one backup file at a time (i.e. any existing\n"+
             "backup is destroyed before the current log file is backuped). The default threshold is 2Mb, the minimum\n"+
             "is 500 Kb and the maximum is 20 Mb.";

    if (name.equals("logging_path"))
      help = "The logging_path property overwrites the TANGO_LOG_PATH environment variable. This property can only be\n"+
             "applied to a DServer class device and has no effect on other devices.";

    // Attribute properties
    if( level==7 ) {
      if (name.equals("description")) help="Attribute description";
      if (name.equals("label")) help="Attribute label";
      if (name.equals("unit")) help="Attribute unit";
      if (name.equals("standard_unit")) help="Conversion factor to MKSA unit";
      if (name.equals("display_unit")) help="The attribute unit in a printable form";
      if (name.equals("format")) help="How to print attribute value, format must follow C standard";
      if (name.equals("min_value")) help="Attribute minimun value";
      if (name.equals("max_value")) help="Attribute maximun value";
    }

    if(level==8) {

      if (name.equals("min_alarm")) help="Attribute low level alarm, quality factory will be set to Tango::ATTR_ALARM if its value\n"+
                                         "is less or equal than min_alarm";
      if (name.equals("max_alarm")) help="Attribute high level alarm, quality factory will be set to Tango::ATTR_ALARM if its value\n"+
                                         "is greater or equal than max_alarm";
      if (name.equals("min_warning")) help="Attribute low level alarm, quality factory will be set to Tango::ATTR_WARNING if its value\n"+
                                         "is less or equal than min_warning";
      if (name.equals("max_warning")) help="Attribute high level alarm, quality factory will be set to Tango::ATTR_WARNING if its value\n"+
                                         "is greater or equal than min_warning";

      if (name.equals("delta_val") || name.equals("delta_t"))
        help="When the attribute is read, if the difference between its read value and the last written\n"+
             "value is something more than or equal to the delta_val parameter and if at least delta_val\n"+
             "milli seconds occurs since the last write operation, the attribute quality factor will be\n"+
             "set to Tango::ATTR_ALARM\n";

      if(name.equals("abs_change")) {
        help="abs_change is a list property of maximum 2 values separated by a coma.It specifies the\n"+
             "positive and negative absolute change of the attribute value w.r.t the value of the previous\n"+
             "change event which will trigger the event. If the attribute is a spectrum or an image then a\n"+
             "change event is generated if any one of the attribute value's satisfies the above criterium.\n"+
             "If only one property is specified then it is used for the positive and negative change.\n"+
             "If no properties are specified then the relative change is used.";
      }

      if(name.equals("rel_change")) {
        help="rel_change is a list property of maximum 2 values separated by a coma. It specifies the\n"+
             "positive and negative relative change of the attribute value w.r.t. the value of the previous\n"+
             "change event which will trigger the event. If the attribute is a spectrum or an image then a\n"+
             "change event is generated if any one of the attribute value's satisfies the above criterium.\n"+
             "If only one property is specified then it is used for the positive and negative change.";
      }

      if(name.equals("event_period")) {
        help="The minimum time between events (in milliseconds). If no property is specified then a default\n"+
             "value of 1 second is used.";
      }

      if(name.equals("archive_rel_change")) {
        help="archive_rel_change is a list property of maximum 2 values separated by a coma which\n"+
             "specifies the positive and negative relative change w.r.t. the previous attribute value\n"+
             "which will trigger the event. If the attribute is a spectrum or an image then an archive\n"+
             "event is generated if any one of the attribute value's satisfies the above criterium.\n"+
             "If only one property is specified then it is used for the positive and negative change.\n"+
             "If no properties are specified then a default fo +-10% is used.";
      }

      if(name.equals("archive_abs_change")) {
        help="archive_abs_change is a list property of maximum 2 values separated by a coma which\n"+
             "specifies the positive and negative absolute change w.r.t the previous attribute value\n"+
             "which will trigger the event. If the attribute is a spectrum or an image then an archive\n"+
             "event is generated if any one of the attribute value's satisfies the above criterium.\n"+
             "If only one property is specified then it is used for the positive and negative change.\n"+
             "If no properties are specified then the relative change is used.";
      }

      if(name.equals("archive_period")) {
        help="archive_period is the minimum time between archive events (in milliseconds).\n"+
             "If no property is specified then a default value of 10 seconds is used.";
      }

    }

    return help;
  }

}
