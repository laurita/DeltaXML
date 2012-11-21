package delta;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ranges.Range;
import org.xml.sax.SAXException;

public class TED {

	static final int INSERT = 1;
	static final int DELETE = 1;
	static final int RENAME = 1;

	public static int ted(Node n1, Node n2) throws XPathExpressionException {
		System.out.println("entering ted");
		// Adding ted_ids for ted algorithm
		add_ids(n1, 0);
		add_ids(n2, 0);
		int n = nodes(n1, 0);
		System.out.println("n=" + n);
		int m = nodes(n2, 0);
		System.out.println("m=" + m);
		int[][] td = new int[n][m];
		int[] l1 = lmld(n1, new int[n]);
		int[] l2 = lmld(n2, new int[m]);
		int[] kr1 = kr(l1, leaves(n1, 0));
		int[] kr2 = kr(l2, leaves(n2, 0));
		System.out.println("kr1");
		for (int i = 0; i < kr1.length; i++) {
			System.out.print(" " + kr1[i]);
		}
		System.out.println();
		System.out.println("kr2");
		for (int i = 0; i < kr2.length; i++) {
			System.out.print(" " + kr2[i]);
		}
		System.out.println();
		for (int i = 0; i < kr1.length; i++) {
			for (int j = 0; j < kr2.length; j++) {
				forestDist(n1, n2, kr1[i], kr2[j], l1, l2, td);
			}
		}
		// Removing ted_ids for ted algorithm
		remove_ids(n1, 0);
		remove_ids(n2, 0);
		return td[n - 1][m - 1];
	}

	public static void forestDist(Node n1, Node n2, int i, int j, int[] l1,
			int[] l2, int[][] td) throws XPathExpressionException {
		System.out.println("entering forestDist(" + i + ", " + j + ")");
		int iFrom = l1[i] - 1;
		int jFrom = l2[j] - 1;
		int dim1 = i - iFrom + 1;
		int dim2 = j - jFrom + 1;
		int[] indexOfi = new int[dim1];
		for (int x = 0; x < dim1; x++) {
			indexOfi[x] = iFrom + x;
		}
		int[] indexOfj = new int[dim2];
		for (int x = 0; x < dim2; x++) {
			indexOfj[x] = jFrom + x;
		}
		int[][] fd = new int[dim1][dim2];
		fd[index(indexOfi, l1[i] - 1)][index(indexOfj, l2[j] - 1)] = 0;
		System.out.println("fd[" + index(indexOfi, l1[i] - 1) + "]["
				+ index(indexOfj, l2[j] - 1) + "] "
				+ fd[index(indexOfi, l1[i] - 1)][index(indexOfj, l2[j] - 1)]);
		for (int di = l1[i]; di <= i; di++) {
			fd[index(indexOfi, di)][index(indexOfj, l2[j] - 1)] = fd[index(
					indexOfi, di - 1)][index(indexOfj, l2[j] - 1)] + DELETE;
			System.out.println("fd[" + index(indexOfi, di) + "]["
					+ index(indexOfj, l2[j] - 1) + "] "
					+ fd[index(indexOfi, di)][index(indexOfj, l2[j] - 1)]);
		}
		for (int dj = l2[j]; dj <= j; dj++) {
			fd[index(indexOfi, l1[i] - 1)][index(indexOfj, dj)] = fd[index(
					indexOfi, l1[i] - 1)][index(indexOfj, dj - 1)] + INSERT;
			System.out.println("fd[" + index(indexOfi, l1[i] - 1) + "]["
					+ index(indexOfj, dj) + "] "
					+ fd[index(indexOfi, l1[i] - 1)][index(indexOfj, dj)]);
		}
		for (int di = l1[i]; di <= i; di++) {

			for (int dj = l2[j]; dj <= j; dj++) {
				System.out.println("di " + di);
				System.out.println("dj " + dj);
				if ((l1[di] == l1[i]) && (l2[dj] == l2[j])) {
					fd[index(indexOfi, di)][index(indexOfj, dj)] = Math
							.min(fd[index(indexOfi, di - 1)][index(indexOfj, dj)]
									+ DELETE,
									Math.min(
											fd[index(indexOfi, di)][index(
													indexOfj, dj - 1)] + INSERT,
											fd[index(indexOfi, di - 1)][index(
													indexOfj, dj - 1)]
													+ renameCost(
															getNode(n1
																	.getOwnerDocument(),
																	String.valueOf(di)),
															getNode(n2
																	.getOwnerDocument(),
																	String.valueOf(dj)))));
					System.out.println("fd[" + index(indexOfi, di) + "]["
							+ index(indexOfj, dj) + "] "
							+ fd[index(indexOfi, di)][index(indexOfj, dj)]);
					td[di][dj] = fd[index(indexOfi, di)][index(indexOfj, dj)];
					System.out.println("adding to td[" + di + "][" + dj + "] "
							+ td[di][dj]);
				} else {
					fd[index(indexOfi, di)][index(indexOfj, dj)] = Math
							.min(fd[index(indexOfi, di - 1)][index(indexOfj, dj)]
									+ DELETE,
									Math.min(
											fd[index(indexOfi, di)][index(
													indexOfj, dj - 1)] + INSERT,
											fd[index(indexOfi, l1[di] - 1)][index(
													indexOfj, l2[dj] - 1)]
													+ td[di][dj]));
					System.out.println("fd[" + index(indexOfi, di) + "]["
							+ index(indexOfj, dj) + "] "
							+ fd[index(indexOfi, di)][index(indexOfj, dj)]);
				}
			}
		}

	}

