import sys
import os
import getopt
import numpy as np
import csv
import json

verboseprint = None
def main(argv):
    json_file = None
    dest_path = None
    verbose = False

    # Argument parsing
    try:
        opts, args = getopt.getopt(argv, "hj:vw:",["help", "json=", "verbose", "write="])
    except getopt.GetoptError:
        usage()
    for opt, arg in opts:
        if opt in ("-h", "--help"):
            usage()
            sys.exit()
        elif opt in ("-j", "--json"):
            json_file = arg
        elif opt in ("-w", "--write"):
            dest_path = arg
        elif opt in ("-v", "--verbose"):
            verbose = True

    # Verify input
    global verboseprint
    verboseprint = print if verbose else lambda *a, **k: None
    verboseprint("[STATUS] Verifying your input ...")
    verify_input(json_file, dest_path)
    verboseprint("[STATUS] Input verified.")

    verboseprint("[STATUS] Loading json file ...")
    results = analyze_json(json_file)
    verboseprint("[STATUS] Output generated!")

    if dest_path:
        verboseprint("[STATUS] Writing output to file " + dest_path)
        header = ["Project", "Version", "Exception"]
        write_output(dest_path, header, results)
        verboseprint("[STATUS] Output written!")
    else:
        verboseprint("[INFO] Printing results ...\n")
        print(results)
    verboseprint("[STATUS] Success! Exiting ...")

def verify_input(json, dest_path):
    if json is None:
        usage()
        sys.exit()
    if not os.path.isfile(json):
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


def analyze_json(json_file):
    results = None
    with open(json_file) as f:
        data = json.load(f)
        for i in range(0,395):
            project = data[i]
            name = project["project"]
            version = project["bugId"]
            error_name = first_exception(project)
            if results is None:
                results = np.array([name, version, error_name])
            else:
                results = np.vstack((results, [name, version, error_name]))
    # Sorting
    ind = np.lexsort((results[:,1].astype(int),results[:,0]))
    return results[ind]


def first_exception(project):
    tests = project["failingTests"]
    error = tests[0]["error"]
    return error


def write_output(fname, header, record):
    with open(fname, 'w+') as text_file:
        try:
            text_file.write(','.join(header) + "\n")
            for line in record:
                text_file.write(', '.join(line) + "\n")
        except Exception as e:
            verboseprint("[ERROR] Exception during writing output to {:s}".format(fname))
            print("Failed. Aborting ...")
            print(e)
            sys.exit()

def usage():
    print("\nPython commandline tool to analyze the disection4j json file.\n")
    print("bug_solver.py -d <dot file>")
    print("Parameters:")
    print("-j : specify json file (--json=)")
    print("-w : specify output file (--write=)")
    print("-v : verbose output (--verbose)")
    print("-h : print this help")

def str(number):
    return "{0:.4f}".format(number).rstrip('0').rstrip('.')


if __name__ == "__main__":
    main(sys.argv[1:])

