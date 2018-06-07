import sys
import os
import getopt
import numpy as np
import csv

verboseprint = None
def main(argv):
    matrix = ''
    dest_path = None
    verbose = False

    # Argument parsing
    try:
        opts, args = getopt.getopt(argv, "hm:vw:",["help", "matrix=", "verbose"])
    except getopt.GetoptError:
        usage()
    for opt, arg in opts:
        if opt in ("-h", "--help"):
            usage()
            sys.exit()
        elif opt in ("-m", "--matrix"):
            matrix = arg
        elif opt == "-w":
            dest_path = arg
        elif opt in ("-v", "--verbose"):
            verbose = True

    # Verify input
    global verboseprint
    verboseprint = print if verbose else lambda *a, **k: None
    verboseprint("[STATUS] Verifying your input ...")
    verify_input(matrix, dest_path)
    verboseprint("[STATUS] Input verified.")

    # Get matrix and spectra data
    verboseprint("[STATUS] Loading matrix file ...")
    verboseprint("[STATUS] Matrix file successfully loaded. Analyzing ...")
    data, results = analyze_matrix(matrix)

    m00 = str(num_tests(results))
    m01 = str(num_passing_tests(results))
    m02 = str(num_failing_tests(results))
    m03 = str(percentage_passing_tests(results))
    m04 = str(percentage_failing_tests(results))
    m05 = str(num_elements(data))
    m06 = str(num_visited_elements(data))
    m07 = str(num_not_visited_elements(data))
    m08 = str(total_visits(data))
    m09 = str(sparsity(data))
    m10 = str(coverage(data))
    m11 = str(coverage_passing_tests(data, results))
    m12 = str(coverage_failing_tests(data, results))
    m13 = str(avg_num_visited_elements(data))
    m14 = str(avg_num_pass_visited_elements(data, results))
    m15 = str(avg_num_fail_visited_elements(data, results))
    m16 = str(same_visited_elements(data, results))
    m17 = str(percentage_same_visited_elements(data, results))

    # header = ["[TM-" + "{:02d}".format(i) + "] " for i in range(18)]
    header = [
        "T-#T",
        "T-#PT",
        "T-#FT",
        "T-%PT",
        "T-%FT",
        "T-#E",
        "T-#VE",
        "T_#NVE",
        "T-#V",
        "T-Spa",
        "T-Cov",
        "T-CovPT",
        "T-CovFT",
        "T-AvgVE",
        "T-AvgVEPT",
        "T-AvgVEFT",
        "T-#VEP^FT",
        "T-%VEP^FT"
    ]

    output =  header[0] + "Number of tests: " + m00 + "\n"
    output += header[1] + "Number of passing tests: " + m01 + "\n"
    output += header[2] + "Number of failing tests: " + m02 + "\n"
    output += header[3] + "Percentage of passing tests: " + m03 + "\n"
    output += header[4] + "Percentage of failing tests: " + m04 + "\n"
    output += header[5] + "Number of elements: " + m05 + "\n"
    output += header[6] + "Number of visited elements: " + m06 + "\n"
    output += header[7] + "Number of not visited elements: " + m07 + "\n"
    output += header[8] + "Number of visits in total: " + m08 + "\n"
    output += header[9] + "Sparsity: " + m09 + "\n"
    output += header[10] + "Coverage: " + m10 + "\n"
    output += header[11] + "Coverage of passing tests: " + m11 + "\n"
    output += header[12] + "Coverage of failing tests: " + m12 + "\n"
    output += header[13] + "Average number of visited elements: " + m13 + "\n"
    output += header[14] + "Average number of visited elements by passing tests: " + m14 + "\n"
    output += header[15] + "Average number of visited elements by failing tests: " + m15 + "\n"
    output += header[16] + "Number of elements visited both by passing and failing tests: " + m16 + "\n"
    output += header[17] + "Percentage of all visited elements visited both by passing and failing tests: " + m17 + "\n"

    verboseprint("[STATUS] Output generated!")
    if dest_path:
        verboseprint("[STATUS] Writing output to file " + dest_path)
        record = [m00, m01, m02, m03, m04, m05, m06, m07, m08, m09, m10, m11, m12, m13, m14, m15, m16, m17]
        write_output(dest_path, header, record)
        verboseprint("[STATUS] Output written!")
    else:
        verboseprint("[INFO] Printing results ...\n")
        print(output)
    verboseprint("[STATUS] Success! Exiting ...")


def analyze_matrix(matrix):
    with open(matrix) as csvfile:
        reader = csv.reader(csvfile, delimiter=' ')
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

def verify_input(matrix, dest_path):
    if(matrix == '' or matrix is None or dest_path == '' or dest_path is None):
        usage()
        sys.exit()
    if not os.path.isfile(matrix):
        verboseprint("[ERROR] Path of matrix is invalid.")
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
    if os.path.isdir(dest_path):
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

def num_failing_tests(results):
    return (results == '-').sum()

def num_passing_tests(results):
    return (results == '+').sum()

def num_tests(results):
    return len(results)

def percentage_failing_tests(results):
    return (results == '-').sum()/len(results)

def percentage_passing_tests(results):
    return (results == '+').sum()/len(results)

def num_elements(data):
    return data.shape[1]

def num_visited_elements(data):
    return (np.sum(data, axis=0) != 0).sum()

def num_not_visited_elements(data):
    return (np.sum(data, axis=0) == 0).sum()

def total_visits(data):
    return np.sum(data)

def sparsity(data):
    return np.sum(data)/(np.prod(data.shape))

def coverage(data):
    return num_visited_elements(data) / num_elements(data)

def coverage_passing_tests(data, results):
    passing_tests = get_passing_data(data, results)
    return num_visited_elements(passing_tests) / num_elements(data)

def coverage_failing_tests(data, results):
    failing_tests = get_failing_data(data, results)
    return num_visited_elements(failing_tests) / num_elements(data)

def avg_num_visited_elements(data):
    visits = np.sum(data, axis=1)
    return np.sum(visits)/ len(data)

def avg_num_pass_visited_elements(data, results):
    passing_tests = get_passing_data(data, results)
    visits = np.sum(passing_tests, axis=1)
    return np.sum(visits)/ len(passing_tests)

def avg_num_fail_visited_elements(data, results):
    failing_tests = get_failing_data(data, results)
    visits = np.sum(failing_tests, axis=1)
    return np.sum(visits)/ len(failing_tests)

def same_visited_elements(data, results):
    passing_data = get_passing_data(data, results)
    failing_data = get_failing_data(data, results)
    visited_passing = (np.sum(passing_data, axis=0) != 0)
    visited_failing = (np.sum(failing_data, axis=0) != 0)
    both_visited = np.logical_and(visited_passing, visited_failing)
    return both_visited.sum()

def percentage_same_visited_elements(data, results):
    return same_visited_elements(data, results) / num_visited_elements(data)

def get_passing_data(data, results):
    mask = np.transpose(results == '+')[0]
    return data[mask]

def get_failing_data(data, results):
    mask = np.transpose(results == '-')[0]
    return data[mask]

def usage():
    print("\nPython command tool to analyze hit-spectra matrices.\n")
    print("faultloc.py -m <matrix file>")
    print("Parameters:")
    print("-m : specify matrix file (--matrix=)")
    print("-w : specify output file")
    print("-v : verbose output (--verbose)")
    print("-h : print this help")

def str(number):
    return "{0:.4f}".format(number).rstrip('0').rstrip('.')


if __name__ == "__main__":
    main(sys.argv[1:])
