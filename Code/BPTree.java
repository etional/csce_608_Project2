import java.util.Arrays;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.lang.*;
import java.util.Random;

// class for B+Tree
public class BPTree
{
   public int order; // order of a tree
   public Node root; // root of a tree
   
   /*
    * Create B+Tree
    */
   public BPTree(int order_in, int[] keys_in, String type)
   {
      order = order_in;
      construct(keys_in, type);
   }
   
   /*
    * Construct a tree
    */
   public void construct(int[] keys, String type) {
      // dense tree - no-root nodes are as full as possible
      if (type.equals("dense")) {         
         // Create leaves
         List<Node> leaves = new ArrayList<Node>();
         int num_full = keys.length / order;
         int remainder = keys.length % order;
         List<Integer> notfull = new ArrayList<Integer>();
         if (remainder < (order + 1) / 2) {
            notfull.add(order - (order + 1) / 2 + remainder);
            notfull.add((order + 1) / 2);
            num_full--;
         }
         else {
            notfull.add(remainder);
         }
         
         for (int i = 0; i < num_full; i++) {
            Node node = new Node(order);
            node.IS_LEAF = true;
            for (int j = 0; j < order; j++) {
               node.insert(keys[i * order + j], null);
            }
            leaves.add(node);
         }
         
         int previous = 0;
         for (int i = 0; i < notfull.size(); i++) {
            Node node = new Node(order);
            node.IS_LEAF = true;
            for (int j = 0; j < notfull.get(i); j++) {
               node.insert(keys[order * num_full + j + previous], null);
            }
            leaves.add(node);
            previous += notfull.get(i);
         }
         
         for (int i = 0; i < leaves.size() - 1; i++) {
            leaves.get(i).ptrs[order] = leaves.get(i + 1);
         }
         
         List<Node> children = new ArrayList<Node>(leaves);
         int num_parents = (int)Math.ceil(leaves.size() / (double)(order + 1));
         
         // Create non-leaf nodes
         while (num_parents != 1) {
            List<Node> parents = new ArrayList<Node>();
            for (int i = 0; i < num_parents; i++) {
               Node node = new Node(order);
               node.IS_LEAF = false;
               for (int j = 0; j < order + 1; j++) {
                  if (i * (order + 1) + j + 1 == children.size()) {
                     node.ptrs[j] = children.get(i * (order + 1) + j);
                     children.get(i * (order + 1) + j).parent = node;
                     break;
                  }
                  if (j < order) {
                     node.ptrs[j] = children.get(i * (order + 1) + j);
                     Node child = children.get(i * (order + 1) + j + 1);
                     while (!child.IS_LEAF) {
                        child = child.ptrs[0];
                     }
                     node.keys[j] = child.keys[0];
                     node.occupied++;
                     children.get(i * (order + 1) + j).parent = node;
                  }
                  else {
                     node.ptrs[j] = children.get(i * (order + 1) + j);
                     children.get(i * (order + 1) + j).parent = node;
                  }
               }
               parents.add(node);
            }
            children = new ArrayList<Node>(parents);
            num_parents = (int)Math.ceil(parents.size() / (double)(order + 1));
         }
         
         // Create root
         Node last = new Node(order);
         last.IS_LEAF = false;
         for (int i = 0; i < children.size(); i++) {
            if (i < children.size() - 1) {
               last.ptrs[i] = children.get(i);
               Node child = children.get(i + 1);
               while (!child.IS_LEAF) {
                  child = child.ptrs[0];
               }
               last.keys[i] = child.keys[0];
               last.occupied++;
               children.get(i).parent = last;
            }
            else {
               last.ptrs[i] = children.get(i);
               children.get(i).parent = last;
            }
         }
         root = last;
         
         System.out.println("root: " + Arrays.toString(root.keys));
      }
      
      // sparse tree - no-root nodes have ceil((n + 1) / 2) pointers
      else {
         // Create leaves
         List<Node> leaves = new ArrayList<Node>();
         int minimum = (order + 1) / 2;
         int num_sparse = keys.length / minimum;
         int remainder = keys.length % minimum;
         int larger = 0;
         if (remainder < minimum) {
            larger = remainder;
            num_sparse -= remainder;
            remainder = 0;
         }
         
         for (int i = 0; i < num_sparse; i++) {
            Node node = new Node(order);
            node.IS_LEAF = true;
            for (int j = 0; j < minimum; j++) {
               node.insert(keys[i * minimum + j], null);
            }
            leaves.add(node);
         }
         
         int previous = 0;
         for (int i = 0; i < larger; i++) {
            Node node = new Node(order);
            node.IS_LEAF = true;
            for (int j = 0; j < minimum + 1; j++) {
               node.insert(keys[minimum * num_sparse + i * j], null);
            }
            leaves.add(node);
         }
         
         for (int i = 0; i < leaves.size() - 1; i++) {
            leaves.get(i).ptrs[order] = leaves.get(i + 1);
         }
         
         List<Node> children = new ArrayList<Node>(leaves);
         int num_parents = (int)Math.ceil(leaves.size() / (double)(minimum + 1));
         
         // Create non-leaf nodes
         while (num_parents != 1) {
            List<Node> parents = new ArrayList<Node>();
            for (int i = 0; i < num_parents; i++) {
               Node node = new Node(order);
               node.IS_LEAF = false;
               for (int j = 0; j < minimum + 1; j++) {
                  if (i * (minimum + 1) + j + 1 == children.size()) {
                     node.ptrs[j] = children.get(i * (minimum + 1) + j);
                     children.get(i * (minimum + 1) + j).parent = node;
                     break;
                  }
                  if (j < order) {
                     node.ptrs[j] = children.get(i * (minimum + 1) + j);
                     Node child = children.get(i * (minimum + 1) + j + 1);
                     while (!child.IS_LEAF) {
                        child = child.ptrs[0];
                     }
                     node.keys[j] = child.keys[0];
                     node.occupied++;
                     children.get(i * (minimum + 1) + j).parent = node;
                  }
                  else {
                     node.ptrs[j] = children.get(i * (order + 1) + j);
                     children.get(i * (minimum + 1) + j).parent = node;
                  }
               }
               parents.add(node);
            }
            children = new ArrayList<Node>(parents);
            num_parents = (int)Math.ceil(parents.size() / (double)(minimum + 1));
         }
         
         // Create root
         Node last = new Node(order);
         last.IS_LEAF = false;
         for (int i = 0; i < children.size(); i++) {
            if (i < children.size() - 1) {
               last.ptrs[i] = children.get(i);
               Node child = children.get(i + 1);
               while (!child.IS_LEAF) {
                  child = child.ptrs[0];
               }
               last.keys[i] = child.keys[0];
               last.occupied++;
               children.get(i).parent = last;
            }
            else {
               last.ptrs[i] = children.get(i);
               children.get(i).parent = last;
            }
         }
         root = last;
         
         System.out.println("root: " + Arrays.toString(root.keys));
      }
   }
   
