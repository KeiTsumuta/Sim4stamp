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
import java.util.logging.Logger;
import tmu.fs.sim4stamp.model.co.Connector;
import tmu.fs.sim4stamp.model.em.Element;

/**
 *
 * @author Keiichi Tsumuta
 */
public class ElementTree {

    private static Logger log = Logger.getLogger(ElementTree.class.getPackage().getName());

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

        root = getRoot();
        if (root != null) {
            root.getElement().setOrder(1);
            flags = new boolean[connectors.size()];
            root.element.setTempFlag(true);
            findChildren(root);
            setTreeOrder(root, 1);
            log.info("tree make:" + root.toString());
            series = new ArrayList<>();
            List<Tree> rootChildren = root.getChildren();
            for (Tree tree : rootChildren) {
                series.add(root.getElement());
                tree.getSeries(series);
            }
        }
    }

    private void findChildren(Tree tree) {
        String id = tree.element.getNodeId();
        int order = tree.element.getOrder();
        for (int i = 0; i < connectors.size(); i++) {
            if (flags[i]) {
                continue;
            }
            Connector con = connectors.get(i);
            String fromId = con.getNodeFromId();
            String toId = con.getNodeToId();
            if (fromId == null || toId == null) {
                continue;
            }
            if (id.equals(toId)) {
                Element el = map.get(fromId);
                if (el.isTempFlag()) {
                    continue;
                }
                el.setOrder(order + 1);
                flags[i] = true;
                Tree child = new Tree(el);
                tree.addChild(child);
                findChildren(child);
            }
        }
    }

    private Tree getRoot() {
        Tree root = null;
        for (Element ele : elements) {
            if (ele.getType() == Element.EType.CONTROLLED_EQUIPMENT) {
                root = new Tree(ele);
                break;
            }
        }
        return root;
    }

    private void setTreeOrder(Tree tree, int order) {
        Element el = tree.getElement();
        el.setOrder(order);
        List<Tree> children = tree.getChildren();
        for (Tree child : children) {
            int next = order + 1;
            int childOrder = child.getElement().getOrder();
            if (next < childOrder) {
                next = childOrder;
            }
            setTreeOrder(child, next);
        }
    }

    public List<Element> getSeries() {
        return series;
    }

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
