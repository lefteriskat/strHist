GETTING STARTED
=======

### Abstract

This is an extension to the STHoles algorithm for RDF data, based on URI prefixes.

### Building project

**Prerequisites**

* git
* mvn
* java 1.7 or above
* Virtuoso Instance with dbpedia dumps of article titles and categories

**Instructions**

* Clone from [github](https://github.com/lefteriskat/strHist.git)
* Change directory into the project root, etc *path_to_project*/strHist
* Install some custom dependencies in your maven repo 
	(Instructions for this in the README file into custom_dependencies folder under strHist)
* Run 'mvn install' to build project


### Experiments & Evaluation

The experiment procedures are divided in 3 main executables for preparing the workload, refining and evaluating it. The abstract order of execution is as follows:

1. Prepare training workload
2. Refine training workload
3. Evaluate

**Instructions**

* Change directory into my_scripts/ directory
* Run prepare_training_workload.sh <number of training batches> <number of queries in each batch> <dbpedia version to run the batch> 
* Run refine_training_workload.sh <number of training batches> <dbpedia version of the batch>
* Run evaluate_on_virtuoso.sh <number of queries to be evaluated> <dbpedia version to run and evaluate the queries>