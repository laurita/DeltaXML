package delta;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class DeltaXMLTest {

	private static Document d1;
	private static Document d2;
	private static Document dRes;
	private static Document aRes;
	private static Element root1;
	private static Element root2;
	private static Element rootRes;
	private static Element rootAres;

	@BeforeClass
	public static void oneTimeSetUp() throws ParserConfigurationException,
			SAXException, IOException, TransformerException,
			XPathExpressionException {
		// one-time initialization code
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		String xmlFile1 = "./data/a1.xml";
		String xmlFile2 = "./data/a2.xml";
		String xmlFileRes = "./data/aRes.xml";
		d1 = builder.parse(new File(xmlFile1));
		d2 = builder.parse(new File(xmlFile2));
		dRes = DeltaXML.copyDoc(d1);
		aRes = builder.parse(new File(xmlFileRes));
		root1 = d1.getDocumentElement();
		root2 = d2.getDocumentElement();
		rootRes = dRes.getDocumentElement();
		rootAres = aRes.getDocumentElement();
		DeltaXML.deleteEmptyChildren(d1);
		DeltaXML.deleteEmptyChildren(d2);
		DeltaXML.deleteEmptyChildren(dRes);
		DeltaXML.deleteEmptyChildren(aRes);
	}


	@Test
	public void test_diff_with_identical_trees()
			throws XPathExpressionException {
		DeltaXML.diff(root1, root1, rootRes);
		assertEquals("Result", "A=B", rootRes.getAttribute("deltaxml:deltaV2"));
	}
	
	@Test
	public void test_diff_with_different_trees()
			throws XPathExpressionException {
		DeltaXML.diff(root1, root2, rootRes);
		assert(rootAres.isEqualNode(rootRes));
	}

	@Test
	public void test_hasChildNamed() {
		assertEquals("Result", true, DeltaXML.hasChildNamed(root1, "c"));
		assertEquals("Result", false, DeltaXML.hasChildNamed(root1, "g"));
	}

}
