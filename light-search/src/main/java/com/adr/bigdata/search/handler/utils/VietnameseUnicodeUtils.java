package com.adr.bigdata.search.handler.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.api.client.util.Strings;

public class VietnameseUnicodeUtils {
	
	public static final int VIET = 0;
	public static final int VNI = 2;
	public static final int VPS = 4;
	public static final int VISCII = 6;
	public static final int TCVN3 = 8;
	public static final int VIQR = 10;
	public static final int DESCRIPTION = 11;
	
	public static void main(String[] args) throws IOException{
		String source = "http://vietunicode.sourceforge.net/charset/";
		
		Map<String, String> charMapper = new HashMap<String, String>();
		
		Element table = Jsoup.connect(source).execute().parse().select("table").get(0);
		
		Elements rows = table.select("tr");
		
		for (int i = 2; i < rows.size(); i++) {
			Element row = rows.get(i);
			Elements cols = row.select("td");
			String viet = cols.get(VIET).text();
//			if(!Strings.isNullOrEmpty(cols.get(VNI).text())){
//				charMapper.put(cols.get(VNI).text(), viet);
//			}
//			if(!Strings.isNullOrEmpty(cols.get(VPS).text())){
//				charMapper.put(cols.get(VPS).text(), viet);
//			}
//			if(!Strings.isNullOrEmpty(cols.get(VISCII).text())){
//				charMapper.put(cols.get(VISCII).text(), viet);
//			}
//			if(!Strings.isNullOrEmpty(cols.get(TCVN3).text())){
//				charMapper.put(cols.get(TCVN3).text(), viet);
//			}
			if(!Strings.isNullOrEmpty(cols.get(VIQR).text())){
				charMapper.put(cols.get(VIQR).text(), viet);
			}
//			
//			Mapper mapper = new Mapper(viet);
//			mapper.add(cols.get(VNI).text());
//			mapper.add(cols.get(VPS).text());
//			mapper.add(cols.get(VISCII).text());
//			mapper.add(cols.get(TCVN3).text());
//			mapper.add(cols.get(VIQR).text());
//			mapper.setDescription(cols.get(DESCRIPTION).text());
//			System.out.println(mapper.formattedString());
		}
		
		for(Map.Entry<String, String> entry: charMapper.entrySet()){
			System.out.println("\"" + entry.getKey() + "\" => \"" + entry.getValue() + "\"");
		}
		
	}
	
	static class Mapper{
		String viet;
		List<String> others;
		String description;
		
		public Mapper(String viet){
			this.viet = viet;
			others = new ArrayList<String>();
		}
		
		public void add(String other){
			if(viet.equals(other)) return;
			this.others.add(other);
		}
		
		public void setDescription(String description){
			this.description = description;
		}
		
		public String formattedString(){
			StringBuilder sb = new StringBuilder();
			sb.append("#").append(description).append("\n");
			for(String other: this.others){
				sb.append("\"").append(other).append("\"").append(" => ").append("\"").append(viet).append("\"\n");
			}
			
			return sb.toString();
		}
	}
}