   /*
    * Search a key in a tree
    */
   public int search(int key_in, Node node_in) {
      System.out.println(Arrays.toString(node_in.keys));
      if (node_in.IS_LEAF) {
         System.out.println("--- Above node is a leaf ---");
         boolean found = false;
         int pos = 0;
         for (int i = 0; i < node_in.occupied; i++) {
            if (key_in == node_in.keys[i]) {
               found = true;
               pos = i;
               break;
            }
         }
         
         if (found) {
            System.out.println("There is a key at " + String.valueOf(pos));
            return node_in.keys[pos];
         }
         else {
            System.out.println("There is no such key");
            return 0;
         }
         
      }
      else {
         int pos = 0;
         boolean found = false;
         for (int i = 0; i < node_in.occupied; i++) {
            if (key_in < node_in.keys[i]) {
               pos = i;
               found = true;
               break;
            }
         }
         if (!found) {
            pos = node_in.occupied;
         }
         System.out.println("Location: " + String.valueOf(pos));
         return search(key_in, node_in.ptrs[pos]);
      }
   }
   
   /*
    * Search keys in range
    */
   public List<Integer> range_search(int key1, int key2, Node node_in) {
      System.out.println(Arrays.toString(node_in.keys));
      List<Integer> result = new ArrayList<Integer>();
      if (node_in.IS_LEAF) {
         System.out.println("--- Above node is a leaf ---");
         int i = 0;
         int key = node_in.keys[i];
         Node node = node_in;
         while (key < key2) {
            if (key >= key1) {
               result.add(key);
            }
            if (i < node.occupied - 1) {
               i++;
               key = node.keys[i];
            }
            else {
               i = 0;
               node = node.ptrs[order];
               System.out.println(Arrays.toString(node.keys));
               key = node.keys[i];
            }
         }
         return result;
      }
      else {
         int pos = 0;
         boolean found = false;
         for (int i = 0; i < order; i++) {
            if (key1 < node_in.keys[i]) {
               pos = i;
               found = true;
               break;
            }
         }
         if (!found) {
            pos = order;
         }
         System.out.println("Location: " + String.valueOf(pos));
         return range_search(key1, key2, node_in.ptrs[pos]);
      }
   }

