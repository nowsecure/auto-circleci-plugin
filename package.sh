#!/bin/bash

# Helper script to convert
# Fat jar into what appears to be
# a single file executable.
# https://skife.org/java/unix/2011/06/20/really_executable_jars.html

./gradlew build

cat << EOF > nowsecure-auto
#!/bin/sh

exec java -jar \$0 "\$@"
EOF

cat build/libs/nowsecure-auto.jar >> nowsecure-auto
chmod +x nowsecure-auto
