#!/usr/bin/env python
import sys,os

import fuzzy.OutputVariable
import fuzzy.InputVariable
import fuzzy.defuzzify.Dict
import fuzzy.fuzzify.Dict


def main(argv, stdin=sys.stdin, stdout=sys.stdout, stderr=sys.stderr):
    import fuzzy.storage.fcl.Reader
    system = fuzzy.storage.fcl.Reader.Reader().load_from_file(argv[1])
    #print system

    from fuzzy.doc.plot.gnuplot import doc
    directory = "docs-"+argv[1]
    try:
        os.makedirs(directory)
    except:
        pass
    d = doc.Doc(directory)
    d.createDoc(system)

    import fuzzy.doc.structure.dot.dot
    import subprocess
    for name,rule in system.rules.items():
            cmd = "dot -T png -o '%s/Rule %s.png'" % (directory,name)
            f = subprocess.Popen(cmd, shell=True, bufsize=32768, stdin=subprocess.PIPE).stdin
            fuzzy.doc.structure.dot.dot.print_header(f,"XXX")
            fuzzy.doc.structure.dot.dot.print_dot(rule,f,system,"")
            fuzzy.doc.structure.dot.dot.print_footer(f)
    cmd = "dot -T png -o '%s/System.png'" % directory
    f = subprocess.Popen(cmd, shell=True, bufsize=32768, stdin=subprocess.PIPE).stdin
    fuzzy.doc.structure.dot.dot.printDot(system,f)

    d.overscan=0
    in_vars = [name for name,var in system.variables.items() if isinstance(var,fuzzy.InputVariable.InputVariable)]
    out_vars = [name for name,var in system.variables.items() if isinstance(var,fuzzy.OutputVariable.OutputVariable)]
    if len(in_vars) == 2 and not (
            isinstance(system.variables[in_vars[0]].fuzzify,fuzzy.fuzzify.Dict.Dict)
        or
            isinstance(system.variables[in_vars[1]].fuzzify,fuzzy.fuzzify.Dict.Dict)
    ):
        for out_var in out_vars:
            args = []
            if isinstance(system.variables[out_var].defuzzify,fuzzy.defuzzify.Dict.Dict):
                for adj in system.variables[out_var].adjectives:
                    d.create3DPlot_adjective(system, in_vars[0], in_vars[1], out_var, adj, {})
            else:
                d.create3DPlot(system, in_vars[0], in_vars[1], out_var, {})

    #raw_input("Enter...")

if __name__ == '__main__':
    main(sys.argv)
