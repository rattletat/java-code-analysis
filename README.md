# java-code-analysis

This tool analyzes java projects by calculating static metrics, dynamic (callgraph) metrics and test suite metrics.
The results of analyzing the [defects4j](https://github.com/rjust/defects4j) projects **Math**, **Time**, **Lang**, **Chart**, **Closure** and **Mockito**  can be found in *results/*. Based on that I predicted the quality of fault localization (using [GZoltar](http://www.gzoltar.com/) and dstar2) with various ML techniques. This can be found in the *analysis/* folder.

# Usage

This maven project needs **JAVA 8**.

1. Place the bug versions as follows: *src/main/resources/{ProjectName}/{BugID}*  
A valid path would be for example *src/main/resources/Math/1*.
You can easily download **defects4j** projects with the **loadVersions.sh** bash script assuming you have the **defects4j** bin in your environment. To load the first 10 buggy versions of project Math simply call from the project root:
```bash
sh src/main/tools/bash/loadVersions.sh Math 10 src/main/resources 
```

2. Place the coverage matrices and spectra files in the corresponding *results* folder.  
A valid path would be for example *results/Math/1/spectra* and *results/Math/1/matrix*.

You can generate these spectra and coverage files easily by calling the **generateSpectraFile.sh** bash script without any arguments. It will call GZoltar (placed at *src/main/tools/java/gzoltar/run_gzoltar.sh*) for every project in the *resource* folder. For this script to work you have to change your java environment to **JAVA 7**. If you are working with another granularity than **METHOD**, you have to change it there.

3. Place your dotfiles in *src/main/dotfiles/{ProjectName}/{BugID}.dot*  
A valid path would be for example *src/main/dotfiles/Math/1.dot*.

4. Configure the **Launcher** class in *src/main/java/main/*. You can set:
    - Which module to run
        - Static analysis
        - Dynamic analysis
        - Test suite analysis
        - Suspiciousness Analysis
        - Label analysis
        - Combine versions
    - Which BugID to start with
    - Which BugID to end with (only ID ranges)
    - Which Project-BugIDs to exclude
    - Which suspiciousness technique to use
    - The resource folder path (default: *src/main/resources*)
    - The result folder path (default: *result/*)
    - Names of all intermediate and resulting output files

5. To increase the maximum heap size needed for program execution, run the following:
```bash 
export MAVEN_OPTS="-Xmx3000M"
```

6. Compile the modified project and run it with:
```bash
mvn clean package && mvn exec:java   
```

# Modules

## Static Metrics:

The **StaticSolver** class calculates metrics using the [Java Parser libary](https://javaparser.org/). Constructors and methods are processed and averaged up the whole project.  

To add a new metric, one can simply extend the **StaticMetric** class and add it in **StaticResult**.  
To avoid processing irrelevant files contained in */test/* or */target/* directories, these get filtered out. Adjustments to the filter can be done in the **ProjectHandler** class.

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

The dynamic metrics get calculated by the **dynamic_solver.py** command line tool. It needs a callgraph in the *.dot* file format, which for example can be created with  [JDCallgraph](https://github.com/dkarv/jdcallgraph) framework. Additional metrics can be implemented in the script.

You should run the dynamic module always with the **Label Solver** module. Otherwise, faulty node specific metrics will be filled with -1.

```
Python commandline tool to analyze dot files of callgraphs.  

dynamic_solver.py -d <dot file>  

Parameters:  
d : specify dot file (--dot=)  
f : specify faulty node (--faulty=)  
w : specify output file (--write=)  
v : verbose output (--verbose)  
h : print this help  
```

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
- **D-#AboveAvgInD**: Number of nodes with above avg in-degree
- **D-#AboveAvgOutD**: Number of nodes with above avg out-degree
- **D-#DiCircles**: Number of directed circles
- **D-#WeakGroups**: Number of weak groups


## Test Suite Metrics:

Python command line tool **faultloc.py**. Calculates metrics using a test suite coverage matrix, which for example can be created with the [GZoltar](http://www.gzoltar.com) framework. More metrics can be added in the script.

```
Python commandline tool to analyze hit-spectra matrices.  

metric_loc.py -m <matrix file>  

Parameters:  
m : specify matrix file (--matrix=)  
w : specify output file  
v : verbose output (--verbose)  
h : print this help  
```

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
- **T-Spa**: Sparsity
- **T-CovPT**: Coverage of passing tests
- **T-CovFT**: Coverage of failing tests
- **T-AvgVE**: Average number of visited elements by passing *or* failing tests
- **T-AvgVEPT**: Average number of visited elements by passing tests
- **T-AvgVEFT**: Average number of visited elements by failing tests
- **T-#VEP^FT**: Number of elements visited by passing *and* failing tests
- **T-%VEP^FT**: Average number of elements visited by passing *and* failing tests


## Suspiciousness Analysis

```
Python command tool to evaluate Gzoltar outputs.  

fault_loc.py -m <matrix file> -s <spectra file> -t <technique>  
    
Parameters:  
m : specify matrix file (--matrix=)  
s : specify spectra file (--spectra=)  
t : specify technique for evaluation (--technique=)  
w : specify output file  
n : specify number of objects to output  
r : specify number of ranks to output  
v : verbose output (--verbose)  
h : print this help  
```

The following techniques are implemented in **faultloc.py** and can be specified in the **Launcher** class:

- dstar2 
- dstar3
- jaccard
- ochiai
- tarantula
- zoltar

## Label analysis

The label analysis module finds the faulty method(s), so that they can be used in the dynamic analysis for faulty node centered metrics. It does that by extracting modified line locations from the [program-repair JSON file](https://github.com/program-repair/defects4j-dissection) and comparing them to the **MethodLineSolver** class result (*see static analysis section*). Then the module matches the faulty method ranges against lines from the *Suspiciousness Analysis*, to find the node name used by GZoltar (for later callgraph analysis) and to extract the minimal rank. It returns the minimal rank of all faulty methods or -1, if the method was not found in the suspiciousness output. This happens when the fix modifies only lines outside of preexisting methods. You can exlude these bug versions from the result by specifying them in the **Launcher** class.

You have to run the **Suspiciousness Analyzer** simultaneous or before running the **LabelSolver** module.

## Combine Version Results

The **Version Combiner** module merges the output of all other modules.

1. It merges all the output files on version level to a file located in the version directory  
(default: *Overall_Version_Results.csv*)
2. It merges all the output files on project level to a file located in the project directory  
(default: *Overall_Project_Results.csv*)
3. It merges all the project results to a file located in the *results* folder  
(default: *Overall_Results.csv*)

***Note***: Step 3 is vagile at the moment. I recommend one of the following workflows:

- One project at a time with all modules activated.
- Mutiple projects with the static-, test suite-, and suspiciousness module and afterwards again with the label-, dynamic-, and combiner module. The last run should always include these three modules.
- If the last merging step (3) fails, copy the project results by hand into a final version.

## Bug metrics:

I used the [defects4j dissection](https://github.com/program-repair/defects4j-dissection/blob/master/script/defects4j-bugs.csv). One has to add it manually to the final result. Also I wrote a command line script which extracts the type of the exception from the bug json file. Can be easily altered. See */src/main/tools/python/bug_tool/*.

### Metrics 

- **B-RepairActions**
- **B-RepairPatterns**	
- **B-RepairTools**	
- **B-Files**
- **B-Methods**
- **B-Classes**	
- **B-LinesAdd**	
- **B-LinesRem**	
- **B-LinesMod**	
- **B-SizeInLines**	
- **B-Chunks**
- **B-SpreadAllLines**	
- **B-SpreadCodeOnly**
- **B-NumExceptions**: Type of exception (*results/exceptions.csv*)

# Enhancements:

- Use the results from the **LabelSolver** to calculate static metrics directly for the faulty methods.
- Refactor **Launcher** and **LabelSolver** class. Add a class which embed all module results.
- Introduce multithreading in python scripts
- Introduce lazy resource fetching in maven
- Reduce heap overhead in **StaticSolver** class.
- Name convention: S-WordCount vs. T-#PT
- Add support for mutiple target labels in **LabelSolver**.  
E.g. The minimal cardinality from all faulty method sets containing all not faulty methods with the same or lower rank 
