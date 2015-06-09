$WEKA weka.classifiers.trees.RandomForest -t 'all_fgbg.arff' -d fgbg.model -I 100 -K 0 -S 1
#$WEKA weka.classifiers.trees.J48 -t 'all_fgbg.arff' -d fgbg.model -C 0.25 -M 2



#FilteredClassifier is wrong choice, we need to use some other, e.g. AdaBoostM1, or no meta classifier at all
#weka.classifiers.meta.AdaBoostM1
#$WEKA weka.classifiers.meta.FilteredClassifier -t 'all_fgbg.arff' -d fgbg.model -W weka.classifiers.trees.J48 -- -C 0.25 -M 2
#$WEKA weka.classifiers.meta.FilteredClassifier -t 'all_fgbg.arff' -d fgbg.model -W weka.classifiers.bayes.NaiveBayes
#$WEKA weka.classifiers.meta.FilteredClassifier -t 'all_fgbg.arff' -d fgbg.model -W weka.classifiers.trees.RandomForest -- -I 100 -K 0 -S 1
#$WEKA weka.classifiers.meta.FilteredClassifier -t 'all_fgbg.arff' -d fgbg.model -W weka.classifiers.bayes.BayesNet -- -D -Q weka.classifiers.bayes.net.search.local.K2 -- -S BAYES -E weka.classifiers.bayes.net.estimate.SimpleEstimator -- -A 0.5

