import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;


@SuppressWarnings("unchecked")
public class BTree<T extends Comparable<T>> {

    final private int maxDegree;

    protected Node<T> root = null;
    protected int size = 0;
    
    //You may add fields here.
    protected ArrayDeque<T> added;
    protected ArrayDeque<Integer> numOfSplited;
    protected ArrayDeque<T> splited;

    /**
     * Default Constructor for a 2-3 B-Tree.
     */
    public BTree() { 
        this(2);
    }

    /**
     * Constructor for B-Tree of ordered parameter. Order here means minimum
     * number of children in a non-root node.
     *
     * @param order
     *            the minimal degree of a node of the B-Tree.
     */
    public BTree(int order) {
        if (order < 2) {
            throw new IllegalArgumentException("Illegal BTree order: " + order);
        }
        
        maxDegree = 2 * order;
    }

    //You may add line of code to the "insert" function below.

    /**
     * Insert the value into this BTree
     * 
     * @param value - the inserted value
     */
    public void insert(T value) {
    	int counter = 0;                    // counting splitting nodes
        if (root == null) {
            root = new Node<T>(null, maxDegree);
            root.addKey(value);
        } else {
            Node<T> currentNode = root;
            boolean wasAdded = false;
            while (currentNode != null && !wasAdded) {
            	
            	// If the node has 2t-1 keys then split it
                if (currentNode.getNumberOfKeys() == maxDegree - 1) {
                	
                	split(currentNode);
                	int medianIndex = currentNode.getNumberOfKeys() / 2;
                	splited.addFirst(currentNode.getKey(medianIndex));
                    counter = counter + 1; //

                	// Return to the parent and descend to the needed node
                	currentNode = currentNode.parent != null ? currentNode.parent : root;
                    int idx = currentNode.getValuePosition(value);
                    currentNode = currentNode.getChild(idx);
                }
                
                // Descend the tree and add the key to a leaf
                if (currentNode.isLeaf()) {
                	currentNode.addKey(value);
                	wasAdded = true;
                } else {
                    int idx = currentNode.getValuePosition(value);
                    currentNode = currentNode.getChild(idx);
                }
            }
        }

        size++;
        numOfSplited.addFirst(counter); //
        added.addFirst(value);
    }
    
    /**
     * Split a node in its median value
     *
     * @param nodeToSplit
     *            a node with (2 * order - 1) keys
     */
    private T split(Node<T> nodeToSplit) {
        Node<T> node = nodeToSplit;
        int numberOfKeys = node.getNumberOfKeys();
        int medianIndex = numberOfKeys / 2;
        T medianValue = node.getKey(medianIndex);

        Node<T> left = createPartialNodeCopy(node, 0, medianIndex);
        Node<T> right = createPartialNodeCopy(node, medianIndex + 1, numberOfKeys);
        
        Node<T> parent = null;
        if (node.parent == null) {
            // create a new root
            parent = new Node<T>(null, maxDegree);
            node.parent = parent;
            root = parent;
        } else {
            // Remove the median from the parent if non-root
            parent = node.parent;
            parent.removeChild(node);
        }
        
        // Move the median value up to the parent
        parent.addKey(medianValue);
        parent.addChild(left);
        parent.addChild(right);

        return medianValue;
    }
    
    private Node<T> createPartialNodeCopy(Node<T> origin, int startIdx, int endIdx) {
        Node<T> copy = new Node<T>(null, maxDegree);
        for (int i = startIdx; i < endIdx; i++) {
            copy.addKey(origin.getKey(i));
        }
        
        if (origin.getNumberOfChildren() > 0) {
            for (int j = startIdx; j <= endIdx; j++) {
                Node<T> child = origin.getChild(j);
                copy.addChild(child);
            }
        }
        
        return copy;
    }
    

    /**
     * {@inheritDoc}
     */
    public boolean contains(T value) {
        Node<T> node = getNode(value);
        return (node != null);
    }

    /**
     * Get the node with value.
     *
     * @param value
     *            to find in the tree.
     * @return Node<T> with value.
     */
    protected Node<T> getNode(T value) {
        Node<T> node = root;
        boolean found = false;
        
        while (node != null && !found) {
            int idx = node.getValuePosition(value);
            if (idx < node.getNumberOfKeys() && node.getKey(idx) == value) {
                found = true;
            } else if (!node.isLeaf()){
                node = node.getChild(idx);
            } else {
                node = null;
            }
        }
        
        return node;
    }


    /**
     * {@inheritDoc}
     */
    public int size() {
        return size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return TreePrinter.getString(this);
    }
    
    protected boolean shouldSplit(Node<T> node) {
        return node.getNumberOfKeys() == maxDegree - 1;
    }

    protected static class Node<T extends Comparable<T>> {

    	protected T[] keys = null;
    	protected int numOfKeys = 0;
    	protected Node<T>[] children = null;
    	protected int numOfChildren = 0;
    	protected Comparator<Node<T>> comparator = new Comparator<Node<T>>() {
            public int compare(Node<T> arg0, Node<T> arg1) {
                return arg0.getKey(0).compareTo(arg1.getKey(0));
            }
        };

