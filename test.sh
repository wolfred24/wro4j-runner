JAR_FILE=$(ls target/wro4j-runner-*.jar | grep -v 'sources' | head -n 1)

java -jar "$JAR_FILE" \
    --wroFile src/test/resources/ro/isdc/wro/runner/wro.xml \
    --contextFolder src/test/resources/ro/isdc/wro/runner \
    --preProcessors jsHint