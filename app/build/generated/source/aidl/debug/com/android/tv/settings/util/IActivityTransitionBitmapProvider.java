/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\MK_XZ\\as_work\\Settings\\app\\src\\main\\aidl\\com\\android\\tv\\settings\\util\\IActivityTransitionBitmapProvider.aidl
 */
package com.android.tv.settings.util;
/** {@hide} */
public interface IActivityTransitionBitmapProvider extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.android.tv.settings.util.IActivityTransitionBitmapProvider
{
private static final java.lang.String DESCRIPTOR = "com.android.tv.settings.util.IActivityTransitionBitmapProvider";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.android.tv.settings.util.IActivityTransitionBitmapProvider interface,
 * generating a proxy if needed.
 */
public static com.android.tv.settings.util.IActivityTransitionBitmapProvider asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.android.tv.settings.util.IActivityTransitionBitmapProvider))) {
return ((com.android.tv.settings.util.IActivityTransitionBitmapProvider)iin);
}
return new com.android.tv.settings.util.IActivityTransitionBitmapProvider.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_getTransitionBitmap:
{
data.enforceInterface(DESCRIPTOR);
android.graphics.Bitmap _result = this.getTransitionBitmap();
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.android.tv.settings.util.IActivityTransitionBitmapProvider
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public android.graphics.Bitmap getTransitionBitmap() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
android.graphics.Bitmap _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getTransitionBitmap, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = android.graphics.Bitmap.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_getTransitionBitmap = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public android.graphics.Bitmap getTransitionBitmap() throws android.os.RemoteException;
}