        Node<T> parent = null;

        Node(Node<T> parent, int maxDegree) {
            this.parent = parent;
            this.keys = (T[]) new Comparable[maxDegree];
            this.numOfKeys = 0;
            this.children = new Node[maxDegree+1];
            this.numOfChildren = 0;
        }

        T getKey(int index) {
            return keys[index];
        }

        int indexOf(T value) {
            for (int i = 0; i < numOfKeys; i++) {
                if (keys[i].equals(value)) return i;
            }
            return -1;
        }

        int indexOf(Node<T> child) {
            for (int i = 0; i < numOfChildren; i++) {
                if (children[i].equals(child))
                    return i;
            }
            return -1;
        }

        void addKey(T value) {
            keys[numOfKeys++] = value;
            Arrays.sort(keys, 0, numOfKeys);
        }

        T removeKey(T value) {
            T removed = null;
            boolean found = false;
            if (numOfKeys == 0) return null;
            for (int i = 0; i < numOfKeys; i++) {
                if (keys[i].equals(value)) {
                    found = true;
                    removed = keys[i];
                } else if (found) {
                    // shift the rest of the keys down
                    keys[i - 1] = keys[i];
                }
            }
            if (found) {
            	numOfKeys--;
                keys[numOfKeys] = null;
            }
            return removed;
        }

        T removeKey(int index) {
            if (index >= numOfKeys)
                return null;
            T value = keys[index];
            for (int i = index + 1; i < numOfKeys; i++) {
                // shift the rest of the keys down
                keys[i - 1] = keys[i];
            }
            numOfKeys--;
            keys[numOfKeys] = null;
            return value;
        }

        int getNumberOfKeys() {
            return numOfKeys;
        }

        Node<T> getChild(int index) {
            return index < numOfChildren ? children[index] : null;
        }
        
        private int getValuePosition(T value) {
            boolean found = false;
            int i = 0;
            
            while(i < getNumberOfKeys() && !found) {
                if(value.compareTo(getKey(i)) <= 0){
                    found = true;
                } else {
                    i++;
                }
            }
            
            return i;
        }

        boolean addChild(Node<T> child) {
            child.parent = this;
            children[numOfChildren++] = child;
            Arrays.sort(children, 0, numOfChildren, comparator);
            return true;
        }

        boolean removeChild(Node<T> child) {
            boolean found = false;
            
            for (int i = 0; i < numOfChildren; i++) {
                if (children[i].equals(child)) {
                    found = true;
                } else if (found) {
                    children[i - 1] = children[i];
                }
            }
            
            if (found) {
                numOfChildren--;
                children[numOfChildren] = null;
            }
            
            return found;
        }

        Node<T> removeChild(int index) {
            if (index >= numOfChildren)
                return null;
            Node<T> value = children[index];
            children[index] = null;
            for (int i = index + 1; i < numOfChildren; i++) {
                // shift the rest of the keys down
                children[i - 1] = children[i];
            }
            numOfChildren--;
            children[numOfChildren] = null;
            return value;
        }

        public int getNumberOfChildren() {
            return numOfChildren;
        }
        
        public boolean isLeaf() {
            return getNumberOfChildren() == 0;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();

            builder.append("keys=[");
            for (int i = 0; i < getNumberOfKeys(); i++) {
                T value = getKey(i);
                builder.append(value);
                if (i < getNumberOfKeys() - 1)
                    builder.append(", ");
            }
            builder.append("]\n");

            if (parent != null) {
                builder.append("parent=[");
                for (int i = 0; i < parent.getNumberOfKeys(); i++) {
                    T value = parent.getKey(i);
                    builder.append(value);
                    if (i < parent.getNumberOfKeys() - 1)
                        builder.append(", ");
                }
                builder.append("]\n");
            }

            if (children != null) {
                builder.append("keySize=").append(getNumberOfKeys()).append(" children=").append(getNumberOfChildren()).append("\n");
            }

            return builder.toString();
        }
    }

    private static class TreePrinter {
        public static <T extends Comparable<T>> String getString(BTree<T> bTree) {
            return bTree.root == null ? "Empty tree" : getString(bTree.root, "", true);
        }

        private static <T extends Comparable<T>> String getString(Node<T> node, String prefix, boolean isTail) {
            StringBuilder builder = new StringBuilder();

            builder.append(prefix).append((isTail ? "~~~ " : "|-- "));
            for (int i = 0; i < node.getNumberOfKeys(); i++) {
                T value = node.getKey(i);
                builder.append(value);
                if (i < node.getNumberOfKeys() - 1)
                    builder.append(", ");
            }
            builder.append("\n");

            if (node.children != null) {
                for (int i = 0; i < node.getNumberOfChildren() - 1; i++) {
                    Node<T> obj = node.getChild(i);
                    builder.append(getString(obj, prefix + (isTail ? "    " : "|   "), false));
                }
                
                if (node.getNumberOfChildren() >= 1) {
                    Node<T> obj = node.getChild(node.getNumberOfChildren() - 1);
                    builder.append(getString(obj, prefix + (isTail ? "    " : "|   "), true));
                }
            }

            return builder.toString();
        }
    }
}