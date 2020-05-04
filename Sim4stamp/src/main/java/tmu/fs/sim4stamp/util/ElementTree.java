/*
 *   sim4stamp - The simulation tool for STAMP/STPA
 *   Copyright (C) 2016  Keiichi Tsumuta
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package tmu.fs.sim4stamp.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tmu.fs.sim4stamp.model.co.Connector;
import tmu.fs.sim4stamp.model.em.Element;

/**
 *
 * @author Keiichi Tsumuta
 */
public class ElementTree {

	private List<Element> elements;
	private Map<String, Element> map;
	private List<Connector> connectors;
	private Tree root;
	private volatile boolean[] flags = null;
	private List<Element> series = null;

	public ElementTree() {

	}

	public void setTree(List<Element> elems, List<Connector> conns) {
		Map<String, Connector> emap = new HashMap<>();
		// 両端接続のコネクタのみ対象とする。
		connectors = new ArrayList<>();
		for (Connector c : conns) {
			String fromId = c.getNodeFromId();
			String toId = c.getNodeToId();
			if (fromId != null || toId != null) {
				connectors.add(c);
				emap.put(fromId, c);
				emap.put(toId, c);
			}
		}
		// 孤立しているノードは対象外とする
		elements = new ArrayList<>();
		for (Element e : elems) {
			if (emap.containsKey(e.getNodeId())) {
				elements.add(e);
			}
		}

		map = new HashMap<>();
		for (Element el : elements) {
			el.setTempFlag(false);
			map.put(el.getNodeId(), el);
		}
		for (Element e : elems) {
			e.setOrder(0);
		}
		root = getRoot();
		if (root == null) {
			return;
		}
		root.getElement().setOrder(1);
		flags = new boolean[connectors.size()];
		root.element.setTempFlag(true);
		findChildren(root);
		//System.out.println("tree make:" + root.toString());
		series = new ArrayList<>();
		List<Element> temp = new ArrayList<>();
		List<Tree> rootChildren = root.getChildren();
		for (Tree tree : rootChildren) {
			temp.add(root.getElement());
			tree.getSeries(temp);
		}
		int order = 2;
		for (Element el : temp) {
			if (el.getType() != Element.EType.INJECTOR) {
				order = Math.max(el.getOrder(), order);
				series.add(el);
			}
		}
		order++;
		for (Element el : temp) {
			if (el.getType() == Element.EType.INJECTOR) {
				if (el.getOrder() != 1) {
					el.setOrder(order++);
				}
				series.add(el);
			}
		}
	}

	private void findChildren(Tree tree) {
		String id = tree.element.getNodeId();
		int order = tree.element.getOrder();
		//System.out.println("tree:" + id + "(" + order + ")");
		for (int i = 0; i < connectors.size(); i++) {
			if (flags[i]) {
				continue;
			}
			Connector con = connectors.get(i);
			String fromId = con.getNodeFromId();
			if (fromId == null) {
				continue;
			}
			String toId = con.getNodeToId();
			if (toId == null) {
				continue;
			}
			if (id.equals(toId)) {
				Element el = map.get(fromId);
				if (el == null) {
					continue;
				}
				if (el.isTempFlag() && el.getType() == Element.EType.CONTROLLED_EQUIPMENT) {
					continue;
				}
				if (el.getOrder() == 0) {
					el.setOrder(order + 1);
				}
				flags[i] = true;
				Tree child = new Tree(el);
				tree.addChild(child);
			}
		}
		tree.sortChildren();
		//for (Tree child : tree.children) {
		//   System.out.println("tree child:" + child.getElement().getNodeId());
		//}
		for (Tree child : tree.children) {
			findChildren(child);
		}
	}

	private Tree getRoot() {
		Tree root = null;
		// ルートElement設定されているものを検索
		for (Element ele : elements) {
			if (ele.isRootElement()) {
				return new Tree(ele);
			}
		}
		// ルートElement設定なしのケース
		List<Element> ceList = new ArrayList<>();
		for (Element ele : elements) {
			if (ele.getType() == Element.EType.CONTROLLED_EQUIPMENT) {
				ceList.add(ele);
			}
		}
		if (ceList.size() > 0) {
			ceList.sort((a, b) -> b.getNodeId().compareTo(a.getNodeId()));
			root = new Tree(ceList.get(0));
		}
		return root;
	}

	public List<Element> getSeries() {
		return series;
	}

	private static final Element.EType[] ETYPE_ORDERS = {Element.EType.INJECTOR, Element.EType.SENSOR,
		Element.EType.CONTROLLER, Element.EType.ACTUATOR, Element.EType.CONTROLLED_EQUIPMENT};

	class Tree {

		private Element element;
		private List<Tree> children = new ArrayList<>();

		public Tree(Element element) {
			this.element = element;
		}

		/**
		 * @return the element
		 */
		public Element getElement() {
			return element;
		}

		/**
		 * @param element the element to set
		 */
		public void setElement(Element element) {
			this.element = element;
		}

		public void addChild(Tree child) {
			children.add(child);
		}

		public List<Tree> getChildren() {
			return children;
		}

		public void getSeries(List<Element> elementList) {
			elementList.add(element);
			for (Tree child : children) {
				child.getSeries(elementList);
			}
		}

		public void sortChildren() {
			List<Tree> newList = new ArrayList<>();
			for (Element.EType etype : ETYPE_ORDERS) {
				for (Tree t : children) {
					if (t.element.getType() == etype) {
						newList.add(t);
					}
				}
			}
			children = newList;
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(element.getNodeId()).append("(").append(element.getOrder()).append(")");
			for (Tree child : children) {
				sb.append(" - ").append(child.toString()).append("\n");
			}
			return sb.toString();
		}
	}
}
