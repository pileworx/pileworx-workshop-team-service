#!/bin/bash
sbt clean
sbt "set coverageEnabled := true" "set coverageEnabled in common := true" "set coverageEnabled in app := true" "set coverageEnabled in domain := true" "set coverageEnabled in port := true" test coverageReport
sbt coverageAggregate codacyCoverage