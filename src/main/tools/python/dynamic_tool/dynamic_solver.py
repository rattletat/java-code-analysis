import sys
import os
import getopt
import pygraphviz as pgv
from igraph import *
import numpy as np
import networkx as nx
from functools import reduce
import csv

verboseprint = None
def main(argv):
    dot = None
    faulty_names = None
    dest_path = None
    verbose = False

    # Argument parsing
    try:
        opts, args = getopt.getopt(argv, "hd:f:vw:",["help", "dot=", "faulty=", "verbose", "write="])
    except getopt.GetoptError:
        usage()
    for opt, arg in opts:
        if opt in ("-h", "--help"):
            usage()
            sys.exit()
        elif opt in ("-d", "--dot"):
            dot = arg
        elif opt in ("-w", "--write"):
            dest_path = arg
        elif opt in ("-f", "--faulty"):
            faulty_names = arg
        elif opt in ("-v", "--verbose"):
            verbose = True

    # Verify input
    global verboseprint
    verboseprint = print if verbose else lambda *a, **k: None
    verboseprint("[STATUS] Verifying your input ...")
    verify_input(dot, dest_path)
    verboseprint("[STATUS] Input verified.")

    # Get matrix and spectra data
    verboseprint("[STATUS] Loading dot file ...")
    g = pgv.AGraph(dot, directed=True)
    edges = g.edges()
    nodes = list(filter(lambda x : "org" in x or "com" in x, g.nodes()))
    # Remove method names $... from node names
    for i,node in enumerate(nodes):
        index1 = node.find("$")
        index2 = node.find("#")
        if index1 != -1:
            nodes[i] = node[:index1] + node[index2:]
    # Remove method names from edges
    for i,edge in enumerate(edges):
        a,b = edge
        index1a = a.find("$")
        index2a = a.find("#")
        index1b = b.find("$")
        index2b = b.find("#")
        if index1a != -1:
            a = a[:index1a] + a[index2a:]
        if index1b != -1:
            b = b[:index1b] + b[index2b:]
        edges[i] = (a,b)

    igraph = Graph(directed = True)
    igraph.add_vertices(nodes)
    igraph.add_edges(edges)

    if faulty_names:
        faulty_vertex_names = faulty_names.split(',')
        faulty_in_graph = []
        for faulty_name in faulty_vertex_names:
            if len(igraph.vs(name=faulty_name)) != 0:
                faulty_in_graph.append(faulty_name)
            else:
                verboseprint("[WARNING] " + faulty_name + " not in callgraph!")
        faulty_vertices = list(map(lambda x : igraph.vs(name=x)[0], faulty_in_graph))
        if len(faulty_vertices) == 0:
            faulty_names = None
    if faulty_names is None:
        verboseprint("[WARNING] No faulty methods in graph.")



    verboseprint("[STATUS] Dot file successfully loaded. Analyzing ...")
    m00 = str(avg_degree(igraph))
    m01 = str(avg_in_degree(igraph))
    m02 = str(avg_out_degree(igraph))
    m03 = str(degree_faulty_node(faulty_vertices)) if faulty_names else "-1"
    m04 = str(in_degree_faulty_node(faulty_vertices)) if faulty_names else "-1"
    m05 = str(out_degree_faulty_node(faulty_vertices)) if faulty_names else "-1"
    m06 = str(directed_diameter(igraph))
    m07 = str(undirected_diameter(igraph))
    m08 = str(avg_closeness(igraph))
    m09 = str(avg_in_closeness(igraph))
    m10 = str(avg_out_closeness(igraph))
    m11 = str(closeness_faulty_node(faulty_vertices)) if faulty_names else "-1"
    m12 = str(weighted_edge_sum(igraph))
    m13 = str(above_avg_degree_vertices(igraph))
    m14 = str(circles(nodes, edges))
    m15 = str(weak_groups(igraph))


    # header = ["[TM-" + "{:02d}".format(i) + "] " for i in range(18)]
    header = [
            "[D-AvgD]",
            "[D-AvgInD]",
            "[D-AvgOutD]",
            "[D-FaultyD]",
            "[D-FaultyInD]",
            "[D-FaultyOutD]",
            "[D-DiDiameter]",
            "[D-UndiDiameter]",
            "[D-Avg-Closeness]",
            "[D-AvgInCloseness]",
            "[D-AvgOutCloseness]",
            "[D-FaultyCloseness]",
            "[D-WeiEdgeSum]",
            "[D-#AboveAvgDegrees]",
            "[D-#DiCircles]",
            "[D-#WeakGroups]",
            ]

    output =  header[0] + " Average node degree: " + m00 + "\n"
    output += header[1] + " Average node in degree: " + m01 + "\n"
    output += header[2] + " Average node out degree: " + m02 + "\n"
    output += header[3] + " Faulty node degree: " + m03 + "\n"
    output += header[4] + " Faulty node in degree: " + m04 + "\n"
    output += header[5] + " Faulty node out degree: " + m05 + "\n"
    output += header[6] + " Directed Diameter: " + m06 + "\n"
    output += header[7] + " Undirected Diameter: " + m07 + "\n"
    output += header[8] + " Average closeness: " + m08 + "\n"
    output += header[9] + " Average in closeness: " + m09 + "\n"
    output += header[10] + " Average out closeness: " + m10 + "\n"
    output += header[11] + " Faulty node closeness: " + m11 + "\n"
    output += header[12] + " Weighted edge sum: " + m12 + "\n"
    output += header[13] + " Number of above average node degrees: " + m13 + "\n"
    output += header[14] + " Number of directed circles: " + m14 + "\n"
    output += header[15] + " Number of weak groups: " + m15 + "\n"

    verboseprint("[STATUS] Output generated!")
    if dest_path:
        verboseprint("[STATUS] Writing output to file " + dest_path)
        record = [m00, m01, m02, m03, m04, m05, m06, m07, m08, m09, m10, m11, m12, m13, m14, m15]
        write_output(dest_path, header, record)
        verboseprint("[STATUS] Output written!")
    else:
        verboseprint("[INFO] Printing results ...\n")
        print(output)
    verboseprint("[STATUS] Success! Exiting ...")