   /*
    * Insert a new key
    */
   public AbstractMap.SimpleEntry <Integer, Node> insert(int key_in, Node node_in) {
      System.out.println(Arrays.toString(node_in.keys));
      // If a node is a leaf
      if (node_in.IS_LEAF) {
         System.out.println("--- Above node is a leaf ---");
         // Insert a key if the node is not full
         if (node_in.occupied < order) {
            System.out.println("Before: " + Arrays.toString(node_in.keys));
            node_in.insert(key_in, null);
            System.out.println("After: " + Arrays.toString(node_in.keys));
            AbstractMap.SimpleEntry <Integer, Node> pa = new AbstractMap.SimpleEntry <Integer, Node> (0, null);
            return pa;
         }
         // If the node is full
         else {
            // re-arrange
            int[] arrange = new int[order + 1];
            int j = 0;
            boolean inserted = false;
            boolean all_inserted = false;
            for (int i = 0; i < order + 1; i++) {
               if (all_inserted || (key_in < node_in.keys[j] && !inserted)) {
                  arrange[i] = key_in;
                  inserted = true;
               }
               else {
                  arrange[i] = node_in.keys[j];
                  j++;
                  if (j == order) {
                     all_inserted = true;
                  }
               }
            }
            // create a new node
            Node p = new Node(order);
            p.IS_LEAF = true;
            int index = (int) Math.floor((order + 1) / 2.0);
            for (int i = index; i < order + 1; i++) {
               p.insert(arrange[i], null);
            }
            p.ptrs[order] = new Node(node_in.ptrs[order]);
            
            System.out.println("Before: " + Arrays.toString(node_in.keys));
            for (int i = 0; i < order; i++) {
               if (i < index) {
                  node_in.keys[i] = arrange[i];
               }
               else {
                  node_in.keys[i] = 0;
                  node_in.occupied--;
               }
            }
            System.out.println("After: " + Arrays.toString(node_in.keys));
            System.out.println("       " + Arrays.toString(p.keys));
            node_in.ptrs[order] = p;
            
            // If the node is a root
            if (node_in == root) {
               Node newRoot = new Node(order);
               newRoot.IS_LEAF = false;
               newRoot.parent = null;
               newRoot.keys[0] = p.keys[0];
               newRoot.ptrs[0] = node_in;
               newRoot.ptrs[1] = p;
               newRoot.occupied = 1;
               node_in.parent = newRoot;
               p.parent = newRoot;
               root = newRoot;
               AbstractMap.SimpleEntry <Integer, Node> pa = new AbstractMap.SimpleEntry <Integer, Node> (0, null);
               return pa;
            }
            else {
               p.parent = node_in.parent;
               AbstractMap.SimpleEntry <Integer, Node> pa = new AbstractMap.SimpleEntry <Integer, Node> (p.keys[0], p);
               return pa;
            }
         }
      }
      // a node is not a leaf
      else {
         int pos = 0;
         boolean found = false;
         for (int i = 0; i < node_in.occupied; i++) {
            if (key_in < node_in.keys[i]) {
               found = true;
               pos = i;
               break;
            }
         }
         if (!found) {
            pos = node_in.occupied;
         }
         System.out.println("Location: " + String.valueOf(pos));
         // recursion - do insertion at its child node
         AbstractMap.SimpleEntry <Integer, Node> pa = insert(key_in, node_in.ptrs[pos]);
         key_in = pa.getKey();
         if (pa.getValue() == null) {
            return pa;
         }
         // Inserat a key if the node is not full
         else if (node_in.occupied < order) {
            System.out.println("Before: " + Arrays.toString(node_in.keys));
            node_in.insert(pa.getKey(), pa.getValue());
            System.out.println("After: " + Arrays.toString(node_in.keys));
            AbstractMap.SimpleEntry <Integer, Node> result = new AbstractMap.SimpleEntry <Integer, Node> (0, null);
            return result;
         }
         // if the node is full
         else {
            // re-arrange
            int[] arrange = new int[order + 1];
            Node[] arrange_ptr = new Node[order + 2];
            int location = 0;
            boolean locate = false;
            for (int i = 0; i < node_in.keys.length; i++) {
               if(key_in < node_in.keys[i]) {
                  locate = true;
                  location = i;
                  break;
               }
            }
            if (!locate) {
               location = node_in.keys.length;
            }
            for (int i = 0; i < arrange.length; i++) {
               if (i < location) {
                  arrange[i] = node_in.keys[i];
               }
               else if (i == location) {
                  arrange[i] = key_in;
               }
               else {
                  arrange[i] = node_in.keys[i - 1];
               }
            }
            for (int i = 0; i < arrange_ptr.length; i++) {
               if (i < location + 1) {
                  arrange_ptr[i] = new Node(node_in.ptrs[i]);
               }
               else if (i == location + 1) {
                  arrange_ptr[i] = pa.getValue();
               }
               else {
                  arrange_ptr[i] = new Node(node_in.ptrs[i - 1]);
               }
            }
            
            // create a new node
            Node p = new Node(order);
            p.IS_LEAF = false;
            int index = (int) Math.floor((order + 1) / 2.0);
            p.ptrs[0] = new Node(node_in.ptrs[index]);
            for (int i = index; i < order + 1; i++) {
               p.insert(arrange[i], arrange_ptr[i + 1]);
            }           
            p.ptrs[order] = new Node(node_in.ptrs[order]);
            
            System.out.println("Before: " + Arrays.toString(node_in.keys));
            node_in = new Node(order);
            node_in.ptrs[0] = arrange_ptr[0];
            for (int i = 0; i < index; i++) {
               node_in.insert(arrange[i], arrange_ptr[i + 1]);
            }
            System.out.println("After: " + Arrays.toString(node_in.keys));
            System.out.println("       " + Arrays.toString(p.keys));
            node_in.ptrs[order] = p;
            
            // if the node is a root   
            if (node_in == root) {
                Node newRoot = new Node(order);
                newRoot.IS_LEAF = false;
                newRoot.parent = null;
                newRoot.keys[0] = p.keys[0];
                newRoot.ptrs[0] = node_in;
                newRoot.ptrs[1] = p;
                node_in.parent = newRoot;
                newRoot.occupied = 1;
                p.parent = newRoot;
                root = newRoot;
                pa = new AbstractMap.SimpleEntry <Integer, Node> (0, null);
                return pa;
            }
            else {
                p.parent = node_in.parent;
                pa = new AbstractMap.SimpleEntry <Integer, Node> (p.keys[0], p);
                return pa;
            }
         }
      }
   }
   
