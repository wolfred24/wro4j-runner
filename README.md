# wro4j-runner


Command line runner for wro4j

# Building
```
    $ git clone git@github.com:wro4j/wro4j-runner.git
    $ mvn clean install
```

# How to Use wro4j-runner
```
    To use `wro4j-runner`, you need to provide a configuration file (`wro.xml`), a properties file (`wro.properties`), and optionally a `.jshintrc` file for JavaScript linting.

    =======================================
    ## USAGE
    =======================================
    --contextFolder PATH                          : Folder used as a root of the context relative
                                                    resources. By default this is the user current
                                                    folder.
    --destinationFolder PATH                      : Where to store the processed result. By default
                                                    uses the folder named [wro].
    --parallel                                    : Turns on the parallel preProcessing of resources.
                                                    This value is false by default.
    --postProcessors POST_PROCESSOR               : Comma separated list of post-processors
    --targetGroups GROUPS                         : Comma separated value of the group names from
                                                    wro.xml to process. If none is provided, all
                                                    groups will be processed.
    --wroConfigurationFile PATH_TO_WRO_PROPERTIES : The path to the wro.properties file. By default
                                                    the configuration file is searched inse the user
                                                    current folder.
    --wroFile PATH_TO_WRO_XML                     : The path to the wro model file. By default the
                                                    model is searched inse the user current folder.
    -c (--compressor, --preProcessors) COMPRESSOR : Comma separated list of pre-processors
    -i (--ignoreMissingResources)                 : Ignores missing resources
    -m (--minimize)                               : Turns on the minimization by applying compressor
```

### 1. Create `wro.xml`

This XML file defines your resource groups and the resources (JS/CSS files) to process. Example:

```xml
<groups xmlns="http://www.isdc.ro/wro">
    <group name="all">
        <js>/js/script1.js</js>
        <js>/js/script2.js</js>
        <js>/js/**.js</js> 
        <css>/css/style1.css</css>
    </group>
</groups>
```

### 2. Create `wro.properties`

This file configures wro4j options, such as pre-processors and output directories. Example:

```properties
preProcessors=cssUrlRewriting,cssMinJawr,semicolonAppender,jsMin
targetGroups=all
destinationFolder=dist
```

### 3. Create `.jshintrc` (Optional)

If you want to enable JS linting, add a `.jshintrc` file with your linting rules:

```json
{
    "undef": true,
    "unused": true,
    "browser": true
}
```

### 4. Run wro4j-runner

Run the tool from the command line, specifying the configuration files:

```sh
java -jar wro4j-runner.jar --wroFile wro.xml --contextFolder ./src/main/webapp --destinationFolder ./dist --propertiesFile wro.properties
```

Adjust the paths as needed for your project structure.
