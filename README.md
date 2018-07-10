# java-code-analysis

This tool analyzes java projects by calculating static metrics, dynamic (callgraph) metrics and test suite metrics.

## Static Metrics:

The **Static Solver** class calculates metrics using the (Java Parser libary)[https://javaparser.org/]. Constructors and methods are processed and averaged up the whole project.  

To add new metrics, one can do so simply by adding a new metric extending the **StaticMetric** class in **StaticResult**.  
To avoid processing irrelevant files contained in */test/* or */target/* directories, these get removed before processing. Adjustments to the filter can be done in the **ProjectHandler** class.

Also, the project features the **MethodLineSolver** class, which creates a CSV file containing the filepath, the method name, the beginning and ending line number of every method/constructor in the (filtered) project. This class can be easily modified to instead extract code blocks. The CSV file is used in combination with the bug metrics csv file (see below) in the **LabelSolver** class to find the methods containing a fault.

### Metrics

- **S-LineCount**: Lines of code
- **S-WordCount**: Number of words
- **S-Density**: Density (word/lines)
- **S-ParaCount**: Number of arguments
- **S-FunCalls**: Number of method calls (not unique)
- **S-VarCount**: Number of local variables
- **S-RepWords**: Number of repeated words
- **S-ComPer**: Percentage of comments (javadoc + inner method comments)
- **S-MaxDepth**: Nested block depth
- **S-BlockCount**: Number of blocks
- **S-CycCom**: Cyclomatic Complexity

## Dynamic Metrics:

The dynamic metrics get calculated by the **dynamic_solver.py** command line tool. It needs a callgraph file in the *.dot* file format, which for example can be created with the (GZoltar)[http://www.gzoltar.com/] framework. Additional metrics can be added in the script.

---------------------------------------------------------------
*Python commandline tool to analyze dot files of callgraphs.*
faultloc.py -d <dot file>
**Parameters**:
**d** : specify dot file (--dot=)
**f** : specify faulty node (--faulty=)
**w** : specify output file (--write=)
**v** : verbose output (--verbose)
**h** : print this help
---------------------------------------------------------------

### Metrics

- **D-AvgD**: Average node degree
- **D-AvgInD**: Average node in-degree
- **D-AvgOutD**: Average node out-degree
- **D-FaultyD**: Faulty node degree
- **D-FaultyInD**: Faulty node in-degree
- **D-FaultyOutD**: Faulty node out-degree
- **D-DiDiameter**: Directed diameter of graph
- **D-UndiDiameter**: Undirected diameter of graph
- **D-Avg-Closeness**: Average closeness centrality
- **D-AvgInCloseness**: Average in-closeness centrality
- **D-AvgOutCloseness**: Average out-closeness centrality
- **D-FaultyCloseness**: Average closeness centrality of faulty node
- **D-WeiEdgeSum**: EdgeCount / NodeCount
- **D-#AboveAvgD**: Number of nodes with above avg degree
- **D-#DiCircles**: Number of directed circles
- **D-#WeakGroups**: Number of weak groups



## Test Suite Metrics:

Python command linetool which calculates metrics using a test suite coverage matrix, which can be created 

---------------------------------------------------------------
*Python commandline tool to analyze hit-spectra matrices.*
faultloc.py -m <matrix file>
**Parameters**:
**m** : specify matrix file (--matrix=)
**w** : specify output file
**v** : verbose output (--verbose)
**h** : print this help
---------------------------------------------------------------


### Metrics 
- **T-#T**: Number of tests
- **T-#PT**: Number of passing tests
- **T-#FT**: Number of failing tests
- **T-%PT**: Percentage of passing tests
- **T-%FT**: Percentage of failing tests
- **T-#E**: Number of elements
- **T-#VE**: Number of visited elements
- **T-#NVE**: Number of not visited elements
- **T-#V**: Number of visits in total
- **T-Cov**: Coverage
- **T-CovPT**: Coverage of passing tests
- **T-CovFT**: Coverage of failing tests
- **T-AvgVE**: Average number of visited elements by passing *or* failing tests
- **T-AvgVEPT**: Average number of visited elements by passing tests
- **T-AvgVEFT**: Average number of visited elements by failing tests
- **T-#VEP^FT**: Number of elements visited by passing *and* failing tests
- **T-%VEP^FT**: Average number of elements visited by passing *and* failing tests

## Bug metrics:

I used the [defects4j dissection](https://github.com/program-repair/defects4j-dissection/blob/master/script/defects4j-bugs.csv).
