package delta;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class DeltaXML {
	static private Document d1;
	//static String xmlFile1 = "./data/a1.xml";
	static String xmlFile1;
	static String xmlFile2;
	static private Document d2;
	//static String xmlFile2 = "./data/a2.xml";
	private static final int OPEN = 1;
	private static final int CLOSE = 2;
	private static final int OPENCLOSE = 3;

	
	public static void diff(Element el1, Element el2, Element elRes) throws XPathExpressionException {
		diffElements(el1, el2, elRes);
		diffAttributes(el1, el2, elRes);
		print(elRes, 0);
	}
	
	// modifies the elRes Element based on the element differences among el1 and el2
	public static void diffElements(Element el1, Element el2, Element elRes)
			throws XPathExpressionException {
		if (el1.isEqualNode(el2)) {
			elRes.setAttribute("deltaxml:deltaV2", "A=B");
			return;
		} else {
			elRes.setAttribute("deltaxml:deltaV2", "A!=B");
			if (el1.hasChildNodes()
					&& el1.getFirstChild().getNodeType() != Document.TEXT_NODE) {
				doIfFirstHasNonTextChildren(el1, el2, elRes);
			} else if (el1.hasChildNodes()
					&& el1.getFirstChild().getNodeType() == Document.TEXT_NODE) {
				doIfFirstHasTextChild(el1, el2, elRes);
			} else if (el2.hasChildNodes()
					&& el2.getFirstChild().getNodeType() != Document.TEXT_NODE) {
				doForLeftNonTextChildren(el1, el2, elRes);
			} else if (el2.hasChildNodes()
					&& el2.getFirstChild().getNodeType() == Document.TEXT_NODE) {
				doForLeftTextNode(el1, el2, elRes);
			}
		}
		diffAttributes(el1, el2, elRes);
	}

	// HELPER METHODS FOR diffElements()
	
	public static void doIfFirstHasNonTextChildren(Element el1, Element el2,
			Element elRes) throws XPathExpressionException {
		Element el11;
		Element el21;
		Element elRes1;
		NodeList nl = el1.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			if (hasChildNamed(el2, nl.item(i).getNodeName())) {
				int minTEDIndex = 0;
				NodeList list = el2.getElementsByTagName(nl.item(i)
						.getNodeName());
				if (list.getLength() > 1) {
					int minTed = TED.ted(nl.item(i), list.item(0));
					for (int j = 0; j < list.getLength(); j++) {
						int ted = TED.ted(nl.item(i), list.item(j));
						if (ted < minTed) {
							minTed = ted;
							minTEDIndex = j;
						}
					}
				}
				el11 = (Element) nl.item(i);
				el21 = (Element) list.item(minTEDIndex);
				elRes1 = (Element) elRes.getChildNodes().item(i);
				diffElements(el11, el21, elRes1);
				el2.removeChild(el21);
			} else {
				((Element) elRes.getChildNodes().item(i)).setAttribute(
						"deltaxml:deltaV2", "A");
			}
		}
		if (el2.hasChildNodes()) {
			NodeList nl2 = el2.getChildNodes();
			for (int k = 0; k < nl2.getLength(); k++) {
				Element clone = (Element) nl2.item(k).cloneNode(true);
				clone.setAttribute("deltaxml:deltaV2", "B");
				Node importedNode = elRes.getOwnerDocument().importNode(clone,
						true);
				elRes.appendChild(importedNode);
			}
		}
	}

	public static void doIfFirstHasTextChild(Element el1, Element el2,
			Element elRes) {
		Element txtGr = elRes.getOwnerDocument().createElement(
				"deltaxml:textGroup");
		elRes.appendChild(txtGr);
		if (el2.hasChildNodes()
				&& el2.getFirstChild().getNodeType() == Document.TEXT_NODE) {
			if (el1.getFirstChild().getTextContent()
					.equals(el2.getFirstChild().getTextContent())) {
				txtGr.setAttribute("deltaxml:deltaV2", "A=B");
			} else {
				txtGr.setAttribute("deltaxml:deltaV2", "A!=B");
				Element txt1 = txtGr.getOwnerDocument().createElement(
						"deltaxml:text");
				txt1.setAttribute("deltaxml:deltaV2", "A");
				Text txt1text = txt1.getOwnerDocument().createTextNode(
						el1.getFirstChild().getTextContent());
				txt1.appendChild(txt1text);
				txtGr.appendChild(txt1);
				Element txt2 = txtGr.getOwnerDocument().createElement(
						"deltaxml:text");
				txt2.setAttribute("deltaxml:deltaV2", "B");
				Text txt2text = txt2.getOwnerDocument().createTextNode(
						el2.getFirstChild().getTextContent());
				txt2.appendChild(txt2text);
				txtGr.appendChild(txt2);
			}
		} else {
			txtGr.setAttribute("deltaxml:deltaV2", "A");
			Element txt1 = txtGr.getOwnerDocument().createElement(
					"deltaxml:text");
			txt1.setAttribute("deltaxml:deltaV2", "A");
			Text txt1text = txt1.getOwnerDocument().createTextNode(
					el1.getFirstChild().getTextContent());
			txt1.appendChild(txt1text);
			txtGr.appendChild(txt1);
		}
		elRes.removeChild(elRes.getFirstChild());
	}

	public static void doForLeftNonTextChildren(Element el1, Element el2,
			Element elRes) {
		NodeList nl2 = el2.getChildNodes();
		for (int k = 0; k < nl2.getLength(); k++) {
			Element clone = (Element) nl2.item(k).cloneNode(true);
			clone.setAttribute("deltaxml:deltaV2", "B");
			Node importedNode = elRes.getOwnerDocument()
					.importNode(clone, true);
			elRes.appendChild(importedNode);
		}
	}

	public static void doForLeftTextNode(Element el1, Element el2, Element elRes) {
		Element txtGr = elRes.getOwnerDocument().createElement(
				"deltaxml:textGroup");
		elRes.appendChild(txtGr);
		txtGr.setAttribute("deltaxml:deltaV2", "B");
		Element txt1 = txtGr.getOwnerDocument().createElement("deltaxml:text");
		txt1.setAttribute("deltaxml:deltaV2", "B");
		Text txt1text = txt1.getOwnerDocument().createTextNode(
				el2.getFirstChild().getTextContent());
		txt1.appendChild(txt1text);
		txtGr.appendChild(txt1);
	}

	
	// modifies the elRes Element based on the attribute differences among el1 and el2  
	public static void diffAttributes(Element el1, Element el2, Element elRes) {
		if (el1.hasAttributes() && el2.hasAttributes()) {
			doIfBothHaveAttributes(el1, el2, elRes);
		} else if (el1.hasAttributes() && !el2.hasAttributes()) {
			doIfOnlyFirstHasAttributes(el1, el2, elRes);
		} else if (!el1.hasAttributes() && el2.hasAttributes()) {
			doIfOnlySecondHasAttributes(el1, el2, elRes);
		}
	}

	
	// HELPER METHODS FOR diffAttributes()
	
	public static void doIfBothHaveAttributes(Element el1, Element el2,
			Element elRes) {
		Element deltaAttr = elRes.getOwnerDocument().createElement(
				"deltaxml:attributes");
		elRes.appendChild(deltaAttr);
		NamedNodeMap attrs1 = el1.getAttributes();
		NamedNodeMap attrs2 = el2.getAttributes();
		if (attrs1.equals(attrs2)) {
			deltaAttr.setAttribute("deltaxml:deltaV2", "A=B");
		} else {
			deltaAttr.setAttribute("deltaxml:deltaV2", "A!=B");
			for (int i = 0; i < attrs1.getLength(); i++) {
				if (attrs2.getNamedItem(attrs1.item(i).getNodeName()).equals(
						null)) {
					String attrName = attrs1.item(i).getNodeName();
					Element elAttr = elRes.getOwnerDocument().createElement(
							"dxa:" + attrName);
					elAttr.setAttribute("deltaxml:deltaV2", "A");
					deltaAttr.appendChild(elAttr);
					Element value = elRes.getOwnerDocument().createElement(
							"deltaxml:attributeValue");
					value.setAttribute("deltaxml:deltaV2", "A");
					value.getOwnerDocument().createTextNode(
							attrs1.item(i).getNodeValue());
					elAttr.appendChild(value);
				} else if (attrs1
						.item(i)
						.getNodeValue()
						.equals(attrs2.getNamedItem(
								attrs1.item(i).getNodeName()).getNodeValue())) {
					String attrName = attrs1.item(i).getNodeName();
					Element elAttr = elRes.getOwnerDocument().createElement(
							"dxa:" + attrName);
					elAttr.setAttribute("deltaxml:deltaV2", "A=B");
					deltaAttr.appendChild(elAttr);
					Element value = (Element) elRes.getOwnerDocument()
							.createElement("deltaxml:attributeValue");
					value.setAttribute("deltaxml:deltaV2", "A=B");
					value.getOwnerDocument().createTextNode(
							attrs1.item(i).getNodeValue());
					elAttr.appendChild(value);
				} else if (!attrs1
						.item(i)
						.getNodeValue()
						.equals(attrs2.getNamedItem(
								attrs1.item(i).getNodeName()).getNodeValue())) {
					String attrName = attrs1.item(i).getNodeName();
					Element elAttr = elRes.getOwnerDocument().createElement(
							"dxa:" + attrName);
					elAttr.setAttribute("deltaxml:deltaV2", "A!=B");
					deltaAttr.appendChild(elAttr);
					Element value1 = (Element) elRes.getOwnerDocument()
							.createElement("deltaxml:attributeValue");
					value1.setAttribute("deltaxml:deltaV2", "A");
					value1.getOwnerDocument().createTextNode(
							attrs1.item(i).getNodeValue());
					elAttr.appendChild(value1);
					Element value2 = (Element) elRes.getOwnerDocument()
							.createElement("deltaxml:attributeValue");
					value2.setAttribute("deltaxml:deltaV2", "B");
					value2.getOwnerDocument().createTextNode(
							attrs2.item(i).getNodeValue());
					elAttr.appendChild(value2);
				}
			}
			for (int i = 0; i < attrs2.getLength(); i++) {
				if (attrs1.getNamedItem(attrs2.item(i).getNodeName()).equals(
						null)) {
					String attrName = attrs2.item(i).getNodeName();
					Element elAttr = elRes.getOwnerDocument().createElement(
							"dxa:" + attrName);
					elAttr.setAttribute("deltaxml:deltaV2", "B");
					deltaAttr.appendChild(elAttr);
					Element value = elRes.getOwnerDocument().createElement(
							"deltaxml:attributeValue");
					value.setAttribute("deltaxml:deltaV2", "B");
					value.getOwnerDocument().createTextNode(
							attrs2.item(i).getNodeValue());
					elAttr.appendChild(value);
				}
			}
		}
	}

	public static void doIfOnlyFirstHasAttributes(Element el1, Element el2,
			Element elRes) {
		Element deltaAttr = elRes.getOwnerDocument().createElement(
				"deltaxml:attributes");
		deltaAttr.setAttribute("deltaxml:deltaV2", "A!=B");
		elRes.appendChild(deltaAttr);
		NamedNodeMap attrs1 = el1.getAttributes();
		for (int i = 0; i < attrs1.getLength(); i++) {
			String attrName = attrs1.item(i).getNodeName();
			Element elAttr = elRes.getOwnerDocument().createElement(
					"dxa:" + attrName);
			elAttr.setAttribute("deltaxml:deltaV2", "A");
			Element value = elRes.getOwnerDocument().createElement(
					"deltaxml:attributeValue");
			value.setAttribute("deltaxml:deltaV2", "A");
			value.getOwnerDocument().createTextNode(
					attrs1.item(i).getNodeValue());
			deltaAttr.appendChild(elAttr);
			elAttr.appendChild(value);
		}
	}

	public static void doIfOnlySecondHasAttributes(Element el1, Element el2,
			Element elRes) {
		Element deltaAttr = elRes.getOwnerDocument().createElement(
				"deltaxml:attributes");
		deltaAttr.setAttribute("deltaxml:deltaV2", "B");
		elRes.appendChild(deltaAttr);
		NamedNodeMap attrs2 = el2.getAttributes();
		for (int i = 0; i < attrs2.getLength(); i++) {
			String attrName = attrs2.item(i).getNodeName();
			Element elAttr = elRes.getOwnerDocument().createElement(
					"dxa:" + attrName);
			elAttr.setAttribute("deltaxml:deltaV2", "B");
			elAttr.getOwnerDocument().createElement("deltaxml:attributeValue")
					.setAttribute("deltaxml:deltaV2", "B");
			deltaAttr.appendChild(elAttr);
			Element value = elRes.getOwnerDocument().createElement(
					"deltaxml:attributeValue");
			value.setAttribute("deltaxml:deltaV2", "B");
			value.getOwnerDocument().createTextNode(
					attrs2.item(i).getNodeValue());
			Text txt = elRes.getOwnerDocument().createTextNode(
					attrs2.item(i).getNodeValue());
			value.appendChild(txt);
			elAttr.appendChild(value);
		}
	}

	// METHODS FOR PRINTING XML DOCUMENT
	
	public static void print(Node n, int level) {
		int i;
		short t = n.getNodeType();
		if (t == Document.ELEMENT_NODE) {
			if (n.hasChildNodes()) {
				print_element(level, n, OPEN);
			} else {
				print_element(level, n, OPENCLOSE);
			}
		} else if (t == Document.TEXT_NODE) {
			if (!is_empty(n)) {
				print_text(level, n);
			}
		}
		NodeList nl = n.getChildNodes();
		if (nl.getLength() == 0) {
			return;
		}
		for (i = 0; i < nl.getLength(); i++) {
			print(nl.item(i), level + 1);
		}
		if (t == Document.ELEMENT_NODE) {
			if (n.hasChildNodes()) {
				print_element(level, n, CLOSE);
			}
		}
	}

	static void print_element(int level, Node n, int mode) {
		if (mode == CLOSE) {
			System.out.print(get_indent(level) + "</" + n.getNodeName());
			System.out.println(">");
		} else if (mode == OPENCLOSE) {
			System.out.print(get_indent(level) + "<" + n.getNodeName());
			if (n.hasAttributes()) {
				print_attributes(n);
			}
			System.out.println("/>");
		} else {
			System.out.print(get_indent(level) + "<" + n.getNodeName());
			if (n.hasAttributes()) {
				print_attributes(n);
			}
			System.out.println(">");
		}
	}

	static void print_attributes(Node n) {
		NamedNodeMap attrs = n.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			Node attr = attrs.item(i);
			System.out.print(" " + attr.getNodeName() + "=\""
					+ attr.getNodeValue() + "\"");
		}
	}

	static void print_text(int level, Node n) {
		String txt = n.getNodeValue().trim();
		txt = txt.replaceAll("\\s+", " ");
		System.out.println(get_indent(level) + txt);
	}

	static String get_indent(int level) {
		int i;
		StringBuffer buf = new StringBuffer();
		for (i = 0; i < level; i++) {
			buf.append("  ");
		}
		return buf.toString();
	}

	static boolean is_empty(Node n) {
		String val = n.getNodeValue();
		val = val.replaceAll("\\s+", "");
		val = val.replaceAll("\n+", "");
		if (val.equals("")) {
			return true;
		}
		return false;
	}

	// GENERAL HELPERS
	
	public static Document copyDoc(Document doc) throws TransformerException {
		TransformerFactory tfactory = TransformerFactory.newInstance();
		Transformer tx = tfactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		DOMResult result = new DOMResult();
		tx.transform(source, result);
		return (Document) result.getNode();
	}
	
	public static void deleteEmptyChildren(Document doc)
			throws XPathExpressionException {
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPathExpression xpathExp = xpathFactory.newXPath().compile(
				"//text()[normalize-space(.) = '']");
		NodeList emptyTextNodes = (NodeList) xpathExp.evaluate(doc,
				XPathConstants.NODESET);
		for (int i = 0; i < emptyTextNodes.getLength(); i++) {
			Node emptyTextNode = emptyTextNodes.item(i);
			emptyTextNode.getParentNode().removeChild(emptyTextNode);
		}
	}

	public static int countChildren(Node n) {
		NodeList nl = n.getChildNodes();
		return nl.getLength();
	}

	public static boolean hasChildNamed(Element el, String nodeName) {
		boolean has = false;
		NodeList nl = el.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeName().equals(nodeName)) {
				has = true;
				return has;
			}
		}
		return has;
	}

	public static void main(String[] args) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		xmlFile1 = args[0];
		xmlFile2 = args[1];
		d1 = builder.parse(new File(xmlFile1));
		d2 = builder.parse(new File(xmlFile2));
		Document dRes = copyDoc(d1);

		Element root1 = d1.getDocumentElement();
		Element root2 = d2.getDocumentElement();
		Element rootRes = dRes.getDocumentElement();

		// Step 1: Preprocessing - delete all empty text nodes in an XML
		// document
		deleteEmptyChildren(d1);
		deleteEmptyChildren(d2);
		deleteEmptyChildren(dRes);

		// Step 2: do a diff and print a modified XML tree (the first one is
		// modified!!!)
		diff(root1, root2, rootRes);
	}
}
