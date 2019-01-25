package com.enjoybt.um;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import org.jdom2.Element;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class DownNotice {

    private String xmlData;
    private String filePath;
    private List<Element> fileList;

    public DownNotice(String xmlData) {
        this.xmlData = xmlData;
        try {
            initDownNotice(xmlData);
        } catch (Exception e) {
        }

    }

    public void initDownNotice(String xmlData) throws JDOMException, IOException {
        Document doc = new SAXBuilder().build(new StringReader(xmlData));
        Element root = doc.getRootElement();
        Element files = root.getChild("filelists");
        fileList = files.getChildren();
        filePath = root.getChild("filepath").getValue();
    }

    public String getFilePath() {
        return filePath;
    }

    public List<Element> getFileList() {
        return fileList;
    }
}
