# kafka-control-partition
A **Java** library that must be used with **Spring Boot** and **Spring Kafka**. It will manager kafka partitions on request/reply.
# Usage
In your pom.xml use:

``` xml
<dependency>
   <groupId>org.bitbucket.tradedigital</groupId>
   <artifactId>kafka-control-partition</artifactId>
   <version>1.0.0</version>
</dependency>
```

and

``` xml
<repositories>
   <repository>
     <id>jitpack.io</id>
     <url>https://jitpack.io</url>
   </repository>
</repositories>
```

# Generating another version

1 - Create a new branch from the master branch.

2 - Make your modifications.

3 - Update the **pom.xml** version section, the **README.md** file, tag your last commit with the new version and push everything.

4 - Merge the code into the master branch. Check the permissions to do it.

5 - Create tag from de master branch whith name version from pom.xml.
