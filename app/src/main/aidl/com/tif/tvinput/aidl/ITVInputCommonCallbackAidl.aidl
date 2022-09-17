// ITVInputCommonCallbackAidl.aidl
package com.tif.tvinput.aidl;

// Declare any non-default types here with import statements

interface ITVInputCommonCallbackAidl
{
    // strType, such as:TeleText
    // strJson, such as:{"state":"finish"}
    void notifyResultInfo(String strType, String strJson);
}