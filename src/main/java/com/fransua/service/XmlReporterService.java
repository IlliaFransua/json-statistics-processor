package com.fransua.service;

import com.fransua.config.ApplicationConfig;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlReporterService {

  public static void createReport(Map<String, Integer> statistics, String attributeName) {
    try {
      Document document = createEmptyDocument();

      fillDocument(statistics, document, attributeName);

      transformAndWrite(document, attributeName);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static void transformAndWrite(Document document, String attributeName)
      throws TransformerException {
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

    DOMSource source = new DOMSource(document);
    File statisticFile = initializeStatisticFile(attributeName);
    StreamResult result = new StreamResult(statisticFile);

    transformer.transform(source, result);
  }

  private static File initializeStatisticFile(String attributeName) {
    String directoryName = ApplicationConfig.statisticDirectoryName();
    try {
      if (!Files.exists(Path.of(directoryName))) {
        Files.createDirectory(Path.of(directoryName));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    String statisticFileName = ApplicationConfig.getStatisticFileNameFor(attributeName);
    Path statisticFilePath = Path.of(directoryName, statisticFileName);
    return statisticFilePath.toFile();
  }

  private static void fillDocument(Map<String, Integer> statistics, Document document,
      String attributeName) {
    Element rootElement = document.createElement("statistic");
    document.appendChild(rootElement);

    for (Map.Entry<String, Integer> statistic : statistics.entrySet()) {
      Element item = document.createElement("item");
      rootElement.appendChild(item);

      String clearAttributeName = attributeName.toLowerCase(Locale.ROOT).replaceAll("-", "");
      Element attribute = document.createElement(clearAttributeName);
      attribute.setTextContent(statistic.getKey());
      item.appendChild(attribute);

      Element count = document.createElement("count");
      count.setTextContent(statistic.getValue().toString());
      item.appendChild(count);
    }
  }

  private static Document createEmptyDocument() throws ParserConfigurationException {
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
    return documentBuilder.newDocument();
  }
}