   /*
    * Delete a key
    */
   public boolean delete_key(int key_in, Node node_in) {
      System.out.println(Arrays.toString(node_in.keys));
      // if the node is a leaf
      if (node_in.IS_LEAF) {
         System.out.println("--- Above node is a leaf ---");
         boolean found = node_in.delete_key(key_in);
         if (!found) {
            System.out.println("There is no such key");
            return false;
         }
         // the node has at least a minimum number of keys
         if (node_in.occupied >= (int) Math.floor((order + 1) / 2.0) || node_in == root) {
            return false;
         }
         // the node has less than a minimum number of keys
         else {
            return true;
         }
      }
      // the node is not a leaf
      else {
         int pos = 0;
         boolean found = false;
         for (int i = 0; i < order; i++) {
            if (key_in < node_in.keys[i]) {
               pos = i;
               found = true;
               break;
            }
         }
         if (!found) {
            pos = node_in.occupied;
         }
         System.out.println("Location: " + String.valueOf(pos));
         Node pi = node_in.ptrs[pos];
         boolean belowmin = delete_key(key_in, pi);
         // half-full condition is satisfied
         if (!belowmin) {
            return false;
         }
         else {
            // check adjacent sibling
            Node sibling;
            boolean is_right;
            if (pos == order) {
               sibling = node_in.ptrs[pos - 1];
               is_right = false;
            }
            else {
               sibling = node_in.ptrs[pos + 1];
               is_right = true;
            }
            // key re-distribution
            if (sibling.occupied > (int) Math.ceil((order + 1) / 2.0)) {
               if (is_right) {
                  System.out.println("Current node:");
                  System.out.println("Before: " + Arrays.toString(pi.keys));
                  pi.insert(sibling.keys[0], sibling.ptrs[0]);
                  System.out.println("After: " + Arrays.toString(pi.keys));
                  System.out.println("Adjacent Sibling:");
                  System.out.println("Before: " + Arrays.toString(sibling.keys));
                  sibling.delete_key(sibling.keys[0]);
                  System.out.println("After: " + Arrays.toString(sibling.keys));
                  node_in.keys[pos] = sibling.keys[0];
               }
               else {
                  System.out.println("Current node:");
                  System.out.println("Before: " + Arrays.toString(pi.keys));
                  pi.insert(sibling.keys[sibling.occupied - 1], sibling.ptrs[sibling.occupied - 1]);
                  System.out.println("After: " + Arrays.toString(pi.keys));
                  System.out.println("Adjacent Sibling:");
                  System.out.println("Before: " + Arrays.toString(sibling.keys));
                  sibling.delete_key(sibling.keys[sibling.occupied - 1]);
                  System.out.println("After: " + Arrays.toString(sibling.keys));
                  node_in.keys[pos] = sibling.keys[sibling.occupied - 1];
               }
               return false;
            }
            // coalescence
            else {
               System.out.println("Before: " + Arrays.toString(pi.keys));
               for (int i = 0; i < sibling.occupied; i++) {
                  pi.insert(sibling.keys[i], sibling.ptrs[i]);
               }
               System.out.println("After: " + Arrays.toString(pi.keys));
               if (is_right) {
                  pi.ptrs[order] = sibling.ptrs[order];
                  System.out.println("Before: " + Arrays.toString(sibling.keys));
                  sibling = null;
                  System.out.println("After: Empty");
                  pi.parent.delete_key(pi.parent.keys[pos]);
               }
               else {
                  sibling.ptrs[order] = pi.ptrs[order];
                  System.out.println("Before: " + Arrays.toString(sibling.keys));
                  pi = null;
                  System.out.println("After: Empty");
                  sibling.parent.delete_key(sibling.parent.keys[pos]);
                  
               }
               if (node_in == root && node_in.occupied == 1) {
                  root = pi;
                  return false;
               }
               if (node_in.occupied + 1 >= (int) Math.ceil((order + 1) / 2.0) || node_in == root) {
                  return false;
               }
               else {
                  return true;
               }
            }
         }
      }
   }
}