	public static int renameCost(Element el1, Element el2) {
		if (el1.getNodeName() == el2.getNodeName()) {
			System.out.println("rename cost of " + el1.getNodeName() + " and "
					+ el2.getNodeName() + " is " + 0);
			return 0;
		} else {
			System.out.println("rename cost of " + el1.getNodeName() + " and "
					+ el2.getNodeName() + " is " + 1);
			return 1;
		}
	}

	public static Element getNode(Document doc, String attributeValue)
			throws XPathExpressionException {
		XPathFactory xpathFactory = XPathFactory.newInstance();
		// XPath to find empty text nodes.
		javax.xml.xpath.XPathExpression xpathExp = xpathFactory.newXPath()
				.compile("//*[@ted_id = \'" + attributeValue + "\']");
		NodeList nl = (NodeList) xpathExp.evaluate(doc, XPathConstants.NODESET);
		return (Element) nl.item(0);
	}

	public static int index(int[] array, int t) {
		return Arrays.binarySearch(array, t);
	}

	// returns an int[] of key root ted_ids
	public static int[] kr(int[] l, int leaves) {
		int[] krArray = new int[leaves];
		boolean[] visited = new boolean[l.length];
		int k = leaves;
		int i = l.length - 1;
		while (k >= 1) {
			if (!visited[l[i]]) {
				krArray[--k] = i;
				visited[l[i]] = true;
			}
			i--;
		}
		Arrays.sort(krArray);
		return krArray;
	}

