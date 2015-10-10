/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.adr.bigdata.search.handler.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.queryparser.classic.QueryParser;

/**
 *
 * @author Tong Hoang Anh
 */
public class StringUtils {

    public final static Pattern SKU_PATTERN = Pattern.compile("\\s{0,}[sS][kK][uU]\\s{0,}:.*");
    private final static String OPEN_BRACKET = "[";
    private final static String CLOSE_BRACKET = "]";

    public static boolean searchBySku(String keyword) {
        Matcher m = SKU_PATTERN.matcher(keyword);
        return m.matches();
    }
    
    public static String rangePrice(String price){
        return new StringBuilder().append(OPEN_BRACKET).append(price).append(CLOSE_BRACKET).toString();
    }

    /**
     * @quangvh if string contain ":", we extract SKU. This string must not
     * repair else then keyword must repair
     *
     * So we need repair those lines
     */
    public static String extractSku(String keyword) {
        int index = keyword.indexOf(":");
        if (index > 0) {
            return keyword.substring(index + 1); // SKU that we need extract
        } else {
            return keyword; //keyword that we need repair
        }
    }

    /**
     * Normalize string by quangvh
     *
     * @quangvh We need repair those special characters
     *
     * @param keyword
     * @return
     */
    public static String nomalizationStringSearch(String keyword) {
        keyword = keyword.trim();
        keyword = keyword.toLowerCase();
        keyword = keyword.replaceAll("\\:", "\\\\:");
        keyword = keyword.replaceAll("\\^", "\\\\^");
        keyword = keyword.replaceAll("\\(", "\\\\(");
        keyword = keyword.replaceAll("\\)", "\\\\)");
        keyword = keyword.replaceAll("\\[", "\\\\[");
        keyword = keyword.replaceAll("\\]", "\\\\]");
        keyword = keyword.replaceAll("\\{", "\\\\{");
        keyword = keyword.replaceAll("\\}", "\\\\}");
        keyword = keyword.replaceAll("\\\"", "\\\\\"");
        keyword = keyword.replaceAll("\\/", "\\\\/");
        keyword = keyword.replaceAll("\\~", "\\\\~");
        keyword = keyword.replaceAll("\\!", "\\\\!");

        keyword = keyword.replaceAll("\\|\\|", "\\\\|\\|");
        keyword = keyword.replaceAll("\\&\\&", "\\\\&\\&");
        keyword = keyword.replaceAll("\\+", "\\\\+");
        keyword = keyword.replaceAll("\\-", "\\\\-");
        keyword = keyword.replaceAll("\\*", "\\\\*");
        keyword = keyword.replaceAll("\\?", "\\\\?");
        return keyword;
    }
    
    public static String queryExact(String keyword){ 
        return QueryParser.escape("\"" + keyword+ "\""); 
    } 
    
    public static String formatNegative(String negavetiveInput){ 
        return QueryParser.escape(negavetiveInput); 
    } 

    public static int atoi(String s)
            throws NumberFormatException {
        if ((s == null) || (s.length() < 1)) {
            throw new IllegalArgumentException("Can't convert empty string to integer");
        }
        if (s.charAt(0) == '+') {
            s = s.substring(1);
        }
        return Integer.parseInt(s);
    }
}
