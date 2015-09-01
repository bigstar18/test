package test.android.webviewtest;

import android.os.Environment;

public abstract class SysUtils {

	/**
	 * 检查SD卡是否存在
	 * 
	 * @return
	 */
	public static final boolean checkSDcard() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}
}
