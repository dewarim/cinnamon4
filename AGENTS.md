# Agents

## Ground rules

* When making changes: run existing tests and create new tests for new features.
* use local temp as temporary folder, not /tmp or system temp dir
* use the idea-mcp server if possible - it provides lots of Java development tools.

* use the tools provided in /home/ingo/code/tools/scripts
    * maven-test-all.sh to run all maven tests
    * maven-compile.sh to check if code compiles
    * maven-single-test.sh to run a single test method; parameters are: \$testClass $methodName, for example: maven-single-test.sh AclServletIntegrationTest listAclsTest 