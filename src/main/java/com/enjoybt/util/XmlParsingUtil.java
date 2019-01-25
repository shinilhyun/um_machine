package com.enjoybt.util;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class XmlParsingUtil {
    /***
     * xml 파싱하여 root element take
     * @param xmlData
     * @return
     * @throws JDOMException
     * @throws IOException
     */
    public static Element getXmlDocRootElement(String xmlData) throws JDOMException, IOException {
        Document doc = new SAXBuilder().build(new StringReader(xmlData));
        return doc.getRootElement();
    }

    /***
     * xml 파싱한 요소에서 fileList 가져옴
     * @param
     * @return
     * @throws JDOMException
     * @throws IOException
     */
    public static List<Element> getFileListFromXml(Element root) throws Exception {
        Element files = root.getChild("filelists");
        return files.getChildren();

    }

    /***
     * xml 파싱항 요소에서 filePath 가져옴
     * @param root
     * @return
     * @throws Exception
     */
    public static String getRemoteFilePath(Element root) throws Exception{
        return root.getChild("filepath").getValue();
    }
}
