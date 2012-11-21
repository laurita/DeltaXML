package delta;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class DeltaXMLTest {

	private static Document d1;
	private static Element root1;
	private static Document d2;
	private static Element root2;
	//private static DeltaXML tester;
	
	@BeforeClass
    public static void oneTimeSetUp() throws ParserConfigurationException, SAXException, IOException {
        // one-time initialization code   
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		String xmlFile1 = "./data/a1.xml";
		String xmlFile2 = "./data/a2.xml";
		d1 = builder.parse(new File(xmlFile1));
		d2 = builder.parse(new File(xmlFile2));
		root1 = d1.getDocumentElement(); 
		root2 = d2.getDocumentElement(); 
		//tester = new DeltaXML();
    }
 
    @AfterClass
    public static void oneTimeTearDown() {
        // one-time cleanup code
    	System.out.println("@AfterClass - oneTimeTearDown");
    }
	
	@Test
	public void test() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		assertEquals("Result", 13, d1.getDocumentElement().getChildNodes().getLength());
	    DeltaXML.deleteEmptyChildren(d1);
	    assertEquals("Result", 6, d1.getDocumentElement().getChildNodes().getLength());
	}
	
	@Test
	public void test_diff_with_identical_trees() throws XPathExpressionException {
		DeltaXML.diff(root1, root1);
		assertEquals("Result", "A=B", root1.getAttribute("deltaxml:deltaV2"));
	}
	
	@Test
	public void test_hasChildNamed() {
		assertEquals("Result", true, DeltaXML.hasChildNamed(root1, "c"));
		assertEquals("Result", false, DeltaXML.hasChildNamed(root1, "g"));
	}

}
