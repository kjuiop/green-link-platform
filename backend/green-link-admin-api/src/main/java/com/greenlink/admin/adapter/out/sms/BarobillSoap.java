package com.greenlink.admin.adapter.out.sms;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class BarobillSoap {

    static final String NAMESPACE = "http://ws.baroservice.com/";

    private static final Pattern ERROR_CODE = Pattern.compile("^-\\d+$");

    private BarobillSoap() {}

    static String soapAction(String method) {
        return "\"" + NAMESPACE + method + "\"";
    }

    /** params 는 WSDL 에 정의된 순서대로 전달해야 한다(LinkedHashMap). */
    static String buildEnvelope(String method, Map<String, String> params) {
        StringBuilder inner = new StringBuilder(256);
        params.forEach((name, value) -> inner.append(element(name, value)));
        return buildEnvelopeXml(method, inner.toString());
    }

    /** innerXml 은 이미 조립·이스케이프된 요청 본문(중첩 요소 포함)이다. */
    static String buildEnvelopeXml(String method, String innerXml) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                + " xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
                + " xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                + "<soap:Body><" + method + " xmlns=\"" + NAMESPACE + "\">"
                + innerXml
                + "</" + method + "></soap:Body></soap:Envelope>";
    }

    static String element(String name, String value) {
        return "<" + name + ">" + escape(value) + "</" + name + ">";
    }

    record KakaotalkButton(String name, String buttonType, String url1, String url2) {}

    record KakaotalkTemplate(String templateName, String title, String content, List<KakaotalkButton> buttons) {}

    /** GetKakaotalkTemplates 응답에서 템플릿 목록을 추출한다. 파싱할 수 없으면 빈 목록. */
    static List<KakaotalkTemplate> parseTemplates(String xml) {
        List<KakaotalkTemplate> templates = new ArrayList<>();
        if (xml == null || xml.isBlank()) return templates;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            Document doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            NodeList nodes = doc.getElementsByTagNameNS("*", "KakaotalkTemplate");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element t = (Element) nodes.item(i);
                List<KakaotalkButton> buttons = new ArrayList<>();
                NodeList btnNodes = t.getElementsByTagNameNS("*", "KakaotalkButton");
                for (int j = 0; j < btnNodes.getLength(); j++) {
                    Element b = (Element) btnNodes.item(j);
                    buttons.add(new KakaotalkButton(text(b, "Name"), text(b, "ButtonType"),
                            text(b, "Url1"), text(b, "Url2")));
                }
                templates.add(new KakaotalkTemplate(text(t, "TemplateName"), text(t, "TemplateTitle"),
                        text(t, "TemplateContent"), buttons));
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
        return templates;
    }

    private static String text(Element parent, String tag) {
        NodeList list = parent.getElementsByTagNameNS("*", tag);
        return list.getLength() == 0 ? "" : list.item(0).getTextContent();
    }

    static String escape(String value) {
        if (value == null) return "";
        StringBuilder sb = new StringBuilder(value.length());
        value.codePoints().forEach(cp -> {
            if (!isXmlChar(cp)) return;
            switch (cp) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&apos;");
                default -> sb.appendCodePoint(cp);
            }
        });
        return sb.toString();
    }

    private static boolean isXmlChar(int cp) {
        return cp == 0x9 || cp == 0xA || cp == 0xD
                || (cp >= 0x20 && cp <= 0xD7FF)
                || (cp >= 0xE000 && cp <= 0xFFFD)
                || (cp >= 0x10000 && cp <= 0x10FFFF);
    }

    /** {Method}Result 요소의 텍스트. 요소가 없으면 empty. */
    static Optional<String> parseResult(String xml, String method) {
        if (xml == null) return Optional.empty();
        Matcher m = Pattern.compile("<(?:\\w+:)?" + method + "Result[^>]*?(?:/>|>(.*?)</(?:\\w+:)?" + method + "Result>)",
                Pattern.DOTALL).matcher(xml);
        if (!m.find()) return Optional.empty();
        String raw = m.group(1);
        return Optional.of(raw == null ? "" : unescape(raw).trim());
    }

    static boolean isErrorCode(String result) {
        return result != null && ERROR_CODE.matcher(result).matches();
    }

    private static String unescape(String s) {
        return s.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"")
                .replace("&apos;", "'").replace("&amp;", "&");
    }

    /** 한글 등 ASCII 이외 문자는 2byte, ASCII 는 1byte 로 계산한다. */
    static int byteLength(String s) {
        if (s == null) return 0;
        return s.codePoints().map(cp -> cp < 0x80 ? 1 : 2).sum();
    }

    static String truncateBytes(String s, int maxBytes) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        int used = 0;
        for (int i = 0; i < s.length(); ) {
            int cp = s.codePointAt(i);
            int len = cp < 0x80 ? 1 : 2;
            if (used + len > maxBytes) break;
            sb.appendCodePoint(cp);
            used += len;
            i += Character.charCount(cp);
        }
        return sb.toString();
    }

    static byte[] utf8(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }
}
