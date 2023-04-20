import java.util.Arrays;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.lang.*;
import java.util.Random;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Scanner;

// class for experiment
public class Project2
{  
   // natural join
   public static List<AbstractMap.SimpleEntry<Tuple, Tuple>> natural_join(VDisk disk, VMM mem) {
      List<AbstractMap.SimpleEntry<Tuple, Tuple>> result = 
                              new ArrayList<AbstractMap.SimpleEntry<Tuple, Tuple>>();
      int io = 0;
      // Phase 1
      // hash tuples in R
      System.out.println("--- Hash tuples in Buckets ---");
      while (!disk.is_empty("R")) {
         // send block from disk to main memory
         Tuple[] block = disk.read("R");
         mem.write_at(block, 0);
         io++;
         // hash tuples into (num_blocks - 1) buckets
         List<AbstractMap.SimpleEntry<Tuple[], Integer>> hashed = mem.hash();
         // send full blocks from main memory to disk
         for (int i = 0; i < hashed.size(); i++) {
            disk.hash_write(hashed.get(i).getKey(), hashed.get(i).getValue(), "R");
            io++;
         }         
      }
      // send remaining tuples to disk
      for (int i = 0; i < mem.hash_size(); i++) {
         Tuple[] block = mem.read_at(i + 1).clone();
         if (block[0].b != 0) {
            disk.hash_write(block, i, "R");
            io++;
         }
      }
      System.out.println("--- Hashing R is finished ---");
      disk.print_hash("R");
      System.out.println("--- I/O After Hashing R: " + String.valueOf(io));
      // hash tuples in S
      while (!disk.is_empty("S")) {
         // send block from disk to main memory
         Tuple[] block = disk.read("S");
         mem.write_at(block, 0);
         io++;
         // hash tuples into (num_blocks - 1) buckets
         List<AbstractMap.SimpleEntry<Tuple[], Integer>> hashed = mem.hash();
         // send full blocks from main memory to disk
         for (int i = 0; i < hashed.size(); i++) {
            disk.hash_write(hashed.get(i).getKey(), hashed.get(i).getValue(), "S");
            io++;
         }
      }
      // send remaining tuples to disk
      for (int i = 0; i < mem.hash_size(); i++) {
         Tuple[] block = mem.read_at(i + 1).clone();
         if (block[0].b != 0) {
            disk.hash_write(block, i, "S");
            io++;
         }
      }
      System.out.println("--- Hashing S is finished ---");
      disk.print_hash("S");
      System.out.println("--- I/O After Hashing S: " + String.valueOf(io));
      System.out.println("--- Finish Hashing ---");
      
      // Phase 2 - bucketwise operation
      System.out.println("--- Begin Natural Join ---");
      int hashs = mem.hash_size();
      for (int i = 0; i < hashs; i++) {
         // send bucket in R from disk to main memory
         if (disk.bucket_size(i, "R") != 0) {
            int r_blocks = disk.bucket_size(i, "R");
            // divide bucket to fit in the main memory
            int iter = (int) Math.ceil(r_blocks / (double)hashs);
            for(int j = 0; j < iter; j++) {
               List<Tuple[]> bucket = disk.bucket_read(i);
               int blocks = bucket.size();
               for (int k = 0; k < blocks; k++) {
                  mem.write_at(bucket.get(k), k + 1);
                  io++;
               }
               // send block in S from disk to main memory
               int s_blocks = disk.bucket_size(i, "S");
               for (int k = 0; k < s_blocks; k++) {
                  Tuple[] block = disk.hashed_block_read(i);
                  mem.write_at(block, 0);
                  io++;
                  List<AbstractMap.SimpleEntry<Tuple, Tuple>> joins = mem.natural_join();
                  for (int l = 0; l < joins.size(); l++) {
                     result.add(joins.get(l));
                  }
                  // if all blocks in the bucket of R are not checked
                  // - rewrite compared block into S
                  if (j < iter - 1) {
                     Tuple[] rewrite = mem.read_at(0).clone();
                     disk.hash_write(block, i, "S");
                     io++;
                  }
               }
               mem.clear();
            }
         }
         else {
            // send block in S from disk to main memory
            int s_blocks = disk.bucket_size(i, "S");
            for (int k = 0; k < s_blocks; k++) {
               Tuple[] block = disk.hashed_block_read(i);
               mem.write_at(block, 0);
               io++;
               List<AbstractMap.SimpleEntry<Tuple, Tuple>> joins = mem.natural_join();
               for (int l = 0; l < joins.size(); l++) {
                  result.add(joins.get(l));
               }
            }
         }
      }
      System.out.println("--- Finish Natural Join ---");
      
      System.out.println("Number of disk I/O: " + String.valueOf(io));
      System.out.println("Number of tuples in join: " + String.valueOf(result.size()));
      return result;
   }
   
