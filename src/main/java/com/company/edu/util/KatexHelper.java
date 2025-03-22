package com.company.edu.util;

public class KatexHelper {
    public static String render(String latex) {
        return "\\(" + latex + "\\)"; // KaTeX 인라인 수식으로 래핑
    }
}