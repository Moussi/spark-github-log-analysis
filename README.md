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

RangePartitioner partitions data of sorted RDDs into roughly equal ranges. It samples the contents of  
the RDD passed to it and determines the range boundaries according to the sampled data.

### Custom Partitioner

Pair RDDs can be partitioned with custom partitioners when it’s important to be precise about the placement  
of data among partitions (and among tasks working on them). You may want to use a custom partitioner,  
for example, if it’s important that each task processes only a specific subset of key-value pairs,  
where all of them belong to a single database, single database table, single user, or something similar.

Custom partitioners can be used only on pair RDDs, by passing them to pair RDD transformations.   
Most pair RDD transformations have two additional overloaded methods: one that takes an additional  
Int argument (the desired number of partitions) and another that takes an additional argument of the  
(custom) Partitioner type.  
The method that takes the number of partitions uses the default Hash-Partitioner.  
For example, these two lines of code are equal, because they both apply HashPartitioner with 100 partitions:

```
rdd.foldByKey(afunction, 100)
rdd.foldByKey(afunction, new HashPartitioner(100))
```

## Shuffling

Physical movement of data between partitions is called shuffling. It occurs when data from multiple  
partitions needs to be combined in order to build partitions for a new RDD.  
When grouping elements by key, for example, Spark needs to examine all of the RDD’s partitions, find  
elements with the same key, and then physically group them, thus forming new partitions.

Tasks that immediately precede and follow the shuffle are called map and reduce tasks, respectively.  
The results of map tasks are written to intermediate files (often to the OS’s filesystem cache only)  
and read by reduce tasks. In addition to being written to disk, the data is sent over the network,  
so it’s important to try to minimize the number of shuffles during Spark jobs. 

```
TIP
Because changing the partitioner provokes shuffles, the safest approach, performance-wise, is to use  
a default partitioner as much as possible and avoid inadvertently causing a shuffle.
```

* Shuffling when explicitly changing partitioners: Shuffling also occurs if a different HashPartitioner  
than the previous one is used. Two HashPartitioners are the same if they have the same number of partitions. 
* Shuffling always occurs when using a custom partitioner in methods that allow you to do that (most pair RDD transformations).  
* Shuffle caused by partitioner removal: Sometimes a transformation causes a shuffle, although we  
were using the default partitioner. `map` and `flatMap` transformations remove the RDD’s partitioner,  
which doesn’t cause a shuffle. Here’s a complete list of transformations that cause a shuffle after  
`map` or `flatMap` transformations:  
- Pair RDD transformations that can change the RDD’s partitioner: `aggregateByKey`, `foldByKey`, `reduceByKey`,  
`groupByKey`, `join`, `leftOuterJoin`, `rightOuterJoin`, `fullOuterJoin`, and `subtractByKey`

```
External shuffle service
An external shuffle service is meant to optimize the exchange of shuffle data by providing a single  
point from which executors can read intermediate shuffle files.  
If an external shuffle service is enabled (by setting `spark.shuffle.service.enabled` to true),  
one external shuffle server is started per worker node.
```

Spark has two shuffle implementations: **sort-based** and **hash-based**.  
You can define which shuffle implementation to use by setting the value of the `spark.shuffle.manager`  
parameter to either `hash` or `sort`.

**Shuffling Parameters**: 
* `spark.shuffle.spill` parameter specifies whether the amount of memory used for these tasks should be  
limited (the default is true)  
* The memory limit is specified by the `spark.shuffle.memoryFraction` parameter (the default is 0.2).  
* `spark.shuffle.spill.compress` parameter tells Spark whether to use compression for the spilled  
data (the default is again true).
* `spark.shuffle.compress` specifies whether to compress intermediate files (the default is true).  
* `spark.shuffle.spill.batchSize` specifies the number of objects that will be serialized or deserialized together when spilling to disk. The default is 10,000.  
* `spark.shuffle.service.port` specifies the port the server will listen on if an external shuffle service is enabled.  

# Repartitioning RDDs
Repartitioning of RDDs can be accomplished with the:  
* `partitionBy`: set a new partitioner , a shuffle is scheduled and a new RDD is created.  
* `coalesce`: is used for either reducing or increasing the number of partition. 
* `repartitionAndSortWithinPartition`: It’s available only on sortable RDDs (pair RDDs with sortable keys).  

# Mapping data in partitions
The last aspect of data partitioning we want to tell you about is mapping data in partitions.  
Spark offers a way to apply a function not to an RDD as a whole, but to each of its partitions separately.  
This can be a precious tool in optimizing your transformations. Many can be rewritten to map data in  
partitions only, thus avoiding shuffles. RDD operations for working on partitions are `mapPartitions`,  
`mapPartitionsWithIndex`, and `glom`, a specialized partition-mapping transformation.


