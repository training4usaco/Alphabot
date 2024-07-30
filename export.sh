#!/bin/sh
mvn clean package
export FNAME="$(ls target | grep alpha | head -1)"
scp target/$FNAME opc:~/alphabot
ssh -tt opc tmux send-keys -t 2 C-c C-m
ssh -tt opc tmux send-keys -t 2 java Space -jar Space $FNAME Enter C-m