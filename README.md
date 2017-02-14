# How to use  Fabric Java SDK

As per the instructions provided [here](https://github.com/hyperledger/fabric-sdk-java#running-the-end-to-end-tests) generate the required docker images.

here is for your reference

Hyperledger Fabric v1.0 is currently under active development and the very latest Hyperledger Fabric builds may not work with this sdk.
You should use the following commit levels of the Hyperledger projects:

| Project        | Commit level                               | Date        |
|:---------------|:------------------------------------------:|------------:|
| fabric         | 5d9e4ede298ab646ac918dc5b034c7d319dd1d9a   | Jan 30 2017 |
| fabric-ca      | bf8fb4d5e497217cd6125025830aa6870de442aa   | Jan 27 2017 |

Also generate the **fabric-java-sdk-1.0-SNAPSHOT.jar** after cloning and building the SDK code.
**NOTE**: We don't have maven repository and can't avoid this step

```
git clone https://github.com/hyperledger/fabric-sdk-java.git
```
```
cd fabric-sdk-java
GOPATH=$PWD/src/test/fixture
mvn install -DskipTests
```

Once the Jar and docker images are available , you are ready to go ahead and run the sample program.

* execute the docker-compose file available in fabric-sdk-repo folder
```
cd fabric-sdk-java/tree/master/src/test/fixture/src

docker-compose up -d
```

* Clone this repo
```
https://github.com/ratnakar-asara/FabricJavaSample
```

set **GOPATH**
```
cd FabricJavaSample
GOPATH=$PWD
```

execute the below command:
```
 mvn clean install exec:java
```

You should see the below comment:


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
