package HRSystem;


/**
* HRSystem/ProjectHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from HRSystem.idl
* Tuesday, November 13, 2018 8:46:24 o'clock PM EST
*/

abstract public class ProjectHelper
{
  private static String  _id = "IDL:HRSystem/Project:1.0";

  public static void insert (org.omg.CORBA.Any a, HRSystem.Project that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static HRSystem.Project extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  private static boolean __active = false;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      synchronized (org.omg.CORBA.TypeCode.class)
      {
        if (__typeCode == null)
        {
          if (__active)
          {
            return org.omg.CORBA.ORB.init().create_recursive_tc ( _id );
          }
          __active = true;
          org.omg.CORBA.StructMember[] _members0 = new org.omg.CORBA.StructMember [3];
          org.omg.CORBA.TypeCode _tcOf_members0 = null;
          _tcOf_members0 = org.omg.CORBA.ORB.init ().create_string_tc (0);
          _members0[0] = new org.omg.CORBA.StructMember (
            "projectID",
            _tcOf_members0,
            null);
          _tcOf_members0 = org.omg.CORBA.ORB.init ().create_string_tc (0);
          _members0[1] = new org.omg.CORBA.StructMember (
            "clientName",
            _tcOf_members0,
            null);
          _tcOf_members0 = org.omg.CORBA.ORB.init ().create_string_tc (0);
          _members0[2] = new org.omg.CORBA.StructMember (
            "projectName",
            _tcOf_members0,
            null);
          __typeCode = org.omg.CORBA.ORB.init ().create_struct_tc (HRSystem.ProjectHelper.id (), "Project", _members0);
          __active = false;
        }
      }
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static HRSystem.Project read (org.omg.CORBA.portable.InputStream istream)
  {
    HRSystem.Project value = new HRSystem.Project ();
    value.projectID = istream.read_string ();
    value.clientName = istream.read_string ();
    value.projectName = istream.read_string ();
    return value;
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, HRSystem.Project value)
  {
    ostream.write_string (value.projectID);
    ostream.write_string (value.clientName);
    ostream.write_string (value.projectName);
  }

}