def analyze_matrix(dot):
    with open(dot) as dotfile:

        try:
            data = np.array([])
            results = np.array([])
            for row in reader:
                if data.size is 0:
                    data = row[:-1]
                    results = row[-1]
                data = np.vstack((data, row[:-1]))
                results = np.vstack((results, row[-1]))
            return (data.astype(np.int),results)
        except csv.Error as ex:
            verboseprint("[ERROR] Exception during matrix file parsing.")
            print("Failed. Aborting ...")
            sys.exit()

def verify_input(dot, dest_path):
    if(dot is None):
        usage()
        sys.exit()
    if not os.path.isfile(dot):
        verboseprint("[ERROR] Path of dot file is invalid.")
        print("Failed. Aborting ...")
        sys.exit()
    if dest_path and not os.path.exists(dest_path):
        try:
            with open(dest_path, 'x') as tempfile:
                pass
        except OSError:
            verboseprint("[ERROR] Destination path invalid.")
            print("Failed. Aborting ...")
            sys.exit()
    if dest_path and os.path.isdir(dest_path):
        verboseprint("[ERROR] Destination is a directory.")
        print("Failed. Aborting ...")
        sys.exit()

def write_output(fname, header, record):
    with open(fname, 'w+') as text_file:
        try:
            text_file.write(', '.join(header)
                    .replace('[', '')
                    .replace(']', '')
                    .replace(' ,', ',') + "\n")
            text_file.write(', '.join(record) + "\n")
        except Exception as e:
            verboseprint("[ERROR] Exception during writing output to {:s}".format(fname))
            print("Failed. Aborting ...")
            print(e)
            sys.exit()

def avg_degree(graph):
    degrees = graph.degree(mode = "all")
    return sum(degrees) / len(degrees)

def avg_in_degree(graph):
    degrees = graph.degree(mode = "in")
    return sum(degrees) / len(degrees)

def avg_out_degree(graph):
    degrees = graph.degree(mode = "out")
    return sum(degrees) / len(degrees)

def degree_faulty_node(faulty_vertices):
    degree_sum = reduce(lambda x, y : x + y.degree(mode = "all"), faulty_vertices, 0)
    return degree_sum/len(faulty_vertices)

def in_degree_faulty_node(faulty_vertices):
    degree_sum = reduce(lambda x, y : x + y.degree(mode = "in"), faulty_vertices, 0)
    return degree_sum/len(faulty_vertices)

def out_degree_faulty_node(faulty_vertices):
    degree_sum = reduce(lambda x, y : x + y.degree(mode = "out"), faulty_vertices, 0)
    return degree_sum/len(faulty_vertices)

def directed_diameter(graph):
    return graph.diameter(directed=True)

def undirected_diameter(graph):
    return graph.diameter(directed=False)

def avg_closeness(graph):
    closeness = graph.closeness(mode="all")
    return sum(closeness) / len(closeness)

def avg_in_closeness(graph):
    closeness = graph.closeness(mode="in")
    return sum(closeness) / len(closeness)

def avg_out_closeness(graph):
    closeness = graph.closeness(mode="out")
    return sum(closeness) / len(closeness)

def closeness_faulty_node(faulty_vertices):
    degree_sum = reduce(lambda x, y : x + y.closeness(), faulty_vertices, 0)
    return degree_sum/len(faulty_vertices)

def weighted_edge_sum(graph):
    return len(graph.es) / len (graph.vs)

def above_avg_degree_vertices(graph):
    avg = avg_degree(graph)
    return (np.array(graph.degree()) > avg).sum()

def circles(nodes, edges):
    G = nx.DiGraph()
    G.add_nodes_from(nodes)
    G.add_edges_from(edges)
    return len(list(nx.simple_cycles(G)))

def weak_groups(graph):
    return len(graph.clusters(mode="WEAK"))

def usage():
    print("\nPython command tool to analyze dot files of callgraphs.\n")
    print("faultloc.py -d <dot file>")
    print("Parameters:")
    print("-d : specify dot file (--dot=)")
    print("-f : specify faulty node (--faulty=)")
    print("-w : specify output file (--write=)")
    print("-v : verbose output (--verbose)")
    print("-h : print this help")

def str(number):
    return "{0:.4f}".format(number).rstrip('0').rstrip('.')


if __name__ == "__main__":
    main(sys.argv[1:])
