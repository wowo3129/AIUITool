package com.iflytek.aiui.demo.chat.repository;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 联系人repo
 */

@Singleton
public class Contacts {
    private static final String LAST_VERSION_KEY = "contact_last_version";
    private static final String CONTACT_KEY = "contact";

    private Context mContext;

    @Inject
    public Contacts(Context context) {
        mContext = context;
    }


    /**
     * 返回通讯录联系人数据
     *
     * @return 联系人记录，格式如下::
     * 姓名$$电话号码
     */
    public List<String> getContacts() {
        List<String> contacts = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            ContentResolver cr = mContext.getContentResolver();
            Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            ArrayList<String> temp = new ArrayList<>();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                    String personName = cursor.getString(nameFieldColumnIndex);
                    String ContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    Cursor phone = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + ContactId, null, null);
                    temp.clear();

                    while(phone.moveToNext()) {
                        String phoneNumber = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phoneNumber = phoneNumber.replace("-", "");
                        phoneNumber = phoneNumber.replace(" ", "");

                        //去除重复联系号码
                        if(!temp.contains(phoneNumber)) {
                            temp.add(phoneNumber);
                            contacts.add(personName + "$$" + phoneNumber);
                        }
                    }

                    if(temp.isEmpty()) {
                        //无号码，加入空联系人
                        contacts.add(personName + "$$");
                    }

                    phone.close();
                }
                cursor.close();
            }
        }
        return contacts;
    }
}
