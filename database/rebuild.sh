#!/bin/sh

sudo mysql < ./DROP.sql
sudo mysql < ./DDL.sql
sudo mysql < ./DCL.sql
sudo mysql < ./DML.sql