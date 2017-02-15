# Summary
The following example demonstrates the java sdk running a sample program against docker images of a working commit level of the Hyperledger Fabric V1.0 architecture.  The development of the fabric is still under development.  This java program and fabric commit levels are not supported. 

The commit levels used are:

| Project        | Commit level                               | Date        |
|:---------------|:------------------------------------------:|------------:|
| fabric         | 5d9e4ede298ab646ac918dc5b034c7d319dd1d9a   | Jan 30 2017 |
| fabric-ca      | bf8fb4d5e497217cd6125025830aa6870de442aa   | Jan 27 2017 |



## How to use  Fabric Java SDK with a sample program



Step 1:  Clone the java sdk repository and building jar

Note: this assumes you will clone the repositories from your home directory.

```
cd ~
git clone https://github.com/hyperledger/fabric-sdk-java.git
cd fabric-sdk-java
GOPATH=$PWD/src/test/fixture
mvn install -DskipTests
```

We have just generated the **fabric-java-sdk-1.0-SNAPSHOT.jar** 
**NOTE**: This is a short-term step until we have maven repository.

Step 2:  Clone the repository to create a network 
```
cd ~
git clone https://github.com/ratnakar-asara/FabricJavaSample.git
cd FabricJavaSample
```

Run the docker-compose file that will pull the docker images and start your fabric

```
docker-compose up -d
```

Now, we need to set the GOPATH
set **GOPATH** for the chaincode written in GO| Project        | Commit level                               | Date        |
|:---------------|:------------------------------------------:|------------:|
| fabric         | 5d9e4ede298ab646ac918dc5b034c7d319dd1d9a   | Jan 30 2017 |
| fabric-ca      | bf8fb4d5e497217cd6125025830aa6870de442aa   | Jan 27 2017 |
LANG to be properly packaged.
```
GOPATH=$PWD
```

Step 3:  Run Sample Program


Execute the below command to pull all the dependant libraries from maven.  This is a temporary solution and won't be needed once the java sdk jar is available in the maven repository.
```
 mvn clean install exec:java
```

You should see the following output at the end of execution.


```
Creating deployment proposal
Deploying chain code with a and b set to 100 and 200 respectively
Received 1 deployment proposal responses. Successful+verified: 1 . Failed: 0
Successfully completed chaincode deployment.
Creating invoke proposal
Received 1 invoke proposal responses. Successful+verified: 1 . Failed: 0
Successfully received invoke proposal response.
Invoking chain code to move 100 from a to b.
Now query chain code for the value of b.
Successfully received query response.
Query payload of b returned 300
Completed End to End Demo ...

```

**Note:**  
The configuration file used for the java program are located at:
FabricJavaSample/config.properties

and can be customized as desired.


Step 4:  In summary, this sample is based on source code:  FabricJavaSample/src/main/java/org/hyperledger/fabric/sdkexample/End2EndSample.java
