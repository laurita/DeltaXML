package delta;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
	static String xmlFile1 = "./data/a1.xml";
	static private Document d2;
	static String xmlFile2 = "./data/a2.xml";

	public static void deleteEmptyChildren(Document doc)
			throws XPathExpressionException {
		XPathFactory xpathFactory = XPathFactory.newInstance();
		// XPath to find empty text nodes.
		XPathExpression xpathExp = xpathFactory.newXPath().compile(
				"//text()[normalize-space(.) = '']");
		NodeList emptyTextNodes = (NodeList) xpathExp.evaluate(doc,
				XPathConstants.NODESET);

		// Remove each empty text node from document.
		for (int i = 0; i < emptyTextNodes.getLength(); i++) {
			Node emptyTextNode = emptyTextNodes.item(i);
			emptyTextNode.getParentNode().removeChild(emptyTextNode);
		}
	}

	private static final int OPEN = 1;
	private static final int CLOSE = 2;
	private static final int OPENCLOSE = 3;

	public static int countChildren(Node n) {
		NodeList nl = n.getChildNodes();
		return nl.getLength();
	}

	// TODO: diff(Node n1, Node n2) based on ted

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

	public static void diff(Element el1, Element el2)
			throws XPathExpressionException {
		System.out.println("diff of: " + el1.getNodeName() + ", " + el2.getNodeName());
		System.out.println("Equal: " + el1.isEqualNode(el2));
		print(el1, 0);
		print(el2, 0);
		Element el11;
		Element el21;
		if (el1.isEqualNode(el2)) {
			el1.setAttribute("deltaxml:deltaV2", "A=B");
			return;
		} else {
			el1.setAttribute("deltaxml:deltaV2", "A!=B");
			if (el1.hasChildNodes() && el1.getFirstChild().getNodeType() != Document.TEXT_NODE) {
				NodeList nl = el1.getChildNodes();
				for (int i = 0; i < nl.getLength(); i++) {
					if (hasChildNamed(el2, nl.item(i).getNodeName())) {
						int minTEDIndex = 0;
						NodeList list = el2.getElementsByTagName(nl.item(i)
								.getNodeName());
						if (list.getLength() == 1) {
							el11 = (Element) nl.item(i);
							el21 = (Element) list.item(minTEDIndex);
						} else {
							int minTed = TED.ted(nl.item(i), list.item(0));
							for (int j = 0; j < list.getLength(); j++) {
								int ted = TED.ted(nl.item(i), list.item(j));
								if (ted < minTed) {
									minTed = ted;
									minTEDIndex = j;
								}
							}
							el11 = (Element) nl.item(i);
							el21 = (Element) list.item(minTEDIndex);
						}
						diff(el11, el21);
						el2.removeChild(el21);
					} else {
						((Element) nl.item(i)).setAttribute("deltaxml:deltaV2", "A");
					}
				}
				if (el2.hasChildNodes()) {
					NodeList nl2 = el2.getChildNodes();
					for (int k = 0; k < nl2.getLength(); k++) {
						Element clone = (Element) nl2.item(k).cloneNode(true);
						clone.setAttribute("deltaxml:deltaV2", "B");
						Node importedNode = el1.getOwnerDocument().importNode(clone, true);
						el1.appendChild(importedNode);
					}
				}
			} else if (el2.hasChildNodes() && el2.getFirstChild().getNodeType() != Document.TEXT_NODE) {
				NodeList nl2 = el2.getChildNodes();
				for (int k = 0; k < nl2.getLength(); k++) {
					Element clone = (Element) nl2.item(k).cloneNode(true);
					clone.setAttribute("deltaxml:deltaV2", "B");
					Node importedNode = el1.getOwnerDocument().importNode(clone, true);
					el1.appendChild(importedNode);
				}
			}
		}
		//diffAttributes(el1, el2);
	}

	public static void diffAttributes(Element el1, Element el2) {
		System.out.println("diff_attributes: " + el1.getNodeName() + ", " + el2.getNodeName());
		if (el1.hasAttributes() && el2.hasAttributes()) {
			Element deltaAttr = el1.getOwnerDocument().createElement(
					"deltaxml:attributes");
			el1.appendChild(deltaAttr);
			NamedNodeMap attrs1 = el1.getAttributes();
			NamedNodeMap attrs2 = el2.getAttributes();
			if (attrs1.equals(attrs2)) {
				deltaAttr.setAttribute("deltaxml:deltaV2", "A=B");
			} else {
				print_element(0, el1, 0);
				print_element(0, el2, 0);
				System.out.println("attrs1: " + attrs1 + ", " + attrs2);
				deltaAttr.setAttribute("deltaxml:deltaV2", "A!=B");
				for (int i = 0; i < attrs1.getLength(); i++) {
					
					if (attrs2.getNamedItem(attrs1.item(i).getNodeName())
							.equals(null)) {
						String attrName = attrs1.item(i).getNodeName();
						Element elAttr = deltaAttr.getOwnerDocument()
								.createElement("dxa:" + attrName);
						elAttr.setAttribute("deltaxml:deltaV2", "A");
						deltaAttr.appendChild(elAttr);
						Element value = elAttr.getOwnerDocument()
								.createElement("deltaxml:attributeValue");
						value.setAttribute("deltaxml:deltaV2", "A");
						value.getOwnerDocument().createTextNode(
								attrs1.item(i).getNodeValue());
						elAttr.appendChild(value);
					} else if (attrs1
							.item(i)
							.getNodeValue()
							.equals(attrs2.getNamedItem(
									attrs1.item(i).getNodeName())
									.getNodeValue())) {
						String attrName = attrs1.item(i).getNodeName();
						Element elAttr = deltaAttr.getOwnerDocument()
								.createElement("dxa:" + attrName);
						elAttr.setAttribute("deltaxml:deltaV2", "A=B");
						deltaAttr.appendChild(elAttr);
						Element value = (Element) elAttr.getOwnerDocument()
								.createElement("deltaxml:attributeValue");
						value.setAttribute("deltaxml:deltaV2", "A=B");
						elAttr.appendChild(value);
						// attrs2.removeNamedItem(attrs1.item(i).getNodeName());
					} else if (!attrs1
							.item(i)
							.getNodeValue()
							.equals(attrs2.getNamedItem(
									attrs1.item(i).getNodeName())
									.getNodeValue())) {
						String attrName = attrs1.item(i).getNodeName();
						Element elAttr = deltaAttr.getOwnerDocument()
								.createElement("dxa:" + attrName);
						elAttr.setAttribute("deltaxml:deltaV2", "A!=B");
						deltaAttr.appendChild(elAttr);
						Element value = (Element) elAttr.getOwnerDocument()
								.createElement("deltaxml:attributeValue");
						value.setAttribute("deltaxml:deltaV2", "A!=B");
						elAttr.appendChild(value);
						// attrs2.removeNamedItem(attrs1.item(i).getNodeName());
					}
				}
				for (int i = 0; i < attrs2.getLength(); i++) {
					if (attrs1.getNamedItem(attrs2.item(i).getNodeName())
							.equals(null)) {
						String attrName = attrs2.item(i).getNodeName();
						Element elAttr = deltaAttr.getOwnerDocument()
								.createElement("dxa:" + attrName);
						elAttr.setAttribute("deltaxml:deltaV2", "B");
						deltaAttr.appendChild(elAttr);
						Element value = elAttr.getOwnerDocument()
								.createElement("deltaxml:attributeValue");
						value.setAttribute("deltaxml:deltaV2", "B");
						value.getOwnerDocument().createTextNode(
								attrs1.item(i).getNodeValue());
						elAttr.appendChild(value);
					}
				}
			}
		} else if (el1.hasAttributes() && !el2.hasAttributes()) {
			System.out.println("ok");
			Element deltaAttr = el1.getOwnerDocument().createElement(
					"deltaxml:attributes");
			deltaAttr.setAttribute("deltaxml:deltaV2", "A!=B");
			el1.appendChild(deltaAttr);
			NamedNodeMap attrs1 = el1.getAttributes();
			for (int i = 0; i < attrs1.getLength(); i++) {
				System.out.println("loop");
				String attrName = attrs1.item(i).getNodeName();
				Element elAttr = deltaAttr.getOwnerDocument().createElement(
						"dxa:" + attrName);
				elAttr.setAttribute("deltaxml:deltaV2", "A");
				elAttr.getOwnerDocument()
						.createElement("deltaxml:attributeValue")
						.setAttribute("deltaxml:deltaV2", "A");
				deltaAttr.appendChild(elAttr);
				Element value = elAttr.getOwnerDocument().createElement(
						"deltaxml:attributeValue");
				value.setAttribute("deltaxml:deltaV2", "A");
				value.getOwnerDocument().createTextNode(
						attrs1.item(i).getNodeValue());
				elAttr.appendChild(value);
			}
		} else if (!el1.hasAttributes() && el2.hasAttributes()) {
			Element deltaAttr = el1.getOwnerDocument().createElement(
					"deltaxml:attributes");
			deltaAttr.setAttribute("deltaxml:deltaV2", "A!=B");
			el1.appendChild(deltaAttr);
			NamedNodeMap attrs2 = el2.getAttributes();
			for (int i = 0; i < attrs2.getLength(); i++) {
				String attrName = attrs2.item(i).getNodeName();
				Element elAttr = deltaAttr.getOwnerDocument().createElement(
						"dxa:" + attrName);
				elAttr.setAttribute("deltaxml:deltaV2", "B");
				elAttr.getOwnerDocument()
						.createElement("deltaxml:attributeValue")
						.setAttribute("deltaxml:deltaV2", "B");
				deltaAttr.appendChild(elAttr);
				Element value = elAttr.getOwnerDocument().createElement(
						"deltaxml:attributeValue");
				value.setAttribute("deltaxml:deltaV2", "B");
				value.getOwnerDocument().createTextNode(
						attrs2.item(i).getNodeValue());
				Text txt = value.getOwnerDocument().createTextNode(
						attrs2.item(i).getNodeValue());
				value.appendChild(txt);
				elAttr.appendChild(value);
			}
		}
	}

	public static void print(Node n, int level) {
		int i;

		// find out what type of node this is

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
		String slash = "";
		if (mode == CLOSE || mode == OPENCLOSE) {
			System.out
					.print(get_indent(level) + "</" + n.getNodeName());
			print_attributes(n);
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

	public static void main(String[] args) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		d1 = builder.parse(new File(xmlFile1));
		d2 = builder.parse(new File(xmlFile2));

		Element root1 = d1.getDocumentElement();
		Element root2 = d2.getDocumentElement();

		// Step 1: Preprocessing - delete all empty text nodes in an XML
		// document
		deleteEmptyChildren(d1);
		deleteEmptyChildren(d2);

		// Step 2:
		diff(root1, root2);
		print(root1, 0);
	}
}
