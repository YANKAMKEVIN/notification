// ITVInputCommonAidl.aidl
package com.tif.tvinput.aidl;

// Declare any non-default types here with import statements
import com.tif.tvinput.aidl.ITVInputCommonCallbackAidl;

interface ITVInputCommonAidl
{
    // parameter strType, such as:TeleText
    // parameter strJson, such as:{"key":"id", "value":"eng"}
    void setInfo(String strType, String strJson);

    // parameter strType, such as:TeleText
    // return value json, such as:{"returncode":"0","value":"running"}
    String getInfo(String strType, String strJson);

    void registerCallback(ITVInputCommonCallbackAidl callback);
    void unRegisterCallback(ITVInputCommonCallbackAidl callback);
}