   public static void treeProcess(BPTree tree, String type)
   {
      boolean back = false;
      int max = 200000;
      int min = 100000;
      Random rand = new Random();
      
      while (!back) {
         System.out.println("\n--------------------------------------------------------------");
         System.out.println("            " + type);
         System.out.println("    Please type one of the options:");
         System.out.println("        Search - Perform search operation with randomly generated key");
         System.out.println("        Range_Search - Perform range search operation with given keys");
         System.out.println("        Insertion - Perform insertion operations with randomly generated key");
         System.out.println("        Deletion - Perform deletion operations with randomly generated key");
         System.out.println("        Back - Back to Tree Selection\n");
         System.out.println("--------------------------------------------------------------\n");
         Scanner scan = new Scanner(System.in);
         System.out.println("    Your choice: ");
         String userChoice = scan.nextLine();
         
         if (userChoice.toLowerCase().equals("search")) {
            int search = rand.nextInt((max - min) + 1) + min;
            System.out.println("\n--- Search in " + type + " ---");
            System.out.println("--- key: " + String.valueOf(search));
            tree.search(search, tree.root);
         }
         else if (userChoice.toLowerCase().equals("range_search")) {
            boolean is_num1 = false;
            boolean is_num2 = false;
            int begin_key = 0;
            int end_key = 0;
               
            while (!is_num1) {
               System.out.println("Please insert begin key and end key");
               System.out.println("--- Begin key: ");
               if (scan.hasNextInt()) {
                  begin_key = scan.nextInt();
                  if (begin_key < min || begin_key > max) {
                     System.out.println("---You put an invalid input---");
                     System.out.println("Input should be between 100000 and 200000");
                     continue;
                  }
                  else {
                     is_num1 = true;
                  }
               }
               else {
                  System.out.println("---You put an invalid input---");
                  System.out.println("Input should be an integer");
                  continue;
               }
            }
            while (!is_num2) {
               System.out.println("--- End key: ");
               if (scan.hasNextInt()) {
                  end_key = scan.nextInt();
                  if (end_key < min || end_key >= max) {
                     System.out.println("---You put an invalid input---");
                     System.out.println("Input should be between 100000 and 200000");
                     continue;
                  }
                  else if (end_key < begin_key){
                     System.out.println("---You put an invalid input---");
                     System.out.println("Input should be less than begin key");
                     continue;
                  }
                  else {
                     is_num2 = true;
                  }
               }
               else {
                  System.out.println("---You put an invalid input---");
                  System.out.println("Input should be an integer");
                  continue;
               }
            }
            
            List<Integer> result;
         
            System.out.println("\n--- Range Search in " + type + " ---");
            System.out.println("--- keys: " + String.valueOf(begin_key) + ", " + String.valueOf(end_key));
            result = tree.range_search(begin_key, end_key, tree.root);
            System.out.println("-- Results ---");
            System.out.println(result);
         }
         else if (userChoice.toLowerCase().equals("insertion")) {
            int insert = rand.nextInt((max - min) + 1) + min;
            System.out.println("\n--- Insert in " + type + " ---");
            System.out.println("--- key: " + String.valueOf(insert));
            tree.insert(insert, tree.root);
         }
         else if (userChoice.toLowerCase().equals("deletion")) {
            int delete = rand.nextInt((max - min) + 1) + min;
            System.out.println("\n--- Delete in " + type + " ---");
            System.out.println("--- key: " + String.valueOf(delete));
            tree.delete_key(delete, tree.root);
         }
         else if (userChoice.toLowerCase().equals("back")) {
            System.out.println("--- Going back to the Menu ---");
            back = true;
         }
         else {
            System.out.println("---You put an invalid input---");
            System.out.println("Input should be one of the options");
         }
      }
   }
   
