# Graph Tracing Engine                                                                        
![BEGOOD logo](BEGOOD_logo.png)

The Graph Tracing Engine is a micro service that allows tracing various data sets through a REST service. 
It supports any kind of topological network, and was originally built for rivers and sewer systems. 

The Graph Tracing Engine is a spring-boot application, that uses geotools for reading data sets and JGraphT for 
tracing. The web services are documented with swagger.

#### Building

Requirements: JDK 1.8 or greater.

The application can be built using the included maven wrapper:

`./mvnw clean install` (linux)

`mvnw clean install` (windows)

or using your own installation of maven:

`mvn clean install`.

This will generate a war file, that can be deployed in an application server or run it stand alone. 

#### Running

The generated war file can be ran stand-alone or deployed in an application server such as tomcat.

To run stand-alone execute following command:

`java -jar target/vmm-gte-1.0.0-SNAPSHOT.war`

this will start the application, with default empty configuration. If you want to run the application with 
a sample network configuration, you can execute following command:

*Make sure the `sample-data` directory is within the path from which you are running the application.*

`java -jar target/vmm-gte-1.0.0-SNAPSHOT.war --spring.profiles.active=sample`

After starting the application with the sample profile, you should see a bunch of log messages ending with:

`2019-01-11 16:10:21.140  INFO 16489 --- [           main] com.geosparc.gte.engine.impl             : Successfully built network from data sources, 83 nodes, 84 edges.`

#### Usage

You can now open http://localhost:8080/swagger-ui.html#/ to go to the swagger page where you can try the webservice.

In the sample-data folder, there is a `_sample-requests.http` file that contains various requests and responses on the sample dataset.

The filters specified in the request follow the CQL standard as implemented in geotools. 
See http://docs.geotools.org/latest/userguide/library/cql/index.html
and http://www.opengeospatial.org/standards/specifications/catalog
for more information.

#### Configuration

Everything is configured via application.yaml or application-SPRING_PROFILE.yaml files. This includes all of the data sources,
their relevant attributes and connections, and the frequency and time that the graph engine will reload the data and rebuild the graph.
See application-sample.yaml for a configuration example with detailed documentation and instructions in the comments.

You may use the SPRING_CONFIG_LOCATION environment variable to refer to your own externalised and custom configuration file in your environment.

#### Server requirements

Because the tracing algorithm loads the topology in memory as a Graph, sufficient memory is needed for the service, 
depending on the size of the network, the configured attributes and their length.

For example: a sample data set containing 400K nodes & 400K edges, with 20 attributes uses about 1.3 GB of memory.


#### License

(c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
Graph Tracing Engine is licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

Graph Tracing Engine uses the following libraries under the [LGPL license](LGPL.md):
- [GeoTools](http://geotools.org/)
- [JGraphT](https://jgrapht.org/)

Graph Tracing Engine uses a number of libraries licensed under the [Apache License Version 2.0]: 
- [Spring](http://www.springsource.org/)
- [Apache Commons IO](http://jakarta.apache.org/commons/)
- [Google Guava](https://github.com/google/guava)
- [Springfox](https://swagger.io/)

