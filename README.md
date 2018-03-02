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
	(You can download dbpedia dumps from [here](http://wiki.dbpedia.org/develop/datasets "Dbpedia Downloads") I recommend to start from dataset3.2 and download only Article Categories dump for this phase)
* After downloading you have to put the dump from dataset3.2 into the graph <http://dbpedia3.2.org> from dataset3.3 into graph <http://dbpedia3.3.org> etc in order to be compatible with current implementation of strhist.

**Instructions**

* Clone from [github](https://github.com/lefteriskat/strHist.git)
* Change directory into the project root, etc *path_to_project*/strHist
* Change variable strhist.path into pom.xml line 18
* Install some custom dependencies in your maven repo 
	(You can find a script in custom_dependencies folder for this but you have to change
	the path to your strhist root path)
* Run 'mvn install' to build project


### Experiments & Evaluation

The experiment procedures are divided in 3 main executables for preparing the workload, refining and evaluating it. The abstract order of execution is as follows:

1. Prepare training workload
2. Refine training workload
3. Evaluate

**Instructions**

* Change directory into my_scripts/ directory
* Set the STRHIST_PATH variable running -> export STRHIST_PATH="<path_to_strhist>/strHist"
* Run prepare_training_workload.sh number_of_training_batches number_of_queries_in_each_batch dbpedia_version_to_run_the_batch 
* Run refine_training_workload.sh number_of_training_batches dbpedia_version_of_the_batch
* Run evaluate_on_virtuoso.sh number_of_queries_to_be_evaluated dbpedia_version_to_run_and_evaluate_the_queries
* After these steps you can find the results{version}.txt into /var/tmp/strHist/ folder.
* Otherwise if you have loaded the datasets 3.2 and 3.3 in your virtuoso dump you can run the full experiment by executing
	./run_full_experiment number_of_training_batches number_of_queries_in_each_batch number_of_queries_to_evaluate
* After this last command you can find result3.2.txt and result3.3.txt into /var/tmp/strHist/ folder.