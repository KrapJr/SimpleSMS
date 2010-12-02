package com.niothiel.simplesms.ui;

import com.niothiel.simplesms.R;
import com.niothiel.simplesms.SMSApp;
import com.niothiel.simplesms.data.Contact;
import com.niothiel.simplesms.data.Conversation;
import com.niothiel.simplesms.store.ContactStore;
import com.niothiel.simplesms.store.ConversationStore;
import com.niothiel.simplesms.store.MessageStore;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ComposeMessageActivity extends Activity {
	private ListView mHistory;
	private TextView mSubject;
	private TextView mTextEditor;
	private Button mSendButton;
	private BroadcastReceiver mReceiver;
	private IntentFilter mFilter;
	
	private MessageStore mStore;
	private Conversation mConv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.compose_message_activity);
		
		// Initialize GUI elements.
		mHistory = (ListView) findViewById(R.id.history);
		mSubject = (TextView) findViewById(R.id.subject);
		mTextEditor = (TextView) findViewById(R.id.text_editor);
		mSendButton = (Button) findViewById(R.id.send_button);
		mSendButton.setOnClickListener(new SendListener(this));
		
		Intent i = getIntent();
		if(i.hasExtra("thread_id")) {
			long threadId = i.getLongExtra("thread_id", -1);
			String name = i.getStringExtra("name");
			String number = i.getStringExtra("number");
			
			Contact c = new Contact(-1, name, number);
			mConv = new Conversation();
			mConv.setContact(c);
			mConv.setThreadId(threadId);
			
			// TODO: Need to remove the name parameter.
			mStore = new MessageStore(mConv.getThreadId(), mConv.getContact().getName());
			mStore.bindView(mHistory);
		}
		
		// TODO: Again, change this to a ContentObserver approach
		mReceiver = new ComposeMessageReceiver();
		mFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		mFilter.setPriority(-1000);
		
		updateTitleBar();
	}
	
	@Override
	protected void onPause() {
		Log.d("SimpleSMS", "Called onPause()!");
		unregisterReceiver(mReceiver);
		ContactStore.exportCache();
		super.onPause();
	}

	@Override
	protected void onResume() {
		Log.d("SimpleSMS", "Called onResume()!");
		if(mStore != null)
			mStore.requery();
		registerReceiver(mReceiver, mFilter);
		super.onResume();
	}
	
	private class ComposeMessageReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
			Log.d("ComposeMessageActivity", "Requerying due to message received.");
			if(mStore != null)
				mStore.requery();
		}
	}
	
	private class SendListener implements OnClickListener {
		private Context mContext;
		
		public SendListener(Context c) {
			mContext = c;
		}
		
		@Override
		public void onClick(View v) {
			CharSequence text = mTextEditor.getText();
			if(text.length() == 0)
				return;
			mTextEditor.setText("");
			
			if(mConv == null) {
				Contact c = ContactStore.getByNumber(mSubject.getText().toString());
				long threadId = ConversationStore.getOrCreateThreadId(c.getNumber());
				mConv = new Conversation();
				mConv.setContact(c);
				mConv.setThreadId(threadId);
			}
			sendMessage(text.toString());
			if(mStore == null) {
				mStore = new MessageStore(mConv.getThreadId(), mConv.getContact().getName());
				mStore.bindView(mHistory);
			}
			mStore.requery();
			updateTitleBar();
		}
	}
	
	private void sendMessage(String message) {
		// Send the message
		ContentValues values = new ContentValues(7);
		values.put("address", mConv.getContact().getNumber());
		values.put("read", false);
		values.put("subject", "");
		values.put("body", message);
		values.put("thread_id", mConv.getThreadId());
		values.put("type", 2);
		
		Uri uri = Uri.parse("content://sms/outbox");
		getContentResolver().insert(uri, values);
		
		Toast.makeText(SMSApp.getContext(), "Sending message: " + message, Toast.LENGTH_SHORT).show();
		// TODO: Handle undelivered messages, etc.
		SmsManager.getDefault().sendTextMessage(mConv.getContact().getNumber(),
				null,
				message.toString(),
				null,
				null);
	}
	
	private void updateTitleBar() {
		if(mConv == null) {
			mSubject.setVisibility(View.VISIBLE);
		}
		else {
			setTitle(mConv.getContact().getFormatted());
			mSubject.setVisibility(View.GONE);
		}
	}
}