import java.util.Arrays;
import java.util.*;
import java.lang.*;

// class for virtual disk
public class VDisk 
{
   private int block_size;
   private int num_blocks;
   private List<Tuple[]> data_R; // tuples in R organized in blocks
   private List<Tuple[]> data_S; // tuples in S organized in blocks
   private List<List<Tuple[]>> hash_R; // tuples in R organized in buckets
   private List<List<Tuple[]>> hash_S; // tuples in S organized in buckets
   
   // constructor
   public VDisk(int num, int size) {
      num_blocks = num;
      block_size = size;
      data_R = new ArrayList<Tuple[]>();
      data_S = new ArrayList<Tuple[]>();
      hash_R = new ArrayList<List<Tuple[]>>();
      // create (num_blocks - 1) empty buckets 
      for (int i = 0; i < num_blocks - 1; i++) {
         List<Tuple[]> bucket = new ArrayList<Tuple[]>();
         hash_R.add(bucket);
      }
      hash_S = new ArrayList<List<Tuple[]>>();
      // create (num_blocks - 1) empty buckets
      for (int i = 0; i < num_blocks - 1; i++) {
         List<Tuple[]> bucket = new ArrayList<Tuple[]>();
         hash_S.add(bucket);
      }
   }
   
   // write tuples into blocks
   public void write(List<Tuple> tuples, String relation) {
      for (int i = 0; i < Math.ceil(tuples.size() / block_size); i++) {
         // create a block
         Tuple[] block = new Tuple[block_size];
         for (int j = 0; j < block_size; j++) {
            if (i * block_size + j < tuples.size()) {
               block[j] = tuples.get(i * block_size + j);
            }
         }
         if (relation.equals("R")) {
            data_R.add(block);
         }
         else {
            data_S.add(block);
         }
      }
   }
   
   // read a block from the disk (read data is removed from the disk)
   public Tuple[] read(String relation) {
      Tuple[] block;
      if (relation.equals("R")) {
         block = data_R.get(0);
         data_R.remove(0);
      }
      else {
         block = data_S.get(0);
         data_S.remove(0);
      }
      
      return block;
   }
   
   // write a block into buckets
   public void hash_write(Tuple[] block, int hash_idx, String relation) {
      if (relation.equals("R")) {
         hash_R.get(hash_idx).add(block);
      }
      else {
         hash_S.get(hash_idx).add(block);
      }
   }
   
   // read a bucket from disk with the index of hash_idx (read bucket is cleared)
   public List<Tuple[]> bucket_read(int hash_idx) {
      if (hash_R.get(hash_idx).size() < num_blocks) {
         List<Tuple[]> bucket = new ArrayList<Tuple[]>(hash_R.get(hash_idx));
         hash_R.get(hash_idx).clear();
         return bucket;
      }
      else {
         List<Tuple[]> bucket = new ArrayList<Tuple[]>();
         for (int i = 0; i < num_blocks - 1; i++) {
            bucket.add(hash_R.get(hash_idx).get(0).clone());
            hash_R.get(hash_idx).remove(0);
         }
         return bucket;
      }
   }
   
   // read a block in a bucket from disk (read block is removed from the bucket)
   public Tuple[] hashed_block_read(int hash_idx) {
      Tuple[] block = hash_S.get(hash_idx).get(0).clone();
      hash_S.get(hash_idx).remove(0);
      return block;
   }
   
   // check whether block of the relation exists or not
   public boolean is_empty(String relation) {
      if (relation.equals("R")) {
         if (data_R.size() == 0) {
            return true;
         }
         else {
            return false;
         }
      }
      else {
         if (data_S.size() == 0) {
            return true;
         }
         else {
            return false;
         }
      }
   }
   
   public int bucket_size(int hash_idx, String relation) {
      if (relation.equals("S")) {
         return hash_S.get(hash_idx).size();
      }
      else {
         return hash_R.get(hash_idx).size();
      }
   }
   
   public void print_hash(String relation) {
      if (relation.equals("R")) {
         for (int i = 0; i < hash_R.size(); i++) {
            System.out.println("Hash " + String.valueOf(i) + ": " 
                     + String.valueOf(hash_R.get(i).size()) + "blocks");
         }
      }
      else {
         for (int i = 0; i < hash_S.size(); i++) {
            System.out.println("Hash " + String.valueOf(i) + ": " 
                     + String.valueOf(hash_S.get(i).size()) + "blocks");
         }
      }
   }
}