./prepare_training_workload $1 $2 3.2
./refine_training_workload $1 3.2
./evaluate_on_virtuoso $3 3.2
./prepare_training_workload $1 $2 3.3
./refine_training_workload $1 3.3
./evaluate_on_virtuoso $3 3.3
