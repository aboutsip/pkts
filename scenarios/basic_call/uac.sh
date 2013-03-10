#!/bin/sh
SCRIPT_DIR=$(cd $(dirname $0) && pwd)

rm -f uac_*.log
sipp -p 5060 -sf uac.xml -trace_msg -l 1 -m 1 $*
