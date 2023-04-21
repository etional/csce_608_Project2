import java.util.Arrays;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.lang.Integer;

// class for virtual main memory
public class VMM
{
   private int block_size;
   private int num_blocks; // 1 block for read block from disk, rest blocks for hashing
   private Tuple[][] data;
   
   public VMM(int num, int size) {
      num_blocks = num;
      block_size = size;
      data = new Tuple[num_blocks][block_size];
      for (int i = 0; i < num_blocks; i++) {
         for (int j = 0; j < block_size; j++) {
            data[i][j] = new Tuple(0, null);
         }
      }
   }
   
   public int hash_size() {
      return num_blocks - 1;
   }
   
   // write block in virtual main memory  
   public void write_at(Tuple[] block_in, int index) {
      for (int i = 0; i < block_in.length; i++) {
         data[index][i] = block_in[i];
      }
   }
   
   public Tuple[] read_at(int index) {
      Tuple[] block = data[index].clone();
      for (int i = 0; i < data[index].length; i++) {
         data[index][i] = new Tuple(0, null);
      } 
      return block;
   }
   
   // hash function
   public int hash_function(int value) {
      int bitsum = Integer.bitCount(value);
      return bitsum % (num_blocks - 1);
   }
   
   // hash the tuples in first block(index: 0) into the rest blocks(index: 1 ~ 14)
   public List<AbstractMap.SimpleEntry<Tuple[], Integer>> hash() {
      List<AbstractMap.SimpleEntry<Tuple[], Integer>> full = 
                           new ArrayList<AbstractMap.SimpleEntry<Tuple[], Integer>>();
      // get data from the first block - make the first block empty
      Tuple[] block = data[0].clone();
      for (int i = 0; i < data[0].length; i++) {
         data[0][i] = new Tuple(0, null);
      }
      
      // hash into the blocks
      for (int i = 0; i < block.length; i++) {
         if (block[i] != null) {
            int hash_idx = hash_function(block[i].b) + 1;
            boolean is_full = false;
            for (int j = 0; j < data[hash_idx].length; j++) {
               if (data[hash_idx][j].b == 0) {
                  data[hash_idx][j] = block[i];
                  if (j == block_size - 1) {
                     is_full = true;
                  }
                  break;
               }
            }
            if (is_full) {
               Tuple[] full_block = data[hash_idx].clone();
               for (int j = 0; j < block_size; j++) {
                  data[hash_idx][j] = new Tuple(0, null);
               }
               AbstractMap.SimpleEntry<Tuple[], Integer> pop = 
                              new AbstractMap.SimpleEntry<Tuple[], Integer>(full_block, hash_idx - 1);
               full.add(pop);
            }
         }
      }
      
      return full;
   }
   
   // perform natural join
   public List<AbstractMap.SimpleEntry<Tuple, Tuple>> natural_join() {
      List<AbstractMap.SimpleEntry<Tuple, Tuple>> result = new ArrayList<AbstractMap.SimpleEntry<Tuple, Tuple>>();
      
      Tuple[] s = data[0].clone();
      for (int i = 1; i < num_blocks; i++) {
         Tuple[] r = data[i].clone();
         for (int j = 0; j < s.length; j++) {
            for (int k = 0; k < r.length; k++) {
               if (s[j].b != 0 && s[j].b == r[k].b) {
                  AbstractMap.SimpleEntry<Tuple, Tuple> join = 
                           new AbstractMap.SimpleEntry<Tuple, Tuple>(r[k], s[j]);
                  result.add(join);
               }
            }
         }
      }
      
      return result;
   }
   
   public void clear() {
      for (int i = 0; i < num_blocks; i++) {
         for (int j = 0; j < block_size; j++) {
            data[i][j] = new Tuple(0, null);
         }
      }
   }
}