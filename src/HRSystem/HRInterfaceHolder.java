package HRSystem;

/**
* HRSystem/HRInterfaceHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from HRSystem.idl
* Tuesday, November 13, 2018 8:46:24 o'clock PM EST
*/

public final class HRInterfaceHolder implements org.omg.CORBA.portable.Streamable
{
  public HRSystem.HRInterface value = null;

  public HRInterfaceHolder ()
  {
  }

  public HRInterfaceHolder (HRSystem.HRInterface initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = HRSystem.HRInterfaceHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    HRSystem.HRInterfaceHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return HRSystem.HRInterfaceHelper.type ();
  }

}