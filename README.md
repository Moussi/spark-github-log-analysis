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
## lookup
Be careful when using lookup function, a lookup function will transfer the values to the driver,  
so you have to make sure the values will fit in its memory.

```
val trans = transData.lookup(53)
```
## mapValues

The mapValues transformation changes the values contained in a pair RDD without  
changing the associated keys

## flatMapValues

The flatMapValues transformation enables you to change the number of elements corresponding to a key   
by mapping each value to zero or more values. That means you can add new values for a key or remove   
a key altogether.  
The signature of the transformation function it expects is `V => TraversableOnce[U]`  
(TraversableOnce is just a special name for a collection). From each of the values in the return  
collection, a new key-value pair is created for the corresponding key. If the transformation function  
returns an empty list for one of the values, the resulting pair RDD will have one fewer element.  
If the transformation function returns a list with two elements for one of the values, the resulting  
pair RDD will have one more element. Note that the mapped values can be of a different type than before.
