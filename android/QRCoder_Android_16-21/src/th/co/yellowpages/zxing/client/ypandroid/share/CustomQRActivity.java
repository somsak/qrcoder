package th.co.yellowpages.zxing.client.ypandroid.share;

import th.co.yellowpages.zxing.client.ypandroid.Contents;
import th.co.yellowpages.zxing.client.ypandroid.Intents;
import th.co.yellowpages.zxing.client.ypandroid.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Contacts;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class CustomQRActivity extends Activity {
	public static final String TEXT = "Text";
	public static final String CONTACT_INFORMATION = "Contact information";
	public static final String EMAIL = "Email address";
	public static final String PHONE_NUMBER = "Phone number";
	public static final String URL = "URL";

	private Spinner spinner;
	private String[] qrType = { CustomQRActivity.TEXT,
			CustomQRActivity.CONTACT_INFORMATION, CustomQRActivity.EMAIL,
			CustomQRActivity.PHONE_NUMBER, CustomQRActivity.URL };

	private View contactView;
	private View textView;
	private View emailView;
	private View phoneNumberView;
	private View urlView;

	// YP Manual text input to create QR Code
	private final Button.OnClickListener manualListener = new Button.OnClickListener() {
		public void onClick(View v) {
			showTextAsQRCode();
		}
	};

	/**
	 * @see android.app.Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.yp_customqr);

		spinner = (Spinner) findViewById(R.id.yp_qr_type_spinner);

		prepareQRTypeSpinner();
		contactView = findViewById(R.id.yp_customqr_contact_layout);
		textView = findViewById(R.id.yp_customqr_text_layout);
		emailView = findViewById(R.id.yp_customqr_email_layout);
		phoneNumberView = findViewById(R.id.yp_customqr_phone_number_layout);
		urlView = findViewById(R.id.yp_customqr_url_layout);

		findViewById(R.id.yp_createqr_button)
				.setOnClickListener(manualListener);
	}

	private void prepareQRTypeSpinner() {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, qrType);

		spinner.setAdapter(adapter);
		spinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						hideAllLayout();

						if (selectedType(CustomQRActivity.CONTACT_INFORMATION)) {
							contactView.setVisibility(View.VISIBLE);
						} else if (selectedType(CustomQRActivity.TEXT)) {
							textView.setVisibility(View.VISIBLE);
						} else if (selectedType(CustomQRActivity.PHONE_NUMBER)) {
							phoneNumberView.setVisibility(View.VISIBLE);
						} else if (selectedType(CustomQRActivity.EMAIL)) {
							emailView.setVisibility(View.VISIBLE);
						} else if (selectedType(CustomQRActivity.URL)) {
							urlView.setVisibility(View.VISIBLE);
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}

				});
	}

	private void showTextAsQRCode() {
		Intent intent = new Intent(Intents.Encode.ACTION);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

		String text = null;

		if (selectedType(CustomQRActivity.CONTACT_INFORMATION)) {
			Bundle bundle = new Bundle();

			String name = getEditTextTextById(R.id.yp_customqr_contact_name);
			String company = getEditTextTextById(R.id.yp_customqr_contact_company);
			String phoneNumber = getEditTextTextById(R.id.yp_customqr_contact_phone_number);
			String email = getEditTextTextById(R.id.yp_customqr_contact_email);
			String address = getEditTextTextById(R.id.yp_customqr_contact_address);
			String memo = getEditTextTextById(R.id.yp_customqr_contact_memo);

			bundle.putString(Contacts.Intents.Insert.NAME, name);
			bundle.putString(Contacts.Intents.Insert.COMPANY, company);
			bundle.putString(Contacts.Intents.Insert.PHONE, phoneNumber);
			bundle.putString(Contents.EMAIL_KEYS[0], email);
			bundle.putString(Contacts.Intents.Insert.POSTAL, address);
			bundle.putString(Contacts.Intents.Insert.NOTES, memo);

			intent.putExtra(Intents.Encode.TYPE, Contents.Type.CONTACT);
			intent.putExtra(Intents.Encode.DATA, bundle);
		} else if (selectedType(CustomQRActivity.TEXT)) {
			text = getEditTextTextById(R.id.yp_customqr_edittext);

			intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT);
			intent.putExtra(Intents.Encode.DATA, text);
		} else if (selectedType(CustomQRActivity.EMAIL)) {
			text = getEditTextTextById(R.id.yp_customqr_email);

			intent.putExtra(Intents.Encode.TYPE, Contents.Type.EMAIL);
			intent.putExtra(Intents.Encode.DATA, text);
		} else if (selectedType(CustomQRActivity.PHONE_NUMBER)) {
			text = getEditTextTextById(R.id.yp_customqr_phone_number);

			intent.putExtra(Intents.Encode.TYPE, Contents.Type.PHONE);
			intent.putExtra(Intents.Encode.DATA, text);
		} else if (selectedType(CustomQRActivity.URL)) {
			text = getEditTextTextById(R.id.yp_customqr_url);

			intent.putExtra(Intents.Encode.TYPE, Contents.Type.TEXT);
			intent.putExtra(Intents.Encode.DATA, text);
		}

		intent.putExtra(Intents.Encode.FORMAT, Contents.Format.QR_CODE);
		startActivity(intent);
	}

	private String getEditTextTextById(int id) {
		return ((EditText) findViewById(id)).getText().toString();
	}

	private boolean selectedType(String type) {
		int position = spinner.getSelectedItemPosition();

		return qrType[position].compareToIgnoreCase(type) == 0;
	}

	private void hideAllLayout() {
		textView.setVisibility(View.GONE);
		contactView.setVisibility(View.GONE);
		emailView.setVisibility(View.GONE);
		phoneNumberView.setVisibility(View.GONE);
		urlView.setVisibility(View.GONE);
	}
}
