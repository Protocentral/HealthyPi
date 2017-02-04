#!/bin/bash

: <<'MAINSTART'

Perform all variables declarations as well as function definition
above this section for clarity, thanks!

MAINSTART

# intro message

if [ $debugmode != "no" ]; then
    if [ $debuguser != "none" ]; then
        gitusername="$debuguser"
    fi
    if [ $debugpoint != "none" ]; then
        gitrepobranch="$debugpoint"
    fi
    newline
    inform "DEBUG MODE ENABLED"
    echo "git user $gitusername and $gitrepobranch branch/tag will be used"
    newline
else
    newline
    echo "This script will install everything needed to use your"
    echo "$productname"
    newline
    if [ "$1" != '-y' ]; then
        inform "Always be careful when running scripts and commands"
        inform "copied from the internet. Ensure they are from a"
        inform "trusted source."
        newline
        echo "If you want to see what this script does before"
        echo "running it, you should run:"
        echo "\curl -sS $GETPOOL/$scriptname"
        newline
    fi
fi