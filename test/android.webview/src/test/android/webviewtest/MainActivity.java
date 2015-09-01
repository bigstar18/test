package test.android.webviewtest;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	public static final int FILECHOOSER_RESULTCODE = 1;
	public static final int REQ_CAMERA = FILECHOOSER_RESULTCODE + 1;
	public static final int REQ_CHOOSE = REQ_CAMERA + 1;
	public static String basePath = Environment.getExternalStorageDirectory()
			.getPath() + File.separator + "uyoung/";

	private String url = "http://uyoungweb.chinacloudapp.cn:8888/event/static/upload.html";
	private WebView webView = null;

	private ValueCallback<Uri> chooser;
	private String imagePaths;
	private Uri cameraUri;

	@SuppressLint("JavascriptInterface")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		new File(basePath).mkdirs();

		webView = (WebView) findViewById(R.id.webview);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.addJavascriptInterface(this, "zoe");

		// contentView.loadUrl("file:///android_asset/web.html");
		webView.loadUrl(url);
		webView.setWebChromeClient(new TestWebChromeClient(this));

		bindButton();
	}

	private void bindButton() {
		Button button1 = (Button) findViewById(R.id.button1);
		Button button2 = (Button) findViewById(R.id.button2);

		button1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				webView.loadUrl("javascript:fun1()");
			}
		});
		button2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				webView.loadUrl("javascript:fun2('参数')");
				webView.loadUrl("javascript:getUserInfo('dd','ff','ee')");
			}
		});
	}

	/**
	 * 返回文件选择
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		// if (requestCode == FILECHOOSER_RESULTCODE) {
		// if (null == mUploadMessage)
		// return;
		// Uri result = intent == null || resultCode != RESULT_OK ? null
		// : intent.getData();
		// mUploadMessage.onReceiveValue(result);
		// mUploadMessage = null;
		// }

		if (null == chooser)
			return;

		Uri uri = null;
		if (requestCode == REQ_CAMERA) {
			afterOpenCamera();
			uri = cameraUri;
		} else if (requestCode == REQ_CHOOSE) {
			uri = afterChosePic(intent);
		}
		chooser.onReceiveValue(uri);
		chooser = null;

		super.onActivityResult(requestCode, resultCode, intent);
	}

	protected final void selectImage(ValueCallback<Uri> selector) {
		if (!SysUtils.checkSDcard()) {
			Toast.makeText(this, "请插入手机存储卡再使用本功能", Toast.LENGTH_SHORT).show();
			return;
		}

		this.chooser = selector;
		String[] selectPicTypeStr = { "camera", "photo" };
		new AlertDialog.Builder(this).setItems(selectPicTypeStr,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						// 相机拍摄
						case 0:
							openCarcme();
							break;
						// 手机相册
						case 1:
							chosePic();
							break;
						default:
							break;
						}
					}
				}).show();
	}

	/**
	 * 打开照相机
	 */
	private void openCarcme() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		imagePaths = basePath + (System.currentTimeMillis() + ".jpg");
		// 必须确保文件夹路径存在，否则拍照后无法完成回调
		File vFile = new File(imagePaths);
		if (vFile.exists()) {
			vFile.delete();
		}
		cameraUri = Uri.fromFile(vFile);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
		startActivityForResult(intent, REQ_CAMERA);
	}

	/**
	 * 本地相册选择图片
	 */
	private void chosePic() {
		// FileUtils.delFile(compressPath);
		Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT); // "android.intent.action.GET_CONTENT"
		String IMAGE_UNSPECIFIED = "image/*";
		innerIntent.setType(IMAGE_UNSPECIFIED); // 查看类型
		Intent wrapperIntent = Intent.createChooser(innerIntent, null);
		startActivityForResult(wrapperIntent, REQ_CHOOSE);
	}

	/**
	 * 拍照结束后
	 */
	private void afterOpenCamera() {
		File f = new File(imagePaths);
		addImageGallery(f);
		// File newFile = FileUtils.compressFile(f.getPath(), compressPath);
	}

	/** 解决拍照后在相册中找不到的问题 */
	private void addImageGallery(File file) {
		ContentValues values = new ContentValues();
		values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
		values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
		getContentResolver().insert(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
	}

	/**
	 * 选择照片后结束
	 * 
	 * @param data
	 */
	private Uri afterChosePic(Intent data) {
		// 获取图片的路径：
		String[] proj = { MediaStore.Images.Media.DATA };
		// 好像是android多媒体数据库的封装接口，具体的看Android文档
		// Cursor cursor = managedQuery(data.getData(), proj, null, null, null);
		if (data == null)
			return null;
		Cursor cursor = (new CursorLoader(this, data.getData(), proj, null,
				null, null)).loadInBackground();
		if (cursor == null) {
			// Toast.makeText(this, "上传的图片仅支持png或jpg格式", Toast.LENGTH_SHORT)
			// .show();
			return null;
		}
		// 按我个人理解 这个是获得用户选择的图片的索引值
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		// 将光标移至开头 ，这个很重要，不小心很容易引起越界
		cursor.moveToFirst();
		// 最后根据索引值获取图片路径
		String path = cursor.getString(column_index);
		if (path != null
				&& (path.endsWith(".png") || path.endsWith(".PNG")
						|| path.endsWith(".jpg") || path.endsWith(".JPG"))) {
			// File newFile = FileUtils.compressFile(path, compressPath);
			return Uri.fromFile(new File(path));
		} else {
			Toast.makeText(this, "上传的图片仅支持png或jpg格式", Toast.LENGTH_SHORT)
					.show();
		}
		return null;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
			webView.goBack();
			return true;
		} else {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	public void startFunction() {
		Toast.makeText(this, "js调用了java函数", Toast.LENGTH_SHORT).show();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
			}
		});
	}

	public void startFunction(final String str) {
		Toast.makeText(this, "js调用了java函数，并传参：" + str, Toast.LENGTH_SHORT)
				.show();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
			}
		});
	}
}
