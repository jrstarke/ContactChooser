package com.monday.cordova;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import android.util.Log;

public class ContactChooserPlugin extends CordovaPlugin {

    private Context context;
    private CallbackContext callbackContext;

    private static final String TAG = "ContactChooser";
    
    private static final int CHOOSE_CONTACT = 1;
    
    private static final int BY_EMAIL = 1;
    private static final int BY_PHONE = 2;
	private int selectType;

	@Override
	public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
	    this.context = cordova.getActivity().getApplicationContext();

	    selectType = data.getInt(0);
	    
		if (action.equals("chooseContact")) {
			Intent intent = null;
			if (data.length() > 0 && data.getInt(0) == BY_PHONE) {
				Log.v(TAG, "ContactChooser: BY_PHONE");
				intent = new Intent(Intent.ACTION_PICK,
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
			}
			else {
				Log.v(TAG, "ContactChooser: BY_EMAIL");
				intent = new Intent(Intent.ACTION_PICK,
						ContactsContract.CommonDataKinds.Email.CONTENT_URI);
			}
            cordova.startActivityForResult(this, intent, CHOOSE_CONTACT);
            
            PluginResult r = new PluginResult(PluginResult.Status.NO_RESULT);
            r.setKeepCallback(true);
            callbackContext.sendPluginResult(r);
            return true;
		}

		return false;
	}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {

            Uri contactData = data.getData();
            String id = contactData.getLastPathSegment();
            ContentResolver resolver = context.getContentResolver();
            Cursor c =  resolver.query(contactData, null, null, null, null);

            if (c.moveToFirst()) {
                try {
                    String contactId = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID));
                    String name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                    String email = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.DATA));
                    String phoneNumber = "";
                    if (Integer.parseInt(c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        String query = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
                        Cursor phoneCursor;
                        if (this.selectType == BY_PHONE) {
                        	phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        			ContactsContract.CommonDataKinds.Phone._ID + " = ?", new String[]{id}, null);
                        }
                        else {
	                        phoneCursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
	                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
	                                new String[]{ contactId }, null);
                        }
                        phoneCursor.moveToFirst();
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
                        phoneCursor.close();
                    }

                    JSONObject contact = new JSONObject();
                    contact.put("email", email);
                    contact.put("displayName", name);
                    contact.put("phoneNumber", phoneNumber);
                    callbackContext.success(contact);

                } catch (Exception e) {
                    callbackContext.error("Parsing contact failed: " + e.getMessage());
                }

            } else {
                callbackContext.error("Contact was not available.");
            }

            c.close();

        } else if (resultCode == Activity.RESULT_CANCELED) {
            callbackContext.error("No contact was selected.");
        }
    }

}