	// returns an int[] of left most leaf descendants for each node id
	// (a lmld of a node with only a text child is itself)
	public static int[] lmld(Node n, int[] l) {
		//System.out.println(n);
		//for (int i = 0; i < l.length; i++) {
		//	System.out.println(l[i]);
		//}
		if (n.hasChildNodes()
				&& (n.getFirstChild().getNodeType() != Document.TEXT_NODE)) {
			NodeList nl = n.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				if (!(nl.item(i).getNodeType() == Document.TEXT_NODE)) {
					l = lmld(nl.item(i), l);
				}
			}
		}
		if (!n.hasChildNodes()
				|| (n.getFirstChild().getNodeType() == Document.TEXT_NODE)) {
			//System.out.println("trying to add " + id(n) + ", but len of l is "
			//		+ l.length);
			//System.out.println("node is " + n);
			l[id(n)] = id(n);
		} else {
			Node c1 = n.getFirstChild();
			l[id(n)] = l[id(c1)];
		}
		return l;
	}

	// returns the number of non-text nodes in n
	public static int nodes(Node n, int count) {
		if (n.getNodeType() == Document.DOCUMENT_NODE) {
			return count;
		} else if (n.getNodeType() == Document.TEXT_NODE) {
			return 0;
		} else if (!n.hasChildNodes()) {
			return 1;
		} else {
			NodeList nl = n.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				count += nodes(nl.item(i), 0);
			}
			return 1 + count;
		}
	}

	// returns the number of leaves (node having a text as a child is counted as
	// leave)
	public static int leaves(Node n, int leaves) {
		if (n.getNodeType() == Document.DOCUMENT_NODE) {
			return leaves;
		} else if (n.getNodeType() == Document.TEXT_NODE) {
			return 0;
		} else if (!n.hasChildNodes()
				|| (n.getFirstChild().getNodeType() == Document.TEXT_NODE)) {
			return 1;
		} else {
			NodeList nl = n.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				leaves += leaves(nl.item(i), 0);
			}
			return leaves;
		}
	}

	// adds ted_id attributes to all non-text nodes in a depth-first way, i.e.
	// the left-most leaf gets the ted_id 0 and the root gets the highest ted_id
	public static void add_ids(Node n, int j) {
		//System.out.println("add_ids: " + n.getNodeName());
		if (n.getNodeType() == Document.DOCUMENT_NODE) {
			return;
		} else if (n.getNodeType() == Document.TEXT_NODE) {
			if (!n.getNextSibling().equals(null)) {
				add_ids(n.getNextSibling(), j);
			} else {
				add_ids(n.getParentNode(), j);
			}
		} else if (!n.hasChildNodes() || allChildrenSet(n)) {
			Element el = (Element) n;
			el.setAttribute("ted_id", String.valueOf(j));
			//System.out.println("adding ted_id to " + el.getNodeName());
			Node sibling = el.getNextSibling();
			if (!(sibling == null)) {
				add_ids(el.getNextSibling(), j + 1);
			} else {
				add_ids(el.getParentNode(), j + 1);
			}
		} else {
			add_ids(n.getFirstChild(), j);
		}
	}

	// removes ted_id attributes to all non-text nodes in a depth-first way
	public static void remove_ids(Node n, int j) {
		if (n.getNodeType() == Document.DOCUMENT_NODE) {
			return;
		} else if (n.getNodeType() == Document.TEXT_NODE) {
			if (!n.getNextSibling().equals(null)) {
				remove_ids(n.getNextSibling(), j);
			} else {
				remove_ids(n.getParentNode(), j);
			}
		} else if (!n.hasChildNodes() || allChildrenUnSet(n)) {
			Element el = (Element) n;
			el.removeAttribute("ted_id");
			// System.out.println("removing ted_id to " + el.getNodeName());
			Node sibling = el.getNextSibling();
			if (!(sibling == null)) {
				remove_ids(el.getNextSibling(), j + 1);
			} else {
				remove_ids(el.getParentNode(), j + 1);
			}
		} else {
			remove_ids(n.getFirstChild(), j);
		}
	}

	// returns true if all non-text child nodes have attributes ted_id set, and
	// false otherwise
	public static boolean allChildrenSet(Node n) {
		NodeList nl = n.getChildNodes();
		boolean allSet = true;
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeType() != Document.TEXT_NODE) {
				Element el = (Element) nl.item(i);
				if (!el.hasAttribute("ted_id")
						|| (el.hasAttribute("ted_id") && el
								.getAttribute("ted_id") == String.valueOf(-1))) {
					allSet = false;
				}
			}
		}
		return allSet;
	}

	// returns true if all non-text child nodes do not have attributes ted_id
	// set, and
	// false otherwise
	public static boolean allChildrenUnSet(Node n) {
		NodeList nl = n.getChildNodes();
		boolean allUnSet = true;
		for (int i = 0; i < nl.getLength(); i++) {
			if (nl.item(i).getNodeType() != Document.TEXT_NODE) {
				Element el = (Element) nl.item(i);
				if (el.hasAttribute("ted_id")) {
					allUnSet = false;
				}
			}
		}
		return allUnSet;
	}

	// returns the value of attribute ted_id of a node if it exists and -1
	// otherwise
	public static int id(Node n) {
		Element el = (Element) n;
		Integer res = -1;
		if (el.hasAttribute("ted_id")) {
			res = Integer.valueOf(el.getAttribute("ted_id"));
		}
		return res;
	}

	/*
	public static void main(String[] args) throws ParserConfigurationException,
			SAXException, IOException, XPathExpressionException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		String xmlFile1 = "./data/a1.xml";
		String xmlFile2 = "./data/a2.xml";
		Document d1 = builder.parse(new File(xmlFile1));
		Document d2 = builder.parse(new File(xmlFile2));
		TED tester = new TED();
		Node root1 = d1.getDocumentElement();
		Node root2 = d2.getDocumentElement();
		DeltaXML.deleteEmptyChildren(d1);
		DeltaXML.deleteEmptyChildren(d2);
		TED.add_ids(root1, 0);
		TED.add_ids(root2, 0);
		System.out.println("TED of a1 and a2 " + TED.ted(root1, root2));
	}
	*/
}
