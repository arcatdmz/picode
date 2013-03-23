/**
 * Autogenerated by Thrift Compiler (0.9.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.phybots.picode.api.remote;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.EncodingUtils;
import org.apache.thrift.TException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MindstormsNXTPoseData implements org.apache.thrift.TBase<MindstormsNXTPoseData, MindstormsNXTPoseData._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("MindstormsNXTPoseData");

  private static final org.apache.thrift.protocol.TField A_FIELD_DESC = new org.apache.thrift.protocol.TField("a", org.apache.thrift.protocol.TType.I32, (short)1);
  private static final org.apache.thrift.protocol.TField B_FIELD_DESC = new org.apache.thrift.protocol.TField("b", org.apache.thrift.protocol.TType.I32, (short)2);
  private static final org.apache.thrift.protocol.TField C_FIELD_DESC = new org.apache.thrift.protocol.TField("c", org.apache.thrift.protocol.TType.I32, (short)3);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new MindstormsNXTPoseDataStandardSchemeFactory());
    schemes.put(TupleScheme.class, new MindstormsNXTPoseDataTupleSchemeFactory());
  }

  public int a; // required
  public int b; // required
  public int c; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    A((short)1, "a"),
    B((short)2, "b"),
    C((short)3, "c");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // A
          return A;
        case 2: // B
          return B;
        case 3: // C
          return C;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __A_ISSET_ID = 0;
  private static final int __B_ISSET_ID = 1;
  private static final int __C_ISSET_ID = 2;
  private byte __isset_bitfield = 0;
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.A, new org.apache.thrift.meta_data.FieldMetaData("a", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.B, new org.apache.thrift.meta_data.FieldMetaData("b", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    tmpMap.put(_Fields.C, new org.apache.thrift.meta_data.FieldMetaData("c", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I32)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(MindstormsNXTPoseData.class, metaDataMap);
  }

  public MindstormsNXTPoseData() {
  }

  public MindstormsNXTPoseData(
    int a,
    int b,
    int c)
  {
    this();
    this.a = a;
    setAIsSet(true);
    this.b = b;
    setBIsSet(true);
    this.c = c;
    setCIsSet(true);
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public MindstormsNXTPoseData(MindstormsNXTPoseData other) {
    __isset_bitfield = other.__isset_bitfield;
    this.a = other.a;
    this.b = other.b;
    this.c = other.c;
  }

  public MindstormsNXTPoseData deepCopy() {
    return new MindstormsNXTPoseData(this);
  }

  @Override
  public void clear() {
    setAIsSet(false);
    this.a = 0;
    setBIsSet(false);
    this.b = 0;
    setCIsSet(false);
    this.c = 0;
  }

  public int getA() {
    return this.a;
  }

  public MindstormsNXTPoseData setA(int a) {
    this.a = a;
    setAIsSet(true);
    return this;
  }

  public void unsetA() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __A_ISSET_ID);
  }

  /** Returns true if field a is set (has been assigned a value) and false otherwise */
  public boolean isSetA() {
    return EncodingUtils.testBit(__isset_bitfield, __A_ISSET_ID);
  }

  public void setAIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __A_ISSET_ID, value);
  }

  public int getB() {
    return this.b;
  }

  public MindstormsNXTPoseData setB(int b) {
    this.b = b;
    setBIsSet(true);
    return this;
  }

  public void unsetB() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __B_ISSET_ID);
  }

  /** Returns true if field b is set (has been assigned a value) and false otherwise */
  public boolean isSetB() {
    return EncodingUtils.testBit(__isset_bitfield, __B_ISSET_ID);
  }

  public void setBIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __B_ISSET_ID, value);
  }

  public int getC() {
    return this.c;
  }

  public MindstormsNXTPoseData setC(int c) {
    this.c = c;
    setCIsSet(true);
    return this;
  }

  public void unsetC() {
    __isset_bitfield = EncodingUtils.clearBit(__isset_bitfield, __C_ISSET_ID);
  }

  /** Returns true if field c is set (has been assigned a value) and false otherwise */
  public boolean isSetC() {
    return EncodingUtils.testBit(__isset_bitfield, __C_ISSET_ID);
  }

  public void setCIsSet(boolean value) {
    __isset_bitfield = EncodingUtils.setBit(__isset_bitfield, __C_ISSET_ID, value);
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case A:
      if (value == null) {
        unsetA();
      } else {
        setA((Integer)value);
      }
      break;

    case B:
      if (value == null) {
        unsetB();
      } else {
        setB((Integer)value);
      }
      break;

    case C:
      if (value == null) {
        unsetC();
      } else {
        setC((Integer)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case A:
      return Integer.valueOf(getA());

    case B:
      return Integer.valueOf(getB());

    case C:
      return Integer.valueOf(getC());

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case A:
      return isSetA();
    case B:
      return isSetB();
    case C:
      return isSetC();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof MindstormsNXTPoseData)
      return this.equals((MindstormsNXTPoseData)that);
    return false;
  }

  public boolean equals(MindstormsNXTPoseData that) {
    if (that == null)
      return false;

    boolean this_present_a = true;
    boolean that_present_a = true;
    if (this_present_a || that_present_a) {
      if (!(this_present_a && that_present_a))
        return false;
      if (this.a != that.a)
        return false;
    }

    boolean this_present_b = true;
    boolean that_present_b = true;
    if (this_present_b || that_present_b) {
      if (!(this_present_b && that_present_b))
        return false;
      if (this.b != that.b)
        return false;
    }

    boolean this_present_c = true;
    boolean that_present_c = true;
    if (this_present_c || that_present_c) {
      if (!(this_present_c && that_present_c))
        return false;
      if (this.c != that.c)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(MindstormsNXTPoseData other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    MindstormsNXTPoseData typedOther = (MindstormsNXTPoseData)other;

    lastComparison = Boolean.valueOf(isSetA()).compareTo(typedOther.isSetA());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetA()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.a, typedOther.a);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetB()).compareTo(typedOther.isSetB());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetB()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.b, typedOther.b);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetC()).compareTo(typedOther.isSetC());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetC()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.c, typedOther.c);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("MindstormsNXTPoseData(");
    boolean first = true;

    sb.append("a:");
    sb.append(this.a);
    first = false;
    if (!first) sb.append(", ");
    sb.append("b:");
    sb.append(this.b);
    first = false;
    if (!first) sb.append(", ");
    sb.append("c:");
    sb.append(this.c);
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // alas, we cannot check 'a' because it's a primitive and you chose the non-beans generator.
    // alas, we cannot check 'b' because it's a primitive and you chose the non-beans generator.
    // alas, we cannot check 'c' because it's a primitive and you chose the non-beans generator.
    // check for sub-struct validity
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bitfield = 0;
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class MindstormsNXTPoseDataStandardSchemeFactory implements SchemeFactory {
    public MindstormsNXTPoseDataStandardScheme getScheme() {
      return new MindstormsNXTPoseDataStandardScheme();
    }
  }

  private static class MindstormsNXTPoseDataStandardScheme extends StandardScheme<MindstormsNXTPoseData> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, MindstormsNXTPoseData struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // A
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.a = iprot.readI32();
              struct.setAIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // B
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.b = iprot.readI32();
              struct.setBIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // C
            if (schemeField.type == org.apache.thrift.protocol.TType.I32) {
              struct.c = iprot.readI32();
              struct.setCIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      if (!struct.isSetA()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'a' was not found in serialized data! Struct: " + toString());
      }
      if (!struct.isSetB()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'b' was not found in serialized data! Struct: " + toString());
      }
      if (!struct.isSetC()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'c' was not found in serialized data! Struct: " + toString());
      }
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, MindstormsNXTPoseData struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(A_FIELD_DESC);
      oprot.writeI32(struct.a);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(B_FIELD_DESC);
      oprot.writeI32(struct.b);
      oprot.writeFieldEnd();
      oprot.writeFieldBegin(C_FIELD_DESC);
      oprot.writeI32(struct.c);
      oprot.writeFieldEnd();
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class MindstormsNXTPoseDataTupleSchemeFactory implements SchemeFactory {
    public MindstormsNXTPoseDataTupleScheme getScheme() {
      return new MindstormsNXTPoseDataTupleScheme();
    }
  }

  private static class MindstormsNXTPoseDataTupleScheme extends TupleScheme<MindstormsNXTPoseData> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, MindstormsNXTPoseData struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeI32(struct.a);
      oprot.writeI32(struct.b);
      oprot.writeI32(struct.c);
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, MindstormsNXTPoseData struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.a = iprot.readI32();
      struct.setAIsSet(true);
      struct.b = iprot.readI32();
      struct.setBIsSet(true);
      struct.c = iprot.readI32();
      struct.setCIsSet(true);
    }
  }

}
