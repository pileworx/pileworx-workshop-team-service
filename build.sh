#!/bin/bash
sbt clean
sbt test coverageReport
sbt coverageAggregate
sbt codacyCoverage