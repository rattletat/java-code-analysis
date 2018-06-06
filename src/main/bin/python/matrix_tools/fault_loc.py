import sys
import os
import getopt
import csv
import numpy as np

techniques = ["dstar2", "dstar3", "jaccard", "ochiai", "tarantula", "zoltar"]

total_passed = 0 # total passed test cases
total_failed = 0 # total failed test cases
total = 0
passed_statements = [] # executions by passing test cases
failed_statements = [] # executions by failing test cases

verboseprint = None

def main(argv):
    matrix = ''
    spectra = ''
    technique = ''
    number = None
    max_rank = None
    dest_path = None
    verbose = False

    # Argument parsing
    try:
        opts, args = getopt.getopt(argv, "hm:s:t:vn:r:w:",["help", "matrix=", "spectra=", "technique=", "verbose"])
    except getopt.GetoptError:
        usage()
        sys.exit()

    for opt, arg in opts:
        if opt in ("-h", "--help"):
            usage()
            sys.exit()
        elif opt in ("-m", "--matrix"):
            matrix = arg
        elif opt in ("-s", "--spectra"):
            spectra = arg
        elif opt in ("-t", "--technique"):
            technique = arg
        elif opt == "-n":
            number = arg
        elif opt == "-r":
            max_rank = arg
        elif opt == "-w":
            dest_path = arg
        elif opt in ("-v", "--verbose"):
            verbose = True

    # Verify input
    global verboseprint
    verboseprint = print if verbose else lambda *a, **k: None
    verboseprint("[STATUS] Verifying your input ...")
    verify_input(matrix, spectra, technique, number, max_rank, dest_path)
    if number: number = int(number)
    if max_rank: max_rank = int(max_rank)
    verboseprint("[STATUS] Input verified.")

    # Get matrix and spectra data
    verboseprint("[STATUS] Loading matrix file ...")
    verboseprint("[STATUS] Matrix file successfully loaded. Analyzing ...")
    analyze_matrix(matrix)
    verboseprint("[STATUS] Analyzing finished! Extracted test cases:")
    verboseprint("[INFO] Total passed: {:d}".format(total_passed))
    verboseprint("[INFO] Total failed: {:d}".format(total_failed))
    verboseprint("[STATUS] Loading spectra file ...")
    spectra_lines = load_spectra(spectra)
    verboseprint("[STATUS] Spectra file successfully loaded.")

    # Call design metric
    verboseprint("[STATUS] Scoring test cases with technique " + technique)
    scores = call_design_metric(technique)
    verboseprint("[STATUS] Scores for test cases successfully generated!")

    # Rank items
    verboseprint("[STATUS] Ranking test cases ...")
    ranks = rank(scores)
    verboseprint("[STATUS] Test cases ranked!")
    sorted_rank_indices = np.argsort(ranks)
    verboseprint("[STATUS] Generating output ...")

    for i, line in enumerate(spectra_lines):
        spectra_lines[i] = "Rank: " + str(ranks[i]) + " | " + "Suspiciousness: " + "{:.4f}".format(scores[i]) + " | " + line
    np_spectra_lines = np.array(spectra_lines)
    sorted_lines = np_spectra_lines[sorted_rank_indices]

    output = sorted_lines
    if number:
        output = sorted_lines[:number]
    if max_rank:
        i, = np.where(np.sort(ranks) == max_rank)
        if len(i) == 0:
            output = sorted_lines
        else:
            output = sorted_lines[:(i[-1]+1)]
    verboseprint("[STATUS] Output generated!")
    if dest_path:
        verboseprint("[STATUS] Writing output to file " + dest_path)
        write_output(dest_path, output)
        verboseprint("[STATUS] Output written!")
    else:
        verboseprint("[INFO] Printing results ...")
        print(output)
    verboseprint("[STATUS] Success! Exiting ...")


def analyze_matrix(matrix):
    global passed_statements, failed_statements, total_failed, total_passed, scores, total
    with open(matrix) as csvfile:
        reader = csv.reader(csvfile, delimiter=' ')
        try:
            for row in reader:
                if passed_statements == []:
                    passed_statements = np.zeros(len(row)-1)
                    failed_statements = np.zeros(len(row)-1)
                if row[-1] == '+':
                    total_passed += 1
                    visited_statements = np.array(row[:-1]).astype(int)
                    passed_statements = np.add(passed_statements, visited_statements)
                elif row[-1] == '-':
                    total_failed += 1
                    visited_statements = np.array(row[:-1]).astype(int)
                    failed_statements = np.add(failed_statements, visited_statements)
            total = total_failed + total_passed
        except csv.Error as ex:
            verboseprint("[ERROR] Exception during matrix file parsing.")
            print("Failed. Aborting ...")
            sys.exit()

