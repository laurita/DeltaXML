package delta;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class TEDTest {

	static DocumentBuilderFactory factory;
	static DocumentBuilder builder;
	static Document d1;
	static String xmlFile;
	static TED tester;
	static Node root;

	@BeforeClass
	public static void oneTimeSetup() throws ParserConfigurationException,
			SAXException, IOException, XPathExpressionException {
		factory = DocumentBuilderFactory.newInstance();
		builder = factory.newDocumentBuilder();
		xmlFile = "./data/a1.xml";
		d1 = builder.parse(new File(xmlFile));
		tester = new TED();
		root = d1.getDocumentElement();
		DeltaXML.deleteEmptyChildren(d1);
	}

	@Test
	public void test_id() {
		assertEquals("id of root node", TED.id(root), -1);
	}

	@Test
	public void test_add_ids() {
		TED.add_ids(root, 0);
		assertEquals("id of root node after adding ids", TED.id(root), 7);
		assertEquals("id of a1 after adding ids", TED.id(root.getFirstChild().getFirstChild()), 0);
	}
	
	@Test
	public void test_remove_ids() {
		TED.add_ids(root, 0);
		TED.remove_ids(root, 0);
		//DeltaXML.print(root, 0);
		assertEquals("id of root node after adding ids", TED.id(root), -1);
	}

	@Test
	public void test_nodes() {
		assertEquals("number of nodes in a tree ", TED.nodes(root, 0), 8);
	}

	@Test
	public void test_leaves() {
		assertEquals("number of leaves in a tree ", TED.leaves(root, 0), 6);
	}

	@Test
	public void test_kr() {
		TED.add_ids(root, 0);
		DeltaXML.print(root, 0);
		int leaves = TED.leaves(root, 0);
		int[] lmld = TED.lmld(root, new int[TED.nodes(root, 0)]);
		int[] real = TED.kr(lmld, leaves);
		int[] shouldBe = new int[] { 2, 3, 4, 5, 6, 7};
		for (int i = 0; i < leaves; i++) {
			assertEquals("lmld of xml tree ", real[i], shouldBe[i]);
		}
	}

	@Test
	public void test_lmld() throws SAXException, IOException, XPathExpressionException {
		String xml1 = "./data/a1.xml";
		Document t1 = builder.parse(new File(xml1));
		Element root1 = t1.getDocumentElement();
		DeltaXML.deleteEmptyChildren(t1);
		TED.add_ids(root1, 0);
		//DeltaXML.print(root1, 0);
		int len = 8;
		int[] real = TED.lmld(root1, new int[len]);
		int[] shouldBe = new int[] { 0, 0, 2, 3, 4, 5, 6, 0};
		for (int i = 0; i < len; i++) {
			assertEquals("lmld of xml tree ", real[i], shouldBe[i]);
		}
	}
	
	@Test
	public void test_getNode() throws SAXException, IOException, XPathExpressionException {
		String xml1 = "./data/t1.xml";
		Document t1 = builder.parse(new File(xml1));
		Element root1 = t1.getDocumentElement();
		DeltaXML.deleteEmptyChildren(t1);
		TED.add_ids(root1, 0);
		//System.out.println(TED.getNode(t1, String.valueOf(1)).getNodeName());
		assertEquals(TED.getNode(t1, String.valueOf(1)).getNodeName(), "b");
	}
	
	
	@Test
	public void test_ted() throws SAXException, IOException, XPathExpressionException {
		//String xml1 = "./data/t1.xml";
		//String xml2 = "./data/t2.xml";
		String xml1 = "./data/a1.xml";
		String xml2 = "./data/a2.xml";
		Document t1 = builder.parse(new File(xml1));
		Document t2 = builder.parse(new File(xml2));
		Element root1 = t1.getDocumentElement();
		Element root2 = t2.getDocumentElement();
		DeltaXML.deleteEmptyChildren(t1);
		DeltaXML.deleteEmptyChildren(t2);
		assertEquals(TED.ted(root1, root2), 3);
	}
	
}
