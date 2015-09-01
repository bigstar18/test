package test.android.webviewtest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

/***
 * 支持js弹框
 */
public class TestWebChromeClient extends WebChromeClient {
	private MainActivity activity;

	public TestWebChromeClient(MainActivity context) {
		super();
		this.activity = context;
	}

	// For Android 3.0+
	public void openFileChooser(ValueCallback<Uri> chooser, String acceptType) {
		// if (mUploadMessage != null)
		// return;
		// mUploadMessage = chooser;
		activity.selectImage(chooser);// can be interface
	}

	// For Android < 3.0
	public void openFileChooser(ValueCallback<Uri> chooser) {
		openFileChooser(chooser, "");
	}

	// For Android > 4.1.1
	public void openFileChooser(ValueCallback<Uri> chooser, String acceptType,
			String capture) {
		openFileChooser(chooser, acceptType);
	}

	// 支持js的弹框
	@Override
	public boolean onJsAlert(WebView view, String url, String message,
			final JsResult result) {
		AlertDialog.Builder b2 = new AlertDialog.Builder(activity).setMessage(
				message).setPositiveButton("ok",
				new AlertDialog.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						result.confirm();
						// MyWebView.this.finish();
					}
				});

		b2.setCancelable(false);
		b2.create();
		b2.show();
		return true;
	}

}
