# Planning

Main focus on resource transfer. 
Let other mods handle world analysis (future project?) and only accept basic data like redstone.


**Data types**:
 - Number (split into int and float?)
 - String (maybe not)
 - Boolean
 - Item stream
 - Fluid stream 
 - Energy stream

When evaluating stream targets, the system starts evaluating a context from the source. 
At a splitting node, the node picks a target to begin with and evaluation continues down that path, 
but if no transfer can be completed, the node gets to pick a new target. If none of the nodes targets work,
and no parent splitters exist, the stream doesn't perform anything this tick.

Some nodes have configuration for things like output count (splitters). These are their own sub-gui. 

**Types of nodes:**
 - Splitters
   - Serial (checks targets in order)
   - Random (checks targets randomly)
 - Resource I/O
   - Item
   - Fluid
   - Energy
 - Data I/O 
   - Redstone
 - Logic
   - Math (expressions with custom inputs and outputs)


**Best way to hardcode values?**
 - ~~Constant nodes (ugly)~~
   - ~~Pain to make (widgets in nodes)~~
 - ~~Instead of connections (hard? confusing (only some data types))~~
 - **Config on nodes** 


## Network data structures
 - `Graph` stores all nodes and their connections
 - `NodeType` contains node logic and gets config and runtime data as nbt.
 - `Node` stores the config and runtime data of a node as well as it's type