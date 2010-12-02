package com.niothiel.simplesms.ui;

import java.util.Date;

import com.niothiel.simplesms.R;
import com.niothiel.simplesms.data.Conversation;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class ConversationListItem extends android.widget.RelativeLayout {
	private TextView mFromView;
	private TextView mSubjectView;
	private TextView mDateView;
	private View mErrorIndicator;
	
	public ConversationListItem(Context context) {
		super(context);
		
	}
	
	public ConversationListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
	
	@Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mFromView = (TextView) findViewById(R.id.from);
        mSubjectView = (TextView) findViewById(R.id.subject);
        mDateView = (TextView) findViewById(R.id.date);
        mErrorIndicator = findViewById(R.id.error);
    }
	
	public void bind(String from, String explain) {
		mFromView.setText(from);
		mSubjectView.setText(explain);
	}
	
	public void bind(Conversation c) {
		mFromView.setText(c.getContact().getName() + " (" + c.getMsgCount() + ")");
		
		Date date = new Date(c.getDate());
		String dateString = date.toLocaleString();
		mDateView.setText(dateString);
		
		if(c.isRead())
			setBackgroundColor(0xff000000);
		else
			setBackgroundColor(0xff000030);
	}
}
