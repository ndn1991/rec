package com.adr.bigdata.search.handler.utils;

public class ConvertStringToString {
	public static String reviseStringFromSQL(String str) {
		if (str == null)
			return "";
		str = str.replaceAll("\\s+", " ");
		return str.trim();
	}

	private static final char sac = '́';
	private static final char huyen = '̀';
	private static final char nang = '̣';
	private static final char hoi = '̉';
	private static final char nga = '̃';

	public static final String decodeSumaryToNormal(String str) {
		char input[] = str.toCharArray();
		char[] output = new char[input.length];
		int count = 0;
		for (int i = 0; i < input.length; i++) {
			char c = input[i];
			char pre;
			switch (c) {
			case sac:
				if (i > 0) {
					pre = input[i - 1];
					switch (pre) {
					case 'a':
						output[count - 1] = 'á';
						break;
					case 'ă':
						output[count - 1] = 'ắ';
						break;
					case 'â':
						output[count - 1] = 'ấ';
						break;
					case 'e':
						output[count - 1] = 'é';
						break;
					case 'ê':
						output[count - 1] = 'ế';
						break;
					case 'i':
						output[count - 1] = 'í';
						break;
					case 'o':
						output[count - 1] = 'ó';
						break;
					case 'ơ':
						output[count - 1] = 'ớ';
						break;
					case 'ô':
						output[count - 1] = 'ố';
						break;
					case 'u':
						output[count - 1] = 'ú';
						break;
					case 'ư':
						output[count - 1] = 'ứ';
						break;
					case 'y':
						output[count - 1] = 'ý';
						break;
					case 'A':
						output[count - 1] = 'Á';
						break;
					case 'Ă':
						output[count - 1] = 'Ắ';
						break;
					case 'Â':
						output[count - 1] = 'Ấ';
						break;
					case 'E':
						output[count - 1] = 'É';
						break;
					case 'Ê':
						output[count - 1] = 'Ế';
						break;
					case 'I':
						output[count - 1] = 'Í';
						break;
					case 'O':
						output[count - 1] = 'Ó';
						break;
					case 'Ơ':
						output[count - 1] = 'Ớ';
						break;
					case 'Ô':
						output[count - 1] = 'Ố';
						break;
					case 'U':
						output[count - 1] = 'Ú';
						break;
					case 'Ư':
						output[count - 1] = 'Ứ';
						break;
					case 'Y':
						output[count - 1] = 'Ý';
						break;
					}
				}
				break;
			case huyen:
				if (i > 0) {
					pre = input[i - 1];
					switch (pre) {
					case 'a':
						output[count - 1] = 'à';
						break;
					case 'ă':
						output[count - 1] = 'ằ';
						break;
					case 'â':
						output[count - 1] = 'ầ';
						break;
					case 'e':
						output[count - 1] = 'è';
						break;
					case 'ê':
						output[count - 1] = 'ề';
						break;
					case 'i':
						output[count - 1] = 'ì';
						break;
					case 'o':
						output[count - 1] = 'ò';
						break;
					case 'ơ':
						output[count - 1] = 'ờ';
						break;
					case 'ô':
						output[count - 1] = 'ồ';
						break;
					case 'u':
						output[count - 1] = 'ù';
						break;
					case 'ư':
						output[count - 1] = 'ừ';
						break;
					case 'y':
						output[count - 1] = 'ỳ';
						break;
					case 'A':
						output[count - 1] = 'À';
						break;
					case 'Ă':
						output[count - 1] = 'Ằ';
						break;
					case 'Â':
						output[count - 1] = 'Ầ';
						break;
					case 'E':
						output[count - 1] = 'È';
						break;
					case 'Ê':
						output[count - 1] = 'Ề';
						break;
					case 'I':
						output[count - 1] = 'Ì';
						break;
					case 'O':
						output[count - 1] = 'Ò';
						break;
					case 'Ơ':
						output[count - 1] = 'Ờ';
						break;
					case 'Ô':
						output[count - 1] = 'Ồ';
						break;
					case 'U':
						output[count - 1] = 'Ù';
						break;
					case 'Ư':
						output[count - 1] = 'Ừ';
						break;
					case 'Y':
						output[count - 1] = 'Ỳ';
						break;
					}
				}
				break;
			case nang:
				if (i > 0) {
					pre = input[i - 1];
					switch (pre) {
					case 'a':
						output[count - 1] = 'ạ';
						break;
					case 'ă':
						output[count - 1] = 'ặ';
						break;
					case 'â':
						output[count - 1] = 'ậ';
						break;
					case 'e':
						output[count - 1] = 'ẹ';
						break;
					case 'ê':
						output[count - 1] = 'ệ';
						break;
					case 'i':
						output[count - 1] = 'ị';
						break;
					case 'o':
						output[count - 1] = 'ọ';
						break;
					case 'ơ':
						output[count - 1] = 'ợ';
						break;
					case 'ô':
						output[count - 1] = 'ộ';
						break;
					case 'u':
						output[count - 1] = 'ụ';
						break;
					case 'ư':
						output[count - 1] = 'ự';
						break;
					case 'y':
						output[count - 1] = 'ỵ';
						break;
					case 'A':
						output[count - 1] = 'Ạ';
						break;
					case 'Ă':
						output[count - 1] = 'Ặ';
						break;
					case 'Â':
						output[count - 1] = 'Ậ';
						break;
					case 'E':
						output[count - 1] = 'Ẹ';
						break;
					case 'Ê':
						output[count - 1] = 'Ệ';
						break;
					case 'I':
						output[count - 1] = 'Ị';
						break;
					case 'O':
						output[count - 1] = 'Ọ';
						break;
					case 'Ơ':
						output[count - 1] = 'Ợ';
						break;
					case 'Ô':
						output[count - 1] = 'Ộ';
						break;
					case 'U':
						output[count - 1] = 'Ụ';
						break;
					case 'Ư':
						output[count - 1] = 'Ự';
						break;
					case 'Y':
						output[count - 1] = 'Ỵ';
						break;
					}
				}
				break;
			case hoi:
				if (i > 0) {
					pre = input[i - 1];
					switch (pre) {
					case 'a':
						output[count - 1] = 'ả';
						break;
					case 'ă':
						output[count - 1] = 'ẳ';
						break;
					case 'â':
						output[count - 1] = 'ẩ';
						break;
					case 'e':
						output[count - 1] = 'ẻ';
						break;
					case 'ê':
						output[count - 1] = 'ể';
						break;
					case 'i':
						output[count - 1] = 'ỉ';
						break;
					case 'o':
						output[count - 1] = 'ỏ';
						break;
					case 'ơ':
						output[count - 1] = 'ở';
						break;
					case 'ô':
						output[count - 1] = 'ổ';
						break;
					case 'u':
						output[count - 1] = 'ủ';
						break;
					case 'ư':
						output[count - 1] = 'ử';
						break;
					case 'y':
						output[count - 1] = 'ỷ';
						break;
					case 'A':
						output[count - 1] = 'Ả';
						break;
					case 'Ă':
						output[count - 1] = 'Ẳ';
						break;
					case 'Â':
						output[count - 1] = 'Ẩ';
						break;
					case 'E':
						output[count - 1] = 'Ẻ';
						break;
					case 'Ê':
						output[count - 1] = 'Ể';
						break;
					case 'I':
						output[count - 1] = 'Ỉ';
						break;
					case 'O':
						output[count - 1] = 'Ỏ';
						break;
					case 'Ơ':
						output[count - 1] = 'Ở';
						break;
					case 'Ô':
						output[count - 1] = 'Ổ';
						break;
					case 'U':
						output[count - 1] = 'Ủ';
						break;
					case 'Ư':
						output[count - 1] = 'Ử';
						break;
					case 'Y':
						output[count - 1] = 'Ỷ';
						break;
					}
				}
				break;
			case nga:
				if (i > 0) {
					pre = input[i - 1];
					switch (pre) {
					case 'a':
						output[count - 1] = 'ã';
						break;
					case 'ă':
						output[count - 1] = 'ẵ';
						break;
					case 'â':
						output[count - 1] = 'ẫ';
						break;
					case 'e':
						output[count - 1] = 'ẽ';
						break;
					case 'ê':
						output[count - 1] = 'ễ';
						break;
					case 'i':
						output[count - 1] = 'ĩ';
						break;
					case 'o':
						output[count - 1] = 'õ';
						break;
					case 'ơ':
						output[count - 1] = 'ỡ';
						break;
					case 'ô':
						output[count - 1] = 'ỗ';
						break;
					case 'u':
						output[count - 1] = 'ũ';
						break;
					case 'ư':
						output[count - 1] = 'ữ';
						break;
					case 'y':
						output[count - 1] = 'ỹ';
						break;
					case 'A':
						output[count - 1] = 'Ã';
						break;
					case 'Ă':
						output[count - 1] = 'Ẵ';
						break;
					case 'Â':
						output[count - 1] = 'Ẫ';
						break;
					case 'E':
						output[count - 1] = 'Ẽ';
						break;
					case 'Ê':
						output[count - 1] = 'Ễ';
						break;
					case 'I':
						output[count - 1] = 'Ĩ';
						break;
					case 'O':
						output[count - 1] = 'Õ';
						break;
					case 'Ơ':
						output[count - 1] = 'Ỡ';
						break;
					case 'Ô':
						output[count - 1] = 'Ỗ';
						break;
					case 'U':
						output[count - 1] = 'Ũ';
						break;
					case 'Ư':
						output[count - 1] = 'Ữ';
						break;
					case 'Y':
						output[count - 1] = 'Ỹ';
						break;
					}
				}
				break;
			default:
				output[count] = input[i];
				count++;
				break;
			}
		}
		char[] ret = new char[count];
		System.arraycopy(output, 0, ret, 0, count);
		return new String(ret);
	}

}