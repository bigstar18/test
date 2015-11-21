package org.hxx.crawler.utils;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;

public abstract class Selection2 {

	// 对外接口
	public static List<String> execSelection(double ipoNum, double buyNum) {
		List<String> endNumList = new ArrayList<String>();

		if (ipoNum >= buyNum)
			return endNumList;

		String decimalNum = calSucRate(ipoNum, buyNum);
		splitSucRate(Double.valueOf(ipoNum).longValue(), Double.valueOf(buyNum).longValue(), decimalNum, endNumList);

		return endNumList;
	}

	// 获得中签率
	private static String calSucRate(double ipoNum, double buyNum) {
		long buyNumLong = Double.valueOf(buyNum).longValue();
		System.out.println("申购字符串为：" + buyNumLong);

		double result = ipoNum / buyNum;
		System.out.println("中签率是：" + result);

		int buyLen = String.valueOf(buyNumLong).length();
		String strZ = "";
		for (int i = 0; i < buyLen; i++) {
			strZ += "0";
		}
		strZ = "0" + "." + strZ;
		DecimalFormat df = new DecimalFormat(strZ);
		df.setRoundingMode(RoundingMode.FLOOR);

		String succRate = df.format(result);
		String[] tmp = succRate.split("\\.");

		System.out.println("中签率字符串是：" + tmp[1] + " 申购字符串长度为：" + (String.valueOf(buyNumLong).length()));
		return tmp[1];
	}

	// 取位(拆分中签率) 并生成相应的尾号
	private static void splitSucRate(long ipoNum, long buyNum, String decimalNum, List<String> endNumList) {
		long succCount = 0l;
		String strBuyNum = String.valueOf(buyNum);

		for (int i = 0; i < strBuyNum.length(); i++) {
			String rateNum = decimalNum.substring(i, i + 1);

			if (!rateNum.equals("0")) {
				long range = (long) Math.pow(10, i + 1);// problem?

				makeEndNum(Integer.parseInt(rateNum), range, endNumList);

				succCount += (buyNum / Math.pow(10, i + 1)) * (Integer.parseInt(rateNum));
				System.out.println("当前配号总数是：" + succCount + " 发行总数是：" + ipoNum);
			}

			if (ipoNum - succCount <= Integer.valueOf(strBuyNum.substring(0, 2)))
				break;
		}
		AdjustmentNum(succCount, ipoNum, buyNum, decimalNum, endNumList);
	}

	// 生成剩余尾号
	private static void makeEndNum(int rateNum, long range, List<String> endNumList) {
		if (range % rateNum == 0) {
			long start = range / 10;
			if (start == 1)
				start = 0;
			long result = RandomUtils.nextLong(start, range - 1);
			while (!notIncludePre(String.valueOf(result), endNumList)) {
				result = RandomUtils.nextLong(start, range - 1);
			}

			int cnt = 0;
			for (int i = 0;; i++) {
				if (cnt++ == rateNum)
					break;

				long endNum = (result + i * (range / (rateNum * 2))) % range;//
				while (String.valueOf(endNum).length() != String.valueOf(result).length()) {
					i++;
					endNum = (result + i * (range / (rateNum * 2))) % range;
				}

				String endNumStr = String.valueOf(endNum);

				if (!endNumList.contains(endNumStr)) {// never,越来越大
					endNumList.add(endNumStr);
					System.out.println("rateNum=" + rateNum + " 尾号：" + endNum + " 数长是：" + String.valueOf(range - 1).length());
					// System.out.println("result=" + result + " range=" + range);
				} else
					System.err.println("make Ballot error happend: " + endNumStr);
			}
		} else {
			if (rateNum == 3) {
				makeEndNum(1, range, endNumList);
				makeEndNum(2, range, endNumList);
			} else if (rateNum == 4) {
				makeEndNum(2, range, endNumList);
				makeEndNum(2, range, endNumList);
			} else if (rateNum > 5) {
				makeEndNum(5, range, endNumList);
				makeEndNum(rateNum % 5, range, endNumList);
			}
		}
	}

	// 排重
	private static boolean notIncludePre(String strRd, List<String> endNumList) {
		if (endNumList.size() != 0) {
			for (int i = 0; i < endNumList.size(); i++) {
				if (strRd.endsWith(endNumList.get(i))) {
					return false;
				}
			}
		}
		return true;
	}

	// 号码补全
	private static void AdjustmentNum(long succCount, long ipoNum, long buyNum, String decimalNum, List<String> endNumList) {
		while (succCount < ipoNum) {
			// int buyLen = String.valueOf(buyNum).length() - 1;
			String strEndNum = String.valueOf(RandomUtils.nextLong((buyNum / 10), buyNum));
			while (!notIncludePre(strEndNum, endNumList)) {
				strEndNum = String.valueOf(RandomUtils.nextLong((buyNum / 10), buyNum));
			}
			endNumList.add(strEndNum);
			succCount++;
			System.out.println("AdjustmentNum: 尾号：" + strEndNum);
		}
		System.out.println("AdjustmentNum: 当前配号总数是：" + succCount + " 发行总数是：" + ipoNum);
	}

	public static void main(String[] args) {
		test();
		// test2();
	}

	public static void test() {
		List<String> result = Selection2.execSelection(23450, 1005700);
		System.out.println("尾号个数：" + result.size());
	}

	public static void test2() {
		// System.out.println(158623464 / 100);

		for (int i = 0; i < 8; i++) {
			long endNum = (8974 + i * (10000 / 8)) % 10000;
			System.out.println(endNum);
		}

	}

}
