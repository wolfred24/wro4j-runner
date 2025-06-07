# wro4j-runner
============

Command line runner for wro4j

# Building
============
```
$ git clone git@github.com:wro4j/wro4j-runner.git
$ mvn clean install
```

# How to Use wro4j-runner

To use `wro4j-runner`, you need to provide a configuration file (`wro.xml`), a properties file (`wro.properties`), and optionally a `.jshintrc` file for JavaScript linting.

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