def verify_input(matrix, spectra, technique, number, max_rank, dest_path):
    if(matrix == '' or spectra == '' or technique == ''):
        usage()
        sys.exit()
    if not os.path.isfile(matrix):
        verboseprint("[ERROR] Path of matrix is invalid.")
        print("Failed. Aborting ...")
        sys.exit()
    if not os.path.isfile(spectra):
        verboseprint("[ERROR] Path of spectra is invalid.")
        print("Failed. Aborting ...")
        sys.exit()
    if not technique in techniques:
        verboseprint("[ERROR] {:s} is not a valid technique. Possible arguments:".format(technique))
        verboseprint(str(techniques))
        print("Failed. Aborting ...")
        sys.exit()
    if number and max_rank:
        verboseprint("[ERROR] Do not specify rank and number of outputs together.")
        print("Failed. Aborting ...")
        sys.exit()
    if number:
        try: number = int(number)
        except ValueError:
            verboseprint("[ERROR] Invalid number of test cases given.")
            print("Failed. Aborting ...")
            sys.exit()
        if number <= 0:
            verboseprint("[ERROR] Number of test cases has to be positive.")
            print("Failed. Aborting ...")
            sys.exit()
    if max_rank:
        try: max_rank = int(max_rank)
        except ValueError:
            verboseprint("[ERROR] Invalid rank given.")
            print("Failed. Aborting ...")
            sys.exit()
        if max_rank <= 0:
            verboseprint("[ERROR] Rank has to be positive.")
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

def load_spectra(fname):
    data = []
    with open(fname, 'r') as text_file:
        try:
            for line in text_file.readlines():
                data.append(line.replace('\n',''))
        except Exception as e:
            verboseprint("[ERROR] Exception during spectra parsing.")
            print("Failed. Aborting ...")
            sys.exit()
    return data

def write_output(fname, output):
    with open(fname, 'w+') as text_file:
        try:
            for line in output:
                text_file.write(line + '\n')
        except Exception:
            verboseprint("[ERROR] Exception during writing output to {:s}".format(fname))
            print("Failed. Aborting ...")
            sys.exit()

def call_design_metric(technique):
    if technique == "dstar2":
        scores = dstar2()
    elif technique == "dstar3":
        scores = dstar3()
    elif technique == "jaccard":
        scores = jaccard()
    elif technique == "ochiai":
        scores = ochiai()
    elif technique == "tarantula":
        scores = tarantula()
    elif technique == "zoltar":
        scores = zoltar()
    else:
        print("[ERROR] Technique not implemented yet.")
        print("Failed. Aborting ...")
        sys.exit()
    return scores

def dstar2():
    result = np.zeros(len(passed_statements))
    for i, _ in enumerate(result):
        exec_pass = passed_statements[i]
        exec_fail = failed_statements[i]
        noex_fail = total_failed - exec_fail
        result[i] = exec_fail**2 / (noex_fail + exec_pass)
    return result

def dstar3():
    result = np.zeros(len(passed_statements))
    for i, _ in enumerate(result):
        exec_pass = passed_statements[i]
        exec_fail = failed_statements[i]
        noex_fail = total_failed - exec_fail
        result[i] = exec_fail**3 / (noex_fail + exec_pass)
    return result

def jaccard():
    result = np.zeros(len(passed_statements))
    for i, _ in enumerate(result):
        exec_pass = passed_statements[i]
        exec_fail = failed_statements[i]
        result[i] = exec_fail / (total_failed + exec_pass)
    return result

def ochiai():
    result = np.zeros(len(passed_statements))
    for i, _ in enumerate(result):
        exec_pass = passed_statements[i]
        exec_fail = failed_statements[i]
        result[i] = exec_fail / np.sqrt(total_failed * (exec_fail + exec_pass))
    return result

def tarantula():
    result = np.zeros(len(passed_statements))
    for i, _ in enumerate(result):
        exec_pass = passed_statements[i]
        exec_fail = failed_statements[i]
        ratio_failing = (exec_fail/total_failed)
        ratio_passing = (exec_pass/total_passed)
        result[i] = ratio_failing / (ratio_failing + ratio_passing)
    return result

def zoltar():
    result = np.zeros(len(passed_statements))
    for i, _ in enumerate(result):
        exec_pass = passed_statements[i]
        exec_fail = failed_statements[i]
        noex_pass = total_passed - exec_pass
        noex_fail = total_failed - exec_fail
        result[i] = exec_fail / (total_failed + exec_pass + 1000 * (noex_fail * exec_pass / exec_fail))
    return result

def usage():
    print("\nPython command tool to evaluate Gzoltar outputs.\n")
    print("faultloc.py -m <matrix file> -s <spectra file> -t <technique>")
    print("Techniques: " + ", ".join(x for x in techniques) + "\n")
    print("Parameters:")
    print("-m : specify matrix file (--matrix=)")
    print("-s : specify spectra file (--spectra=)")
    print("-t : specify technique for evaluation (--technique=)")
    print("-w : specify output file")
    print("-n : specify number of objects to output")
    print("-r : specify number of ranks to output")
    print("-v : verbose output (--verbose)")
    print("-h : print this help")

def rank(v):
    if v is None or len(v) == 0:
        return []
    desc_indices = np.flipud(np.argsort(v))
    # Sort NaN values to the end
    desc_indices = np.roll(desc_indices, -np.count_nonzero(np.isnan(v)))
    result = np.empty(len(v),int)
    result[desc_indices[0]] = 1
    for i in range(1, len(result)):
        if v[desc_indices[i]] == v[desc_indices[i-1]] or (np.isnan(v[desc_indices[i]]) and np.isnan(v[desc_indices[i-1]])):
            result[desc_indices[i]] = result[desc_indices[i-1]]
        else:
            result[desc_indices[i]] = result[desc_indices[i-1]] + 1
    return result

if __name__ == "__main__":
    main(sys.argv[1:])
