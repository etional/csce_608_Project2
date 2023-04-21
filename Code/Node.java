import java.util.Arrays;

// class for node
public class Node
{
   public boolean IS_LEAF; // indicates whether the node is a leaf or not
   public Node parent; // indicates which node points this node
   public int[] keys; // holds keys
   public Node[] ptrs; // holds pointers
   public int occupied; // indicates the number of keys in the node
   
   /*
    * Create Node.
    * empty keys are 0 and empty pointers are null
    */
   public Node(int order_in)
   {
      keys = new int[order_in];
      ptrs = new Node[order_in + 1];
      for (int i = 0; i < order_in; i++) {
         keys[i] = 0;
      }
      for (int i = 0; i < order_in + 1; i++) {
         ptrs[i] = null;
      }
      occupied = 0;
   }
   
   /*
    * Copy Node.
    */
   public Node(Node node_in)
   {
      keys = node_in.keys;
      ptrs = node_in.ptrs;
      IS_LEAF = node_in.IS_LEAF;
      parent = node_in.parent;
      occupied = node_in.occupied;
   }
   
   /*
    * Insert new key and pointers
    */
   public void insert(int key_in, Node node_in) {
      int pos = 0;
      boolean found = false;
      for (int i = 0; i < occupied; i++) {
         if (key_in < keys[i]) {
            pos = i;
            found = true;
            break;
         }
      }
      if (!found) {
         pos = occupied;
      }
      occupied = occupied + 1;
      for (int i = occupied; i > pos + 1; i--) {
         keys[i - 1] = keys[i - 2];
      }
      for (int i = occupied; i > pos; i--) {
         ptrs[i] = ptrs[i - 1];
      }
      
      keys[pos] = key_in;
      ptrs[pos + 1] = node_in;
   }
   
   /*
    * Delete a key
    */
   public boolean delete_key(int key_in) {
      int pos = 0;
      boolean found = false;
      for (int i = 0; i < occupied; i++) {
         if (key_in == keys[i]) {
            pos = i;
            found = true;
            break;
         }
      }
      if (!found) {
         return false;
      }
      System.out.println("Before: " + Arrays.toString(keys));
      occupied = occupied - 1;
      for (int i = pos; i < occupied; i++) {
         keys[i] = keys[i + 1];
         ptrs[i] = ptrs[i + 1];
      }
      keys[occupied] = 0;
      ptrs[occupied + 1] = null;
      System.out.println("After: " + Arrays.toString(keys));
      return true;
   }
}