#!/bin/sh
SCRIPT_DIR=$(cd $(dirname $0) && pwd)

rm -f uas_*.log
sipp -p 5090 -i 127.0.0.1 -sf uas.xml -trace_msg $*
