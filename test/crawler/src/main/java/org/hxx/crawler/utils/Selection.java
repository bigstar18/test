package org.hxx.crawler.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Selection {
	private double g_numHave;// ʵ��ӵ�л�������
	private double g_numBuy;// ����������
	private String g_SucRate;// ��ǩ��
	private String g_NumDecimal;
	private int numSuc = 0;
	private List<String> g_listEndNum = new ArrayList<String>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Selection s = new Selection();
		List<String> temp = s.fnMainSelection(97350000, 1586324000);
		s.Matching();
		s.AdjustmentNum();
		for (int i = 0; i < temp.size(); i++) {
			System.out.println(temp.get(i));
		}
		System.out.println("β�Ÿ�����" + temp.size());
		System.out.println("ƥ�������" + s.numSuc);
		/*
		 * String str = "";
		 * int numSuc1=0;
		 * for(int j=0;j<String.valueOf((int)s.g_numBuy).length();j++){
		 * str+="0";
		 * }
		 * DecimalFormat df = new DecimalFormat(str);
		 * for(int j=0;j<s.g_listEndNum.size();j++){
		 * for(int i=0;i<=(int)s.g_numBuy;i++){
		 * String stri =df.format(i);
		 * if(stri.endsWith(s.g_listEndNum.get(j))){
		 * numSuc1++;
		 * }
		 * }
		 * }
		 * System.out.println("ƥ�������"+numSuc1);
		 */
	}

	// ����ӿ�
	private List<String> fnMainSelection(double g_numHave, double g_numBuy) {
		this.g_numHave = g_numHave;
		this.g_numBuy = g_numBuy;
		fnGetSucRate();
		fnSplitSucRate();
		return g_listEndNum;
	}

	// ƥ��
	private void Matching() {
		String str = "";
		for (int j = 0; j < String.valueOf((int) g_numBuy).length(); j++) {
			str += "0";
		}
		DecimalFormat df = new DecimalFormat(str);
		for (int j = 0; j < g_listEndNum.size(); j++) {
			String strTemp = g_listEndNum.get(j);
			for (int i = Integer.parseInt(strTemp); i <= (int) g_numBuy; i = i + (int) Math.pow(10, strTemp.length())) {
				String stri = df.format(i);
				if (stri.endsWith(g_listEndNum.get(j))) {
					numSuc++;
				}
			}
		}
		// AdjustmentNum(df);
	}

	// ���벹ȫ
	private void AdjustmentNum() {
		while (numSuc < g_numHave) {
			// for(int i=0;i<g_numHave-numSuc;i++){
			Random rd = new Random();
			int iTemp = Integer.parseInt(String.valueOf((int) g_numBuy).substring(1, String.valueOf((int) g_numBuy).length()));
			String strEndNum = String.valueOf(rd.nextInt(((int) g_numBuy - iTemp)) + iTemp);
			while (!DeleSame(strEndNum)) {
				strEndNum = String.valueOf(rd.nextInt(((int) g_numBuy - iTemp)) + iTemp);
			}
			g_listEndNum.add(strEndNum);
			numSuc++;
			// }
		}
	}

	// �����ǩ��
	private void fnGetSucRate() {
		double result = g_numHave / g_numBuy;
		int iTemp = String.valueOf((int) g_numBuy).length();
		String strZ = "";
		for (int i = 0; i < iTemp - 2; i++) {
			strZ += "0";
		}
		strZ = "0" + "." + strZ;

		DecimalFormat df = new DecimalFormat(strZ);
		g_SucRate = df.format(result);
		String[] a_splitSucRate = g_SucRate.split("\\.");
		g_NumDecimal = a_splitSucRate[1];
	}

	// ȡλ(�����ǩ��) ��������Ӧ��β��
	private void fnSplitSucRate() {
		for (int i = 0; i < String.valueOf((int) g_numBuy).length() - 2; i++) {
			String m_Decimal = g_NumDecimal.substring(i, i + 1);
			if (!m_Decimal.equals("0")) {
				int m_num = (int) Math.pow(10, i + 1);
				fnGetOtherEndNum(Integer.parseInt(m_Decimal), m_num);
			}
		}
	}

	// ����
	private boolean DeleSame(String strRd) {
		if (g_listEndNum.size() != 0) {
			for (int i = 0; i < g_listEndNum.size(); i++) {
				if (strRd.endsWith(g_listEndNum.get(i))) {
					return false;
				}
			}
			return true;
		} else {
			return true;
		}
	}
	// ����ʣ��β��

	private void fnGetOtherEndNum(int m_num, int temp) {
		if (temp % m_num == 0) {
			Random rd = new Random();

			int m_result = rd.nextInt(temp);
			String zero = String.valueOf(temp).substring(1, String.valueOf(temp).length());
			DecimalFormat df = new DecimalFormat(zero);
			while (!DeleSame(String.valueOf(m_result))) {
				m_result = rd.nextInt(temp);
			}
			for (int i = 0; i < m_num; i++) {
				int endNum = (m_result + i * (temp / m_num)) % temp;
				if (!g_listEndNum.contains(df.format(endNum))) {
					g_listEndNum.add(df.format(endNum));
				}
			}
		} else {
			fnSplitNum(m_num, temp);
		}
	}

	private void fnSplitNum(int m_num, int temp) {
		if (m_num == 3) {
			fnGetOtherEndNum(1, temp);
			fnGetOtherEndNum(2, temp);
		} else if (m_num == 4) {
			fnGetOtherEndNum(2, temp);
			fnGetOtherEndNum(2, temp);
		} else if (m_num > 5) {
			fnGetOtherEndNum(5, temp);
			fnGetOtherEndNum(m_num % 5, temp);
		}
	}

}
