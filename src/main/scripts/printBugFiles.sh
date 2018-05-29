#!/bin/bash
for number in {1..10}
do
    name=$number'_faultyfiles'
    info=$(defects4j info -p Time -b $number)
    echo "$info" | sed  \
    -e '1,/sources/ d' \
    -e '/-----/,$ d' \
    -re 's/[^a-zA-Z]*(.*)/\1/' >> $name
done
exit 0
