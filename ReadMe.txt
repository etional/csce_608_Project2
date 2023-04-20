Class for part 1:
Node.java
BPTree.java

Class for part 2:
Tuple.java
VDisk.java
VMM.java

A program for tesing the implementation for both parts is Project2.java.
To run a program, Project2.java needs to be compiled first.

ex) ..Directory...>javac Project2.java
     ..Directory...>java Project2

When the program begins, it will show a prompt and let user to type in an input.
Please type "Tree" to run a B+Tree part of the project.
Please type "Hash" to run a Join based on Hashing part of the project.
To quit the program, please type "Exit".

--------------------------------------------------------------
                       Project 2

    Please type one of the options:
        Tree - B+Tree
        Hash - Join based on Hashing
        Exit - Close the program

--------------------------------------------------------------
Ex) First prompt

When user types "Tree", it will create 4 B+tree and show a prompt like below.

--------------------------------------------------------------

    Please type one of the options:
        Dense13 - Dense B+Tree of order 13
        Dense24 - Dense B+Tree of order 24
        Sparse13 - Sparse B+Tree of order 13
        Sparse24 - Sparse B+Tree of order 24
        Menu - Return to the menu

--------------------------------------------------------------

User can choose one of the trees to do the operations.

When the user chooses a tree, the program will show a prompt like below.

--------------------------------------------------------------
            Dense B+Tree of order 13
    Please type one of the options:
        Search - Perform search operation with randomly generated key
        Range_Search - Perform range search operation with given keys
        Insertion - Perform insertion operations with randomly generated key
        Deletion - Perform deletion operations with randomly generated key
        Back - Back to Tree Selection

--------------------------------------------------------------

User can choose one of the operations. To change a tree, user needs to type "Back" to go back to the previous prompt and choose other tree.

User can go back to the first prompt by typing "Menu" in tree selection prompt.

When user types "Hash" in the first prompt, the program will show a prompt like below.

--------------------------------------------------------------

    Please type one of the options:
        Experiment1 - Perform 5.1 in Join based on Hashing
        Experiment2 - Perform 5.2 in Join based on Hashing
        Menu - Return to the menu

--------------------------------------------------------------

User can run 5.1 of the project by typing "Experiment1" and run 5.2 of the project by typing "Experiment2".
It will show all process of the each experiment.