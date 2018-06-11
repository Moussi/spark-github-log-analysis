# Using Spark-submit

```
spark-submit \
    --class <main-class> \
    --master <master-url> \
    --deploy-mode <deploy-mode> \
    --conf <key>=<value> \
    ... # other options
    <application-jar> \
    [application-arguments]
```

```
spark-submit --class com.moussi.app.GithubDayCluster --master local[*]  
 --name "Daily GitHub Push Counter"  
 /target/spark-github-log-analysis-1.0-SNAPSHOT.jar
 "/github-archive-sia/*.json"  
 "ghEmployees.txt"  
 "$HOME/emp-gh-push-output" "json"
```
# Pair RDDs

Be careful of using lookup function, a lookup function will transfer the values to the driver,  
so you have to make sure the values will fit in its memory.

```
val trans = transData.lookup(53)
```
