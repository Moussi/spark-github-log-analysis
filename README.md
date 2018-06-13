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

## reduceByKey

reduceByKey lets you merge all the values of a key into a single value of the same type.  
The merge function you pass to it merges two values at a time until there is only one value left.  
The function should be associative; otherwise you won’t get the same result every time you perform  
the reduceByKey transformation on the same RDD.

## foldByKey

foldByKey does the same thing as reduceByKey, except that it requires an additional parameter, zeroValue,  
in an extra parameter list that comes before the one with the reduce function.

zeroValue should be a neutral value (0 for addition, 1 for multiplication, Nil for lists, and so forth).  
It’s applied on the first value of a key (using the input function), and the result is applied on the  
second value. You should be careful here, because, unlike in the foldLeft and foldRight methods in Scala,  
zeroValue may be applied multiple times. This happens because of RDD’s parallel nature.

## aggregateByKey

aggregateByKey is similar to foldByKey and reduceByKey in that it merges values and takes a zero value,  
but it also transforms values to another type. In addition to the zeroValue argument, it takes two  
functions as arguments: a transform function for transforming values from type V to type U  
(with the signature (U, V) => U) and a merge function for merging the transformed values  
(with the signature (U, U) => U)

`Signature(initial value)(function inside partition, function between partitions)`

# Partitioning and shuffling
## Partitioning
Data partitioning is Spark’s mechanism for dividing data between multiple nodes in a cluster.  
Each part (piece or slice) of an RDD is called a partition. When you load a text file from your  
local filesystem into Spark, for example, the file’s contents are split into partitions, which are  
evenly distributed to nodes in a cluster.  

The number of RDD partitions is important because, in addition to influencing data distribution throughout the cluster,  
it also directly determines the number of tasks that will be running RDD transformations.
 
Partitioning of RDDs is performed by Partitioner objects that assign a partition index to each RDD element.  
Two implementations are provided by Spark: `HashPartitioner` and `RangePartitioner`.  
Pair RDDs also accept `custom partitioners`.

### HashPartitioner
`HashPartitioner` is the default partitioner in Spark.  

```
### Partition index calculation
partitionIndex = hashCode % numberOfPartitions
```

The default number of data partitions when using HashPartitioner is determined by the Spark configuration  
parameter `spark.default.parallelism`.  
If that parameter isn’t specified by the user, it will be set to the number of cores in the cluster

### RangePartitioner
