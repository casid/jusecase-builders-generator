// The failed run must not have deleted the builders that are checked in
assert new File(basedir, "src/test/builders/example/UserBuilderMethods.java").exists()
