#!/bin/sh

# Get the current directory.
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"


# Copy the site contents from parent.
cp -R ${DIR}/target/site/* "${DIR}/site"


# Copy the site contents from modules
cd ${DIR}

for i in `ls -d xwiki-platform-search-*`
do
    mkdir -p "${DIR}/site/$i"
    cp -R ${DIR}/${i}/target/site/* "${DIR}/site/${i}"
done