   public static void main(String[] args)
   {
      boolean quit = false;
      while (!quit) {
         System.out.println("\n--------------------------------------------------------------");
         System.out.println("                       Project 2\n");
         System.out.println("    Please type one of the options:");
         System.out.println("        Tree - B+Tree");
         System.out.println("        Hash - Join based on Hashing");
         System.out.println("        Exit - Close the program\n");
         System.out.println("--------------------------------------------------------------\n");
         Scanner scan = new Scanner(System.in);
         System.out.println("    Your choice: ");
         String userChoice = scan.nextLine();
      
         if (userChoice.toLowerCase().equals("tree")) {
            // 1. B+ Trees
            int min = 100000;
            int max = 200000;
            int size = 10000;
            Random rand = new Random();
         
            Set<Integer> set = new LinkedHashSet<Integer>();
            while (set.size() < size) {
               set.add(rand.nextInt((max - min) + 1) + min);
            }

            int[] keys = new int[size];
            int j = 0;
            for (Integer i : set) {
               keys[j++] = i;
            }
         
            Arrays.sort(keys);
         
            // Dense B+Tree of order 13
            BPTree dense1 = new BPTree(13, keys, "dense");
            System.out.println("\n--- Dense B+Tree of order 13 has created ---\n");
            // Dense B+Tree of order 24
            BPTree dense2 = new BPTree(24, keys, "dense");
            System.out.println("\n--- Dense B+Tree of order 24 has created ---\n");
            // Sparse B+Tree of order 13
            BPTree sparse1 = new BPTree(13, keys, "sparse");
            System.out.println("\n--- Sparse B+Tree of order 13 has created ---\n");
            // Sparse B+Tree of order 24
            BPTree sparse2 = new BPTree(24, keys, "sparse");
            System.out.println("\n--- Sparse B+Tree of order 24 has created ---\n");
            
            boolean menu = false;
            while (!menu) {
               System.out.println("\n--------------------------------------------------------------\n");
               System.out.println("    Please type one of the options:");
               System.out.println("        Dense13 - Dense B+Tree of order 13");
               System.out.println("        Dense24 - Dense B+Tree of order 24");
               System.out.println("        Sparse13 - Sparse B+Tree of order 13");
               System.out.println("        Sparse24 - Sparse B+Tree of order 24");
               System.out.println("        Menu - Return to the menu\n");
               System.out.println("--------------------------------------------------------------\n");
               System.out.println("    Your choice: ");
               String treeType = scan.nextLine();
               if (treeType.toLowerCase().equals("dense13")) {
                  treeProcess(dense1, "Dense B+Tree of order 13");
               }
               else if (treeType.toLowerCase().equals("dense24")) {
                  treeProcess(dense2, "Dense B+Tree of order 24");
               }
               else if (treeType.toLowerCase().equals("sparse13")) {
                  treeProcess(sparse1, "Sparse B+Tree of order 13");
               }
               else if (treeType.toLowerCase().equals("sparse24")) {
                  treeProcess(sparse2, "Sparse B+Tree of order 24");
               }
               else if (treeType.toLowerCase().equals("menu")) {
                  System.out.println("\n--- Going back to the Menu ---");
                  menu = true;
               }
               else {
                  System.out.println("---You put an invalid input---");
                  System.out.println("Input should be one of the options");
               }
            }
         }
         else if (userChoice.toLowerCase().equals("hash")) {
            VMM vmm = new VMM(15, 8); // virtual main memory
            VDisk vdisk = new VDisk(15, 8); // virtual disk
      
            // create relation S
            int s_min = 10000;
            int s_max = 50000;
            int s_size = 5000;
            Set<Integer> set = new LinkedHashSet<Integer>();
            Random rand = new Random();
            while (set.size() < s_size) {
               set.add(rand.nextInt((s_max - s_min) + 1) + s_min);
            }
         
            List<Integer> sList = new ArrayList<Integer>(set);
            Collections.sort(sList);
         
            List<Tuple> S = new ArrayList<Tuple>();
            for (int i = 0; i < s_size; i++) {
               String c = "s" + String.valueOf(i);
               Tuple tuple = new Tuple(sList.get(i), c);
               S.add(tuple);
            }
            
            boolean menu = false;
            while (!menu) {
               System.out.println("\n--------------------------------------------------------------\n");
               System.out.println("    Please type one of the options:");
               System.out.println("        Experiment1 - Perform 5.1 in Join based on Hashing");
               System.out.println("        Experiment2 - Perform 5.2 in Join based on Hashing");
               System.out.println("        Menu - Return to the menu\n");
               System.out.println("--------------------------------------------------------------\n");
               System.out.println("    Your choice: ");
               String treeType = scan.nextLine();
               if (treeType.toLowerCase().equals("experiment1")) {
                  // write relation S in virtual disk
                  vdisk.write(S, "S");
         
                  // create relation R for 5.1
                  int r1_size = 1000;
                  int[] rList = new int[r1_size];
                  for (int i = 0; i < r1_size; i++) {
                     int index = rand.nextInt(s_size);
                     rList[i] = sList.get(index);
                  }
                  List<Tuple> R1 = new ArrayList<Tuple>();
                  for (int i = 0; i < r1_size; i++) {
                     String a = "r" + String.valueOf(i);
                     Tuple tuple = new Tuple(rList[i], a);
                     R1.add(tuple);
                  }
         
                  // write relation R in virtual disk
                  vdisk.write(R1, "R");
         
                  // perform natural join
                  List<AbstractMap.SimpleEntry<Tuple, Tuple>> joins = natural_join(vdisk, vmm);
         
                  Set<Integer> selection = new LinkedHashSet<Integer>();
                  while (selection.size() < 20) {
                     selection.add(rList[rand.nextInt(rList.length)]);
                  }
         
                  List<Integer> bs = new ArrayList<Integer>(selection);
                  Collections.sort(bs);
         
                  System.out.println("--- Selected B values: " + bs);
         
                  System.out.println("------ Natural Join ------");
                  for (int i = 0; i < bs.size(); i++) {
                     for (int j = 0; j < joins.size(); j++) {
                        if (joins.get(j).getKey().b == bs.get(i)) {
                           String a = joins.get(j).getKey().a;
                           String c = joins.get(j).getValue().a;
                           int b = joins.get(j).getKey().b;
                           System.out.println("A: " + a + "     B: " + String.valueOf(b) + "     C: " + c);
                        }
                     }         
                  }
               }
               else if (treeType.toLowerCase().equals("experiment2")) {
                  // write relation S in virtual disk
                  vdisk.write(S, "S");
         
         
                  // create relation R for 5.2
                  int r2_min = 20000;
                  int r2_max = 30000;
                  int r2_size = 1200;
                  int[] r2List = new int[r2_size];
                  for (int i = 0; i < r2_size; i++) {
                     int num = rand.nextInt((r2_max - r2_min) + 1) + r2_min;
                     r2List[i] = num;
                  }
                  List<Tuple> R2 = new ArrayList<Tuple>();
                  for (int i = 0; i < r2_size; i++) {
                     String a = "r" + String.valueOf(i);
                     Tuple tuple = new Tuple(r2List[i], a);
                     R2.add(tuple);
                  }
         
                  // write relation R in virtual disk
                  vdisk.write(R2, "R");
         
                  // perform natural join
                  List<AbstractMap.SimpleEntry<Tuple, Tuple>> joins2 = natural_join(vdisk, vmm);
                  
                  System.out.println("------ Natural Join ------");
                  for (int i = 0; i < joins2.size(); i++) {
                     String a = joins2.get(i).getKey().a;
                     String c = joins2.get(i).getValue().a;
                     int b = joins2.get(i).getKey().b;
                     System.out.println("A: " + a + "     B: " + String.valueOf(b) + "     C: " + c);
                  }
                  System.out.println("Number of Results: " + String.valueOf(joins2.size()));
               }
               else if (treeType.toLowerCase().equals("menu")) {
                  System.out.println("\n--- Going back to the Menu ---");
                  menu = true;
               }
               else {
                  System.out.println("---You put an invalid input---");
                  System.out.println("Input should be one of the options");
               }
            }
         }
         else if (userChoice.toLowerCase().equals("exit")) {
            System.out.println("--- Exit program ---");
            System.out.println("--- Thank you ---");
            quit = true;
         }
         else {
            System.out.println("---You put an invalid input---");
            System.out.println("Input should be one of the options");
         }
      }
   }
}